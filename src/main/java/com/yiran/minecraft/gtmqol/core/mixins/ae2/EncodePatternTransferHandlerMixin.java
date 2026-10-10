package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import brachy.modularui.integration.jei.JeiIngredientHandler;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing patterns encoded from JEI leave out the inputs that aren't consumed (chance 0). JEI's slots don't know
 * about chances, so they are matched back to the recipe's contents: {@code GTRecipeJEICategory} adds one input slot
 * per stack of each item content, then of each fluid content. AE2 skips empty input slots.
 */
@Mixin(EncodePatternTransferHandler.class)
public class EncodePatternTransferHandlerMixin {

    @WrapOperation(method = "transferRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/integration/modules/jei/GenericEntryStackHelper;ofInputs(Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;)Ljava/util/List;"), remap = false)
    private List<List<GenericStack>> gtmqol$skipNotConsumed(IRecipeSlotsView slotsView,
                                                            Operation<List<List<GenericStack>>> original,
                                                            @Local(argsOnly = true) Object recipeBase) {
        List<List<GenericStack>> inputs = original.call(slotsView);
        if (!(recipeBase instanceof GTRecipe recipe) || !GTMQoLConfig.get().ae2.skipNotConsumedInputs) return inputs;

        List<Boolean> consumed = new ArrayList<>(inputs.size());
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            int slots = JeiIngredientHandler.toJeiIngredient(ItemRecipeCapability
                    .mapIngredientToEntryList(ItemRecipeCapability.CAP.of(content.content()))).size();
            for (int i = 0; i < slots; i++) consumed.add(content.chance() != 0);
        }
        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            int slots = JeiIngredientHandler.toJeiIngredient(FluidRecipeCapability
                    .mapIngredientToEntryList(FluidRecipeCapability.CAP.of(content.content()))).size();
            for (int i = 0; i < slots; i++) consumed.add(content.chance() != 0);
        }
        // a layout we don't know
        if (consumed.size() != inputs.size()) return inputs;

        List<List<GenericStack>> result = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            result.add(consumed.get(i) ? inputs.get(i) : List.of());
        }
        return result;
    }
}
