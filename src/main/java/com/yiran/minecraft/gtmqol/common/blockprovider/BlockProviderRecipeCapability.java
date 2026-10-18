package com.yiran.minecraft.gtmqol.common.blockprovider;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A block in the world as a recipe input: the machine provides it through a {@link BlockProviderTrait} (e.g. the
 * block in the middle of its structure), recipes name the block they need. It is a recipe lookup filter, so recipes
 * that differ only in the block are told apart by gtceu's lookup like by an item, without needing an item input.
 * Never consumed; recipes add it with chance 0, like {@code notConsumable}.
 */
public final class BlockProviderRecipeCapability extends RecipeCapability<Block> {

    public static final BlockProviderRecipeCapability CAP = new BlockProviderRecipeCapability();

    private BlockProviderRecipeCapability() {
        super(GTMQoL.id("block"), 0xFF8D6E63, true, 5, Serializer.INSTANCE);
    }

    /** Registers the capability and its recipe lookup. Call during mod construction. */
    public static void init() {
        GTMQoLAddon.registrate()
                .generic(CAP.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> CAP)
                .lang(v -> v.getId().toLanguageKey("recipe_capability"), "Block")
                .register();
        // gtceu's lookup only falls back to getDefaultMapIngredient for subclasses of Block
        MapIngredientTypeManager.registerMapIngredient(Block.class, CAP::getDefaultMapIngredient);
    }

    @Override
    public Block copyInner(Block content) {
        return content;
    }

    @Override
    public Block copyWithModifier(Block content, ContentModifier modifier) {
        return content;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
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
        return object instanceof Block block ? List.of(new BlockMapIngredient(block)) : List.of();
    }

    private enum Serializer implements IContentSerializer<Block> {

        INSTANCE;

        /** Also takes block ids, as KubeJS scripts give them. */
        @Override
        public Block of(Object o) {
            if (o instanceof Block block) return block;
            if (o instanceof BlockState state) return state.getBlock();
            ResourceLocation id = o instanceof ResourceLocation rl ? rl :
                    o instanceof CharSequence s ? ResourceLocation.tryParse(s.toString()) : null;
            if (id == null) throw new IllegalArgumentException("Not a block: " + o);
            return BuiltInRegistries.BLOCK.getOptional(id)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown block: " + o));
        }

        @Override
        public Block defaultValue() {
            return Blocks.AIR;
        }

        @Override
        public Class<Block> contentClass() {
            return Block.class;
        }

        @Override
        public Codec<Block> codec() {
            return BuiltInRegistries.BLOCK.byNameCodec();
        }
    }
}
