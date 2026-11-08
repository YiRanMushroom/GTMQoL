package com.yiran.minecraft.gtmqol.integration.ae2.crafting;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import org.jetbrains.annotations.Nullable;


/**
 * Many crafts of a pattern as one push, see {@link BatchPlan}. CPUs save their jobs as a pattern's definition, so that
 * is an item of its own that holds the pattern and the amount, and a job comes back from a restart as it was. Nobody
 * ever holds that item.
 */
public final class PatternBatch implements IPatternDetails {

    private static final String PATTERN_KEY = "pattern";
    private static final String MULTIPLIER_KEY = "multiplier";

    public static ItemEntry<Item> ITEM;

    private final IPatternDetails original;
    private final long multiplier;
    private final AEItemKey definition;
    // AE2's CPU asks for these on every pass over its tasks, so they are made once
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    /** @param definition from {@link #of} */
    public PatternBatch(IPatternDetails original, long multiplier, AEItemKey definition) {
        this.original = original;
        this.multiplier = multiplier;
        this.definition = definition;
        IInput[] originalInputs = original.getInputs();
        this.inputs = new IInput[originalInputs.length];
        for (int i = 0; i < originalInputs.length; i++) inputs[i] = new ScaledInput(originalInputs[i], multiplier);
        GenericStack[] originalOutputs = original.getOutputs();
        this.outputs = new GenericStack[originalOutputs.length];
        for (int i = 0; i < originalOutputs.length; i++) {
            outputs[i] = new GenericStack(originalOutputs[i].what(), originalOutputs[i].amount() * multiplier);
        }
    }

    public IPatternDetails original() {
        return original;
    }

    public long multiplier() {
        return multiplier;
    }

    /** The definition holds the pattern and the multiplier, so it alone tells batches apart. */
    @Override
    public boolean equals(Object other) {
        return other instanceof PatternBatch batch && batch.definition.equals(definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    public static void init() {
        ITEM = GTMQoLAddon.registrate().item("pattern_batch", Item::new)
                .lang("Pattern Batch")
                .removeTab(GTMQoLAddon.MAIN_TAB.getKey())
                .model((context, provider) -> provider.withExistingParent(context.getName(), "minecraft:item/paper"))
                .register();
        PatternDetailsHelper.registerDecoder(Decoder.INSTANCE);
    }

    public static PatternBatch of(IPatternDetails original, long multiplier) {
        ItemStack stack = ITEM.asStack();
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(PATTERN_KEY, original.getDefinition().toTag());
        tag.putLong(MULTIPLIER_KEY, multiplier);
        return new PatternBatch(original, multiplier, AEItemKey.of(stack));
    }

    /** {@code crafts} crafts of it as one push: the pattern itself for one. */
    public static IPatternDetails piece(IPatternDetails original, long crafts) {
        return crafts == 1 ? original : of(original, crafts);
    }

    /** The pattern behind a push. */
    public static IPatternDetails unwrap(IPatternDetails pattern) {
        return pattern instanceof PatternBatch scaled ? scaled.original : pattern;
    }

    private enum Decoder implements IPatternDetailsDecoder {

        INSTANCE;

        @Override
        public boolean isEncodedPattern(ItemStack stack) {
            return stack.is(ITEM.get());
        }

        @Override
        public @Nullable IPatternDetails decodePattern(@Nullable AEItemKey what, Level level) {
            if (what == null || what.getItem() != ITEM.get()) return null;
            CompoundTag tag = what.getTag();
            if (tag == null) return null;
            AEItemKey pattern = AEItemKey.fromTag(tag.getCompound(PATTERN_KEY));
            IPatternDetails original = pattern == null ? null : PatternDetailsHelper.decodePattern(pattern, level);
            return original == null ? null : new PatternBatch(original, tag.getLong(MULTIPLIER_KEY), what);
        }

        @Override
        public @Nullable IPatternDetails decodePattern(ItemStack what, Level level, boolean tryRecovery) {
            return decodePattern(AEItemKey.of(what), level);
        }
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
        return original.supportsPushInputsToExternalInventory();
    }

    /** In the order the pattern wants, which only it knows, by the amounts of one craft. */
    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        KeyCounter[] oneCraft = new KeyCounter[inputHolder.length];
        for (int i = 0; i < inputHolder.length; i++) {
            oneCraft[i] = new KeyCounter();
            for (var input : inputHolder[i]) oneCraft[i].add(input.getKey(), input.getLongValue() / multiplier);
        }
        original.pushInputsToExternalInventory(oneCraft,
                (key, amount) -> inputSink.pushInput(key, amount * multiplier));
    }

    private record ScaledInput(IInput original, long multiplier) implements IInput {

        @Override
        public GenericStack[] getPossibleInputs() {
            return original.getPossibleInputs();
        }

        @Override
        public long getMultiplier() {
            return original.getMultiplier() * multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return original.isValid(input, level);
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return original.getRemainingKey(template);
        }
    }
}
