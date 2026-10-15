package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessUI;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** {@link WirelessUI} with the stored EU, and the amperage input. */
public final class WirelessEnergyUI {

    public static final String STORED_KEY = "gtmqol.wireless.energy_stored";
    public static final String AMPERAGE_KEY = "gtmqol.wireless.amperage";
    public static final String AMPERAGE_TOOLTIP_KEY = "gtmqol.wireless.amperage.tooltip";

    private WirelessEnergyUI() {}

    public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player) {
        return WirelessUI.create(syncManager, binding, player, STORED_KEY, WirelessEnergySavedData::get);
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
