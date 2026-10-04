package com.skywrc.am.equality;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * Describes what a problem expects as input: a list of coefficients or a rows x cols matrix.
 * Values are always addressed row by row, so a coefficient list is a single row.
 */
public final class InputShape {

    public enum Kind {
        COEFFICIENTS,
        MATRIX
    }

    private static final String EXTRA_KIND = "extra_input_kind";
    private static final String EXTRA_ROWS = "extra_input_rows";
    private static final String EXTRA_COLS = "extra_input_cols";

    private final Kind kind;
    private final int rows;
    private final int cols;

    private InputShape(@NonNull Kind kind, int rows, int cols) {
        this.kind = kind;
        this.rows = rows;
        this.cols = cols;
    }

    @NonNull
    public static InputShape coefficients(int count) {
        return new InputShape(Kind.COEFFICIENTS, 1, count);
    }

    @NonNull
    public static InputShape matrix(int rows, int cols) {
        return new InputShape(Kind.MATRIX, rows, cols);
    }

    @NonNull
    public Kind getKind() {
        return kind;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getCellCount() {
        return rows * cols;
    }

    /** a, b, c… for coefficients; a11, a12… for a matrix. */
    @NonNull
    public String getCellLabel(int index) {
        if (kind == Kind.MATRIX) {
            return "a" + (index / cols + 1) + (index % cols + 1);
        }
        if (index < 26) {
            return String.valueOf((char) ('a' + index));
        }
        return "p" + (index + 1);
    }

    public void writeTo(@NonNull Intent intent) {
        intent.putExtra(EXTRA_KIND, kind.name());
        intent.putExtra(EXTRA_ROWS, rows);
        intent.putExtra(EXTRA_COLS, cols);
    }

    @NonNull
    public static InputShape readFrom(@NonNull Intent intent) {
        Kind kind;
        try {
            kind = Kind.valueOf(intent.getStringExtra(EXTRA_KIND));
        } catch (IllegalArgumentException | NullPointerException e) {
            kind = Kind.COEFFICIENTS;
        }
        return new InputShape(kind, intent.getIntExtra(EXTRA_ROWS, 1), intent.getIntExtra(EXTRA_COLS, 0));
    }
}
