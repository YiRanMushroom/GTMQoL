package com.yiran.minecraft.gtmqol.api.generation;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link MachineBuilder} with a chainable {@link #dynamicallyGenerated(boolean)}. Every chained GTCEu
 * method returns {@code SELF}, so this stays available anywhere before {@code register()}.
 */
public class GTMQoLMachineBuilder<D extends MachineDefinition, M extends MetaMachine>
        extends MachineBuilder<D, M, GTMQoLMachineBuilder<D, M>> {

    public GTMQoLMachineBuilder(GTRegistrate registrate, String name,
                                Function<ResourceLocation, D> definition,
                                BiFunction<BlockBehaviour.Properties, D, MetaMachineBlock> blockFactory,
                                BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                MachineInstanceFactory<M> instanceFactory) {
        super(registrate, name, definition, blockFactory, itemFactory, instanceFactory);
    }

    public GTMQoLMachineBuilder<D, M> dynamicallyGenerated(boolean dynamicGenerated) {
        ((IDynamicGenerationHandler) this).gtmqol$setDynamicallyGenerated(dynamicGenerated);
        return this;
    }
}
