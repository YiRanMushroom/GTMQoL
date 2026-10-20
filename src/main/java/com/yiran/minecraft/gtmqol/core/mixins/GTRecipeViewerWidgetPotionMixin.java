package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * {@code gtceufix/FluidRecipeCapabilityMixin} shows one fluid for an NBT fluid input (a potion), though the recipe
 * takes the same NBT in any fluid of the tag (other mods' potions). Say so in the slot's tooltip.
 * <p>
 * Kept apart from {@code GTRecipeViewerWidgetMixin}, which {@code GTMQoLMixinPlugin} skips with the OC overhaul.
 */
@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public class GTRecipeViewerWidgetPotionMixin {

    // slots are filled again on every OC change, and tooltip builders add up
    @Unique
    private static final Set<IWidget> gtmqol$hinted = Collections.newSetFromMap(new WeakHashMap<>());

    @WrapOperation(method = "loadCapContent", at = @At(value = "INVOKE",
            target = "Lcom/gregtechceu/gtceu/api/recipe/gui/CapabilityContentBuilder;buildWidgetContent(Lbrachy/modularui/api/widget/IWidget;Lcom/gregtechceu/gtceu/api/recipe/content/Content;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;ZLcom/gregtechceu/gtceu/api/recipe/GTRecipeType;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;II)V"))
    private void gtmqol$anyModHint(CapabilityContentBuilder builder, IWidget widget, Content content, IO io,
                                   boolean perTick, GTRecipeType recipeType, GTRecipe recipe, int chanceTier,
                                   int recipeTier, Operation<Void> original) {
        original.call(builder, widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier);
        if (io == IO.IN && widget instanceof RecipeViewerSlotWidget<?, ?> slot &&
                content.content() instanceof FluidIngredient ingredient && gtmqol$anyMod(ingredient) &&
                gtmqol$hinted.add(widget)) {
            // rebuilt every frame so the hint can flash
            slot.tooltipAutoUpdate(true);
            slot.tooltipBuilder(tooltip -> tooltip.addLine(Component.translatable(PotionContainers.ANY_MOD_KEY)
                    .withStyle(Util.getMillis() / 500 % 2 == 0 ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
        }
    }

    @Unique
    private static boolean gtmqol$anyMod(FluidIngredient ingredient) {
        CompoundTag nbt = ingredient.getNbt();
        if (nbt == null || nbt.isEmpty() || ingredient instanceof IRangedIngredient) return false;
        return Arrays.stream(ingredient.getStacks())
                .map(FluidStack::getFluid)
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
                .distinct()
                .count() > 1;
    }
}
