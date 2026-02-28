package com.yiran.minecraft.gtmqol.mixin_impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.gregtechceu.gtceu.GTCEu
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
    val nameToMultiblockDefinitionMap = hashMapOf<String, MultiblockMachineDefinition>()

    @JvmStatic
    fun generateMultiblockForSimpleMachine(
        registrate: GTRegistrate,
        simpleMachineName: String,
        recipeType: GTRecipeType
    ) {
        val machineName = "sophisticated_$simpleMachineName"
        val definition = registrate.multiblock(
            machineName,
            ::WorkableElectricMultiblockMachine
        ).rotationState(RotationState.NON_Y_AXIS)
            .recipeType(recipeType)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern { d ->
                FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(d.block)))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get(), Blocks.GLASS, GTBlocks.CASING_TEMPERED_GLASS.get())
                        .or(Predicates.autoAbilities(*d.recipeTypes))
                        .or(Predicates.autoAbilities(true, false, false)))
                    .where('#', Predicates.air())
                    .build()
            }
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                GTCEu.id("block/multiblock/primitive_blast_furnace"))
            .register()

        nameToMultiblockDefinitionMap[machineName] = definition
        System.out.println("[GTMQoL-DEBUG] MACHINE_REG: $machineName")
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
                System.out.println("[GTMQoL-DEBUG] PACK_INJECT_SUCCESS")
                event.addRepositorySource { it.accept(pack) }
            }
        }
    }

    class SophisticatedPackResources : PackResources {
        private val steelCasingTex = "gtceu:block/casings/solid/machine_casing_solid_steel"

        override fun getRootResource(vararg elements: String): IoSupplier<InputStream>? = null

        override fun getResource(type: PackType, location: ResourceLocation): IoSupplier<InputStream>? {
            if (type != PackType.CLIENT_RESOURCES || location.namespace != "gtceu") return null

            val path = location.path
            System.out.println("[GTMQoL-DEBUG] GET_RESOURCE: $path")

            val name = path.substringAfterLast("/").substringBefore(".json")
            if (!nameToMultiblockDefinitionMap.containsKey(name)) return null

            if (path.startsWith("blockstates/")) {
                System.out.println("[GTMQoL-DEBUG] PROVIDING_BS: $name")
                return IoSupplier { ByteArrayInputStream(generateMultipartBS(name).toByteArray(StandardCharsets.UTF_8)) }
            }

            if (path.startsWith("models/item/")) {
                System.out.println("[GTMQoL-DEBUG] PROVIDING_ITEM: $name")
                val json = """{"parent": "gtceu:block/$name"}"""
                return IoSupplier { ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8)) }
            }

            if (path.startsWith("models/block/")) {
                System.out.println("[GTMQoL-DEBUG] PROVIDING_BLOCK_MODEL: $name")
                val overlayTex = "gtceu:block/multiblock/overlays/${name.substringAfter("sophisticated_")}"
                return IoSupplier { ByteArrayInputStream(generateCompositeModel(overlayTex).toByteArray(StandardCharsets.UTF_8)) }
            }

            return null
        }

        override fun listResources(packType: PackType, namespace: String, path: String, resourceOutput: PackResources.ResourceOutput) {
            if (packType != PackType.CLIENT_RESOURCES || namespace != "gtceu") return

            System.out.println("[GTMQoL-DEBUG] LIST_RESOURCES_SCAN: $path")

            if (path.contains("blockstates")) {
                nameToMultiblockDefinitionMap.keys.forEach { name ->
                    val loc = ResourceLocation.tryBuild("gtceu", "blockstates/$name.json")
                    System.out.println("[GTMQoL-DEBUG] LIST_ACCEPT_BS: $loc")
                    resourceOutput.accept(loc) { ByteArrayInputStream(generateMultipartBS(name).toByteArray(StandardCharsets.UTF_8)) }
                }
            }

            if (path.contains("models")) {
                nameToMultiblockDefinitionMap.keys.forEach { name ->
                    val itemLoc = ResourceLocation.tryBuild("gtceu", "models/item/$name.json")
                    System.out.println("[GTMQoL-DEBUG] LIST_ACCEPT_ITEM: $itemLoc")
                    resourceOutput.accept(itemLoc) { ByteArrayInputStream("""{"parent": "gtceu:block/$name"}""".toByteArray(StandardCharsets.UTF_8)) }

                    val blockLoc = ResourceLocation.tryBuild("gtceu", "models/block/$name.json")
                    System.out.println("[GTMQoL-DEBUG] LIST_ACCEPT_BLOCK_MODEL: $blockLoc")
                    val overlayTex = "gtceu:block/multiblock/overlays/${name.substringAfter("sophisticated_")}"
                    resourceOutput.accept(blockLoc) { ByteArrayInputStream(generateCompositeModel(overlayTex).toByteArray(StandardCharsets.UTF_8)) }
                }
            }
        }

        private fun generateMultipartBS(name: String): String {
            val root = JsonObject()
            val multipart = JsonArray()
            val hDirs = arrayOf("north", "south", "east", "west")
            for (dir in hDirs) {
                val part = JsonObject()
                val whenObj = JsonObject()
                whenObj.addProperty("facing", dir)
                part.add("when", whenObj)
                val apply = JsonObject()
                apply.addProperty("model", "gtceu:block/$name")
                val y = when (dir) {
                    "south" -> 180
                    "west" -> 270
                    "east" -> 90
                    else -> 0
                }
                if (y != 0) apply.addProperty("y", y)
                part.add("apply", apply)
                multipart.add(part)
            }
            root.add("multipart", multipart)
            return root.toString()
        }

        private fun generateCompositeModel(overlayTex: String): String {
            return """
            {
              "parent": "minecraft:block/block",
              "textures": {
                "particle": "$steelCasingTex",
                "overlay": "$overlayTex"
              },
              "elements": [
                {
                  "from": [ 0, 0, -0.01 ],
                  "to": [ 16, 16, 0.01 ],
                  "faces": {
                    "north": { "texture": "#overlay" }
                  }
                }
              ]
            }
            """.trimIndent()
        }

        override fun getNamespaces(type: PackType): Set<String> {
            return if (type == PackType.CLIENT_RESOURCES) setOf("gtceu") else emptySet()
        }

        override fun <T : Any?> getMetadataSection(serializer: MetadataSectionSerializer<T>): T? = null
        override fun packId(): String = "gtmqol_sophisticated_assets"
        override fun close() {}
    }
}