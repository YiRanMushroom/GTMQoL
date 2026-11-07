package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.common.crafter.CraftingPattern;
import com.yiran.minecraft.gtmqol.common.crafter.ICrafterPattern;
import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Our crafting pattern as AE2 sees it. What the recipe doesn't use up isn't an input here: AE2 would take it out of the
 * network and wait for it to come back. The pattern matrix finds it among its catalysts instead, so a smithing template
 * pattern that keeps its template reads as materials to one template. AE2 needs the decoder to bring jobs back after a
 * restart; with it pattern providers accept the item too, but they can't do anything with it.
 */
public final class CrafterPatternDetails implements IPatternDetails {

    public enum Decoder implements IPatternDetailsDecoder {

        INSTANCE;

        @Override
        public boolean isEncodedPattern(ItemStack stack) {
            return stack.is(PatternCrafter.PATTERN.get());
        }

        @Override
        public @Nullable IPatternDetails decodePattern(@Nullable AEItemKey what, Level level) {
            if (what == null || what.getItem() != PatternCrafter.PATTERN.get()) return null;
            CraftingPattern pattern = CraftingPattern.read(what.toStack());
            CraftingPattern.Plan plan = pattern == null ? null : pattern.plan(level);
            return plan == null ? null : new CrafterPatternDetails(what, plan);
        }

        @Override
        public @Nullable IPatternDetails decodePattern(ItemStack what, Level level, boolean tryRecovery) {
            return decodePattern(AEItemKey.of(what), level);
        }
    }

    private final AEItemKey definition;
    private final CraftingPattern.Plan plan;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    private CrafterPatternDetails(AEItemKey definition, CraftingPattern.Plan plan) {
        this.definition = definition;
        this.plan = plan;
        List<IInput> inputs = new ArrayList<>();
        for (var input : plan.consumed()) inputs.add(new ItemInput(input.item(), input.ingredient()));
        for (FluidStack fluid : plan.fluids()) inputs.add(new FluidInput(fluid));
        this.inputs = inputs.toArray(IInput[]::new);
        this.outputs = plan.outputs().stream().map(GenericStack::fromItemStack).toArray(GenericStack[]::new);
    }

    public ICrafterPattern plan() {
        return plan;
    }

    /** Whether a catalyst of this pattern changes with every craft. */
    public boolean wearsCatalyst() {
        return plan.itemInputs().stream().anyMatch(ICrafterPattern.ItemInput::changes);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CrafterPatternDetails details && details.definition.equals(definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    /** The encoded item, then whatever else the recipe accepts in its place. */
    private record ItemInput(GenericStack[] possible, long multiplier, Ingredient ingredient) implements IInput {

        ItemInput(ItemStack item, Ingredient ingredient) {
            this(possible(item, ingredient), item.getCount(), ingredient);
        }

        private static GenericStack[] possible(ItemStack item, Ingredient ingredient) {
            Set<AEItemKey> keys = new LinkedHashSet<>();
            keys.add(AEItemKey.of(item));
            for (ItemStack choice : ingredient.getItems()) keys.add(AEItemKey.of(choice));
            return keys.stream().map(key -> new GenericStack(key, 1)).toArray(GenericStack[]::new);
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return possible;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input instanceof AEItemKey item && ingredient.test(item.toStack());
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ItemInput input && input.multiplier == multiplier &&
                    Arrays.equals(input.possible, possible);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(possible);
        }
    }

    private record FluidInput(GenericStack stack) implements IInput {

        FluidInput(FluidStack fluid) {
            this(new GenericStack(AEFluidKey.of(fluid), fluid.getAmount()));
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] { stack };
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return stack.what().equals(input);
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
