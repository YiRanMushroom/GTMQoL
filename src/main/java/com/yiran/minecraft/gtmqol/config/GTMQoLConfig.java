package com.yiran.minecraft.gtmqol.config;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraftforge.fml.loading.FMLLoader;

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

    private static volatile GTMQoLConfig instance;

    public static GTMQoLConfig get() {
        GTMQoLConfig config = instance;
        return config != null ? config : load();
    }

    private static synchronized GTMQoLConfig load() {
        if (instance == null) {
            // Data generation must see every feature, otherwise their lang, models and tags go missing.
            instance = FMLLoader.getLaunchHandler().isData() ? new GTMQoLConfig() :
                    Configuration.registerConfig(GTMQoLConfig.class, ConfigFormats.YAML).getConfigInstance();
        }
        return instance;
    }

    /** FE from the wireless EU network: both switches. */
    public boolean wirelessFE() {
        return wireless.energy && wireless.fe;
    }

    /** The Assembling Matrix: its switch, and what it is made of (Crafting Patterns, wireless FE). */
    public boolean assemblingMatrix() {
        return ae2.assemblingMatrix && machines.patternCrafter && wirelessFE();
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
        @Configurable.Comment({ "Easy SAF recipe: besides the normal one, 16 assembly lines + circuit 24 in the magical",
                "assembler", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean easySAFRecipe = true;

        @Configurable
        @Configurable.Comment({ "Dimensionally Transcendent Fusion Reactor (fusion of any tier)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean dimensionallyTranscendentFusionReactor = true;

        @Configurable
        @Configurable.Comment({ "Void Miner (mines the dimension's vein ores for wireless EU). Needs wireless.energy",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean voidMiner = true;

        @Configurable
        @Configurable.Comment({ "Industrial Fishing Pond (vanilla fishing loot for EU, with a rod in the controller)",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean industrialFishingPond = true;

        @Configurable
        @Configurable.Comment({ "Crystal Growth Chamber (shards of the budding block in its middle: vanilla, AE2, GeOre)",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean crystalGrowthChamber = true;

        @Configurable
        @Configurable.Comment({ "Greenhouse (grows seeds and saplings with water: vanilla, GT, Mystical Agriculture, " +
                "other mods' by name)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean greenhouse = true;

        @Configurable
        @Configurable.Comment({ "Pattern Crafter (runs crafting table, stonecutter and smithing table recipes from Crafting",
                "Patterns and AE2 patterns)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean patternCrafter = true;

        @Configurable
        @Configurable.Comment({ "Electric Implosion Compressor (every implosion recipe that uses TNT, without the TNT)",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean electricImplosionCompressor = true;

        @Configurable
        @Configurable.Comment({ "Industrial Large Chemical Reactor (large chemical reactor recipes 64x faster)",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean industrialLargeChemicalReactor = true;

        @Configurable
        @Configurable.Comment({ "Probable Certainty Device (chanced outputs always come out) and Probable Improbability",
                "Device (chanced inputs are never consumed), parts for most multiblocks", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean probableDevices = true;

        @Configurable
        @Configurable.Comment({ "Large steam multiblocks, the steam parallel hatch, the steam magical assembler and",
                "the steam single blocks GTCEu does not have (bender, wiremill, ...)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean advancedSteamMachines = true;
    }

    @Configurable
    @Configurable.Comment("Multiblock versions of tiered single-block machines. Requires a restart.")
    public ModularMachines modularMachines = new ModularMachines();

    public static class ModularMachines {

        @Configurable
        @Configurable.Comment({ "Register a 3x3x3 modular multiblock for every tiered single-block machine", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean enabled = true;
    }

    @Configurable
    @Configurable.Comment("GT recipe types and machines running other mods' machine recipes. Requires a restart.")
    public Gregification gregification = new Gregification();

    public static class Gregification {

        @Configurable
        @Configurable.Comment({ "Mekanism's machine recipes on GT multiblocks, at LV voltage", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean mekanism = true;

        @Configurable
        @Configurable.Comment({ "GT machines for Productive Bees: comb centrifuge, incubator, gene extractor, beehive, breeder",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean productiveBees = true;

        @Configurable
        @Configurable.Comment({ "GT machines for Create's processing: millstone, crushing wheels, press, compactor,",
                "mixer, fan, deployer, spout, item drain, saw, mechanical crafter and sequenced assembly",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean create = true;

        @Configurable
        @Configurable.Comment({ "GT machines for Hostile Neural Networks: simulation chamber and loot fabricator",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean hostileNeuralNetworks = true;
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
        @Configurable.Comment({ "FE from the wireless energy network: FE input and output hatches, energy cube and,",
                "with AE2, the wireless energy cell. Needs wireless.energy", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean fe = true;

        @Configurable
        @Configurable.Comment({ "Wireless steam hatches, covers, accessor and monitor", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean steam = true;

        @Configurable
        @Configurable.Comment({ "Wireless computation hatches and router over computation channels", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean computation = true;

        @Configurable
        @Configurable.Comment({ "Wireless optical data hatches over optical data channels", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean opticalData = true;
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
        @Configurable.Comment({ "With Mekanism: GT circuits and Mekanism's control circuits count as each other, tier by tier",
                "(tags, so it applies when data is reloaded)", "Default: true" })
        public boolean mekanismCircuitTags = true;

    }

    @Configurable
    @Configurable.Comment("Recipe changes. Requires a restart.")
    public Recipes recipes = new Recipes();

    public static class Recipes {

        @Configurable
        @Configurable.Comment({ "Miscellaneous recipes (steel from iron in the EBF with circuit 24, the creative",
                "data access hatch and rubber saplings in the magical assembler, ...)", "Default: true" })
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

        @Configurable
        @Configurable.Comment({ "Keep vanilla's TNT crafting recipe even when GTCEu's removeVanillaTNTRecipe is on",
                "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean keepVanillaTNT = true;

        @Configurable
        @Configurable.Comment({ "Nether star dust in the mixer (HV): 4 diamond dust + 16 silver dust", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean netherStarDust = true;

        @Configurable
        @Configurable.Comment({ "Use legacy part recipes: the rod, bolt, gear, small gear and rotor extruder molds GTCEu v8",
                "removed, with their extruder recipes, plus a long rod extruder mold. Rotors recycle into 4 ingots",
                "again", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean useLegacyPartRecipes = true;
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
        @Configurable.Comment({ "Steam macerator, steam grinder and steam oven keep all of a recipe's outputs",
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
            "overclocking.overhaul in gtmqol-early.properties. Requires a restart." })
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

        @Configurable
        @Configurable.Comment({ "Parallel hatches don't raise EU/t: a multiblock runs as many parallels as its hatch allows",
                "at the EU/t of one recipe (not generators)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean absoluteParallelHatches = true;
    }

    @Configurable
    @Configurable.Comment("AE2 content, only when AE2 is installed. Machines and items require a restart, the rest applies at once.")
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

        @Configurable
        @Configurable.Comment({ "ME Dual Input (stocking bus + hatch), ME Dual Output (bus + hatch), and the ME Universal",
                "Input and Output, which also take other ME types (e.g. Mekanism chemicals)", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean dualHatches = true;

        @Configurable
        @Configurable.Comment({ "Pattern buffers send the outputs of their multiblock straight to the ME network",
                "(every buffer has a switch in its window)", "Default: true" })
        public boolean patternBufferReturn = true;

        @Configurable
        @Configurable.Comment({ "Encoding a processing pattern from the recipe viewer leaves out inputs the recipe",
                "doesn't consume (molds, lenses, ...)", "Default: true" })
        public boolean skipNotConsumedInputs = true;

        @Configurable
        @Configurable.Comment({ "Assembling Matrix: a multiblock that crafts AE2 crafting, stonecutting and smithing",
                "patterns and the Pattern Crafter's Crafting Patterns in bulk for an ME network. Also needs",
                "machines.patternCrafter, wireless.energy and wireless.fe", "Default: true" })
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        public boolean assemblingMatrix = true;

        @Configurable
        @Configurable.Comment({ "FE each craft in an Assembling Matrix uses", "Default: 64" })
        @Configurable.Range(min = 0)
        public int assemblingMatrixFEPerCraft = 64;

        @Configurable
        @Configurable.Comment({ "Pattern batching: the crafts of a pattern go out in batches of 1, 2, 4 and so on. Above 1,",
                "the largest batch is about this fraction of them, so the next step of a chain starts sooner", "Default: 1" })
        @Configurable.Range(min = 1, max = 4096)
        public int patternBatchParts = 1;

        @Configurable
        @Configurable.Comment({ "Work out crafting plans by adding up what each item needs instead of walking AE2's",
                "tree, so that the amount doesn't matter. Requests it can't judge still go AE2's way", "Default: true" })
        public boolean fastCraftingPlans = true;
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

        @Configurable
        @Configurable.Comment({ "Auto build button on multiblock controllers: places the structure from the player's inventory.",
                "Default: true" })
        public boolean autoBuild = true;
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

        @Configurable
        @Configurable.Comment({ "Most operations per cycle (different ores per cycle)", "Default: 64" })
        @Configurable.Range(min = 1, max = 256)
        public int maxOperations = 64;

        @Configurable
        @Configurable.Comment({ "Most stacks per ore and operation", "Default: 4096" })
        @Configurable.Range(min = 1, max = 65536)
        public int maxMultiplier = 4096;
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
