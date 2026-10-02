package com.yiran.minecraft.gtmqol.wireless.energy;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.wireless.WirelessBindingTrait;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

/**
 * Standalone block connecting cables to the bound player's wireless energy network: the front emits, every
 * other side accepts, both at its tier and adjustable amperage (see {@link WirelessEnergyAccessorContainer}).
 */
public class WirelessEnergyAccessorMachine extends MetaMachine
                                           implements ITieredMachine, IMuiMachine, IDataStickInteractable {

    private final int tier;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    @SaveField
    public final WirelessEnergyAccessorContainer energyContainer;

    @SaveField
    @SyncToClient
    protected int amperage = WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE;

    public WirelessEnergyAccessorMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
        this.binding = attachTrait(new WirelessBindingTrait());
        this.energyContainer = attachTrait(new WirelessEnergyAccessorContainer(binding::getNetworkId, tier,
                this::getAmperage, this::getFrontFacing));
    }

    @Override
    public int getTier() {
        return tier;
    }

    public int getAmperage() {
        return amperage;
    }

    public void setAmperage(int amperage) {
        this.amperage = WirelessEnergyHatchPartMachine.clampAmperage(amperage);
        syncDataHolder.markClientSyncFieldDirty("amperage");
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
        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 22)
                .child(WirelessEnergyUI.amperageRow(syncManager, this::getAmperage, this::setAmperage)));
    }
}
