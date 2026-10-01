package com.yiran.minecraft.gtmqol.common.multiblocks

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

class SingleHatchTierSkippingWorkableElectricMachine(holder: IMachineBlockEntity, vararg args: Any)
    : WorkableElectricMultiblockMachine(holder, *args) {
    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    companion object {
        val MANAGED_FIELD_HOLDER : ManagedFieldHolder = ManagedFieldHolder(
            SingleHatchTierSkippingWorkableElectricMachine::class.java, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER)
    }

    override fun getMaxVoltage(): Long {
        return this.overclockVoltage
    }
}