package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * One recipe type of another mod, which gets its own GT recipe type and either tiered single-block machines or a
 * multiblock.
 *
 * <p>Each machine is crafted from its {@code counterpart} plus the {@code catalyst}, which is not consumed. Ids,
 * since neither may be registered yet when this is declared.</p>
 *
 * @param name        path of the GT recipe type and the machines, e.g. {@code mi_macerator}
 * @param englishName e.g. {@code MI Macerator}
 * @param proxy       the foreign recipe type, only asked for at common setup (it may not be registered when this
 *                    is declared)
 * @param recipeType  anything else the GT recipe type needs, e.g. slot counts of other capabilities
 * @param sound       working sound, null for none
 * @param ui          recipe UI, e.g. the progress bar
 * @param catalyst    item id, e.g. the foreign mod's guide book
 * @param counterpart item id of the machine it is crafted from, by tier (ignored for multiblocks)
 * @param model       single blocks: a GTCEu workable model, e.g. {@code gtceu:block/machines/macerator}
 * @param shape       multiblocks: the GT multiblock it is built like
 */
public record ForeignMachineType(String name, String englishName, Supplier<? extends RecipeType<?>> proxy,
                                 ForeignRecipeConverter converter,
                                 int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs,
                                 UnaryOperator<GTRecipeTypeBuilder> recipeType,
                                 @Nullable Holder<SoundEntry> sound, UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                 ResourceLocation catalyst, IntFunction<ResourceLocation> counterpart,
                                 @Nullable ResourceLocation model, @Nullable MultiblockShape shape) {

    public static ForeignMachineType singleBlock(String name, String englishName,
                                                 Supplier<? extends RecipeType<?>> proxy,
                                                 ForeignRecipeConverter converter, int itemInputs,
                                                 int itemOutputs, int fluidInputs, int fluidOutputs,
                                                 @Nullable Holder<SoundEntry> sound,
                                                 UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                                 ResourceLocation catalyst,
                                                 IntFunction<ResourceLocation> counterpart, ResourceLocation model) {
        return new ForeignMachineType(name, englishName, proxy, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, UnaryOperator.identity(), sound, ui, catalyst, counterpart, model, null);
    }

    public static ForeignMachineType multiblock(String name, String englishName,
                                                Supplier<? extends RecipeType<?>> proxy,
                                                ForeignRecipeConverter converter, int itemInputs, int itemOutputs,
                                                int fluidInputs, int fluidOutputs,
                                                @Nullable Holder<SoundEntry> sound,
                                                UnaryOperator<GTRecipeTypeUILayout.Builder> ui,
                                                ResourceLocation catalyst, ResourceLocation counterpart,
                                                MultiblockShape shape) {
        return new ForeignMachineType(name, englishName, proxy, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, UnaryOperator.identity(), sound, ui, catalyst, tier -> counterpart, null, shape);
    }

    public ForeignMachineType withRecipeType(UnaryOperator<GTRecipeTypeBuilder> recipeType) {
        return new ForeignMachineType(name, englishName, proxy, converter, itemInputs, itemOutputs, fluidInputs,
                fluidOutputs, recipeType, sound, ui, catalyst, counterpart, model, shape);
    }

    public boolean isMultiblock() {
        return shape != null;
    }
}
