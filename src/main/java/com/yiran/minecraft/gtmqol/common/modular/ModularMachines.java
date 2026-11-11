package com.yiran.minecraft.gtmqol.common.modular;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.GregificationModifiers;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.api.GTValues.VN;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

/**
 * A 3x3x3 multiblock version of every tiered single-block machine with recipe types.
 *
 * <p>{@code MachineBuilderMixin} hands every registered machine here; the first electric-tier (≥ LV) one with each
 * set of recipe types gets the modular machine, named after it without the {@code lv_}/{@code mv_}/... prefix
 * (machines without that prefix are skipped).
 * That covers
 * {@code GTMachineUtils.registerTieredMachines}, KubeJS tiered machines and addons looping their tiers themselves.</p>
 *
 * <p>On 1.21 every machine is a deferred Registrate entry, so they are only queued here in registration order (gtceu's
 * own are queued before our config even exists). The multiblocks are declared at the start of the machine registry
 * event, before our registrate creates its machines. GTCEu registers recipe types after the machines, so nothing here
 * resolves them then: recipe type sets are told apart by id, and what depends on the types themselves (generator or
 * not, gregified or not) is a {@link Kind}, worked out when the machine first runs or its model is generated.</p>
 */
public final class ModularMachines {

    private record Pending(String namespace, String name, String englishName, MachineEntry<?> simple,
                           List<DeferredHolder<RecipeType<?>, GTRecipeType>> recipeTypes) {}

    private record Entry(MachineEntry<?> simple, MachineEntry<MultiblockMachineDefinition> modular) {}

    /**
     * What a modular machine's recipe types make of it. A machine mixing generator and other recipe types runs as a
     * processing machine.
     */
    private record Kind(boolean generator, boolean gregified) {

        /** Gregified machines keep their own recipe logic. */
        RecipeModifier overclock() {
            return generator ? GENERATOR_OVERCLOCK :
                    gregified ? GregificationModifiers.OVERCLOCK : GTRecipeModifiers.OC_PERFECT_SUBTICK;
        }
    }

    private static final List<Pending> PENDING = new ArrayList<>();
    private static final Set<Set<ResourceLocation>> RECIPE_TYPES = new HashSet<>();
    private static final Map<ResourceLocation, Supplier<Kind>> KINDS = new HashMap<>();
    private static final Set<String> NAMES = new HashSet<>();
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static boolean declared;

    private static final RecipeModifier GENERATOR_OVERCLOCK = GTRecipeModifiers.ELECTRIC_OVERCLOCK
            .apply(OverclockingLogic.create(0.5, 4.0, true));
    private static final ResourceLocation BASE_CASING = GTCEu.id("block/casings/solid/machine_casing_solid_steel");

    private static final String TOOLTIP_KEY = "gtmqol.multiblock.modular.tooltip";
    private static final String MACHINE_TOOLTIP_KEY = "gtmqol.multiblock.modular.tooltip.machine";
    private static final String GENERATOR_TOOLTIP_KEY = "gtmqol.multiblock.modular.tooltip.generator";

    private ModularMachines() {}

    public static void init(IEventBus modBus) {
        var lang = GTMQoLAddon.registrate();
        lang.addRawLang(TOOLTIP_KEY, "A 3×3×3 box: buses and hatches go anywhere in it, the rest can be any blocks.");
        lang.addRawLang(MACHINE_TOOLTIP_KEY, "Recipes run 8× faster.");
        lang.addRawLang(GENERATOR_TOOLTIP_KEY, "Fuel lasts 8× as long.");
        // Our registrate registers at LOW priority, so this runs first.
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(GTRegistries.Keys.MACHINE)) {
                declareAll();
            }
        });
    }

    /** Synchronized: mods are constructed in parallel, and any of them may register machines in its constructor. */
    public static synchronized void queue(String namespace, String name, int tier, @Nullable String langValue,
                                          MachineEntry<?> simple,
                                          List<DeferredHolder<RecipeType<?>, GTRecipeType>> recipeTypes) {
        if (tier < LV || recipeTypes.isEmpty()) return;
        // Electric machines are named <voltage>_<name>; this also keeps out hp_ steam machines, which are tier 1 (LV).
        String prefix = VN[tier].toLowerCase(Locale.ROOT) + "_";
        if (!name.startsWith(prefix)) return;
        String family = name.substring(prefix.length());
        if (declared) {
            if (GTMQoLConfig.get().modularMachines.enabled && !RECIPE_TYPES.contains(ids(recipeTypes))) {
                GTMQoL.LOGGER.warn("Machine {}:{} was registered after the modular machines were declared, " +
                        "it gets no modular machine", namespace, name);
            }
            return;
        }
        PENDING.add(new Pending(namespace, family, englishName(langValue, tier, family), simple, recipeTypes));
    }

    /** The single block's English name without GT's tier words ("Basic ... ", " II"), else one made from its id. */
    private static String englishName(@Nullable String langValue, int tier, String name) {
        if (langValue != null) {
            String head = VLVH[tier] + " ";
            String tail = " " + VLVT[tier];
            if (langValue.length() > head.length() + tail.length() && langValue.startsWith(head) &&
                    langValue.endsWith(tail)) {
                return langValue.substring(head.length(), langValue.length() - tail.length());
            }
        }
        return FormattingUtil.toEnglishName(name);
    }


    private static synchronized void declareAll() {
        declared = true;
        List<Pending> pending = new ArrayList<>(PENDING);
        PENDING.clear();
        if (!GTMQoLConfig.get().modularMachines.enabled) return;
        if (DatagenModLoader.isRunningDataGen()) return;
        pending.forEach(ModularMachines::declare);
    }

    private static Set<ResourceLocation> ids(List<DeferredHolder<RecipeType<?>, GTRecipeType>> recipeTypes) {
        return recipeTypes.stream().map(DeferredHolder::getId).collect(Collectors.toUnmodifiableSet());
    }

    private static Kind kindOf(List<DeferredHolder<RecipeType<?>, GTRecipeType>> recipeTypes) {
        GTRecipeType[] types = recipeTypes.stream().map(DeferredHolder::get).toArray(GTRecipeType[]::new);
        boolean generator = Arrays.stream(types).allMatch(type -> "generator".equals(type.group));
        return new Kind(generator, Gregification.allGregified(types));
    }

    /** Whether a modular machine runs generator recipes. Only after the recipe types are registered. */
    static boolean isGenerator(MachineDefinition definition) {
        Supplier<Kind> kind = KINDS.get(definition.getId());
        return kind != null && kind.get().generator();
    }

    private static void declare(Pending pending) {
        Set<ResourceLocation> ids = ids(pending.recipeTypes());
        if (!RECIPE_TYPES.add(ids) || ids.contains(GTRecipeTypes.DUMMY_RECIPES.getId())) return;

        String namespace = pending.namespace();
        String name = pending.name();
        String modularName = "modular_" +
                (namespace.equals(GTCEu.MOD_ID) || namespace.equals(GTMQoL.MOD_ID) ? name : namespace + "_" + name);
        if (!NAMES.add(modularName)) {
            GTMQoL.LOGGER.warn("Machine {}:{} has new recipe types but {} already exists, it gets no modular machine",
                    namespace, name, modularName);
            return;
        }

        List<DeferredHolder<RecipeType<?>, GTRecipeType>> recipeTypes = pending.recipeTypes();
        Supplier<Kind> kind = Suppliers.memoize(() -> kindOf(recipeTypes));
        RecipeModifier startModifier = (machine, recipe) -> ModifierFunction.builder()
                .durationMultiplier(kind.get().generator() ? 8.0 : 0.125)
                .build();
        RecipeModifier overclock = (machine, recipe) -> kind.get().overclock().getModifier(machine, recipe);
        @SuppressWarnings("unchecked")
        DeferredHolder<RecipeType<?>, GTRecipeType>[] typeHolders = recipeTypes.toArray(DeferredHolder[]::new);
        MachineEntry<MultiblockMachineDefinition> modular = GTMQoLAddon.multiblock(modularName, ModularMachine::new)
                .dynamicallyGenerated(true)
                .rotationState(RotationState.ALL)
                .recipeTypes(typeHolders)
                // GT's machine UI only shows the batch button for this exact modifier
                .recipeModifiers(startModifier, overclock, GTRecipeModifiers.BATCH_MODE)
                // generator and regressWhenWaiting: set by ModularMachine from its kind
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXX", "XXX", "XXX")
                        .slice("XXX", "XXX", "XXX")
                        .slice("XXX", "XSX", "XXX")
                        .where('S', Predicates.controller(definition))
                        .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get(), Blocks.GLASS,
                                        GTBlocks.CASING_TEMPERED_GLASS.get()).setPreviewCount(100)
                                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                .or(Predicates.any()))
                        .build())
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model((context, provider, model) -> createWorkableCasingMachineModel(BASE_CASING,
                        overlay(namespace, name, kind.get().generator())).configureModel(context, provider, model))
                // Untested: modular machines are named after their single block ("Modular ME Assembler") and say
                // what they are in their tooltip
                .langValue("Modular " + pending.englishName())
                .tooltips(Component.translatable(TOOLTIP_KEY))
                // the kind is only known once the recipe types are registered
                .tooltipBuilder((stack, lines) -> lines.add(Component.translatable(
                        kind.get().generator() ? GENERATOR_TOOLTIP_KEY : MACHINE_TOOLTIP_KEY)))
                .register();

        KINDS.put(modular.getId(), kind);
        ENTRIES.add(new Entry(pending.simple(), modular));
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

    public static void addRecipes(RecipeOutput provider) {
        for (Entry entry : ENTRIES) {
            String path = entry.modular.getId().getPath();
            MagicalAssembler.RECIPE_TYPE.get().recipeBuilder(GTMQoL.id("convert_to_" + path))
                    .inputItems(entry.simple.asStack())
                    .outputItems(entry.modular.asStack())
                    .circuitMeta(5)
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("hammer_convert_to_" + path),
                    entry.modular.asStack(), "h", "M", 'M', entry.simple.asStack());
        }
    }
}
