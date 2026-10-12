package com.skywrc.am.equality;

public final class NativeSolver {

    static {
        System.loadLibrary("equality");
    }

    private NativeSolver() {
    }

    /** Returns {n1, n2} exactly as written by linear_solver. */
    static native double[] linear(double a, double b, char domain);

    /**
     * Returns {n1[0], n1[1], n2[0], n2[1], n3} exactly as written by quadratic_solver.
     * With rootForm set the solver returns -b/2a instead of the explicit roots (Rt = 1).
     */
    static native double[] quadratic(double a, double b, double c, char domain, boolean rootForm);

    /**
     * Returns {x1re, x1im, x2re, x2im, x3re, x3im, D, count} as written by cubic_solver;
     * count is the number of filled roots, -1 when a = 0 and -2 for an unknown domain.
     */
    static native double[] cubic(double a, double b, double c, double d, char domain);
}
