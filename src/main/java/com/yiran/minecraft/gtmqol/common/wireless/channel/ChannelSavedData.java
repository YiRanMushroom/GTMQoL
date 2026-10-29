package com.yiran.minecraft.gtmqol.common.wireless.channel;

import com.yiran.minecraft.gtmqol.common.wireless.WirelessNetworks;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Every channel and what is bound to it. Ids are never reused, so an endpoint still holding a deleted channel's id
 * just finds nothing. The methods taking a player check permissions and return a lang key saying why they refused,
 * or {@code null} when done.
 */
public final class ChannelSavedData extends SavedData {

    private static final String NAME = "gtmqol_channels";

    public static final String NOT_FOUND_KEY = "gtmqol.channel.error.not_found";
    public static final String NOT_ALLOWED_KEY = "gtmqol.channel.error.not_allowed";
    public static final String BAD_NAME_KEY = "gtmqol.channel.error.bad_name";
    public static final String ROUTER_NEEDED_KEY = "gtmqol.channel.error.router_needed";

    private final Int2ObjectLinkedOpenHashMap<Channel> channels = new Int2ObjectLinkedOpenHashMap<>();
    private int nextId = 1;

    private ChannelSavedData() {}

    public static ChannelSavedData get() {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ChannelSavedData::new, ChannelSavedData::load), NAME);
    }

    public @Nullable Channel get(int id) {
        return channels.get(id);
    }

    private static boolean isOwnerOrOp(Player player, Channel channel) {
        return player.getUUID().equals(channel.owner) || player.hasPermissions(2);
    }

    private static boolean sameTeam(UUID a, UUID b) {
        return Objects.equals(WirelessNetworks.networkId(a, false), WirelessNetworks.networkId(b, false));
    }

    public static boolean canSee(Player player, Channel channel) {
        if (isOwnerOrOp(player, channel)) return true;
        return switch (channel.access) {
            case PRIVATE -> false;
            case TEAM -> sameTeam(player.getUUID(), channel.owner);
            case PUBLIC -> true;
        };
    }

    public static boolean canEdit(Player player, Channel channel) {
        return isOwnerOrOp(player, channel);
    }

    /** Channels of this kind the player may see, oldest first. */
    public List<Channel> visible(Player player, ChannelKind kind) {
        List<Channel> list = new ArrayList<>();
        for (Channel channel : channels.values()) {
            if (channel.kind == kind && canSee(player, channel)) list.add(channel);
        }
        return list;
    }

    private static boolean isBadName(String name) {
        return name.isBlank() || name.strip().length() > Channel.MAX_NAME_LENGTH;
    }

    /** Creates a channel owned by the player; a lang key saying why not, or null when created. */
    public @Nullable String create(Player player, ChannelKind kind, String name, int color, ChannelAccess access) {
        if (isBadName(name)) return BAD_NAME_KEY;
        Channel channel = new Channel(nextId++, kind, player.getUUID(), name.strip(), color & 0xFFFFFF, access);
        channels.put(channel.id, channel);
        setDirty();
        return null;
    }

    public @Nullable String edit(Player player, int id, String name, int color, ChannelAccess access) {
        Channel channel = channels.get(id);
        if (channel == null) return NOT_FOUND_KEY;
        if (!canEdit(player, channel)) return NOT_ALLOWED_KEY;
        if (isBadName(name)) return BAD_NAME_KEY;
        channel.name = name.strip();
        channel.color = color & 0xFFFFFF;
        channel.access = access;
        setDirty();
        return null;
    }

    /** Bound endpoints drop the channel ({@link ChannelBindingTrait} finds it gone). */
    public @Nullable String delete(Player player, int id) {
        Channel channel = channels.get(id);
        if (channel == null) return NOT_FOUND_KEY;
        if (!canEdit(player, channel)) return NOT_ALLOWED_KEY;
        channels.remove(id);
        ChannelRegistry.onDeleted(id);
        setDirty();
        return null;
    }

    /**
     * Binds the endpoint at {@code pos}, leaving any channel it was on. Refuses a {@link ChannelKind#routerForMany}
     * channel's second transmitter or receiver while it has no router.
     */
    public @Nullable String bind(Player player, int id, ChannelKind kind, GlobalPos pos, ChannelRole role) {
        Channel channel = channels.get(id);
        if (channel == null || channel.kind != kind) return NOT_FOUND_KEY;
        if (!canSee(player, channel)) return NOT_ALLOWED_KEY;
        if (channel.kind.routerForMany && role != ChannelRole.ROUTER && channel.count(ChannelRole.ROUTER) == 0) {
            for (var e : channel.endpoints.entrySet()) {
                if (e.getValue() == role && !e.getKey().equals(pos)) return ROUTER_NEEDED_KEY;
            }
        }
        unbind(pos);
        channel.endpoints.put(pos, role);
        setDirty();
        return null;
    }

    /** No permission check: for the bound machine itself (removed, or unbound from its UI). */
    public void unbind(GlobalPos pos) {
        for (Channel channel : channels.values()) {
            if (channel.endpoints.remove(pos) != null) setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("next_id", nextId);
        ListTag list = new ListTag();
        for (Channel channel : channels.values()) list.add(channel.save());
        tag.put("channels", list);
        return tag;
    }

    private static ChannelSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ChannelSavedData data = new ChannelSavedData();
        data.nextId = Math.max(1, tag.getInt("next_id"));
        for (Tag t : tag.getList("channels", Tag.TAG_COMPOUND)) {
            Channel channel = Channel.load((CompoundTag) t);
            data.channels.put(channel.id, channel);
        }
        return data;
    }
}
