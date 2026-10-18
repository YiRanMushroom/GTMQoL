package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * One recipe type of another mod, which gets its own GT recipe type and either tiered single-block machines or a
 * multiblock.
 *
 * <p>Each machine is crafted from its {@code counterpart} plus the {@code catalyst}, which is not consumed; without a
 * catalyst, from the counterpart and a GT hammer or the magical assembler. Ids, since neither may be registered yet
 * when this is declared.</p>
 *
 * @param name        path of the GT recipe type and the machines, e.g. {@code mi_macerator}
 * @param englishName e.g. {@code MI Macerator}
 * @param proxies     the foreign recipe types, only asked for at common setup (they may not be registered when
 *                    this is declared, or may depend on a registry); the converter takes the recipes of all of
 *                    them. None, with a null
 *                    converter, for a type whose recipes all come from custom recipe logic (added in
 *                    {@code recipeType})
 * @param recipeType  anything else the GT recipe type needs, e.g. slot counts of other capabilities
 * @param sound       working sound, null for none
 * @param ui          recipe UI, e.g. the progress bar
 * @param catalyst    item id, e.g. the foreign mod's guide book, or null for hammer/magical assembler recipes
 * @param counterpart item id of the machine it is crafted from, by tier (ignored for multiblocks)
 * @param model       single blocks: a GTCEu workable model, e.g. {@code gtceu:block/machines/macerator}
 * @param shape       multiblocks: the GT multiblock it is built like
 * @param tooltip     lang key of an extra line on the machines' tooltip, or null
 * @param modifiers   multiblocks: recipe modifiers instead of the default ones, or null
 * @param crafting    without a catalyst: the machine's recipes instead of the hammer and magical assembler ones,
 *                    or null
 */
public record ForeignMachineType(String name, String englishName, Supplier<List<RecipeType<?>>> proxies,
                                 @Nullable ForeignRecipeConverter converter,
                                 int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs,
                                 UnaryOperator<GTRecipeTypeBuilder> recipeType,
                                 @Nullable Holder<SoundEntry> sound, UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                 @Nullable ResourceLocation catalyst, IntFunction<ResourceLocation> counterpart,
                                 @Nullable ResourceLocation model, @Nullable MultiblockShape shape,
                                 @Nullable String tooltip, @Nullable List<RecipeModifier> modifiers,
                                 @Nullable Crafting crafting) {

    public static ForeignMachineType singleBlock(String name, String englishName,
                                                 @Nullable Supplier<? extends RecipeType<?>> proxy,
                                                 @Nullable ForeignRecipeConverter converter, int itemInputs,
                                                 int itemOutputs, int fluidInputs, int fluidOutputs,
                                                 @Nullable Holder<SoundEntry> sound,
                                                 UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                                 @Nullable ResourceLocation catalyst,
                                                 IntFunction<ResourceLocation> counterpart, ResourceLocation model) {
        return new ForeignMachineType(name, englishName, proxies(proxy), converter, itemInputs, itemOutputs,
                fluidInputs, fluidOutputs, UnaryOperator.identity(), sound, ui, catalyst, counterpart, model, null,
                null, null, null);
    }

    public static ForeignMachineType multiblock(String name, String englishName,
                                                @Nullable Supplier<? extends RecipeType<?>> proxy,
                                                ForeignRecipeConverter converter, int itemInputs, int itemOutputs,
                                                int fluidInputs, int fluidOutputs,
                                                @Nullable Holder<SoundEntry> sound,
                                                UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                                @Nullable ResourceLocation catalyst, ResourceLocation counterpart,
                                                MultiblockShape shape) {
        return new ForeignMachineType(name, englishName, proxies(proxy), converter, itemInputs, itemOutputs,
                fluidInputs, fluidOutputs, UnaryOperator.identity(), sound, ui, catalyst, tier -> counterpart, null,
                shape, null, null, null);
    }

    private static Supplier<List<RecipeType<?>>> proxies(@Nullable Supplier<? extends RecipeType<?>> proxy) {
        return proxy == null ? List::of : () -> List.of(proxy.get());
    }

    public ForeignMachineType withRecipeType(UnaryOperator<GTRecipeTypeBuilder> recipeType) {
        return new ForeignMachineType(name, englishName, proxies, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape, tooltip, modifiers,
                crafting);
    }

    public ForeignMachineType withTooltip(String tooltip) {
        return new ForeignMachineType(name, englishName, proxies, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape, tooltip, modifiers,
                crafting);
    }

    /** More foreign recipe types, all through the same converter. */
    @SafeVarargs
    public final ForeignMachineType withMoreProxies(Supplier<? extends RecipeType<?>>... more) {
        Supplier<List<RecipeType<?>>> before = proxies;
        return withProxies(() -> {
            List<RecipeType<?>> all = new ArrayList<>(before.get());
            for (var proxy : more) all.add(proxy.get());
            return all;
        });
    }

    /** Other foreign recipe types instead, e.g. worked out from a registry. */
    public ForeignMachineType withProxies(Supplier<List<RecipeType<?>>> proxies) {
        return new ForeignMachineType(name, englishName, proxies, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape, tooltip, modifiers,
                crafting);
    }

    public ForeignMachineType withModifiers(RecipeModifier... modifiers) {
        return new ForeignMachineType(name, englishName, proxies, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape, tooltip, List.of(modifiers),
                crafting);
    }

    public ForeignMachineType withCrafting(Crafting crafting) {
        return new ForeignMachineType(name, englishName, proxies, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape, tooltip, modifiers,
                crafting);
    }

    public boolean isMultiblock() {
        return shape != null;
    }

    /** How a machine is crafted from its counterpart. */
    @FunctionalInterface
    public interface Crafting {

        /** @param id free for one recipe; add a suffix for more */
        void addRecipes(RecipeOutput provider, ResourceLocation id, ItemStack counterpart, ItemStack machine);
    }
}
