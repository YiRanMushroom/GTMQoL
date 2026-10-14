package com.yiran.minecraft.gtmqol.integration.ae2;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

/**
 * ME Assembler and ME Circuit Slicer with the silicon chips, ported from the v7 {@code QoLRecipeTypes} /
 * {@code QoLMachines} / {@code QoLRecipes}: the slicer cuts wafers into chips and AE2 materials into prints, the
 * assembler turns chip + print + silicon print into processors (more per chip for better wafers) and makes the
 * GTCEu ME parts from AE2 parts. Also the v7 misc AE recipes in GTCEu machines. Only touch this class when AE2 is
 * loaded ({@code GTCEu.Mods.isAE2Loaded()}).
 * <p>
 * Support for AE integration mods are only for 1.21.1, as many of the recipes changed from 1.20 to 1.21
 */
public final class AEProcessing {

    public static GTRecipeTypeEntry ME_ASSEMBLER_RECIPES;
    public static GTRecipeTypeEntry ME_CIRCUIT_SLICER_RECIPES;
    public static MachineEntry<MachineDefinition>[] ME_ASSEMBLER;
    public static MachineEntry<MachineDefinition>[] ME_CIRCUIT_SLICER;

    public static ItemEntry<Item> SILICON_CHIP;
    public static ItemEntry<Item> PHOSPHORUS_DOPED_SILICON_CHIP;
    public static ItemEntry<Item> NAQUADAH_DOPED_SILICON_CHIP;
    public static ItemEntry<Item> NEUTRONIUM_DOPED_SILICON_CHIP;

    private static final TagKey<Item> SILICON = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "silicon"));
    private static final TagKey<Item> FLUIX = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/fluix"));
    private static final TagKey<Item> skyStoneDust = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/sky_stone"));

    private AEProcessing() {
    }

    public static void initItems() {
        SILICON_CHIP = chip("silicon_chip", "Silicon Chip");
        PHOSPHORUS_DOPED_SILICON_CHIP = chip("phosphorus_doped_silicon_chip", "Phosphorus Doped Silicon Chip");
        NAQUADAH_DOPED_SILICON_CHIP = chip("naquadah_doped_silicon_chip", "Naquadah Doped Silicon Chip");
        NEUTRONIUM_DOPED_SILICON_CHIP = chip("neutronium_doped_silicon_chip", "Neutronium Doped Silicon Chip");
    }

    private static ItemEntry<Item> chip(String id, String name) {
        return GTMQoLAddon.registrate().item(id, Item::new).lang(name).register();
    }

    public static void initRecipeTypes() {
        ME_ASSEMBLER_RECIPES = GTMQoLAddon.registrate().recipeType("me_assembler", GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(6, 1, 3, 0)
                .setEUIO(IO.IN)
                .prepareBuilder(builder -> builder.EUt(VA[LV]))
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
                .setSound(GTSoundEntries.ASSEMBLER)
                .lang("ME Assembler")
                .register();

        ME_CIRCUIT_SLICER_RECIPES = GTMQoLAddon.registrate().recipeType("me_circuit_slicer", GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.IN)
                .prepareBuilder(builder -> builder.EUt(VA[LV]))
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CUTTER))
                .setSound(GTSoundEntries.CUT)
                .lang("ME Circuit Slicer")
                .register();
    }

    public static void initMachines() {
        ME_ASSEMBLER = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), "me_assembler",
                ME_ASSEMBLER_RECIPES).register();
        ME_CIRCUIT_SLICER = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), "me_circuit_slicer",
                ME_CIRCUIT_SLICER_RECIPES).register();
    }

    public static void addRecipes(RecipeOutput provider) {
        registerMachineRecipe(provider, ME_ASSEMBLER,
                "ACA", "VMV", "WCW",
                'M', AEBlocks.INSCRIBER.asItem(),
                'V', CONVEYOR,
                'A', ROBOT_ARM,
                'C', CIRCUIT,
                'W', CABLE);

        registerMachineRecipe(provider, ME_CIRCUIT_SLICER,
                "WCG", "VMB", "CWE",
                'M', AEBlocks.INSCRIBER.asItem(),
                'E', MOTOR,
                'V', CONVEYOR,
                'C', CIRCUIT,
                'W', CABLE,
                'G', GLASS,
                'B', SAWBLADE);

        addSlicerRecipes(provider);
        addAssemblerRecipes(provider);
        addMiscRecipes(provider);
    }

    private static void addSlicerRecipes(RecipeOutput provider) {
        slice(provider, "slice_silicon_chip", GTItems.SILICON_WAFER.get(), SILICON_CHIP.get(), 8, 200);
        slice(provider, "slice_phosphorus_doped_silicon_chip", GTItems.PHOSPHORUS_WAFER.get(),
                PHOSPHORUS_DOPED_SILICON_CHIP.get(), 16, 400);
        slice(provider, "slice_naquadah_doped_silicon_chip", GTItems.NAQUADAH_WAFER.get(),
                NAQUADAH_DOPED_SILICON_CHIP.get(), 32, 800);
        slice(provider, "slice_neutronium_doped_silicon_chip", GTItems.NEUTRONIUM_WAFER.get(),
                NEUTRONIUM_DOPED_SILICON_CHIP.get(), 64, 1600);

        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_calculation_circuit"))
                .inputItems(gem, CertusQuartz)
                .outputItems(AEItems.CALCULATION_PROCESSOR_PRINT.asItem(), 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_silicon_circuit"))
                .inputItems(SILICON)
                .outputItems(AEItems.SILICON_PRINT.asItem(), 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_logic_circuit"))
                .inputItems(ingot, Gold)
                .outputItems(AEItems.LOGIC_PROCESSOR_PRINT.asItem(), 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_engineering_circuit"))
                .inputItems(gem, Diamond)
                .outputItems(AEItems.ENGINEERING_PROCESSOR_PRINT.asItem(), 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        if (isExtendedAELoaded()) {
            var entroCrystal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "entro_crystal"));
            var concurrentCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "concurrent_processor_print"));
            if (entroCrystal != Items.AIR && concurrentCircuit != Items.AIR) {
                ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_concurrent_circuit"))
                        .inputItems(entroCrystal)
                        .outputItems(concurrentCircuit, 4)
                        .duration(200)
                        .EUt(VA[LV])
                        .save(provider);
            }
        }

        if (isAdvancedAELoaded()) {
            var quantumAlloy = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_alloy"));
            var quantumCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "printed_quantum_processor"));
            if (quantumAlloy != Items.AIR && quantumCircuit != Items.AIR) {
                ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_quantum_circuit"))
                        .inputItems(quantumAlloy)
                        .outputItems(quantumCircuit, 4)
                        .duration(200)
                        .EUt(VA[LV])
                        .save(provider);
            }
        }

        if (isMegaCellsLoaded()) {
            var skySteelIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "sky_steel_ingot"));
            var accumulationCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "printed_accumulation_processor"));
            if (skySteelIngot != Items.AIR && accumulationCircuit != Items.AIR) {
                ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_accumulation_processor_print"))
                        .inputItems(skySteelIngot)
                        .outputItems(accumulationCircuit, 4)
                        .duration(200)
                        .EUt(VA[LV])
                        .save(provider);
            }
        }

        if (isAppliedFluxLoaded()) {
            var chargedRedstone = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "charged_redstone"));
            var energyCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "printed_energy_processor"));
            if (chargedRedstone != Items.AIR && energyCircuit != Items.AIR) {
                ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id("slice_energy_processor_print"))
                        .inputItems(chargedRedstone)
                        .outputItems(energyCircuit, 4)
                        .duration(200)
                        .EUt(VA[LV])
                        .save(provider);
            }
        }
    }

    private static void slice(RecipeOutput provider, String id, Item wafer, Item chip, int count, int duration) {
        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id(id))
                .inputItems(wafer)
                .outputItems(chip, count)
                .duration(duration)
                .EUt(VA[LV])
                .save(provider);
    }

    private static void addAssemblerRecipes(RecipeOutput provider) {
        Map<Item, Integer> chipToCount = Map.of(
                SILICON_CHIP.get(), 8,
                PHOSPHORUS_DOPED_SILICON_CHIP.get(), 16,
                NAQUADAH_DOPED_SILICON_CHIP.get(), 32,
                NEUTRONIUM_DOPED_SILICON_CHIP.get(), 64);
        Map<Item, Item> printToProcessor = new HashMap<>(Map.of(
                AEItems.CALCULATION_PROCESSOR_PRINT.asItem(), AEItems.CALCULATION_PROCESSOR.asItem(),
                AEItems.LOGIC_PROCESSOR_PRINT.asItem(), AEItems.LOGIC_PROCESSOR.asItem(),
                AEItems.ENGINEERING_PROCESSOR_PRINT.asItem(), AEItems.ENGINEERING_PROCESSOR.asItem()));

        if (isExtendedAELoaded()) {
            var concurrentCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "concurrent_processor_print"));
            var concurrentProcessor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "concurrent_processor"));
            if (concurrentCircuit != Items.AIR && concurrentProcessor != Items.AIR) {
                printToProcessor.put(concurrentCircuit, concurrentProcessor);
            }
        }

        if (isAdvancedAELoaded()) {
            var quantumCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "printed_quantum_processor"));
            var quantumProcessor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_processor"));
            if (quantumCircuit != Items.AIR && quantumProcessor != Items.AIR) {
                printToProcessor.put(quantumCircuit, quantumProcessor);
            }
        }

        if (isMegaCellsLoaded()) {
            var accumulationCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "printed_accumulation_processor"));
            var accumulationProcessor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "accumulation_processor"));
            if (accumulationCircuit != Items.AIR && accumulationProcessor != Items.AIR) {
                printToProcessor.put(accumulationCircuit, accumulationProcessor);
            }
        }

        if (isAppliedFluxLoaded()) {
            var energyCircuit = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "printed_energy_processor"));
            var energyProcessor = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "energy_processor"));
            if (energyCircuit != Items.AIR && energyProcessor != Items.AIR) {
                printToProcessor.put(energyCircuit, energyProcessor);
            }
        }

        chipToCount.forEach((chip, count) -> printToProcessor.forEach((print, processor) -> {
            String id = path(processor) + "_from_" + path(chip);
            ME_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id(id + "_1"))
                    .inputItems(AEItems.SILICON_PRINT.asItem())
                    .inputItems(chip)
                    .inputItems(print)
                    .inputFluids(Redstone, L)
                    .outputItems(processor, count)
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);

            ME_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id(id + "_2"))
                    .inputItems(foil, Copper, 4)
                    .inputItems(chip)
                    .inputItems(print)
                    .inputFluids(Redstone, L)
                    .outputItems(processor, count)
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);
        }));

        // extraProcessor: 4 more processors besides the 4 logic processors, null for none
        record Part(String id, Item aePart, Item extraProcessor, MachineDefinition output, Material plateMaterial) {
        }
        for (Part p : List.of(
                new Part("me_input_bus", AEParts.EXPORT_BUS.asItem(), null, GTAEMachines.ITEM_IMPORT_BUS_ME.get(),
                        Iron),
                new Part("me_output_bus", AEParts.IMPORT_BUS.asItem(), null, GTAEMachines.ITEM_EXPORT_BUS_ME.get(),
                        Iron),
                new Part("me_input_hatch", AEParts.EXPORT_BUS.asItem(), null,
                        GTAEMachines.FLUID_IMPORT_HATCH_ME.get(), Copper),
                new Part("me_output_hatch", AEParts.IMPORT_BUS.asItem(), null,
                        GTAEMachines.FLUID_EXPORT_HATCH_ME.get(), Copper),
                new Part("stocking_input_bus", AEParts.INTERFACE.asItem(), null,
                        GTAEMachines.STOCKING_IMPORT_BUS_ME.get(), Iron),
                new Part("stocking_input_hatch", AEParts.INTERFACE.asItem(), null,
                        GTAEMachines.STOCKING_IMPORT_HATCH_ME.get(), Copper),
                new Part("me_pattern_buffer", AEParts.PATTERN_PROVIDER.asItem(),
                        AEItems.CALCULATION_PROCESSOR.asItem(), GTAEMachines.ME_PATTERN_BUFFER.get(), Steel),
                new Part("me_pattern_buffer_proxy", AEParts.ME_P2P_TUNNEL.asItem(),
                        AEItems.ENGINEERING_PROCESSOR.asItem(), GTAEMachines.ME_PATTERN_BUFFER_PROXY.get(), Steel))) {
            var builder = ME_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id(p.id()));
            if (p.extraProcessor() != null) builder.inputItems(p.extraProcessor(), 4);
            builder.inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 4)
                    .inputItems(p.aePart())
                    .inputItems(plate, p.plateMaterial(), 4)
                    .outputItems(p.output())
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);
        }
    }

    private static void addMiscRecipes(RecipeOutput provider) {
        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder(GTMQoL.id("quartz_fiber_from_nether_quartz"))
                .inputItems(gem, NetherQuartz)
                .outputItems(AEParts.QUARTZ_FIBER.asItem(), 3)
                .EUt(VA[LV])
                .duration(100)
                .save(provider);

        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder(GTMQoL.id("quartz_fiber_from_certus_quartz"))
                .inputItems(gem, CertusQuartz)
                .outputItems(AEParts.QUARTZ_FIBER.asItem(), 3)
                .EUt(VA[LV])
                .duration(100)
                .save(provider);

        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder(GTMQoL.id("ae_fluix_cable"))
                .inputItems(FLUIX)
                .outputItems(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT).asItem(), 4)
                .EUt(VA[LV])
                .duration(160)
                .save(provider);

        GTRecipeTypes.POLARIZER_RECIPES.recipeBuilder(GTMQoL.id("charge_certus_quartz"))
                .inputItems(gem, CertusQuartz)
                .outputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .EUt(VA[LV])
                .duration(80)
                .save(provider);

        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_fluix"))
                .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .inputItems(dust, Redstone)
                .inputItems(gem, NetherQuartz)
                .inputFluids(Water, 100)
                .outputItems(AEItems.FLUIX_CRYSTAL.asItem(), 4)
                .EUt(VA[LV])
                .duration(200)
                .save(provider);

        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTMQoL.id("macerate_fluix"))
                .inputItems(FLUIX)
                .outputItems(AEItems.FLUIX_DUST.asItem())
                .EUt(2)
                .duration(800)
                .save(provider);

        if (isExtendedAELoaded()) {
            var entroDust = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "entro_dust"));
            var entroCrystal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "entro_crystal"));
            var entroIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae", "entro_ingot"));

            if (entroDust != Items.AIR && entroCrystal != Items.AIR) {
                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_entro"))
                        .notConsumable(entroDust)
                        .inputItems(FLUIX)
                        .inputFluids(Water, 10)
                        .outputItems(entroCrystal)
                        .EUt(VA[LV])
                        .duration(20)
                        .save(provider);

                GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTMQoL.id("macerate_entro"))
                        .inputItems(entroCrystal)
                        .outputItems(entroDust)
                        .EUt(2)
                        .duration(800)
                        .save(provider);

                if (entroIngot != Items.AIR) {
                    GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_entro_ingot"))
                            .inputItems(entroDust, 1)
                            .inputItems(dust, Lapis)
                            .inputItems(ingot, Gold)
                            .inputFluids(Water, 100)
                            .outputItems(entroIngot, 2)
                            .EUt(VA[LV])
                            .duration(80)
                            .save(provider);
                }

                if (isExtendedAEPlusLoaded()) {
                    var lattraDust = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae_plus", "lattra_dust"));
                    var lattraCrystal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae_plus", "lattra_crystal"));
                    if (lattraDust != Items.AIR && lattraCrystal != Items.AIR) {
                        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_lattra"))
                                .notConsumable(lattraDust)
                                .inputItems(entroCrystal)
                                .inputFluids(Water, 10)
                                .outputItems(lattraCrystal)
                                .EUt(VA[LV])
                                .duration(20)
                                .save(provider);

                        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_lattra_from_entro"))
                                .inputItems(dust, Redstone)
                                .inputItems(entroCrystal)
                                .inputItems(Items.GHAST_TEAR)
                                .outputItems(lattraCrystal, 4)
                                .inputFluids(Water, 100)
                                .EUt(VA[LV])
                                .duration(20)
                                .save(provider);

                        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTMQoL.id("macerate_lattra"))
                                .inputItems(lattraCrystal)
                                .outputItems(lattraDust)
                                .EUt(2)
                                .duration(800)
                                .save(provider);
                    }
                    var oblivionSingularity = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("extendedae_plus", "oblivion_singularity"));
                    if (oblivionSingularity != Items.AIR) {
                        GTRecipeTypes.IMPLOSION_RECIPES.recipeBuilder(GTMQoL.id("implode_oblivion_singularity"))
                                .inputItems(Items.NETHER_STAR)
                                .inputItems(TagPrefix.block, Netherite)
                                .outputItems(oblivionSingularity, 2)
                                .EUt(VA[UHV])
                                .duration(1)
                                .save(provider);
                    }
                }


            }
        }

        if (isAdvancedAELoaded()) {
            var shatteredSingularity = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "shattered_singularity"));
            if (shatteredSingularity != Items.AIR) {
                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_shattered_singularity"))
                        .inputItems(AEItems.SINGULARITY)
                        .inputItems(dust, EnderPearl)
                        .inputItems(skyStoneDust)
                        .inputFluids(Lava, 100)
                        .outputItems(shatteredSingularity, 4)
                        .EUt(VA[HV])
                        .duration(200)
                        .save(provider);

                var quantumInfusedDust = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_infused_dust"));
                if (quantumInfusedDust != Items.AIR) {
                    GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(GTMQoL.id("macerate_quantum_infused_dust"))
                            .inputItems(shatteredSingularity)
                            .outputItems(quantumInfusedDust)
                            .EUt(2)
                            .duration(800)
                            .save(provider);

                    var quantumInfusionSource = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_infusion_source"));
                    if (quantumInfusionSource != Fluids.EMPTY) {
                        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_quantum_infused_dust"))
                                .inputItems(quantumInfusedDust)
                                .inputFluids(Water, 4000)
                                .outputFluids(new FluidStack(quantumInfusionSource, 1000))
                                .EUt(VA[HV])
                                .duration(200)
                                .save(provider);

                        var quantumAlloy = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_alloy"));
                        if (quantumAlloy != Items.AIR) {
                            GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_quantum_alloy"))
                                    .inputFluids(SizedFluidIngredient.of(quantumInfusionSource, 1000))
                                    .inputItems(TagPrefix.ingot, Copper, 4)
                                    .inputItems(shatteredSingularity, 4)
                                    .inputItems(AEItems.SINGULARITY, 4)
                                    .outputItems(quantumAlloy, 4)
                                    .EUt(VA[HV])
                                    .duration(200)
                                    .save(provider);

                            var quantumAlloyPlate = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("advanced_ae", "quantum_alloy_plate"));
                            if (quantumAlloyPlate != Items.AIR) {
                                GTRecipeTypes.COMPRESSOR_RECIPES.recipeBuilder(GTMQoL.id("press_quantum_alloy_plate"))
                                        .inputItems(quantumAlloy, 4)
                                        .outputItems(quantumAlloyPlate)
                                        .EUt(VA[HV])
                                        .duration(200)
                                        .save(provider);
                            }
                        }
                    }
                }
            }
        }

        if (isMegaCellsLoaded()) {
            var skySteelIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "sky_steel_ingot"));
            var skyBronzeIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("megacells", "sky_bronze_ingot"));
            if (skySteelIngot != Items.AIR && skyBronzeIngot != Items.AIR) {
                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_sky_steel"))
                        .inputItems(TagPrefix.ingot, Iron)
                        .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                        .inputItems(AEBlocks.SKY_STONE_BLOCK)
                        .inputFluids(Lava, 5)
                        .outputItems(skySteelIngot, 4)
                        .EUt(VA[LV])
                        .duration(20)
                        .save(provider);

                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_sky_bronze"))
                        .inputItems(TagPrefix.ingot, Copper)
                        .inputItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                        .inputItems(AEBlocks.SKY_STONE_BLOCK)
                        .inputFluids(Lava, 5)
                        .outputItems(skyBronzeIngot, 4)
                        .EUt(VA[LV])
                        .duration(20)
                        .save(provider);
            }
        }

        if (isAppliedFluxLoaded()) {
            var redstoneCrystal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "redstone_crystal"));
            var chargedRedstone = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "charged_redstone"));
            if (redstoneCrystal != Items.AIR && chargedRedstone != Items.AIR) {
                GTRecipeTypes.POLARIZER_RECIPES.recipeBuilder(GTMQoL.id("charge_redstone_crystal"))
                        .inputItems(redstoneCrystal)
                        .outputItems(chargedRedstone)
                        .EUt(VA[LV])
                        .duration(80)
                        .save(provider);

                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_redstone_crystal"))
                        .inputItems(block, Redstone)
                        .inputItems(FLUIX)
                        .inputItems(dust, Glowstone)
                        .inputFluids(Water, 100)
                        .outputItems(redstoneCrystal, 4)
                        .EUt(VA[LV])
                        .duration(80)
                        .save(provider);
            }

            var insulatingResin = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("appflux", "insulating_resin"));
            if (insulatingResin != Items.AIR) {
                GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("mix_insulating_resin"))
                        .inputItems(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("c", "silicone")))
                        .inputItems(Items.CACTUS)
                        .inputItems(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("c", "dusts/bone")))
                        .inputItems(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("c", "slimeballs")))
                        .inputFluids(Water, 100)
                        .outputItems(insulatingResin, 4)
                        .EUt(VA[LV])
                        .duration(200)
                        .save(provider);
            }
        }
    }


    private static boolean isExtendedAELoaded() {
        return GTCEu.isModLoaded("extendedae");
    }

    private static boolean isExtendedAEPlusLoaded() {
        return GTCEu.isModLoaded("extendedae_plus");
    }

    private static boolean isAdvancedAELoaded() {
        return GTCEu.isModLoaded("advanced_ae");
    }

    private static boolean isMegaCellsLoaded() {
        return GTCEu.isModLoaded("megacells");
    }

    private static boolean isAppliedFluxLoaded() {
        return GTCEu.isModLoaded("appflux");
    }

    private static String path(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }
}
