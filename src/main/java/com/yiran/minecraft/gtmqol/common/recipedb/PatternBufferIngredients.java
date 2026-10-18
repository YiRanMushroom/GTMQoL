package com.yiran.minecraft.gtmqol.common.recipedb;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.core.mixins.ae2.MEPatternBufferWorkerPatternAccessor;
import com.yiran.minecraft.gtmqol.integration.ae2.AbstractMEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.PatternOutputMarks;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeSlot;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A pattern buffer (or its proxy) is one BUS_DISTINCT handler list, but its item/fluid handlers aggregate every
 * worker slot, and {@code BufferRecipeHandlerList.handleRecipe} only ever uses one worker plus the circuit and
 * shared inventories. Split it the same way here, with what our buffers add: a slot with Shared Hatches on also
 * combines with the machine's undyed parts, and with Mark Pattern Outputs on it holds its pattern's output mark.
 * Only gtceu's buffer and ours: another mod's subclass may use its slots its own way, so it isn't split (and as an
 * unknown handler list, it isn't restricted either). Only loaded when AE2 is.
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
        boolean shares = handlerList instanceof SharedHatches.HandlerList shared && shared.gtmqol$sharesHatches();
        // the buffer's own list only: proxies don't get the marks (PatternOutputMarks)
        boolean marks = handlerList.getClass().getEnclosingClass() == MEPatternBufferPartMachine.class &&
                buffer instanceof AbstractMEPatternBufferPartMachine ours && ours.marksPatternOutputs();
        list.addContents(ItemRecipeCapability.CAP, buffer.getCircuitSlot().getContents(), kind, owner, catalyst,
                shares);
        list.addContents(ItemRecipeCapability.CAP, buffer.getShareInventory().getContents(), kind, owner, catalyst,
                shares);
        list.addContents(FluidRecipeCapability.CAP, buffer.getShareTank().getContents(), kind, owner, catalyst,
                shares);
        int slot = GroupedIngredientList.WHOLE;
        // workers and Worker.slot are public through our AT
        for (var worker : buffer.workers) {
            var internalSlot = worker.slot;
            slot++;
            list.addContents(ItemRecipeCapability.CAP, new ArrayList<Object>(internalSlot.getItems()), kind, owner,
                    slot, shares);
            list.addContents(FluidRecipeCapability.CAP, new ArrayList<Object>(internalSlot.getFluids()), kind, owner,
                    slot, shares);
            var stackLike = (PatternBufferStackLikeSlot) internalSlot;
            for (var bridge : AEStackLikeBridges.all()) {
                list.addContents(bridge.cap(), stackLike.gtmqol$getStackLikeHandler(bridge).getContents(), kind,
                        owner, slot, shares);
            }
            if (marks) {
                GhostIngredient<?> mark = PatternOutputMarks.of(
                        ((MEPatternBufferWorkerPatternAccessor) (Object) worker).gtmqol$getWorkerPattern());
                if (mark != null) {
                    list.addContents(GhostIngredientRecipeCapability.CAP, List.<Object>of(mark), kind, owner, slot, shares);
                }
            }
        }
        return true;
    }

    private static @Nullable MEPatternBufferPartMachine bufferOf(RecipeHandlerList handlerList) {
        MEPatternBufferPartMachine buffer = anyBufferOf(handlerList);
        return buffer != null && (buffer.getClass() == MEPatternBufferPartMachine.class ||
                buffer instanceof AbstractMEPatternBufferPartMachine) ? buffer : null;
    }

    private static @Nullable MEPatternBufferPartMachine anyBufferOf(RecipeHandlerList handlerList) {
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
            if (machine instanceof MEPatternBufferProxyPartMachine proxy &&
                    proxy.getClass() == MEPatternBufferProxyPartMachine.class) {
                return proxy.getBuffer();
            }
            return null;
        }
        return null;
    }
}
