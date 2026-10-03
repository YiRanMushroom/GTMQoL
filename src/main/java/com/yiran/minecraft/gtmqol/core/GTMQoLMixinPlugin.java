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
 * Skips the overclocking overhaul mixins of {@code gtmqol.mixins.json} when
 * {@link EarlyConfig#OVERCLOCKING_OVERHAUL} is off. Everything else in that config always applies and checks
 * {@code GTMQoLConfig} at runtime.
 */
public class GTMQoLMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("GTMQoL");

    private static final String PACKAGE = "com.yiran.minecraft.gtmqol.core.mixins.";
    private static final Set<String> OVERCLOCKING_MIXINS = Set.of(
            PACKAGE + "OverclockingLogicMixin",
            PACKAGE + "GTRecipeViewerWidgetMixin",
            PACKAGE + "GTRecipeModifiersMixin");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Overclocking overhaul {}", EarlyConfig.OVERCLOCKING_OVERHAUL ? "enabled" : "disabled");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return EarlyConfig.OVERCLOCKING_OVERHAUL || !OVERCLOCKING_MIXINS.contains(mixinClassName);
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
