package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;

import java.util.List;

public class ExperimentTaskAdapter extends RecyclerView.Adapter<ExperimentTaskAdapter.TaskViewHolder> {

    private Context context;
    private List<ExperimentTask> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(ExperimentTask task);
    }

    public ExperimentTaskAdapter(Context context, List<ExperimentTask> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_experiment_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ExperimentTask task = taskList.get(position);
        
        holder.tvTaskName.setText(task.getTaskName());
        holder.tvTaskInfo.setText(task.getTaskInfo());

        LinearLayout taskContainer = holder.itemView.findViewById(R.id.taskContainer);
        
        if (task.isCompleted()) {
            taskContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.task_completed_bg));
            holder.tvTaskName.setTextColor(ContextCompat.getColor(context, R.color.task_completed_text));
            holder.tvTaskInfo.setTextColor(ContextCompat.getColor(context, R.color.task_completed_text));
        } else {
            taskContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.task_pending_bg));
            holder.tvTaskName.setTextColor(ContextCompat.getColor(context, R.color.task_pending_text));
            holder.tvTaskInfo.setTextColor(ContextCompat.getColor(context, R.color.task_pending_text));
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvTaskInfo;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskInfo = itemView.findViewById(R.id.tvTaskInfo);
        }
    }
}
