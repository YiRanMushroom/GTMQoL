package com.yiran.minecraft.gtmqol.common.wireless.channel;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.UUID;

/**
 * One channel as saved in {@link ChannelSavedData}: its settings and every endpoint bound to it, loaded or not
 * (so the one-to-one rule of {@link ChannelKind#routerForMany} also counts machines in unloaded chunks).
 * Server only; clients see {@link ChannelInfo}.
 */
public final class Channel {

    public static final int MAX_NAME_LENGTH = 24;

    public final int id;
    public final ChannelKind kind;
    public final UUID owner;
    String name;
    int color;
    ChannelAccess access;
    final Map<GlobalPos, ChannelRole> endpoints = new Object2ObjectOpenHashMap<>();

    Channel(int id, ChannelKind kind, UUID owner, String name, int color, ChannelAccess access) {
        this.id = id;
        this.kind = kind;
        this.owner = owner;
        this.name = name;
        this.color = color;
        this.access = access;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public ChannelAccess getAccess() {
        return access;
    }

    public int count(ChannelRole role) {
        int n = 0;
        for (ChannelRole r : endpoints.values()) {
            if (r == role) n++;
        }
        return n;
    }

    /**
     * Whether anything flows: always, unless the kind needs a router for more than one transmitter and receiver
     * and has more without one.
     */
    public boolean isActive() {
        if (!kind.routerForMany || count(ChannelRole.ROUTER) > 0) return true;
        return count(ChannelRole.TRANSMITTER) <= 1 && count(ChannelRole.RECEIVER) <= 1;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putString("kind", kind.name());
        tag.putUUID("owner", owner);
        tag.putString("name", name);
        tag.putInt("color", color);
        tag.putString("access", access.name());
        ListTag list = new ListTag();
        endpoints.forEach((pos, role) -> {
            CompoundTag e = new CompoundTag();
            e.put("pos", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow());
            e.putString("role", role.name());
            list.add(e);
        });
        tag.put("endpoints", list);
        return tag;
    }

    static Channel load(CompoundTag tag) {
        Channel channel = new Channel(tag.getInt("id"), ChannelKind.valueOf(tag.getString("kind")),
                tag.getUUID("owner"), tag.getString("name"), tag.getInt("color"),
                ChannelAccess.valueOf(tag.getString("access")));
        for (Tag t : tag.getList("endpoints", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            GlobalPos.CODEC.parse(NbtOps.INSTANCE, e.get("pos")).result().ifPresent(pos ->
                    channel.endpoints.put(pos, ChannelRole.valueOf(e.getString("role"))));
        }
        return channel;
    }
}
