package com.yiran.minecraft.gtmqol.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;

/**
 * Applies {@code gtmqol.jeifix.mixins.json} only when JEI is installed and its {@code RecipeSlot} no longer has the
 * {@code allIngredients} / {@code displayIngredients} fields (removed in JEI 19.46, replaced by
 * {@code RecipeSlotIngredients}). ModularUI's {@code RecipeSlotAccessor} still targets them and its config is
 * required, so on new JEI the class fails to load and every JEMI recipe in EMI breaks.
 */
public class JeiRecipeSlotFixPlugin extends GTMQoLMixinConfigPlugin {

    private static final String RECIPE_SLOT = "mezz.jei.library.gui.ingredients.RecipeSlot";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!isModLoading("jei")) return false;
        try {
            ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(RECIPE_SLOT);
            return node.fields.stream()
                    .noneMatch(f -> f.name.equals("allIngredients") || f.name.equals("displayIngredients"));
        } catch (ClassNotFoundException | IOException e) {
            return false;
        }
    }
}
