package com.skywrc.am.equality;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

        adapter = new ProblemAdapter(this::openProblem, this::toggleFavorite);
        adapter.submitSource(ProblemRepository.getSampleProblems(this),
                preferences.getFavoriteProblemIds(), this::updateEmptyState);
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

    @SuppressWarnings("deprecation")
    private void openProblem(@NonNull Problem problem) {
        startActivity(ProblemInputActivity.createIntent(this, problem));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            InputDraftStore.clear();
        }
        super.onDestroy();
    }

    private void showOverflowMenu(View anchor) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(
                this,
                R.style.ThemeOverlay_The_Equality_PopupMenu
        );
        PopupMenu popupMenu = new PopupMenu(wrapper, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_overflow, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.action_number_domain).setTitle(numberDomainMenuTitle());
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
        NumberDomainDialog.show(this, preferences, selected -> { });
    }

    @NonNull
    private CharSequence numberDomainMenuTitle() {
        String code = String.valueOf(preferences.getNumberDomain().getCode());
        SpannableString title = new SpannableString(getString(R.string.menu_number_domain_with_code, code));
        int start = title.length() - code.length();
        title.setSpan(new ForegroundColorSpan(getColor(R.color.text_secondary)), start, title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setSpan(new StyleSpan(Typeface.BOLD), start, title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return title;
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

    private void toggleFavorite(@NonNull Problem problem) {
        boolean favorite = !preferences.getFavoriteProblemIds().contains(problem.getId());
        preferences.setFavorite(problem.getId(), favorite);
        adapter.setFavorites(preferences.getFavoriteProblemIds(), this::updateEmptyState);
    }

    private void updateEmptyState() {
        emptyView.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
