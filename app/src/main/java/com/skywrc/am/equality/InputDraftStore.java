package com.skywrc.am.equality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Remembers the texts typed on {@link ProblemInputActivity} per problem.
 * Kept in memory on purpose: drafts survive leaving and reopening a problem,
 * but disappear together with the app process or when {@link #clear()} is called on exit.
 */
final class InputDraftStore {

    private static final Map<String, ArrayList<String>> DRAFTS = new HashMap<>();

    private InputDraftStore() {
    }

    static void save(@NonNull String problemId, @NonNull List<String> texts) {
        DRAFTS.put(problemId, new ArrayList<>(texts));
    }

    @Nullable
    static ArrayList<String> load(@NonNull String problemId) {
        ArrayList<String> texts = DRAFTS.get(problemId);
        return texts != null ? new ArrayList<>(texts) : null;
    }

    static void remove(@NonNull String problemId) {
        DRAFTS.remove(problemId);
    }

    static void clear() {
        DRAFTS.clear();
    }
}
