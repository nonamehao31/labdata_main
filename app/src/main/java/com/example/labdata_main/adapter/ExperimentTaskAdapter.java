package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExperimentTaskAdapter extends RecyclerView.Adapter<ExperimentTaskAdapter.TaskViewHolder> {
    private List<ExperimentTask> tasks = new ArrayList<>();
    private OnTaskClickListener listener;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskClick(ExperimentTask task);
    }

    public ExperimentTaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        ExperimentTask task = tasks.get(position);
        
        // 设置任务名称
        String taskName = task.getTaskName();
        if (taskName != null && !taskName.isEmpty()) {
            holder.taskName.setText(taskName);
        } else {
            holder.taskName.setText("未命名任务");
        }

        // 设置截止日期
        long deadline = task.getDeadline();
        if (deadline > 0) {
            String formattedDate = DATE_FORMAT.format(new Date(deadline));
            holder.taskDeadline.setText(String.format("截止日期：%s", formattedDate));
        } else {
            holder.taskDeadline.setText("无截止日期");
        }

        // 设置按钮为空文本，只显示图标
        holder.taskButton.setText("");
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<ExperimentTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton taskButton;
        private final TextView taskName;
        private final TextView taskDeadline;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskButton = itemView.findViewById(R.id.task_button);
            taskName = itemView.findViewById(R.id.task_name);
            taskDeadline = itemView.findViewById(R.id.task_deadline);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }
    }
}
