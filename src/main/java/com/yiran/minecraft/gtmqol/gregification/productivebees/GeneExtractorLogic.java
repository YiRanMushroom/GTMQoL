package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.item.Gene;
import cy.jdkdigital.productivebees.util.GeneAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification.*;

/** A caged bee gives each of its genes at full purity, and the empty cage. */
enum GeneExtractorLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        Level level = level();
        if (level == null) return null;
        for (List<ItemStack> items : inventories(holder)) {
            for (ItemStack stack : items) {
                if (!BeeCage.isFilled(stack)) continue;
                GTRecipe recipe = recipe(stack, level);
                if (recipe != null) return recipe;
            }
        }
        return null;
    }

    private static @Nullable GTRecipe recipe(ItemStack cage, Level level) {
        Bee bee = BeeCage.getEntityFromStack(cage, level, true);
        if (bee == null) return null;
        // like PB's gene bottle
        String type = bee instanceof ProductiveBee productiveBee ? productiveBee.getBeeType().toString() :
                bee.getEncodeId();
        if (type == null) return null;
        var builder = recipeType(GENE_EXTRACTOR).recipeBuilder(GTMQoL.id(path(type)))
                .inputItems(cage.copyWithCount(1))
                .outputItems(new ItemStack(cage.getItem()))
                .outputItems(Gene.getStack(type, 100));
        var attributes = bee.getData(ProductiveBees.ATTRIBUTE_HANDLER);
        for (GeneAttribute attribute : GeneAttribute.values()) {
            if (attribute == GeneAttribute.TYPE) continue;
            builder.outputItems(Gene.getStack(attribute, attributes.getAttributeValue(attribute), 1, 100));
        }
        return builder.EUt(VA[LV]).duration(600).build();
    }

    @Override
    public void buildRepresentativeRecipes() {
        Level level = level();
        if (level == null) return;
        GTRecipeType type = recipeType(GENE_EXTRACTOR);
        for (var bee : bees()) {
            ItemStack cage = cage(bee, level);
            if (cage == null) continue;
            GTRecipe recipe = recipe(cage, level);
            if (recipe != null) addRepresentative(type, recipe);
        }
    }
}
