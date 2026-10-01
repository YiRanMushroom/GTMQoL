package com.yiran.minecraft.gtmqol.wireless.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;

public final class WirelessSteamMachines {

    private static final String NETWORK_TOOLTIP_KEY = "gtmqol.wireless.tooltip.network";
    private static final String HATCH_INPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_input_hatch";
    private static final String HATCH_OUTPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_output_hatch";
    private static final String ACCESSOR_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_accessor";
    private static final String MONITOR_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_monitor";

    private static final ResourceLocation WIRELESS_OVERLAY = GTMQoL.id("block/overlay/machine/overlay_wireless");

    public static MachineDefinition STEAM_INPUT_HATCH;
    public static MachineDefinition STEAM_OUTPUT_HATCH;
    public static MachineDefinition STEAM_ACCESSOR;
    public static MachineDefinition STEAM_MONITOR;

    private WirelessSteamMachines() {}

    public static void init() {
        STEAM_INPUT_HATCH = GTMQoLAddon
                .machine("wireless_steam_input_hatch", info -> new WirelessSteamHatchPartMachine(info, IO.IN))
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.STEAM)
                .modelProperty(IS_FORMED, false)
                .colorOverlaySteamHullModel(WIRELESS_OVERLAY,
                        GTCEu.id("block/overlay/machine/overlay_pipe"),
                        GTCEu.id("block/overlay/machine/overlay_pipe_in_emissive"))
                .themeId(GTGuiTheme.BRONZE.getId())
                .allowCoverOnFront(true)
                .langValue("Wireless Steam Input Hatch")
                .tooltips(Component.translatable(HATCH_INPUT_TOOLTIP_KEY),
                        Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        STEAM_OUTPUT_HATCH = GTMQoLAddon
                .machine("wireless_steam_output_hatch", info -> new WirelessSteamHatchPartMachine(info, IO.OUT))
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.EXPORT_FLUIDS)
                .modelProperty(IS_FORMED, false)
                .colorOverlaySteamHullModel(WIRELESS_OVERLAY,
                        GTCEu.id("block/overlay/machine/overlay_pipe"),
                        GTCEu.id("block/overlay/machine/overlay_pipe_out_emissive"))
                .themeId(GTGuiTheme.BRONZE.getId())
                .allowCoverOnFront(true)
                .langValue("Wireless Steam Output Hatch")
                .tooltips(Component.translatable(HATCH_OUTPUT_TOOLTIP_KEY),
                        Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        STEAM_ACCESSOR = GTMQoLAddon
                .machine("wireless_steam_accessor", WirelessSteamAccessorMachine::new)
                .rotationState(RotationState.ALL)
                .modelProperty(IS_FORMED, false)
                .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
                .themeId(GTGuiTheme.BRONZE.getId())
                .langValue("Wireless Steam Accessor")
                .tooltips(Component.translatable(ACCESSOR_TOOLTIP_KEY),
                        Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        STEAM_MONITOR = GTMQoLAddon
                .machine("wireless_steam_monitor", WirelessSteamMonitorMachine::new)
                .rotationState(RotationState.ALL)
                .modelProperty(IS_FORMED, false)
                .colorOverlaySteamHullModel(GTCEu.id("block/overlay/machine/overlay_screen"), null,
                        GTCEu.id("block/overlay/machine/overlay_screen_emissive"))
                .themeId(GTGuiTheme.BRONZE.getId())
                .langValue("Wireless Steam Monitor")
                .tooltips(Component.translatable(MONITOR_TOOLTIP_KEY),
                        Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();
        addLang(NETWORK_TOOLTIP_KEY, "Bound to whoever places it; shares steam with every wireless machine of their FTB team, or a separate private pool. Data stick: right-click to bind or paste, shift-right-click to copy");
        addLang(HATCH_INPUT_TOOLTIP_KEY, "Supplies steam multiblocks from the wireless network");
        addLang(HATCH_OUTPUT_TOOLTIP_KEY, "Sends steam produced by the multiblock to the wireless network");
        addLang(ACCESSOR_TOOLTIP_KEY, "Pipes can insert steam into or extract it from the wireless network");
        addLang(MONITOR_TOOLTIP_KEY, "Shows the steam stored in the wireless network and its input, output and net rates");
        addLang(WirelessSteamUI.BOUND_TO_KEY, "Bound to: %s");
        addLang(WirelessSteamUI.NETWORK_KEY, "Network: %s");
        addLang(WirelessSteamUI.STORED_KEY, "Steam: %s mB");
        addLang(WirelessSteamUI.PRIVATE_ON_KEY, "Private: your own network, separate from any team (including your personal FTB team)");
        addLang(WirelessSteamUI.PRIVATE_OFF_KEY, "Shared: using your team's network");
        addLang(WirelessSteamUI.BIND_KEY, "Bind");
        addLang(WirelessSteamUI.UNBIND_KEY, "Unbind");
        addLang(WirelessSteamUI.MOVE_TO_TEAM_KEY, "To team");
        addLang(WirelessSteamUI.MOVE_TO_TEAM_TOOLTIP_KEY, "Move all steam in your private network into your team's network and switch this machine to team mode");
        addLang(WirelessSteamAccessorMachine.AUTO_OUTPUT_ON_KEY, "Auto output to front: on");
        addLang(WirelessSteamAccessorMachine.AUTO_OUTPUT_OFF_KEY, "Auto output to front: off");
        addLang(WirelessSteamMonitorMachine.INPUT_RATE_KEY, "Input: %s mB/t");
        addLang(WirelessSteamMonitorMachine.OUTPUT_RATE_KEY, "Output: %s mB/t");
        addLang(WirelessSteamMonitorMachine.NET_RATE_KEY, "Net: %s mB/t (" + WirelessSteamSavedData.IOStats.SAMPLES + " s avg)");
        addLang(WirelessBindingTrait.BOUND_KEY, "Bound to %s");
        addLang(WirelessBindingTrait.UNBOUND_KEY, "Unbound");
        addLang(WirelessBindingTrait.ALREADY_BOUND_KEY, "Already bound to %s");
        addLang(WirelessBindingTrait.NOT_ALLOWED_KEY, "Only the bound player (%s) can do that");
        addLang(WirelessBindingTrait.MOVED_TO_TEAM_KEY, "Moved %s mB of private steam to %s");
        addLang(WirelessBindingTrait.COPIED_KEY, "Wireless binding copied to the data stick");
        addLang(WirelessBindingTrait.STICK_OTHER_PLAYER_KEY, "This data stick holds %s's binding");
        addLang(WirelessBindingTrait.STICK_NAME_KEY, "Wireless Binding (%s)");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
