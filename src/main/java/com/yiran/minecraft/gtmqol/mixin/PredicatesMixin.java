package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(Predicates.class)
public class PredicatesMixin {

    @Inject(method = "abilities", at = @At("HEAD"), remap = false)
    private static void enableMultiHatch(PartAbility[] abilities, CallbackInfoReturnable<TraceabilityPredicate> cir,
                                            @Local(argsOnly = true) LocalRef<PartAbility[]> abilitiesRef) {
        if (!ConfigHolder.getInstance().allowMultiAmpHatchesForAllMultiblocks) {
            return;
        }

        // if ability contains INPUT_ENERGY, add INPUT_LASER and SUBSTATION_INPUT_ENERGY if it is not already present
        // also do that for output energy
        boolean hasInputEnergy = false;
        boolean hasOutputEnergy = false;
        for (PartAbility ability : abilities) {
            if (ability == PartAbility.INPUT_ENERGY) {
                hasInputEnergy = true;
            } else if (ability == PartAbility.OUTPUT_ENERGY) {
                hasOutputEnergy = true;
            }
        }

        List<PartAbility> newAbilities = new ArrayList<>(Arrays.asList(abilities));

        if (hasInputEnergy) {
            boolean hasInputLaser = false;
            boolean hasSubstationInputEnergy = false;
            for (PartAbility ability : abilities) {
                if (ability == PartAbility.INPUT_LASER) {
                    hasInputLaser = true;
                } else if (ability == PartAbility.SUBSTATION_INPUT_ENERGY) {
                    hasSubstationInputEnergy = true;
                }
            }
            if (!hasInputLaser) {
                newAbilities.add(PartAbility.INPUT_LASER);
            }
            if (!hasSubstationInputEnergy) {
                newAbilities.add(PartAbility.SUBSTATION_INPUT_ENERGY);
            }
        }

        if (hasOutputEnergy) {
            boolean hasOutputLaser = false;
            boolean hasSubstationOutputEnergy = false;
            for (PartAbility ability : abilities) {
                if (ability == PartAbility.OUTPUT_LASER) {
                    hasOutputLaser = true;
                } else if (ability == PartAbility.SUBSTATION_OUTPUT_ENERGY) {
                    hasSubstationOutputEnergy = true;
                }
            }
            if (!hasOutputLaser) {
                newAbilities.add(PartAbility.OUTPUT_LASER);
            }
            if (!hasSubstationOutputEnergy) {
                newAbilities.add(PartAbility.SUBSTATION_OUTPUT_ENERGY);
            }
        }

        abilitiesRef.set(newAbilities.toArray(new PartAbility[0]));
    }
}
