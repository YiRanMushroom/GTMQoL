package com.yiran.minecraft.gtmqol.common.wireless.computation;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.yiran.minecraft.gtmqol.common.wireless.channel.Channel;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelBindingTrait;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelRegistry;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelRole;
import com.yiran.minecraft.gtmqol.common.wireless.channel.ChannelSavedData;

import net.neoforged.neoforge.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A computation channel seen from its receivers: one provider per channel that asks every loaded transmitter in
 * turn, like gtceu's network switch, without its bridge requirement. Being one object per channel it also goes into
 * {@code seen}, so a request never comes back through the same channel. Server only.
 */
public final class ChannelComputation {

    private static final Int2ObjectOpenHashMap<Node> NODES = new Int2ObjectOpenHashMap<>();

    private ChannelComputation() {}

    static Node node(int channel) {
        return NODES.computeIfAbsent(channel, Node::new);
    }

    public static void clear() {
        NODES.clear();
    }

    static final class Node implements IOpticalComputationProvider {

        private final int channel;
        // like the network switch: once nothing was left this tick, don't ask everyone again
        private long tick = -1;
        private boolean saturated;

        private Node(int channel) {
            this.channel = channel;
        }

        private boolean flows() {
            Channel c = ChannelSavedData.get().get(channel);
            return c != null && c.isActive();
        }

        @Override
        public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            seen.add(this);
            if (cwut <= 0 || !flows()) return 0;
            long now = ServerLifecycleHooks.getCurrentServer().getTickCount();
            if (tick != now) {
                tick = now;
                saturated = false;
            } else if (saturated) {
                return 0;
            }
            int allocated = 0;
            for (ChannelBindingTrait transmitter : ChannelRegistry.loaded(channel, ChannelRole.TRANSMITTER)) {
                if (!(transmitter.getMachine() instanceof WirelessComputationHatchPartMachine hatch)) continue;
                int got = hatch.container.requestCWUt(cwut - allocated, simulate, seen);
                allocated += got;
                if (allocated >= cwut) break;
            }
            if (!simulate && allocated == 0) saturated = true;
            return allocated;
        }

        @Override
        public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            seen.add(this);
            if (!flows()) return 0;
            int max = 0;
            for (ChannelBindingTrait transmitter : ChannelRegistry.loaded(channel, ChannelRole.TRANSMITTER)) {
                if (transmitter.getMachine() instanceof WirelessComputationHatchPartMachine hatch) {
                    max += hatch.container.getMaxCWUt(seen);
                }
            }
            return max;
        }

        // no bridge components needed (user's choice)
        @Override
        public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
            seen.add(this);
            return true;
        }
    }
}
