package com.yiran.minecraft.gtmqol.common.wireless;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

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
import java.util.function.Supplier;

/**
 * The part of the UI shared by every wireless machine and cover: binding, network name, what the network stores and
 * the binding buttons. Also the lang both kinds share.
 */
public final class WirelessUI {

    public static final String BOUND_TO_KEY = "gtmqol.wireless.bound_to";
    public static final String NETWORK_KEY = "gtmqol.wireless.network";
    public static final String PRIVATE_ON_KEY = "gtmqol.wireless.private.enabled";
    public static final String PRIVATE_OFF_KEY = "gtmqol.wireless.private.disabled";
    public static final String BIND_KEY = "gtmqol.wireless.bind";
    public static final String UNBIND_KEY = "gtmqol.wireless.unbind";
    public static final String MOVE_TO_TEAM_KEY = "gtmqol.wireless.move_to_team";
    public static final String MOVE_TO_TEAM_TOOLTIP_KEY = "gtmqol.wireless.move_to_team.tooltip";

    private static boolean langAdded;

    private WirelessUI() {}

    /**
     * {@code player} is the one who opened the UI; the button actions run on the server as them.
     *
     * @param storedKey lang key of the stored amount line, with the amount as {@code %s}
     * @param pool      read on the server only
     */
    public static Flow create(PanelSyncManager syncManager, WirelessBindingTrait binding, Player player,
                              String storedKey, Supplier<? extends WirelessPool> pool) {
        var boundTo = new StringSyncValue(binding::getBoundPlayerName);
        var name = new StringSyncValue(binding::getNetworkName);
        var stored = new BigIntegerSyncValue(() -> {
            NetworkId id = binding.getNetworkId();
            return id == null ? BigInteger.ZERO : pool.get().getStored(id);
        }, null);
        var privateNetwork = new BooleanSyncValue(binding::isPrivateNetwork,
                value -> binding.setPrivateNetwork(player, value)).allowC2S();
        syncManager.syncValue("bound_to", boundTo);
        syncManager.syncValue("network_name", name);
        syncManager.syncValue("network_stored", stored);
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
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(storedKey,
                        FormattingUtil.formatNumbers(stored.getValue()))))
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

    /** Lang of the shared UI, the binding and the covers. Called by each kind; adds it once. */
    public static void addSharedLang() {
        // the lang provider throws on a duplicate key
        if (langAdded) return;
        langAdded = true;
        addLang(BOUND_TO_KEY, "Bound to: %s");
        addLang(NETWORK_KEY, "Network: %s");
        addLang(PRIVATE_ON_KEY, "Private: your own network, separate from any team (including your personal FTB team)");
        addLang(PRIVATE_OFF_KEY, "Shared: using your team's network");
        addLang(BIND_KEY, "Bind");
        addLang(UNBIND_KEY, "Unbind");
        addLang(MOVE_TO_TEAM_KEY, "To team");
        addLang(MOVE_TO_TEAM_TOOLTIP_KEY, "Move all steam and EU in your private network into your team's network and switch this machine to team mode");
        addLang(WirelessBindingTrait.BOUND_KEY, "Bound to %s");
        addLang(WirelessBindingTrait.UNBOUND_KEY, "Unbound");
        addLang(WirelessBindingTrait.ALREADY_BOUND_KEY, "Already bound to %s");
        addLang(WirelessBindingTrait.NOT_ALLOWED_KEY, "Only the bound player (%s) can do that");
        addLang(WirelessBindingTrait.MOVED_TO_TEAM_KEY, "Moved %s mB of steam and %s EU from your private network to %s");
        addLang(WirelessBindingTrait.COPIED_KEY, "Wireless binding copied to the data stick");
        addLang(WirelessBindingTrait.STICK_OTHER_PLAYER_KEY, "This data stick holds %s's binding");
        addLang(WirelessBindingTrait.STICK_NAME_KEY, "Wireless Binding (%s)");
        addLang(WirelessCovers.COVER_BINDING_TOOLTIP_KEY, "Bound to whoever attaches it; screwdriver or shift-right-click with an empty hand opens the settings");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
