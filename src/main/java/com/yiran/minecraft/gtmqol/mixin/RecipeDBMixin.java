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
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.mojang.datafixers.util.Either;
import com.yiran.minecraft.gtmqol.mixin_helper.RecipeDBStatic;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Predicate;

@Mixin(value = RecipeDB.class, remap = false)
public abstract class RecipeDBMixin {

    @Overwrite
    private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        var handlerMap = holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());
        if (handlerMap.isEmpty()) return null;

        List<List<AbstractMapIngredient>> resultList = new ObjectArrayList<>(handlerMap.size() * 8);

        List<MetaMachine> indexToMachineList = new ArrayList<>();
        Map<MetaMachine, List<Integer>> machineToIndices = new IdentityHashMap<>();

        Set<MetaMachine> distinctMachines = Collections.newSetFromMap(new IdentityHashMap<>());
        MetaMachine fallbackMachine = holder instanceof MetaMachine mm ? mm : null;

        int currentIndex = 0;
        for (var entry : handlerMap.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            if (!cap.isRecipeSearchFilter()) continue;

            for (IRecipeHandler<?> handler : entry.getValue()) {
                MetaMachine machine = fallbackMachine;

                if (handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                    machine = trait.getMachine();
                    if (machine != null && trait.isDistinct()) {
                        distinctMachines.add(machine);
                    }
                }

                if (machine != null) {
                    machineToIndices.putIfAbsent(machine, new ArrayList<>());
                }

                var compressed = cap.compressIngredients(handler.getContents());
                for (Object ingredient : compressed) {
                    resultList.add(MapIngredientTypeManager.getFrom(ingredient, cap));
                    indexToMachineList.add(machine);

                    if (machine != null) {
                        machineToIndices.get(machine).add(currentIndex);
                    }
                    currentIndex++;
                }
            }
        }

        if (resultList.isEmpty()) return null;

        MetaMachine[] machineArr = indexToMachineList.toArray(new MetaMachine[0]);

        Map<MetaMachine, int[]> finalMachineLookup = new IdentityHashMap<>();
        List<Integer> normalPoolList = new ArrayList<>();

        for (int i = 0; i < currentIndex; i++) {
            MetaMachine m = machineArr[i];
            if (m == null || !distinctMachines.contains(m)) {
                normalPoolList.add(i);
            }
        }

        for (MetaMachine m : distinctMachines) {
            if (machineToIndices.containsKey(m)) {
                finalMachineLookup.put(m, machineToIndices.get(m).stream().mapToInt(i -> i).toArray());
            }
        }

        int[] normalPoolArr = normalPoolList.stream().mapToInt(i -> i).toArray();

        RecipeDBStatic.DISTINCT_MACHINES.set(distinctMachines);
        RecipeDBStatic.INDEX_TO_MACHINE.set(machineArr);
        RecipeDBStatic.MACHINE_LOOKUP.set(finalMachineLookup);
        RecipeDBStatic.NORMAL_POOL.set(normalPoolArr);

        return resultList;
    }

    @Mixin(value = RecipeDB.RecipeIterator.class, remap = false)
    public static abstract class RecipeIteratorMixin {
        @Shadow @Final private List<List<AbstractMapIngredient>> ingredients;
        @Shadow @Final private Predicate<GTRecipe> predicate;
        @Shadow @Final private Deque<Object> stack;

        @Unique private Set<MetaMachine> distinctMachines;
        @Unique private MetaMachine[] indexToMachine;
        @Unique private Map<MetaMachine, int[]> machineLookup;
        @Unique private int[] normalPool;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void onInit(RecipeDB db, List ingredients, Predicate predicate, CallbackInfo ci) {
            this.distinctMachines = RecipeDBStatic.DISTINCT_MACHINES.get();
            this.indexToMachine = RecipeDBStatic.INDEX_TO_MACHINE.get();
            this.machineLookup = RecipeDBStatic.MACHINE_LOOKUP.get();
            this.normalPool = RecipeDBStatic.NORMAL_POOL.get();

            RecipeDBStatic.DISTINCT_MACHINES.remove();
            RecipeDBStatic.INDEX_TO_MACHINE.remove();
            RecipeDBStatic.MACHINE_LOOKUP.remove();
            RecipeDBStatic.NORMAL_POOL.remove();
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

                            MetaMachine curMachine = this.indexToMachine[curIndex];

                            boolean isMachineDistinct = curMachine != null && this.distinctMachines != null && this.distinctMachines.contains(curMachine);

                            if (isMachineDistinct) {
                                int[] targets = this.machineLookup.get(curMachine);
                                if (targets != null) {
                                    for (int i = targets.length - 1; i >= 0; i--) {
                                        this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(targets[i], nextBranch));
                                    }
                                }
                            } else {
                                if (this.normalPool != null) {
                                    for (int i = this.normalPool.length - 1; i >= 0; i--) {
                                        this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(this.normalPool[i], nextBranch));
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