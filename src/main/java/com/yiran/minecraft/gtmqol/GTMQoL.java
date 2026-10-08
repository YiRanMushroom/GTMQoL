package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
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
import com.yiran.minecraft.gtmqol.common.crystal.CrystalGrowth;
import com.yiran.minecraft.gtmqol.common.implosion.ElectricImplosion;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.common.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessCovers;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessNetworks;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.common.wireless.steam.WirelessSteamMachines;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
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
        if (config.circuits.universalCircuits) UniversalCircuits.init();
        if (config.circuits.controlCircuits) ControlCircuits.init();
        if (config.circuits.mekanismCircuitTags) CircuitTags.init();
        ModularMachines.init(modBus);

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
        if (config.machines.advancedSteamMachines) AdvancedSteamMachines.init();
        if (config.wireless.steam) WirelessSteamMachines.init();
        if (config.wireless.energy) WirelessEnergyMachines.init();
        if (GTCEu.Mods.isAE2Loaded()) {
            if (config.ae2.overclockedPatternBuffer) AE2Machines.init();
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

        modBus.addListener(this::onRegisterDynamicResources);

        if (FMLEnvironment.dist.isClient()) {
            GTMQoLClient.init();
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
