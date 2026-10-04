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
    private static final String STATE_ANALYSIS_VISIBLE = "state_analysis_visible";

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
    private View functionButton;
    private View analysisCard;
    private TextView analysisFunctionView;
    private TextView analysisGraphView;
    private TextView analysisSignsView;
    private TextView analysisBehaviorView;

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
        functionButton = findViewById(R.id.btn_function);
        analysisCard = findViewById(R.id.analysis_card);
        analysisFunctionView = findViewById(R.id.analysis_function);
        analysisGraphView = findViewById(R.id.analysis_graph);
        analysisSignsView = findViewById(R.id.analysis_signs);
        analysisBehaviorView = findViewById(R.id.analysis_behavior);
        functionButton.setOnClickListener(v -> toggleAnalysis());

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
            hideResult();
            updateFormula();
            InputDraftStore.save(problemId, inputPanel.getCellTexts());
        });
        ViewGroup container = findViewById(R.id.input_container);
        container.addView(inputPanel.getView());
        updateFormula();

        findViewById(R.id.btn_reset).setOnClickListener(v -> resetInput());

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESULT_VISIBLE)) {
            calculate();
            if (savedInstanceState.getBoolean(STATE_ANALYSIS_VISIBLE)
                    && functionButton.getVisibility() == View.VISIBLE) {
                analysisCard.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_CELL_TEXTS, inputPanel.getCellTexts());
        outState.putBoolean(STATE_RESULT_VISIBLE, resultCard.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_ANALYSIS_VISIBLE, analysisCard.getVisibility() == View.VISIBLE);
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
        hideResult();
        updateFormula();
    }

    private void hideResult() {
        resultCard.setVisibility(View.GONE);
        functionButton.setVisibility(View.GONE);
        analysisCard.setVisibility(View.GONE);
    }

    private void toggleAnalysis() {
        if (analysisCard.getVisibility() == View.VISIBLE) {
            analysisCard.setVisibility(View.GONE);
            return;
        }
        analysisCard.setVisibility(View.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, analysisCard.getBottom()));
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
        formulaView.setText(FormulaRenderer.render(cellLabels(), inputPanel.peekValues()));
    }

    @NonNull
    private List<String> cellLabels() {
        InputShape shape = inputPanel.getShape();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < shape.getCellCount(); i++) {
            labels.add(shape.getCellLabel(i));
        }
        return labels;
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

        FunctionAnalysis analysis = domain == NumberDomain.REALS && result != null
                ? analyze(values, result)
                : null;
        if (analysis == null) {
            functionButton.setVisibility(View.GONE);
            analysisCard.setVisibility(View.GONE);
        } else {
            bindAnalysis(values, analysis);
            functionButton.setVisibility(View.VISIBLE);
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

    @Nullable
    private FunctionAnalysis analyze(@NonNull InputValues values, @NonNull SolveResult result) {
        double[] k = values.toArray();
        List<Double> roots = new ArrayList<>();
        for (SolveResult.Root root : result.getRoots()) {
            roots.add(root.getRe());
        }
        if (PROBLEM_LINEAR.equals(problemId) && k.length == 2) {
            if (k[0] != 0 && roots.isEmpty()) {
                return null;
            }
            return FunctionAnalysis.linear(k[0], k[1], roots.isEmpty() ? 0 : roots.get(0));
        }
        if (PROBLEM_QUADRATIC.equals(problemId) && k.length == 3 && k[0] != 0) {
            // The vertex is the root of F'(x) = 2ax + b.
            char reals = NumberDomain.REALS.getCode();
            double[] raw = NativeSolver.linear(2 * k[0], k[1], reals);
            SolveResult derivative = SolveResultDecoder.decodeLinear(2 * k[0], k[1], reals, raw);
            if (derivative.getRoots().isEmpty()) {
                return null;
            }
            return FunctionAnalysis.quadratic(k[0], k[1], k[2], roots, derivative.getRoots().get(0).getRe());
        }
        return null;
    }

    private void bindAnalysis(@NonNull InputValues values, @NonNull FunctionAnalysis analysis) {
        List<Double> coefficients = new ArrayList<>();
        for (double value : values.toArray()) {
            coefficients.add(value);
        }
        analysisFunctionView.setText(getString(R.string.analysis_function,
                FormulaRenderer.renderExpression(cellLabels(), coefficients)));

        List<String> graph = new ArrayList<>();
        graph.add(getString(analysis.getGraphType() == FunctionAnalysis.GraphType.PARABOLA
                ? R.string.analysis_graph_parabola
                : R.string.analysis_graph_line));
        if (analysis.getBranches() != null) {
            graph.add(getString(analysis.getBranches() == FunctionAnalysis.Branches.UP
                    ? R.string.analysis_branches_up
                    : R.string.analysis_branches_down));
        }
        analysisGraphView.setText(TextUtils.join("\n", graph));

        analysisSignsView.setText(TextUtils.join("\n", Arrays.asList(
                getString(R.string.analysis_positive, Interval.format(analysis.getPositive())),
                getString(R.string.analysis_negative, Interval.format(analysis.getNegative())),
                getString(R.string.analysis_zero, Interval.format(analysis.getZero()))
        )));

        FunctionAnalysis.ExtremumType extremum = analysis.getExtremumType();
        if (extremum == null) {
            analysisBehaviorView.setVisibility(View.GONE);
            return;
        }
        String x = Interval.formatNumber(analysis.getExtremumX());
        String y = Interval.formatNumber(analysis.getExtremumY());
        String increasing = getString(R.string.analysis_increasing, Interval.format(analysis.getIncreasing()));
        String decreasing = getString(R.string.analysis_decreasing, Interval.format(analysis.getDecreasing()));
        List<String> lines = extremum == FunctionAnalysis.ExtremumType.MINIMUM
                ? Arrays.asList(getString(R.string.analysis_minimum, x, y), decreasing, increasing)
                : Arrays.asList(getString(R.string.analysis_maximum, x, y), increasing, decreasing);
        analysisBehaviorView.setText(TextUtils.join("\n", lines));
        analysisBehaviorView.setVisibility(View.VISIBLE);
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
