package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

/**
 * Applies {@code gtmqol.ae2.mixins.json} only when AE2 is installed, and the ExtendedAE mixin only when ExtendedAE
 * is too.
 */
public class AE2MixinPlugin extends GTMQoLMixinConfigPlugin {

    private static final String ENCODING_MIXIN = "com.yiran.minecraft.gtmqol.core.mixins.ae2.EncodingHelperMixin";
    private static final String EMI_ENCODING_MIXIN = "com.yiran.minecraft.gtmqol.core.mixins.ae2.EmiEncodePatternHandlerMixin";
    private static final String EXTENDEDAE_MIXIN = "com.yiran.minecraft.gtmqol.core.mixins.ae2.PartSpecialStorageBusMixin";
    private static final String STOCKING_MIXIN_PREFIX = "com.yiran.minecraft.gtmqol.core.mixins.ae2.MEStocking";
    private static final String PATTERN_BUFFER_MIXIN_PREFIX = "com.yiran.minecraft.gtmqol.core.mixins.ae2.MEPatternBuffer";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!isModLoading("ae2")) return false;
        if (mixinClassName.equals(ENCODING_MIXIN)) return EarlyConfig.AE2_UNIVERSAL_CIRCUIT_ENCODING;
        // toggled at runtime by GTMQoLConfig.ae2.skipNotConsumedInputs
        if (mixinClassName.equals(EMI_ENCODING_MIXIN)) return isModLoading("emi");
        // duplicate checks against the universal ME input; no effect without one
        if (mixinClassName.startsWith(STOCKING_MIXIN_PREFIX)) return true;
        // bridged capabilities (e.g. chemicals) in the pattern buffer; no effect without a bridge
        if (mixinClassName.startsWith(PATTERN_BUFFER_MIXIN_PREFIX)) return true;
        // shared hatches (toggled per buffer in its UI); not all of them target the pattern buffer
        if (mixinClassName.endsWith("SharedHatchesMixin")) return true;
        // everything else is the sticky card
        if (!EarlyConfig.AE2_STICKY_CARD) return false;
        return !mixinClassName.equals(EXTENDEDAE_MIXIN) || isModLoading("extendedae");
    }
}
