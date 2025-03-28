package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentAssignment;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAssignmentAdapter extends RecyclerView.Adapter<ExperimentAssignmentAdapter.AssignmentViewHolder> {

    private List<ExperimentAssignment> assignments = new ArrayList<>();
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(ExperimentAssignment assignment);
    }

    public ExperimentAssignmentAdapter(OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    public void setAssignments(List<ExperimentAssignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_experiment_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        ExperimentAssignment assignment = assignments.get(position);
        holder.bind(assignment);
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivExperimentIcon;
        private TextView tvAssignmentName;
        private TextView tvAssignmentDetails;

        AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExperimentIcon = itemView.findViewById(R.id.ivExperimentIcon);
            tvAssignmentName = itemView.findViewById(R.id.tvAssignmentName);
            tvAssignmentDetails = itemView.findViewById(R.id.tvAssignmentDetails);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAssignmentClick(assignments.get(position));
                }
            });
        }

        void bind(ExperimentAssignment assignment) {
            tvAssignmentName.setText(assignment.getAssignmentName());
            String details = assignment.getAssignmentDetails();
            if (details != null && !details.isEmpty()) {
                tvAssignmentDetails.setVisibility(View.VISIBLE);
                tvAssignmentDetails.setText(details);
            } else {
                tvAssignmentDetails.setVisibility(View.GONE);
            }
            
            // 根据任务完成状态设置不同的图标颜色
            if (assignment.isFinished()) {
                ivExperimentIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.green_complete), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                ivExperimentIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.blue_theme), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }
}
