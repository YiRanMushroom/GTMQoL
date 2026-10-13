package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.MachineCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import java.util.function.IntFunction;

/**
 * The breeder's inputs: the two parents and the empty cage in a column, then the breeding items in a column of their
 * own, in the order {@link BreederLogic} puts them into its recipes. The offspring go into a 4x4 grid.
 */
final class BreederUI {

    static final int CAGES = 3;
    static final int ITEMS = 2;

    private static final int GAP = 4;
    private static final String[] OUTPUT_GRID = { "ssss", "ssss", "ssss", "ssss" };

    private BreederUI() {}

    static GTRecipeTypeUILayout.Builder layout(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                .setLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, layout -> OUTPUT_GRID)
                .setMachineCapabilityLayoutBuilder(ItemRecipeCapability.CAP, MACHINE_ITEMS)
                .setRecipeViewerLayoutCapabilityLayoutBuilder(ItemRecipeCapability.CAP, VIEWER_ITEMS);
    }

    private static Flow inputs(IntFunction<IWidget> item) {
        Flow cages = Flow.col().coverChildren();
        for (int i = 0; i < CAGES; i++) cages.child(item.apply(i));
        Flow items = Flow.col().coverChildren();
        for (int i = 0; i < ITEMS; i++) items.child(item.apply(CAGES + i));
        return Flow.row().coverChildren().childPadding(GAP).child(cages).child(items);
    }

    private static final MachineCapabilityLayoutBuilder MACHINE_ITEMS = (machine, layout, widget, io) -> {
        if (io != IO.IN) {
            MachineCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(machine, layout, widget, io);
            return;
        }
        var items = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, IO.IN);
        if (items.isEmpty()) return;
        NotifiableItemStackHandler itemHandler = items.get(0);
        var slotGroup = new SlotGroup(ItemRecipeCapability.CAP.id + "_" + io.name(), 2);
        boolean insert = itemHandler.getCapabilityIO().support(IO.IN);
        widget.inputColumn.child(inputs(i -> new ItemSlot()
                .slot(new ModularSlot(itemHandler.storage, i).slotGroup(slotGroup).accessibility(insert, true))));
    };

    private static final RecipeViewerCapabilityLayoutBuilder VIEWER_ITEMS = (layout, widget, io) -> {
        if (io != IO.IN) {
            RecipeViewerCapabilityLayoutBuilder.ITEM.createCapabilityUILayout(layout, widget, io);
            return;
        }
        widget.inputColumn.child(inputs(i -> RecipeViewerSlotWidget.create(ItemStack.class)
                .value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM)
                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, IO.IN, i))));
    };
}
