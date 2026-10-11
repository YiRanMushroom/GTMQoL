package com.yiran.minecraft.gtmqol.common.stacklike;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

/**
 * A stack-like slot's amount in buckets, drawn like gtceu's fluid amounts ({@code ContentOverlay.drawFluidAmount}),
 * which only handles fluid ingredients.
 */
@OnlyIn(Dist.CLIENT)
public record StackLikeAmountOverlay(long amount) implements IDrawable {

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        var graphics = context.getGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        Font font = Minecraft.getInstance().font;
        String s = FormattingUtil.formatBuckets(amount);
        if (font.width(s) > 32)
            s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_1F, "B");
        if (font.width(s) > 32)
            s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_0F, "B");
        graphics.drawString(font, s, (int) ((x + (width / 3f)) * 2 - font.width(s) + 22),
                (int) ((y + (height / 3f) + 6) * 2), 0xFFFFFF, true);
        graphics.pose().popPose();
    }
}
