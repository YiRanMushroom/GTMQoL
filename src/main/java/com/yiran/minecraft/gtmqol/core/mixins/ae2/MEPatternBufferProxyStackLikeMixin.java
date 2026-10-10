package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLike;
import com.yiran.minecraft.gtmqol.integration.ae2.stacklike.PatternBufferStackLikeHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A pattern buffer proxy's handler list, with a proxy of each of the buffer's bridged aggregate handlers (see
 * {@link PatternBufferStackLike}). Recipes already run through the buffer's own list.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler$ProxyRHL",
       remap = false)
public abstract class MEPatternBufferProxyStackLikeMixin {

    @Unique
    private final List<PatternBufferStackLike.Proxy<?>> gtmqol$proxies = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtmqol$addStackLikeProxies(MEPatternBufferProxyPartMachine machine, CallbackInfo ci) {
        for (var bridge : AEStackLikeBridges.all()) {
            var proxy = machine.attachTrait(PatternBufferStackLike.proxy(bridge.cap()));
            gtmqol$proxies.add(proxy);
            ((RecipeHandlerList) (Object) this).addHandlers(proxy);
        }
    }

    @Inject(method = "setBuffer", at = @At("TAIL"))
    private void gtmqol$setStackLikeProxies(MEPatternBufferPartMachine buffer, CallbackInfo ci) {
        var holder = (PatternBufferStackLikeHolder) buffer;
        for (var proxy : gtmqol$proxies) proxy.setProxy(holder.gtmqol$getStackLikeAggregate(proxy.getCapability()));
    }

    @Inject(method = "clearBuffer", at = @At("TAIL"))
    private void gtmqol$clearStackLikeProxies(CallbackInfo ci) {
        for (var proxy : gtmqol$proxies) proxy.setProxy(null);
    }
}
