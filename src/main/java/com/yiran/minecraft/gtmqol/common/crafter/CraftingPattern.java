package com.yiran.minecraft.gtmqol.common.crafter;

import com.yiran.minecraft.gtmqol.core.mixins.SmithingTransformRecipeAccessor;
import com.yiran.minecraft.gtmqol.core.mixins.SmithingTrimRecipeAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * What an encoded pattern holds, like AE2's patterns: a 3 x 3 grid (stonecutting and smithing use part of it) and the
 * recipe it matched. One craft is simulated when encoding and its result kept on the item ({@code uses}, {@code
 * outputs}), so a bottle becoming an empty bottle costs nothing afterwards. Only inputs that change with every craft,
 * like tools, are simulated while running ({@link PatternWear}). A pattern whose recipe changed doesn't run, as in AE2.
 * The recipe is found by its id, or else by the grid: a recipe a script adds without an id can get another one on the
 * next load.
 *
 * @param fluids      crafting only: take buckets as their fluid, as AE2's fluid substitution
 * @param substitutes crafting and smithing: accept whatever the recipe accepts in a slot, at any data, as AE2's
 *                    substitution (smithing: the template and the addition, the base stays exact); else exactly the
 *                    encoded items, durability included
 * @param duplicates  crafting only: a recipe making more of an input than it uses (smithing templates) keeps that input
 *                    and makes only what it adds
 * @param uses        per grid slot
 * @param outputs     the crafted item first, then what the recipe gives back for good
 */
public record CraftingPattern(Mode mode, List<ItemStack> inputs, ResourceLocation recipeId, boolean fluids,
                              boolean substitutes, boolean duplicates, List<SlotUse> uses, List<ItemStack> outputs) {

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
    private static final String DUPLICATES_KEY = "duplicates";
    private static final String USES_KEY = "uses";
    private static final String OUTPUTS_KEY = "outputs";

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
        /** Needed and comes back as it was. */
        KEPT,
        /** A bucket, taken as its fluid. */
        FLUID,
        /** Needed and stays in the machine, but comes back changed: a tool wears out with every craft. */
        CHANGED
    }

    /**
     * An input as encoded, with its amount in {@code item}, and what the machine accepts for it.
     *
     * @param slot the first grid slot it is in
     */
    public record Input(ItemStack item, Ingredient ingredient, int slot, SlotUse use) {}

    /**
     * One craft.
     *
     * @param kept kept and changed inputs; they come first in {@link #itemInputs}
     */
    public record Plan(List<SlotUse> uses, List<Input> consumed, List<Input> kept, List<FluidStack> fluids,
                       List<ItemStack> outputs, List<ItemStack> grid, Recipe<?> recipe) implements ICrafterPattern {

        @Override
        public List<ItemInput> itemInputs() {
            List<ItemInput> inputs = new ArrayList<>();
            for (Input input : kept) {
                inputs.add(new ItemInput(input.ingredient(), 1, false, input.use() == SlotUse.CHANGED));
            }
            for (Input input : consumed) {
                inputs.add(new ItemInput(input.ingredient(), input.item().getCount(), true, false));
            }
            return inputs;
        }

        @Override
        public List<FluidStack> fluidInputs() {
            return fluids;
        }

        @Override
        public ItemStack afterCraft(int input, ItemStack item) {
            int slot = kept.get(input).slot();
            List<ItemStack> changed = new ArrayList<>(grid);
            changed.set(slot, item);
            return remainders((CraftingRecipe) recipe, changed).get(slot);
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
                                                   boolean substitutes, boolean duplicates, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack item = grid.get(slot);
            inputs.add(item.isEmpty() || !mode.uses(slot) ? ItemStack.EMPTY : item.copyWithCount(1));
        }
        var recipes = level.getRecipeManager();
        Optional<? extends Recipe<?>> found = switch (mode) {
            case CRAFTING -> inputs.stream().allMatch(ItemStack::isEmpty) ? Optional.empty() :
                    recipes.getRecipeFor(RecipeType.CRAFTING, craftingInput(inputs), level);
            case STONECUTTING -> {
                var matching = stonecutting(inputs, level);
                yield matching.isEmpty() ? Optional.empty() :
                        Optional.of(matching.get(Math.floorMod(choice, matching.size())));
            }
            case SMITHING -> recipes.getRecipeFor(RecipeType.SMITHING, smithingInput(inputs), level);
        };
        return found.isEmpty() ? null :
                simulate(mode, inputs, found.get(), fluids, substitutes, duplicates, level);
    }

    /** One craft of this recipe on this grid; null when it doesn't match. */
    private static @Nullable CraftingPattern simulate(Mode mode, List<ItemStack> inputs, Recipe<?> recipe,
                                                      boolean fluids, boolean substitutes, boolean duplicates,
                                                      Level level) {
        boolean crafting = mode == Mode.CRAFTING;
        List<SlotUse> uses = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        ItemStack crafted = switch (mode) {
            case CRAFTING -> assemble((CraftingRecipe) recipe, craftingInput(inputs), level);
            case STONECUTTING -> assemble((StonecutterRecipe) recipe,
                    new SimpleContainer(inputs.get(STONECUTTING_SLOT)), level);
            case SMITHING -> assemble((SmithingRecipe) recipe, smithingInput(inputs), level);
        };
        if (crafted.isEmpty()) return null;
        merge(outputs, crafted);
        if (crafting) {
            List<ItemStack> remaining = remainders((CraftingRecipe) recipe, inputs);
            for (int slot = 0; slot < SLOTS; slot++) {
                uses.add(use(inputs.get(slot), remaining.get(slot), fluids));
                if (uses.get(slot) == SlotUse.CONSUMED && !remaining.get(slot).isEmpty()) {
                    merge(outputs, remaining.get(slot));
                }
            }
            if (duplicates) keepDuplicated(inputs, uses, outputs.get(0));
        } else {
            for (ItemStack input : inputs) uses.add(input.isEmpty() ? SlotUse.EMPTY : SlotUse.CONSUMED);
        }
        return new CraftingPattern(mode, inputs, recipe.getId(), crafting && fluids,
                (crafting || mode == Mode.SMITHING) && substitutes, crafting && duplicates, uses, outputs);
    }

    private static <C extends Container> ItemStack assemble(Recipe<C> recipe, C input, Level level) {
        return recipe.matches(input, level) ? recipe.assemble(input, level.registryAccess()) : ItemStack.EMPTY;
    }

    /** The inputs a recipe makes more of stay in the machine, the output is only what it adds. */
    private static void keepDuplicated(List<ItemStack> inputs, List<SlotUse> uses, ItemStack crafted) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            if (uses.get(slot) == SlotUse.CONSUMED && ItemStack.isSameItemSameTags(inputs.get(slot), crafted)) {
                slots.add(slot);
            }
        }
        if (slots.isEmpty() || crafted.getCount() <= slots.size()) return;
        for (int slot : slots) uses.set(slot, SlotUse.KEPT);
        crafted.shrink(slots.size());
    }

    private static SlotUse use(ItemStack item, ItemStack remainder, boolean fluids) {
        if (item.isEmpty()) return SlotUse.EMPTY;
        if (fluids && bucketFluid(item, remainder)) return SlotUse.FLUID;
        if (!ItemStack.isSameItem(remainder, item)) return SlotUse.CONSUMED;
        return ItemStack.isSameItemSameTags(remainder, item) ? SlotUse.KEPT : SlotUse.CHANGED;
    }

    /** What the recipe leaves in each grid slot after crafting this grid. */
    private static List<ItemStack> remainders(CraftingRecipe recipe, List<ItemStack> grid) {
        return recipe.getRemainingItems(craftingInput(grid));
    }

    /** Only real buckets that give an empty bucket back, as in AE2: other containers can do anything when emptied. */
    private static boolean bucketFluid(ItemStack item, ItemStack remainder) {
        boolean bucket = item.getItem() instanceof BucketItem || item.getItem() instanceof MilkBucketItem;
        return bucket && remainder.getCount() == 1 && remainder.is(Items.BUCKET) &&
                FluidUtil.getFluidContained(item).isPresent();
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

    /** Whether a crafting recipe's slot takes more than one item, so its pattern needs tags to be of any use. */
    public static boolean hasChoices(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe &&
                recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.getItems().length > 1);
    }

    /** One craft of this pattern, or null when its recipe is gone or no longer does what was encoded. */
    public @Nullable Plan plan(Level level) {
        Recipe<?> recipe = recipe(level);
        if (recipe == null) return null;
        List<Input> consumed = new ArrayList<>();
        List<Input> kept = new ArrayList<>();
        List<FluidStack> fluidInputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack item = inputs.get(slot);
            SlotUse use = uses.get(slot);
            switch (use) {
                case EMPTY -> {}
                case FLUID -> FluidUtil.getFluidContained(item).ifPresent(fluid -> merge(fluidInputs, fluid));
                case CONSUMED -> addInput(consumed, item, ingredient(recipe, item, use, slot), slot);
                case KEPT, CHANGED -> {
                    if (kept.stream().noneMatch(other -> ItemStack.isSameItem(other.item(), item))) {
                        kept.add(new Input(item, ingredient(recipe, item, use, slot), slot, use));
                    }
                }
            }
        }
        return new Plan(uses, consumed, kept, fluidInputs, outputs, inputs, recipe);
    }

    /**
     * Whether the recipe still makes what is on the item: it can change and keep its id. Only the outputs are compared,
     * as what happens to a tool can come down to chance.
     */
    /**
     * The recipe of this pattern: the one with its id if it still makes what was encoded, else one that makes it from
     * the same grid. Recipes a script adds without an id can swap ids between loads: KubeJS names them after their
     * output and numbers those with the same one in the order it processes them, which isn't fixed.
     */
    // Untested: a pattern for a KubeJS recipe without an id (ATM10's netherite template duplication) keeps working after
    // a rejoin; smithing patterns with substitutes take other templates and additions the recipe accepts
    private @Nullable Recipe<?> recipe(Level level) {
        Recipe<?> byId = level.getRecipeManager().byKey(recipeId).orElse(null);
        if (byId != null && Mode.of(byId) == mode && stillMakes(byId, level)) return byId;
        for (Recipe<?> candidate : candidates(level)) {
            if (candidate != byId && stillMakes(candidate, level)) return candidate;
        }
        return null;
    }

    /** The recipes that match this pattern's grid. */
    private List<? extends Recipe<?>> candidates(Level level) {
        var recipes = level.getRecipeManager();
        return switch (mode) {
            case CRAFTING -> recipes.getRecipesFor(RecipeType.CRAFTING, craftingInput(inputs), level);
            case STONECUTTING -> stonecutting(inputs, level);
            case SMITHING -> recipes.getRecipesFor(RecipeType.SMITHING, smithingInput(inputs), level);
        };
    }

    private boolean stillMakes(Recipe<?> recipe, Level level) {
        CraftingPattern now = simulate(mode, inputs, recipe, fluids, substitutes, duplicates, level);
        if (now == null || now.outputs.size() != outputs.size()) return false;
        for (int i = 0; i < outputs.size(); i++) {
            if (!ItemStack.matches(now.outputs.get(i), outputs.get(i))) return false;
        }
        return true;
    }

    /** Only this item with this NBT. */
    public static Ingredient exact(ItemStack item) {
        return item.hasTag() ? StrictNBTIngredient.of(item) : Ingredient.of(item);
    }

    /**
     * Special recipes stay exact when an item is used up: their result can depend on it, as repairing does on the
     * durability, and the result was worked out for the encoded item.
     */
    private Ingredient ingredient(Recipe<?> recipe, ItemStack item, SlotUse use, int slot) {
        if (!substitutes || (recipe.isSpecial() && use == SlotUse.CONSUMED)) return exact(item);
        if (recipe instanceof SmithingTransformRecipe) {
            // the base's data (enchantments, damage) goes into the result, so it stays exact
            var smithing = (SmithingTransformRecipeAccessor) recipe;
            if (slot == SMITHING_SLOTS[0]) return smithing.gtmqol$template();
            if (slot == SMITHING_SLOTS[2]) return smithing.gtmqol$addition();
            return exact(item);
        }
        // a trim depends on every input
        if (recipe instanceof SmithingRecipe) return exact(item);
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.test(item)) return ingredient;
        }
        return Ingredient.of(item.getItem());
    }

    private static void addInput(List<Input> inputs, ItemStack item, Ingredient ingredient, int slot) {
        for (Input other : inputs) {
            if (ItemStack.isSameItemSameTags(other.item(), item)) {
                other.item().grow(item.getCount());
                return;
            }
        }
        inputs.add(new Input(item.copy(), ingredient, slot, SlotUse.CONSUMED));
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
        if (tag == null || !tag.contains(USES_KEY)) return null;
        ResourceLocation recipeId = ResourceLocation.tryParse(tag.getString(RECIPE_KEY));
        if (recipeId == null) return null;
        ListTag inputList = tag.getList(INPUTS_KEY, Tag.TAG_COMPOUND);
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) inputs.add(ItemStack.of(inputList.getCompound(slot)));
        List<ItemStack> outputs = new ArrayList<>();
        for (Tag output : tag.getList(OUTPUTS_KEY, Tag.TAG_COMPOUND)) outputs.add(ItemStack.of((CompoundTag) output));
        List<SlotUse> uses = Arrays.stream(tag.getIntArray(USES_KEY)).mapToObj(use -> SlotUse.values()[use]).toList();
        return new CraftingPattern(Mode.values()[tag.getInt(MODE_KEY)], inputs, recipeId, tag.getBoolean(FLUIDS_KEY),
                tag.getBoolean(SUBSTITUTES_KEY), tag.getBoolean(DUPLICATES_KEY), uses, outputs);
    }

    /** Puts this pattern on the item. */
    public void write(ItemStack stack) {
        ListTag inputList = new ListTag();
        for (ItemStack input : inputs) inputList.add(input.save(new CompoundTag()));
        ListTag outputList = new ListTag();
        for (ItemStack output : outputs) outputList.add(output.save(new CompoundTag()));
        CompoundTag tag = stack.getOrCreateTagElement(TAG_KEY);
        tag.putInt(MODE_KEY, mode.ordinal());
        tag.put(INPUTS_KEY, inputList);
        tag.putString(RECIPE_KEY, recipeId.toString());
        tag.putBoolean(FLUIDS_KEY, fluids);
        tag.putBoolean(SUBSTITUTES_KEY, substitutes);
        tag.putBoolean(DUPLICATES_KEY, duplicates);
        tag.put(USES_KEY, new IntArrayTag(uses.stream().mapToInt(SlotUse::ordinal).toArray()));
        tag.put(OUTPUTS_KEY, outputList);
    }
}
