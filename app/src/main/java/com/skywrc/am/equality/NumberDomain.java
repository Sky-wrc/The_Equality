package com.skywrc.am.equality;

import androidx.annotation.StringRes;

public enum NumberDomain {
    INTEGERS(R.string.domain_integers),
    RATIONALS(R.string.domain_rationals),
    REALS(R.string.domain_reals),
    COMPLEX(R.string.domain_complex);

    @StringRes
    private final int labelResId;

    NumberDomain(@StringRes int labelResId) {
        this.labelResId = labelResId;
    }

    @StringRes
    public int getLabelResId() {
        return labelResId;
    }

    public static NumberDomain fromName(String name, NumberDomain fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return NumberDomain.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
