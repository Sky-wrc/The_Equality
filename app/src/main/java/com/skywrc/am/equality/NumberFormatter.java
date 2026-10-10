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

    /** Formats the roots -b/2a ± √D/2a as "center ± √D/|2a|", with i√|D| for a negative D. */
    public static String formatRootForm(double center, double discriminant, double a) {
        String radical = "√" + format(Math.abs(discriminant));
        if (discriminant < 0) {
            radical = "i" + radical;
        }
        double denominator = Math.abs(2 * a);
        String deviation = denominator == 1 ? radical : radical + "/" + format(denominator);
        return center == 0 ? "±" + deviation : format(center) + " ± " + deviation;
    }
}
