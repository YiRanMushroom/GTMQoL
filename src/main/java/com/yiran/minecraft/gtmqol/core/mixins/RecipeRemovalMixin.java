package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.resources.ResourceLocation;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.function.Consumer;

/**
 * Keep vanilla's sand → glass smelting even with gtceu's {@code hardGlassRecipes}, so the glass + casing hatch
 * recipes ({@code EarlyGameRecipes}) work before the alloy smelter. The rest of the hard glass removals stay.
 * <p>
 * Also keeps vanilla's TNT recipe regardless of gtceu's {@code removeVanillaTNTRecipe} (same reason as
 * {@link ManualCompressionMixin}: setting gtceu's config from our constructor doesn't stick).
 */
@Mixin(value = RecipeRemoval.class, remap = false)
public class RecipeRemovalMixin {

    @WrapOperation(method = "hardGlassRecipes",
                   at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static void gtmqol$keepGlass(Consumer<Object> registry, Object id, Operation<Void> original) {
        if (GTMQoLConfig.get().recipes.earlyGame && ResourceLocation.withDefaultNamespace("glass").equals(id)) return;
        original.call(registry, id);
    }

    @ModifyExpressionValue(method = "generalRemovals",
                           at = @At(value = "FIELD",
                                    target = "Lcom/gregtechceu/gtceu/config/ConfigHolder$RecipeConfigs;removeVanillaTNTRecipe:Z",
                                    opcode = Opcodes.GETFIELD))
    private static boolean gtmqol$keepTNT(boolean remove) {
        return remove && !GTMQoLConfig.get().recipes.keepVanillaTNT;
    }
}
