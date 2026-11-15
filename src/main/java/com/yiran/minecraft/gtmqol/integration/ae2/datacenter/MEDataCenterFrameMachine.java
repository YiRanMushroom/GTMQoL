package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/**
 * Holds crafting units for its ME Data Center, one per slot: a slot more for each tier above ULV. The units are its
 * own, the data center only counts them (and shows them all in one window).
 */
public class MEDataCenterFrameMachine extends MultiblockPartMachine implements IMuiMachine {

    @SaveField
    private final CustomItemStackHandler units;

    public MEDataCenterFrameMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.units = new CustomItemStackHandler(tier + 1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        units.setFilter(MEDataCenter::isUnit);
        units.setOnContentsChanged(this::notifyDataCenter);
    }

    private void notifyDataCenter() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof MEDataCenterMachine dataCenter) dataCenter.onUnitsChanged();
        }
    }

    /** While a data center it belongs to has jobs, nothing goes in or out. */
    private boolean isLocked() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof MEDataCenterMachine dataCenter && dataCenter.isLocked()) return true;
        }
        return false;
    }

    public CustomItemStackHandler units() {
        return units;
    }

    /** It stands inside the structure, behind glass: it stays what it is. */
    @Override
    public boolean replacePartModelWhenFormed() {
        return false;
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        units.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        String group = "me_data_center_frame";
        var locked = new BooleanSyncValue(this::isLocked);
        syncManager.syncValue("locked", locked);
        syncManager.registerSlotGroup(new SlotGroup(group, units.getSlots()).setAllowSorting(false));
        Flow slots = Flow.row().coverChildren();
        for (int i = 0; i < units.getSlots(); i++) {
            slots.child(new ItemSlot().slot(new ModularSlot(units, i) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !locked.getBoolValue() && super.mayPlace(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return !locked.getBoolValue() && super.mayPickup(player);
                }
            }.slotGroup(group).filter(MEDataCenter::isUnit)));
        }
        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(slots)
                .child(new TextWidget<>(Text.dynamic(() -> locked.getBoolValue() ?
                        Component.translatable(MEDataCenterMachine.KEY + "locked").withStyle(ChatFormatting.RED) :
                        Component.empty()))));
    }
}
