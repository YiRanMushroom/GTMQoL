package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraftforge.fluids.FluidType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Steam single blocks get the ghost circuit slot, attached the same way {@code SimpleTieredMachine} does. The steam
 * panel does not show trait configurators, {@code GTSingleblockMachinePanelsMixin} turns them on.
 * <p>
 * They also get fluid tanks sized by their recipe types, like {@code WorkableTieredMachine}. The input tank refuses
 * steam, so steam only goes to the steam tank. It has a higher trait priority than the steam tank (also an
 * {@code IO.IN} fluid tank) because the machine UI draws the first input tank as the recipe's fluid slots.
 */
@Mixin(value = SimpleSteamMachine.class, remap = false)
public class SimpleSteamMachineMixin {

    // Both constructors call super(...), not this(...), so this runs once per machine.
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void gtmqol$attachTraits(CallbackInfo ci) {
        SimpleSteamMachine machine = (SimpleSteamMachine) (Object) this;
        GTMQoLConfig.SteamTweaks tweaks = GTMQoLConfig.get().steamTweaks;
        if (tweaks.circuitSlots) machine.attachPersistentTrait("circuit", new ProgrammableCircuitSlotTrait());
        if (!tweaks.fluidTanks) return;

        var recipeTypes = machine.getRecipeTypes();
        int capacity = 8 * FluidType.BUCKET_VOLUME;
        NotifiableFluidTank importFluids = new NotifiableFluidTank(
                machine.getDefinition().getInputSize(FluidRecipeCapability.CAP, recipeTypes), capacity, IO.IN);
        importFluids.setFilter(f -> !f.getFluid().is(GTMaterials.Steam.getFluidTag()));
        machine.attachPersistentTrait("gtmqol_import_fluids", importFluids, 2);
        machine.attachPersistentTrait("gtmqol_export_fluids", new NotifiableFluidTank(
                machine.getDefinition().getOutputSize(FluidRecipeCapability.CAP, recipeTypes), capacity, IO.OUT));
    }
}
