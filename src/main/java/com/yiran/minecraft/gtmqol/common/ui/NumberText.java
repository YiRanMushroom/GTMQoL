package com.yiran.minecraft.gtmqol.common.ui;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Numbers that can get very large (energy, bytes, crafts), short in a window: {@code 232G} instead of
 * {@code 232,417,000,000}, with the full number in the tooltip.
 */
public final class NumberText {

    /** Below this a number is short enough as it is. */
    private static final BigDecimal COMPACT_FROM = BigDecimal.valueOf(10_000);
    private static final String SUFFIXES = "kMGTPEZY";
    private static final MathContext DIGITS = new MathContext(3, RoundingMode.DOWN);

    private NumberText() {}

    /** Every digit, with separators. */
    public static String full(Number value) {
        if (value instanceof Double || value instanceof Float) return FormattingUtil.formatNumbers(value.doubleValue());
        if (value instanceof BigInteger) return FormattingUtil.formatNumbers((Object) value);
        return FormattingUtil.formatNumbers(value.longValue());
    }

    /** Three digits and a suffix, rounded towards zero: {@code 1.23k}, {@code 232G}; past the suffixes {@code 1.23E+30}. */
    public static String compact(Number value) {
        BigDecimal exact;
        if (value instanceof BigInteger big) {
            exact = new BigDecimal(big);
        } else if (value instanceof Double || value instanceof Float) {
            if (!Double.isFinite(value.doubleValue())) return full(value);
            exact = BigDecimal.valueOf(value.doubleValue());
        } else {
            exact = BigDecimal.valueOf(value.longValue());
        }
        BigDecimal size = exact.abs();
        if (size.compareTo(COMPACT_FROM) < 0) return full(value);
        String sign = exact.signum() < 0 ? "-" : "";
        // thousands in it: 1 for k
        int group = (size.precision() - size.scale() - 1) / 3;
        if (group > SUFFIXES.length()) return sign + size.round(DIGITS).stripTrailingZeros();
        return sign + size.movePointLeft(3 * group).round(DIGITS).stripTrailingZeros().toPlainString() +
                SUFFIXES.charAt(group - 1);
    }

    /**
     * A line whose numbers are short, with the same line in full as its tooltip when that differs.
     *
     * @param text the line, its numbers put through the given format
     */
    public static TextWidget<?> line(Function<Function<Number, String>, Component> text) {
        return new TextWidget<>(Text.dynamic(() -> text.apply(NumberText::compact)))
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> {
                    Component full = text.apply(NumberText::full);
                    if (!full.getString().equals(text.apply(NumberText::compact).getString())) tooltip.addLine(full);
                });
    }
}
