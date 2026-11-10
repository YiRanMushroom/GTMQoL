package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import appeng.api.stacks.AEFluidKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * gtceu's ME output hatch buffers a fluid in a long, but takes no more once it holds {@link Integer#MAX_VALUE} of it:
 * outputs then stop until the buffer goes into the network, and a recipe's output parallels are cut to what still
 * fits. Like the ME output bus (and our dual output part), it takes up to {@link Long#MAX_VALUE}; a single fill is
 * still an int.
 */
// Untested: an ME output hatch buffers more than 2.1G mB of one fluid (ME network full or offline) and the machine
// keeps running
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine$FluidStorageDelegate",
       remap = false)
public abstract class MEOutputHatchBufferMixin {

    @Shadow
    @Final
    MEOutputHatchPartMachine this$0;

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void gtmqol$fillPastIntMax(FluidStack resource, FluidAction action, CallbackInfoReturnable<Integer> cir) {
        KeyStorage buffer = ((MEOutputHatchPartMachineAccessor) this$0).gtmqol$getInternalBuffer();
        var key = AEFluidKey.of(resource);
        long oldValue = buffer.storage.getOrDefault(key, 0L);
        int change = (int) Math.min(Long.MAX_VALUE - oldValue, resource.getAmount());
        if (change > 0 && action.execute()) {
            buffer.storage.put(key, oldValue + change);
            buffer.onChanged();
        }
        cir.setReturnValue(change);
    }
}
