package com.yiran.minecraft.gtmqol.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.client.DTFRRingRender;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plateDouble;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

/** Smart Assembly Factory and Dimensionally Transcendent Fusion Reactor, ported from the 7.x version. */
public final class GTMQoLMultiblocks {

    private static final String DTFR_TOOLTIP_KEY = "gtmqol.multiblock.dimensionally_transcendent_fusion_reactor.tooltip";

    public static MultiblockMachineDefinition SMART_ASSEMBLY_FACTORY;
    public static MultiblockMachineDefinition DIMENSIONALLY_TRANSCENDENT_FUSION_REACTOR;

    private GTMQoLMultiblocks() {}

    public static void init() {
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

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
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
}
