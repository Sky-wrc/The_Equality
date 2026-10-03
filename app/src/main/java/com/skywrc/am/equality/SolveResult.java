package com.skywrc.am.equality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public final class SolveResult {

    public enum Status {
        ROOTS,
        NO_ROOTS,
        INFINITE_ROOTS,
        NOT_QUADRATIC,
        UNDETERMINED
    }

    public static final class Root {
        private final double re;
        private final double im;

        public Root(double re, double im) {
            this.re = re;
            this.im = im;
        }

        public double getRe() {
            return re;
        }

        public double getIm() {
            return im;
        }
    }

    private final Status status;
    private final List<Root> roots;
    @Nullable
    private final Double discriminant;
    private final boolean discriminantNegative;
    @Nullable
    private final Double quotient;
    @Nullable
    private final Double modulo;

    SolveResult(@NonNull Status status, @NonNull List<Root> roots,
                @Nullable Double discriminant, boolean discriminantNegative) {
        this(status, roots, discriminant, discriminantNegative, null, null);
    }

    private SolveResult(@NonNull Status status, @NonNull List<Root> roots,
                        @Nullable Double discriminant, boolean discriminantNegative,
                        @Nullable Double quotient, @Nullable Double modulo) {
        this.status = status;
        this.roots = Collections.unmodifiableList(roots);
        this.discriminant = discriminant;
        this.discriminantNegative = discriminantNegative;
        this.quotient = quotient;
        this.modulo = modulo;
    }

    @NonNull
    SolveResult withDivision(@Nullable Double quotient, @Nullable Double modulo) {
        return new SolveResult(status, roots, discriminant, discriminantNegative, quotient, modulo);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @NonNull
    public List<Root> getRoots() {
        return roots;
    }

    /** Discriminant value returned by the solver, or null when it reports only that D is negative. */
    @Nullable
    public Double getDiscriminant() {
        return discriminant;
    }

    public boolean isDiscriminantNegative() {
        return discriminantNegative;
    }

    /** Integer division result of -b / a in N and Z, or null when it should not be shown. */
    @Nullable
    public Double getQuotient() {
        return quotient;
    }

    /** Modulo of -b / a in N and Z, or null when it should not be shown. */
    @Nullable
    public Double getModulo() {
        return modulo;
    }
}
