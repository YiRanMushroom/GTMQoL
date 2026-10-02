package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.yiran.minecraft.gtmqol.overclock.Overclocking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** The recipe viewer's OC preview calls {@code runOverclockingLogic} directly, bypassing {@code getModifier}. */
@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public class GTRecipeViewerWidgetMixin {

    @ModifyVariable(method = "applyOverclock", at = @At("HEAD"), argsOnly = true)
    private OverclockingLogic gtmqol$replaceOverclock(OverclockingLogic logic) {
        return Overclocking.replace(logic);
    }
}
