package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;

/**
 * Tells the player who encoded a pattern that it went into an Assembling Matrix, or that a matrix already had it. The
 * encoding terminal's window covers the action bar and the pattern leaves its slot at once, so nothing else shows it.
 */
public record AssemblingMatrixUploadMessage(ItemStack icon, boolean alreadyThere) implements CustomPacketPayload {

    public static final Type<AssemblingMatrixUploadMessage> TYPE = new Type<>(GTMQoL.id("assembling_matrix_upload"));
    private static final StreamCodec<RegistryFriendlyByteBuf, AssemblingMatrixUploadMessage> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, AssemblingMatrixUploadMessage::icon,
            ByteBufCodecs.BOOL, AssemblingMatrixUploadMessage::alreadyThere,
            AssemblingMatrixUploadMessage::new);

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToClient(TYPE, CODEC, (message, context) ->
                        AssemblingMatrixUploadToast.show(message.icon, message.alreadyThere)));
    }

    /** With the pattern's product as the icon, or the pattern itself when it can't be decoded. */
    public static void send(ServerPlayer player, ItemStack pattern, boolean alreadyThere) {
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, player.level());
        ItemStack icon = details != null && details.getPrimaryOutput().what() instanceof AEItemKey item ?
                item.toStack() : pattern.copy();
        PacketDistributor.sendToPlayer(player, new AssemblingMatrixUploadMessage(icon, alreadyThere));
    }

    @Override
    public Type<AssemblingMatrixUploadMessage> type() {
        return TYPE;
    }
}
