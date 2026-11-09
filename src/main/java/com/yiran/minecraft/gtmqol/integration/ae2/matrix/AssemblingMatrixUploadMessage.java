package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload.Outcome;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;

/**
 * Tells the player who encoded a pattern what became of the upload: it went into an Assembling Matrix, a matrix already
 * has it, or none had room. The encoding terminal's window covers the action bar, so nothing else would show it.
 * {@link Request} goes the other way: whether the encode click is an upload (Shift held).
 */
public record AssemblingMatrixUploadMessage(ItemStack icon, Outcome outcome) {

    private static final String VERSION = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(GTMQoL.id("assembling_matrix"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    /** Sent with every click on the encode button, just before AE2's own encode action. */
    public record Request(boolean upload) {

        private void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(upload);
        }

        private static Request decode(FriendlyByteBuf buf) {
            return new Request(buf.readBoolean());
        }
    }

    /** Call from the mod constructor. */
    public static void register() {
        CHANNEL.messageBuilder(AssemblingMatrixUploadMessage.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AssemblingMatrixUploadMessage::encode)
                .decoder(AssemblingMatrixUploadMessage::decode)
                .consumerMainThread((message, context) ->
                        AssemblingMatrixUploadToast.show(message.icon, message.outcome))
                .add();
        CHANNEL.messageBuilder(Request.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Request::encode)
                .decoder(Request::decode)
                .consumerMainThread((request, context) -> {
                    var player = context.get().getSender();
                    if (player != null) AssemblingMatrixUpload.request(player, request.upload);
                })
                .add();
    }

    /** With the pattern's product as the icon, or the pattern itself when it can't be decoded. */
    public static void send(ServerPlayer player, ItemStack pattern, Outcome outcome) {
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, player.level());
        ItemStack icon = details != null && details.getPrimaryOutput().what() instanceof AEItemKey item ?
                item.toStack() : pattern.copy();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AssemblingMatrixUploadMessage(icon, outcome));
    }

    /** From the client: the encode click being made is an upload or not. */
    public static void sendRequest(boolean upload) {
        CHANNEL.sendToServer(new Request(upload));
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeItem(icon);
        buf.writeEnum(outcome);
    }

    private static AssemblingMatrixUploadMessage decode(FriendlyByteBuf buf) {
        return new AssemblingMatrixUploadMessage(buf.readItem(), buf.readEnum(Outcome.class));
    }
}
