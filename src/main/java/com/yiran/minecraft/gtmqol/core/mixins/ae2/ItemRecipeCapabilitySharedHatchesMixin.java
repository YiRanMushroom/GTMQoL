package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Parallels: a pattern buffer sharing hatches ({@link SharedHatches}) counts the shared parts' contents with its own,
 * as its recipes may take from both.
 */
@Mixin(value = ItemRecipeCapability.class, remap = false)
public abstract class ItemRecipeCapabilitySharedHatchesMixin {

    // the first getCapability is the distinct handler lists' own inventory
    @WrapOperation(method = "getInputContents",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeHandlerList;getCapability(Lcom/gregtechceu/gtceu/api/capability/recipe/RecipeCapability;)Ljava/util/List;",
                            ordinal = 0))
    private static List<IRecipeHandler<?>> gtmqol$withSharedHatches(RecipeHandlerList handlerList,
                                                                    RecipeCapability<?> cap,
                                                                    Operation<List<IRecipeHandler<?>>> original,
                                                                    @Local(argsOnly = true) IRecipeCapabilityHolder holder) {
        List<IRecipeHandler<?>> handlers = original.call(handlerList, cap);
        if (!(handlerList instanceof SharedHatches.HandlerList buffer) || !buffer.gtmqol$sharesHatches()) {
            return handlers;
        }
        List<IRecipeHandler<?>> all = new ArrayList<>(handlers);
        for (RecipeHandlerList shared : SharedHatches.of(holder.getCapabilitiesForIO(IO.IN), handlerList)) {
            all.addAll(shared.getCapability(cap));
        }
        return all;
    }
}
