package com.yiran.minecraft.gtmqol.functionality

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.gregtechceu.gtceu.api.GTValues.LV
import com.gregtechceu.gtceu.api.GTValues.VA
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.yiran.minecraft.gtmqol.ModUtils
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.client.Minecraft
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.resources.IoSupplier
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.function.Consumer

object AddModularMultiblocksLogic {

    data class MachineEntry(
        val modularName: String,
        val simpleName: String,
        val namespace: String,
        val definition: MultiblockMachineDefinition,
        val simpleMachineDefinition: MachineDefinition
    )

    private val registryData = hashMapOf<String, MutableMap<String, MachineEntry>>()

    private val perfectGeneratorOverclockingLogic = OverclockingLogic.create(0.5, 4.0, true)
    private val perfectGeneratorOverclockingRecipeModifier =
        GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(perfectGeneratorOverclockingLogic)

    @JvmStatic
    fun generateMultiblockForSimpleMachine(
        registrate: GTRegistrate,
        simpleMachineName: String,
        recipeTypes: Array<GTRecipeType>,
        simpleMachineDefinition: MachineDefinition
    ) {
        val isDataGen = ModUtils.isDataGen()

        if (isDataGen) {
            return
        }

        if (recipeTypes.isEmpty() || recipeTypes.any {
                it == GTRecipeTypes.DUMMY_RECIPES
            }) {
            return
        }

        // Recipe types must not contain generator, or must all be generator

        val allGenerator = recipeTypes.all { it.group == "generator" }
        val anyGenerator = recipeTypes.any { it.group == "generator" }

        if (anyGenerator && !allGenerator) {
            return
        }

        val baseRecipeModifier: RecipeModifier = if (!allGenerator) GTRecipeModifiers.OC_PERFECT_SUBTICK
        else perfectGeneratorOverclockingRecipeModifier

        val namespace = registrate.modid
        val modularName = "modular_$simpleMachineName"

        val definition = registrate.multiblock(modularName, ::WorkableElectricMultiblockMachine)
            .rotationState(RotationState.ALL)
            .recipeTypes(*recipeTypes)
            .recipeModifiers(baseRecipeModifier, GTRecipeModifiers.BATCH_MODE)
            .generator(allGenerator)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern { d ->
                FactoryBlockPattern.start()
                    .aisle("XXX", "#X#", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "#S#", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(d.block)))
                    .where(
                        'X', Predicates.blocks(
                            GTBlocks.CASING_STEEL_SOLID.get(),
                            Blocks.GLASS,
                            GTBlocks.CASING_TEMPERED_GLASS.get()
                        ).or(Predicates.autoAbilities(*d.recipeTypes))
                            .or(Predicates.autoAbilities(true, false, false))
                    )
                    .where('#', Predicates.any())
                    .build()
            }
            .register()

        registryData.computeIfAbsent(namespace) { hashMapOf() }[modularName] =
            MachineEntry(modularName, simpleMachineName, namespace, definition, simpleMachineDefinition)
    }

    @JvmStatic
    fun onAddPackFinders(event: AddPackFindersEvent) {
        if (event.packType == PackType.CLIENT_RESOURCES) {
            val pack = Pack.create(
                "gtmqol_universal_assets",
                Component.literal("GTMQoL Universal Machine Assets"),
                true,
                { UniversalPackResources() },
                Pack.Info(Component.literal("Dynamic Assets for Universal Modular Machines"), 15, FeatureFlagSet.of()),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                false,
                PackSource.BUILT_IN
            )
            if (pack != null) {
                event.addRepositorySource { it.accept(pack) }
            }
        }
    }

    fun registerMachineRecipes(provider: Consumer<FinishedRecipe>) {
        registryData.values.forEach {
            it.values.forEach { entry ->
                QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!.recipeBuilder("${entry.namespace}_${entry.simpleName}_convert_to_modular")
                    .inputItems(entry.simpleMachineDefinition.item)
                    .outputItems(entry.definition.item)
                    .circuitMeta(5)
                    .EUt(VA[LV].toLong())
                    .duration(200)
                    .save(provider)
            }
        }
    }

    class UniversalPackResources : PackResources {
        override fun getRootResource(vararg elements: String): IoSupplier<InputStream>? = null

        override fun getResource(type: PackType, location: ResourceLocation): IoSupplier<InputStream>? {
            if (type != PackType.CLIENT_RESOURCES) return null

            val namespace = location.namespace
            val entries = registryData[namespace] ?: return null

            val path = location.path
            val name = path.substringAfterLast("/").substringBefore(".json")
            val entry = entries[name] ?: if (path.startsWith("lang/")) entries.values.first() else return null

            return when {
                path.startsWith("lang/") -> {
                    val langCode = path.substringAfter("lang/").substringBefore(".json")
                    IoSupplier {
                        ByteArrayInputStream(
                            generateDynamicLang(namespace, langCode).toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }
                }

                path.startsWith("blockstates/") ->
                    IoSupplier {
                        ByteArrayInputStream(
                            generateAllRotationBS(namespace, name).toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }

                path.startsWith("models/item/") ->
                    IoSupplier {
                        ByteArrayInputStream(
                            """{"parent": "$namespace:block/machine/$name"}""".toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }

                path.startsWith("models/block/machine/") ->
                    IoSupplier {
                        ByteArrayInputStream(
                            generateFullBlockModel(namespace, entry).toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }

                else -> null
            }
        }

        override fun listResources(
            packType: PackType,
            namespace: String,
            path: String,
            resourceOutput: PackResources.ResourceOutput
        ) {
            if (packType != PackType.CLIENT_RESOURCES) return
            val entries = registryData[namespace] ?: return

            if (path == "lang" || path.startsWith("lang/")) {
                val allLangs = mutableSetOf<String>()
                allLangs.addAll(scanLangsForMod("gtmqol"))
                allLangs.addAll(scanLangsForMod(namespace))

                allLangs.forEach { lang ->
                    val loc = ResourceLocation.tryBuild(namespace, "lang/$lang.json")!!
                    resourceOutput.accept(loc) {
                        ByteArrayInputStream(
                            generateDynamicLang(namespace, lang).toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }
                }
            }

            if (path.contains("blockstates")) {
                entries.keys.forEach { name ->
                    val loc = ResourceLocation.tryBuild(namespace, "blockstates/$name.json")!!
                    resourceOutput.accept(loc) {
                        ByteArrayInputStream(
                            generateAllRotationBS(
                                namespace,
                                name
                            ).toByteArray(StandardCharsets.UTF_8)
                        )
                    }
                }
            }
            if (path.contains("models")) {
                entries.values.forEach { entry ->
                    val itemLoc = ResourceLocation.tryBuild(namespace, "models/item/${entry.modularName}.json")!!
                    resourceOutput.accept(itemLoc) {
                        ByteArrayInputStream(
                            """{"parent": "$namespace:block/machine/${entry.modularName}"}""".toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    }

                    val blockLoc =
                        ResourceLocation.tryBuild(namespace, "models/block/machine/${entry.modularName}.json")!!
                    resourceOutput.accept(blockLoc) {
                        ByteArrayInputStream(
                            generateFullBlockModel(
                                namespace,
                                entry
                            ).toByteArray(StandardCharsets.UTF_8)
                        )
                    }
                }
            }
        }

        private fun scanLangsForMod(modId: String): Set<String> {
            val langs = mutableSetOf<String>()
            val modFile = ModList.get().getModFileById(modId)?.file ?: return langs
            val langRoot = modFile.findResource("assets", modId, "lang")
            if (Files.exists(langRoot)) {
                Files.list(langRoot).use { stream ->
                    stream.forEach { p ->
                        val fileName = p.fileName.toString()
                        if (fileName.endsWith(".json")) langs.add(fileName.substringBefore(".json"))
                    }
                }
            }
            return langs
        }

        private fun generateDynamicLang(namespace: String, langCode: String): String {
            val resultLang = JsonObject()
            val entries = registryData[namespace] ?: return "{}"

            val myLang = loadRawLang("gtmqol", langCode)
            val myFallback = loadRawLang("gtmqol", "en_us")
            val formatKey = "gtmqol.modular_machine.name_format"
            val format = myLang[formatKey] ?: myFallback[formatKey] ?: "Modular %s"

            val sourceLang = loadRawLang(namespace, langCode)
            val sourceFallback = loadRawLang(namespace, "en_us")

            entries.values.forEach { entry ->
                val baseKey = "$namespace.${entry.simpleName}"

                val baseNameFromLang = sourceLang[baseKey] ?: sourceFallback[baseKey]

                val baseName = baseNameFromLang ?: entry.simpleName
                    .split("_")
                    .filter { it.isNotEmpty() }
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }.trim()

                resultLang.addProperty("block.$namespace.${entry.modularName}", format.replace("%s", baseName))
            }
            return resultLang.toString()
        }

        private fun loadRawLang(modId: String, langCode: String): Map<String, String> {
            val map = mutableMapOf<String, String>()
            val modFile = ModList.get().getModFileById(modId)?.file ?: return map
            val path = modFile.findResource("assets", modId, "lang", "$langCode.json")
            if (Files.exists(path)) {
                try {
                    Files.newInputStream(path).use { stream ->
                        val json =
                            JsonParser.parseReader(InputStreamReader(stream, StandardCharsets.UTF_8)).asJsonObject
                        json.entrySet().forEach { (k, v) -> map[k] = v.asString }
                    }
                } catch (e: Exception) {
                }
            }
            return map
        }

        private fun generateAllRotationBS(namespace: String, name: String): String {
            val root = JsonObject()
            val variants = JsonObject()
            val facings = arrayOf("north", "south", "east", "west", "up", "down")
            val upwards = arrayOf("north", "south", "east", "west")
            for (f in facings) {
                for (u in upwards) {
                    val config = JsonObject()
                    config.addProperty("model", "$namespace:block/machine/$name")
                    when (f) {
                        "south" -> config.addProperty("y", 180)
                        "east" -> config.addProperty("y", 90)
                        "west" -> config.addProperty("y", 270)
                        "down" -> config.addProperty("x", 90)
                        "up" -> config.addProperty("x", 270)
                    }
                    val z = when (f) {
                        "north", "south", "east", "west" -> when (u) {
                            "south" -> 180; "west" -> 90; "east" -> 270; else -> 0
                        }

                        "down" -> when (u) {
                            "south" -> 180; "east" -> 90; "west" -> 270; else -> 0
                        }

                        "up" -> when (u) {
                            "north" -> 180; "east" -> 90; "west" -> 270; else -> 0
                        }

                        else -> 0
                    }
                    if (z != 0) config.addProperty("gtceu:z", z)
                    variants.add("facing=$f,upwards_facing=$u", config)
                }
            }
            root.add("variants", variants)
            return root.toString()
        }

        private fun generateFullBlockModel(namespace: String, entry: MachineEntry): String {
            val modid = entry.namespace
            val name = entry.simpleName
            val resourceManager = Minecraft.getInstance().resourceManager

            val frontPath = ResourceLocation.tryBuild(modid, "textures/block/machines/$name/overlay_front.png")!!
            val hasOverlay = resourceManager.getResource(frontPath).isPresent

            val finalBaseOverlay = if (hasOverlay) {
                "$modid:block/machines/$name/overlay_front"
            } else {
                "gtceu:block/machines/assembler/overlay_front"
            }

            val casing = "gtceu:block/casings/solid/machine_casing_solid_steel"
            val template = "gtceu:block/machine/template/cube_all/sided"

            val root = JsonObject()
            root.addProperty("parent", "minecraft:block/block")
            root.addProperty("loader", "gtceu:machine")
            root.addProperty("machine", "$namespace:${entry.modularName}")

            val textureOverrides = JsonObject()
            textureOverrides.addProperty("all", casing)
            root.add("texture_overrides", textureOverrides)

            val variants = JsonObject()
            val statuses = mapOf(
                "idle" to "",
                "suspend" to "",
                "waiting" to "_active",
                "working" to "_active"
            )

            listOf(true, false).forEach { formed ->
                statuses.forEach { (status, suffix) ->
                    val model = JsonObject()
                    model.addProperty("parent", template)

                    val tex = JsonObject()
                    tex.addProperty("all", casing)
                    tex.addProperty("overlay_front", "$finalBaseOverlay$suffix")
                    model.add("textures", tex)

                    val config = JsonObject()
                    config.add("model", model)
                    variants.add("is_formed=$formed,recipe_logic_status=$status", config)
                }
            }

            root.add("variants", variants)
            return root.toString()
        }

        override fun getNamespaces(type: PackType): Set<String> =
            if (type == PackType.CLIENT_RESOURCES) registryData.keys else emptySet()

        override fun <T : Any?> getMetadataSection(serializer: MetadataSectionSerializer<T>): T? = null
        override fun packId(): String = "gtmqol_universal_assets"
        override fun close() {}
    }
}