package com.yiran.minecraft.gtmqol.mixin.gtceufix;

import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * gtceu 1.21 bug: the ghost circuit setter builds the new circuit with the current slot's count, which is 0
 * while the slot is empty, so the circuit is never set. 1.20.1 uses {@code stack(v)}. Remove this mixin once
 * gtceu fixes it.
 */
@Mixin(value = GTMuiWidgets.class, remap = false)
public class GTMuiWidgetsMixin {

    // The setter lambda in createCircuitSlotSyncValue.
    @ModifyArg(method = "/^lambda\\$createCircuitSlotSyncValue\\$/",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/common/item/behavior/IntCircuitBehaviour;stack(II)Lnet/minecraft/world/item/ItemStack;"),
               index = 1)
    private static int gtmqol$atLeastOneCircuit(int count) {
        return Math.max(1, count);
    }
}
