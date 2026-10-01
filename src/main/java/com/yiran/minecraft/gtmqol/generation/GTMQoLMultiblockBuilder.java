package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;

/**
 * Multiblock counterpart of {@link GTMQoLMachineBuilder}. Java has no multiple inheritance, hence the copy.
 */
public class GTMQoLMultiblockBuilder<M extends MultiblockControllerMachine>
        extends MultiblockMachineBuilder<MultiblockMachineDefinition, M, GTMQoLMultiblockBuilder<M>> {

    public GTMQoLMultiblockBuilder(GTRegistrate registrate, String name,
                                   BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                   BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                   MachineInstanceFactory<M> blockEntityFactory) {
        super(registrate, name, blockFactory, itemFactory, blockEntityFactory);
    }

    public GTMQoLMultiblockBuilder<M> dynamicallyGenerated(boolean dynamicGenerated) {
        ((IDynamicGenerationHandler) this).gtmqol$setDynamicallyGenerated(dynamicGenerated);
        return this;
    }
}
