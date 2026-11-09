package com.yiran.minecraft.gtmqol.gregification.hostilenetworks;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRecipeLogicModifierTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.yiran.minecraft.gtmqol.api.machine.MachineFeatures;
import com.yiran.minecraft.gtmqol.gregification.Gregification;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.data.DataModelInstance;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.yiran.minecraft.gtmqol.gregification.hostilenetworks.HostileNetworksGregification.*;
import static com.yiran.minecraft.gtmqol.gregification.hostilenetworks.SimulationChamberLogic.*;

/**
 * The data model of a simulation chamber run, on every machine that runs the recipe type (the single blocks, the
 * modular multiblock, any other): GT decides a run's parallels after the recipe is made, so the recipe can't know how
 * many crafts it teaches the model, nor that each craft's prediction chance follows the model as it learns. Once the
 * parallels are known, every craft is simulated on a copy of the model: a training run is cut to the crafts that
 * still teach it, a simulation gets the predictions of all crafts together. The model learns when the run ends (an
 * interrupted run teaches nothing); the next run, made for the model as it was, is looked up again.
 */
// Untested: in the simulation chamber (and its modular multiblock), a run of several parallels adds as many data to
// the model when it ends, and its predictions follow the rising accuracy (on average a craft makes accuracy
// predictions, also with many parallels); circuit 1 trains from Faulty on, without a matrix, and stops at max tier;
// the recipe viewer says which recipes teach the model
final class SimulationTraining extends MachineTrait implements IRecipeLogicModifierTrait {

    /** In a run's data: what the model had learnt when the run was made for it. */
    private static final String LEARNT_KEY = "hnn_model_data";

    static void init() {
        MachineFeatures.onRecipeType(() -> recipeType(SIM_CHAMBER), machine -> machine.attachTrait(new SimulationTraining()));
    }

    private record Model(NotifiableItemStackHandler items, int slot, ItemStack stack) {}

    private record Run(int learnt, double predictions, DataModelInstance after) {}

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        if (!recipe.data.contains(MODEL_KEY)) return recipe;
        Model model = model(recipe);
        if (model == null) return recipe;
        int crafts = crafts(recipe);
        Run run = simulate(model.stack, crafts);
        GTRecipe modified;
        if (recipe.data.getBoolean(TRAINING_KEY)) {
            if (run.learnt == 0) return null;
            modified = run.learnt < crafts ? cut(recipe, run.learnt, crafts) : recipe.copy();
        } else {
            modified = recipe.copy();
            setPredictions(modified, run.predictions, crafts,
                    new DataModelInstance(model.stack, 0).getPredictionDrop());
        }
        // the recipe was made for this search, its data is not shared with anything else
        modified.data.putInt(LEARNT_KEY, DataModelItem.getData(model.stack));
        return modified;
    }

    @Override
    public Component getFailReason() {
        return Component.translatable(MODEL_DONE);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null || !recipe.data.contains(MODEL_KEY)) return true;
        Model model = model(recipe);
        if (model == null) return true;
        if (!recipe.data.contains(LEARNT_KEY) || recipe.data.getInt(LEARNT_KEY) != DataModelItem.getData(model.stack)) {
            // a repeated run made for the model as it was: look it up again
            ((IRecipeLogicMachine) getMachine()).getRecipeLogic().markLastRecipeDirty();
            return false;
        }
        return true;
    }

    @Override
    public void afterWorking() {
        RecipeLogic logic = ((IRecipeLogicMachine) getMachine()).getRecipeLogic();
        GTRecipe recipe = logic.getLastRecipe();
        // also called when a run is interrupted, which teaches nothing
        if (recipe == null || logic.getProgress() < logic.getDuration() || !recipe.data.contains(MODEL_KEY)) return;
        Model model = model(recipe);
        // the model was taken out or swapped during the run
        if (model == null || recipe.data.getInt(LEARNT_KEY) != DataModelItem.getData(model.stack)) return;
        int crafts = crafts(recipe);
        ItemStack learnt = simulate(model.stack, crafts).after.getSourceStack();
        DataModelItem.setIters(learnt, DataModelItem.getIters(learnt) + crafts);
        model.items.storage.setStackInSlot(model.slot, learnt);
    }

    private static int crafts(GTRecipe recipe) {
        return recipe.parallels * recipe.subtickParallels * recipe.batchParallels;
    }

    /** {@code crafts} simulations on a copy of {@code stack}: what it learns and how many predictions they make. */
    private static Run simulate(ItemStack stack, int crafts) {
        DataModelInstance model = new DataModelInstance(stack.copy(), 0);
        int learnt = 0;
        double predictions = 0;
        for (int i = 0; i < crafts; i++) {
            predictions += model.getAccuracy();
            if (!canLearn(model)) continue;
            model.setData(model.getData() + 1);
            learnt++;
        }
        return new Run(learnt, predictions, model);
    }

    /** The single data model of the recipe among the machine's inputs. */
    private @Nullable Model model(GTRecipe recipe) {
        String id = recipe.data.getString(MODEL_KEY);
        for (var handlerList : ((IRecipeCapabilityHolder) getMachine()).getCapabilitiesForIO(IO.IN)) {
            for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                if (!(handler instanceof NotifiableItemStackHandler items)) continue;
                for (int slot = 0; slot < items.storage.getSlots(); slot++) {
                    ItemStack stack = items.storage.getStackInSlot(slot);
                    if (!(stack.getItem() instanceof DataModelItem) || stack.getCount() != 1) continue;
                    var stored = DataModelItem.getStoredModel(stack);
                    if (stored.isBound() && id.equals(DataModelRegistry.INSTANCE.getKey(stored.get()).toString())) {
                        return new Model(items, slot, stack);
                    }
                }
            }
        }
        return null;
    }

    /** A training run of {@code learnt} crafts where GT planned {@code crafts}: the rest wouldn't teach anything. */
    private static GTRecipe cut(GTRecipe recipe, int learnt, int crafts) {
        GTRecipe cut = ModifierFunction.builder().eutMultiplier((double) learnt / crafts).build().apply(recipe);
        if (cut == null) return recipe.copy();
        cut.parallels = 1;
        cut.batchParallels = 1;
        cut.subtickParallels = learnt;
        return cut;
    }

    /**
     * Replaces the predictions GT scaled with the parallels by those of all crafts, each at its own accuracy: {@code
     * expected} of them on average. GT rolls a chanced output once per craft of the run (its chance times the crafts),
     * so the part below a whole prediction is given as a chance per craft.
     */
    private static void setPredictions(GTRecipe recipe, double expected, int crafts, ItemStack prediction) {
        List<Content> outputs = new ArrayList<>();
        for (Content content : recipe.outputs.getOrDefault(ItemRecipeCapability.CAP, List.of())) {
            if (!(content.content() instanceof SizedIngredient ingredient) ||
                    !ingredient.ingredient().test(prediction)) {
                outputs.add(content);
            }
        }
        int whole = (int) expected;
        int max = ChanceLogic.getMaxChancedValue();
        int chance = expected > whole ? Gregification.chance((expected - whole) / crafts) : 0;
        if (chance >= max) {
            whole++;
            chance = 0;
        }
        if (whole > 0) {
            outputs.add(new Content(RecipeHelper.makeSizedIngredient(prediction.copyWithCount(whole)), max, max));
        }
        if (chance > 0) {
            outputs.add(new Content(RecipeHelper.makeSizedIngredient(prediction.copyWithCount(1)), chance, max));
        }
        recipe.outputs.put(ItemRecipeCapability.CAP, outputs);
    }
}
