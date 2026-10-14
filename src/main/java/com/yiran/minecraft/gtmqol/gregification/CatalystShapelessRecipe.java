package com.yiran.minecraft.gtmqol.gregification;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A shapeless recipe that leaves its {@code catalyst} in the grid, like a GT tool, but without durability.
 */
public class CatalystShapelessRecipe extends ShapelessRecipe {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, GTMQoL.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CatalystShapelessRecipe>> SERIALIZER =
            SERIALIZERS.register("catalyst_shapeless", () -> new RecipeSerializer<>() {

                private static final MapCodec<CatalystShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i
                        .group(RecipeSerializer.SHAPELESS_RECIPE.codec().forGetter(r -> r),
                                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(r -> r.catalyst))
                        .apply(i, CatalystShapelessRecipe::new));

                private static final StreamCodec<RegistryFriendlyByteBuf, CatalystShapelessRecipe> STREAM_CODEC =
                        StreamCodec.composite(
                                RecipeSerializer.SHAPELESS_RECIPE.streamCodec(), r -> r,
                                Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
                                CatalystShapelessRecipe::new);

                @Override
                public MapCodec<CatalystShapelessRecipe> codec() {
                    return CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, CatalystShapelessRecipe> streamCodec() {
                    return STREAM_CODEC;
                }
            });

    private final Ingredient catalyst;

    /** {@code recipe}'s ingredients must include the catalyst. */
    public CatalystShapelessRecipe(ShapelessRecipe recipe, Ingredient catalyst) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients());
        this.catalyst = catalyst;
    }

    public static void init(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && catalyst.test(stack)) remaining.set(i, stack.copyWithCount(1));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }
}
