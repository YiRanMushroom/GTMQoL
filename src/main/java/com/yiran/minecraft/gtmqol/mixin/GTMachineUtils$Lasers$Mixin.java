package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.yiran.minecraft.gtmqol.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GTMachineUtils.class)
public class GTMachineUtils$Lasers$Mixin {
    @ModifyExpressionValue(method = "registerLaserHatch(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;ILcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At(value = "FIELD", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;HIGH_TIERS:[I"))
    private static int[] modifyHighTiers(int[] original) {
        return ModUtils.getLaserTiers();
    }

}
