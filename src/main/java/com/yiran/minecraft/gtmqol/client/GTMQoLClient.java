package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.yiran.minecraft.gtmqol.GTMQoL;

/** Called from the mod constructor on the client only, like gtceu's {@code ClientProxy}. */
public final class GTMQoLClient {

    private GTMQoLClient() {}

    public static void init() {
        // Must be registered before machine models are generated or loaded, they reference the type by id.
        DynamicRenderManager.register(GTMQoL.id("dtfr_ring"), DTFRRingRender.TYPE);
    }
}
