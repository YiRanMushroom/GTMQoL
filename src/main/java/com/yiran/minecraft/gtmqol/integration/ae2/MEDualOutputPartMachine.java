package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.AEKeyStorageSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEStackDisplayWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.ScrollPreservingGrid;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ME output bus and output hatch in one. Items go through the ME output bus's buffer, fluids through a second
 * buffer here. Both are pushed into the network every {@code updateIntervals} ticks, like the plain bus and hatch
 * (batching whatever arrived in between). The UI lists both buffers together.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEDualOutputPartMachine extends MEOutputBusPartMachine {

    @SaveField
    private KeyStorage fluidBuffer = new KeyStorage();

    public MEDualOutputPartMachine(BlockEntityCreationInfo info) {
        super(info);
        var tank = attachTrait(new FluidBufferTank(fluidBuffer));
        fluidBuffer.setOnContentsChanged(() -> {
            tank.onContentsChanged();
            updateInventorySubscription();
        });
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        var grid = getMainNode().getGrid();
        if (grid != null && !fluidBuffer.isEmpty()) {
            for (var entry : fluidBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                        Actionable.MODULATE, actionSource);
            }
        }
    }

    @Override
    protected boolean shouldSubscribe() {
        return isWorkingEnabled() && isOnline() && (!fluidBuffer.isEmpty() || storageIterator().hasNext());
    }

    @Override
    public void autoIO() {
        if (!shouldSyncME()) return;
        if (!updateMEStatus()) return;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            var network = grid.getStorageService().getInventory();
            if (!fluidBuffer.isEmpty()) fluidBuffer.insertInventory(network, actionSource);
            flushItems(network);
        }
        updateInventorySubscription();
    }

    // The superclass's item buffer is private and its own autoIO only runs every updateIntervals ticks, so go
    // through storageIterator() (live entries of the same buffer) instead.
    private void flushItems(MEStorage network) {
        var it = storageIterator();
        boolean changed = false;
        while (it.hasNext()) {
            var entry = it.next();
            long amount = entry.getLongValue();
            long inserted = network.insert(entry.getKey(), amount, Actionable.MODULATE, actionSource);
            if (inserted <= 0) continue;
            changed = true;
            if (inserted >= amount) it.remove();
            else entry.setValue(amount - inserted);
        }
        if (changed) getInventory().notifyListeners();
    }

    // MEOutputBusPartMachine#buildMainUI, with the waiting list showing the fluid buffer too.
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        var flow = Flow.col().coverChildren();
        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        var storageSyncHandler = new BothBuffersSyncHandler();
        syncManager.syncValue("ae_output_display", storageSyncHandler);

        int[] savedScroll = { 0 };
        var dynamicHandler = new DynamicLinkedSyncHandler<>(storageSyncHandler)
                .widgetProvider((sm, value) -> {
                    var col = Flow.col().leftRel(0.5f).coverChildrenHeight();
                    var list = value.getValue();
                    if (list.isEmpty()) return col.child(Text.lang("gtceu.gui.waiting_list_empty").asWidget());
                    col.child(Text.lang("gtceu.gui.waiting_list").asWidget().margin(0, 2));
                    col.child(new ScrollPreservingGrid(savedScroll)
                            .size(167, 67)
                            .scrollable(new VerticalScrollData())
                            .gridOfSizeWidth(list.size(), 1, (x, y, index) -> {
                                var entry = list.get(index);
                                return Flow.row()
                                        .coverChildrenHeight()
                                        .child(new AEStackDisplayWidget(list, index))
                                        .child(Text.comp(Component
                                                .literal(FormattingUtil.formatNumbers(entry.amount()) + "x")
                                                .append(CommonComponents.SPACE)
                                                .append(entry.what().getDisplayName()))
                                                .asWidget()
                                                .width(140)
                                                .marginLeft(3));
                            }));
                    return col;
                });

        flow.child(new DynamicSyncedWidget<>()
                .syncHandler(dynamicHandler)
                .size(167, 80));

        mainWidget.child(flow);
    }

    // The item buffer is private to the superclass (only storageIterator() reaches it), so copy both buffers into
    // one KeyStorage right before the handler compares it with what the client has.
    private class BothBuffersSyncHandler extends AEKeyStorageSyncHandler {

        private final KeyStorage display;

        BothBuffersSyncHandler() {
            this(new KeyStorage());
        }

        private BothBuffersSyncHandler(KeyStorage display) {
            super(display);
            this.display = display;
        }

        @Override
        public boolean updateCacheFromSource(boolean isFirstSync) {
            display.storage.clear();
            storageIterator().forEachRemaining(entry -> display.storage.put(entry.getKey(), entry.getLongValue()));
            display.storage.putAll(fluidBuffer.storage);
            return super.updateCacheFromSource(isFirstSync);
        }
    }

    private static class FluidBufferTank extends NotifiableFluidTank {

        private final BufferStorage storage;

        FluidBufferTank(KeyStorage buffer) {
            super(List.of(new BufferStorage(buffer)), IO.OUT, IO.NONE);
            storage = (BufferStorage) getStorages()[0];
            allowSameFluids = true;
        }

        @Override
        public int getTanks() {
            return 128;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setFluidInTank(int tank, FluidStack fluidStack) {}

        @Override
        public int getTankCapacity(int tank) {
            return storage.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<FluidIngredient> left,
                                                       boolean simulate) {
            if (io != IO.OUT) return left;
            FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
            for (var it = left.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                FluidStack[] fluids;
                if (ingredient instanceof IntProviderFluidIngredient provider && simulate) {
                    fluids = new FluidStack[] { provider.getMaxSizeStack() };
                } else {
                    fluids = ingredient.getStacks();
                }
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                int remaining = fluids[0].getAmount() - storage.fill(fluids[0], action);
                if (remaining > 0) ingredient.setAmount(remaining);
                else it.remove();
            }
            return left;
        }
    }

    private static class BufferStorage extends CustomFluidTank {

        private final KeyStorage buffer;

        BufferStorage(KeyStorage buffer) {
            super(0);
            this.buffer = buffer;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var key = AEFluidKey.of(resource);
            int oldValue = GTMath.saturatedCast(buffer.storage.getOrDefault(key, 0));
            int change = Math.min(Integer.MAX_VALUE - oldValue, resource.getAmount());
            if (change > 0 && action.execute()) {
                buffer.storage.put(key, oldValue + change);
                buffer.onChanged();
            }
            return change;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
