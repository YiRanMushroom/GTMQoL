package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link AEStackLikeBridge}s of the loaded mods. Registered during mod construction, before any machine is
 * created: the ME parts attach one handler per bridge in their constructor.
 */
public final class AEStackLikeBridges {

    private static final List<AEStackLikeBridge<?, ?>> BRIDGES = new ArrayList<>();

    private AEStackLikeBridges() {}

    public static void register(AEStackLikeBridge<?, ?> bridge) {
        BRIDGES.add(bridge);
    }

    public static List<AEStackLikeBridge<?, ?>> all() {
        return Collections.unmodifiableList(BRIDGES);
    }

    public static @Nullable AEStackLikeBridge<?, ?> forKey(AEKey key) {
        for (var bridge : BRIDGES) {
            if (bridge.isKey(key)) return bridge;
        }
        return null;
    }
}
