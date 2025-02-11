package com.example.labdata_main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletedExperimentAdapter extends RecyclerView.Adapter<CompletedExperimentAdapter.ViewHolder> {
    private static final String TAG = "CompletedAdapter";
    private List<TaskWithData> tasks = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static class TaskWithData {
        public final ExperimentTask task;
        public final List<ExperimentData> dataList;

        public TaskWithData(ExperimentTask task, List<ExperimentData> dataList) {
            this.task = task;
            this.dataList = dataList;
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
        List<ExperimentData> dataList = taskWithData.dataList;

        // 设置任务基本信息
        holder.tvTaskName.setText(task.getTaskName());
        holder.tvTaskId.setText(task.getTaskId());
        
        // 设置实验人员
        String experimenter = task.getExperimenter();
        Log.d(TAG, "Setting experimenter: " + experimenter);
        holder.tvExperimenter.setText(experimenter != null ? experimenter : "未知");

        // 设置完成时间
        long completionTime = task.getExperimentCompletionTime();
        if (completionTime > 0) {
            holder.tvCompletionTime.setText(dateFormat.format(new Date(completionTime)));
        } else {
            holder.tvCompletionTime.setText("未记录");
        }

        // 构建实验结果和设备信息
        StringBuilder results = new StringBuilder();
        StringBuilder devices = new StringBuilder();
        
        for (ExperimentData data : dataList) {
            // 添加实验类型和结果
            if (data.getExperimentName() != null) {
                results.append(data.getExperimentName());
                String result = data.getResult();
                if (result != null && !result.isEmpty()) {
                    results.append(": ").append(result);
                }
                results.append("\n");
            }

            // 添加设备信息
            String deviceInfo = data.getDeviceManufacturer() + " " + data.getDeviceModel();
            if (!deviceInfo.trim().isEmpty() && !devices.toString().contains(deviceInfo)) {
                if (devices.length() > 0) {
                    devices.append("\n");
                }
                devices.append(deviceInfo);
            }
        }

        // 设置实验结果
        if (results.length() > 0) {
            holder.tvResults.setText(results.substring(0, results.length() - 1)); // 移除最后的换行符
        } else {
            holder.tvResults.setText("无实验结果");
        }

        // 设置设备信息
        if (devices.length() > 0) {
            holder.tvDevices.setText(devices.toString());
        } else {
            holder.tvDevices.setText("未使用设备");
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskId = itemView.findViewById(R.id.tvTaskId);
            tvExperimenter = itemView.findViewById(R.id.tvExperimenter);
            tvCompletionTime = itemView.findViewById(R.id.tvCompletionTime);
            tvResults = itemView.findViewById(R.id.tvResults);
            tvDevices = itemView.findViewById(R.id.tvDevices);
        }
    }
}
