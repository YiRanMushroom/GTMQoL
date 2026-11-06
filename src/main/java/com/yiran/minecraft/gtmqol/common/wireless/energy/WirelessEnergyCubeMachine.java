package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTMath;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * FE block on the bound network, like Mekanism's energy cube with the network as its storage: every side is set to
 * input, output or nothing. Input sides take what they are given, output sides push to their neighbours and can be
 * pulled from. Neither has a rate, only FE's own limit of what fits an int per call. It has no front: sides are the
 * world's, north to down.
 */
public class WirelessEnergyCubeMachine extends MetaMachine
                                       implements ITieredMachine, IMuiMachine, IDataStickInteractable {

    public static final String SIDE_KEY = "gtmqol.wireless.energy_cube.side.";
    public static final String MODE_KEY = "gtmqol.wireless.energy_cube.mode.";
    public static final String CHANGE_KEY = "gtmqol.wireless.energy_cube.change";

    public enum SideMode {

        NONE(0xFF606060, 0xFF808080),
        INPUT(0xFF3C9A3C, 0xFF50C050),
        OUTPUT(0xFFB83C3C, 0xFFE05050);

        private final IDrawable block;
        private final IDrawable hoveredBlock;

        SideMode(int color, int hoveredColor) {
            this.block = new Rectangle().color(color);
            this.hoveredBlock = new Rectangle().color(hoveredColor);
        }

        private SideMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        private String key() {
            return MODE_KEY + name().toLowerCase(Locale.ROOT);
        }
    }

    private static final int BITS_PER_SIDE = 2;
    private static final int SIDE_MASK = (1 << BITS_PER_SIDE) - 1;
    private static final int BUTTON_SIZE = 18;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private final WirelessFEBuffer buffer;

    /** The {@link SideMode} of each {@link Direction}, all off at first. */
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private int sideModes;

    public WirelessEnergyCubeMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.buffer = new WirelessFEBuffer(binding::getNetworkId);
        attachTrait(new SideStorage(SideMode.INPUT));
        attachTrait(new SideStorage(SideMode.OUTPUT));
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::pushEnergy);
    }

    public SideMode getMode(@Nullable Direction side) {
        if (side == null) return SideMode.NONE;
        return SideMode.values()[sideModes >> side.ordinal() * BITS_PER_SIDE & SIDE_MASK];
    }

    private void setMode(Direction side, SideMode mode) {
        int shift = side.ordinal() * BITS_PER_SIDE;
        sideModes = sideModes & ~(SIDE_MASK << shift) | mode.ordinal() << shift;
    }

    private void cycleMode(Direction side) {
        setMode(side, getMode(side).next());
        syncDataHolder.markClientSyncFieldDirty("sideModes");
        // what's on that side changed: cables and the capability caches of the neighbours have to look again
        getLevel().invalidateCapabilities(getBlockPos());
        getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
    }

    private void pushEnergy() {
        for (Direction side : Direction.values()) {
            if (getMode(side) != SideMode.OUTPUT) continue;
            IEnergyStorage target = GTCapabilityHelper.getForgeEnergy(getLevel(), getBlockPos().relative(side),
                    side.getOpposite());
            if (target == null || !target.canReceive()) continue;
            int offered = (int) buffer.extract(Integer.MAX_VALUE, true);
            if (offered <= 0) return;
            buffer.extract(target.receiveEnergy(offered, false), false);
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return binding.onDataStickUse(player, dataStick);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        return binding.onDataStickShiftUse(player, dataStick);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        // a map: north at the top, up in the middle as seen from above, down next to it
        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer())
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 60)
                .child(Flow.col().coverChildren()
                        .horizontalCenter()
                        .childPadding(2)
                        .child(sideRow(gap("north_left"), sideButton(syncManager, Direction.NORTH), gap("north_right")))
                        .child(sideRow(sideButton(syncManager, Direction.WEST), sideButton(syncManager, Direction.UP),
                                sideButton(syncManager, Direction.EAST)))
                        .child(sideRow(gap("south_left"), sideButton(syncManager, Direction.SOUTH),
                                sideButton(syncManager, Direction.DOWN)))));
    }

    private static Flow sideRow(IWidget left, IWidget middle, IWidget right) {
        return Flow.row().coverChildren().childPadding(2).child(left).child(middle).child(right);
    }

    /** Named: plain widgets count as equal, and a parent refuses a child equal to one it has. */
    private static Widget<?> gap(String name) {
        return new Widget<>().name(name).size(BUTTON_SIZE);
    }

    /** A square in the color of the side's mode, with the side's first letter. */
    private ButtonWidget<?> sideButton(PanelSyncManager syncManager, Direction side) {
        var cycle = new InteractionSyncHandler();
        cycle.setOnMousePressed(mouse -> {
            if (!syncManager.isClient()) cycleMode(side);
        });
        return new ButtonWidget<>()
                .size(BUTTON_SIZE)
                .background(new DynamicDrawable(() -> getMode(side).block))
                .hoverBackground(new DynamicDrawable(() -> getMode(side).hoveredBlock))
                .overlay(Text.str(side.getName().substring(0, 1).toUpperCase(Locale.ROOT)).color(Color.WHITE.main))
                .tooltipAutoUpdate(true)
                .tooltipDynamic(t -> t.addLine(Component.translatable(SIDE_KEY + side.getName(),
                        Component.translatable(getMode(side).key())))
                        .addLine(Component.translatable(CHANGE_KEY)))
                .syncHandler(cycle);
    }

    /** What the cube offers on the sides with {@code mode}. */
    private final class SideStorage extends MachineTrait implements IEnergyStorage {

        private final SideMode mode;

        SideStorage(SideMode mode) {
            this.mode = mode;
            setCapabilityValidator(side -> getMode(side) == mode);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || !buffer.canInsert()) return 0;
            if (!simulate) buffer.insert(maxReceive);
            return maxReceive;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return canExtract() ? (int) buffer.extract(maxExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return GTMath.saturatedCast((long) buffer.getStored());
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return mode == SideMode.OUTPUT;
        }

        @Override
        public boolean canReceive() {
            return mode == SideMode.INPUT;
        }
    }
}
