package com.yiran.minecraft.gtmqol.common.fe;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.utils.GTMath;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lets every GT machine and cable that takes EU on a side also take FE there, converted at GTCEu's
 * {@code feToEuRatio}. The other direction needs nothing: GTCEu already gives FE blocks an EU capability
 * ({@code EUToFEProvider}, {@code nativeEUToFE}), so EU outputs push into them.
 *
 * <p>The machine's energy container is looked up without the attached capabilities
 * ({@link MetaMachine#getCapability(MetaMachine, Capability, Direction)}, {@link CableBlockEntity#getEnergyContainer}),
 * otherwise this and GTCEu's provider would ask each other forever on sides without one.</p>
 */
public final class FEInputProvider implements ICapabilityProvider {

    private final BlockEntity blockEntity;

    private FEInputProvider(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Call from the mod constructor.
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, FEInputProvider::onAttachCapabilities);
    }

    private static void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        if (blockEntity instanceof MetaMachine || blockEntity instanceof CableBlockEntity) {
            event.addCapability(GTMQoL.id("fe_input"), new FEInputProvider(blockEntity));
        }
    }

    private @Nullable IEnergyContainer energyContainer(@Nullable Direction side) {
        if (blockEntity instanceof CableBlockEntity cable) {
            return cable.getEnergyContainer(side);
        }
        if (blockEntity instanceof MetaMachine machine) {
            return MetaMachine.getCapability(machine, GTCapability.CAPABILITY_ENERGY_CONTAINER, side)
                    .resolve().orElse(null);
        }
        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap != ForgeCapabilities.ENERGY) return LazyOptional.empty();
        IEnergyContainer container = energyContainer(side);
        if (container == null) return LazyOptional.empty();
        return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> new Storage(container, side)));
    }

    private record Storage(IEnergyContainer container, @Nullable Direction side) implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!container.inputsEnergy(side)) return 0;
            long voltage = container.getInputVoltage();
            if (voltage <= 0) return 0;
            int ratio = FeCompat.ratio(true);
            long eu = Math.min(maxReceive / ratio, container.getEnergyCanBeInserted());
            if (eu <= 0) {
                // FE cables often probe with 1 FE, which is less than 1 EU.
                return simulate && maxReceive > 0 && container.getEnergyCanBeInserted() > 0 ? maxReceive : 0;
            }
            // Packets at the input voltage, or one smaller packet: never above it, so nothing explodes.
            long packet = voltage;
            long amps = Math.min(eu / voltage, container.getInputAmperage());
            if (amps <= 0) {
                packet = eu;
                amps = 1;
            }
            if (!simulate) {
                amps = container.acceptEnergyFromNetwork(side, packet, amps);
            }
            return GTMath.saturatedCast(amps * packet * ratio);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return GTMath.saturatedCast(container.getEnergyStored() * FeCompat.ratio(true));
        }

        @Override
        public int getMaxEnergyStored() {
            return GTMath.saturatedCast(container.getEnergyCapacity() * FeCompat.ratio(true));
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return container.inputsEnergy(side);
        }
    }
}
