package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link GhostIngredient} as a recipe input: tells the machine which recipe to run when the real inputs fit
 * several. Only a {@link GhostIngredientHatchPartMachine} provides it (what is marked in its list), so real items and
 * fluids never satisfy it, and nothing is consumed. A recipe lookup filter, so recipes that differ only in the mark
 * are told apart. Recipes: {@code .input(CAP, new ItemGhostIngredient(stack))}.
 */
public final class GhostIngredientRecipeCapability extends RecipeCapability<GhostIngredient<?>> {

    public static final GhostIngredientRecipeCapability CAP = new GhostIngredientRecipeCapability();

    private GhostIngredientRecipeCapability() {
        super(GTMQoL.id("ghost_ingredient"), 0xFFB0BEC5, true, 6, Serializer.INSTANCE);
    }

    /**
     * Registers the capability. Call during mod construction. The lookup needs no {@code registerMapIngredient}:
     * contents are always subclasses of {@link GhostIngredient}, which gtceu's lookup hands to
     * {@link #getDefaultMapIngredient}.
     */
    public static void init() {
        GTMQoLAddon.registrate()
                .generic(CAP.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> CAP)
                .lang(v -> v.getId().toLanguageKey("recipe_capability"), "Ghost Ingredient")
                .register();
    }

    @Override
    public GhostIngredient<?> copyInner(GhostIngredient<?> content) {
        return content;
    }

    @Override
    public GhostIngredient<?> copyWithModifier(GhostIngredient<?> content, ContentModifier modifier) {
        return content;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    /**
     * A ghost ingredient hatch is a plain non-distinct part (gtceu's default would add it to every distinct group), and
     * pattern buffers that mark their outputs stay distinct.
     */
    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }

    /**
     * Never consumed, so it doesn't limit parallels. The default ({@code Integer.MAX_VALUE}, "skip") makes gtceu's
     * parallel logic report "no input capabilities" for recipes with no other input.
     */
    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        return limit;
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ArrayList<>(ingredients.size());
        for (Object o : ingredients) {
            if (!list.contains(o)) list.add(o);
        }
        return list;
    }

    @Override
    public List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        // mutable: GT's recipe lookup pools these in place (MapIngredientPool.applyPooling sets list elements)
        List<AbstractMapIngredient> list = new ArrayList<>(1);
        if (object instanceof GhostIngredient<?> ghost) list.add(new GhostMapIngredient(ghost));
        return list;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private enum Serializer implements IContentSerializer<GhostIngredient<?>> {

        INSTANCE;

        /**
         * Also takes the value of any type (an item stack, a fluid stack...), and items and item ids (as KubeJS
         * scripts give them).
         */
        @Override
        public GhostIngredient<?> of(Object o) {
            if (o instanceof GhostIngredient<?> ghost) return ghost;
            for (var type : GhostIngredient.types()) {
                GhostIngredient<?> ghost = ofValue(type, o);
                if (ghost != null) return ghost;
            }
            if (o instanceof ItemLike item) return new ItemGhostIngredient(item);
            ResourceLocation id = o instanceof ResourceLocation rl ? rl :
                    o instanceof CharSequence s ? ResourceLocation.tryParse(s.toString()) : null;
            if (id == null) throw new IllegalArgumentException("Not a ghost ingredient: " + o);
            return new ItemGhostIngredient(BuiltInRegistries.ITEM.getOptional(id)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + o)));
        }

        private static <T> GhostIngredient<T> ofValue(GhostIngredient.Type<T> type, Object o) {
            return type.valueClass().isInstance(o) ? type.of(type.valueClass().cast(o)) : null;
        }

        @Override
        public GhostIngredient<?> defaultValue() {
            return new ItemGhostIngredient(Items.AIR);
        }

        @Override
        public Class<GhostIngredient<?>> contentClass() {
            return (Class) GhostIngredient.class;
        }

        @Override
        public Codec<GhostIngredient<?>> codec() {
            return GhostIngredient.CODEC;
        }
    }
}
