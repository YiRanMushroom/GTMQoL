package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.yiran.minecraft.gtmqol.api.RecipeModifierPartMachines;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(Predicates.class)
public abstract class PredicatesMixin {

    @Shadow
    public static TraceabilityPredicate abilities(PartAbility... abilities) {
        return null;
    }

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

    //    @WrapMethod(method = "autoAbilities([Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;ZZZZZZ)Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;")
//    private static TraceabilityPredicate qol$inject$modifier$autoAbilities(GTRecipeType[] recipeType, boolean checkEnergyIn, boolean checkEnergyOut, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, Operation<TraceabilityPredicate> original) {
//        return original.call(recipeType, checkEnergyIn, checkEnergyOut, checkItemIn, checkItemOut, checkFluidIn, checkFluidOut)
//                .or(abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
//    }
//
//    @WrapMethod(method = "autoAbilities(ZZZ)Lcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;")
//    private static TraceabilityPredicate qol$inject$modifier$autoAbilitiesNoRecipeType(boolean checkMaintenance, boolean checkMuffler, boolean checkParallel, Operation<TraceabilityPredicate> original) {
//        if (checkMaintenance || checkParallel) {
//            return original.call(checkMaintenance, checkMuffler, checkParallel)
//                    .or(abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
//        }
//
//        return original.call(checkMaintenance, checkMuffler, checkParallel);
//    }
    @WrapMethod(method = "abilities")
    private static TraceabilityPredicate qol$inject$modifier$abilities(PartAbility[] abilities, Operation<TraceabilityPredicate> original) {
        if (Arrays.stream(abilities).anyMatch(ability -> ability == PartAbility.IMPORT_ITEMS || ability == PartAbility.EXPORT_ITEMS || ability == PartAbility.IMPORT_FLUIDS || ability == PartAbility.EXPORT_FLUIDS)) {
            return original.call((Object[]) abilities).or(abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
        } else {
            return original.call((Object[]) abilities);
        }
    }
}
