package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.functionality.IAppendOnBuildAction;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(GTRecipeBuilder.class)
public abstract class GTRecipeBuilderMixin implements IAppendOnBuildAction {
    @Shadow @Nullable public BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave;

    @Shadow public abstract @NotNull GTRecipeBuilder onSave(@Nullable BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave);

    @Override
    public void appendOnRecipeBuild(@NotNull BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave) {
        if (this.onSave == null) {
            this.onSave = onSave;
        } else {
            this.onSave = this.onSave.andThen(onSave);
        }
    }
}
