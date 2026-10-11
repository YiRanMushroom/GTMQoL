package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import brachy.modularui.integration.emi.EmiStackConverter;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * {@link GenericStackLikeRecipeCapability} contents in gtceu's EMI recipes' inputs and outputs (the
 * {@code GTEmiRecipeStackLikeMixin}), which EMI uses for recipe lookup and other mods for filling recipes, e.g. AE2's
 * pattern encoding. gtceu only lists items and fluids. Capabilities without an {@link EmiStackConverter} are skipped.
 */
public final class StackLikeEmi {

    private StackLikeEmi() {}

    public static void addInputs(GTRecipe recipe, List<EmiIngredient> inputs) {
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            for (var content : recipe.getInputContents(cap)) {
                var ingredient = convert(cap, content);
                if (ingredient != null) inputs.add(ingredient);
            }
        }
    }

    public static void addOutputs(GTRecipe recipe, List<EmiStack> outputs) {
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            for (var content : recipe.getOutputContents(cap)) {
                var ingredient = convert(cap, content);
                // Like gtceu's outputs: the first stack, with the chance.
                if (ingredient != null) {
                    outputs.add(ingredient.getEmiStacks().get(0)
                            .setChance((float) content.chance() / content.maxChance()));
                }
            }
        }
    }

    private static <S, I> @Nullable EmiIngredient convert(GenericStackLikeRecipeCapability<S, I> cap, Content content) {
        var converter = EmiStackConverter.getForNullable(cap.type.stackClass());
        if (converter == null) return null;
        I ingredient = cap.of(content.content());
        var stacks = new StackLikeEntryList<>(cap.type.stackClass(), cap.type.getStacks(ingredient));
        var emi = converter.convertTo(stacks, (float) content.chance() / content.maxChance(), UnaryOperator.identity());
        return emi.isEmpty() ? null : emi;
    }
}
