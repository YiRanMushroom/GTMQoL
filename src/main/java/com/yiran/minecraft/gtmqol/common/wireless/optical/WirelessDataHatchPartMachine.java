package com.yiran.minecraft.gtmqol.common.wireless.optical;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalDataHatchMachine;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelKind;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelRegistry;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelRole;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelUI;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

import java.util.Collection;

/**
 * gtceu's optical data hatch over a channel: the transmitter goes on a data bank (and works as gtceu's), the
 * receiver on an assembly line and asks every loaded transmitter of its channel. Always many to many.
 */
public class WirelessDataHatchPartMachine extends OpticalDataHatchMachine implements IMuiMachine {

    @SaveField
    @SyncToClient
    public final ChannelBindingTrait binding;

    public WirelessDataHatchPartMachine(BlockEntityCreationInfo info, boolean transmitter) {
        super(info, transmitter);
        this.binding = attachTrait(new ChannelBindingTrait(ChannelKind.OPTICAL,
                transmitter ? ChannelRole.TRANSMITTER : ChannelRole.RECEIVER));
    }

    @Override
    public boolean isRecipeAvailable(GTRecipe recipe, Collection<IDataAccessHatch> seen) {
        if (isTransmitter()) return super.isRecipeAvailable(recipe, seen);
        seen.add(this);
        if (!isFormed() || binding.getChannel() == null) return false;
        for (ChannelBindingTrait transmitter : ChannelRegistry.loaded(binding.getChannelId(),
                ChannelRole.TRANSMITTER)) {
            if (transmitter.getMachine() instanceof IDataAccessHatch hatch && !seen.contains(hatch) &&
                    hatch.isRecipeAvailable(recipe, seen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(ChannelUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
