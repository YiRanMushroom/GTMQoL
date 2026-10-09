package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
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
import com.yiran.minecraft.gtmqol.client.FishingPondWaterRender;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;

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
 * Smart Assembly Factory and Dimensionally Transcendent Fusion Reactor, ported from the 7.x version, the
 * Void Miner and the Industrial Fishing Pond.
 */
public final class GTMQoLMultiblocks {

    private static final String DTFR_TOOLTIP_KEY = "gtmqol.multiblock.dimensionally_transcendent_fusion_reactor.tooltip";

    public static MultiblockMachineDefinition SMART_ASSEMBLY_FACTORY;
    public static MultiblockMachineDefinition DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR;
    public static MultiblockMachineDefinition VOID_MINER;
    public static MultiblockMachineDefinition FISHING_POND;

    private GTMQoLMultiblocks() {}

    public static void init() {
        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;
        if (config.smartAssemblyFactory) initSmartAssemblyFactory();
        if (config.dimensionallyTranscendentFusionReactor) initDTFR();
        if (config.voidMiner) initVoidMiner();
        if (config.fishingPond) initFishingPond();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        GTMQoLConfig.Machines config = GTMQoLConfig.get().machines;
        if (config.smartAssemblyFactory) addSmartAssemblyFactoryRecipe(provider);
        if (config.dimensionallyTranscendentFusionReactor) addDTFRRecipe(provider);
        if (config.voidMiner) addVoidMinerRecipe(provider);
        if (config.fishingPond) addFishingPondRecipe(provider);
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
        addCyclicMachineLang(key, "Mining: %s / %s s", "Output full: %s ores left");
        lang.addRawLang(key + "status.no_network", "No wireless network bound");
        lang.addRawLang(key + "status.not_enough_eu", "Not enough EU in the network");
        lang.addRawLang(key + "status.no_ores", "No ore veins in this dimension");
        lang.addRawLang(key + "settings", "%s × %s stacks, %s EU per cycle");
        lang.addRawLang(key + "preview", "Ores");
        lang.addRawLang(key + "config", "Settings");
        lang.addRawLang(key + "preview.title", "Ore chances");
        lang.addRawLang(key + "preview.empty", "No ore veins in this dimension");
        lang.addRawLang(key + "config.title", "Settings (Shift: ±4)");
        lang.addRawLang(key + "config.operations", "Operations: %s");
        lang.addRawLang(key + "config.multiplier", "Stacks per ore: %s");
    }

    private static void initFishingPond() {
        // GTCEu's Large Chemical Bath, widened: a 5 × 5 × 4 cavity (vanilla's open water check), open on top.
        FISHING_POND = GTMQoLAddon
                .multiblock("fishing_pond", FishingPondMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .tooltips(Component.translatable(FishingPondMachine.KEY + "tooltip.0"),
                        Component.translatable(FishingPondMachine.KEY + "tooltip.1"),
                        Component.translatable(FishingPondMachine.KEY + "tooltip.2"),
                        Component.translatable(FishingPondMachine.KEY + "tooltip.3"))
                .appearanceBlock(GCYMBlocks.CASING_WATERTIGHT)
                // startOffset is the controller -> first block of the first slice: the back wall is 6 slices behind
                // (1 + 5 repeats), S is in string 1 and char 3. Without it gtceu guesses from the controller's slice
                // index, which counts the repeatable slice once.
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .startOffset(OriginOffset.of(BACK, 6).move(DOWN, 1).move(LEFT, 3))
                        .slice("XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX")
                        .sliceRepeatable(5, 5, "XXXXXXX", "X     X", "X     X", "X     X", "X     X")
                        .slice("XXXXXXX", "XXXSXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(GCYMBlocks.CASING_WATERTIGHT.get()).setMinGlobalLimited(100)
                                .and(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                                .and(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setPreviewCount(1)))
                        .where(' ', air())
                        .build())
                .hasBER(true)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                        GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
                        .andThen(b -> b.addDynamicRenderer(FishingPondWaterRender::new)))
                .langValue("Industrial Fishing Pond")
                .register();

        var lang = GTMQoLAddon.registrate();
        String key = FishingPondMachine.KEY;
        lang.addRawLang(key + "tooltip.0",
                "Fishes vanilla fishing loot with the rod in its controller, every 5 s. The rod takes no damage.");
        lang.addRawLang(key + "tooltip.1",
                "1,000 EU per fish, 4× in treasure mode (open water: treasure too). Lure makes fish cheaper, " +
                        "Luck of the Sea works as usual.");
        lang.addRawLang(key + "tooltip.2",
                "Buffers up to 5 s of its energy hatches' input and spends all it can each cycle: " +
                        "every roll is multiplied by how many times the buffer pays for all of them.");
        lang.addRawLang(key + "tooltip.3", "Never loses progress for lack of power.");
        addCyclicMachineLang(key, "Fishing: %s / %s s", "Output full: %s items left");
        lang.addRawLang(key + "status.no_rod", "No fishing rod");
        lang.addRawLang(key + "buffer", "Buffer: %s / %s EU");
        lang.addRawLang(key + "settings", "%s rolls per cycle, %s");
        lang.addRawLang(key + "mode.normal", "fish and junk");
        lang.addRawLang(key + "mode.treasure", "treasure mode");
        lang.addRawLang(key + "rod", "Fishing rod");
        lang.addRawLang(key + "config", "Settings");
        lang.addRawLang(key + "treasure.on", "Treasure mode: on (4× EU)");
        lang.addRawLang(key + "treasure.off", "Treasure mode: off");
        lang.addRawLang(key + "config.title", "Settings (Shift: ±8)");
        lang.addRawLang(key + "config.rolls", "Rolls per cycle: %s");
    }

    /** The lang keys {@link CyclicMultiblockMachine} uses. */
    private static void addCyclicMachineLang(String key, String working, String outputFull) {
        var lang = GTMQoLAddon.registrate();
        lang.addRawLang(key + "status.stopped", "Stopped");
        lang.addRawLang(key + "status.starting", "Starting...");
        lang.addRawLang(key + "status.working", working);
        lang.addRawLang(key + "status.output_full", outputFull);
        lang.addRawLang(key + "status.not_formed", "Structure not formed");
        lang.addRawLang(key + "stored", "Stored");
        lang.addRawLang(key + "stored.title", "Waiting for output");
        lang.addRawLang(key + "stored.empty", "Nothing stored");
        lang.addRawLang(key + "running", "Running");
        lang.addRawLang(key + "stopped", "Stopped");
    }

    private static void addSmartAssemblyFactoryRecipe(Consumer<FinishedRecipe> provider) {
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

    private static void addDTFRRecipe(Consumer<FinishedRecipe> provider) {
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

    private static void addVoidMinerRecipe(Consumer<FinishedRecipe> provider) {
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

    private static void addFishingPondRecipe(Consumer<FinishedRecipe> provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("fishing_pond"))
                .inputItems(GTMachines.FISHER[EV], 4)
                .inputItems(CustomTags.EV_CIRCUITS, 8)
                .inputItems(GTItems.ELECTRIC_PUMP_EV, 8)
                .inputItems(GTItems.ROBOT_ARM_EV, 4)
                .inputItems(GCYMBlocks.CASING_WATERTIGHT.asItem(), 32)
                .inputItems(plateDouble, Titanium, 32)
                .inputItems(GTMachines.RESERVOIR_HATCH)
                .outputItems(FISHING_POND)
                .duration(1200)
                .EUt(VA[EV])
                .save(provider);
    }
}
