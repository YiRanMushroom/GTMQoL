package com.yiran.minecraft.gtmqol.data

import appeng.core.definitions.AEBlockEntities
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEBlocks.INSCRIBER
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import com.gregtechceu.gtceu.api.GTValues.LV
import com.gregtechceu.gtceu.api.GTValues.VA
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe
import com.yiran.minecraft.gtmqol.ae2PresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.data.QoLRecipes.WoodEntry
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import java.util.function.Consumer

object QoLRecipes {

    data class WoodEntry(val sapling: Item, val log: Item, val leaves: Item, val name: String)

    @JvmStatic
    @Suppress("Unused")
    fun addWood(sapling: Item, log: Item, leaves: Item, name: String) {
        WOOD_ENTRIES.add(WoodEntry(sapling, log, leaves, name))
    }

    val WOOD_ENTRIES = mutableListOf(
        WoodEntry(Items.OAK_SAPLING, Items.OAK_LOG, Items.OAK_LEAVES, "oak"),
        WoodEntry(Items.SPRUCE_SAPLING, Items.SPRUCE_LOG, Items.SPRUCE_LEAVES, "spruce"),
        WoodEntry(Items.BIRCH_SAPLING, Items.BIRCH_LOG, Items.BIRCH_LEAVES, "birch"),
        WoodEntry(Items.JUNGLE_SAPLING, Items.JUNGLE_LOG, Items.JUNGLE_LEAVES, "jungle"),
        WoodEntry(Items.ACACIA_SAPLING, Items.ACACIA_LOG, Items.ACACIA_LEAVES, "acacia"),
        WoodEntry(Items.DARK_OAK_SAPLING, Items.DARK_OAK_LOG, Items.DARK_OAK_LEAVES, "dark_oak"),
        WoodEntry(Items.MANGROVE_PROPAGULE, Items.MANGROVE_LOG, Items.MANGROVE_LEAVES, "mangrove"),
        WoodEntry(Items.CHERRY_SAPLING, Items.CHERRY_LOG, Items.CHERRY_LEAVES, "cherry"),
        WoodEntry(Items.AZALEA, Items.OAK_LOG, Items.AZALEA_LEAVES, "azalea"),
        WoodEntry(Items.FLOWERING_AZALEA, Items.OAK_LOG, Items.FLOWERING_AZALEA_LEAVES, "flowering_azalea"),
        WoodEntry(Items.CRIMSON_FUNGUS, Items.CRIMSON_STEM, Items.NETHER_WART_BLOCK, "crimson"),
        WoodEntry(Items.WARPED_FUNGUS, Items.WARPED_STEM, Items.WARPED_WART_BLOCK, "warped"),
        WoodEntry(
            GTBlocks.RUBBER_SAPLING.asItem(),
            GTBlocks.RUBBER_LOG.asItem(),
            GTBlocks.RUBBER_LEAVES.asItem(),
            "rubber"
        )
    )

    fun init(provider: Consumer<FinishedRecipe>) {
        if (ConfigHolder.instance.addonConfig.enableGreenhouse) {
            registerGreenhouseMachineRecipes(provider)
            registerTreeGrowingRecipes(WOOD_ENTRIES, provider)
        }

        if (ae2PresentedAndIntegrationEnabled()) {
            registerAEMachineRecipes(provider)
            registerCircuitSlicerRecipes(provider)
            registerMEAssemblerRecipes(provider)
        }
    }

    private fun registerGreenhouseMachineRecipes(provider: Consumer<FinishedRecipe>) {
        registerMachineRecipe(
            provider, QoLMachines.GREENHOUSE, "GGG", "GHG", "PCP",
            'H', HULL,
            'G', GLASS,
            'P', PUMP,
            'C', CIRCUIT
        )
    }

    fun registerTreeGrowingRecipes(woodEntries: List<WoodEntry>, provider: Consumer<FinishedRecipe>) {
        woodEntries.forEach { entry ->
            QoLRecipeTypes.GREEN_HOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_1")
                .notConsumable(entry.sapling)
                .inputFluids(FluidStack(Fluids.WATER, 100))
                .circuitMeta(1)
                .outputItems(ItemStack(entry.sapling, 2))
                .outputItems(ItemStack(entry.log, 4))
                .outputItems(ItemStack(entry.leaves, 4))
                .outputItems(ItemStack(Items.STICK, 4))
                .duration(400)
                .EUt(VA[LV].toLong())
                .save(provider)

            QoLRecipeTypes.GREEN_HOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_2")
                .notConsumable(entry.sapling)
                .inputFluids(FluidStack(Fluids.WATER, 500))
                .circuitMeta(2)
                .outputItems(ItemStack(entry.sapling, 20))
                .outputItems(ItemStack(entry.log, 40))
                .outputItems(ItemStack(entry.leaves, 40))
                .outputItems(ItemStack(Items.STICK, 40))
                .duration(4000)
                .EUt(VA[LV].toLong())
                .save(provider)

            QoLRecipeTypes.GREEN_HOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_3")
                .notConsumable(entry.sapling)
                .notConsumableFluid(FluidStack(Fluids.WATER, 1000))
                .circuitMeta(3)
                .outputItems(ItemStack(entry.sapling, 2))
                .outputItems(ItemStack(entry.log, 4))
                .outputItems(ItemStack(entry.leaves, 4))
                .outputItems(ItemStack(Items.STICK, 4))
                .duration(600)
                .EUt(VA[LV].toLong())
                .save(provider)
        }
    }

    fun registerAEMachineRecipes(provider: Consumer<FinishedRecipe>) {
        registerMachineRecipe(
            provider, QoLMachines.ME_ASSEMBLER,
            "ACA", "VMV", "WCW",
            'M', INSCRIBER.asItem(),
            'V', CONVEYOR,
            'A', ROBOT_ARM,
            'C', CIRCUIT,
            'W', CABLE
        );

        registerMachineRecipe(
            provider, QoLMachines.ME_CIRCUIT_SLICER, "WCG", "VMB", "CWE",
            'M', INSCRIBER.asItem(),
            'E', MOTOR,
            'V', CONVEYOR,
            'C', CIRCUIT,
            'W', CABLE,
            'G', GLASS,
            'B', SAWBLADE
        )
    }

    fun registerMEAssemblerRecipes(provider: Consumer<FinishedRecipe>) {
        val siliconChipsToMultiplier = listOf(
            QoLItems.SILICON_CHIP.get() to 8,
            QoLItems.PHOSPHORUS_DOPED_SILICON_CHIP.get() to 16,
            QoLItems.NAQUADAH_DOPED_SILICON_CHIP.get() to 32,
            QoLItems.NEUTRONIUM_DOPED_SILICON_CHIP.get() to 64
        )

        val printToProcessor = listOf(
            AEItems.CALCULATION_PROCESSOR_PRINT.asItem() to AEItems.CALCULATION_PROCESSOR.asItem(),
            AEItems.LOGIC_PROCESSOR_PRINT.asItem() to AEItems.LOGIC_PROCESSOR.asItem(),
            AEItems.ENGINEERING_PROCESSOR_PRINT.asItem() to AEItems.ENGINEERING_PROCESSOR.asItem()
        )

        siliconChipsToMultiplier.forEach { (siliconChip, multiplier) ->
            printToProcessor.forEach { (print, processor) ->
                QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!.recipeBuilder("${processor.descriptionId}_from_${siliconChip.descriptionId}_1")
                    .inputItems(AEItems.SILICON_PRINT.asItem())
                    .inputItems(siliconChip)
                    .inputItems(print)
                    .inputFluids(GTMaterials.Redstone, 144)
                    .outputItems(processor, multiplier)
                    .duration(200)
                    .EUt(VA[LV].toLong())
                    .save(provider)

                QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!.recipeBuilder("${processor.descriptionId}_from_${siliconChip.descriptionId}_1")
                    .inputItems(TagPrefix.foil, GTMaterials.Copper, 4)
                    .inputItems(siliconChip)
                    .inputItems(print)
                    .inputFluids(GTMaterials.Redstone, 144)
                    .outputItems(processor, multiplier)
                    .duration(200)
                    .EUt(VA[LV].toLong())
                    .save(provider)
            }
        }

        val builder = { string: String -> QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!.recipeBuilder(string) }
        builder("me_input_bus")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.EXPORT_BUS.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
            .outputItems(GTAEMachines.ITEM_IMPORT_BUS_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("me_output_bus")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.IMPORT_BUS.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
            .outputItems(GTAEMachines.ITEM_EXPORT_BUS_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("me_input_hatch")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.EXPORT_BUS.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Copper, 4)
            .outputItems(GTAEMachines.FLUID_IMPORT_HATCH_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("me_output_hatch")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.IMPORT_BUS.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Copper, 4)
            .outputItems(GTAEMachines.FLUID_EXPORT_HATCH_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("stocking_input_bus")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.INTERFACE.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
            .outputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("stocking_input_hatch")
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.INTERFACE.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Copper, 4)
            .outputItems(GTAEMachines.STOCKING_IMPORT_HATCH_ME)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("me_pattern_buffer")
            .inputItems(AEItems.CALCULATION_PROCESSOR.asItem(), 4)
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.PATTERN_PROVIDER.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
            .outputItems(GTAEMachines.ME_PATTERN_BUFFER)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("me_pattern_buffer_proxy")
            .inputItems(AEItems.ENGINEERING_PROCESSOR.asItem(), 4)
            .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
            .inputItems(AEParts.ME_P2P_TUNNEL.asItem())
            .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
            .outputItems(GTAEMachines.ME_PATTERN_BUFFER_PROXY)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)
    }

    fun registerCircuitSlicerRecipes(provider: Consumer<FinishedRecipe>) {
        val builder = { string: String -> QoLRecipeTypes.ME_CIRCUIT_SLICER_RECIPES!!.recipeBuilder(string) }
        builder("slice_silicon_chip")
            .inputItems(GTItems.SILICON_WAFER)
            .outputItems(QoLItems.SILICON_CHIP, 8)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_phosphorus_doped_silicon_chip")
            .inputItems(GTItems.PHOSPHORUS_WAFER)
            .outputItems(QoLItems.PHOSPHORUS_DOPED_SILICON_CHIP, 16)
            .duration(400)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_naquadah_doped_silicon_chip")
            .inputItems(GTItems.NAQUADAH_WAFER)
            .outputItems(QoLItems.NAQUADAH_DOPED_SILICON_CHIP, 32)
            .duration(800)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_neutronium_doped_silicon_chip")
            .inputItems(GTItems.NEUTRONIUM_WAFER)
            .outputItems(QoLItems.NEUTRONIUM_DOPED_SILICON_CHIP, 64)
            .duration(1600)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_calculation_circuit")
            .inputItems(
                ItemTags.create(
                    ResourceLocation.tryBuild("forge", "gems/certus_quartz")!!
                )
            )
            .outputItems(AEItems.CALCULATION_PROCESSOR_PRINT.asItem(), 4)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_silicon_circuit")
            .inputItems(
                ItemTags.create(
                    ResourceLocation.tryBuild("forge", "silicon")!!
                )
            )
            .outputItems(AEItems.SILICON_PRINT.asItem(), 4)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_logic_circuit")
            .inputItems(TagPrefix.ingot, GTMaterials.Gold)
            .outputItems(AEItems.LOGIC_PROCESSOR_PRINT.asItem(), 4)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_engineering_circuit")
            .inputItems(TagPrefix.gem, GTMaterials.Diamond)
            .outputItems(AEItems.ENGINEERING_PROCESSOR_PRINT.asItem(), 4)
            .duration(200)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_iron_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Iron)
            .outputItems(AEBlocks.SPATIAL_ANCHOR.asItem(), 4)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_steel_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Steel)
            .outputItems(AEBlocks.SPATIAL_ANCHOR.asItem(), 8)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_aluminum_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Aluminium)
            .outputItems(AEBlocks.SPATIAL_ANCHOR.asItem(), 16)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_titanium_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Titanium)
            .outputItems(AEBlocks.SPATIAL_ANCHOR.asItem(), 32)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_tungsten_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Tungsten)
            .outputItems(AEBlocks.SPATIAL_ANCHOR.asItem(), 64)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)
    }
}