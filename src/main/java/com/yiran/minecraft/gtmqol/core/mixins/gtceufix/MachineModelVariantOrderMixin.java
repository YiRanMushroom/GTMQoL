package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder.PartialState;

import net.minecraft.world.level.block.state.properties.Property;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * gtceu bug, only in data generation: the order of a machine model's variants adds up the differences of all properties,
 * which isn't an order, and Java's sort throws on it from about 32 variants with two properties. The pattern matrix's
 * controller has a casing per tier and a recipe status. The first property that differs decides here. Remove once gtceu
 * fixes it.
 */
@Mixin(value = PartialState.class, remap = false)
public abstract class MachineModelVariantOrderMixin {

    @ModifyReturnValue(method = "comparingByProperties", at = @At("RETURN"))
    private static Comparator<PartialState<?>> gtmqol$properOrder(Comparator<PartialState<?>> original) {
        return MachineModelVariantOrderMixin::gtmqol$compare;
    }

    @Unique
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int gtmqol$compare(PartialState<?> first, PartialState<?> second) {
        SortedSet<Property<?>> properties = new TreeSet<>(first.getSetStates().comparator().reversed());
        properties.addAll(first.getSetStates().keySet());
        properties.addAll(second.getSetStates().keySet());
        for (Property<?> property : properties) {
            Comparable firstValue = first.getSetStates().get(property);
            Comparable secondValue = second.getSetStates().get(property);
            if (firstValue == secondValue) continue;
            if (firstValue == null) return -1;
            if (secondValue == null) return 1;
            int compared = firstValue.compareTo(secondValue);
            if (compared != 0) return compared;
        }
        return 0;
    }
}
