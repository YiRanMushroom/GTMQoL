package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessUI;

import net.minecraft.world.entity.player.Player;

import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;

/** {@link WirelessUI} with the stored steam. */
public final class WirelessSteamUI {

    public static final String STORED_KEY = "gtmqol.wireless.steam_stored";

    private WirelessSteamUI() {}

    public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player) {
        return WirelessUI.create(syncManager, binding, player, STORED_KEY, WirelessSteamSavedData::get);
    }
}
