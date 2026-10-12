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

    /** Writes a non-negative index with Unicode subscript digits, e.g. 12 → ₁₂. */
    public static String subscript(int index) {
        StringBuilder result = new StringBuilder();
        for (char digit : String.valueOf(index).toCharArray()) {
            result.append((char) ('\u2080' + (digit - '0')));
        }
        return result.toString();
    }

    /**
     * Formats one of the roots -b/2a ± √D/2a as "center + √D/|2a|" or "center − √D/|2a|",
     * with i√|D| for a negative D.
     */
    public static String formatRootForm(double center, double discriminant, double a, boolean plus) {
        String radical = "√" + format(Math.abs(discriminant));
        if (discriminant < 0) {
            radical = "i" + radical;
        }
        double denominator = Math.abs(2 * a);
        String deviation = denominator == 1 ? radical : radical + "/" + format(denominator);
        if (center == 0) {
            return plus ? deviation : "−" + deviation;
        }
        return format(center) + (plus ? " + " : " − ") + deviation;
    }
}
