package com.yiran.minecraft.gtmqol.integration.jade;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.jade.provider.MachineTraitProvider;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeRecipeCapability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import org.jetbrains.annotations.Nullable;

/**
 * The running recipe's stack-like outputs (e.g. chemicals), below gtceu's own {@code RecipeOutputProvider}, which
 * only knows items and fluids. Text only, one line per output.
 */
public class StackLikeRecipeOutputProvider extends MachineTraitProvider<RecipeLogic, CompoundTag> {

    // gtceu adds the "Recipe Outputs:" line only when there are item or fluid outputs.
    private static final String HEADER = "Header";

    public StackLikeRecipeOutputProvider() {
        super(GTMQoL.id("stack_like_recipe_output"), RecipeLogic.class);
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY + 1;
    }

    @Override
    protected CompoundTag write(RecipeLogic recipeLogic) {
        CompoundTag data = new CompoundTag();
        if (!recipeLogic.isWorking()) return data;
        GTRecipe recipe = recipeLogic.getLastUnrolledRecipe();
        if (recipe == null) return data;
        var ops = recipeLogic.getMachine().getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            ListTag list = writeOutputs(cap, recipe, ops);
            if (!list.isEmpty()) data.put(cap.id.toString(), list);
        }
        if (!data.isEmpty()) {
            data.putBoolean(HEADER, recipe.getOutputContents(ItemRecipeCapability.CAP).isEmpty() &&
                    recipe.getOutputContents(FluidRecipeCapability.CAP).isEmpty());
        }
        return data;
    }

    private static <S, I> ListTag writeOutputs(GenericStackLikeRecipeCapability<S, I> cap, GTRecipe recipe,
                                               RegistryOps<Tag> ops) {
        ListTag list = new ListTag();
        int runs = recipe.getTotalRuns();
        for (var content : recipe.getOutputContents(cap)) {
            I ingredient = cap.of(content.content());
            // Like gtceu: chanced outputs show the expected amount.
            if (content.chance() < content.maxChance()) {
                double amount = (double) cap.type.ingredientAmount(ingredient) * runs * content.chance() /
                        content.maxChance();
                ingredient = cap.type.withAmount(ingredient, Math.max(1, Math.round(amount)));
            }
            cap.type.ingredientCodec().encodeStart(ops, ingredient).result().ifPresent(list::add);
        }
        return list;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        var ops = block.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        boolean header = data.getBoolean(HEADER);
        for (var cap : GenericStackLikeRecipeCapability.ALL) {
            if (!(data.get(cap.id.toString()) instanceof ListTag list)) continue;
            for (Tag tag : list) {
                var line = readOutput(cap, tag, ops);
                if (line == null) continue;
                if (header) {
                    tooltip.add(Component.translatable("gtceu.top.recipe_output"));
                    header = false;
                }
                tooltip.add(line);
            }
        }
    }

    private static <S, I> @Nullable Component readOutput(GenericStackLikeRecipeCapability<S, I> cap, Tag tag,
                                               RegistryOps<Tag> ops) {
        I ingredient = cap.type.ingredientCodec().parse(ops, tag).result().orElse(null);
        if (ingredient == null) return null;
        return Component.literal(" ")
                .append(cap.describe(ingredient, cap.type.ingredientAmount(ingredient)))
                .append(" (").append(cap.getColoredName()).append(")");
    }
}
