package com.yiran.minecraft.gtmqol.data

import com.lowdragmc.lowdraglib.Platform

object ClientDynamicModelRegisterer {
    private val onClientSetupListeners : MutableList<() -> Unit> = mutableListOf()

    @JvmStatic
    fun onGTCEuClientSetup(listener: () -> Unit) {
        // if code is not called on client, throw exception
        if (!Platform.isClient()) {
            throw IllegalStateException("onClientSetup can only be called on client side")
        }
        onClientSetupListeners.add(listener)
    }

    @JvmStatic
    fun setupClient() {
        onClientSetupListeners.forEach { it() }
    }
}