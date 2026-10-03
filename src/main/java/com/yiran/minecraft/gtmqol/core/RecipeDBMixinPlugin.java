package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Turns the whole {@code gtmqol.recipedb.mixins.json} set on or off, from {@link EarlyConfig}.
 */
public class RecipeDBMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("GTMQoL");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("RecipeDB grouped search {}", EarlyConfig.RECIPE_DB_GROUPED_SEARCH ? "enabled" : "disabled");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return EarlyConfig.RECIPE_DB_GROUPED_SEARCH;
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
