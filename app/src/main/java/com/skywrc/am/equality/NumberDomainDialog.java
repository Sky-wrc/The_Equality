package com.skywrc.am.equality;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class NumberDomainDialog {

    private NumberDomainDialog() {
    }

    public static void show(@NonNull Context context, @NonNull AppPreferences preferences,
                            @NonNull Consumer<NumberDomain> onSelected) {
        NumberDomain[] domains = NumberDomain.values();
        String[] labels = new String[domains.length];
        int checked = 0;
        NumberDomain current = preferences.getNumberDomain();
        for (int i = 0; i < domains.length; i++) {
            labels[i] = context.getString(domains[i].getLabelResId());
            if (domains[i] == current) {
                checked = i;
            }
        }

        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_The_Equality_AlertDialog)
                .setTitle(R.string.number_domain_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    NumberDomain selected = domains[which];
                    preferences.setNumberDomain(selected);
                    Toast.makeText(
                            context,
                            context.getString(R.string.number_domain_saved,
                                    context.getString(selected.getLabelResId())),
                            Toast.LENGTH_SHORT
                    ).show();
                    dialog.dismiss();
                    onSelected.accept(selected);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
