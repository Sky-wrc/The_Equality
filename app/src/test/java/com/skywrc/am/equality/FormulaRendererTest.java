package com.skywrc.am.equality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FormulaRendererTest {

    private static final List<String> LINEAR = Arrays.asList("a", "b");
    private static final List<String> QUADRATIC = Arrays.asList("a", "b", "c");
    private static final List<String> CUBIC = Arrays.asList("a", "b", "c", "d");

    @Test
    public void emptyFieldsKeepLetters() {
        assertEquals("ax+b=0", FormulaRenderer.render(LINEAR, Arrays.asList(null, null)));
        assertEquals("ax²+bx+c=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(null, null, null)));
        assertEquals("ax³+bx²+cx+d=0",
                FormulaRenderer.render(CUBIC, Arrays.asList(null, null, null, null)));
    }

    @Test
    public void numbersReplaceLetters() {
        assertEquals("x²−3x+2=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(1.0, -3.0, 2.0)));
        assertEquals("2x²+bx+c=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(2.0, null, null)));
        assertEquals("−x²+5=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(-1.0, 0.0, 5.0)));
        assertEquals("2.5x²−x=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(2.5, -1.0, 0.0)));
        assertEquals("0=0", FormulaRenderer.render(QUADRATIC, Arrays.asList(0.0, 0.0, 0.0)));
    }

    @Test
    public void parseAcceptsCommaAndRejectsPartialInput() {
        assertEquals(2.5, FormulaRenderer.parse("2,5"), 0);
        assertNull(FormulaRenderer.parse("-"));
        assertNull(FormulaRenderer.parse(""));
    }

    @Test
    public void linearIntegersShowQuotientAndModulo() {
        SolveResult result = SolveResultDecoder.decodeLinear(6, 17, 'Z', new double[]{-2, -5});
        assertEquals(SolveResult.Status.NO_ROOTS, result.getStatus());
        assertEquals(-2, result.getQuotient(), 0);
        assertEquals(-5, result.getModulo(), 0);

        SolveResult zeroQuotient = SolveResultDecoder.decodeLinear(3, -1.5, 'Z', new double[]{0, 1});
        assertEquals(SolveResult.Status.NO_ROOTS, zeroQuotient.getStatus());
        assertEquals(0, zeroQuotient.getQuotient(), 0);
        assertEquals(1.5, zeroQuotient.getModulo(), 0);

        SolveResult rootZero = SolveResultDecoder.decodeLinear(4, 0, 'Z', new double[]{0, 0});
        assertEquals(SolveResult.Status.ROOTS, rootZero.getStatus());
        assertEquals(0, rootZero.getModulo(), 0);
    }

    @Test
    public void linearNaturalsHideZeroQuotientAndNegativeModulo() {
        SolveResult natural = SolveResultDecoder.decodeLinear(2, -4, 'N', new double[]{2, 0});
        assertEquals(SolveResult.Status.ROOTS, natural.getStatus());
        assertEquals(2, natural.getQuotient(), 0);
        assertEquals(0, natural.getModulo(), 0);

        SolveResult negativeModulo = SolveResultDecoder.decodeLinear(-6, 17, 'N', new double[]{2, -5});
        assertEquals(2, negativeModulo.getQuotient(), 0);
        assertNull(negativeModulo.getModulo());

        SolveResult zeroQuotient = SolveResultDecoder.decodeLinear(5, -2, 'N', new double[]{-1, -2});
        assertEquals(SolveResult.Status.NO_ROOTS, zeroQuotient.getStatus());
        assertNull(zeroQuotient.getQuotient());
        assertNull(zeroQuotient.getModulo());
    }

    @Test
    public void linearFractionalDivisorUsesNoModuloCodes() {
        SolveResult integerRoot = SolveResultDecoder.decodeLinear(0.5, -3, 'Z', new double[]{6, -4.1});
        assertEquals(SolveResult.Status.ROOTS, integerRoot.getStatus());
        assertEquals(6, integerRoot.getRoots().get(0).getRe(), 0);
        assertEquals(6, integerRoot.getQuotient(), 0);
        assertNull(integerRoot.getModulo());

        SolveResult noIntegerRoot = SolveResultDecoder.decodeLinear(0.3, -1, 'Z', new double[]{3, -4.1});
        assertEquals(SolveResult.Status.NO_ROOTS, noIntegerRoot.getStatus());
        assertEquals(3, noIntegerRoot.getQuotient(), 0);
        assertNull(noIntegerRoot.getModulo());

        SolveResult zeroQuotient = SolveResultDecoder.decodeLinear(0.5, -0.2, 'Z', new double[]{0, -4.1});
        assertEquals(SolveResult.Status.NO_ROOTS, zeroQuotient.getStatus());
        assertEquals(0, zeroQuotient.getQuotient(), 0);
        assertEquals(0.2, zeroQuotient.getModulo(), 0);

        SolveResult natural = SolveResultDecoder.decodeLinear(0.5, -3, 'N', new double[]{6, -4});
        assertEquals(SolveResult.Status.ROOTS, natural.getStatus());
        assertEquals(6, natural.getQuotient(), 0);
        assertNull(natural.getModulo());

        SolveResult noNaturalRoot = SolveResultDecoder.decodeLinear(0.3, -1, 'N', new double[]{3, -4});
        assertEquals(SolveResult.Status.NO_ROOTS, noNaturalRoot.getStatus());
        assertEquals(3, noNaturalRoot.getQuotient(), 0);
        assertNull(noNaturalRoot.getModulo());

        SolveResult realModuloMinusFour = SolveResultDecoder.decodeLinear(-6, 10, 'N', new double[]{1, -4});
        assertEquals(SolveResult.Status.NO_ROOTS, realModuloMinusFour.getStatus());
        assertEquals(1, realModuloMinusFour.getQuotient(), 0);
        assertNull(realModuloMinusFour.getModulo());
    }

    @Test
    public void linearRealsHaveNoDivision() {
        SolveResult real = SolveResultDecoder.decodeLinear(2, -4, 'R', new double[]{2, 0});
        assertNull(real.getQuotient());
        assertNull(real.getModulo());
    }
}
