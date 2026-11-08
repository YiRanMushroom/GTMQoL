package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * {@code gtceufix/FluidRecipeCapabilityMixin} shows one fluid for a component fluid input (a potion), though the recipe
 * takes the same contents in any fluid of the tag (other mods' potions). Say so in the slot's tooltip. Wraps each
 * recipe type's fluid slot builder, see {@link com.yiran.minecraft.gtmqol.common.ui.RecipeTypeLayouts}.
 */
public final class PotionFluidHint {

    // slots are filled again on every OC change, and tooltip builders add up
    private static final Set<IWidget> HINTED = Collections.newSetFromMap(new WeakHashMap<>());

    private PotionFluidHint() {}

    public static void addTo(GTRecipeTypeUILayout layout) {
        var info = layout.capabilityInfo(FluidRecipeCapability.CAP);
        CapabilityContentBuilder fluids = info.capabilityWidgetBuilder;
        if (fluids == null) return;
        info.capabilityWidgetBuilder = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
            fluids.buildWidgetContent(widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier);
            hint(widget, content, io);
        };
    }

    private static void hint(IWidget widget, Content content, IO io) {
        if (io == IO.IN && widget instanceof RecipeViewerSlotWidget<?, ?> slot &&
                content.content() instanceof SizedFluidIngredient ingredient && anyMod(ingredient) &&
                HINTED.add(widget)) {
            // rebuilt every frame so the hint can flash
            slot.tooltipAutoUpdate(true);
            slot.tooltipBuilder(tooltip -> tooltip.addLine(Component.translatable(PotionContainers.ANY_MOD_KEY)
                    .withStyle(Util.getMillis() / 500 % 2 == 0 ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
        }
    }

    private static boolean anyMod(SizedFluidIngredient ingredient) {
        if (!(ingredient.ingredient() instanceof DataComponentFluidIngredient component) ||
                component.components().alwaysMatches()) {
            return false;
        }
        return Arrays.stream(ingredient.getFluids())
                .map(FluidStack::getFluid)
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
                .distinct()
                .count() > 1;
    }
}
