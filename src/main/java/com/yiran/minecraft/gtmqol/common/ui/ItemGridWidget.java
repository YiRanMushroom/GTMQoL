package com.yiran.minecraft.gtmqol.common.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.AbstractScrollWidget;
import brachy.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The items of many inventories as one, each shown by its icon (the Assembling Matrix's patterns as what they make, the
 * ME Data Center's crafting units). Slots would not do: a big multiblock holds tens of thousands. So this draws only the
 * rows in view of its scrolling parent (a {@code ListWidget}), and its height follows the list. Works as an inventory
 * too: a click picks an item up, Shift sends it to the player's inventory, and a click with an item in hand puts it
 * in. Client side only; clicks go out through the callbacks.
 */
// Untested: the patterns window opens and updates quickly with thousands of patterns, icons and tooltips as before
public class ItemGridWidget extends Widget<ItemGridWidget> implements Interactable {

    public static final int COLUMNS = 9;
    public static final int CELL = 18;

    private record Shown(ItemGridEntry entry, ItemStack icon, String name) {}

    private final Supplier<List<ItemGridEntry>> entries;
    private final Supplier<String> search;
    private final BiConsumer<ItemGridEntry, Boolean> onTake;
    private final Runnable onPut;
    private final String emptyKey;
    private final String takeKey;
    private List<ItemGridEntry> seenEntries = List.of();
    private String seenSearch = "";
    private List<Shown> all = List.of();
    private List<Shown> shown = List.of();
    private int rows = -1;
    // the hovered item's tooltip, built again only when that changes: building it can decode a pattern
    private @Nullable Shown tooltipFor;
    private boolean tooltipShift;
    private boolean tooltipAdvanced;
    private List<Component> tooltip = List.of();

    /**
     * @param onTake   the entry, and whether it goes to the inventory instead of the hand
     * @param emptyKey lang key of the line shown when there is nothing
     * @param takeKey  lang key of the line under an item's tooltip
     */
    public ItemGridWidget(Supplier<List<ItemGridEntry>> entries, Supplier<String> search,
                          BiConsumer<ItemGridEntry, Boolean> onTake, Runnable onPut, String emptyKey,
                          String takeKey) {
        this.entries = entries;
        this.search = search;
        this.onTake = onTake;
        this.onPut = onPut;
        this.emptyKey = emptyKey;
        this.takeKey = takeKey;
        size(COLUMNS * CELL, CELL);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        List<ItemGridEntry> list = entries.get();
        String text = search.get().toLowerCase(Locale.ROOT);
        // the list may have got shorter than where it was scrolled to (the size changes a frame later)
        if (getParent() instanceof AbstractScrollWidget<?, ?> scroll && scroll.getScrollArea().getScrollY() != null) {
            scroll.getScrollArea().getScrollY().clamp(scroll.getScrollArea());
        }
        if (list.equals(seenEntries) && text.equals(seenSearch)) return;
        if (!text.equals(seenSearch) && getParent() instanceof AbstractScrollWidget<?, ?> scroll &&
                scroll.getScrollArea().getScrollY() != null) {
            // a new search starts at the top, else the matches can be above what is in view
            scroll.getScrollArea().getScrollY().scrollTo(scroll.getScrollArea(), 0);
        }
        if (!list.equals(seenEntries)) {
            seenEntries = List.copyOf(list);
            all = list.stream().map(ItemGridWidget::shown).toList();
        }
        seenSearch = text;
        List<Shown> matching = new ArrayList<>();
        for (Shown entry : all) {
            if (entry.name.contains(text)) matching.add(entry);
        }
        shown = matching;
        int newRows = Math.max(1, (shown.size() + COLUMNS - 1) / COLUMNS);
        if (newRows == rows) return;
        rows = newRows;
        size(COLUMNS * CELL, rows * CELL);
        if (isValid()) scheduleResize();
    }

    /** The icon comes from the server. */
    private static Shown shown(ItemGridEntry entry) {
        ItemStack icon = entry.icon();
        return new Shown(entry, icon, icon.getHoverName().getString().toLowerCase(Locale.ROOT));
    }

    /** The first row in view; {@code getScrollY} is how far the parent has moved this widget up. */
    private int firstRow() {
        return getParent() instanceof AbstractScrollWidget<?, ?> scroll ? scroll.getScrollY() / CELL : 0;
    }

    private int rowsInView() {
        return getParent().getArea().height / CELL + 2;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
        var font = Minecraft.getInstance().font;
        if (shown.isEmpty()) {
            Component empty = Component.translatable(emptyKey);
            graphics.drawString(font, empty, (COLUMNS * CELL - font.width(empty)) / 2, 4, 0x808080, false);
            return;
        }
        int hovered = isHovering() ? indexAt(context.getMouseX(), context.getMouseY()) : -1;
        int first = firstRow() * COLUMNS;
        int end = Math.min(first + rowsInView() * COLUMNS, shown.size());
        for (int i = first; i < end; i++) {
            int x = i % COLUMNS * CELL;
            int y = i / COLUMNS * CELL;
            graphics.fill(x, y, x + CELL, y + CELL, 0xFF8B8B8B);
            graphics.fill(x, y, x + CELL - 1, y + 1, 0xFF373737);
            graphics.fill(x, y, x + 1, y + CELL - 1, 0xFF373737);
            graphics.fill(x + 1, y + CELL - 1, x + CELL, y + CELL, 0xFFFFFFFF);
            graphics.fill(x + CELL - 1, y + 1, x + CELL, y + CELL, 0xFFFFFFFF);
            ItemStack icon = shown.get(i).icon;
            graphics.renderItem(icon, x + 1, y + 1);
            graphics.renderItemDecorations(font, icon, x + 1, y + 1, countText(shown.get(i).entry.count()));
            if (i == hovered) graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x80FFFFFF);
        }
    }

    /** How many an entry stands for, short enough for a slot; nothing for one. */
    private static @Nullable String countText(int count) {
        if (count <= 1) return null;
        if (count < 1000) return Integer.toString(count);
        if (count < 1_000_000) return count / 1000 + "k";
        return count / 1_000_000 + "M";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering()) return;
        int index = indexAt(context.getMouseX(), context.getMouseY());
        if (index < 0) return;
        var minecraft = Minecraft.getInstance();
        Shown hovered = shown.get(index);
        boolean shift = Screen.hasShiftDown();
        boolean advanced = minecraft.options.advancedItemTooltips;
        if (hovered != tooltipFor || shift != tooltipShift || advanced != tooltipAdvanced) {
            List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, hovered.entry.stack()));
            lines.add(Component.translatable(takeKey)
                    .withStyle(ChatFormatting.YELLOW));
            tooltip = lines;
            tooltipFor = hovered;
            tooltipShift = shift;
            tooltipAdvanced = advanced;
        }
        context.getGraphics().renderComponentTooltip(minecraft.font, tooltip, context.getAbsMouseX(),
                context.getAbsMouseY());
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0) return Result.IGNORE;
        if (!Minecraft.getInstance().player.containerMenu.getCarried().isEmpty()) {
            onPut.run();
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        int index = indexAt(getContext().getMouseX(), getContext().getMouseY());
        if (index < 0) return Result.IGNORE;
        onTake.accept(shown.get(index).entry, Screen.hasShiftDown());
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    private int indexAt(double x, double y) {
        if (x < 0 || y < 0 || x >= COLUMNS * CELL) return -1;
        int index = (int) (y / CELL) * COLUMNS + (int) (x / CELL);
        return index < shown.size() ? index : -1;
    }
}
