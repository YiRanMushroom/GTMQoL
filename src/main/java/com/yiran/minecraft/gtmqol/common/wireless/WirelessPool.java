package com.yiran.minecraft.gtmqol.common.wireless;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * What every wireless network (see {@link WirelessNetworks#networkId}) holds of one thing, EU or steam, and its
 * input/output rates for the monitor. Kept on the overworld so all dimensions share it. Server thread only.
 */
public abstract class WirelessPool extends SavedData {

    /** The NBT key of each network's amount. */
    private final String amountKey;
    private final Map<NetworkId, BigInteger> stored = new HashMap<>();
    /** {@link #moveAll} doesn't count. */
    private final Map<NetworkId, IOStats> stats = new HashMap<>();

    protected WirelessPool(String amountKey) {
        this.amountKey = amountKey;
    }

    public BigInteger getStored(NetworkId network) {
        return stored.getOrDefault(network, BigInteger.ZERO);
    }

    public IOStats getStats(NetworkId network) {
        return stats.computeIfAbsent(network, $ -> new IOStats());
    }

    /** Called every {@link IOStats#SAMPLE_INTERVAL} server ticks. */
    public void sampleStats(long tick) {
        stats.values().forEach(s -> s.sample(tick));
    }

    public void insert(NetworkId network, long amount) {
        if (amount <= 0) return;
        stored.merge(network, BigInteger.valueOf(amount), BigInteger::add);
        getStats(network).recordInsert(amount);
        setDirty();
    }

    /** Takes {@code amount}, which the caller has checked the network has. */
    protected void take(NetworkId network, BigInteger amount) {
        stored.put(network, getStored(network).subtract(amount));
        getStats(network).recordExtract(amount.longValue());
        setDirty();
    }

    /** Moves everything stored in {@code from} into {@code to}. */
    public void moveAll(NetworkId from, NetworkId to) {
        if (from.equals(to)) return;
        BigInteger amount = stored.remove(from);
        if (amount == null || amount.signum() == 0) return;
        stored.merge(to, amount, BigInteger::add);
        setDirty();
    }

    protected void loadNetworks(CompoundTag tag) {
        ListTag networks = tag.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networks.size(); i++) {
            CompoundTag network = networks.getCompound(i);
            stored.put(new NetworkId(network.getBoolean("private"), network.getUUID("id")),
                    new BigInteger(network.getString(amountKey)));
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        ListTag networks = new ListTag();
        stored.forEach((id, amount) -> {
            if (amount.signum() == 0) return;
            CompoundTag network = new CompoundTag();
            network.putBoolean("private", id.privateNetwork());
            network.putUUID("id", id.id());
            // BigInteger has no NBT type
            network.putString(amountKey, amount.toString());
            networks.add(network);
        });
        tag.put("networks", networks);
        return tag;
    }
}
