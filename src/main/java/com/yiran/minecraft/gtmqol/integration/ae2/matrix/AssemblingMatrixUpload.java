package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;

import java.util.ArrayList;
import java.util.List;

/** Takes patterns right out of an encoding terminal, into the assembling matrices on its network that have it switched on. */
public final class AssemblingMatrixUpload {

    public enum Outcome {
        /** It stays where it is. */
        NOT_TAKEN,
        TAKEN,
        /** A matrix has this very pattern: the copy has no use. */
        ALREADY_THERE
    }

    private AssemblingMatrixUpload() {}

    public static Outcome upload(IGrid grid, ItemStack pattern) {
        if (AssemblingMatrix.CONTROLLER == null || !PatternCrafter.isPattern(pattern)) return Outcome.NOT_TAKEN;
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
        return Outcome.NOT_TAKEN;
    }
}
