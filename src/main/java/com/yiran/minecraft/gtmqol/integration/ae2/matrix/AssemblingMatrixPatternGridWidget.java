package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixMachine.PatternEntry;

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
 * All the patterns of a matrix as an inventory, each shown as what it makes. Slots would not do: a big matrix holds tens
 * of thousands. So this draws only the rows in view of its scrolling parent, and its height follows the list. Works as
 * an inventory too: a click picks a pattern up, Shift sends it to the player's inventory, and a click with a pattern in
 * hand puts it in. Client side only; clicks go out through the callbacks.
 */
// Untested: the patterns window opens and updates quickly with thousands of patterns, icons and tooltips as before
class AssemblingMatrixPatternGridWidget extends Widget<AssemblingMatrixPatternGridWidget> implements Interactable {

    static final int COLUMNS = 9;
    static final int CELL = 18;

    private record Shown(PatternEntry entry, ItemStack icon, String name) {}

    private final Supplier<List<PatternEntry>> entries;
    private final Supplier<String> search;
    private final BiConsumer<PatternEntry, Boolean> onTake;
    private final Runnable onPut;
    private List<PatternEntry> seenEntries = List.of();
    private String seenSearch = "";
    private List<Shown> all = List.of();
    private List<Shown> shown = List.of();
    private int rows = -1;
    // the hovered pattern's tooltip, built again only when that changes: building it can decode the pattern
    private @Nullable Shown tooltipFor;
    private boolean tooltipShift;
    private boolean tooltipAdvanced;
    private List<Component> tooltip = List.of();

    /** @param onTake the entry, and whether it goes to the inventory instead of the hand */
    AssemblingMatrixPatternGridWidget(Supplier<List<PatternEntry>> entries, Supplier<String> search,
                            BiConsumer<PatternEntry, Boolean> onTake, Runnable onPut) {
        this.entries = entries;
        this.search = search;
        this.onTake = onTake;
        this.onPut = onPut;
        size(COLUMNS * CELL, CELL);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        List<PatternEntry> list = entries.get();
        String text = search.get().toLowerCase(Locale.ROOT);
        if (list.equals(seenEntries) && text.equals(seenSearch)) return;
        if (!list.equals(seenEntries)) {
            seenEntries = List.copyOf(list);
            all = list.stream().map(AssemblingMatrixPatternGridWidget::shown).toList();
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

    /** The icon comes from the server, which has the pattern decoded already. */
    private static Shown shown(PatternEntry entry) {
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
            Component empty = Component.translatable(AssemblingMatrixMachine.KEY + "patterns.empty");
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
            graphics.renderItemDecorations(font, icon, x + 1, y + 1);
            if (i == hovered) graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x80FFFFFF);
        }
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
            lines.add(Component.translatable(AssemblingMatrixMachine.KEY + "patterns.take")
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
