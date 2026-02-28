package com.yiran.minecraft.gtmqol;

import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.data.OPMultiblocks;
import com.yiran.minecraft.gtmqol.mixin_impl.AddDefaultMultiesImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(GTMQoL.MOD_ID)
public class GTMQoL {
    public static final String MOD_ID = "gtmqol";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public GTMQoL(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        GTMQoLRegistrate.getREGISTRATE().registerRegistrate();

        ConfigHolder.init();

        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(AddDefaultMultiesImpl::onAddPackFinders);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("{} initializing", MOD_ID);
    }

    public static ResourceLocation id(@NotNull String string) {
        return ResourceLocation.tryBuild(MOD_ID, string);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("{} common setup", MOD_ID);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("{} client setup", MOD_ID);
    }
}