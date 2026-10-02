package com.yiran.minecraft.gtmqol.config;

import com.yiran.minecraft.gtmqol.GTMQoL;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import dev.toma.configuration.config.format.ConfigFormats;

/**
 * Uses gtceu's config library rather than ForgeConfigSpec: it reads the file as soon as it is
 * registered, so values can be used during registration (gtceu calls addons at the end of CONSTRUCT,
 * before Forge loads COMMON configs).
 */
@Config(id = GTMQoL.MOD_ID)
public final class GTMQoLConfig {

    public static GTMQoLConfig INSTANCE;

    public static void init() {
        INSTANCE = Configuration.registerConfig(GTMQoLConfig.class, ConfigFormats.YAML).getConfigInstance();
    }

    @Configurable
    @Configurable.Comment("Multiblock versions of tiered single-block machines")
    public ModularMachines modularMachines = new ModularMachines();

    public static class ModularMachines {

        @Configurable
        @Configurable.Comment({ "Register a 3x3x3 modular multiblock for every tiered single-block machine", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean enabled = true;
    }

    @Configurable
    @Configurable.Comment("Overclocking changes for specific multiblocks")
    public Overclocking overclocking = new Overclocking();

    public static class Overclocking {

        @Configurable
        @Configurable.Comment({ "Fusion reactors use perfect sub-tick overclocking, their full hatch voltage and accept substation and laser hatches",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean buffFusionReactor = true;

        @Configurable
        @Configurable.Comment({ "Enable multi-tier skipping for multiblocks that can tier skip (multiple energy hatches)",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean enableMultiTierSkipping = true;
    }

    @Configurable
    @Configurable.Comment("Development and integration test settings")
    public IntegrationTests integrationTests = new IntegrationTests();

    public static class IntegrationTests {

        @Configurable
        @Configurable.Comment({ "Register visible runtime-generated machine examples during startup", "Default: false" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean enabled = false;
    }
}
