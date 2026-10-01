package com.yiran.minecraft.gtmqol.fe_to_eu_conversion

// Inspired by Gregfluxology (DBot) - https://github.com/AmpAutomation/Gregfluxology
// MIT License

import com.gregtechceu.gtceu.api.capability.IEnergyContainer
import com.gregtechceu.gtceu.api.capability.compat.CapabilityCompatProvider
import com.gregtechceu.gtceu.api.capability.compat.FeCompat
import com.gregtechceu.gtceu.api.capability.forge.GTCapability
import com.gregtechceu.gtceu.common.pipelike.cable.EnergyNetHandler
import net.minecraft.core.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.IEnergyStorage

class FEToEUProvider(upValue: ICapabilityProvider) : CapabilityCompatProvider(upValue) {

    override fun <T> getCapability(capability: Capability<T>, facing: Direction?): LazyOptional<T> {
        if (capability != ForgeCapabilities.ENERGY) return LazyOptional.empty()

        val energyContainer = getUpvalueCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, facing)
        return if (energyContainer.isPresent) {
            ForgeCapabilities.ENERGY.orEmpty(
                capability,
                LazyOptional.of { FEEnergyWrapper(energyContainer.resolve().get(), facing) }
            )
        } else {
            LazyOptional.empty()
        }
    }

    class FEEnergyWrapper(
        private val energyContainer: IEnergyContainer,
        private val facing: Direction?
    ) : IEnergyStorage {

        override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
            if (maxReceive == 1 && simulate) {
                return if (energyContainer.energyCanBeInserted > 0L) 1 else 0
            }

            var maxIn = maxReceive.toLong() / FeCompat.ratio(true)
            val missing = energyContainer.energyCanBeInserted

            if (missing <= 0) return 0

            val voltage = energyContainer.inputVoltage
            if (voltage <= 0) return 0

            maxIn = minOf(missing, maxIn)
            var maxAmp = minOf(energyContainer.inputAmperage, maxIn / voltage)

            if (energyContainer is EnergyNetHandler) {
                maxIn = maxReceive.toLong() / FeCompat.ratio(true)
                maxAmp = maxIn / voltage
            }

            if (maxAmp < 1L) {
                if (maxIn <= 0) return 0

                return if (!simulate) {
                    val inserted = energyContainer.acceptEnergyFromNetwork(facing, maxIn, 1L)
                    if (inserted <= 0) 0 else safeConvertEUToFE(maxIn)
                } else {
                    if (!energyContainer.inputsEnergy(facing)) 0
                    else safeConvertEUToFE(maxIn)
                }
            }

            if (!simulate) {
                maxAmp = energyContainer.acceptEnergyFromNetwork(facing, voltage, maxAmp)
            }

            return safeConvertEUToFE(maxAmp * voltage)
        }

        override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int = 0

        override fun getEnergyStored(): Int = safeConvertEUToFE(energyContainer.energyStored)

        override fun getMaxEnergyStored(): Int = safeConvertEUToFE(energyContainer.energyCapacity)

        override fun canExtract(): Boolean = false

        override fun canReceive(): Boolean = energyContainer.inputsEnergy(facing)

        companion object {
            fun safeCastLongToInt(value: Long): Int {
                return if (value > Int.MAX_VALUE) Int.MAX_VALUE else value.toInt()
            }

            fun safeConvertEUToFE(eu: Long): Int {
                return safeCastLongToInt(eu * FeCompat.ratio(true))
            }
        }
    }
}
