package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import com.yiran.minecraft.gtmqol.integration.ae2.batch.CraftingSolver.Recipe;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.CraftingSolver.Result;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.CraftingSolver.Stack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Recipes and stock for tests, with two independent judges: {@link #runPlan} plays a plan out the way a crafting CPU
 * does, and {@link #referenceCrafts} follows AE2's tree walk from CraftingTreeNode.request step by step.
 */
final class TestWorld implements CraftingSolver.Source<String, String> {

    final Map<String, List<Recipe<String, String>>> recipes = new HashMap<>();
    final Map<String, Long> stock = new HashMap<>();
    final Set<String> emitable = new HashSet<>();
    private final Map<String, Recipe<String, String>> byPattern = new HashMap<>();
    private final Map<String, String> outputOf = new HashMap<>();
    private int patterns;

    /** {@code recipe("gear", 1, "plate", 4, "rod", 1)}: what, how many a craft makes, then input and amount in turns. */
    TestWorld recipe(String what, long output, Object... inputs) {
        return add(what, output, List.of(), true, false, inputs);
    }

    TestWorld recipeWithByproduct(String what, long output, String byproduct, long amount, Object... inputs) {
        return add(what, output, List.of(new Stack<>(byproduct, amount)), true, false, inputs);
    }

    TestWorld unplannableRecipe(String what, long output, Object... inputs) {
        return add(what, output, List.of(), false, false, inputs);
    }

    TestWorld substitutingRecipe(String what, long output, Object... inputs) {
        return add(what, output, List.of(), true, true, inputs);
    }

    private TestWorld add(String what, long output, List<Stack<String>> byproducts, boolean plannable,
                          boolean substitutes, Object... inputs) {
        List<Stack<String>> stacks = new ArrayList<>();
        for (int i = 0; i < inputs.length; i += 2) {
            stacks.add(new Stack<>((String) inputs[i], ((Number) inputs[i + 1]).longValue()));
        }
        String pattern = what + "#" + patterns++;
        Recipe<String, String> recipe = new Recipe<>(pattern, stacks, output, byproducts, plannable, substitutes);
        recipes.computeIfAbsent(what, key -> new ArrayList<>()).add(recipe);
        byPattern.put(pattern, recipe);
        outputOf.put(pattern, what);
        return this;
    }

    TestWorld stock(String what, long amount) {
        stock.put(what, amount);
        return this;
    }

    TestWorld emit(String what) {
        emitable.add(what);
        return this;
    }

    @Override
    public long stock(String what) {
        return stock.getOrDefault(what, 0L);
    }

    @Override
    public boolean canEmit(String what) {
        return emitable.contains(what);
    }

    @Override
    public List<Recipe<String, String>> recipes(String what) {
        return recipes.getOrDefault(what, List.of());
    }

    Result<String, String> solve(String what, long amount) {
        return CraftingSolver.solve(this, what, amount);
    }

    // ---------- judge: playing a plan out ----------

    /** Asserts that a plan is sound: it takes what exists, adds up, and a CPU gets through it without getting stuck. */
    void assertRuns(String what, long amount, Result<String, String> plan, Random random) {
        assertEquals(CraftingSolver.Status.PLANNED, plan.status());
        plan.used().forEach((key, used) -> assertTrue(used <= stock(key), key + ": took more than there is"));
        assertAddsUp(what, amount, plan);
        runPlan(what, amount, plan, 0, random);
        runPlan(what, amount, plan, 1, random);
        runPlan(what, amount, plan, 16, random);
    }

    private void assertAddsUp(String what, long amount, Result<String, String> plan) {
        Map<String, Long> balance = new HashMap<>(plan.used());
        plan.emitted().forEach((key, emitted) -> balance.merge(key, emitted, Long::sum));
        balance.merge(what, -amount, Long::sum);
        plan.crafts().forEach((pattern, crafts) -> {
            Recipe<String, String> recipe = byPattern.get(pattern);
            balance.merge(outputOf.get(pattern), recipe.output() * crafts, Long::sum);
            for (Stack<String> input : recipe.inputs()) balance.merge(input.what(), -input.amount() * crafts, Long::sum);
            for (Stack<String> byproduct : recipe.byproducts()) {
                balance.merge(byproduct.what(), byproduct.amount() * crafts, Long::sum);
            }
        });
        balance.forEach((key, left) -> assertTrue(left >= 0, "plan is short of " + key + ": " + balance));
    }

    private record Piece(String pattern, long crafts) {}

    /** Inputs are all there before a piece starts, as the CPU extracts them; the order is whatever can go. */
    /** @param parts as the config, 0 for no pieces */
    private void runPlan(String what, long amount, Result<String, String> plan, int parts, Random random) {
        boolean inPieces = parts > 0;
        Map<String, Long> cpu = new HashMap<>(plan.used());
        plan.emitted().forEach((key, emitted) -> cpu.merge(key, emitted, Long::sum));
        List<Piece> pieces = new ArrayList<>();
        plan.crafts().forEach((pattern, count) -> {
            if (inPieces) {
                BatchSizes.split(count, Long.MAX_VALUE, parts)
                        .forEach((size, times) -> {
                            for (long i = 0; i < times; i++) pieces.add(new Piece(pattern, size));
                        });
            } else {
                pieces.add(new Piece(pattern, count));
            }
        });
        // single crafts would be a very long list: a whole pattern goes as one when it can, else piece by piece
        while (!pieces.isEmpty()) {
            List<Piece> ready = new ArrayList<>();
            for (Piece piece : pieces) {
                if (canRun(piece, cpu)) ready.add(piece);
            }
            if (ready.isEmpty()) {
                if (!inPieces) {
                    Piece split = splitOne(pieces, cpu);
                    if (split != null) continue;
                }
                fail("Stuck with " + pieces + " and " + cpu + " for " + amount + " " + what);
            }
            Piece piece = ready.get(random.nextInt(ready.size()));
            pieces.remove(piece);
            run(piece, cpu);
        }
        assertTrue(cpu.getOrDefault(what, 0L) >= amount, "made too little: " + cpu + " of " + amount + " " + what);
        cpu.forEach((key, left) -> assertTrue(left >= 0, key + " went negative"));
    }

    /** A pattern can need its own product from an earlier part of the plan: run what can go now of the first one. */
    private Piece splitOne(List<Piece> pieces, Map<String, Long> cpu) {
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            long can = piece.crafts();
            for (Stack<String> input : byPattern.get(piece.pattern()).inputs()) {
                can = Math.min(can, cpu.getOrDefault(input.what(), 0L) / input.amount());
            }
            if (can <= 0) continue;
            pieces.set(i, new Piece(piece.pattern(), piece.crafts() - can));
            run(new Piece(piece.pattern(), can), cpu);
            return piece;
        }
        return null;
    }

    private boolean canRun(Piece piece, Map<String, Long> cpu) {
        for (Stack<String> input : byPattern.get(piece.pattern()).inputs()) {
            if (cpu.getOrDefault(input.what(), 0L) < input.amount() * piece.crafts()) return false;
        }
        return true;
    }

    private void run(Piece piece, Map<String, Long> cpu) {
        Recipe<String, String> recipe = byPattern.get(piece.pattern());
        for (Stack<String> input : recipe.inputs()) cpu.merge(input.what(), -input.amount() * piece.crafts(), Long::sum);
        cpu.merge(outputOf.get(piece.pattern()), recipe.output() * piece.crafts(), Long::sum);
        for (Stack<String> byproduct : recipe.byproducts()) {
            cpu.merge(byproduct.what(), byproduct.amount() * piece.crafts(), Long::sum);
        }
    }

    // ---------- judge: AE2's walk ----------

    private static final class Failure extends RuntimeException {

        static final Failure INSTANCE = new Failure();

        private Failure() {
            super(null, null, false, false);
        }
    }

    private static final class State {

        final Map<String, Long> stock;
        final Map<String, Long> crafts;

        State(Map<String, Long> stock, Map<String, Long> crafts) {
            this.stock = new HashMap<>(stock);
            this.crafts = new HashMap<>(crafts);
        }
    }

    long referenceSteps;

    /** AE2's crafts for this request on a world without loops, or null when it would say something is missing. */
    Map<String, Long> referenceCrafts(String what, long amount) {
        State state = new State(stock, Map.of());
        state.stock.put(what, 0L);
        referenceSteps = 0;
        try {
            request(state, what, amount);
            return state.crafts;
        } catch (Failure failure) {
            return null;
        }
    }

    private void request(State state, String what, long amount) {
        long extracted = Math.min(state.stock.getOrDefault(what, 0L), amount);
        state.stock.merge(what, -extracted, Long::sum);
        long left = amount - extracted;
        if (left == 0) return;
        if (emitable.contains(what)) return;
        var options = recipes(what);
        if (options.size() == 1) {
            Recipe<String, String> recipe = options.get(0);
            long times = (left + recipe.output() - 1) / recipe.output();
            craft(state, recipe, times);
            state.stock.merge(what, recipe.output() * times - left, Long::sum);
            return;
        }
        for (Recipe<String, String> recipe : options) {
            try {
                while (left > 0) {
                    State child = new State(state.stock, state.crafts);
                    craft(child, recipe, 1);
                    state.stock.clear();
                    state.stock.putAll(child.stock);
                    state.crafts.clear();
                    state.crafts.putAll(child.crafts);
                    long took = Math.min(recipe.output(), left);
                    state.stock.merge(what, recipe.output() - took, Long::sum);
                    left -= took;
                }
                return;
            } catch (Failure failure) {
                // on to the next pattern
            }
        }
        throw Failure.INSTANCE;
    }

    private void craft(State state, Recipe<String, String> recipe, long times) {
        referenceSteps++;
        for (Stack<String> input : recipe.inputs()) request(state, input.what(), input.amount() * times);
        for (Stack<String> byproduct : recipe.byproducts()) {
            state.stock.merge(byproduct.what(), byproduct.amount() * times, Long::sum);
        }
        state.crafts.merge(recipe.pattern(), times, Long::sum);
    }

    // ---------- random worlds ----------

    /**
     * @param depth      layers of crafted items; the last has the one item that gets requested
     * @param alternates chance of an item having a second pattern
     * @param byproducts chance of a pattern giving something else too, always something that comes later
     */
    static TestWorld random(Random random, int depth, int width, double alternates, double byproducts,
                            double doubleOutput) {
        TestWorld world = new TestWorld();
        int rawCount = 3 + random.nextInt(4);
        List<String> all = new ArrayList<>();
        for (int i = 0; i < rawCount; i++) all.add("raw" + i);
        List<String> previous = new ArrayList<>(all);
        for (int layer = 0; layer < depth; layer++) {
            int count = layer == depth - 1 ? 1 : width;
            List<String> current = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String item = layer == depth - 1 ? "root" : "i" + layer + "_" + i;
                int patterns = random.nextDouble() < alternates ? 2 : 1;
                for (int p = 0; p < patterns; p++) {
                    int inputCount = 1 + random.nextInt(3);
                    List<Object> inputs = new ArrayList<>();
                    Set<String> seen = new HashSet<>();
                    for (int j = 0; j < inputCount; j++) {
                        List<String> pool = random.nextDouble() < 0.75 ? previous : all;
                        String input = pool.get(random.nextInt(pool.size()));
                        if (seen.add(input)) {
                            inputs.add(input);
                            inputs.add((long) (1 + random.nextInt(3)));
                        }
                    }
                    long output = random.nextDouble() < doubleOutput ? 2 : 1;
                    if (random.nextDouble() < byproducts) {
                        world.recipeWithByproduct(item, output, all.get(random.nextInt(all.size())),
                                1 + random.nextInt(2), inputs.toArray());
                    } else {
                        world.recipe(item, output, inputs.toArray());
                    }
                }
                current.add(item);
            }
            all.addAll(current);
            previous = current;
        }
        return world;
    }

    /**
     * Turns a world into one with loops: items that are made from each other, as ingots and blocks, and byproducts of
     * anything, also of what is made from them.
     */
    void addLoops(Random random, int reversals, int strayByproducts) {
        List<String> items = new ArrayList<>(recipes.keySet());
        items.sort(String::compareTo);
        List<String> all = new ArrayList<>(items);
        for (var options : recipes.values()) {
            for (var recipe : options) {
                for (var input : recipe.inputs()) {
                    if (!all.contains(input.what())) all.add(input.what());
                }
            }
        }
        all.sort(String::compareTo);
        for (int i = 0; i < reversals; i++) {
            String made = all.get(random.nextInt(all.size()));
            String from = all.get(random.nextInt(all.size()));
            if (!made.equals(from) && !made.equals("root")) recipe(made, 1 + random.nextInt(3), from, 1 + random.nextInt(3));
        }
        for (int i = 0; i < strayByproducts; i++) {
            String made = items.get(random.nextInt(items.size()));
            String from = all.get(random.nextInt(all.size()));
            String byproduct = all.get(random.nextInt(all.size()));
            if (!made.equals(byproduct) && !from.equals(made)) {
                recipeWithByproduct(made, 1, byproduct, 1 + random.nextInt(2), from, 1 + random.nextInt(3));
            }
        }
    }

    /** Stock around what the request takes, so that some requests can be made and some can't. */
    void randomStock(Random random, long amount) {
        Set<String> raw = new HashSet<>();
        Set<String> crafted = recipes.keySet();
        for (var options : recipes.values()) {
            for (var recipe : options) {
                for (var input : recipe.inputs()) {
                    if (!crafted.contains(input.what())) raw.add(input.what());
                }
            }
        }
        Map<String, Long> first = firstChoiceNeeds(amount);
        double level = 0.45 + random.nextDouble() * 0.7;
        for (String item : raw) {
            stock.put(item, (long) (first.getOrDefault(item, amount) * level * (0.7 + random.nextDouble() * 0.9)));
        }
        for (String item : crafted) {
            if (!item.equals("root") && random.nextDouble() < 0.3) stock.put(item, (long) random.nextInt((int) amount + 1));
        }
    }

    private Map<String, Long> firstChoiceNeeds(long amount) {
        Map<String, Long> needs = new HashMap<>();
        addNeeds("root", amount, needs, new HashSet<>());
        return needs;
    }

    private void addNeeds(String what, long amount, Map<String, Long> needs, Set<String> path) {
        var options = recipes(what);
        if (options.isEmpty() || !path.add(what)) {
            needs.merge(what, amount, Long::sum);
            return;
        }
        var recipe = options.get(0);
        long times = (amount + recipe.output() - 1) / recipe.output();
        for (var input : recipe.inputs()) addNeeds(input.what(), input.amount() * times, needs, path);
        path.remove(what);
    }
}
