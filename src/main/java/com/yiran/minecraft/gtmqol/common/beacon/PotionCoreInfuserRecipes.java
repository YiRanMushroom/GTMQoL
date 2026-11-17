package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.V;

/**
 * The Potion Core Infuser's recipe viewer page: how much of each potion, plain and lingering, makes a Potion Beacon
 * Core. Only shown; the infuser doesn't run recipes.
 */
// Untested: EMI shows a Potion Core Infuser page with each potion, plain and lingering, and the amount that makes a
// core (e.g. 120,000 mB of Swiftness 3:00); the infuser is its workstation; a core made from exactly 24 h frees the
// infuser for another effect
public enum PotionCoreInfuserRecipes implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            if (potion == Potions.WATER) return;
            ResourceLocation id = potion.key().location();
            FluidStack bottle = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);
            for (Item container : List.of(Items.POTION, Items.LINGERING_POTION)) {
                FluidStack fluid = PotionContainers.withContainer(bottle, container);
                MobEffectInstance effect = PotionCoreInfuserMachine.effectOf(fluid);
                long perBottle = PotionCoreInfuserMachine.ticksOf(fluid);
                if (effect == null || perBottle <= 0) continue;
                long amount = LongMath.divide(PotionCoreInfuserMachine.CORE_TICKS * PotionFluidHelper.BOTTLE_AMOUNT,
                        perBottle, RoundingMode.CEILING);
                String name = (container == Items.LINGERING_POTION ? "lingering_" : "") + id.getNamespace() + "_" +
                        id.getPath();
                GTRecipe recipe = PersonalBeacon.INFUSER_RECIPES.recipeBuilder(name)
                        .inputFluids(fluid.copyWithAmount((int) amount))
                        .outputItems(BeaconCoreItem.potionCore(effect.getEffect()))
                        // the whole tank goes in each tick
                        .duration((int) LongMath.divide(amount, PotionCoreInfuserMachine.TANK_CAPACITY,
                                RoundingMode.CEILING))
                        .EUt(V[EV])
                        .build();
                // for EMI to detect it's a synthetic recipe (not ever in JSON)
                recipe.setId(recipe.getId().withPrefix("/"));
                PersonalBeacon.INFUSER_RECIPES.addToMainCategory(recipe);
            }
        });
    }
}
