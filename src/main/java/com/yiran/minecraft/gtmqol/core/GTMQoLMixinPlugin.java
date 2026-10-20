package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Skips the overclocking overhaul mixins of {@code gtmqol.mixins.json} when
 * {@link EarlyConfig#OVERCLOCKING_OVERHAUL} is off. Everything else in that config always applies and checks
 * {@code GTMQoLConfig} at runtime. Mixins in {@code mekanism/} target Mekanism classes and only apply when it is
 * loaded.
 */
public class GTMQoLMixinPlugin extends GTMQoLMixinConfigPlugin {

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
        if (mixinClassName.startsWith(PACKAGE + "mekanism.")) {
            return isModLoading("mekanism");
        }
        return EarlyConfig.OVERCLOCKING_OVERHAUL || !OVERCLOCKING_MIXINS.contains(mixinClassName);
    }
}
