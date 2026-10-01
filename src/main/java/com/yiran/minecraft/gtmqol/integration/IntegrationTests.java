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
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

/**
 * Visible test content, enabled through the gtmqol config. These are real registered machines reusing
 * GTCEu's own models and textures, with their assets generated at runtime.
 */
public final class IntegrationTests {

    private IntegrationTests() {}

    public static void registerExampleMachines() {
        MachineDefinition singleBlock = GTMQoLAddon
                .machine("runtime_single_block", info -> new SimpleTieredMachine(info, GTValues.LV))
                .tier(GTValues.LV)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                .workableTieredHullModel(GTCEu.id("block/machines/macerator"))
                .langValue("Runtime Single Block")
                .dynamicallyGenerated(true)
                .register();

        MachineDefinition multiblock = GTMQoLAddon
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
                        .where('Y', Predicates.controller(definition))
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/electric_blast_furnace"))
                .langValue("Runtime Multiblock")
                .dynamicallyGenerated(true)
                .register();

        GTMQoL.LOGGER.info("Integration examples registered: {} and {}",
                singleBlock.getId(), multiblock.getId());
    }
}
