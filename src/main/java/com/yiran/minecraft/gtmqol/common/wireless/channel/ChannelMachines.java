package com.yiran.minecraft.gtmqol.common.wireless.channel;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.wireless.computation.ChannelComputation;
import com.yiran.minecraft.gtmqol.common.wireless.computation.WirelessComputationHatchPartMachine;
import com.yiran.minecraft.gtmqol.common.wireless.computation.WirelessComputationRouterMachine;
import com.yiran.minecraft.gtmqol.common.wireless.optical.WirelessDataHatchPartMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gregtechceu.gtceu.api.GTValues.ZPM;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;

/**
 * The channel machines: wireless computation hatches and router, wireless optical data hatches. Same tiers as
 * gtceu's optical hatches; bound only from their UI ({@link ChannelUI}).
 */
// Untested: channel UI (list, create/edit/delete, private/team/public); an HPCA's wireless
// transmitter feeds a research station's receiver; a second receiver is refused without a router and allowed with one;
// a data bank's wireless transmitter gives an assembly line its research; bindings survive a reload
public final class ChannelMachines {

    private static final String CHANNEL_TOOLTIP_KEY = "gtmqol.channel.tooltip";
    private static final String NO_SHARING_KEY = "gtceu.part_sharing.disabled";

    private static final ResourceLocation WIRELESS_OVERLAY = GTMQoL.id("block/overlay/machine/overlay_wireless");

    public static MachineEntry<MachineDefinition> COMPUTATION_TRANSMISSION_HATCH;
    public static MachineEntry<MachineDefinition> COMPUTATION_RECEPTION_HATCH;
    public static MachineEntry<MachineDefinition> COMPUTATION_ROUTER;
    public static MachineEntry<MachineDefinition> OPTICAL_DATA_TRANSMISSION_HATCH;
    public static MachineEntry<MachineDefinition> OPTICAL_DATA_RECEPTION_HATCH;

    private static boolean commonAdded;

    private ChannelMachines() {}

    public static void initComputation() {
        common();
        COMPUTATION_TRANSMISSION_HATCH = hatch("wireless_computation_transmission_hatch",
                "Wireless Computation Transmission Hatch", ZPM,
                info -> new WirelessComputationHatchPartMachine(info, true),
                PartAbility.COMPUTATION_DATA_TRANSMISSION, "gtmqol.channel.tooltip.computation_transmission");
        COMPUTATION_RECEPTION_HATCH = hatch("wireless_computation_reception_hatch",
                "Wireless Computation Reception Hatch", ZPM,
                info -> new WirelessComputationHatchPartMachine(info, false),
                PartAbility.COMPUTATION_DATA_RECEPTION, "gtmqol.channel.tooltip.computation_reception");
        COMPUTATION_ROUTER = GTMQoLAddon
                .machine("wireless_computation_router", WirelessComputationRouterMachine::new)
                .tier(ZPM)
                .rotationState(RotationState.ALL)
                // the network switch's screen, which it is made from
                .colorOverlayTieredHullModel(GTCEu.id("block/multiblock/network_switch/overlay_front"), null,
                        GTCEu.id("block/multiblock/network_switch/overlay_front_emissive"))
                .langValue("Wireless Computation Network Router")
                .tooltips(Component.translatable("gtmqol.channel.tooltip.router"),
                        Component.translatable(CHANNEL_TOOLTIP_KEY))
                .register();
        addLang("gtmqol.channel.tooltip.computation_transmission",
                "Sends the CWU/t of its HPCA or network switch into its computation channel");
        addLang("gtmqol.channel.tooltip.computation_reception",
                "Gives its research station or network switch the CWU/t of its computation channel");
        addLang("gtmqol.channel.tooltip.router",
                "Lets its computation channel have any number of transmitters and receivers (one of each without a router). Needs no energy and doesn't need to be loaded");
    }

    public static void initOptical() {
        common();
        OPTICAL_DATA_TRANSMISSION_HATCH = hatch("wireless_optical_data_transmission_hatch", "Wireless Optical Data Transmission Hatch", LuV,
                info -> new WirelessDataHatchPartMachine(info, true),
                PartAbility.OPTICAL_DATA_TRANSMISSION, "gtmqol.channel.tooltip.optical_data_transmission");
        OPTICAL_DATA_RECEPTION_HATCH = hatch("wireless_optical_data_reception_hatch", "Wireless Optical Data Reception Hatch", LuV,
                info -> new WirelessDataHatchPartMachine(info, false),
                PartAbility.OPTICAL_DATA_RECEPTION, "gtmqol.channel.tooltip.optical_data_reception");
        addLang("gtmqol.channel.tooltip.optical_data_transmission",
                "Shares the research of its data bank with its optical data channel");
        addLang("gtmqol.channel.tooltip.optical_data_reception",
                "Gives its assembly line the research of every data bank on its optical data channel");
    }

    private static MachineEntry<MachineDefinition> hatch(
            String name, String langValue, int tier,
            com.gregtechceu.gtceu.api.machine.MachineInstanceFactory<?> factory, PartAbility ability,
            String tooltipKey) {
        return GTMQoLAddon.machine(name, factory)
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(ability)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(WIRELESS_OVERLAY)
                .langValue(langValue)
                .tooltips(Component.translatable(tooltipKey), Component.translatable(CHANNEL_TOOLTIP_KEY),
                        Component.translatable(NO_SHARING_KEY))
                .register();
    }

    /** Lang and the server stop cleanup both kinds share; once. */
    private static void common() {
        if (commonAdded) return;
        commonAdded = true;
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            ChannelRegistry.clear();
            ChannelComputation.clear();
        });
        addLang(CHANNEL_TOOLTIP_KEY,
                "Open its UI to create channels and bind it (private, team or public; works across dimensions while both ends are loaded)");
        ChannelUI.addLang();
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
