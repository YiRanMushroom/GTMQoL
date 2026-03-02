package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.ae2PresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import java.util.Locale.getDefault


object QoLMachines {
    @JvmField
    var GREENHOUSE: Array<MachineDefinition>? = null

    @JvmField
    var ME_ASSEMBLER: Array<MachineDefinition>? = null

    @JvmField
    var ME_CIRCUIT_SLICER: Array<MachineDefinition>? = null

    fun registerSimpleMachine(
        name: String,
        recipeType: GTRecipeType,
        displayNameTransformer: ((String) -> String)? = null
    ): Array<MachineDefinition> {
        // display name should remove _ and capitalize each word
        val displayName = displayNameTransformer?.invoke(name) ?: name.split("_").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    getDefault()
                ) else it.toString()
            }
        }
        return GTMachineUtils.registerTieredMachines(
            GTMQoLRegistrate.REGISTRATE,
            name,
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
                    recipeType,
                    GTMachineUtils.defaultTankSizeFunction.applyAsInt(tier).toLong(),
                    true
                )

                builder.langValue("${GTValues.VLVH[tier]} $displayName ${GTValues.VLVT[tier]}")
                    .editableUI(
                        SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(
                            com.yiran.minecraft.gtmqol.GTMQoL.id(name),
                            recipeType
                        )
                    )
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(recipeType)
                    .workableTieredHullModel(com.yiran.minecraft.gtmqol.GTMQoL.id("block/machines/$name"))
                    .tooltips(*tooltipList)
                    .register()
            },
            *GTMachineUtils.ELECTRIC_TIERS
        )
    }

    init {
        if (ConfigHolder.instance.addonConfig.enableGreenhouse) {
            GREENHOUSE = registerSimpleMachine("greenhouse", QoLRecipeTypes.GREEN_HOUSE_RECIPES!!)
        }

        if (ae2PresentedAndIntegrationEnabled()) {
            ME_ASSEMBLER = registerSimpleMachine("me_assembler", QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!) {
                "ME Assembler"
            }
            ME_CIRCUIT_SLICER = registerSimpleMachine("me_circuit_slicer", QoLRecipeTypes.ME_CIRCUIT_SLICER_RECIPES!!) {
                "ME Circuit Slicer"
            }

        }
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLMachines object.
    }
}