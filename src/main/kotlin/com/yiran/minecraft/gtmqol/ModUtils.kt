package com.yiran.minecraft.gtmqol

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.lowdragmc.lowdraglib.LDLib
import com.yiran.minecraft.gtmqol.GTMQoL.LOGGER
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import net.minecraftforge.fml.loading.FMLLoader


object ModUtils {
    @JvmStatic
    fun isDataGen(): Boolean {
        return FMLLoader.getLaunchHandler().isData
    }

    @JvmStatic
    fun binlog(bits: Int): Int {
        var bits = bits
        var log = 0
        if ((bits and -0x10000) != 0) {
            bits = bits ushr 16
            log = 16
        }
        if (bits >= 256) {
            bits = bits ushr 8
            log += 8
        }
        if (bits >= 16) {
            bits = bits ushr 4
            log += 4
        }
        if (bits >= 4) {
            bits = bits ushr 2
            log += 2
        }
        return log + (bits ushr 1)
    }

    inline fun <reified T> Any?.asType(): T? {
        return this as? T
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> T?.asNotNull(): T {
        return this as T
    }

    inline fun <reified T> T?.orElse(defaultValue: () -> T): T {
        return this ?: defaultValue()
    }

    inline fun <reified T> T?.orElse(defaultValue: T): T {
        return this ?: defaultValue
    }

    @JvmStatic
    val highLaserAmps : IntArray = intArrayOf(16384, 65536)
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
