package com.yiran.minecraft.gtmqol.gregification.hostilenetworks;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.shadowsoffire.hostilenetworks.Hostile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.GTValues.VN;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/**
 * Hostile Neural Networks' simulation chamber and loot fabricator as GT single blocks of every electric tier (and so
 * modular multiblocks too). HNN has no recipes for them: both run custom recipe logic built from its data models, a
 * datapack registry. HNN 5 (1.20.1): five fixed tiers, accuracy at most 1, model data in NBT.
 * <ul>
 * <li>Simulation chamber ({@link SimulationChamberLogic}): a data model, which stays, and a prediction matrix give HNN's
 * base drop and predictions, and the model learns from every craft. With programmed circuit 1 the model only learns,
 * without a matrix or outputs, at the same energy. {@link SimulationTraining} keeps the model right on every machine
 * running the type, parallels included.</li>
 * <li>Loot fabricator ({@link LootFabricatorLogic}): a prediction becomes one of its model's fabricator drops, picked
 * with a ghost mark (none needed for a model with a single drop).</li>
 * </ul>
 */
public final class HostileNetworksGregification {

    static final String SIM_CHAMBER = "hnn_simulation_chamber";
    static final String LOOT_FABRICATOR = "hnn_loot_fabricator";
    /** gtceu shows {@code <namespace>.machine.<path>.tooltip} on its own. */
    private static final String SIM_TOOLTIP = "gtmqol.machine." + SIM_CHAMBER + ".tooltip";
    private static final String FAB_TOOLTIP = "gtmqol.machine." + LOOT_FABRICATOR + ".tooltip";
    static final String MODEL_DONE = "gtmqol.machine." + SIM_CHAMBER + ".model_done";

    static {
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SIM_TOOLTIP,
                "Simulates a data model with a prediction matrix. With programmed circuit 1 it only trains the data " +
                        "model, even a Faulty one.");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", FAB_TOOLTIP,
                "Turns a prediction into one of its drops, picked by a ghost ingredient.");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", MODEL_DONE,
                "The data model can't learn any more from simulations");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SimulationChamberLogic.LEARNS_INFO,
                "The data model learns: +1 data per craft");
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", SimulationChamberLogic.TRAINING_INFO,
                "Training only: +1 data per craft, for a Faulty data model too");
    }

    private HostileNetworksGregification() {}

    public static void init() {
        SimulationTraining.init();
    }

    public static List<ForeignMachineType> types() {
        return List.of(
                ForeignMachineType.singleBlock(SIM_CHAMBER, "HNN Simulation Chamber", null, null, 2, 2, 0, 0,
                                GTSoundEntries.COMPUTATION,
                                builder -> builder.addRecipeUIModifier(SimulationChamberLogic.INFO), null,
                                HostileNetworksGregification::hull, GTCEu.id("block/machines/scanner"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(SimulationChamberLogic.INSTANCE))
                        .withTooltip(SIM_TOOLTIP)
                        .withCrafting(HostileNetworksGregification::craftSimulationChamber),
                ForeignMachineType.singleBlock(LOOT_FABRICATOR, "HNN Loot Fabricator", null, null, 1, 1, 0, 0,
                                GTSoundEntries.REPLICATOR, UnaryOperator.identity(), null,
                                HostileNetworksGregification::hull, GTCEu.id("block/machines/replicator"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(LootFabricatorLogic.INSTANCE)
                                .setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1))
                        .withTooltip(FAB_TOOLTIP)
                        .withCrafting(HostileNetworksGregification::craftLootFabricator));
    }

    /** The counterpart the crafting recipes start from. */
    private static ResourceLocation hull(int tier) {
        return GTCEu.id(VN[tier].toLowerCase(Locale.ROOT) + "_machine_hull");
    }

    private static int tier(ItemStack machine) {
        return ((MetaMachineItem) machine.getItem()).getDefinition().getTier();
    }

    /** Field generators around HNN's simulation chamber. */
    private static void craftSimulationChamber(Consumer<FinishedRecipe> provider, ResourceLocation id, ItemStack hull,
                                               ItemStack machine) {
        int tier = tier(machine);
        VanillaRecipeHelper.addShapedRecipe(provider, id, machine, "FCF", "WHW", "CSC",
                'F', FIELD_GENERATOR.get(tier), 'C', CIRCUIT.get(tier), 'W', CABLE.get(tier), 'H', hull,
                'S', new ItemStack(Hostile.Items.SIM_CHAMBER.get()));
    }

    /** A sensor and an emitter with HNN's loot fabricator. */
    private static void craftLootFabricator(Consumer<FinishedRecipe> provider, ResourceLocation id, ItemStack hull,
                                            ItemStack machine) {
        int tier = tier(machine);
        VanillaRecipeHelper.addShapedRecipe(provider, id, machine, "ECS", "WHW", "CLC",
                'E', EMITTER.get(tier), 'S', SENSOR.get(tier), 'C', CIRCUIT.get(tier), 'W', CABLE.get(tier),
                'H', hull, 'L', new ItemStack(Hostile.Items.LOOT_FABRICATOR.get()));
    }

    static GTRecipeType recipeType(String name) {
        return (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(GTMQoL.id(name));
    }

    /** HNN's energy per tick in FE as GT's EU per tick, at least 1. */
    static long euPerTick(int fePerTick) {
        int ratio = Math.max(1, ConfigHolder.INSTANCE.compat.energy.feToEuRatio);
        return Math.max(1, (fePerTick + ratio - 1) / ratio);
    }

    /** For a recipe the recipe viewer shows: the leading '/' marks it synthetic. */
    static void addRepresentative(GTRecipeType type, GTRecipe recipe) {
        recipe.setId(recipe.getId().withPrefix("/"));
        type.addToMainCategory(recipe);
    }

    /** Input items of each distinct bus on its own, then of all the others together. */
    static List<List<ItemStack>> inventories(IRecipeCapabilityHolder holder) {
        List<List<ItemStack>> inventories = new ArrayList<>();
        List<ItemStack> shared = new ArrayList<>();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            List<ItemStack> items = handlerList.isDistinct() ? new ArrayList<>() : shared;
            for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) items.add(stack);
                }
            }
            if (handlerList.isDistinct()) inventories.add(items);
        }
        inventories.add(shared);
        return inventories;
    }

    /** Recipe id path for a data model, e.g. {@code hostilenetworks/zombie}. */
    static String path(ResourceLocation model) {
        return model.getNamespace() + "/" + model.getPath();
    }
}
