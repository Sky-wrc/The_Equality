package com.skywrc.am.equality;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProblemAdapter extends ListAdapter<Problem, ProblemAdapter.ProblemViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(Problem problem);
    }

    private static final DiffUtil.ItemCallback<Problem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Problem>() {
                @Override
                public boolean areItemsTheSame(@NonNull Problem oldItem, @NonNull Problem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Problem oldItem, @NonNull Problem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId())
                            && Objects.equals(oldItem.getName(), newItem.getName())
                            && Objects.equals(oldItem.getFormula(), newItem.getFormula())
                            && oldItem.getParameterCount() == newItem.getParameterCount();
                }
            };

    private final List<Problem> allProblems = new ArrayList<>();
    private final OnProblemClickListener clickListener;

    public ProblemAdapter(@NonNull OnProblemClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    public void submitSource(@NonNull List<Problem> problems, @Nullable Runnable onCommitted) {
        allProblems.clear();
        allProblems.addAll(problems);
        submitList(new ArrayList<>(allProblems), onCommitted);
    }

    public void filter(String query, @Nullable Runnable onCommitted) {
        if (query == null || query.trim().isEmpty()) {
            submitList(new ArrayList<>(allProblems), onCommitted);
            return;
        }
        String lower = query.trim().toLowerCase(Locale.ROOT);
        List<Problem> filtered = new ArrayList<>();
        for (Problem problem : allProblems) {
            if (problem.getName().toLowerCase(Locale.ROOT).contains(lower)
                    || problem.getFormula().toLowerCase(Locale.ROOT).contains(lower)) {
                filtered.add(problem);
            }
        }
        submitList(filtered, onCommitted);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_problem, parent, false);
        return new ProblemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProblemViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView formulaView;
        private final TextView paramCountView;

        ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.problem_name);
            formulaView = itemView.findViewById(R.id.problem_formula);
            paramCountView = itemView.findViewById(R.id.problem_param_count);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || clickListener == null) {
                    return;
                }
                clickListener.onProblemClick(getItem(position));
            });
        }

        void bind(Problem problem) {
            nameView.setText(problem.getName());
            formulaView.setText(problem.getFormula());
            paramCountView.setText(itemView.getContext().getString(
                    R.string.parameter_count_format,
                    problem.getParameterCount()
            ));
        }
    }
}
