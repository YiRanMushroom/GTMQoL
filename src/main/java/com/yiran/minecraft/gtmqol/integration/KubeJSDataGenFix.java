package com.yiran.minecraft.gtmqol.integration;

import dev.latvian.mods.kubejs.util.KubeJSBackgroundThread;

/**
 * KubeJS decides whether to start its non-daemon background thread via
 * {@code ModLoader.isDataGenRunning()}, which Forge 47 never sets to true. The thread only stops when
 * {@code Util.shutdownExecutors()} runs, which datagen never calls, so the datagen JVM never exits.
 * Clearing the flag lets the thread leave its loop and shut its executors down, same as KubeJS does on
 * a normal shutdown.
 * <p>
 * Only touch this class after checking that kubejs is loaded.
 */
public final class KubeJSDataGenFix {

    public static void apply() {
        KubeJSBackgroundThread.running = false;
    }

    private KubeJSDataGenFix() {
    }
}
