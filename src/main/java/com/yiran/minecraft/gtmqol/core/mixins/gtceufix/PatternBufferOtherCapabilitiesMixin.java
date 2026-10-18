package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;

/**
 * gtceu bug: the pattern buffer's handler list only takes a recipe when one of its slots can take every content, but
 * slots only hold items and fluids, so recipes with any other input (ghost ingredients, block providers...) never ran
 * from a pattern buffer: it handed back all the contents, and the items showed as missing. Those other inputs come
 * from parts that bypass distinctness (gtceu adds them to every distinct group), so the buffer leaves them out and
 * hands them back. Remove once gtceu fixes it.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$BufferRecipeHandlerList",
       remap = false)
public abstract class PatternBufferOtherCapabilitiesMixin {

    @WrapMethod(method = "handleRecipe")
    private Map<RecipeCapability<?>, List<Object>> gtmqol$slotContentsOnly(
            IO io, GTRecipe recipe, Map<RecipeCapability<?>, List<Object>> contents, boolean simulate,
            Operation<Map<RecipeCapability<?>, List<Object>>> original) {
        Map<RecipeCapability<?>, List<Object>> slotContents = new Reference2ObjectOpenHashMap<>();
        Map<RecipeCapability<?>, List<Object>> others = new Reference2ObjectOpenHashMap<>();
        contents.forEach((cap, list) -> (gtmqol$inSlots(cap) ? slotContents : others).put(cap, list));
        if (others.isEmpty()) return original.call(io, recipe, contents, simulate);
        Map<RecipeCapability<?>, List<Object>> left = new Reference2ObjectOpenHashMap<>(
                original.call(io, recipe, slotContents, simulate));
        left.putAll(others);
        return left;
    }

    /** What the slots hold: items, fluids and the capabilities we bridge into AE (its {@code handlersFor}). */
    @Unique
    private static boolean gtmqol$inSlots(RecipeCapability<?> cap) {
        return cap == ItemRecipeCapability.CAP || cap == FluidRecipeCapability.CAP ||
                AEStackLikeBridges.forCap(cap) != null;
    }
}
