package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;

import java.util.List;

/** Any fluid of a fluid tag: a recipe input that a mark of any of its fluids satisfies. Not marked by clicking. */
public final class FluidTagGhostIngredient extends TagGhostIngredient<Fluid> {

    public static final Type<TagKey<Fluid>> TYPE = new Type<>("fluid_tag", tagClass(),
            TagKey.hashedCodec(Registries.FLUID), TagGhostIngredient::write, reader(Registries.FLUID),
            tag -> !tag.isFor(Registries.FLUID),
            FluidTagGhostIngredient::new, (held, rightClick) -> null, null);

    public FluidTagGhostIngredient(TagKey<Fluid> tag) {
        super(tag);
    }

    @Override
    public Type<TagKey<Fluid>> type() {
        return TYPE;
    }

    @Override
    public boolean isSatisfiedBy(GhostIngredient<?> mark) {
        return mark instanceof FluidGhostIngredient fluid && fluid.value().getFluid().defaultFluidState().is(value) || equals(mark);
    }

    @Override
    public EntryList<FluidStack> toEntryList() {
        return FluidTagList.of(value, 1, null);
    }

    @Override
    public IDrawable slotBackground() {
        return GuiTextures.SLOT_FLUID;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y) {
        List<Holder<Fluid>> fluids = contents(BuiltInRegistries.FLUID, value);
        if (!fluids.isEmpty()) GuiDraw.drawFluidTexture(graphics, new FluidStack(current(fluids).value(), 1), x, y, 16, 16, 0);
    }
}
