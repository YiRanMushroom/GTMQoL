package com.yiran.minecraft.gtmqol.config;

import com.yiran.minecraft.gtmqol.GTMQoL;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;
import lombok.Getter;

@Config(id = GTMQoL.MOD_ID)
public class ConfigHolder {
    @Getter
    private static ConfigHolder instance;

    private static final Object lock = new Object();

    public static void init() {
        synchronized (lock) {
            if (instance == null) {
                instance = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.properties()).getConfigInstance();
            }
        }
    }

    @Configurable
    @Configurable.Comment("GTEU machine now accepts FE")
    public boolean enableFEToEUConversion = true;
}
