package com.yiran.minecraft.gtmqol.wireless.energy;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamUI;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.BigIntegerSyncValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.math.BigInteger;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * The part of the UI shared by all wireless energy machines: binding, network name, stored EU and the
 * binding buttons. Binding lang keys are the steam ones ({@link WirelessSteamUI}).
 */
public final class WirelessEnergyUI {

    public static final String STORED_KEY = "gtmqol.wireless.energy_stored";
    public static final String AMPERAGE_KEY = "gtmqol.wireless.amperage";
    public static final String AMPERAGE_TOOLTIP_KEY = "gtmqol.wireless.amperage.tooltip";

    private WirelessEnergyUI() {}

    private static BigInteger storedEnergy(WirelessBindingTrait binding) {
        NetworkId id = binding.getNetworkId();
        return id == null ? BigInteger.ZERO : WirelessEnergySavedData.get().getStored(id);
    }

    /**
     * {@code player} is the one who opened the UI; the button actions run on the server as them.
     */
    public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player) {
        var boundTo = new StringSyncValue(binding::getBoundPlayerName);
        var name = new StringSyncValue(binding::getNetworkName);
        var energy = new BigIntegerSyncValue(() -> storedEnergy(binding), null);
        var privateNetwork = new BooleanSyncValue(binding::isPrivateNetwork,
                value -> binding.setPrivateNetwork(player, value)).allowC2S();
        syncManager.syncValue("bound_to", boundTo);
        syncManager.syncValue("network_name", name);
        syncManager.syncValue("network_energy", energy);
        syncManager.syncValue("private_network", privateNetwork);

        var toggleBinding = new InteractionSyncHandler();
        toggleBinding.setOnMousePressed(mouse -> binding.toggleBinding(player));
        var moveToTeam = new InteractionSyncHandler();
        moveToTeam.setOnMousePressed(mouse -> binding.movePrivateToTeam(player));

        return Flow.col()
                .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT)
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .childPadding(4)
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(WirelessSteamUI.BOUND_TO_KEY,
                        boundTo.getValue())))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(WirelessSteamUI.NETWORK_KEY,
                        name.getValue())))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(STORED_KEY,
                        FormattingUtil.formatNumbers(energy.getValue()))))
                        .horizontalCenter())
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(new ButtonWidget<>()
                                .size(50, 18)
                                .overlay(Text.dynamic(() -> Component.translatable(
                                        binding.isBound() ? WirelessSteamUI.UNBIND_KEY : WirelessSteamUI.BIND_KEY)))
                                .syncHandler(toggleBinding))
                        .child(new ToggleButton()
                                .syncHandler("private_network")
                                .tooltipDynamic(t -> t.addLine(Component.translatable(
                                        privateNetwork.getBoolValue() ? WirelessSteamUI.PRIVATE_ON_KEY :
                                                WirelessSteamUI.PRIVATE_OFF_KEY)))
                                .overlay(false, GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                                .overlay(true, GTGuiTextures.PRIVATE_MODE_BUTTON[1])
                                .background(GuiTextures.MC_BUTTON)
                                .background(true, GuiTextures.MC_BUTTON_PRESSED))
                        .child(new ButtonWidget<>()
                                .size(50, 18)
                                .overlay(Text.lang(WirelessSteamUI.MOVE_TO_TEAM_KEY))
                                .tooltipDynamic(t -> t.addLine(Component.translatable(
                                        WirelessSteamUI.MOVE_TO_TEAM_TOOLTIP_KEY)))
                                .syncHandler(moveToTeam)));
    }

    /**
     * Amperage input, 1 to {@link WirelessEnergyHatchPartMachine#MAX_AMPERAGE}: type a value, or multiply/divide with the buttons
     * (×4, Shift ×16, Ctrl ×2). Same layout as GTCEu's {@code GTMuiWidgets.createIntInputWithButtons}, which
     * only adds and subtracts.
     */
    public static Flow amperageRow(PanelSyncManager syncManager, IntSupplier getter, IntConsumer setter) {
        var amperage = new IntSyncValue(getter, setter).allowC2S();
        syncManager.syncValue("amperage", amperage);
        var text = new StringSyncValue(amperage::getStringValue, amperage::setStringValue).allowC2S();

        return Flow.row()
                .coverChildrenHeight()
                .width(140)
                .horizontalCenter()
                .child(scaleButton(amperage, false))
                .child(new TextFieldWidget()
                        .left(18).right(18)
                        .setTextAlignment(Alignment.Center)
                        .setTextColor(Color.WHITE.darker(1))
                        .setNumbers(1, WirelessEnergyHatchPartMachine.MAX_AMPERAGE)
                        .value(text)
                        .background(GTGuiTextures.DISPLAY)
                        .tooltipDynamic(t -> t.addLine(Component.translatable(AMPERAGE_KEY,
                                FormattingUtil.formatNumbers(amperage.getIntValue())))
                                .addLine(Component.translatable(AMPERAGE_TOOLTIP_KEY))))
                .child(scaleButton(amperage, true).right(0));
    }

    private static ButtonWidget<?> scaleButton(IntSyncValue amperage, boolean multiply) {
        return new ButtonWidget<>()
                .width(18)
                .onMousePressed((context, button) -> {
                    long factor = scaleFactor(MouseData.create(button));
                    long value = amperage.getIntValue();
                    value = multiply ? Math.min(value * factor, WirelessEnergyHatchPartMachine.MAX_AMPERAGE) : Math.max(value / factor, 1);
                    amperage.setIntValue((int) value, true, true);
                    return true;
                })
                .onUpdateListener(w -> w.overlay(Text.str((multiply ? "×" : "÷") +
                        scaleFactor(MouseData.create(-1)))
                        .color(Color.WHITE.main)
                        .scale(0.8f)));
    }

    private static long scaleFactor(MouseData mouse) {
        if (mouse.shift()) return 16;
        if (mouse.ctrl()) return 2;
        return 4;
    }
}
