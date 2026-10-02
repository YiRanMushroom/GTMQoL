package com.yiran.minecraft.gtmqol.integration;

import net.neoforged.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Applies {@code gtmqol.jeifix.mixins.json} only when JEI is installed and its {@code RecipeSlot} no longer has the
 * {@code allIngredients} / {@code displayIngredients} fields (removed in JEI 19.46, replaced by
 * {@code RecipeSlotIngredients}). ModularUI's {@code RecipeSlotAccessor} still targets them and its config is
 * required, so on new JEI the class fails to load and every JEMI recipe in EMI breaks.
 */
public class JeiRecipeSlotFixPlugin implements IMixinConfigPlugin {

    private static final String RECIPE_SLOT = "mezz.jei.library.gui.ingredients.RecipeSlot";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (FMLLoader.getLoadingModList().getModFileById("jei") == null) return false;
        try {
            ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(RECIPE_SLOT);
            return node.fields.stream()
                    .noneMatch(f -> f.name.equals("allIngredients") || f.name.equals("displayIngredients"));
        } catch (ClassNotFoundException | IOException e) {
            return false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
