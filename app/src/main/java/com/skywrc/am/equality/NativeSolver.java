package com.skywrc.am.equality;

public final class NativeSolver {

    static {
        System.loadLibrary("equality");
    }

    private NativeSolver() {
    }

    /** Returns {n1, n2} exactly as written by linear_solver. */
    static native double[] linear(double a, double b, char domain);

    /** Returns {n1[0], n1[1], n2[0], n2[1], n3} exactly as written by quadratic_solver. */
    static native double[] quadratic(double a, double b, double c, char domain);
}
