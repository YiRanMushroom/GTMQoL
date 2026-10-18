package com.yiran.minecraft.gtmqol.api.generation;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.builders.BuilderCallback;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.defaultTankSizeFunction;

/**
 * {@link MachineBuilder} with a chainable {@link #dynamicallyGenerated(boolean)} and {@link #workableTiered}. Every
 * chained GTCEu method returns {@code SELF}, so these stay available anywhere before {@code register()}.
 */
public class GTMQoLMachineBuilder<D extends MachineDefinition, M extends MetaMachine>
        extends MachineBuilder<D, M, GTMQoLMachineBuilder<D, M>> {

    private int tier;

    public GTMQoLMachineBuilder(GTRegistrate registrate, String name, BuilderCallback callback,
                                MachineInstanceFactory<M> instanceFactory) {
        super(registrate, name, callback, instanceFactory);
    }

    public GTMQoLMachineBuilder<D, M> dynamicallyGenerated(boolean dynamicGenerated) {
        ((IDynamicGenerationHandler) this).gtmqol$setDynamicallyGenerated(dynamicGenerated);
        return this;
    }

    @Override
    public GTMQoLMachineBuilder<D, M> tier(int tier) {
        this.tier = tier;
        return super.tier(tier);
    }

    /**
     * Recipe type, model and tooltips of a GT electric single block of this {@link #tier}: the workable hull model
     * and GT's voltage, energy and tank lines.
     */
    public GTMQoLMachineBuilder<D, M> workableTiered(Supplier<GTRecipeType> recipeType, ResourceLocation model) {
        return recipeType(recipeType)
                .workableTieredHullModel(model)
                .tooltips(GTMachineUtils.workableTiered(tier, V[tier], V[tier] * 64, recipeType,
                        defaultTankSizeFunction.applyAsInt(tier), true));
    }
}
