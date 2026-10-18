package com.yiran.minecraft.gtmqol.gregification.create;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;

/**
 * Create's sequenced assembly as a line like GT's assembly line, 3 to 16 slices long. Every slice has one bronze
 * gearbox casing, so that is how long it is. Below {@link #MAX_LENGTH}, it only runs recipes with at most as many
 * steps as its length, loops not counted ({@link #STEP_LIMIT}); at full length, any recipe.
 */
public class SequencedAssemblyMachine extends WorkableElectricMultiblockMachine {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 16;
    /** Recipe data: the number of steps, loops not counted. */
    public static final String STEPS = "gtmqol_sequenced_steps";
    public static final String TOO_SHORT = "gtmqol.recipe_modifier.sequenced_assembly_too_short";
    public static final String MIN_LENGTH_INFO = "gtmqol.recipe.sequenced_assembly_min_length";

    /** Recipes need a line at least {@link #minLength} long. */
    public static final RecipeModifier STEP_LIMIT = (machine, recipe) -> {
        if (machine instanceof SequencedAssemblyMachine line && line.length < minLength(recipe)) {
            return ModifierFunction.cancel(Component.translatable(TOO_SHORT));
        }
        return ModifierFunction.IDENTITY;
    };

    /** The recipe viewer line with {@link #minLength}. */
    public static final RecipeUIModifier LENGTH_INFO = (recipe, widget) -> widget.textComponents
            .child(new TextWidget<>(Text.lang(MIN_LENGTH_INFO, minLength(recipe))));

    private int length;

    public SequencedAssemblyMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    /** One slice per step, at least {@link #MIN_LENGTH}; a full line takes any number of steps. */
    public static int minLength(GTRecipe recipe) {
        return Math.max(MIN_LENGTH, Math.min(recipe.data.getInt(STEPS), MAX_LENGTH));
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;
        int[] slices = { 0 };
        forEachFormed(substructureName, (info, pos) -> {
            if (info.getBlockState().is(GTBlocks.CASING_BRONZE_GEARBOX.get())) slices[0]++;
        });
        length = slices[0];
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        if (DEFAULT_STRUCTURE.equals(name)) length = 0;
    }
}
