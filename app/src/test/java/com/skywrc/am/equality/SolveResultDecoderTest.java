package com.skywrc.am.equality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Raw arrays mirror what quadratic_solver writes with Rt = 1: {n1[0], n1[1], n2[0], n2[1], n3}. */
public class SolveResultDecoderTest {

    @Test
    public void realRootFormKeepsCenterAndDiscriminant() {
        SolveResult result = SolveResultDecoder.decodeQuadratic(1, -2, -1, 'R', true,
                new double[]{1, 0, -10, 0, 8});
        assertEquals(SolveResult.Status.ROOT_FORM, result.getStatus());
        assertEquals(1, result.getCenter(), 0);
        assertEquals(8, result.getDiscriminant(), 0);
        assertEquals("1 − √8/2", NumberFormatter.formatRootForm(result.getCenter(), 8, 1, false));
        assertEquals("1 + √8/2", NumberFormatter.formatRootForm(result.getCenter(), 8, 1, true));
    }

    @Test
    public void rootFormWithZeroDiscriminantIsSingleRoot() {
        SolveResult result = SolveResultDecoder.decodeQuadratic(1, -2, 1, 'R', true,
                new double[]{1, 0, -10, 0, 0});
        assertEquals(SolveResult.Status.ROOTS, result.getStatus());
        assertEquals(1, result.getRoots().size());
        assertEquals(1, result.getRoots().get(0).getRe(), 0);
    }

    @Test
    public void negativeDiscriminantOutsideComplexHasNoRoots() {
        SolveResult result = SolveResultDecoder.decodeQuadratic(1, 0, 1, 'R', true,
                new double[]{-1, 0, -1, 0, -1});
        assertEquals(SolveResult.Status.NO_ROOTS, result.getStatus());
        assertTrue(result.isDiscriminantNegative());
    }

    @Test
    public void integerAndNaturalMarkers() {
        assertEquals(SolveResult.Status.NO_ROOTS, SolveResultDecoder.decodeQuadratic(1, -3, 1, 'Z', true,
                new double[]{-0.000001, 0, -11, 0, 5}).getStatus());
        assertEquals(SolveResult.Status.ROOT_FORM, SolveResultDecoder.decodeQuadratic(1, -4, 1, 'Z', true,
                new double[]{2, 0, -10, 0, 12}).getStatus());
        assertEquals(SolveResult.Status.ROOT_FORM, SolveResultDecoder.decodeQuadratic(1, -4, 1, 'N', true,
                new double[]{2, 0, -12, 0, 12}).getStatus());
        assertEquals(SolveResult.Status.NO_ROOTS, SolveResultDecoder.decodeQuadratic(1, 4, 1, 'N', true,
                new double[]{0, 0, -13, 0, 12}).getStatus());
    }

    @Test
    public void complexRootFormUsesImaginaryRadical() {
        SolveResult result = SolveResultDecoder.decodeQuadratic(1, 2, 5, 'C', true,
                new double[]{-1, 0, -14, 0, -16});
        assertEquals(SolveResult.Status.ROOT_FORM, result.getStatus());
        assertEquals("-1 + i√16/2", NumberFormatter.formatRootForm(result.getCenter(), -16, 1, true));
    }

    @Test
    public void rootFormOmitsZeroCenterAndUnitDenominator() {
        assertEquals("−√5", NumberFormatter.formatRootForm(0, 5, 0.5, false));
        assertEquals("√5", NumberFormatter.formatRootForm(0, 5, 0.5, true));
        assertEquals("1.5 − √3/4", NumberFormatter.formatRootForm(1.5, 3, -2, false));
    }

    @Test
    public void cubicCountSelectsRoots() {
        SolveResult three = SolveResultDecoder.decodeCubic(new double[]{1, 0, 2, 0, 3, 0, 4, 3});
        assertEquals(SolveResult.Status.ROOTS, three.getStatus());
        assertEquals(3, three.getRoots().size());
        assertEquals(3, three.getRoots().get(2).getRe(), 0);
        assertEquals(4, three.getDiscriminant(), 0);

        SolveResult complex = SolveResultDecoder.decodeCubic(new double[]{-2, 0, 1, 2, 1, -2, -2704, 3});
        assertEquals(2, complex.getRoots().get(1).getIm(), 0);
        assertEquals(-2, complex.getRoots().get(2).getIm(), 0);

        assertEquals(SolveResult.Status.NO_ROOTS,
                SolveResultDecoder.decodeCubic(new double[]{0, 0, 0, 0, 0, 0, -108, 0}).getStatus());
        assertEquals(SolveResult.Status.NOT_CUBIC,
                SolveResultDecoder.decodeCubic(new double[]{0, 0, 0, 0, 0, 0, 0, -1}).getStatus());
    }

    @Test
    public void subscriptUsesUnicodeDigits() {
        assertEquals("₁", NumberFormatter.subscript(1));
        assertEquals("₂", NumberFormatter.subscript(2));
        assertEquals("₁₀", NumberFormatter.subscript(10));
    }

    @Test
    public void explicitRootsAreUnchanged() {
        SolveResult result = SolveResultDecoder.decodeQuadratic(1, -3, 2, 'R', false,
                new double[]{1, 0, 2, 0, 1});
        assertEquals(SolveResult.Status.ROOTS, result.getStatus());
        assertEquals(2, result.getRoots().size());
    }
}
