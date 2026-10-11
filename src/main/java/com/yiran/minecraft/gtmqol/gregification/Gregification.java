package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.ShapelessRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;

/**
 * Gregification: recipe types of other mods become GT recipe types of their own, run by GT machines.
 *
 * <p>Every {@link ForeignMachineType} gets a {@link GregifiedRecipeType} proxying the foreign type, so its recipes
 * are converted on every reload and nothing is written to data packs. Single-block types get tiered machines
 * ({@code ModularMachines} then adds their multiblock), multiblock types a multiblock shaped like a GT one. All of
 * them run {@link GregificationModifiers}.</p>
 *
 * <p>Sources are read on GTCEu's recipe type {@code RegisterEvent} (posted from its common proxy, after every mod is
 * constructed), the machines are declared on the machine {@code RegisterEvent}, which comes later.</p>
 */
public final class Gregification {

    private static final List<Supplier<List<ForeignMachineType>>> SOURCES = new ArrayList<>();
    private static final Set<String> NAMES = new HashSet<>();
    private static final List<Declared> DECLARED = new ArrayList<>();
    private static final List<GregifiedRecipeType> RECIPE_TYPES = new ArrayList<>();
    private static final List<Crafted> CRAFTED = new ArrayList<>();

    private record Declared(ForeignMachineType type, GregifiedRecipeType recipeType) {}

    private record Crafted(MachineDefinition machine, @Nullable ResourceLocation catalyst,
                           ResourceLocation counterpart) {}

    private Gregification() {}

    /** Call from the mod constructor. */
    public static void init(IEventBus modBus) {
        CatalystShapelessRecipe.init(modBus);
        modBus.addGenericListener(GTRecipeType.class, Gregification::onRegisterRecipeTypes);
        modBus.addGenericListener(MachineDefinition.class, Gregification::onRegisterMachines);
        modBus.addListener((FMLCommonSetupEvent event) -> {
            for (GregifiedRecipeType recipeType : RECIPE_TYPES) recipeType.resolveProxy();
        });
    }

    /** Call from the mod constructor. */
    public static void addSource(Supplier<List<ForeignMachineType>> source) {
        SOURCES.add(source);
    }

    private static void onRegisterRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        if (FMLLoader.getLaunchHandler().isData()) return;
        for (var source : SOURCES) {
            for (ForeignMachineType type : source.get()) {
                if (!NAMES.add(type.name())) {
                    GTMQoL.LOGGER.warn("Gregified recipe type {} already exists, skipping the second one", type.name());
                    continue;
                }
                DECLARED.add(new Declared(type, declareRecipeType(type)));
            }
        }
    }

    private static void onRegisterMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        for (Declared declared : DECLARED) {
            if (declared.type.isMultiblock()) {
                declareMultiblock(declared.type, declared.recipeType);
            } else {
                declareSingleBlocks(declared.type, declared.recipeType);
            }
        }
    }

    private static GregifiedRecipeType declareRecipeType(ForeignMachineType type) {
        var recipeType = new GregifiedRecipeType(GTMQoL.id(type.name()), GTRecipeTypes.ELECTRIC, type.converter(),
                type.proxy());
        // same as GTRecipeTypes.register, which only makes plain GTRecipeTypes
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        recipeType.setMaxIOSize(type.itemInputs(), type.itemOutputs(), type.fluidInputs(), type.fluidOutputs())
                .setEUIO(IO.IN)
                .UI(type.ui())
                .setSound(type.sound());
        type.recipeType().apply(recipeType);
        RECIPE_TYPES.add(recipeType);
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", recipeType.registryName.toLanguageKey(),
                type.englishName());
        return recipeType;
    }

    private static void declareSingleBlocks(ForeignMachineType type, GregifiedRecipeType recipeType) {
        for (int tier : ELECTRIC_TIERS) {
            MachineDefinition machine = GTMQoLAddon.machine(VN[tier].toLowerCase(Locale.ROOT) + "_" + type.name(),
                            info -> new GregifiedTieredMachine(info, tier))
                    .dynamicallyGenerated(true)
                    .tier(tier)
                    .langValue("%s %s %s".formatted(VLVH[tier], type.englishName(), VLVT[tier]))
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(recipeType)
                    .recipeModifiers(GregificationModifiers.OVERCLOCK, GregificationModifiers.BATCH)
                    .workableTieredHullModel(type.model())
                    .tooltips(workableTiered(tier, V[tier], V[tier] * 64, recipeType,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .register();
            CRAFTED.add(new Crafted(machine, type.catalyst(), type.counterpart().apply(tier)));
        }
    }

    private static void declareMultiblock(ForeignMachineType type, GregifiedRecipeType recipeType) {
        MultiblockShape shape = type.shape();
        GTMQoLMultiblockBuilder<WorkableElectricMultiblockMachine> builder = GTMQoLAddon
                .multiblock(type.name(), shape.machine())
                .dynamicallyGenerated(true);
        shape.configure(builder);
        MachineDefinition machine = builder.recipeType(recipeType)
                // GT's machine UI only shows the batch button for GT's own BATCH_MODE
                .recipeModifiers(GregificationModifiers.COIL_TEMPERATURE, GregificationModifiers.OVERCLOCK,
                        GTRecipeModifiers.BATCH_MODE)
                .appearanceBlock(shape.casing)
                .pattern(shape::pattern)
                .workableCasingModel(shape.casingModel, shape.overlay)
                .langValue(type.englishName())
                .register();
        CRAFTED.add(new Crafted(machine, type.catalyst(), type.counterpart().apply(-1)));
    }

    /**
     * Shapeless: counterpart + catalyst (kept) = machine. Without a catalyst, like the modular machines: the
     * counterpart hit with a GT hammer, or through the magical assembler.
     */
    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        for (Crafted crafted : CRAFTED) {
            var counterpart = BuiltInRegistries.ITEM.getOptional(crafted.counterpart);
            ResourceLocation id = crafted.machine.getId();
            if (crafted.catalyst == null) {
                if (counterpart.isEmpty()) {
                    GTMQoL.LOGGER.debug("No recipe for {}: {} doesn't exist", id, crafted.counterpart);
                    continue;
                }
                ItemStack counterpartStack = new ItemStack(counterpart.get());
                MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("gregification/" + id.getPath()))
                        .inputItems(counterpartStack)
                        .outputItems(crafted.machine.asStack())
                        .circuitMeta(5)
                        .EUt(VA[LV])
                        .duration(200)
                        .save(provider);
                VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("gregification/hammer_" + id.getPath()),
                        crafted.machine.asStack(), "h", "M", 'M', counterpartStack);
                continue;
            }
            var catalyst = BuiltInRegistries.ITEM.getOptional(crafted.catalyst);
            if (catalyst.isEmpty() || counterpart.isEmpty()) {
                // e.g. a tier GTCEu doesn't register without high tier content
                GTMQoL.LOGGER.debug("No recipe for {}: {} or {} doesn't exist", id, crafted.catalyst,
                        crafted.counterpart);
                continue;
            }
            ResourceLocation recipeId = GTMQoL.id("gregification/" + id.getPath());
            Ingredient catalystIngredient = Ingredient.of(catalyst.get());
            var shapeless = new ShapelessRecipeBuilder(recipeId)
                    .requires(catalystIngredient)
                    .requires(Ingredient.of(counterpart.get()))
                    .output(crafted.machine.asStack());
            provider.accept(CatalystShapelessRecipe.finished(recipeId, shapeless, catalystIngredient));
        }
    }

    public static List<GregifiedRecipeType> recipeTypes() {
        return RECIPE_TYPES;
    }

    /** Whether every recipe type is gregified, so a machine running them should use {@link GregificationModifiers}. */
    public static boolean allGregified(GTRecipeType... recipeTypes) {
        if (recipeTypes.length == 0) return false;
        for (GTRecipeType recipeType : recipeTypes) {
            if (!(recipeType instanceof GregifiedRecipeType)) return false;
        }
        return true;
    }
}
