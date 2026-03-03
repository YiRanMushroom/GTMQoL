package com.yiran.minecraft.gtmqol.mixin.bug_fix;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.ModUtils;
import dev.latvian.mods.kubejs.platform.forge.MiscForgeHelper;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MiscForgeHelper.class)
@Condition(value = "kubejs", type = Condition.Type.MOD)
public class KubeJsDataGenFixMixin {
    @ModifyReturnValue(method = "isDataGen", at = @At("RETURN"), remap = false, require = 0) // set to 0 so that if this is fixed, this does nothing rather than crash the game
    private static boolean gtmqol$fixKubeJsDataGen(boolean original) {
        if (original != ModUtils.isDataGen()) {
            GTMQoL.LOGGER.warn("KubeJS is having problems detecting data generation environment. This will cause non-daemon threads to be created but never stopped, which will cause the JVM to never exit. To fix this, GTMQoL is forcing KubeJS to use the correct value for data generation environment detection. If you are seeing this message, it means the fix is working.");
        }
        return ModUtils.isDataGen();
    }
}
