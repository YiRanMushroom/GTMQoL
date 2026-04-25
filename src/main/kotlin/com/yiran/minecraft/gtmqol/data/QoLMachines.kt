package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.GTValues.LuV
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.ae2PresentedAndIntegrationEnabled
import com.yiran.minecraft.gtmqol.api.RecipeModifierPartMachines
import com.yiran.minecraft.gtmqol.common.multiblocks.parts.ProbableCertaintyDevice
import com.yiran.minecraft.gtmqol.common.multiblocks.parts.ProbableImprobabilityDevice
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.integration.ae2.AbstractMEPatternBufferPartMachine
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair
import net.minecraft.network.chat.Component
import java.util.Locale.getDefault


object QoLMachines {
    @JvmField
    var GREENHOUSE: Array<MachineDefinition>? = null

    @JvmField
    var ME_ASSEMBLER: Array<MachineDefinition>? = null

    @JvmField
    var ME_CIRCUIT_SLICER: Array<MachineDefinition>? = null

    @JvmField
    var MAGICAL_ASSEMBLER: Array<MachineDefinition>? = null

    @JvmField
    var HIGH_AMP_LASERS: HashMap<Int, Pair<Array<MachineDefinition>, Array<MachineDefinition>>>? = null

    @JvmField
    var PROBABLE_IMPROBABILITY_DEVICE: MachineDefinition? = null

    @JvmField
    var PROBABLE_CERTAINTY_DEVICE: MachineDefinition? = null

    @JvmField
    var OVERCLOCKED_ME_PATTERN_BUFFER: MachineDefinition? = null

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
            GREENHOUSE = registerSimpleMachine("greenhouse", QoLRecipeTypes.GREENHOUSE_RECIPES!!)
        }

        if (ae2PresentedAndIntegrationEnabled()) {
            ME_ASSEMBLER = registerSimpleMachine("me_assembler", QoLRecipeTypes.ME_ASSEMBLER_RECIPES!!) {
                "ME Assembler"
            }
            ME_CIRCUIT_SLICER = registerSimpleMachine("me_circuit_slicer", QoLRecipeTypes.ME_CIRCUIT_SLICER_RECIPES!!) {
                "ME Circuit Slicer"
            }
        }

        MAGICAL_ASSEMBLER = registerSimpleMachine("magical_assembler", QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!)

        if (ConfigHolder.instance.addonConfig.enableMachinePartModifiers) {
            PROBABLE_IMPROBABILITY_DEVICE = GTMQoLRegistrate.REGISTRATE
                .machine("probable_improbability_device", ::ProbableImprobabilityDevice)
                .langValue("Probable Improbability Device")
                .rotationState(RotationState.ALL)
                .abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER)
//                .tooltips(
//                    Component.translatable("gtmqol.machine.probable_improbability_device.tooltip"),
//                )
                .tier(GTValues.IV)
                .colorOverlayTieredHullModel(GTMQoL.id("block/overlay/machine/probable_improbability_device"))
                .register()

            PROBABLE_CERTAINTY_DEVICE = GTMQoLRegistrate.REGISTRATE
                .machine("probable_certainty_device", ::ProbableCertaintyDevice)
                .langValue("Probable Certainty Device")
                .rotationState(RotationState.ALL)
                .abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER)
//                .tooltips(
//                    Component.translatable("gtmqol.machine.probable_certainty_device.tooltip"),
//                )
                .tier(GTValues.ZPM)
                .colorOverlayTieredHullModel(GTMQoL.id("block/overlay/machine/probable_certainty_device"))
                .register()
        }

        OVERCLOCKED_ME_PATTERN_BUFFER = GTMQoLRegistrate.REGISTRATE
            .machine("overclocked_me_pattern_buffer") { args ->
                object : AbstractMEPatternBufferPartMachine(args) {
                    override fun getPatternGridSize(): IntIntImmutablePair {
                        return IntIntImmutablePair.of(12, 18)
                    }

                    override fun createSharedFluidTank(): NotifiableFluidTank {
                        return NotifiableFluidTank(this, 4, Integer.MAX_VALUE, IO.IN, IO.NONE)
                    }

                    override fun createSharedItemStackHandler(): NotifiableItemStackHandler {
                        return NotifiableItemStackHandler(this, 4, IO.IN, IO.NONE)
                    }
                }
            }
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(
                PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                PartAbility.EXPORT_FLUIDS,
                PartAbility.EXPORT_ITEMS
            )
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
            .langValue("Overclocked ME Pattern Buffer")
            .tooltips(
                Component.translatable("block.gtceu.pattern_buffer.desc.0"),
                Component.translatable("block.gtceu.pattern_buffer.desc.1"),
                Component.translatable("block.gtceu.pattern_buffer.desc.2"),
                Component.translatable("gtceu.part_sharing.enabled")
            )
            .register()
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLMachines object.
    }
}