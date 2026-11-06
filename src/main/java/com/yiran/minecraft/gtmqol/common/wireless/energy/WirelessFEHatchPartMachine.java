package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

/**
 * Hatch giving its multiblock FE from the bound network ({@link IO#IN}) or taking FE from it into the network
 * ({@link IO#OUT}), as much as it asks in a tick. Found through {@link WirelessFEMachines#FE_INPUT} and
 * {@link WirelessFEMachines#FE_OUTPUT}.
 */
public class WirelessFEHatchPartMachine extends TieredIOPartMachine implements IMuiMachine, IDataStickInteractable {

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private final WirelessFEBuffer buffer;

    public WirelessFEHatchPartMachine(BlockEntityCreationInfo info, IO io) {
        super(info, GTValues.LV, io);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.buffer = new WirelessFEBuffer(binding::getNetworkId);
    }

    /**
     * Input hatch: takes FE from the network for the controller. Server only.
     *
     * @return what was taken, at most {@code fe}
     */
    public double extractFE(double fe, boolean simulate) {
        return io == IO.IN ? buffer.extract(fe, simulate) : 0;
    }

    /**
     * Output hatch: gives FE from the controller to the network. Server only.
     *
     * @return what went in, all of it unless the hatch is unbound
     */
    public double insertFE(double fe, boolean simulate) {
        if (io != IO.OUT || !buffer.canInsert()) return 0;
        if (!simulate) buffer.insert(fe);
        return fe;
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
        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
