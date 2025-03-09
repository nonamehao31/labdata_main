package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 沥青实验任务适配器，用于在列表中显示沥青实验任务
 */
public class AsphaltExperimentTaskAdapter extends RecyclerView.Adapter<AsphaltExperimentTaskAdapter.ViewHolder> {

    private final List<ExperimentTask> tasks;
    private final Context context;
    private final OnTaskClickListener listener;

    public AsphaltExperimentTaskAdapter(Context context, List<ExperimentTask> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExperimentTask task = tasks.get(position);
        
        holder.tvTaskName.setText(task.getTaskName());
        holder.tvStatus.setText(task.getStatus());
        
        // 设置创建时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String creationTime = dateFormat.format(new Date(task.getCreationTime()));
        holder.tvCreationTime.setText(String.format("创建时间: %s", creationTime));
        
        // 设置截止时间
        if (task.getDeadline() > 0) {
            String deadline = dateFormat.format(new Date(task.getDeadline()));
            holder.tvDeadline.setText(String.format("截止时间: %s", deadline));
            holder.tvDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }
        
        // 根据任务状态设置状态视图的背景
        if ("完成".equals(task.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background_completed);
        } else if ("进行中".equals(task.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background_in_progress);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background_pending);
        }
        
        // 设置任务类型标志
        holder.tvTaskType.setText("沥青实验");
        holder.tvTaskType.setVisibility(View.VISIBLE);
        
        // 设置点击事件
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClicked(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<ExperimentTask> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    /**
     * 任务点击监听器
     */
    public interface OnTaskClickListener {
        void onTaskClicked(ExperimentTask task);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView tvTaskName;
        final TextView tvStatus;
        final TextView tvCreationTime;
        final TextView tvDeadline;
        final TextView tvTaskType;

        ViewHolder(View view) {
            super(view);
            cardView = (MaterialCardView) view;
            tvTaskName = view.findViewById(R.id.tvTaskName);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvCreationTime = view.findViewById(R.id.tvCreationTime);
            tvDeadline = view.findViewById(R.id.tvDeadline);
            tvTaskType = view.findViewById(R.id.tvTaskType);
        }
    }
}
