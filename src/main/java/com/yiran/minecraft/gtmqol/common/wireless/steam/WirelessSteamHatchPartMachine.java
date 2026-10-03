package com.yiran.minecraft.gtmqol.common.wireless.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
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

import java.util.List;

/**
 * Steam hatch backed by the bound player's wireless network. {@link IO#IN} feeds steam multiblocks
 * ({@code PartAbility.STEAM}); {@link IO#OUT} takes fluid outputs such as large boiler steam
 * ({@code PartAbility.EXPORT_FLUIDS}).
 */
public class WirelessSteamHatchPartMachine extends TieredIOPartMachine
                                           implements IMuiMachine, IDataStickInteractable {

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    @SaveField
    public final NotifiableFluidTank tank;

    public WirelessSteamHatchPartMachine(BlockEntityCreationInfo info, IO io) {
        super(info, 0, io);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.tank = attachTrait(new NotifiableFluidTank(List.of(new WirelessSteamTank(binding::getNetworkId)), io));
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
        mainWidget.child(WirelessSteamUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
