package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.util.BeeCreator;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification.*;

/**
 * gtceu's canner: a caged bee becomes its spawn egg and the empty cage, and a bee spawn egg goes back into an empty
 * cage. The egg carries the bee's data like a cage does, so genes and name survive the round trip.
 */
enum CannerBeeLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final int DURATION = 100;
    /** What the cage stores besides the bee's own data. */
    private static final List<String> CAGE_KEYS = List.of("entity", "name", "HivePos", "isProductiveBee", "mod");

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        for (List<ItemStack> items : inventories(holder)) {
            for (ItemStack stack : items) {
                if (!BeeCage.isFilled(stack)) continue;
                GTRecipe recipe = releaseRecipe(stack);
                if (recipe != null) return recipe;
            }
            ItemStack cage = items.stream()
                    .filter(stack -> stack.getItem() instanceof BeeCage && !BeeCage.isFilled(stack))
                    .findFirst().orElse(null);
            if (cage == null) continue;
            Level level = level();
            if (level == null) return null;
            for (ItemStack stack : items) {
                if (!(stack.getItem() instanceof SpawnEggItem)) continue;
                GTRecipe recipe = catchRecipe(stack, cage, level);
                if (recipe != null) return recipe;
            }
        }
        return null;
    }

    private static @Nullable GTRecipe releaseRecipe(ItemStack cage) {
        ResourceLocation type = ResourceLocation.tryParse(BeeCage.getBeeType(cage));
        if (type == null) return null;
        ItemStack egg = BeeCreator.getSpawnEgg(type);
        if (!(egg.getItem() instanceof SpawnEggItem)) return null;
        CompoundTag tag = cage.get(DataComponents.CUSTOM_DATA).copyTag();
        String entity = tag.getString("entity");
        CAGE_KEYS.forEach(tag::remove);
        tag.putString("id", entity);
        egg.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
        return GTRecipeTypes.CANNER_RECIPES.recipeBuilder(GTMQoL.id("release/" + path(type.toString())))
                .inputItems(cage.copyWithCount(1))
                .outputItems(new ItemStack(cage.getItem()))
                .outputItems(egg)
                .EUt(VA[ULV]).duration(DURATION)
                .build();
    }

    private static @Nullable GTRecipe catchRecipe(ItemStack egg, ItemStack cage, Level level) {
        // like spawning it: the egg's entity data is loaded over a fresh bee
        if (!(((SpawnEggItem) egg.getItem()).getType(egg).create(level) instanceof Bee bee)) return null;
        CustomData data = egg.get(DataComponents.ENTITY_DATA);
        if (data != null) data.loadInto(bee);
        ItemStack filled = new ItemStack(cage.getItem());
        BeeCage.captureEntity(bee, filled);
        return GTRecipeTypes.CANNER_RECIPES.recipeBuilder(GTMQoL.id("catch/" + path(BeeCage.getBeeType(filled))))
                .inputItems(egg.copyWithCount(1))
                .inputItems(cage.copyWithCount(1))
                .outputItems(filled)
                .EUt(VA[ULV]).duration(DURATION)
                .build();
    }

    @Override
    public void buildRepresentativeRecipes() {
        Level level = levelWithBees();
        if (level == null) return;
        GTRecipeType type = GTRecipeTypes.CANNER_RECIPES.get();
        ItemStack emptyCage = new ItemStack(ModItems.BEE_CAGE.get());
        for (var bee : bees()) {
            ItemStack cage = cage(bee, level);
            if (cage == null) continue;
            GTRecipe release = releaseRecipe(cage);
            if (release != null) addRepresentative(type, release);
            ItemStack egg = BeeCreator.getSpawnEgg(bee.getBeeType());
            if (!(egg.getItem() instanceof SpawnEggItem)) continue;
            GTRecipe recipe = catchRecipe(egg, emptyCage, level);
            if (recipe != null) addRepresentative(type, recipe);
        }
    }
}
