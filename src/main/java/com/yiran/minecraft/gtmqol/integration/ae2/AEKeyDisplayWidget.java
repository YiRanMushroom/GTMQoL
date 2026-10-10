package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.AEGuiHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.ITheme;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * gtceu's {@code AEStackDisplayWidget}, drawn by AE's key renderers so any key type shows (that one only draws
 * items and fluids). Reads from a shared list by index, so amount updates don't rebuild it.
 */
public class AEKeyDisplayWidget extends Widget<AEKeyDisplayWidget> {

    private final List<GenericStack> source;
    private final int index;

    public AEKeyDisplayWidget(List<GenericStack> source, int index) {
        this.source = source;
        this.index = index;
        size(18);
    }

    private @Nullable GenericStack getStack() {
        return index < source.size() ? source.get(index) : null;
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GTGuiTextures.SLOT_DARK.draw(context, 0, 0, 18, 18);
        GenericStack stack = getStack();
        if (stack == null) return;
        AEKeyRendering.drawInGui(Minecraft.getInstance(), context.getGraphics(), 1, 1, stack.what());
        AEGuiHelper.drawAmountOverlay(context.getGraphics(), stack.amount(), 1, 1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering()) return;
        GenericStack stack = getStack();
        if (stack == null) return;
        AEGuiHelper.drawSelectionOverlay(context.getGraphics(), 1, 1, 16, 16);
        List<Component> lines = new ArrayList<>(AEKeyRendering.getTooltip(stack.what()));
        lines.add(Component.literal("x" + AEGuiHelper.formatAmountFull(stack.amount()))
                .withStyle(ChatFormatting.GRAY));
        context.getGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines,
                context.getAbsMouseX(), context.getAbsMouseY());
    }
}
