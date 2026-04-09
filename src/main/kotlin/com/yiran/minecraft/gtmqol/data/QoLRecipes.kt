package com.yiran.minecraft.gtmqol.data

import appeng.api.util.AEColor
import appeng.core.definitions.AEBlocks.INSCRIBER
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines
import com.gregtechceu.gtceu.data.recipe.CustomTags
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe
import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull
import com.yiran.minecraft.gtmqol.ae2PresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.common.item.StickyCardItem
import com.yiran.minecraft.gtmqol.common.multiblocks.PCBFactoryMachine
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.functionality.AddModularMultiblocksLogic
import com.yiran.minecraft.gtmqol.gtmthingsPresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.logic.GreenhouseRecipeLogic
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import java.util.function.Consumer


object QoLRecipes {

    data class WoodEntry(val sapling: Item, val log: Item, val leaves: Item, val name: String)

    fun init(provider: Consumer<FinishedRecipe>) {
        if (ConfigHolder.instance.addonConfig.enableGreenhouse) {
            registerGreenhouseMachineRecipes(provider)
            GreenhouseRecipeLogic.init(provider)
        }

        if (ae2PresentedAndIntegrationEnabled()) {
            registerAEMachineRecipes(provider)
            registerCircuitSlicerRecipes(provider)
            registerMEAssemblerRecipes(provider)
            registerMiscAERecipes(provider)

            if (StickyCardItem.shouldAct()) {
                QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!.recipeBuilder("qol_sticky_card")
                    .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 2)
                    .inputItems(Items.SLIME_BALL)
                    .inputItems(AEItems.ADVANCED_CARD.asItem())
                    .outputItems(QoLItems.STICKY_CARD_ITEM.get())
                    .duration(20)
                    .EUt(VA[LV].toLong())
                    .save(provider)

                VanillaRecipeHelper.addShapedRecipe(
                    provider, "qol_sticky_card_craft", ItemStack(QoLItems.STICKY_CARD_ITEM.get()),
                    " d ", " S ", "LCL",
                    'S', Items.SLIME_BALL,
                    'L', AEItems.LOGIC_PROCESSOR.asItem(),
                    'C', AEItems.ADVANCED_CARD.asItem()
                )
            }
        }

        registerMiscRecipes(provider)

        if (gtmthingsPresentedAndIntegrationEnabled()) {
            GTMRecipeGen.initGTMRecipes(provider)
        }

        if (ConfigHolder.instance.addonConfig.enableHigherAmpLaserHatches) {
            GTMRecipeGen.overrideLaserRecipes(provider)
        }

        if (ConfigHolder.instance.addonConfig.enablePCBFactory) {
            PCBFactoryMachine.registerDefaultRecipes(provider)
        }
    }

    private fun registerMiscAERecipes(provider: Consumer<FinishedRecipe>) {
        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder("gtmqol:quartz_fiber_from_nether_quartz")
            .inputItems(TagPrefix.gem, GTMaterials.NetherQuartz)
            .outputItems(AEParts.QUARTZ_FIBER.asItem(), 3)
            .EUt(VA[LV].toLong())
            .duration(100)
            .save(provider)

        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder("gtmqol:quartz_fiber_from_certus_quartz")
            .inputItems(TagPrefix.gem, GTMaterials.CertusQuartz)
            .outputItems(AEParts.QUARTZ_FIBER.asItem(), 3)
            .EUt(VA[LV].toLong())
            .duration(100)
            .save(provider)

        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder("gtmqol:ae_fluix_cable")
            .inputItems(ItemTags.create(ResourceLocation.tryBuild("forge", "gems/fluix")!!))
            .outputItems(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT).asItem(), 4)
            .EUt(VA[LV].toLong())
            .duration(160)
            .save(provider)

        GTRecipeTypes.POLARIZER_RECIPES.recipeBuilder("gtmqol:charge_certus_quartz")
            .inputItems(TagPrefix.gem, GTMaterials.CertusQuartz)
            .outputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
            .EUt(VA[LV].toLong())
            .duration(80)
            .save(provider)

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder("gtmqol:mix_fluix")
            .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
            .inputItems(TagPrefix.dust, GTMaterials.Redstone)
            .inputItems(TagPrefix.gem, GTMaterials.NetherQuartz)
            .inputFluids(GTMaterials.Water, 100)
            .outputItems(AEItems.FLUIX_CRYSTAL.asItem(), 4)
            .EUt(VA[LV].toLong())
            .duration(200)
            .save(provider)

    }

    private fun registerMiscRecipes(provider: Consumer<FinishedRecipe>) {
        if (ConfigHolder.instance.addonConfig.enableResourceGenerationRecipes) {
            registerResourceGenerationRecipes(provider)
        }

        registerMachineRecipe(
            provider, QoLMachines.MAGICAL_ASSEMBLER,
            "PGP", "GMG", "PCP",
            'M', HULL,
            'G', Items.GLASS,
            'C', CIRCUIT,
            'P', PLATE
        )

        QoLItems.circuitTiers().forEach {
            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!.recipeBuilder("gtmqol:circuit_conversion_tier_${it}")
                .inputItems(CustomTags.CIRCUITS_ARRAY[it])
                .outputItems(QoLItems.UNIVERSAL_CIRCUITS[it])
                .circuitMeta(5)
                .duration(1)
                .EUt(1)
                .save(provider)
        }

        if (ConfigHolder.instance.addonConfig.registerModularMachinesForSimpleMachines) {
            AddModularMultiblocksLogic.registerMachineRecipes(provider)
        }

        if (ConfigHolder.instance.addonConfig.enableElectricImplosionRecipes) {
            VanillaRecipeHelper.addShapedRecipe(
                provider, true, "gtmqol:electric_implosion_compressor",
                QoLMultiblocks.ELECTRIC_IMPLOSION_COMPRESSOR!!.asStack(),
                "PCP", "FSF", "PCP",
                'C', CustomTags.ZPM_CIRCUITS,
                'S', GTMultiMachines.IMPLOSION_COMPRESSOR.asStack(),
                'P', GTItems.ELECTRIC_MOTOR_IV.asStack(),
                'F', GTItems.FIELD_GENERATOR_IV
            )
        }

        if (ConfigHolder.instance.addonConfig.enableSmartAssemblyFactory) {

            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!.recipeBuilder("gtmqol:smart_assembly_factory")
                .inputItems(GTMultiMachines.ASSEMBLY_LINE.asStack(16))
                .inputItems(CustomTags.UV_CIRCUITS, 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Osmiridium, 64)
                .inputItems(GTItems.FIELD_GENERATOR_ZPM, 16)
                .inputItems(GTItems.ELECTRIC_PUMP_UV, 8)
                .inputItems(GTItems.ROBOT_ARM_UV, 8)
                .inputFluids(GTMaterials.Europium, 144 * 64)
                .inputFluids(GTMaterials.Polybenzimidazole, 144 * 32)
                .inputFluids(GTMaterials.Naquadria, 144 * 16)
                .outputItems(QoLMultiblocks.SMART_ASSEMBLY_FACTORY!!.asStack())
                .duration(2000)
                .EUt(VA[ZPM].toLong())
                .save(provider)
        }

        if (ConfigHolder.instance.addonConfig.enableDimensionallyTranscendentFusionReactor) {

            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!.recipeBuilder("gtmqol:dimensionally_transcendent_fusion_reactor")
                .inputItems(GTMultiMachines.FUSION_REACTOR[UV].asStack(16))
                .inputItems(CustomTags.UHV_CIRCUITS, 8)
                .inputItems(TagPrefix.plateDouble, GTMaterials.Tritanium, 64)
                .inputItems(GTItems.FIELD_GENERATOR_UV, 16)
                .inputItems(GTBlocks.FUSION_COIL, 64)
                .inputItems(GTBlocks.FUSION_CASING_MK3, 64)
                .inputFluids(GTMaterials.Duranium, 144 * 64)
                .inputFluids(GTMaterials.Polybenzimidazole, 144 * 32)
                .inputFluids(GTMaterials.Neutronium, 144 * 16)
                .outputItems(QoLMultiblocks.DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR!!.asStack())
                .duration(2000)
                .EUt(VA[UV].toLong())
                .save(provider)
        }

        if (ConfigHolder.instance.addonConfig.enableMachinePartModifiers) {
            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES.asNotNull()
                .recipeBuilder("gtmqol:probable_improbability_device")
                .inputItems(GTMachines.HULL[IV])
                .inputItems(GTItems.FIELD_GENERATOR_EV, 16)
                .inputItems(CustomTags.LuV_CIRCUITS, 64)
                .inputItems(GTItems.ROBOT_ARM_IV, 4)
                .inputFluids(GTMaterials.Polybenzimidazole, 144 * 4)
                .outputItems(QoLMachines.PROBABLE_IMPROBABILITY_DEVICE.asNotNull().asStack())
                .duration(2000)
                .EUt(VA[IV].toLong())
                .save(provider)

            QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES.asNotNull()
                .recipeBuilder("gtmqol:probable_certainty_device")
                .inputItems(GTMachines.HULL[ZPM])
                .inputItems(GTItems.FIELD_GENERATOR_ZPM, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 64)
                .inputItems(GTItems.FLUID_REGULATOR_ZPM, 4)
                .inputFluids(GTMaterials.Naquadria, 144 * 4)
                .outputItems(QoLMachines.PROBABLE_CERTAINTY_DEVICE.asNotNull().asStack())
                .duration(2000)
                .EUt(VA[ZPM].toLong())
                .save(provider)
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
            QoLRecipeTypes.GREENHOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_1")
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

            QoLRecipeTypes.GREENHOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_2")
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

            QoLRecipeTypes.GREENHOUSE_RECIPES!!.recipeBuilder("${entry.name}_growing_3")
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

                QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!.recipeBuilder("${processor.descriptionId}_from_${siliconChip.descriptionId}_2")
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
            .outputItems(AEParts.CABLE_ANCHOR.asItem(), 4)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_steel_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Steel)
            .outputItems(AEParts.CABLE_ANCHOR.asItem(), 8)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_aluminum_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Aluminium)
            .outputItems(AEParts.CABLE_ANCHOR.asItem(), 16)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_titanium_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.Titanium)
            .outputItems(AEParts.CABLE_ANCHOR.asItem(), 32)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

        builder("slice_tungsten_steel_to_cable_anchor")
            .inputItems(TagPrefix.ingot, GTMaterials.TungstenSteel)
            .outputItems(AEParts.CABLE_ANCHOR.asItem(), 64)
            .duration(100)
            .EUt(VA[LV].toLong())
            .save(provider)

    }

    fun registerResourceGenerationRecipes(provider: Consumer<FinishedRecipe>) {
        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(GTMQoL.id("salt_water_from_water"))
            .inputFluids(GTMaterials.Water, 1000)
            .notConsumable(Blocks.SAND.asItem())
            .outputFluids(FluidStack(GTMaterials.SaltWater.fluid, 1000))
            .duration(800 * 20)
            .EUt(VA[ULV].toLong())
            .save(provider)
    }
}