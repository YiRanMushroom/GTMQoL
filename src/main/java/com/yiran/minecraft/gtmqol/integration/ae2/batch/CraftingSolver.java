package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works out a crafting plan without a tree. AE2 walks a tree of everything it needs, in which a shared ingredient comes
 * up again and again, and with several patterns for an item it tries them craft by craft. Here each item exists once:
 * items are put in an order where everything that uses an item comes before it, so one walk over them adds up what is
 * needed of each, and its crafts follow from a division. The amount makes no difference to the work.
 * <p>
 * An item takes its first recipe (the patterns of the highest priority) unless that one is limited. When a walk ends
 * short of something, crafts move to the next recipe of the nearest item above it that can do without it, by as many as
 * the shortage calls for, and everything below that item is worked out again. Dividing materials among recipes that
 * compete for them is a hard problem, so this is a rule of thumb: it can miss a plan that exists, as AE2 can, and each
 * finds some the other doesn't. With one recipe per item, or when the first recipes are enough, it makes AE2's plan.
 * <p>
 * Recipes that would come back to an item they are needed for are left out, as AE2 does. Byproducts count towards what
 * is needed of them, unless that forms a loop. Cases this can't judge are {@linkplain Status#REFUSED refused}, for AE2
 * to do them.
 * <p>
 * No game classes here, so tests can run this without the game.
 *
 * @param <K> what is crafted
 * @param <P> the pattern behind a recipe
 */
public final class CraftingSolver<K, P> {

    /** Walks before it gives up. */
    public static final int MAX_WALKS = 300;

    private static final long NO_LIMIT = Long.MAX_VALUE;

    public record Stack<K>(K what, long amount) {}

    /**
     * @param inputs      per craft
     * @param output      how many of the item a craft makes
     * @param byproducts  per craft
     * @param plannable   false: this can't be planned here, what it gives back changes as it runs
     * @param substitutes its inputs have alternatives, which only count in AE2's way of planning
     */
    public record Recipe<K, P>(P pattern, List<Stack<K>> inputs, long output, List<Stack<K>> byproducts,
                               boolean plannable, boolean substitutes) {}

    /** The ME network as the solver sees it. */
    public interface Source<K, P> {

        long stock(K what);

        boolean canEmit(K what);

        /** Best first. */
        List<Recipe<K, P>> recipes(K what);
    }

    public enum Status {
        PLANNED,
        /** Something is missing; the result says what, and what the rest would take. */
        MISSING,
        /** Not something this can judge. */
        REFUSED
    }

    public record Result<K, P>(Status status, Map<P, Long> crafts, Map<K, Long> used, Map<K, Long> emitted,
                               Map<K, Long> missing, int walks) {}

    private static final class RecipeState<K, P> {

        final Recipe<K, P> recipe;
        final List<Item<K, P>> inputs = new ArrayList<>();
        /** Byproducts that can be counted. */
        final List<Item<K, P>> credited = new ArrayList<>();
        final List<Long> creditedAmounts = new ArrayList<>();
        /** Byproducts that can't: what wants them can only be judged by playing it out. */
        final List<Item<K, P>> uncredited = new ArrayList<>();
        /** Left out: it needs something that needs this. */
        boolean looped;
        long limit = NO_LIMIT;
        long crafts;

        RecipeState(Recipe<K, P> recipe) {
            this.recipe = recipe;
        }

        double perOutput(double[] perItem) {
            double total = 0;
            for (int i = 0; i < inputs.size(); i++) {
                total += recipe.inputs().get(i).amount() * perItem[inputs.get(i).position];
            }
            return total / recipe.output();
        }
    }

    private static final class Item<K, P> {

        final K what;
        final long stock;
        final boolean canEmit;
        final List<RecipeState<K, P>> recipes = new ArrayList<>();
        int position;
        boolean onPath;
        int waitingFor;
        long needed;
        long credit;
        long stockLeft;

        Item(K what, long stock, boolean canEmit) {
            this.what = what;
            this.stock = stock;
            this.canEmit = canEmit;
        }

        int lastInUse() {
            int last = -1;
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).crafts > 0) last = i;
            }
            return last;
        }
    }

    private record Shortage<K, P>(Item<K, P> item, long amount) {}

    private final Source<K, P> source;
    private final Map<K, Item<K, P>> items = new LinkedHashMap<>();
    private List<Item<K, P>> order = List.of();

    private CraftingSolver(Source<K, P> source) {
        this.source = source;
    }

    public static <K, P> Result<K, P> solve(Source<K, P> source, K what, long amount) {
        try {
            return new CraftingSolver<>(source).solve(what, amount);
        } catch (ArithmeticException tooMuch) {
            return new Result<>(Status.REFUSED, Map.of(), Map.of(), Map.of(), Map.of(), 0);
        }
    }

    private Result<K, P> solve(K what, long amount) {
        Item<K, P> root = visit(what);
        order = order();
        List<Shortage<K, P>> shortages = List.of();
        int walks = 0;
        while (walks < MAX_WALKS) {
            walks++;
            shortages = walk(root, amount);
            if (shortages == null || dependsOnLateByproduct()) return refused(walks);
            if (shortages.isEmpty()) return result(Status.PLANNED, shortages, walks);
            if (!moveCrafts(shortages)) break;
        }
        return needsAE2Rules() ? refused(walks) : result(Status.MISSING, shortages, walks);
    }

    private Result<K, P> refused(int walks) {
        return new Result<>(Status.REFUSED, Map.of(), Map.of(), Map.of(), Map.of(), walks);
    }

    // ---------- building ----------

    /** Everything reachable. A recipe that needs an item on the current path would loop and stays out, as in AE2. */
    private Item<K, P> visit(K what) {
        Item<K, P> item = items.get(what);
        if (item != null) return item;
        item = new Item<>(what, source.stock(what), source.canEmit(what));
        items.put(what, item);
        item.onPath = true;
        if (!item.canEmit) {
            for (Recipe<K, P> recipe : source.recipes(what)) {
                RecipeState<K, P> state = new RecipeState<>(recipe);
                item.recipes.add(state);
                for (Stack<K> input : recipe.inputs()) {
                    Item<K, P> inputItem = items.get(input.what());
                    if (inputItem != null && inputItem.onPath) {
                        state.looped = true;
                        break;
                    }
                }
                if (state.looped) continue;
                for (Stack<K> input : recipe.inputs()) state.inputs.add(visit(input.what()));
            }
        }
        item.onPath = false;
        return item;
    }

    /**
     * Everything that uses an item before it, and what makes a byproduct before the item, which is the order the amounts
     * can be added up in. Without byproducts this always works out, because recipes that loop are left out. A byproduct
     * doesn't count when it stands in the way of that, or when it can't exist in time: the recipe needs it, somewhere
     * down its ingredients, before it is made.
     */
    private List<Item<K, P>> order() {
        for (Item<K, P> item : items.values()) {
            for (RecipeState<K, P> recipe : item.recipes) {
                if (recipe.looped) continue;
                for (Stack<K> byproduct : recipe.recipe.byproducts()) {
                    Item<K, P> made = items.get(byproduct.what());
                    if (made == null || made == item) continue;
                    recipe.credited.add(made);
                    recipe.creditedAmounts.add(byproduct.amount());
                }
            }
        }
        stopCountingUnplaced(sort(true));
        List<Item<K, P>> sorted = sort(false);
        if (sorted.size() < items.size()) {
            stopCountingUnplaced(sorted);
            sorted = sort(false);
        }
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).position = i;
        return sorted;
    }

    /** Whatever couldn't be placed is in or behind a loop; of those, only byproducts can give way. */
    private void stopCountingUnplaced(List<Item<K, P>> placedItems) {
        Set<Item<K, P>> placed = new HashSet<>(placedItems);
        for (Item<K, P> item : items.values()) {
            if (placed.contains(item)) continue;
            for (RecipeState<K, P> recipe : item.recipes) {
                for (int i = recipe.credited.size() - 1; i >= 0; i--) {
                    if (placed.contains(recipe.credited.get(i))) continue;
                    recipe.uncredited.add(recipe.credited.remove(i));
                    recipe.creditedAmounts.remove(i);
                }
            }
        }
    }

    /**
     * @param inTime what has to exist first comes first: inputs before the item, the item before its byproducts. Else
     *               the order of adding up: the item before its inputs and its byproducts.
     * @return as many items as can be put in that order
     */
    private List<Item<K, P>> sort(boolean inTime) {
        Map<Item<K, P>, List<Item<K, P>>> next = new HashMap<>();
        for (Item<K, P> item : items.values()) item.waitingFor = 0;
        for (Item<K, P> item : items.values()) {
            for (RecipeState<K, P> recipe : item.recipes) {
                if (recipe.looped) continue;
                for (Item<K, P> input : recipe.inputs) {
                    Item<K, P> first = inTime ? input : item;
                    Item<K, P> then = inTime ? item : input;
                    next.computeIfAbsent(first, key -> new ArrayList<>()).add(then);
                    then.waitingFor++;
                }
                for (Item<K, P> made : recipe.credited) {
                    next.computeIfAbsent(item, key -> new ArrayList<>()).add(made);
                    made.waitingFor++;
                }
            }
        }
        ArrayDeque<Item<K, P>> ready = new ArrayDeque<>();
        for (Item<K, P> item : items.values()) {
            if (item.waitingFor == 0) ready.add(item);
        }
        List<Item<K, P>> sorted = new ArrayList<>(items.size());
        while (!ready.isEmpty()) {
            Item<K, P> item = ready.poll();
            sorted.add(item);
            for (Item<K, P> then : next.getOrDefault(item, List.of())) {
                if (--then.waitingFor == 0) ready.add(then);
            }
        }
        return sorted;
    }

    // ---------- walking ----------

    /** One walk; what it fell short of, or null when this needs a recipe that can't be planned here. */
    private @Nullable List<Shortage<K, P>> walk(Item<K, P> root, long amount) {
        for (Item<K, P> item : order) {
            item.needed = 0;
            item.credit = 0;
            item.stockLeft = item.stock;
            for (RecipeState<K, P> recipe : item.recipes) recipe.crafts = 0;
        }
        root.needed = amount;
        List<Shortage<K, P>> shortages = new ArrayList<>();
        for (Item<K, P> item : order) {
            if (item.needed == 0) continue;
            long left = item.needed;
            long fromCredit = Math.min(left, item.credit);
            left -= fromCredit;
            long fromStock = Math.min(left, item.stockLeft);
            left -= fromStock;
            item.stockLeft -= fromStock;
            if (left == 0 || item.canEmit) continue;
            for (RecipeState<K, P> recipe : item.recipes) {
                if (left <= 0) break;
                if (recipe.looped || recipe.limit == 0) continue;
                if (!recipe.recipe.plannable()) return null;
                long output = recipe.recipe.output();
                long crafts = Math.min(recipe.limit, left / output + (left % output == 0 ? 0 : 1));
                recipe.crafts = crafts;
                left -= Math.multiplyExact(crafts, output);
                for (int i = 0; i < recipe.inputs.size(); i++) {
                    Item<K, P> input = recipe.inputs.get(i);
                    long needed = Math.multiplyExact(recipe.recipe.inputs().get(i).amount(), crafts);
                    input.needed = Math.addExact(input.needed, needed);
                }
                for (int i = 0; i < recipe.credited.size(); i++) {
                    Item<K, P> made = recipe.credited.get(i);
                    made.credit = Math.addExact(made.credit, Math.multiplyExact(recipe.creditedAmounts.get(i), crafts));
                }
            }
            if (left > 0) shortages.add(new Shortage<>(item, left));
        }
        return shortages;
    }

    /** Something this craft makes on the side is wanted, but can't be counted. */
    private boolean dependsOnLateByproduct() {
        for (Item<K, P> item : order) {
            for (RecipeState<K, P> recipe : item.recipes) {
                if (recipe.crafts == 0) continue;
                for (Item<K, P> byproduct : recipe.uncredited) {
                    if (byproduct.needed > 0) return true;
                }
            }
        }
        return false;
    }

    // ---------- moving crafts ----------

    /** Moves crafts away from what is short; false when nothing can. */
    private boolean moveCrafts(List<Shortage<K, P>> shortages) {
        List<Shortage<K, P>> topFirst = new ArrayList<>(shortages);
        topFirst.sort(Comparator.comparingInt(shortage -> shortage.item().position));
        for (Shortage<K, P> shortage : topFirst) {
            if (moveCrafts(shortage)) return true;
        }
        return false;
    }

    private boolean moveCrafts(Shortage<K, P> shortage) {
        int size = order.size();
        double[] used = new double[size];
        double[] usedByNext = new double[size];
        int target = shortage.item().position;
        // per item of each, how much of what is short it takes: what stopping it saves, what going on with it costs
        for (int position = size - 1; position >= 0; position--) {
            if (position == target) {
                used[position] = 1;
                usedByNext[position] = 1;
                continue;
            }
            Item<K, P> item = order.get(position);
            int last = item.lastInUse();
            if (last >= 0) used[position] = item.recipes.get(last).perOutput(used);
            double cheapest = Double.POSITIVE_INFINITY;
            for (int i = Math.max(last, 0); i < item.recipes.size(); i++) {
                RecipeState<K, P> recipe = item.recipes.get(i);
                if (!recipe.looped && recipe.limit > 0) cheapest = Math.min(cheapest, recipe.perOutput(usedByNext));
            }
            if (cheapest < Double.POSITIVE_INFINITY) usedByNext[position] = cheapest;
        }
        for (int position = target - 1; position >= 0; position--) {
            Item<K, P> item = order.get(position);
            for (int i = 0; i + 1 < item.recipes.size(); i++) {
                RecipeState<K, P> recipe = item.recipes.get(i);
                if (recipe.crafts == 0) continue;
                RecipeState<K, P> next = nextRecipe(item, i);
                if (next == null) continue;
                double saved = recipe.perOutput(used) - next.perOutput(usedByNext);
                if (saved <= 0) continue;
                long crafts = (long) Math.ceil(shortage.amount() / saved / recipe.recipe.output());
                crafts = Math.min(recipe.crafts, Math.max(crafts, 1));
                recipe.limit = recipe.crafts - crafts;
                // what is below this depends on it, and the shares found there no longer hold
                for (int below = position + 1; below < size; below++) {
                    for (RecipeState<K, P> lower : order.get(below).recipes) lower.limit = NO_LIMIT;
                }
                return true;
            }
        }
        return false;
    }

    private @Nullable RecipeState<K, P> nextRecipe(Item<K, P> item, int index) {
        for (int i = index + 1; i < item.recipes.size(); i++) {
            RecipeState<K, P> recipe = item.recipes.get(i);
            if (!recipe.looped && recipe.limit > 0) return recipe;
        }
        return null;
    }

    /** Short in the end, with something in play that only AE2 can make use of. */
    private boolean needsAE2Rules() {
        for (Item<K, P> item : order) {
            boolean madeHere = item.needed > item.stock + item.credit;
            for (RecipeState<K, P> recipe : item.recipes) {
                if (madeHere && recipe.looped) return true;
                if (recipe.crafts > 0 && recipe.recipe.substitutes()) return true;
            }
        }
        return false;
    }

    // ---------- result ----------

    private Result<K, P> result(Status status, List<Shortage<K, P>> shortages, int walks) {
        Map<P, Long> crafts = new LinkedHashMap<>();
        Map<K, Long> used = new LinkedHashMap<>();
        Map<K, Long> emitted = new LinkedHashMap<>();
        Map<K, Long> missing = new LinkedHashMap<>();
        for (Item<K, P> item : order) {
            for (RecipeState<K, P> recipe : item.recipes) {
                if (recipe.crafts > 0) crafts.merge(recipe.recipe.pattern(), recipe.crafts, Long::sum);
            }
            if (item.stock > item.stockLeft) used.put(item.what, item.stock - item.stockLeft);
            if (item.canEmit) {
                long emit = item.needed - Math.min(item.needed, item.credit) - (item.stock - item.stockLeft);
                if (emit > 0) emitted.put(item.what, emit);
            }
        }
        for (Shortage<K, P> shortage : shortages) missing.put(shortage.item().what, shortage.amount());
        return new Result<>(status, crafts, used, emitted, missing, walks);
    }
}
