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

    @JvmStatic
    fun safePow(base: Int, exp: Int, limit: Long): Long {
        if (exp < 0) return 0
        if (exp == 0) return 1
        if (base == 0) return 0
        if (base == 1) return 1

        var res = 1L
        var b = base.toLong()
        var e = exp

        while (e > 0) {
            if (e % 2 == 1) {
                res = multiplyWithLimit(res, b, limit)
            }
            if (res >= limit) return limit

            e /= 2
            if (e > 0) {
                b = multiplyWithLimit(b, b, limit)
            }
        }
        return res
    }

    @JvmStatic
    private fun multiplyWithLimit(a: Long, b: Long, limit: Long): Long {
        if (a == 0L || b == 0L) return 0
        if (a > limit / b) return limit
        return a * b
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
