package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.datacenter.MEDataCenterHooks;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tells an ME Data Center how much storage a job took that one of its CPUs accepted: the plan's size is known only
 * here.
 */
@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class CraftingCPUClusterMixin {

    @Inject(method = "submitJob", at = @At("RETURN"))
    private void gtmqol$dataCenterJob(IGrid grid, ICraftingPlan plan, IActionSource src, ICraftingRequester requester,
                                      CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        if (cir.getReturnValue().successful()) {
            MEDataCenterHooks.submitted((CraftingCPUCluster) (Object) this, plan.bytes());
        }
    }
}
