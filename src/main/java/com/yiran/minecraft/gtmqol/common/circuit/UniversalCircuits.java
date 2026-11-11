package com.yiran.minecraft.gtmqol.common.circuit;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * One universal circuit per tier, tagged as that tier's GT circuit. Made from any circuit of the tier in the
 * magical assembler, so the whole tier collapses into one item (handy for AE patterns).
 */
public final class UniversalCircuits {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ItemEntry<Item>[] UNIVERSAL_CIRCUITS = new ItemEntry[TIER_COUNT];

    private UniversalCircuits() {}

    public static void init() {
        for (int tier : ALL_TIERS) {
            String name = VN[tier].toLowerCase(Locale.ROOT) + "_universal_circuit";
            UNIVERSAL_CIRCUITS[tier] = GTMQoLAddon.registrate()
                    .item(name, Item::new)
                    .lang(VN[tier] + " Universal Circuit")
                    .register();
            GTMQoLAddon.optionalItemTag(CustomTags.CIRCUITS_ARRAY[tier], name);
        }
    }

    public static void addRecipes(RecipeOutput provider) {
        for (int tier : ALL_TIERS) {
            MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("circuit_conversion_tier_" + tier))
                    .inputItems(CustomTags.CIRCUITS_ARRAY[tier])
                    .outputItems(UNIVERSAL_CIRCUITS[tier])
                    .circuitMeta(5)
                    .duration(1)
                    .EUt(1)
                    .save(provider);
        }
    }
}
