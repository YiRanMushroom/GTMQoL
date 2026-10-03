package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.client.DTFRRingRender;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gear;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plateDouble;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

/**
 * Smart Assembly Factory and Dimensionally Transcendent Fusion Reactor, ported from the 7.x version, and the
 * Void Miner.
 */
public final class GTMQoLMultiblocks {

    private static final String DTFR_TOOLTIP_KEY = "gtmqol.multiblock.dimensionally_transcendent_fusion_reactor.tooltip";

    public static MachineEntry<MultiblockMachineDefinition> SMART_ASSEMBLY_FACTORY;
    public static MachineEntry<MultiblockMachineDefinition> DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR;
    public static MachineEntry<MultiblockMachineDefinition> VOID_MINER;

    private GTMQoLMultiblocks() {}

    public static void init() {
        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;
        if (config.smartAssemblyFactory) initSmartAssemblyFactory();
        if (config.dimensionallyTranscendentFusionReactor) initDTFR();
        if (config.voidMiner) initVoidMiner();
    }

    public static void addRecipes(RecipeOutput provider) {
        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;
        if (config.smartAssemblyFactory) addSmartAssemblyFactoryRecipe(provider);
        if (config.dimensionallyTranscendentFusionReactor) addDTFRRecipe(provider);
        if (config.voidMiner) addVoidMinerRecipe(provider);
    }

    private static void initSmartAssemblyFactory() {
        SMART_ASSEMBLY_FACTORY = GTMQoLAddon
                .multiblock("smart_assembly_factory", WorkableElectricMultiblockMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
                .recipeModifiers(PARALLEL_HATCH, OC_PERFECT_SUBTICK, BATCH_MODE)
                .appearanceBlock(CASING_STEEL_SOLID)
                .pattern(definition -> MultiblockPatternBuilder.start(RIGHT, UP, BACK)
                        .slice("XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX")
                        .sliceRepeatable(4, 4,
                                "XXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .slice("SXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .sliceRepeatable(4, 4,
                                "XXXXXXX", "RTTTTTR", "RAAAAAR", "GTTTTTG", "RAAAAAR", "RTTTTTR", "XXXXXXX")
                        .slice("XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(CASING_STEEL_SOLID.get())
                                .and(autoAbilities(definition.getRecipeTypes()))
                                .and(autoAbilities(true, false, true))
                                .and(dataHatchPredicate()))
                        .where('G', blocks(CASING_GRATE.get()))
                        .where('A', blocks(CASING_ASSEMBLY_CONTROL.get()))
                        .where('R', blocks(CASING_LAMINATED_GLASS.get()))
                        .where('T', blocks(CASING_ASSEMBLY_LINE.get()))
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/assembly_line"))
                .langValue("Smart Assembly Factory")
                .register();
    }

    private static void initDTFR() {
        DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR = GTMQoLAddon
                .multiblock("dimensionally_transcendent_fusion_reactor", DTFRMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.FUSION_RECIPES)
                .recipeModifiers(PARALLEL_HATCH, OC_PERFECT_SUBTICK, BATCH_MODE)
                .tooltips(Component.translatable(DTFR_TOOLTIP_KEY))
                .appearanceBlock(() -> FusionReactorMachine.getCasingState(UV))
                .pattern(definition -> {
                    var casing = blocks(FusionReactorMachine.getCasingState(UV));
                    return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                            .slice("###############", "######XGX######", "###############")
                            .slice("######XCX######", "####GGAAAGG####", "######XCX######")
                            .slice("####CC###CC####", "###EAAXGXAAE###", "####CC###CC####")
                            .slice("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                            .slice("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                            .slice("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                            .slice("#X###########X#", "XAX#########XAX", "#X###########X#")
                            .slice("#C###########C#", "GAG#########GAG", "#C###########C#")
                            .slice("#X###########X#", "XAX#########XAX", "#X###########X#")
                            .slice("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                            .slice("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                            .slice("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                            .slice("####CC###CC####", "###EAAXGXAAE###", "####CC###CC####")
                            .slice("######XCX######", "####GGAAAGG####", "######XCX######")
                            .slice("###############", "######XSX######", "###############")
                            .where('S', controller(blocks(definition.getBlock())))
                            .where('G', blocks(FUSION_GLASS.get()).or(casing))
                            .where('E', casing.and(abilities(PartAbility.INPUT_ENERGY,
                                    PartAbility.SUBSTATION_INPUT_ENERGY, PartAbility.INPUT_LASER)
                                    .setMinGlobalLimited(1).setPreviewCount(16)))
                            .where('C', casing)
                            .where('K', blocks(FusionReactorMachine.getCoilState(UV)))
                            .where('X', casing.and(abilities(PartAbility.EXPORT_FLUIDS))
                                    .and(abilities(PartAbility.IMPORT_FLUIDS))
                                    .and(autoAbilities(false, false, true)))
                            .where('A', air())
                            .where('#', any())
                            .build();
                })
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableCasingMachineModel(FusionReactorMachine.getCasingType(UV).getTexture(),
                        GTCEu.id("block/multiblock/fusion_reactor"))
                        .andThen(b -> b.addDynamicRenderer(DTFRRingRender::new)))
                .hasBER(true)
                .langValue("Dimensionally Transcendent Fusion Reactor")
                .register();

        GTMQoLAddon.registrate().addRawLang(DTFR_TOOLTIP_KEY,
                "Can run fusion recipes of any tier, as long as you have enough energy inputs.");
    }

    private static void initVoidMiner() {
        // Shaped like GTCEu's EV Large Miner, in steel.
        VOID_MINER = GTMQoLAddon
                .multiblock("void_miner", VoidMinerMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .tooltips(Component.translatable(VoidMinerMachine.KEY + "tooltip.0"),
                        Component.translatable(VoidMinerMachine.KEY + "tooltip.1"),
                        Component.translatable(VoidMinerMachine.KEY + "tooltip.2"))
                .appearanceBlock(CASING_STEEL_SOLID)
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                        .slice("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                        .slice("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(CASING_STEEL_SOLID.get())
                                .and(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1)))
                        .where('C', blocks(CASING_STEEL_SOLID.get()))
                        .where('F', frames(Steel))
                        .where('#', any())
                        .build())
                .allowExtendedFacing(true)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/large_miner"))
                        .andThen((ctx, prov, modelBuilder) -> {
                            // same as GTCEu's large miner: the formed model has the drill
                            modelBuilder.replaceForAllStates((state, models) -> {
                                if (!state.getValue(GTMachineModelProperties.IS_FORMED)) {
                                    return models;
                                }
                                var parentModel = prov.models()
                                        .getExistingFile(GTCEu.id("block/machine/large_miner_active"));
                                for (ConfiguredModel model : models) {
                                    ((BlockModelBuilder) model.model).parent(parentModel);
                                }
                                return models;
                            });
                        }))
                .langValue("Void Miner")
                .register();

        var lang = GTMQoLAddon.registrate();
        String key = VoidMinerMachine.KEY;
        lang.addRawLang(key + "tooltip.0",
                "Mines the ores of its dimension's GregTech veins, averaged by vein weight, out of thin air.");
        lang.addRawLang(key + "tooltip.1",
                "Paid from the bound wireless EU network: 1,000,000 EU per stack, all up front. Cycles take 15 s.");
        lang.addRawLang(key + "tooltip.2",
                "Each of 1-16 operations mines one ore, 1-16 stacks of it. Stopping finishes the current cycle.");
        lang.addRawLang(key + "status.stopped", "Stopped");
        lang.addRawLang(key + "status.starting", "Starting...");
        lang.addRawLang(key + "status.mining", "Mining: %s / %s s");
        lang.addRawLang(key + "status.output_full", "Output full: %s ores left, retry in %s s");
        lang.addRawLang(key + "status.no_network", "No wireless network bound");
        lang.addRawLang(key + "status.not_enough_eu", "Not enough EU in the network");
        lang.addRawLang(key + "status.no_ores", "No ore veins in this dimension");
        lang.addRawLang(key + "status.not_formed", "Structure not formed");
        lang.addRawLang(key + "settings", "%s × %s stacks, %s EU per cycle");
        lang.addRawLang(key + "preview", "Ores");
        lang.addRawLang(key + "stored", "Stored");
        lang.addRawLang(key + "stored.title", "Waiting for output");
        lang.addRawLang(key + "stored.empty", "Nothing stored");
        lang.addRawLang(key + "config", "Settings");
        lang.addRawLang(key + "running", "Running");
        lang.addRawLang(key + "stopped", "Stopped");
        lang.addRawLang(key + "preview.title", "Ore chances");
        lang.addRawLang(key + "preview.empty", "No ore veins in this dimension");
        lang.addRawLang(key + "config.title", "Settings (Shift: ±4)");
        lang.addRawLang(key + "config.operations", "Operations: %s");
        lang.addRawLang(key + "config.multiplier", "Stacks per ore: %s");
    }

    private static void addSmartAssemblyFactoryRecipe(RecipeOutput provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("smart_assembly_factory"))
                .inputItems(GTMultiMachines.ASSEMBLY_LINE, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 8)
                .inputItems(plateDouble, Osmiridium, 64)
                .inputItems(GTItems.FIELD_GENERATOR_ZPM, 16)
                .inputItems(GTItems.ELECTRIC_PUMP_UV, 8)
                .inputItems(GTItems.ROBOT_ARM_UV, 8)
                .inputFluids(Europium.getFluid(L * 64))
                .inputFluids(Polybenzimidazole.getFluid(L * 32))
                .inputFluids(Naquadria.getFluid(L * 16))
                .outputItems(SMART_ASSEMBLY_FACTORY)
                .duration(2000)
                .EUt(VA[ZPM])
                .save(provider);
    }

    private static void addDTFRRecipe(RecipeOutput provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("dimensionally_transcendent_fusion_reactor"))
                .inputItems(GTMultiMachines.FUSION_REACTOR[UV], 16)
                .inputItems(CustomTags.UHV_CIRCUITS, 8)
                .inputItems(plateDouble, Tritanium, 64)
                .inputItems(GTItems.FIELD_GENERATOR_UV, 16)
                .inputItems(FUSION_COIL.asItem(), 64)
                .inputItems(FUSION_CASING_MK3.asItem(), 64)
                .inputFluids(Duranium.getFluid(L * 64))
                .inputFluids(Polybenzimidazole.getFluid(L * 32))
                .inputFluids(Neutronium.getFluid(L * 16))
                .outputItems(DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR)
                .duration(2000)
                .EUt(VA[UV])
                .save(provider);
    }

    private static void addVoidMinerRecipe(RecipeOutput provider) {
        // Makeable at LV without chips (no GaAs in a skyblock).
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("void_miner"))
                .inputItems(GTMachines.MINER[LV], 4)
                .inputItems(CustomTags.LV_CIRCUITS, 16)
                .inputItems(GTItems.ELECTRIC_MOTOR_LV, 16)
                .inputItems(GTItems.ELECTRIC_PISTON_LV, 16)
                .inputItems(GTItems.CONVEYOR_MODULE_LV, 16)
                .inputItems(CASING_STEEL_SOLID.asItem(), 16)
                .inputItems(plateDouble, Steel, 64)
                .inputItems(gear, Steel, 16)
                .outputItems(VOID_MINER)
                .duration(1200)
                .EUt(VA[LV])
                .save(provider);
    }
}
