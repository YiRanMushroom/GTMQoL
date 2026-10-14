package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yiran.minecraft.gtmqol.functionality.ElectricImplosionRecipeLogic;
import com.yiran.minecraft.gtmqol.functionality.IAppendOnBuildAction;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(GTRecipeTypes.class)
public class GTRecipeTypes$Misc$Mixin {


    @Definition(id = "IMPLOSION_RECIPES", field = "Lcom/gregtechceu/gtceu/common/data/GTRecipeTypes;IMPLOSION_RECIPES:Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;")
    @Definition(id = "setSound", method = "Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;setSound(Lcom/gregtechceu/gtceu/api/sound/SoundEntry;)Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;")
    @Expression("IMPLOSION_RECIPES = @(?).setSound(?)")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static GTRecipeType addElectricImplosion(GTRecipeType original) {
        ((IAppendOnBuildAction)original).appendOnRecipeBuild(ElectricImplosionRecipeLogic::onImplosionRecipeBuild);
        return original;
    }
}
