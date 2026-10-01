package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CleanroomCondition.class)
public class CleanroomConditionMixin {
    @WrapOperation(method = "testCondition", at = @At(value = "FIELD", target = "Lcom/gregtechceu/gtceu/config/ConfigHolder$MachineConfigs;cleanMultiblocks:Z", opcode = Opcodes.GETFIELD))
    private boolean overrideCleanroomCondition(com.gregtechceu.gtceu.config.ConfigHolder.MachineConfigs instance, Operation<Boolean> original) {
        return ConfigHolder.getInstance().addonConfig.multiblocksIgnoreCleanroomRecipes || original.call(instance);
    }
}
