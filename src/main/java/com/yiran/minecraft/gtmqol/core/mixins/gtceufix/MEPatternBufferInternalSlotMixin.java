package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.neoforged.neoforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: fluidInventory is a plain Object2LongOpenHashMap<FluidStack>, but NeoForge 1.21 FluidStack has no
 * equals/hashCode, so every push (and every saved entry on load) gets its own key: pushing a pattern twice leaves two
 * entries per fluid instead of one with double the amount. The item map uses a hash strategy and doesn't have this.
 * Merge into an existing key of the same fluid and components. Remove once gtceu fixes it.
 */
// Untested: upstream #5568 replaced our count fix; a pattern needing 2+ items or >1 mB runs
@Mixin(value = MEPatternBufferPartMachine.InternalSlot.class, remap = false)
public class MEPatternBufferInternalSlotMixin {

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
