package com.skywrc.am.equality;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.ProblemViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(Problem problem);
    }

    private final List<Problem> allProblems;
    private final List<Problem> visibleProblems = new ArrayList<>();
    private final OnProblemClickListener clickListener;

    public ProblemAdapter(List<Problem> problems, OnProblemClickListener clickListener) {
        this.allProblems = new ArrayList<>(problems);
        this.visibleProblems.addAll(problems);
        this.clickListener = clickListener;
    }

    public void filter(String query) {
        visibleProblems.clear();
        if (query == null || query.trim().isEmpty()) {
            visibleProblems.addAll(allProblems);
        } else {
            String lower = query.trim().toLowerCase(Locale.ROOT);
            for (Problem problem : allProblems) {
                if (problem.getName().toLowerCase(Locale.ROOT).contains(lower)
                        || problem.getFormula().toLowerCase(Locale.ROOT).contains(lower)) {
                    visibleProblems.add(problem);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return visibleProblems.isEmpty();
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
        holder.bind(visibleProblems.get(position));
    }

    @Override
    public int getItemCount() {
        return visibleProblems.size();
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
        }

        void bind(Problem problem) {
            nameView.setText(problem.getName());
            formulaView.setText(problem.getFormula());
            paramCountView.setText(itemView.getContext().getString(
                    R.string.parameter_count_format,
                    problem.getParameterCount()
            ));
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProblemClick(problem);
                }
            });
        }
    }
}
