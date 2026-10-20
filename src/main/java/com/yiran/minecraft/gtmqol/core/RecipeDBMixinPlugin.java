package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Turns the whole {@code gtmqol.recipedb.mixins.json} set on or off, from {@link EarlyConfig}.
 */
public class RecipeDBMixinPlugin extends GTMQoLMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("GTMQoL");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("RecipeDB grouped search {}", EarlyConfig.RECIPE_DB_GROUPED_SEARCH ? "enabled" : "disabled");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return EarlyConfig.RECIPE_DB_GROUPED_SEARCH;
    }
}
