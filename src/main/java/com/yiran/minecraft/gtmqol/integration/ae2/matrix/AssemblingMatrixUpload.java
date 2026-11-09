package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Takes patterns right out of an encoding terminal, into the assembling matrices on its network that have it switched
 * on, when the player Shift-clicks the encode button. The client says so just before AE2's encode action arrives
 * ({@link #request}), since the server doesn't see the Shift key.
 */
public final class AssemblingMatrixUpload {

    public enum Outcome {
        /** Not a pattern the matrix runs: nothing to say. */
        NOT_OURS,
        TAKEN,
        /** A matrix has this very pattern; the copy stays in the terminal. */
        ALREADY_THERE,
        /** No matrix with auto upload on, or all full; it stays in the terminal. */
        NO_ROOM
    }

    /** Players whose next encode goes to the matrix. */
    private static final Set<UUID> REQUESTED = ConcurrentHashMap.newKeySet();

    private AssemblingMatrixUpload() {}

    /** The player's next encode is to be uploaded, or not. */
    public static void request(Player player, boolean upload) {
        if (upload) REQUESTED.add(player.getUUID());
        else REQUESTED.remove(player.getUUID());
    }

    /** Whether this encode was asked to be uploaded; the request is used up. */
    public static boolean takeRequest(Player player) {
        return REQUESTED.remove(player.getUUID());
    }

    public static Outcome upload(IGrid grid, ItemStack pattern) {
        if (AssemblingMatrix.CONTROLLER == null || !PatternCrafter.isPattern(pattern)) return Outcome.NOT_OURS;
        List<InternalInventory> inventories = new ArrayList<>();
        for (AssemblingMatrixInterfaceMachine machine : grid.getMachines(AssemblingMatrixInterfaceMachine.class)) {
            InternalInventory inventory = machine.uploadInventory();
            if (inventory != null) inventories.add(inventory);
        }
        for (InternalInventory inventory : inventories) {
            for (ItemStack stack : inventory) {
                if (ItemStack.isSameItemSameTags(stack, pattern)) return Outcome.ALREADY_THERE;
            }
        }
        for (InternalInventory inventory : inventories) {
            if (inventory.addItems(pattern.copy()).isEmpty()) return Outcome.TAKEN;
        }
        return Outcome.NO_ROOM;
    }
}
