package com.skywrc.am.equality;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoefficientInputActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_FORMULA = "extra_formula";
    public static final String EXTRA_PARAM_COUNT = "extra_param_count";

    private static final String PROBLEM_LINEAR = "linear";
    private static final String PROBLEM_QUADRATIC = "quadratic";
    private static final Set<String> POLYNOMIAL_PROBLEMS =
            new HashSet<>(Arrays.asList(PROBLEM_LINEAR, PROBLEM_QUADRATIC, "cubic"));

    private final List<TextInputLayout> inputLayouts = new ArrayList<>();
    private final List<TextInputEditText> inputs = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();

    private String problemId;
    private AppPreferences preferences;
    private TextView formulaView;
    private MaterialButton domainButton;
    private ScrollView scrollView;
    private View resultCard;
    private TextView resultDomainView;
    private TextView resultDiscriminantView;
    private TextView resultSqrtDiscriminantView;
    private TextView resultBodyView;
    private TextView resultDivisionView;

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

        preferences = new AppPreferences(this);

        View root = findViewById(R.id.coefficient_root);
        View headerBar = findViewById(R.id.header_bar);
        View calculateButton = findViewById(R.id.btn_calculate);
        scrollView = findViewById(R.id.coefficients_scroll);
        resultCard = findViewById(R.id.result_card);
        resultDomainView = findViewById(R.id.result_domain);
        resultDiscriminantView = findViewById(R.id.result_discriminant);
        resultSqrtDiscriminantView = findViewById(R.id.result_sqrt_discriminant);
        resultBodyView = findViewById(R.id.result_body);
        resultDivisionView = findViewById(R.id.result_division);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            headerBar.setPadding(
                    headerBar.getPaddingLeft(),
                    systemBars.top + dpToPx(4),
                    headerBar.getPaddingRight(),
                    headerBar.getPaddingBottom()
            );
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        problemId = getIntent().getStringExtra(EXTRA_ID);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String formula = getIntent().getStringExtra(EXTRA_FORMULA);
        int paramCount = getIntent().getIntExtra(EXTRA_PARAM_COUNT, 0);

        TextView nameView = findViewById(R.id.problem_name);
        formulaView = findViewById(R.id.problem_formula);
        nameView.setText(name != null ? name : "");
        formulaView.setText(formula != null ? formula : "");

        findViewById(R.id.btn_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        calculateButton.setOnClickListener(v -> calculate());

        domainButton = findViewById(R.id.btn_domain);
        updateDomainButton();
        domainButton.setOnClickListener(v -> NumberDomainDialog.show(this, preferences, selected -> {
            updateDomainButton();
            if (resultCard.getVisibility() == View.VISIBLE) {
                calculate();
            }
        }));

        LinearLayout container = findViewById(R.id.coefficients_container);
        for (int i = 0; i < paramCount; i++) {
            String label = coefficientLabel(i);
            labels.add(label);
            container.addView(createCoefficientField(label));
        }
        updateFormula();
    }

    private void updateDomainButton() {
        domainButton.setText(String.valueOf(preferences.getNumberDomain().getCode()));
    }

    private void updateFormula() {
        if (!POLYNOMIAL_PROBLEMS.contains(problemId) || inputs.isEmpty()) {
            return;
        }
        List<Double> values = new ArrayList<>();
        for (TextInputEditText input : inputs) {
            values.add(FormulaRenderer.parse(input.getText()));
        }
        formulaView.setText(FormulaRenderer.render(labels, values));
    }

    private void calculate() {
        double[] coefficients = readCoefficients();
        if (coefficients == null) {
            return;
        }
        hideKeyboard();

        NumberDomain domain = preferences.getNumberDomain();
        resultDomainView.setText(getString(R.string.result_domain, getString(domain.getLabelResId())));

        SolveResult result = solve(coefficients, domain.getCode());
        if (result == null) {
            resultDiscriminantView.setVisibility(View.GONE);
            resultSqrtDiscriminantView.setVisibility(View.GONE);
            resultDivisionView.setVisibility(View.GONE);
            resultBodyView.setText(R.string.result_solver_unavailable);
        } else {
            showResult(result);
        }

        resultCard.setVisibility(View.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, resultCard.getBottom()));
    }

    @Nullable
    private SolveResult solve(@NonNull double[] k, char domain) {
        if (PROBLEM_LINEAR.equals(problemId) && k.length == 2) {
            double[] raw = NativeSolver.linear(k[0], k[1], domain);
            return SolveResultDecoder.decodeLinear(k[0], k[1], domain, raw);
        }
        if (PROBLEM_QUADRATIC.equals(problemId) && k.length == 3) {
            double[] raw = NativeSolver.quadratic(k[0], k[1], k[2], domain);
            return SolveResultDecoder.decodeQuadratic(k[0], k[1], k[2], domain, raw);
        }
        return null;
    }

    private void showResult(@NonNull SolveResult result) {
        Double discriminant = result.getDiscriminant();
        if (result.isDiscriminantNegative()) {
            resultDiscriminantView.setText(R.string.result_discriminant_negative);
            resultDiscriminantView.setVisibility(View.VISIBLE);
            resultSqrtDiscriminantView.setVisibility(View.GONE);
        } else if (discriminant != null) {
            resultDiscriminantView.setText(
                    getString(R.string.result_discriminant, NumberFormatter.format(discriminant))
            );
            resultDiscriminantView.setVisibility(View.VISIBLE);
            resultSqrtDiscriminantView.setText(
                    getString(R.string.result_sqrt_discriminant, formatSqrt(discriminant))
            );
            resultSqrtDiscriminantView.setVisibility(View.VISIBLE);
        } else {
            resultDiscriminantView.setVisibility(View.GONE);
            resultSqrtDiscriminantView.setVisibility(View.GONE);
        }

        List<String> division = new ArrayList<>();
        if (result.getQuotient() != null) {
            division.add(getString(R.string.result_quotient, NumberFormatter.format(result.getQuotient())));
        }
        if (result.getModulo() != null) {
            division.add(getString(R.string.result_modulo, NumberFormatter.format(result.getModulo())));
        }
        if (division.isEmpty()) {
            resultDivisionView.setVisibility(View.GONE);
        } else {
            resultDivisionView.setText(TextUtils.join("   ", division));
            resultDivisionView.setVisibility(View.VISIBLE);
        }

        switch (result.getStatus()) {
            case ROOTS:
                resultBodyView.setText(formatRoots(result.getRoots()));
                break;
            case NO_ROOTS:
                resultBodyView.setText(R.string.result_no_roots);
                break;
            case INFINITE_ROOTS:
                resultBodyView.setText(R.string.result_infinite_roots);
                break;
            case NOT_QUADRATIC:
                resultBodyView.setText(R.string.result_not_quadratic);
                break;
            case UNDETERMINED:
            default:
                resultBodyView.setText(R.string.result_undetermined);
                break;
        }
    }

    @NonNull
    private String formatRoots(@NonNull List<SolveResult.Root> roots) {
        if (roots.size() == 1) {
            return getString(R.string.result_root_single, formatRoot(roots.get(0)));
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            lines.add(getString(R.string.result_root_indexed, i + 1, formatRoot(roots.get(i))));
        }
        return TextUtils.join("\n", lines);
    }

    @NonNull
    private static String formatRoot(@NonNull SolveResult.Root root) {
        double im = root.getIm();
        if (im == 0) {
            return NumberFormatter.format(root.getRe());
        }
        String imPart = formatImaginary(Math.abs(im));
        if (root.getRe() == 0) {
            return im < 0 ? "−" + imPart : imPart;
        }
        return NumberFormatter.format(root.getRe()) + (im < 0 ? " − " : " + ") + imPart;
    }

    @NonNull
    private static String formatSqrt(double discriminant) {
        if (discriminant < 0) {
            return formatImaginary(Math.sqrt(-discriminant));
        }
        return NumberFormatter.format(Math.sqrt(discriminant));
    }

    @NonNull
    private static String formatImaginary(double magnitude) {
        return magnitude == 1 ? "i" : NumberFormatter.format(magnitude) + "i";
    }

    @Nullable
    private double[] readCoefficients() {
        double[] values = new double[inputs.size()];
        boolean valid = true;
        for (int i = 0; i < inputs.size(); i++) {
            TextInputLayout layout = inputLayouts.get(i);
            CharSequence text = inputs.get(i).getText();
            if (text == null || text.toString().trim().isEmpty()) {
                layout.setError(getString(R.string.error_enter_coefficient, labels.get(i)));
                valid = false;
                continue;
            }
            Double value = FormulaRenderer.parse(text);
            if (value == null) {
                layout.setError(getString(R.string.error_invalid_number));
                valid = false;
                continue;
            }
            values[i] = value;
            layout.setError(null);
        }
        return valid ? values : null;
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focused != null && imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            focused.clearFocus();
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
                resultCard.setVisibility(View.GONE);
                updateFormula();
            }
        });
        inputLayout.addView(editText);

        inputLayouts.add(inputLayout);
        inputs.add(editText);
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
