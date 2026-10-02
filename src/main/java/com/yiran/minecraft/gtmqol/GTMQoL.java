package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.circuit.CircuitTags;
import com.yiran.minecraft.gtmqol.circuit.ControlCircuits;
import com.yiran.minecraft.gtmqol.circuit.UniversalCircuits;
import com.yiran.minecraft.gtmqol.client.GTMQoLClient;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.fe.FEInputProvider;
import com.yiran.minecraft.gtmqol.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.integration.IntegrationTests;
import com.yiran.minecraft.gtmqol.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.wireless.WirelessCovers;
import com.yiran.minecraft.gtmqol.wireless.WirelessNetworks;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamMachines;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
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
        GTMQoLAddon.registrate().registerEventListeners(modBus);
        GTMQoLConfig.init();
        WirelessNetworks.init();
        FEInputProvider.init(modBus);
        UniversalCircuits.init();
        ControlCircuits.init();
        CircuitTags.init();
        ModularMachines.init(modBus);

        // On 1.21 gtceu's registries are Registrate deferred registers, so content is declared right here like
        // gtceu's own CommonProxy does; the entries are created when the registry's RegisterEvent fires.
        // Recipe types first, machines reference them.
        MagicalAssembler.initRecipeType();
        WirelessCovers.init();
        MagicalAssembler.initMachines();
        GTMQoLMultiblocks.init();
        AdvancedSteamMachines.init();
        WirelessSteamMachines.init();
        WirelessEnergyMachines.init();
        if (GTCEu.Mods.isAE2Loaded()) {
            AE2Machines.init();
        }
        // do not run integration tests in data generation, They are only for testing in a running game.
        if (GTMQoLConfig.INSTANCE.integrationTests.enabled && !DatagenModLoader.isRunningDataGen()) {
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
