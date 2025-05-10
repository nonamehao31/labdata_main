package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.Project;

public class ProjectSelectionAdapter extends ListAdapter<Project, ProjectSelectionAdapter.ProjectViewHolder> {
    private final OnProjectSelectedListener listener;
    private Project selectedProject = null;

    public ProjectSelectionAdapter(OnProjectSelectedListener listener) {
        super(new ProjectDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = getItem(position);
        holder.bind(project, project == selectedProject, listener, () -> {
            selectedProject = project;
            notifyDataSetChanged();
        });
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectName;
        private final ImageButton btnDelete;
        private final View itemContainer;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.projectItemContainer);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Project project, boolean isSelected, OnProjectSelectedListener listener, Runnable onSelected) {
            tvProjectName.setText(project.getName());
            itemView.setSelected(isSelected);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectSelected(project);
                    onSelected.run();
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectDeleteRequested(project);
                }
            });
        }
    }

    public interface OnProjectSelectedListener {
        void onProjectSelected(Project project);
        void onProjectDeleteRequested(Project project);
    }

    private static class ProjectDiffCallback extends DiffUtil.ItemCallback<Project> {
        @Override
        public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
