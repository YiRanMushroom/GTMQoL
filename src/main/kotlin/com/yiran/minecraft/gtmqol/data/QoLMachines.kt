package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate


object QoLMachines {
    @JvmStatic
    val GREENHOUSE: Array<MachineDefinition>

    init {
        GREENHOUSE = GTMachineUtils.registerTieredMachines(
            GTMQoLRegistrate.REGISTRATE,
            "greenhouse",
            { holder, tier ->
                SimpleTieredMachine(
                    holder, tier,
                    GTMachineUtils.defaultTankSizeFunction
                )
            },
            { tier, builder ->
                val tooltipList = GTMachineUtils.workableTiered(
                    tier,
                    GTValues.V[tier],
                    GTValues.V[tier] * 64L,
                    QoLRecipeTypes.GREEN_HOUSE_RECIPES,
                    GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier).toLong(),
                    true
                )

                builder.langValue("${GTValues.VLVH[tier]} Greenhouse ${GTValues.VLVT[tier]}")
                    .editableUI(
                        SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                            com.yiran.minecraft.gtmqol.GTMQoL.id("greenhouse"),
                            QoLRecipeTypes.GREEN_HOUSE_RECIPES
                        )
                    )
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(QoLRecipeTypes.GREEN_HOUSE_RECIPES)
                    .workableTieredHullModel(com.yiran.minecraft.gtmqol.GTMQoL.id("block/machines/greenhouse"))
                    .tooltips(*tooltipList)
                    .register()
            },
            *GTMachineUtils.ELECTRIC_TIERS
        )
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLMachines object.
    }
}