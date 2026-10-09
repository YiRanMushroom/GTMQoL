package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.common.multiblock.PendingOutputTrait.Stored;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A multiblock that works in fixed-length cycles without GTCEu's recipe logic, so overflowing outputs are kept
 * instead of voided. A cycle starts ({@link #startCycle()}), runs {@link #cycleTicks()} ticks
 * ({@link #onCycleTick()}), then produces into {@link #output} ({@link #finishCycle()}); whatever doesn't fit in the
 * output buses is retried every {@link #RETRY_TICKS}, and the next cycle only starts once everything is out.
 * A failed start is retried every {@link #RETRY_TICKS} too. Stopping lets the current cycle finish.
 *
 * <p>Lang keys are under {@link #langKey()}: {@code status.<status>} (see {@link Status}), {@code running},
 * {@code stopped}, {@code stored}, {@code stored.title}, {@code stored.empty}.</p>
 */
public abstract class CyclicMultiblockMachine extends MultiblockControllerMachine implements IMuiMachine {

    public static final int RETRY_TICKS = 5;

    public enum State {
        IDLE, WORKING, OUTPUTTING
    }

    /** What the UI shows. {@link #PROBLEM} shows the lang key returned by {@link #startCycle()} instead. */
    public enum Status {
        STOPPED, STARTING, WORKING, OUTPUT_FULL, PROBLEM, NOT_FORMED
    }

    @SaveField
    public final PendingOutputTrait output;
    @SaveField
    private boolean enabled;
    @SaveField
    private State state = State.IDLE;
    /** Ticks into the cycle while working, ticks since the last output attempt while outputting. */
    @SaveField
    protected int progress;

    /** Lang key of why the last start failed; not saved. */
    private @Nullable String startProblem;
    private @Nullable TickableSubscription tickSubs;

    protected CyclicMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
        this.output = attachTrait(new PendingOutputTrait());
    }

    /** Prefix of this machine's lang keys, ending with a dot. */
    protected abstract String langKey();

    protected abstract int cycleTicks();

    /**
     * Checks and pays for a cycle.
     *
     * @return {@code null} if the cycle starts, otherwise the lang key of the reason it can't
     */
    protected abstract @Nullable String startCycle();

    /** Called every tick of a cycle, before {@link #progress} is advanced. */
    protected void onCycleTick() {}

    /** Called when a cycle ends; add the products to {@link #output}. */
    protected abstract void finishCycle();

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

    // ---------- logic (server) ----------

    private void tick() {
        if (!isFormed()) return;
        switch (state) {
            case IDLE -> {
                if (enabled && getOffsetTimer() % RETRY_TICKS == 0) tryStart();
            }
            case WORKING -> {
                onCycleTick();
                if (++progress >= cycleTicks()) {
                    progress = 0;
                    finishCycle();
                    setState(State.OUTPUTTING);
                    tryOutput();
                }
                markAsChanged();
            }
            case OUTPUTTING -> {
                if (++progress >= RETRY_TICKS) {
                    progress = 0;
                    tryOutput();
                }
            }
        }
    }

    private void tryStart() {
        startProblem = startCycle();
        if (startProblem != null) return;
        progress = 0;
        setState(State.WORKING);
    }

    private void tryOutput() {
        if (output.output()) {
            setState(State.IDLE);
            if (enabled) tryStart();
        }
    }

    private void setState(State state) {
        this.state = state;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                state == State.WORKING ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        markAsChanged();
    }

    private void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) startProblem = null;
        markAsChanged();
    }

    private Status status() {
        if (!isFormed()) return Status.NOT_FORMED;
        return switch (state) {
            case WORKING -> Status.WORKING;
            case OUTPUTTING -> Status.OUTPUT_FULL;
            case IDLE -> !enabled ? Status.STOPPED : startProblem != null ? Status.PROBLEM : Status.STARTING;
        };
    }

    // ---------- UI ----------

    /**
     * {@code status.working} gets the progress and the cycle length in seconds, {@code status.output_full} the
     * number of items still waiting.
     */
    protected TextWidget<?> statusLine(PanelSyncManager syncManager) {
        var status = new EnumSyncValue<>(Status.class, this::status);
        var problem = new StringSyncValue(() -> startProblem == null ? "" : startProblem);
        var progressValue = new IntSyncValue(() -> progress);
        var pending = new LongSyncValue(output::total);
        syncManager.syncValue("status", status);
        syncManager.syncValue("problem", problem);
        syncManager.syncValue("progress", progressValue);
        syncManager.syncValue("pending", pending);
        return new TextWidget<>(Text.dynamic(() -> switch (status.getValue()) {
            case WORKING -> Component.translatable(langKey() + "status.working",
                    seconds(progressValue.getIntValue()), seconds(cycleTicks()));
            case OUTPUT_FULL -> Component.translatable(langKey() + "status.output_full",
                    FormattingUtil.formatNumbers(pending.getLongValue()));
            case PROBLEM -> Component.translatable(problem.getStringValue());
            default -> Component.translatable(langKey() + "status." + status.getValue().name().toLowerCase());
        }));
    }

    /** Opens a live view of {@link #output}; {@code rows} is the most distinct items a cycle can produce. */
    protected ButtonWidget<?> storedButton(PanelSyncManager syncManager, int rows) {
        var stored = new GenericListSyncHandler.Builder<Stored>()
                .getter(() -> isRemote() ? List.of() : output.storedItems())
                .serializer(Stored::write)
                .deserializer(Stored::read)
                .build();
        syncManager.syncValue("stored", stored);
        var panel = syncManager.syncedPanel(langKey() + "stored", false,
                (manager, handler) -> storedPanel(stored, rows));
        return new ButtonWidget<>()
                .size(44, 18)
                .overlay(Text.lang(langKey() + "stored"))
                .onMousePressed((context, button) -> {
                    togglePanel(panel);
                    return true;
                });
    }

    protected ToggleButton powerButton(PanelSyncManager syncManager) {
        var enabledValue = new BooleanSyncValue(() -> enabled, this::setEnabled).allowC2S();
        syncManager.syncValue("enabled", enabledValue);
        return new ToggleButton()
                .value(enabledValue)
                .overlay(false, GTGuiTextures.BUTTON_POWER[0])
                .overlay(true, GTGuiTextures.BUTTON_POWER[1])
                .background(GuiTextures.MC_BUTTON)
                .background(true, GuiTextures.MC_BUTTON_PRESSED)
                .tooltipDynamic(t -> t.addLine(Component.translatable(
                        enabledValue.getBoolValue() ? langKey() + "running" : langKey() + "stopped")))
                .tooltipAutoUpdate(true);
    }

    /**
     * Rows past the end are hidden; the next cycle waits until everything is out, so {@code rows} is enough.
     */
    private ModularPanel<?> storedPanel(GenericListSyncHandler<Stored> stored, int rows) {
        var list = new ListWidget<>()
                .widthRel(1f)
                .height(140)
                .crossAxisAlignment(Alignment.CrossAxis.START);
        for (int i = 0; i < rows; i++) {
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
                                        .append(s.item().getHoverName());
                    }))
                            .verticalCenter()));
        }
        return popup(langKey() + "stored", 200, 170)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                                stored.getValue().isEmpty() ? langKey() + "stored.empty" :
                                        langKey() + "stored.title"))))
                        .child(list));
    }

    protected static String seconds(int ticks) {
        return "%.2f".formatted(ticks / 20.0);
    }

    protected static void togglePanel(IPanelHandler panel) {
        if (panel.isPanelOpen()) {
            panel.closePanel();
        } else {
            panel.openPanel();
        }
    }

    /** GTCEu's popup (background, close button top right), movable, the main UI stays usable. */
    protected static PopupPanel popup(String name, int width, int height) {
        return PopupPanel.createPopupPanel(name, width, height)
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true);
    }

    /** {@code [-] label [+]}; Shift steps by {@code shiftStep}. */
    protected static Flow counterRow(IntSyncValue value, int max, int shiftStep, String langKey) {
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
                    int step = MouseData.create(button).shift() ? shiftStep : 1;
                    int next = Mth.clamp(value.getIntValue() + (up ? step : -step), 1, max);
                    value.setIntValue(next, true, true);
                    return true;
                });
    }
}
