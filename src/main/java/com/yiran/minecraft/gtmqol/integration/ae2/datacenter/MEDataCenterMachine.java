package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.ui.DepositHandler;
import com.yiran.minecraft.gtmqol.common.ui.ItemGridEntry;
import com.yiran.minecraft.gtmqol.common.ui.ItemGridWidget;
import com.yiran.minecraft.gtmqol.common.ui.NumberText;
import com.yiran.minecraft.gtmqol.integration.ae2.CasingFrameBox;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.block.crafting.ICraftingUnitType;
import appeng.core.definitions.AEBlocks;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Controller of the {@link MEDataCenter}: crafting CPUs for an ME network without building one of blocks for every
 * job. The crafting units in its CPU frames give the storage and co-processors, the casing tier gives credits, each of
 * which quadruples one or the other. Each job takes the storage it needs and every job has all the co-processors
 * ({@link MEDataCenterCpus}).
 */
public class MEDataCenterMachine extends MultiblockControllerMachine implements IMuiMachine, CasingFrameBox.Controller {

    public static final String KEY = "gtmqol.me_data_center.";

    /** AE2 counts what a CPU may still do this tick as co-processors + 1, in an int. */
    private static final int MAX_CO_PROCESSORS = 1 << 30;
    private static final String UNITS_PANEL = "me_data_center_units";
    private static final String INPUT_SLOT_GROUP = "me_data_center_input";
    /** Half a machine panel and a gap: the units window stays clear of the player's inventory. */
    private static final int UNITS_PANEL_OFFSET = 92;
    private static final int VISIBLE_ROWS = 8;

    // Untested: in game, a formed ME Data Center with crafting units in its CPU Frames shows in a Crafting Status as one
    // CPU with the free storage; each job started gets its own CPU and the free one shrinks by the job's size; jobs
    // finish and their storage comes back; credits double storage or co-processors; units and credits are locked while
    // a job runs; breaking a casing pauses the jobs and reforming resumes them; a job survives leaving and rejoining
    // the world; breaking the controller cancels the jobs and returns their items to the ME network
    @SaveField
    private final MEDataCenterCpus cpus = new MEDataCenterCpus();
    /** Credits spent on the storage and on the co-processors, each ×4; the rest of the tier's credits is unspent. */
    @SaveField
    private int storageCredits;
    @SaveField
    private int coProcessorCredits;
    /** The inside of the formed box for {@link MEDataCenterStarsRender}: up, left, right, back in 4 bits each. */
    @SyncToClient
    private int box;

    /** Kept when the structure breaks: paused jobs still belong to its network. */
    private @Nullable MEDataCenterInterfaceMachine meInterface;
    private final List<MEDataCenterFrameMachine> frames = new ArrayList<>();
    private int credits;
    private long storage;
    private int coProcessors;
    private int slots;
    private String size = "";
    private boolean unitsChanged = true;
    private boolean destroyed;
    private @Nullable List<ItemGridEntry> entries;

    public MEDataCenterMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        if (MEDataCenterCompat.isCompatible()) cpus.start(this);
        subscribeServerTick(this::tick);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (isRemote()) return;
        cpus.stop();
        if (meInterface != null) meInterface.setDataCenter(null);
        meInterface = null;
    }

    // ---------- structure ----------

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!isFormed()) return;
        PatternState state = patternStates.get(substructureName);
        var bounds = CasingFrameBox.bounds().apply(getLevel(), getBlockPos().mutable(), getFrontFacing(),
                getUpwardsFacing());
        if (!CasingFrameBox.isCentered(bounds)) {
            state.setError(new PatternStringError(
                    Component.translatable("gtceu.predicate_error.cleanroom.not_centered")));
            invalidateStructure();
            return;
        }
        int tier = CasingFrameBox.tier(state);
        if (tier == CasingFrameBox.MIXED) {
            state.setError(new PatternStringError(Component.translatable(KEY + "error.mixed_casings")));
            invalidateStructure();
            return;
        }
        credits = tier;
        // a lower tier than the credits were spent at
        coProcessorCredits = Math.min(coProcessorCredits, credits);
        storageCredits = Math.min(storageCredits, credits - coProcessorCredits);
        size = String.format(Locale.ROOT, "%d x %d x %d", bounds.getInt(2) + bounds.getInt(3) + 1,
                bounds.getInt(0) + 1, bounds.getInt(5) + 1);
        setBox(bounds.getInt(0) | bounds.getInt(2) << 4 | bounds.getInt(3) << 8 | bounds.getInt(5) << 12);
        setRenderState(getRenderState().setValue(CasingFrameBox.CASING_TIER, tier));
        frames.clear();
        slots = 0;
        MEDataCenterInterfaceMachine found = null;
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MEDataCenterFrameMachine frame) {
                frames.add(frame);
                slots += frame.units().getSlots();
            } else if (part instanceof MEDataCenterInterfaceMachine machine) {
                found = machine;
            }
        }
        if (found != meInterface) {
            if (meInterface != null) meInterface.setDataCenter(null);
            meInterface = found;
        }
        if (meInterface != null) meInterface.setDataCenter(this);
        onUnitsChanged();
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        frames.clear();
        slots = 0;
        setBox(0);
        onUnitsChanged();
    }

    private void setBox(int box) {
        if (this.box == box) return;
        this.box = box;
        getSyncDataHolder().markClientSyncFieldDirty("box");
    }

    int box() {
        return box;
    }

    @Override
    public BlockState casingState() {
        return CasingFrameBox.casings().get(getRenderState().getValue(CasingFrameBox.CASING_TIER)).getDefaultState();
    }

    void interfaceGone(MEDataCenterInterfaceMachine machine) {
        if (meInterface == machine) meInterface = null;
    }

    // ---------- crafting CPUs (server) ----------

    MEDataCenterCpus cpus() {
        return cpus;
    }

    /**
     * The grid node its CPUs act through; without one AE2 takes them as offline and their jobs pause. So they do while
     * the structure isn't formed.
     */
    @Nullable
    IGridNode cpuNode() {
        if (meInterface == null || !isFormed() && !destroyed) return null;
        return meInterface.getMainNode().getNode();
    }

    /** Nothing about the crafting units or credits changes while a job runs. */
    boolean isLocked() {
        return cpus.jobCount() > 0;
    }

    void onUnitsChanged() {
        unitsChanged = true;
        entries = null;
    }

    private void tick() {
        if (unitsChanged) {
            unitsChanged = false;
            updateCapacity();
        }
        IGrid grid = meInterface == null ? null : meInterface.getMainNode().getGrid();
        cpus.tick(grid);
        RecipeLogic.Status status = isFormed() && cpus.jobCount() > 0 ? RecipeLogic.Status.WORKING :
                RecipeLogic.Status.IDLE;
        var renderState = getRenderState();
        if (renderState.getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS) != status) {
            setRenderState(renderState.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
        }
    }

    private void updateCapacity() {
        long bytes = 0;
        long threads = 0;
        for (MEDataCenterFrameMachine frame : frames) {
            for (int slot = 0; slot < frame.units().getSlots(); slot++) {
                ICraftingUnitType unit = MEDataCenter.unit(frame.units().getStackInSlot(slot));
                if (unit == null) continue;
                bytes = MEDataCenterCpus.saturatedAdd(bytes, Math.max(0, unit.getStorageBytes()));
                threads = MEDataCenterCpus.saturatedAdd(threads, Math.max(0, unit.getAcceleratorThreads()));
            }
        }
        storage = doubled(bytes, 2 * storageCredits, Long.MAX_VALUE);
        coProcessors = (int) doubled(threads, 2 * coProcessorCredits, MAX_CO_PROCESSORS);
        cpus.setCapacity(isFormed() ? storage : 0, coProcessors);
    }

    /** {@code value} doubled {@code times} times, at most {@code max}. */
    private static long doubled(long value, int times, long max) {
        if (value <= 0) return 0;
        return times >= 63 || value > max >> times ? max : value << times;
    }

    /** Spends {@code delta} more credits on the storage or the co-processors, or takes them back when negative. */
    private void spendCredits(boolean onCoProcessors, int delta) {
        if (isLocked() || !isFormed()) return;
        int spent = onCoProcessors ? coProcessorCredits : storageCredits;
        int unspent = credits - storageCredits - coProcessorCredits;
        int now = Mth.clamp(spent + delta, 0, spent + unspent);
        if (now == spent) return;
        if (onCoProcessors) coProcessorCredits = now;
        else storageCredits = now;
        onUnitsChanged();
        markAsChanged();
    }

    @Override
    public void onMachineDestroyed() {
        if (!isRemote()) {
            // the CPUs' last act goes through the interface although the structure is breaking
            destroyed = true;
            cpus.cancelAll();
        }
        super.onMachineDestroyed();
    }

    // ---------- UI ----------

    /** The crafting units of all frames, one entry per kind with how many there are; rebuilt after a change. */
    private List<ItemGridEntry> unitEntries() {
        if (entries == null) {
            List<ItemStack> kinds = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            for (MEDataCenterFrameMachine frame : frames) {
                var units = frame.units();
                slots:
                for (int slot = 0; slot < units.getSlots(); slot++) {
                    ItemStack stack = units.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    for (int kind = 0; kind < kinds.size(); kind++) {
                        if (!ItemStack.isSameItemSameTags(kinds.get(kind), stack)) continue;
                        counts.set(kind, counts.get(kind) + 1);
                        continue slots;
                    }
                    kinds.add(stack.copy());
                    counts.add(1);
                }
            }
            List<ItemGridEntry> list = new ArrayList<>(kinds.size());
            for (int kind = 0; kind < kinds.size(); kind++) {
                list.add(new ItemGridEntry(kind, 0, kinds.get(kind), kinds.get(kind), counts.get(kind)));
            }
            entries = list;
        }
        return entries;
    }

    /** Takes up to a stack of one kind of unit out of the frames, to the hand or the inventory. */
    private void take(int kind, boolean toInventory, Player player) {
        List<ItemGridEntry> kinds = unitEntries();
        if (isLocked() || kind < 0 || kind >= kinds.size()) return;
        ItemStack wanted = kinds.get(kind).stack();
        ItemStack taken = ItemStack.EMPTY;
        for (MEDataCenterFrameMachine frame : frames) {
            var units = frame.units();
            for (int slot = 0; slot < units.getSlots(); slot++) {
                if (taken.getCount() >= wanted.getMaxStackSize()) break;
                if (!ItemStack.isSameItemSameTags(units.getStackInSlot(slot), wanted)) continue;
                ItemStack unit = units.extractItem(slot, 1, false);
                if (unit.isEmpty()) continue;
                if (taken.isEmpty()) taken = unit;
                else taken.grow(1);
            }
        }
        if (taken.isEmpty()) return;
        if (toInventory || !player.containerMenu.getCarried().isEmpty()) {
            player.getInventory().placeItemBackInInventory(taken);
        } else {
            player.containerMenu.setCarried(taken);
        }
    }

    private void putCarried(Player player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (MEDataCenter.isUnit(carried)) player.containerMenu.setCarried(addUnits(carried));
    }

    /** Puts them in the first free slots of the frames, one each; what didn't fit. */
    private ItemStack addUnits(ItemStack stack) {
        if (isLocked()) return stack;
        for (MEDataCenterFrameMachine frame : frames) {
            for (int slot = 0; slot < frame.units().getSlots() && !stack.isEmpty(); slot++) {
                stack = frame.units().insertItem(slot, stack, false);
            }
        }
        return stack;
    }

    private boolean hasRoom() {
        if (isLocked()) return false;
        for (MEDataCenterFrameMachine frame : frames) {
            for (int slot = 0; slot < frame.units().getSlots(); slot++) {
                if (frame.units().getStackInSlot(slot).isEmpty()) return true;
            }
        }
        return false;
    }

    private static TextWidget<?> line(Supplier<Component> text) {
        return new TextWidget<>(Text.dynamic(text::get));
    }

    private static String number(long value) {
        return FormattingUtil.formatNumbers(value);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var formed = new BooleanSyncValue(this::isFormed);
        var compatible = new BooleanSyncValue(MEDataCenterCompat::isCompatible);
        var tier = new IntSyncValue(() -> getRenderState().getValue(CasingFrameBox.CASING_TIER));
        var sizeValue = new StringSyncValue(() -> size);
        var storageValue = new LongSyncValue(() -> storage);
        var freeValue = new LongSyncValue(() -> isFormed() ? cpus.free() : 0);
        var coProcessorsValue = new IntSyncValue(() -> coProcessors);
        var jobs = new IntSyncValue(cpus::jobCount);
        var creditsValue = new IntSyncValue(() -> credits);
        var storageCreditsValue = new IntSyncValue(() -> storageCredits);
        var coProcessorCreditsValue = new IntSyncValue(() -> coProcessorCredits);
        var unitCount = new IntSyncValue(() -> {
            int count = 0;
            if (!isRemote()) for (ItemGridEntry entry : unitEntries()) count += entry.count();
            return count;
        });
        var slotCount = new IntSyncValue(() -> slots);
        syncManager.syncValue("formed", formed);
        syncManager.syncValue("compatible", compatible);
        syncManager.syncValue("tier", tier);
        syncManager.syncValue("size", sizeValue);
        syncManager.syncValue("storage", storageValue);
        syncManager.syncValue("free", freeValue);
        syncManager.syncValue("coProcessors", coProcessorsValue);
        syncManager.syncValue("jobs", jobs);
        syncManager.syncValue("credits", creditsValue);
        syncManager.syncValue("storageCredits", storageCreditsValue);
        syncManager.syncValue("coProcessorCredits", coProcessorCreditsValue);
        syncManager.syncValue("unitCount", unitCount);
        syncManager.syncValue("slotCount", slotCount);
        Player player = guiData.getPlayer();
        syncManager.registerSyncedAction("credits", false, true,
                buf -> spendCredits(buf.readBoolean(), buf.readVarInt()));
        syncManager.registerSyncedAction("take", false, true,
                buf -> take(buf.readVarInt(), buf.readBoolean(), player));
        syncManager.registerSyncedAction("put", false, true, buf -> putCarried(player));
        var units = syncManager.syncedPanel(UNITS_PANEL, false,
                (manager, handler) -> unitsPanel(syncManager, manager, unitCount, slotCount, jobs));

        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(2)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(line(() -> !compatible.getBoolValue() ?
                        Component.translatable(KEY + "incompatible").withStyle(ChatFormatting.RED) :
                        !formed.getBoolValue() ?
                                Component.translatable(KEY + "not_formed").withStyle(ChatFormatting.RED) :
                                Component.translatable(KEY + "frame", GTValues.VNF[tier.getIntValue()],
                                        sizeValue.getStringValue())))
                .child(NumberText.line(format -> Component.translatable(KEY + "storage",
                        format.apply(freeValue.getLongValue()), format.apply(storageValue.getLongValue()))))
                .child(NumberText.line(format -> Component.translatable(KEY + "co_processors",
                        format.apply(coProcessorsValue.getIntValue()))))
                .child(line(() -> Component.translatable(KEY + "jobs", number(jobs.getIntValue()))))
                .child(line(() -> Component.translatable(KEY + "credits", creditsValue.getIntValue() -
                        storageCreditsValue.getIntValue() - coProcessorCreditsValue.getIntValue(),
                        creditsValue.getIntValue())).tooltipBuilder(tooltip -> tooltip
                                .addLine(Text.lang(KEY + "credits.tooltip.0"))
                                .addLine(Text.lang(KEY + "credits.tooltip.1"))))
                .child(creditRow(syncManager, false, "credits.storage", storageCreditsValue, jobs))
                .child(creditRow(syncManager, true, "credits.co_processors", coProcessorCreditsValue, jobs))
                .child(new ButtonWidget<>()
                        .size(18)
                        .overlay(new ItemDrawable(AEBlocks.CRAFTING_STORAGE_1K.asItem()).asIcon().size(16))
                        .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(KEY + "units")))
                        .onMousePressed((context, button) -> {
                            units.togglePanel();
                            return true;
                        })));
    }

    /** {@code [-] Storage: n credits [+]}; Shift spends or takes back all. */
    private Flow creditRow(PanelSyncManager syncManager, boolean onCoProcessors, String langKey, IntSyncValue spent,
                           IntSyncValue jobs) {
        return Flow.row()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .child(creditButton(syncManager, onCoProcessors, false, jobs))
                .child(NumberText.line(format -> Component.translatable(KEY + langKey, spent.getIntValue(),
                        // ×4 per credit; 4^14 still fits
                        format.apply(1L << 2 * spent.getIntValue()))).width(150).textAlign(Alignment.Center))
                .child(creditButton(syncManager, onCoProcessors, true, jobs));
    }

    private ButtonWidget<?> creditButton(PanelSyncManager syncManager, boolean onCoProcessors, boolean more,
                                         IntSyncValue jobs) {
        return new ButtonWidget<>()
                .size(14)
                .overlay(Text.str(more ? "+" : "-"))
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> tooltip.addLine(jobs.getIntValue() > 0 ?
                        Component.translatable(KEY + "locked").withStyle(ChatFormatting.RED) :
                        Component.translatable(KEY + "credits.shift")))
                .onMousePressed((context, button) -> {
                    int step = MouseData.create(button).shift() ? GTValues.MAX : 1;
                    syncManager.callSyncedAction("credits", buf -> {
                        buf.writeBoolean(onCoProcessors);
                        buf.writeVarInt(more ? step : -step);
                    });
                    return true;
                });
    }

    private ModularPanel<?> unitsPanel(PanelSyncManager main, PanelSyncManager manager, IntSyncValue unitCount,
                                       IntSyncValue slotCount, IntSyncValue jobs) {
        var list = new GenericListSyncHandler.Builder<ItemGridEntry>()
                .getter(() -> isRemote() ? List.of() : unitEntries())
                .serializer(ItemGridEntry::write)
                .deserializer(ItemGridEntry::read)
                // entries are never changed, so the cache can hold them as they are
                .immutableCopy()
                .build();
        manager.syncValue("entries", list);
        manager.registerSlotGroup(new SlotGroup(INPUT_SLOT_GROUP, 1).setAllowSorting(false));
        var search = new StringValue("");
        int gridWidth = ItemGridWidget.COLUMNS * ItemGridWidget.CELL;
        var grid = new ItemGridWidget(list::getValue, search::getStringValue,
                (entry, toInventory) -> main.callSyncedAction("take", buf -> {
                    buf.writeVarInt(entry.inventory());
                    buf.writeBoolean(toInventory);
                }),
                () -> main.callSyncedAction("put", buf -> {}), KEY + "units.empty", KEY + "units.take");
        // not deleteCachedPanel: MUI refuses that with an item slot in the panel
        return popup(UNITS_PANEL, gridWidth + 24, VISIBLE_ROWS * ItemGridWidget.CELL + 52)
                // beside the machine panel and open while the inventory is in use, units come from there
                .closeOnOutOfBoundsClick(false)
                .leftRel(0.5f, UNITS_PANEL_OFFSET, 0f)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(line(() -> jobs.getIntValue() > 0 ?
                                Component.translatable(KEY + "locked").withStyle(ChatFormatting.RED) :
                                Component.translatable(KEY + "units.title", number(unitCount.getIntValue()),
                                        number(slotCount.getIntValue()))))
                        .child(Flow.row()
                                .size(gridWidth, 18)
                                .childPadding(4)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .child(new TextFieldWidget()
                                        .size(gridWidth - 22, 14)
                                        .autoUpdateOnChange(true)
                                        .hintText(Component.translatable(KEY + "units.search"))
                                        .value(search))
                                .child(new ItemSlot()
                                        .slot(new ModularSlot(new DepositHandler(this::isRemote,
                                                // the client doesn't know the frames: the server has the last word
                                                stack -> MEDataCenter.isUnit(stack) && (isRemote() || hasRoom()),
                                                this::addUnits), 0).slotGroup(INPUT_SLOT_GROUP))
                                        .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(KEY + "units.put")))))
                        .child(new ListWidget<>()
                                .size(gridWidth + 8, VISIBLE_ROWS * ItemGridWidget.CELL)
                                .child(grid)));
    }

    static void addLang() {
        var lang = GTMQoLAddon.registrate();
        lang.addRawLang(KEY + "tooltip.0", "Crafting CPUs for an ME network: every crafting job gets a CPU of its own.");
        lang.addRawLang(KEY + "tooltip.1", "3 to 15 blocks each way. Inside: ME Data Center CPU Frames or air.");
        lang.addRawLang(KEY + "tooltip.2", "Machine Casings of any one tier. Needs one ME Data Center Interface.");
        lang.addRawLang(KEY + "tooltip.3", "CPU Frames hold AE2 Crafting Storages and Crafting Co-Processing Units.");
        lang.addRawLang(KEY + "tooltip.4", "A job takes the storage it needs. Every job has all the co-processors.");
        lang.addRawLang(KEY + "tooltip.5", "Each casing tier above ULV gives a credit: ×4 storage or ×4 " +
                "co-processors.");
        lang.addRawLang(KEY + "frame.tooltip", "Holds %s Crafting Storages or Crafting Co-Processing Units");
        lang.addRawLang(KEY + "interface.tooltip", "Connects an ME Data Center to an ME network");
        lang.addRawLang(KEY + "interface.no_data_center", "Not part of a formed ME Data Center");
        lang.addRawLang(KEY + "error.mixed_casings", "All Machine Casings must be of one tier");
        lang.addRawLang(KEY + "not_formed", "Structure not formed");
        lang.addRawLang(KEY + "incompatible", "This AE2 version is not supported");
        lang.addRawLang(KEY + "frame", "Casing tier: %s, size: %s");
        lang.addRawLang(KEY + "storage", "Storage: %s of %s bytes free");
        lang.addRawLang(KEY + "co_processors", "Co-processors: %s");
        lang.addRawLang(KEY + "jobs", "Crafting jobs: %s");
        lang.addRawLang(KEY + "credits", "Credits: %s of %s unspent");
        lang.addRawLang(KEY + "credits.tooltip.0", "One credit per casing tier above ULV");
        lang.addRawLang(KEY + "credits.tooltip.1", "Each gives ×4 storage or ×4 co-processors");
        lang.addRawLang(KEY + "credits.storage", "Storage: %s credits, ×%s");
        lang.addRawLang(KEY + "credits.co_processors", "Co-processors: %s credits, ×%s");
        lang.addRawLang(KEY + "credits.shift", "Shift-click: all");
        lang.addRawLang(KEY + "locked", "Locked while a crafting job runs");
        lang.addRawLang(KEY + "units", "Crafting units: view, search, put in and take out");
        lang.addRawLang(KEY + "units.title", "Crafting units: %s / %s");
        lang.addRawLang(KEY + "units.search", "Search");
        lang.addRawLang(KEY + "units.empty", "No crafting units");
        lang.addRawLang(KEY + "units.take", "Click: take a stack out. Shift-click: move a stack to your inventory");
        lang.addRawLang(KEY + "units.put", "Put Crafting Storages and Crafting Co-Processing Units here, or " +
                "Shift-click them in your inventory");
    }
}
