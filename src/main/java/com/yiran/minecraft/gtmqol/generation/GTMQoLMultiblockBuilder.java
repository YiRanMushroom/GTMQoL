package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;

import com.tterrag.registrate.builders.BuilderCallback;

/**
 * Multiblock counterpart of {@link GTMQoLMachineBuilder}. On 1.21 {@link MultiblockMachineBuilder} fixes its
 * {@code SELF} type, so every chained call returns the plain builder: call {@link #dynamicallyGenerated}
 * first in the chain.
 */
public class GTMQoLMultiblockBuilder<M extends MultiblockControllerMachine> extends MultiblockMachineBuilder<M> {

    public GTMQoLMultiblockBuilder(GTRegistrate registrate, String name, BuilderCallback callback,
                                   MachineInstanceFactory<M> blockEntityFactory) {
        super(registrate, name, callback, blockEntityFactory);
    }

    public GTMQoLMultiblockBuilder<M> dynamicallyGenerated(boolean dynamicGenerated) {
        ((IDynamicGenerationHandler) this).gtmqol$setDynamicallyGenerated(dynamicGenerated);
        return this;
    }
}
