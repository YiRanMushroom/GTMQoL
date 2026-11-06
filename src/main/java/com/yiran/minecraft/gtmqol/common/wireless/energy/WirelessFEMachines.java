package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.client.EnergyCubeSidesRender;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;

/**
 * FE from the wireless energy network, converted at GTCEu's ratios: two hatches with abilities of their own,
 * and the energy cube for FE blocks and cables. Networks hold no FE, so none of them has a capacity or a rate.
 */
public final class WirelessFEMachines {

    /** Gives the multiblock FE. */
    public static final PartAbility FE_INPUT = new PartAbility("fe_input");
    /** Takes FE from the multiblock. */
    public static final PartAbility FE_OUTPUT = new PartAbility("fe_output");

    private static final String NETWORK_TOOLTIP_KEY = "gtmqol.wireless.tooltip.fe_network";
    private static final String INPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.fe_input_hatch";
    private static final String OUTPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.fe_output_hatch";
    private static final String CUBE_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_cube";

    public static MachineDefinition FE_INPUT_HATCH;
    public static MachineDefinition FE_OUTPUT_HATCH;
    public static MachineDefinition ENERGY_CUBE;

    private WirelessFEMachines() {}

    public static void init() {
        // Untested: the cube's side squares and overlays, FE in and out of it, the wireless energy cell on an ME network
        FE_INPUT_HATCH = GTMQoLAddon
                .machine("wireless_fe_input_hatch", info -> new WirelessFEHatchPartMachine(info, IO.IN))
                .tier(LV)
                .rotationState(RotationState.ALL)
                .abilities(FE_INPUT)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(WirelessEnergyMachines.WIRELESS_OVERLAY)
                .langValue("Wireless FE Input Hatch")
                .tooltips(Component.translatable(INPUT_TOOLTIP_KEY), Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        FE_OUTPUT_HATCH = GTMQoLAddon
                .machine("wireless_fe_output_hatch", info -> new WirelessFEHatchPartMachine(info, IO.OUT))
                .tier(LV)
                .rotationState(RotationState.ALL)
                .abilities(FE_OUTPUT)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(WirelessEnergyMachines.WIRELESS_OVERLAY)
                .langValue("Wireless FE Output Hatch")
                .tooltips(Component.translatable(OUTPUT_TOOLTIP_KEY), Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        ENERGY_CUBE = GTMQoLAddon
                .machine("wireless_energy_cube", WirelessEnergyCubeMachine::new)
                .tier(LV)
                .rotationState(RotationState.NONE)
                .model(GTMachineModels.createTieredHullMachineModel(GTCEu.id("block/machine/template/sided/sided"))
                        .andThen(b -> b.addDynamicRenderer(EnergyCubeSidesRender::new)))
                .langValue("Wireless Energy Cube")
                .tooltips(Component.translatable(CUBE_TOOLTIP_KEY), Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();

        addLang(NETWORK_TOOLTIP_KEY, "Uses the EU of its wireless network, converted to FE. No limit on the rate");
        addLang(INPUT_TOOLTIP_KEY, "Gives its multiblock FE from the wireless network");
        addLang(OUTPUT_TOOLTIP_KEY, "Sends FE from its multiblock to the wireless network");
        addLang(CUBE_TOOLTIP_KEY, "Every side can be set to take FE in or give it out");
        for (Direction side : Direction.values()) {
            String name = side.getName();
            addLang(WirelessEnergyCubeMachine.SIDE_KEY + name,
                    name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1) + ": %s");
        }
        addLang(WirelessEnergyCubeMachine.MODE_KEY + "none", "Off");
        addLang(WirelessEnergyCubeMachine.MODE_KEY + "input", "Input");
        addLang(WirelessEnergyCubeMachine.MODE_KEY + "output", "Output");
        addLang(WirelessEnergyCubeMachine.CHANGE_KEY, "Click to change");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
