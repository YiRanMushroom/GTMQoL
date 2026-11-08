package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.AECraftingPlanner;
import com.yiran.minecraft.gtmqol.integration.ae2.crafting.CraftingSolver;

import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tries {@link CraftingSolver} before AE2 walks its tree. When it can't judge a request, AE2's code goes on as if
 * nothing happened.
 */
@Mixin(value = CraftingCalculation.class, remap = false)
public abstract class CraftingCalculationPlannerMixin {

    @Shadow
    @Final
    private NetworkCraftingSimulationState networkInv;
    @Shadow
    @Final
    private AEKey output;
    @Shadow
    @Final
    private KeyCounter missing;
    @Shadow
    @Final
    ICraftingSimulationRequester simRequester;
    @Shadow
    private boolean simulate;

    @Inject(method = "runCraftAttempt", at = @At("HEAD"), cancellable = true)
    private void gtmqol$solve(boolean simulate, long amount, CallbackInfoReturnable<CraftingPlan> cir) {
        var node = simRequester.getGridNode();
        if (node == null || !GTMQoLConfig.get().ae2.fastCraftingPlans) return;
        var inventory = new ChildCraftingSimulationState(networkInv);
        inventory.ignore(output);
        var planner = new AECraftingPlanner(node.getGrid().getCraftingService(), inventory);
        var result = CraftingSolver.solve(planner, output, amount);
        switch (result.status()) {
            case REFUSED -> {}
            case MISSING -> {
                if (!simulate) {
                    cir.setReturnValue(null);
                    return;
                }
                gtmqol$finish(planner, result, inventory, simulate, amount, cir);
            }
            case PLANNED -> gtmqol$finish(planner, result, inventory, simulate, amount, cir);
        }
    }

    @Unique
    private void gtmqol$finish(AECraftingPlanner planner,
                               CraftingSolver.Result<AEKey, appeng.api.crafting.IPatternDetails> result,
                               ChildCraftingSimulationState inventory, boolean simulate, long amount,
                               CallbackInfoReturnable<CraftingPlan> cir) {
        this.simulate = simulate;
        planner.apply(result, output, amount, missing);
        cir.setReturnValue(CraftingSimulationState.buildCraftingPlan(inventory,
                (CraftingCalculation) (Object) this, amount));
    }
}
