package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.Predicates.blocks
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate

object QoLMultiblocks {
    @JvmStatic
    var SMART_ASSEMBLY_FACTORY: MultiblockMachineDefinition? = null

    @JvmStatic
    fun init() {
        var registrate = GTMQoLRegistrate.REGISTRATE

        SMART_ASSEMBLY_FACTORY =
            registrate.multiblock(
                "smart_assembly_factory",
                ::WorkableElectricMultiblockMachine
            ).rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
                .recipeModifiers(
                    GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.OC_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE
                )
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern { definition ->
                    FactoryBlockPattern.start(
                        RelativeDirection.BACK,
                        RelativeDirection.UP,
                        RelativeDirection.RIGHT
                    ) // 7 * 7 * 11
                        .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX") // first
                        // layer
                        .aisle("XXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .setRepeatable(4) // middle:
                        .aisle("SXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .aisle("XXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .setRepeatable(4)
                        .aisle("XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX") // last
                        // layer
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where(
                            'X', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                .or(Predicates.autoAbilities(*definition.getRecipeTypes()))
                                .or(Predicates.autoAbilities(true, false, true))
                                .or(Predicates.dataHatchPredicate(blocks(GTBlocks.CASING_GRATE.get())))
                        )
                        .where('G', blocks(GTBlocks.CASING_GRATE.get()))
                        .where('A', blocks(GTBlocks.CASING_ASSEMBLY_CONTROL.get()))
                        .where('R', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                        .where('T', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                        .where('#', Predicates.any())
                        .build()
                }
                .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembly_line"))
                .register()
    }
}