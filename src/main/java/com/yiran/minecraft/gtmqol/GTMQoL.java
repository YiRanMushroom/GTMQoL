package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.common.autobuild.AutoBuildButton;
import com.yiran.minecraft.gtmqol.common.probable.ProbableDevices;
import com.yiran.minecraft.gtmqol.integration.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PatternBatch;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrix;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUploadMessage;
import com.yiran.minecraft.gtmqol.integration.ae2.parts.AEDualParts;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import com.yiran.minecraft.gtmqol.integration.ae2.stickycard.StickyCardItem;
import com.yiran.minecraft.gtmqol.common.wireless.energy.ae2.WirelessEnergyCellMachine;
import com.yiran.minecraft.gtmqol.common.stacklike.ae2.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.common.stacklike.ae2.AppMekChemicalBridge;
import com.yiran.minecraft.gtmqol.integration.productivebees.LegacyCapabilityAliases;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.autobuild.AutoBuilder;
import com.yiran.minecraft.gtmqol.common.circuit.CircuitTags;
import com.yiran.minecraft.gtmqol.data.tag.MaterialAliasTags;
import com.yiran.minecraft.gtmqol.common.circuit.ControlCircuits;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;
import com.yiran.minecraft.gtmqol.common.circuit.UniversalCircuits;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredients;
import com.yiran.minecraft.gtmqol.data.recipe.LegacyPartRecipes;
import com.yiran.minecraft.gtmqol.client.GTMQoLClient;
import com.yiran.minecraft.gtmqol.config.EarlyConfig;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.common.feinput.FEInputProvider;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.test.IntegrationTests;
import com.yiran.minecraft.gtmqol.common.test.MixinTargetCheck;
import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;
import com.yiran.minecraft.gtmqol.common.crystal.CrystalGrowth;
import com.yiran.minecraft.gtmqol.common.greenhouse.Greenhouse;
import com.yiran.minecraft.gtmqol.common.implosion.ElectricImplosion;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.common.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;
import com.yiran.minecraft.gtmqol.common.ui.RecipeTypeLayouts;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalStackLike;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.common.steam.SteamTweaks;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessCovers;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessNetworks;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelMachines;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEMachines;
import com.yiran.minecraft.gtmqol.common.wireless.steam.WirelessSteamMachines;
import com.yiran.minecraft.gtmqol.gregification.GTMaterialTags;
import com.yiran.minecraft.gtmqol.gregification.LoadingTags;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;
import com.yiran.minecraft.gtmqol.gregification.create.CreateGregification;
import com.yiran.minecraft.gtmqol.gregification.mekanism.MekanismGregification;
import com.yiran.minecraft.gtmqol.gregification.mi.MIGregification;
import com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTMQoL.MOD_ID)
public final class GTMQoL {
    public static final String MOD_ID = "gtmqol";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "");

    public GTMQoL(IEventBus modBus, ModContainer container) {
        GTMQoLConfig config = GTMQoLConfig.get();
        GTMQoLAddon.registrate().registerEventListeners(modBus);
        // Always on: the void miner uses the wireless EU network too.
        WirelessNetworks.init();
        if (config.misc.feInput) FEInputProvider.init(modBus);
        // always: the button itself checks config.misc.autoBuild
        AutoBuilder.init();
        if (config.circuits.universalCircuits) UniversalCircuits.init();
        if (config.circuits.controlCircuits) ControlCircuits.init();
        if (config.recipes.useLegacyPartRecipes) LegacyPartRecipes.init(modBus);
        if (config.circuits.mekanismCircuitTags) CircuitTags.init();
        MaterialAliasTags.init();
        PotionContainers.init(modBus);
        RecipeTypeLayouts.init(modBus);
        SteamTweaks.init(modBus);
        AutoBuildButton.init();
        // always: KubeJS scripts may use it with any recipe type
        BlockProviderRecipeCapability.init();
        GhostIngredients.init();
        ProbableDevices.init();
        // here, not in the Jade plugin: that class needs Jade, the lang datagen doesn't
        GTMQoLAddon.registrate().addRawLang("config.jade.plugin_gtmqol.stack_like_recipe_output",
                "Recipe Output (Chemicals)");
        if (ModList.get().isLoaded("mekanism")) ChemicalStackLike.init();
        ModularMachines.init(modBus);
        LoadingTags.init();
        RenamedIds.init(modBus);
        Gregification.init(modBus);
        if (config.gregification.modernIndustrialization && ModList.get().isLoaded("modern_industrialization")) {
            Gregification.addSource(MIGregification::types);
            GTMaterialTags.init();
        }
        if (config.gregification.mekanism && ModList.get().isLoaded("mekanism")) {
            Gregification.addSource(MekanismGregification::types);
        }
        if (config.gregification.create && ModList.get().isLoaded("create")) {
            Gregification.addSource(CreateGregification::types);
        }
        if (ModList.get().isLoaded("productivebees")) {
            LegacyCapabilityAliases.init(modBus);
            if (config.gregification.productiveBees) {
                Gregification.addSource(ProductiveBeesGregification::types);
                ProductiveBeesGregification.init(modBus);
            }
        }

        // On 1.21 gtceu's registries are Registrate deferred registers, so content is declared right here like
        // gtceu's own CommonProxy does; the entries are created when the registry's RegisterEvent fires.
        // Recipe types first, machines reference them. The magical assembler is always registered, most of our
        // recipes use it.
        MagicalAssembler.initRecipeType();
        if (config.wireless.energy) WirelessCovers.initEnergy();
        if (config.wireless.steam) WirelessCovers.initSteam();
        MagicalAssembler.initMachines();
        GTMQoLMultiblocks.init();
        if (config.machines.electricImplosionCompressor) ElectricImplosion.init(modBus);
        if (config.machines.crystalGrowthChamber) CrystalGrowth.init();
        if (config.machines.greenhouse) Greenhouse.init();
        if (config.machines.patternCrafter) PatternCrafter.init();
        if (config.machines.advancedSteamMachines) AdvancedSteamMachines.init();
        if (config.wireless.steam) WirelessSteamMachines.init();
        if (config.wireless.energy) WirelessEnergyMachines.init();
        if (config.wireless.energy && config.wireless.fe) WirelessFEMachines.init();
        if (config.wireless.computation) ChannelMachines.initComputation();
        if (config.wireless.opticalData) ChannelMachines.initOptical();
        if (GTCEu.Mods.isAE2Loaded()) {
            PatternBatch.init();
            // Before any machine exists: the universal parts attach one handler per bridge when constructed.
            if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")) {
                AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);
            }
            if (config.ae2.overclockedPatternBuffer) AE2Machines.init();
            if (config.wireless.energy && config.wireless.fe) WirelessEnergyCellMachine.init();
            if (config.ae2.assemblingMatrix && config.machines.patternCrafter && config.wireless.energy &&
                    config.wireless.fe) {
                AssemblingMatrix.init();
                AssemblingMatrixUploadMessage.register(modBus);
            }
            if (config.ae2.processing) {
                AEProcessing.initItems();
                AEProcessing.initRecipeTypes();
                AEProcessing.initMachines();
            }
            if (config.ae2.dualHatches) AEDualParts.init();
            if (config.ae2.patternBufferReturn) AEDualParts.initLang();
            if (EarlyConfig.AE2_STICKY_CARD) {
                StickyCardItem.init();
                modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(StickyCardItem::registerUpgrades));
            }
        }
        // do not run integration tests in data generation, They are only for testing in a running game.
        if (config.integrationTests.enabled && !DatagenModLoader.isRunningDataGen()) {
            IntegrationTests.registerExampleMachines();
        }
        if (DatagenModLoader.isRunningDataGen()) {
            modBus.addListener(GatherDataEvent.class, event -> MixinTargetCheck.run());
        }

        modBus.addListener(this::onRegisterDynamicResources);

        if (FMLEnvironment.dist.isClient()) {
            GTMQoLClient.init();
            GregificationClient.init();
        }
    }

    /**
     * Same as {@code GTCEu.id}: {@code "ns:path"} is parsed as is, anything else goes under {@code gtmqol},
     * with camelCase converted to snake_case.
     */
    public static ResourceLocation id(String path) {
        if (path.isBlank()) {
            return TEMPLATE_LOCATION;
        }

        int i = path.indexOf(':');
        if (i > 0) {
            return ResourceLocation.parse(path);
        } else if (i == 0) {
            path = path.substring(i + 1);
        }
        // only convert it to camel_case if it has any uppercase to begin with
        if (FormattingUtil.hasUpperCase(path)) {
            path = FormattingUtil.toLowerCaseUnderscore(path);
        }
        return TEMPLATE_LOCATION.withPath(path);
    }

    /**
     * GTCEu fires this from its {@code ModelManager} mixin, in mod order, right before models are
     * baked. This is the only point where adding to {@code GTDynamicResourcePack} still has effect.
     */
    private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {
        RuntimeGeneration.generateAll();
    }
}
