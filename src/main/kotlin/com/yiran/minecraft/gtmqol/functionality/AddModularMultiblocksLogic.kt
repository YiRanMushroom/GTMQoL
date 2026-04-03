package com.yiran.minecraft.gtmqol.functionality

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.LV
import com.gregtechceu.gtceu.api.GTValues.VA
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.MultiblockState
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper
import com.yiran.minecraft.gtmqol.ModUtils
import com.yiran.minecraft.gtmqol.common.multiblocks.SingleHatchTierSkippingWorkableElectricMachine
import com.yiran.minecraft.gtmqol.data.ClientDynamicModelRegisterer
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.client.Minecraft
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.fml.ModList
import org.apache.commons.lang3.ArrayUtils
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.max

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

        val startModifier = RecipeModifier {
            _, _ ->
            ModifierFunction.builder().durationMultiplier(if (allGenerator) 8.0 else 0.125).build()
        }

        val baseRecipeModifier: RecipeModifier = if (!allGenerator) GTRecipeModifiers.OC_PERFECT_SUBTICK
        else perfectGeneratorOverclockingRecipeModifier

        val namespace = registrate.modid
        val modularName = "modular_$simpleMachineName"

        val builder = registrate.multiblock(modularName, ::SingleHatchTierSkippingWorkableElectricMachine)
            .rotationState(RotationState.ALL)
            .recipeTypes(*recipeTypes)
            .recipeModifiers(startModifier, baseRecipeModifier, GTRecipeModifiers.BATCH_MODE)
            .generator(allGenerator)
            .regressWhenWaiting(!allGenerator)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern { d ->
                FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX").aisle("XXX", "XXX", "XXX").aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(d.block)))
                    .where(
                        'X',
                        Predicates.blocks(
                            GTBlocks.CASING_STEEL_SOLID.get(),
                            Blocks.GLASS,
                            GTBlocks.CASING_TEMPERED_GLASS.get()
                        ).setPreviewCount(100)
                            .or(Predicates.autoAbilities(*d.recipeTypes))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(Predicates.any())
                    )
                    .build()
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
        val format = loadRawLang("gtmqol", langCode)["gtmqol.modular_machine.name_format"] ?: "Modular %s"

        registryData.forEach { entry ->
            val targetNS = entry.namespace
            val sourceNS = entry.simpleMachineDefinition.id.namespace
            val simpleName = entry.simpleName
            val modName = entry.modularName

            val machineFront = ResourceLocation.tryBuild(sourceNS, "textures/block/machines/$simpleName/overlay_front.png")!!
            val generatorFront = ResourceLocation.tryBuild(sourceNS, "textures/block/generators/$simpleName/overlay_front.png")!!

            val overlayDir = when {
                rm.getResource(machineFront).isPresent -> ResourceLocation.tryBuild(sourceNS, "block/machines/$simpleName")!!
                rm.getResource(generatorFront).isPresent -> ResourceLocation.tryBuild(sourceNS, "block/generators/$simpleName")!!
                entry.isGenerator -> GTCEu.id("block/multiblock/generator/large_combustion_engine")
                else -> GTCEu.id("block/multiblock/implosion_compressor")
            }

            val baseCasing = GTCEu.id("block/casings/solid/machine_casing_solid_steel")
            entry.builder.model(GTMachineModels.createWorkableCasingMachineModel(baseCasing, overlayDir))

            try {
                entry.builder.generateAssetJsons(null)
            } catch (e: Exception) {
                GTCEu.LOGGER.error(" - FAILED to generate assets for $modName: ${e.message}")
            }

            val itemModel = JsonObject()
            itemModel.addProperty("parent", "$targetNS:block/machine/$modName")
            ClientDynamicModelRegisterer.registerItemModel(ResourceLocation.tryBuild(targetNS, modName)!!, itemModel)

            val sourceLang = loadRawLang(sourceNS, langCode)
            val sourceFallback = loadRawLang(sourceNS, "en_us")

            val baseName = sourceLang["$sourceNS.$simpleName"] ?: sourceFallback["$sourceNS.$simpleName"]
            ?: simpleName.split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

            val langKey = "block.$targetNS.$modName"
            val langValue = format.replace("%s", baseName)

            ClientDynamicModelRegisterer.addLanguageEntry(targetNS, langCode, langKey, langValue)
        }
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

            VanillaRecipeHelper.addShapedRecipe(provider, "hammer_convert_${entry.namespace}_${entry.simpleName}_to_modular",
                ItemStack(entry.definition.item),
                "h", "M", 'M', entry.simpleMachineDefinition.item
            )
        }
    }

    init {
        ClientDynamicModelRegisterer.onGTCEuClientSetup(::runAllClientSetup)
    }
}