package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * FE to and from a wireless network, at GTCEu's converter ratios. A network holds whole EU, so what doesn't make a
 * whole EU waits here (not saved, it is less than 1 EU). Amounts are doubles because AE2's power is fractional.
 * Server only.
 */
public final class WirelessFEBuffer {

    private final Supplier<@Nullable NetworkId> network;
    private double spare;

    public WirelessFEBuffer(Supplier<@Nullable NetworkId> network) {
        this.network = network;
    }

    public boolean canInsert() {
        return network.get() != null;
    }

    public double getStored() {
        NetworkId id = network.get();
        if (id == null) return 0;
        return spare + WirelessEnergySavedData.get().getStored(id).doubleValue() * FeCompat.ratio(false);
    }

    /** All of it goes in, networks have no limit. */
    public void insert(double fe) {
        NetworkId id = network.get();
        if (id == null || fe <= 0) return;
        int ratio = FeCompat.ratio(true);
        double total = spare + fe;
        long eu = (long) (total / ratio);
        WirelessEnergySavedData.get().insert(id, eu);
        spare = total - (double) eu * ratio;
    }

    /**
     * @return what was taken, at most {@code fe}
     */
    public double extract(double fe, boolean simulate) {
        NetworkId id = network.get();
        if (id == null || fe <= 0) return 0;
        int ratio = FeCompat.ratio(false);
        var data = WirelessEnergySavedData.get();
        if (simulate) return Math.min(fe, spare + (double) data.getAffordable(id, 1) * ratio);
        double missing = fe - spare;
        long eu = missing <= 0 ? 0 : data.extract(id, (long) Math.ceil(missing / ratio), 1);
        double available = spare + (double) eu * ratio;
        double taken = Math.min(fe, available);
        spare = available - taken;
        return taken;
    }
}
