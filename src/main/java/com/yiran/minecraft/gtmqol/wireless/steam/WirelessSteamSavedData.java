package com.yiran.minecraft.gtmqol.wireless.steam;

import com.yiran.minecraft.gtmqol.wireless.IOStats;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessNetworks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Steam stored in every wireless network (see {@link WirelessNetworks#networkId}).
 * Kept on the overworld so all dimensions share it. Server thread only.
 */
public final class WirelessSteamSavedData extends SavedData {

    private static final String NAME = "gtmqol_wireless_steam";
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    private final Map<NetworkId, BigInteger> steam = new HashMap<>();
    /**
     * Input/output rates (mB/t) per network, for the monitor. {@link #moveAll} doesn't count.
     */
    private final Map<NetworkId, IOStats> stats = new HashMap<>();

    private WirelessSteamSavedData() {}

    public static WirelessSteamSavedData get() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(WirelessSteamSavedData::new, WirelessSteamSavedData::load), NAME);
    }

    public BigInteger getStored(NetworkId network) {
        return steam.getOrDefault(network, BigInteger.ZERO);
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

    /**
     * Stored amount clamped to an int, for Forge fluid handlers.
     */
    public int getStoredInt(NetworkId network) {
        return getStored(network).min(INT_MAX).intValue();
    }

    public void insert(NetworkId network, long amount) {
        if (amount <= 0) return;
        steam.merge(network, BigInteger.valueOf(amount), BigInteger::add);
        getStats(network).recordInsert(amount);
        setDirty();
    }

    /**
     * @return the amount actually extracted, at most {@code max}
     */
    public int extract(NetworkId network, int max, boolean simulate) {
        if (max <= 0) return 0;
        int extracted = Math.min(max, getStoredInt(network));
        if (extracted > 0 && !simulate) {
            steam.put(network, getStored(network).subtract(BigInteger.valueOf(extracted)));
            getStats(network).recordExtract(extracted);
            setDirty();
        }
        return extracted;
    }

    /**
     * Moves everything stored in {@code from} into {@code to}.
     */
    public void moveAll(NetworkId from, NetworkId to) {
        if (from.equals(to)) return;
        BigInteger amount = steam.remove(from);
        if (amount == null || amount.signum() == 0) return;
        steam.merge(to, amount, BigInteger::add);
        setDirty();
    }

    private static WirelessSteamSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WirelessSteamSavedData data = new WirelessSteamSavedData();
        ListTag networks = tag.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networks.size(); i++) {
            CompoundTag network = networks.getCompound(i);
            data.steam.put(new NetworkId(network.getBoolean("private"), network.getUUID("id")),
                    new BigInteger(network.getString("steam")));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        ListTag networks = new ListTag();
        steam.forEach((id, amount) -> {
            if (amount.signum() == 0) return;
            CompoundTag network = new CompoundTag();
            network.putBoolean("private", id.privateNetwork());
            network.putUUID("id", id.id());
            // BigInteger has no NBT type
            network.putString("steam", amount.toString());
            networks.add(network);
        });
        tag.put("networks", networks);
        return tag;
    }
}
