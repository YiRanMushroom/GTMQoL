package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import appeng.api.stacks.GenericStack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * gtceu 1.21 bug: the stocking/ME input bus config widget never updated while the UI was open, only after reopening
 * it. The server detected the change and the client received the packet, but the display stayed old. Likely cause:
 * {@code AEConfigSyncHandler.detectAndSendChanges} decides each slot's {@code changed} flag and updates its cache
 * inside the {@code syncToClient} writer lambda, which runs when the packet is encoded; if that runs more than once
 * per packet, the delivered one has every flag false. {@code init} forces every flag to true, which is why the first
 * sync after opening was always right. Sending the full state (init) whenever anything changed fixes it (confirmed
 * in game; the encode-twice part itself was not verified). Remove once gtceu fixes it.
 */
@Mixin(value = AEConfigSyncHandler.class, remap = false)
public abstract class AEConfigSyncHandlerMixin {

    @Shadow
    @Final
    private IConfigurableSlotList slotList;
    @Shadow
    @Final
    private int slotCount;
    @Shadow
    @Final
    private GenericStack[] cachedConfig;
    @Shadow
    @Final
    private GenericStack[] cachedStock;

    @Shadow
    private static boolean areEqual(GenericStack a, GenericStack b) {
        throw new AssertionError();
    }

    @WrapMethod(method = "detectAndSendChanges")
    private void gtmqol$resendAllOnChange(boolean init, Operation<Void> original) {
        boolean changed = init;
        for (int i = 0; !changed && i < slotCount; i++) {
            IConfigurableSlot slot = slotList.getConfigurableSlot(i);
            changed = !areEqual(slot.getConfig(), cachedConfig[i]) || !areEqual(slot.getStock(), cachedStock[i]);
        }
        original.call(changed);
    }
}
