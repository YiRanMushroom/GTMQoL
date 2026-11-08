package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver.Result;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver.Status;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingSolverTest {

    private static final Random RANDOM = new Random(1);

    private static void assertCrafts(Result<String, String> result, Object... patternAndCrafts) {
        Map<String, Long> expected = new HashMap<>();
        for (int i = 0; i < patternAndCrafts.length; i += 2) {
            expected.put((String) patternAndCrafts[i], ((Number) patternAndCrafts[i + 1]).longValue());
        }
        assertEquals(expected, result.crafts());
    }

    // ---------- one recipe per item ----------

    @Test
    void chainWithoutStock() {
        var world = new TestWorld().recipe("c", 1, "b", 1).recipe("b", 1, "a", 2).stock("a", 200);
        var result = world.solve("c", 100);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "c#0", 100, "b#1", 100);
        assertEquals(Map.of("a", 200L), result.used());
        world.assertRuns("c", 100, result, RANDOM);
    }

    @Test
    void stockInTheMiddleSavesWhatIsBelowIt() {
        var world = new TestWorld().recipe("c", 1, "b", 1).recipe("b", 1, "a", 1).stock("b", 30).stock("a", 70);
        var result = world.solve("c", 100);
        assertCrafts(result, "c#0", 100, "b#1", 70);
        assertEquals(Map.of("b", 30L, "a", 70L), result.used());
        world.assertRuns("c", 100, result, RANDOM);
    }

    @Test
    void sharedIngredientIsAddedUpBeforeItsStockIsUsed() {
        var world = new TestWorld()
                .recipe("top", 1, "left", 1, "right", 1)
                .recipe("left", 1, "shared", 2)
                .recipe("right", 1, "shared", 3)
                .recipe("shared", 1, "raw", 1)
                .stock("shared", 5).stock("raw", 45);
        var result = world.solve("top", 10);
        assertCrafts(result, "top#0", 10, "left#1", 10, "right#2", 10, "shared#3", 45);
        world.assertRuns("top", 10, result, RANDOM);
    }

    @Test
    void outputsRoundUpOnTheTotalNotOnEachUser() {
        // two users of 1 each from a recipe that makes 4: one craft, not two
        var world = new TestWorld()
                .recipe("top", 1, "left", 1, "right", 1)
                .recipe("left", 1, "shared", 1)
                .recipe("right", 1, "shared", 1)
                .recipe("shared", 4, "raw", 1)
                .stock("raw", 1);
        var result = world.solve("top", 1);
        assertEquals(1L, result.crafts().get("shared#3"));
        world.assertRuns("top", 1, result, RANDOM);
    }

    @Test
    void multipleOutputsRoundUp() {
        var world = new TestWorld().recipe("b", 4, "a", 1).stock("a", 3);
        var result = world.solve("b", 9);
        assertCrafts(result, "b#0", 3);
        world.assertRuns("b", 9, result, RANDOM);
    }

    @Test
    void nothingMissingWhenOnlyStockIsUsed() {
        var world = new TestWorld().stock("a", 5);
        var result = world.solve("a", 5);
        assertEquals(Status.PLANNED, result.status());
        assertEquals(Map.of("a", 5L), result.used());
        assertTrue(result.crafts().isEmpty());
    }

    @Test
    void missingSaysHowMuchOfWhat() {
        var world = new TestWorld().recipe("b", 1, "a", 2).stock("a", 5);
        var result = world.solve("b", 10);
        assertEquals(Status.MISSING, result.status());
        assertEquals(Map.of("a", 15L), result.missing());
        assertCrafts(result, "b#0", 10);
        assertEquals(Map.of("a", 5L), result.used());
    }

    @Test
    void anItemNobodyMakesIsMissing() {
        var result = new TestWorld().solve("nothing", 3);
        assertEquals(Status.MISSING, result.status());
        assertEquals(Map.of("nothing", 3L), result.missing());
    }

    @Test
    void emitedItemsAfterStock() {
        var world = new TestWorld().recipe("b", 1, "a", 1).emit("a").stock("a", 4);
        var result = world.solve("b", 10);
        assertEquals(Status.PLANNED, result.status());
        assertEquals(Map.of("a", 4L), result.used());
        assertEquals(Map.of("a", 6L), result.emitted());
        world.assertRuns("b", 10, result, RANDOM);
    }

    @Test
    void theWorkDoesNotDependOnTheAmount() {
        var world = new TestWorld().recipe("c", 1, "b", 3).recipe("b", 2, "a", 7).stock("a", Long.MAX_VALUE / 8);
        var few = world.solve("c", 10);
        var many = world.solve("c", 1_000_000_000_000L);
        assertEquals(1, few.walks());
        assertEquals(1, many.walks());
        assertEquals(1_000_000_000_000L, many.crafts().get("c#0"));
        assertEquals(1_500_000_000_000L, many.crafts().get("b#1"));
    }

    @Test
    void numbersTooBigAreRefused() {
        var world = new TestWorld().recipe("c", 1, "b", Long.MAX_VALUE / 2).recipe("b", 1, "a", 8);
        assertEquals(Status.REFUSED, world.solve("c", 4).status());
    }

    // ---------- several recipes ----------

    @Test
    void firstRecipeIsUsedWhenItIsEnough() {
        var world = new TestWorld().recipe("b", 1, "good", 1).recipe("b", 1, "bad", 1).stock("good", 10).stock("bad", 10);
        assertCrafts(world.solve("b", 10), "b#0", 10);
    }

    @Test
    void secondRecipeTakesWhatTheFirstCannotDo() {
        var world = new TestWorld().recipe("b", 1, "good", 1).recipe("b", 1, "bad", 1).stock("good", 7).stock("bad", 10);
        var result = world.solve("b", 10);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "b#0", 7, "b#1", 3);
        world.assertRuns("b", 10, result, RANDOM);
    }

    @Test
    void missingWhenNoRecipeHelps() {
        var world = new TestWorld().recipe("b", 1, "good", 1).recipe("b", 1, "bad", 1).stock("good", 7).stock("bad", 1);
        assertEquals(Status.MISSING, world.solve("b", 10).status());
    }

    @Test
    void movesAlsoFromARecipeThatIsNotTheLastInUse() {
        // found by the first version of the shifting rule: it only looked at the last recipe in use
        var world = new TestWorld()
                .recipe("I3", 1, "R1", 3, "R2", 3)
                .recipe("I3", 1, "R2", 1)
                .recipe("I5", 1, "R0", 2, "R2", 3, "I3", 3)
                .stock("R0", 13).stock("R1", 9).stock("R2", 35);
        var result = world.solve("I5", 3);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "I5#2", 3, "I3#0", 3, "I3#1", 6);
        world.assertRuns("I5", 3, result, RANDOM);
    }

    @Test
    void costOfAnAlternativeLooksAtItsOwnAlternatives() {
        // the second top recipe seems to need more R0 than the first, until I4's second recipe is taken into account
        var world = new TestWorld()
                .recipe("I3", 1, "R0", 2, "R1", 3)
                .recipe("I4", 1, "R0", 3, "R2", 1)
                .recipe("I4", 1, "R2", 1)
                .recipe("I5", 1, "I3", 1)
                .recipe("I5", 1, "I4", 1)
                .stock("R1", 12).stock("R2", 14);
        var result = world.solve("I5", 2);
        assertEquals(Status.PLANNED, result.status());
        world.assertRuns("I5", 2, result, RANDOM);
    }

    @Test
    void lowerChoicesAreWorkedOutAgainAfterAHigherOneChanges() {
        var world = new TestWorld()
                .recipe("I3", 1, "R2", 3, "R1", 1)
                .recipe("I3", 1, "R0", 2)
                .recipe("I4", 1, "R1", 1)
                .recipe("I5", 1, "I3", 3, "I4", 2)
                .recipe("I5", 1, "I3", 3)
                .stock("R0", 22).stock("R1", 7).stock("R2", 5);
        var result = world.solve("I5", 4);
        assertEquals(Status.PLANNED, result.status());
        world.assertRuns("I5", 4, result, RANDOM);
    }

    @Test
    void competingUsersAreBothServed() {
        // AE2 gives everything to whoever comes first and can't take it back; here both get made
        var world = new TestWorld()
                .recipe("top", 1, "a", 1, "b", 1)
                .recipe("a", 1, "shared", 1)
                .recipe("a", 1, "other", 1)
                .recipe("b", 1, "shared", 1)
                .stock("shared", 10).stock("other", 10);
        var result = world.solve("top", 10);
        assertEquals(Status.PLANNED, result.status());
        assertEquals(null, world.referenceCrafts("top", 10));
        world.assertRuns("top", 10, result, RANDOM);
    }

    // ---------- loops ----------

    private static TestWorld ingotsAndBlocks() {
        return new TestWorld().recipe("block", 1, "ingot", 9).recipe("ingot", 9, "block", 1);
    }

    @Test
    void loopDoesNotMatterWithEnoughStock() {
        var world = ingotsAndBlocks().stock("ingot", 90);
        var result = world.solve("block", 10);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "block#0", 10);
        world.assertRuns("block", 10, result, RANDOM);
    }

    @Test
    void theOtherWayRoundToo() {
        var world = ingotsAndBlocks().stock("block", 3);
        var result = world.solve("ingot", 27);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "ingot#1", 3);
        world.assertRuns("ingot", 27, result, RANDOM);
    }

    @Test
    void loopOnAnItemThatFallsShortGoesToAE2() {
        // whether the recipe left out could have helped is AE2's to find out
        assertEquals(Status.REFUSED, ingotsAndBlocks().stock("ingot", 5).solve("block", 10).status());
    }

    @Test
    void loopElsewhereDoesNotStopAPlan() {
        var world = new TestWorld()
                .recipe("top", 1, "block", 1, "plate", 1)
                .recipe("block", 1, "ingot", 9)
                .recipe("ingot", 9, "block", 1)
                .recipe("plate", 1, "ingot", 1)
                .stock("ingot", 100);
        var result = world.solve("top", 10);
        assertEquals(Status.PLANNED, result.status());
        world.assertRuns("top", 10, result, RANDOM);
    }

    @Test
    void recipeThatNeedsItsOwnOutputIsLeftOut() {
        var world = new TestWorld().recipe("template", 2, "template", 1, "diamond", 7).stock("template", 5);
        assertEquals(Status.PLANNED, world.solve("template", 5).status());
        assertEquals(Status.REFUSED, world.solve("template", 6).status());
    }

    // ---------- byproducts ----------

    @Test
    void byproductCountsForWhatComesLater() {
        var world = new TestWorld()
                .recipe("top", 1, "a", 1, "side", 1)
                .recipeWithByproduct("a", 1, "side", 1, "raw", 1)
                .stock("raw", 10);
        var result = world.solve("top", 10);
        assertEquals(Status.PLANNED, result.status());
        assertTrue(result.used().get("raw") == 10);
        assertFalse(result.used().containsKey("side"));
        world.assertRuns("top", 10, result, RANDOM);
    }

    @Test
    void byproductSavesStock() {
        var world = new TestWorld()
                .recipe("top", 1, "a", 1, "side", 2)
                .recipeWithByproduct("a", 1, "side", 1, "raw", 1)
                .stock("raw", 10).stock("side", 100);
        var result = world.solve("top", 10);
        assertEquals(10L, result.used().get("side"));
        world.assertRuns("top", 10, result, RANDOM);
    }

    @Test
    void wantedByproductInALoopGoesToAE2() {
        // a's byproduct s is what b needs, and a needs b: s can only be counted by playing it out
        var world = new TestWorld()
                .recipeWithByproduct("a", 1, "s", 1, "b", 1)
                .recipe("b", 1, "s", 1)
                .stock("s", 3);
        assertEquals(Status.REFUSED, CraftingSolver.solve(new SwappedOrder(world), "a", 1).status());
    }

    /** Lists s as made from a, so that a has to come after s and before it at once. */
    private record SwappedOrder(TestWorld world) implements CraftingSolver.Source<String, String> {

        @Override
        public long stock(String what) {
            return world.stock(what);
        }

        @Override
        public boolean canEmit(String what) {
            return false;
        }

        @Override
        public java.util.List<CraftingSolver.Recipe<String, String>> recipes(String what) {
            if (what.equals("s")) {
                return java.util.List.of(new CraftingSolver.Recipe<>("s#loop", java.util.List.of(
                        new CraftingSolver.Stack<>("raw", 1L)), 1, java.util.List.of(
                                new CraftingSolver.Stack<>("a", 1L)), true, false));
            }
            return world.recipes(what);
        }
    }

    @Test
    void unwantedByproductInALoopDoesNotMatter() {
        // x is only reached through a recipe that isn't used; a makes x and x is made from a
        var world = new TestWorld()
                .recipe("top", 1, "a", 1)
                .recipe("top", 1, "x", 1)
                .recipeWithByproduct("a", 1, "x", 1, "raw", 1)
                .recipe("x", 1, "a", 1)
                .stock("raw", 5);
        var result = world.solve("top", 5);
        assertEquals(Status.PLANNED, result.status());
        assertCrafts(result, "top#0", 5, "a#2", 5);
        world.assertRuns("top", 5, result, RANDOM);
    }

    @Test
    void byproductCanNotPayForItsOwnRecipe() {
        // x needs s and gives s back: it can't start from nothing, however well the numbers add up
        var world = new TestWorld()
                .recipeWithByproduct("x", 1, "s", 1, "s", 1);
        assertEquals(Status.REFUSED, world.solve("x", 3).status());
        var deeper = new TestWorld()
                .recipe("top", 1, "x", 1, "s", 1)
                .recipeWithByproduct("x", 1, "s", 2, "y", 1)
                .recipe("y", 1, "s", 1);
        assertEquals(Status.REFUSED, deeper.solve("top", 3).status());
    }

    @Test
    void byproductOfALaterStepCanNotPayForAnEarlierOne() {
        var world = new TestWorld()
                .recipe("top", 1, "first", 1, "second", 1)
                .recipe("first", 1, "s", 1)
                .recipeWithByproduct("second", 1, "s", 1, "first", 1);
        assertEquals(Status.REFUSED, world.solve("top", 2).status());
    }

    // ---------- what only AE2 can do ----------

    @Test
    void recipeThatCannotBePlannedGoesToAE2OnlyWhenUsed() {
        var world = new TestWorld().recipe("b", 1, "a", 1).unplannableRecipe("a", 1, "bucket", 1).stock("a", 5);
        assertEquals(Status.PLANNED, world.solve("b", 5).status());
        assertEquals(Status.REFUSED, world.solve("b", 6).status());
    }

    @Test
    void alternativesInAnInputGoToAE2WhenShort() {
        var world = new TestWorld().substitutingRecipe("b", 1, "a", 1).stock("a", 5);
        assertEquals(Status.PLANNED, world.solve("b", 5).status());
        assertEquals(Status.REFUSED, world.solve("b", 6).status());
    }

    // ---------- against AE2's walk, on random worlds ----------

    @Test
    void withOneRecipeEachThePlanIsAE2s() {
        Random random = new Random(11);
        int compared = 0;
        for (int i = 0; i < 4000; i++) {
            TestWorld world = TestWorld.random(random, 2 + random.nextInt(6), 1 + random.nextInt(4), 0, 0, 0.2);
            long amount = 1 + random.nextInt(2000);
            world.randomStock(random, amount);
            var reference = world.referenceCrafts("root", amount);
            var result = world.solve("root", amount);
            if (reference == null) {
                assertEquals(Status.MISSING, result.status(), "world " + i);
                continue;
            }
            compared++;
            assertEquals(Status.PLANNED, result.status(), "world " + i);
            assertEquals(reference, result.crafts(), "world " + i);
            world.assertRuns("root", amount, result, random);
        }
        assertTrue(compared > 500, "too few worlds could be made: " + compared);
    }

    @Test
    void whenTheFirstRecipesAreEnoughThePlanIsAE2s() {
        Random random = new Random(12);
        int compared = 0;
        for (int i = 0; i < 4000; i++) {
            TestWorld world = TestWorld.random(random, 2 + random.nextInt(5), 1 + random.nextInt(4), 0.4, 0, 0.2);
            long amount = 1 + random.nextInt(300);
            world.randomStock(random, amount);
            var result = world.solve("root", amount);
            if (result.status() != Status.PLANNED || result.walks() != 1) continue;
            var reference = world.referenceCrafts("root", amount);
            compared++;
            assertEquals(reference, result.crafts(), "world " + i);
        }
        assertTrue(compared > 300, "too few worlds could be made: " + compared);
    }

    @Test
    void everyPlanRunsOnRandomWorlds() {
        Random random = new Random(13);
        int planned = 0;
        for (int i = 0; i < 20000; i++) {
            TestWorld world = TestWorld.random(random, 2 + random.nextInt(7), 1 + random.nextInt(4),
                    random.nextDouble() * 0.8, random.nextDouble() * 0.4, 0.2);
            long amount = 1 + random.nextInt(100000);
            world.randomStock(random, amount);
            var result = world.solve("root", amount);
            assertTrue(result.walks() <= CraftingSolver.MAX_WALKS);
            if (result.status() != Status.PLANNED) continue;
            planned++;
            world.assertRuns("root", amount, result, random);
        }
        assertTrue(planned > 3000, "too few plans: " + planned);
    }

    @Test
    void everyPlanRunsOnWorldsWithLoops() {
        Random random = new Random(16);
        Map<Status, Integer> statuses = new java.util.EnumMap<>(Status.class);
        for (int i = 0; i < 30000; i++) {
            TestWorld world = TestWorld.random(random, 2 + random.nextInt(6), 1 + random.nextInt(4),
                    random.nextDouble() * 0.6, random.nextDouble() * 0.3, 0.2);
            long amount = 1 + random.nextInt(10000);
            world.randomStock(random, amount);
            world.addLoops(random, random.nextInt(5), random.nextInt(5));
            var result = world.solve("root", amount);
            statuses.merge(result.status(), 1, Integer::sum);
            if (result.status() == Status.PLANNED) world.assertRuns("root", amount, result, random);
        }
        System.out.println("Worlds with loops: " + statuses);
        assertTrue(statuses.getOrDefault(Status.PLANNED, 0) > 3000, "too few plans: " + statuses);
    }

    @Test
    void comparedWithAE2OnWorldsWithChoices() {
        Random random = new Random(14);
        int both = 0;
        int same = 0;
        int ae2Only = 0;
        int oursOnly = 0;
        int maxWalks = 0;
        for (int i = 0; i < 6000; i++) {
            TestWorld world = TestWorld.random(random, 2 + random.nextInt(4), 2 + random.nextInt(2), 0.5, 0, 0);
            long amount = 1 + random.nextInt(60);
            world.randomStock(random, amount);
            var reference = world.referenceCrafts("root", amount);
            var result = world.solve("root", amount);
            maxWalks = Math.max(maxWalks, result.walks());
            boolean planned = result.status() == Status.PLANNED;
            if (planned) world.assertRuns("root", amount, result, random);
            if (reference != null && planned) {
                both++;
                if (reference.equals(result.crafts())) same++;
            } else if (reference != null) {
                ae2Only++;
            } else if (planned) {
                oursOnly++;
            }
        }
        System.out.printf("Choices: both %d, same plan %d, AE2 only %d, ours only %d, most walks %d%n", both, same,
                ae2Only, oursOnly, maxWalks);
        assertTrue(oursOnly > ae2Only, "should find more plans than it misses");
        assertTrue(ae2Only * 50 < both + ae2Only, "misses too many: " + ae2Only + " of " + (both + ae2Only));
    }

    @Test
    void walksStaySmallAndAmountsDoNotMatter() {
        Random random = new Random(15);
        for (int i = 0; i < 2000; i++) {
            long seed = random.nextLong();
            TestWorld small = TestWorld.random(new Random(seed), 6, 3, 0.2, 0, 0);
            small.randomStock(new Random(seed + 1), 1000);
            TestWorld large = TestWorld.random(new Random(seed), 6, 3, 0.2, 0, 0);
            small.stock.forEach((what, amount) -> large.stock(what, amount * 1_000_000));
            var smallResult = small.solve("root", 1000);
            var largeResult = large.solve("root", 1_000_000_000L);
            assertEquals(smallResult.status(), largeResult.status(), "world " + i);
            if (smallResult.status() == Status.PLANNED && smallResult.walks() == 1) {
                int world = i;
                smallResult.crafts().forEach((pattern, crafts) -> assertEquals(crafts * 1_000_000,
                        largeResult.crafts().get(pattern), "world " + world));
            }
        }
    }
}
