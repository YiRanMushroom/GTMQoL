package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.CannerLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * gtceu's canner logic fills and drains any fluid container at runtime but has no representative recipes, so EMI shows
 * none. Add bottling and draining for every potion, like {@code BreweryLogic} does for brewing.
 * <p>
 * Also turns potions into splash potions and splash into lingering ones (vanilla's container mixes: gunpowder, dragon's
 * breath), keeping the contents. The brewery has no item output, so this is done here.
 */
@Mixin(value = CannerLogic.class, remap = false)
public class CannerLogicMixin {

    // before gtceu's logic, which would drain the potion instead
    @Inject(method = "createCustomRecipe", at = @At("HEAD"), cancellable = true)
    private void gtmqol$containerMix(IRecipeCapabilityHolder holder, CallbackInfoReturnable<GTRecipe> cir) {
        PotionBrewing brewing = gtmqol$brewing();
        if (brewing == null) return;
        List<PotionBrewing.Mix<Item>> mixes = ((PotionBrewingContainerMixesAccessor) brewing)
                .gtmqol$getContainerMixes();
        List<ItemStack> notDistinct = new ArrayList<>();
        for (RecipeHandlerList handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            List<ItemStack> items = handlerList.isDistinct() ? new ArrayList<>() : notDistinct;
            for (IRecipeHandler<?> handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) items.add(stack);
                }
            }
            if (handlerList.isDistinct()) {
                GTRecipe recipe = gtmqol$containerMix(mixes, items);
                if (recipe != null) {
                    cir.setReturnValue(recipe);
                    return;
                }
            }
        }
        GTRecipe recipe = gtmqol$containerMix(mixes, notDistinct);
        if (recipe != null) cir.setReturnValue(recipe);
    }

    private static @Nullable GTRecipe gtmqol$containerMix(List<PotionBrewing.Mix<Item>> mixes, List<ItemStack> items) {
        for (PotionBrewing.Mix<Item> mix : mixes) {
            for (ItemStack potion : items) {
                if (!potion.is(mix.from()) || !potion.has(DataComponents.POTION_CONTENTS)) continue;
                for (ItemStack reagent : items) {
                    if (mix.ingredient().test(reagent)) {
                        return gtmqol$containerMix("container_mix", potion.copyWithCount(1), reagent, mix)
                                .build();
                    }
                }
            }
        }
        return null;
    }

    private static GTRecipeBuilder gtmqol$containerMix(String id, ItemStack potion, ItemStack reagent,
                                                      PotionBrewing.Mix<Item> mix) {
        GTRecipeBuilder builder = GTRecipeTypes.CANNER_RECIPES.recipeBuilder(id)
                .inputItems(potion)
                .inputItems(mix.ingredient())
                .outputItems(potion.transmuteCopy(mix.to().value(), 1))
                .duration(16)
                .EUt(4);
        // dragon's breath leaves its bottle
        ItemStack remainder = reagent.getCraftingRemainingItem();
        if (!remainder.isEmpty()) builder.outputItems(remainder);
        return builder;
    }

    public void buildRepresentativeRecipes() {
        PotionBrewing brewing = gtmqol$brewing();
        List<PotionBrewing.Mix<Item>> mixes = brewing == null ? List.of() :
                ((PotionBrewingContainerMixesAccessor) brewing).gtmqol$getContainerMixes();
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            ResourceLocation id = potion.key().location();
            String name = id.getNamespace() + "_" + id.getPath();
            ItemStack bottle = PotionContents.createItemStack(Items.POTION, potion);
            FluidStack fluid = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);

            // a glass bottle only takes potion fluids, so water can't be bottled here
            if (potion != Potions.WATER) {
                gtmqol$add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("fill_potion_" + name)
                        .inputItems(new ItemStack(Items.GLASS_BOTTLE))
                        .inputFluids(fluid)
                        .outputItems(bottle)
                        .duration(16)
                        .EUt(4)
                        .build());
            }
            gtmqol$add(GTRecipeTypes.CANNER_RECIPES.recipeBuilder("drain_potion_" + name)
                    .inputItems(bottle)
                    .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                    .outputFluids(fluid)
                    .duration(16)
                    .EUt(4)
                    .build());

            for (PotionBrewing.Mix<Item> mix : mixes) {
                ItemStack[] reagents = mix.ingredient().getItems();
                if (reagents.length == 0) continue;
                String to = BuiltInRegistries.ITEM.getKey(mix.to().value()).getPath();
                gtmqol$add(gtmqol$containerMix(to + "_" + name, PotionContents.createItemStack(mix.from().value(),
                        potion), reagents[0], mix).build());
            }
        });
    }

    private static void gtmqol$add(GTRecipe recipe) {
        // for EMI to detect it's a synthetic recipe (not ever in JSON)
        recipe.setId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.CANNER_RECIPES.addToMainCategory(recipe);
    }

    /** The server's, or the client's when the recipe viewer loads on a remote server. */
    private static @Nullable PotionBrewing gtmqol$brewing() {
        MinecraftServer server = GTCEu.getMinecraftServer();
        if (server != null) return server.potionBrewing();
        Level level = GregificationClient.level();
        return level == null ? null : level.potionBrewing();
    }
}
