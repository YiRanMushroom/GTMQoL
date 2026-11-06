package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeMachineWidget;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.MachineCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/**
 * Pattern crafter layout: a slot for the pattern, the materials in a 3 x 3 grid like a crafting table's, 3 x 3 tanks,
 * and GT's default 3 x 3 outputs. Pipes fill any slot, the machine finds its pattern wherever it is; the filters are for
 * the player.
 */
final class PatternCrafterUI {

    private static final String[] GRID = { "sss", "sss", "sss" };
    private static final int PATTERN_SLOT = CraftingPattern.SLOTS;

    /** GT's panel without its logo, which would sit on the last output: this layout fills the panel's width. */
    static final PanelFactory PANEL = (data, syncManager, settings, machine) -> {
        var crafter = (SimpleTieredMachine) machine;
        return MachineUIPanelBuilder.panelBuilder(machine)
                .drawGTLogo(false)
                .mainContents(parent -> parent.child(new GTRecipeTypeMachineWidget(crafter.getRecipeType(),
                        syncManager, machine, crafter.getRecipeLogic()::getProgressPercent)))
                .build(syncManager, settings)
                .excludeAreaInRecipeViewer();
    };

    private PatternCrafterUI() {}

    static GTRecipeTypeUILayout.Builder apply(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                .setMachineCapabilityLayoutBuilder(ItemRecipeCapability.CAP, MACHINE_ITEM)
                .setMachineCapabilityLayoutBuilder(FluidRecipeCapability.CAP, MACHINE_FLUID);
    }

    private static final MachineCapabilityLayoutBuilder MACHINE_ITEM = (machine, layout, widget, io) -> {
        if (io != IO.IN) {
            MachineCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(machine, layout, widget, io);
            return;
        }
        var itemHandler = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, io).get(0);
        var fluidTank = FluidRecipeCapability.CAP.getCapabilityHandlers(machine, io).get(0);
        var slotGroup = new SlotGroup(ItemRecipeCapability.CAP.id + "_" + io.name(), CraftingPattern.SIZE);

        widget.inputColumn.child(Flow.row().coverChildren()
                .childPadding(4)
                .child(new ItemSlot()
                        .slot(new ModularSlot(itemHandler.storage, PATTERN_SLOT)
                                .slotGroup(slotGroup)
                                .filter(PatternCrafter::isPattern))
                        .backgroundOverlay(GTGuiTextures.PATTERN_OVERLAY))
                .child(SlotGroupWidget.builder()
                        .matrix(GRID)
                        .key('s', i -> new ItemSlot()
                                .slot(new ModularSlot(itemHandler.storage, i)
                                        .slotGroup(slotGroup)
                                        .filter(stack -> !PatternCrafter.isPattern(stack))))
                        .build()
                        .coverChildren(18, 18))
                .child(SlotGroupWidget.builder()
                        .matrix(GRID)
                        .key('s', i -> new FluidSlot()
                                .syncHandler(new FluidSlotSyncHandler(fluidTank.getStorages()[i])
                                        .canFillSlot(true)
                                        .canDrainSlot(true)
                                        .controlsAmount(true)))
                        .build()
                        .coverChildren(18, 18)));
    };

    private static final MachineCapabilityLayoutBuilder MACHINE_FLUID = (machine, layout, widget, io) -> {
        if (io != IO.IN) MachineCapabilityLayoutBuilder.FLUID.createCapabilityUILayout(machine, layout, widget, io);
    };
}
