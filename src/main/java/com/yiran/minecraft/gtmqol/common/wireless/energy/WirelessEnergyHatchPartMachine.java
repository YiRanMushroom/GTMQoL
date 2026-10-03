package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;

import java.util.List;

/**
 * Energy hatch backed by the bound player's wireless network, at its own tier with adjustable amperage.
 * {@link IO#IN} powers multiblocks; with overclock on it runs one tier higher but every EU costs 4 from the
 * network (16× the energy for 4× the voltage). {@link IO#OUT} takes generator output.
 */
public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine
                                            implements IMuiMachine, IDataStickInteractable {

    public static final String OVERCLOCK_ON_KEY = "gtmqol.wireless.overclock.enabled";
    public static final String OVERCLOCK_OFF_KEY = "gtmqol.wireless.overclock.disabled";
    public static final String SAVE_KEY = "gtmqol.wireless.save";
    public static final String SAVE_TOOLTIP_KEY = "gtmqol.wireless.save.tooltip";

    public static final int DEFAULT_AMPERAGE = 4;
    public static final int MAX_AMPERAGE = 1 << 24;
    /**
     * Overclock: 4× the voltage for 16× the energy, so each EU drawn costs 4.
     */
    public static final int OVERCLOCK_COST = 4;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    @SaveField
    public final WirelessEnergyContainer energyContainer;

    @SaveField
    @SyncToClient
    protected int amperage = DEFAULT_AMPERAGE;

    @SaveField
    @SyncToClient
    protected boolean overclocked;

    public WirelessEnergyHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.energyContainer = attachTrait(new WirelessEnergyContainer(io, binding::getNetworkId,
                this::getVoltageTier, this::getAmperage, () -> isOverclocked() ? OVERCLOCK_COST : 1));
    }

    public boolean canOverclock() {
        return io == IO.IN && tier < GTValues.MAX;
    }

    public boolean isOverclocked() {
        return overclocked && canOverclock();
    }

    public int getVoltageTier() {
        return isOverclocked() ? tier + 1 : tier;
    }

    public int getAmperage() {
        return amperage;
    }

    public static int clampAmperage(int amperage) {
        return Math.max(1, Math.min(amperage, MAX_AMPERAGE));
    }

    /**
     * Server only. Multiblocks only read voltage and amperage when they form, so on a change every formed
     * controller of this hatch re-forms.
     */
    public void applySettings(int amperage, boolean overclocked) {
        amperage = clampAmperage(amperage);
        if (amperage == this.amperage && overclocked == this.overclocked) return;
        this.amperage = amperage;
        this.overclocked = overclocked;
        syncDataHolder.markClientSyncFieldDirty("amperage");
        syncDataHolder.markClientSyncFieldDirty("overclocked");
        reformControllers();
    }

    /**
     * Same as GTCEu does when a block inside a structure changes ({@code PatternState.onBlockStateChanged}):
     * the pattern check hits the cache, and {@code formStructure} collects the energy containers and tier again.
     */
    private void reformControllers() {
        // Copied: forming can add or remove this part from controllers.
        for (MultiblockControllerMachine controller : List.copyOf(getControllers())) {
            if (!controller.isFormed()) continue;
            for (String name : controller.getStructureNames()) {
                if (!controller.checkStructurePattern(name).hasErrors()) {
                    controller.formStructure(name);
                }
            }
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

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        // Edits stay in this UI until saved; closing the UI saves too.
        var pending = new PendingSettings(getAmperage(), isOverclocked());
        Runnable save = () -> applySettings(pending.amperage, pending.overclocked);
        syncManager.addCloseListener(player -> {
            if (!syncManager.isClient()) save.run();
        });
        var saveButton = new InteractionSyncHandler();
        saveButton.setOnMousePressed(mouse -> {
            if (!syncManager.isClient()) save.run();
        });

        var buttons = Flow.row()
                .coverChildren()
                .childPadding(4)
                .horizontalCenter();
        if (canOverclock()) {
            var overclockValue = new BooleanSyncValue(() -> pending.overclocked, v -> pending.overclocked = v)
                    .allowC2S();
            syncManager.syncValue("overclocked", overclockValue);
            buttons.child(new ToggleButton()
                    .syncHandler("overclocked")
                    .tooltipDynamic(t -> t.addLine(Component.translatable(
                            overclockValue.getBoolValue() ? OVERCLOCK_ON_KEY : OVERCLOCK_OFF_KEY,
                            GTValues.VNF[tier + 1])))
                    .overlay(false, GTGuiTextures.LIGHTNING_OVERLAY_1)
                    .overlay(true, GTGuiTextures.LIGHTNING_OVERLAY_2)
                    .background(GuiTextures.MC_BUTTON)
                    .background(true, GuiTextures.MC_BUTTON_PRESSED));
        }
        buttons.child(new ButtonWidget<>()
                .size(18, 18)
                .overlay(GuiTextures.SAVE)
                .tooltipDynamic(t -> t.addLine(Component.translatable(SAVE_KEY))
                        .addLine(Component.translatable(SAVE_TOOLTIP_KEY)))
                .syncHandler(saveButton));

        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 44)
                .child(WirelessEnergyUI.amperageRow(syncManager, () -> pending.amperage,
                        v -> pending.amperage = clampAmperage(v)))
                .child(buttons));
    }

    /**
     * Unsaved edits of one open UI.
     */
    private static final class PendingSettings {

        int amperage;
        boolean overclocked;

        PendingSettings(int amperage, boolean overclocked) {
            this.amperage = amperage;
            this.overclocked = overclocked;
        }
    }
}
