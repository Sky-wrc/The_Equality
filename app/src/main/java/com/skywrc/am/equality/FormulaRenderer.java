package com.skywrc.am.equality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Renders a polynomial equation whose coefficients go from the highest power down to x⁰,
 * e.g. labels {a, b, c} give "ax²+bx+c=0". A null value keeps the coefficient letter.
 */
public final class FormulaRenderer {

    private static final char[] SUPERSCRIPT_DIGITS = {
            '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹'
    };
    private static final char MINUS = '−';

    private FormulaRenderer() {
    }

    @NonNull
    public static String render(@NonNull List<String> labels, @NonNull List<Double> values) {
        int degree = labels.size() - 1;
        StringBuilder formula = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            int power = degree - i;
            Double value = values.get(i);
            if (value == null) {
                if (formula.length() > 0) {
                    formula.append('+');
                }
                formula.append(labels.get(i)).append(variable(power));
                continue;
            }
            if (value == 0) {
                continue;
            }
            double abs = Math.abs(value);
            if (value < 0) {
                formula.append(MINUS);
            } else if (formula.length() > 0) {
                formula.append('+');
            }
            if (abs != 1 || power == 0) {
                formula.append(NumberFormatter.format(abs));
            }
            formula.append(variable(power));
        }
        if (formula.length() == 0) {
            formula.append('0');
        }
        return formula.append("=0").toString();
    }

    @Nullable
    public static Double parse(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }
        String raw = text.toString().trim().replace(',', '.');
        if (raw.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(raw);
            return Double.isNaN(value) || Double.isInfinite(value) ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NonNull
    private static String variable(int power) {
        if (power == 0) {
            return "";
        }
        if (power == 1) {
            return "x";
        }
        StringBuilder result = new StringBuilder("x");
        for (char digit : String.valueOf(power).toCharArray()) {
            result.append(SUPERSCRIPT_DIGITS[digit - '0']);
        }
        return result.toString();
    }
}
