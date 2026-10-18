package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;

import java.util.Objects;

/** A marked item, with its NBT; the count doesn't matter (kept at 1). Left click with it to mark it. */
public final class ItemGhostIngredient extends GhostIngredient<ItemStack> {

    public static final Type<ItemStack> TYPE = register(new Type<>("item", ItemStack.class,
            ItemStack.CODEC, FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem, ItemStack::isEmpty,
            ItemGhostIngredient::new, (held, rightClick) -> rightClick ? null : held));

    public ItemGhostIngredient(ItemStack stack) {
        super(stack.copyWithCount(1));
    }

    public ItemGhostIngredient(ItemLike item) {
        this(new ItemStack(item));
    }

    @Override
    public Type<ItemStack> type() {
        return TYPE;
    }

    @Override
    protected boolean sameValue(ItemStack other) {
        return ItemStack.isSameItemSameTags(value, other);
    }

    @Override
    protected int valueHash() {
        return Objects.hash(value.getItem(), value.getTag());
    }

    @Override
    public Component getName() {
        return value.getHoverName();
    }

    @Override
    public EntryList<ItemStack> toEntryList() {
        return ItemStackList.of(value);
    }

    @Override
    public IDrawable slotBackground() {
        return GuiTextures.SLOT_ITEM;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(value, x, y);
    }
}
