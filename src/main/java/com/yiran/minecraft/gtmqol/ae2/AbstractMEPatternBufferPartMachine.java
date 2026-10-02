package com.yiran.minecraft.gtmqol.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.crafting.pattern.EncodedPatternItem;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/**
 * GTCEu's pattern buffer with a different number of pattern slots. GTCEu inlines its 27 (MAX_PATTERN_COUNT) in
 * the constructor and the worker count; {@code mixin.MEPatternBufferPartMachineMixin} swaps those for
 * {@link #getMaxPatternCount()}. The pattern terminal inventory and the main UI also use 27, they are overridden
 * here.
 */
public abstract class AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine {

    private static final int MAX_VISIBLE_ROWS = 6;

    private final InternalInventory terminalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return getMaxPatternCount();
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return getPatternInventory().getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            getPatternInventory().setStackInSlot(slotIndex, stack);
            getPatternInventory().onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }
    };

    protected AbstractMEPatternBufferPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    /**
     * Called from the superclass constructor (through the mixin), before this class's fields are assigned, so
     * implementations must return constants.
     */
    public abstract int getPatternColumns();

    /** Same restriction as {@link #getPatternColumns()}. */
    public abstract int getPatternRows();

    public final int getMaxPatternCount() {
        return getPatternColumns() * getPatternRows();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return terminalPatternInventory;
    }

    // Same as GTCEu's, but sized from the grid and scrolling past MAX_VISIBLE_ROWS.
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        int columns = getPatternColumns();
        int rows = getPatternRows();
        SlotGroup patternSlotGroup = new SlotGroup("pattern_slots", columns, 0, true);

        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        var flow = Flow.col().coverChildren();

        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        var grid = new Grid()
                .minElementMargin(0, 0)
                .minColWidth(18).minRowHeight(18)
                .leftRel(0.5f);
        if (rows > MAX_VISIBLE_ROWS) {
            // Leave room for the scroll bar next to the slots.
            grid.size(18 * columns + 8, 18 * MAX_VISIBLE_ROWS).scrollable(new VerticalScrollData());
        } else {
            grid.height(18 * rows);
        }
        flow.child(grid.gridOfSizeWidth(getMaxPatternCount(), columns, (x, y, index) -> new ItemSlot()
                .slot(SyncHandlers.itemSlot(getPatternInventory(), index)
                        .slotGroup(patternSlotGroup)
                        .accessibility(true, true)
                        .filter(stack -> stack.getItem() instanceof EncodedPatternItem)
                        .changeListener((i, o, c, init) -> onPatternChange(index)))
                .background(GTGuiTextures.SLOT, GTGuiTextures.PATTERN_OVERLAY)));

        mainWidget.child(flow.center());
    }
}
