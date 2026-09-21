package com.skywrc.am.equality;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CoefficientInputActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_FORMULA = "extra_formula";
    public static final String EXTRA_PARAM_COUNT = "extra_param_count";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull Problem problem) {
        Intent intent = new Intent(context, CoefficientInputActivity.class);
        intent.putExtra(EXTRA_ID, problem.getId());
        intent.putExtra(EXTRA_NAME, problem.getName());
        intent.putExtra(EXTRA_FORMULA, problem.getFormula());
        intent.putExtra(EXTRA_PARAM_COUNT, problem.getParameterCount());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coefficient_input);

        View root = findViewById(R.id.coefficient_root);
        View headerBar = findViewById(R.id.header_bar);
        View calculateButton = findViewById(R.id.btn_calculate);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            headerBar.setPadding(
                    headerBar.getPaddingLeft(),
                    systemBars.top + dpToPx(4),
                    headerBar.getPaddingRight(),
                    headerBar.getPaddingBottom()
            );
            calculateButton.setPadding(
                    calculateButton.getPaddingLeft(),
                    calculateButton.getPaddingTop(),
                    calculateButton.getPaddingRight(),
                    calculateButton.getPaddingBottom()
            );
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String formula = getIntent().getStringExtra(EXTRA_FORMULA);
        int paramCount = getIntent().getIntExtra(EXTRA_PARAM_COUNT, 0);

        TextView nameView = findViewById(R.id.problem_name);
        TextView formulaView = findViewById(R.id.problem_formula);
        nameView.setText(name != null ? name : "");
        formulaView.setText(formula != null ? formula : "");

        findViewById(R.id.btn_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        calculateButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        );

        LinearLayout container = findViewById(R.id.coefficients_container);
        for (int i = 0; i < paramCount; i++) {
            container.addView(createCoefficientField(coefficientLabel(i)));
        }
    }

    @NonNull
    private View createCoefficientField(@NonNull String label) {
        TextInputLayout inputLayout = new TextInputLayout(
                this,
                null,
                com.google.android.material.R.attr.textInputOutlinedStyle
        );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = dpToPx(12);
        inputLayout.setLayoutParams(layoutParams);
        inputLayout.setHint(getString(R.string.coefficient_hint, label));
        inputLayout.setBoxBackgroundColor(getColor(R.color.surface_card));
        inputLayout.setDefaultHintTextColor(
                getColorStateList(R.color.text_secondary)
        );

        TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setTextColor(getColor(R.color.text_primary));
        inputLayout.addView(editText);
        return inputLayout;
    }

    @NonNull
    private static String coefficientLabel(int index) {
        if (index < 26) {
            return String.valueOf((char) ('a' + index));
        }
        return "p" + (index + 1);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
