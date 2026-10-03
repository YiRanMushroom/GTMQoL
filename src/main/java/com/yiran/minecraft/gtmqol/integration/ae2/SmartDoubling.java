package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.pattern.AEProcessingPattern;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.extendedae_plus.api.crafting.ScaledProcessingPattern;
import com.extendedae_plus.api.smartDoubling.ISmartDoublingAwarePattern;
import com.extendedae_plus.api.smartDoubling.ISmartDoublingHolder;
import com.extendedae_plus.util.smartDoubling.PatternScaler;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

/**
 * ExtendedAE Plus smart doubling on GTCEu's pattern buffer, used by {@code mixin.eap.MEPatternBufferSmartDoublingMixin}.
 * Only loaded when ExtendedAE Plus is installed. Lang keys are registered in {@link AE2Machines}.
 */
public final class SmartDoubling {

    public static final String TITLE_KEY = "gtmqol.gui.pattern_buffer.smart_doubling";
    public static final String TOGGLE_KEY = "gtmqol.gui.pattern_buffer.smart_doubling.toggle";
    public static final String LIMIT_KEY = "gtmqol.gui.pattern_buffer.smart_doubling.limit";

    private SmartDoubling() {}

    /** Smart doubling settings live on each pattern, EAP reads them when it plans a craft. */
    public static void apply(@Nullable IPatternDetails[] patterns, boolean enabled, int limit) {
        for (IPatternDetails pattern : patterns) {
            if (pattern instanceof AEProcessingPattern processing &&
                    (Object) processing instanceof ISmartDoublingAwarePattern aware) {
                aware.eap$setAllowScaling(enabled);
                aware.eap$setMultiplierLimit(PatternScaler.getComputedMul(processing, limit));
            }
        }
    }

    /** AE2 pushes the scaled wrapper; the buffer only knows the patterns in its slots. */
    public static IPatternDetails unwrap(IPatternDetails pattern) {
        return pattern instanceof ScaledProcessingPattern scaled ? scaled.getOriginal() : pattern;
    }

    /** Adds a button to the left configurators that opens the smart doubling settings. */
    public static MachineUIPanelBuilder addConfigurator(MachineUIPanelBuilder builder, PanelSyncManager syncManager,
                                                        ISmartDoublingHolder holder) {
        IPanelHandler panelHandler = syncManager.syncedPanel("gtmqol_smart_doubling", true,
                (syncManager1, handler) -> PopupPanel.createPopupPanel("gtmqol_smart_doubling_panel", 140, 70)
                        .child(Flow.col()
                                .coverChildren()
                                .child(Text.lang(TITLE_KEY).asWidget())
                                // On/off shows in the button background and its tooltip (TOGGLE_KEY.enabled/.disabled).
                                .child(GTMuiWidgets.createToggleButton(holder::eap$getSmartDoubling,
                                        holder::eap$setSmartDoubling, GTGuiTextures.BUTTON_BATCH[0],
                                        GTGuiTextures.BUTTON_BATCH[1], TOGGLE_KEY).marginTop(4))
                                .child(Text.lang(LIMIT_KEY).asWidget().marginTop(4))
                                .child(new TextFieldWidget()
                                        .size(90, 14)
                                        .setNumbers(0, Integer.MAX_VALUE)
                                        .value(SyncHandlers.intNumber(holder::eap$getProviderSmartDoublingLimit,
                                                holder::eap$setProviderSmartDoublingLimit).allowC2S()))
                                .margin(5)));

        var left = builder.leftConfigurators();
        return builder.leftConfigurators(left.andThen(f -> f.child(new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, b) -> {
                    if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                        panelHandler.openPanel();
                        return true;
                    }
                    return false;
                })
                .overlay(Text.str("×2").asIcon().size(16))
                .tooltip(new RichTooltip().addLine(Text.lang(TITLE_KEY))))));
    }
}
