package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
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
 * Pattern crafter layout: the materials in a 3 x 3 grid like a crafting table's, 3 x 3 tanks, a slot for the crafted
 * item and 3 x 3 for what recipes give back, which also gets filled first come, first served. Below them a row of 9
 * patterns as in a pattern provider, right over the columns of the player's inventory. Pipes fill any slot, the machine
 * finds its patterns wherever they are; the filters are for the player.
 */
final class PatternCrafterUI {

    private static final String[] GRID = { "sss", "sss", "sss" };
    private static final String[] PATTERN_ROW = { "sssssssss" };
    private static final int FIRST_PATTERN_SLOT = CraftingPattern.SLOTS;
    /**
     * All slots of the machine: shift-click moves to other groups only, storage before the player's, so with a group
     * for the outputs what they hold would go to the inputs instead of the inventory.
     */
    private static final String SLOT_GROUP = "pattern_crafter";

    /** GT's panel with the patterns under its recipe area, without its logo, which would sit on the last output. */
    static final PanelFactory PANEL = (data, syncManager, settings, machine) -> {
        var crafter = (SimpleTieredMachine) machine;
        syncManager.registerSlotGroup(new SlotGroup(SLOT_GROUP, CraftingPattern.SIZE).setAllowSorting(false));
        return MachineUIPanelBuilder.panelBuilder(machine)
                .drawGTLogo(false)
                .mainContents(parent -> {
                    var recipeArea = new GTRecipeTypeMachineWidget(crafter.getRecipeType(), syncManager, machine,
                            crafter.getRecipeLogic()::getProgressPercent);
                    // it centers itself in the panel, and a Flow leaves out children with a position
                    recipeArea.resizer().resetPosition();
                    parent.child(Flow.col().coverChildren()
                            .center()
                            .childPadding(2)
                            .child(recipeArea)
                            .child(patternRow(machine)));
                })
                .build(syncManager, settings)
                .excludeAreaInRecipeViewer();
    };

    private static SlotGroupWidget patternRow(MetaMachine machine) {
        var itemHandler = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, IO.IN).get(0);
        return SlotGroupWidget.builder()
                .matrix(PATTERN_ROW)
                .key('s', i -> new ItemSlot()
                        .slot(new ModularSlot(itemHandler.storage, FIRST_PATTERN_SLOT + i)
                                .slotGroup(SLOT_GROUP)
                                .filter(PatternCrafter::isPattern))
                        .backgroundOverlay(GTGuiTextures.PATTERN_OVERLAY))
                .build()
                .coverChildren(18, 18);
    }

    private PatternCrafterUI() {}

    static GTRecipeTypeUILayout.Builder apply(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                .setMachineCapabilityLayoutBuilder(ItemRecipeCapability.CAP, MACHINE_ITEM)
                .setMachineCapabilityLayoutBuilder(FluidRecipeCapability.CAP, MACHINE_FLUID);
    }

    private static final MachineCapabilityLayoutBuilder MACHINE_ITEM = (machine, layout, widget, io) -> {
        var itemHandler = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, io).get(0);
        if (io != IO.IN) {
            widget.outputColumn.child(Flow.row().coverChildren()
                    .childPadding(4)
                    .child(new ItemSlot()
                            .slot(new ModularSlot(itemHandler.storage, 0)
                                    .slotGroup(SLOT_GROUP)
                                    .accessibility(false, true)))
                    .child(SlotGroupWidget.builder()
                            .matrix(GRID)
                            .key('s', i -> new ItemSlot()
                                    .slot(new ModularSlot(itemHandler.storage, i + 1)
                                            .slotGroup(SLOT_GROUP)
                                            .accessibility(false, true)))
                            .build()
                            .coverChildren(18, 18)));
            return;
        }
        var fluidTank = FluidRecipeCapability.CAP.getCapabilityHandlers(machine, io).get(0);

        widget.inputColumn.child(Flow.row().coverChildren()
                .childPadding(4)
                .child(SlotGroupWidget.builder()
                        .matrix(GRID)
                        .key('s', i -> new ItemSlot()
                                .slot(new ModularSlot(itemHandler.storage, i)
                                        .slotGroup(SLOT_GROUP)
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
