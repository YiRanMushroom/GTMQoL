package com.yiran.minecraft.gtmqol.common.crafter;

import com.yiran.minecraft.gtmqol.core.mixins.SmithingTransformRecipeAccessor;
import com.yiran.minecraft.gtmqol.core.mixins.SmithingTrimRecipeAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

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

    private static final String TAG_KEY = "gtmqol_pattern";
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

    /** A crafting grid needs a menu to tell of changes; nobody listens here. */
    private static final class NoMenu extends AbstractContainerMenu {

        NoMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static CraftingContainer craftingInput(List<ItemStack> inputs) {
        CraftingContainer container = new TransientCraftingContainer(new NoMenu(), SIZE, SIZE);
        for (int slot = 0; slot < SLOTS; slot++) container.setItem(slot, inputs.get(slot));
        return container;
    }

    private static Container smithingInput(List<ItemStack> inputs) {
        return new SimpleContainer(inputs.get(SMITHING_SLOTS[0]), inputs.get(SMITHING_SLOTS[1]),
                inputs.get(SMITHING_SLOTS[2]));
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
        Optional<? extends Recipe<?>> recipe = switch (mode) {
            case CRAFTING -> inputs.stream().allMatch(ItemStack::isEmpty) ? Optional.empty() :
                    recipes.getRecipeFor(RecipeType.CRAFTING, craftingInput(inputs), level);
            case STONECUTTING -> {
                var matching = stonecutting(inputs, level);
                yield matching.isEmpty() ? Optional.empty() :
                        Optional.of(matching.get(Math.floorMod(choice, matching.size())));
            }
            case SMITHING -> recipes.getRecipeFor(RecipeType.SMITHING, smithingInput(inputs), level);
        };
        boolean crafting = mode == Mode.CRAFTING;
        return recipe.map(found -> new CraftingPattern(mode, inputs, found.getId(), crafting && fluids,
                crafting && substitutes)).orElse(null);
    }

    /** The stonecutting recipes of the grid's item, in the same order on both sides. */
    public static List<StonecutterRecipe> stonecutting(List<ItemStack> grid, Level level) {
        ItemStack item = grid.get(STONECUTTING_SLOT);
        if (item.isEmpty()) return List.of();
        return level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SimpleContainer(item), level);
    }

    /** Which of the grid's stonecutting recipes this is; 0 for other recipes. */
    public static int choice(ResourceLocation recipeId, List<ItemStack> grid, Level level) {
        var matching = stonecutting(grid, level);
        for (int i = 0; i < matching.size(); i++) {
            if (matching.get(i).getId().equals(recipeId)) return i;
        }
        return 0;
    }

    /** A recipe laid out on the grid, the first choice of each slot; for the recipe viewers' transfer. */
    public static List<ItemStack> grid(Recipe<?> recipe) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(SLOTS, ItemStack.EMPTY));
        if (recipe instanceof StonecutterRecipe) {
            place(grid, STONECUTTING_SLOT, recipe.getIngredients().get(0));
        } else if (recipe instanceof SmithingTransformRecipe) {
            var smithing = (SmithingTransformRecipeAccessor) recipe;
            place(grid, SMITHING_SLOTS[0], smithing.gtmqol$template());
            place(grid, SMITHING_SLOTS[1], smithing.gtmqol$base());
            place(grid, SMITHING_SLOTS[2], smithing.gtmqol$addition());
        } else if (recipe instanceof SmithingTrimRecipe) {
            var smithing = (SmithingTrimRecipeAccessor) recipe;
            place(grid, SMITHING_SLOTS[0], smithing.gtmqol$template());
            place(grid, SMITHING_SLOTS[1], smithing.gtmqol$base());
            place(grid, SMITHING_SLOTS[2], smithing.gtmqol$addition());
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
        Recipe<?> found = level.getRecipeManager().byKey(recipeId).orElse(null);
        if (found == null) return null;
        return switch (mode) {
            case CRAFTING -> found instanceof CraftingRecipe recipe ? craftingPlan(recipe, level) : null;
            case STONECUTTING -> found instanceof StonecutterRecipe recipe ?
                    exactPlan(recipe, new SimpleContainer(inputs.get(STONECUTTING_SLOT)), level) : null;
            case SMITHING -> found instanceof SmithingRecipe recipe ?
                    exactPlan(recipe, smithingInput(inputs), level) : null;
        };
    }

    /**
     * Stonecutting and smithing: everything is used up, nothing comes back, and every input counts exactly: a smithed
     * item keeps the base item's durability and enchantments.
     */
    private @Nullable Plan exactPlan(Recipe<Container> recipe, Container input, Level level) {
        if (!recipe.matches(input, level)) return null;
        ItemStack crafted = recipe.assemble(input, level.registryAccess());
        if (crafted.isEmpty()) return null;
        List<SlotUse> uses = new ArrayList<>();
        List<Input> consumed = new ArrayList<>();
        for (ItemStack item : inputs) {
            uses.add(item.isEmpty() ? SlotUse.EMPTY : SlotUse.CONSUMED);
            if (!item.isEmpty()) addInput(consumed, item, exact(item));
        }
        return new Plan(uses, consumed, List.of(), List.of(), List.of(crafted));
    }

    private @Nullable Plan craftingPlan(CraftingRecipe recipe, Level level) {
        CraftingContainer input = craftingInput(inputs);
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
            ItemStack remainder = remaining.get(slot);
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

    /** Only this item with this NBT. */
    public static Ingredient exact(ItemStack item) {
        return item.hasTag() ? StrictNBTIngredient.of(item) : Ingredient.of(item);
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
        return fuzzy && item.isDamageableItem() ? Ingredient.of(item.getItem()) : exact(item);
    }

    private static void addInput(List<Input> inputs, ItemStack item, Ingredient ingredient) {
        for (Input other : inputs) {
            if (ItemStack.isSameItemSameTags(other.item(), item)) {
                other.item().grow(item.getCount());
                return;
            }
        }
        inputs.add(new Input(item.copy(), ingredient));
    }

    private static void merge(List<ItemStack> stacks, ItemStack stack) {
        for (ItemStack other : stacks) {
            if (ItemStack.isSameItemSameTags(other, stack)) {
                other.grow(stack.getCount());
                return;
            }
        }
        stacks.add(stack.copy());
    }

    private static void merge(List<FluidStack> stacks, FluidStack stack) {
        for (FluidStack other : stacks) {
            if (other.isFluidEqual(stack)) {
                other.grow(stack.getAmount());
                return;
            }
        }
        stacks.add(stack.copy());
    }

    /** The pattern on this item, or null when it holds none. */
    public static @Nullable CraftingPattern read(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(TAG_KEY);
        if (tag == null) return null;
        ListTag list = tag.getList(INPUTS_KEY, Tag.TAG_COMPOUND);
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) inputs.add(ItemStack.of(list.getCompound(slot)));
        ResourceLocation recipeId = ResourceLocation.tryParse(tag.getString(RECIPE_KEY));
        if (recipeId == null) return null;
        return new CraftingPattern(Mode.values()[tag.getInt(MODE_KEY)], inputs, recipeId, tag.getBoolean(FLUIDS_KEY),
                tag.getBoolean(SUBSTITUTES_KEY));
    }

    /** Puts this pattern on the item. */
    public void write(ItemStack stack) {
        ListTag list = new ListTag();
        for (ItemStack input : inputs) list.add(input.save(new CompoundTag()));
        CompoundTag tag = stack.getOrCreateTagElement(TAG_KEY);
        tag.putInt(MODE_KEY, mode.ordinal());
        tag.put(INPUTS_KEY, list);
        tag.putString(RECIPE_KEY, recipeId.toString());
        tag.putBoolean(FLUIDS_KEY, fluids);
        tag.putBoolean(SUBSTITUTES_KEY, substitutes);
    }
}
