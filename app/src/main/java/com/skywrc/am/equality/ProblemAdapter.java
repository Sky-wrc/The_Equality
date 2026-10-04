package com.skywrc.am.equality;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Shows problems with favorites first, each group keeping the source order. */
public class ProblemAdapter extends ListAdapter<ProblemAdapter.Item, ProblemAdapter.ProblemViewHolder> {

    public interface OnProblemClickListener {
        void onProblemClick(Problem problem);
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(Problem problem);
    }

    static final class Item {
        final Problem problem;
        final boolean favorite;

        Item(@NonNull Problem problem, boolean favorite) {
            this.problem = problem;
            this.favorite = favorite;
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Item>() {
                @Override
                public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                    return Objects.equals(oldItem.problem.getId(), newItem.problem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                    Problem oldProblem = oldItem.problem;
                    Problem newProblem = newItem.problem;
                    return oldItem.favorite == newItem.favorite
                            && Objects.equals(oldProblem.getName(), newProblem.getName())
                            && Objects.equals(oldProblem.getFormula(), newProblem.getFormula())
                            && oldProblem.getParameterCount() == newProblem.getParameterCount();
                }
            };

    private final List<Problem> allProblems = new ArrayList<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private final OnProblemClickListener clickListener;
    private final OnFavoriteToggleListener favoriteListener;
    private String query = "";

    public ProblemAdapter(@NonNull OnProblemClickListener clickListener,
                          @NonNull OnFavoriteToggleListener favoriteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.favoriteListener = favoriteListener;
    }

    public void submitSource(@NonNull List<Problem> problems, @NonNull Set<String> favorites,
                             @Nullable Runnable onCommitted) {
        allProblems.clear();
        allProblems.addAll(problems);
        favoriteIds.clear();
        favoriteIds.addAll(favorites);
        publish(onCommitted);
    }

    public void setFavorites(@NonNull Set<String> favorites, @Nullable Runnable onCommitted) {
        favoriteIds.clear();
        favoriteIds.addAll(favorites);
        publish(onCommitted);
    }

    public void filter(@Nullable String query, @Nullable Runnable onCommitted) {
        this.query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        publish(onCommitted);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    private void publish(@Nullable Runnable onCommitted) {
        List<Item> favorites = new ArrayList<>();
        List<Item> others = new ArrayList<>();
        for (Problem problem : allProblems) {
            if (!matchesQuery(problem)) {
                continue;
            }
            boolean favorite = favoriteIds.contains(problem.getId());
            (favorite ? favorites : others).add(new Item(problem, favorite));
        }
        favorites.addAll(others);
        submitList(favorites, onCommitted);
    }

    private boolean matchesQuery(@NonNull Problem problem) {
        return query.isEmpty()
                || problem.getName().toLowerCase(Locale.ROOT).contains(query)
                || problem.getFormula().toLowerCase(Locale.ROOT).contains(query);
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
        private final ImageButton favoriteButton;

        ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.problem_name);
            formulaView = itemView.findViewById(R.id.problem_formula);
            paramCountView = itemView.findViewById(R.id.problem_param_count);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onProblemClick(getItem(position).problem);
                }
            });
            favoriteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    favoriteListener.onFavoriteToggle(getItem(position).problem);
                }
            });
        }

        void bind(@NonNull Item item) {
            Problem problem = item.problem;
            nameView.setText(problem.getName());
            formulaView.setText(problem.getFormula());
            paramCountView.setText(itemView.getContext().getString(
                    R.string.parameter_count_format,
                    problem.getParameterCount()
            ));
            favoriteButton.setImageResource(item.favorite ? R.drawable.ic_star_24 : R.drawable.ic_star_outline_24);
            favoriteButton.setContentDescription(itemView.getContext().getString(item.favorite
                    ? R.string.content_description_remove_favorite
                    : R.string.content_description_add_favorite));
        }
    }
}
