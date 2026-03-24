package com.yiran.minecraft.gtmqol.integration.monifactory

import com.lowdragmc.lowdraglib.LDLib
import com.yiran.minecraft.gtmqol.config.ConfigHolder

object MoniUtils {
    @JvmStatic
    fun isMoniFactoryAndIntegrationEnabled(): Boolean {
        return LDLib.isModLoaded("monilabs") && ConfigHolder.instance.addonConfig.enableMoniFactoryIntegration
    }
}