package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.StringValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Marks something by its id, for players without a recipe viewer to drag from: a text field and an Add button per
 * kind that can be typed ({@link GhostIngredient#typeable}: Add as item, Add as fluid), each showing what it would
 * add and greyed out while the id names nothing of its kind. Opened from the empty row of
 * {@link GhostIngredientListWidget}, client side only; the mark goes through the list's own synced action.
 */
public final class GhostIdPanel {

    public static final String TITLE_KEY = GhostIngredientHatchPartMachine.KEY + "by_id";
    /** Followed by the kind's type id. */
    public static final String ADD_KEY = TITLE_KEY + ".add.";
    public static final String UNKNOWN_KEY = TITLE_KEY + ".unknown";

    private static final int WIDTH = 196;
    private static final int INNER = WIDTH - 12;
    private static final int GAP = 4;
    /** Over a button whose kind has nothing of the id: the popup's grey, so it looks faded. */
    private static final int FADE = 0xA0C6C6C6;
    private static final int FADED_TEXT = 0xFF8B8B8B;

    private GhostIdPanel() {}

    /** @param mark adds the mark to the list */
    public static ModularPanel<?> build(Consumer<GhostIngredient<?>> mark) {
        var text = new StringValue("");
        var lookup = new Lookup(text, GhostIngredient.typeable());
        PopupPanel panel = popup("gtmqol_ghost_by_id", WIDTH, 78);
        Consumer<GhostIngredient<?>> add = ghost -> {
            mark.accept(ghost);
            text.setStringValue("");
            panel.closeIfOpen();
        };
        int count = lookup.types.size();
        int buttonWidth = (INNER - (count - 1) * GAP) / count;
        Flow buttons = Flow.row().coverChildren().childPadding(GAP);
        for (int i = 0; i < count; i++) buttons.child(addButton(lookup, i, buttonWidth, add));
        panel.child(Flow.col()
                .padding(6)
                .childPadding(5)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .child(Text.lang(TITLE_KEY).asWidget())
                .child(new IdField(() -> {
                    GhostIngredient<?> only = lookup.only();
                    if (only != null) add.accept(only);
                })
                        .size(INNER, 14)
                        // else the text only reaches the value on unfocus, after Enter or a button read it
                        .autoUpdateOnChange(true)
                        .setFocusOnGuiOpen(true)
                        .hintText(Component.literal("minecraft:water"))
                        .value(text))
                .child(buttons)
                .child(new TextWidget<>(Text.dynamic(() -> text.getStringValue().isBlank() || lookup.any() ?
                        Component.empty() : Component.translatable(UNKNOWN_KEY).withStyle(ChatFormatting.RED)))
                        .size(INNER, 9)
                        .textAlign(Alignment.Center)));
        return panel;
    }

    /** Add as (kind): the icon of what it would add and the label; faded, and ignoring clicks, while there is none. */
    private static ButtonWidget<?> addButton(Lookup lookup, int index, int width,
                                             Consumer<GhostIngredient<?>> add) {
        Component label = Component.translatable(ADD_KEY + lookup.types.get(index).id());
        IDrawable face = (context, x, y, w, h, theme) -> {
            var graphics = context.getGraphics();
            var font = Minecraft.getInstance().font;
            GhostIngredient<?> ghost = lookup.get(index);
            int left = x;
            if (ghost != null) {
                ghost.drawIcon(graphics, x + 2, y + 1);
                left += 18;
            } else {
                GuiDraw.drawRect(graphics, x, y, w, h, FADE);
            }
            int textX = left + (x + w - left - font.width(label)) / 2;
            graphics.drawString(font, label, textX, y + (h - 8) / 2, ghost != null ? theme.getTextColor() : FADED_TEXT,
                    ghost != null && theme.isTextShadow());
        };
        return new ButtonWidget<>()
                .size(width, 18)
                .overlay(face)
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    GhostIngredient<?> ghost = lookup.get(index);
                    if (ghost != null) ghost.getTooltip().forEach(tooltip::addLine);
                })
                .onMousePressed((context, button) -> {
                    GhostIngredient<?> ghost = lookup.get(index);
                    if (ghost == null) return false;
                    add.accept(ghost);
                    return true;
                });
    }

    /** What the field's text marks as each typeable kind, looked up again only when the text changes. */
    private static final class Lookup {

        private final StringValue text;
        private final List<GhostIngredient.Type<?>> types;
        private final GhostIngredient<?>[] found;
        private String looked = "";

        Lookup(StringValue text, List<GhostIngredient.Type<?>> types) {
            this.text = text;
            this.types = types;
            this.found = new GhostIngredient<?>[types.size()];
        }

        @Nullable
        GhostIngredient<?> get(int index) {
            refresh();
            return found[index];
        }

        boolean any() {
            refresh();
            return Arrays.stream(found).anyMatch(Objects::nonNull);
        }

        /** What Enter adds: the one kind that has something of the id, else nothing. */
        @Nullable
        GhostIngredient<?> only() {
            refresh();
            List<GhostIngredient<?>> all = Arrays.stream(found).filter(Objects::nonNull).toList();
            return all.size() == 1 ? all.get(0) : null;
        }

        private void refresh() {
            String now = text.getStringValue();
            if (now.equals(looked)) return;
            looked = now;
            ResourceLocation id = GhostIngredient.parseId(now);
            for (int i = 0; i < found.length; i++) found[i] = id == null ? null : types.get(i).fromId(id);
        }
    }

    /** Enter adds what {@link Lookup#only} finds, instead of only leaving the field. */
    private static final class IdField extends TextFieldWidget {

        private final Runnable submit;

        IdField(Runnable submit) {
            this.submit = submit;
        }

        @Override
        public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
            if (isFocused() && (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)) {
                submit.run();
                return Result.SUCCESS;
            }
            return super.onKeyPressed(keyCode, scanCode, modifiers);
        }
    }
}
