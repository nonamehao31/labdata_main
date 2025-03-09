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

        // 设置任务类型
        String experimentType = task.getExperimentType();
        if (experimentType != null && !experimentType.isEmpty()) {
            holder.taskType.setText(experimentType);
            holder.taskType.setVisibility(View.VISIBLE);
        } else {
            holder.taskType.setVisibility(View.GONE);
        }

        // 设置任务状态
        String status = task.getStatus();
        if (status != null && !status.isEmpty()) {
            holder.taskStatus.setText(status);
            
            // 根据状态设置背景
            if ("已完成".equals(status)) {
                holder.taskStatus.setBackgroundResource(R.drawable.status_background_completed);
            } else if ("进行中".equals(status)) {
                holder.taskStatus.setBackgroundResource(R.drawable.status_background_in_progress);
            } else {
                holder.taskStatus.setBackgroundResource(R.drawable.status_background_pending);
            }
        } else {
            holder.taskStatus.setVisibility(View.GONE);
        }

        // 设置创建时间
        long creationTime = task.getCreationTime();
        if (creationTime > 0) {
            String formattedDate = DATE_FORMAT.format(new Date(creationTime));
            holder.taskCreationTime.setText(String.format("创建时间：%s", formattedDate));
        } else {
            holder.taskCreationTime.setText("无创建时间");
        }

        // 设置截止日期
        long deadline = task.getDeadline();
        if (deadline > 0) {
            String formattedDate = DATE_FORMAT.format(new Date(deadline));
            holder.taskDeadline.setText(String.format("截止日期：%s", formattedDate));
        } else {
            holder.taskDeadline.setText("无截止日期");
        }
        
        // 设置整个卡片的点击事件
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

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName;
        private final TextView taskType;
        private final TextView taskStatus;
        private final TextView taskCreationTime;
        private final TextView taskDeadline;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.tvTaskName);
            taskType = itemView.findViewById(R.id.tvTaskType);
            taskStatus = itemView.findViewById(R.id.tvStatus);
            taskCreationTime = itemView.findViewById(R.id.tvCreationTime);
            taskDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }
}
