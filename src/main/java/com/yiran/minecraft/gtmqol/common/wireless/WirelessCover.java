package com.yiran.minecraft.gtmqol.common.wireless;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;

import org.jetbrains.annotations.Nullable;

/**
 * A cover moving something between the machine it is on and the network of whoever attached it, every tick:
 * {@link IO#IN} into the machine, {@link IO#OUT} out of it.
 */
public abstract class WirelessCover extends CoverBehavior implements IMuiCover {

    protected final IO io;

    @SaveField
    @SyncToClient
    public final WirelessBindingTrait binding;

    private @Nullable TickableSubscription subscription;

    protected WirelessCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, IO io) {
        super(definition, coverHolder, attachedSide);
        this.io = io;
        this.binding = new WirelessBindingTrait(this, coverHolder::isRemote);
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null && !(player instanceof FakePlayer)) {
            onAttachedBy(player);
        }
    }

    /** By a real player: binds the cover to them. */
    protected void onAttachedBy(ServerPlayer player) {
        binding.bind(player.getUUID(), false);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, () -> {
            NetworkId network = binding.getNetworkId();
            if (network != null) update(network);
        });
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /** Every server tick while bound. */
    protected abstract void update(NetworkId network);
}
