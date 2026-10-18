package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.SharedHatches;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/** A pattern buffer proxy follows its buffer's {@link SharedHatches} toggle, with its own machine's shared parts. */
@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.ProxySlotRecipeHandler$ProxyRHL",
       remap = false)
public abstract class MEPatternBufferProxySharedHatchesMixin implements SharedHatches.HandlerList {

    @Shadow
    private @Nullable MEPatternBufferPartMachine buffer;

    @Override
    public boolean gtmqol$sharesHatches() {
        return buffer != null && ((SharedHatches.HandlerList) buffer.getBufferRecipeHandler()).gtmqol$sharesHatches();
    }

    @Override
    public void gtmqol$setSharedHatches(List<RecipeHandlerList> shared) {
        if (buffer != null) ((SharedHatches.HandlerList) buffer.getBufferRecipeHandler()).gtmqol$setSharedHatches(shared);
    }
}
