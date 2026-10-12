package com.skywrc.am.equality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class FunctionAnalysisTest {

    @Test
    public void quadraticWithTwoRootsOpensUp() {
        FunctionAnalysis analysis = FunctionAnalysis.quadratic(1, -3, 2, Arrays.asList(2.0, 1.0), 1.5);
        assertEquals(FunctionAnalysis.GraphType.PARABOLA, analysis.getGraphType());
        assertEquals(FunctionAnalysis.Branches.UP, analysis.getBranches());
        assertEquals("(−∞, 1) ∪ (2, +∞)", Interval.format(analysis.getPositive()));
        assertEquals("(1, 2)", Interval.format(analysis.getNegative()));
        assertEquals("{1, 2}", Interval.format(analysis.getZero()));
        FunctionAnalysis.Extremum vertex = analysis.getExtrema().get(0);
        assertEquals(FunctionAnalysis.ExtremumType.MINIMUM, vertex.getType());
        assertEquals(1.5, vertex.getX(), 0);
        assertEquals(-0.25, vertex.getY(), 0);
        assertEquals("(−∞, 1.5]", Interval.format(analysis.getDecreasing()));
        assertEquals("[1.5, +∞)", Interval.format(analysis.getIncreasing()));
    }

    @Test
    public void quadraticWithDoubleRootOpensDown() {
        FunctionAnalysis analysis = FunctionAnalysis.quadratic(-1, 2, -1, Collections.singletonList(1.0), 1);
        assertEquals(FunctionAnalysis.Branches.DOWN, analysis.getBranches());
        assertEquals("∅", Interval.format(analysis.getPositive()));
        assertEquals("(−∞, 1) ∪ (1, +∞)", Interval.format(analysis.getNegative()));
        assertEquals("{1}", Interval.format(analysis.getZero()));
        assertEquals(FunctionAnalysis.ExtremumType.MAXIMUM, analysis.getExtrema().get(0).getType());
        assertEquals(0, analysis.getExtrema().get(0).getY(), 0);
        assertEquals("(−∞, 1]", Interval.format(analysis.getIncreasing()));
        assertEquals("[1, +∞)", Interval.format(analysis.getDecreasing()));
    }

    @Test
    public void quadraticWithoutRealRoots() {
        FunctionAnalysis analysis = FunctionAnalysis.quadratic(-2, 0, -1, Collections.emptyList(), 0);
        assertEquals("∅", Interval.format(analysis.getPositive()));
        assertEquals("(−∞, +∞)", Interval.format(analysis.getNegative()));
        assertEquals("∅", Interval.format(analysis.getZero()));
        assertEquals(-1, analysis.getExtrema().get(0).getY(), 0);
    }

    @Test
    public void cubicWithThreeRootsHasMaximumThenMinimum() {
        // (x − 1)(x − 2)(x − 3) = x³ − 6x² + 11x − 6, F'(x) = 3x² − 12x + 11
        double left = 2 - 1 / Math.sqrt(3);
        double right = 2 + 1 / Math.sqrt(3);
        FunctionAnalysis analysis = FunctionAnalysis.cubic(1, -6, 11, -6,
                Arrays.asList(1.0, 2.0, 3.0), Arrays.asList(right, left), 2);
        assertEquals(FunctionAnalysis.GraphType.CUBIC_PARABOLA, analysis.getGraphType());
        assertEquals(FunctionAnalysis.Branches.UP, analysis.getBranches());
        assertEquals("(1, 2) ∪ (3, +∞)", Interval.format(analysis.getPositive()));
        assertEquals("(−∞, 1) ∪ (2, 3)", Interval.format(analysis.getNegative()));
        assertEquals("{1, 2, 3}", Interval.format(analysis.getZero()));
        assertEquals(2, analysis.getExtrema().size());
        assertEquals(FunctionAnalysis.ExtremumType.MAXIMUM, analysis.getExtrema().get(0).getType());
        assertEquals(left, analysis.getExtrema().get(0).getX(), 1e-12);
        assertEquals(FunctionAnalysis.ExtremumType.MINIMUM, analysis.getExtrema().get(1).getType());
        assertEquals(2, analysis.getIncreasing().size());
        assertEquals(1, analysis.getDecreasing().size());
        assertEquals(2, analysis.getInflectionX(), 0);
        assertEquals(0, analysis.getInflectionY(), 0);
    }

    @Test
    public void cubicWithDoubleRootKeepsSignAcrossIt() {
        // (x − 1)²(x + 2) = x³ − 3x + 2, F'(x) = 3x² − 3
        FunctionAnalysis analysis = FunctionAnalysis.cubic(1, 0, -3, 2,
                Arrays.asList(-2.0, 1.0), Arrays.asList(-1.0, 1.0), 0);
        assertEquals("(−2, 1) ∪ (1, +∞)", Interval.format(analysis.getPositive()));
        assertEquals("(−∞, −2)", Interval.format(analysis.getNegative()));
        assertEquals("(−∞, −1] ∪ [1, +∞)", Interval.format(analysis.getIncreasing()));
        assertEquals("[−1, 1]", Interval.format(analysis.getDecreasing()));
        assertEquals(4, analysis.getExtrema().get(0).getY(), 0);
        assertEquals(0, analysis.getExtrema().get(1).getY(), 0);
    }

    @Test
    public void monotonicCubicHasNoExtrema() {
        // −x³ − x, F'(x) = −3x² − 1 has no real roots
        FunctionAnalysis analysis = FunctionAnalysis.cubic(-1, 0, -1, 0,
                Collections.singletonList(0.0), Collections.emptyList(), 0);
        assertEquals(FunctionAnalysis.Branches.DOWN, analysis.getBranches());
        assertEquals("(−∞, 0)", Interval.format(analysis.getPositive()));
        assertEquals("∅", Interval.format(analysis.getIncreasing()));
        assertEquals("(−∞, +∞)", Interval.format(analysis.getDecreasing()));
        assertTrue(analysis.getExtrema().isEmpty());

        // x³ has a stationary point at 0 but keeps increasing
        FunctionAnalysis cube = FunctionAnalysis.cubic(1, 0, 0, 0,
                Collections.singletonList(0.0), Collections.singletonList(0.0), 0);
        assertEquals("(−∞, +∞)", Interval.format(cube.getIncreasing()));
        assertTrue(cube.getExtrema().isEmpty());
    }

    @Test
    public void linearSignDependsOnA() {
        FunctionAnalysis rising = FunctionAnalysis.linear(2, -4, 2);
        assertEquals(FunctionAnalysis.GraphType.STRAIGHT_LINE, rising.getGraphType());
        assertNull(rising.getBranches());
        assertEquals("(2, +∞)", Interval.format(rising.getPositive()));
        assertEquals("(−∞, 2)", Interval.format(rising.getNegative()));
        assertEquals("{2}", Interval.format(rising.getZero()));
        assertTrue(rising.getExtrema().isEmpty());

        FunctionAnalysis falling = FunctionAnalysis.linear(-1, -3, -3);
        assertEquals("(−∞, −3)", Interval.format(falling.getPositive()));
        assertEquals("(−3, +∞)", Interval.format(falling.getNegative()));
    }

    @Test
    public void linearConstant() {
        FunctionAnalysis positive = FunctionAnalysis.linear(0, 3, 0);
        assertEquals("(−∞, +∞)", Interval.format(positive.getPositive()));
        assertEquals("∅", Interval.format(positive.getZero()));

        FunctionAnalysis zero = FunctionAnalysis.linear(0, 0, 0);
        assertEquals("(−∞, +∞)", Interval.format(zero.getZero()));
        assertEquals("∅", Interval.format(zero.getPositive()));
    }
}
