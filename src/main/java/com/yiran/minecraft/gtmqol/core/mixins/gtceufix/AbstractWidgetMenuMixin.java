package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import brachy.modularui.widget.AbstractWidget;
import brachy.modularui.widgets.menu.AbstractMenuButton;

/**
 * ModularUI bug that GTCEu's multiblock UI runs into: a menu button's menu panel stays open after the button is
 * removed from the tree, and the next click anywhere crashes in {@code MenuPanel.closeAllMenus} ("ContextMenuButton#...
 * is not in a valid state"). GTCEu rebuilds the structure error list (a {@code DynamicWidget}, with a
 * {@code ContextMenuButton} per missing part ability, opened by just hovering it) whenever the errors change, which
 * auto-build makes happen while the UI is open. Close the menu while the button is still valid. Remove this mixin
 * once ModularUI fixes it.
 */
@Mixin(value = AbstractWidget.class, remap = false)
public class AbstractWidgetMenuMixin {

    @Inject(method = "dispose", at = @At("HEAD"))
    private void gtmqol$closeMenuOfDisposedButton(CallbackInfo ci) {
        if ((Object) this instanceof AbstractMenuButton<?> button && button.isOpen() && button.isValid()) {
            button.closeMenu(false);
        }
    }
}
