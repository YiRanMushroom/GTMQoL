package com.yiran.minecraft.gtmqol.modular;

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
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

/**
 * A 3x3x3 multiblock version of every tiered single-block machine with recipe types.
 *
 * <p>On 1.21 every machine is a deferred Registrate entry, so {@code GTMachineUtilsMixin} only queues the
 * tiered machines here (gtceu's own are queued before our config even exists). The multiblocks are declared
 * at the start of the machine registry event: recipe types are registered by then, and our registrate
 * hasn't created its machines yet.</p>
 */
public final class ModularMachines {

    private record Pending(String namespace, String name, MachineEntry<MachineDefinition>[] tiers,
                           Set<Supplier<GTRecipeType>> recipeTypes) {}

    private record Entry(MachineEntry<MachineDefinition> simple, MachineEntry<MultiblockMachineDefinition> modular) {}

    private static final List<Pending> PENDING = new ArrayList<>();
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static final RecipeModifier GENERATOR_OVERCLOCK = GTRecipeModifiers.ELECTRIC_OVERCLOCK
            .apply(OverclockingLogic.create(0.5, 4.0, true));
    private static final ResourceLocation BASE_CASING = GTCEu.id("block/casings/solid/machine_casing_solid_steel");

    private ModularMachines() {}

    public static void init(IEventBus modBus) {
        // Our registrate registers at LOW priority, so this runs first.
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(GTRegistries.Keys.MACHINE)) {
                declareAll();
            }
        });
    }

    public static void queue(GTRegistrate registrate, String name, MachineEntry<MachineDefinition>[] tiers,
                             Set<Supplier<GTRecipeType>> recipeTypes) {
        PENDING.add(new Pending(registrate.getModid(), name, tiers, recipeTypes));
    }

    private static void declareAll() {
        List<Pending> pending = new ArrayList<>(PENDING);
        PENDING.clear();
        if (!GTMQoLConfig.INSTANCE.modularMachines.enabled) return;
        if (DatagenModLoader.isRunningDataGen()) return;
        pending.forEach(ModularMachines::declare);
    }

    private static void declare(Pending pending) {
        MachineEntry<MachineDefinition> simple = Arrays.stream(pending.tiers()).filter(Objects::nonNull)
                .findFirst().orElse(null);
        if (simple == null) return;
        GTRecipeType[] recipeTypes = pending.recipeTypes().stream().map(Supplier::get).toArray(GTRecipeType[]::new);
        if (recipeTypes.length == 0) return;
        if (Arrays.asList(recipeTypes).contains(GTRecipeTypes.DUMMY_RECIPES.get())) return;

        boolean generator = Arrays.stream(recipeTypes).allMatch(type -> "generator".equals(type.group));
        if (!generator && Arrays.stream(recipeTypes).anyMatch(type -> "generator".equals(type.group))) return;

        String namespace = pending.namespace();
        String name = pending.name();
        String modularName = "modular_" +
                (namespace.equals(GTCEu.MOD_ID) || namespace.equals(GTMQoL.MOD_ID) ? name : namespace + "_" + name);

        RecipeModifier startModifier = (machine, recipe) -> ModifierFunction.builder()
                .durationMultiplier(generator ? 8.0 : 0.125)
                .build();

        @SuppressWarnings("unchecked")
        Supplier<GTRecipeType>[] typeSuppliers = pending.recipeTypes().toArray(Supplier[]::new);
        MachineEntry<MultiblockMachineDefinition> modular = GTMQoLAddon.multiblock(modularName, ModularMachine::new)
                .dynamicallyGenerated(true)
                .rotationState(RotationState.ALL)
                .recipeTypes(typeSuppliers)
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

    public static void addRecipes(RecipeOutput provider) {
        for (Entry entry : ENTRIES) {
            String path = entry.modular.getId().getPath();
            MagicalAssembler.RECIPE_TYPE.get().recipeBuilder(GTMQoL.id("convert_to_" + path))
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
