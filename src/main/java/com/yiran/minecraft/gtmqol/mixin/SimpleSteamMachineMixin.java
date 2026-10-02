package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Steam single blocks get the ghost circuit slot, attached the same way {@code SimpleTieredMachine} does. The steam
 * panel does not show trait configurators, {@code GTSingleblockMachinePanelsMixin} turns them on.
 */
@Mixin(value = SimpleSteamMachine.class, remap = false)
public class SimpleSteamMachineMixin {

    // Both constructors call super(...), not this(...), so this runs once per machine.
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void gtmqol$attachCircuitSlot(CallbackInfo ci) {
        ((MetaMachine) (Object) this).attachPersistentTrait("circuit", new ProgrammableCircuitSlotTrait());
    }
}
