package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamItemBusPartMachine;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GTCEu disables the ghost circuit slot on steam buses; enable it on the input bus like the electric ones.
 */
@Mixin(value = SteamItemBusPartMachine.class, remap = false)
public class SteamItemBusPartMachineMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtmqol$enableCircuitSlot(BlockEntityCreationInfo info, IO io, CallbackInfo ci) {
        if (!GTMQoLConfig.get().steamTweaks.circuitSlots) return;
        ((ItemBusPartMachine) (Object) this).getCircuitSlot().setEnabled(io == IO.IN);
    }
}
