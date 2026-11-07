package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/** Holds patterns for its pattern matrix, which sees them all together. */
public class MatrixPatternCoreMachine extends MultiblockPartMachine implements IMuiMachine {

    public static final int SLOTS = 72;

    private static final int COLUMNS = 9;
    private static final String[] GRID = { "sssssssss", "sssssssss", "sssssssss", "sssssssss", "sssssssss",
            "sssssssss", "sssssssss", "sssssssss" };

    @SaveField
    private final CustomItemStackHandler patterns = new CustomItemStackHandler(SLOTS) {

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final InternalInventory terminalInventory = new InternalInventory() {

        @Override
        public int size() {
            return SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return patterns.getStackInSlot(slot);
        }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {
            patterns.setStackInSlot(slot, stack);
            patterns.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return PatternCrafter.isPattern(stack);
        }
    };

    public MatrixPatternCoreMachine(BlockEntityCreationInfo info) {
        super(info);
        patterns.setFilter(PatternCrafter::isPattern);
        patterns.setOnContentsChanged(this::notifyMatrix);
    }

    private void notifyMatrix() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof PatternMatrixMachine matrix) matrix.onPatternsChanged();
        }
    }

    public CustomItemStackHandler patterns() {
        return patterns;
    }

    public InternalInventory terminalInventory() {
        return terminalInventory;
    }

    /** It stands inside the structure, behind glass: it stays what it is. */
    @Override
    public boolean replacePartModelWhenFormed() {
        return false;
    }

    @Override
    public void onMachineDestroyed() {
        patterns.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        String group = "pattern_core";
        syncManager.registerSlotGroup(new SlotGroup(group, COLUMNS).setAllowSorting(false));
        mainWidget.child(SlotGroupWidget.builder()
                .matrix(GRID)
                .key('s', i -> new ItemSlot()
                        .slot(new ModularSlot(patterns, i)
                                .slotGroup(group)
                                .filter(PatternCrafter::isPattern))
                        .backgroundOverlay(GTGuiTextures.PATTERN_OVERLAY))
                .build()
                .coverChildren(18, 18)
                .center());
    }
}
