package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.yiran.minecraft.gtmqol.common.autobuild.AutoBuildButton;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

/**
 * Adds the auto-build button to every multiblock controller panel. {@code build} is where GTCEu adds its own default
 * configurators (power, voiding, ...), and every controller UI that uses the panel builder goes through it.
 */
@Mixin(value = MachineUIPanelBuilder.class, remap = false)
public class MachineUIPanelBuilderMixin {

    @Shadow
    @Final
    private MetaMachine machine;

    @Inject(method = "build", at = @At("RETURN"))
    private void gtmqol$addAutoBuildButton(PanelSyncManager syncManager, UISettings settings,
                                           CallbackInfoReturnable<MachineUIPanel> cir) {
        if (machine instanceof MultiblockControllerMachine controller && GTMQoLConfig.get().misc.autoBuild) {
            AutoBuildButton.attach(controller, cir.getReturnValue(), syncManager);
        }
    }
}
