package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentType;
import java.util.HashSet;
import java.util.Set;

public class ExperimentTypeAdapter extends ListAdapter<ExperimentType, ExperimentTypeAdapter.ExperimentTypeViewHolder> {
    private final Set<ExperimentType> selectedExperiments = new HashSet<>();

    public ExperimentTypeAdapter() {
        super(new ExperimentTypeDiffCallback());
    }

    @NonNull
    @Override
    public ExperimentTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_type, parent, false);
        return new ExperimentTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperimentTypeViewHolder holder, int position) {
        ExperimentType experimentType = getItem(position);
        holder.bind(experimentType, selectedExperiments.contains(experimentType));
    }

    public Set<ExperimentType> getSelectedExperiments() {
        return new HashSet<>(selectedExperiments);
    }

    public void toggleSelection(ExperimentType experimentType) {
        if (selectedExperiments.contains(experimentType)) {
            selectedExperiments.remove(experimentType);
        } else {
            selectedExperiments.add(experimentType);
        }
        notifyItemChanged(getCurrentList().indexOf(experimentType));
    }

    class ExperimentTypeViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        ExperimentTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);

            // 点击整个项目或复选框都能触发选择
            View.OnClickListener clickListener = v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ExperimentType experimentType = getItem(position);
                    toggleSelection(experimentType);
                }
            };

            itemView.setOnClickListener(clickListener);
            checkBox.setOnClickListener(clickListener);
        }

        void bind(ExperimentType experimentType, boolean isSelected) {
            checkBox.setText(experimentType.getName());
            checkBox.setChecked(isSelected);
        }
    }

    private static class ExperimentTypeDiffCallback extends DiffUtil.ItemCallback<ExperimentType> {
        @Override
        public boolean areItemsTheSame(@NonNull ExperimentType oldItem, @NonNull ExperimentType newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExperimentType oldItem, @NonNull ExperimentType newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getType().equals(newItem.getType());
        }
    }
}
