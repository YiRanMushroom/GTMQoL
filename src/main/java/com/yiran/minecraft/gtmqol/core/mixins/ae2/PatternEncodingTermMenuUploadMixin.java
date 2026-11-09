package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload.Outcome;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUploadMessage;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUploadToast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A pattern encoded with Shift goes right to an Assembling Matrix, and a toast tells the player what became of it; a
 * pattern the matrix already has, or with no room, stays in the terminal. The client says whether Shift was held just
 * before AE2's encode action reaches the server. Before anything ExtendedAE Plus does on the same method, which then
 * finds the slot empty: a matrix on the network comes first.
 */
@Mixin(value = PatternEncodingTermMenu.class, remap = false, priority = 500)
public abstract class PatternEncodingTermMenuUploadMixin {

    @Shadow
    @Final
    private RestrictedInputSlot encodedPatternSlot;

    @Inject(method = "encode", at = @At("HEAD"))
    private void gtmqol$sayIfUpload(CallbackInfo ci) {
        var menu = (PatternEncodingTermMenu) (Object) this;
        if (menu.getPlayer().level().isClientSide()) AssemblingMatrixUploadToast.requestUpload();
    }

    @Inject(method = "encode", at = @At("TAIL"))
    private void gtmqol$uploadToMatrix(CallbackInfo ci) {
        var menu = (PatternEncodingTermMenu) (Object) this;
        if (!(menu.getPlayer() instanceof ServerPlayer player) || !AssemblingMatrixUpload.takeRequest(player)) return;
        var node = menu.getGridNode();
        ItemStack pattern = encodedPatternSlot.getItem();
        if (node == null || pattern.isEmpty()) return;
        Outcome outcome = AssemblingMatrixUpload.upload(node.getGrid(), pattern);
        if (outcome == Outcome.NOT_OURS) return;
        if (outcome == Outcome.TAKEN) encodedPatternSlot.set(ItemStack.EMPTY);
        AssemblingMatrixUploadMessage.send(player, pattern, outcome);
    }
}
