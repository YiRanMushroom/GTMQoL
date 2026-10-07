package com.yiran.minecraft.gtmqol.core.mixins.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.common.circuit.UniversalCircuits;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * When a recipe ingredient is a circuit tag, the pattern encoder picks the matching network item with the highest
 * {@code ENTRY_COMPARATOR} rank (craftable, undamaged, stored amount). Put "is a universal circuit" right after
 * craftable, so patterns use the universal circuit. Ported from the v7 {@code EncodingHelper} mixin.
 */
@Mixin(value = EncodingHelper.class, remap = false)
public class EncodingHelperMixin {

    @Unique
    private static final Supplier<Set<Item>> gtmqol$UNIVERSAL_CIRCUITS = Suppliers.memoize(() ->
            Arrays.stream(UniversalCircuits.UNIVERSAL_CIRCUITS)
                    .filter(Objects::nonNull)
                    .map(ItemEntry::get)
                    .collect(Collectors.toUnmodifiableSet()));

    @ModifyExpressionValue(method = "<clinit>",
                           at = @At(value = "INVOKE",
                                    target = "Ljava/util/Comparator;comparing(Ljava/util/function/Function;)Ljava/util/Comparator;"))
    private static Comparator<GridInventoryEntry> gtmqol$prioritizeUniversalCircuit(Comparator<GridInventoryEntry> original) {
        return original.thenComparing(entry ->
                entry.getWhat() instanceof AEItemKey key && gtmqol$UNIVERSAL_CIRCUITS.get().contains(key.getItem()));
    }
}
