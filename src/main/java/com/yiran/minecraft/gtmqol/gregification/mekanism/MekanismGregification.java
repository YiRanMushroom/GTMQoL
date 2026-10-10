package com.yiran.minecraft.gtmqol.gregification.mekanism;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalStackLike;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.ForeignRecipeConverter;
import com.yiran.minecraft.gtmqol.gregification.MultiblockShape;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackChemicalToObjectRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityCombiner;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

import java.util.List;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Mekanism's machines as 3x3x3 multiblocks taking any part, crafted from the Mek machine with a GT hammer or through
 * the magical assembler. Recipes run at {@code VA[LV]} for as long as Mekanism's machine takes; machines that work
 * every tick without progress (chemical infuser, washer, separator...) get one operation per tick, as in Mekanism.
 * Batch mode and parallels make up the throughput.
 *
 * <p>Left out: Mek smelting (the energized smelter runs furnace recipes, GT's electric furnace does that already),
 * fission, fusion, SPS, pumps, the nucleosynthesizer, and item to energy/chemical conversion.</p>
 */
public final class MekanismGregification {

    private static final String MEK = "mekanism";
    /** Recipe length of machines that run one operation per tick. */
    private static final int PER_TICK_DURATION = 1;

    private MekanismGregification() {}

    public static List<ForeignMachineType> types() {
        int electric = TileEntityElectricMachine.BASE_TICKS_REQUIRED;
        int advanced = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;
        return List.of(
                type("crusher", "Mek Crusher", MekanismRecipeTypes.TYPE_CRUSHING,
                        (h, b) -> convertItemToItem(h, b, electric),
                        1, 1, 0, 0, 0, 0, GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_MACERATE, mek("crusher")),
                type("enrichment_chamber", "Mek Enrichment Chamber", MekanismRecipeTypes.TYPE_ENRICHING,
                        (h, b) -> convertItemToItem(h, b, electric),
                        1, 1, 0, 0, 0, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS,
                        mek("enrichment_chamber")),
                type("osmium_compressor", "Mek Osmium Compressor", MekanismRecipeTypes.TYPE_COMPRESSING,
                        (h, b) -> convertItemChemical(h, b, advanced),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS,
                        mek("osmium_compressor")),
                type("purification_chamber", "Mek Purification Chamber", MekanismRecipeTypes.TYPE_PURIFYING,
                        (h, b) -> convertItemChemical(h, b, advanced),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("purification_chamber")),
                type("chemical_injection_chamber", "Mek Chemical Injection Chamber",
                        MekanismRecipeTypes.TYPE_INJECTING, (h, b) -> convertItemChemical(h, b, advanced),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_injection_chamber")),
                type("metallurgic_infuser", "Mek Metallurgic Infuser", MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING,
                        (h, b) -> convertItemChemical(h, b, TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.FURNACE, GTGuiTextures.PROGRESS_ARROW,
                        mek("metallurgic_infuser")),
                type("painting_machine", "Mek Painting Machine", MekanismRecipeTypes.TYPE_PAINTING,
                        (h, b) -> convertItemChemical(h, b, TileEntityPaintingMachine.BASE_TICKS_REQUIRED),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("painting_machine")),
                type("chemical_dissolution_chamber", "Mek Chemical Dissolution Chamber",
                        MekanismRecipeTypes.TYPE_DISSOLUTION,
                        (h, b) -> convertItemChemical(h, b, TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED),
                        1, 0, 0, 0, 1, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_dissolution_chamber")),
                type("combiner", "Mek Combiner", MekanismRecipeTypes.TYPE_COMBINING,
                        MekanismGregification::convertCombining,
                        2, 1, 0, 0, 0, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS, mek("combiner")),
                type("precision_sawmill", "Mek Precision Sawmill", MekanismRecipeTypes.TYPE_SAWING,
                        MekanismGregification::convertSawing,
                        1, 2, 0, 0, 0, 0, GTSoundEntries.CUT, GTGuiTextures.PROGRESS_CUTTER, mek("precision_sawmill")),
                type("chemical_crystallizer", "Mek Chemical Crystallizer", MekanismRecipeTypes.TYPE_CRYSTALLIZING,
                        MekanismGregification::convertCrystallizing,
                        0, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_CRYSTALLIZATION,
                        mek("chemical_crystallizer")),
                type("chemical_oxidizer", "Mek Chemical Oxidizer", MekanismRecipeTypes.TYPE_OXIDIZING,
                        (h, b) -> convertItemToChemical(h, b, TileEntityChemicalOxidizer.BASE_TICKS_REQUIRED),
                        1, 0, 0, 0, 0, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_oxidizer")),
                type("pigment_extractor", "Mek Pigment Extractor", MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING,
                        (h, b) -> convertItemToChemical(h, b, TileEntityPigmentExtractor.BASE_TICKS_REQUIRED),
                        1, 0, 0, 0, 0, 1, GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("pigment_extractor")),
                type("pressurized_reaction_chamber", "Mek Pressurized Reaction Chamber",
                        MekanismRecipeTypes.TYPE_REACTION, MekanismGregification::convertReaction,
                        1, 1, 1, 0, 1, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("pressurized_reaction_chamber")),
                // per tick from here on
                type("chemical_infuser", "Mek Chemical Infuser", MekanismRecipeTypes.TYPE_CHEMICAL_INFUSING,
                        MekanismGregification::convertChemicalChemical,
                        0, 0, 0, 0, 2, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("chemical_infuser")),
                type("pigment_mixer", "Mek Pigment Mixer", MekanismRecipeTypes.TYPE_PIGMENT_MIXING,
                        MekanismGregification::convertChemicalChemical,
                        0, 0, 0, 0, 2, 1, GTSoundEntries.MIXER, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("pigment_mixer")),
                type("isotopic_centrifuge", "Mek Isotopic Centrifuge", MekanismRecipeTypes.TYPE_CENTRIFUGING,
                        MekanismGregification::convertChemicalToChemical,
                        0, 0, 0, 0, 1, 1, GTSoundEntries.CENTRIFUGE, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("isotopic_centrifuge")),
                type("solar_neutron_activator", "Mek Solar Neutron Activator", MekanismRecipeTypes.TYPE_ACTIVATING,
                        MekanismGregification::convertChemicalToChemical,
                        0, 0, 0, 0, 1, 1, GTSoundEntries.SCIENCE, GTGuiTextures.PROGRESS_MAGNET,
                        mek("solar_neutron_activator")),
                type("chemical_washer", "Mek Chemical Washer", MekanismRecipeTypes.TYPE_WASHING,
                        MekanismGregification::convertWashing,
                        0, 0, 1, 0, 1, 1, GTSoundEntries.BATH, GTGuiTextures.PROGRESS_ARROW, mek("chemical_washer")),
                type("electrolytic_separator", "Mek Electrolytic Separator", MekanismRecipeTypes.TYPE_SEPARATING,
                        MekanismGregification::convertSeparating,
                        0, 0, 1, 0, 0, 2, GTSoundEntries.ELECTROLYZER, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("electrolytic_separator")),
                // one Mek machine, two directions: the second is crafted from the first so the recipes differ
                type("rotary_condensentrator", "Mek Rotary Condensentrator", MekanismRecipeTypes.TYPE_ROTARY,
                        (h, b) -> convertRotary(h, b, false),
                        0, 0, 0, 1, 1, 0, GTSoundEntries.COOLING, GTGuiTextures.PROGRESS_ARROW,
                        mek("rotary_condensentrator")),
                type("rotary_decondensentrator", "Mek Rotary Decondensentrator", MekanismRecipeTypes.TYPE_ROTARY,
                        (h, b) -> convertRotary(h, b, true),
                        0, 0, 1, 0, 0, 1, GTSoundEntries.BOILER, GTGuiTextures.PROGRESS_ARROW,
                        GTMQoL.id("mek_rotary_condensentrator")),
                type("thermal_evaporation_plant", "Mek Thermal Evaporation Plant",
                        MekanismRecipeTypes.TYPE_EVAPORATING, MekanismGregification::convertEvaporating,
                        0, 0, 1, 1, 0, 0, GTSoundEntries.BOILER, GTGuiTextures.PROGRESS_ARROW,
                        mek("thermal_evaporation_controller")));
    }

    private static ResourceLocation mek(String path) {
        return ResourceLocation.fromNamespaceAndPath(MEK, path);
    }

    private static ForeignMachineType type(String path, String englishName, Supplier<? extends RecipeType<?>> proxy,
                                           ForeignRecipeConverter converter, int itemInputs, int itemOutputs,
                                           int fluidInputs, int fluidOutputs, int chemicalInputs, int chemicalOutputs,
                                           Holder<SoundEntry> sound, ProgressBarTextureSet progressBar,
                                           ResourceLocation counterpart) {
        return ForeignMachineType.multiblock("mek_" + path, englishName, proxy, converter,
                        itemInputs, itemOutputs, fluidInputs, fluidOutputs, sound,
                        builder -> builder.setProgressBar(progressBar), null, counterpart, MultiblockShape.ANY_PARTS)
                .withRecipeType(builder -> {
                    if (chemicalInputs > 0) builder.setMaxSize(IO.IN, ChemicalStackLike.CAP, chemicalInputs);
                    if (chemicalOutputs > 0) builder.setMaxSize(IO.OUT, ChemicalStackLike.CAP, chemicalOutputs);
                    return builder;
                });
    }

    // Mekanism lists one output per possible input; its own recipes all have the same, so the first is taken.

    private static boolean convertItemToItem(RecipeHolder<?> holder, GTRecipeBuilder builder, int ticks) {
        if (!(holder.value() instanceof ItemStackToItemStackRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(recipe.getInput().ingredient());
        builder.outputItems(outputs.getFirst());
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    /** Item + chemical to an item, or to a chemical (dissolution). */
    private static boolean convertItemChemical(RecipeHolder<?> holder, GTRecipeBuilder builder, int ticks) {
        if (!(holder.value() instanceof ItemStackChemicalToObjectRecipe<?> recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        var chemical = recipe.getChemicalInput();
        // all at once instead of every tick
        if (recipe.perTickUsage()) chemical = scale(chemical, ticks);
        builder.inputItems(recipe.getItemInput().ingredient());
        builder.input(ChemicalStackLike.CAP, chemical);
        if (outputs.getFirst() instanceof ItemStack item) {
            builder.outputItems(item);
        } else if (outputs.getFirst() instanceof ChemicalStack output) {
            builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output));
        } else {
            return false;
        }
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    private static boolean convertItemToChemical(RecipeHolder<?> holder, GTRecipeBuilder builder, int ticks) {
        if (!(holder.value() instanceof ItemStackToChemicalRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(recipe.getInput().ingredient());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    private static boolean convertCombining(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof CombinerRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(recipe.getMainInput().ingredient());
        builder.inputItems(recipe.getExtraInput().ingredient());
        builder.outputItems(outputs.getFirst());
        builder.EUt(VA[LV]).duration(TileEntityCombiner.BASE_TICKS_REQUIRED);
        return true;
    }

    private static boolean convertSawing(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof SawmillRecipe recipe) || recipe.isIncomplete()) return false;
        var main = recipe.getMainOutputDefinition();
        var secondary = recipe.getSecondaryOutputDefinition();
        if (main.isEmpty() && secondary.isEmpty()) return false;
        builder.inputItems(recipe.getInput().ingredient());
        if (!main.isEmpty()) builder.outputItems(main.getFirst());
        double chance = recipe.getSecondaryChance();
        if (!secondary.isEmpty() && chance > 0) {
            if (chance >= 1) {
                builder.outputItems(secondary.getFirst());
            } else {
                builder.chancedOutput(secondary.getFirst(),
                        Math.max(1, (int) Math.round(chance * ChanceLogic.getMaxChancedValue())));
            }
        }
        builder.EUt(VA[LV]).duration(TileEntityPrecisionSawmill.BASE_TICKS_REQUIRED);
        return true;
    }

    private static boolean convertCrystallizing(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ChemicalCrystallizerRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getInput());
        builder.outputItems(outputs.getFirst());
        builder.EUt(VA[LV]).duration(TileEntityChemicalCrystallizer.BASE_TICKS_REQUIRED);
        return true;
    }

    private static boolean convertReaction(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof PressurizedReactionRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        var output = outputs.getFirst();
        builder.inputItems(recipe.getInputSolid().ingredient());
        builder.inputFluids(recipe.getInputFluid().ingredient());
        builder.input(ChemicalStackLike.CAP, recipe.getInputChemical());
        if (!output.item().isEmpty()) builder.outputItems(output.item());
        if (!output.chemical().isEmpty()) {
            builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.chemical()));
        }
        builder.EUt(VA[LV]).duration(recipe.getDuration());
        return true;
    }

    private static boolean convertChemicalChemical(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ChemicalChemicalToChemicalRecipe recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getLeftInput());
        builder.input(ChemicalStackLike.CAP, recipe.getRightInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertChemicalToChemical(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ChemicalToChemicalRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertWashing(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof FluidChemicalToChemicalRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputFluids(recipe.getFluidInput().ingredient());
        builder.input(ChemicalStackLike.CAP, recipe.getChemicalInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertSeparating(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ElectrolysisRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        var output = outputs.getFirst();
        builder.inputFluids(recipe.getInput().ingredient());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.left()));
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.right()));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    /** Fluid to chemical when {@code decondensentrating}, chemical to fluid otherwise; recipes may do either or both. */
    private static boolean convertRotary(RecipeHolder<?> holder, GTRecipeBuilder builder, boolean decondensentrating) {
        if (!(holder.value() instanceof RotaryRecipe recipe) || recipe.isIncomplete()) return false;
        if (decondensentrating) {
            if (!recipe.hasFluidToChemical()) return false;
            var outputs = recipe.getChemicalOutputDefinition();
            if (outputs.isEmpty()) return false;
            builder.inputFluids(recipe.getFluidInput().ingredient());
            builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        } else {
            if (!recipe.hasChemicalToFluid()) return false;
            var outputs = recipe.getFluidOutputDefinition();
            if (outputs.isEmpty()) return false;
            builder.input(ChemicalStackLike.CAP, recipe.getChemicalInput());
            builder.outputFluids(outputs.getFirst());
        }
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertEvaporating(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof FluidToFluidRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputFluids(recipe.getInput().ingredient());
        builder.outputFluids(outputs.getFirst());
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static ChemicalStackIngredient scale(ChemicalStackIngredient ingredient, int ticks) {
        return ChemicalStackLike.TYPE.withAmount(ingredient, ingredient.amount() * ticks);
    }
}
