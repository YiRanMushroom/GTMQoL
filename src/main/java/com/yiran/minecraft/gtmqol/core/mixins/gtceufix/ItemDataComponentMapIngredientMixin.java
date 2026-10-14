package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemDataComponentMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * gtceu 1.21 bug: recipe lookup keys for a partial (non-strict) {@link DataComponentIngredient} only equal keys with
 * the very same component predicate. An input stack's key carries all of its components, so a recipe asking for
 * just some of them (Productive Bees' {@code productivebees:component}: a configurable comb of one bee type) is never
 * found, though the recipe itself would match. Let two partial keys of the same item match when either predicate
 * accepts the other's stack; recipes of the same item with different predicates still don't, and the found recipe
 * is matched against the real inputs afterwards anyway. Remove once gtceu fixes it.
 */
@Mixin(value = ItemDataComponentMapIngredient.class, remap = false)
public abstract class ItemDataComponentMapIngredientMixin extends ItemStackMapIngredient {

    @Shadow
    protected DataComponentIngredient componentIngredient;

    private ItemDataComponentMapIngredientMixin() {
        super(ItemStack.EMPTY);
    }

    @ModifyReturnValue(method = "equals", at = @At("RETURN"))
    private boolean gtmqol$matchPartialComponents(boolean equal, Object obj) {
        if (equal || !(obj instanceof ItemDataComponentMapIngredient)) return equal;
        var other = (ItemDataComponentMapIngredientMixin) obj;
        if (componentIngredient == null || other.componentIngredient == null ||
                componentIngredient.isStrict() || other.componentIngredient.isStrict() ||
                !ItemStack.isSameItem(stack, other.stack)) {
            return false;
        }
        return componentIngredient.test(other.stack) || other.componentIngredient.test(stack);
    }
}
