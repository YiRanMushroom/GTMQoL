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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/** Holds patterns for its assembling matrix, which sees them all together. */
public class AssemblingMatrixPatternCoreMachine extends MultiblockPartMachine implements IMuiMachine {

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

    // Decoding a pattern can check its recipe, so each slot keeps its pattern while it holds the same stack and the
    // recipes weren't reloaded (a reload makes a new recipe manager)
    private final IPatternDetails[] decoded = new IPatternDetails[SLOTS];
    private final ItemStack[] decodedFrom = new ItemStack[SLOTS];
    private @Nullable RecipeManager decodedWith;

    public AssemblingMatrixPatternCoreMachine(BlockEntityCreationInfo info) {
        super(info);
        patterns.setFilter(PatternCrafter::isPattern);
        patterns.setOnContentsChanged(this::notifyMatrix);
    }

    private void notifyMatrix() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof AssemblingMatrixMachine matrix) matrix.onPatternsChanged();
        }
    }

    /** The pattern in {@code slot}, or null; decoded again only when the slot or the recipes changed. */
    @Nullable
    IPatternDetails decoded(int slot, Level level) {
        if (decodedWith != level.getRecipeManager()) {
            Arrays.fill(decodedFrom, null);
            decodedWith = level.getRecipeManager();
        }
        ItemStack stack = patterns.getStackInSlot(slot);
        ItemStack from = decodedFrom[slot];
        if (from == null || !ItemStack.isSameItemSameComponents(from, stack)) {
            decodedFrom[slot] = stack.copy();
            decoded[slot] = stack.isEmpty() ? null : PatternDetailsHelper.decodePattern(stack, level);
        }
        return decoded[slot];
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
        super.onMachineDestroyed();
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
