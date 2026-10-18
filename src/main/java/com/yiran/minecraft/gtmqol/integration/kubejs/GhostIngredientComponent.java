package com.yiran.minecraft.gtmqol.integration.kubejs;

import com.yiran.minecraft.gtmqol.common.ghost.FluidGhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;

import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;

/**
 * {@link GhostIngredientRecipeCapability} contents in gtceu's KubeJS schema. Takes what the capability takes (item
 * stacks, fluid stacks, items, item ids), KubeJS fluids ({@code Fluid.of(...)}), and the saved JSON form.
 */
public final class GhostIngredientComponent implements RecipeComponent<GhostIngredient<?>> {

    public static final GhostIngredientComponent INSTANCE = new GhostIngredientComponent();

    private GhostIngredientComponent() {}

    @Override
    public String componentType() {
        return "gtmqol:ghost_ingredient";
    }

    @Override
    public Class<?> componentClass() {
        return GhostIngredient.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, GhostIngredient<?> value) {
        return GhostIngredient.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow(false, error -> {});
    }

    @Override
    public GhostIngredient<?> read(RecipeJS recipe, Object from) {
        if (from instanceof JsonObject json) {
            return GhostIngredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {});
        }
        if (from instanceof FluidStackJS fluid) {
            return new FluidGhostIngredient(new FluidStack(fluid.getFluid(), 1, fluid.getNbt()));
        }
        return GhostIngredientRecipeCapability.CAP.of(from);
    }
}
