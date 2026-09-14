package com.skywrc.am.equality;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private ProblemAdapter adapter;
    private TextView emptyView;
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        TextView titleProblems = findViewById(R.id.title_problems);
        View searchButton = findViewById(R.id.btn_search);
        RecyclerView recycler = findViewById(R.id.recycler_problems);
        emptyView = findViewById(R.id.empty_view);
        searchInputLayout = findViewById(R.id.search_input_layout);
        searchInput = findViewById(R.id.search_input);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            titleProblems.setPadding(
                    titleProblems.getPaddingLeft(),
                    systemBars.top + dpToPx(16),
                    titleProblems.getPaddingRight(),
                    titleProblems.getPaddingBottom()
            );
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        adapter = new ProblemAdapter(ProblemRepository.getSampleProblems(), problem ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        );
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        updateEmptyState();

        searchButton.setOnClickListener(v -> toggleSearch());

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                    updateEmptyState();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void toggleSearch() {
        if (searchInputLayout.getVisibility() == View.VISIBLE) {
            searchInputLayout.setVisibility(View.GONE);
            if (searchInput != null) {
                searchInput.setText("");
            }
            adapter.filter("");
            updateEmptyState();
        } else {
            searchInputLayout.setVisibility(View.VISIBLE);
            if (searchInput != null) {
                searchInput.requestFocus();
            }
        }
    }

    private void updateEmptyState() {
        emptyView.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
