package com.yiran.minecraft.gtmqol.common.wireless;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.common.ui.NumberText;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

import java.util.function.Supplier;

/**
 * Shows the bound network's stored amount plus its input, output and net rates (see {@link IOStats}). One subclass
 * per kind, which holds the lang keys.
 */
public abstract class WirelessMonitorMachine extends MetaMachine implements IMuiMachine, IDataStickInteractable {

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private final Supplier<? extends WirelessPool> pool;
    private final String storedKey;
    private final String inputRateKey;
    private final String outputRateKey;
    private final String netRateKey;

    /** The keys get the amount as {@code %s}. */
    protected WirelessMonitorMachine(BlockEntityCreationInfo info, Supplier<? extends WirelessPool> pool,
                                     String storedKey, String inputRateKey, String outputRateKey,
                                     String netRateKey) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.pool = pool;
        this.storedKey = storedKey;
        this.inputRateKey = inputRateKey;
        this.outputRateKey = outputRateKey;
        this.netRateKey = netRateKey;
    }

    private double inputRate() {
        NetworkId network = binding.getNetworkId();
        return network == null ? 0 : pool.get().getStats(network).inputRate();
    }

    private double outputRate() {
        NetworkId network = binding.getNetworkId();
        return network == null ? 0 : pool.get().getStats(network).outputRate();
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

        mainWidget.child(WirelessUI.create(syncManager, binding, guiData.getPlayer(), storedKey, pool)
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 40)
                .child(NumberText.line(format -> Component.translatable(inputRateKey,
                        format.apply(input.getDoubleValue())))
                        .horizontalCenter())
                .child(NumberText.line(format -> Component.translatable(outputRateKey,
                        format.apply(output.getDoubleValue())))
                        .horizontalCenter())
                .child(NumberText.line(format -> {
                    double net = input.getDoubleValue() - output.getDoubleValue();
                    return Component.translatable(netRateKey, (net > 0 ? "+" : "") + format.apply(net));
                })
                        .horizontalCenter()));
    }
}
