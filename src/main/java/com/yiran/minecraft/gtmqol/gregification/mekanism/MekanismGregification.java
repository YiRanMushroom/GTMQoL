package com.yiran.minecraft.gtmqol.gregification.mekanism;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalStackLike;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.ForeignRecipeConverter;
import com.yiran.minecraft.gtmqol.gregification.MultiblockShape;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.MultiFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.SingleFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.TaggedFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.MultiItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.SingleItemStackIngredient;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;

import java.util.ArrayList;
import java.util.Arrays;
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
    // Mekanism 10.4 passes most of these to the tile's super constructor as literals, so they are repeated here:
    // crusher, enrichment chamber, metallurgic infuser, painting machine, combiner, precision sawmill, chemical
    // crystallizer.
    private static final int ELECTRIC_TICKS = 200;
    // chemical oxidizer, pigment extractor
    private static final int OXIDIZER_TICKS = 100;

    private MekanismGregification() {}

    public static List<ForeignMachineType> types() {
        int advanced = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;
        return List.of(
                type("crusher", "Mek Crusher", MekanismRecipeType.CRUSHING,
                        (r, b) -> convertItemToItem(r, b, ELECTRIC_TICKS),
                        1, 1, 0, 0, 0, 0, GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_MACERATE, mek("crusher")),
                type("enrichment_chamber", "Mek Enrichment Chamber", MekanismRecipeType.ENRICHING,
                        (r, b) -> convertItemToItem(r, b, ELECTRIC_TICKS),
                        1, 1, 0, 0, 0, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS,
                        mek("enrichment_chamber")),
                // the advanced machines use their gas every tick
                type("osmium_compressor", "Mek Osmium Compressor", MekanismRecipeType.COMPRESSING,
                        (r, b) -> convertItemChemicalToItem(r, b, advanced, true),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS,
                        mek("osmium_compressor")),
                type("purification_chamber", "Mek Purification Chamber", MekanismRecipeType.PURIFYING,
                        (r, b) -> convertItemChemicalToItem(r, b, advanced, true),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("purification_chamber")),
                type("chemical_injection_chamber", "Mek Chemical Injection Chamber",
                        MekanismRecipeType.INJECTING, (r, b) -> convertItemChemicalToItem(r, b, advanced, true),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_injection_chamber")),
                // these two use their infuse type / pigment once per operation
                type("metallurgic_infuser", "Mek Metallurgic Infuser", MekanismRecipeType.METALLURGIC_INFUSING,
                        (r, b) -> convertItemChemicalToItem(r, b, ELECTRIC_TICKS, false),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.FURNACE, GTGuiTextures.PROGRESS_ARROW,
                        mek("metallurgic_infuser")),
                type("painting_machine", "Mek Painting Machine", MekanismRecipeType.PAINTING,
                        (r, b) -> convertItemChemicalToItem(r, b, ELECTRIC_TICKS, false),
                        1, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("painting_machine")),
                type("chemical_dissolution_chamber", "Mek Chemical Dissolution Chamber",
                        MekanismRecipeType.DISSOLUTION, MekanismGregification::convertDissolution,
                        1, 0, 0, 0, 1, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_dissolution_chamber")),
                type("combiner", "Mek Combiner", MekanismRecipeType.COMBINING,
                        MekanismGregification::convertCombining,
                        2, 1, 0, 0, 0, 0, GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS, mek("combiner")),
                type("precision_sawmill", "Mek Precision Sawmill", MekanismRecipeType.SAWING,
                        MekanismGregification::convertSawing,
                        1, 2, 0, 0, 0, 0, GTSoundEntries.CUT, GTGuiTextures.PROGRESS_CUTTER, mek("precision_sawmill")),
                type("chemical_crystallizer", "Mek Chemical Crystallizer", MekanismRecipeType.CRYSTALLIZING,
                        MekanismGregification::convertCrystallizing,
                        0, 1, 0, 0, 1, 0, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_CRYSTALLIZATION,
                        mek("chemical_crystallizer")),
                type("chemical_oxidizer", "Mek Chemical Oxidizer", MekanismRecipeType.OXIDIZING,
                        (r, b) -> convertItemToChemical(r, b, OXIDIZER_TICKS),
                        1, 0, 0, 0, 0, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW,
                        mek("chemical_oxidizer")),
                type("pigment_extractor", "Mek Pigment Extractor", MekanismRecipeType.PIGMENT_EXTRACTING,
                        (r, b) -> convertItemToChemical(r, b, OXIDIZER_TICKS),
                        1, 0, 0, 0, 0, 1, GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("pigment_extractor")),
                type("pressurized_reaction_chamber", "Mek Pressurized Reaction Chamber",
                        MekanismRecipeType.REACTION, MekanismGregification::convertReaction,
                        1, 1, 1, 0, 1, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("pressurized_reaction_chamber")),
                // per tick from here on
                type("chemical_infuser", "Mek Chemical Infuser", MekanismRecipeType.CHEMICAL_INFUSING,
                        MekanismGregification::convertChemicalChemical,
                        0, 0, 0, 0, 2, 1, GTSoundEntries.CHEMICAL, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("chemical_infuser")),
                type("pigment_mixer", "Mek Pigment Mixer", MekanismRecipeType.PIGMENT_MIXING,
                        MekanismGregification::convertChemicalChemical,
                        0, 0, 0, 0, 2, 1, GTSoundEntries.MIXER, GTGuiTextures.PROGRESS_ARROW_MULTIPLE,
                        mek("pigment_mixer")),
                type("isotopic_centrifuge", "Mek Isotopic Centrifuge", MekanismRecipeType.CENTRIFUGING,
                        MekanismGregification::convertChemicalToChemical,
                        0, 0, 0, 0, 1, 1, GTSoundEntries.CENTRIFUGE, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("isotopic_centrifuge")),
                type("solar_neutron_activator", "Mek Solar Neutron Activator", MekanismRecipeType.ACTIVATING,
                        MekanismGregification::convertChemicalToChemical,
                        0, 0, 0, 0, 1, 1, GTSoundEntries.SCIENCE, GTGuiTextures.PROGRESS_MAGNET,
                        mek("solar_neutron_activator")),
                type("chemical_washer", "Mek Chemical Washer", MekanismRecipeType.WASHING,
                        MekanismGregification::convertWashing,
                        0, 0, 1, 0, 1, 1, GTSoundEntries.BATH, GTGuiTextures.PROGRESS_ARROW, mek("chemical_washer")),
                type("electrolytic_separator", "Mek Electrolytic Separator", MekanismRecipeType.SEPARATING,
                        MekanismGregification::convertSeparating,
                        0, 0, 1, 0, 0, 2, GTSoundEntries.ELECTROLYZER, GTGuiTextures.PROGRESS_EXTRACT,
                        mek("electrolytic_separator")),
                machine("rotary_condensentrator", "Mek Rotary Condensentrator", MekanismRecipeType.ROTARY,
                        MekanismGregification::convertRotary,
                        0, 0, 1, 1, 1, 1, GTSoundEntries.COOLING, GTGuiTextures.PROGRESS_ARROW,
                        mek("rotary_condensentrator")),
                type("thermal_evaporation_plant", "Mek Thermal Evaporation Plant",
                        MekanismRecipeType.EVAPORATING, MekanismGregification::convertEvaporating,
                        0, 0, 1, 1, 0, 0, GTSoundEntries.BOILER, GTGuiTextures.PROGRESS_ARROW,
                        mek("thermal_evaporation_controller")));
    }

    private static ResourceLocation mek(String path) {
        return ResourceLocation.fromNamespaceAndPath(MEK, path);
    }

    /** One GT recipe per Mek recipe. */
    private static ForeignMachineType type(String path, String englishName, Supplier<? extends RecipeType<?>> proxy,
                                           ForeignRecipeConverter.Single converter, int itemInputs, int itemOutputs,
                                           int fluidInputs, int fluidOutputs, int chemicalInputs, int chemicalOutputs,
                                           SoundEntry sound, ProgressBarTextureSet progressBar,
                                           ResourceLocation counterpart) {
        return machine(path, englishName, proxy, ForeignRecipeConverter.single(converter), itemInputs, itemOutputs,
                fluidInputs, fluidOutputs, chemicalInputs, chemicalOutputs, sound, progressBar, counterpart);
    }

    private static ForeignMachineType machine(String path, String englishName,
                                              Supplier<? extends RecipeType<?>> proxy,
                                              ForeignRecipeConverter converter, int itemInputs, int itemOutputs,
                                              int fluidInputs, int fluidOutputs, int chemicalInputs,
                                              int chemicalOutputs, SoundEntry sound,
                                              ProgressBarTextureSet progressBar, ResourceLocation counterpart) {
        return ForeignMachineType.multiblock("mek_" + path, englishName, proxy, converter,
                        itemInputs, itemOutputs, fluidInputs, fluidOutputs, sound,
                        builder -> builder.setProgressBar(progressBar), null, counterpart, MultiblockShape.ANY_PARTS)
                .withRecipeType(recipeType -> {
                    if (chemicalInputs > 0) recipeType.setMaxSize(IO.IN, ChemicalStackLike.CAP, chemicalInputs);
                    if (chemicalOutputs > 0) recipeType.setMaxSize(IO.OUT, ChemicalStackLike.CAP, chemicalOutputs);
                    return recipeType;
                });
    }

    // Mekanism lists one output per possible input; its own recipes all have the same, so the first is taken.

    private static boolean convertItemToItem(Recipe<?> foreign, GTRecipeBuilder builder, int ticks) {
        if (!(foreign instanceof ItemStackToItemStackRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(item(recipe.getInput()));
        builder.outputItems(outputs.get(0));
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    /** Item + chemical to an item; {@code perTick}: Mekanism's amount is used every tick, here all at once. */
    private static boolean convertItemChemicalToItem(Recipe<?> foreign, GTRecipeBuilder builder, int ticks,
                                                     boolean perTick) {
        if (!(foreign instanceof ItemStackChemicalToItemStackRecipe<?, ?, ?> recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        ChemicalStackIngredient<?, ?> chemical = recipe.getChemicalInput();
        if (perTick) chemical = scale(chemical, ticks);
        builder.inputItems(item(recipe.getItemInput()));
        builder.input(ChemicalStackLike.CAP, chemical);
        builder.outputItems(outputs.get(0));
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    /** Item + gas (used every tick) to any chemical. */
    private static boolean convertDissolution(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ChemicalDissolutionRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        int ticks = TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        builder.inputItems(item(recipe.getItemInput()));
        builder.input(ChemicalStackLike.CAP, scale(recipe.getGasInput(), ticks));
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0).getChemicalStack()));
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    private static boolean convertItemToChemical(Recipe<?> foreign, GTRecipeBuilder builder, int ticks) {
        if (!(foreign instanceof ItemStackToChemicalRecipe<?, ?> recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(item(recipe.getInput()));
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0)));
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }

    private static boolean convertCombining(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof CombinerRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(item(recipe.getMainInput()));
        builder.inputItems(item(recipe.getExtraInput()));
        builder.outputItems(outputs.get(0));
        builder.EUt(VA[LV]).duration(ELECTRIC_TICKS);
        return true;
    }

    private static boolean convertSawing(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof SawmillRecipe recipe) || recipe.isIncomplete()) return false;
        var main = recipe.getMainOutputDefinition();
        var secondary = recipe.getSecondaryOutputDefinition();
        if (main.isEmpty() && secondary.isEmpty()) return false;
        builder.inputItems(item(recipe.getInput()));
        if (!main.isEmpty()) builder.outputItems(main.get(0));
        double chance = recipe.getSecondaryChance();
        if (!secondary.isEmpty() && chance > 0) {
            if (chance >= 1) {
                builder.outputItems(secondary.get(0));
            } else {
                builder.chancedOutput(secondary.get(0),
                        Math.max(1, (int) Math.round(chance * ChanceLogic.getMaxChancedValue())));
            }
        }
        builder.EUt(VA[LV]).duration(ELECTRIC_TICKS);
        return true;
    }

    private static boolean convertCrystallizing(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ChemicalCrystallizerRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getInput());
        builder.outputItems(outputs.get(0));
        builder.EUt(VA[LV]).duration(ELECTRIC_TICKS);
        return true;
    }

    private static boolean convertReaction(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof PressurizedReactionRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        var output = outputs.get(0);
        builder.inputItems(item(recipe.getInputSolid()));
        builder.inputFluids(fluid(recipe.getInputFluid()));
        builder.input(ChemicalStackLike.CAP, recipe.getInputGas());
        if (!output.item().isEmpty()) builder.outputItems(output.item());
        if (!output.gas().isEmpty()) builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.gas()));
        // Mekanism's extra energy per tick (getEnergyRequired) is left out, as everywhere else
        builder.EUt(VA[LV]).duration(recipe.getDuration());
        return true;
    }

    private static boolean convertChemicalChemical(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ChemicalChemicalToChemicalRecipe<?, ?, ?> recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getLeftInput());
        builder.input(ChemicalStackLike.CAP, recipe.getRightInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0)));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertChemicalToChemical(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ChemicalToChemicalRecipe<?, ?, ?> recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.input(ChemicalStackLike.CAP, recipe.getInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0)));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertWashing(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof FluidChemicalToChemicalRecipe<?, ?, ?> recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputFluids(fluid(recipe.getFluidInput()));
        builder.input(ChemicalStackLike.CAP, recipe.getChemicalInput());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0)));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static boolean convertSeparating(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ElectrolysisRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        var output = outputs.get(0);
        builder.inputFluids(fluid(recipe.getInput()));
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.left()));
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(output.right()));
        // the multiplier is energy per operation; at a fixed voltage that's time
        long multiplier = Math.max(1, recipe.getEnergyMultiplier().longValue());
        builder.EUt(VA[LV]).duration((int) Math.min(Integer.MAX_VALUE, PER_TICK_DURATION * multiplier));
        return true;
    }

    /** A GT recipe per direction the Mek recipe has: gas to fluid, fluid to gas. */
    private static void convertRotary(Recipe<?> foreign, ForeignRecipeConverter.Output output) {
        if (!(foreign instanceof RotaryRecipe recipe) || recipe.isIncomplete()) return;
        if (recipe.hasGasToFluid()) {
            output.add(builder -> {
                var outputs = recipe.getFluidOutputDefinition();
                if (outputs.isEmpty()) return false;
                builder.input(ChemicalStackLike.CAP, recipe.getGasInput());
                builder.outputFluids(outputs.get(0));
                builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
                return true;
            });
        }
        if (recipe.hasFluidToGas()) {
            output.add(builder -> {
                var outputs = recipe.getGasOutputDefinition();
                if (outputs.isEmpty()) return false;
                builder.inputFluids(fluid(recipe.getFluidInput()));
                builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.get(0)));
                builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
                return true;
            });
        }
    }

    private static boolean convertEvaporating(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof FluidToFluidRecipe recipe) || recipe.isIncomplete()) return false;
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputFluids(fluid(recipe.getInput()));
        builder.outputFluids(outputs.get(0));
        builder.EUt(VA[LV]).duration(PER_TICK_DURATION);
        return true;
    }

    private static ChemicalStackIngredient<?, ?> scale(ChemicalStackIngredient<?, ?> ingredient, int ticks) {
        return ChemicalStackLike.TYPE.withAmount(ingredient, ChemicalStackLike.TYPE.ingredientAmount(ingredient) * ticks);
    }

    /**
     * Mekanism 10.4's item ingredient as a GT one. Its API only gives the matching stacks, so this goes by the two
     * implementations in {@code ItemStackIngredientCreator}; a multi ingredient needs one amount for all its parts.
     */
    private static Ingredient item(ItemStackIngredient ingredient) {
        if (ingredient instanceof SingleItemStackIngredient single) {
            return SizedIngredient.create(single.getInputRaw(), single.getAmountRaw());
        } else if (ingredient instanceof MultiItemStackIngredient multi) {
            List<Ingredient> parts = new ArrayList<>();
            int amount = -1;
            for (ItemStackIngredient child : multi.getIngredients()) {
                if (!(child instanceof SingleItemStackIngredient single)) {
                    throw new IllegalArgumentException("Nested item ingredient " + child.getClass().getName());
                }
                if (amount >= 0 && single.getAmountRaw() != amount) {
                    throw new IllegalArgumentException("Item ingredient parts with different amounts");
                }
                amount = single.getAmountRaw();
                parts.add(single.getInputRaw());
            }
            return SizedIngredient.create(Ingredient.merge(parts), amount);
        }
        throw new IllegalArgumentException("Unknown item ingredient " + ingredient.getClass().getName());
    }

    /** Same as {@link #item}, by the three implementations in {@code FluidStackIngredientCreator}. */
    private static FluidIngredient fluid(FluidStackIngredient ingredient) {
        if (ingredient instanceof SingleFluidStackIngredient single) {
            return FluidIngredient.of(single.getInputRaw());
        } else if (ingredient instanceof TaggedFluidStackIngredient tagged) {
            return FluidIngredient.of(tagged.getTag(), tagged.getRawAmount());
        } else if (ingredient instanceof MultiFluidStackIngredient multi) {
            List<FluidIngredient.Value> values = new ArrayList<>();
            int amount = -1;
            for (FluidStackIngredient child : multi.getIngredients()) {
                FluidIngredient part = fluid(child);
                if (amount >= 0 && part.getAmount() != amount) {
                    throw new IllegalArgumentException("Fluid ingredient parts with different amounts");
                }
                amount = part.getAmount();
                values.addAll(Arrays.asList(part.values));
            }
            // NBT of the parts is dropped
            return FluidIngredient.fromValues(values, amount, null);
        }
        throw new IllegalArgumentException("Unknown fluid ingredient " + ingredient.getClass().getName());
    }
}
