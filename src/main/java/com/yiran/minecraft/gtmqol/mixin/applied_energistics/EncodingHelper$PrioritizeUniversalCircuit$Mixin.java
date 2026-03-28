package com.yiran.minecraft.gtmqol.mixin.applied_energistics;

import appeng.api.stacks.AEItemKey;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.yiran.minecraft.gtmqol.data.QoLItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(EncodingHelper.class)
public class EncodingHelper$PrioritizeUniversalCircuit$Mixin {
    @Unique
    private static final Supplier<Set<Item>> UNIVERSAL_CIRCUIT_SUPPLIER = Suppliers.memoize(() ->
            Arrays.stream(QoLItems.getUNIVERSAL_CIRCUITS())
                    .map(RegistryEntry::get)
                    .collect(Collectors.toUnmodifiableSet())
    );

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Comparator;comparing(Ljava/util/function/Function;)Ljava/util/Comparator;"))
    private static Comparator<GridInventoryEntry> prioritizeUniversalCircuit(Comparator<GridInventoryEntry> original) {
        return original.thenComparing(entry -> {
            if (entry.getWhat() instanceof AEItemKey itemKey) {
                return UNIVERSAL_CIRCUIT_SUPPLIER.get().contains(itemKey.getItem());
            }
            return false;
        });
    }
}
