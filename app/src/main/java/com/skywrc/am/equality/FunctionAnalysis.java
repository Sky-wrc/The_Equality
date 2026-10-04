package com.skywrc.am.equality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sign, extremum and monotonicity of F(x) over the reals, built from the roots
 * already returned by the native solvers.
 */
public final class FunctionAnalysis {

    public enum GraphType {
        STRAIGHT_LINE,
        PARABOLA
    }

    public enum Branches {
        UP,
        DOWN
    }

    public enum ExtremumType {
        MINIMUM,
        MAXIMUM
    }

    private static final double ZERO_EPSILON = 1e-12;

    private final GraphType graphType;
    @Nullable
    private final Branches branches;
    private final List<Interval> positive;
    private final List<Interval> negative;
    private final List<Interval> zero;
    @Nullable
    private final ExtremumType extremumType;
    private final double extremumX;
    private final double extremumY;
    private final List<Interval> increasing;
    private final List<Interval> decreasing;

    private FunctionAnalysis(@NonNull GraphType graphType, @Nullable Branches branches,
                             @NonNull List<Interval> positive, @NonNull List<Interval> negative,
                             @NonNull List<Interval> zero, @Nullable ExtremumType extremumType,
                             double extremumX, double extremumY,
                             @NonNull List<Interval> increasing, @NonNull List<Interval> decreasing) {
        this.graphType = graphType;
        this.branches = branches;
        this.positive = Collections.unmodifiableList(positive);
        this.negative = Collections.unmodifiableList(negative);
        this.zero = Collections.unmodifiableList(zero);
        this.extremumType = extremumType;
        this.extremumX = extremumX;
        this.extremumY = extremumY;
        this.increasing = Collections.unmodifiableList(increasing);
        this.decreasing = Collections.unmodifiableList(decreasing);
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
                null, 0, 0,
                new ArrayList<>(), new ArrayList<>()
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

        double vertexY = a * vertexX * vertexX + b * vertexX + c;
        if (Math.abs(vertexY) < ZERO_EPSILON) {
            vertexY = 0;
        }
        List<Interval> left = list(Interval.upTo(vertexX));
        List<Interval> right = list(Interval.startingAt(vertexX));
        return new FunctionAnalysis(
                GraphType.PARABOLA, a > 0 ? Branches.UP : Branches.DOWN,
                a > 0 ? outside : inside,
                a > 0 ? inside : outside,
                zero,
                a > 0 ? ExtremumType.MINIMUM : ExtremumType.MAXIMUM,
                vertexX, vertexY,
                a > 0 ? right : left,
                a > 0 ? left : right
        );
    }

    @NonNull
    private static FunctionAnalysis constant(double value) {
        return new FunctionAnalysis(
                GraphType.STRAIGHT_LINE, null,
                value > 0 ? list(Interval.ALL) : new ArrayList<>(),
                value < 0 ? list(Interval.ALL) : new ArrayList<>(),
                value == 0 ? list(Interval.ALL) : new ArrayList<>(),
                null, 0, 0,
                new ArrayList<>(), new ArrayList<>()
        );
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

    /** Null for linear functions. */
    @Nullable
    public ExtremumType getExtremumType() {
        return extremumType;
    }

    public double getExtremumX() {
        return extremumX;
    }

    public double getExtremumY() {
        return extremumY;
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
}
