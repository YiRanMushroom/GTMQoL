package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

/**
 * With {@code recipes.keepVanillaRecipes}, GTCEu's recipe removals ({@link RecipeRemoval#init}, its wood removals
 * included) skip every {@code minecraft:} recipe, whatever its config says. Its removals of its own recipes stay. The
 * ids are filtered here because setting gtceu's config from our constructor doesn't stick (see
 * {@link ManualCompressionMixin}, which keeps the compression recipes of GT materials under the same option).
 */
// Untested: with recipes.keepVanillaRecipes on, vanilla's TNT, sand -> glass smelting, bucket, paper, charcoal and
// flint and steel recipes are there next to GTCEu's harder ones, and 3x3 compression still works for GT materials;
// off: GTCEu's recipe settings remove them again
@Mixin(value = RecipeRemoval.class, remap = false)
public class RecipeRemovalMixin {

    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true)
    private static Consumer<ResourceLocation> gtmqol$keepVanillaRecipes(Consumer<ResourceLocation> registry) {
        if (!GTMQoLConfig.get().recipes.keepVanillaRecipes) return registry;
        return id -> {
            if (!ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace())) registry.accept(id);
        };
    }
}
