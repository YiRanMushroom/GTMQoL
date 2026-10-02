package com.yiran.minecraft.gtmqol.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.assembler.MagicalAssembler;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

/** The advanced steam multiblocks ({@link AdvancedSteamMultiMachine}), their parallel hatch and the steam magical assembler. */
public final class AdvancedSteamMachines {

    private static final String TIER_TOOLTIP_KEY = "gtmqol.multiblock.advanced_steam.tooltip.tier";
    private static final String MODIFIER_TOOLTIP_KEY = "gtmqol.multiblock.advanced_steam.tooltip.modifier";
    private static final String PARALLEL_TOOLTIP_KEY = "gtmqol.multiblock.advanced_steam.tooltip.parallel";
    private static final String HATCH_TOOLTIP_KEY = "gtmqol.machine.steam_parallel_hatch.tooltip";

    public static final PartAbility STEAM_PARALLEL = new PartAbility("steam_parallel_hatch");

    public static MachineDefinition STEAM_PARALLEL_HATCH;
    public static Pair<MachineDefinition, MachineDefinition> STEAM_MAGICAL_ASSEMBLER;

    /** Multiblock and the machine it is made from in the magical assembler. */
    private record Entry(MultiblockMachineDefinition machine, Supplier<ItemStack> base) {}

    private static final List<Entry> MULTIBLOCKS = new ArrayList<>();

    private AdvancedSteamMachines() {}

    /** After {@link MagicalAssembler#initMachines()}, which this uses the recipe type of. */
    public static void init() {
        STEAM_PARALLEL_HATCH = GTMQoLAddon
                .machine("steam_parallel_hatch", SteamParallelHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                .abilities(STEAM_PARALLEL)
                .modelProperty(IS_FORMED, false)
                .colorOverlaySteamHullModel(GTCEu.id("block/machines/parallel_hatch_mk1/overlay_front"))
                .themeId(GTGuiTheme.BRONZE.getId())
                .langValue("Steam Parallel Control Hatch")
                .tooltips(Component.translatable(HATCH_TOOLTIP_KEY,
                        SteamParallelHatchPartMachine.MIN_PARALLEL, SteamParallelHatchPartMachine.MAX_PARALLEL),
                        Component.translatable("gtceu.part_sharing.disabled"))
                .register();

        STEAM_MAGICAL_ASSEMBLER = GTMachineUtils.registerSteamMachines(GTMQoLAddon.registrate(),
                "steam_magical_assembler", SimpleSteamMachine::new,
                (pressure, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .recipeType(MagicalAssembler.RECIPE_TYPE)
                        .recipeModifier(SimpleSteamMachine::recipeModifier)
                        .themeId(i -> i > 0 ? GTGuiTheme.STEEL.getId() : GTGuiTheme.BRONZE.getId())
                        .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                        .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                        .workableSteamHullModel(pressure, GTMQoL.id("block/machines/magical_assembler"))
                        .register());

        register("macerator", MACERATOR_RECIPES, MV, gtOverlay("macerator"),
                hp(GTMachines.STEAM_MACERATOR), AdvancedSteamShapes.MACERATOR);
        register("compressor", COMPRESSOR_RECIPES, MV, gtOverlay("compressor"),
                hp(GTMachines.STEAM_COMPRESSOR), AdvancedSteamShapes.COMPRESSOR);
        register("forge_hammer", FORGE_HAMMER_RECIPES, MV, gtOverlay("forge_hammer"),
                hp(GTMachines.STEAM_HAMMER), AdvancedSteamShapes.FORGE_HAMMER);
        register("extractor", EXTRACTOR_RECIPES, MV, gtOverlay("extractor"),
                hp(GTMachines.STEAM_EXTRACTOR), AdvancedSteamShapes.EXTRACTOR);
        register("alloy_smelter", ALLOY_SMELTER_RECIPES, MV, gtOverlay("alloy_smelter"),
                hp(GTMachines.STEAM_ALLOY_SMELTER), AdvancedSteamShapes.ALLOY_SMELTER);
        register("furnace", FURNACE_RECIPES, MV, gtOverlay("furnace"),
                hp(GTMachines.STEAM_FURNACE), AdvancedSteamShapes.FURNACE);
        register("bender", BENDER_RECIPES, MV, gtOverlay("bender"),
                lv(GTMachines.BENDER), AdvancedSteamShapes.BENDER);
        register("wiremill", WIREMILL_RECIPES, MV, gtOverlay("wiremill"),
                lv(GTMachines.WIREMILL), AdvancedSteamShapes.WIREMILL);
        register("lathe", LATHE_RECIPES, MV, gtOverlay("lathe"),
                lv(GTMachines.LATHE), AdvancedSteamShapes.LATHE);
        register("cutter", CUTTER_RECIPES, MV, gtOverlay("cutter"),
                lv(GTMachines.CUTTER), AdvancedSteamShapes.CUTTER);
        register("extruder", EXTRUDER_RECIPES, MV, gtOverlay("extruder"),
                lv(GTMachines.EXTRUDER), AdvancedSteamShapes.EXTRUDER);
        register("forming_press", FORMING_PRESS_RECIPES, MV, gtOverlay("forming_press"),
                lv(GTMachines.FORMING_PRESS), AdvancedSteamShapes.FORMING_PRESS);
        register("mixer", MIXER_RECIPES, MV, gtOverlay("mixer"),
                lv(GTMachines.MIXER), AdvancedSteamShapes.MIXER);
        register("centrifuge", CENTRIFUGE_RECIPES, MV, gtOverlay("centrifuge"),
                lv(GTMachines.CENTRIFUGE), AdvancedSteamShapes.CENTRIFUGE);
        register("thermal_centrifuge", THERMAL_CENTRIFUGE_RECIPES, MV, gtOverlay("thermal_centrifuge"),
                lv(GTMachines.THERMAL_CENTRIFUGE), AdvancedSteamShapes.THERMAL_CENTRIFUGE);
        register("ore_washer", ORE_WASHER_RECIPES, MV, gtOverlay("ore_washer"),
                lv(GTMachines.ORE_WASHER), AdvancedSteamShapes.ORE_WASHER);
        register("chemical_bath", CHEMICAL_BATH_RECIPES, MV, gtOverlay("chemical_bath"),
                lv(GTMachines.CHEMICAL_BATH), AdvancedSteamShapes.CHEMICAL_BATH);
        register("sifter", SIFTER_RECIPES, MV, gtOverlay("sifter"),
                lv(GTMachines.SIFTER), AdvancedSteamShapes.SIFTER);
        register("assembler", ASSEMBLER_RECIPES, MV, gtOverlay("assembler"),
                lv(GTMachines.ASSEMBLER), AdvancedSteamShapes.MANUFACTURER);
        // MV makes the ULV-HV control circuits (see ControlCircuits).
        register("circuit_assembler", CIRCUIT_ASSEMBLER_RECIPES, MV, gtOverlay("circuit_assembler"),
                lv(GTMachines.CIRCUIT_ASSEMBLER), AdvancedSteamShapes.CIRCUIT_ASSEMBLER);
        register("magical_assembler", MagicalAssembler.RECIPE_TYPE, LV,
                GTMQoL.id("block/machines/magical_assembler"), hp(STEAM_MAGICAL_ASSEMBLER),
                AdvancedSteamShapes.MANUFACTURER);

        GTMQoLAddon.registrate().addRawLang(TIER_TOOLTIP_KEY,
                "Runs recipes up to %s, non-perfectly overclocked to that tier");
        GTMQoLAddon.registrate().addRawLang(MODIFIER_TOOLTIP_KEY,
                "Recipes take 80%% of the time and 75%% of the steam per tick");
        GTMQoLAddon.registrate().addRawLang(PARALLEL_TOOLTIP_KEY,
                "%s parallels, up to %s with a Steam Parallel Control Hatch");
        GTMQoLAddon.registrate().addRawLang(HATCH_TOOLTIP_KEY,
                "Sets the parallels of an advanced steam multiblock, from %s to %s");
    }

    private static ResourceLocation gtOverlay(String machine) {
        return GTCEu.id("block/machines/" + machine);
    }

    private static Supplier<ItemStack> hp(Pair<MachineDefinition, MachineDefinition> steamMachine) {
        return () -> steamMachine.right().asStack(4);
    }

    private static Supplier<ItemStack> lv(MachineDefinition[] machines) {
        return () -> machines[LV].asStack();
    }

    /** {@code shape} is one of {@link AdvancedSteamShapes}. */
    private static void register(String name, GTRecipeType recipeType, int maxRecipeTier, ResourceLocation overlay,
                                 Supplier<ItemStack> base, String[][] shape) {
        MultiblockMachineDefinition machine = GTMQoLAddon
                .multiblock("large_steam_" + name, info -> new AdvancedSteamMultiMachine(info, maxRecipeTier))
                .rotationState(RotationState.ALL)
                .appearanceBlock(CASING_BRONZE_BRICKS)
                .themeId(GTGuiTheme.BRONZE.getId())
                .recipeType(recipeType)
                .recipeModifier(AdvancedSteamMultiMachine::recipeModifier, true)
                .tooltips(Component.translatable(TIER_TOOLTIP_KEY, VN[maxRecipeTier]),
                        Component.translatable(MODIFIER_TOOLTIP_KEY),
                        Component.translatable(PARALLEL_TOOLTIP_KEY, AdvancedSteamMultiMachine.DEFAULT_PARALLELS,
                                SteamParallelHatchPartMachine.MAX_PARALLEL))
                .pattern(definition -> {
                    MultiblockPatternBuilder builder = MultiblockPatternBuilder.start();
                    int casings = 0;
                    for (String[] slice : shape) {
                        builder.slice(slice);
                        for (String row : slice) {
                            casings += (int) row.chars().filter(c -> c == 'X').count();
                        }
                    }
                    return builder
                            .where('S', controller(blocks(definition.getBlock())))
                            // Regular buses and hatches of any tier, as well as steam buses.
                            .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(casings / 2)
                                    .and(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true,
                                            true))
                                    .and(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .and(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .and(abilities(PartAbility.STEAM).setExactLimit(1))
                                    .and(abilities(STEAM_PARALLEL).setMaxGlobalLimited(1)))
                            .where('G', blocks(CASING_BRONZE_GEARBOX.get()))
                            .where('P', blocks(CASING_BRONZE_PIPE.get()))
                            .where('F', frames(Bronze))
                            .where('B', blocks(FIREBOX_BRONZE.get()))
                            .where('H', blocks(BRONZE_HULL.get()))
                            .where('K', blocks(BRONZE_BRICKS_HULL.get()))
                            .where('Z', blocks(ChemicalHelper.getBlock(block, Bronze)))
                            .where('L', blockTag(Tags.Blocks.GLASS))
                            .where('I', blocks(Blocks.IRON_BLOCK))
                            .where('D', blocks(Blocks.DIAMOND_BLOCK))
                            .where('T', blocks(Blocks.STONE_BRICKS))
                            .build();
                })
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), overlay)
                .langValue("Large Steam " + FormattingUtil.toEnglishName(name))
                .register();
        MULTIBLOCKS.add(new Entry(machine, base));
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTMQoL.id("lp_steam_magical_assembler"),
                STEAM_MAGICAL_ASSEMBLER.left().asStack(), "PXP", "CMC", "PXP",
                'M', BRONZE_HULL.asStack(),
                'C', Blocks.CRAFTING_TABLE,
                'P', new MaterialEntry(plate, Bronze),
                'X', new MaterialEntry(pipeSmallFluid, Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTMQoL.id("hp_steam_magical_assembler"),
                STEAM_MAGICAL_ASSEMBLER.right().asStack(), "WSW", "WMW", "WPW",
                'M', STEAM_MAGICAL_ASSEMBLER.left().asStack(),
                'S', new MaterialEntry(plate, Steel),
                'W', new MaterialEntry(plate, WroughtIron),
                'P', new MaterialEntry(pipeSmallFluid, TinAlloy));

        for (Entry entry : MULTIBLOCKS) {
            MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id(entry.machine().getName()))
                    .inputItems(entry.base().get())
                    .inputItems(CASING_BRONZE_GEARBOX.asItem(), 4)
                    .inputItems(CASING_BRONZE_BRICKS.asItem(), 8)
                    .inputItems(plate, Bronze, 16)
                    .inputItems(gear, Bronze, 4)
                    .inputItems(CustomTags.ULV_CIRCUITS, 4)
                    .outputItems(entry.machine())
                    .duration(400)
                    .EUt(VA[ULV])
                    .save(provider);
        }

        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("steam_parallel_hatch"))
                .inputItems(GTMachines.STEAM_HATCH)
                .inputItems(CASING_BRONZE_GEARBOX.asItem(), 2)
                .inputItems(plate, Bronze, 8)
                .inputItems(gear, Bronze, 4)
                .inputItems(CustomTags.LV_CIRCUITS, 4)
                .outputItems(STEAM_PARALLEL_HATCH)
                .duration(400)
                .EUt(VA[ULV])
                .save(provider);
    }
}
