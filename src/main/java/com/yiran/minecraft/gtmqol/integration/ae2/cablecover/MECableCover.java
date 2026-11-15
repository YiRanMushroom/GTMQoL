package com.yiran.minecraft.gtmqol.integration.ae2.cablecover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;

import net.minecraft.core.Direction;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.me.InWorldGridNode;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Makes the machine it is on part of the ME network's cabling, on all six sides. A machine without an ME connection
 * carries channels through like a dense cable (a node of this cover, found by AE2 through
 * {@link MECableCovers}); one with its own connection (ME hatches, pattern buffers) gets that connection on all sides
 * instead of its front only.
 */
public class MECableCover extends CoverBehavior implements IInWorldGridNodeHost {

    private static final Set<Direction> ALL_SIDES = EnumSet.allOf(Direction.class);
    /** Ticks between looks at a machine's own node, which the machine sets back to its front when it is turned. */
    private static final int CHECK_INTERVAL = 10;

    private @Nullable IManagedGridNode node;
    /** The sides a machine's own node had before this cover opened all of them, for when the cover comes off. */
    private @Nullable Set<Direction> machineSides;
    private @Nullable TickableSubscription subscription;

    public MECableCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    private @Nullable MetaMachine machine() {
        return MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getBlockPos());
    }

    @Override
    public boolean canAttach() {
        if (!super.canAttach()) return false;
        MetaMachine machine = machine();
        if (machine == null) return false;
        // one is enough, a second would be a second node in the same block
        for (CoverBehavior cover : coverHolder.getCovers()) {
            if (cover instanceof MECableCover) return false;
        }
        // a machine that is cabling already (GTCEu's hulls) has nothing to gain
        return machine instanceof IGridConnectedMachine ||
                GridHelper.getNodeHost(coverHolder.getLevel(), coverHolder.getBlockPos()) == null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // not right away: the neighbours, and the machine's own node, are only there from the next tick on
        if (!coverHolder.isRemote()) subscription = coverHolder.subscribeServerTick(subscription, this::tick);
    }

    // Untested: on a machine without an ME connection, cables on any sides connect through it and channels pass (32
    // by default); on an ME hatch or pattern buffer, cables connect on all sides, still after turning it with a
    // wrench, and only on the front again once the cover is removed; survives a reload
    private void tick() {
        MetaMachine machine = machine();
        if (machine == null) return;
        if (machine instanceof IGridConnectedMachine connected) {
            if (coverHolder.getOffsetTimer() % CHECK_INTERVAL != 0) return;
            if (!(connected.getMainNode().getNode() instanceof InWorldGridNode own)) return;
            Set<Direction> sides = EnumSet.noneOf(Direction.class);
            for (Direction side : ALL_SIDES) {
                if (own.isExposedOnSide(side)) sides.add(side);
            }
            if (sides.size() == ALL_SIDES.size()) return;
            machineSides = sides;
            connected.getMainNode().setExposedOnSides(ALL_SIDES);
        } else if (node == null) {
            node = GridHelper.createManagedNode(this, Listener.INSTANCE)
                    .setFlags(GridFlags.DENSE_CAPACITY)
                    .setInWorldNode(true)
                    .setExposedOnSides(ALL_SIDES)
                    .setIdlePowerUsage(0)
                    .setVisualRepresentation(MECableCovers.ITEM.asStack());
            node.create(coverHolder.getLevel(), coverHolder.getBlockPos());
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        stop();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        stop();
        if (machineSides != null && machine() instanceof IGridConnectedMachine connected) {
            connected.getMainNode().setExposedOnSides(machineSides);
        }
        machineSides = null;
    }

    private void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        if (node != null) {
            node.destroy();
            node = null;
        }
    }

    /** Whether AE2 finds this cover's own node in the machine's block. */
    boolean hasNode() {
        return node != null;
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) {
        return node == null ? null : node.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    /** The node holds nothing worth saving, and is made again on load. */
    private static final class Listener implements IGridNodeListener<MECableCover> {

        static final Listener INSTANCE = new Listener();

        @Override
        public void onSaveChanges(MECableCover nodeOwner, IGridNode node) {}
    }
}
