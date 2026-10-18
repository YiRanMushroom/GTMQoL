package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;

import java.util.Objects;

/**
 * A marked fluid, with its NBT; the amount doesn't matter (kept at 1). Right click with a container of it to mark
 * it.
 */
public final class FluidGhostIngredient extends GhostIngredient<FluidStack> {

    public static final Type<FluidStack> TYPE = register(new Type<>("fluid", FluidStack.class,
            FluidStack.CODEC, (buf, stack) -> stack.writeToPacket(buf), FluidStack::readFromPacket,
            FluidStack::isEmpty, FluidGhostIngredient::new,
            (held, rightClick) -> rightClick ? FluidUtil.getFluidContained(held).orElse(null) : null));

    public FluidGhostIngredient(FluidStack stack) {
        super(new FluidStack(stack, 1));
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
        return value.isFluidEqual(other);
    }

    @Override
    protected int valueHash() {
        return Objects.hash(value.getFluid(), value.getTag());
    }

    @Override
    public Component getName() {
        return value.getDisplayName();
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
