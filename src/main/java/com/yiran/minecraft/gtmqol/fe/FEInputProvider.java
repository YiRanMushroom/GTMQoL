package com.yiran.minecraft.gtmqol.fe;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets every GT machine and cable that takes EU on a side also take FE there, converted at GTCEu's
 * {@code feToEuRatio}. The other direction needs nothing: GTCEu already gives FE blocks an EU capability
 * ({@code EUToFEProvider}, {@code nativeEUToFE}), so EU outputs push into them.
 *
 * <p>Registered after gtceu's own providers, so a machine that has its own FE storage keeps it. The energy
 * container is found the same way gtceu's {@code MetaMachineBlock#attachCapabilities} does, not through the
 * capability lookup, otherwise this and gtceu's {@code EUToFEProvider} would ask each other forever on sides
 * without one.</p>
 */
public final class FEInputProvider {

    private FEInputProvider() {}

    /**
     * Call from the mod constructor.
     */
    public static void init(IEventBus modBus) {
        modBus.addListener(FEInputProvider::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof MetaMachineBlock || block instanceof CableBlock) {
                event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
                    IEnergyContainer container = energyContainer(blockEntity, side);
                    return container != null ? new Storage(container, side) : null;
                }, block);
            }
        }
    }

    private static @Nullable IEnergyContainer energyContainer(@Nullable BlockEntity blockEntity,
                                                              @Nullable Direction side) {
        if (blockEntity instanceof CableBlockEntity cable) {
            return cable.getEnergyContainer(side);
        }
        if (blockEntity instanceof MetaMachine machine) {
            if (machine instanceof IEnergyContainer container) return container;
            List<IEnergyContainer> list = new ArrayList<>();
            for (MachineTrait trait : machine.getTraitHolder().getAllTraits()) {
                if (trait.hasCapability(side) && trait instanceof IEnergyContainer container) {
                    list.add(container);
                }
            }
            if (!list.isEmpty()) return list.size() == 1 ? list.getFirst() : new EnergyContainerList(list);
        }
        return null;
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
