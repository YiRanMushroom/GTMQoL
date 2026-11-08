package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.yiran.minecraft.gtmqol.integration.ae2.AECrafterPattern;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One craft as the pattern crafter sees it, whatever kind of pattern it came from: {@link CraftingPattern} or
 * {@link AECrafterPattern}. Like AE2's IPatternDetails.
 */
public interface ICrafterPattern {

    /**
     * @param consumed false: needed, but stays in the machine
     * @param changes  stays in the machine but changes with every craft, see {@link #afterCraft}
     */
    record ItemInput(Ingredient ingredient, int count, boolean consumed, boolean changes) {}

    List<ItemInput> itemInputs();

    List<FluidStack> fluidInputs();

    /** The crafted item first, then what the recipe gives back. */
    List<ItemStack> outputs();

    /** What the input at this index of {@link #itemInputs} turns into after one craft, for those that change. */
    ItemStack afterCraft(int input, ItemStack item);

    /** The pattern on this item, or null when it holds none that can run. */
    static @Nullable ICrafterPattern decode(ItemStack stack, Level level) {
        return DecodedPatterns.get(stack, level, item -> {
            if (item.is(PatternCrafter.PATTERN.get())) {
                CraftingPattern pattern = CraftingPattern.read(item, level.registryAccess());
                return pattern == null ? null : pattern.plan(level);
            }
            return GTCEu.Mods.isAE2Loaded() ? AECrafterPattern.decode(item, level) : null;
        });
    }
}
