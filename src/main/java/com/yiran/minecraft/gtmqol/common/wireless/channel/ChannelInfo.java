package com.yiran.minecraft.gtmqol.common.wireless.channel;

import com.yiran.minecraft.gtmqol.common.wireless.WirelessNetworks;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

/** What a client sees of one channel in the list, with the viewer's permission already worked out. */
public record ChannelInfo(int id, String name, int color, String owner, ChannelAccess access, boolean canEdit,
                          int transmitters, int receivers, int routers, boolean active) {

    public static final StreamCodec<FriendlyByteBuf, ChannelInfo> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeVarInt(info.id);
                buf.writeUtf(info.name);
                buf.writeInt(info.color);
                buf.writeUtf(info.owner);
                buf.writeVarInt(info.access.ordinal());
                buf.writeBoolean(info.canEdit);
                buf.writeVarInt(info.transmitters);
                buf.writeVarInt(info.receivers);
                buf.writeVarInt(info.routers);
                buf.writeBoolean(info.active);
            },
            buf -> new ChannelInfo(buf.readVarInt(), buf.readUtf(), buf.readInt(), buf.readUtf(),
                    ChannelAccess.byId(buf.readVarInt()), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readBoolean()));

    public static ChannelInfo of(Channel channel, Player viewer) {
        return new ChannelInfo(channel.id, channel.name, channel.color, WirelessNetworks.playerName(channel.owner),
                channel.access, ChannelSavedData.canEdit(viewer, channel), channel.count(ChannelRole.TRANSMITTER),
                channel.count(ChannelRole.RECEIVER), channel.count(ChannelRole.ROUTER), channel.isActive());
    }
}
