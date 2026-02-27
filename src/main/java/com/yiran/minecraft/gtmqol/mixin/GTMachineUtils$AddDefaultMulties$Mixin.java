package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.mixin_impl.AddDefaultMultiesImpl;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GTMachineUtils.class)
public class GTMachineUtils$AddDefaultMulties$Mixin {
    @Inject(method = "registerSimpleMachines(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Ljava/lang/String;Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;Lit/unimi/dsi/fastutil/ints/Int2IntFunction;Z)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;",
            at = @At("HEAD"),
            remap = false)
    private static void $addDefaultMulties(GTRegistrate registrate, String name, GTRecipeType recipeType, Int2IntFunction tankScalingFunction, boolean hasPollutionDebuff, CallbackInfoReturnable<MachineDefinition[]> cir) {
        AddDefaultMultiesImpl.generateMultiblockForSimpleMachine(registrate, name, recipeType);
    }
}
