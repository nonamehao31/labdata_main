package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.AsphaltExperimentData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompletedExperimentAdapter extends RecyclerView.Adapter<CompletedExperimentAdapter.ViewHolder> {
    private List<TaskWithData> tasks = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static class TaskWithData {
        public final ExperimentTask task;
        public final List<? extends Object> data; // 使用通用类型来支持不同类型的实验数据

        public TaskWithData(ExperimentTask task, List<? extends Object> data) {
            this.task = task;
            this.data = data;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_completed_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskWithData taskWithData = tasks.get(position);
        ExperimentTask task = taskWithData.task;
        List<? extends Object> data = taskWithData.data;

        // 设置基本信息
        holder.tvTaskName.setText(task.getTaskName() != null ? task.getTaskName() : "未命名任务");
        holder.tvTaskId.setText(String.format("任务ID：%s", task.getTaskId() != null ? task.getTaskId() : "无任务ID"));
        holder.tvExperimenter.setText(String.format("实验人员：%s", task.getExperimenter() != null ? task.getExperimenter() : "未指定"));
        
        StringBuilder resultsBuilder = new StringBuilder();
        StringBuilder devicesBuilder = new StringBuilder();
        
        // 处理所有实验数据
        for (Object item : data) {
            if (item instanceof AsphaltExperimentData) {
                AsphaltExperimentData asphaltData = (AsphaltExperimentData) item;
                // 设置完成时间（使用最新的数据时间）
                holder.tvCompletionTime.setText(dateFormat.format(new Date(asphaltData.getCreateTime())));
                
                // 添加沥青实验结果
                resultsBuilder.append("实验类型：").append(asphaltData.getExperimentType()).append("\n");
                Map<String, String> values = asphaltData.getExperimentValues();
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    resultsBuilder.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
                }
                
                // 添加设备信息
                String deviceManufacturer = asphaltData.getDeviceManufacturer();
                String deviceModel = asphaltData.getDeviceModel();
                if (deviceManufacturer != null && deviceModel != null) {
                    devicesBuilder.append(String.format("%s %s", deviceManufacturer, deviceModel)).append("\n");
                }
            } else if (item instanceof ExperimentData) {
                ExperimentData experimentData = (ExperimentData) item;
                // 设置完成时间（使用最新的数据时间）
                holder.tvCompletionTime.setText(dateFormat.format(new Date(experimentData.getCreateTime())));
                
                // 添加实验结果
                resultsBuilder.append("实验名称：").append(experimentData.getExperimentName()).append("\n");
                if (experimentData.getResult() != null) {
                    resultsBuilder.append("结果：").append(experimentData.getResult()).append("\n");
                }
                
                // 添加设备信息
                if (experimentData.getDeviceManufacturer() != null) {
                    devicesBuilder.append(String.format("%s %s", 
                        experimentData.getDeviceManufacturer(), 
                        experimentData.getDeviceModel())).append("\n");
                }
            }
        }
        
        // 设置实验结果
        if (resultsBuilder.length() > 0) {
            holder.tvResults.setText(resultsBuilder.substring(0, resultsBuilder.length() - 1)); // 移除最后的换行符
        }
        
        // 设置设备信息
        if (devicesBuilder.length() > 0) {
            holder.tvDevices.setText(String.format("使用设备：\n%s", 
                devicesBuilder.substring(0, devicesBuilder.length() - 1))); // 移除最后的换行符
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<TaskWithData> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvTaskId;
        TextView tvExperimenter;
        TextView tvCompletionTime;
        TextView tvResults;
        TextView tvDevices;

        public ViewHolder(View view) {
            super(view);
            tvTaskName = view.findViewById(R.id.tvTaskName);
            tvTaskId = view.findViewById(R.id.tvTaskId);
            tvExperimenter = view.findViewById(R.id.tvExperimenter);
            tvCompletionTime = view.findViewById(R.id.tvCompletionTime);
            tvResults = view.findViewById(R.id.tvResults);
            tvDevices = view.findViewById(R.id.tvDevices);
        }
    }
}
