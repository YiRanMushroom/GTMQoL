package com.yiran.minecraft.gtmqol.integration.ae2.batch;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Many crafts of a pattern as one push, see {@link BatchPlan}. CPUs save their jobs as a pattern's definition, so that
 * is an item of its own that holds the pattern and the amount, and a job comes back from a restart as it was. Nobody
 * ever holds that item.
 *
 * @param definition from {@link #of}
 */
public record ScaledPattern(IPatternDetails original, long multiplier, AEItemKey definition) implements IPatternDetails {

    private static final String PATTERN_KEY = "pattern";
    private static final String MULTIPLIER_KEY = "multiplier";

    public static ItemEntry<Item> ITEM;

    public static void init() {
        ITEM = GTMQoLAddon.registrate().item("scaled_crafting_pattern", Item::new)
                .lang("Crafting Batch")
                .removeTab(GTMQoLAddon.MAIN_TAB.getKey())
                .tag(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS)
                .model((context, provider) -> provider.withExistingParent(context.getName(), "minecraft:item/paper"))
                .register();
        PatternDetailsHelper.registerDecoder(Decoder.INSTANCE);
    }

    public static ScaledPattern of(IPatternDetails original, long multiplier, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put(PATTERN_KEY, original.getDefinition().toTag(registries));
        tag.putLong(MULTIPLIER_KEY, multiplier);
        ItemStack stack = ITEM.asStack();
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return new ScaledPattern(original, multiplier, AEItemKey.of(stack));
    }

    /** {@code crafts} crafts of it as one push: the pattern itself for one. */
    public static IPatternDetails piece(IPatternDetails original, long crafts, HolderLookup.Provider registries) {
        return crafts == 1 ? original : of(original, crafts, registries);
    }

    /** The pattern behind a push. */
    public static IPatternDetails unwrap(IPatternDetails pattern) {
        return pattern instanceof ScaledPattern scaled ? scaled.original : pattern;
    }

    private enum Decoder implements IPatternDetailsDecoder {

        INSTANCE;

        @Override
        public boolean isEncodedPattern(ItemStack stack) {
            return stack.is(ITEM.get());
        }

        @Override
        public @Nullable IPatternDetails decodePattern(@Nullable AEItemKey what, Level level) {
            if (what == null || !what.is(ITEM.get())) return null;
            CustomData data = what.get(DataComponents.CUSTOM_DATA);
            if (data == null) return null;
            CompoundTag tag = data.copyTag();
            AEItemKey pattern = AEItemKey.fromTag(level.registryAccess(), tag.getCompound(PATTERN_KEY));
            IPatternDetails original = pattern == null ? null : PatternDetailsHelper.decodePattern(pattern, level);
            return original == null ? null : new ScaledPattern(original, tag.getLong(MULTIPLIER_KEY), what);
        }
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        IInput[] inputs = original.getInputs();
        IInput[] scaled = new IInput[inputs.length];
        for (int i = 0; i < inputs.length; i++) scaled[i] = new ScaledInput(inputs[i], multiplier);
        return scaled;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return original.getOutputs().stream()
                .map(output -> new GenericStack(output.what(), output.amount() * multiplier))
                .toList();
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

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        return original.getTooltip(level, flags);
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
