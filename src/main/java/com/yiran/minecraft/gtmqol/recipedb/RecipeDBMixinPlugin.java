package com.yiran.minecraft.gtmqol.recipedb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Turns the whole {@code gtmqol.recipedb.mixins.json} set on or off. Mixin configs are read before any mod is
 * constructed, so {@code GTMQoLConfig} (toma) is not available yet. Same approach as GTCEu's
 * {@code GTMixinPlugin}: a separate properties file read with plain java.
 */
public class RecipeDBMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("GTMQoL");

    // Relative to the game directory, like GTCEu's ./config/gtceu-early.properties.
    private static final Path FILE = Path.of("config", "gtmqol-early.properties");
    private static final String KEY = "recipeDB.groupedSearch";
    private static final String COMMENT = """
            GTMQoL options that have to be known before mods load (they decide which mixins apply).
            recipeDB.groupedSearch: only combine ingredients that a single recipe handler group could
            supply when searching for recipes. Strongly recommended with many distinct buses or pattern
            buffers. Requires a restart. Default: true""";

    private boolean enabled = true;

    @Override
    public void onLoad(String mixinPackage) {
        enabled = readEnabled();
        LOGGER.info("RecipeDB grouped search {}", enabled ? "enabled" : "disabled");
    }

    private static boolean readEnabled() {
        Properties properties = new Properties();
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    properties.load(reader);
                }
            }
            if (!properties.containsKey(KEY)) {
                properties.setProperty(KEY, "true");
                Files.createDirectories(FILE.getParent());
                try (Writer writer = Files.newBufferedWriter(FILE)) {
                    properties.store(writer, COMMENT);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read {}, RecipeDB grouped search stays enabled", FILE, e);
            return true;
        }
        return Boolean.parseBoolean(properties.getProperty(KEY).trim());
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return enabled;
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
