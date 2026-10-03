package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.common.multiblock.VoidMinerOres.OreChance;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergySavedData;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyUI;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Mines the averaged ore veins of its dimension, paid from the bound wireless EU network. Each operation of a cycle
 * draws one ore and yields {@code multiplier} stacks of it. A cycle pays {@link #EU_PER_STACK} per stack up front,
 * takes {@link #CYCLE_TICKS}, then everything goes to the output buses;
 * whatever doesn't fit is retried every {@link #RETRY_TICKS} before the next cycle starts. Stopping lets the
 * current cycle finish.
 */
public class VoidMinerMachine extends MultiblockControllerMachine implements IMuiMachine, IDataStickInteractable {

    public static final long EU_PER_STACK = 1_000_000;
    public static final int CYCLE_TICKS = 15 * 20;
    public static final int RETRY_TICKS = 5 * 20;
    public static final int MAX_OPERATIONS = 16;
    public static final int MAX_MULTIPLIER = 16;

    public static final String KEY = "gtmqol.void_miner.";

    public enum State {
        IDLE, MINING, OUTPUTTING
    }

    /** One entry of {@link #pending}, for the UI. */
    public record Stored(Item item, long count) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Stored> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), Stored::item,
                ByteBufCodecs.VAR_LONG, Stored::count,
                Stored::new);
    }

    /**
     * What the UI shows; each has a lang key {@code gtmqol.void_miner.status.<name>}.
     */
    public enum Status {
        STOPPED, STARTING, MINING, OUTPUT_FULL, NO_NETWORK, NOT_ENOUGH_EU, NO_ORES, NOT_FORMED;

        public String langKey() {
            return KEY + "status." + name().toLowerCase();
        }
    }

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;
    @SaveField
    private boolean enabled;
    @SaveField
    private int operations = 1;
    @SaveField
    private int multiplier = 1;
    @SaveField
    private State state = State.IDLE;
    /** Ticks into the cycle while mining, ticks since the last output attempt while outputting. */
    @SaveField
    private int progress;
    /** Settings paid for in the current cycle; changing them mid-cycle only affects the next one. */
    @SaveField
    private int cycleOperations;
    @SaveField
    private int cycleMultiplier;
    /** Mined but not yet output: item id to count. */
    @SaveField
    private CompoundTag pending = new CompoundTag();

    /** Why the last start failed; not saved. */
    private @Nullable Status startProblem;
    private @Nullable List<OreChance> ores;
    private double[] cumulativeChances = new double[0];
    private @Nullable TickableSubscription tickSubs;

    public VoidMinerMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && tickSubs == null) {
            tickSubs = subscribeServerTick(this::tick);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return binding.onDataStickUse(player, dataStick);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return binding.onDataStickShiftUse(player, dataStick);
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

    private void tick() {
        if (!isFormed()) return;
        switch (state) {
            case IDLE -> {
                if (enabled && getOffsetTimer() % 20 == 0) tryStart();
            }
            case MINING -> {
                if (++progress >= CYCLE_TICKS) {
                    mine(cycleOperations, cycleMultiplier);
                    cycleOperations = 0;
                    cycleMultiplier = 0;
                    progress = 0;
                    setState(State.OUTPUTTING);
                    tryFinish();
                }
                markAsChanged();
            }
            case OUTPUTTING -> {
                if (++progress >= RETRY_TICKS) {
                    progress = 0;
                    tryFinish();
                }
            }
        }
    }

    private void tryStart() {
        if (ores().isEmpty()) {
            startProblem = Status.NO_ORES;
            return;
        }
        NetworkId network = binding.getNetworkId();
        if (network == null) {
            startProblem = Status.NO_NETWORK;
            return;
        }
        // one unit of the whole cost: all or nothing
        if (WirelessEnergySavedData.get().extract(network, 1, costPerCycle()) == 0) {
            startProblem = Status.NOT_ENOUGH_EU;
            return;
        }
        startProblem = null;
        cycleOperations = operations;
        cycleMultiplier = multiplier;
        progress = 0;
        setState(State.MINING);
    }

    /**
     * Each operation draws one ore from the distribution and yields {@code multiplier} stacks of it, so a cycle
     * gives at most {@code operations} different ores.
     */
    private void mine(int operations, int multiplier) {
        List<OreChance> ores = ores();
        if (ores.isEmpty()) return;
        long[] counts = new long[ores.size()];
        RandomSource random = getLevel().getRandom();
        for (int i = 0; i < operations; i++) {
            int index = Arrays.binarySearch(cumulativeChances, random.nextDouble());
            if (index < 0) index = -index - 1;
            counts[Math.min(index, counts.length - 1)] += multiplier * 64L;
        }
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 0) continue;
            String id = BuiltInRegistries.ITEM.getKey(ores.get(i).item()).toString();
            pending.putLong(id, pending.getLong(id) + counts[i]);
        }
    }

    private void tryFinish() {
        if (output()) {
            setState(State.IDLE);
            if (enabled) tryStart();
        }
        markAsChanged();
    }

    /**
     * Moves as much of {@link #pending} into the output buses (ME output buses included) as fits.
     *
     * @return whether everything was output
     */
    private boolean output() {
        List<NotifiableItemStackHandler> handlers = getTraitsFromParts(NotifiableItemStackHandler.class).stream()
                .filter(handler -> handler.getHandlerIO() == IO.OUT)
                .toList();
        for (String id : new ArrayList<>(pending.getAllKeys())) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            long left = item == Items.AIR ? 0 : pending.getLong(id);
            int maxStack = new ItemStack(item).getMaxStackSize();
            for (NotifiableItemStackHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
                    while (left > 0) {
                        int amount = (int) Math.min(left, maxStack);
                        int inserted = amount - handler.insertItemInternal(slot, new ItemStack(item, amount), false)
                                .getCount();
                        left -= inserted;
                        if (inserted < amount) break;
                    }
                }
            }
            if (left > 0) {
                pending.putLong(id, left);
            } else {
                pending.remove(id);
            }
        }
        return pending.isEmpty();
    }

    /** {@link #pending} as a list, largest first. */
    private List<Stored> storedItems() {
        List<Stored> stored = new ArrayList<>();
        for (String id : pending.getAllKeys()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            if (item != Items.AIR) stored.add(new Stored(item, pending.getLong(id)));
        }
        stored.sort(Comparator.comparingLong(Stored::count).reversed());
        return stored;
    }

    private long pendingCount() {
        long total = 0;
        for (String id : pending.getAllKeys()) total += pending.getLong(id);
        return total;
    }

    private void setState(State state) {
        this.state = state;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                state == State.MINING ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        markAsChanged();
    }

    private void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) startProblem = null;
        markAsChanged();
    }

    private void setOperations(int operations) {
        this.operations = Math.clamp(operations, 1, MAX_OPERATIONS);
        markAsChanged();
    }

    private void setMultiplier(int multiplier) {
        this.multiplier = Math.clamp(multiplier, 1, MAX_MULTIPLIER);
        markAsChanged();
    }

    private Status status() {
        if (!isFormed()) return Status.NOT_FORMED;
        return switch (state) {
            case MINING -> Status.MINING;
            case OUTPUTTING -> Status.OUTPUT_FULL;
            case IDLE -> !enabled ? Status.STOPPED : startProblem != null ? startProblem : Status.STARTING;
        };
    }

    // ---------- UI ----------

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var status = new EnumSyncValue<>(Status.class, this::status);
        var progressValue = new IntSyncValue(() -> progress);
        var pendingValue = new LongSyncValue(this::pendingCount);
        var operationsValue = new IntSyncValue(() -> operations, this::setOperations).allowC2S();
        var multiplierValue = new IntSyncValue(() -> multiplier, this::setMultiplier).allowC2S();
        var enabledValue = new BooleanSyncValue(() -> enabled, this::setEnabled).allowC2S();
        var oresValue = new GenericListSyncHandler.Builder<RegistryFriendlyByteBuf, OreChance>()
                .getter(() -> isRemote() ? List.of() : ores())
                .serializer(OreChance.STREAM_CODEC)
                .deserializer(OreChance.STREAM_CODEC)
                .build();
        var storedValue = new GenericListSyncHandler.Builder<RegistryFriendlyByteBuf, Stored>()
                .getter(() -> isRemote() ? List.of() : storedItems())
                .serializer(Stored.STREAM_CODEC)
                .deserializer(Stored.STREAM_CODEC)
                .build();
        syncManager.syncValue("status", status);
        syncManager.syncValue("progress", progressValue);
        syncManager.syncValue("pending", pendingValue);
        syncManager.syncValue("operations", operationsValue);
        syncManager.syncValue("multiplier", multiplierValue);
        syncManager.syncValue("enabled", enabledValue);
        syncManager.syncValue("ores", oresValue);
        syncManager.syncValue("stored", storedValue);

        var preview = syncManager.syncedPanel("void_miner_preview", false,
                (manager, handler) -> previewPanel(oresValue.getValue()));
        var stored = syncManager.syncedPanel("void_miner_stored", false,
                (manager, handler) -> storedPanel(storedValue));
        var config = syncManager.syncedPanel("void_miner_config", false,
                (manager, handler) -> configPanel(operationsValue, multiplierValue));

        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 64)
                .child(new TextWidget<>(Text.dynamic(() -> switch (status.getValue()) {
                    case MINING -> Component.translatable(status.getValue().langKey(),
                            seconds(progressValue.getIntValue()), seconds(CYCLE_TICKS));
                    case OUTPUT_FULL -> Component.translatable(status.getValue().langKey(),
                            FormattingUtil.formatNumbers(pendingValue.getLongValue()),
                            seconds(RETRY_TICKS - progressValue.getIntValue()));
                    default -> Component.translatable(status.getValue().langKey());
                }))
                        .horizontalCenter())
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
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "stored"))
                                .onMousePressed((context, button) -> {
                                    togglePanel(stored);
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "config"))
                                .onMousePressed((context, button) -> {
                                    togglePanel(config);
                                    return true;
                                }))
                        .child(new ToggleButton()
                                .value(enabledValue)
                                .overlay(false, GTGuiTextures.BUTTON_POWER[0])
                                .overlay(true, GTGuiTextures.BUTTON_POWER[1])
                                .background(GuiTextures.MC_BUTTON)
                                .background(true, GuiTextures.MC_BUTTON_PRESSED)
                                .tooltipDynamic(t -> t.addLine(Component.translatable(
                                        enabledValue.getBoolValue() ? KEY + "running" : KEY + "stopped")))
                                .tooltipAutoUpdate(true))));
    }

    private static String seconds(int ticks) {
        return "%.2f".formatted(ticks / 20.0);
    }

    private static void togglePanel(IPanelHandler panel) {
        if (panel.isPanelOpen()) {
            panel.closePanel();
        } else {
            panel.openPanel();
        }
    }

    /** GTCEu's popup (background, close button top right), movable, the main UI stays usable. */
    private static PopupPanel popup(String name, int width, int height) {
        return PopupPanel.createPopupPanel(name, width, height)
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true);
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

    /**
     * Live view of {@link #pending}. A cycle yields at most {@link #MAX_OPERATIONS} ores and the next one waits
     * until everything is out, so that many rows are enough; rows past the end are hidden.
     */
    private static ModularPanel<?> storedPanel(GenericListSyncHandler<RegistryFriendlyByteBuf, Stored> stored) {
        var list = new ListWidget<>()
                .widthRel(1f)
                .height(140)
                .crossAxisAlignment(Alignment.CrossAxis.START);
        for (int i = 0; i < MAX_OPERATIONS; i++) {
            int index = i;
            Supplier<Stored> entry = () -> index < stored.getValue().size() ?
                    stored.getValue().get(index) : null;
            list.child(Flow.row()
                    .height(18)
                    .childPadding(4)
                    .setEnabledIf(row -> entry.get() != null)
                    .child(new DynamicDrawable(() -> {
                        Stored s = entry.get();
                        return s == null ? IDrawable.EMPTY : new ItemDrawable(s.item());
                    }).asWidget().size(16))
                    .child(new TextWidget<>(Text.dynamic(() -> {
                        Stored s = entry.get();
                        return s == null ? Component.empty() :
                                Component.literal(FormattingUtil.formatNumbers(s.count()) + "  ")
                                        .append(s.item().getDescription());
                    }))
                            .verticalCenter()));
        }
        return popup("void_miner_stored", 200, 170)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                                stored.getValue().isEmpty() ? KEY + "stored.empty" : KEY + "stored.title"))))
                        .child(list));
    }

    private static ModularPanel<?> configPanel(IntSyncValue operations, IntSyncValue multiplier) {
        return popup("void_miner_config", 160, 76)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(Text.lang(KEY + "config.title").asWidget())
                        .child(counterRow(operations, MAX_OPERATIONS, KEY + "config.operations"))
                        .child(counterRow(multiplier, MAX_MULTIPLIER, KEY + "config.multiplier")));
    }

    /** {@code [-] label [+]}; Shift steps by 4. */
    private static Flow counterRow(IntSyncValue value, int max, String langKey) {
        return Flow.row()
                .height(18)
                .childPadding(4)
                .child(stepButton(value, max, false))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(langKey, value.getIntValue())))
                        .width(100)
                        .verticalCenter())
                .child(stepButton(value, max, true));
    }

    private static ButtonWidget<?> stepButton(IntSyncValue value, int max, boolean up) {
        return new ButtonWidget<>()
                .size(18)
                .overlay(Text.str(up ? "+" : "-"))
                .onMousePressed((context, button) -> {
                    int step = MouseData.create(button).shift() ? 4 : 1;
                    int next = Math.clamp(value.getIntValue() + (up ? step : -step), 1, max);
                    value.setIntValue(next, true, true);
                    return true;
                });
    }
}
