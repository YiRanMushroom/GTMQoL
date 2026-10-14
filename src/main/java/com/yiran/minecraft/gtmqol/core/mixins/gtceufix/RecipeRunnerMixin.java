package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * gtceu bug: the OR and XOR chance logics compute {@code times * chance} as an int. With enough runs (above about
 * 215k for a 100% output, parallel times batch) it overflows to a negative number, so the roll gives no guaranteed
 * part and never passes: the chanced outputs are silently lost, while the recipe still shows them. Roll in chunks
 * small enough not to overflow, with gtceu's own logic. Remove once gtceu fixes it.
 */
@Mixin(value = RecipeRunner.class, remap = false)
public abstract class RecipeRunnerMixin {

    @WrapOperation(method = "fillContentMatchList", at = @At(value = "INVOKE",
            target = "Lcom/gregtechceu/gtceu/api/recipe/chance/logic/ChanceLogic;roll(Lcom/gregtechceu/gtceu/api/capability/recipe/RecipeCapability;Ljava/util/List;Lit/unimi/dsi/fastutil/objects/Object2IntMap;I)Ljava/util/List;"))
    private List<Content> gtmqol$rollInChunks(ChanceLogic logic, RecipeCapability<?> cap, List<Content> entries,
                                              Object2IntMap<?> cache, int times, Operation<List<Content>> original) {
        int maxChance = ChanceLogic.getMaxChancedValue();
        for (Content entry : entries) maxChance = Math.max(maxChance, Math.max(entry.chance(), entry.maxChance()));
        int chunk = Integer.MAX_VALUE / maxChance;
        if (times <= chunk) return original.call(logic, cap, entries, cache, times);
        List<Content> rolled = new ArrayList<>();
        for (int left = times; left > 0; left -= chunk) {
            rolled.addAll(original.call(logic, cap, entries, cache, Math.min(left, chunk)));
        }
        return gtmqol$merge(rolled);
    }

    /**
     * Every chunk adds its own entries, and gtceu's item output is quadratic in their number, so a huge batch
     * lagged. Sum the same item or fluid back up, splitting only where the total doesn't fit in an int.
     */
    @Unique
    private static List<Content> gtmqol$merge(List<Content> rolled) {
        List<Content> merged = new ArrayList<>();
        List<Content> kinds = new ArrayList<>();
        LongArrayList totals = new LongArrayList();
        for (Content content : rolled) {
            long amount = gtmqol$amount(content.content());
            if (amount < 0) {
                merged.add(content);
                continue;
            }
            int i = 0;
            while (i < kinds.size() && !gtmqol$sameKind(kinds.get(i).content(), content.content())) i++;
            if (i == kinds.size()) {
                kinds.add(content);
                totals.add(amount);
            } else {
                totals.set(i, totals.getLong(i) + amount);
            }
        }
        for (int i = 0; i < kinds.size(); i++) {
            Content kind = kinds.get(i);
            for (long left = totals.getLong(i); left > 0; left -= Integer.MAX_VALUE) {
                int amount = (int) Math.min(left, Integer.MAX_VALUE);
                merged.add(new Content(gtmqol$withAmount(kind.content(), amount), kind.chance(), kind.maxChance()));
            }
        }
        return merged;
    }

    /** A plain item's or fluid's amount; -1 for anything else (ranged outputs, other capabilities), kept as is. */
    @Unique
    private static long gtmqol$amount(Object content) {
        if (content instanceof SizedIngredient item) {
            return item.ingredient().getCustomIngredient() instanceof IntProviderIngredient ? -1 : item.count();
        }
        if (content instanceof SizedFluidIngredient fluid) {
            return fluid.ingredient() instanceof IntProviderFluidIngredient || fluid.ingredient().hasNoFluids() ? -1 :
                    fluid.amount();
        }
        return -1;
    }

    // gtceu's copies keep the ingredient and only change the amount.
    @Unique
    private static boolean gtmqol$sameKind(Object a, Object b) {
        if (a instanceof SizedIngredient x && b instanceof SizedIngredient y) return x.ingredient().equals(y.ingredient());
        if (a instanceof SizedFluidIngredient x && b instanceof SizedFluidIngredient y) {
            return x.ingredient().equals(y.ingredient());
        }
        return false;
    }

    @Unique
    private static Object gtmqol$withAmount(Object content, int amount) {
        if (content instanceof SizedIngredient item) return new SizedIngredient(item.ingredient(), amount);
        return new SizedFluidIngredient(((SizedFluidIngredient) content).ingredient(), amount);
    }
}
