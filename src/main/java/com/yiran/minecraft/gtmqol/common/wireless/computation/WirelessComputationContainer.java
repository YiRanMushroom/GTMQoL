package com.yiran.minecraft.gtmqol.common.wireless.computation;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelBindingTrait;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * gtceu's computation container for our hatches. A transmitter works as gtceu's (asks its controller); a receiver
 * asks its channel ({@link ChannelComputation}) where gtceu's would look for an optical cable next to it.
 */
public class WirelessComputationContainer extends NotifiableComputationContainer {

    private final ChannelBindingTrait binding;

    public WirelessComputationContainer(ChannelBindingTrait binding, boolean transmitter) {
        super(IO.IN, transmitter);
        this.binding = binding;
    }

    private @Nullable IOpticalComputationProvider channel() {
        int id = binding.getChannelId();
        return isTransmitter() || id == ChannelBindingTrait.NONE || binding.isRemote() ? null :
                ChannelComputation.node(id);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, Collection<IOpticalComputationProvider> seen) {
        if (isTransmitter()) return super.requestCWUt(cwut, simulate, seen);
        seen.add(this);
        IOpticalComputationProvider channel = channel();
        return channel == null ? 0 : channel.requestCWUt(cwut, simulate, seen);
    }

    @Override
    public int getMaxCWUt(Collection<IOpticalComputationProvider> seen) {
        if (isTransmitter()) return super.getMaxCWUt(seen);
        seen.add(this);
        IOpticalComputationProvider channel = channel();
        return channel == null ? 0 : channel.getMaxCWUt(seen);
    }

    @Override
    public boolean canBridge(Collection<IOpticalComputationProvider> seen) {
        if (isTransmitter()) return super.canBridge(seen);
        seen.add(this);
        return true;
    }

    @Override
    public @Nullable IOpticalComputationProvider getComputationProvider() {
        return isTransmitter() ? super.getComputationProvider() : channel();
    }

    /** gtceu's, with the channel in place of the optical cable. */
    @Override
    public List<Integer> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<Integer> left, boolean simulate) {
        if (isTransmitter() || io != IO.IN) return super.handleRecipeInner(io, recipe, left, simulate);
        IOpticalComputationProvider provider = channel();
        if (provider == null) return left;
        int sum = left.stream().mapToInt(Integer::intValue).sum();
        int availableCWUt = requestCWUt(Integer.MAX_VALUE, true);
        if (availableCWUt >= sum) {
            if (recipe != null && recipe.data.getBoolean("duration_is_total_cwu")) {
                int drawn = provider.requestCWUt(availableCWUt, simulate);
                if (!simulate) {
                    // remove the progress the recipe logic adds
                    if (getMachine() instanceof IRecipeLogicMachine rlm) {
                        rlm.getRecipeLogic().setProgressDelta(drawn - 1);
                    } else if (getMachine() instanceof MultiblockPartMachine part) {
                        for (MultiblockControllerMachine controller : part.getControllers()) {
                            if (controller instanceof IRecipeLogicMachine rlm) {
                                rlm.getRecipeLogic().setProgressDelta(drawn - 1);
                            }
                        }
                    }
                }
                sum -= drawn;
            } else {
                sum -= provider.requestCWUt(sum, simulate);
            }
        }
        return sum <= 0 ? Collections.emptyList() : Collections.singletonList(sum);
    }
}
