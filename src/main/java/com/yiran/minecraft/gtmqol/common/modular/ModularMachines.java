package com.yiran.minecraft.gtmqol.common.modular;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.api.GTValues.VN;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

/**
 * A 3x3x3 multiblock version of every tiered single-block machine with recipe types.
 *
 * <p>{@code MachineBuilderMixin} hands every registered machine here; the first electric-tier (≥ LV) one with each
 * set of recipe types gets the modular machine, registered right away and named after it without the
 * {@code lv_}/{@code mv_}/... prefix (machines without that prefix are skipped). That covers {@code GTMachineUtils.registerTieredMachines}, KubeJS tiered
 * machines and addons looping their tiers themselves.</p>
 */
public final class ModularMachines {

    private record Entry(MachineDefinition simple, MultiblockMachineDefinition modular) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Set<Set<GTRecipeType>> RECIPE_TYPES = new HashSet<>();
    private static final Set<String> NAMES = new HashSet<>();

    private static final RecipeModifier GENERATOR_OVERCLOCK = GTRecipeModifiers.ELECTRIC_OVERCLOCK
            .apply(OverclockingLogic.create(0.5, 4.0, true));
    private static final ResourceLocation BASE_CASING = GTCEu.id("block/casings/solid/machine_casing_solid_steel");

    private ModularMachines() {}

    public static void register(MachineDefinition simple) {
        if (simple instanceof MultiblockMachineDefinition) return;
        int tier = simple.getTier();
        GTRecipeType[] recipeTypes = simple.getRecipeTypes();
        if (tier < LV || recipeTypes == null || recipeTypes.length == 0) return;
        // Electric machines are named <voltage>_<name>; this also keeps out hp_ steam machines, which are tier 1 (LV).
        String namespace = simple.getId().getNamespace();
        String prefix = VN[tier].toLowerCase(Locale.ROOT) + "_";
        if (!simple.getId().getPath().startsWith(prefix)) return;
        String name = simple.getId().getPath().substring(prefix.length());
        if (!RECIPE_TYPES.add(Set.copyOf(Arrays.asList(recipeTypes)))) return;

        if (!GTMQoLConfig.get().modularMachines.enabled) return;
        if (FMLLoader.getLaunchHandler().isData()) return;
        if (Arrays.asList(recipeTypes).contains(GTRecipeTypes.DUMMY_RECIPES)) return;

        boolean generator = Arrays.stream(recipeTypes).allMatch(type -> "generator".equals(type.group));
        if (!generator && Arrays.stream(recipeTypes).anyMatch(type -> "generator".equals(type.group))) return;

        String modularName = "modular_" +
                (namespace.equals(GTCEu.MOD_ID) || namespace.equals(GTMQoL.MOD_ID) ? name : namespace + "_" + name);
        if (!NAMES.add(modularName)) {
            GTMQoL.LOGGER.warn("Machine {} has new recipe types but {} already exists, it gets no modular machine",
                    simple.getId(), modularName);
            return;
        }

        RecipeModifier startModifier = (machine, recipe) -> ModifierFunction.builder()
                .durationMultiplier(generator ? 8.0 : 0.125)
                .build();

        MultiblockMachineDefinition modular = GTMQoLAddon.multiblock(modularName, ModularMachine::new)
                .rotationState(RotationState.ALL)
                .recipeTypes(recipeTypes)
                .recipeModifiers(startModifier,
                        generator ? GENERATOR_OVERCLOCK : GTRecipeModifiers.OC_PERFECT_SUBTICK,
                        GTRecipeModifiers.BATCH_MODE)
                .generator(generator)
                .regressWhenWaiting(!generator)
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXX", "XXX", "XXX")
                        .slice("XXX", "XXX", "XXX")
                        .slice("XXX", "XSX", "XXX")
                        .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                        .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get(), Blocks.GLASS,
                                        GTBlocks.CASING_TEMPERED_GLASS.get()).setPreviewCount(100)
                                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.any()))
                        .build())
                .workableCasingModel(BASE_CASING, overlay(namespace, name, generator))
                .langValue("Modular " + FormattingUtil.toEnglishName(name))
                .dynamicallyGenerated(true)
                .register();

        ENTRIES.add(new Entry(simple, modular));
    }

    /** The single-block machine's own overlay if it has a front one, else a gtceu multiblock's. */
    private static ResourceLocation overlay(String namespace, String name, boolean generator) {
        var modFile = ModList.get().getModFileById(namespace);
        for (String dir : new String[] { "machines", "generators" }) {
            if (modFile != null && Files.exists(modFile.getFile()
                    .findResource("assets", namespace, "textures", "block", dir, name, "overlay_front.png"))) {
                return ResourceLocation.fromNamespaceAndPath(namespace, "block/" + dir + "/" + name);
            }
        }
        return generator ? GTCEu.id("block/multiblock/generator/large_combustion_engine") :
                GTCEu.id("block/multiblock/implosion_compressor");
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        for (Entry entry : ENTRIES) {
            String path = entry.modular.getId().getPath();
            MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("convert_to_" + path))
                    .inputItems(entry.simple.asStack())
                    .outputItems(entry.modular.asStack())
                    .circuitMeta(5)
                    .EUt(VA[LV])
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("hammer_convert_to_" + path),
                    entry.modular.asStack(), "h", "M", 'M', entry.simple.asStack());
        }
    }
}
