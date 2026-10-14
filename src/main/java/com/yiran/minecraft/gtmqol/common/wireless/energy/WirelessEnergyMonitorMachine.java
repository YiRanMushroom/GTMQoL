package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessMonitorMachine;

public class WirelessEnergyMonitorMachine extends WirelessMonitorMachine {

    public static final String INPUT_RATE_KEY = "gtmqol.wireless.monitor.energy_input";
    public static final String OUTPUT_RATE_KEY = "gtmqol.wireless.monitor.energy_output";
    public static final String NET_RATE_KEY = "gtmqol.wireless.monitor.energy_net";

    public WirelessEnergyMonitorMachine(BlockEntityCreationInfo info) {
        super(info, WirelessEnergySavedData::get, WirelessEnergyUI.STORED_KEY, INPUT_RATE_KEY, OUTPUT_RATE_KEY,
                NET_RATE_KEY);
    }
}
