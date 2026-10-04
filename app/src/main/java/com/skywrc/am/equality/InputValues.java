package com.skywrc.am.equality;

import androidx.annotation.NonNull;

/** Validated input values stored row by row. */
public final class InputValues {

    private final int rows;
    private final int cols;
    private final double[] values;

    public InputValues(int rows, int cols, @NonNull double[] values) {
        if (values.length != rows * cols) {
            throw new IllegalArgumentException("Expected " + rows * cols + " values, got " + values.length);
        }
        this.rows = rows;
        this.cols = cols;
        this.values = values.clone();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int size() {
        return values.length;
    }

    public double get(int index) {
        return values[index];
    }

    public double get(int row, int col) {
        return values[row * cols + col];
    }

    @NonNull
    public double[] toArray() {
        return values.clone();
    }

    @NonNull
    public double[][] toMatrix() {
        double[][] matrix = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(values, r * cols, matrix[r], 0, cols);
        }
        return matrix;
    }
}
