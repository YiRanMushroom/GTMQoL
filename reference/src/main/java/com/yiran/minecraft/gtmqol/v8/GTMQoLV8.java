package com.yiran.minecraft.gtmqol.v8;

import net.minecraftforge.fml.common.Mod;

@Mod(GTMQoLV8.MOD_ID)
public final class GTMQoLV8 {
    public static final String MOD_ID = "gtmqol";

    public GTMQoLV8() {
        GTMQoLV8Addon.registrate().registerRegistrate();
    }
}
