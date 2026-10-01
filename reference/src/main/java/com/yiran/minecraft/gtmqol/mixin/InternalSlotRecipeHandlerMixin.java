package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.InternalSlotRecipeHandler;
import com.yiran.minecraft.gtmqol.api.ISlotHint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(InternalSlotRecipeHandler.class)
public class InternalSlotRecipeHandlerMixin {
    @Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.InternalSlotRecipeHandler$SlotRHL")
    public static class SlotRHLMixin {

//        @ModifyArg(
//                method = "<init>",
//                at = @At(
//                        value = "INVOKE",
//                        target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/trait/InternalSlotRecipeHandler$SlotRHL;addHandlers([Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeHandler;)V"
//                ),
//                index = 0
//        )
//        private IRecipeHandler<?>[] qol$filterSharedHandlers(IRecipeHandler<?>[] originalHandlers) {
//            if (originalHandlers.length >= 5) {
//                return new IRecipeHandler<?>[]{ originalHandlers[3], originalHandlers[4] };
//            }
//            return originalHandlers;
//        }
    }

    @Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.InternalSlotRecipeHandler$SlotItemRecipeHandler")
    public static class SlotItemRecipeHandler$SlotItemRecipeHandlerMixin implements ISlotHint {
        @Shadow
        @Final
        private MEPatternBufferPartMachine.InternalSlot slot;

        @Override
        public Object qol$getMatchingGroup() {
            return slot;
        }
    }

    @Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.trait.InternalSlotRecipeHandler$SlotFluidRecipeHandler")
    public static class SlotFluidRecipeHandler$SlotFluidRecipeHandlerMixin implements ISlotHint {
        @Shadow
        @Final
        private MEPatternBufferPartMachine.InternalSlot slot;

        @Override
        public Object qol$getMatchingGroup() {
            return slot;
        }
    }
}
