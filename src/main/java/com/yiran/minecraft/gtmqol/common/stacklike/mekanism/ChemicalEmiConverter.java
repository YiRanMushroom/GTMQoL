package com.yiran.minecraft.gtmqol.common.stacklike.mekanism;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import mekanism.api.chemical.ChemicalStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Chemicals in ModularUI's EMI slots (gtceu's recipe viewer). Mekanism 10.4 has no EMI integration, its chemicals
 * only exist in EMI through JEMI (EMI loading JEI plugins), so these are JEMI's stacks: rendered by Mekanism's JEI
 * renderer, and what recipe lookup and other mods' EMI handlers (e.g. AE2 pattern encoding through Applied
 * Mekanistics) see. Uses EMI internals ({@code dev.emi.emi.jemi}, not API). Only touch this class when EMI, JEI and
 * Mekanism are loaded.
 */
@SuppressWarnings("rawtypes")
public final class ChemicalEmiConverter implements EmiStackConverter.Converter<ChemicalStack> {

    private ChemicalEmiConverter() {}

    public static void register() {
        EmiStackConverter.register(ChemicalStack.class, new ChemicalEmiConverter());
    }

    @Override
    public @Nullable ChemicalStack convertFrom(EmiStack stack) {
        return stack instanceof JemiStack<?> jemi && jemi.ingredient instanceof ChemicalStack<?> chemical ?
                chemical : null;
    }

    @Override
    public EmiIngredient convertTo(EntryList<ChemicalStack> stacks, float chance, UnaryOperator<ChemicalStack> mapper) {
        // Null until JEI's runtime exists.
        if (stacks.isEmpty() || JemiPlugin.runtime == null) return EmiStack.EMPTY;
        return EmiIngredient.of(stacks.getStacks().stream()
                .map(mapper)
                // AE2 encoding reads the EMI amount, not the wrapped stack's.
                .map(stack -> JemiUtil.getStack(stack).setAmount(stack.getAmount()))
                .filter(stack -> !stack.isEmpty())
                .toList())
                .setChance(chance);
    }
}
