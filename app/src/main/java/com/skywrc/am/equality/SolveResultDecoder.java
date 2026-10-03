package com.skywrc.am.equality;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates the raw output of the C solvers in app/c into a {@link SolveResult}.
 * The marker values below mirror the ones written by linear.c and quadratic.c.
 */
public final class SolveResultDecoder {

    private static final double Z_NOT_INTEGER = -0.000001;
    private static final double Z_NO_INTEGER_ROOT = -5;
    private static final double N_NO_NATURAL_ROOT = -8;
    private static final double N_SMALLER_NOT_NATURAL = -3;
    private static final double N_BIGGER_NOT_NATURAL = -4;
    private static final double N_NO_MODULO = -4;
    private static final double Z_NO_MODULO = -4.1;
    private static final double ROOT_EPSILON = 1e-9;

    private SolveResultDecoder() {
    }

    @NonNull
    public static SolveResult decodeLinear(double a, double b, char domain, @NonNull double[] raw) {
        double n1 = raw[0];
        double n2 = raw[1];

        if (a == 0) {
            return b == 0
                    ? result(SolveResult.Status.INFINITE_ROOTS, new ArrayList<>(), null, false)
                    : noRoots(null, false);
        }

        // In N and Z linear_solver writes the integer division result to n1 and the modulo to n2.
        // For |a| < 1 the modulo can't be found: n2 is -4.1 in Z and -4 in N.
        switch (domain) {
            case 'C':
            case 'R':
                return roots(single(n1), null);
            case 'Z': {
                if (n2 == Z_NO_MODULO) {
                    boolean integerRoot = Math.abs(a * n1 + b) < ROOT_EPSILON;
                    SolveResult base = integerRoot ? roots(single(n1), null) : noRoots(null, false);
                    return base.withDivision(n1, n1 == 0 ? -b : null);
                }
                double modulo = n1 == 0 ? -b : n2;
                SolveResult base = modulo == 0 ? roots(single(n1), null) : noRoots(null, false);
                return base.withDivision(n1, modulo);
            }
            case 'N': {
                if ((int) (-b / a) <= 0) {
                    return noRoots(null, false);
                }
                if (Math.abs(a) < 1 && n2 == N_NO_MODULO) {
                    boolean naturalRoot = Math.abs(a * n1 + b) < ROOT_EPSILON;
                    SolveResult base = naturalRoot ? roots(single(n1), null) : noRoots(null, false);
                    return base.withDivision(n1, null);
                }
                SolveResult base = n2 == 0 ? roots(single(n1), null) : noRoots(null, false);
                return base.withDivision(n1, n2 >= 0 ? n2 : null);
            }
            default:
                return undetermined();
        }
    }

    @NonNull
    public static SolveResult decodeQuadratic(double a, double b, double c, char domain,
                                              @NonNull double[] raw) {
        double n1Re = raw[0];
        double n1Im = raw[1];
        double n2Re = raw[2];
        double n2Im = raw[3];
        double n3 = raw[4];

        if (a == 0) {
            return result(SolveResult.Status.NOT_QUADRATIC, new ArrayList<>(), null, false);
        }

        if (domain == 'C') {
            List<SolveResult.Root> roots = new ArrayList<>();
            if (n3 < 0) {
                roots.add(new SolveResult.Root(n1Re, n1Im));
                roots.add(new SolveResult.Root(n2Re, n2Im));
            } else if (n3 == 0) {
                roots.add(new SolveResult.Root(n1Re, 0));
            } else {
                roots.add(new SolveResult.Root(n1Re, 0));
                roots.add(new SolveResult.Root(n2Re, 0));
            }
            return roots(roots, n3);
        }

        if (n3 < 0) {
            return noRoots(null, true);
        }

        // Z and N truncate D to int, so a positive D below 1 also comes back as 0.
        boolean discriminantZero = domain == 'R'
                ? n3 == 0
                : n3 == 0 && !(b * b - 4 * a * c > 0);

        switch (domain) {
            case 'R': {
                List<SolveResult.Root> roots = new ArrayList<>();
                roots.add(new SolveResult.Root(n1Re, 0));
                if (!discriminantZero) {
                    roots.add(new SolveResult.Root(n2Re, 0));
                }
                return roots(roots, n3);
            }
            case 'Z': {
                if (discriminantZero) {
                    return n2Re == Z_NO_INTEGER_ROOT ? noRoots(n3, false) : roots(single(n1Re), n3);
                }
                List<SolveResult.Root> roots = new ArrayList<>();
                if (n1Re != Z_NOT_INTEGER) {
                    roots.add(new SolveResult.Root(n1Re, 0));
                }
                if (n2Re != Z_NOT_INTEGER) {
                    roots.add(new SolveResult.Root(n2Re, 0));
                }
                return roots.isEmpty() ? noRoots(n3, false) : roots(roots, n3);
            }
            case 'N': {
                if (discriminantZero) {
                    return n2Re == N_NO_NATURAL_ROOT ? noRoots(n3, false) : roots(single(n1Re), n3);
                }
                List<SolveResult.Root> roots = new ArrayList<>();
                if (n1Re != N_SMALLER_NOT_NATURAL) {
                    roots.add(new SolveResult.Root(n1Re, 0));
                }
                if (n2Re != N_BIGGER_NOT_NATURAL) {
                    roots.add(new SolveResult.Root(n2Re, 0));
                }
                return roots.isEmpty() ? noRoots(n3, false) : roots(roots, n3);
            }
            default:
                return undetermined();
        }
    }

    @NonNull
    private static List<SolveResult.Root> single(double re) {
        List<SolveResult.Root> roots = new ArrayList<>();
        roots.add(new SolveResult.Root(re, 0));
        return roots;
    }

    @NonNull
    private static SolveResult roots(@NonNull List<SolveResult.Root> roots, Double discriminant) {
        return result(SolveResult.Status.ROOTS, roots, discriminant, false);
    }

    @NonNull
    private static SolveResult noRoots(Double discriminant, boolean discriminantNegative) {
        return result(SolveResult.Status.NO_ROOTS, new ArrayList<>(), discriminant, discriminantNegative);
    }

    @NonNull
    private static SolveResult undetermined() {
        return result(SolveResult.Status.UNDETERMINED, new ArrayList<>(), null, false);
    }

    @NonNull
    private static SolveResult result(@NonNull SolveResult.Status status,
                                      @NonNull List<SolveResult.Root> roots,
                                      Double discriminant, boolean discriminantNegative) {
        return new SolveResult(status, roots, discriminant, discriminantNegative);
    }
}
