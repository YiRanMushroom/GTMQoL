package com.yiran.minecraft.gtmqol.integration.ae2.patternbuffer;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Pattern batching on GTCEu's pattern buffer, used by {@code MEPatternBufferPatternBatchingMixin}. The work is in
 * {@link com.yiran.minecraft.gtmqol.integration.ae2.crafting.BatchPlan}. Lang keys are registered in {@link AE2Machines}.
 */
public final class PatternBatching {

    public static final String TITLE_KEY = "gtmqol.gui.pattern_buffer.pattern_batching";
    public static final String TOGGLE_KEY = "gtmqol.gui.pattern_buffer.pattern_batching.toggle";
    public static final String LIMIT_KEY = "gtmqol.gui.pattern_buffer.pattern_batching.limit";

    private PatternBatching() {}

    /**
     * The crafts of a pattern in one batch.
     *
     * @param limit the most of any input in a batch, in items or buckets; 0 = no limit
     */
    public static long limit(IPatternDetails pattern, int limit) {
        if (limit <= 0) return Long.MAX_VALUE;
        long crafts = Long.MAX_VALUE;
        for (var input : pattern.getInputs()) {
            GenericStack first = input.getPossibleInputs()[0];
            long perCraft = first.amount() * input.getMultiplier();
            crafts = Math.min(crafts, Math.max(1, limit * (long) first.what().getAmountPerUnit() / perCraft));
        }
        return crafts;
    }

    /** Adds a button to the left configurators that opens the pattern batching settings. */
    public static MachineUIPanelBuilder addConfigurator(MachineUIPanelBuilder builder, PanelSyncManager syncManager,
                                                        BooleanSupplier enabled, BooleanConsumer setEnabled,
                                                        IntSupplier limit, IntConsumer setLimit) {
        IPanelHandler panelHandler = syncManager.syncedPanel("gtmqol_pattern_batching", true,
                (syncManager1, handler) -> popup("gtmqol_pattern_batching_panel", 140, 70)
                        .child(Flow.col()
                                .coverChildren()
                                .child(Text.lang(TITLE_KEY).asWidget())
                                // On/off shows in the button background and its tooltip (TOGGLE_KEY.enabled/.disabled).
                                .child(GTMuiWidgets.createToggleButton(enabled,
                                        setEnabled, GTGuiTextures.BUTTON_BATCH[0],
                                        GTGuiTextures.BUTTON_BATCH[1], TOGGLE_KEY).marginTop(4))
                                .child(Text.lang(LIMIT_KEY).asWidget().marginTop(4))
                                .child(new TextFieldWidget()
                                        .size(90, 14)
                                        .setNumbers(0, Integer.MAX_VALUE)
                                        .tooltipBuilder(tooltip -> tooltip.addLine(
                                                Text.lang(LIMIT_KEY + ".tooltip")))
                                        .value(SyncHandlers.intNumber(limit, setLimit).allowC2S()))
                                .margin(5)));

        var left = builder.leftConfigurators();
        return builder.leftConfigurators(left.andThen(f -> f.child(new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, b) -> {
                    if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                        panelHandler.togglePanel();
                        return true;
                    }
                    return false;
                })
                .overlay(Text.str("×2").asIcon().size(16))
                .tooltip(new RichTooltip().addLine(Text.lang(TITLE_KEY))
                        .addLine(Text.lang(TITLE_KEY + ".tooltip"))))));
    }
}
