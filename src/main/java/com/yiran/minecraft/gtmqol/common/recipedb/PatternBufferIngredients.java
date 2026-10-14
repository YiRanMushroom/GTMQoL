package com.yiran.minecraft.gtmqol.common.recipedb;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeSlot;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A pattern buffer (or its proxy) is one BUS_DISTINCT handler list, but its item/fluid handlers aggregate every
 * worker slot, and {@code BufferRecipeHandlerList.handleRecipe} only ever uses one worker plus the circuit and
 * shared inventories. Split it the same way here. Only loaded when AE2 is.
 */
final class PatternBufferIngredients {

    private PatternBufferIngredients() {}

    /** @return false if {@code handlerList} is not a pattern buffer's, nothing was added */
    static boolean addSplit(GroupedIngredientList list, RecipeHandlerList handlerList, int owner) {
        MEPatternBufferPartMachine buffer = bufferOf(handlerList);
        if (buffer == null) {
            return false;
        }
        int kind = GroupedIngredientList.DISTINCT;
        int catalyst = GroupedIngredientList.CATALYST;
        list.addContents(ItemRecipeCapability.CAP, buffer.getCircuitSlot().getContents(), kind, owner, catalyst);
        list.addContents(ItemRecipeCapability.CAP, buffer.getShareInventory().getContents(), kind, owner, catalyst);
        list.addContents(FluidRecipeCapability.CAP, buffer.getShareTank().getContents(), kind, owner, catalyst);
        int slot = GroupedIngredientList.WHOLE;
        // workers and Worker.slot are public through our AT
        for (var worker : buffer.workers) {
            var internalSlot = worker.slot;
            slot++;
            list.addContents(ItemRecipeCapability.CAP, new ArrayList<Object>(internalSlot.getItems()), kind, owner,
                    slot);
            list.addContents(FluidRecipeCapability.CAP, new ArrayList<Object>(internalSlot.getFluids()), kind, owner,
                    slot);
            var stackLike = (PatternBufferStackLikeSlot) internalSlot;
            for (var bridge : AEStackLikeBridges.all()) {
                list.addContents(bridge.cap(), stackLike.gtmqol$getStackLikeHandler(bridge).getContents(), kind,
                        owner, slot);
            }
        }
        return true;
    }

    private static @Nullable MEPatternBufferPartMachine bufferOf(RecipeHandlerList handlerList) {
        // BufferRecipeHandlerList and ProxyRHL are not visible from here.
        Class<?> enclosing = handlerList.getClass().getEnclosingClass();
        if (enclosing != MEPatternBufferPartMachine.class && enclosing != ProxySlotRecipeHandler.class) {
            return null;
        }
        for (var handler : handlerList.getHandlersFlat()) {
            if (!(handler instanceof MachineTrait trait)) {
                continue;
            }
            MetaMachine machine = trait.getMachine();
            if (machine instanceof MEPatternBufferPartMachine buffer) {
                return buffer;
            }
            if (machine instanceof MEPatternBufferProxyPartMachine proxy) {
                return proxy.getBuffer();
            }
            return null;
        }
        return null;
    }
}
