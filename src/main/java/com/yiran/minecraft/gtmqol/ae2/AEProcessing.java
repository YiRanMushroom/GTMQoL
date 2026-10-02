package com.yiran.minecraft.gtmqol.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
 */
public final class AEProcessing {

    public static GTRecipeType ME_ASSEMBLER_RECIPES;
    public static GTRecipeType ME_CIRCUIT_SLICER_RECIPES;
    public static MachineDefinition[] ME_ASSEMBLER;
    public static MachineDefinition[] ME_CIRCUIT_SLICER;

    public static ItemEntry<Item> SILICON_CHIP;
    public static ItemEntry<Item> PHOSPHORUS_DOPED_SILICON_CHIP;
    public static ItemEntry<Item> NAQUADAH_DOPED_SILICON_CHIP;
    public static ItemEntry<Item> NEUTRONIUM_DOPED_SILICON_CHIP;

    private static final TagKey<Item> SILICON = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "silicon"));
    private static final TagKey<Item> FLUIX = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gems/fluix"));

    private AEProcessing() {}

    public static void initItems() {
        SILICON_CHIP = chip("silicon_chip", "Silicon Chip");
        PHOSPHORUS_DOPED_SILICON_CHIP = chip("phosphorus_doped_silicon_chip", "Phosphorus Doped Silicon Chip");
        NAQUADAH_DOPED_SILICON_CHIP = chip("naquadah_doped_silicon_chip", "Naquadah Doped Silicon Chip");
        NEUTRONIUM_DOPED_SILICON_CHIP = chip("neutronium_doped_silicon_chip", "Neutronium Doped Silicon Chip");
    }

    private static ItemEntry<Item> chip(String id, String name) {
        return GTMQoLAddon.registrate().item(id, Item::new).lang(name).register();
    }

    /** From GTCEu's recipe type {@code RegisterEvent}. */
    public static void initRecipeTypes() {
        ME_ASSEMBLER_RECIPES = GTRecipeTypes.register(GTMQoL.id("me_assembler"), GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(6, 1, 3, 0)
                .setEUIO(IO.IN)
                .prepareBuilder(builder -> builder.EUt(VA[LV]))
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
                .setSound(GTSoundEntries.ASSEMBLER);
        GTMQoLAddon.registrate().addRawLang(ME_ASSEMBLER_RECIPES.registryName.toLanguageKey(), "ME Assembler");

        ME_CIRCUIT_SLICER_RECIPES = GTRecipeTypes.register(GTMQoL.id("me_circuit_slicer"), GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.IN)
                .prepareBuilder(builder -> builder.EUt(VA[LV]))
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CUTTER))
                .setSound(GTSoundEntries.CUT);
        GTMQoLAddon.registrate().addRawLang(ME_CIRCUIT_SLICER_RECIPES.registryName.toLanguageKey(),
                "ME Circuit Slicer");
    }

    /** From GTCEu's machine {@code RegisterEvent}. */
    public static void initMachines() {
        ME_ASSEMBLER = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), "me_assembler",
                ME_ASSEMBLER_RECIPES).register();
        ME_CIRCUIT_SLICER = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), "me_circuit_slicer",
                ME_CIRCUIT_SLICER_RECIPES).register();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
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

    private static void addSlicerRecipes(Consumer<FinishedRecipe> provider) {
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
    }

    private static void slice(Consumer<FinishedRecipe> provider, String id, Item wafer, Item chip, int count, int duration) {
        ME_CIRCUIT_SLICER_RECIPES.recipeBuilder(GTMQoL.id(id))
                .inputItems(wafer)
                .outputItems(chip, count)
                .duration(duration)
                .EUt(VA[LV])
                .save(provider);
    }

    private static void addAssemblerRecipes(Consumer<FinishedRecipe> provider) {
        Map<Item, Integer> chipToCount = Map.of(
                SILICON_CHIP.get(), 8,
                PHOSPHORUS_DOPED_SILICON_CHIP.get(), 16,
                NAQUADAH_DOPED_SILICON_CHIP.get(), 32,
                NEUTRONIUM_DOPED_SILICON_CHIP.get(), 64);
        Map<Item, Item> printToProcessor = Map.of(
                AEItems.CALCULATION_PROCESSOR_PRINT.asItem(), AEItems.CALCULATION_PROCESSOR.asItem(),
                AEItems.LOGIC_PROCESSOR_PRINT.asItem(), AEItems.LOGIC_PROCESSOR.asItem(),
                AEItems.ENGINEERING_PROCESSOR_PRINT.asItem(), AEItems.ENGINEERING_PROCESSOR.asItem());

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
        record Part(String id, Item aePart, Item extraProcessor, MachineDefinition output, Material plateMaterial) {}
        for (Part p : List.of(
                new Part("me_input_bus", AEParts.EXPORT_BUS.asItem(), null, GTAEMachines.ITEM_IMPORT_BUS_ME,
                        Iron),
                new Part("me_output_bus", AEParts.IMPORT_BUS.asItem(), null, GTAEMachines.ITEM_EXPORT_BUS_ME,
                        Iron),
                new Part("me_input_hatch", AEParts.EXPORT_BUS.asItem(), null,
                        GTAEMachines.FLUID_IMPORT_HATCH_ME, Copper),
                new Part("me_output_hatch", AEParts.IMPORT_BUS.asItem(), null,
                        GTAEMachines.FLUID_EXPORT_HATCH_ME, Copper),
                new Part("stocking_input_bus", AEParts.INTERFACE.asItem(), null,
                        GTAEMachines.STOCKING_IMPORT_BUS_ME, Iron),
                new Part("stocking_input_hatch", AEParts.INTERFACE.asItem(), null,
                        GTAEMachines.STOCKING_IMPORT_HATCH_ME, Copper),
                new Part("me_pattern_buffer", AEParts.PATTERN_PROVIDER.asItem(),
                        AEItems.CALCULATION_PROCESSOR.asItem(), GTAEMachines.ME_PATTERN_BUFFER, Steel),
                new Part("me_pattern_buffer_proxy", AEParts.ME_P2P_TUNNEL.asItem(),
                        AEItems.ENGINEERING_PROCESSOR.asItem(), GTAEMachines.ME_PATTERN_BUFFER_PROXY, Steel))) {
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

    private static void addMiscRecipes(Consumer<FinishedRecipe> provider) {
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
    }

    private static String path(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }
}
