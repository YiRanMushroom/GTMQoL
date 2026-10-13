package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;

/** The auto-build button on every multiblock controller's right configurator column. Built on both sides. */
public final class AutoBuildButton {

    private AutoBuildButton() {}

    public static void attach(MultiblockControllerMachine controller, MachineUIPanel panel,
                              PanelSyncManager syncManager) {
        var press = new InteractionSyncHandler();
        press.setOnMousePressed(mouse -> {
            // right click is reserved for the settings window
            if (mouse.isClient() || !mouse.isLeftMouseButton()) return;
            if (syncManager.getPlayer() instanceof ServerPlayer player) {
                AutoBuilder.onPressed(player, controller, mouse.shift());
            }
        });
        panel.getRightConfiguratorPanel().child(new ButtonWidget<>()
                .size(18, 18)
                .overlay(new ItemDrawable(GTItems.TERMINAL.get()))
                .tooltipDynamic(t -> t.addLine(Component.translatable(AutoBuilder.BUTTON_KEY))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "0", AutoBuilder.BLOCKS_PER_BATCH))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "1", AutoBuilder.BLOCKS_PER_BATCH,
                                AutoBuilder.INTERVAL))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "2")))
                .syncHandler(press));
    }
}
