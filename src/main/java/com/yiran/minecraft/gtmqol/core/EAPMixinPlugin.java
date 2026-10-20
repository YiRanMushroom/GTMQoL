package com.yiran.minecraft.gtmqol.core;

/**
 * Applies {@code gtmqol.eap.mixins.json} only when ExtendedAE Plus is installed.
 */
public class EAPMixinPlugin extends GTMQoLMixinConfigPlugin {

    private static final String EAP_MOD_ID = "extendedae_plus";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isModLoading(EAP_MOD_ID);
    }
}
