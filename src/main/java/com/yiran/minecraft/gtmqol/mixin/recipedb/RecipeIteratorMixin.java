package com.yiran.minecraft.gtmqol.mixin.recipedb;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.yiran.minecraft.gtmqol.recipedb.GroupedIngredientList;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * When getNext reaches a branch it pushes a frame for every ingredient index, so the search tries every
 * combination of everything in the machine. Only push the indices that {@link GroupedIngredientList#canFollow}
 * the index that led to the branch. Lists that don't come from our fromHolder (e.g. RecipeDB.find(Map)) are
 * searched as before.
 */
@Mixin(value = RecipeDB.RecipeIterator.class, remap = false)
public class RecipeIteratorMixin {

    @Shadow
    @Final
    private Deque<Object> stack;

    @Unique
    private @Nullable GroupedIngredientList gtmqol$grouped;

    /** Ingredient index of the frame whose ingredient led to the branch being expanded. */
    @Unique
    private int gtmqol$from;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtmqol$readGroups(RecipeDB db, List<List<AbstractMapIngredient>> ingredients,
                                   Predicate<GTRecipe> predicate, CallbackInfo ci) {
        if (ingredients instanceof GroupedIngredientList grouped) {
            gtmqol$grouped = grouped;
        }
    }

    // The frame is still on top of the stack while its branch is expanded.
    @WrapOperation(method = "getNext",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/datafixers/util/Either;ifRight(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"))
    private Either<?, ?> gtmqol$rememberFrom(Either<?, ?> result, Consumer<?> pushBranch,
                                             Operation<Either<?, ?>> original) {
        if (gtmqol$grouped != null) {
            gtmqol$from = ((SearchFrameAccessor) stack.peek()).gtmqol$getIndex();
        }
        return original.call(result, pushBranch);
    }

    // The `b -> { for (j...) stack.push(new SearchFrame(j, b)); }` passed to ifRight above.
    @WrapOperation(method = "lambda$getNext$0",
                   at = @At(value = "INVOKE", target = "Ljava/util/Deque;push(Ljava/lang/Object;)V"))
    private void gtmqol$pushRelated(Deque<Object> deque, Object frame, Operation<Void> original) {
        if (gtmqol$grouped != null &&
                !gtmqol$grouped.canFollow(gtmqol$from, ((SearchFrameAccessor) frame).gtmqol$getIndex())) {
            return;
        }
        original.call(deque, frame);
    }
}
