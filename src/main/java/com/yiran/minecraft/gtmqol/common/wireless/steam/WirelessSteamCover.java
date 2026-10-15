package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessCover;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

/**
 * Moves steam between the machine it is on and the bound player's wireless network, as much as the machine
 * takes or gives each tick: input fills it, output drains it (boilers).
 */
public class WirelessSteamCover extends WirelessCover {

    public WirelessSteamCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, IO io) {
        super(definition, coverHolder, attachedSide, io);
    }

    private @Nullable IFluidHandler getFluidHandler() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .resolve().orElse(null);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getFluidHandler() != null;
    }

    @Override
    protected void update(NetworkId network) {
        IFluidHandler handler = getFluidHandler();
        if (handler == null) return;
        var data = WirelessSteamSavedData.get();
        if (io == IO.IN) {
            int stored = data.getStoredInt(network);
            if (stored <= 0) return;
            int wanted = handler.fill(GTMaterials.Steam.getFluid(stored), FluidAction.SIMULATE);
            int taken = data.extract(network, wanted, false);
            if (taken <= 0) return;
            int filled = handler.fill(GTMaterials.Steam.getFluid(taken), FluidAction.EXECUTE);
            if (filled < taken) data.insert(network, taken - filled);
        } else {
            FluidStack drained = handler.drain(GTMaterials.Steam.getFluid(Integer.MAX_VALUE), FluidAction.EXECUTE);
            if (!drained.isEmpty()) data.insert(network, drained.getAmount());
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        column.child(WirelessSteamUI.create(syncManager, binding, data.getPlayer()).widthRel(1f));
    }
}
