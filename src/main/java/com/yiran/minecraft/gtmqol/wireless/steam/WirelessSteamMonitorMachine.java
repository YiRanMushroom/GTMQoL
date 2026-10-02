package com.yiran.minecraft.gtmqol.wireless.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;

/**
 * Shows the bound network's stored steam plus its input, output and net rates
 * (see {@link com.yiran.minecraft.gtmqol.wireless.IOStats}).
 */
public class WirelessSteamMonitorMachine extends MetaMachine implements IMuiMachine, IDataStickInteractable {

    public static final String INPUT_RATE_KEY = "gtmqol.wireless.monitor.input";
    public static final String OUTPUT_RATE_KEY = "gtmqol.wireless.monitor.output";
    public static final String NET_RATE_KEY = "gtmqol.wireless.monitor.net";

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    public WirelessSteamMonitorMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
    }

    private double inputRate() {
        NetworkId network = binding.getNetworkId();
        return network == null ? 0 : WirelessSteamSavedData.get().getStats(network).inputRate();
    }

    private double outputRate() {
        NetworkId network = binding.getNetworkId();
        return network == null ? 0 : WirelessSteamSavedData.get().getStats(network).outputRate();
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return binding.onDataStickUse(player, dataStick);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return binding.onDataStickShiftUse(player, dataStick);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var input = new DoubleSyncValue(this::inputRate);
        var output = new DoubleSyncValue(this::outputRate);
        syncManager.syncValue("input_rate", input);
        syncManager.syncValue("output_rate", output);

        mainWidget.child(WirelessSteamUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 40)
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(INPUT_RATE_KEY,
                        FormattingUtil.formatNumbers(input.getDoubleValue()))))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(OUTPUT_RATE_KEY,
                        FormattingUtil.formatNumbers(output.getDoubleValue()))))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> {
                    double net = input.getDoubleValue() - output.getDoubleValue();
                    return Component.translatable(NET_RATE_KEY,
                            (net > 0 ? "+" : "") + FormattingUtil.formatNumbers(net));
                }))
                        .horizontalCenter()));
    }
}
