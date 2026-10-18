package com.yiran.minecraft.gtmqol.gregification.create;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

/**
 * Create's sequenced assembly as a line like GT's assembly line, 3 to 16 slices long. Every slice has one bronze
 * gearbox casing, so that is how long it is. Below {@link #MAX_LENGTH}, it only runs recipes with fewer steps than
 * its length minus one, loops not counted ({@link #STEP_LIMIT}).
 */
public class SequencedAssemblyMachine extends WorkableElectricMultiblockMachine {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 16;
    /** Recipe data: the number of steps, loops not counted. */
    public static final String STEPS = "gtmqol_sequenced_steps";
    public static final String TOO_SHORT = "gtmqol.recipe_modifier.sequenced_assembly_too_short";

    /** Recipes need a line longer than their steps plus one, unless it is {@link #MAX_LENGTH} long. */
    public static final RecipeModifier STEP_LIMIT = (machine, recipe) -> {
        if (!(machine instanceof SequencedAssemblyMachine line) || line.length >= MAX_LENGTH) {
            return ModifierFunction.IDENTITY;
        }
        if (recipe.data.getInt(STEPS) < line.length - 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.cancel(Component.translatable(TOO_SHORT));
    };

    private int length;

    public SequencedAssemblyMachine(BlockEntityCreationInfo info) {
        super(info);
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
