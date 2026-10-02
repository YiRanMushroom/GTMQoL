package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/**
 * The steam single block panel turns off trait configurators, so the circuit slot from
 * {@code SimpleSteamMachineMixin} would not show. Turn them back on; a steam single block has no other trait with
 * configurators (those are the auto output and battery slot traits of electric machines). The call is in the
 * {@code GENERAL_MACHINE} lambda, matched by regex like {@code GTMultiMachinesMixin}.
 */
@Mixin(value = GTSingleblockMachinePanels.class, remap = false)
public class GTSingleblockMachinePanelsMixin {

    @WrapOperation(method = "/^lambda\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/mui/MachineUIPanelBuilder;defaultSteamMachinePanelBuilder(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;)Lcom/gregtechceu/gtceu/api/machine/mui/MachineUIPanelBuilder;"))
    private static MachineUIPanelBuilder gtmqol$showCircuitSlot(MetaMachine machine,
                                                                Operation<MachineUIPanelBuilder> original) {
        return original.call(machine).addTraitConfigurators(true);
    }
}
