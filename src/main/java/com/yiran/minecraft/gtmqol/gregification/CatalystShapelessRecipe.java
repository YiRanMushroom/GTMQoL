package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.data.recipe.builder.ShapelessRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * A shapeless recipe that leaves its {@code catalyst} in the grid, like a GT tool, but without durability.
 * Its JSON is a vanilla shapeless recipe plus a {@code catalyst} ingredient.
 */
public class CatalystShapelessRecipe extends ShapelessRecipe {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, GTMQoL.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CatalystShapelessRecipe>> SERIALIZER =
            SERIALIZERS.register("catalyst_shapeless", Serializer::new);

    private final Ingredient catalyst;

    /** {@code recipe}'s ingredients must include the catalyst. */
    public CatalystShapelessRecipe(ShapelessRecipe recipe, Ingredient catalyst) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients());
        this.catalyst = catalyst;
    }

    public static void init(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(container);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && catalyst.test(stack)) remaining.set(i, stack.copyWithCount(1));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }

    /** For a {@code Consumer<FinishedRecipe>}: {@code shapeless}' ingredients must include the catalyst. */
    public static FinishedRecipe finished(ResourceLocation id, ShapelessRecipeBuilder shapeless, Ingredient catalyst) {
        return new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject json) {
                shapeless.toJson(json);
                json.add("catalyst", catalyst.toJson());
            }

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SERIALIZER.get();
            }

            @Override
            public @Nullable JsonObject serializeAdvancement() {
                return null;
            }

            @Override
            public @Nullable ResourceLocation getAdvancementId() {
                return null;
            }
        };
    }

    private static class Serializer implements RecipeSerializer<CatalystShapelessRecipe> {

        @Override
        public CatalystShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new CatalystShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json),
                    Ingredient.fromJson(GsonHelper.getNonNull(json, "catalyst"), false));
        }

        @Override
        public CatalystShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new CatalystShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf),
                    Ingredient.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CatalystShapelessRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
            recipe.catalyst.toNetwork(buf);
        }
    }
}
