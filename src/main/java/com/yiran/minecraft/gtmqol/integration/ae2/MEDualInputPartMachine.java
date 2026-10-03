package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/**
 * ME stocking input bus and stocking input hatch in one. The item half is the stocking bus itself (its main UI);
 * the fluid half is a second stocking list with its own UI, opened from the left configurators. Auto pull, the
 * minimum stack size and the cycle time apply to both.
 */
public class MEDualInputPartMachine extends MEStockingBusPartMachine {

    public static final String FLUIDS_KEY = "gtmqol.gui.dual_input.fluids";

    private final StockingFluidList fluidList;
    private Predicate<GenericStack> fluidPullTest = $ -> false;

    public MEDualInputPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.fluidList = attachTrait(new StockingFluidList(CONFIG_SIZE));
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    @Override
    public void autoIO() {
        super.autoIO();
        if (isAutoPull() && getOffsetTimer() % getTicksPerCycle() == 0) {
            refreshFluids();
        }
    }

    @Override
    protected void syncME() {
        super.syncME();
        MEStorage networkInv = getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEFluidSlot slot : fluidList.getInventory()) {
            var config = slot.getConfig();
            if (config != null) {
                var key = config.what();
                long extracted = networkInv.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted >= getMinStackSize()) {
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
            if (amount >= getMinStackSize()) {
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
        return false;
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
    protected CompoundTag writeConfigToTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.writeConfigToTag(provider);
        if (!isAutoPull()) {
            CompoundTag configStacks = new CompoundTag();
            for (int i = 0; i < CONFIG_SIZE; i++) {
                GenericStack config = fluidList.getInventory()[i].getConfig();
                if (config != null) configStacks.put(Integer.toString(i), GenericStack.writeTag(provider, config));
            }
            tag.put("FluidConfigStacks", configStacks);
        }
        return tag;
    }

    @Override
    protected void readConfigFromTag(HolderLookup.Provider provider, CompoundTag tag) {
        super.readConfigFromTag(provider, tag);
        if (tag.getBoolean("AutoPull")) return;
        // A tag copied from a plain stocking bus has no fluids: clear them.
        CompoundTag configStacks = tag.getCompound("FluidConfigStacks");
        for (int i = 0; i < CONFIG_SIZE; i++) {
            String key = Integer.toString(i);
            fluidList.getInventory()[i].setConfig(
                    configStacks.contains(key) ? GenericStack.readTag(provider, configStacks.getCompound(key)) : null);
        }
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    // AEConfigWidget talks over fixed action names ("ae_config_set", ...), so the fluid list can't share a
    // PanelSyncManager with the item list. It gets its own panel, and with that its own manager.
    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var builder = super.getPanelBuilder(data, syncManager, settings);
        IPanelHandler panelHandler = syncManager.syncedPanel("gtmqol_dual_fluids", true, (sm, handler) -> {
            registerFluidActions(sm);
            return PopupPanel.createPopupPanel("gtmqol_dual_fluids_panel", 160, 112)
                    .child(Flow.col()
                            .coverChildren()
                            .child(Text.lang(FLUIDS_KEY).asWidget().marginBottom(4))
                            .child(new AEConfigWidget(fluidList, CONFIG_SIZE, true)
                                    .syncManager(sm)
                                    .size(8 * 18, 2 * (18 * 2 + 2)))
                            .margin(5));
        });
        var left = builder.leftConfigurators();
        return builder.leftConfigurators(left.andThen(f -> f.child(new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, b) -> {
                    if (b == InputConstants.MOUSE_BUTTON_LEFT) {
                        panelHandler.openPanel();
                        return true;
                    }
                    return false;
                })
                .overlay(Text.str("F").asIcon().size(16))
                .tooltip(new RichTooltip().addLine(Text.lang(FLUIDS_KEY))))));
    }

    // MEInputHatchPartMachine#registerConfigActions, on our list.
    private void registerFluidActions(PanelSyncManager sm) {
        sm.registerServerSyncedAction("ae_config_set", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            ItemStack held = sm.getPlayer().containerMenu.getCarried();
            FluidUtil.getFluidContained(held)
                    .ifPresent(fluid -> fluidList.getInventory()[index].setConfig(AEUtil.fromFluidStack(fluid)));
        });
        sm.registerServerSyncedAction("ae_config_clear", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            fluidList.getInventory()[index].setConfig(null);
        });
        sm.registerServerSyncedAction("ae_config_amount", packet -> {
            int index = packet.readVarInt();
            long amount = packet.readVarLong();
            if (index < 0 || index >= CONFIG_SIZE) return;
            var slot = fluidList.getInventory()[index];
            if (slot.getConfig() != null && amount > 0) {
                slot.setConfig(ExportOnlyAESlot.copy(slot.getConfig(), amount));
            }
        });
        sm.registerServerSyncedAction("ae_config_set_ghost", packet -> {
            int index = packet.readVarInt();
            if (index < 0 || index >= CONFIG_SIZE) return;
            if (packet.readBoolean()) {
                FluidStack fluid = FluidStack.STREAM_CODEC.decode(packet);
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
