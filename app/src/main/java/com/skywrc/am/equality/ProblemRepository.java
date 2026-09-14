package com.skywrc.am.equality;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProblemRepository {

    private ProblemRepository() {
    }

    @NonNull
    public static List<Problem> getSampleProblems() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
                new Problem(
                        "quadratic",
                        "quadratic equation",
                        "ax²+bx+c=0",
                        3
                ),
                new Problem(
                        "linear",
                        "linear equation",
                        "ax+b=0",
                        2
                ),
                new Problem(
                        "cubic",
                        "cubic equation",
                        "ax³+bx²+cx+d=0",
                        4
                )
        )));
    }
}
