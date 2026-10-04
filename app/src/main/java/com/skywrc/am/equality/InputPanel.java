package com.skywrc.am.equality;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** A block of input fields for one {@link InputShape}. */
public interface InputPanel {

    @NonNull
    View getView();

    @NonNull
    InputShape getShape();

    /** Called whenever any cell is edited. */
    void setOnValuesChangedListener(@Nullable Runnable listener);

    /** Current cell values row by row; null for empty or invalid cells. Does not show errors. */
    @NonNull
    List<Double> peekValues();

    /** Validates every cell and shows errors; returns null if any cell is empty or invalid. */
    @Nullable
    InputValues readValues();

    /** Raw text of every cell row by row, as typed by the user. */
    @NonNull
    ArrayList<String> getCellTexts();

    /** Restores texts from {@link #getCellTexts()}; ignored if the cell count doesn't match. */
    void setCellTexts(@NonNull List<String> texts);

    /** Empties every cell and clears errors. */
    void clear();

    @NonNull
    static InputPanel create(@NonNull Context context, @NonNull InputShape shape) {
        switch (shape.getKind()) {
            case COEFFICIENTS:
                return new CoefficientInputPanel(context, shape);
            case MATRIX:
            default:
                throw new IllegalArgumentException("No input panel for " + shape.getKind());
        }
    }
}
