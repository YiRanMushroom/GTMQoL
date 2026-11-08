package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.yiran.minecraft.gtmqol.GTMQoL;

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
 * Tells the player who encoded a pattern that it went into an Assembling Matrix, or that a matrix already had it. The
 * encoding terminal's window covers the action bar and the pattern leaves its slot at once, so nothing else shows it.
 */
public record AssemblingMatrixUploadMessage(ItemStack icon, boolean alreadyThere) {

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(GTMQoL.id("assembling_matrix"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    /** Call from the mod constructor. */
    public static void register() {
        CHANNEL.messageBuilder(AssemblingMatrixUploadMessage.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AssemblingMatrixUploadMessage::encode)
                .decoder(AssemblingMatrixUploadMessage::decode)
                .consumerMainThread((message, context) ->
                        AssemblingMatrixUploadToast.show(message.icon, message.alreadyThere))
                .add();
    }

    /** With the pattern's product as the icon, or the pattern itself when it can't be decoded. */
    public static void send(ServerPlayer player, ItemStack pattern, boolean alreadyThere) {
        IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, player.level());
        ItemStack icon = details != null && details.getPrimaryOutput().what() instanceof AEItemKey item ?
                item.toStack() : pattern.copy();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AssemblingMatrixUploadMessage(icon, alreadyThere));
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeItem(icon);
        buf.writeBoolean(alreadyThere);
    }

    private static AssemblingMatrixUploadMessage decode(FriendlyByteBuf buf) {
        return new AssemblingMatrixUploadMessage(buf.readItem(), buf.readBoolean());
    }
}
