package com.skywrc.am.equality;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class AppPreferences {

    private static final String PREFS_NAME = "equality_prefs";
    private static final String KEY_NUMBER_DOMAIN = "number_domain";

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
}
