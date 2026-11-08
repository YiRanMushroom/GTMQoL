package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver.Recipe;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver.Result;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver.Stack;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ICraftingSimulationState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Puts {@link CraftingSolver} on a crafting calculation: AE2's patterns and simulated network as its recipes and stock,
 * its result back into AE2's simulation, which then builds the plan as if its own walk had been through.
 */
public final class AECraftingPlanner implements CraftingSolver.Source<AEKey, IPatternDetails> {

    /** What a CPU sets aside per entry of a crafting tree, in bytes. AE2 counts tree nodes, here it is one per pattern. */
    private static final int BYTES_PER_PATTERN = 8;

    private final ICraftingService service;
    private final ICraftingSimulationState inventory;

    public AECraftingPlanner(ICraftingService service, ICraftingSimulationState inventory) {
        this.service = service;
        this.inventory = inventory;
    }

    @Override
    public long stock(AEKey what) {
        return inventory.extract(what, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    @Override
    public boolean canEmit(AEKey what) {
        return service.canEmitFor(what);
    }

    @Override
    public List<Recipe<AEKey, IPatternDetails>> recipes(AEKey what) {
        List<Recipe<AEKey, IPatternDetails>> recipes = new ArrayList<>();
        for (IPatternDetails pattern : service.getCraftingFor(what)) recipes.add(recipe(what, pattern));
        return recipes;
    }

    private static Recipe<AEKey, IPatternDetails> recipe(AEKey what, IPatternDetails pattern) {
        Map<AEKey, Long> inputs = new LinkedHashMap<>();
        boolean plannable = true;
        boolean substitutes = false;
        for (var input : pattern.getInputs()) {
            GenericStack[] possible = input.getPossibleInputs();
            inputs.merge(possible[0].what(), possible[0].amount() * input.getMultiplier(), Long::sum);
            substitutes |= possible.length > 1;
            plannable &= input.getRemainingKey(possible[0].what()) == null;
        }
        long output = 0;
        List<Stack<AEKey>> byproducts = new ArrayList<>();
        for (GenericStack stack : pattern.getOutputs()) {
            if (stack.what().equals(what)) output += stack.amount();
            else byproducts.add(new Stack<>(stack.what(), stack.amount()));
        }
        List<Stack<AEKey>> stacks = new ArrayList<>();
        inputs.forEach((key, amount) -> stacks.add(new Stack<>(key, amount)));
        return new Recipe<>(pattern, stacks, output, byproducts, plannable, substitutes);
    }

    /** Plays the result into the simulation: what it takes, what it makes, what it takes up in the CPU. */
    public void apply(Result<AEKey, IPatternDetails> result, AEKey what, long amount, KeyCounter missing) {
        inventory.addStackBytes(what, 1, amount);
        result.used().forEach((key, used) -> inventory.extract(key, used, Actionable.MODULATE));
        result.emitted().forEach(inventory::emitItems);
        result.missing().forEach(missing::add);
        result.crafts().forEach((pattern, crafts) -> {
            inventory.addCrafting(pattern, crafts);
            inventory.addBytes(crafts + BYTES_PER_PATTERN);
            for (var input : pattern.getInputs()) {
                GenericStack first = input.getPossibleInputs()[0];
                inventory.addStackBytes(first.what(), first.amount(), input.getMultiplier() * crafts);
            }
        });
    }
}
