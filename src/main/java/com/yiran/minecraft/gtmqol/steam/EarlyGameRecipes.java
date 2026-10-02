package com.yiran.minecraft.gtmqol.steam;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Cheap ULV/LV buses and hatches so steam-age multiblocks (large boilers) can be built before electricity:
 * machine casing + chest is a bus, machine casing + glass is a hatch; the chest/glass on top is an input, below
 * is an output.
 */
public final class EarlyGameRecipes {

    private EarlyGameRecipes() {}

    public static void addRecipes(RecipeOutput provider) {
        for (int tier : new int[] { ULV, LV }) {
            ItemLike casing = tier == ULV ? GTBlocks.MACHINE_CASING_ULV.get() : GTBlocks.MACHINE_CASING_LV.get();
            input(provider, GTMachines.ITEM_IMPORT_BUS[tier], Items.CHEST, casing);
            output(provider, GTMachines.ITEM_EXPORT_BUS[tier], Items.CHEST, casing);
            input(provider, GTMachines.FLUID_IMPORT_HATCH[tier], Items.GLASS, casing);
            output(provider, GTMachines.FLUID_EXPORT_HATCH[tier], Items.GLASS, casing);
        }
    }

    private static void input(RecipeOutput provider, MachineEntry<MachineDefinition> machine, ItemLike top,
                              ItemLike casing) {
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("easy_" + machine.get().getName()),
                machine.asStack(), "X", "C", 'X', top, 'C', casing);
    }

    private static void output(RecipeOutput provider, MachineEntry<MachineDefinition> machine, ItemLike bottom,
                               ItemLike casing) {
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("easy_" + machine.get().getName()),
                machine.asStack(), "C", "X", 'X', bottom, 'C', casing);
    }
}
