package com.yiran.minecraft.gtmqol.common.stacklike.mekanism;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.ChemicalStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Chemicals in ModularUI's EMI slots (gtceu's recipe viewer), with Mekanism's own EMI stacks so recipe lookup and
 * other mods' EMI handlers (e.g. AE2 pattern encoding through Applied Mekanistics) see them as chemicals.
 * Only touch this class when EMI and Mekanism are loaded.
 */
public final class ChemicalEmiConverter implements EmiStackConverter.Converter<ChemicalStack> {

    private ChemicalEmiConverter() {}

    public static void register() {
        EmiStackConverter.register(ChemicalStack.class, new ChemicalEmiConverter());
    }

    @Override
    public @Nullable ChemicalStack convertFrom(EmiStack stack) {
        return IMekanismAccess.INSTANCE.emiHelper().asChemicalStack(stack).orElse(null);
    }

    @Override
    public EmiIngredient convertTo(EntryList<ChemicalStack> stacks, float chance, UnaryOperator<ChemicalStack> mapper) {
        if (stacks.isEmpty()) return EmiStack.EMPTY;
        var helper = IMekanismAccess.INSTANCE.emiHelper();
        return EmiIngredient.of(stacks.getStacks().stream().map(mapper).map(helper::createEmiStack).toList())
                .setChance(chance);
    }
}
