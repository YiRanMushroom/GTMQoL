package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.yiran.minecraft.gtmqol.common.stacklike.ae2.AEStackLikeBridges;
import com.yiran.minecraft.gtmqol.common.stacklike.ae2.PatternBufferStackLike;
import com.yiran.minecraft.gtmqol.common.stacklike.ae2.PatternBufferStackLikeHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.integration.ae2.patternbuffer.AbstractMEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.patternbuffer.PatternOutputMarks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

/**
 * What a pattern buffer proxy's handler list needs for the recipe lookup besides gtceu's item and fluid proxies: a
 * proxy of each of the buffer's bridged aggregate handlers (see {@link PatternBufferStackLike}), and the buffer's
 * pattern output marks ({@link PatternOutputMarks}). Recipes already run through the buffer's own list, slot by slot.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler$ProxyRHL",
       remap = false)
public abstract class MEPatternBufferProxyStackLikeMixin {

    @Shadow
    private @Nullable MEPatternBufferPartMachine buffer;

    @Unique
    private final List<PatternBufferStackLike.Proxy<?>> gtmqol$proxies = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtmqol$addStackLikeProxies(MEPatternBufferProxyPartMachine machine, CallbackInfo ci) {
        for (var bridge : AEStackLikeBridges.all()) {
            var proxy = machine.attachTrait(PatternBufferStackLike.proxy(bridge.cap()));
            gtmqol$proxies.add(proxy);
            ((RecipeHandlerList) (Object) this).addHandlers(proxy);
        }
        // Untested: a multiblock fed through an ME Pattern Buffer Proxy of an Overclocked ME Pattern Buffer with
        // Mark Pattern Outputs on runs the recipe each pattern is for (e.g. two patterns with the same inputs)
        ((RecipeHandlerList) (Object) this).addHandlers(new PatternOutputMarks(this::gtmqol$outputMarks));
    }

    @Unique
    private List<GhostIngredient<?>> gtmqol$outputMarks() {
        return buffer instanceof AbstractMEPatternBufferPartMachine ours ? ours.allOutputMarks() : List.of();
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
