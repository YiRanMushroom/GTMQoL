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
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidTagMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.mojang.datafixers.util.Either;
import com.yiran.minecraft.gtmqol.api.ISlotHint;
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

    @Unique
    private static boolean qol$isFluidOrItemAbstractMapIngredient(AbstractMapIngredient ingredient) {
        return ingredient instanceof CustomMapIngredient || ingredient instanceof FluidStackMapIngredient
                || ingredient instanceof FluidTagMapIngredient || ingredient instanceof IntersectionMapIngredient
                || ingredient instanceof ItemStackMapIngredient || ingredient instanceof ItemTagMapIngredient
                || ingredient instanceof NBTPredicateItemStackMapIngredient || ingredient instanceof StrictNBTItemStackMapIngredient
                || ingredient instanceof PartialNBTItemStackMapIngredient;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        var handlerMap = holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());
        if (handlerMap.isEmpty()) return null;

        List<List<AbstractMapIngredient>> resultList = new ObjectArrayList<>(handlerMap.size() * 8);

        List<IRecipeHandler<?>> handlers = new ArrayList<>();
        List<MetaMachine> machines = new ArrayList<>();
        List<Boolean> isDistincts = new ArrayList<>();
        List<Boolean> isCatalysts = new ArrayList<>();
        List<Boolean> isPhysicals = new ArrayList<>();

        MetaMachine fallbackMachine = holder instanceof MetaMachine mm ? mm : null;

        int currentIndex = 0;
        for (var entry : handlerMap.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            if (!cap.isRecipeSearchFilter()) continue;

            for (IRecipeHandler<?> handler : entry.getValue()) {
                MetaMachine machine = fallbackMachine;
                boolean isDistinct = false;
                boolean isCatalyst = false;

                if (handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                    var hint = (ISlotHint) trait;
                    machine = trait.getMachine();
                    isCatalyst = hint.qol$isCatalystSlot();

                    isDistinct = isCatalyst || trait.isDistinct();
                }

                var compressed = cap.compressIngredients(handler.getContents());
                for (Object ingredient : compressed) {
                    List<AbstractMapIngredient> mapIngs = MapIngredientTypeManager.getFrom(ingredient, cap);
                    resultList.add(mapIngs);

                    handlers.add(handler);
                    machines.add(machine);
                    isDistincts.add(isDistinct);
                    isCatalysts.add(isCatalyst);

                    boolean isPhys = !mapIngs.isEmpty() && qol$isFluidOrItemAbstractMapIngredient(mapIngs.get(0));
                    isPhysicals.add(isPhys);

                    currentIndex++;
                }
            }
        }

        if (resultList.isEmpty()) return null;

        // 构建机器内的隔离映射关系
        Map<IRecipeHandler<?>, List<Integer>> handlerToPhysicals = new IdentityHashMap<>();
        Map<MetaMachine, List<Integer>> machineToCatalysts = new IdentityHashMap<>();
        Map<MetaMachine, List<Integer>> machineToAllPhysicalDistincts = new IdentityHashMap<>();
        List<Integer> normalPhysicalPool = new ArrayList<>();
        List<Integer> nonPhysicalPool = new ArrayList<>();

        for (int i = 0; i < currentIndex; i++) {
            if (!isPhysicals.get(i)) {
                nonPhysicalPool.add(i);
                continue;
            }

            if (!isDistincts.get(i)) {
                normalPhysicalPool.add(i);
            } else {
                MetaMachine m = machines.get(i);
                IRecipeHandler<?> h = handlers.get(i);

                if (h != null) {
                    handlerToPhysicals.computeIfAbsent(h, k -> new ArrayList<>()).add(i);
                }

                if (m != null) {
                    machineToAllPhysicalDistincts.computeIfAbsent(m, k -> new ArrayList<>()).add(i);
                    if (isCatalysts.get(i)) {
                        machineToCatalysts.computeIfAbsent(m, k -> new ArrayList<>()).add(i);
                    }
                }
            }
        }

        int[][] targetLookup = new int[currentIndex][];
        for (int i = 0; i < currentIndex; i++) {
            Set<Integer> allowed = new LinkedHashSet<>();

            allowed.addAll(normalPhysicalPool);

            MetaMachine m = machines.get(i);
            if (m != null && isDistincts.get(i)) {
                if (isCatalysts.get(i)) {
                    if (machineToAllPhysicalDistincts.containsKey(m)) {
                        allowed.addAll(machineToAllPhysicalDistincts.get(m));
                    }
                } else {
                    IRecipeHandler<?> h = handlers.get(i);
                    if (handlerToPhysicals.containsKey(h)) {
                        allowed.addAll(handlerToPhysicals.get(h));
                    }
                    if (machineToCatalysts.containsKey(m)) {
                        allowed.addAll(machineToCatalysts.get(m));
                    }
                }
            }

            targetLookup[i] = allowed.stream().mapToInt(x -> x).toArray();
        }

        int[] nonPhysicalTargets = nonPhysicalPool.stream().mapToInt(x -> x).toArray();

        RecipeDBStatic.TARGET_LOOKUP.set(targetLookup);
        RecipeDBStatic.NON_PHYSICAL_TARGETS.set(nonPhysicalTargets);

        return resultList;
    }

    @Mixin(value = RecipeDB.RecipeIterator.class, remap = false)
    public static abstract class RecipeIteratorMixin {
        @Shadow @Final private List<List<AbstractMapIngredient>> ingredients;
        @Shadow @Final private Predicate<GTRecipe> predicate;
        @Shadow @Final private Deque<Object> stack;

        @Unique private int[][] targetLookup;
        @Unique private int[] nonPhysicalTargets;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void onInit(RecipeDB db, List ingredients, Predicate predicate, CallbackInfo ci) {
            this.targetLookup = RecipeDBStatic.TARGET_LOOKUP.get();
            this.nonPhysicalTargets = RecipeDBStatic.NON_PHYSICAL_TARGETS.get();

            RecipeDBStatic.TARGET_LOOKUP.remove();
            RecipeDBStatic.NON_PHYSICAL_TARGETS.remove();
        }

        /**
         * @author
         * @reason
         */
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

                            for (int i = this.nonPhysicalTargets.length - 1; i >= 0; i--) {
                                this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(this.nonPhysicalTargets[i], nextBranch));
                            }

                            int[] physicalTargets = this.targetLookup[curIndex];
                            for (int i = physicalTargets.length - 1; i >= 0; i--) {
                                this.stack.push(RecipeDBStatic.searchFrameConstructor.newInstance(physicalTargets[i], nextBranch));
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