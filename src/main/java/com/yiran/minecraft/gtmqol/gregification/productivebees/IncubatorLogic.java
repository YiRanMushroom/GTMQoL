package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.item.Gene;
import cy.jdkdigital.productivebees.common.item.HoneyTreat;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.util.BeeCreator;
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
        for (Tag entry : HoneyTreat.getGenes(treat)) {
            if (!(entry instanceof CompoundTag geneTag)) continue;
            ItemStack gene = ItemStack.of(geneTag);
            // a type gene: its attribute is the bee type, not one of the attributes
            if (Gene.getAttribute(gene) != null) continue;
            String type = Gene.getAttributeName(gene);
            if (type.isEmpty()) continue;
            ItemStack egg = BeeCreator.getSpawnEgg(type);
            if (!(egg.getItem() instanceof SpawnEggItem)) continue;
            // like HoneyTreat.applyGenesToBee
            int purity = geneTag.contains("purity") ? geneTag.getInt("purity") : Gene.getPurity(gene);
            var builder = recipeType(INCUBATOR)
                    .recipeBuilder(GTMQoL.id("egg/" + path(type)))
                    .inputItems(Tags.Items.EGGS, 1)
                    .inputItems(treat.copyWithCount(1));
            // PB rolls nextInt(100) <= purity
            int chance = chance(Math.min(100, purity + 1) / 100.0);
            if (chance >= ChanceLogic.getMaxChancedValue()) {
                builder.outputItems(egg);
            } else {
                builder.chancedOutput(egg, chance);
            }
            return builder.EUt(VA[ULV]).duration(DURATION).buildRawRecipe();
        }
        return null;
    }

    private static GTRecipe cageRecipe(ItemStack cage, int treatUse) {
        ItemStack adult = cage.copyWithCount(1);
        adult.getOrCreateTag().putInt("Age", 0);
        return recipeType(INCUBATOR)
                .recipeBuilder(GTMQoL.id("grow/" + path(BeeCage.getBeeType(cage))))
                .inputItems(cage.copyWithCount(1))
                .inputItems(StrictNBTIngredient.of(new ItemStack(ModItems.HONEY_TREAT.get())), treatUse)
                .outputItems(adult)
                .EUt(VA[ULV]).duration(DURATION)
                .buildRawRecipe();
    }

    /** Treats with genes apply them in PB's incubator; that isn't done here. */
    private static boolean isPlainTreat(ItemStack stack) {
        return ItemStack.isSameItemSameTags(stack, new ItemStack(ModItems.HONEY_TREAT.get()));
    }

    private static boolean isBaby(ItemStack stack) {
        return BeeCage.isFilled(stack) && stack.getTag().getInt("Age") < 0;
    }

    @Override
    public void buildRepresentativeRecipes() {
        GTRecipeType type = recipeType(INCUBATOR);
        for (var bee : bees()) {
            GTRecipe recipe = eggRecipe(HoneyTreat.getTypeStack(bee.getBeeType().toString(), 100));
            if (recipe != null) addRepresentative(type, recipe);
        }
        onLevelThread(level -> {
            Bee baby = EntityType.BEE.create(level);
            if (baby == null) return;
            baby.setAge(-24000);
            addRepresentative(type, cageRecipe(cage(baby), config(ProductiveBeesConfig.GENERAL.incubatorTreatUse)));
        });
    }
}
