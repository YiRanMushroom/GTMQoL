package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.data.recipe.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipe.generated.MaterialRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

/**
 * Keeps the crafting table compression recipes (ingot <-> block, nugget <-> ingot, raw ore <-> raw ore block)
 * regardless of GTCEu's {@code disableManualCompression}. Setting that field from the mod constructor does not
 * stick, so every place GTCEu reads it sees {@code false} instead.
 */
@Mixin(value = { RecipeAddition.class, RecipeRemoval.class, MaterialRecipeHandler.class, OreRecipeHandler.class },
       remap = false)
public class ManualCompressionMixin {

    @ModifyExpressionValue(method = { "disableManualCompression", "init", "processNugget", "processBlock",
            "processRawOre" },
                           at = @At(value = "FIELD",
                                    target = "Lcom/gregtechceu/gtceu/config/ConfigHolder$RecipeConfigs;disableManualCompression:Z",
                                    opcode = Opcodes.GETFIELD))
    private static boolean gtmqol$keepManualCompression(boolean disabled) {
        return disabled && !GTMQoLConfig.get().recipes.keepManualCompression;
    }
}
