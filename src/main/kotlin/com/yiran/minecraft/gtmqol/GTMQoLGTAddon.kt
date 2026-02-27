package com.yiran.minecraft.gtmqol

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate

@GTAddon
class GTMQoLGTAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return GTMQoLRegistrate.REGISTRATE
    }

    override fun initializeAddon() {

    }

    override fun addonModId(): String {
        return GTMQoL.MOD_ID
    }
}