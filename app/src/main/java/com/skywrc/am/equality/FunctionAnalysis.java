package com.skywrc.am.equality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sign, extrema and monotonicity of F(x) over the reals, built from the roots
 * already returned by the native solvers.
 */
public final class FunctionAnalysis {

    public enum GraphType {
        STRAIGHT_LINE,
        PARABOLA,
        CUBIC_PARABOLA
    }

    /** For a parabola where the branches point; for a cubic parabola UP means it rises from −∞ to +∞. */
    public enum Branches {
        UP,
        DOWN
    }

    public enum ExtremumType {
        MINIMUM,
        MAXIMUM
    }

    public static final class Extremum {
        private final ExtremumType type;
        private final double x;
        private final double y;

        Extremum(@NonNull ExtremumType type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        @NonNull
        public ExtremumType getType() {
            return type;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private static final double ZERO_EPSILON = 1e-12;

    private final GraphType graphType;
    @Nullable
    private final Branches branches;
    private final List<Interval> positive;
    private final List<Interval> negative;
    private final List<Interval> zero;
    private final List<Extremum> extrema;
    private final List<Interval> increasing;
    private final List<Interval> decreasing;
    @Nullable
    private final Double inflectionX;
    private final double inflectionY;

    private FunctionAnalysis(@NonNull GraphType graphType, @Nullable Branches branches,
                             @NonNull List<Interval> positive, @NonNull List<Interval> negative,
                             @NonNull List<Interval> zero, @NonNull List<Extremum> extrema,
                             @NonNull List<Interval> increasing, @NonNull List<Interval> decreasing,
                             @Nullable Double inflectionX, double inflectionY) {
        this.graphType = graphType;
        this.branches = branches;
        this.positive = Collections.unmodifiableList(positive);
        this.negative = Collections.unmodifiableList(negative);
        this.zero = Collections.unmodifiableList(zero);
        this.extrema = Collections.unmodifiableList(extrema);
        this.increasing = Collections.unmodifiableList(increasing);
        this.decreasing = Collections.unmodifiableList(decreasing);
        this.inflectionX = inflectionX;
        this.inflectionY = inflectionY;
    }

    /** F(x) = ax + b; {@code root} is the solver's root, ignored when a = 0. */
    @NonNull
    public static FunctionAnalysis linear(double a, double b, double root) {
        if (a == 0) {
            return constant(b);
        }
        List<Interval> right = list(Interval.open(root, Double.POSITIVE_INFINITY));
        List<Interval> left = list(Interval.open(Double.NEGATIVE_INFINITY, root));
        return new FunctionAnalysis(
                GraphType.STRAIGHT_LINE, null,
                a > 0 ? right : left,
                a > 0 ? left : right,
                list(Interval.point(root)),
                new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                null, 0
        );
    }

    /**
     * F(x) = ax² + bx + c with a ≠ 0; {@code roots} are the solver's real roots,
     * {@code vertexX} is the root of F'(x) = 2ax + b.
     */
    @NonNull
    public static FunctionAnalysis quadratic(double a, double b, double c,
                                             @NonNull List<Double> roots, double vertexX) {
        List<Double> sorted = new ArrayList<>(roots);
        Collections.sort(sorted);

        List<Interval> outside;
        List<Interval> inside = new ArrayList<>();
        List<Interval> zero = new ArrayList<>();
        if (sorted.size() >= 2) {
            double x1 = sorted.get(0);
            double x2 = sorted.get(sorted.size() - 1);
            outside = list(Interval.open(Double.NEGATIVE_INFINITY, x1), Interval.open(x2, Double.POSITIVE_INFINITY));
            inside.add(Interval.open(x1, x2));
            zero.add(Interval.point(x1));
            zero.add(Interval.point(x2));
        } else if (sorted.size() == 1) {
            double x0 = sorted.get(0);
            outside = list(Interval.open(Double.NEGATIVE_INFINITY, x0), Interval.open(x0, Double.POSITIVE_INFINITY));
            zero.add(Interval.point(x0));
        } else {
            outside = list(Interval.ALL);
        }

        double vertexY = snapToZero(a * vertexX * vertexX + b * vertexX + c);
        List<Interval> left = list(Interval.upTo(vertexX));
        List<Interval> right = list(Interval.startingAt(vertexX));
        List<Extremum> extrema = new ArrayList<>();
        extrema.add(new Extremum(a > 0 ? ExtremumType.MINIMUM : ExtremumType.MAXIMUM, vertexX, vertexY));
        return new FunctionAnalysis(
                GraphType.PARABOLA, a > 0 ? Branches.UP : Branches.DOWN,
                a > 0 ? outside : inside,
                a > 0 ? inside : outside,
                zero,
                extrema,
                a > 0 ? right : left,
                a > 0 ? left : right,
                null, 0
        );
    }

    /**
     * F(x) = ax³ + bx² + cx + d with a ≠ 0; {@code roots} are the solver's real roots,
     * {@code criticalPoints} the real roots of F'(x) = 3ax² + 2bx + c and
     * {@code inflectionX} the root of F''(x) = 6ax + 2b.
     */
    @NonNull
    public static FunctionAnalysis cubic(double a, double b, double c, double d,
                                         @NonNull List<Double> roots, @NonNull List<Double> criticalPoints,
                                         double inflectionX) {
        double[] f = {a, b, c, d};
        double[] derivative = {3 * a, 2 * b, c};

        List<Double> zeros = distinctSorted(roots);
        List<Interval> positive = new ArrayList<>();
        List<Interval> negative = new ArrayList<>();
        List<Interval> zero = new ArrayList<>();
        List<Double> bounds = withInfiniteEnds(zeros);
        for (int i = 0; i + 1 < bounds.size(); i++) {
            double from = bounds.get(i);
            double to = bounds.get(i + 1);
            (evaluate(f, sampleBetween(from, to)) > 0 ? positive : negative).add(Interval.open(from, to));
        }
        for (double x : zeros) {
            zero.add(Interval.point(x));
        }

        // Pieces between critical points; neighbours with the same direction are merged.
        List<Double> pieceStarts = new ArrayList<>();
        List<Boolean> pieceRising = new ArrayList<>();
        List<Double> criticalBounds = withInfiniteEnds(distinctSorted(criticalPoints));
        List<Extremum> extrema = new ArrayList<>();
        for (int i = 0; i + 1 < criticalBounds.size(); i++) {
            double from = criticalBounds.get(i);
            boolean rising = evaluate(derivative, sampleBetween(from, criticalBounds.get(i + 1))) > 0;
            int last = pieceRising.size() - 1;
            if (last >= 0 && pieceRising.get(last) == rising) {
                continue;
            }
            if (last >= 0) {
                extrema.add(new Extremum(rising ? ExtremumType.MINIMUM : ExtremumType.MAXIMUM,
                        from, snapToZero(evaluate(f, from))));
            }
            pieceStarts.add(from);
            pieceRising.add(rising);
        }
        List<Interval> increasing = new ArrayList<>();
        List<Interval> decreasing = new ArrayList<>();
        for (int i = 0; i < pieceStarts.size(); i++) {
            double to = i + 1 < pieceStarts.size() ? pieceStarts.get(i + 1) : Double.POSITIVE_INFINITY;
            (pieceRising.get(i) ? increasing : decreasing).add(Interval.closedAtFiniteEnds(pieceStarts.get(i), to));
        }

        return new FunctionAnalysis(
                GraphType.CUBIC_PARABOLA, a > 0 ? Branches.UP : Branches.DOWN,
                positive, negative, zero,
                extrema,
                increasing, decreasing,
                inflectionX, snapToZero(evaluate(f, inflectionX))
        );
    }

    @NonNull
    private static FunctionAnalysis constant(double value) {
        return new FunctionAnalysis(
                GraphType.STRAIGHT_LINE, null,
                value > 0 ? list(Interval.ALL) : new ArrayList<>(),
                value < 0 ? list(Interval.ALL) : new ArrayList<>(),
                value == 0 ? list(Interval.ALL) : new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(),
                null, 0
        );
    }

    /** Coefficients go from the highest power down to x⁰. */
    private static double evaluate(@NonNull double[] coefficients, double x) {
        double value = 0;
        for (double coefficient : coefficients) {
            value = value * x + coefficient;
        }
        return value;
    }

    private static double sampleBetween(double from, double to) {
        boolean fromInfinite = Double.isInfinite(from);
        boolean toInfinite = Double.isInfinite(to);
        if (fromInfinite && toInfinite) {
            return 0;
        }
        if (fromInfinite) {
            return to - 1;
        }
        if (toInfinite) {
            return from + 1;
        }
        return (from + to) / 2;
    }

    private static double snapToZero(double value) {
        return Math.abs(value) < ZERO_EPSILON ? 0 : value;
    }

    @NonNull
    private static List<Double> distinctSorted(@NonNull List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        List<Double> distinct = new ArrayList<>();
        for (double value : sorted) {
            if (distinct.isEmpty() || distinct.get(distinct.size() - 1) != value) {
                distinct.add(value);
            }
        }
        return distinct;
    }

    @NonNull
    private static List<Double> withInfiniteEnds(@NonNull List<Double> points) {
        List<Double> bounds = new ArrayList<>();
        bounds.add(Double.NEGATIVE_INFINITY);
        bounds.addAll(points);
        bounds.add(Double.POSITIVE_INFINITY);
        return bounds;
    }

    @NonNull
    private static List<Interval> list(@NonNull Interval... intervals) {
        List<Interval> result = new ArrayList<>();
        Collections.addAll(result, intervals);
        return result;
    }

    @NonNull
    public GraphType getGraphType() {
        return graphType;
    }

    /** Null for a straight line. */
    @Nullable
    public Branches getBranches() {
        return branches;
    }

    @NonNull
    public List<Interval> getPositive() {
        return positive;
    }

    @NonNull
    public List<Interval> getNegative() {
        return negative;
    }

    @NonNull
    public List<Interval> getZero() {
        return zero;
    }

    /** Local extrema from left to right; empty for linear and monotonic cubic functions. */
    @NonNull
    public List<Extremum> getExtrema() {
        return extrema;
    }

    /** Empty for linear functions. */
    @NonNull
    public List<Interval> getIncreasing() {
        return increasing;
    }

    /** Empty for linear functions. */
    @NonNull
    public List<Interval> getDecreasing() {
        return decreasing;
    }

    /** Only cubic functions have an inflection point. */
    @Nullable
    public Double getInflectionX() {
        return inflectionX;
    }

    public double getInflectionY() {
        return inflectionY;
    }
}
