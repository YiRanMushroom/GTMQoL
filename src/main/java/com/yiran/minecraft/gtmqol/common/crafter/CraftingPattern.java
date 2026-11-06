package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * What an encoded pattern holds, like AE2's patterns: a 3 x 3 grid (stonecutting and smithing use part of it) and the
 * recipe it matched. The rest comes from the recipe each time ({@link #plan}), so a pattern whose recipe was changed stops working instead of
 * crafting something it no longer should.
 *
 * @param fluids      crafting only: take buckets as their fluid, as AE2's fluid substitution
 * @param substitutes crafting only: accept whatever the recipe accepts in a slot (tags), as AE2's substitution
 */
public record CraftingPattern(Mode mode, List<ItemStack> inputs, ResourceLocation recipeId, boolean fluids,
                              boolean substitutes) {

    public static final int SIZE = 3;
    public static final int SLOTS = SIZE * SIZE;

    private static final int STONECUTTING_SLOT = 4;
    private static final int[] SMITHING_SLOTS = { 3, 4, 5 };

    private static final String MODE_KEY = "mode";
    private static final String INPUTS_KEY = "inputs";
    private static final String RECIPE_KEY = "recipe";
    private static final String FLUIDS_KEY = "fluids";
    private static final String SUBSTITUTES_KEY = "substitutes";

    /** Which kind of recipe a pattern holds, and so which slots of the grid it uses. */
    public enum Mode {

        CRAFTING,
        STONECUTTING,
        SMITHING;

        public boolean uses(int slot) {
            return switch (this) {
                case CRAFTING -> true;
                case STONECUTTING -> slot == STONECUTTING_SLOT;
                case SMITHING -> slot >= SMITHING_SLOTS[0] && slot <= SMITHING_SLOTS[2];
            };
        }

        public static @Nullable Mode of(Recipe<?> recipe) {
            if (recipe instanceof CraftingRecipe) return CRAFTING;
            if (recipe instanceof StonecutterRecipe) return STONECUTTING;
            if (recipe instanceof SmithingRecipe) return SMITHING;
            return null;
        }
    }

    /** What a slot of the grid does. */
    public enum SlotUse {
        EMPTY,
        CONSUMED,
        /** Needed but stays in the machine: tools and anything else the recipe gives back as itself. */
        KEPT,
        /** A bucket, taken as its fluid. */
        FLUID
    }

    /** An input as encoded, with its amount in {@code item}, and what the machine accepts for it. */
    public record Input(ItemStack item, Ingredient ingredient) {}

    /**
     * One craft.
     *
     * @param uses per grid slot
     */
    public record Plan(List<SlotUse> uses, List<Input> consumed, List<Input> kept, List<FluidStack> fluids,
                       List<ItemStack> outputs) implements ICrafterPattern {

        @Override
        public List<ItemInput> itemInputs() {
            List<ItemInput> inputs = new ArrayList<>();
            for (Input input : kept) inputs.add(new ItemInput(input.ingredient(), 1, false));
            for (Input input : consumed) {
                inputs.add(new ItemInput(input.ingredient(), input.item().getCount(), true));
            }
            return inputs;
        }

        @Override
        public List<FluidStack> fluidInputs() {
            return fluids;
        }
    }

    /**
     * The pattern for this grid, or null when no recipe matches it.
     *
     * @param choice which stonecutting recipe of the item, wrapping around
     */
    public static @Nullable CraftingPattern encode(Mode mode, List<ItemStack> grid, int choice, boolean fluids,
                                                   boolean substitutes, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack item = grid.get(slot);
            inputs.add(item.isEmpty() || !mode.uses(slot) ? ItemStack.EMPTY : item.copyWithCount(1));
        }
        var recipes = level.getRecipeManager();
        Optional<? extends RecipeHolder<?>> holder = switch (mode) {
            case CRAFTING -> {
                CraftingInput input = CraftingInput.ofPositioned(SIZE, SIZE, inputs).input();
                yield input.isEmpty() ? Optional.empty() : recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
            }
            case STONECUTTING -> {
                var matching = stonecutting(inputs, level);
                yield matching.isEmpty() ? Optional.empty() :
                        Optional.of(matching.get(Math.floorMod(choice, matching.size())));
            }
            case SMITHING -> recipes.getRecipeFor(RecipeType.SMITHING, smithingInput(inputs), level);
        };
        boolean crafting = mode == Mode.CRAFTING;
        return holder.map(found -> new CraftingPattern(mode, inputs, found.id(), crafting && fluids,
                crafting && substitutes)).orElse(null);
    }

    /** The stonecutting recipes of the grid's item, in the same order on both sides. */
    public static List<RecipeHolder<StonecutterRecipe>> stonecutting(List<ItemStack> grid, Level level) {
        ItemStack item = grid.get(STONECUTTING_SLOT);
        if (item.isEmpty()) return List.of();
        return level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SingleRecipeInput(item), level);
    }

    /** Which of the grid's stonecutting recipes this is; 0 for other recipes. */
    public static int choice(ResourceLocation recipeId, List<ItemStack> grid, Level level) {
        var matching = stonecutting(grid, level);
        for (int i = 0; i < matching.size(); i++) {
            if (matching.get(i).id().equals(recipeId)) return i;
        }
        return 0;
    }

    private static SmithingRecipeInput smithingInput(List<ItemStack> inputs) {
        return new SmithingRecipeInput(inputs.get(SMITHING_SLOTS[0]), inputs.get(SMITHING_SLOTS[1]),
                inputs.get(SMITHING_SLOTS[2]));
    }

    /** A recipe laid out on the grid, the first choice of each slot; for the recipe viewers' transfer. */
    public static List<ItemStack> grid(Recipe<?> recipe) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(SLOTS, ItemStack.EMPTY));
        if (recipe instanceof StonecutterRecipe) {
            place(grid, STONECUTTING_SLOT, recipe.getIngredients().get(0));
        } else if (recipe instanceof SmithingTransformRecipe smithing) {
            place(grid, SMITHING_SLOTS[0], smithing.template);
            place(grid, SMITHING_SLOTS[1], smithing.base);
            place(grid, SMITHING_SLOTS[2], smithing.addition);
        } else if (recipe instanceof SmithingTrimRecipe smithing) {
            place(grid, SMITHING_SLOTS[0], smithing.template);
            place(grid, SMITHING_SLOTS[1], smithing.base);
            place(grid, SMITHING_SLOTS[2], smithing.addition);
        } else {
            int width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : SIZE;
            var ingredients = recipe.getIngredients();
            for (int i = 0; i < ingredients.size() && i < SLOTS; i++) {
                place(grid, i / width * SIZE + i % width, ingredients.get(i));
            }
        }
        return grid;
    }

    private static void place(List<ItemStack> grid, int slot, Ingredient ingredient) {
        ItemStack[] choices = ingredient.getItems();
        if (choices.length > 0) grid.set(slot, choices[0].copyWithCount(1));
    }

    /** Whether a crafting recipe's slot takes more than one item, so its pattern needs substitutes to be of any use. */
    public static boolean hasChoices(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe &&
                recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.getItems().length > 1);
    }

    /** One craft of this pattern, or null when its recipe is gone or no longer matches its grid. */
    public @Nullable Plan plan(Level level) {
        var holder = level.getRecipeManager().byKey(recipeId).orElse(null);
        if (holder == null) return null;
        return switch (mode) {
            case CRAFTING -> holder.value() instanceof CraftingRecipe recipe ? craftingPlan(recipe, level) : null;
            case STONECUTTING -> holder.value() instanceof StonecutterRecipe recipe ?
                    exactPlan(recipe, new SingleRecipeInput(inputs.get(STONECUTTING_SLOT)), level) : null;
            case SMITHING -> holder.value() instanceof SmithingRecipe recipe ?
                    exactPlan(recipe, smithingInput(inputs), level) : null;
        };
    }

    /**
     * Stonecutting and smithing: everything is used up, nothing comes back, and every input counts exactly: a smithed
     * item keeps the base item's durability and enchantments.
     */
    private <I extends RecipeInput> @Nullable Plan exactPlan(Recipe<I> recipe, I input, Level level) {
        if (!recipe.matches(input, level)) return null;
        ItemStack crafted = recipe.assemble(input, level.registryAccess());
        if (crafted.isEmpty()) return null;
        List<SlotUse> uses = new ArrayList<>();
        List<Input> consumed = new ArrayList<>();
        for (ItemStack item : inputs) {
            uses.add(item.isEmpty() ? SlotUse.EMPTY : SlotUse.CONSUMED);
            if (!item.isEmpty()) addInput(consumed, item, RecipeHelper.makeItemIngredient(item));
        }
        return new Plan(uses, consumed, List.of(), List.of(), List.of(crafted));
    }

    private @Nullable Plan craftingPlan(CraftingRecipe recipe, Level level) {
        var positioned = CraftingInput.ofPositioned(SIZE, SIZE, inputs);
        CraftingInput input = positioned.input();
        if (!recipe.matches(input, level)) return null;
        ItemStack crafted = recipe.assemble(input, level.registryAccess());
        if (crafted.isEmpty()) return null;

        List<ItemStack> remaining = recipe.getRemainingItems(input);
        List<SlotUse> uses = new ArrayList<>();
        List<Input> consumed = new ArrayList<>();
        List<Input> kept = new ArrayList<>();
        List<FluidStack> fluidInputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        merge(outputs, crafted);
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack item = inputs.get(slot);
            if (item.isEmpty()) {
                uses.add(SlotUse.EMPTY);
                continue;
            }
            int x = slot % SIZE - positioned.left();
            int y = slot / SIZE - positioned.top();
            ItemStack remainder = remaining.get(x + y * input.width());
            Optional<FluidStack> fluid = fluids ? bucketFluid(item, remainder) : Optional.empty();
            if (fluid.isPresent()) {
                uses.add(SlotUse.FLUID);
                merge(fluidInputs, fluid.get());
            } else if (ItemStack.isSameItem(remainder, item)) {
                uses.add(SlotUse.KEPT);
                if (kept.stream().noneMatch(other -> ItemStack.isSameItem(other.item(), item))) {
                    kept.add(new Input(item, ingredient(recipe, item, true)));
                }
            } else {
                uses.add(SlotUse.CONSUMED);
                addInput(consumed, item, ingredient(recipe, item, !recipe.isSpecial()));
                if (!remainder.isEmpty()) merge(outputs, remainder);
            }
        }
        return new Plan(uses, consumed, kept, fluidInputs, outputs);
    }

    /** Only real buckets that give an empty bucket back, as in AE2: other containers can do anything when emptied. */
    private static Optional<FluidStack> bucketFluid(ItemStack item, ItemStack remainder) {
        boolean bucket = item.getItem() instanceof BucketItem || item.getItem() instanceof MilkBucketItem;
        if (!bucket || remainder.getCount() != 1 || !remainder.is(Items.BUCKET)) return Optional.empty();
        return FluidUtil.getFluidContained(item);
    }

    /**
     * Exact, but any durability of a damageable item (fuzzy). Special recipes stay exact when an item is used up: their
     * result can depend on it, as repairing does on the durability.
     */
    private Ingredient ingredient(CraftingRecipe recipe, ItemStack item, boolean fuzzy) {
        if (substitutes && !recipe.isSpecial()) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.test(item)) return ingredient;
            }
        }
        return fuzzy && item.isDamageableItem() ? Ingredient.of(item.getItem()) : RecipeHelper.makeItemIngredient(item);
    }

    private static void addInput(List<Input> inputs, ItemStack item, Ingredient ingredient) {
        for (Input other : inputs) {
            if (ItemStack.isSameItemSameComponents(other.item(), item)) {
                other.item().grow(item.getCount());
                return;
            }
        }
        inputs.add(new Input(item.copy(), ingredient));
    }

    private static void merge(List<ItemStack> stacks, ItemStack stack) {
        for (ItemStack other : stacks) {
            if (ItemStack.isSameItemSameComponents(other, stack)) {
                other.grow(stack.getCount());
                return;
            }
        }
        stacks.add(stack.copy());
    }

    private static void merge(List<FluidStack> stacks, FluidStack stack) {
        for (FluidStack other : stacks) {
            if (FluidStack.isSameFluidSameComponents(other, stack)) {
                other.grow(stack.getAmount());
                return;
            }
        }
        stacks.add(stack.copy());
    }

    /** The pattern on this item, or null when it holds none. */
    public static @Nullable CraftingPattern read(ItemStack stack, HolderLookup.Provider registries) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(RECIPE_KEY)) return null;
        CompoundTag tag = data.copyTag();
        ListTag list = tag.getList(INPUTS_KEY, Tag.TAG_COMPOUND);
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            inputs.add(ItemStack.parseOptional(registries, list.getCompound(slot)));
        }
        ResourceLocation recipeId = ResourceLocation.tryParse(tag.getString(RECIPE_KEY));
        if (recipeId == null) return null;
        return new CraftingPattern(Mode.values()[tag.getInt(MODE_KEY)], inputs, recipeId, tag.getBoolean(FLUIDS_KEY),
                tag.getBoolean(SUBSTITUTES_KEY));
    }

    /** Puts this pattern on the item. */
    public void write(ItemStack stack, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ItemStack input : inputs) {
            list.add(input.isEmpty() ? new CompoundTag() : input.save(registries));
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt(MODE_KEY, mode.ordinal());
        tag.put(INPUTS_KEY, list);
        tag.putString(RECIPE_KEY, recipeId.toString());
        tag.putBoolean(FLUIDS_KEY, fluids);
        tag.putBoolean(SUBSTITUTES_KEY, substitutes);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
