package com.yiran.minecraft.gtmqol.wireless.steam;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BigIntegerSyncValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;

import java.math.BigInteger;

/**
 * The part of the UI shared by all wireless steam machines: binding, network name, stored steam and the
 * binding buttons.
 */
public final class WirelessSteamUI {

    public static final String BOUND_TO_KEY = "gtmqol.wireless.bound_to";
    public static final String NETWORK_KEY = "gtmqol.wireless.network";
    public static final String STORED_KEY = "gtmqol.wireless.steam_stored";
    public static final String PRIVATE_ON_KEY = "gtmqol.wireless.private.enabled";
    public static final String PRIVATE_OFF_KEY = "gtmqol.wireless.private.disabled";
    public static final String BIND_KEY = "gtmqol.wireless.bind";
    public static final String UNBIND_KEY = "gtmqol.wireless.unbind";
    public static final String MOVE_TO_TEAM_KEY = "gtmqol.wireless.move_to_team";
    public static final String MOVE_TO_TEAM_TOOLTIP_KEY = "gtmqol.wireless.move_to_team.tooltip";

    private WirelessSteamUI() {}

    private static BigInteger storedSteam(WirelessBindingTrait binding) {
        NetworkId id = binding.getNetworkId();
        return id == null ? BigInteger.ZERO : WirelessSteamSavedData.get().getStored(id);
    }

    /**
     * {@code player} is the one who opened the UI; the button actions run on the server as them.
     */
    public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player) {
        var boundTo = new StringSyncValue(binding::getBoundPlayerName);
        var name = new StringSyncValue(binding::getNetworkName);
        var steam = new BigIntegerSyncValue(() -> storedSteam(binding), null);
        var privateNetwork = new BooleanSyncValue(binding::isPrivateNetwork,
                value -> binding.setPrivateNetwork(player, value)).allowC2S();
        syncManager.syncValue("bound_to", boundTo);
        syncManager.syncValue("network_name", name);
        syncManager.syncValue("network_steam", steam);
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
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(BOUND_TO_KEY, boundTo.getValue())))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(NETWORK_KEY, name.getValue())))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(STORED_KEY,
                        FormattingUtil.formatNumbers(steam.getValue()))))
                        .horizontalCenter())
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(new ButtonWidget<>()
                                .size(50, 18)
                                .overlay(Text.dynamic(() -> Component.translatable(
                                        binding.isBound() ? UNBIND_KEY : BIND_KEY)))
                                .syncHandler(toggleBinding))
                        .child(new ToggleButton()
                                .syncHandler("private_network")
                                .tooltipDynamic(t -> t.addLine(Component.translatable(
                                        privateNetwork.getBoolValue() ? PRIVATE_ON_KEY : PRIVATE_OFF_KEY)))
                                .overlay(false, GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                                .overlay(true, GTGuiTextures.PRIVATE_MODE_BUTTON[1])
                                .background(GuiTextures.MC_BUTTON)
                                .background(true, GuiTextures.MC_BUTTON_PRESSED))
                        .child(new ButtonWidget<>()
                                .size(50, 18)
                                .overlay(Text.lang(MOVE_TO_TEAM_KEY))
                                .tooltipDynamic(t -> t.addLine(Component.translatable(MOVE_TO_TEAM_TOOLTIP_KEY)))
                                .syncHandler(moveToTeam)));
    }
}
