package com.skywrc.am.equality;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InputShapeTest {

    @Test
    public void coefficientsAreOneRowLabelledByLetters() {
        InputShape shape = InputShape.coefficients(3);
        assertEquals(1, shape.getRows());
        assertEquals(3, shape.getCellCount());
        assertEquals("a", shape.getCellLabel(0));
        assertEquals("c", shape.getCellLabel(2));
    }

    @Test
    public void matrixCellsAreLabelledByRowAndColumn() {
        InputShape shape = InputShape.matrix(2, 3);
        assertEquals(6, shape.getCellCount());
        assertEquals("a11", shape.getCellLabel(0));
        assertEquals("a13", shape.getCellLabel(2));
        assertEquals("a21", shape.getCellLabel(3));
    }

    @Test
    public void valuesAreStoredRowByRow() {
        InputValues values = new InputValues(2, 2, new double[]{1, 2, 3, 4});
        assertEquals(2, values.get(0, 1), 0);
        assertEquals(3, values.get(1, 0), 0);
        assertArrayEquals(new double[]{3, 4}, values.toMatrix()[1], 0);
    }
}
