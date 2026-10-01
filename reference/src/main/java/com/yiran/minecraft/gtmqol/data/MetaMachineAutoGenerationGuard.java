package com.yiran.minecraft.gtmqol.data;

public class MetaMachineAutoGenerationGuard implements AutoCloseable {
    private static final Object lock = new Object();
    private static int holderCount = 0;

    public static boolean shouldAutoGenerate() {
        synchronized (lock) {
            return holderCount != 0;
        }
    }

    MetaMachineAutoGenerationGuard() {
        synchronized (lock) {
            holderCount++;
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            holderCount--;
        }
    }
}
