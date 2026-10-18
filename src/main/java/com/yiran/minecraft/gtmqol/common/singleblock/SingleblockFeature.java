package com.yiran.minecraft.gtmqol.common.singleblock;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.GhostMarksTrait;

/**
 * Something a {@link ComposableTieredMachine} can have, attached when the machine is created. Features are saved
 * traits; a trait implementing {@code IAttachConfiguratorsTrait} brings its own buttons to GT's machine UI.
 */
@FunctionalInterface
public interface SingleblockFeature {

    void attach(MetaMachine machine);

    /**
     * GT's batch button, off by default. Only does something with
     * {@link com.yiran.minecraft.gtmqol.gregification.GregificationModifiers#BATCH} among the recipe modifiers.
     */
    SingleblockFeature BATCH_MODE = machine -> machine.attachPersistentTrait("batchMode", new BatchModeTrait());

    /** Its own list of ghost ingredient marks, if any of its recipe types take them. */
    SingleblockFeature GHOST_MARKS = machine -> {
        for (GTRecipeType type : machine.getDefinition().getRecipeTypes()) {
            if (type.getMaxInputs(GhostIngredientRecipeCapability.CAP) > 0) {
                machine.attachPersistentTrait("ghostMarks", new GhostMarksTrait());
                return;
            }
        }
    };
}
