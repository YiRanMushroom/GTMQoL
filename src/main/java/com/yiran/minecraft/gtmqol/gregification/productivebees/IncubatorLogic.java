package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.item.HoneyTreat;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.util.BeeCreator;
import cy.jdkdigital.productivebees.util.GeneAttribute;
import cy.jdkdigital.productivebees.util.GeneGroup;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification.*;

/**
 * Like PB's incubator: an egg and a honey treat with a type gene give that bee's spawn egg, with the gene's purity as
 * the chance; a caged baby bee and plain honey treats give the grown-up bee.
 */
enum IncubatorLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final int DURATION = 12000;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        int treatUse = config(ProductiveBeesConfig.GENERAL.incubatorTreatUse);
        for (List<ItemStack> items : inventories(holder)) {
            if (items.stream().anyMatch(stack -> stack.is(Tags.Items.EGGS))) {
                for (ItemStack stack : items) {
                    if (!stack.is(ModItems.HONEY_TREAT.get()) || !HoneyTreat.hasBeeType(stack)) continue;
                    GTRecipe recipe = eggRecipe(stack);
                    if (recipe != null) return recipe;
                }
            }
            int treats = items.stream().filter(IncubatorLogic::isPlainTreat).mapToInt(ItemStack::getCount).sum();
            if (treats < treatUse) continue;
            for (ItemStack stack : items) {
                if (isBaby(stack)) return cageRecipe(stack, treatUse);
            }
        }
        return null;
    }

    private static @Nullable GTRecipe eggRecipe(ItemStack treat) {
        for (GeneGroup gene : HoneyTreat.getGenes(treat)) {
            if (gene.attribute() != GeneAttribute.TYPE) continue;
            ResourceLocation type = ResourceLocation.tryParse(gene.value());
            if (type == null) continue;
            ItemStack egg = BeeCreator.getSpawnEgg(type);
            if (!(egg.getItem() instanceof SpawnEggItem)) continue;
            var builder = recipeType(INCUBATOR)
                    .recipeBuilder(GTMQoL.id("egg/" + path(type.toString())))
                    .inputItems(Tags.Items.EGGS, 1)
                    .inputItems(treat.copyWithCount(1));
            // PB rolls nextInt(100) <= purity
            int chance = chance(Math.min(100, gene.purity() + 1) / 100.0);
            if (chance >= ChanceLogic.getMaxChancedValue()) {
                builder.outputItems(egg);
            } else {
                builder.chancedOutput(RecipeHelper.makeSizedIngredient(egg), chance);
            }
            return builder.EUt(VA[ULV]).duration(DURATION).build();
        }
        return null;
    }

    private static GTRecipe cageRecipe(ItemStack cage, int treatUse) {
        CompoundTag tag = cage.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.putInt("Age", 0);
        ItemStack adult = cage.copyWithCount(1);
        adult.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return recipeType(INCUBATOR)
                .recipeBuilder(GTMQoL.id("grow/" + path(BeeCage.getBeeType(cage))))
                .inputItems(cage.copyWithCount(1))
                .inputItems(DataComponentIngredient.of(true, new ItemStack(ModItems.HONEY_TREAT.get())), treatUse)
                .outputItems(adult)
                .EUt(VA[ULV]).duration(DURATION)
                .build();
    }

    /** Treats with genes apply them in PB's incubator; that isn't done here. */
    private static boolean isPlainTreat(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, new ItemStack(ModItems.HONEY_TREAT.get()));
    }

    private static boolean isBaby(ItemStack stack) {
        if (!BeeCage.isFilled(stack)) return false;
        return stack.get(DataComponents.CUSTOM_DATA).copyTag().getInt("Age") < 0;
    }

    @Override
    public void buildRepresentativeRecipes() {
        GTRecipeType type = recipeType(INCUBATOR);
        for (var bee : bees()) {
            GTRecipe recipe = eggRecipe(HoneyTreat.getTypeStack(bee.getBeeType().toString(), 100));
            if (recipe != null) addRepresentative(type, recipe);
        }
        Level level = level();
        if (level == null) return;
        Bee baby = EntityType.BEE.create(level);
        if (baby == null) return;
        baby.setAge(-24000);
        addRepresentative(type, cageRecipe(cage(baby), config(ProductiveBeesConfig.GENERAL.incubatorTreatUse)));
    }
}
