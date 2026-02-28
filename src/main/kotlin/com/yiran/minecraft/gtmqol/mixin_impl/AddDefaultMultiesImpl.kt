package com.yiran.minecraft.gtmqol.mixin_impl

import com.google.gson.JsonObject
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
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
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object AddDefaultMultiesImpl {
    val nameToSimpleNameMap = hashMapOf<String, String>()
    val nameToMultiblockDefinitionMap = hashMapOf<String, MultiblockMachineDefinition>()

    @JvmStatic
    fun generateMultiblockForSimpleMachine(
        registrate: GTRegistrate,
        simpleMachineName: String,
        recipeType: GTRecipeType
    ) {
        val machineName = "sophisticated_$simpleMachineName"
        val definition = registrate.multiblock(machineName, ::WorkableElectricMultiblockMachine)
            .rotationState(RotationState.ALL)
            .recipeType(recipeType)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern { d ->
                FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(d.block)))
                    .where('X', Predicates.blocks(
                        GTBlocks.CASING_STEEL_SOLID.get(),
                        Blocks.GLASS,
                        GTBlocks.CASING_TEMPERED_GLASS.get()
                    ).or(Predicates.autoAbilities(*d.recipeTypes))
                        .or(Predicates.autoAbilities(true, false, false)))
                    .where('#', Predicates.air())
                    .build()
            }
            .workableCasingModel(
                ResourceLocation.tryBuild("gtceu", "block/casings/solid/machine_casing_solid_steel")!!,
                ResourceLocation.tryBuild("gtceu", "block/machines/$simpleMachineName")!!
            )
            .register()

        nameToSimpleNameMap[machineName] = simpleMachineName
        nameToMultiblockDefinitionMap[machineName] = definition
    }

    @JvmStatic
    fun onAddPackFinders(event: AddPackFindersEvent) {
        if (event.packType == PackType.CLIENT_RESOURCES) {
            val pack = Pack.create(
                "gtmqol_sophisticated_assets",
                Component.literal("GTMQoL Sophisticated Assets"),
                true,
                { SophisticatedPackResources() },
                Pack.Info(Component.literal("Dynamic Assets"), 15, FeatureFlagSet.of()),
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

    class SophisticatedPackResources : PackResources {
        override fun getRootResource(vararg elements: String): IoSupplier<InputStream>? = null

        override fun getResource(type: PackType, location: ResourceLocation): IoSupplier<InputStream>? {
            if (type != PackType.CLIENT_RESOURCES || location.namespace != "gtceu") return null

            val path = location.path
            val name = path.substringAfterLast("/").substringBefore(".json")
            val simpleName = nameToSimpleNameMap[name] ?: return null

            return when {
                path.startsWith("blockstates/") ->
                    IoSupplier { ByteArrayInputStream(generateAllRotationBS(name).toByteArray(StandardCharsets.UTF_8)) }
                path.startsWith("models/item/") ->
                    IoSupplier { ByteArrayInputStream("""{"parent": "gtceu:block/machine/$name"}""".toByteArray(StandardCharsets.UTF_8)) }
                path.startsWith("models/block/machine/") ->
                    IoSupplier { ByteArrayInputStream(generateFullBlockModel(name, simpleName).toByteArray(StandardCharsets.UTF_8)) }
                else -> null
            }
        }

        override fun listResources(packType: PackType, namespace: String, path: String, resourceOutput: PackResources.ResourceOutput) {
            if (packType != PackType.CLIENT_RESOURCES || namespace != "gtceu") return

            if (path.contains("blockstates")) {
                nameToSimpleNameMap.keys.forEach { name ->
                    val loc = ResourceLocation.tryBuild("gtceu", "blockstates/$name.json")!!
                    resourceOutput.accept(loc) { ByteArrayInputStream(generateAllRotationBS(name).toByteArray(StandardCharsets.UTF_8)) }
                }
            }
            if (path.contains("models")) {
                nameToSimpleNameMap.forEach { (name, simpleName) ->
                    val itemLoc = ResourceLocation.tryBuild("gtceu", "models/item/$name.json")!!
                    resourceOutput.accept(itemLoc) { ByteArrayInputStream("""{"parent": "gtceu:block/machine/$name"}""".toByteArray(StandardCharsets.UTF_8)) }

                    val blockLoc = ResourceLocation.tryBuild("gtceu", "models/block/machine/$name.json")!!
                    resourceOutput.accept(blockLoc) { ByteArrayInputStream(generateFullBlockModel(name, simpleName).toByteArray(StandardCharsets.UTF_8)) }
                }
            }
        }

        private fun generateAllRotationBS(name: String): String {
            val root = JsonObject()
            val variants = JsonObject()
            val facings = arrayOf("north", "south", "east", "west", "up", "down")
            val upwards = arrayOf("north", "south", "east", "west")

            for (f in facings) {
                for (u in upwards) {
                    val config = JsonObject()
                    config.addProperty("model", "gtceu:block/machine/$name")

                    // Match logic from assembly_line example
                    when (f) {
                        "north" -> {
                            val z = when(u) { "south" -> 180; "west" -> 90; "east" -> 270; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                        "south" -> {
                            config.addProperty("y", 180)
                            val z = when(u) { "south" -> 180; "west" -> 90; "east" -> 270; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                        "east" -> {
                            config.addProperty("y", 90)
                            val z = when(u) { "south" -> 180; "west" -> 90; "east" -> 270; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                        "west" -> {
                            config.addProperty("y", 270)
                            val z = when(u) { "south" -> 180; "west" -> 90; "east" -> 270; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                        "down" -> {
                            config.addProperty("x", 90)
                            val z = when(u) { "south" -> 180; "west" -> 270; "east" -> 90; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                        "up" -> {
                            config.addProperty("x", 270)
                            val z = when(u) { "north" -> 180; "west" -> 270; "east" -> 90; else -> 0 }
                            if (z != 0) config.addProperty("gtceu:z", z)
                        }
                    }
                    variants.add("facing=$f,upwards_facing=$u", config)
                }
            }
            root.add("variants", variants)
            return root.toString()
        }

        private fun generateFullBlockModel(name: String, simpleName: String): String {
            val baseOverlay = "gtceu:block/machines/$simpleName/overlay_front"
            val casing = "gtceu:block/casings/solid/machine_casing_solid_steel"
            val template = "gtceu:block/machine/template/cube_all/sided"

            val root = JsonObject()
            root.addProperty("parent", "minecraft:block/block")
            root.addProperty("loader", "gtceu:machine")
            root.addProperty("machine", "gtceu:$name")

            val overrides = JsonObject()
            overrides.addProperty("all", casing)
            root.add("texture_overrides", overrides)

            val variants = JsonObject()
            // Mapping statuses to suffixes. Only using _active where safe.
            val statuses = mapOf(
                "idle" to "",
                "suspend" to "",
                "waiting" to "_active",
                "working" to "_active"
            )

            arrayOf(true, false).forEach { formed ->
                statuses.forEach { (status, suffix) ->
                    val model = JsonObject()
                    model.addProperty("parent", template)
                    val tex = JsonObject()
                    tex.addProperty("all", casing)
                    tex.addProperty("overlay_front", "$baseOverlay$suffix")
                    // Note: Skipping emissive layers to avoid purple-black grid on simple machines
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
            if (type == PackType.CLIENT_RESOURCES) setOf("gtceu") else emptySet()
        override fun <T : Any?> getMetadataSection(serializer: MetadataSectionSerializer<T>): T? = null
        override fun packId(): String = "gtmqol_sophisticated_assets"
        override fun close() {}
    }
}