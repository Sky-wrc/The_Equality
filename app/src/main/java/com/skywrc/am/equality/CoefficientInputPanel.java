package com.skywrc.am.equality;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/** One full-width text field per coefficient, stacked vertically. */
final class CoefficientInputPanel implements InputPanel {

    private final Context context;
    private final InputShape shape;
    private final LinearLayout root;
    private final List<TextInputLayout> inputLayouts = new ArrayList<>();
    private final List<TextInputEditText> inputs = new ArrayList<>();
    @Nullable
    private Runnable onValuesChanged;

    CoefficientInputPanel(@NonNull Context context, @NonNull InputShape shape) {
        this.context = context;
        this.shape = shape;
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        for (int i = 0; i < shape.getCellCount(); i++) {
            root.addView(createField(shape.getCellLabel(i)));
        }
    }

    @NonNull
    @Override
    public View getView() {
        return root;
    }

    @NonNull
    @Override
    public InputShape getShape() {
        return shape;
    }

    @Override
    public void setOnValuesChangedListener(@Nullable Runnable listener) {
        onValuesChanged = listener;
    }

    @NonNull
    @Override
    public List<Double> peekValues() {
        List<Double> values = new ArrayList<>();
        for (TextInputEditText input : inputs) {
            values.add(FormulaRenderer.parse(input.getText()));
        }
        return values;
    }

    @Nullable
    @Override
    public InputValues readValues() {
        double[] values = new double[inputs.size()];
        boolean valid = true;
        for (int i = 0; i < inputs.size(); i++) {
            TextInputLayout layout = inputLayouts.get(i);
            CharSequence text = inputs.get(i).getText();
            if (text == null || text.toString().trim().isEmpty()) {
                layout.setError(context.getString(R.string.error_enter_coefficient, shape.getCellLabel(i)));
                valid = false;
                continue;
            }
            Double value = FormulaRenderer.parse(text);
            if (value == null) {
                layout.setError(context.getString(R.string.error_invalid_number));
                valid = false;
                continue;
            }
            values[i] = value;
            layout.setError(null);
        }
        return valid ? new InputValues(shape.getRows(), shape.getCols(), values) : null;
    }

    @NonNull
    @Override
    public ArrayList<String> getCellTexts() {
        ArrayList<String> texts = new ArrayList<>();
        for (TextInputEditText input : inputs) {
            CharSequence text = input.getText();
            texts.add(text != null ? text.toString() : "");
        }
        return texts;
    }

    @Override
    public void setCellTexts(@NonNull List<String> texts) {
        if (texts.size() != inputs.size()) {
            return;
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setText(texts.get(i));
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setText(null);
            inputLayouts.get(i).setError(null);
        }
    }

    @NonNull
    private View createField(@NonNull String label) {
        TextInputLayout inputLayout = new TextInputLayout(
                context,
                null,
                com.google.android.material.R.attr.textInputOutlinedStyle
        );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = dpToPx(12);
        inputLayout.setLayoutParams(layoutParams);
        inputLayout.setHint(context.getString(R.string.coefficient_hint, label));
        inputLayout.setBoxBackgroundColor(context.getColor(R.color.surface_card));
        inputLayout.setDefaultHintTextColor(context.getColorStateList(R.color.text_secondary));

        TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setTextColor(context.getColor(R.color.text_primary));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputLayout.setError(null);
                if (onValuesChanged != null) {
                    onValuesChanged.run();
                }
            }
        });
        inputLayout.addView(editText);

        inputLayouts.add(inputLayout);
        inputs.add(editText);
        return inputLayout;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
