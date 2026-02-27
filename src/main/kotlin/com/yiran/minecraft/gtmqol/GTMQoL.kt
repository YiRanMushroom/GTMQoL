package com.yiran.minecraft.gtmqol

import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.mixin_impl.AddDefaultMultiesImpl
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(GTMQoL.MOD_ID)
object GTMQoL {

    const val MOD_ID = "gtmqol"

    @JvmStatic
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    init {
        MOD_BUS.addListener(::onCommonSetup)
        MOD_BUS.addListener(::onClientSetup)
        MOD_BUS.addListener(AddDefaultMultiesImpl::onAddPackFinders)

        LOGGER.info("$MOD_ID initializing")
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("$MOD_ID common setup")

        ConfigHolder.init()
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.info("$MOD_ID client setup")

//        AddDefaultMultiesImpl-()
    }
}

