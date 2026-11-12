package com.yiran.minecraft.gtmqol.common.stacklike.mekanism;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeRecipeCapability;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeType;
import com.yiran.minecraft.gtmqol.gregification.LoadingTags;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mekanism chemicals (gases, infuse types, pigments and slurries are one type on 1.21) as a recipe capability.
 * Only touch this class when Mekanism is loaded.
 */
public final class ChemicalStackLike implements GenericStackLikeType<ChemicalStack, ChemicalStackIngredient> {

    public static final ChemicalStackLike TYPE = new ChemicalStackLike();
    public static GenericStackLikeRecipeCapability<ChemicalStack, ChemicalStackIngredient> CAP;

    private static final ResourceLocation ID = GTMQoL.id("chemical");

    private ChemicalStackLike() {}

    public static void init() {
        // Sort index 4: after gtceu's cwu (3), before block_state (5).
        CAP = GenericStackLikeRecipeCapability.register(TYPE, 0xFF9C5ED6, 4, "Chemical");
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Class<ChemicalStack> stackClass() {
        return ChemicalStack.class;
    }

    @Override
    public Class<ChemicalStackIngredient> ingredientClass() {
        return ChemicalStackIngredient.class;
    }

    @Override
    public boolean isEmpty(ChemicalStack stack) {
        return stack.isEmpty();
    }

    @Override
    public long stackAmount(ChemicalStack stack) {
        return stack.getAmount();
    }

    @Override
    public ChemicalStack copyWithAmount(ChemicalStack stack, long amount) {
        return stack.copyWithAmount(amount);
    }

    @Override
    public boolean isSameType(ChemicalStack a, ChemicalStack b) {
        return ChemicalStack.isSameChemical(a, b);
    }

    @Override
    public Object lookupKey(ChemicalStack stack) {
        return stack.getChemical();
    }

    @Override
    public Codec<ChemicalStack> stackCodec() {
        return ChemicalStack.OPTIONAL_CODEC;
    }

    @Override
    public Component displayName(ChemicalStack stack) {
        return stack.getTextComponent();
    }

    @Override
    public long ingredientAmount(ChemicalStackIngredient ingredient) {
        return ingredient.amount();
    }

    @Override
    public ChemicalStackIngredient withAmount(ChemicalStackIngredient ingredient, long amount) {
        return IngredientCreatorAccess.chemicalStack().from(ingredient.ingredient(), amount);
    }

    @Override
    public boolean test(ChemicalStackIngredient ingredient, ChemicalStack stack) {
        return !stack.isEmpty() && ingredient.testType(stack);
    }

    @Override
    public List<ChemicalStack> getStacks(ChemicalStackIngredient ingredient) {
        return ingredient.getRepresentations();
    }

    // Untested: after launching and loading a world, Mekanism reports no incomplete metallurgic infusing / injecting
    // recipes, its metallurgic infuser takes carbon and redstone, and the gregified infuser runs them
    @Override
    public Set<Object> lookupKeys(ChemicalStackIngredient ingredient) {
        return new LinkedHashSet<>(chemicals(ingredient.ingredient()));
    }

    /**
     * The chemicals an ingredient takes, worked out from its parts and {@link LoadingTags}, not with
     * {@code getChemicals}: that keeps what it finds, and asked while recipes load, before the registries have the new
     * tags, it left Mekanism's own recipes (holding the same ingredient) taking nothing from a tag.
     */
    private static Set<Chemical> chemicals(ChemicalIngredient ingredient) {
        Set<Chemical> chemicals = new LinkedHashSet<>();
        switch (ingredient) {
            case SingleChemicalIngredient single -> chemicals.add(single.chemical().value());
            case TagChemicalIngredient tagged -> LoadingTags.get(MekanismAPI.CHEMICAL_REGISTRY, tagged.tag().location())
                    .forEach(holder -> chemicals.add(holder.value()));
            case CompoundChemicalIngredient compound -> {
                for (ChemicalIngredient child : compound.children()) chemicals.addAll(chemicals(child));
            }
            case DifferenceChemicalIngredient difference -> {
                chemicals.addAll(chemicals(difference.base()));
                chemicals.removeAll(chemicals(difference.subtracted()));
            }
            case IntersectionChemicalIngredient intersection -> {
                List<ChemicalIngredient> children = intersection.children();
                chemicals.addAll(chemicals(children.getFirst()));
                for (ChemicalIngredient child : children.subList(1, children.size())) {
                    chemicals.retainAll(chemicals(child));
                }
            }
            case EmptyChemicalIngredient ignored -> {
            }
            // a kind newer than Mekanism 10.7 (without this, a MatchException): its own resolution, which keeps nothing
            default -> ingredient.generateChemicalHolders().forEach(holder -> chemicals.add(holder.value()));
        }
        return chemicals;
    }

    @Override
    public ChemicalStackIngredient of(ChemicalStack stack) {
        return IngredientCreatorAccess.chemicalStack().from(stack);
    }

    @Override
    public Codec<ChemicalStackIngredient> ingredientCodec() {
        return ChemicalStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalStackIngredient> ingredientStreamCodec() {
        return ChemicalStackIngredient.STREAM_CODEC;
    }
}
