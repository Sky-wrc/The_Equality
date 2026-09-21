package com.skywrc.am.equality;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private ProblemAdapter adapter;
    private TextView emptyView;
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchInput;
    private View headerBar;
    private AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferences = new AppPreferences(this);

        View root = findViewById(R.id.main);
        headerBar = findViewById(R.id.header_bar);
        View searchButton = findViewById(R.id.btn_search);
        View addButton = findViewById(R.id.btn_add);
        View moreButton = findViewById(R.id.btn_more);
        RecyclerView recycler = findViewById(R.id.recycler_problems);
        emptyView = findViewById(R.id.empty_view);
        searchInputLayout = findViewById(R.id.search_input_layout);
        searchInput = findViewById(R.id.search_input);

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

        adapter = new ProblemAdapter(problem ->
                startActivity(CoefficientInputActivity.createIntent(this, problem))
        );
        adapter.submitSource(ProblemRepository.getSampleProblems(this), this::updateEmptyState);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        searchButton.setOnClickListener(v -> toggleSearch());
        addButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        );
        moreButton.setOnClickListener(this::showOverflowMenu);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString(), MainActivity.this::updateEmptyState);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboardAndClearFocus();
                return true;
            }
            return false;
        });
    }

    private void showOverflowMenu(View anchor) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(
                this,
                R.style.ThemeOverlay_The_Equality_PopupMenu
        );
        PopupMenu popupMenu = new PopupMenu(wrapper, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_overflow, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_about) {
                showAboutDialog();
                return true;
            }
            if (id == R.id.action_number_domain) {
                showNumberDomainDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_The_Equality_AlertDialog)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showNumberDomainDialog() {
        NumberDomain[] domains = NumberDomain.values();
        String[] labels = new String[domains.length];
        int checked = 0;
        NumberDomain current = preferences.getNumberDomain();
        for (int i = 0; i < domains.length; i++) {
            labels[i] = getString(domains[i].getLabelResId());
            if (domains[i] == current) {
                checked = i;
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_The_Equality_AlertDialog)
                .setTitle(R.string.number_domain_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    NumberDomain selected = domains[which];
                    preferences.setNumberDomain(selected);
                    Toast.makeText(
                            this,
                            getString(R.string.number_domain_saved, getString(selected.getLabelResId())),
                            Toast.LENGTH_SHORT
                    ).show();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleSearch() {
        if (searchInputLayout.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            searchInputLayout.setVisibility(View.VISIBLE);
            searchInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void closeSearch() {
        searchInputLayout.setVisibility(View.GONE);
        searchInput.setText("");
        adapter.filter("", this::updateEmptyState);
        hideKeyboardAndClearFocus();
    }

    private void hideKeyboardAndClearFocus() {
        View focused = getCurrentFocus();
        if (focused == null) {
            focused = searchInput;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && focused != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
        searchInput.clearFocus();
    }

    private void updateEmptyState() {
        emptyView.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
