package com.yiran.minecraft.gtmqol.common.blockprovider;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.SlotGroupWidget;

/**
 * Recipe viewer display of {@link BlockProviderRecipeCapability} inputs: item slots like gtceu's, in the input
 * column, showing the block's item as a catalyst. Recipe types need {@code setMaxSize(IO.IN, CAP, n)} for slots.
 */
public final class BlockProviderRecipeViewer {

    private static final BlockProviderRecipeCapability CAP = BlockProviderRecipeCapability.CAP;

    private BlockProviderRecipeViewer() {}

    /** Called for every recipe type UI layout; a recipe type's own {@code UI(...)} can still override these. */
    public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {
        builder.setRecipeViewerLayoutCapabilityLayoutBuilder(CAP, LAYOUT);
        builder.setCapabilityContentBuilder(CAP, CONTENT);
    }

    /** Same as gtceu's {@code RecipeViewerCapabilityLayoutBuilder.ITEM}. */
    private static final RecipeViewerCapabilityLayoutBuilder LAYOUT = (layout, widget, io) -> {
        int slots = layout.getRecipeType().getMaxSlots(CAP, io);
        if (slots == 0) return;
        var column = io == IO.IN ? widget.inputColumn : widget.outputColumn;
        if (slots == 1) {
            column.child(slot(layout, io, 0));
            return;
        }
        column.child(SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(CAP).getRecipeViewerGrid(io))
                .key('s', i -> slot(layout, io, i))
                .build()
                .coverChildren(18, 18));
    };

    private static RecipeViewerSlotWidget<ItemStack, ?> slot(GTRecipeTypeUILayout layout, IO io, int i) {
        return RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, layout.capabilityInfo(CAP).getOverlay(io, i))
                .name(GTRecipeViewerWidget.capabilityWidgetName(CAP, io, i));
    }

    @SuppressWarnings("unchecked")
    private static final CapabilityContentBuilder CONTENT = (widget, content, io, perTick, recipeType, recipe,
                                                             chanceTier, recipeTier) -> {
        if (!(widget instanceof RecipeViewerSlotWidget<?, ?> w)) return;
        var slot = (RecipeViewerSlotWidget<ItemStack, ?>) w;
        slot.value(ItemStackList.of(new ItemStack(CAP.of(content.content()).asItem())));
        slot.overlay(new ContentOverlay(content, perTick));
        // never consumed
        slot.recipeSlotRole(RecipeSlotRole.CATALYST);
    };
}
