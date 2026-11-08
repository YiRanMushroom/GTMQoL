package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixUpload;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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
 * A pattern encoded for a assembling matrix goes right to it. Before anything ExtendedAE Plus does on the same method, which
 * then finds the slot empty: a matrix on the network comes first.
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
        if (node == null || pattern.isEmpty()) return;
        Player player = menu.getPlayer();
        switch (AssemblingMatrixUpload.upload(node.getGrid(), pattern)) {
            case NOT_TAKEN -> {}
            case TAKEN -> {
                encodedPatternSlot.set(ItemStack.EMPTY);
                player.displayClientMessage(Component.translatable(AssemblingMatrixMachine.KEY + "uploaded"), true);
            }
            case ALREADY_THERE -> {
                encodedPatternSlot.set(ItemStack.EMPTY);
                ItemStack blank = blankPatternSlot.safeInsert(AEItems.BLANK_PATTERN.stack());
                if (!blank.isEmpty()) player.getInventory().placeItemBackInInventory(blank);
                player.displayClientMessage(Component.translatable(AssemblingMatrixMachine.KEY + "already_there"), true);
            }
        }
    }
}
