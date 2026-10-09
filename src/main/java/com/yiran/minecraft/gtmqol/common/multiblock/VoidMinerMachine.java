package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.common.multiblock.VoidMinerOres.OreChance;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergySavedData;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyUI;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Mines the averaged ore veins of its dimension, paid from the bound wireless EU network. Each operation of a cycle
 * draws one ore and yields {@code multiplier} stacks of it. A cycle pays {@link #EU_PER_STACK} per stack up front
 * and takes {@link #CYCLE_TICKS}; see {@link CyclicMultiblockMachine} for the rest.
 */
public class VoidMinerMachine extends CyclicMultiblockMachine implements IDataStickInteractable {

    public static final long EU_PER_STACK = 1_000_000;
    public static final int CYCLE_TICKS = 15 * 20;
    public static final int MAX_OPERATIONS = 16;
    public static final int MAX_MULTIPLIER = 16;

    public static final String KEY = "gtmqol.void_miner.";

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;
    @SaveField
    private int operations = 1;
    @SaveField
    private int multiplier = 1;
    /** Settings paid for in the current cycle; changing them mid-cycle only affects the next one. */
    @SaveField
    private int cycleOperations;
    @SaveField
    private int cycleMultiplier;

    private @Nullable List<OreChance> ores;
    private double[] cumulativeChances = new double[0];

    public VoidMinerMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
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
    protected String langKey() {
        return KEY;
    }

    @Override
    protected int cycleTicks() {
        return CYCLE_TICKS;
    }

    // ---------- logic (server) ----------

    /** Computed on first use; datapack reloads need the chunk reloaded. */
    private List<OreChance> ores() {
        if (ores == null) {
            ores = VoidMinerOres.compute((ServerLevel) getLevel());
            cumulativeChances = new double[ores.size()];
            double sum = 0;
            for (int i = 0; i < ores.size(); i++) {
                sum += ores.get(i).chance();
                cumulativeChances[i] = sum;
            }
        }
        return ores;
    }

    private long costPerCycle() {
        return operations * multiplier * EU_PER_STACK;
    }

    @Override
    protected @Nullable String startCycle() {
        if (ores().isEmpty()) return KEY + "status.no_ores";
        NetworkId network = binding.getNetworkId();
        if (network == null) return KEY + "status.no_network";
        // one unit of the whole cost: all or nothing
        if (WirelessEnergySavedData.get().extract(network, 1, costPerCycle()) == 0) {
            return KEY + "status.not_enough_eu";
        }
        cycleOperations = operations;
        cycleMultiplier = multiplier;
        return null;
    }

    /**
     * Each operation draws one ore from the distribution and yields {@code multiplier} stacks of it, so a cycle
     * gives at most {@code operations} different ores.
     */
    @Override
    protected void finishCycle() {
        List<OreChance> ores = ores();
        if (!ores.isEmpty()) {
            long[] counts = new long[ores.size()];
            RandomSource random = getLevel().getRandom();
            for (int i = 0; i < cycleOperations; i++) {
                int index = Arrays.binarySearch(cumulativeChances, random.nextDouble());
                if (index < 0) index = -index - 1;
                counts[Math.min(index, counts.length - 1)] += cycleMultiplier * 64L;
            }
            for (int i = 0; i < counts.length; i++) {
                output.add(new ItemStack(ores.get(i).item()), counts[i]);
            }
        }
        cycleOperations = 0;
        cycleMultiplier = 0;
    }

    private void setOperations(int operations) {
        this.operations = Mth.clamp(operations, 1, MAX_OPERATIONS);
        markAsChanged();
    }

    private void setMultiplier(int multiplier) {
        this.multiplier = Mth.clamp(multiplier, 1, MAX_MULTIPLIER);
        markAsChanged();
    }

    // ---------- UI ----------

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var operationsValue = new IntSyncValue(() -> operations, this::setOperations).allowC2S();
        var multiplierValue = new IntSyncValue(() -> multiplier, this::setMultiplier).allowC2S();
        var oresValue = new GenericListSyncHandler.Builder<OreChance>()
                .getter(() -> isRemote() ? List.of() : ores())
                .serializer(OreChance::write)
                .deserializer(OreChance::read)
                .build();
        syncManager.syncValue("operations", operationsValue);
        syncManager.syncValue("multiplier", multiplierValue);
        syncManager.syncValue("ores", oresValue);

        var preview = syncManager.syncedPanel("void_miner_preview", false,
                (manager, handler) -> previewPanel(oresValue.getValue()));
        var config = syncManager.syncedPanel("void_miner_config", false,
                (manager, handler) -> configPanel(operationsValue, multiplierValue));

        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 64)
                .child(statusLine(syncManager).horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(KEY + "settings",
                        operationsValue.getIntValue(), multiplierValue.getIntValue(),
                        FormattingUtil.formatNumbers(
                                operationsValue.getIntValue() * multiplierValue.getIntValue() * EU_PER_STACK))))
                        .horizontalCenter())
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "preview"))
                                .onMousePressed((context, button) -> {
                                    togglePanel(preview);
                                    return true;
                                }))
                        .child(storedButton(syncManager, MAX_OPERATIONS))
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "config"))
                                .onMousePressed((context, button) -> {
                                    togglePanel(config);
                                    return true;
                                }))
                        .child(powerButton(syncManager))));
    }

    private static ModularPanel<?> previewPanel(List<OreChance> ores) {
        var list = new ListWidget<>()
                .widthRel(1f)
                .height(140)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .children(ores, ore -> Flow.row()
                        .height(18)
                        .childPadding(4)
                        .child(new ItemDrawable(ore.item()).asWidget().size(16))
                        .child(new TextWidget<>(Text.of(Component.literal("%.2f%%  ".formatted(ore.chance() * 100))
                                .append(ore.item().getDescription())))
                                .verticalCenter()));
        // rebuilt on every open: the ore list may not have been synced yet the first time
        return popup("void_miner_preview", 200, 170)
                .deleteCachedPanel(true)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(Text.lang(ores.isEmpty() ? KEY + "preview.empty" : KEY + "preview.title")
                                .asWidget())
                        .child(list));
    }

    private static ModularPanel<?> configPanel(IntSyncValue operations, IntSyncValue multiplier) {
        return popup("void_miner_config", 160, 76)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(Text.lang(KEY + "config.title").asWidget())
                        .child(counterRow(operations, MAX_OPERATIONS, 4, KEY + "config.operations"))
                        .child(counterRow(multiplier, MAX_MULTIPLIER, 4, KEY + "config.multiplier")));
    }
}
