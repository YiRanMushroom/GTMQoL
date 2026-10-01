package com.yiran.minecraft.gtmqol.integration;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.generation.RuntimeGeneration;

import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

/**
 * Visible test content, enabled through {@code config/gtmqol-common.toml}. These are real registered
 * machines reusing GTCEu's own models and textures, not hand-written JSON.
 */
public final class IntegrationTests {
    private static MachineBuilder<?, ?, ?> singleBlockBuilder;
    private static MachineDefinition singleBlock;
    private static MachineBuilder<?, ?, ?> multiblockBuilder;
    private static MachineDefinition multiblock;

    private IntegrationTests() {}

    public static void registerExampleMachines() {
        var single = GTMQoLAddon.registrate()
                .machine("runtime_single_block", info -> new SimpleTieredMachine(info, GTValues.LV))
                .tier(GTValues.LV)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                .workableTieredHullModel(GTCEu.id("block/machines/macerator"))
                .langValue("Runtime Single Block");
        singleBlockBuilder = single;
        singleBlock = single.register();

        var multi = GTMQoLAddon.registrate()
                .multiblock("runtime_multiblock", WorkableElectricMultiblockMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXX", "XXX", "XXX")
                        .slice("XXX", "X#X", "XXX")
                        .slice("XXX", "XYX", "XXX")
                        .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                        .setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(2)))
                        .where('#', Predicates.air())
                        .where('Y', Predicates.controller(Predicates.blocks(definition.getBlock())))
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/electric_blast_furnace"))
                .langValue("Runtime Multiblock");
        multiblockBuilder = multi;
        multiblock = multi.register();

        GTMQoL.LOGGER.info("Integration examples registered: {} and {}",
                singleBlock.getId(), multiblock.getId());
    }

    public static void generateExampleAssets() {
        if (singleBlock == null || multiblock == null) {
            throw new IllegalStateException("Runtime example machines were not registered");
        }
        RuntimeGeneration.generateMachineAssets(singleBlockBuilder, singleBlock);
        RuntimeGeneration.generateMachineAssets(multiblockBuilder, multiblock);
        GTMQoL.LOGGER.info("Integration example assets generated through GTCEu's machine model builders");
    }
}
