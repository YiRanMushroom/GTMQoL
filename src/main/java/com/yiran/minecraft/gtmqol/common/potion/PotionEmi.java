package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;

/**
 * EMI tells potion fluids apart by all their data, so a plain, a splash and a lingering potion of the same effect are
 * three different fluids: looking one up doesn't find the others' recipes, and filling a recipe takes the right one.
 * GTCEu's comparison only looks at the potion (not the container component that PotionContainers adds). EMI orders only its own plugin first, so GTCEu's
 * may register after this one; late recipes are registered after every plugin and before the recipes are indexed.
 */
// Untested: in EMI, recipes for a splash potion fluid don't list the plain or lingering one's, and filling a splash
// potion recipe into a Pattern Encoding Terminal takes the splash potion fluid
@EmiEntrypoint
public final class PotionEmi implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addDeferredRecipes(recipes -> {
            PotionFluid fluid = GTFluids.POTION.get();
            registry.setDefaultComparison(fluid.getSource(), Comparison.compareComponents());
            registry.setDefaultComparison(fluid.getFlowing(), Comparison.compareComponents());
        });
    }
}
