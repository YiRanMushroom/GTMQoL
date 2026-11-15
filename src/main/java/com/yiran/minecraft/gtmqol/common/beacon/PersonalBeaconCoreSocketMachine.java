package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.Nullable;

/** Holds one core for its Personal Beacon, and shows it on every side in the colour of its effect. */
public class PersonalBeaconCoreSocketMachine extends MultiblockPartMachine implements IMuiMachine {

    /** {@link #tintColor}'s index of the core on the block's sides, as in the block's model. */
    private static final int CORE_TINT = 2;

    @SaveField
    private final CustomItemStackHandler core;
    /** The colour of the core's effect, -1 (white) when empty. */
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private int coreColor = -1;

    public PersonalBeaconCoreSocketMachine(BlockEntityCreationInfo info) {
        super(info);
        this.core = new CustomItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        core.setFilter(BeaconCoreItem::isCore);
        core.setOnContentsChanged(this::onCoreChanged);
    }

    private void onCoreChanged() {
        var effect = effect();
        coreColor = effect == null ? -1 : 0xFF000000 | effect.getColor();
        syncDataHolder.markClientSyncFieldDirty("coreColor");
        markAsChanged();
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof PersonalBeaconMachine beacon) beacon.onCoresChanged();
        }
    }

    public ItemStack core() {
        return core.getStackInSlot(0);
    }

    /** Its core's effect, or null when empty. */
    public @Nullable MobEffect effect() {
        return BeaconCoreItem.effect(core());
    }

    // Untested: a socket shows a white core on all six sides, and the core in its effect's colour once one is in
    @Override
    public int tintColor(int index) {
        return index == CORE_TINT ? coreColor : super.tintColor(index);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        core.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(Text.lang(PersonalBeacon.KEY + "socket.label").asWidget())
                .child(new ItemSlot()
                        .slot(new ModularSlot(core, 0).singletonSlotGroup(0).filter(BeaconCoreItem::isCore))
                        .background(GTGuiTextures.SLOT)));
    }
}
