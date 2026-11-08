package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.yiran.minecraft.gtmqol.api.machine.MachineFeatures;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

/**
 * The panel stage of {@link MachineFeatures}: {@code build} is where GTCEu adds its own default configurators (power,
 * voiding, ...), and every machine UI that uses the panel builder goes through it, also those that show no trait
 * configurators.
 */
@Mixin(value = MachineUIPanelBuilder.class, remap = false)
public class MachineUIPanelBuilderMixin {

    @Shadow
    @Final
    private MetaMachine machine;

    @Inject(method = "build", at = @At("RETURN"))
    private void gtmqol$addPanelFeatures(PanelSyncManager syncManager, UISettings settings,
                                         CallbackInfoReturnable<MachineUIPanel> cir) {
        MachineFeatures.panelBuilt(machine, cir.getReturnValue(), syncManager);
    }
}
