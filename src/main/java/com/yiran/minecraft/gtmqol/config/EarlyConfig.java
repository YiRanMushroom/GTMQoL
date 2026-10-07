package com.yiran.minecraft.gtmqol.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Options that decide which mixins apply. Mixin configs are read before any mod is constructed, so
 * {@link GTMQoLConfig} (toma) is not available yet. Same approach as GTCEu's {@code GTMixinPlugin}: a separate
 * properties file read with plain java.
 */
public final class EarlyConfig {

    private static final Logger LOGGER = LogManager.getLogger("GTMQoL");

    // Relative to the game directory, like GTCEu's ./config/gtceu-early.properties.
    private static final Path FILE = Path.of("config", "gtmqol-early.properties");
    private static final String COMMENT = """
            GTMQoL options that have to be known before mods load (they decide which mixins apply).
            All of them require a restart.
            recipeDB.groupedSearch: only combine ingredients that a single recipe handler group could
            supply when searching for recipes. Strongly recommended with many distinct buses or pattern
            buffers. Default: true
            overclocking.overhaul: overclocking that uses every energy hatch's voltage and amperage, with
            tier skipping and the overclock info in the recipe viewer. Default: true
            ae2.universalCircuitEncoding: AE2 pattern encoding from the recipe viewer prefers universal
            circuits over the other circuits. Default: true
            ae2.stickyCard: the Sticky Card item (and its recipes) for storage buses, which stops a network
            insert at a bus whose filter matches. Default: true""";

    public static final boolean RECIPE_DB_GROUPED_SEARCH;
    public static final boolean OVERCLOCKING_OVERHAUL;
    public static final boolean AE2_UNIVERSAL_CIRCUIT_ENCODING;
    public static final boolean AE2_STICKY_CARD;

    static {
        Properties properties = load();
        RECIPE_DB_GROUPED_SEARCH = Boolean.parseBoolean(properties.getProperty("recipeDB.groupedSearch").trim());
        OVERCLOCKING_OVERHAUL = Boolean.parseBoolean(properties.getProperty("overclocking.overhaul").trim());
        AE2_UNIVERSAL_CIRCUIT_ENCODING = Boolean.parseBoolean(properties.getProperty("ae2.universalCircuitEncoding").trim());
        AE2_STICKY_CARD = Boolean.parseBoolean(properties.getProperty("ae2.stickyCard").trim());
    }

    private EarlyConfig() {}

    private static Properties load() {
        Properties defaults = new Properties();
        defaults.setProperty("recipeDB.groupedSearch", "true");
        defaults.setProperty("overclocking.overhaul", "true");
        defaults.setProperty("ae2.universalCircuitEncoding", "true");
        defaults.setProperty("ae2.stickyCard", "true");

        Properties properties = new Properties();
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    properties.load(reader);
                }
            }
            boolean missing = false;
            for (String key : defaults.stringPropertyNames()) {
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, defaults.getProperty(key));
                    missing = true;
                }
            }
            if (missing) {
                Files.createDirectories(FILE.getParent());
                try (Writer writer = Files.newBufferedWriter(FILE)) {
                    properties.store(writer, COMMENT);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read {}, using defaults", FILE, e);
            return defaults;
        }
        return properties;
    }
}
