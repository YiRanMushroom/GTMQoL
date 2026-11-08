package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.batch.PartialPush;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.PatternBatch;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ICraftingInventory;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Batches on AE2's own CPU. The batches of a pattern pushed together cost one of the CPU's operations, not one each: the
 * pieces are one job cut up so that they can leave as soon as their inputs are there. And a batch that can't go whole,
 * for inputs still being made or too little power, pushes what {@link PartialPush} says can go, the rest becoming a
 * piece of its own. Only on AE2's CPU; others push the plan's pieces as they are and spend more operations.
 */
// Untested: a chain's next step starts with part of a batch as the step before delivers; a batch needing more power
// than the network stores still goes, in parts; the rest of a part shows in the CPU's list and survives a restart
@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCpuLogicBatchMixin {

    @WrapOperation(method = "executeCrafting",
                   at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private Set<?> gtmqol$seeTasks(Map<IPatternDetails, Object> tasks, Operation<Set<?>> entrySet,
                                   @Share("tasks") LocalRef<Map<IPatternDetails, Object>> seen) {
        seen.set(tasks);
        return entrySet.call(tasks);
    }

    @WrapOperation(method = "executeCrafting",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/crafting/execution/CraftingCpuHelper;extractPatternInputs(Lappeng/api/crafting/IPatternDetails;Lappeng/crafting/inv/ICraftingInventory;Lnet/minecraft/world/level/Level;Lappeng/api/stacks/KeyCounter;Lappeng/api/stacks/KeyCounter;)[Lappeng/api/stacks/KeyCounter;"))
    private KeyCounter @Nullable [] gtmqol$extractPart(IPatternDetails details, ICraftingInventory inventory,
                                                       Level level, KeyCounter outputs, KeyCounter containerItems,
                                                       Operation<KeyCounter[]> extract,
                                                       @Local(argsOnly = true) IEnergyService energy,
                                                       @Share("tasks") LocalRef<Map<IPatternDetails, Object>> tasks,
                                                       @Share("part") LocalRef<IPatternDetails> part,
                                                       @Share("partInputs") LocalRef<KeyCounter[]> partInputs) {
        KeyCounter[] inputs = extract.call(details, inventory, level, outputs, containerItems);
        if (!(details instanceof PatternBatch scaled) || !gtmqol$isLastOfItsSize(tasks.get(), scaled)) return inputs;
        long crafts;
        if (inputs != null) {
            double power = CraftingCpuHelper.calculatePatternPower(inputs);
            double available = energy.extractAEPower(power, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            crafts = available >= power - 0.01 ? 0 : PartialPush.byPower(scaled.multiplier(), power, available);
            // whole, or not even one: as AE2 would
            if (crafts == 0) return inputs;
            CraftingCpuHelper.reinjectPatternInputs(inventory, inputs);
        } else {
            crafts = Math.min(scaled.multiplier() - 1, gtmqol$craftsByInputs(scaled, inventory, level));
        }
        while (crafts > 0) {
            outputs.reset();
            containerItems.reset();
            IPatternDetails piece = PatternBatch.piece(scaled.original(), crafts);
            KeyCounter[] pieceInputs = extract.call(piece, inventory, level, outputs, containerItems);
            if (pieceInputs == null) {
                // the count copies AE2's extraction, so this shouldn't happen; smaller is still safe
                crafts /= 2;
                continue;
            }
            double power = CraftingCpuHelper.calculatePatternPower(pieceInputs);
            double available = energy.extractAEPower(power, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (available >= power - 0.01) {
                part.set(piece);
                partInputs.set(pieceInputs);
                return pieceInputs;
            }
            CraftingCpuHelper.reinjectPatternInputs(inventory, pieceInputs);
            crafts = PartialPush.byPower(crafts, power, available);
        }
        outputs.reset();
        containerItems.reset();
        return null;
    }

    /**
     * Only then can the rest get a counter: the finished piece's. AE2's counter can't be made here, its class is
     * package private.
     */
    @Unique
    private static boolean gtmqol$isLastOfItsSize(@Nullable Map<IPatternDetails, Object> tasks, PatternBatch scaled) {
        return tasks != null && tasks.get(scaled) instanceof CraftingTaskProgressAccessor progress &&
                progress.gtmqol$getValue() == 1;
    }

    @Unique
    private static long gtmqol$craftsByInputs(PatternBatch scaled, ICraftingInventory inventory, Level level) {
        List<PartialPush.Slot<AEKey>> slots = new ArrayList<>();
        Map<AEKey, Long> stock = new HashMap<>();
        for (IPatternDetails.IInput input : scaled.original().getInputs()) {
            List<PartialPush.Template<AEKey>> templates = new ArrayList<>();
            for (InputTemplate template : CraftingCpuHelper.getValidItemTemplates(inventory, input, level)) {
                templates.add(new PartialPush.Template<>(template.key(), template.amount()));
                stock.computeIfAbsent(template.key(),
                        key -> inventory.extract(key, Long.MAX_VALUE, Actionable.SIMULATE));
            }
            slots.add(new PartialPush.Slot<>(templates, input.getMultiplier()));
        }
        return PartialPush.byInputs(scaled.multiplier(), slots, stock);
    }

    @WrapOperation(method = "executeCrafting",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"))
    private boolean gtmqol$pushBatches(ICraftingProvider provider, IPatternDetails details, KeyCounter[] inputs,
                                       Operation<Boolean> push, @Share("free") LocalIntRef free,
                                       @Share("seen") LocalRef<Set<IPatternDetails>> seen,
                                       @Share("tasks") LocalRef<Map<IPatternDetails, Object>> tasks,
                                       @Share("part") LocalRef<IPatternDetails> part,
                                       @Share("partInputs") LocalRef<KeyCounter[]> partInputs,
                                       @Share("rests") LocalRef<List<Map.Entry<Object, IPatternDetails>>> rests) {
        boolean isPart = inputs == partInputs.get();
        IPatternDetails pushed = isPart ? part.get() : details;
        if (!push.call(provider, pushed, inputs)) return false;
        if (pushed instanceof PatternBatch scaled) {
            if (seen.get() == null) seen.set(new HashSet<>());
            if (!seen.get().add(scaled.original())) free.set(free.get() + 1);
        }
        if (isPart && details instanceof PatternBatch whole) {
            long rest = whole.multiplier() - (pushed instanceof PatternBatch scaled ? scaled.multiplier() : 1);
            if (rests.get() == null) rests.set(new ArrayList<>());
            // the finished piece's counter, reused for the rest (no mixin classes of our own in here)
            rests.get().add(Map.entry(tasks.get().get(details),
                    PatternBatch.piece(whole.original(), rest)));
        }
        return true;
    }

    @ModifyVariable(method = "executeCrafting", at = @At("LOAD"), argsOnly = true, ordinal = 0)
    private int gtmqol$budgetWithoutFreePushes(int maxPatterns, @Share("free") LocalIntRef free) {
        return maxPatterns + free.get();
    }

    /** The CPU counted the piece as pushed and dropped it, so the rest goes in now, after its loop over the tasks. */
    @ModifyReturnValue(method = "executeCrafting", at = @At("RETURN"))
    private int gtmqol$pushesThatCost(int pushed, @Share("free") LocalIntRef free,
                                      @Share("tasks") LocalRef<Map<IPatternDetails, Object>> tasks,
                                      @Share("rests") LocalRef<List<Map.Entry<Object, IPatternDetails>>> rests) {
        if (rests.get() != null) {
            for (var rest : rests.get()) {
                Object counter = tasks.get().get(rest.getValue());
                if (counter == null) {
                    counter = rest.getKey();
                    ((CraftingTaskProgressAccessor) counter).gtmqol$setValue(0);
                    tasks.get().put(rest.getValue(), counter);
                }
                var progress = (CraftingTaskProgressAccessor) counter;
                progress.gtmqol$setValue(progress.gtmqol$getValue() + 1);
            }
            rests.get().clear();
        }
        return pushed - free.get();
    }
}
