package com.yiran.minecraft.gtmqol.common.crystal;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * The block right behind the controller (the middle of a 3 × 3 × 3 structure whose controller is in the middle of
 * its front face) must be {@link #block}. Shown in EMI/JEI as a line with the block's item.
 */
public class CenterBlockCondition extends RecipeCondition<CenterBlockCondition> {

    public static final String KEY = "gtmqol.recipe.condition.center_block";

    // spotless:off
    public static final Codec<CenterBlockCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(c -> c.block))
            .apply(instance, CenterBlockCondition::new));
    // spotless:on

    private final Block block;

    public CenterBlockCondition() {
        this(false, Blocks.AIR);
    }

    public CenterBlockCondition(Block block) {
        this(false, block);
    }

    public CenterBlockCondition(boolean isReverse, Block block) {
        super(isReverse);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public RecipeConditionType<CenterBlockCondition> getType() {
        return CrystalGrowth.CENTER_BLOCK;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(KEY + ".tooltip", block.getName());
    }

    @Override
    public RecipeUIModifier modifyUI() {
        // Same as GTCEu's AdjacentBlockCondition.
        return (recipe, widget) -> {
            var row = Flow.row().coverChildrenHeight().widthRel(1);
            row.child(Text.lang(KEY).asWidget());
            row.child(RecipeViewerSlotWidget.create(ItemStack.class).marginLeft(2)
                    .recipeSlotRole(RecipeSlotRole.RENDER_ONLY).value(new ItemStack(block.asItem())));
            widget.textComponents.child(row);
        };
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        Level level = machine.getLevel();
        if (level == null) return false;
        var center = machine.getBlockPos().relative(machine.getFrontFacing().getOpposite());
        return level.getBlockState(center).is(block);
    }

    @Override
    public CenterBlockCondition createTemplate() {
        return new CenterBlockCondition();
    }
}
