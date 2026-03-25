package com.yiran.minecraft.gtmqol.mixin.modpack_integration.monifactory;

import com.yiran.minecraft.gtmqol.functionality.IAppendOnBuildAction;
import com.yiran.minecraft.gtmqol.integration.monifactory.MoniRecipeTypesExtension;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import net.neganote.monilabs.gtbridge.MoniRecipeTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoniRecipeTypes.class)
@Condition(type = Condition.Type.MOD, value = "monilabs")
public class MoniRecipeTypesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onStaticInit(CallbackInfo ci) {
        MoniRecipeTypesExtension.init();

        ((IAppendOnBuildAction) (MoniRecipeTypes.CHROMATIC_PROCESSING))
                .appendOnRecipeBuild(MoniRecipeTypesExtension::onPrismaCRecipeBuild);

        ((IAppendOnBuildAction) (MoniRecipeTypes.CHROMATIC_TRANSCENDENCE))
                .appendOnRecipeBuild(MoniRecipeTypesExtension::onPrismaCRecipeBuild);

        ((IAppendOnBuildAction) (MoniRecipeTypes.MICROVERSE_RECIPES))
                .appendOnRecipeBuild(MoniRecipeTypesExtension::onMicroverseRecipeBuild);
    }
}
