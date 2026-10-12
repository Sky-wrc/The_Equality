package com.skywrc.am.equality;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;

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

    private static final float ANALYSIS_BODY_MAX_SP = 16f;
    private static final float ANALYSIS_BODY_MIN_SP = 12f;
    private static final float ANALYSIS_FUNCTION_MAX_SP = 22f;
    private static final float ANALYSIS_FUNCTION_MIN_SP = 16f;
    private static final float ANALYSIS_STEP_SP = 0.5f;
    private static final int ANALYSIS_COMFORT_LINES = 6;
    /** Inner padding of analysis_card in activity_problem_input.xml. */
    private static final int ANALYSIS_CARD_PADDING_DP = 20;

    private static final String PROBLEM_LINEAR = "linear";
    private static final String PROBLEM_QUADRATIC = "quadratic";
    private static final String PROBLEM_CUBIC = "cubic";
    private static final Set<String> POLYNOMIAL_PROBLEMS =
            new HashSet<>(Arrays.asList(PROBLEM_LINEAR, PROBLEM_QUADRATIC, PROBLEM_CUBIC));

    private String problemId;
    private InputPanel inputPanel;
    private AppPreferences preferences;
    private TextView formulaView;
    private MaterialButton domainButton;
    private ImageButton simplifyRootsButton;
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

        simplifyRootsButton = findViewById(R.id.btn_simplify_roots);
        updateSimplifyRootsButton();
        simplifyRootsButton.setOnClickListener(v -> {
            preferences.setSimplifyRoots(!preferences.isSimplifyRoots());
            updateSimplifyRootsButton();
            if (resultCard.getVisibility() == View.VISIBLE) {
                calculate();
            }
        });

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

    private void updateSimplifyRootsButton() {
        boolean simplify = preferences.isSimplifyRoots();
        ImageViewCompat.setImageTintList(simplifyRootsButton, ColorStateList.valueOf(
                getColor(simplify ? R.color.accent : R.color.text_secondary)));
        simplifyRootsButton.setContentDescription(getString(simplify
                ? R.string.content_description_simplify_roots_on
                : R.string.content_description_simplify_roots_off));
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

        boolean rootForm = preferences.isSimplifyRoots();
        SolveResult result = solve(values, domain.getCode(), rootForm);
        if (result == null) {
            resultDiscriminantView.setVisibility(View.GONE);
            resultSqrtDiscriminantView.setVisibility(View.GONE);
            resultDivisionView.setVisibility(View.GONE);
            resultBodyView.setText(R.string.result_solver_unavailable);
        } else {
            showResult(result, values.toArray()[0]);
        }

        // The analysis needs the explicit roots, which the root form doesn't contain.
        SolveResult explicitResult = rootForm ? solve(values, domain.getCode(), false) : result;
        FunctionAnalysis analysis = domain == NumberDomain.REALS && explicitResult != null
                ? analyze(values, explicitResult)
                : null;
        if (analysis == null) {
            functionButton.setVisibility(View.GONE);
            analysisCard.setVisibility(View.GONE);
        } else {
            bindAnalysis(values, analysis);
            fitAnalysisTextWhenLaidOut();
            functionButton.setVisibility(View.VISIBLE);
        }

        resultCard.setVisibility(View.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, resultCard.getBottom()));
    }

    @Nullable
    private SolveResult solve(@NonNull InputValues values, char domain, boolean rootForm) {
        double[] k = values.toArray();
        if (PROBLEM_LINEAR.equals(problemId) && k.length == 2) {
            double[] raw = NativeSolver.linear(k[0], k[1], domain);
            return SolveResultDecoder.decodeLinear(k[0], k[1], domain, raw);
        }
        if (PROBLEM_QUADRATIC.equals(problemId) && k.length == 3) {
            double[] raw = NativeSolver.quadratic(k[0], k[1], k[2], domain, rootForm);
            return SolveResultDecoder.decodeQuadratic(k[0], k[1], k[2], domain, rootForm, raw);
        }
        if (PROBLEM_CUBIC.equals(problemId) && k.length == 4) {
            return SolveResultDecoder.decodeCubic(NativeSolver.cubic(k[0], k[1], k[2], k[3], domain));
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
        if (PROBLEM_CUBIC.equals(problemId) && k.length == 4 && k[0] != 0) {
            // Extrema are the roots of F'(x) = 3ax² + 2bx + c, the inflection point is the root of F''(x) = 6ax + 2b.
            char reals = NumberDomain.REALS.getCode();
            double[] rawDerivative = NativeSolver.quadratic(3 * k[0], 2 * k[1], k[2], reals, false);
            SolveResult derivative = SolveResultDecoder.decodeQuadratic(
                    3 * k[0], 2 * k[1], k[2], reals, false, rawDerivative);
            List<Double> criticalPoints = new ArrayList<>();
            for (SolveResult.Root root : derivative.getRoots()) {
                criticalPoints.add(root.getRe());
            }
            double[] rawSecond = NativeSolver.linear(6 * k[0], 2 * k[1], reals);
            SolveResult second = SolveResultDecoder.decodeLinear(6 * k[0], 2 * k[1], reals, rawSecond);
            if (second.getRoots().isEmpty()) {
                return null;
            }
            return FunctionAnalysis.cubic(k[0], k[1], k[2], k[3], roots, criticalPoints,
                    second.getRoots().get(0).getRe());
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

        boolean cubic = analysis.getGraphType() == FunctionAnalysis.GraphType.CUBIC_PARABOLA;
        List<String> graph = new ArrayList<>();
        switch (analysis.getGraphType()) {
            case PARABOLA:
                graph.add(getString(R.string.analysis_graph_parabola));
                break;
            case CUBIC_PARABOLA:
                graph.add(getString(R.string.analysis_graph_cubic_parabola));
                break;
            case STRAIGHT_LINE:
            default:
                graph.add(getString(R.string.analysis_graph_line));
                break;
        }
        if (analysis.getBranches() != null) {
            boolean up = analysis.getBranches() == FunctionAnalysis.Branches.UP;
            if (cubic) {
                graph.add(getString(up ? R.string.analysis_cubic_rising : R.string.analysis_cubic_falling));
            } else {
                graph.add(getString(up ? R.string.analysis_branches_up : R.string.analysis_branches_down));
            }
        }
        analysisGraphView.setText(TextUtils.join("\n", graph));

        analysisSignsView.setText(TextUtils.join("\n", Arrays.asList(
                getString(R.string.analysis_positive, Interval.format(analysis.getPositive())),
                getString(R.string.analysis_negative, Interval.format(analysis.getNegative())),
                getString(R.string.analysis_zero, Interval.format(analysis.getZero()))
        )));

        List<Interval> increasing = analysis.getIncreasing();
        List<Interval> decreasing = analysis.getDecreasing();
        if (increasing.isEmpty() && decreasing.isEmpty()) {
            analysisBehaviorView.setVisibility(View.GONE);
            return;
        }
        List<String> lines = new ArrayList<>();
        for (FunctionAnalysis.Extremum extremum : analysis.getExtrema()) {
            lines.add(getString(extremum.getType() == FunctionAnalysis.ExtremumType.MINIMUM
                            ? R.string.analysis_minimum
                            : R.string.analysis_maximum,
                    Interval.formatNumber(extremum.getX()), Interval.formatNumber(extremum.getY())));
        }
        // The set that starts at −∞ goes first, so the lines follow the graph from left to right.
        String increasingLine = increasing.isEmpty() ? null
                : getString(R.string.analysis_increasing, Interval.format(increasing));
        String decreasingLine = decreasing.isEmpty() ? null
                : getString(R.string.analysis_decreasing, Interval.format(decreasing));
        boolean increasingFirst = !increasing.isEmpty() && increasing.get(0).startsAtMinusInfinity();
        for (String line : increasingFirst
                ? Arrays.asList(increasingLine, decreasingLine)
                : Arrays.asList(decreasingLine, increasingLine)) {
            if (line != null) {
                lines.add(line);
            }
        }
        Double inflectionX = analysis.getInflectionX();
        if (inflectionX != null) {
            lines.add(getString(R.string.analysis_inflection,
                    Interval.formatNumber(inflectionX), Interval.formatNumber(analysis.getInflectionY())));
        }
        analysisBehaviorView.setText(TextUtils.join("\n", lines));
        analysisBehaviorView.setVisibility(View.VISIBLE);
    }

    /** The analysis card may still be gone, so its width is taken from the scroll view once it has one. */
    private void fitAnalysisTextWhenLaidOut() {
        if (scrollView.getWidth() > 0) {
            fitAnalysisText();
            return;
        }
        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                fitAnalysisText();
            }
        });
    }

    /**
     * Shrinks the analysis text a little for every line above {@link #ANALYSIS_COMFORT_LINES} and then
     * until the widest line fits without wrapping, but never below the minimum size.
     */
    private void fitAnalysisText() {
        float available = scrollView.getWidth() - scrollView.getPaddingLeft() - scrollView.getPaddingRight()
                - 2 * dpToPx(ANALYSIS_CARD_PADDING_DP);
        if (available <= 0) {
            return;
        }

        TextView[] bodyViews = {analysisGraphView, analysisSignsView, analysisBehaviorView};
        List<String> lines = new ArrayList<>();
        for (TextView view : bodyViews) {
            if (view.getVisibility() == View.VISIBLE) {
                lines.addAll(Arrays.asList(view.getText().toString().split("\n")));
            }
        }
        float bodySize = Math.max(ANALYSIS_BODY_MIN_SP,
                ANALYSIS_BODY_MAX_SP - ANALYSIS_STEP_SP * Math.max(0, lines.size() - ANALYSIS_COMFORT_LINES));
        bodySize = fitWidth(analysisSignsView, lines, available, bodySize, ANALYSIS_BODY_MIN_SP);
        for (TextView view : bodyViews) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, bodySize);
        }

        float functionSize = fitWidth(analysisFunctionView,
                Arrays.asList(analysisFunctionView.getText().toString()),
                available, ANALYSIS_FUNCTION_MAX_SP, ANALYSIS_FUNCTION_MIN_SP);
        analysisFunctionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, functionSize);
    }

    /** The largest size from {@code startSp} down to {@code minSp} at which every line fits into {@code width}. */
    private float fitWidth(@NonNull TextView view, @NonNull List<String> lines, float width,
                           float startSp, float minSp) {
        TextPaint paint = new TextPaint(view.getPaint());
        float size = startSp;
        while (size > minSp) {
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size,
                    getResources().getDisplayMetrics()));
            float widest = 0;
            for (String line : lines) {
                widest = Math.max(widest, paint.measureText(line));
            }
            if (widest <= width) {
                break;
            }
            size -= ANALYSIS_STEP_SP;
        }
        return Math.max(size, minSp);
    }

    private void showResult(@NonNull SolveResult result, double a) {
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
            if (PROBLEM_CUBIC.equals(problemId)) {
                // √D is part of the quadratic formula only.
                resultSqrtDiscriminantView.setVisibility(View.GONE);
            } else {
                resultSqrtDiscriminantView.setText(
                        getString(R.string.result_sqrt_discriminant, formatSqrt(discriminant))
                );
                resultSqrtDiscriminantView.setVisibility(View.VISIBLE);
            }
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
            case ROOT_FORM:
                resultBodyView.setText(formatRootForm(result.getCenter(), discriminant, a));
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
            case NOT_CUBIC:
                resultBodyView.setText(R.string.result_not_cubic);
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
            lines.add(getString(R.string.result_root_indexed, NumberFormatter.subscript(i + 1),
                    formatRoot(roots.get(i))));
        }
        return TextUtils.join("\n", lines);
    }

    /** Real roots go in ascending order; complex ones in the solver's order, +i√|D|/2a first. */
    @NonNull
    private String formatRootForm(double center, double discriminant, double a) {
        boolean firstPlus = discriminant < 0 && a > 0;
        return getString(R.string.result_root_indexed, NumberFormatter.subscript(1),
                NumberFormatter.formatRootForm(center, discriminant, a, firstPlus))
                + "\n"
                + getString(R.string.result_root_indexed, NumberFormatter.subscript(2),
                NumberFormatter.formatRootForm(center, discriminant, a, !firstPlus));
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
