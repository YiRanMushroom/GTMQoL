package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUploadMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A pattern encoded for an Assembling Matrix goes right to it, and a toast tells the player. Before anything ExtendedAE
 * Plus does on the same method, which then finds the slot empty: a matrix on the network comes first.
 */
@Mixin(value = PatternEncodingTermMenu.class, remap = false, priority = 500)
public abstract class PatternEncodingTermMenuUploadMixin {

    @Shadow
    @Final
    private RestrictedInputSlot encodedPatternSlot;
    @Shadow
    @Final
    private RestrictedInputSlot blankPatternSlot;

    @Inject(method = "encode", at = @At("TAIL"))
    private void gtmqol$uploadToMatrix(CallbackInfo ci) {
        var menu = (PatternEncodingTermMenu) (Object) this;
        var node = menu.getNetworkNode();
        ItemStack pattern = encodedPatternSlot.getItem();
        if (node == null || pattern.isEmpty() || !(menu.getPlayer() instanceof ServerPlayer player)) return;
        switch (AssemblingMatrixUpload.upload(node.getGrid(), pattern)) {
            case NOT_TAKEN -> {}
            case TAKEN -> {
                encodedPatternSlot.set(ItemStack.EMPTY);
                AssemblingMatrixUploadMessage.send(player, pattern, false);
            }
            case ALREADY_THERE -> {
                encodedPatternSlot.set(ItemStack.EMPTY);
                ItemStack blank = blankPatternSlot.safeInsert(AEItems.BLANK_PATTERN.stack());
                if (!blank.isEmpty()) player.getInventory().placeItemBackInInventory(blank);
                AssemblingMatrixUploadMessage.send(player, pattern, true);
            }
        }
    }
}
