package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

/**
 * Applies {@code gtmqol.ae2.mixins.json} only when AE2 is installed. The mixins with a switch or a mod of their own are
 * named here; every other one applies, having no effect without what it serves (a bridge, a universal ME input, a
 * buffer with the option on).
 */
public class AE2MixinPlugin extends GTMQoLMixinConfigPlugin {

    private static final String PACKAGE = "com.yiran.minecraft.gtmqol.core.mixins.ae2.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!isModLoading("ae2")) return false;
        return switch (mixinClassName.substring(PACKAGE.length())) {
            // early config ae2.universalCircuitEncoding
            case "EncodingHelperMixin" -> EarlyConfig.AE2_UNIVERSAL_CIRCUIT_ENCODING;
            // toggled at runtime by GTMQoLConfig.ae2.skipNotConsumedInputs
            case "EmiEncodePatternHandlerMixin" -> isModLoading("emi");
            case "EncodePatternTransferHandlerMixin" -> isModLoading("jei");
            // the sticky card: early config ae2.stickyCard
            case "MEInventoryHandlerMixin", "NetworkStorageMixin", "StorageBusPartMixin" -> EarlyConfig.AE2_STICKY_CARD;
            // ExtendedAE's mod id is still the old name of its 1.20.1 fork
            case "PartSpecialStorageBusMixin" -> EarlyConfig.AE2_STICKY_CARD && isModLoading("expatternprovider");
            default -> true;
        };
    }
}
