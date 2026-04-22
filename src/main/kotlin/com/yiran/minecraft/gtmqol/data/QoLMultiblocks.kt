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
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTBlocks.CASING_PTFE_INERT
import com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine
import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull
import com.yiran.minecraft.gtmqol.common.multiblocks.PCBFactoryMachine
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Blocks

object QoLMultiblocks {
    @JvmStatic
    var SMART_ASSEMBLY_FACTORY: MultiblockMachineDefinition? = null

    @JvmStatic
    var ELECTRIC_IMPLOSION_COMPRESSOR: MultiblockMachineDefinition? = null

    @JvmStatic
    var DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR: MultiblockMachineDefinition? = null

    @JvmStatic
    var GREENHOUSE: MultiblockMachineDefinition? = null

    @JvmStatic
    var PCB_FACTORY: MultiblockMachineDefinition? = null

    @JvmStatic
    var INDUSTRIAL_LARGE_CHEMICAL_REACTOR: MultiblockMachineDefinition? = null

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
                        .aisle("###############", "######XGX######", "###############")
                        .aisle("######XCX######", "####GGAAAGG####", "######XCX######")
                        .aisle("####CC###CC####", "###EAAXGXAAE###", "####CC###CC####")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("#X###########X#", "XAX#########XAX", "#X###########X#")
                        .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                        .aisle("#X###########X#", "XAX#########XAX", "#X###########X#")
                        .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                        .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                        .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                        .aisle("####CC###CC####", "###EAAXGXAAE###", "####CC###CC####")
                        .aisle("######XCX######", "####GGAAAGG####", "######XCX######")
                        .aisle("###############", "######XSX######", "###############")
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
                        .where('X', casing.or(abilities(PartAbility.EXPORT_FLUIDS).or(abilities(PartAbility.IMPORT_FLUIDS)).or(autoAbilities(false, false, true))))
                        .where('A', air())
                        .where('#', any())
                        .build()
                }

                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(
                    createWorkableCasingMachineModel(
                        FusionReactorMachine.getCasingType(GTValues.UV).texture,
                        GTCEu.id("block/multiblock/fusion_reactor")
                    )
                )
                .register()
        }

        if (ConfigHolder.instance.addonConfig.enableGreenhouse) {
            GREENHOUSE = registrate.multiblock("greenhouse", ::WorkableElectricMultiblockMachine)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(QoLRecipeTypes.GREENHOUSE_RECIPES.asNotNull())
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern { definition ->
                    FactoryBlockPattern.start()
                        .aisle("CCC", "CGC", "CGC", "CLC", "CCC")
                        .aisle("CMC", "G#G", "G#G", "LIL", "COC")
                        .aisle("CKC", "CGC", "CGC", "CLC", "CNC")
                        .where('K', controller(blocks(definition.get())))
                        .where(
                            'M', blocks(Blocks.MOSS_BLOCK)
                                .or(blocks(Blocks.DIRT))
                                .or(blocks(Blocks.GRASS_BLOCK))
                        )
                        .where('G', blocks(Blocks.GLASS))
                        .where('I', blocks(Blocks.GLOWSTONE))
                        .where('L', blocks(GTBlocks.CASING_GRATE.get()))
                        .where(
                            'C', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                .or(autoAbilities(*definition.getRecipeTypes()))
                        )
                        .where(
                            'O', abilities(PartAbility.MUFFLER)
                                .setExactLimit(1)
                        )
                        .where('N', abilities(PartAbility.MAINTENANCE))
                        .where('#', air())
                        .build()
                }
                .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTMQoL.id("block/multiblock/greenhouse")
                )
                .register()
        }

        if (ConfigHolder.instance.addonConfig.enablePCBFactory) {
            PCB_FACTORY = PCBFactoryMachine.createDefinition("pcb_factory")
        }

        if (ConfigHolder.instance.addonConfig.additionalGCYMMachinesAndAdditionalRecipes) {
            INDUSTRIAL_LARGE_CHEMICAL_REACTOR = registrate.multiblock(
                "industrial_large_chemical_reactor",
                ::WorkableElectricMultiblockMachine
            )
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
                .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
                .tooltips(Component.translatable("gtmqol.multiblock.industrial_large_chemical_reactor.tooltip0"))
                .tooltips(
                    Component.translatable(
                        "gtceu.machine.available_recipe_map_1.tooltip",
                        Component.translatable("gtceu.large_chemical_reactor")
                    )
                )
                .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, { _, _ ->
                    ModifierFunction.builder().durationMultiplier(1.0 / 64.0).build()
                }, GTRecipeModifiers.OC_PERFECT_SUBTICK, BATCH_MODE)
                .appearanceBlock(CASING_PTFE_INERT)
                .pattern { definition ->
                    FactoryBlockPattern.start()
                        .aisle("XXXXX", "XGGGX", "XG@GX", "XGGGX", "XXXXX")
                        .aisleRepeatable(5, 5, "XPXPX", "GAAAG", "GAHAG", "GAAAG", "XPXPX")
                        .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                        .where('@', controller(blocks(definition.block)))
                        .where('X', blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(16)
                            .or(autoAbilities(*definition.recipeTypes))
                            .or(autoAbilities(true, false, true))
                        ).build()
                }
                .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor")
                )
                .register()
        }
    }
}