package com.yiran.minecraft.gtmqol.common.wireless.energy.ae2;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;
import com.yiran.minecraft.gtmqol.common.wireless.WirelessBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyUI;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEBuffer;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEMachines;

import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEBlocks;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.LV;

/**
 * Energy cell whose storage is the bound wireless energy network: AE2 sees its EU as power, through FE. It only gives
 * power: with the highest priority it would otherwise take every other power source's surplus into the wireless network,
 * ahead of AE2's own cells. Only touch this class when AE2 is loaded ({@code GTCEu.Mods.isAE2Loaded()}).
 */
public class WirelessEnergyCellMachine extends MetaMachine
                                       implements IGridConnectedMachine, IAEPowerStorage, IMuiMachine,
                                       IDataStickInteractable {

    private static final String TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_cell";
    private static final String NETWORK_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_cell.network";

    public static MachineDefinition ENERGY_CELL;

    /** Above the dense energy cell's, so the network is used first and AE2's own cells stay as a reserve. */
    private static final int PRIORITY = 3200;
    /** AE2 adds the maximum up when the cell joins, so it can't follow the network. Far above anything it holds. */
    private static final double MAX_POWER = 1e30;
    private static final int RECHECK_INTERVAL = 20;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    @SaveField
    private final GridNodeHolder nodeHolder;

    /** One bit per {@link Direction}, for the client: it doesn't know the grid, and these sides show no overlay. */
    @SyncToClient
    @RerenderOnChanged
    private int connectedSides;

    private final WirelessFEBuffer buffer;
    private boolean online;

    public WirelessEnergyCellMachine(BlockEntityCreationInfo info) {
        super(info);
        this.binding = attachTrait(new WirelessBindingTrait());
        this.buffer = new WirelessFEBuffer(binding::getNetworkId);
        this.nodeHolder = attachTrait(new GridNodeHolder(this));
        // gtceu's hatch defaults: a channel, idle power and one side
        nodeHolder.getMainNode()
                .setFlags()
                .setIdlePowerUsage(0)
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .addService(IAEPowerStorage.class, this);
    }

    public static void init() {
        ENERGY_CELL = GTMQoLAddon.machine("wireless_energy_cell", WirelessEnergyCellMachine::new)
                .tier(LV)
                .rotationState(RotationState.NONE)
                .model(GTMachineModels.createBasicMachineModel(GTMQoL.id("block/wireless_energy_cell"))
                        .andThen(b -> b.addDynamicRenderer(EnergyCellSidesRender::new)))
                .langValue("Wireless Energy Cell")
                .tooltips(Component.translatable(TOOLTIP_KEY), Component.translatable(NETWORK_TOOLTIP_KEY))
                .register();
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY, "Gives its ME network power from a wireless energy network");
        GTMQoLAddon.registrate().addRawLang(NETWORK_TOOLTIP_KEY,
                "Only gives power: other power sources on the ME network can't fill the wireless energy network");
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("wireless_energy_cell"))
                .inputItems(AEBlocks.DENSE_ENERGY_CELL.asItem(), 8)
                .inputItems(WirelessFEMachines.ENERGY_CUBE)
                .outputItems(ENERGY_CELL)
                .duration(400)
                .save(provider);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::recheck);
    }

    /**
     * AE2 drops a storage that fell short, until it reports it can give power again. A cell does that when its amount
     * changes; this one's changes without it, whenever the wireless network does.
     */
    private void recheck() {
        if (getOffsetTimer() % RECHECK_INTERVAL != 0) return;
        updateConnectedSides();
        getMainNode().ifPresent(grid -> {
            grid.postEvent(new GridPowerStorageStateChanged(this, PowerEventType.PROVIDE_POWER));
        });
    }

    private void updateConnectedSides() {
        int sides = 0;
        IGridNode node = getMainNode().getNode();
        if (node != null) {
            for (Direction side : node.getInWorldConnections().keySet()) sides |= 1 << side.ordinal();
        }
        if (sides == connectedSides) return;
        connectedSides = sides;
        syncDataHolder.markClientSyncFieldDirty("connectedSides");
    }

    public boolean isConnected(Direction side) {
        return (connectedSides >> side.ordinal() & 1) != 0;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier) {
        double fe = PowerUnits.AE.convertTo(PowerUnits.FE, multiplier.multiply(amt));
        double taken = buffer.extract(fe, mode == Actionable.SIMULATE);
        // back through the conversion it may come out a bit less, and AE2 would take that for running out
        return taken >= fe ? amt : multiplier.divide(PowerUnits.FE.convertTo(PowerUnits.AE, taken));
    }

    /** Takes nothing (see the class comment); AE2 doesn't offer it any with {@link #getPowerFlow()} READ. */
    // Untested: an energy acceptor's power on the same ME network goes into AE2's energy cells, not the wireless network
    @Override
    public double injectAEPower(double amt, Actionable mode) {
        return amt;
    }

    @Override
    public double getAEMaxPower() {
        return MAX_POWER;
    }

    /**
     * AE2 adds up what its storages report, so of the cells on one wireless network in a grid only one reports it. All
     * of them still give power.
     */
    private boolean reportsNetwork() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return true;
        NetworkId network = binding.getNetworkId();
        for (WirelessEnergyCellMachine cell : grid.getMachines(WirelessEnergyCellMachine.class)) {
            if (Objects.equals(cell.binding.getNetworkId(), network)) return cell == this;
        }
        return true;
    }

    @Override
    public double getAECurrentPower() {
        if (!reportsNetwork()) return 0;
        return PowerUnits.FE.convertTo(PowerUnits.AE, buffer.getStored());
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
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
        mainWidget.child(WirelessEnergyUI.create(syncManager, binding, guiData.getPlayer()));
    }
}
