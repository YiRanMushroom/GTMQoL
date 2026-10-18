package com.yiran.minecraft.gtmqol.gregification;

import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * For machines that pick between recipes by a filter or by hand (Create's saw, MI's assembler): a recipe that another
 * recipe's ingredients would also run (each of its inputs takes something the other one's do, e.g. the same input,
 * or a part of the other's inputs) marks its main output as a ghost ingredient, so it only runs when that is marked.
 * The others need no mark. Counts are not compared.
 */
public final class MarkCoveredRecipes implements ForeignRecipeConverter {

    private final Single converter;
    private final Function<Recipe<?>, List<Set<Object>>> inputs;
    private final Function<Recipe<?>, @Nullable GhostIngredient<?>> mark;
    private Set<ResourceLocation> covered = Set.of();

    /**
     * @param inputs per input, the items (or fluids) it takes
     * @param mark   the recipe's main output as a mark, or null for none
     */
    public MarkCoveredRecipes(Single converter, Function<Recipe<?>, List<Set<Object>>> inputs,
                              Function<Recipe<?>, @Nullable GhostIngredient<?>> mark) {
        this.converter = converter;
        this.inputs = inputs;
        this.mark = mark;
    }

    @Override
    public void prepare(List<? extends Recipe<?>> recipes) {
        List<List<Set<Object>>> all = new ArrayList<>(recipes.size());
        List<Set<Object>> unions = new ArrayList<>(recipes.size());
        Map<Object, List<Integer>> users = new HashMap<>();
        for (int i = 0; i < recipes.size(); i++) {
            List<Set<Object>> in = inputs.apply(recipes.get(i));
            Set<Object> union = new HashSet<>();
            for (Set<Object> input : in) union.addAll(input);
            all.add(in);
            unions.add(union);
            for (Object o : union) users.computeIfAbsent(o, k -> new ArrayList<>()).add(i);
        }
        Set<ResourceLocation> result = new HashSet<>();
        for (int i = 0; i < recipes.size(); i++) {
            List<Set<Object>> in = all.get(i);
            if (in.isEmpty()) continue;
            Set<Integer> checked = new HashSet<>();
            search:
            for (Object o : in.get(0)) {
                for (int j : users.getOrDefault(o, List.of())) {
                    if (j == i || !checked.add(j)) continue;
                    if (coveredBy(in, unions.get(j))) {
                        result.add(recipes.get(i).getId());
                        break search;
                    }
                }
            }
        }
        covered = result;
    }

    private static boolean coveredBy(List<Set<Object>> inputs, Set<Object> other) {
        for (Set<Object> input : inputs) {
            if (Collections.disjoint(input, other)) return false;
        }
        return true;
    }

    @Override
    public void convert(Recipe<?> holder, Output output) {
        output.add(builder -> {
            if (!converter.convert(holder, builder)) return false;
            if (covered.contains(holder.getId())) {
                GhostIngredient<?> main = mark.apply(holder);
                if (main != null) builder.input(GhostIngredientRecipeCapability.CAP, main);
            }
            return true;
        });
    }
}
