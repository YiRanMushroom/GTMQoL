package com.yiran.minecraft.gtmqol.common.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.yiran.minecraft.gtmqol.common.wireless.NetworkId;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Energy container of a wireless energy hatch. Holds no energy itself; multiblocks (including the power
 * substation and active transformer) draw from or feed the network through it. It has no sides, so cables
 * can't connect.
 *
 * <p>Instead of the network's total, it reports what is left of this tick's budget of voltage × amperage,
 * like a hatch that can pass that much per tick. GTCEu sums stored and capacity over all hatches of a
 * multiblock, so reporting the network total would also count it once per hatch.</p>
 *
 * <p>{@link IO#IN}: every EU taken costs {@code costMultiplier} EU from the network (overclock mode).
 * {@link IO#OUT}: everything put in goes to the network.</p>
 *
 * <p>Voltage, amperage and cost are read live, but multiblocks only read voltage and amperage when they
 * form.</p>
 */
public class WirelessEnergyContainer extends NotifiableEnergyContainer {

    private final IO io;
    private final Supplier<@Nullable NetworkId> network;
    private final IntSupplier voltageTier;
    private final IntSupplier amperage;
    private final IntSupplier costMultiplier;

    private long budgetTick = Long.MIN_VALUE;
    private long usedThisTick;

    public WirelessEnergyContainer(IO io, Supplier<@Nullable NetworkId> network, IntSupplier voltageTier,
                                   IntSupplier amperage, IntSupplier costMultiplier) {
        // Non-zero placeholders so the base class picks the right handler IO; the getters below take over.
        super(0, io == IO.IN ? 1 : 0, io == IO.IN ? 1 : 0, io == IO.OUT ? 1 : 0, io == IO.OUT ? 1 : 0);
        this.io = io;
        this.network = network;
        this.voltageTier = voltageTier;
        this.amperage = amperage;
        this.costMultiplier = costMultiplier;
        setSideInputCondition(side -> false);
        setSideOutputCondition(side -> false);
        setCapabilityValidator(side -> side == null);
    }

    private long voltage() {
        return GTValues.V[voltageTier.getAsInt()];
    }

    @Override
    public long getInputVoltage() {
        return io == IO.IN ? voltage() : 0;
    }

    @Override
    public long getInputAmperage() {
        return io == IO.IN ? amperage.getAsInt() : 0;
    }

    @Override
    public long getOutputVoltage() {
        return io == IO.OUT ? voltage() : 0;
    }

    @Override
    public long getOutputAmperage() {
        return io == IO.OUT ? amperage.getAsInt() : 0;
    }

    /**
     * At most V[MAX] × MAX_AMPERAGE = 2^31 × 2^24, so it fits a long.
     */
    private long budget() {
        return voltage() * amperage.getAsInt();
    }

    private long remainingBudget() {
        long now = getMachine().getOffsetTimer();
        if (now != budgetTick) {
            budgetTick = now;
            usedThisTick = 0;
        }
        return Math.max(0, budget() - usedThisTick);
    }

    @Override
    public long getEnergyCapacity() {
        return budget();
    }

    @Override
    public long getEnergyStored() {
        NetworkId id = network.get();
        if (id == null) return 0;
        long remaining = remainingBudget();
        if (io == IO.OUT) return budget() - remaining;
        return Math.min(remaining, WirelessEnergySavedData.get().getAffordable(id, costMultiplier.getAsInt()));
    }

    /**
     * The base class returns its own (always 0) field, and recipe handling skips input handlers with 0.
     */
    @Override
    public double getTotalContentAmount() {
        return getEnergyStored();
    }

    /**
     * Same total EU as the base class, without {@code EnergyContainerList.calculateVoltageAmperage}: its
     * prime factor check is linear in the amperage, which is up to 2^24 here (worst for powers of two), and this
     * is called on every parallel calculation.
     */
    @Override
    public List<Object> getContents() {
        return List.of(new EnergyStack(getEnergyStored()));
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        NetworkId id = network.get();
        if (id == null) return 0;
        if (io == IO.IN && energyToAdd < 0) {
            long taken = WirelessEnergySavedData.get().extract(id, Math.min(-energyToAdd, remainingBudget()),
                    costMultiplier.getAsInt());
            usedThisTick += taken;
            return -taken;
        }
        if (io == IO.OUT && energyToAdd > 0) {
            long added = Math.min(energyToAdd, remainingBudget());
            WirelessEnergySavedData.get().insert(id, added);
            usedThisTick += added;
            return added;
        }
        return 0;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        return 0;
    }

    @Override
    public void checkOutputSubscription() {
        // Never emits to neighbours.
    }
}
