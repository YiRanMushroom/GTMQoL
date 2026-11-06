package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

/**
 * Turns the patterns among a machine's inputs into recipes. The pattern itself is a non-consumed input, so taking it
 * out stops the machine. {@link GTRecipeType#searchRecipe} tries only the first recipe custom logic returns, so with
 * several patterns the one that can run is picked here.
 */
enum PatternCrafterLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var server = GTCEu.getMinecraftServer();
        if (server == null) return null;
        Level level = server.overworld();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (!(content instanceof ItemStack stack) || !PatternCrafter.isPattern(stack)) continue;
                    ICrafterPattern pattern = ICrafterPattern.decode(stack, level);
                    if (pattern == null) continue;
                    GTRecipe recipe = recipe(stack, pattern, level);
                    if (RecipeHelper.matchRecipe(holder, recipe).isSuccess()) return recipe;
                }
            }
        }
        return null;
    }

    private static GTRecipe recipe(ItemStack stack, ICrafterPattern pattern, Level level) {
        var builder = PatternCrafter.RECIPE_TYPE
                .recipeBuilder(GTMQoL.id(Integer.toHexString(ItemStack.hashItemAndComponents(stack))))
                .notConsumable(stack.copyWithCount(1));
        for (var input : pattern.itemInputs()) {
            if (input.consumed()) builder.inputItems(input.ingredient(), input.count());
            else builder.notConsumable(input.ingredient());
        }
        for (FluidStack fluid : pattern.fluidInputs()) builder.inputFluids(fluid);
        for (ItemStack item : pattern.outputs()) builder.outputItems(item);
        if (pattern.itemInputs().stream().anyMatch(ICrafterPattern.ItemInput::changes)) {
            builder.addData(PatternWear.PATTERN_KEY, stack.copyWithCount(1).save(level.registryAccess()));
        }
        return builder.EUt(PatternCrafter.EU_PER_TICK).duration(PatternCrafter.DURATION).build();
    }
}
