package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeNotifiableHandler;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridge;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/**
 * ME stocking input with 36 slots, each configured with any AE key the part can feed to recipes: items, fluids,
 * and the keys of every {@link AEStackLikeBridge} (e.g. chemicals). One list of slots is the source of truth (it is
 * also the item handler, like gtceu's stocking bus); the fluid tank and the per-bridge handlers are views of the
 * slots holding their keys. Auto pull, the minimum stack size and the cycle time apply to all of them.
 */
public class MEUniversalInputPartMachine extends MEInputBusPartMachine implements IMEStockingPart {

    public static final int SLOTS = 36;
    private static final int VISIBLE_ROWS = 2;

    private final UniversalList list;
    private final NotifiableFluidTank fluidView;
    private final List<BridgedView<?, ?>> bridgedViews = new ArrayList<>();

    @SyncToClient
    @SaveField
    private boolean autoPull;
    @SaveField
    private int minStackSize = 1;
    @SaveField
    private int ticksPerCycle = 40;
    private Predicate<GenericStack> autoPullTest = $ -> false;

    public MEUniversalInputPartMachine(BlockEntityCreationInfo info) {
        super(info, new UniversalList(SLOTS));
        this.list = (UniversalList) getInventory();
        this.aeItemHandler = list;
        list.bind(this);
        setOffsetBound(ticksPerCycle);
        this.fluidView = attachTrait(new FluidView(this));
        for (var bridge : AEStackLikeBridges.all()) {
            bridgedViews.add(attachTrait(new BridgedView<>(this, bridge)));
        }
    }

    /** Whether a slot can hold the key: the recipe handlers only see items, fluids and bridged keys. */
    public static boolean isSupported(AEKey key) {
        return key instanceof AEItemKey || key instanceof AEFluidKey || AEStackLikeBridges.forKey(key) != null;
    }

    /** The slots changed: the item handler notified itself, tell the other views. */
    private void onSlotsChanged() {
        // null while the superclass constructor is still running
        if (fluidView != null) fluidView.onContentsChanged();
        for (var view : bridgedViews) view.notifyListeners();
    }

    /**
     * Takes from the network for a recipe check (simulate) or run (modulate), and on modulate updates the slot's
     * stock like gtceu's stocking slots do.
     *
     * @return the amount taken
     */
    private long extractFromNetwork(ExportOnlyAESlot slot, long amount, boolean simulate) {
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        if (config == null || stock == null || amount <= 0 || !isOnline()) return 0;
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return 0;
        long extracted = grid.getStorageService().getInventory().extract(config.what(), amount,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE, actionSource);
        if (extracted > 0 && !simulate) {
            long left = stock.amount() - extracted;
            slot.setStock(left > 0 ? new GenericStack(stock.what(), left) : null);
        }
        return extracted;
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void addedToController(MultiblockControllerMachine controller, String name) {
        super.addedToController(controller, name);
        IMEStockingPart.super.addedToController(controller, name);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        IMEStockingPart.super.removedFromController(controller);
        super.removedFromController(controller);
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        if (!isWorkingEnabled()) return;
        if (ticksPerCycle <= 0) ticksPerCycle = 40;
        if (getOffsetTimer() % ticksPerCycle != 0) return;
        if (!updateMEStatus()) return;
        if (autoPull) refreshList();
        syncME();
        updateInventorySubscription();
    }

    @Override
    protected void syncME() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return;
        MEStorage networkInv = grid.getStorageService().getInventory();
        for (ExportOnlyAEItemSlot slot : list.getInventory()) {
            GenericStack config = slot.getConfig();
            if (config != null) {
                AEKey key = config.what();
                long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted >= minStackSize) {
                    slot.setStock(new GenericStack(key, extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }

    @Override
    protected void flushInventory() {
        // nothing to send back, the stock is only a view of the network
    }

    /** MEStockingBusPartMachine#refreshList over every supported key. */
    private void refreshList() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            list.clearInventory(0);
            return;
        }
        MEStorage networkStorage = grid.getStorageService().getInventory();
        PriorityQueue<Object2LongMap.Entry<AEKey>> top = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry<AEKey>::getLongValue));
        for (Object2LongMap.Entry<AEKey> entry : networkStorage.getAvailableStacks()) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();
            if (amount <= 0 || amount < minStackSize || !isSupported(what)) continue;
            if (networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource) == 0) continue;
            if (autoPullTest != null && !autoPullTest.test(new GenericStack(what, amount))) continue;
            if (top.size() < SLOTS) {
                top.offer(entry);
            } else if (amount > top.peek().getLongValue()) {
                top.poll();
                top.offer(entry);
            }
        }
        int index;
        int count = top.size();
        for (index = 0; index < SLOTS; index++) {
            if (top.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = top.poll();
            AEKey what = entry.getKey();
            long request = networkStorage.extract(what, entry.getLongValue(), Actionable.SIMULATE, actionSource);
            // poll() gives the smallest first, show the biggest first
            var slot = list.getInventory()[count - index - 1];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
        }
        list.clearInventory(index);
    }

    @Override
    public boolean isAutoPull() {
        return autoPull;
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("autoPull");
            if (!autoPull) {
                list.clearInventory(0);
            } else if (updateMEStatus()) {
                refreshList();
                updateInventorySubscription();
            }
        }
    }

    @Override
    public void setAutoPullTest(Predicate<GenericStack> autoPullTest) {
        this.autoPullTest = autoPullTest;
    }

    @Override
    public int getMinStackSize() {
        return minStackSize;
    }

    @Override
    public void setMinStackSize(int minStackSize) {
        this.minStackSize = minStackSize;
    }

    @Override
    public int getTicksPerCycle() {
        return ticksPerCycle;
    }

    @Override
    public void setTicksPerCycle(int ticksPerCycle) {
        this.ticksPerCycle = ticksPerCycle;
        setOffsetBound(ticksPerCycle);
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            setAutoPull(!autoPull);
            context.getPlayer().sendSystemMessage(Component.translatable(autoPull ?
                    "gtceu.machine.me.stocking_auto_pull_enabled" : "gtceu.machine.me.stocking_auto_pull_disabled"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    /////////////////////////////////
    // ****** Duplicate checks *****//
    /////////////////////////////////

    @Override
    public IConfigurableSlotList getSlotList() {
        return list;
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        super.setDistinct(isDistinct);
        // Same as the stocking bus: duplicates are allowed in distinct mode only.
        if (!isRemote() && !isDistinct) validateConfig();
    }

    // Like the stocking bus: a distinct part runs recipes on its own, so it may share keys with other parts.
    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null || !isFormed() || isDistinct()) return false;
        for (MultiblockControllerMachine controller : getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part == this) continue;
                if (part instanceof MEUniversalInputPartMachine other) {
                    if (!other.isDistinct() && other.list.hasStackInConfig(config, false)) return true;
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

    /**
     * For the other stocking parts' duplicate checks (see the gtceu stocking part mixins): whether a non-distinct
     * universal input of the controllers, other than {@code self}, has the key configured.
     */
    public static boolean configuredInAny(MultiblockPartMachine self, GenericStack config) {
        if (!self.isFormed()) return false;
        for (MultiblockControllerMachine controller : self.getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part != self && part instanceof MEUniversalInputPartMachine universal &&
                        !universal.isDistinct() && universal.list.hasStackInConfig(config, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Sets a slot's config from the UI: only supported keys, and none another slot or part already has. */
    private void setConfigChecked(int index, @Nullable GenericStack stack) {
        var slot = list.getInventory()[index];
        if (stack == null) {
            slot.setConfig(null);
            slot.setStock(null);
            return;
        }
        if (!isSupported(stack.what())) return;
        GenericStack config = new GenericStack(stack.what(), 1);
        if (list.hasStackInConfig(config, true)) return;
        slot.setConfig(config);
        slot.setStock(null);
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    // MEStockingBusPartMachine's format over 36 slots, so a stocking bus's copy pastes into the first 16.
    @Override
    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AutoPull", autoPull);
        tag.putByte("GhostCircuit", (byte) circuitSlot.getCurrentCircuit());
        if (!autoPull) {
            CompoundTag configStacks = new CompoundTag();
            for (int i = 0; i < SLOTS; i++) {
                GenericStack config = list.getInventory()[i].getConfig();
                if (config != null) configStacks.put(Integer.toString(i), GenericStack.writeTag(config));
            }
            tag.put("ConfigStacks", configStacks);
            tag.putBoolean("DistinctBuses", isDistinct());
        }
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        // auto pull first: turning it off clears the slots
        setAutoPull(tag.getBoolean("AutoPull"));
        if (tag.contains("GhostCircuit")) circuitSlot.setCurrentCircuit(tag.getByte("GhostCircuit"));
        if (autoPull) return;
        if (tag.contains("ConfigStacks")) {
            CompoundTag configStacks = tag.getCompound("ConfigStacks");
            for (int i = 0; i < SLOTS; i++) {
                String key = Integer.toString(i);
                GenericStack config = configStacks.contains(key) ?
                        GenericStack.readTag(configStacks.getCompound(key)) : null;
                list.getInventory()[i].setConfig(config != null && isSupported(config.what()) ? config : null);
            }
        }
        if (tag.contains("DistinctBuses")) setDistinct(tag.getBoolean("DistinctBuses"));
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    // The auto pull toggle and the settings popup (one min stack size and cycle time for every key).
    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMEStockingPart.super.getPanelBuilder(data, syncManager, settings);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);
        registerUniversalActions(syncManager);

        var flow = Flow.col().coverChildren();
        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));
        flow.child(new ListWidget<>()
                .size(UniversalConfigWidget.COLUMNS * 18 + 4, VISIBLE_ROWS * UniversalConfigWidget.PAIR_HEIGHT)
                .child(new UniversalConfigWidget(list, SLOTS, this::isAutoPull).syncManager(syncManager)));
        mainWidget.child(flow.center());
    }

    private void registerUniversalActions(PanelSyncManager sm) {
        sm.registerServerSyncedAction(UniversalConfigWidget.CLICK, packet -> {
            int index = packet.readVarInt();
            int button = packet.readVarInt();
            if (autoPull || index < 0 || index >= SLOTS) return;
            ItemStack held = sm.getPlayer().containerMenu.getCarried();
            if (held.isEmpty()) {
                setConfigChecked(index, null);
            } else if (button == 0) {
                setConfigChecked(index, GenericStack.fromItemStack(held));
            } else {
                // AE's config slots: right click takes what the container holds
                setConfigChecked(index, ContainerItemStrategies.getContainedStack(held));
            }
        });
        sm.registerServerSyncedAction(UniversalConfigWidget.SET_GHOST, packet -> {
            int index = packet.readVarInt();
            GenericStack stack = GenericStack.readBuffer(packet);
            if (autoPull || index < 0 || index >= SLOTS || stack == null) return;
            setConfigChecked(index, stack);
        });
    }

    ////////////////////////////////
    // ********** Slots **********//
    ////////////////////////////////

    // Static: the list is created before the machine exists (it goes to the superclass constructor), so the slots
    // get the machine afterwards through bind(), like gtceu's stocking list.
    private static class UniversalList extends ExportOnlyAEItemList {

        private @Nullable MEUniversalInputPartMachine machine;

        UniversalList(int slots) {
            super(slots, UniversalSlot::new);
        }

        void bind(MEUniversalInputPartMachine machine) {
            this.machine = machine;
            for (var slot : inventory) ((UniversalSlot) slot).machine = machine;
        }

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            if (machine != null) machine.onSlotsChanged();
        }

        @Override
        public boolean isAutoPull() {
            return machine != null && machine.isAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            if (super.hasStackInConfig(stack, false)) return true;
            return checkExternal && machine != null && machine.testConfiguredInOtherPart(stack);
        }
    }

    private static class UniversalSlot extends ExportOnlyAEItemSlot {

        private @Nullable MEUniversalInputPartMachine machine;

        UniversalSlot() {
            super();
        }

        UniversalSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || machine == null || stock == null || !(stock.what() instanceof AEItemKey key)) {
                return ItemStack.EMPTY;
            }
            long extracted = machine.extractFromNetwork(this, amount, simulate);
            return extracted > 0 ? key.toStack((int) extracted) : ItemStack.EMPTY;
        }

        @Override
        public UniversalSlot copy() {
            var copy = new UniversalSlot(config == null ? null : copy(config), stock == null ? null : copy(stock));
            copy.machine = machine;
            return copy;
        }
    }

    // Not persistent (attachTrait): the slots are saved with the item list.
    private static class FluidView extends NotifiableFluidTank {

        FluidView(MEUniversalInputPartMachine machine) {
            super(tanks(machine), IO.IN, IO.NONE);
        }

        private static List<CustomFluidTank> tanks(MEUniversalInputPartMachine machine) {
            List<CustomFluidTank> tanks = new ArrayList<>(SLOTS);
            for (var slot : machine.list.getInventory()) tanks.add(new FluidSlotView(machine, slot));
            return tanks;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }
    }

    private static class FluidSlotView extends CustomFluidTank {

        private final MEUniversalInputPartMachine machine;
        private final ExportOnlyAESlot slot;

        FluidSlotView(MEUniversalInputPartMachine machine, ExportOnlyAESlot slot) {
            super(0);
            this.machine = machine;
            this.slot = slot;
        }

        @Override
        public FluidStack getFluid() {
            GenericStack stock = slot.getStock();
            return stock != null && stock.what() instanceof AEFluidKey key ?
                    key.toStack((int) Math.min(stock.amount(), Integer.MAX_VALUE)) : FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount() {
            return getFluid().getAmount();
        }

        @Override
        public void setFluid(FluidStack stack) {}

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            GenericStack stock = slot.getStock();
            if (stock == null || !(stock.what() instanceof AEFluidKey key)) return FluidStack.EMPTY;
            long drained = machine.extractFromNetwork(slot, maxDrain, action.simulate());
            return drained > 0 ? key.toStack((int) drained) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!resource.isFluidEqual(getFluid())) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }
    }

    private static class BridgedView<S, I> extends GenericStackLikeNotifiableHandler<S, I> {

        private final MEUniversalInputPartMachine machine;
        private final AEStackLikeBridge<S, I> bridge;

        BridgedView(MEUniversalInputPartMachine machine, AEStackLikeBridge<S, I> bridge) {
            super(bridge.cap(), IO.IN);
            this.machine = machine;
            this.bridge = bridge;
        }

        @Override
        public List<S> getStacks() {
            List<S> stacks = new ArrayList<>();
            for (var slot : machine.list.getInventory()) {
                GenericStack stock = slot.getStock();
                if (stock != null && bridge.isKey(stock.what())) stacks.add(bridge.fromKey(stock.what(), stock.amount()));
            }
            return stacks;
        }

        // Keys are unique within the part (setConfigChecked), so the first slot with the key is the one.
        @Override
        protected long extract(S stack, long amount, boolean simulate) {
            AEKey key = bridge.toKey(stack);
            if (key == null) return 0;
            for (var slot : machine.list.getInventory()) {
                GenericStack stock = slot.getStock();
                if (stock != null && stock.what().equals(key)) return machine.extractFromNetwork(slot, amount, simulate);
            }
            return 0;
        }

        @Override
        protected long insert(S stack, long amount, boolean simulate) {
            return 0;
        }

        // The slot updates already notified every view (onSlotsChanged).
        @Override
        protected void onContentsChanged() {}
    }
}
