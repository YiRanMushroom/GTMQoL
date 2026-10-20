package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.common.probable.ProbableDevices;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every multiblock that takes the parts of its recipe types through {@code autoAbilities} also takes a
 * probable device ({@link ProbableDevices}), like 2.0.0 did. Not shown in the structure preview.
 */
@Mixin(value = Predicates.class, remap = false)
public class PredicatesProbableDeviceMixin {

    @Inject(method = "autoAbilities([Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;ZZZZZZ)Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;",
            at = @At("RETURN"), cancellable = true)
    private static void gtmqol$probableDevice(GTRecipeType[] recipeType, boolean checkEnergyIn, boolean checkEnergyOut,
                                              boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn,
                                              boolean checkFluidOut, CallbackInfoReturnable<MultiPredicate> cir) {
        if (!ProbableDevices.enabled()) return;
        cir.setReturnValue(cir.getReturnValue()
                .and(Predicates.abilities(ProbableDevices.PROBABLE_DEVICE).setPreviewCount(0)));
    }
}
