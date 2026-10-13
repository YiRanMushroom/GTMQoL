package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.widgets.ButtonWidget;

/**
 * The auto-build button on every multiblock controller's right configurator column. Built on both sides; the click
 * itself is handled on the client, which sends the request together with its {@link BuildSettings}.
 */
public final class AutoBuildButton {

    private AutoBuildButton() {}

    public static void attach(MultiblockControllerMachine controller, MachineUIPanel panel,
                              PanelSyncManager syncManager) {
        var request = new BuildRequest(controller);
        syncManager.syncValue("gtmqol_auto_build", request);
        IPanelHandler settingsPanel = syncManager.isClient() ?
                IPanelHandler.simple(panel, (parent, player) -> AutoBuildPanel.build(controller), true) :
                null;
        panel.getRightConfiguratorPanel().child(new ButtonWidget<>()
                .size(18, 18)
                .overlay(new ItemDrawable(GTItems.TERMINAL.get()))
                .tooltipDynamic(t -> t.addLine(Component.translatable(AutoBuilder.BUTTON_KEY))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "0"))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "1"))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "3"))
                        .addLine(Component.translatable(AutoBuilder.TOOLTIP_KEY + "2")))
                // only ever called on the client
                .onMousePressed((context, button) -> {
                    if (button == 0) {
                        request.send(Interactable.hasShiftDown(), AutoBuildPanel.settings(controller));
                        return true;
                    }
                    if (button == 1 && settingsPanel != null) {
                        settingsPanel.togglePanel();
                        return true;
                    }
                    return false;
                }));
    }

    /** Client to server: shift + the settings. Runs the build on the server side. */
    private static final class BuildRequest extends SyncHandler<BuildRequest> {

        private final MultiblockControllerMachine controller;

        BuildRequest(MultiblockControllerMachine controller) {
            this.controller = controller;
            allowC2S();
        }

        void send(boolean shift, BuildSettings settings) {
            syncToServer(0, buf -> {
                buf.writeBoolean(shift);
                settings.write(buf);
            });
        }

        @Override
        public void readOnClient(int id, FriendlyByteBuf buf) {}

        @Override
        public void readOnServer(int id, FriendlyByteBuf buf) {
            boolean shift = buf.readBoolean();
            BuildSettings settings = BuildSettings.read(buf);
            if (getSyncManager().getPlayer() instanceof ServerPlayer player) {
                AutoBuilder.onPressed(player, controller, shift, settings);
            }
        }
    }
}
