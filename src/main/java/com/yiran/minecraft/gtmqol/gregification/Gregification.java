package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.singleblock.SingleblockFeature;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * <p>Sources are read on the first registry event, after every mod is constructed (MI's KubeJS recipe types exist
 * by then) and before any of our registrate's registries, and the machines are queued before
 * {@code ModularMachines} declares its multiblocks on the machine registry event.</p>
 */
public final class Gregification {

    private static final List<Supplier<List<ForeignMachineType>>> SOURCES = new ArrayList<>();
    private static final Set<String> NAMES = new HashSet<>();
    private static final List<GTRecipeTypeEntry> RECIPE_TYPES = new ArrayList<>();
    private static final List<Crafted> CRAFTED = new ArrayList<>();
    private static boolean declared;

    private record Crafted(MachineEntry<?> machine, @Nullable ResourceLocation catalyst,
                           ResourceLocation counterpart, ForeignMachineType.@Nullable Crafting crafting) {}

    private Gregification() {}

    public static void init(IEventBus modBus) {
        CatalystShapelessRecipe.init(modBus);
        // The first registry event of all: gtceu moves its registries and recipe types to the front, in an order
        // we'd rather not depend on.
        modBus.addListener(EventPriority.HIGHEST, RegisterEvent.class, event -> declareAll());
        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            for (GregifiedRecipeType recipeType : recipeTypes()) recipeType.resolveProxy();
        });
    }

    /** Call from the mod constructor. */
    public static void addSource(Supplier<List<ForeignMachineType>> source) {
        SOURCES.add(source);
    }

    private static void declareAll() {
        if (declared) return;
        declared = true;
        if (DatagenModLoader.isRunningDataGen()) return;
        for (var source : SOURCES) {
            for (ForeignMachineType type : source.get()) {
                if (!NAMES.add(type.name())) {
                    GTMQoL.LOGGER.warn("Gregified recipe type {} already exists, skipping the second one", type.name());
                    continue;
                }
                declare(type);
            }
        }
    }

    private static void declare(ForeignMachineType type) {
        GTRegistrate registrate = GTMQoLAddon.registrate();
        String name = type.name();
        GTRecipeTypeBuilder builder = registrate
                .<RecipeType<?>, GTRecipeType, GTRegistrate, GTRecipeTypeBuilder>entry(name,
                        callback -> new GregifiedRecipeTypeBuilder(registrate, name, callback, GTRecipeTypes.ELECTRIC,
                                type.converter(), type.proxies()))
                .setMaxIOSize(type.itemInputs(), type.itemOutputs(), type.fluidInputs(), type.fluidOutputs())
                .setEUIO(IO.IN)
                .UI(type.ui())
                .setSound(type.sound());
        GTRecipeTypeEntry recipeType = type.recipeType().apply(builder).register();
        RECIPE_TYPES.add(recipeType);
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us",
                GTMQoL.id(name).toLanguageKey(GTRecipeType.LANGUAGE_KEY_PATH), type.englishName());

        if (type.isMultiblock()) {
            declareMultiblock(type, recipeType);
        } else {
            declareSingleBlocks(type, recipeType);
        }
    }

    private static void declareSingleBlocks(ForeignMachineType type, GTRecipeTypeEntry recipeType) {
        for (int tier : ELECTRIC_TIERS) {
            MachineEntry<?> machine = GTMQoLAddon.tieredMachine(type.name(), tier,
                            SingleblockFeature.BATCH_MODE, SingleblockFeature.GHOST_MARKS)
                    .dynamicallyGenerated(true)
                    .langValue("%s %s %s".formatted(VLVH[tier], type.englishName(), VLVT[tier]))
                    .recipeModifiers(GregificationModifiers.OVERCLOCK, GregificationModifiers.BATCH)
                    .tooltips(tooltip(type))
                    .workableTiered(recipeType, type.model())
                    .register();
            CRAFTED.add(new Crafted(machine, type.catalyst(), type.counterpart().apply(tier), type.crafting()));
        }
    }

    private static void declareMultiblock(ForeignMachineType type, GTRecipeTypeEntry recipeType) {
        MultiblockShape shape = type.shape();
        GTMQoLMultiblockBuilder<WorkableElectricMultiblockMachine> builder = GTMQoLAddon
                .multiblock(type.name(), shape.machine())
                .dynamicallyGenerated(true);
        shape.configure(builder);
        // GT's machine UI only shows the batch button for GT's own BATCH_MODE
        RecipeModifier[] modifiers = type.modifiers() != null ? type.modifiers().toArray(RecipeModifier[]::new) :
                new RecipeModifier[] { GregificationModifiers.COIL_TEMPERATURE, GregificationModifiers.OVERCLOCK,
                        GTRecipeModifiers.BATCH_MODE };
        MachineEntry<?> machine = builder.recipeType(recipeType)
                .recipeModifiers(modifiers)
                .appearanceBlock(shape.casing)
                .pattern(shape::pattern)
                .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(shape.model())
                .langValue(type.englishName())
                .tooltips(tooltip(type))
                .register();
        CRAFTED.add(new Crafted(machine, type.catalyst(), type.counterpart().apply(-1), type.crafting()));
    }

    private static @Nullable Component tooltip(ForeignMachineType type) {
        return type.tooltip() == null ? null : Component.translatable(type.tooltip());
    }

    /**
     * Shapeless: counterpart + catalyst (kept) = machine. Without a catalyst, like the modular machines: the
     * counterpart hit with a GT hammer, or through the magical assembler; or the type's own crafting.
     */
    public static void addRecipes(RecipeOutput provider) {
        for (Crafted crafted : CRAFTED) {
            var counterpart = BuiltInRegistries.ITEM.getOptional(crafted.counterpart);
            ResourceLocation id = crafted.machine.getId();
            if (crafted.catalyst == null) {
                if (counterpart.isEmpty()) {
                    GTMQoL.LOGGER.debug("No recipe for {}: {} doesn't exist", id, crafted.counterpart);
                    continue;
                }
                ItemStack counterpartStack = new ItemStack(counterpart.get());
                ResourceLocation recipeId = GTMQoL.id("gregification/" + id.getPath());
                if (crafted.crafting != null) {
                    crafted.crafting.addRecipes(provider, recipeId, counterpartStack, crafted.machine.asStack());
                    continue;
                }
                MagicalAssembler.RECIPE_TYPE.get().recipeBuilder(recipeId)
                        .inputItems(counterpartStack)
                        .outputItems(crafted.machine.asStack())
                        .circuitMeta(5)
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
            Ingredient catalystIngredient = Ingredient.of(catalyst.get());
            var recipe = new ShapelessRecipe("", CraftingBookCategory.MISC, crafted.machine.asStack(),
                    NonNullList.of(Ingredient.EMPTY, catalystIngredient, Ingredient.of(counterpart.get())));
            provider.accept(GTMQoL.id("gregification/" + id.getPath()),
                    new CatalystShapelessRecipe(recipe, catalystIngredient), null);
        }
    }

    public static List<GregifiedRecipeType> recipeTypes() {
        return RECIPE_TYPES.stream().map(entry -> (GregifiedRecipeType) entry.get()).toList();
    }

    /** Whether every recipe type is gregified, so a machine running them should use {@link GregificationModifiers}. */
    public static boolean allGregified(GTRecipeType... recipeTypes) {
        if (recipeTypes.length == 0) return false;
        for (GTRecipeType recipeType : recipeTypes) {
            if (!(recipeType instanceof GregifiedRecipeType)) return false;
        }
        return true;
    }

    /** A probability (1 = always) as a GT chance out of {@link ChanceLogic#getMaxChancedValue()}, at least 1. */
    public static int chance(double probability) {
        int max = ChanceLogic.getMaxChancedValue();
        return (int) Math.max(1, Math.min(max, Math.round(probability * max)));
    }

    /** An item output with a probability (1 = always): chanced, or a plain output if it would always happen. */
    public static void chancedOutput(GTRecipeBuilder builder, SizedIngredient output, double probability) {
        int chance = chance(probability);
        if (chance >= ChanceLogic.getMaxChancedValue()) {
            builder.outputItems(output.ingredient(), output.count());
        } else {
            builder.chancedOutput(output, chance);
        }
    }
}
