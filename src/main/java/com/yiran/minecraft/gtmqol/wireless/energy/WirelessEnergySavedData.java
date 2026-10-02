package com.yiran.minecraft.gtmqol.wireless.energy;

import com.yiran.minecraft.gtmqol.wireless.IOStats;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessNetworks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * EU stored in every wireless network (see {@link WirelessNetworks#networkId}).
 * Kept on the overworld so all dimensions share it. Server thread only.
 */
public final class WirelessEnergySavedData extends SavedData {

    private static final String NAME = "gtmqol_wireless_energy";
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final Map<NetworkId, BigInteger> energy = new HashMap<>();
    /**
     * Input/output rates (EU/t) per network, for the monitor. {@link #moveAll} doesn't count.
     */
    private final Map<NetworkId, IOStats> stats = new HashMap<>();
    /**
     * Amperage newly placed machines get, per player (set with {@code /gtmqol default_amperage}).
     */
    private final Map<UUID, Integer> defaultAmperage = new HashMap<>();

    private WirelessEnergySavedData() {}

    public int getDefaultAmperage(UUID player) {
        return defaultAmperage.getOrDefault(player, WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE);
    }

    public void setDefaultAmperage(UUID player, int amperage) {
        defaultAmperage.put(player, WirelessEnergyHatchPartMachine.clampAmperage(amperage));
        setDirty();
    }

    public static WirelessEnergySavedData get() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .computeIfAbsent(WirelessEnergySavedData::load, WirelessEnergySavedData::new, NAME);
    }

    public BigInteger getStored(NetworkId network) {
        return energy.getOrDefault(network, BigInteger.ZERO);
    }

    /**
     * How many units of {@code unitCost} EU the network can pay for, clamped to a long.
     */
    public long getAffordable(NetworkId network, long unitCost) {
        return getStored(network).divide(BigInteger.valueOf(unitCost)).min(LONG_MAX).longValue();
    }

    public IOStats getStats(NetworkId network) {
        return stats.computeIfAbsent(network, $ -> new IOStats());
    }

    /**
     * Called every {@link IOStats#SAMPLE_INTERVAL} server ticks.
     */
    public void sampleStats(long tick) {
        stats.values().forEach(s -> s.sample(tick));
    }

    public void insert(NetworkId network, long amount) {
        if (amount <= 0) return;
        energy.merge(network, BigInteger.valueOf(amount), BigInteger::add);
        getStats(network).recordInsert(amount);
        setDirty();
    }

    /**
     * Takes {@code units * unitCost} EU, as many units as the network can pay for, at most {@code maxUnits}.
     *
     * @return the number of units paid for
     */
    public long extract(NetworkId network, long maxUnits, long unitCost) {
        if (maxUnits <= 0) return 0;
        long units = Math.min(maxUnits, getAffordable(network, unitCost));
        if (units > 0) {
            BigInteger cost = BigInteger.valueOf(units).multiply(BigInteger.valueOf(unitCost));
            energy.put(network, getStored(network).subtract(cost));
            getStats(network).recordExtract(cost.longValue());
            setDirty();
        }
        return units;
    }

    /**
     * Moves everything stored in {@code from} into {@code to}.
     */
    public void moveAll(NetworkId from, NetworkId to) {
        if (from.equals(to)) return;
        BigInteger amount = energy.remove(from);
        if (amount == null || amount.signum() == 0) return;
        energy.merge(to, amount, BigInteger::add);
        setDirty();
    }

    private static WirelessEnergySavedData load(CompoundTag tag) {
        WirelessEnergySavedData data = new WirelessEnergySavedData();
        ListTag networks = tag.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networks.size(); i++) {
            CompoundTag network = networks.getCompound(i);
            data.energy.put(new NetworkId(network.getBoolean("private"), network.getUUID("id")),
                    new BigInteger(network.getString("energy")));
        }
        ListTag defaults = tag.getList("default_amperage", Tag.TAG_COMPOUND);
        for (int i = 0; i < defaults.size(); i++) {
            CompoundTag entry = defaults.getCompound(i);
            data.defaultAmperage.put(entry.getUUID("player"), entry.getInt("amperage"));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag networks = new ListTag();
        energy.forEach((id, amount) -> {
            if (amount.signum() == 0) return;
            CompoundTag network = new CompoundTag();
            network.putBoolean("private", id.privateNetwork());
            network.putUUID("id", id.id());
            // BigInteger has no NBT type
            network.putString("energy", amount.toString());
            networks.add(network);
        });
        tag.put("networks", networks);
        ListTag defaults = new ListTag();
        defaultAmperage.forEach((player, amperage) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            entry.putInt("amperage", amperage);
            defaults.add(entry);
        });
        tag.put("default_amperage", defaults);
        return tag;
    }
}
