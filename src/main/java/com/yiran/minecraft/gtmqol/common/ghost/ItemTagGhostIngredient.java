package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;

import java.util.List;

/** Any item of an item tag: a recipe input that a mark of any of its items satisfies. Not marked by clicking. */
public final class ItemTagGhostIngredient extends TagGhostIngredient<Item> {

    public static final Type<TagKey<Item>> TYPE = new Type<>("item_tag", tagClass(),
            TagKey.hashedCodec(Registries.ITEM), streamCodec(Registries.ITEM), tag -> !tag.isFor(Registries.ITEM),
            ItemTagGhostIngredient::new, (held, rightClick) -> null, null);

    public ItemTagGhostIngredient(TagKey<Item> tag) {
        super(tag);
    }

    @Override
    public Type<TagKey<Item>> type() {
        return TYPE;
    }

    @Override
    public boolean isSatisfiedBy(GhostIngredient<?> mark) {
        return mark instanceof ItemGhostIngredient item && item.value().is(value) || equals(mark);
    }

    @Override
    public EntryList<ItemStack> toEntryList() {
        return ItemTagList.of(value, 1, DataComponentPatch.EMPTY);
    }

    @Override
    public IDrawable slotBackground() {
        return GuiTextures.SLOT_ITEM;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y) {
        List<Holder<Item>> items = contents(BuiltInRegistries.ITEM, value);
        if (!items.isEmpty()) graphics.renderItem(new ItemStack(current(items)), x, y);
    }
}
