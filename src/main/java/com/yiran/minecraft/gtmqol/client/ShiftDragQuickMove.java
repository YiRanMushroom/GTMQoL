package com.yiran.minecraft.gtmqol.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.screen.ModularContainerMenu;
import brachy.modularui.widgets.slot.ModularSlot;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Shift + drag over slots in ModularUI windows (every GTCEu machine): with Shift held and nothing on the cursor, each
 * slot the mouse passes over is Shift-clicked once, as Mouse Tweaks does in other screens. ModularUI's slot widgets take
 * the mouse press and drag for themselves, so Mouse Tweaks never sees them there.
 */
// Untested: Shift + drag over the inventory moves stacks into a GTCEu machine, and over machine slots back out;
// dragging a held stack still spreads it
public final class ShiftDragQuickMove {

    private static final Set<Slot> visited = new HashSet<>();
    private static boolean armed;

    private ShiftDragQuickMove() {}

    public static void init() {
        var bus = MinecraftForge.EVENT_BUS;
        // before ModularUI takes the press, which it then cancels for the others
        bus.addListener(EventPriority.HIGH, false, ScreenEvent.MouseButtonPressed.Pre.class,
                ShiftDragQuickMove::onPressed);
        // ModularUI cancels drags over its widgets
        bus.addListener(EventPriority.LOWEST, true, ScreenEvent.MouseDragged.Pre.class, ShiftDragQuickMove::onDragged);
        bus.addListener(EventPriority.LOWEST, true, ScreenEvent.MouseButtonReleased.Pre.class, event -> reset());
        bus.addListener(EventPriority.NORMAL, false, ScreenEvent.Closing.class, event -> reset());
    }

    private static void onPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        reset();
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT || !Screen.hasShiftDown()) return;
        Slot slot = slotToMove(event.getScreen());
        if (slot == null) return;
        armed = true;
        // ModularUI's own Shift-click moves this one
        visited.add(slot);
    }

    private static void onDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!armed || event.getMouseButton() != InputConstants.MOUSE_BUTTON_LEFT || !Screen.hasShiftDown()) return;
        Slot slot = slotToMove(event.getScreen());
        if (slot == null || !slot.hasItem() || !visited.add(slot)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null || minecraft.player == null) return;
        var menu = ((AbstractContainerScreen<?>) event.getScreen()).getMenu();
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, InputConstants.MOUSE_BUTTON_LEFT,
                ClickType.QUICK_MOVE, minecraft.player);
    }

    /** The slot under the mouse in a ModularUI window, when a Shift-click on it could move something. */
    private static @Nullable Slot slotToMove(Screen screen) {
        if (!(screen instanceof IMuiScreen) || !(screen instanceof AbstractContainerScreen<?> container) ||
                !(container.getMenu() instanceof ModularContainerMenu menu)) {
            return null;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || !menu.getCarried().isEmpty()) return null;
        Slot slot = container.getSlotUnderMouse();
        // phantom slots aren't among the menu's slots
        if (!(slot instanceof ModularSlot) || !menu.slots.contains(slot) || !slot.isActive() || !slot.mayPickup(player)) {
            return null;
        }
        return slot;
    }

    private static void reset() {
        armed = false;
        visited.clear();
    }
}
