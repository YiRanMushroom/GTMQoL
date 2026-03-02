package com.yiran.minecraft.gtmqol

import com.lowdragmc.lowdraglib.LDLib
import com.yiran.minecraft.gtmqol.config.ConfigHolder

fun ae2PresentedAndIntegrationEnabled(): Boolean {
    return LDLib.isModLoaded("ae2") && ConfigHolder.instance.addonConfig.enableAE2Integration
}