package com.yiran.minecraft.gtmqol.common.wireless.channel;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

/**
 * The channel a machine is bound to, as one endpoint of {@link #kind} with {@link #role}. The binding itself lives in
 * {@link ChannelSavedData}; this keeps the id and puts the machine into {@link ChannelRegistry} while it is loaded.
 * Machines hold it as a {@code @SaveField @SyncToClient} field.
 */
public class ChannelBindingTrait extends MachineTrait {

    public static final int NONE = 0;

    public final ChannelKind kind;
    public final ChannelRole role;

    @SaveField
    @SyncToClient
    private int channelId = NONE;

    private boolean registered;

    public ChannelBindingTrait(ChannelKind kind, ChannelRole role) {
        this.kind = kind;
        this.role = role;
    }

    public int getChannelId() {
        return channelId;
    }

    public GlobalPos globalPos() {
        return GlobalPos.of(getMachine().getLevel().dimension(), getMachine().getBlockPos());
    }

    /** Server only; null while unbound or after the channel was deleted. */
    public @Nullable Channel getChannel() {
        if (channelId == NONE || isRemote()) return null;
        Channel channel = ChannelSavedData.get().get(channelId);
        return channel != null && channel.endpoints.get(globalPos()) == role ? channel : null;
    }

    /** Server only: bound to a channel on which things flow (see {@link Channel#isActive}). */
    public boolean isActive() {
        Channel channel = getChannel();
        return channel != null && channel.isActive();
    }

    private void setChannelId(int id) {
        if (registered) {
            ChannelRegistry.remove(channelId, globalPos());
            registered = false;
        }
        channelId = id;
        syncDataHolder.markClientSyncFieldDirty("channelId");
        markAsChanged();
        register();
    }

    private void register() {
        if (registered || isRemote() || channelId == NONE) return;
        if (getChannel() == null) {
            // deleted while unloaded
            channelId = NONE;
            syncDataHolder.markClientSyncFieldDirty("channelId");
            return;
        }
        ChannelRegistry.add(channelId, globalPos(), this);
        registered = true;
    }

    /** A lang key saying why not, or null when bound. */
    public @Nullable String bind(Player player, int id) {
        String error = ChannelSavedData.get().bind(player, id, kind, globalPos(), role);
        if (error == null) setChannelId(id);
        return error;
    }

    /** Anyone who can use the machine's UI may unbind it. */
    public void unbind() {
        if (channelId == NONE) return;
        ChannelSavedData.get().unbind(globalPos());
        setChannelId(NONE);
    }

    void onChannelDeleted() {
        registered = false;
        setChannelId(NONE);
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        register();
    }

    @Override
    public void onMachineUnload() {
        super.onMachineUnload();
        if (registered) {
            ChannelRegistry.remove(channelId, globalPos());
            registered = false;
        }
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        if (isRemote()) return;
        if (registered) {
            ChannelRegistry.remove(channelId, globalPos());
            registered = false;
        }
        ChannelSavedData.get().unbind(globalPos());
    }
}
