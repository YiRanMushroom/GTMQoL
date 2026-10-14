package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.MachineCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import java.util.function.IntFunction;

/**
 * The beehive's inputs: the four upgrade slots in a column on the left, then the bee, flower and flower fluid in a
 * column of their own. Item slot 0 is the bee, 1 the flower, the rest upgrades, in the order {@link BeehiveLogic}
 * puts them into its recipes. GT would put the fluid under the items, so the item layout places it and the fluid
 * layout adds nothing for inputs.
 */
final class BeehiveUI {

    static final String INFO = "gtmqol.recipe.pb_beehive.info";

    private static final int UPGRADES = 4;
    private static final int GAP = 4;

    private BeehiveUI() {}

    static GTRecipeTypeUILayout.Builder layout(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                .setMachineCapabilityLayoutBuilder(ItemRecipeCapability.CAP, MACHINE_ITEMS)
                .setMachineCapabilityLayoutBuilder(FluidRecipeCapability.CAP, MACHINE_FLUIDS)
                .setRecipeViewerLayoutCapabilityLayoutBuilder(ItemRecipeCapability.CAP, VIEWER_ITEMS)
                .setRecipeViewerLayoutCapabilityLayoutBuilder(FluidRecipeCapability.CAP, VIEWER_FLUIDS)
                .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang(INFO)));
    }

    private static Flow inputs(IntFunction<IWidget> item, IWidget fluid) {
        Flow upgrades = Flow.col().coverChildren();
        for (int i = 0; i < UPGRADES; i++) upgrades.child(item.apply(2 + i));
        Flow bee = Flow.col().coverChildren().child(item.apply(0)).child(item.apply(1)).child(fluid);
        return Flow.row().coverChildren().childPadding(GAP).child(upgrades).child(bee);
    }

    private static final MachineCapabilityLayoutBuilder MACHINE_ITEMS = (machine, layout, widget, io) -> {
        if (io != IO.IN) {
            MachineCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(machine, layout, widget, io);
            return;
        }
        var items = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, IO.IN);
        var fluids = FluidRecipeCapability.CAP.getCapabilityHandlers(machine, IO.IN);
        if (items.isEmpty() || fluids.isEmpty()) return;
        NotifiableItemStackHandler itemHandler = items.get(0);
        NotifiableFluidTank fluidTank = fluids.get(0);
        var slotGroup = new SlotGroup(ItemRecipeCapability.CAP.id + "_" + io.name(), 3);
        boolean insert = itemHandler.getCapabilityIO().support(IO.IN);
        var fluid = new FluidSlot()
                .syncHandler(new FluidSlotSyncHandler(fluidTank.getStorages()[0])
                        .canFillSlot(fluidTank.getCapabilityIO().support(IO.IN))
                        .canDrainSlot(true)
                        .controlsAmount(true));
        widget.inputColumn.child(inputs(i -> new ItemSlot()
                .slot(new ModularSlot(itemHandler.storage, i).slotGroup(slotGroup).accessibility(insert, true)),
                fluid));
    };

    private static final MachineCapabilityLayoutBuilder MACHINE_FLUIDS = (machine, layout, widget, io) -> {
        if (io != IO.IN) MachineCapabilityLayoutBuilder.FLUID.createCapabilityUILayout(machine, layout, widget, io);
    };

    private static final RecipeViewerCapabilityLayoutBuilder VIEWER_ITEMS = (layout, widget, io) -> {
        if (io != IO.IN) {
            RecipeViewerCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(layout, widget, io);
            return;
        }
        var fluid = RecipeViewerSlotWidget.create(FluidStack.class).value(FluidStackList.of(FluidStack.EMPTY))
                .background(GuiTextures.SLOT_FLUID)
                .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, IO.IN, 0));
        widget.inputColumn.child(inputs(i -> RecipeViewerSlotWidget.create(ItemStack.class)
                .value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, i)), fluid));
    };

    private static final RecipeViewerCapabilityLayoutBuilder VIEWER_FLUIDS = (layout, widget, io) -> {
        if (io != IO.IN) RecipeViewerCapabilityLayoutBuilder.FLUID.createCapabilityUILayout(layout, widget, io);
    };
}
