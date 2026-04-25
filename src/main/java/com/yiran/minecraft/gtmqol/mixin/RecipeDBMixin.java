package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.mojang.datafixers.util.Either;
import com.yiran.minecraft.gtmqol.mixin_helper.RecipeDBStatic;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Predicate;

@Mixin(value = RecipeDB.class, remap = false)
public abstract class RecipeDBMixin {

    @Inject(method = "fromHolder", at = @At("RETURN"))
    private void captureDistinctStatus(IRecipeCapabilityHolder holder, CallbackInfoReturnable<List<List<AbstractMapIngredient>>> cir) {
        List<List<AbstractMapIngredient>> resultList = cir.getReturnValue();
        if (resultList == null) return;

        Map<RecipeCapability<?>, List<IRecipeHandler<?>>> handlerMap =
                holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());

        boolean[] flags = new boolean[resultList.size()];
        int[] busIds = new int[resultList.size()];

        int cursor = 0;
        Map<MetaMachine, Integer> machineToIdMap = new HashMap<>();
        int nextBusId = 0;

        for (var entry : handlerMap.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            if (!cap.isRecipeSearchFilter()) continue;

            for (IRecipeHandler<?> handler : entry.getValue()) {
                int assignedBusId;
                boolean isDistinct = false;

                if (handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                    var machine = trait.getMachine();
                    if (!machineToIdMap.containsKey(machine)) {
                        machineToIdMap.put(machine, nextBusId++);
                    }
                    assignedBusId = machineToIdMap.get(machine);
                    isDistinct = trait.isDistinct();
                } else {
                    assignedBusId = nextBusId++;
                }

                int count = cap.compressIngredients(handler.getContents()).size();
                for (int i = 0; i < count; i++) {
                    if (cursor < flags.length) {
                        flags[cursor] = isDistinct;
                        busIds[cursor] = assignedBusId;
                        cursor++;
                    }
                }
            }
        }
        RecipeDBStatic.DISTINCT_FLAGS.set(flags);
        RecipeDBStatic.BUS_IDS.set(busIds);
    }

    @Mixin(value = RecipeDB.RecipeIterator.class, remap = false)
    public static abstract class RecipeIteratorMixin {
        @Shadow
        @Final
        private List<List<AbstractMapIngredient>> ingredients;
        @Shadow @Final private Predicate<GTRecipe> predicate;
        @Shadow @Final private Deque<Object> stack;

        @Unique
        private boolean[] isDistinct;
        @Unique private int[] busIds;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void onInit(RecipeDB db, List ingredients, Predicate predicate, CallbackInfo ci) {
            boolean[] capturedFlags = RecipeDBStatic.DISTINCT_FLAGS.get();
            int[] capturedBusIds = RecipeDBStatic.BUS_IDS.get();

            this.isDistinct = (capturedFlags != null) ? capturedFlags : new boolean[ingredients.size()];
            this.busIds = (capturedBusIds != null) ? capturedBusIds : new int[ingredients.size()];

            RecipeDBStatic.DISTINCT_FLAGS.remove();
            RecipeDBStatic.BUS_IDS.remove();
        }

        @Overwrite
        public GTRecipe next() {
            try {
                while (!this.stack.isEmpty()) {
                    Object frame = this.stack.peek();
                    int curIndex = RecipeDBStatic.fIndex.getInt(frame);
                    int curIngredientIdx = RecipeDBStatic.fIngredientIndex.getInt(frame);
                    Object curBranch = RecipeDBStatic.fBranch.get(frame);

                    if (curIngredientIdx >= this.ingredients.get(curIndex).size()) {
                        this.stack.pop();
                        continue;
                    }

                    List<AbstractMapIngredient> ingredientList = this.ingredients.get(curIndex);
                    AbstractMapIngredient ingredient = ingredientList.get(curIngredientIdx);

                    RecipeDBStatic.fIngredientIndex.setInt(frame, curIngredientIdx + 1);

                    @SuppressWarnings("unchecked")
                    Map<AbstractMapIngredient, Either<GTRecipe, ?>> nodes =
                            (Map<AbstractMapIngredient, Either<GTRecipe, ?>>) RecipeDBStatic.nodesForIngredientMethod.invoke(null, ingredient, curBranch);

                    Either<GTRecipe, ?> result = nodes.get(ingredient);

                    if (result != null) {
                        if (result.left().isPresent()) {
                            GTRecipe recipe = (GTRecipe) result.left().get();
                            if (this.predicate.test(recipe)) return recipe;
                        }

                        if (result.right().isPresent()) {
                            Object nextBranch = result.right().get();

                            int currentBusId = this.busIds[curIndex];
                            boolean currentIsDistinct = this.isDistinct[curIndex];

                            for (int j = this.ingredients.size() - 1; j >= 0; j--) {
                                if (currentIsDistinct) {
                                    if (this.busIds[j] == currentBusId) {
                                        this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(j, nextBranch));
                                    }
                                } else {
                                    if (!this.isDistinct[j]) {
                                        this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(j, nextBranch));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }
}