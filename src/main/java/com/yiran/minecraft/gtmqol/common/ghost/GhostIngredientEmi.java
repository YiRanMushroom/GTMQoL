package com.yiran.minecraft.gtmqol.common.ghost;

import brachy.modularui.integration.emi.EmiStackConverter;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

/**
 * EMI side of ghost ingredients; only touched when EMI is loaded. Goes through ModularUI's EMI converters, found by
 * each type's value class, in type registration order.
 */
final class GhostIngredientEmi {

    private GhostIngredientEmi() {}

    static @Nullable GhostIngredient<?> fromEmi(Object ingredient) {
        if (!(ingredient instanceof EmiStack emiStack)) return null;
        for (var type : GhostIngredient.types()) {
            GhostIngredient<?> ghost = convert(type, emiStack);
            if (ghost != null) return ghost;
        }
        return null;
    }

    private static <T> @Nullable GhostIngredient<T> convert(GhostIngredient.Type<T> type, EmiStack emiStack) {
        var converter = EmiStackConverter.getForNullable(type.valueClass());
        return converter == null ? null : type.of(converter.convertFrom(emiStack));
    }
}
