package com.yiran.minecraft.gtmqol.gregification.create;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.ItemGhostIngredient;
import com.yiran.minecraft.gtmqol.common.modifier.AbsoluteParallel;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.ForeignRecipeConverter;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.GregificationModifiers;
import com.yiran.minecraft.gtmqol.gregification.MarkCoveredRecipes;
import com.yiran.minecraft.gtmqol.gregification.MultiblockShape;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Create's processing as 3x3x3 hollow multiblocks of andesite casings, without maintenance, each crafted from the
 * Create machine and LV circuits in the magical assembler. Create recipes use no energy, so they get a fixed one:
 * LV for 5 s, heated basin recipes MV and superheated HV; sandpaper polishing LV for 30 s with circuit 1. These run
 * gregified overclocking (the same energy per recipe).
 *
 * <p>The fan does every kind of fan processing, Create's and addons' ({@link FanProcessing}), told apart by what it
 * would blow through, as inputs that are never consumed: any of the kind's catalyst blocks as an item, or a bucket's
 * worth of its catalyst fluid. Blasting runs every furnace recipe. Fan recipes are ULV for 30 s, with a plain
 * overclock and then as many at once as the inputs and outputs allow, at no extra cost: a fan processes any amount as
 * fast as one.</p>
 *
 * <p>The mechanical crafter is 5x5x5 of brass casings, crafted from 81 mechanical crafters and an IV circuit in the
 * magical assembler. Its recipes are shapeless, with the result marked as a ghost ingredient: recipes with the same
 * items in other shapes are told apart by what is marked in a Ghost Ingredient Hatch.</p>
 *
 * <p>Sequenced assembly is flattened into one recipe on a line like GT's assembly line, see
 * {@link SequencedAssemblyMachine} and {@link SequencedAssemblySteps}.</p>
 *
 * <p>Left out: what Create does without recipes (the press and mixer running crafting recipes, crafting recipes in
 * mechanical crafters, potions in the spout and basin).</p>
 */
public final class CreateGregification {

    private static final String CREATE = "create";
    /** Most recipes: 5 s. */
    private static final int DURATION = 100;
    /** Fan processing and sandpaper polishing: 30 s. */
    private static final int LONG_DURATION = 600;
    /** A catalyst fluid needs a bucket's worth present. */
    private static final int CATALYST_FLUID = 1000;
    /** Distinct items of a mechanical crafting recipe (up to 9x9). */
    private static final int MECHANICAL_CRAFTING_INPUTS = 16;
    /** Base item and the steps' items; Create's own need 4. */
    private static final int SEQUENCED_INPUTS = 9;
    /** Create's precision mechanism has 9 possible results. */
    private static final int SEQUENCED_OUTPUTS = 9;
    /** gtceu shows {@code <namespace>.machine.<path>.tooltip} on its own, no {@code withTooltip} needed. */
    private static final String SEQUENCED_TOOLTIP = "gtmqol.machine.create_sequenced_assembly.tooltip";

    static {
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SEQUENCED_TOOLTIP,
                "Runs recipes with at most as many steps as the line is long; at full length (16), any recipe.");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SequencedAssemblyMachine.TOO_SHORT,
                "Line too short for this recipe");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SequencedAssemblyMachine.MIN_LENGTH_INFO,
                "Minimum Line Length: %s");
    }

    private CreateGregification() {}

    public static List<ForeignMachineType> types() {
        return List.of(
                machine("millstone", "Create Millstone", createType("milling"),
                        CreateGregification::convertProcessing, 1, 4, 0, 0,
                        GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_MACERATE, "millstone"),
                machine("crushing_wheels", "Create Crushing Wheels", createType("crushing"),
                        CreateGregification::convertProcessing, 1, 4, 0, 0,
                        GTSoundEntries.MACERATOR, GTGuiTextures.PROGRESS_MACERATE, "crushing_wheel"),
                machine("press", "Create Mechanical Press", createType("pressing"),
                        CreateGregification::convertProcessing, 1, 2, 0, 0,
                        GTSoundEntries.FORGE_HAMMER, GTGuiTextures.PROGRESS_BENDING, "mechanical_press"),
                // the press is taken by the press above
                machine("compactor", "Create Compactor", createType("compacting"),
                        CreateGregification::convertProcessing, 9, 4, 2, 2,
                        GTSoundEntries.COMPRESSOR, GTGuiTextures.PROGRESS_COMPRESS, "basin"),
                machine("mixer", "Create Mechanical Mixer", createType("mixing"),
                        CreateGregification::convertProcessing, 9, 4, 2, 2,
                        GTSoundEntries.MIXER, GTGuiTextures.PROGRESS_ARROW_MULTIPLE, "mechanical_mixer"),
                machine("fan", "Create Encased Fan", null, CreateGregification::convertFan, 2, 4, 1, 0,
                        GTSoundEntries.MOTOR, GTGuiTextures.PROGRESS_ARROW, "encased_fan")
                        .withProxies(FanProcessing::recipeTypes)
                        .withModifiers(GTRecipeModifiers.OC_NON_PERFECT, AbsoluteParallel.upTo(Integer.MAX_VALUE)),
                machine("deployer", "Create Deployer", createType("deploying"),
                        CreateGregification::convertDeploying, 2, 4, 0, 0,
                        GTSoundEntries.ASSEMBLER, GTGuiTextures.PROGRESS_ASSEMBLER, "deployer")
                        .withMoreProxies(createType("item_application"), createType("sandpaper_polishing")),
                machine("spout", "Create Spout", createType("filling"),
                        CreateGregification::convertProcessing, 1, 1, 1, 0,
                        GTSoundEntries.BATH, GTGuiTextures.PROGRESS_CANNER, "spout"),
                machine("item_drain", "Create Item Drain", createType("emptying"),
                        CreateGregification::convertProcessing, 1, 1, 0, 1,
                        GTSoundEntries.BATH, GTGuiTextures.PROGRESS_EXTRACT, "item_drain"),
                machine("saw", "Create Mechanical Saw", createType("cutting"), sawConverter(), 1, 4, 0, 0,
                        GTSoundEntries.CUT, GTGuiTextures.PROGRESS_CUTTER, "mechanical_saw")
                        .withRecipeType(type -> type.setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1)),
                ForeignMachineType.multiblock("create_mechanical_crafter", "Create Mechanical Crafter",
                                createType("mechanical_crafting"),
                                ForeignRecipeConverter.single(CreateGregification::convertMechanicalCrafting),
                                MECHANICAL_CRAFTING_INPUTS, 1, 0, 0, GTSoundEntries.ASSEMBLER,
                                builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER), null,
                                create("mechanical_crafter"), MultiblockShape.CREATE_BRASS)
                        .withRecipeType(type -> type.setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1))
                        .withCrafting(CreateGregification::assembleMechanicalCrafter),
                ForeignMachineType.multiblock("create_sequenced_assembly", "Create Sequenced Assembly Line",
                                createType("sequenced_assembly"),
                                ForeignRecipeConverter.single(CreateGregification::convertSequencedAssembly),
                                SEQUENCED_INPUTS, SEQUENCED_OUTPUTS, 2, 0, GTSoundEntries.ASSEMBLER,
                                builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER)
                                        .addRecipeUIModifier(SequencedAssemblyMachine.LENGTH_INFO), null,
                                create("belt_connector"), MultiblockShape.CREATE_ASSEMBLY_LINE)
                        .withRecipeType(type -> type.setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1))
                        .withModifiers(SequencedAssemblyMachine.STEP_LIMIT, GregificationModifiers.OVERCLOCK,
                                GTRecipeModifiers.BATCH_MODE)
                        .withCrafting(CreateGregification::assembleSequencedAssembly));
    }

    // --- machine recipes ---

    /** The Create machine and two LV circuits. */
    private static void magicalAssemble(Consumer<FinishedRecipe> provider, ResourceLocation id,
                                        ItemStack counterpart, ItemStack machine) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(id)
                .inputItems(counterpart)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .outputItems(machine)
                .duration(200)
                .save(provider);
    }

    /** 81 mechanical crafters (a full 9x9) and an IV circuit, in the magical assembler. */
    private static void assembleMechanicalCrafter(Consumer<FinishedRecipe> provider, ResourceLocation id,
                                                  ItemStack counterpart, ItemStack machine) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(id)
                .inputItems(Ingredient.of(counterpart), 81)
                .inputItems(CustomTags.IV_CIRCUITS)
                .outputItems(machine)
                .duration(400)
                .save(provider);
    }

    /** A Create mechanical belt, 16 brass casings and a LuV circuit, in the magical assembler. */
    private static void assembleSequencedAssembly(Consumer<FinishedRecipe> provider, ResourceLocation id,
                                                  ItemStack counterpart, ItemStack machine) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(id)
                .inputItems(counterpart)
                .inputItems(Ingredient.of(BuiltInRegistries.ITEM.get(create("brass_casing"))), 16)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .circuitMeta(16)
                .outputItems(machine)
                .duration(400)
                .save(provider);
    }

    private static ForeignMachineType machine(String path, String englishName,
                                              Supplier<? extends RecipeType<?>> proxy,
                                              ForeignRecipeConverter.Single converter, int itemInputs,
                                              int itemOutputs, int fluidInputs, int fluidOutputs,
                                              SoundEntry sound, ProgressBarTextureSet progressBar,
                                              String counterpart) {
        return machine(path, englishName, proxy, ForeignRecipeConverter.single(converter), itemInputs, itemOutputs,
                fluidInputs, fluidOutputs, sound, progressBar, counterpart);
    }

    private static ForeignMachineType machine(String path, String englishName,
                                              @Nullable Supplier<? extends RecipeType<?>> proxy,
                                              ForeignRecipeConverter converter, int itemInputs,
                                              int itemOutputs, int fluidInputs, int fluidOutputs,
                                              SoundEntry sound, ProgressBarTextureSet progressBar,
                                              String counterpart) {
        return ForeignMachineType.multiblock("create_" + path, englishName, proxy,
                        converter, itemInputs, itemOutputs, fluidInputs, fluidOutputs,
                        sound, builder -> builder.setProgressBar(progressBar), null, create(counterpart),
                        MultiblockShape.CREATE)
                .withCrafting(CreateGregification::magicalAssemble);
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath(CREATE, path);
    }

    /** Looked up at common setup, when Create's types are registered. */
    private static Supplier<RecipeType<?>> createType(String path) {
        return () -> BuiltInRegistries.RECIPE_TYPE.get(create(path));
    }

    // --- converters ---

    /** Create's saw picks between recipes with the same input by its filter, see {@link MarkCoveredRecipes}. */
    private static ForeignRecipeConverter sawConverter() {
        return new MarkCoveredRecipes(CreateGregification::convertProcessing, CreateGregification::itemInputs,
                CreateGregification::firstOutputMark);
    }

    private static List<Set<Object>> itemInputs(Recipe<?> recipe) {
        List<Set<Object>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            Set<Object> items = new HashSet<>();
            for (ItemStack stack : ingredient.getItems()) items.add(stack.getItem());
            if (!items.isEmpty()) inputs.add(items);
        }
        return inputs;
    }

    private static @Nullable GhostIngredient<?> firstOutputMark(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) return null;
        for (ProcessingOutput output : processing.getRollableResults()) {
            if (!output.getStack().isEmpty()) return new ItemGhostIngredient(output.getStack());
        }
        return null;
    }

    /** Item and fluid inputs and outputs as they are, LV, or MV/HV for heated/superheated basin recipes. */
    private static boolean convertProcessing(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof ProcessingRecipe<?> recipe)) return false;
        if (!addInputs(recipe, builder) || !addOutputs(recipe, builder)) return false;
        int tier = switch (recipe.getRequiredHeat()) {
            case NONE -> LV;
            case HEATED -> MV;
            case SUPERHEATED -> HV;
        };
        builder.EUt(VA[tier]).duration(DURATION);
        return true;
    }

    /** Once for each fan kind running the recipe's type and each sort of catalyst it has: items, or a fluid. */
    private static void convertFan(Recipe<?> foreign, ForeignRecipeConverter.Output output) {
        // as Create's fan: recipes for hand use only are skipped
        if (foreign.getId().getPath().endsWith("_manual_only")) return;
        for (ResourceLocation kind : FanProcessing.kindsOf(foreign.getType())) {
            FanProcessing.Catalysts catalysts = FanProcessing.catalysts(kind);
            if (!catalysts.items().isEmpty()) {
                Ingredient items = Ingredient.of(catalysts.items().toArray(Item[]::new));
                output.add(builder -> fanRecipe(foreign, builder.notConsumable(items)));
            }
            if (catalysts.fluid() != null) {
                var fluid = com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient.of(catalysts.fluid(),
                        CATALYST_FLUID);
                output.add(builder -> fanRecipe(foreign, builder.notConsumableFluid(fluid)));
            }
        }
    }

    /** The recipe itself: vanilla cooking or Create processing, ULV. */
    private static boolean fanRecipe(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (foreign instanceof AbstractCookingRecipe cooking) {
            ItemStack result = cooking.getResultItem(RegistryAccess.EMPTY);
            if (cooking.getIngredients().isEmpty() || result.isEmpty()) return false;
            builder.inputItems(cooking.getIngredients().get(0)).outputItems(result);
        } else if (foreign instanceof ProcessingRecipe<?> recipe) {
            if (!addInputs(recipe, builder) || !addOutputs(recipe, builder)) return false;
        } else {
            return false;
        }
        builder.EUt(VA[ULV]).duration(LONG_DURATION);
        return true;
    }

    /** Deploying and item application: the held item, kept if the recipe says so. Sandpaper: circuit 1. */
    private static boolean convertDeploying(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (foreign instanceof SandPaperPolishingRecipe recipe) {
            if (!addInputs(recipe, builder) || !addOutputs(recipe, builder)) return false;
            builder.circuitMeta(1).EUt(VA[LV]).duration(LONG_DURATION);
            return true;
        }
        if (!(foreign instanceof ItemApplicationRecipe recipe)) return false;
        Ingredient processed = recipe.getProcessedItem();
        Ingredient held = recipe.getRequiredHeldItem();
        if (processed.isEmpty() || held.isEmpty()) return false;
        builder.inputItems(processed);
        if (recipe.shouldKeepHeldItem()) {
            builder.notConsumable(held);
        } else {
            builder.inputItems(held);
        }
        if (!addOutputs(recipe, builder)) return false;
        builder.EUt(VA[LV]).duration(DURATION);
        return true;
    }

    /** Shapeless, with the result as a ghost ingredient. */
    private static boolean convertMechanicalCrafting(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof CraftingRecipe recipe)) return false;
        ItemStack result = recipe.getResultItem(RegistryAccess.EMPTY);
        if (result.isEmpty() || !addItemInputs(recipe.getIngredients(), builder)) return false;
        builder.input(GhostIngredientRecipeCapability.CAP, new ItemGhostIngredient(result))
                .outputItems(result)
                .EUt(VA[LV])
                .duration(DURATION);
        return true;
    }

    /**
     * Flattened: the base item and everything the steps consume over all loops (see {@link SequencedAssemblySteps}),
     * LV for as long as the steps take. One result is picked by weight, like Create, with GT's XOR chance logic. The
     * main result is a ghost ingredient, so recipes with the same inputs are told apart.
     */
    private static boolean convertSequencedAssembly(Recipe<?> foreign, GTRecipeBuilder builder) {
        if (!(foreign instanceof SequencedAssemblyRecipe recipe)) return false;
        List<SequencedRecipe<?>> sequence = recipe.getSequence();
        ItemStack main = recipe.getResultItem(RegistryAccess.EMPTY);
        if (sequence.isEmpty() || main.isEmpty() || recipe.resultPool.isEmpty()) return false;
        // no loops: Create runs the first step once
        int loops = recipe.getLoops();
        List<SequencedRecipe<?>> steps = loops > 0 ? sequence : sequence.subList(0, 1);
        builder.inputItems(recipe.getIngredient());
        var context = new SequencedAssemblySteps.Context(builder, Math.max(1, loops));
        for (SequencedRecipe<?> step : steps) {
            if (!SequencedAssemblySteps.write(step.getRecipe(), context)) return false;
        }
        builder.input(GhostIngredientRecipeCapability.CAP, new ItemGhostIngredient(main));
        if (recipe.resultPool.size() == 1) {
            builder.outputItems(recipe.resultPool.get(0).getStack());
        } else {
            // Create's chances are weights
            float total = 0;
            for (ProcessingOutput output : recipe.resultPool) total += output.getChance();
            if (total <= 0) return false;
            for (ProcessingOutput output : recipe.resultPool) {
                builder.chancedOutput(output.getStack(), Gregification.chance(output.getChance() / total));
            }
            builder.chancedItemOutputLogic(ChanceLogic.XOR);
        }
        builder.addData(SequencedAssemblyMachine.STEPS, sequence.size())
                .EUt(VA[LV])
                .duration(Math.max(1, context.duration()));
        return true;
    }

    /** Create's fluid ingredient as GT's, with another amount: any of the fluids it matches. */
    static com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient toGT(FluidIngredient fluid, int amount) {
        List<FluidStack> stacks = new ArrayList<>();
        for (FluidStack stack : fluid.getMatchingFluidStacks()) {
            FluidStack copy = stack.copy();
            copy.setAmount(amount);
            stacks.add(copy);
        }
        return com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient.of(stacks);
    }

    /** Items and fluids; false if there are none. */
    private static boolean addInputs(ProcessingRecipe<?> recipe, GTRecipeBuilder builder) {
        boolean items = addItemInputs(recipe.getIngredients(), builder);
        List<FluidIngredient> fluids = recipe.getFluidIngredients();
        for (FluidIngredient fluid : fluids) builder.inputFluids(toGT(fluid, fluid.getRequiredAmount()));
        return items || !fluids.isEmpty();
    }

    /** Create lists one ingredient per item; equal ones become one input with a count. False if there are none. */
    private static boolean addItemInputs(List<Ingredient> all, GTRecipeBuilder builder) {
        List<Ingredient> ingredients = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Ingredient ingredient : all) {
            if (ingredient.isEmpty()) continue;
            int index = ingredients.indexOf(ingredient);
            if (index < 0) {
                ingredients.add(ingredient);
                counts.add(1);
            } else {
                counts.set(index, counts.get(index) + 1);
            }
        }
        for (int i = 0; i < ingredients.size(); i++) builder.inputItems(ingredients.get(i), counts.get(i));
        return !ingredients.isEmpty();
    }

    /** Chanced outputs keep Create's chance, each rolled on its own. False if there are none. */
    private static boolean addOutputs(ProcessingRecipe<?> recipe, GTRecipeBuilder builder) {
        boolean any = false;
        for (ProcessingOutput output : recipe.getRollableResults()) {
            ItemStack stack = output.getStack();
            if (stack.isEmpty()) continue;
            int chance = Gregification.chance(output.getChance());
            if (chance >= ChanceLogic.getMaxChancedValue()) {
                builder.outputItems(stack);
            } else {
                builder.chancedOutput(stack, chance);
            }
            any = true;
        }
        for (FluidStack fluid : recipe.getFluidResults()) {
            if (fluid.isEmpty()) continue;
            builder.outputFluids(fluid);
            any = true;
        }
        return any;
    }
}
