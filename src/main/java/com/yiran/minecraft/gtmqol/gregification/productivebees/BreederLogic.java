package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient;
import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredientFactory;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBee;
import cy.jdkdigital.productivebees.common.entity.bee.IProductiveBee;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.recipe.BeeBreedingRecipe;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.util.BeeHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification.*;

/**
 * Like PB's breeding chamber: two caged bees and their breeding items give a caged baby bee, in an empty cage. The
 * parents are never used up. When the two bees have several possible offspring, one of them comes out at random.
 */
enum BreederLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        if (!config(ProductiveBeesConfig.BEES.allowBeeBreeding)) return null;
        Level level = level();
        if (level == null) return null;
        for (List<ItemStack> items : inventories(holder)) {
            ItemStack emptyCage = items.stream()
                    .filter(stack -> stack.getItem() instanceof BeeCage && !BeeCage.isFilled(stack))
                    .findFirst().orElse(null);
            if (emptyCage == null) continue;
            List<ItemStack> parents = items.stream().filter(BreederLogic::isAdult).toList();
            for (int i = 0; i < parents.size(); i++) {
                for (int j = i; j < parents.size(); j++) {
                    ItemStack cage1 = parents.get(i);
                    ItemStack cage2 = parents.get(j);
                    if (i == j && cage1.getCount() < 2) continue;
                    var offspring = offspring(BeeCage.getBeeType(cage1), BeeCage.getBeeType(cage2), level);
                    if (offspring.isEmpty()) continue;
                    GTRecipe recipe = recipe(cage1, cage2, emptyCage, offspring, items, level);
                    if (recipe != null) return recipe;
                }
            }
        }
        return null;
    }

    /**
     * @param items what the breeding items have to be found in, or null to not check (for the recipe viewer)
     * @return null if a bee can't be had or the breeding items aren't there
     */
    private static @Nullable GTRecipe recipe(ItemStack cage1, ItemStack cage2, ItemStack emptyCage,
                                             List<BeeIngredient> offspring, @Nullable List<ItemStack> items,
                                             Level level) {
        Bee parent1 = BeeCage.getEntityFromStack(cage1, level, true);
        Bee parent2 = BeeCage.getEntityFromStack(cage2, level, true);
        if (parent1 == null || parent2 == null) return null;
        String type1 = BeeCage.getBeeType(cage1);
        String type2 = BeeCage.getBeeType(cage2);
        var builder = recipeType(BREEDER)
                .recipeBuilder(GTMQoL.id(path(type1) + "/" + path(type2)));
        // always five inputs in this order, which BreederUI lays out as cages and breeding items
        builder.notConsumable(cage1.copyWithCount(1)).notConsumable(cage2.copyWithCount(1));
        // strict, so a filled cage isn't taken for it
        builder.inputItems(DataComponentIngredient.of(true, new ItemStack(emptyCage.getItem())), 1);
        // like the chamber: each parent eats its own breeding items
        Ingredient food1 = breedingIngredient(parent1);
        Ingredient food2 = breedingIngredient(parent2);
        int count1 = breedingItemCount(parent1);
        int count2 = breedingItemCount(parent2);
        if (type1.equals(type2) ? !has(items, food1, count1 + count2) :
                !has(items, food1, count1) || !has(items, food2, count2)) {
            return null;
        }
        builder.inputItems(food1, count1).inputItems(food2, count2);

        List<ItemStack> babies = new ArrayList<>();
        for (BeeIngredient bee : offspring) {
            ItemStack baby = baby(bee, parent1, parent2, emptyCage, level);
            if (baby != null) babies.add(baby);
        }
        if (babies.isEmpty()) return null;
        if (babies.size() == 1) {
            builder.outputItems(babies.getFirst());
        } else {
            int chance = ChanceLogic.getMaxChancedValue() / babies.size();
            babies.forEach(baby -> builder.chancedOutput(RecipeHelper.makeSizedIngredient(baby), chance));
            builder.chancedItemOutputLogic(ChanceLogic.XOR.get());
        }
        return builder.EUt(VA[LV]).duration(config(ProductiveBeesConfig.GENERAL.breedingChamberProcessingTime))
                .build();
    }

    /** Like the chamber's offspring: the type's default genes, improved by the parents' */
    private static @Nullable ItemStack baby(BeeIngredient type, Bee parent1, Bee parent2, ItemStack emptyCage,
                                            Level level) {
        if (!(type.getBeeEntity().create(level) instanceof Bee bee)) return null;
        if (bee instanceof ConfigurableBee configurable) {
            configurable.setBeeType(type.getBeeType().toString());
            configurable.setDefaultAttributes();
        }
        BeeHelper.setOffspringAttributes(bee, parent1, parent2);
        bee.setAge(-24000);
        ItemStack cage = new ItemStack(emptyCage.getItem());
        BeeCage.captureEntity(bee, cage);
        return cage;
    }

    /** Like {@link BeeHelper#getBreedingRecipes}, which needs a server level. */
    private static List<BeeIngredient> offspring(String type1, String type2, Level level) {
        List<BeeIngredient> offspring = new ArrayList<>();
        var parents = new BeeHelper.IdentifierInventory(type1, type2);
        for (RecipeHolder<BeeBreedingRecipe> recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.BEE_BREEDING_TYPE.get())) {
            if (!recipe.value().matches(parents, level)) continue;
            BeeIngredient bee = recipe.value().offspring.get();
            if (bee != null) offspring.add(bee);
        }
        if (type1.equals(type2)) {
            var self = BeeIngredientFactory.getIngredient(type1);
            BeeIngredient bee = self != null ? self.get() : null;
            if (bee != null && canSelfBreed(bee, level)) offspring.add(bee);
        }
        return offspring;
    }

    private static boolean canSelfBreed(BeeIngredient bee, Level level) {
        return !(bee.getCachedEntity(level) instanceof IProductiveBee productive) || productive.canSelfBreed();
    }

    private static Ingredient breedingIngredient(Bee bee) {
        return bee instanceof IProductiveBee productive ? productive.getBreedingIngredient() :
                Ingredient.of(ModTags.DEFAULT_BREEDING);
    }

    private static int breedingItemCount(Bee bee) {
        return bee instanceof IProductiveBee productive ? Math.max(1, productive.getBreedingItemCount()) : 1;
    }

    private static boolean has(@Nullable List<ItemStack> items, Ingredient ingredient, int count) {
        return items == null ||
                items.stream().filter(ingredient).mapToInt(ItemStack::getCount).sum() >= count;
    }

    /** PB's chamber won't take babies. */
    private static boolean isAdult(ItemStack cage) {
        if (!BeeCage.isFilled(cage)) return false;
        return cage.get(DataComponents.CUSTOM_DATA).copyTag().getInt("Age") >= 0;
    }

    @Override
    public void buildRepresentativeRecipes() {
        Level level = levelWithBees();
        if (level == null) return;
        GTRecipeType type = recipeType(BREEDER);
        ItemStack emptyCage = new ItemStack(ModItems.BEE_CAGE.get());
        // one recipe per pair of parents, with all their offspring, like the machine makes them
        Map<String, BeeIngredient> bees = new HashMap<>();
        Set<List<String>> pairs = new LinkedHashSet<>();
        for (RecipeHolder<BeeBreedingRecipe> breeding : level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.BEE_BREEDING_TYPE.get())) {
            BeeIngredient parent1 = breeding.value().parent1.get();
            BeeIngredient parent2 = breeding.value().parent2.get();
            if (parent1 == null || parent2 == null) continue;
            pairs.add(pair(parent1, parent2, bees));
        }
        for (BeeIngredient bee : bees()) {
            if (canSelfBreed(bee, level)) pairs.add(pair(bee, bee, bees));
        }
        for (List<String> pair : pairs) {
            ItemStack cage1 = cage(bees.get(pair.get(0)), level);
            ItemStack cage2 = cage(bees.get(pair.get(1)), level);
            if (cage1 == null || cage2 == null) continue;
            var offspring = offspring(pair.get(0), pair.get(1), level);
            if (offspring.isEmpty()) continue;
            GTRecipe recipe = recipe(cage1, cage2, emptyCage, offspring, null, level);
            if (recipe != null) addRepresentative(type, recipe);
        }
    }

    private static List<String> pair(BeeIngredient bee1, BeeIngredient bee2, Map<String, BeeIngredient> bees) {
        String type1 = bee1.getBeeType().toString();
        String type2 = bee2.getBeeType().toString();
        bees.put(type1, bee1);
        bees.put(type2, bee2);
        return type1.compareTo(type2) <= 0 ? List.of(type1, type2) : List.of(type2, type1);
    }
}
