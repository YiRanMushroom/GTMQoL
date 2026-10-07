package com.yiran.minecraft.gtmqol.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class MiscRecipes {
    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTCEu.id("steel_24_wrought_iron"))
                .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
                .circuitMeta(24)
                .outputItems(TagPrefix.ingot, GTMaterials.Steel)
                .chancedOutput(TagPrefix.dust, GTMaterials.Ash, 1000)
                .duration(20 * 20 * 4)
                .EUt(GTValues.VA[GTValues.LV])
                .blastFurnaceTemp(1799)
                .save(provider);

        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(GTCEu.id("steel_24_iron"))
                .inputItems(TagPrefix.ingot, GTMaterials.Iron)
                .circuitMeta(24)
                .outputItems(TagPrefix.ingot, GTMaterials.Steel)
                .chancedOutput(TagPrefix.dust, GTMaterials.Ash, 1000)
                .duration(30 * 20 * 4)
                .EUt(GTValues.VA[GTValues.LV])
                .blastFurnaceTemp(1799)
                .save(provider);
    }

    /** Nether stars before IV: the dust goes through the implosion compressor. */
    public static void addNetherStarDust(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GTMQoL.id("nether_star_dust"))
                .inputItems(TagPrefix.dust, GTMaterials.Diamond, 4)
                .inputItems(TagPrefix.dust, GTMaterials.Silver, 16)
                .outputItems(TagPrefix.dust, GTMaterials.NetherStar)
                .duration(20 * 20)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }
}