package com.skywrc.am.equality;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AppPreferences {

    private static final String PREFS_NAME = "equality_prefs";
    private static final String KEY_NUMBER_DOMAIN = "number_domain";
    private static final String KEY_FAVORITE_PROBLEMS = "favorite_problems";

    private final SharedPreferences preferences;

    public AppPreferences(@NonNull Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public NumberDomain getNumberDomain() {
        return NumberDomain.fromName(
                preferences.getString(KEY_NUMBER_DOMAIN, NumberDomain.REALS.name()),
                NumberDomain.REALS
        );
    }

    public void setNumberDomain(@NonNull NumberDomain domain) {
        preferences.edit().putString(KEY_NUMBER_DOMAIN, domain.name()).apply();
    }

    @NonNull
    public Set<String> getFavoriteProblemIds() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITE_PROBLEMS, Collections.emptySet()));
    }

    public void setFavorite(@NonNull String problemId, boolean favorite) {
        Set<String> ids = getFavoriteProblemIds();
        if (favorite) {
            ids.add(problemId);
        } else {
            ids.remove(problemId);
        }
        preferences.edit().putStringSet(KEY_FAVORITE_PROBLEMS, ids).apply();
    }
}
