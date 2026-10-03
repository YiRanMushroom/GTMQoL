package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.wireless.IOStats;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;

/**
 * Wireless energy machines. Binding lang keys are registered by
 * {@link com.yiran.minecraft.gtmqol.common.wireless.steam.WirelessSteamMachines}.
 */
public final class WirelessEnergyMachines {

    private static final String NETWORK_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_network";
    public static final String AMPERAGE_TOOLTIP_KEY = "gtmqol.wireless.tooltip.adjustable_amperage";
    private static final String OVERCLOCK_TOOLTIP_KEY = "gtmqol.wireless.tooltip.overclock";
    private static final String HATCH_INPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_input_hatch";
    private static final String HATCH_OUTPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_output_hatch";
    private static final String ACCESSOR_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_accessor";
    private static final String ACCESSOR_ANY_VOLTAGE_TOOLTIP_KEY =
            "gtmqol.wireless.tooltip.energy_accessor.any_voltage";
    private static final String MONITOR_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_monitor";

    private static final ResourceLocation WIRELESS_OVERLAY = GTMQoL.id("block/overlay/machine/overlay_wireless");

    @SuppressWarnings("unchecked")
    public static MachineEntry<MachineDefinition>[] ENERGY_INPUT_HATCH = new MachineEntry[TIER_COUNT];
    @SuppressWarnings("unchecked")
    public static MachineEntry<MachineDefinition>[] ENERGY_OUTPUT_HATCH = new MachineEntry[TIER_COUNT];
    @SuppressWarnings("unchecked")
    public static MachineEntry<MachineDefinition>[] ENERGY_ACCESSOR = new MachineEntry[TIER_COUNT];
    public static MachineEntry<MachineDefinition> ENERGY_MONITOR;

    private WirelessEnergyMachines() {}

    private static String name(int tier, String suffix) {
        return VN[tier].toLowerCase(Locale.ROOT) + "_" + suffix;
    }

    public static void init() {
        for (int tier : GTMachineUtils.ALL_TIERS) {
            ENERGY_INPUT_HATCH[tier] = GTMQoLAddon
                    .machine(name(tier, "wireless_energy_input_hatch"),
                            info -> new WirelessEnergyHatchPartMachine(info, tier, IO.IN))
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY, PartAbility.SUBSTATION_INPUT_ENERGY,
                            PartAbility.INPUT_LASER)
                    .modelProperty(IS_FORMED, false)
                    .colorOverlayTieredHullModel(WIRELESS_OVERLAY)
                    .allowCoverOnFront(true)
                    .langValue(VNF[tier] + " Wireless Energy Hatch")
                    .tooltips(Component.translatable(HATCH_INPUT_TOOLTIP_KEY),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable(AMPERAGE_TOOLTIP_KEY,
                                    WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE),
                            tier < MAX ? Component.translatable(OVERCLOCK_TOOLTIP_KEY, VNF[tier + 1]) : null,
                            Component.translatable(NETWORK_TOOLTIP_KEY))
                    .register();

            ENERGY_OUTPUT_HATCH[tier] = GTMQoLAddon
                    .machine(name(tier, "wireless_energy_output_hatch"),
                            info -> new WirelessEnergyHatchPartMachine(info, tier, IO.OUT))
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY, PartAbility.SUBSTATION_OUTPUT_ENERGY,
                            PartAbility.OUTPUT_LASER)
                    .modelProperty(IS_FORMED, false)
                    .colorOverlayTieredHullModel(WIRELESS_OVERLAY)
                    .allowCoverOnFront(true)
                    .langValue(VNF[tier] + " Wireless Dynamo Hatch")
                    .tooltips(Component.translatable(HATCH_OUTPUT_TOOLTIP_KEY),
                            Component.translatable("gtceu.universal.tooltip.voltage_out",
                                    FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable(AMPERAGE_TOOLTIP_KEY,
                                    WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE),
                            Component.translatable(NETWORK_TOOLTIP_KEY))
                    .register();

            ENERGY_ACCESSOR[tier] = GTMQoLAddon
                    .machine(name(tier, "wireless_energy_accessor"),
                            info -> new WirelessEnergyAccessorMachine(info, tier))
                    .tier(tier)
                    .rotationState(RotationState.ALL)
                    .modelProperty(IS_FORMED, false)
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_output_hatch"))
                    .langValue(VNF[tier] + " Wireless Energy Accessor")
                    .tooltips(Component.translatable(ACCESSOR_TOOLTIP_KEY),
                            Component.translatable(ACCESSOR_ANY_VOLTAGE_TOOLTIP_KEY),
                            Component.translatable("gtceu.universal.tooltip.voltage_out",
                                    FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable(AMPERAGE_TOOLTIP_KEY,
                                    WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE),
                            Component.translatable(NETWORK_TOOLTIP_KEY))
                    .register();
        }

        ENERGY_MONITOR = GTMQoLAddon
                .machine("wireless_energy_monitor", WirelessEnergyMonitorMachine::new)
                .tier(LV)
                .rotationState(RotationState.ALL)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/overlay_screen"), null,
                        GTCEu.id("block/overlay/machine/overlay_screen_emissive"))
                .langValue("Wireless Energy Monitor")
                .tooltips(Component.translatable(MONITOR_TOOLTIP_KEY),
                        Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        addLang(NETWORK_TOOLTIP_KEY, "Bound to whoever places it; shares EU with every wireless machine of their FTB team, or a separate private pool. Data stick: right-click to bind or paste, shift-right-click to copy");
        addLang(AMPERAGE_TOOLTIP_KEY, "Amperage adjustable in the UI (default %sA, set your own with /gtmqol default_amperage)");
        addLang(DefaultAmperageCommand.SHOW_KEY, "Your wireless energy machines are placed with %sA");
        addLang(DefaultAmperageCommand.SET_KEY, "Wireless energy machines you place now start with %sA");
        addLang(OVERCLOCK_TOOLTIP_KEY, "Overclock mode: runs at %s, but uses 16× the energy");
        addLang(HATCH_INPUT_TOOLTIP_KEY, "Powers multiblocks (including the power substation and active transformer) from the wireless network");
        addLang(HATCH_OUTPUT_TOOLTIP_KEY, "Sends energy produced by the multiblock to the wireless network");
        addLang(ACCESSOR_TOOLTIP_KEY, "Outputs energy from the wireless network on the front; cables on any other side feed it in");
        addLang(ACCESSOR_ANY_VOLTAGE_TOOLTIP_KEY, "Input accepts any voltage, up to its amperage per tick");
        addLang(MONITOR_TOOLTIP_KEY, "Shows the EU stored in the wireless network and its input, output and net rates");
        addLang(WirelessEnergyUI.STORED_KEY, "EU: %s");
        addLang(WirelessEnergyUI.AMPERAGE_KEY, "Amperage: %sA");
        addLang(WirelessEnergyUI.AMPERAGE_TOOLTIP_KEY, "Buttons: ×4 / ÷4, Shift: ×16 / ÷16, Ctrl: ×2 / ÷2");
        addLang(WirelessEnergyHatchPartMachine.OVERCLOCK_ON_KEY, "Overclock: on (%s, every EU costs 4)");
        addLang(WirelessEnergyHatchPartMachine.OVERCLOCK_OFF_KEY, "Overclock: off (click to run at %s, every EU costs 4)");
        addLang(WirelessEnergyHatchPartMachine.SAVE_KEY, "Save");
        addLang(WirelessEnergyHatchPartMachine.SAVE_TOOLTIP_KEY, "Apply amperage and overclock (closing the UI also applies them)");
        addLang(WirelessEnergyMonitorMachine.INPUT_RATE_KEY, "Input: %s EU/t");
        addLang(WirelessEnergyMonitorMachine.OUTPUT_RATE_KEY, "Output: %s EU/t");
        addLang(WirelessEnergyMonitorMachine.NET_RATE_KEY, "Net: %s EU/t (" + IOStats.SAMPLES + " s avg)");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
