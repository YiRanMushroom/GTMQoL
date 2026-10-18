package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;

/**
 * A marked fluid, with its components; the amount doesn't matter (kept at 1). Right click with a container of it to
 * mark it.
 */
public final class FluidGhostIngredient extends GhostIngredient<FluidStack> {

    public static final Type<FluidStack> TYPE = register(new Type<>("fluid", FluidStack.class,
            FluidStack.CODEC, FluidStack.STREAM_CODEC, FluidStack::isEmpty, FluidGhostIngredient::new,
            (held, rightClick) -> rightClick ? FluidUtil.getFluidContained(held).orElse(null) : null));

    public FluidGhostIngredient(FluidStack stack) {
        super(stack.copyWithAmount(1));
    }

    public FluidGhostIngredient(Fluid fluid) {
        this(new FluidStack(fluid, 1));
    }

    @Override
    public Type<FluidStack> type() {
        return TYPE;
    }

    @Override
    protected boolean sameValue(FluidStack other) {
        return FluidStack.isSameFluidSameComponents(value, other);
    }

    @Override
    protected int valueHash() {
        return FluidStack.hashFluidAndComponents(value);
    }

    @Override
    public Component getName() {
        return value.getHoverName();
    }

    @Override
    public EntryList<FluidStack> toEntryList() {
        return FluidStackList.of(value);
    }

    @Override
    public IDrawable slotBackground() {
        return GuiTextures.SLOT_FLUID;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y) {
        GuiDraw.drawFluidTexture(graphics, value, x, y, 16, 16, 0);
    }
}
