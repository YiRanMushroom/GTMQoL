package com.yiran.minecraft.gtmqol.integration.kubejs;

import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

/**
 * Listed in {@code kubejs.plugins.txt}, so KubeJS only loads it when it is installed.
 *
 * <p>{@code BlockProviderRecipeCapability.CAP} goes into gtceu's schema as any capability, with a block id:
 * {@code event.recipes.gtmqol.crystal_growth('x').input(BlockProviderRecipeCapability.CAP, 'minecraft:budding_amethyst')}.
 * Add it with {@code .chance(0)} before and {@code .chance(10000)} after, like gtceu's non-consumed inputs.</p>
 */
public class GTMQoLKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("BlockProviderRecipeCapability", BlockProviderRecipeCapability.class);
    }
}
