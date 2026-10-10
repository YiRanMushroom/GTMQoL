package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.utils.ISubscription;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeNotifiableHandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Bridged capabilities (e.g. chemicals) in gtceu's pattern buffer, through the {@code mixin.ae2.MEPatternBuffer*}
 * mixins. A slot keeps the bridged keys pushed into it next to its items and fluids
 * ({@link PatternBufferStackLikeSlot}) and has one handler per bridge, which the buffer's handler list uses for that
 * slot like gtceu's per slot item and fluid handlers. The buffer gets one aggregate handler per bridge for recipe
 * lookup, and the proxies one proxy handler of each aggregate. Refunds, saving and the pattern buffer's own outputs
 * ({@code PatternBufferReturn}) cover them too.
 */
public final class PatternBufferStackLike {

    private static final String NBT_KEY = "gtmqol:stack_like";

    private PatternBufferStackLike() {}

    /** gtceu's pushPattern check, also allowing bridged keys. */
    public static boolean allSupported(KeyCounter[] inputs) {
        for (KeyCounter input : inputs) {
            for (AEKey key : input.keySet()) {
                if (!(key instanceof AEItemKey) && !(key instanceof AEFluidKey) &&
                        AEStackLikeBridges.forKey(key) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /** {@code couldSlotMatchContents} for the bridged keys: whether any ingredient matches one of them. */
    public static boolean couldMatch(MEPatternBufferPartMachine.InternalSlot slot,
                                     Map<RecipeCapability<?>, List<Object>> contents) {
        var storage = storage(slot);
        if (storage.isEmpty()) return false;
        for (var bridge : AEStackLikeBridges.all()) {
            if (couldMatch(storage, bridge, contents.get(bridge.cap()))) return true;
        }
        return false;
    }

    private static <S, I> boolean couldMatch(Object2LongOpenHashMap<AEKey> storage, AEStackLikeBridge<S, I> bridge,
                                             @Nullable List<Object> ingredients) {
        if (ingredients == null) return false;
        var type = bridge.cap().type;
        for (Object o : ingredients) {
            if (!type.ingredientClass().isInstance(o)) continue;
            I ingredient = type.ingredientClass().cast(o);
            for (var entry : storage.object2LongEntrySet()) {
                if (bridge.isKey(entry.getKey()) &&
                        type.test(ingredient, bridge.fromKey(entry.getKey(), entry.getLongValue()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** InternalSlot.refund for the bridged keys: whatever the network takes leaves the slot. */
    public static void refund(Object2LongOpenHashMap<AEKey> storage, MEStorage network, IEnergySource energy,
                              IActionSource source) {
        for (var it = storage.object2LongEntrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            long amount = entry.getLongValue();
            long inserted = amount > 0 ?
                    StorageHelper.poweredInsert(energy, network, entry.getKey(), amount, source) : 0;
            if (inserted >= amount) it.remove();
            else if (inserted > 0) entry.setValue(amount - inserted);
        }
    }

    public static void save(Object2LongOpenHashMap<AEKey> storage, CompoundTag tag, HolderLookup.Provider provider) {
        if (storage.isEmpty()) return;
        ListTag list = new ListTag();
        for (var entry : storage.object2LongEntrySet()) {
            CompoundTag ct = entry.getKey().toTagGeneric(provider);
            ct.putLong("real", entry.getLongValue());
            list.add(ct);
        }
        tag.put(NBT_KEY, list);
    }

    /** Keys of capabilities that are no longer bridged (mod removed) are dropped. */
    public static void load(Object2LongOpenHashMap<AEKey> storage, CompoundTag tag, HolderLookup.Provider provider) {
        for (Tag t : tag.getList(NBT_KEY, Tag.TAG_COMPOUND)) {
            if (!(t instanceof CompoundTag ct)) continue;
            AEKey key = AEKey.fromTagGeneric(provider, ct);
            long amount = ct.getLong("real");
            if (key != null && amount > 0 && AEStackLikeBridges.forKey(key) != null) storage.addTo(key, amount);
        }
    }

    public static <S, I> SlotHandler<S, I> slotHandler(MEPatternBufferPartMachine buffer,
                                                       MEPatternBufferPartMachine.InternalSlot slot,
                                                       AEStackLikeBridge<S, I> bridge) {
        return new SlotHandler<>(buffer, slot, bridge);
    }

    public static <S, I> Aggregate<S, I> aggregate(AEStackLikeBridge<S, I> bridge,
                                                   Supplier<List<MEPatternBufferPartMachine.InternalSlot>> slots) {
        return new Aggregate<>(bridge, slots);
    }

    public static <I> Proxy<I> proxy(RecipeCapability<I> cap) {
        return new Proxy<>(cap);
    }

    private static Object2LongOpenHashMap<AEKey> storage(MEPatternBufferPartMachine.InternalSlot slot) {
        return ((PatternBufferStackLikeSlot) slot).gtmqol$getStackLike();
    }

    private static <S, I> void addStacks(List<S> stacks, MEPatternBufferPartMachine.InternalSlot slot,
                                         AEStackLikeBridge<S, I> bridge) {
        for (var entry : storage(slot).object2LongEntrySet()) {
            if (bridge.isKey(entry.getKey())) stacks.add(bridge.fromKey(entry.getKey(), entry.getLongValue()));
        }
    }

    /**
     * One slot's stacks of a bridge, like gtceu's {@code SlotItemHandler}. Not an attached trait (gtceu's aren't
     * either), so consumed inputs are recorded for the buffer.
     */
    public static final class SlotHandler<S, I> extends GenericStackLikeNotifiableHandler<S, I> {

        private final MEPatternBufferPartMachine buffer;
        private final MEPatternBufferPartMachine.InternalSlot slot;
        private final AEStackLikeBridge<S, I> bridge;

        private SlotHandler(MEPatternBufferPartMachine buffer, MEPatternBufferPartMachine.InternalSlot slot,
                            AEStackLikeBridge<S, I> bridge) {
            super(bridge.cap(), IO.IN);
            this.buffer = buffer;
            this.slot = slot;
            this.bridge = bridge;
        }

        @Override
        public List<S> getStacks() {
            List<S> stacks = new ArrayList<>();
            addStacks(stacks, slot, bridge);
            return stacks;
        }

        @Override
        protected long extract(S stack, long amount, boolean simulate) {
            AEKey key = bridge.toKey(stack);
            if (key == null) return 0;
            var storage = storage(slot);
            long stored = storage.getLong(key);
            long taken = Math.min(stored, amount);
            if (!simulate && taken > 0) {
                if (taken == stored) storage.removeLong(key);
                else storage.put(key, stored - taken);
            }
            return taken;
        }

        @Override
        protected long insert(S stack, long amount, boolean simulate) {
            return 0;
        }

        // The slot's callback notifies the buffer's aggregate handlers.
        @Override
        protected void onContentsChanged() {
            slot.onContentsChanged();
        }

        @Override
        protected @Nullable MetaMachine getRecipeMachine() {
            return buffer;
        }

        @Override
        public int getPriority() {
            return IFilteredHandler.HIGH;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    /**
     * Every slot's stacks of a bridge, for recipe lookup and parallel checks, like gtceu's
     * {@code AggregateItemHandler}. Recipes run through the slot handlers instead.
     */
    public static final class Aggregate<S, I> extends GenericStackLikeNotifiableHandler<S, I> {

        private final AEStackLikeBridge<S, I> bridge;
        private final Supplier<List<MEPatternBufferPartMachine.InternalSlot>> slots;

        private Aggregate(AEStackLikeBridge<S, I> bridge,
                          Supplier<List<MEPatternBufferPartMachine.InternalSlot>> slots) {
            super(bridge.cap(), IO.IN);
            this.bridge = bridge;
            this.slots = slots;
        }

        @Override
        public List<S> getStacks() {
            List<S> stacks = new ArrayList<>();
            for (var slot : slots.get()) addStacks(stacks, slot, bridge);
            return stacks;
        }

        @Override
        public @NotNull List<I> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<I> left, boolean simulate) {
            return left;
        }

        @Override
        protected long extract(S stack, long amount, boolean simulate) {
            return 0;
        }

        @Override
        protected long insert(S stack, long amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getPriority() {
            return IFilteredHandler.HIGH;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    /** A proxy's view of an {@link Aggregate}, like gtceu's {@code ProxyItemRecipeHandler}. */
    public static final class Proxy<I> extends NotifiableRecipeHandlerTrait<I> {

        private final RecipeCapability<I> cap;
        private @Nullable IRecipeHandlerTrait<I> proxy;
        private @Nullable ISubscription proxySub;

        private Proxy(RecipeCapability<I> cap) {
            this.cap = cap;
        }

        @SuppressWarnings("unchecked")
        public void setProxy(@Nullable IRecipeHandlerTrait<?> proxy) {
            this.proxy = (IRecipeHandlerTrait<I>) proxy;
            if (proxySub != null) {
                proxySub.unsubscribe();
                proxySub = null;
            }
            if (proxy != null) proxySub = proxy.addChangedListener(this::notifyListeners);
        }

        @Override
        public List<I> handleRecipeInner(IO io, GTRecipe recipe, List<I> left, boolean simulate) {
            return proxy == null ? left : proxy.handleRecipeInner(io, recipe, left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return proxy == null ? Collections.emptyList() : proxy.getContents();
        }

        @Override
        public double getTotalContentAmount() {
            return proxy == null ? 0 : proxy.getTotalContentAmount();
        }

        @Override
        public int getPriority() {
            return proxy == null ? IFilteredHandler.LOW : proxy.getPriority();
        }

        @Override
        public RecipeCapability<I> getCapability() {
            return cap;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }
}
