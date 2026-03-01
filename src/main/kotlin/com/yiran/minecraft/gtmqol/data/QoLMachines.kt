package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate


object QoLMachines {
    @JvmStatic
    val GREENHOUSE: Array<MachineDefinition>

    init {
        GREENHOUSE = GTMachineUtils.registerSimpleMachines(
            GTMQoLRegistrate.REGISTRATE,
            "greenhouse",
            QoLRecipeTypes.GREEN_HOUSE_RECIPES
        )
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLMachines object.
    }
}