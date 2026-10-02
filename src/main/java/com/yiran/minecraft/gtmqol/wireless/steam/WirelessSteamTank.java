package com.yiran.minecraft.gtmqol.wireless.steam;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.yiran.minecraft.gtmqol.wireless.NetworkId;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A tank with no contents of its own: every read and write goes to the network's steam pool.
 * Wrapped in a regular {@code NotifiableFluidTank}, so GTCEu's recipe, pipe and steam multiblock code
 * (which checks for {@code NotifiableFluidTank}) work unchanged.
 *
 * <p>Forge tanks are int-sized, so amounts above {@link Integer#MAX_VALUE} are reported as that.
 * Changes made through other machines on the network do not notify this machine; recipe logic short on
 * steam keeps polling on its own.</p>
 */
public class WirelessSteamTank extends CustomFluidTank {

    private final Supplier<@Nullable NetworkId> network;

    public WirelessSteamTank(Supplier<@Nullable NetworkId> network) {
        super(Integer.MAX_VALUE, stack -> stack.getFluid().is(GTMaterials.Steam.getFluidTag()));
        this.network = network;
    }

    @Override
    public @NotNull FluidStack getFluid() {
        int amount = getFluidAmount();
        return amount > 0 ? GTMaterials.Steam.getFluid(amount) : FluidStack.EMPTY;
    }

    @Override
    public int getFluidAmount() {
        NetworkId id = network.get();
        return id == null ? 0 : WirelessSteamSavedData.get().getStoredInt(id);
    }

    @Override
    public boolean isEmpty() {
        return getFluidAmount() <= 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        NetworkId id = network.get();
        if (id == null || resource.isEmpty() || !isFluidValid(resource)) return 0;
        if (action.execute()) {
            WirelessSteamSavedData.get().insert(id, resource.getAmount());
            onContentsChanged();
        }
        return resource.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        NetworkId id = network.get();
        if (id == null) return FluidStack.EMPTY;
        int drained = WirelessSteamSavedData.get().extract(id, maxDrain, action.simulate());
        if (drained <= 0) return FluidStack.EMPTY;
        if (action.execute()) onContentsChanged();
        return GTMaterials.Steam.getFluid(drained);
    }

    @Override
    public void setFluid(FluidStack stack) {
        // Contents live in the network.
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {}
}
