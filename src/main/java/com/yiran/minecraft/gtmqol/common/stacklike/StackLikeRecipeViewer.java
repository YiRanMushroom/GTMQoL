package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;

import net.minecraft.network.chat.Component;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widgets.SlotGroupWidget;

import java.util.List;

/**
 * Recipe viewer display of {@link GenericStackLikeRecipeCapability} contents: recipe viewer slots like gtceu's fluid
 * ones, in the input or output column. The recipe viewer needs a converter for the stack class (EMI:
 * {@code EmiStackConverter}, see {@code ChemicalEmiConverter}), without one the slots stay empty.
 */
public final class StackLikeRecipeViewer {

    private StackLikeRecipeViewer() {}

    /** Called for every recipe type UI layout; a recipe type's own {@code UI(...)} can still override these. */
    public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            builder.setRecipeViewerLayoutCapabilityLayoutBuilder(cap, layout(cap));
            builder.setCapabilityContentBuilder(cap, content(cap));
        }
    }

    /** Same as gtceu's {@code RecipeViewerCapabilityLayoutBuilder.FLUID}. */
    private static RecipeViewerCapabilityLayoutBuilder layout(GenericStackLikeRecipeCapability<?, ?> cap) {
        return (layout, widget, io) -> {
            int slots = layout.getRecipeType().getMaxSlots(cap, io);
            if (slots == 0) return;
            var column = io == IO.IN ? widget.inputColumn : widget.outputColumn;
            if (slots == 1) {
                column.child(slot(cap, layout, io, 0));
                return;
            }
            column.child(SlotGroupWidget.builder()
                    .matrix(layout.capabilityInfo(cap).getRecipeViewerGrid(io))
                    .key('s', i -> slot(cap, layout, io, i))
                    .build()
                    .coverChildren(18, 18));
        };
    }

    private static <S> RecipeViewerSlotWidget<S, ?> slot(GenericStackLikeRecipeCapability<S, ?> cap,
                                                         GTRecipeTypeUILayout layout, IO io, int i) {
        return RecipeViewerSlotWidget.create(cap.type.stackClass())
                .value(new StackLikeEntryList<>(cap.type.stackClass(), List.of()))
                .background(GuiTextures.SLOT_FLUID, layout.capabilityInfo(cap).getOverlay(io, i))
                .name(GTRecipeViewerWidget.capabilityWidgetName(cap, io, i));
    }

    /** Same as gtceu's {@code CapabilityContentBuilder.FLUID}, plus the amount which its overlay only draws for fluids. */
    @SuppressWarnings("unchecked")
    private static <S, I> CapabilityContentBuilder content(GenericStackLikeRecipeCapability<S, I> cap) {
        return (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
            if (!(widget instanceof RecipeViewerSlotWidget<?, ?> w)) return;
            var slot = (RecipeViewerSlotWidget<S, ?>) w;
            I ingredient = cap.of(content.content());

            slot.value(new StackLikeEntryList<>(cap.type.stackClass(), cap.type.getStacks(ingredient)));
            // value() resets the background to the item slot for anything but fluids.
            slot.background(GuiTextures.SLOT_FLUID);
            slot.overlay(new ContentOverlay(content, perTick),
                    new StackLikeAmountOverlay(cap.type.ingredientAmount(ingredient)));
            slot.chance((float) content.chance() / content.maxChance());

            // The chance is in the recipe viewer's own tooltip, as for gtceu's fluids.
            if (perTick) slot.tooltipBuilder(tooltip -> tooltip.addLine(Component.translatable("gtceu.gui.content.per_tick")));

            if (io == IO.IN && content.chance() == 0) slot.recipeSlotRole(RecipeSlotRole.CATALYST);
            else if (io == IO.IN) slot.recipeSlotRole(RecipeSlotRole.INPUT);
            else slot.recipeSlotRole(RecipeSlotRole.OUTPUT);
        };
    }
}
