package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import com.tterrag.registrate.builders.BuilderCallback;

/**
 * {@link MachineBuilder} with a chainable {@link #dynamicallyGenerated(boolean)}. Every chained GTCEu
 * method returns {@code SELF}, so this stays available anywhere before {@code register()}.
 */
public class GTMQoLMachineBuilder<D extends MachineDefinition, M extends MetaMachine>
        extends MachineBuilder<D, M, GTMQoLMachineBuilder<D, M>> {

    public GTMQoLMachineBuilder(GTRegistrate registrate, String name, BuilderCallback callback,
                                MachineInstanceFactory<M> instanceFactory) {
        super(registrate, name, callback, instanceFactory);
    }

    public GTMQoLMachineBuilder<D, M> dynamicallyGenerated(boolean dynamicGenerated) {
        ((IDynamicGenerationHandler) this).gtmqol$setDynamicallyGenerated(dynamicGenerated);
        return this;
    }
}
