package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

/**
 * A recipe capability for a fluid-like stack type, see {@link GenericStackLikeType}. Matching and parallel logic
 * follow {@code FluidRecipeCapability}; handlers are {@link GenericStackLikeNotifiableHandler}s whose
 * {@code getContents()} are stacks of the type.
 */
public class GenericStackLikeRecipeCapability<S, I> extends RecipeCapability<I> {

    /** Every capability made by {@link #register}, in registration order. */
    public static final List<GenericStackLikeRecipeCapability<?, ?>> ALL = new ArrayList<>();

    public final GenericStackLikeType<S, I> type;

    public GenericStackLikeRecipeCapability(GenericStackLikeType<S, I> type, int color, int sortIndex) {
        super(type.id(), color, true, sortIndex, new Serializer<>(type));
        this.type = type;
    }

    /** Registers the capability (lang {@code name}) and its recipe lookup. Call during mod construction. */
    public static <S, I> GenericStackLikeRecipeCapability<S, I> register(GenericStackLikeType<S, I> type,
                                                                         int color, int sortIndex, String name) {
        var cap = new GenericStackLikeRecipeCapability<>(type, color, sortIndex);
        GTMQoLAddon.registrate()
                .generic(cap.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> cap)
                .lang(v -> v.getId().toLanguageKey("recipe_capability"), name)
                .register();
        // Recipe ingredients are exactly the content class, so getFrom never falls back to
        // getDefaultMapIngredient for them (stacks from handlers do).
        MapIngredientTypeManager.registerMapIngredient(type.ingredientClass(), cap::getDefaultMapIngredient);
        ALL.add(cap);
        return cap;
    }

    /** "1,000 x Oxygen", naming the first type the ingredient matches; empty if it matches nothing. */
    public MutableComponent describe(I ingredient, long amount) {
        List<S> stacks = type.getStacks(ingredient);
        if (stacks.isEmpty()) return Component.empty();
        return Component.literal(FormattingUtil.formatNumbers(amount) + " ")
                .append(Component.translatable("gtceu.gui.content.times_item", type.displayName(stacks.getFirst())));
    }

    @Override
    public I copyInner(I content) {
        return content;
    }

    @Override
    public I copyWithModifier(I content, ContentModifier modifier) {
        return type.withAmount(content, modifier.apply(type.ingredientAmount(content)));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object o : ingredients) {
            if (!containsEqual(list, o)) {
                list.add(o);
            }
        }
        return list;
    }

    private boolean containsEqual(List<Object> list, Object o) {
        Class<I> ingClass = type.ingredientClass();
        Class<S> stackClass = type.stackClass();
        for (Object existing : list) {
            if (ingClass.isInstance(o)) {
                if (ingClass.isInstance(existing) && existing.equals(o)) return true;
                if (stackClass.isInstance(existing) &&
                        type.test(ingClass.cast(o), stackClass.cast(existing))) return true;
            } else if (stackClass.isInstance(o)) {
                if (ingClass.isInstance(existing) &&
                        type.test(ingClass.cast(existing), stackClass.cast(o))) return true;
                if (stackClass.isInstance(existing) &&
                        type.isSameType(stackClass.cast(existing), stackClass.cast(o))) return true;
            }
        }
        return false;
    }

    @Override
    public List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (type.ingredientClass().isInstance(object)) {
            List<AbstractMapIngredient> list = new ArrayList<>();
            Set<Object> seen = new HashSet<>();
            for (S stack : type.getStacks(type.ingredientClass().cast(object))) {
                Object key = type.lookupKey(stack);
                if (seen.add(key)) list.add(new GenericStackLikeMapIngredient(key));
            }
            return list;
        }
        if (type.stackClass().isInstance(object)) {
            return List.of(new GenericStackLikeMapIngredient(type.lookupKey(type.stackClass().cast(object))));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier,
                                        boolean tick) {
        var outputContents = (tick ? recipe.tickOutputs : recipe.outputs).get(this);
        if (outputContents == null || outputContents.isEmpty()) return multiplier;
        if (!holder.hasCapabilityProxies()) return 0;
        var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
        if (handlers.isEmpty()) return 0;
        int minMultiplier = 0;
        int maxMultiplier = multiplier;
        long maxAmount = 0;
        List<I> ingredients = new ArrayList<>(outputContents.size());
        for (var content : outputContents) {
            I ing = this.of(content.content());
            maxAmount = Math.max(maxAmount, type.ingredientAmount(ing));
            ingredients.add(ing);
        }
        if (maxAmount == 0) return multiplier;
        if (multiplier > Long.MAX_VALUE / maxAmount) {
            maxMultiplier = multiplier = GTMath.saturatedCast(Long.MAX_VALUE / maxAmount);
        }
        while (minMultiplier != maxMultiplier) {
            List<Object> scaled = new ArrayList<>(ingredients.size());
            for (I ing : ingredients) {
                scaled.add(copyWithModifier(ing, ContentModifier.multiplier(multiplier)));
            }
            List<?> copied = scaled;
            for (var handler : handlers) {
                copied = handler.handleRecipe(IO.OUT, recipe, copied, true);
                if (copied.isEmpty()) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(copied.isEmpty(), minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }
        return multiplier;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (!holder.hasCapabilityProxies()) return 0;
        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return limit;
        List<List<Amount<S>>> inventoryGroups = getInputContents(holder);
        if (inventoryGroups.isEmpty()) return 0;
        // Same as fluids: duplicated ingredients are summed, non-consumables don't count towards the ratio.
        List<Amount<I>> nonConsumables = new ArrayList<>();
        List<Amount<I>> consumables = new ArrayList<>();
        for (Content content : inputs) {
            I ing = of(content.content());
            long amount = type.ingredientAmount(ing);
            if (content.chance() == 0) {
                nonConsumables.add(new Amount<>(ing, amount));
                continue;
            }
            List<S> stacks = type.getStacks(ing);
            Amount<I> existing = null;
            if (!stacks.isEmpty()) {
                for (var c : consumables) {
                    if (type.test(c.value, stacks.getFirst())) {
                        existing = c;
                        break;
                    }
                }
            }
            if (existing != null) existing.amount += amount;
            else consumables.add(new Amount<>(ing, amount));
        }
        if (consumables.isEmpty() && nonConsumables.isEmpty()) return limit;
        int maxMultiplier = 0;
        for (var group : inventoryGroups) {
            boolean satisfied = true;
            for (var nc : nonConsumables) {
                long needed = nc.amount;
                for (var stack : group) {
                    if (type.test(nc.value, stack.value)) {
                        long lesser = Math.min(needed, stack.amount);
                        stack.amount -= lesser;
                        needed -= lesser;
                        if (needed == 0) break;
                    }
                }
                if (needed > 0) {
                    satisfied = false;
                    break;
                }
            }
            if (!satisfied) continue;
            if (consumables.isEmpty()) return limit;
            int invMultiplier = Integer.MAX_VALUE;
            for (var c : consumables) {
                long needed = c.amount;
                long maxNeeded = needed * limit;
                long available = 0;
                for (var stack : group) {
                    if (type.test(c.value, stack.value)) {
                        available += stack.amount;
                        if (available >= maxNeeded) break;
                    }
                }
                int ratio = GTMath.saturatedCast(Math.min(limit, available / needed));
                invMultiplier = Math.min(invMultiplier, ratio);
                if (ratio == 0) break;
            }
            if (invMultiplier == limit) return limit;
            maxMultiplier = Math.max(maxMultiplier, invMultiplier);
        }
        return maxMultiplier;
    }

    /** Contents of the input handlers per inventory group, like {@code FluidRecipeCapability.getInputContents}. */
    private List<List<Amount<S>>> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return Collections.emptyList();
        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlerLists) {
            if (!handler.hasCapability(this)) continue;
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }
        List<List<Amount<S>>> invs = new ArrayList<>();
        // Distinct handler lists each are their own inventory.
        for (RecipeHandlerList handlerList : handlerGroups.getOrDefault(RecipeHandlerGroupDistinctness.BUS_DISTINCT,
                Collections.emptyList())) {
            List<Amount<S>> inv = new ArrayList<>();
            addContents(inv, handlerList.getCapability(this));
            if (!inv.isEmpty()) invs.add(inv);
        }
        for (var entry : handlerGroups.entrySet()) {
            if (entry.getKey() == RecipeHandlerGroupDistinctness.BUS_DISTINCT) continue;
            List<Amount<S>> inv = new ArrayList<>();
            for (RecipeHandlerList handlerList : entry.getValue()) {
                addContents(inv, handlerList.getCapability(this));
            }
            if (!inv.isEmpty()) invs.add(inv);
        }
        return invs;
    }

    private void addContents(List<Amount<S>> inv, List<IRecipeHandler<?>> handlers) {
        for (IRecipeHandler<?> handler : handlers) {
            for (Object content : handler.getContents()) {
                if (!type.stackClass().isInstance(content)) continue;
                S stack = type.stackClass().cast(content);
                if (type.isEmpty(stack)) continue;
                Amount<S> existing = null;
                for (var a : inv) {
                    if (type.isSameType(a.value, stack)) {
                        existing = a;
                        break;
                    }
                }
                if (existing != null) existing.amount += type.stackAmount(stack);
                else inv.add(new Amount<>(stack, type.stackAmount(stack)));
            }
        }
    }

    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<GenericStackLikeNotifiableHandler<S, I>> getCapabilityHandlers(MetaMachine machine) {
        List<GenericStackLikeNotifiableHandler<S, I>> list = new ArrayList<>();
        for (GenericStackLikeNotifiableHandler handler : machine.getTraits(GenericStackLikeNotifiableHandler.class)) {
            if (handler.getCapability() == this) list.add(handler);
        }
        return list;
    }

    /** A value with a mutable amount; stacks and ingredients themselves are treated as immutable. */
    private static final class Amount<T> {

        final T value;
        long amount;

        Amount(T value, long amount) {
            this.value = value;
            this.amount = amount;
        }
    }

    private record Serializer<S, I>(GenericStackLikeType<S, I> type) implements IContentSerializer<I> {

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, I content) {
            type.ingredientStreamCodec().encode(buf, content);
        }

        @Override
        public I fromNetwork(RegistryFriendlyByteBuf buf) {
            return type.ingredientStreamCodec().decode(buf);
        }

        @Override
        public I of(Object o) {
            if (type.ingredientClass().isInstance(o)) return type.ingredientClass().cast(o);
            if (type.stackClass().isInstance(o)) return type.of(type.stackClass().cast(o));
            throw new IllegalArgumentException("Not a " + type.id() + " ingredient: " + o);
        }

        @Override
        public I defaultValue() {
            // Not used by gtceu; ingredients of these types usually can't be empty.
            throw new UnsupportedOperationException("No default " + type.id() + " ingredient");
        }

        @Override
        public Class<I> contentClass() {
            return type.ingredientClass();
        }

        @Override
        public Codec<I> codec() {
            return type.ingredientCodec();
        }
    }
}
