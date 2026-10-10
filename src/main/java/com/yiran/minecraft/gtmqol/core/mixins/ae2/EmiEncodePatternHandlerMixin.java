package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiEncodePatternHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import dev.emi.emi.api.recipe.EmiRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing patterns encoded from EMI leave out the inputs that aren't consumed: GT shows them as inputs with
 * chance 0. AE2 skips empty input slots, so those just become empty.
 */
@Mixin(EmiEncodePatternHandler.class)
public class EmiEncodePatternHandlerMixin {

    @WrapOperation(method = "transferRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Lnet/minecraft/world/item/crafting/Recipe;Ldev/emi/emi/api/recipe/EmiRecipe;Z)Lappeng/integration/modules/emi/AbstractRecipeHandler$Result;",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/integration/modules/emi/EmiStackHelper;ofInputs(Ldev/emi/emi/api/recipe/EmiRecipe;)Ljava/util/List;"), remap = false)
    private List<List<GenericStack>> gtmqol$skipNotConsumed(EmiRecipe recipe,
                                                            Operation<List<List<GenericStack>>> original) {
        List<List<GenericStack>> inputs = original.call(recipe);
        if (!GTMQoLConfig.get().ae2.skipNotConsumedInputs) return inputs;
        // ofInputs maps getInputs() one to one
        var ingredients = recipe.getInputs();
        List<List<GenericStack>> result = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            result.add(ingredients.get(i).getChance() == 0 ? List.of() : inputs.get(i));
        }
        return result;
    }
}
