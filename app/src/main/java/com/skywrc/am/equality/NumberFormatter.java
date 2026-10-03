package com.skywrc.am.equality;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

public final class NumberFormatter {

    private NumberFormatter() {
    }

    @NonNull
    public static String format(double value) {
        if (value == 0) {
            return "0";
        }
        if (Math.rint(value) == value && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        String rounded = new BigDecimal(String.format(Locale.ROOT, "%.6f", value))
                .stripTrailingZeros()
                .toPlainString();
        return "0".equals(rounded) || "-0".equals(rounded) ? String.valueOf(value) : rounded;
    }
}
