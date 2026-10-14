package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessPool;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.math.BigInteger;

/** Steam (mB) in every wireless network. */
public final class WirelessSteamSavedData extends WirelessPool {

    private static final String NAME = "gtmqol_wireless_steam";
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    private WirelessSteamSavedData() {
        super("steam");
    }

    public static WirelessSteamSavedData get() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(WirelessSteamSavedData::new, WirelessSteamSavedData::load), NAME);
    }

    /**
     * Stored amount clamped to an int, for Forge fluid handlers.
     */
    public int getStoredInt(NetworkId network) {
        return getStored(network).min(INT_MAX).intValue();
    }

    /**
     * @return the amount actually extracted, at most {@code max}
     */
    public int extract(NetworkId network, int max, boolean simulate) {
        if (max <= 0) return 0;
        int extracted = Math.min(max, getStoredInt(network));
        if (extracted > 0 && !simulate) take(network, BigInteger.valueOf(extracted));
        return extracted;
    }

    private static WirelessSteamSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WirelessSteamSavedData data = new WirelessSteamSavedData();
        data.loadNetworks(tag);
        return data;
    }
}
