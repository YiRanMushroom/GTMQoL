package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessMonitorMachine;

public class WirelessSteamMonitorMachine extends WirelessMonitorMachine {

    public static final String INPUT_RATE_KEY = "gtmqol.wireless.monitor.input";
    public static final String OUTPUT_RATE_KEY = "gtmqol.wireless.monitor.output";
    public static final String NET_RATE_KEY = "gtmqol.wireless.monitor.net";

    public WirelessSteamMonitorMachine(BlockEntityCreationInfo info) {
        super(info, WirelessSteamSavedData::get, WirelessSteamUI.STORED_KEY, INPUT_RATE_KEY, OUTPUT_RATE_KEY,
                NET_RATE_KEY);
    }
}
