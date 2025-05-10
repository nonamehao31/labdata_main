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

public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {
    private final OnProjectClickListener listener;

    public ProjectAdapter(OnProjectClickListener listener) {
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
        holder.bind(project, listener);
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectName;
        private final ImageButton btnDelete;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Project project, OnProjectClickListener listener) {
            tvProjectName.setText(project.getName());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectDeleteClick(project);
                }
            });
        }
    }

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onProjectDeleteClick(Project project);
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
