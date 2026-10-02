package com.yiran.minecraft.gtmqol.mixin.gtceufix;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: a pattern buffer slot keys its contents by a count-1 stack ({@code AEItemKey.toStack()},
 * {@code AEFluidKey.toStack(1)}) and keeps the real amount separately, but matches them with NeoForge's
 * {@code SizedIngredient.test} / {@code SizedFluidIngredient.test}, which also require
 * {@code stack count >= ingredient count}. So any ingredient asking for more than 1 (every parallel, every fluid
 * over 1 mB) never matches and the recipe fails with insufficient input. The 1.20.1 gtceu SizedIngredient ignores
 * the count. Only test the item/fluid; the loop already handles the amounts. Remove once gtceu fixes it.
 */
@Mixin(value = MEPatternBufferPartMachine.InternalSlot.class, remap = false)
public class MEPatternBufferInternalSlotMixin {

    @WrapOperation(method = "handleItemInternal",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/neoforge/common/crafting/SizedIngredient;test(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtmqol$testItemIgnoringCount(SizedIngredient ingredient, ItemStack stack,
                                                 Operation<Boolean> original) {
        return ingredient.ingredient().test(stack);
    }

    @WrapOperation(method = "handleFluidInternal",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/neoforge/fluids/crafting/SizedFluidIngredient;test(Lnet/neoforged/neoforge/fluids/FluidStack;)Z"))
    private boolean gtmqol$testFluidIgnoringAmount(SizedFluidIngredient ingredient, FluidStack stack,
                                                   Operation<Boolean> original) {
        return ingredient.ingredient().test(stack);
    }
}
