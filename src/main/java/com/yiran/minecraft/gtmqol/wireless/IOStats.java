package com.yiran.minecraft.gtmqol.wireless;

import java.util.ArrayDeque;

/**
 * Input/output rates of one network, for the monitors. Counts every insert/extract and samples the totals
 * every {@link #SAMPLE_INTERVAL} ticks; rates are averaged over the last {@link #SAMPLES} samples.
 * Not saved, so rates restart from zero with the server.
 *
 * <p>The totals may wrap around; only differences between samples are used, which stay correct.</p>
 */
public final class IOStats {

    public static final int SAMPLE_INTERVAL = 20;
    public static final int SAMPLES = 10;

    private long inserted, extracted;
    /**
     * {tick, inserted, extracted}, oldest first.
     */
    private final ArrayDeque<long[]> samples = new ArrayDeque<>();
    private double inputRate, outputRate;

    public void recordInsert(long amount) {
        inserted += amount;
    }

    public void recordExtract(long amount) {
        extracted += amount;
    }

    public void sample(long tick) {
        samples.addLast(new long[] { tick, inserted, extracted });
        while (samples.size() > SAMPLES + 1) samples.removeFirst();
        long[] first = samples.getFirst(), last = samples.getLast();
        long ticks = last[0] - first[0];
        if (ticks <= 0) return;
        inputRate = (double) (last[1] - first[1]) / ticks;
        outputRate = (double) (last[2] - first[2]) / ticks;
    }

    /**
     * Per tick.
     */
    public double inputRate() {
        return inputRate;
    }

    /**
     * Per tick.
     */
    public double outputRate() {
        return outputRate;
    }
}
