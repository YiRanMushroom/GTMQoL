package com.yiran.minecraft.gtmqol.mixin.eaep;

import com.extendedae_plus.util.uploadPattern.RecipeTypeNameConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "extendedae_plus")
)
@Mixin(RecipeTypeNameConfig.class)
public class RecipeTypeNameConfig$BetterMatchingLogic$Mixin {
    @Inject(method = "getDefaultMappings", at = @At("HEAD"), cancellable = true)
    private static void getDefaultMappings(CallbackInfoReturnable<Map<String, String>> cir) {
        cir.setReturnValue(new HashMap<>());
    }

    @Inject(method = "mapGTCEuRecipeToSearchKey", at = @At("RETURN"), cancellable = true)
    private static void mapGTCEuRecipeToSearchKey(Object gtRecipeObj, CallbackInfoReturnable<String> cir) {
        if (cir.getReturnValue().contains("_")) {
            cir.setReturnValue(qol$ToEmptySpaceFirstLetterUppercase(cir.getReturnValue()));
        }
    }

    @Unique
    private static String qol$ToEmptySpaceFirstLetterUppercase(String qol) {
        if (qol == null || qol.isEmpty()) return qol;

        String processed = qol.replace("_", " ").toLowerCase().trim();

        if (processed.isEmpty()) return processed;

        return Character.toUpperCase(processed.charAt(0)) + processed.substring(1);
    }
}
