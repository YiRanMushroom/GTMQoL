package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * ME stocking input bus and stocking input hatch in one. The item half is the stocking bus itself; the fluid half
 * is a second stocking list. The main UI shows the item list on the left and the fluid list on the right. Auto
 * pull applies to both, the minimum stack size and the cycle time are separate for items and fluids.
 */
public class MEDualInputPartMachine extends MEStockingBusPartMachine {

    public static final String ITEMS_KEY = "gtmqol.gui.dual_input.items";
    public static final String FLUIDS_KEY = "gtmqol.gui.dual_input.fluids";

    private final StockingFluidList fluidList;
    private Predicate<GenericStack> fluidPullTest = $ -> false;
    // The item side is the stocking bus's own minStackSize / ticksPerCycle.
    @SaveField
    private int fluidMinStackSize = 1;
    @SaveField
    private int fluidTicksPerCycle = 40;

    public MEDualInputPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.fluidList = new StockingFluidList(CONFIG_SIZE);
        // attachTrait alone doesn't save the trait's data (the config would be lost on reload).
        attachPersistentTrait("fluid_config", fluidList);
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        super.autoIO();
        if (fluidTicksPerCycle <= 0) fluidTicksPerCycle = 40;
        if (getOffsetTimer() % fluidTicksPerCycle == 0) {
            if (isAutoPull()) refreshFluids();
            syncFluids();
        }
    }

    private void syncFluids() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return;
        MEStorage networkInv = grid.getStorageService().getInventory();
        for (ExportOnlyAEFluidSlot slot : fluidList.getInventory()) {
            var config = slot.getConfig();
            if (config != null) {
                var key = config.what();
                long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted >= fluidMinStackSize) {
                    slot.setStock(new GenericStack(key, extracted));
                    continue;
                }
            }
            slot.setStock(null);
        }
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        super.setAutoPull(autoPull);
        if (!isRemote() && fluidList != null) {
            if (!autoPull) {
                fluidList.clearInventory(0);
            } else if (updateMEStatus()) {
                refreshFluids();
            }
        }
    }

    @Override
    public void setAutoPullTest(Predicate<GenericStack> autoPullTest) {
        this.fluidPullTest = autoPullTest;
        super.setAutoPullTest(autoPullTest);
    }

    /** Same as the stocking hatch: the biggest fluids in the network become the config. */
    private void refreshFluids() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            fluidList.clearInventory(0);
            return;
        }
        MEStorage networkStorage = grid.getStorageService().getInventory();
        PriorityQueue<Object2LongMap.Entry<AEKey>> top = new PriorityQueue<>(
                Comparator.comparingLong(Object2LongMap.Entry<AEKey>::getLongValue));
        for (Object2LongMap.Entry<AEKey> entry : networkStorage.getAvailableStacks()) {
            long amount = entry.getLongValue();
            AEKey what = entry.getKey();
            if (amount <= 0 || !(what instanceof AEFluidKey)) continue;
            if (networkStorage.extract(what, amount, Actionable.SIMULATE, actionSource) == 0) continue;
            if (fluidPullTest != null && !fluidPullTest.test(new GenericStack(what, amount))) continue;
            if (amount >= fluidMinStackSize) {
                if (top.size() < CONFIG_SIZE) {
                    top.offer(entry);
                } else if (amount > top.peek().getLongValue()) {
                    top.poll();
                    top.offer(entry);
                }
            }
        }
        int index;
        int count = top.size();
        for (index = 0; index < CONFIG_SIZE; index++) {
            if (top.isEmpty()) break;
            Object2LongMap.Entry<AEKey> entry = top.poll();
            AEKey what = entry.getKey();
            long request = networkStorage.extract(what, entry.getLongValue(), Actionable.SIMULATE, actionSource);
            // poll() gives the smallest first, show the biggest first
            var slot = fluidList.getInventory()[count - index - 1];
            slot.setConfig(new GenericStack(what, 1));
            slot.setStock(new GenericStack(what, request));
        }
        fluidList.clearInventory(index);
    }

    /////////////////////////////////
    // ****** Duplicate checks *****//
    /////////////////////////////////

    @Override
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!(config.what() instanceof AEFluidKey)) return super.testConfiguredInOtherPart(config);
        if (!isFormed() || isDistinct()) return false;
        for (MultiblockControllerMachine controller : getControllers()) {
            for (MultiblockPartMachine part : controller.getParts()) {
                if (part instanceof MEDualInputPartMachine dual) {
                    if (dual == this || dual.isDistinct()) continue;
                    if (dual.fluidList.hasStackInConfig(config, false)) return true;
                } else if (part instanceof MEStockingHatchPartMachine hatch) {
                    if (hatch.getSlotList().hasStackInConfig(config, false)) return true;
                }
            }
        }
        return MEUniversalInputPartMachine.configuredInAny(this, config);
    }

    /** For {@link MEUniversalInputPartMachine}'s duplicate check. */
    boolean hasFluidInConfig(GenericStack config) {
        return fluidList.hasStackInConfig(config, false);
    }

    @Override
    public void validateConfig() {
        super.validateConfig();
        for (int i = 0; i < fluidList.getConfigurableSlots(); i++) {
            var slot = fluidList.getConfigurableSlot(i);
            if (slot.getConfig() != null && testConfiguredInOtherPart(slot.getConfig())) {
                slot.setConfig(null);
                slot.setStock(null);
            }
        }
    }

    ////////////////////////////////
    // ****** Configuration ******//
    ////////////////////////////////

    @Override
    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = super.writeConfigToTag();
        if (!isAutoPull()) {
            CompoundTag configStacks = new CompoundTag();
            for (int i = 0; i < CONFIG_SIZE; i++) {
                GenericStack config = fluidList.getInventory()[i].getConfig();
                if (config != null) configStacks.put(Integer.toString(i), GenericStack.writeTag(config));
            }
            tag.put("FluidConfigStacks", configStacks);
        }
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        super.readConfigFromTag(tag);
        if (tag.getBoolean("AutoPull")) return;
        // A tag copied from a plain stocking bus has no fluids: clear them.
        CompoundTag configStacks = tag.getCompound("FluidConfigStacks");
        for (int i = 0; i < CONFIG_SIZE; i++) {
            String key = Integer.toString(i);
            fluidList.getInventory()[i].setConfig(
                    configStacks.contains(key) ? GenericStack.readTag(configStacks.getCompound(key)) : null);
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    // IMEStockingPart#getPanelBuilder with the settings popup split into an item and a fluid column.
    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IPanelHandler settingsPanelHandler = syncManager.syncedPanel("stocking_settings", true,
                (sm, sh) -> PopupPanel.createPopupPanel("stocking_settings_panel", 290, 90)
                        .child(Flow.row()
                                .coverChildren()
                                .child(settingsColumn(ITEMS_KEY, this::getMinStackSize, this::setMinStackSize,
                                        this::getTicksPerCycle, this::setTicksPerCycle).marginRight(10))
                                .child(settingsColumn(FLUIDS_KEY, () -> fluidMinStackSize,
                                        size -> fluidMinStackSize = size, () -> fluidTicksPerCycle,
                                        ticks -> fluidTicksPerCycle = ticks))
                                .margin(5)));

        return MachineUIPanelBuilder.panelBuilder(this.self())
                .rightConfigurators(f -> f
                        .child(new ToggleButton()
                                .value(new BooleanSyncValue(this::isAutoPull, this::setAutoPull).allowC2S())
                                .stateOverlay(GTGuiTextures.BUTTON_AUTO_PULL)
                                .tooltipAutoUpdate(true)
                                .tooltipBuilder(r -> r.addLine(Text.lang("gtceu.gui.me_network.auto_pull_toggle"))))
                        .child(new ButtonWidget<>()
                                .size(18)
                                .onMousePressed((context, b) -> {
                                    settingsPanelHandler.openPanel();
                                    return true;
                                })
                                .overlay(new ItemDrawable(GTItems.TOOL_DATA_STICK.asItem()).asIcon().size(16))
                                .tooltip(new RichTooltip()
                                        .addLine(Text.lang("gtceu.gui.me_network.stocking_settings")))));
    }

    private static Flow settingsColumn(String titleKey, IntSupplier minStack, IntConsumer setMinStack,
                                       IntSupplier ticks, IntConsumer setTicks) {
        return Flow.col()
                .coverChildren()
                .child(Text.lang(titleKey).asWidget().marginBottom(2))
                .child(Text.lang("gtceu.gui.me_network.min_stack_size").asWidget())
                .child(new TextFieldWidget()
                        .size(120, 18)
                        .value(SyncHandlers.intNumber(minStack, setMinStack).allowC2S())
                        .setNumbers(1, Integer.MAX_VALUE))
                .child(Text.lang("gtceu.gui.me_network.ticks_per_cycle").asWidget())
                .child(new TextFieldWidget()
                        .size(120, 18)
                        .value(SyncHandlers.intNumber(ticks, setTicks).allowC2S())
                        .setNumbers(1, 200));
    }

    // Items on the left, fluids on the right. Both lists are AEConfigWidgets, but the fluid one is our copy with
    // its own sync names, see FluidConfigWidget.
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        registerConfigActions(syncManager);
        registerFluidActions(syncManager);
        var flow = Flow.col().coverChildren();
        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));
        flow.child(Flow.row()
                .coverChildren()
                .child(Flow.col()
                        .coverChildren()
                        .child(Text.lang(ITEMS_KEY).asWidget().marginBottom(2))
                        .child(new AEConfigWidget(getSlotList(), CONFIG_SIZE, false)
                                .syncManager(syncManager)
                                .size(8 * 18, 2 * (18 * 2 + 2)))
                        .marginRight(8))
                .child(Flow.col()
                        .coverChildren()
                        .child(Text.lang(FLUIDS_KEY).asWidget().marginBottom(2))
                        .child(new FluidConfigWidget(fluidList, CONFIG_SIZE, this::isAutoPull)
                                .syncManager(syncManager)
                                .size(8 * 18, 2 * (18 * 2 + 2)))));
        mainWidget.child(flow.center());
    }

    // MEInputHatchPartMachine#registerConfigActions, on our list, with FluidConfigWidget's names.
    private void registerFluidActions(PanelSyncManager sm) {
        sm.registerServerSyncedAction(FluidConfigWidget.SET, packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            ItemStack held = sm.getPlayer().containerMenu.getCarried();
            FluidUtil.getFluidContained(held)
                    .ifPresent(fluid -> fluidList.getInventory()[index].setConfig(AEUtil.fromFluidStack(fluid)));
        });
        sm.registerServerSyncedAction(FluidConfigWidget.CLEAR, packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            fluidList.getInventory()[index].setConfig(null);
        });
        sm.registerServerSyncedAction(FluidConfigWidget.SET_GHOST, packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            if (packet.readBoolean()) {
                FluidStack fluid = FluidStack.readFromPacket(packet);
                if (!fluid.isEmpty()) {
                    fluidList.getInventory()[index].setConfig(AEUtil.fromFluidStack(fluid));
                }
            }
        });
    }

    ////////////////////////////////
    // ******* Fluid slots *******//
    ////////////////////////////////

    // MEStockingHatchPartMachine's private list and slot, bound to this machine.
    private class StockingFluidList extends ExportOnlyAEFluidList {

        StockingFluidList(int slots) {
            super(MEDualInputPartMachine.this, slots, StockingFluidSlot::new);
        }

        @Override
        public boolean isAutoPull() {
            return MEDualInputPartMachine.this.isAutoPull();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
            if (super.hasStackInConfig(stack, false)) return true;
            return checkExternal && testConfiguredInOtherPart(stack);
        }
    }

    private class StockingFluidSlot extends ExportOnlyAEFluidSlot {

        StockingFluidSlot() {
            super();
        }

        StockingFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new StockingFluidSlot(this.config == null ? null : copy(this.config),
                    this.stock == null ? null : copy(this.stock));
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (this.stock != null && this.config != null) {
                // Take from the network when the recipe is checked (simulate) or run (execute).
                if (!isOnline()) return FluidStack.EMPTY;
                MEStorage network = getMainNode().getGrid().getStorageService().getInventory();
                var key = config.what();
                long extracted = network.extract(key, maxDrain,
                        action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE, actionSource);
                if (extracted > 0) {
                    FluidStack result = key instanceof AEFluidKey fluidKey ?
                            AEUtil.toFluidStack(fluidKey, extracted) : FluidStack.EMPTY;
                    if (action.execute()) {
                        this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                        if (this.stock.amount() == 0) this.stock = null;
                        onContentsChanged();
                    }
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
