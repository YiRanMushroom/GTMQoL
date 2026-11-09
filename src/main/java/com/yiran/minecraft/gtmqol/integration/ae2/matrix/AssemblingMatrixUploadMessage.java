package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload.Outcome;

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
 * Tells the player who encoded a pattern what became of the upload: it went into an Assembling Matrix, a matrix already
 * has it, or none had room. The encoding terminal's window covers the action bar, so nothing else would show it.
 * {@link Request} goes the other way: whether the encode click is an upload (Shift held).
 */
public record AssemblingMatrixUploadMessage(ItemStack icon, Outcome outcome) implements CustomPacketPayload {

    public static final Type<AssemblingMatrixUploadMessage> TYPE = new Type<>(GTMQoL.id("assembling_matrix_upload"));
    private static final StreamCodec<RegistryFriendlyByteBuf, AssemblingMatrixUploadMessage> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, AssemblingMatrixUploadMessage::icon,
            ByteBufCodecs.idMapper(i -> Outcome.values()[i], Outcome::ordinal), AssemblingMatrixUploadMessage::outcome,
            AssemblingMatrixUploadMessage::new);

    /** Sent with every click on the encode button, just before AE2's own encode action. */
    public record Request(boolean upload) implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(GTMQoL.id("assembling_matrix_upload_request"));
        private static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Request::upload, Request::new);

        @Override
        public Type<Request> type() {
            return TYPE;
        }
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToClient(TYPE, CODEC, (message, context) ->
                        AssemblingMatrixUploadToast.show(message.icon, message.outcome))
                .playToServer(Request.TYPE, Request.CODEC, (request, context) ->
                        AssemblingMatrixUpload.request(context.player(), request.upload)));
    }

    /** With the pattern's product as the icon, or the pattern itself when it can't be decoded. */
    public static void send(ServerPlayer player, ItemStack pattern, Outcome outcome) {
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, player.level());
        ItemStack icon = details != null && details.getPrimaryOutput().what() instanceof AEItemKey item ?
                item.toStack() : pattern.copy();
        PacketDistributor.sendToPlayer(player, new AssemblingMatrixUploadMessage(icon, outcome));
    }

    /** From the client: the encode click being made is an upload or not. */
    public static void sendRequest(boolean upload) {
        PacketDistributor.sendToServer(new Request(upload));
    }

    @Override
    public Type<AssemblingMatrixUploadMessage> type() {
        return TYPE;
    }
}
