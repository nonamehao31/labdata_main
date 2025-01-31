package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskCardAdapter extends RecyclerView.Adapter<TaskCardAdapter.TaskViewHolder> {
    private List<ExperimentTask> tasks = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(ExperimentTask task);
    }

    public TaskCardAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ExperimentTask task = tasks.get(position);
        holder.taskName.setText(task.getTaskId());
        
        // 设置截止日期，如果有的话
        String deadline = task.getDeadline() > 0 
            ? dateFormat.format(new Date(task.getDeadline()))
            : "无截止日期";
        holder.taskDeadline.setText("截止日期：" + deadline);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<ExperimentTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView taskDeadline;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            taskDeadline = itemView.findViewById(R.id.task_deadline);
        }
    }
}
