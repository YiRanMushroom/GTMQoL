package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Energy container of the wireless energy accessor. Cables connect on every side: the front emits up to
 * amperage × V[tier] per tick from the network; every other side accepts up to amperage amps per tick into
 * it, at any voltage (the tier only limits output).
 *
 * <p>Stored and capacity report the network (clamped to a long); nothing is kept in the container itself.</p>
 */
public class WirelessEnergyAccessorContainer extends NotifiableEnergyContainer {

    private final Supplier<@Nullable NetworkId> network;
    private final int tier;
    private final IntSupplier amperage;
    private final Supplier<Direction> front;

    public WirelessEnergyAccessorContainer(Supplier<@Nullable NetworkId> network, int tier, IntSupplier amperage,
                                           Supplier<Direction> front) {
        // Non-zero placeholders so the base class sets handler IO to BOTH; the getters below take over.
        super(0, 1, 1, 1, 1);
        this.network = network;
        this.tier = tier;
        this.amperage = amperage;
        this.front = front;
        setSideInputCondition(side -> side != front.get());
        setSideOutputCondition(side -> side == front.get());
    }

    @Override
    public long getInputVoltage() {
        return GTValues.V[tier];
    }

    @Override
    public long getOutputVoltage() {
        return GTValues.V[tier];
    }

    @Override
    public long getInputAmperage() {
        return amperage.getAsInt();
    }

    @Override
    public long getOutputAmperage() {
        return amperage.getAsInt();
    }

    @Override
    public long getEnergyCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getEnergyStored() {
        NetworkId id = network.get();
        return id == null ? 0 : WirelessEnergySavedData.get().getAffordable(id, 1);
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        NetworkId id = network.get();
        if (id == null) return 0;
        if (energyToAdd > 0) {
            WirelessEnergySavedData.get().insert(id, energyToAdd);
            return energyToAdd;
        }
        if (energyToAdd < 0) {
            return -WirelessEnergySavedData.get().extract(id, -energyToAdd, 1);
        }
        return 0;
    }

    @Override
    public long acceptEnergyFromNetwork(@Nullable Direction side, long voltage, long amperage) {
        long now = getMachine().getOffsetTimer();
        if (lastTimeStamp < now) {
            amps = 0;
            lastTimeStamp = now;
        }
        NetworkId id = network.get();
        if (id == null || voltage <= 0 || amps >= getInputAmperage() || !inputsEnergy(side)) return 0;
        // Keep voltage × amps within a long.
        long accepted = Math.min(Math.min(amperage, getInputAmperage() - amps), Long.MAX_VALUE / voltage);
        if (accepted <= 0) return 0;
        WirelessEnergySavedData.get().insert(id, voltage * accepted);
        amps += accepted;
        return accepted;
    }

    /**
     * Always ticking: the network can be filled by other machines at any time, and the base class only
     * re-checks when its own stored energy changes.
     */
    @Override
    public void checkOutputSubscription() {
        outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
    }

    @Override
    public void serverTick() {
        NetworkId id = network.get();
        if (id == null) return;
        long voltage = getOutputVoltage();
        var data = WirelessEnergySavedData.get();
        long maxAmps = Math.min(getOutputAmperage(), data.getAffordable(id, voltage));
        if (maxAmps <= 0) return;
        Direction side = front.get();
        Direction opposite = side.getOpposite();
        var target = GTCapabilityHelper.getEnergyContainer(getLevel(), getBlockPos().relative(side), opposite);
        if (target == null || !target.inputsEnergy(opposite)) return;
        long used = target.acceptEnergyFromNetwork(opposite, voltage, maxAmps);
        if (used > 0) data.extract(id, used, voltage);
    }
}
