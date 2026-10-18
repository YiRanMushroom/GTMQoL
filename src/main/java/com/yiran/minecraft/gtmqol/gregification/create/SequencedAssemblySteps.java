package com.yiran.minecraft.gtmqol.gregification.create;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What each step of a Create sequenced assembly recipe adds to its flattened GT recipe, by the step's recipe type:
 * the inputs it consumes over all loops, and its time. Other mods with their own step types register a {@link Step}
 * for them. Types without one use what Create's own recipe viewer shows for them ({@link IAssemblyRecipe}), 5 s each.
 */
public final class SequencedAssemblySteps {

    /** 5 s. */
    public static final int SHORT = 100;
    /** 10 s. */
    public static final int LONG = 200;

    private static final Map<ResourceLocation, Step> STEPS = new HashMap<>();

    static {
        register(create("deploying"), SequencedAssemblySteps::deploying);
        register(create("pressing"), (step, context) -> timeOnly(context, SHORT));
        register(create("cutting"), (step, context) -> timeOnly(context, LONG));
        register(create("filling"), SequencedAssemblySteps::filling);
    }

    private SequencedAssemblySteps() {}

    /** One step type. */
    @FunctionalInterface
    public interface Step {

        /**
         * Writes the step's inputs into the builder, times {@link Context#times()}, and adds its duration.
         *
         * @return false if a GT machine can't do it, which leaves the whole recipe out
         */
        boolean write(Recipe<?> step, Context context);
    }

    /** The flattened recipe being built. */
    public static final class Context {

        private final GTRecipeBuilder builder;
        private final int times;
        private int duration;

        Context(GTRecipeBuilder builder, int times) {
            this.builder = builder;
            this.times = times;
        }

        public GTRecipeBuilder builder() {
            return builder;
        }

        /** How often each step runs: the recipe's loops. */
        public int times() {
            return times;
        }

        /** Adds {@code ticks} for every time the step runs. */
        public void addStepDuration(int ticks) {
            duration += ticks * times;
        }

        public int duration() {
            return duration;
        }
    }

    /** For the step's recipe type, e.g. {@code create:deploying}; replaces an earlier one. */
    public static void register(ResourceLocation recipeType, Step step) {
        STEPS.put(recipeType, step);
    }

    static boolean write(Recipe<?> step, Context context) {
        ResourceLocation type = BuiltInRegistries.RECIPE_TYPE.getKey(step.getType());
        Step handler = type == null ? null : STEPS.get(type);
        if (handler != null) return handler.write(step, context);
        if (!(step instanceof IAssemblyRecipe assembly)) {
            GTMQoL.LOGGER.debug("Sequenced assembly step of unknown type {}", type);
            return false;
        }
        List<Ingredient> items = new ArrayList<>();
        List<SizedFluidIngredient> fluids = new ArrayList<>();
        assembly.addAssemblyIngredients(items);
        assembly.addAssemblyFluidIngredients(fluids);
        for (Ingredient item : items) context.builder.inputItems(item, context.times);
        for (SizedFluidIngredient fluid : fluids) addFluid(fluid, context);
        context.addStepDuration(SHORT);
        return true;
    }

    /** Steps that consume nothing but time. */
    private static boolean timeOnly(Context context, int ticks) {
        context.addStepDuration(ticks);
        return true;
    }

    /** The held item, consumed every time unless the recipe keeps it. */
    private static boolean deploying(Recipe<?> step, Context context) {
        if (!(step instanceof ItemApplicationRecipe recipe)) return false;
        Ingredient held = recipe.getRequiredHeldItem();
        if (held.isEmpty()) return false;
        if (recipe.shouldKeepHeldItem()) {
            context.builder.notConsumable(held);
        } else {
            context.builder.inputItems(held, context.times);
        }
        context.addStepDuration(SHORT);
        return true;
    }

    private static boolean filling(Recipe<?> step, Context context) {
        if (!(step instanceof ProcessingRecipe<?, ?> recipe) || recipe.getFluidIngredients().isEmpty()) return false;
        for (SizedFluidIngredient fluid : recipe.getFluidIngredients()) addFluid(fluid, context);
        context.addStepDuration(SHORT);
        return true;
    }

    private static void addFluid(SizedFluidIngredient fluid, Context context) {
        context.builder.inputFluids(new SizedFluidIngredient(fluid.ingredient(), fluid.amount() * context.times));
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("create", path);
    }
}
