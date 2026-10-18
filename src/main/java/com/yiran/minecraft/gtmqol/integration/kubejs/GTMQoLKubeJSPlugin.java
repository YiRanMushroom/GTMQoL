package com.yiran.minecraft.gtmqol.integration.kubejs;

import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.yiran.minecraft.gtmqol.common.blockprovider.BlockProviderRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.BlockComponent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import com.mojang.datafixers.util.Pair;

/**
 * Listed in {@code kubejs.plugins.txt}, so KubeJS only loads it when it is installed.
 *
 * <p>{@code BlockProviderRecipeCapability.CAP} goes into gtceu's schema as any capability, with a block id:
 * {@code event.recipes.gtmqol.crystal_growth('x').input(BlockProviderRecipeCapability.CAP, 'minecraft:budding_amethyst')}.
 * The block is never consumed, whatever the chance.</p>
 */
public class GTMQoLKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("BlockProviderRecipeCapability", BlockProviderRecipeCapability.class);
        event.add("GhostIngredientRecipeCapability", GhostIngredientRecipeCapability.class);
    }

    /** From {@code GTMQoLAddon.registerRecipeKeys}: how gtceu's KubeJS schema writes the capability's contents. */
    public static void registerRecipeKeys(KJSRecipeKeyEvent event) {
        var cap = BlockProviderRecipeCapability.CAP;
        event.registerKey(cap, Pair.of(new ContentJS<>(BlockComponent.INPUT, cap, false),
                new ContentJS<>(BlockComponent.OUTPUT, cap, true)));
        var ghost = GhostIngredientRecipeCapability.CAP;
        event.registerKey(ghost, Pair.of(new ContentJS<>(GhostIngredientComponent.INSTANCE, ghost, false),
                new ContentJS<>(GhostIngredientComponent.INSTANCE, ghost, true)));
    }
}
