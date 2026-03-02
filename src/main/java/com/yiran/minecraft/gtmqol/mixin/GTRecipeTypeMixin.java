package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.functionality.IAppendOnBuildAction;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(GTRecipeType.class)
public class GTRecipeTypeMixin implements IAppendOnBuildAction {
    @Shadow
    private GTRecipeBuilder recipeBuilder;

    @Override
    public void appendOnRecipeBuild(@NotNull BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave) {
        ((IAppendOnBuildAction) this.recipeBuilder).appendOnRecipeBuild(onSave);
    }
}
