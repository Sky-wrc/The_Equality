package com.skywrc.am.equality;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProblemRepository {

    private ProblemRepository() {
    }

    @NonNull
    public static List<Problem> getSampleProblems(@NonNull Context context) {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
                new Problem(
                        "linear",
                        context.getString(R.string.problem_linear_name),
                        context.getString(R.string.problem_linear_formula),
                        2
                ),
                new Problem(
                        "quadratic",
                        context.getString(R.string.problem_quadratic_name),
                        context.getString(R.string.problem_quadratic_formula),
                        3
                ),
                new Problem(
                        "cubic",
                        context.getString(R.string.problem_cubic_name),
                        context.getString(R.string.problem_cubic_formula),
                        4
                )
        )));
    }
}
