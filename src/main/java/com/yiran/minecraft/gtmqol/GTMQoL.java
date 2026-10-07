package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.integration.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.integration.ae2.AEDualParts;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.data.tag.CircuitTags;
import com.yiran.minecraft.gtmqol.common.circuit.ControlCircuits;
import com.yiran.minecraft.gtmqol.common.circuit.UniversalCircuits;
import com.yiran.minecraft.gtmqol.client.GTMQoLClient;
import com.yiran.minecraft.gtmqol.config.EarlyConfig;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.common.fe.FEInputProvider;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.test.IntegrationTests;
import com.yiran.minecraft.gtmqol.common.implosion.ElectricImplosion;
import com.yiran.minecraft.gtmqol.integration.KubeJSDataGenFix;
import com.yiran.minecraft.gtmqol.data.recipe.MiscRecipes;
import com.yiran.minecraft.gtmqol.common.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessCovers;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessNetworks;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.common.wireless.steam.WirelessSteamMachines;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTMQoL.MOD_ID)
public final class GTMQoL {
    public static final String MOD_ID = "gtmqol";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "");

    public GTMQoL(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        GTMQoLConfig config = GTMQoLConfig.get();
        GTMQoLAddon.registrate().registerEventListeners(modBus);
        WirelessNetworks.init();
        if (config.misc.feInput) FEInputProvider.init();
        if (config.circuits.universalCircuits) UniversalCircuits.init();
        if (config.circuits.controlCircuits) ControlCircuits.init();
        if (config.circuits.mekanismCircuitTags) CircuitTags.init();
        if (GTCEu.Mods.isAE2Loaded() && config.ae2.processing) {
            AEProcessing.initItems();
        }
        if (GTCEu.Mods.isAE2Loaded() && EarlyConfig.AE2_STICKY_CARD) {
            StickyCardItem.init();
            modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(StickyCardItem::registerUpgrades));
        }
        modBus.addGenericListener(GTRecipeType.class, this::onRegisterRecipeTypes);
        modBus.addGenericListener(CoverDefinition.class, this::onRegisterCovers);
        modBus.addGenericListener(MachineDefinition.class, this::onRegisterMachines);
        modBus.addListener(this::onRegisterDynamicResources);

        if (FMLEnvironment.dist.isClient()) {
            GTMQoLClient.init();
        }

        if (FMLLoader.getLaunchHandler().isData() && ModList.get().isLoaded("kubejs")) {
            KubeJSDataGenFix.apply();
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

    private void onRegisterRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        MagicalAssembler.initRecipeType();
        if (GTMQoLConfig.get().machines.electricImplosionCompressor) ElectricImplosion.initRecipeType();
        if (GTCEu.Mods.isAE2Loaded() && GTMQoLConfig.get().ae2.processing) {
            AEProcessing.initRecipeTypes();
        }
    }

    /**
     * Posted by {@code GTCovers.init()} before it freezes the cover registry; {@code IGTAddon.registerCovers}
     * is deprecated.
     */
    private void onRegisterCovers(GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition> event) {
        GTMQoLConfig config = GTMQoLConfig.get();
        if (config.wireless.energy) WirelessCovers.initEnergy();
        if (config.wireless.steam) WirelessCovers.initSteam();
    }

    /**
     * GTCEu posts this at the end of {@code GTMachines.init()}, right before it freezes the machine
     * registry. {@code IGTAddon.initializeAddon()} runs after that, so machines registered there fail with
     * "registry gtceu:machine has been frozen".
     *
     */
    private void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTMQoLConfig config = GTMQoLConfig.get();
        MagicalAssembler.initMachines();
        GTMQoLMultiblocks.init();
        if (config.machines.electricImplosionCompressor) ElectricImplosion.initMachine();
        if (config.machines.advancedSteamMachines) AdvancedSteamMachines.init();
        if (config.wireless.steam) WirelessSteamMachines.init();
        if (config.wireless.energy) WirelessEnergyMachines.init();
        if (GTCEu.Mods.isAE2Loaded()) {
            if (config.ae2.overclockedPatternBuffer) AE2Machines.init();
            if (config.ae2.processing) AEProcessing.initMachines();
            if (config.ae2.dualHatches) AEDualParts.init();
            if (config.ae2.patternBufferReturn) AEDualParts.initLang();
        }
        // do not run integration tests in data generation, They are only for testing in a running game.
        if (config.integrationTests.enabled && !FMLLoader.getLaunchHandler().isData()) {
            IntegrationTests.registerExampleMachines();
        }
    }

    /**
     * GTCEu fires this from its {@code ModelManager} mixin, in mod order, right before models are
     * baked. This is the only point where adding to {@code GTDynamicResourcePack} still has effect.
     */
    private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {
        RuntimeGeneration.generateAll();
    }
}
