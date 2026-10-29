package com.yiran.minecraft.gtmqol.common.wireless.channel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * The channels of one kind, one row each: color, name (the bound one highlighted), access, and an edit button on
 * channels the player may edit. Clicking a row binds to it. Its height follows the synced list, so it shows any
 * number of channels; put it in a scrolling parent. Client side only; clicks go out through the callbacks.
 */
public class ChannelListWidget extends Widget<ChannelListWidget> implements Interactable {

    public static final int ROW = 16;
    private static final int EDIT_WIDTH = 14;

    private final Supplier<List<ChannelInfo>> channels;
    private final IntSupplier boundId;
    private final Consumer<ChannelInfo> onBind;
    private final Consumer<ChannelInfo> onEdit;
    private final int width;
    private int rows = -1;

    public ChannelListWidget(Supplier<List<ChannelInfo>> channels, IntSupplier boundId, Consumer<ChannelInfo> onBind,
                             Consumer<ChannelInfo> onEdit, int width) {
        this.channels = channels;
        this.boundId = boundId;
        this.onBind = onBind;
        this.onEdit = onEdit;
        this.width = width;
        updateSize();
    }

    private void updateSize() {
        int newRows = Math.max(1, channels.get().size());
        if (newRows == rows) return;
        rows = newRows;
        size(width, rows * ROW);
        if (isValid()) scheduleResize();
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
        List<ChannelInfo> list = channels.get();
        if (list.isEmpty()) {
            Component empty = Component.translatable(ChannelUI.LIST_EMPTY_KEY);
            graphics.drawString(font, empty, (width - font.width(empty)) / 2, 4, 0x808080, false);
            return;
        }
        int hovered = isHovering() ? rowAt(context.getMouseY()) : -1;
        boolean overEdit = isHovering() && context.getMouseX() >= width - EDIT_WIDTH;
        for (int i = 0; i < list.size(); i++) {
            ChannelInfo channel = list.get(i);
            int y = i * ROW;
            boolean bound = channel.id() == boundId.getAsInt();
            if (bound) {
                graphics.fill(0, y, width, y + ROW, 0x40000000 | channel.color());
            } else if (i == hovered && !(overEdit && channel.canEdit())) {
                graphics.fill(0, y, width, y + ROW, 0x30FFFFFF);
            }
            graphics.fill(3, y + 4, 11, y + 12, 0xFF000000 | channel.color());
            int right = width - (channel.canEdit() ? EDIT_WIDTH + 2 : 2);
            Component access = Component.translatable(channel.access().shortLangKey());
            int accessX = right - font.width(access);
            graphics.drawString(font, access, accessX, y + 4, 0x808080, false);
            String name = font.plainSubstrByWidth(channel.name(), accessX - 18);
            graphics.drawString(font, Component.literal(name)
                    .withStyle(channel.active() ? ChatFormatting.RESET : ChatFormatting.STRIKETHROUGH),
                    15, y + 4, bound ? 0x202020 : 0x404040, false);
            if (channel.canEdit()) {
                int x = width - EDIT_WIDTH;
                if (i == hovered && overEdit) graphics.fill(x, y + 1, width, y + ROW - 1, 0x30FFFFFF);
                graphics.drawString(font, "✎", x + 3, y + 4, 0x404040, false);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering()) return;
        int index = rowAt(context.getMouseY());
        List<ChannelInfo> list = channels.get();
        if (index < 0 || index >= list.size()) return;
        ChannelInfo channel = list.get(index);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(channel.name()).withStyle(style -> style.withColor(channel.color())));
        lines.add(Component.translatable(ChannelUI.ROW_OWNER_KEY, channel.owner()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(channel.access().langKey()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(ChannelUI.ROW_COUNTS_KEY, channel.transmitters(), channel.receivers(),
                channel.routers()).withStyle(ChatFormatting.GRAY));
        if (!channel.active()) {
            lines.add(Component.translatable(ChannelUI.ROW_INACTIVE_KEY).withStyle(ChatFormatting.RED));
        }
        boolean overEdit = channel.canEdit() && context.getMouseX() >= width - EDIT_WIDTH;
        lines.add(Component.translatable(overEdit ? ChannelUI.ROW_EDIT_HINT_KEY :
                channel.id() == boundId.getAsInt() ? ChannelUI.ROW_BOUND_HINT_KEY : ChannelUI.ROW_BIND_HINT_KEY)
                .withStyle(ChatFormatting.YELLOW));
        context.getGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines,
                context.getAbsMouseX(), context.getAbsMouseY());
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0) return Result.IGNORE;
        int index = rowAt(getContext().getMouseY());
        List<ChannelInfo> list = channels.get();
        if (index < 0 || index >= list.size()) return Result.IGNORE;
        ChannelInfo channel = list.get(index);
        if (channel.canEdit() && getContext().getMouseX() >= width - EDIT_WIDTH) {
            onEdit.accept(channel);
        } else if (channel.id() != boundId.getAsInt()) {
            onBind.accept(channel);
        } else {
            return Result.SUCCESS;
        }
        // the rows act as buttons
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    private int rowAt(double localY) {
        if (localY < 0) return -1;
        return (int) (localY / ROW);
    }
}
