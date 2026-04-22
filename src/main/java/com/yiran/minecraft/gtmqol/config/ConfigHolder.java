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

    public static class OverclockingConfig {
        @Configurable
        @Configurable.Comment({"Enable overclocking duration override for machines", "Default: true"})
        public boolean enableOverclockingDurationOverride = true;

        @Configurable
        @Configurable.Comment({"Buff Fusion Reactor Overclocking", "Default: true"})
        public boolean buffFusionReactorOverclocking = true;

        @Configurable
        @Configurable.Comment({"Enable Multi-Tier Skipping for Multiblocks That Can Tier Skip", "Default: true"})
        public boolean enableMultiTierSkipping = true;

        @Configurable
        @Configurable.Comment({"Single Energy Hatch Tier Skipping for Workable Electric Multiblock Machine", "Default: true"})
        public boolean singleEnergyHatchTierSkipping = true;
    }

    @Configurable
    public OverclockingConfig overclockingConfig = new OverclockingConfig();

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

        @Configurable
        @Configurable.Comment({"Enable Electric Implosion Recipes", "Default: true"})
        public boolean enableElectricImplosionRecipes = true;

        @Configurable
        @Configurable.Comment({"Enable Dimensionally Transcendent Fusion Reactor", "Default: true"})
        public boolean enableDimensionallyTranscendentFusionReactor = true;

        @Configurable
        @Configurable.Comment({"Register Laser Hatches for all tiers rather than only high tiers", "Default: true"})
        public boolean registerLaserHatchesForAllTiers = true;

        @Configurable
        @Configurable.Comment({"Enable Higher Amp Laser Hatches", "Default: true"})
        public boolean enableHigherAmpLaserHatches = true;

        @Configurable
        @Configurable.Comment({"Multiblocks Ignore Cleanroom Recipe Conditions", "Default: true"})
        public boolean multiblocksIgnoreCleanroomRecipes = true;

        @Configurable
        @Configurable.Comment({"Enable Integration with Moni Factory", "Default: true"})
        public boolean enableMoniFactoryIntegration = true;

        @Configurable
        @Configurable.Comment({"Enable PCB Factory", "Default: true"})
        public boolean enablePCBFactory = true;

        @Configurable
        @Configurable.Comment({"Enable AE Sticky Card", "Default: true"})
        public boolean enableAEStickyCard = true;

        @Configurable
        @Configurable.Comment({"Enable Resource Generation Recipes", "Default: true"})
        public boolean enableResourceGenerationRecipes = true;

        @Configurable
        @Configurable.Comment({"Enable Machine Part Modifiers", "Default: true"})
        public boolean enableMachinePartModifiers = true;

        @Configurable
        @Configurable.Comment({"Parallel Hatches Are Absolute for Machines", "Default: true"})
        public boolean parallelHatchesAreAbsolute = true;

        @Configurable
        @Configurable.Comment({"Add custom GCYM machines and add additional recipe type supports to them", "Default: true"})
        public boolean additionalGCYMMachinesAndAdditionalRecipes = true;
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
