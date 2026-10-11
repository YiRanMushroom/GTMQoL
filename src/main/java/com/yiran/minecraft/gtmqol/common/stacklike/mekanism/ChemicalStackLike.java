package com.yiran.minecraft.gtmqol.common.stacklike.mekanism;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeRecipeCapability;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeType;
import com.yiran.minecraft.gtmqol.core.mixins.mekanism.TaggedChemicalStackIngredientAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;

import java.util.List;
import java.util.stream.Stream;

/**
 * Mekanism chemicals as one recipe capability. Mekanism 10.4 has four kinds (gases, infuse types, pigments,
 * slurries) with their own stack and ingredient classes but no common generic type, so the cap works on
 * {@code ChemicalStack<?>} / {@code ChemicalStackIngredient<?, ?>} and dispatches on Mekanism's {@link ChemicalType}
 * (raw types inside). Mekanism 10.4 has no codecs: ingredients go through Mekanism's recipe JSON, stacks through
 * {@link BoxedChemicalStack}'s NBT. Only touch this class when Mekanism is loaded.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ChemicalStackLike implements GenericStackLikeType<ChemicalStack<?>, ChemicalStackIngredient<?, ?>> {

    public static final ChemicalStackLike TYPE = new ChemicalStackLike();
    public static GenericStackLikeRecipeCapability<ChemicalStack<?>, ChemicalStackIngredient<?, ?>> CAP;

    private static final ResourceLocation ID = GTMQoL.id("chemical");

    private static final Codec<ChemicalType> CHEMICAL_TYPE_CODEC = StringRepresentable.fromEnum(ChemicalType::values);

    /** Mekanism's JSON can't tell the kind of a tag ingredient, so the kind is stored next to it. */
    private static final Codec<ChemicalStackIngredient<?, ?>> INGREDIENT_CODEC = RecordCodecBuilder.create(i -> i
            .group(CHEMICAL_TYPE_CODEC.fieldOf("chemicalType")
                    .forGetter(ingredient -> ChemicalType.getTypeFor(ingredient)),
                    ExtraCodecs.JSON.fieldOf("ingredient").forGetter(ingredient -> ingredient.serialize()))
            .apply(i, (chemicalType, json) -> (ChemicalStackIngredient<?, ?>) creator(chemicalType).deserialize(json)));

    private static final Codec<ChemicalStack<?>> STACK_CODEC = CompoundTag.CODEC.xmap(
            tag -> BoxedChemicalStack.read(tag).getChemicalStack(),
            stack -> BoxedChemicalStack.box(stack).write(new CompoundTag()));

    private ChemicalStackLike() {}

    public static void init() {
        // Sort index 4: after gtceu's cwu (3), before block_state (5).
        CAP = GenericStackLikeRecipeCapability.register(TYPE, 0xFF9C5ED6, 4, "Chemical");
    }

    private static IChemicalStackIngredientCreator creator(ChemicalType chemicalType) {
        return IngredientCreatorAccess.getCreatorForType(chemicalType);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Class<ChemicalStack<?>> stackClass() {
        return (Class) ChemicalStack.class;
    }

    @Override
    public Class<ChemicalStackIngredient<?, ?>> ingredientClass() {
        return (Class) ChemicalStackIngredient.class;
    }

    @Override
    public boolean isEmpty(ChemicalStack<?> stack) {
        return stack.isEmpty();
    }

    @Override
    public long stackAmount(ChemicalStack<?> stack) {
        return stack.getAmount();
    }

    @Override
    public ChemicalStack<?> copyWithAmount(ChemicalStack<?> stack, long amount) {
        ChemicalStack<?> copy = stack.copy();
        copy.setAmount(amount);
        return copy;
    }

    @Override
    public boolean isSameType(ChemicalStack<?> a, ChemicalStack<?> b) {
        return a.getType() == b.getType();
    }

    @Override
    public Object lookupKey(ChemicalStack<?> stack) {
        return stack.getType();
    }

    @Override
    public Codec<ChemicalStack<?>> stackCodec() {
        return STACK_CODEC;
    }

    @Override
    public Component displayName(ChemicalStack<?> stack) {
        return stack.getTextComponent();
    }

    /** The amount of the first matched stack; the elements of a multi ingredient may have different amounts. */
    @Override
    public long ingredientAmount(ChemicalStackIngredient<?, ?> ingredient) {
        List<ChemicalStack<?>> stacks = getStacks(ingredient);
        return stacks.isEmpty() ? 0 : stacks.get(0).getAmount();
    }

    /**
     * Mekanism's API can't change an ingredient's amount, so this rebuilds it with the creator from its parts (every
     * element of a multi ingredient gets the same amount). Mekanism 10.4 only has these three implementations.
     */
    @Override
    public ChemicalStackIngredient<?, ?> withAmount(ChemicalStackIngredient<?, ?> ingredient, long amount) {
        return withAmount(creator(ChemicalType.getTypeFor(ingredient)), ingredient, amount);
    }

    private static ChemicalStackIngredient<?, ?> withAmount(IChemicalStackIngredientCreator creator,
                                                            ChemicalStackIngredient<?, ?> ingredient, long amount) {
        if (ingredient instanceof SingleChemicalStackIngredient single) {
            return (ChemicalStackIngredient<?, ?>) creator.from(single.getInputRaw(), amount);
        } else if (ingredient instanceof TaggedChemicalStackIngredient tagged) {
            TagKey<?> tag = ((TaggedChemicalStackIngredientAccessor) tagged).gtmqol$getTag().getKey();
            return (ChemicalStackIngredient<?, ?>) creator.from(tag, amount);
        } else if (ingredient instanceof IMultiIngredient multi) {
            // from(Stream) builds the array with the kind's own ingredient class, createMulti on a raw array doesn't.
            Stream<ChemicalStackIngredient<?, ?>> children = ((List<ChemicalStackIngredient<?, ?>>) multi.getIngredients())
                    .stream().map(child -> withAmount(creator, child, amount));
            return (ChemicalStackIngredient<?, ?>) creator.from(children);
        }
        throw new IllegalArgumentException("Unknown chemical ingredient " + ingredient.getClass().getName());
    }

    @Override
    public boolean test(ChemicalStackIngredient<?, ?> ingredient, ChemicalStack<?> stack) {
        // An ingredient of one kind may cast the stack to its own stack class.
        return !stack.isEmpty() && ChemicalType.getTypeFor(ingredient) == ChemicalType.getTypeFor(stack) &&
                ((ChemicalStackIngredient) ingredient).testType(stack);
    }

    @Override
    public List<ChemicalStack<?>> getStacks(ChemicalStackIngredient<?, ?> ingredient) {
        return (List) ingredient.getRepresentations();
    }

    @Override
    public ChemicalStackIngredient<?, ?> of(ChemicalStack<?> stack) {
        return (ChemicalStackIngredient<?, ?>) creator(ChemicalType.getTypeFor(stack)).from(stack);
    }

    @Override
    public Codec<ChemicalStackIngredient<?, ?>> ingredientCodec() {
        return INGREDIENT_CODEC;
    }

    @Override
    public void writeIngredient(FriendlyByteBuf buf, ChemicalStackIngredient<?, ?> ingredient) {
        buf.writeEnum(ChemicalType.getTypeFor(ingredient));
        ingredient.write(buf);
    }

    @Override
    public ChemicalStackIngredient<?, ?> readIngredient(FriendlyByteBuf buf) {
        return (ChemicalStackIngredient<?, ?>) creator(buf.readEnum(ChemicalType.class)).read(buf);
    }
}
