package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class AsphaltProjectCardAdapter extends RecyclerView.Adapter<AsphaltProjectCardAdapter.ViewHolder> {
    private List<ExperimentTask> tasks = new ArrayList<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private OnAsphaltTaskActionListener listener;

    public interface OnAsphaltTaskActionListener {
        void onViewAsphaltInfo(ExperimentTask task);
        void onRecordData(ExperimentTask task);
    }

    public void setOnAsphaltTaskActionListener(OnAsphaltTaskActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<ExperimentTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asphalt_project_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExperimentTask task = tasks.get(position);
        
        // 设置任务名称
        holder.tvTaskName.setText(task.getTaskName());
        
        // 设置截止日期
        if (task.getDeadline() > 0) {
            holder.tvDeadline.setText("截止日期：" + DATE_FORMAT.format(new Date(task.getDeadline())));
        } else {
            holder.tvDeadline.setText("截止日期：无");
        }
        
        // 设置任务状态
        holder.tvStatus.setText(task.getStatus());
        
        // 解析并设置步骤状态
        updateStepStatus(holder, task);

        // 设置按钮点击事件
        holder.btnViewAsphaltInfo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewAsphaltInfo(task);
            }
        });

        holder.btnRecordData.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordData(task);
            }
        });
    }

    private void updateStepStatus(ViewHolder holder, ExperimentTask task) {
        // 解析任务状态并更新步骤显示
        String notes = task.getNotes();
        boolean hasAsphaltInfo = false;
        boolean hasExperimentData = false;

        if (notes != null) {
            String[] sections = notes.split("\n\n");
            for (String section : sections) {
                if (section.startsWith("沥青信息:")) {
                    hasAsphaltInfo = true;
                } else if (section.startsWith("实验数据:")) {
                    hasExperimentData = true;
                }
            }
        }

        // 更新步骤1状态
        if (hasAsphaltInfo) {
            holder.tvStep1Status.setText("已完成");
            holder.tvStep1Status.setTextColor(holder.itemView.getContext().getColor(R.color.step_completed));
            holder.step1Circle.setBackgroundResource(R.drawable.step_circle_completed);
        } else {
            holder.tvStep1Status.setText("待确认");
            holder.tvStep1Status.setTextColor(holder.itemView.getContext().getColor(R.color.gray_dark));
            holder.step1Circle.setBackgroundResource(R.drawable.step_circle_background);
        }

        // 更新步骤2状态
        if (hasExperimentData) {
            holder.tvStep2Status.setText("已完成");
            holder.tvStep2Status.setTextColor(holder.itemView.getContext().getColor(R.color.step_completed));
            holder.step2Circle.setBackgroundResource(R.drawable.step_circle_completed);
        } else {
            holder.tvStep2Status.setText("待完成");
            holder.tvStep2Status.setTextColor(holder.itemView.getContext().getColor(R.color.gray_dark));
            holder.step2Circle.setBackgroundResource(R.drawable.step_circle_background);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTaskName;
        final TextView tvDeadline;
        final TextView tvStatus;
        final TextView tvStep1Status;
        final TextView tvStep2Status;
        final View step1Circle;
        final View step2Circle;
        final Button btnViewAsphaltInfo;
        final Button btnRecordData;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStep1Status = itemView.findViewById(R.id.tvStep1Status);
            tvStep2Status = itemView.findViewById(R.id.tvStep2Status);
            step1Circle = itemView.findViewById(R.id.step1Circle);
            step2Circle = itemView.findViewById(R.id.step2Circle);
            btnViewAsphaltInfo = itemView.findViewById(R.id.btnViewAsphaltInfo);
            btnRecordData = itemView.findViewById(R.id.btnRecordData);
        }
    }
}
