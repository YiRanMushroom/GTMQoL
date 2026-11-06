package com.yiran.minecraft.gtmqol.common.crafter;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;

import brachy.modularui.screen.ModularContainerMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;

/**
 * The machines are listed as stations of the three kinds of recipes, and JEI's transfer button puts such a recipe into
 * the encoding window. ModularUI's own transfer hook wants one exact recipe class, here recipes are of many classes.
 * Every ModularUI window has the same menu class, so the handler checks the screen.
 */
@JeiPlugin
public final class PatternCrafterJei implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return GTMQoL.id("pattern_crafter");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (PatternCrafter.MACHINES == null) return;
        for (int tier : ELECTRIC_TIERS) {
            var machine = PatternCrafter.MACHINES[tier].asStack();
            registration.addRecipeCatalyst(machine, RecipeTypes.CRAFTING, RecipeTypes.STONECUTTING,
                    RecipeTypes.SMITHING);
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (PatternCrafter.MACHINES == null) return;
        var helper = registration.getTransferHelper();
        registration.addRecipeTransferHandler(new Handler<>(RecipeTypes.CRAFTING, helper), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new Handler<>(RecipeTypes.STONECUTTING, helper),
                RecipeTypes.STONECUTTING);
        registration.addRecipeTransferHandler(new Handler<>(RecipeTypes.SMITHING, helper), RecipeTypes.SMITHING);
    }

    private record Handler<R extends Recipe<?>>(RecipeType<R> type, IRecipeTransferHandlerHelper helper)
            implements IRecipeTransferHandler<ModularContainerMenu, R> {

        @Override
        public Class<? extends ModularContainerMenu> getContainerClass() {
            return ModularContainerMenu.class;
        }

        @Override
        public Optional<MenuType<ModularContainerMenu>> getMenuType() {
            return Optional.empty();
        }

        @Override
        public RecipeType<R> getRecipeType() {
            return type;
        }

        @Override
        public @Nullable IRecipeTransferError transferRecipe(ModularContainerMenu container, R recipe,
                                                             IRecipeSlotsView slots, Player player,
                                                             boolean maxTransfer, boolean doTransfer) {
            if (!(container.getScreen() instanceof CraftingPatternScreen screen)) return helper.createInternalError();
            if (doTransfer) screen.fill(recipe);
            return null;
        }
    }
}
