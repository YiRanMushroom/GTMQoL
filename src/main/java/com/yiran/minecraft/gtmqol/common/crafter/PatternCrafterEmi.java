package com.yiran.minecraft.gtmqol.common.crafter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.crafting.Recipe;

import brachy.modularui.ModularUIMenuTypes;
import brachy.modularui.screen.ModularContainerMenu;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;

/**
 * The machines are listed as stations of the three kinds of recipes, and EMI's fill button puts such a recipe into the
 * encoding window.
 * EMI picks handlers by menu type and every ModularUI window has the same one, so the handler checks the screen.
 */
@EmiEntrypoint
public final class PatternCrafterEmi implements EmiPlugin, EmiRecipeHandler<ModularContainerMenu> {

    @Override
    public void register(EmiRegistry registry) {
        if (PatternCrafter.MACHINES == null) return;
        for (EmiRecipeCategory category : List.of(VanillaEmiRecipeCategories.CRAFTING,
                VanillaEmiRecipeCategories.STONECUTTING, VanillaEmiRecipeCategories.SMITHING)) {
            for (int tier : ELECTRIC_TIERS) {
                registry.addWorkstation(category, EmiStack.of(PatternCrafter.MACHINES[tier].asStack()));
            }
        }
        registry.addRecipeHandler(ModularUIMenuTypes.MODULAR_CONTAINER.get(), this);
    }

    private static @Nullable CraftingPatternScreen screen(ModularContainerMenu menu) {
        return menu.isScreenInitialized() && menu.getScreen() instanceof CraftingPatternScreen screen ? screen : null;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ModularContainerMenu> screen) {
        // not EmiPlayerInventory.of: that asks this handler, forever
        return new EmiPlayerInventory(screen.getMenu().getPlayer().getInventory().items.stream()
                .filter(stack -> !stack.isEmpty())
                .<EmiStack>map(EmiStack::of)
                .toList());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return holder(recipe) != null;
    }

    private static @Nullable Recipe<?> holder(EmiRecipe recipe) {
        var category = recipe.getCategory();
        if (category != VanillaEmiRecipeCategories.CRAFTING && category != VanillaEmiRecipeCategories.STONECUTTING &&
                category != VanillaEmiRecipeCategories.SMITHING) {
            return null;
        }
        var backing = recipe.getBackingRecipe();
        return backing != null && CraftingPattern.Mode.of(backing) != null ? backing : null;
    }

    /** Every other ModularUI window has nothing to do with these recipes. */
    @Override
    public boolean alwaysDisplaySupport(EmiRecipe recipe) {
        return false;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModularContainerMenu> context) {
        return screen(context.getScreenHandler()) != null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModularContainerMenu> context) {
        CraftingPatternScreen screen = screen(context.getScreenHandler());
        Recipe<?> holder = holder(recipe);
        if (screen == null || holder == null) return false;
        screen.fill(holder);
        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }
}
