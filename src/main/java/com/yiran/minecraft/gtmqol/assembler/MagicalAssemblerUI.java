package com.yiran.minecraft.gtmqol.assembler;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.MachineCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/**
 * Magical assembler layout: a 4x4 item grid with the 4 fluid inputs as a column right next to it, then the
 * progress bar and outputs. GTCEu stacks each capability's inputs vertically in {@code inputColumn}, so the item
 * builder puts items and fluids into one row and the fluid builder skips the input side. Outputs use the
 * defaults. Slot widgets are built the same way as in {@link MachineCapabilityLayoutBuilder#ITEM} and friends.
 */
final class MagicalAssemblerUI {

    private static final String[] ITEM_GRID = { "ssss", "ssss", "ssss", "ssss" };
    private static final String[] FLUID_GRID = { "s", "s", "s", "s" };

    private MagicalAssemblerUI() {}

    static GTRecipeTypeUILayout.Builder apply(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER)
                .setMachineCapabilityLayoutBuilder(ItemRecipeCapability.CAP, MACHINE_ITEM)
                .setMachineCapabilityLayoutBuilder(FluidRecipeCapability.CAP, MACHINE_FLUID)
                .setRecipeViewerLayoutCapabilityLayoutBuilder(ItemRecipeCapability.CAP, VIEWER_ITEM)
                .setRecipeViewerLayoutCapabilityLayoutBuilder(FluidRecipeCapability.CAP, VIEWER_FLUID);
    }

    private static final MachineCapabilityLayoutBuilder MACHINE_ITEM = (machine, layout, widget, io) -> {
        if (io != IO.IN) {
            MachineCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(machine, layout, widget, io);
            return;
        }
        var row = Flow.row().coverChildren();

        var itemHandlers = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, io);
        if (!itemHandlers.isEmpty() && itemHandlers.get(0) instanceof NotifiableItemStackHandler itemHandler) {
            var slotGroup = new SlotGroup(ItemRecipeCapability.CAP.id + "_" + io.name(), 4);
            row.child(SlotGroupWidget.builder()
                    .matrix(ITEM_GRID)
                    .key('s', i -> new ItemSlot()
                            .slot(new ModularSlot(itemHandler.storage, i)
                                    .slotGroup(slotGroup)
                                    .accessibility(itemHandler.getCapabilityIO().support(IO.IN), true))
                            .backgroundOverlay(layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i)))
                    .build()
                    .coverChildren(18, 18));
        }

        var fluidHandlers = FluidRecipeCapability.CAP.getCapabilityHandlers(machine, io);
        if (!fluidHandlers.isEmpty() && fluidHandlers.get(0) instanceof NotifiableFluidTank fluidTank) {
            row.child(SlotGroupWidget.builder()
                    .matrix(FLUID_GRID)
                    .key('s', i -> new FluidSlot()
                            .syncHandler(new FluidSlotSyncHandler(fluidTank.getStorages()[i])
                                    .canFillSlot(fluidTank.getCapabilityIO().support(IO.IN))
                                    .canDrainSlot(true)
                                    .controlsAmount(true))
                            .backgroundOverlay(layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i)))
                    .build()
                    .coverChildren(18, 18));
        }

        widget.inputColumn.child(row);
    };

    private static final MachineCapabilityLayoutBuilder MACHINE_FLUID = (machine, layout, widget, io) -> {
        if (io != IO.IN) MachineCapabilityLayoutBuilder.FLUID.createCapabilityUILayout(machine, layout, widget, io);
    };

    private static final RecipeViewerCapabilityLayoutBuilder VIEWER_ITEM = (layout, widget, io) -> {
        if (io != IO.IN) {
            RecipeViewerCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(layout, widget, io);
            return;
        }
        widget.inputColumn.child(Flow.row().coverChildren()
                .child(SlotGroupWidget.builder()
                        .matrix(ITEM_GRID)
                        .key('s', i -> RecipeViewerSlotWidget.create(ItemStack.class)
                                .value(ItemStackList.of(ItemStack.EMPTY))
                                .background(GuiTextures.SLOT_ITEM,
                                        layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, i)))
                        .build()
                        .coverChildren(18, 18))
                .child(SlotGroupWidget.builder()
                        .matrix(FLUID_GRID)
                        .key('s', i -> RecipeViewerSlotWidget.create(FluidStack.class)
                                .value(FluidStackList.of(FluidStack.EMPTY))
                                .background(GuiTextures.SLOT_FLUID,
                                        layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, i)))
                        .build()
                        .coverChildren(18, 18)));
    };

    private static final RecipeViewerCapabilityLayoutBuilder VIEWER_FLUID = (layout, widget, io) -> {
        if (io != IO.IN) RecipeViewerCapabilityLayoutBuilder.FLUID.createCapabilityUILayout(layout, widget, io);
    };
}
