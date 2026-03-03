package com.yiran.minecraft.gtmqol.functionality

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.gregtechceu.gtceu.GTCEu
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
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack
import com.yiran.minecraft.gtmqol.ModUtils
import com.yiran.minecraft.gtmqol.data.ClientDynamicModelRegisterer
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.client.Minecraft
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.fml.ModList
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
        val simpleMachineDefinition: MachineDefinition,
        val builder: MachineBuilder<*, *>,
        val isGenerator: Boolean
    )

    private val registryData = mutableListOf<MachineEntry>()

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
        if (ModUtils.isDataGen()) return
        if (recipeTypes.isEmpty() || recipeTypes.any { it == GTRecipeTypes.DUMMY_RECIPES }) return

        val allGenerator = recipeTypes.all { it.group == "generator" }
        if (recipeTypes.any { it.group == "generator" } && !allGenerator) return

        val baseRecipeModifier: RecipeModifier = if (!allGenerator) GTRecipeModifiers.OC_PERFECT_SUBTICK
        else perfectGeneratorOverclockingRecipeModifier

        val namespace = registrate.modid
        val modularName = "modular_$simpleMachineName"

        val builder = registrate.multiblock(modularName, ::WorkableElectricMultiblockMachine)
            .rotationState(RotationState.ALL)
            .recipeTypes(*recipeTypes)
            .recipeModifiers(baseRecipeModifier, GTRecipeModifiers.BATCH_MODE)
            .generator(allGenerator)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern { d ->
                FactoryBlockPattern.start()
                    .aisle("XXX", "#X#", "XXX").aisle("XXX", "X#X", "XXX").aisle("XXX", "#S#", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(d.block)))
                    .where(
                        'X',
                        Predicates.blocks(
                            GTBlocks.CASING_STEEL_SOLID.get(),
                            Blocks.GLASS,
                            GTBlocks.CASING_TEMPERED_GLASS.get()
                        )
                            .or(Predicates.autoAbilities(*d.recipeTypes))
                            .or(Predicates.autoAbilities(true, false, false))
                    )
                    .where('#', Predicates.any()).build()
            }

        registryData.add(
            MachineEntry(
                modularName,
                simpleMachineName,
                namespace,
                builder.register(),
                simpleMachineDefinition,
                builder,
                allGenerator
            )
        )
    }

    @JvmStatic
    fun runAllClientSetup() {
        val rm = Minecraft.getInstance().resourceManager
        val langCode = Minecraft.getInstance().languageManager.selected
        val langAccumulator = mutableMapOf<String, JsonObject>()

//        GTCEu.LOGGER.info("[GTMQoL] Starting Modular Multiblock Asset Injection...")

        registryData.forEach { entry ->
            val targetNS = entry.namespace
            val sourceNS = entry.simpleMachineDefinition.id.namespace
            val simpleName = entry.simpleName
            val modName = entry.modularName

            val machineFront = ResourceLocation.tryBuild(sourceNS, "textures/block/machines/$simpleName/overlay_front.png")!!
            val generatorFront = ResourceLocation.tryBuild(sourceNS, "textures/block/generators/$simpleName/overlay_front.png")!!

            val hasMachineFront = rm.getResource(machineFront).isPresent
            val hasGeneratorFront = rm.getResource(generatorFront).isPresent

//            GTCEu.LOGGER.info("[GTMQoL] Processing Machine: $simpleName")
//            GTCEu.LOGGER.info(" - Checking Machine Path: $machineFront -> $hasMachineFront")
//            GTCEu.LOGGER.info(" - Checking Generator Path: $generatorFront -> $hasGeneratorFront")

            val overlayDir = when {
                hasMachineFront -> ResourceLocation.tryBuild(sourceNS, "block/machines/$simpleName")!!
                hasGeneratorFront -> ResourceLocation.tryBuild(sourceNS, "block/generators/$simpleName")!!
                entry.isGenerator -> {
                    val fallback = GTCEu.id("block/multiblock/generator/large_combustion_engine")
//                    GTCEu.LOGGER.info(" - Fallback: Using Generator Fallback -> $fallback")
                    fallback
                }
                else -> {
                    val fallback = GTCEu.id("block/multiblock/implosion_compressor")
//                    GTCEu.LOGGER.info(" - Fallback: Using Machine Fallback -> $fallback")
                    fallback
                }
            }

//            GTCEu.LOGGER.info(" - Final OverlayDir: $overlayDir")

            val baseCasing = GTCEu.id("block/casings/solid/machine_casing_solid_steel")
            entry.builder.model(GTMachineModels.createWorkableCasingMachineModel(baseCasing, overlayDir))

            try {
                entry.builder.generateAssetJsons(null)
//                GTCEu.LOGGER.info(" - Asset JSONs generated successfully for $modName")
            } catch (e: Exception) {
                GTCEu.LOGGER.error(" - FAILED to generate assets for $modName: ${e.message}")
            }

            val itemModel = JsonObject()
            itemModel.addProperty("parent", "$targetNS:block/machine/$modName")
            GTDynamicResourcePack.addItemModel(ResourceLocation.tryBuild(targetNS, modName)!!, itemModel)

            val currentLangJson = langAccumulator.getOrPut(targetNS) { JsonObject() }
            val format = loadRawLang("gtmqol", langCode)["gtmqol.modular_machine.name_format"] ?: "Modular %s"
            val sourceLang = loadRawLang(sourceNS, langCode)
            val sourceFallback = loadRawLang(sourceNS, "en_us")

            val baseName = sourceLang["$sourceNS.$simpleName"] ?: sourceFallback["$sourceNS.$simpleName"]
            ?: simpleName.split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

            val langKey = "block.$targetNS.$modName"
            val langValue = format.replace("%s", baseName)
            currentLangJson.addProperty(langKey, langValue)
//            GTCEu.LOGGER.info(" - Language Entry Added: $langKey -> $langValue")
        }

        langAccumulator.forEach { (ns, json) ->
            val langPath = ResourceLocation.tryBuild(ns, "lang/$langCode.json")!!
            GTDynamicResourcePack.addResource(langPath, json.toString().toByteArray(StandardCharsets.UTF_8))
//            GTCEu.LOGGER.info("[GTMQoL] Language file injected to: $langPath with ${json.size()} entries.")
        }

//        GTCEu.LOGGER.info("[GTMQoL] All Modular assets injected.")
    }

    private fun loadRawLang(modId: String, langCode: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val path = ModList.get().getModFileById(modId)?.file?.findResource("assets", modId, "lang", "$langCode.json")
            ?: return map
        if (Files.exists(path)) {
            try {
                Files.newInputStream(path).use { s ->
                    val json = JsonParser.parseReader(InputStreamReader(s, StandardCharsets.UTF_8)).asJsonObject
                    json.entrySet().forEach { (k, v) -> map[k] = v.asString }
                }
            } catch (e: Exception) {
            }
        }
        return map
    }

    fun registerMachineRecipes(provider: Consumer<FinishedRecipe>) {
        registryData.forEach { entry ->
            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!.recipeBuilder("${entry.namespace}_${entry.simpleName}_convert_to_modular")
                .inputItems(entry.simpleMachineDefinition.item).outputItems(entry.definition.item)
                .circuitMeta(5).EUt(VA[LV].toLong()).duration(200).save(provider)
        }
    }

    init {
        ClientDynamicModelRegisterer.onGTCEuClientSetup(::runAllClientSetup)
    }
}