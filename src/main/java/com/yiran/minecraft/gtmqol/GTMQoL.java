package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.integration.IntegrationTests;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTMQoL.MOD_ID)
public final class GTMQoL {
    public static final String MOD_ID = "gtmqol";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public GTMQoL(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, GTMQoLConfig.SPEC);
        modBus.addListener(this::onRegisterDynamicResources);
    }

    /**
     * GTCEu fires this from its {@code ModelManager} mixin, in mod order, right before models are
     * baked. This is the only point where adding to {@code GTDynamicResourcePack} still has effect.
     */
    private void onRegisterDynamicResources(RegisterDynamicResourcesEvent event) {
        if (GTMQoLConfig.ENABLE_INTEGRATION_TESTS.get()) {
            IntegrationTests.generateExampleAssets();
        }
    }
}
