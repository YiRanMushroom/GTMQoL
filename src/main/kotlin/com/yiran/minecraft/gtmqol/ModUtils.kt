package com.yiran.minecraft.gtmqol

import com.lowdragmc.lowdraglib.LDLib
import com.yiran.minecraft.gtmqol.GTMQoL.LOGGER
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import net.minecraftforge.fml.loading.FMLLoader

object ModUtils {
    @JvmStatic
    fun isDataGen(): Boolean {
        return FMLLoader.getLaunchHandler().isData
    }
}

fun ae2PresentedAndIntegrationEnabled(): Boolean {
    return LDLib.isModLoaded("ae2") && ConfigHolder.instance.addonConfig.enableAE2Integration
}

fun mekanismPresentedAndIntegrationEnabled(): Boolean {
    LOGGER.info("Checking Mekanism integration: isModLoaded = ${LDLib.isModLoaded("mekanism")}, configEnabled = ${ConfigHolder.instance.addonConfig.enableMekanismIntegration}")
    return LDLib.isModLoaded("mekanism") && ConfigHolder.instance.addonConfig.enableMekanismIntegration
}

fun gtmthingsPresentedAndIntegrationEnabled(): Boolean {
    return LDLib.isModLoaded("gtmthings") && ConfigHolder.instance.addonConfig.enableGTMThingsIntegration
}
