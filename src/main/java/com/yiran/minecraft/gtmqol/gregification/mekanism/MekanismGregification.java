package com.yiran.minecraft.gtmqol.gregification.mekanism;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalStackLike;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.MultiblockShape;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Two of Mekanism's machines, as 3x3x3 multiblocks taking any part, to try chemicals as a recipe capability.
 * Recipes run at {@code VA[LV]} for as long as Mekanism's machine takes.
 */
public final class MekanismGregification {

    private static final String MEK = "mekanism";
    private static final ResourceLocation CONFIGURATOR = ResourceLocation.fromNamespaceAndPath(MEK, "configurator");

    private MekanismGregification() {}

    public static List<ForeignMachineType> types() {
        return List.of(
                ForeignMachineType.multiblock("mek_chemical_oxidizer", "Mek Chemical Oxidizer",
                        MekanismRecipeTypes.TYPE_OXIDIZING, MekanismGregification::convertOxidizing,
                        1, 0, 0, 0, GTSoundEntries.CHEMICAL,
                        builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW), CONFIGURATOR,
                        ResourceLocation.fromNamespaceAndPath(MEK, "chemical_oxidizer"), MultiblockShape.ANY_PARTS)
                        .withRecipeType(builder -> builder.setMaxSize(IO.OUT, ChemicalStackLike.CAP, 1)),
                ForeignMachineType.multiblock("mek_metallurgic_infuser", "Mek Metallurgic Infuser",
                        MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING, MekanismGregification::convertInfusing,
                        1, 1, 0, 0, GTSoundEntries.FURNACE,
                        builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW), CONFIGURATOR,
                        ResourceLocation.fromNamespaceAndPath(MEK, "metallurgic_infuser"), MultiblockShape.ANY_PARTS)
                        .withRecipeType(builder -> builder.setMaxSize(IO.IN, ChemicalStackLike.CAP, 1)));
    }

    private static boolean convertOxidizing(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ItemStackToChemicalRecipe recipe) || recipe.isIncomplete()) return false;
        // one per input item; Mekanism's own are all the same
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        builder.inputItems(recipe.getInput().ingredient());
        builder.output(ChemicalStackLike.CAP, ChemicalStackLike.TYPE.of(outputs.getFirst()));
        builder.EUt(VA[LV]).duration(TileEntityChemicalOxidizer.BASE_TICKS_REQUIRED);
        return true;
    }

    private static boolean convertInfusing(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof ItemStackChemicalToItemStackRecipe recipe) || recipe.isIncomplete()) {
            return false;
        }
        var outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) return false;
        int ticks = TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED;
        var chemical = recipe.getChemicalInput();
        // all at once instead of every tick
        if (recipe.perTickUsage()) {
            chemical = ChemicalStackLike.TYPE.withAmount(chemical, chemical.amount() * ticks);
        }
        builder.inputItems(recipe.getItemInput().ingredient());
        builder.input(ChemicalStackLike.CAP, chemical);
        builder.outputItems(outputs.getFirst());
        builder.EUt(VA[LV]).duration(ticks);
        return true;
    }
}
