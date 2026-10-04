package com.skywrc.am.equality;

import androidx.annotation.NonNull;

import java.util.List;

/** A piece of the real line; infinite ends use {@link Double#NEGATIVE_INFINITY} / {@link Double#POSITIVE_INFINITY}. */
public final class Interval {

    public static final Interval ALL = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, false);

    private static final char MINUS = '−';

    private final double from;
    private final double to;
    private final boolean fromClosed;
    private final boolean toClosed;

    private Interval(double from, double to, boolean fromClosed, boolean toClosed) {
        this.from = from;
        this.to = to;
        this.fromClosed = fromClosed;
        this.toClosed = toClosed;
    }

    @NonNull
    public static Interval open(double from, double to) {
        return new Interval(from, to, false, false);
    }

    @NonNull
    public static Interval point(double x) {
        return new Interval(x, x, true, true);
    }

    /** (−∞, x] */
    @NonNull
    public static Interval upTo(double x) {
        return new Interval(Double.NEGATIVE_INFINITY, x, false, true);
    }

    /** [x, +∞) */
    @NonNull
    public static Interval startingAt(double x) {
        return new Interval(x, Double.POSITIVE_INFINITY, true, false);
    }

    public boolean isPoint() {
        return from == to;
    }

    @NonNull
    @Override
    public String toString() {
        if (isPoint()) {
            return "{" + formatNumber(from) + "}";
        }
        return (fromClosed ? "[" : "(") + formatNumber(from) + ", " + formatNumber(to) + (toClosed ? "]" : ")");
    }

    /** "∅", "{1, 2}" or "(−∞, 1) ∪ (2, +∞)". */
    @NonNull
    public static String format(@NonNull List<Interval> set) {
        if (set.isEmpty()) {
            return "∅";
        }
        boolean allPoints = true;
        for (Interval interval : set) {
            allPoints &= interval.isPoint();
        }
        StringBuilder text = new StringBuilder();
        if (allPoints) {
            text.append('{');
            for (int i = 0; i < set.size(); i++) {
                if (i > 0) {
                    text.append(", ");
                }
                text.append(formatNumber(set.get(i).from));
            }
            return text.append('}').toString();
        }
        for (int i = 0; i < set.size(); i++) {
            if (i > 0) {
                text.append(" ∪ ");
            }
            text.append(set.get(i));
        }
        return text.toString();
    }

    @NonNull
    public static String formatNumber(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return MINUS + "∞";
        }
        if (value == Double.POSITIVE_INFINITY) {
            return "+∞";
        }
        String text = NumberFormatter.format(value);
        return text.startsWith("-") ? MINUS + text.substring(1) : text;
    }
}
