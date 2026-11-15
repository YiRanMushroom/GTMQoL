package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import com.yiran.minecraft.gtmqol.integration.ae2.crafting.PatternBatch;
import com.yiran.minecraft.gtmqol.integration.ae2.datacenter.MEDataCenterHooks;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.crafting.CraftingLink;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Pattern batching: who can do a batch is who offers its pattern (patterns are the keys here, and batches are
 * nobody's). ME Data Centers: their crafting CPUs join the ones the service finds built of blocks.
 */
@Mixin(value = CraftingService.class, remap = false)
public abstract class CraftingServiceMixin {

    @Shadow
    @Final
    private Set<CraftingCPUCluster> craftingCPUClusters;
    @Shadow
    @Final
    private IGrid grid;

    @Shadow
    public abstract void addLink(CraftingLink link);

    @ModifyVariable(method = "getProviders", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails gtmqol$patternOfBatch(IPatternDetails key) {
        return PatternBatch.unwrap(key);
    }

    @Inject(method = "updateCPUClusters", at = @At("TAIL"))
    private void gtmqol$dataCenterCpus(CallbackInfo ci) {
        MEDataCenterHooks.addCpus(grid, craftingCPUClusters, this::addLink);
    }
}
