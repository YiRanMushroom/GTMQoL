package com.yiran.minecraft.gtmqol.common.ui;

import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

/** Shared look of our popups (open/close them with {@code IPanelHandler.togglePanel}). */
public final class PopupWidgets {

    private PopupWidgets() {}

    /** GTCEu's popup (background, close button top right), movable, the main UI stays usable. */
    public static PopupPanel popup(String name, int width, int height) {
        return PopupPanel.createPopupPanel(name, width, height)
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true);
    }

    /** {@code [-] label [+]}; Shift steps by {@code shiftStep}, Ctrl doubles or halves. */
    public static Flow counterRow(IntSyncValue value, int max, int shiftStep, String langKey) {
        return Flow.row()
                .height(18)
                .childPadding(4)
                .child(stepButton(value, max, shiftStep, false))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(langKey, value.getIntValue())))
                        .width(100)
                        .verticalCenter())
                .child(stepButton(value, max, shiftStep, true));
    }

    private static ButtonWidget<?> stepButton(IntSyncValue value, int max, int shiftStep, boolean up) {
        return new ButtonWidget<>()
                .size(18)
                .overlay(Text.str(up ? "+" : "-"))
                .onMousePressed((context, button) -> {
                    MouseData mouse = MouseData.create(button);
                    int current = value.getIntValue();
                    int next;
                    if (mouse.ctrl()) {
                        next = up ? current * 2 : current / 2;
                    } else {
                        int step = mouse.shift() ? shiftStep : 1;
                        next = current + (up ? step : -step);
                    }
                    next = Mth.clamp(next, 1, max);
                    value.setIntValue(next, true, true);
                    return true;
                });
    }
}
