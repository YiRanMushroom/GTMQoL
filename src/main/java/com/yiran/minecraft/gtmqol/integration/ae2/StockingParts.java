package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/**
 * What our stocking lists (the dual input's fluids, the universal input) share with gtceu's stocking bus and hatch,
 * whose versions are private: syncing and auto pulling a slot list, and the check that only one part of a multiblock
 * stocks a key (it would be counted twice).
 */
public final class StockingParts {

    private StockingParts() {}

    /** MEStockingBusPartMachine#syncME on any list: a configured slot's stock is what the network has, if enough. */
    static void syncStock(IConfigurableSlotList list, MEStorage network, IActionSource source, int minStackSize) {
        for (int i = 0; i < list.getConfigurableSlots(); i++) {
            var slot = list.getConfigurableSlot(i);
            GenericStack config = slot.getConfig();
            if (config != null) {
                long extracted = network.extract(config.what(), Long.MAX_VALUE, Actionable.SIMULATE, source);
                if (extracted >= minStackSize) {
                    slot.setStock(new GenericStack(config.what(), extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }

    /**
     * MEStockingBusPartMachine#refreshList on any list and keys: the biggest stacks in the network become the config,
     * biggest first, and the rest of the list is cleared.
     */
    static void autoPull(IConfigurableSlotList list, MEStorage network, IActionSource source, int minStackSize,
                         Predicate<AEKey> keys, @Nullable Predicate<GenericStack> pullTest) {
        int size = list.getConfigurableSlots();
        PriorityQueue<Object2LongMap.Entry<AEKey>> top = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry<AEKey>::getLongValue));
        for (Object2LongMap.Entry<AEKey> entry : network.getAvailableStacks()) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();
            if (amount <= 0 || amount < minStackSize || !keys.test(what)) continue;
            if (network.extract(what, amount, Actionable.SIMULATE, source) == 0) continue;
            if (pullTest != null && !pullTest.test(new GenericStack(what, amount))) continue;
            if (top.size() < size) {
                top.offer(entry);
            } else if (amount > top.peek().getLongValue()) {
                top.poll();
                top.offer(entry);
            }
        }
        int count = top.size();
        for (int index = 0; index < count; index++) {
            Object2LongMap.Entry<AEKey> entry = top.poll();
            AEKey what = entry.getKey();
            long request = network.extract(what, entry.getLongValue(), Actionable.SIMULATE, source);
            // poll() gives the smallest first, show the biggest first
            var slot = list.getConfigurableSlot(count - index - 1);
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
        }
        list.clearInventory(count);
    }

    /**
     * Whether a part of {@code self}'s multiblocks other than itself stocks the key: a non-distinct stocking bus
     * (and a dual input's fluids), a stocking hatch, or a non-distinct universal input. gtceu's bus and hatch only
     * check their own kind; the mixins on them ask this too.
     */
    public static boolean configuredElsewhere(MultiblockPartMachine self, GenericStack config) {
        if (!self.isFormed()) return false;
        for (MultiblockControllerMachine controller : self.getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part == self) continue;
                if (part instanceof MEUniversalInputPartMachine universal) {
                    if (!universal.isDistinct() && universal.getSlotList().hasStackInConfig(config, false)) {
                        return true;
                    }
                } else if (part instanceof MEStockingBusPartMachine bus) {
                    if (bus.isDistinct()) continue;
                    if (bus.getSlotList().hasStackInConfig(config, false)) return true;
                    if (bus instanceof MEDualInputPartMachine dual && dual.hasFluidInConfig(config)) return true;
                } else if (part instanceof MEStockingHatchPartMachine hatch) {
                    if (hatch.getSlotList().hasStackInConfig(config, false)) return true;
                }
            }
        }
        return false;
    }
}
