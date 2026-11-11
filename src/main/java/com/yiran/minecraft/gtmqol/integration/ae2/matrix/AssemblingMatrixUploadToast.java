package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload.Outcome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The toast for {@link AssemblingMatrixUploadMessage}: the product's icon, whether it uploaded, and the product's name
 * (or why it couldn't). Toasts show above any open window. A pattern encoded while the toast shows replaces it, so
 * encoding many in a row doesn't queue up one toast each.
 */
// Untested: Shift-clicking encode uploads and shows the toast over the terminal; a copy or no room says so and the
// pattern stays in the terminal; a plain click doesn't upload
@OnlyIn(Dist.CLIENT)
public final class AssemblingMatrixUploadToast implements Toast {

    private static final long DISPLAY_TIME = 5000;
    private static final int TEXT_X = 30;

    private ItemStack icon = ItemStack.EMPTY;
    private Component title = Component.empty();
    private Component detail = Component.empty();
    private int width = 160;
    private boolean changed;
    private long lastChanged;

    private AssemblingMatrixUploadToast() {}

    /** For the encode click: Shift uploads. */
    public static void requestUpload() {
        AssemblingMatrixUploadMessage.sendRequest(Screen.hasShiftDown());
    }

    static void show(ItemStack icon, Outcome outcome) {
        if (outcome == Outcome.NOT_OURS) return;
        ToastComponent toasts = Minecraft.getInstance().getToasts();
        AssemblingMatrixUploadToast toast = toasts.getToast(AssemblingMatrixUploadToast.class, NO_TOKEN);
        boolean shown = toast != null;
        if (!shown) toast = new AssemblingMatrixUploadToast();
        toast.icon = icon;
        boolean taken = outcome == Outcome.TAKEN;
        toast.title = Component.translatable(AssemblingMatrixMachine.KEY + (taken ? "uploaded" : "cannot_upload"));
        toast.detail = taken ? icon.getHoverName() : Component.translatable(AssemblingMatrixMachine.KEY +
                (outcome == Outcome.ALREADY_THERE ? "already_there" : "no_room"));
        Font font = Minecraft.getInstance().font;
        toast.width = Math.max(160, TEXT_X + Math.max(font.width(toast.title), font.width(toast.detail)) + 8);
        toast.changed = true;
        if (!shown) toasts.addToast(toast);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible) {
        if (changed) {
            lastChanged = timeSinceLastVisible;
            changed = false;
        }
        drawBackground(graphics);
        Font font = toasts.getMinecraft().font;
        graphics.drawString(font, title, TEXT_X, 7, 0xFFFFFF00, false);
        graphics.drawString(font, detail, TEXT_X, 18, 0xFFFFFFFF, false);
        graphics.renderFakeItem(icon, 8, 8);
        double displayTime = DISPLAY_TIME * toasts.getNotificationDisplayTimeMultiplier();
        return timeSinceLastVisible - lastChanged >= displayTime ? Visibility.HIDE : Visibility.SHOW;
    }

    /** The advancement toast's background, made wider the way vanilla's system toasts are: ends kept, middle repeated. */
    private void drawBackground(GuiGraphics graphics) {
        int left = 20;
        int right = 60;
        // the advancement background is the top of TEXTURE
        graphics.blit(TEXTURE, 0, 0, 0, 0, left, 32);
        for (int x = left; x < width - right; x += 64) {
            graphics.blit(TEXTURE, x, 0, 32, 0, Math.min(64, width - right - x), 32);
        }
        graphics.blit(TEXTURE, width - right, 0, 160 - right, 0, right, 32);
    }
}
