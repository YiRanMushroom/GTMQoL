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
    public static ConfigHolder instance;

    private static final Object lock = new Object();

    public static void init() {
        synchronized (lock) {
            if (instance == null) {
                instance = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.properties()).getConfigInstance();
            }
        }
    }

    public static class OverlockingConfig {
        @Configurable
        @Configurable.Comment({"Enable overclocking duration override for machines", "Default: true"})
        public boolean enableOverclockingDurationOverride = true;

        @Configurable
        @Configurable.Comment({"Override overclocking duration for non-perfect machines.", "Default: 0.25"})
        public double overclockingDurationMultiplier = 0.25;

        @Configurable
        @Configurable.Comment({"Override overclocking duration for perfect machines.", "Default: 0.125"})
        public double perfectOverclockingDurationMultiplier = 0.125;

        @Configurable
        @Configurable.Comment({"Enable subtick parallels for all recipe logics", "Default: true"})
        public boolean enableSubtickParallelsForAllLogics = true;

        @Configurable
        @Configurable.Comment({"Buff Fusion Reactor Overclocking", "Default: true"})
        public boolean buffFusionReactorOverclocking = true;
    }

    @Configurable
    public OverlockingConfig overlockingConfig = new OverlockingConfig();

    public static class AddonConfig {
        @Configurable
        @Configurable.Comment({"Enable Greenhouse", "Default: true"})
        public boolean enableGreenhouse = true;

        @Configurable
        @Configurable.Comment({"Enable Smart Assembly Factory", "Default: true"})
        public boolean enableSmartAssemblyFactory = true;

        @Configurable
        @Configurable.Comment({"Register Modular Machines for Simple Machines", "Default: true"})
        public boolean registerModularMachinesForSimpleMachines = true;

        @Configurable
        @Configurable.Comment({"Enable AE2 Integration (if present)", "Default: true"})
        public boolean enableAE2Integration = true;

        @Configurable
        @Configurable.Comment({"Enable Mekanism Integration (if present)", "Default: true"})
        public boolean enableMekanismIntegration = true;

        @Configurable
        @Configurable.Comment({"Enable GTMThings Integration (if present)", "Default: true"})
        public boolean enableGTMThingsIntegration = true;
    }

    @Configurable
    public AddonConfig addonConfig = new AddonConfig();

    @Configurable
    @Configurable.Comment("GTEU machine now accepts FE")
    public boolean enableFEToEUConversion = true;

    @Configurable
    @Configurable.Comment("Allow every multiblock to use multiamp hatches")
    public boolean allowMultiAmpHatchesForAllMultiblocks = true;
}
