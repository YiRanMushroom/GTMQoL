package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.integration.ae2.PatternBufferReturn;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern buffers (GTCEu's and ours) hand the outputs of the multiblock they are in to the ME network, see
 * {@link PatternBufferReturn}. Per buffer switch, on by default.
 */
@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferReturnMixin {

    @Unique
    @SaveField
    @SyncToClient
    private boolean gtmqol$returnToNetwork = true;

    // Built once: the multiblock removes the lists it added by identity when it unforms.
    @Unique
    private @Nullable List<RecipeHandlerList> gtmqol$handlers;

    @Unique
    private boolean gtmqol$getReturnToNetwork() {
        return gtmqol$returnToNetwork;
    }

    @Unique
    private void gtmqol$setReturnToNetwork(boolean enabled) {
        gtmqol$returnToNetwork = enabled;
        gtmqol$self().getSyncDataHolder().markClientSyncFieldDirty("gtmqol$returnToNetwork");
    }

    @Unique
    private MEPatternBufferPartMachine gtmqol$self() {
        return (MEPatternBufferPartMachine) (Object) this;
    }

    @ModifyReturnValue(method = "getRecipeHandlers", at = @At("RETURN"))
    private List<RecipeHandlerList> gtmqol$addReturnHandlers(List<RecipeHandlerList> original) {
        if (original == null) return null;
        if (gtmqol$handlers == null) {
            var handlers = new ArrayList<>(original);
            handlers.add(PatternBufferReturn.createHandlerList(gtmqol$self(), this::gtmqol$getReturnToNetwork));
            gtmqol$handlers = List.copyOf(handlers);
        }
        return gtmqol$handlers;
    }

    @ModifyReturnValue(method = "getPanelBuilder", at = @At("RETURN"))
    private MachineUIPanelBuilder gtmqol$addReturnToggle(MachineUIPanelBuilder builder) {
        return PatternBufferReturn.addToggle(builder, this::gtmqol$getReturnToNetwork,
                this::gtmqol$setReturnToNetwork);
    }
}
