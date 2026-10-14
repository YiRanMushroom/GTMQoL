package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    /*
     * gtceu 1.21 bug: fluidInventory is a plain Object2LongOpenHashMap<FluidStack>, but NeoForge 1.21 FluidStack
     * has no equals/hashCode, so every push (and every saved entry on load) gets its own key: pushing a pattern
     * twice leaves two entries per fluid instead of one with double the amount. The item map uses a hash strategy
     * and doesn't have this. Merge into an existing key of the same fluid and components.
     */

    @WrapOperation(method = "add",
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongOpenHashMap;addTo(Ljava/lang/Object;J)J"))
    private long gtmqol$mergeSameFluidOnPush(Object2LongOpenHashMap<FluidStack> map, Object key, long amount,
                                            Operation<Long> original) {
        return original.call(map, gtmqol$existingKey(map, (FluidStack) key), amount);
    }

    @WrapOperation(method = "deserializeNBT",
                   at = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2LongOpenHashMap;put(Ljava/lang/Object;J)J"))
    private long gtmqol$mergeSameFluidOnLoad(Object2LongOpenHashMap<FluidStack> map, Object key, long amount,
                                            Operation<Long> original) {
        return map.addTo(gtmqol$existingKey(map, (FluidStack) key), amount);
    }

    @Unique
    private static FluidStack gtmqol$existingKey(Object2LongOpenHashMap<FluidStack> map, FluidStack stack) {
        for (FluidStack existing : map.keySet()) {
            if (FluidStack.isSameFluidSameComponents(existing, stack)) return existing;
        }
        return stack;
    }
}
