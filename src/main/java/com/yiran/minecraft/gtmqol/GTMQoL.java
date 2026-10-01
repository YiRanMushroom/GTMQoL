package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.integration.IntegrationTests;
import com.yiran.minecraft.gtmqol.integration.KubeJSDataGenFix;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTMQoL.MOD_ID)
public final class GTMQoL {
    public static final String MOD_ID = "gtmqol";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public GTMQoL(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        GTMQoLConfig.init();
        modBus.addGenericListener(MachineDefinition.class, this::onRegisterMachines);
        modBus.addListener(this::onRegisterDynamicResources);

        if (FMLLoader.getLaunchHandler().isData() && ModList.get().isLoaded("kubejs")) {
            KubeJSDataGenFix.apply();
        }
    }

    /**
     * GTCEu posts this at the end of {@code GTMachines.init()}, right before it freezes the machine
     * registry. {@code IGTAddon.initializeAddon()} runs after that, so machines registered there fail with
     * "registry gtceu:machine has been frozen".
     */
    private void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        if (GTMQoLConfig.INSTANCE.integrationTests.enabled) {
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
