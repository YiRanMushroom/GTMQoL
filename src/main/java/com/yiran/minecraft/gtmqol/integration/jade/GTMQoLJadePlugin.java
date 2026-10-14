package com.yiran.minecraft.gtmqol.integration.jade;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Found by Jade's annotation scan, so only loaded when Jade is installed. */
@WailaPlugin
public class GTMQoLJadePlugin implements IWailaPlugin {

    private final StackLikeRecipeOutputProvider stackLikeOutputs = new StackLikeRecipeOutputProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(stackLikeOutputs, MetaMachine.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(stackLikeOutputs, MetaMachineBlock.class);
    }
}
