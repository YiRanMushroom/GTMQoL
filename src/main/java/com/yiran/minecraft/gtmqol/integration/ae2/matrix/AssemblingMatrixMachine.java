package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.crafter.ICrafterPattern;
import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;
import com.yiran.minecraft.gtmqol.common.crafter.PatternWear;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEHatchPartMachine;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.util.inv.CombinedInternalInventory;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.DynamicDrawable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Controller of the {@link AssemblingMatrix}. The frame's tier and the processing cores give its capacity; the work itself
 * is in {@link AssemblingMatrixWork}.
 */
public class AssemblingMatrixMachine extends MultiblockControllerMachine implements IMuiMachine {

    public static final String KEY = "gtmqol.assembling_matrix.";

    private static final int CRAFTS_PER_CORE = 4;
    private static final String PATTERNS_PANEL = "assembling_matrix_patterns";
    private static final String INPUT_SLOT_GROUP = "assembling_matrix_input";
    /** Half a machine panel and a gap: the patterns window stays clear of the player's inventory. */
    private static final int PATTERNS_PANEL_OFFSET = 92;
    private static final int VISIBLE_ROWS = 8;

    /** A pattern in the patterns window. */
    public record PatternEntry(int core, int slot, ItemStack stack, ItemStack icon) {

        static PatternEntry read(FriendlyByteBuf buf) {
            return new PatternEntry(buf.readVarInt(), buf.readVarInt(), buf.readItem(), buf.readItem());
        }

        static void write(FriendlyByteBuf buf, PatternEntry entry) {
            buf.writeVarInt(entry.core);
            buf.writeVarInt(entry.slot);
            buf.writeItem(entry.stack);
            buf.writeItem(entry.icon);
        }

        /**
         * By content: ItemStack has no equals of its own, and the UI resends the list whenever an entry differs. The icon
         * follows from the pattern. Unchanged slots keep their entry, so most comparisons end at the first check.
         */
        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof PatternEntry entry && entry.core == core && entry.slot == slot &&
                    ItemStack.matches(entry.stack, stack);
        }

        @Override
        public int hashCode() {
            return (31 * core + slot) * 31 + stack.getItem().hashCode();
        }
    }

    @SaveField
    private final AssemblingMatrixWork work = new AssemblingMatrixWork();
    @SaveField
    @SyncToClient
    private boolean autoUpload = true;

    private @Nullable AssemblingMatrixInterfaceMachine meInterface;
    private final List<AssemblingMatrixPatternCoreMachine> patternCores = new ArrayList<>();
    private final List<WirelessFEHatchPartMachine> feHatches = new ArrayList<>();
    private final List<NotifiableItemStackHandler> catalysts = new ArrayList<>();
    private InternalInventory terminalInventory = InternalInventory.empty();
    private int processingCores;
    private long capacity;
    private String size = "";
    private boolean patternsChanged;
    private @Nullable List<PatternEntry> entries;
    private List<PatternEntry> lastEntries = List.of();

    public AssemblingMatrixMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) subscribeServerTick(this::tick);
    }

    // ---------- structure ----------

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!isFormed()) return;
        PatternState state = patternStates.get(substructureName);
        var bounds = AssemblingMatrix.bounds().apply(getLevel(), getBlockPos().mutable(), getFrontFacing(),
                getUpwardsFacing());
        if (Math.abs(bounds.getInt(2) - bounds.getInt(3)) > 1) {
            state.setError(new PatternStringError(
                    Component.translatable("gtceu.predicate_error.cleanroom.not_centered")));
            invalidateStructure();
            return;
        }
        int tier = -1;
        int processing = 0;
        var casings = AssemblingMatrix.casings();
        Block processingCore = AssemblingMatrix.PROCESSING_CORE.get();
        for (var info : state.getCache().values()) {
            Block block = info.getBlockState().getBlock();
            if (block == processingCore) {
                processing++;
                continue;
            }
            for (int i = 0; i < casings.size(); i++) {
                if (!casings.get(i).is(block)) continue;
                if (tier >= 0 && tier != i) {
                    state.setError(new PatternStringError(Component.translatable(KEY + "error.mixed_casings")));
                    invalidateStructure();
                    return;
                }
                tier = i;
            }
        }
        tier = Math.max(tier, GTValues.ULV);
        processingCores = processing;
        capacity = (long) processing * CRAFTS_PER_CORE << 2 * tier;
        size = String.format(Locale.ROOT, "%d x %d x %d", bounds.getInt(2) + bounds.getInt(3) + 1,
                bounds.getInt(0) + 1, bounds.getInt(5) + 1);
        setRenderState(getRenderState().setValue(AssemblingMatrix.CASING_TIER, tier));
        collectParts();
    }

    private void collectParts() {
        patternCores.clear();
        feHatches.clear();
        catalysts.clear();
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof AssemblingMatrixPatternCoreMachine core) {
                patternCores.add(core);
            } else if (part instanceof AssemblingMatrixInterfaceMachine machine) {
                meInterface = machine;
            } else if (part instanceof WirelessFEHatchPartMachine hatch) {
                feHatches.add(hatch);
            } else {
                for (var handlerList : part.getRecipeHandlers()) {
                    if (handlerList.getHandlerIO() != IO.IN) continue;
                    for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                        if (handler instanceof NotifiableItemStackHandler bus) catalysts.add(bus);
                    }
                }
            }
        }
        terminalInventory = new CombinedInternalInventory(patternCores.stream()
                .map(AssemblingMatrixPatternCoreMachine::terminalInventory)
                .toArray(InternalInventory[]::new));
        onPatternsChanged();
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        meInterface = null;
        patternCores.clear();
        feHatches.clear();
        catalysts.clear();
        terminalInventory = InternalInventory.empty();
        processingCores = 0;
        capacity = 0;
        entries = null;
        lastEntries = List.of();
    }

    /** The casing of the frame, which is what its parts show. */
    public BlockState casingState() {
        return AssemblingMatrix.casings().get(getRenderState().getValue(AssemblingMatrix.CASING_TIER)).getDefaultState();
    }

    InternalInventory terminalInventory() {
        return terminalInventory;
    }

    // ---------- crafting (server) ----------

    void onPatternsChanged() {
        patternsChanged = true;
        entries = null;
    }

    private void tick() {
        if (!isFormed() || meInterface == null) return;
        if (patternsChanged) {
            patternsChanged = false;
            meInterface.updatePatterns(decodePatterns());
        }
        work.tick(this);
        RecipeLogic.Status status = !work.isWorking() ? RecipeLogic.Status.IDLE :
                work.isWaitingForFE() ? RecipeLogic.Status.WAITING : RecipeLogic.Status.WORKING;
        // no markAsChanged(): the work isn't synced to clients, and GTCEu marks the chunk for saving every tick anyway
        var renderState = getRenderState();
        if (renderState.getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS) != status) {
            setRenderState(renderState.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
        }
    }

    private List<IPatternDetails> decodePatterns() {
        Set<IPatternDetails> patterns = new LinkedHashSet<>();
        for (AssemblingMatrixPatternCoreMachine core : patternCores) {
            for (int slot = 0; slot < AssemblingMatrixPatternCoreMachine.SLOTS; slot++) {
                IPatternDetails details = core.decoded(slot, getLevel());
                if (details instanceof IMolecularAssemblerSupportedPattern ||
                        details instanceof CraftingPatternDetails) {
                    patterns.add(details);
                }
            }
        }
        return List.copyOf(patterns);
    }

    /**
     * Takes over {@code crafts} crafts; our patterns need their catalysts first.
     *
     * @return false, having changed nothing, when a catalyst is missing or can't do that many
     */
    boolean push(IPatternDetails pattern, KeyCounter[] inputs, long crafts) {
        if (pattern instanceof CraftingPatternDetails details && !useCatalysts(details.plan(), crafts)) return false;
        work.add(pattern, inputs, crafts);
        return true;
    }

    private boolean useCatalysts(ICrafterPattern pattern, long crafts) {
        for (var input : pattern.itemInputs()) {
            if (input.consumed() || input.changes()) continue;
            if (!hasCatalyst(input)) return false;
        }
        return crafts <= Integer.MAX_VALUE && PatternWear.wear(catalysts, pattern, (int) crafts);
    }

    private boolean hasCatalyst(ICrafterPattern.ItemInput input) {
        for (NotifiableItemStackHandler bus : catalysts) {
            for (int slot = 0; slot < bus.storage.getSlots(); slot++) {
                if (input.ingredient().test(bus.storage.getStackInSlot(slot))) return true;
            }
        }
        return false;
    }

    long capacity() {
        return capacity;
    }

    boolean isAutoUpload() {
        return autoUpload;
    }

    void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        getSyncDataHolder().markClientSyncFieldDirty("autoUpload");
        markAsChanged();
    }

    @Override
    public void onMachineDestroyed() {
        if (!isRemote()) work.giveBackAll(this);
        super.onMachineDestroyed();
    }

    private void cancelWork() {
        work.cancel();
        markAsChanged();
    }

    /** Pays for up to {@code crafts} crafts from the FE hatches; how many it could. */
    long pay(long crafts) {
        double perCraft = GTMQoLConfig.get().ae2.assemblingMatrixFEPerCraft;
        if (perCraft <= 0) return crafts;
        double wanted = perCraft * crafts;
        double available = 0;
        // hatches bound to one wireless network each see all of its FE: count it once
        Set<Object> networks = new HashSet<>();
        for (WirelessFEHatchPartMachine hatch : feHatches) {
            Object network = hatch.binding.getNetworkId();
            if (!networks.add(network == null ? hatch : network)) continue;
            available += hatch.extractFE(wanted - available, true);
            if (available >= wanted) break;
        }
        long paid = Math.min(crafts, (long) (available / perCraft));
        double left = paid * perCraft;
        for (WirelessFEHatchPartMachine hatch : feHatches) {
            if (left <= 0) break;
            left -= hatch.extractFE(left, false);
        }
        return paid;
    }

    /** What the ME network took. */
    long insert(AEKey what, long amount) {
        return meInterface == null ? 0 : meInterface.insert(what, amount);
    }

    // ---------- UI ----------

    /** Rebuilt after a change, keeping the entries of slots that still hold the same pattern. */
    private List<PatternEntry> patternEntries() {
        if (entries == null) {
            Map<Integer, PatternEntry> before = new HashMap<>();
            for (PatternEntry entry : lastEntries) {
                before.put(entry.core() * AssemblingMatrixPatternCoreMachine.SLOTS + entry.slot(), entry);
            }
            List<PatternEntry> list = new ArrayList<>();
            for (int core = 0; core < patternCores.size(); core++) {
                var patterns = patternCores.get(core).patterns();
                for (int slot = 0; slot < AssemblingMatrixPatternCoreMachine.SLOTS; slot++) {
                    ItemStack stack = patterns.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    PatternEntry old = before.get(core * AssemblingMatrixPatternCoreMachine.SLOTS + slot);
                    list.add(old != null && ItemStack.matches(old.stack(), stack) ? old :
                            new PatternEntry(core, slot, stack.copy(), icon(patternCores.get(core), slot, stack)));
                }
            }
            entries = list;
            lastEntries = list;
        }
        return entries;
    }

    /** What the pattern makes, as the window shows it: decoded here, so the client never decodes thousands. */
    private ItemStack icon(AssemblingMatrixPatternCoreMachine core, int slot, ItemStack stack) {
        IPatternDetails details = core.decoded(slot, getLevel());
        GenericStack output = details == null ? null : details.getPrimaryOutput();
        if (output != null && output.what() instanceof AEItemKey item) {
            return item.toStack((int) Math.min(output.amount(), Integer.MAX_VALUE));
        }
        return stack.copy();
    }

    private void take(int core, int slot, boolean toInventory, Player player) {
        if (core < 0 || core >= patternCores.size()) return;
        ItemStack stack = patternCores.get(core).patterns().extractItem(slot, 1, false);
        if (stack.isEmpty()) return;
        if (toInventory || !player.containerMenu.getCarried().isEmpty()) {
            player.getInventory().placeItemBackInInventory(stack);
        } else {
            player.containerMenu.setCarried(stack);
        }
    }

    private void putCarried(Player player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (PatternCrafter.isPattern(carried)) player.containerMenu.setCarried(addPattern(carried));
    }

    /** Puts it in the first free places of the pattern cores; what didn't fit. */
    private ItemStack addPattern(ItemStack stack) {
        for (AssemblingMatrixPatternCoreMachine core : patternCores) {
            for (int slot = 0; slot < AssemblingMatrixPatternCoreMachine.SLOTS && !stack.isEmpty(); slot++) {
                stack = core.patterns().insertItem(slot, stack, false);
            }
        }
        return stack;
    }

    private boolean hasRoom() {
        for (AssemblingMatrixPatternCoreMachine core : patternCores) {
            for (int slot = 0; slot < AssemblingMatrixPatternCoreMachine.SLOTS; slot++) {
                if (core.patterns().getStackInSlot(slot).isEmpty()) return true;
            }
        }
        return false;
    }

    /** The patterns window's slot: never holds anything, what it gets goes straight to the pattern cores. */
    private final class PatternInput implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!isRemote() && !stack.isEmpty()) addPattern(stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            if (!simulate) return addPattern(stack);
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        /** The client doesn't know the cores: it lets anything through, and the server has the last word. */
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return PatternCrafter.isPattern(stack) && (isRemote() || hasRoom());
        }
    }

    private TextWidget<?> line(Supplier<Component> text) {
        return new TextWidget<>(Text.dynamic(text::get));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var formed = new BooleanSyncValue(this::isFormed);
        var tier = new IntSyncValue(() -> getRenderState().getValue(AssemblingMatrix.CASING_TIER));
        var sizeValue = new StringSyncValue(() -> size);
        var processing = new IntSyncValue(() -> processingCores);
        var cores = new IntSyncValue(patternCores::size);
        var capacityValue = new LongSyncValue(() -> capacity);
        var patternCount = new IntSyncValue(() -> isRemote() ? 0 : patternEntries().size());
        var crafts = new LongSyncValue(work::crafts);
        var stored = new LongSyncValue(work::stored);
        var waitingForFE = new BooleanSyncValue(work::isWaitingForFE);
        var canCancel = new BooleanSyncValue(work::canCancel);
        syncManager.syncValue("formed", formed);
        syncManager.syncValue("tier", tier);
        syncManager.syncValue("size", sizeValue);
        syncManager.syncValue("processing", processing);
        syncManager.syncValue("cores", cores);
        syncManager.syncValue("capacity", capacityValue);
        syncManager.syncValue("patternCount", patternCount);
        syncManager.syncValue("crafts", crafts);
        syncManager.syncValue("stored", stored);
        syncManager.syncValue("waitingForFE", waitingForFE);
        syncManager.syncValue("canCancel", canCancel);
        syncManager.registerSyncedAction("cancel", false, true, buf -> cancelWork());
        Player player = guiData.getPlayer();
        syncManager.registerSyncedAction("take", false, true,
                buf -> take(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), player));
        syncManager.registerSyncedAction("put", false, true, buf -> putCarried(player));
        var patterns = syncManager.syncedPanel(PATTERNS_PANEL, false,
                (manager, handler) -> patternsPanel(syncManager, manager, patternCount));

        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(2)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(line(() -> !formed.getBoolValue() ?
                        Component.translatable(KEY + "not_formed").withStyle(ChatFormatting.RED) :
                        Component.translatable(KEY + "frame", GTValues.VNF[tier.getIntValue()],
                                sizeValue.getStringValue())))
                .child(line(() -> Component.translatable(KEY + "capacity",
                        FormattingUtil.formatNumbers(capacityValue.getLongValue()))))
                .child(line(() -> Component.translatable(KEY + "cores", processing.getIntValue(),
                        cores.getIntValue())))
                .child(line(() -> Component.translatable(KEY + "pattern_count",
                        FormattingUtil.formatNumbers(patternCount.getIntValue()),
                        FormattingUtil.formatNumbers((long) cores.getIntValue() * AssemblingMatrixPatternCoreMachine.SLOTS))))
                .child(line(() -> waitingForFE.getBoolValue() ?
                        Component.translatable(KEY + "no_fe").withStyle(ChatFormatting.RED) :
                        Component.translatable(KEY + "crafting",
                                FormattingUtil.formatNumbers(crafts.getLongValue()))))
                .child(line(() -> Component.translatable(KEY + "stored",
                        FormattingUtil.formatNumbers(stored.getLongValue()))))
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(2)
                        .child(new ButtonWidget<>()
                                .size(18)
                                .overlay(new ItemDrawable(PatternCrafter.PATTERN.get()).asIcon().size(16))
                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(KEY + "patterns")))
                                .onMousePressed((context, button) -> {
                                    patterns.togglePanel();
                                    return true;
                                }))
                        .child(GTMuiWidgets.createToggleButton(this::isAutoUpload, this::setAutoUpload,
                                IO.OUT.uiTexture, KEY + "auto_upload"))
                        .child(new ButtonWidget<>()
                                .size(18)
                                .overlay(new DynamicDrawable(() -> canCancel.getBoolValue() ?
                                        GTGuiTextures.REFUND_OVERLAY.asIcon().size(16) :
                                        new DrawableStack(GTGuiTextures.REFUND_OVERLAY, new ItemDrawable(Items.BARRIER))
                                                .asIcon().size(16)))
                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(KEY + "cancel.0"))
                                        .addLine(Text.lang(KEY + "cancel.1")))
                                // Shift, so it isn't pressed by mistake: it drops all the matrix's work
                                .onMousePressed((context, button) -> {
                                    if (!canCancel.getBoolValue() || !MouseData.create(button).shift()) return false;
                                    syncManager.callSyncedAction("cancel", buf -> {});
                                    return true;
                                }))));
    }

    private ModularPanel<?> patternsPanel(PanelSyncManager main, PanelSyncManager manager, IntSyncValue patternCount) {
        var list = new GenericListSyncHandler.Builder<PatternEntry>()
                .getter(() -> isRemote() ? List.of() : patternEntries())
                .serializer(PatternEntry::write)
                .deserializer(PatternEntry::read)
                // entries are never changed, so the cache can hold them as they are
                .immutableCopy()
                .build();
        manager.syncValue("entries", list);
        manager.registerSlotGroup(new SlotGroup(INPUT_SLOT_GROUP, 1).setAllowSorting(false));
        var search = new StringValue("");
        int gridWidth = AssemblingMatrixPatternGridWidget.COLUMNS * AssemblingMatrixPatternGridWidget.CELL;
        var grid = new AssemblingMatrixPatternGridWidget(list::getValue, search::getStringValue,
                (entry, toInventory) -> main.callSyncedAction("take", buf -> {
                    buf.writeVarInt(entry.core());
                    buf.writeVarInt(entry.slot());
                    buf.writeBoolean(toInventory);
                }),
                () -> main.callSyncedAction("put", buf -> {}));
        // not deleteCachedPanel: MUI refuses that with an item slot in the panel
        return popup(PATTERNS_PANEL, gridWidth + 24, VISIBLE_ROWS * AssemblingMatrixPatternGridWidget.CELL + 52)
                // beside the machine panel and open while the inventory is in use, patterns come from there
                .closeOnOutOfBoundsClick(false)
                .leftRel(0.5f, PATTERNS_PANEL_OFFSET, 0f)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(line(() -> Component.translatable(KEY + "patterns.title",
                                FormattingUtil.formatNumbers(patternCount.getIntValue()))))
                        .child(Flow.row()
                                .size(gridWidth, 18)
                                .childPadding(4)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .child(new TextFieldWidget()
                                        .size(gridWidth - 22, 14)
                                        .autoUpdateOnChange(true)
                                        .hintText(Component.translatable(KEY + "patterns.search"))
                                        .value(search))
                                .child(new ItemSlot()
                                        .slot(new ModularSlot(new PatternInput(), 0).slotGroup(INPUT_SLOT_GROUP))
                                        .backgroundOverlay(GTGuiTextures.PATTERN_OVERLAY)
                                        .tooltipBuilder(tooltip -> tooltip.addLine(
                                                Text.lang(KEY + "patterns.put")))))
                        .child(new ListWidget<>()
                                .size(gridWidth + 8, VISIBLE_ROWS * AssemblingMatrixPatternGridWidget.CELL)
                                .child(grid)));
    }

    static void addLang() {
        var lang = GTMQoLAddon.registrate();
        lang.addRawLang(KEY + "tooltip.0", "Crafts in bulk for an ME network: AE2 Crafting, Stonecutting and Smithing " +
                "Patterns, and Crafting Patterns encoded in a Pattern Crafter.");
        lang.addRawLang(KEY + "tooltip.1", "A box of 3 to 15 blocks each way: edges of Machine Casings, faces of " +
                "Quartz Glass. Inside: Assembling Matrix Processing Cores, Assembling Matrix Pattern Cores or air.");
        lang.addRawLang(KEY + "tooltip.2", "All Machine Casings must be of one tier, and a higher tier crafts faster. " +
                "The structure preview shows ULV, but any tier works.");
        lang.addRawLang(KEY + "tooltip.3", "Each Assembling Matrix Processing Core does 4 crafts per tick, ×4 for each " +
                "casing tier above ULV. Each craft uses %s FE.");
        lang.addRawLang(KEY + "tooltip.4", "On the edges: one Assembling Matrix ME Interface, at least one Wireless FE " +
                "Input Hatch, and Input Buses for catalysts.");
        lang.addRawLang(KEY + "tooltip.5", "Catalysts: tools, smithing templates and other items a Crafting Pattern " +
                "keeps instead of using up. They stay in the Input Buses, where tools wear down.");
        lang.addRawLang(KEY + "processing_core.tooltip", "Does 4 crafts per tick inside an Assembling Matrix, ×4 for " +
                "each casing tier above ULV");
        lang.addRawLang(KEY + "pattern_core.tooltip", "Holds 72 patterns inside an Assembling Matrix");
        lang.addRawLang(KEY + "interface.tooltip", "Connects an Assembling Matrix to an ME network: offers its " +
                "patterns to crafting jobs and sends the products back");
        lang.addRawLang(KEY + "interface.rename", "Name in Pattern Access Terminals");
        lang.addRawLang(KEY + "interface.no_matrix", "Not part of a formed Assembling Matrix");
        lang.addRawLang(KEY + "error.mixed_casings", "All Machine Casings must be of one tier");
        lang.addRawLang(KEY + "not_formed", "Structure not formed");
        lang.addRawLang(KEY + "frame", "Casing tier: %s, size: %s");
        lang.addRawLang(KEY + "capacity", "Speed: %s crafts per tick");
        lang.addRawLang(KEY + "cores", "Cores: %s processing, %s pattern");
        lang.addRawLang(KEY + "pattern_count", "Patterns: %s / %s");
        lang.addRawLang(KEY + "crafting", "Crafts queued: %s");
        lang.addRawLang(KEY + "no_fe", "Not enough FE, crafting paused");
        lang.addRawLang(KEY + "stored", "Products to send: %s");
        lang.addRawLang(KEY + "patterns", "Patterns: view, search, put in and take out");
        lang.addRawLang(KEY + "patterns.title", "Patterns: %s");
        lang.addRawLang(KEY + "patterns.search", "Search");
        lang.addRawLang(KEY + "patterns.empty", "No patterns");
        lang.addRawLang(KEY + "patterns.take", "Click: take out. Shift-click: move to your inventory");
        lang.addRawLang(KEY + "patterns.put", "Put patterns here, or Shift-click patterns in your inventory");
        lang.addRawLang(KEY + "auto_upload.enabled", "Auto upload: on. Crafting, stonecutting and smithing patterns " +
                "encoded with Shift-click in a Pattern Encoding Terminal on the same ME network move into this " +
                "Assembling Matrix");
        lang.addRawLang(KEY + "auto_upload.disabled", "Auto upload: off. Encoded patterns stay in the Pattern " +
                "Encoding Terminal");
        lang.addRawLang(KEY + "cancel.0", "Shift-click: cancel all crafts not done yet and return their ingredients " +
                "to the ME network");
        lang.addRawLang(KEY + "cancel.1", "Cancel the crafting job in the Crafting Status first, or the job waits " +
                "for the products forever");
        lang.addRawLang(KEY + "uploaded", "Pattern moved into an Assembling Matrix");
        lang.addRawLang(KEY + "already_there", "Not uploaded: an Assembling Matrix already has this pattern");
        lang.addRawLang(KEY + "no_room", "Not uploaded: no Assembling Matrix with auto upload has room");
        lang.addRawLang(KEY + "stays", "The pattern stays in the terminal");
    }
}
