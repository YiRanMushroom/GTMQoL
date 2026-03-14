package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.Predicates.*
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import net.minecraft.network.chat.Component

object QoLMultiblocks {
    @JvmStatic
    var SMART_ASSEMBLY_FACTORY: MultiblockMachineDefinition? = null

    @JvmStatic
    var ELECTRIC_IMPLOSION_COMPRESSOR: MultiblockMachineDefinition? = null

    @JvmStatic
    var DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR: MultiblockMachineDefinition? = null

    @JvmStatic
    fun init() {
        var registrate = GTMQoLRegistrate.REGISTRATE

        if (ConfigHolder.instance.addonConfig.enableSmartAssemblyFactory) {

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
                            .where('S', Predicates.controller(blocks(definition.block)))
                            .where(
                                'X', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .or(Predicates.autoAbilities(*definition.recipeTypes))
                                    .or(Predicates.autoAbilities(true, false, true))
                                    .or(Predicates.dataHatchPredicate(blocks(GTBlocks.CASING_GRATE.get())))
                            )
                            .where('G', blocks(GTBlocks.CASING_GRATE.get()))
                            .where('A', blocks(GTBlocks.CASING_ASSEMBLY_CONTROL.get()))
                            .where('R', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                            .where('T', blocks(GTBlocks.CASING_ASSEMBLY_LINE.get()))
                            .build()
                    }
                    .workableCasingModel(
                        GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/assembly_line")
                    )
                    .register()
        }

        if (ConfigHolder.instance.addonConfig.enableElectricImplosionRecipes) {
            ELECTRIC_IMPLOSION_COMPRESSOR =
                registrate.multiblock(
                    "electric_implosion_compressor",
                    ::WorkableElectricMultiblockMachine
                ).rotationState(RotationState.ALL)
                    .recipeType(QoLRecipeTypes.ELECTRIC_IMPLOSION_RECIPES!!)
                    .recipeModifiers(
                        GTRecipeModifiers.PARALLEL_HATCH,
                        GTRecipeModifiers.OC_PERFECT_SUBTICK,
                        GTRecipeModifiers.BATCH_MODE
                    )
                    .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
                    .pattern { definition ->
                        FactoryBlockPattern.start()
                            .aisle("XXXXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                            .aisle("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                            .aisle("XXXXX", "#GAG#", "#GAG#", "#GAG#", "#GAG#", "XXXXX")
                            .aisle("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                            .aisle("XXSXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                            .where('S', controller(blocks(definition.get())))
                            .where(
                                'X',
                                blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .or(autoAbilities(*definition.recipeTypes))
                                    .or(autoAbilities(true, false, true))
                            )
                            .where('P', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where('G', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                            .where('F', blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                            .where('A', air())
                            .where('#', any())
                            .build();
                    }
                    .workableCasingModel(
                        GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                        GTCEu.id("block/multiblock/implosion_compressor")
                    )
                    .register()
        }

        if (ConfigHolder.instance.addonConfig.enableDimensionallyTranscendentFusionReactor) {
            DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR = GTMQoLRegistrate.REGISTRATE.multiblock(
                "dimensionally_transcendent_fusion_reactor",
                ::WorkableElectricMultiblockMachine
            )
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.FUSION_RECIPES)
                .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_PERFECT_SUBTICK, BATCH_MODE)
                .appearanceBlock { FusionReactorMachine.getCasingState(GTValues.UV) }
                .tooltips(Component.translatable("gtmqol.multiblock.dimensionally_transcendent_fusion_reactor.tooltip"))
                .pattern { definition ->
                    var casing = blocks(FusionReactorMachine.getCasingState(GTValues.UV))
                    FactoryBlockPattern.start()
                        .aisle("###############", "######OGO######", "###############")
                        .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                        .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                        .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                        .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                        .aisle("###############", "######OSO######", "###############")
                        .where('S', controller(blocks(definition.get())))
                        .where('G', blocks(FUSION_GLASS.get()).or(casing))
                        .where(
                            'E', casing.or(
                                abilities(PartAbility.INPUT_ENERGY)
                                    .setMinGlobalLimited(1).setPreviewCount(16)
                            )
                        )
                        .where('C', casing)
                        .where('K', blocks(FusionReactorMachine.getCoilState(3)))
                        .where('O', casing.or(abilities(PartAbility.EXPORT_FLUIDS)))
                        .where('A', air())
                        .where('I', casing.or(abilities(PartAbility.IMPORT_FLUIDS)))
                        .where('#', any())
                        .build()
                }

                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(
                    createWorkableCasingMachineModel(
                        FusionReactorMachine.getCasingType(GTValues.UV).texture,
                        GTCEu.id("block/multiblock/fusion_reactor")
                    )/*.andThen { b -> b.addDynamicRenderer(DynamicRenderHelper::createFusionRingRender) }*/
                )
//                .hasBER(true)
                .register()
        }
    }
}