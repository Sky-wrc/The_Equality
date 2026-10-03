package com.skywrc.am.equality;

import androidx.annotation.StringRes;

public enum NumberDomain {
    NATURALS('N', R.string.domain_naturals),
    INTEGERS('Z', R.string.domain_integers),
    REALS('R', R.string.domain_reals),
    COMPLEX('C', R.string.domain_complex);

    private final char code;
    @StringRes
    private final int labelResId;

    NumberDomain(char code, @StringRes int labelResId) {
        this.code = code;
        this.labelResId = labelResId;
    }

    public char getCode() {
        return code;
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
