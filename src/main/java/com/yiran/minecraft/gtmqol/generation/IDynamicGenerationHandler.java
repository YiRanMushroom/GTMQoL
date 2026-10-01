package com.yiran.minecraft.gtmqol.generation;

/**
 * Mixed into every GTCEu {@code MachineBuilder} (see {@code MachineBuilderMixin}). When set,
 * {@code register()} hands the builder to {@link RuntimeGeneration}.
 * <p>
 * Our own builders expose this as a chainable {@code dynamicGenerated(boolean)}. For builders made
 * elsewhere (e.g. GTCEu's helpers), cast to this interface.
 */
public interface IDynamicGenerationHandler {

    void gtmqol$setDynamicallyGenerated(boolean dynamicGenerated);

    boolean gtmqol$isDynamicallyGenerated();
}
