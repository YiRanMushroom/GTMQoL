package com.yiran.minecraft.gtmqol.integration.ae2.stacklike;

import com.yiran.minecraft.gtmqol.common.stacklike.GenericStackLikeRecipeCapability;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalStackLike;

import appeng.api.stacks.AEKey;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import org.jetbrains.annotations.Nullable;

/** Mekanism chemicals in ME networks, through Applied Mekanistics. Only touch this class when appmek is loaded. */
public final class AppMekChemicalBridge implements AEStackLikeBridge<ChemicalStack, ChemicalStackIngredient> {

    public static final AppMekChemicalBridge INSTANCE = new AppMekChemicalBridge();

    private AppMekChemicalBridge() {}

    @Override
    public GenericStackLikeRecipeCapability<ChemicalStack, ChemicalStackIngredient> cap() {
        return ChemicalStackLike.CAP;
    }

    @Override
    public boolean isKey(AEKey key) {
        return key instanceof MekanismKey;
    }

    @Override
    public @Nullable AEKey toKey(ChemicalStack stack) {
        return stack.isEmpty() ? null : MekanismKey.of(stack);
    }

    @Override
    public ChemicalStack fromKey(AEKey key, long amount) {
        return ((MekanismKey) key).withAmount(amount);
    }
}
