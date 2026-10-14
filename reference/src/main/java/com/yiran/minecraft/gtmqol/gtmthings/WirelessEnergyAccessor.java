package com.yiran.minecraft.gtmqol.gtmthings;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyAccessor extends TieredIOPartMachine implements IInteractedMachine, IMachineLife, IWirelessEnergyContainerHolder, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyAccessor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public static MachineDefinition INSTANCE;

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    @Setter
    @Nullable
    private WirelessEnergyContainer WirelessEnergyContainerCache;

    @Persisted
    public UUID owner_uuid;

    @Persisted
    private long voltage = 0;

    @Persisted
    private int amps = 1;

    @Persisted
    private int setTier = 0;

    @Persisted
    private boolean active = false;

    @Persisted
    private long energyIOPerSec = 0;

    private long lastAverageEnergyIOPerTick = 0;

    @Persisted
    public final NotifiableEnergyContainer energyContainer;

    public WirelessEnergyAccessor(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.OUT);
        this.energyContainer = createEnergyContainer();
    }

    @SuppressWarnings("unused")
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        return new WirelessEnergyOutputContainer(this);
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        return createEnergyContainer(new Object[0]);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::updateEnergyTick);
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    protected void updateEnergyTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.setIOSpeed(energyIOPerSec / 20);
            energyIOPerSec = 0;
        }

        if (!active || voltage <= 0 || amps <= 0 || owner_uuid == null) return;

        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) return;

        BigInteger availableEnergy = container.getStorage();
        if (availableEnergy == null || availableEnergy.compareTo(BigInteger.ZERO) <= 0) return;

        long availableEnergyLong = availableEnergy.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? Long.MAX_VALUE : availableEnergy.longValue();

        long maxPossibleAmps = availableEnergyLong / voltage;
        if (maxPossibleAmps < 1) return;

        long ampsToUse = Math.min(amps, maxPossibleAmps);

        Level level = getLevel();
        if (level == null) return;

        Direction facing = getFrontFacing();
        Direction opposite = facing.getOpposite();
        IEnergyContainer energyContainer = GTCapabilityHelper.getEnergyContainer(level, getPos().relative(facing), opposite);

        if (energyContainer != null && energyContainer.inputsEnergy(opposite) && energyContainer.getEnergyCanBeInserted() > 0) {
            long ampsUsed = energyContainer.acceptEnergyFromNetwork(opposite, voltage, ampsToUse);

            if (ampsUsed > 0) {
                long energyExtracted = ampsUsed * voltage;
                container.removeEnergy(energyExtracted, this);
                energyIOPerSec += energyExtracted;
            }
        }
    }

    public void setIOSpeed(long energyIOPerSec) {
        if (this.lastAverageEnergyIOPerTick != energyIOPerSec) {
            this.lastAverageEnergyIOPerTick = energyIOPerSec;
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (isRemote()) return InteractionResult.PASS;
        ItemStack is = player.getItemInHand(hand);
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            this.owner_uuid = player.getUUID();
            setWirelessEnergyContainerCache(null);
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", GetName(player)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (isRemote()) return false;
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) return false;
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            this.owner_uuid = null;
            setWirelessEnergyContainerCache(null);
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
            return true;
        }
        return false;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.owner_uuid = player.getUUID();
        }
    }

    @Override
    public UUID getUUID() {
        return this.owner_uuid;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(voltage),
                        value -> {
                            voltage = Long.parseLong(value);
                            setTier = GTUtil.getTierByVoltage(voltage);
                        }).setNumbersOnly(0L, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
                .widget(new ButtonWidget(7, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
                        cd -> amps = --amps == -1 ? 0 : amps))
                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(amps),
                        value -> amps = Integer.parseInt(value)).setNumbersOnly(0, Integer.MAX_VALUE))
                .widget(new ButtonWidget(149, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
                        cd -> {
                            if (amps < Integer.MAX_VALUE) {
                                amps++;
                            }
                        }))
                .widget(new LabelWidget(7, 110,
                        () -> "Average Energy I/O per tick: " + this.lastAverageEnergyIOPerTick))
                .widget(new LabelWidget(7, 125, () -> {
                    WirelessEnergyContainer container = getWirelessEnergyContainer();
                    if (container != null && container.getStorage() != null) {
                        return "Network Energy: " + container.getStorage();
                    }
                    return "Network Energy: N/A";
                }))
                .widget(new SwitchWidget(7, 139, 77, 20, (clickData, value) -> active = value)
                        .setTexture(
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                        new TextTexture("gtceu.creative.activity.off")),
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                        new TextTexture("gtceu.creative.activity.on")))
                        .setPressed(active))
                .widget(new SelectorWidget(7, 7, 50, 20, Arrays.stream(GTValues.VNF).toList(), -1)
                        .setOnChanged(tier -> {
                            setTier = ArrayUtils.indexOf(GTValues.VNF, tier);
                            voltage = GTValues.VEX[setTier];
                        })
                        .setSupplier(() -> GTValues.VNF[setTier])
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(ColorPattern.BLACK.rectTexture())
                        .setValue(GTValues.VNF[setTier]));
    }

    private static class WirelessEnergyOutputContainer extends NotifiableEnergyContainer {

        private final WirelessEnergyAccessor machine;

        public WirelessEnergyOutputContainer(WirelessEnergyAccessor machine) {
            super(machine, Long.MAX_VALUE, 0, 0, Long.MAX_VALUE, Integer.MAX_VALUE);
            this.machine = machine;
        }

        @Override
        public long getEnergyStored() {
            WirelessEnergyContainer container = machine.getWirelessEnergyContainer();
            if (container == null) return 0;
            BigInteger storage = container.getStorage();
            if (storage == null) return 0;
            return storage.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? Long.MAX_VALUE : storage.longValue();
        }

        @Override
        public long getEnergyCapacity() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getInputVoltage() {
            return 0;
        }

        @Override
        public long getInputAmperage() {
            return 0;
        }

        @Override
        public long getOutputVoltage() {
            return machine.active ? machine.voltage : 0;
        }

        @Override
        public long getOutputAmperage() {
            return machine.active ? machine.amps : 0;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return false;
        }

        @Override
        public boolean outputsEnergy(Direction side) {
            if (!machine.active) return false;
            return side == machine.getFrontFacing();
        }

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            return 0;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            if (differenceAmount >= 0) return 0;

            long energyToExtract = -differenceAmount;
            if (energyToExtract <= 0) return 0;

            WirelessEnergyContainer container = machine.getWirelessEnergyContainer();
            if (container == null) return 0;

            BigInteger availableEnergy = container.getStorage();
            if (availableEnergy == null || availableEnergy.compareTo(BigInteger.ZERO) <= 0) return 0;

            long availableEnergyLong = availableEnergy.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ?
                    Long.MAX_VALUE : availableEnergy.longValue();

            long actualExtract = Math.min(energyToExtract, availableEnergyLong);
            long extracted = container.removeEnergy(actualExtract, machine);

            if (extracted > 0) {
                machine.energyIOPerSec += extracted;
            }

            return -extracted;
        }
    }
}
