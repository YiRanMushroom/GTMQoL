package com.yiran.minecraft.gtmqol.integration.ae2;

import com.yiran.minecraft.gtmqol.common.crafter.CraftingPattern;
import com.yiran.minecraft.gtmqol.common.crafter.ICrafterPattern;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An AE2 pattern in the pattern crafter: what a molecular assembler can make (crafting, stonecutting, smithing).
 * Processing patterns can't: their outputs are whatever the player wrote, no recipe backs them.
 * <p>
 * Fluids are as AE2's own fluid substitution decided. An item that comes back as itself stays in the machine at any
 * durability, as with our own patterns; other items count exactly, or as the pattern's substitutes.
 */
public record AECrafterPattern(List<ItemInput> itemInputs, List<FluidStack> fluidInputs, List<ItemStack> outputs)
        implements ICrafterPattern {

    public static boolean isPattern(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem && !stack.is(AEItems.PROCESSING_PATTERN.asItem());
    }

    public static @Nullable ICrafterPattern decode(ItemStack stack, Level level) {
        if (!isPattern(stack) ||
                !(PatternDetailsHelper.decodePattern(stack, level) instanceof IMolecularAssemblerSupportedPattern pattern)) {
            return null;
        }
        List<ItemInput> items = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (GenericStack output : pattern.getOutputs()) {
            if (output.what() instanceof AEItemKey item) outputs.add(item.toStack((int) output.amount()));
        }
        for (var input : pattern.getInputs()) {
            GenericStack[] possible = input.getPossibleInputs();
            int amount = (int) (possible[0].amount() * input.getMultiplier());
            if (possible[0].what() instanceof AEFluidKey fluid) {
                fluids.add(fluid.toStack(amount));
            } else if (possible[0].what() instanceof AEItemKey item) {
                var remainder = input.getRemainingKey(item) instanceof AEItemKey key ? key : null;
                if (remainder != null && remainder.getItem() == item.getItem()) {
                    items.add(new ItemInput(ingredient(possible, true), 1, false));
                } else {
                    items.add(new ItemInput(ingredient(possible, false), amount, true));
                    if (remainder != null) outputs.add(remainder.toStack((int) input.getMultiplier()));
                }
            }
        }
        return new AECrafterPattern(items, fluids, outputs);
    }

    private static Ingredient ingredient(GenericStack[] possible, boolean anyDurability) {
        List<ItemStack> items = Arrays.stream(possible)
                .map(stack -> stack.what() instanceof AEItemKey item ? item.toStack() : null)
                .filter(Objects::nonNull)
                .toList();
        // Ingredient.of ignores NBT, which is what any durability means
        if (items.size() == 1 && !anyDurability) return CraftingPattern.exact(items.get(0));
        return Ingredient.of(items.stream());
    }
}
