package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessPool;

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
import java.util.UUID;

/** EU in every wireless network, and each player's default amperage. */
public final class WirelessEnergySavedData extends WirelessPool {

    private static final String NAME = "gtmqol_wireless_energy";
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    /**
     * Amperage newly placed machines get, per player (set with {@code /gtmqol default_amperage}).
     */
    private final Map<UUID, Integer> defaultAmperage = new HashMap<>();

    private WirelessEnergySavedData() {
        super("energy");
    }

    public static WirelessEnergySavedData get() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(WirelessEnergySavedData::new, WirelessEnergySavedData::load), NAME);
    }

    public int getDefaultAmperage(UUID player) {
        return defaultAmperage.getOrDefault(player, WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE);
    }

    public void setDefaultAmperage(UUID player, int amperage) {
        defaultAmperage.put(player, WirelessEnergyHatchPartMachine.clampAmperage(amperage));
        setDirty();
    }

    /**
     * How many units of {@code unitCost} EU the network can pay for, clamped to a long.
     */
    public long getAffordable(NetworkId network, long unitCost) {
        return getStored(network).divide(BigInteger.valueOf(unitCost)).min(LONG_MAX).longValue();
    }

    /**
     * Takes {@code units * unitCost} EU, as many units as the network can pay for, at most {@code maxUnits}.
     *
     * @return the number of units paid for
     */
    public long extract(NetworkId network, long maxUnits, long unitCost) {
        if (maxUnits <= 0) return 0;
        long units = Math.min(maxUnits, getAffordable(network, unitCost));
        if (units > 0) take(network, BigInteger.valueOf(units).multiply(BigInteger.valueOf(unitCost)));
        return units;
    }

    private static WirelessEnergySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WirelessEnergySavedData data = new WirelessEnergySavedData();
        data.loadNetworks(tag);
        ListTag defaults = tag.getList("default_amperage", Tag.TAG_COMPOUND);
        for (int i = 0; i < defaults.size(); i++) {
            CompoundTag entry = defaults.getCompound(i);
            data.defaultAmperage.put(entry.getUUID("player"), entry.getInt("amperage"));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        super.save(tag, registries);
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
