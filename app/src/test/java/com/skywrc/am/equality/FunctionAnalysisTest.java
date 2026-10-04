package com.skywrc.am.equality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals(FunctionAnalysis.ExtremumType.MINIMUM, analysis.getExtremumType());
        assertEquals(1.5, analysis.getExtremumX(), 0);
        assertEquals(-0.25, analysis.getExtremumY(), 0);
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
        assertEquals(FunctionAnalysis.ExtremumType.MAXIMUM, analysis.getExtremumType());
        assertEquals(0, analysis.getExtremumY(), 0);
        assertEquals("(−∞, 1]", Interval.format(analysis.getIncreasing()));
        assertEquals("[1, +∞)", Interval.format(analysis.getDecreasing()));
    }

    @Test
    public void quadraticWithoutRealRoots() {
        FunctionAnalysis analysis = FunctionAnalysis.quadratic(-2, 0, -1, Collections.emptyList(), 0);
        assertEquals("∅", Interval.format(analysis.getPositive()));
        assertEquals("(−∞, +∞)", Interval.format(analysis.getNegative()));
        assertEquals("∅", Interval.format(analysis.getZero()));
        assertEquals(-1, analysis.getExtremumY(), 0);
    }

    @Test
    public void linearSignDependsOnA() {
        FunctionAnalysis rising = FunctionAnalysis.linear(2, -4, 2);
        assertEquals(FunctionAnalysis.GraphType.STRAIGHT_LINE, rising.getGraphType());
        assertNull(rising.getBranches());
        assertEquals("(2, +∞)", Interval.format(rising.getPositive()));
        assertEquals("(−∞, 2)", Interval.format(rising.getNegative()));
        assertEquals("{2}", Interval.format(rising.getZero()));
        assertNull(rising.getExtremumType());

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
