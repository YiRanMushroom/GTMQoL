package com.yiran.minecraft.gtmqol.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class GTMQoLConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_INTEGRATION_TESTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Development and integration test settings").push("integration_tests");
        ENABLE_INTEGRATION_TESTS = builder
                .comment("Register visible runtime-generated machine examples during startup")
                .define("enabled", false);
        builder.pop();
        SPEC = builder.build();
    }

    private GTMQoLConfig() {
    }
}
