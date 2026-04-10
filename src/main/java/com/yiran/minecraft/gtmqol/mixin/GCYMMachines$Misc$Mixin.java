package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yiran.minecraft.gtmqol.api.RecipeModifierPartMachines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GCYMMachines.class)
public class GCYMMachines$Misc$Mixin {
    @Definition(id = "where", method = "Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;where(CLcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;")
    @Expression("?.where('Y', @(?))")
    @ModifyExpressionValue(method = "lambda$static$40", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TraceabilityPredicate addModifierForLargeDistillery(TraceabilityPredicate original) {
        original.or(Predicates.abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
        return original;
    }
}
