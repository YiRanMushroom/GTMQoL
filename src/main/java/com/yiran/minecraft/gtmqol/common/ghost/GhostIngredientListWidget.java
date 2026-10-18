package com.yiran.minecraft.gtmqol.common.ghost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The list of a {@link GhostIngredientHatchPartMachine}: one row per mark (icon and name), plus an empty row at the
 * bottom which marking adds to, so the list grows as you mark. Its height follows the synced list; put it in a
 * scrolling parent.
 *
 * <p>Left click marks the held item, right click what the held container holds, an empty hand removes the row.
 * Recipe viewer drags mark what is dragged. The ghost slot takes any ingredient ({@code Object}): JEI hands over
 * its own stacks (items, fluids), asking {@link #castGhostIngredientIfValid} for each; EMI goes through
 * {@link #ingredientHandlingOverride}.</p>
 */
public class GhostIngredientListWidget extends Widget<GhostIngredientListWidget>
                                       implements Interactable, GhostIngredientSlot<Object> {

    public static final String PREFIX = "gtmqol_ghost_";
    /** Index, mouse button. */
    public static final String CLICK = PREFIX + "click";
    /** Index, a {@link GhostIngredient}. */
    public static final String SET = PREFIX + "set";

    public static final int ROW = 18;

    private static final boolean EMI_LOADED = ModList.get().isLoaded("emi");

    private final Supplier<List<GhostIngredient<?>>> marks;
    private final PanelSyncManager syncManager;
    private final int width;
    private int rows = -1;

    // Local and already scrolled: the parent's scroll is a transform of the context.
    private double lastMouseY = -1;

    /** @param marks the client's copy of the list */
    public GhostIngredientListWidget(Supplier<List<GhostIngredient<?>>> marks, PanelSyncManager syncManager,
                                     int width) {
        this.marks = marks;
        this.syncManager = syncManager;
        this.width = width;
        updateSize();
    }

    private void updateSize() {
        int newRows = marks.get().size() + 1;
        if (newRows == rows) return;
        rows = newRows;
        size(width, rows * ROW);
        if (isValid()) scheduleResize();
    }

    @Override
    public void onInit() {
        super.onInit();
        // recipe viewer drops only reach registered ghost slots
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        updateSize();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
        var font = Minecraft.getInstance().font;
        List<GhostIngredient<?>> list = marks.get();
        lastMouseY = isHovering() ? context.getMouseY() : -1;
        int hovered = rowAt(lastMouseY);
        for (int i = 0; i <= list.size(); i++) {
            int y = i * ROW;
            GuiTextures.SLOT_ITEM.draw(context, 0, y, 18, 18);
            if (i < list.size()) {
                list.get(i).drawIcon(graphics, 1, y + 1);
                graphics.drawString(font, list.get(i).getName(), 22, y + 5, 0x404040, false);
            } else {
                GuiTextures.ADD.draw(context, 4, y + 4, 10, 10);
            }
            if (i == hovered) graphics.fill(1, y + 1, 17, y + 17, 0x80FFFFFF);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering()) return;
        int index = rowAt(context.getMouseY());
        if (index < 0) return;
        List<GhostIngredient<?>> list = marks.get();
        List<Component> lines = new ArrayList<>();
        if (index < list.size()) lines.addAll(list.get(index).getTooltip());
        lines.add(Component.translatable(GhostIngredientHatchPartMachine.KEY + "hint.mark"));
        if (index < list.size()) lines.add(Component.translatable(GhostIngredientHatchPartMachine.KEY + "hint.remove"));
        context.getGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines,
                context.getAbsMouseX(), context.getAbsMouseY());
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0 && button != 1) return Result.IGNORE;
        int index = rowAt(getContext().getMouseY());
        if (index < 0) return Result.IGNORE;
        syncManager.callSyncedAction(CLICK, buf -> {
            buf.writeVarInt(index);
            buf.writeVarInt(button);
        });
        return Result.SUCCESS;
    }

    // --- GhostIngredientSlot ---

    @Override
    public void setGhostIngredient(Object ingredient) {
        GhostIngredient<?> ghost = GhostIngredient.ofValue(ingredient);
        if (ghost != null) send(ghost);
    }

    @Override
    public boolean ingredientHandlingOverride(Object ingredient) {
        if (!EMI_LOADED) return false;
        GhostIngredient<?> ghost = GhostIngredientEmi.fromEmi(ingredient);
        if (ghost == null) return false;
        send(ghost);
        return true;
    }

    @Override
    public @Nullable Object castGhostIngredientIfValid(Object ingredient) {
        if (!areAncestorsEnabled()) return null;
        return GhostIngredient.ofValue(ingredient) != null ? ingredient : null;
    }

    @Override
    public Class<Object> ingredientClass() {
        return Object.class;
    }

    /** Onto the hovered row, else a new one at the bottom. */
    private void send(GhostIngredient<?> ghost) {
        int hovered = rowAt(lastMouseY);
        int index = hovered >= 0 ? hovered : marks.get().size();
        syncManager.callSyncedAction(SET, buf -> {
            buf.writeVarInt(index);
            GhostIngredient.write(buf, ghost);
        });
    }

    /** The row (the empty one included) at a local y, or -1. */
    private int rowAt(double localY) {
        if (localY < 0) return -1;
        int row = (int) (localY / ROW);
        return row <= marks.get().size() ? row : -1;
    }
}
