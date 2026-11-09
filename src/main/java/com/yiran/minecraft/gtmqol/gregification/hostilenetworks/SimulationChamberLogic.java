package com.yiran.minecraft.gtmqol.gregification.hostilenetworks;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.gregification.Gregification;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelInstance;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.yiran.minecraft.gtmqol.gregification.hostilenetworks.HostileNetworksGregification.*;

/**
 * Recipes of the simulation chamber, one craft of a data model as HNN runs it: 300 ticks at the model's simulation
 * cost, one prediction matrix, the base drop, and as many predictions as the model's accuracy (its fraction a
 * chance). With programmed circuit 1: the same time and energy, only training, which works from the first tier on,
 * also the ones HNN can't simulate (Faulty). {@link SimulationTraining} then fits a run to its parallels and teaches
 * the model. The recipe viewer says that the model learns ({@link #INFO}).
 */
enum SimulationChamberLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    /** HNN's simulation runtime. */
    static final int DURATION = 300;
    static final int TRAINING_CIRCUIT = 1;
    /** In a recipe's data: the data model it was made for. */
    static final String MODEL_KEY = "hnn_model";
    static final String TRAINING_KEY = "hnn_training";
    static final String LEARNS_INFO = "gtmqol.recipe.hnn_model_learns";
    static final String TRAINING_INFO = "gtmqol.recipe.hnn_training_only";

    /** The recipe viewer line that tells the model learns from the recipe, or only learns. */
    static final RecipeUIModifier INFO = (recipe, widget) -> {
        if (!recipe.data.contains(MODEL_KEY) || HostileConfig.simModelUpgrade <= 0) return;
        String key = recipe.data.getBoolean(TRAINING_KEY) ? TRAINING_INFO : LEARNS_INFO;
        widget.textComponents.child(new TextWidget<>(Text.lang(key)));
    };

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        for (List<ItemStack> items : inventories(holder)) {
            boolean training = items.stream().anyMatch(SimulationChamberLogic::isTrainingCircuit);
            for (ItemStack stack : items) {
                if (!(stack.getItem() instanceof DataModelItem) || stack.getCount() != 1) continue;
                DataModelInstance model = new DataModelInstance(stack, 0);
                if (!model.isValid()) continue;
                if (training) {
                    if (canLearn(model)) return recipe(model, true);
                } else if (model.getTier().canSim() && items.stream().anyMatch(model.getModel().input())) {
                    return recipe(model, false);
                }
            }
        }
        return null;
    }

    private static boolean isTrainingCircuit(ItemStack stack) {
        return stack.is(GTItems.PROGRAMMED_CIRCUIT.get()) &&
                IntCircuitBehaviour.getCircuitConfiguration(stack) == TRAINING_CIRCUIT;
    }

    /** Whether one more simulation teaches the model, as in HNN's chamber (config simModelUpgrade). */
    static boolean canLearn(DataModelInstance model) {
        if (model.getTier().isMax() || HostileConfig.simModelUpgrade <= 0) return false;
        return HostileConfig.simModelUpgrade != 2 || model.getData() + 1 <= model.getNextTierData();
    }

    private static GTRecipe recipe(DataModelInstance model, boolean training) {
        DataModel dataModel = model.getModel();
        String id = path(DataModelRegistry.INSTANCE.getKey(dataModel)) + (training ? "/training" : "");
        GTRecipeBuilder builder = recipeType(SIM_CHAMBER).recipeBuilder(GTMQoL.id(id))
                .notConsumable(modelIngredient(model.getSourceStack()))
                .addData(MODEL_KEY, DataModelRegistry.INSTANCE.getKey(dataModel).toString())
                .addData(TRAINING_KEY, training)
                .EUt(euPerTick(dataModel.simCost()))
                .duration(DURATION);
        if (training) {
            builder.circuitMeta(TRAINING_CIRCUIT);
        } else {
            builder.inputItems(dataModel.input());
            if (!dataModel.baseDrop().isEmpty()) builder.outputItems(dataModel.baseDrop().copy());
            addPredictions(builder, model.getPredictionDrop(), model.getAccuracy());
        }
        return builder.build();
    }

    /** {@code expected} predictions: the whole part always, the fraction as a chance. */
    static void addPredictions(GTRecipeBuilder builder, ItemStack prediction, double expected) {
        int whole = (int) expected;
        if (whole > 0) builder.outputItems(prediction.copyWithCount(whole));
        int chance = (int) Math.round((expected - whole) * 10000);
        if (chance > 0) {
            builder.chancedOutput(RecipeHelper.makeSizedIngredient(prediction.copyWithCount(1)),
                    Gregification.chance(expected - whole));
        }
    }

    /** The same data model, whatever it has learnt. */
    static Ingredient modelIngredient(ItemStack model) {
        DynamicHolder<DataModel> holder = DataModelItem.getStoredModel(model);
        return DataComponentIngredient.of(false,
                DataComponentPredicate.builder().expect(Hostile.Components.DATA_MODEL, holder).build(),
                Hostile.Items.DATA_MODEL.value());
    }

    /** A data model of {@code dataModel} at the start of {@code tier}. */
    private static DataModelInstance model(DataModel dataModel, ModelTier tier) {
        ItemStack stack = new ItemStack(Hostile.Items.DATA_MODEL);
        DataModelItem.setStoredModel(stack, dataModel);
        DataModelItem.setData(stack, dataModel.getRequiredData(tier));
        return new DataModelInstance(stack, 0);
    }

    @Override
    public void buildRepresentativeRecipes() {
        GTRecipeType type = recipeType(SIM_CHAMBER);
        // simulations from the first tier HNN simulates, training from the very first
        ModelTier simTier = ModelTierRegistry.getSortedTiers().stream().filter(ModelTier::canSim).findFirst()
                .orElse(null);
        for (DataModel dataModel : DataModelRegistry.INSTANCE.getValues()) {
            if (simTier != null) {
                DataModelInstance model = model(dataModel, simTier);
                if (model.isValid()) addRepresentative(type, recipe(model, false));
            }
            DataModelInstance first = model(dataModel, ModelTierRegistry.getMinTier());
            if (first.isValid() && canLearn(first)) addRepresentative(type, recipe(first, true));
        }
    }
}
