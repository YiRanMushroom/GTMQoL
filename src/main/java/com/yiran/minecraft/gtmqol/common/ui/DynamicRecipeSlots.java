package com.yiran.minecraft.gtmqol.common.ui;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.SlotGroupWidget;

/**
 * Recipe viewer slots that follow each recipe instead of the recipe type's fixed grid: give a capability
 * {@link #ANY} as its slot count (setMaxIOSize, setMaxSize, a gregified type's counts) and the viewer shows as many
 * slots as each recipe has, three to a row, where the fixed grid hid whatever didn't fit. EMI and JEI size each recipe
 * from its widget. For multiblock types, which take any number of hatches: a single block gets ANY real slots.
 */
public final class DynamicRecipeSlots {

    /** A slot count meaning "as many as each recipe has". Large, not unlimited: a fixed grid of it is only ugly. */
    public static final int ANY = 256;

    private static final int ROW = 3;

    private DynamicRecipeSlots() {}

    /** Slot {@code index} of a capability, made as its fixed grid makes it (name, background, overlay). */
    @FunctionalInterface
    public interface SlotFactory {

        IWidget slot(GTRecipeTypeUILayout layout, IO io, int index);
    }

    /** From {@link RecipeTypeLayouts}: the capability's layout gets per-recipe slots wherever its count is ANY. */
    // Untested: EMI (and JEI on 1.20) shows every gregified multiblock recipe with its own slots, three to a row: all
    // MI quarry drops, the distillation tower's 8 products, 5 for Create crushing horse armor, Mekanism chemicals; a
    // recipe of such a type without fluids shows no empty fluid slot
    public static void apply(GTRecipeType type, RecipeCapability<?> cap, SlotFactory factory) {
        if (type.getMaxInputs(cap) != ANY && type.getMaxOutputs(cap) != ANY) return;
        var info = type.getUiLayout().capabilityInfo(cap);
        var fixed = info.recipeViewerLayoutBuilder;
        info.recipeViewerLayoutBuilder = (layout, widget, io) -> {
            if (layout.getRecipeType().getMaxSlots(cap, io) == ANY) {
                build(cap, factory, layout, widget, io);
            } else if (fixed != null) {
                fixed.createCapabilityUILayout(layout, widget, io);
            }
        };
    }

    private static void build(RecipeCapability<?> cap, SlotFactory factory, GTRecipeTypeUILayout layout,
                              GTRecipeViewerWidget widget, IO io) {
        GTRecipe recipe = ((RecipeViewerWidgetAccess) widget).gtmqol$recipe();
        int count = io == IO.IN ?
                recipe.getInputContents(cap).size() + recipe.getTickInputContents(cap).size() :
                recipe.getOutputContents(cap).size() + recipe.getTickOutputContents(cap).size();
        if (count == 0) return;
        var slots = SlotGroupWidget.builder()
                .matrix(GTMuiWidgets.createGrid(count, Math.min(ROW, count), io == IO.OUT, 's'))
                .key('s', i -> factory.slot(layout, io, i))
                .build()
                .coverChildren(18, 18);
        (io == IO.IN ? widget.inputColumn : widget.outputColumn).child(slots);
    }

    /** gtceu's item slot ({@code RecipeViewerCapabilityLayoutBuilder.ITEM}). */
    public static IWidget itemSlot(GTRecipeTypeUILayout layout, IO io, int index) {
        var cap = ItemRecipeCapability.CAP;
        return RecipeViewerSlotWidget.create(ItemStack.class).value(ItemStackList.of(ItemStack.EMPTY))
                .background(GuiTextures.SLOT_ITEM, layout.capabilityInfo(cap).getOverlay(io, index))
                .name(GTRecipeViewerWidget.capabilityWidgetName(cap, io, index));
    }

    /** gtceu's fluid slot ({@code RecipeViewerCapabilityLayoutBuilder.FLUID}). */
    public static IWidget fluidSlot(GTRecipeTypeUILayout layout, IO io, int index) {
        var cap = FluidRecipeCapability.CAP;
        return RecipeViewerSlotWidget.create(FluidStack.class).value(FluidStackList.of(FluidStack.EMPTY))
                .background(GuiTextures.SLOT_FLUID, layout.capabilityInfo(cap).getOverlay(io, index))
                .name(GTRecipeViewerWidget.capabilityWidgetName(cap, io, index));
    }
}
