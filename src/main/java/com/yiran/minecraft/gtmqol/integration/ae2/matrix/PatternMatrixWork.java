package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a pattern matrix has taken from the network and owes it, kept over restarts and broken structures. Crafts of a
 * pattern queue up, and every tick up to the matrix's capacity of them get done. Patterns take turns: a big backlog of
 * one can't hold up the pattern that needs its output. Patterns that give something back are kept apart, because the
 * CPU waits for what comes back, which depends on what it pushed. Finished work leaves through {@link #outputs}, which
 * keeps what the network doesn't take for now. The inputs of crafts not done yet are kept too, so they can go back to the
 * network when the player {@linkplain #cancel cancels} the work.
 */
final class PatternMatrixWork implements INBTSerializable<CompoundTag> {

    private static final String QUEUE_KEY = "queue";
    private static final String GIVING_BACK_KEY = "givingBack";
    private static final String OUTPUTS_KEY = "outputs";
    private static final String STACKS_KEY = "stacks";
    private static final String CRAFTS_KEY = "crafts";
    private static final String PAID_KEY = "paid";
    private static final String PATTERN_KEY = "pattern";
    private static final String INPUTS_KEY = "inputs";
    private static final String WITH_INPUTS_KEY = "withInputs";
    private static final String SLOTS_KEY = "slots";
    private static final String USES_KEY = "uses";
    private static final String PARTS_KEY = "parts";
    private static final String PER_USE_KEY = "perUse";
    private static final String STACK_KEY = "stack";

    /** What one input of a pattern was pushed: each key, how much of it one use takes, and how much is left. */
    private static final class Part {

        final AEKey key;
        final long perUse;
        long amount;

        Part(AEKey key, long perUse, long amount) {
            this.key = key;
            this.perUse = perUse;
            this.amount = amount;
        }
    }

    /** One input of a pattern: how many uses a craft takes, and what was pushed for it. */
    private static final class Slot {

        final long usesPerCraft;
        final List<Part> parts = new ArrayList<>();

        Slot(long usesPerCraft) {
            this.usesPerCraft = usesPerCraft;
        }

        void add(IPatternDetails.IInput input, KeyCounter pushed) {
            for (var stack : pushed) {
                Part part = parts.stream().filter(p -> p.key.equals(stack.getKey())).findFirst().orElse(null);
                if (part != null) part.amount += stack.getLongValue();
                else parts.add(new Part(stack.getKey(), perUse(input, stack.getKey()), stack.getLongValue()));
            }
        }

        /** Takes what {@code crafts} crafts use, whole uses of one key after another, as the CPU took them out. */
        void use(long crafts) {
            long uses = crafts * usesPerCraft;
            for (var iterator = parts.iterator(); iterator.hasNext() && uses > 0;) {
                Part part = iterator.next();
                long taken = Math.min(uses, part.amount / part.perUse);
                part.amount -= taken * part.perUse;
                uses -= taken;
                if (part.amount == 0) iterator.remove();
            }
        }

        private static long perUse(IPatternDetails.IInput input, AEKey key) {
            for (GenericStack possible : input.getPossibleInputs()) {
                if (possible.what().equals(key)) return possible.amount();
            }
            return input.getPossibleInputs()[0].amount();
        }
    }

    /**
     * Crafts waiting for a batch, and what was pushed for the last {@code withInputs} of them, input by input. Each done
     * craft takes exactly what it uses, so a cancel gives back exactly what is left. Crafts saved before inputs were kept
     * have none and go first.
     */
    private static final class Queued {

        final List<GenericStack> outputs;
        long crafts;
        List<Slot> slots;
        long withInputs;

        Queued(List<GenericStack> outputs, long crafts, List<Slot> slots, long withInputs) {
            this.outputs = outputs;
            this.crafts = crafts;
            this.slots = slots;
            this.withInputs = slots.isEmpty() ? 0 : withInputs;
        }

        void add(IPatternDetails pattern, KeyCounter[] inputs, long crafts) {
            var patternInputs = pattern.getInputs();
            if (slots.size() != patternInputs.length) {
                slots = new ArrayList<>();
                for (var input : patternInputs) slots.add(new Slot(input.getMultiplier()));
            }
            for (int i = 0; i < patternInputs.length; i++) slots.get(i).add(patternInputs[i], inputs[i]);
            this.crafts += crafts;
            withInputs += crafts;
        }

        void finish(long done) {
            long fromInputs = Math.max(0, done - (crafts - withInputs));
            crafts -= done;
            if (fromInputs == 0) return;
            for (Slot slot : slots) slot.use(fromInputs);
            withInputs -= fromInputs;
        }
    }

    /** Crafts that give something back; finished as one, with all that comes out. */
    private static final class Run {

        final List<GenericStack> outputs;
        final long crafts;
        final KeyCounter inputs;
        long paid;

        Run(List<GenericStack> outputs, long crafts, KeyCounter inputs, long paid) {
            this.outputs = outputs;
            this.crafts = crafts;
            this.inputs = inputs;
            this.paid = paid;
        }
    }

    private final Map<AEItemKey, Queued> queue = new LinkedHashMap<>();
    private final List<Run> givingBack = new ArrayList<>();
    private final Object2LongLinkedOpenHashMap<AEKey> outputs = new Object2LongLinkedOpenHashMap<>();
    private boolean waitingForFE;

    /**
     * @param inputs what the CPU took out for all the crafts, for what comes back: each input's
     *               {@code getRemainingKey}, as the CPU counts it
     */
    void add(IPatternDetails pattern, KeyCounter[] inputs, long crafts) {
        KeyCounter remainders = new KeyCounter();
        var patternInputs = pattern.getInputs();
        for (int i = 0; i < patternInputs.length; i++) {
            long perUse = patternInputs[i].getPossibleInputs()[0].amount();
            for (var input : inputs[i]) {
                AEKey remainder = patternInputs[i].getRemainingKey(input.getKey());
                if (remainder != null) remainders.add(remainder, input.getLongValue() / perUse);
            }
        }
        if (remainders.isEmpty()) {
            queue.computeIfAbsent(pattern.getDefinition(),
                    key -> new Queued(List.of(pattern.getOutputs()), 0, new ArrayList<>(), 0)).add(pattern, inputs, crafts);
            return;
        }
        List<GenericStack> totals = new ArrayList<>(scaled(List.of(pattern.getOutputs()), crafts));
        for (var remainder : remainders) totals.add(new GenericStack(remainder.getKey(), remainder.getLongValue()));
        KeyCounter received = new KeyCounter();
        for (KeyCounter input : inputs) received.addAll(input);
        givingBack.add(new Run(totals, crafts, received, 0));
    }

    /**
     * Drops the crafts not done yet and sends what was pushed for them back to the network, with the finished items.
     * FE already paid is gone. Crafts saved before inputs were kept have nothing to give back, so they go on.
     */
    // Untested: Shift-click on the controller's cancel button after cancelling the job gives back the inputs of crafts
    // not done (substitutes as pushed), finished items still arrive, half-done batches give back the rest; a save from
    // before keeps crafting its queue
    void cancel() {
        for (var iterator = queue.values().iterator(); iterator.hasNext();) {
            Queued queued = iterator.next();
            for (Slot slot : queued.slots) {
                for (Part part : slot.parts) outputs.addTo(part.key, part.amount);
            }
            queued.crafts -= queued.withInputs;
            queued.slots = new ArrayList<>();
            queued.withInputs = 0;
            if (queued.crafts <= 0) iterator.remove();
        }
        for (var iterator = givingBack.iterator(); iterator.hasNext();) {
            Run run = iterator.next();
            if (run.inputs.isEmpty()) continue;
            addOutputs(run.inputs);
            iterator.remove();
        }
    }

    /**
     * For a controller that is going away: what was pushed for crafts not done and every finished item go to the
     * network, as far as it takes them; the rest is gone, as is all the work.
     */
    // Untested: breaking the controller mid-job sends undone crafts' inputs and finished items to the network
    void giveBackAll(PatternMatrixMachine matrix) {
        cancel();
        flushOutputs(matrix);
        queue.clear();
        givingBack.clear();
        outputs.clear();
    }

    /** Whether {@link #cancel} would give anything back. */
    boolean canCancel() {
        for (Queued queued : queue.values()) if (queued.withInputs > 0) return true;
        for (Run run : givingBack) if (!run.inputs.isEmpty()) return true;
        return false;
    }

    private static List<GenericStack> scaled(List<GenericStack> stacks, long multiplier) {
        return stacks.stream().map(stack -> new GenericStack(stack.what(), stack.amount() * multiplier)).toList();
    }

    void tick(PatternMatrixMachine matrix) {
        flushOutputs(matrix);
        waitingForFE = false;
        long budget = matrix.capacity();
        for (var iterator = givingBack.iterator(); iterator.hasNext() && budget > 0;) {
            Run run = iterator.next();
            long wanted = Math.min(run.crafts - run.paid, budget);
            long paid = matrix.pay(wanted);
            run.paid += paid;
            budget -= paid;
            if (paid < wanted) waitingForFE = true;
            if (run.paid == run.crafts) {
                addOutputs(run.outputs, 1);
                iterator.remove();
            }
        }
        // each pattern once at most: what it couldn't finish goes to the end of the queue
        for (int turns = queue.size(); turns > 0 && budget > 0; turns--) {
            var first = queue.entrySet().iterator().next();
            Queued next = first.getValue();
            long wanted = Math.min(next.crafts, budget);
            long crafts = matrix.pay(wanted);
            if (crafts < wanted) waitingForFE = true;
            if (crafts == 0) break;
            budget -= crafts;
            next.finish(crafts);
            addOutputs(next.outputs, crafts);
            AEItemKey pattern = first.getKey();
            queue.remove(pattern);
            if (next.crafts > 0) queue.put(pattern, next);
        }
    }

    private void addOutputs(List<GenericStack> stacks, long crafts) {
        for (GenericStack stack : stacks) outputs.addTo(stack.what(), stack.amount() * crafts);
    }

    private void addOutputs(KeyCounter stacks) {
        for (var stack : stacks) outputs.addTo(stack.getKey(), stack.getLongValue());
    }

    private void flushOutputs(PatternMatrixMachine matrix) {
        for (var iterator = outputs.object2LongEntrySet().fastIterator(); iterator.hasNext();) {
            var entry = iterator.next();
            long left = entry.getLongValue() - matrix.insert(entry.getKey(), entry.getLongValue());
            if (left > 0) entry.setValue(left);
            else iterator.remove();
        }
    }

    boolean isWorking() {
        return !queue.isEmpty() || !givingBack.isEmpty() || !outputs.isEmpty();
    }

    boolean isWaitingForFE() {
        return waitingForFE;
    }

    /** Crafts not finished yet. */
    long crafts() {
        long crafts = 0;
        for (Queued queued : queue.values()) crafts += queued.crafts;
        for (Run run : givingBack) crafts += run.crafts;
        return crafts;
    }

    /** Finished items the network hasn't taken yet. */
    long stored() {
        long stored = 0;
        for (long amount : outputs.values()) stored += amount;
        return stored;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag queueTag = new ListTag();
        queue.forEach((pattern, queued) -> {
            CompoundTag entry = new CompoundTag();
            entry.put(PATTERN_KEY, pattern.toTag());
            entry.put(STACKS_KEY, write(queued.outputs));
            entry.putLong(CRAFTS_KEY, queued.crafts);
            entry.put(SLOTS_KEY, writeSlots(queued.slots));
            entry.putLong(WITH_INPUTS_KEY, queued.withInputs);
            queueTag.add(entry);
        });
        tag.put(QUEUE_KEY, queueTag);
        ListTag givingBackTag = new ListTag();
        for (Run run : givingBack) givingBackTag.add(write(run));
        tag.put(GIVING_BACK_KEY, givingBackTag);
        List<GenericStack> stored = new ArrayList<>();
        for (var entry : outputs.object2LongEntrySet()) {
            stored.add(new GenericStack(entry.getKey(), entry.getLongValue()));
        }
        tag.put(OUTPUTS_KEY, write(stored));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        queue.clear();
        givingBack.clear();
        outputs.clear();
        for (Tag entry : tag.getList(QUEUE_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag queued = (CompoundTag) entry;
            AEItemKey pattern = AEItemKey.fromTag(queued.getCompound(PATTERN_KEY));
            if (pattern == null) continue;
            queue.put(pattern, new Queued(readStacks(queued.getList(STACKS_KEY, Tag.TAG_COMPOUND)),
                    queued.getLong(CRAFTS_KEY), readSlots(queued.getList(SLOTS_KEY, Tag.TAG_COMPOUND)),
                    queued.getLong(WITH_INPUTS_KEY)));
        }
        for (Tag run : tag.getList(GIVING_BACK_KEY, Tag.TAG_COMPOUND)) {
            givingBack.add(readRun((CompoundTag) run));
        }
        for (GenericStack stack : readStacks(tag.getList(OUTPUTS_KEY, Tag.TAG_COMPOUND))) {
            outputs.addTo(stack.what(), stack.amount());
        }
    }

    private static ListTag write(List<GenericStack> stacks) {
        ListTag list = new ListTag();
        for (GenericStack stack : stacks) list.add(GenericStack.writeTag(stack));
        return list;
    }

    private static ListTag write(KeyCounter stacks) {
        ListTag list = new ListTag();
        for (var stack : stacks) {
            list.add(GenericStack.writeTag(new GenericStack(stack.getKey(), stack.getLongValue())));
        }
        return list;
    }

    private static ListTag writeSlots(List<Slot> slots) {
        ListTag list = new ListTag();
        for (Slot slot : slots) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putLong(USES_KEY, slot.usesPerCraft);
            ListTag parts = new ListTag();
            for (Part part : slot.parts) {
                CompoundTag partTag = new CompoundTag();
                partTag.put(STACK_KEY, GenericStack.writeTag(new GenericStack(part.key, part.amount)));
                partTag.putLong(PER_USE_KEY, part.perUse);
                parts.add(partTag);
            }
            slotTag.put(PARTS_KEY, parts);
            list.add(slotTag);
        }
        return list;
    }

    private static CompoundTag write(Run run) {
        CompoundTag tag = new CompoundTag();
        tag.put(STACKS_KEY, write(run.outputs));
        tag.put(INPUTS_KEY, write(run.inputs));
        tag.putLong(CRAFTS_KEY, run.crafts);
        tag.putLong(PAID_KEY, run.paid);
        return tag;
    }

    private static List<GenericStack> readStacks(ListTag list) {
        List<GenericStack> stacks = new ArrayList<>();
        for (Tag stack : list) {
            GenericStack read = GenericStack.readTag((CompoundTag) stack);
            if (read != null) stacks.add(read);
        }
        return stacks;
    }

    private static KeyCounter readCounter(ListTag list) {
        KeyCounter counter = new KeyCounter();
        for (GenericStack stack : readStacks(list)) counter.add(stack.what(), stack.amount());
        return counter;
    }

    private static List<Slot> readSlots(ListTag list) {
        List<Slot> slots = new ArrayList<>();
        for (Tag entry : list) {
            CompoundTag slotTag = (CompoundTag) entry;
            Slot slot = new Slot(slotTag.getLong(USES_KEY));
            for (Tag part : slotTag.getList(PARTS_KEY, Tag.TAG_COMPOUND)) {
                CompoundTag partTag = (CompoundTag) part;
                GenericStack stack = GenericStack.readTag(partTag.getCompound(STACK_KEY));
                long perUse = partTag.getLong(PER_USE_KEY);
                if (stack != null && perUse > 0) slot.parts.add(new Part(stack.what(), perUse, stack.amount()));
            }
            slots.add(slot);
        }
        return slots;
    }

    private static Run readRun(CompoundTag tag) {
        return new Run(readStacks(tag.getList(STACKS_KEY, Tag.TAG_COMPOUND)), tag.getLong(CRAFTS_KEY),
                readCounter(tag.getList(INPUTS_KEY, Tag.TAG_COMPOUND)), tag.getLong(PAID_KEY));
    }
}
