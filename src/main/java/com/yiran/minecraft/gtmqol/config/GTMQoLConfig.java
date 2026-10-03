package com.yiran.minecraft.gtmqol.config;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.neoforged.neoforge.data.loading.DatagenModLoader;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import dev.toma.configuration.config.format.ConfigFormats;

/**
 * Uses gtceu's config library rather than ForgeConfigSpec: it reads the file as soon as it is
 * registered, so values can be used during registration (gtceu calls addons at the end of CONSTRUCT,
 * before Forge loads COMMON configs).
 * <p>
 * Registered on first use, like gtceu's {@code ConfigHolder}: some mixins read it from gtceu's static
 * initializers, which run while gtceu is constructed, before this mod is.
 * <p>
 * Options that decide whether a mixin applies at all are in {@link EarlyConfig} instead.
 */
@Config(id = GTMQoL.MOD_ID)
public final class GTMQoLConfig {

    private static GTMQoLConfig instance;

    public static GTMQoLConfig get() {
        if (instance == null) {
            // Data generation must see every feature, otherwise their lang, models and tags go missing.
            instance = DatagenModLoader.isRunningDataGen() ? new GTMQoLConfig() :
                    Configuration.registerConfig(GTMQoLConfig.class, ConfigFormats.YAML).getConfigInstance();
        }
        return instance;
    }

    @Configurable
    @Configurable.Comment("Machines added by this mod. Requires a restart.")
    public Machines machines = new Machines();

    public static class Machines {

        @Configurable
        @Configurable.Comment({ "Smart Assembly Factory (assembly line multiblock with parallels)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean smartAssemblyFactory = true;

        @Configurable
        @Configurable.Comment({ "Dimensionally Transcendent Fusion Reactor (fusion of any tier)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean dimensionallyTranscendentFusionReactor = true;

        @Configurable
        @Configurable.Comment({ "Void Miner (mines the dimension's vein ores for wireless EU)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean voidMiner = true;

        @Configurable
        @Configurable.Comment({ "Large steam multiblocks, the steam parallel hatch, the steam magical assembler and",
                "the steam single blocks GTCEu does not have (bender, wiremill, ...)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean advancedSteamMachines = true;
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
    @Configurable.Comment("Wireless networks shared by an FTB team. Requires a restart.")
    public Wireless wireless = new Wireless();

    public static class Wireless {

        @Configurable
        @Configurable.Comment({ "Wireless energy hatches, covers, accessors and monitor", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean energy = true;

        @Configurable
        @Configurable.Comment({ "Wireless steam hatches, covers, accessor and monitor", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean steam = true;
    }

    @Configurable
    @Configurable.Comment("Circuits. Requires a restart.")
    public Circuits circuits = new Circuits();

    public static class Circuits {

        @Configurable
        @Configurable.Comment({ "One universal circuit per tier, made from any circuit of that tier in the magical assembler",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean universalCircuits = true;

        @Configurable
        @Configurable.Comment({ "Cheap ULV-EV control circuits made in the circuit assembler", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean controlCircuits = true;

        @Configurable
        @Configurable.Comment({ "Tags making GT circuits and Mekanism control circuits interchangeable", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean mekanismCircuitTags = true;
    }

    @Configurable
    @Configurable.Comment("Recipe changes. Requires a restart.")
    public Recipes recipes = new Recipes();

    public static class Recipes {

        @Configurable
        @Configurable.Comment({ "Miscellaneous recipes (steel from iron in the EBF with circuit 24, ...)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean miscRecipes = true;

        @Configurable
        @Configurable.Comment({ "Early game: casing + chest/glass buses and hatches, and sand -> glass smelting is kept",
                "with GTCEu's hardGlassRecipes", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean earlyGame = true;

        @Configurable
        @Configurable.Comment({ "Keep 3x3 / 2x2 compression recipes in the crafting table (ingot <-> block, nugget <-> ingot)",
                "even when GTCEu's disableManualCompression is on", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean keepManualCompression = true;
    }

    @Configurable
    @Configurable.Comment("Changes to GTCEu's steam machines. Requires a restart.")
    public SteamTweaks steamTweaks = new SteamTweaks();

    public static class SteamTweaks {

        @Configurable
        @Configurable.Comment({ "Ghost circuit slot on steam single blocks and steam input buses", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean circuitSlots = true;

        @Configurable
        @Configurable.Comment({ "Fluid input and output tanks on steam single blocks", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean fluidTanks = true;

        @Configurable
        @Configurable.Comment({ "Steam macerator, LV-HV macerators, steam grinder and steam oven keep all of a recipe's outputs",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean noOutputLimits = true;

        @Configurable
        @Configurable.Comment({ "The large boilers' muffler and maintenance hatch are optional", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean optionalLargeBoilerParts = true;
    }

    @Configurable
    @Configurable.Comment({ "Overclocking changes for specific multiblocks. The overclocking overhaul itself is",
            "overclocking.overhaul in gtmqol-early.properties." })
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
    @Configurable.Comment("AE2 content, only when AE2 is installed. Requires a restart.")
    public AE2 ae2 = new AE2();

    public static class AE2 {

        @Configurable
        @Configurable.Comment({ "Overclocked ME Pattern Buffer", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean overclockedPatternBuffer = true;

        @Configurable
        @Configurable.Comment({ "ME Assembler, ME Circuit Slicer, silicon chips and the AE2 part recipes", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean processing = true;
    }

    @Configurable
    @Configurable.Comment("Everything else")
    public Misc misc = new Misc();

    public static class Misc {

        @Configurable
        @Configurable.Comment({ "GT machines and cables that take EU also take FE, at GTCEu's feToEuRatio. Requires a restart.",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean feInput = true;
    }

    @Configurable
    @Configurable.Comment("Void miner")
    public VoidMiner voidMiner = new VoidMiner();

    public static class VoidMiner {

        @Configurable
        @Configurable.Comment({ "Void miners in one dimension mine another dimension's ores, as \"from=to\" dimension ids,",
                "e.g. \"skyblockbuilder:skyblock=minecraft:overworld\" for a skyblock world that is not the overworld",
                "Default: none" })
        public String[] dimensionMapping = new String[0];
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
