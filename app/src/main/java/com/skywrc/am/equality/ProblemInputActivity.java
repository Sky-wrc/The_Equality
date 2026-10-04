package com.skywrc.am.equality;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProblemInputActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_FORMULA = "extra_formula";

    private static final String STATE_CELL_TEXTS = "state_cell_texts";
    private static final String STATE_RESULT_VISIBLE = "state_result_visible";

    private static final String PROBLEM_LINEAR = "linear";
    private static final String PROBLEM_QUADRATIC = "quadratic";
    private static final Set<String> POLYNOMIAL_PROBLEMS =
            new HashSet<>(Arrays.asList(PROBLEM_LINEAR, PROBLEM_QUADRATIC, "cubic"));

    private String problemId;
    private InputPanel inputPanel;
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
        Intent intent = new Intent(context, ProblemInputActivity.class);
        intent.putExtra(EXTRA_ID, problem.getId());
        intent.putExtra(EXTRA_NAME, problem.getName());
        intent.putExtra(EXTRA_FORMULA, problem.getFormula());
        problem.getInputShape().writeTo(intent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left);
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.slide_in_left, R.anim.slide_out_right);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_problem_input);

        preferences = new AppPreferences(this);

        View root = findViewById(R.id.problem_input_root);
        View headerBar = findViewById(R.id.header_bar);
        View calculateButton = findViewById(R.id.btn_calculate);
        scrollView = findViewById(R.id.input_scroll);
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
                    systemBars.top + dpToPx(8),
                    headerBar.getPaddingRight(),
                    headerBar.getPaddingBottom()
            );
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        String id = getIntent().getStringExtra(EXTRA_ID);
        problemId = id != null ? id : "";
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String formula = getIntent().getStringExtra(EXTRA_FORMULA);

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

        inputPanel = InputPanel.create(this, InputShape.readFrom(getIntent()));
        List<String> savedTexts = savedInstanceState != null
                ? savedInstanceState.getStringArrayList(STATE_CELL_TEXTS)
                : InputDraftStore.load(problemId);
        if (savedTexts != null) {
            inputPanel.setCellTexts(savedTexts);
        }
        inputPanel.setOnValuesChangedListener(() -> {
            resultCard.setVisibility(View.GONE);
            updateFormula();
            InputDraftStore.save(problemId, inputPanel.getCellTexts());
        });
        ViewGroup container = findViewById(R.id.input_container);
        container.addView(inputPanel.getView());
        updateFormula();

        findViewById(R.id.btn_reset).setOnClickListener(v -> resetInput());

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESULT_VISIBLE)) {
            calculate();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_CELL_TEXTS, inputPanel.getCellTexts());
        outState.putBoolean(STATE_RESULT_VISIBLE, resultCard.getVisibility() == View.VISIBLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void resetInput() {
        inputPanel.clear();
        InputDraftStore.remove(problemId);
        resultCard.setVisibility(View.GONE);
        updateFormula();
    }

    private void updateDomainButton() {
        domainButton.setText(String.valueOf(preferences.getNumberDomain().getCode()));
    }

    private void updateFormula() {
        InputShape shape = inputPanel.getShape();
        if (!POLYNOMIAL_PROBLEMS.contains(problemId)
                || shape.getKind() != InputShape.Kind.COEFFICIENTS
                || shape.getCellCount() == 0) {
            return;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < shape.getCellCount(); i++) {
            labels.add(shape.getCellLabel(i));
        }
        formulaView.setText(FormulaRenderer.render(labels, inputPanel.peekValues()));
    }

    private void calculate() {
        InputValues values = inputPanel.readValues();
        if (values == null) {
            return;
        }
        hideKeyboard();

        NumberDomain domain = preferences.getNumberDomain();
        resultDomainView.setText(getString(R.string.result_domain, getString(domain.getLabelResId())));

        SolveResult result = solve(values, domain.getCode());
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
    private SolveResult solve(@NonNull InputValues values, char domain) {
        double[] k = values.toArray();
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

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focused != null && imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            focused.clearFocus();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
