package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.batch.BatchPlan;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.CraftingSimulationState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/** Every crafting plan passes here, whichever CPU ends up with it: see {@link BatchPlan}. */
@Mixin(value = CraftingSimulationState.class, remap = false)
public abstract class CraftingSimulationStateBatchMixin {

    @Shadow
    @Final
    private Map<IPatternDetails, Long> crafts;
    @Shadow
    @Final
    private KeyCounter emittedItems;

    @Inject(method = "buildCraftingPlan", at = @At("HEAD"))
    private static void gtmqol$cutIntoBatches(CraftingSimulationState state, CraftingCalculation calculation,
                                              long calculatedAmount, CallbackInfoReturnable<CraftingPlan> cir) {
        var node = ((CraftingCalculationAccessor) calculation).gtmqol$getRequester().getGridNode();
        if (node == null) return;
        var self = (CraftingSimulationStateBatchMixin) (Object) state;
        BatchPlan.cut(self.crafts, self.emittedItems.keySet(), node);
    }
}
