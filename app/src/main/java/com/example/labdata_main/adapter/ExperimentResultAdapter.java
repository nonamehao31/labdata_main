package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExperimentResultAdapter extends RecyclerView.Adapter<ExperimentResultAdapter.ViewHolder> {
    private List<ExperimentData> experiments;
    private final SimpleDateFormat dateFormat;

    public ExperimentResultAdapter() {
        this.experiments = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    public void setExperiments(List<ExperimentData> experiments) {
        this.experiments = experiments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExperimentData experiment = experiments.get(position);
        
        holder.tvExperimentName.setText(experiment.getExperimentName());
        
        // 显示实验结果
        String result = experiment.getResult();
        if (result != null && !result.isEmpty()) {
            holder.tvResult.setText(result);
        } else {
            // 如果没有合并的结果，使用原始的输入值
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append(experiment.getInput1Label())
                    .append(": ")
                    .append(experiment.getInput1Value());
            
            if (experiment.getInput2Label() != null && !experiment.getInput2Label().isEmpty()) {
                resultBuilder.append("\n")
                        .append(experiment.getInput2Label())
                        .append(": ")
                        .append(experiment.getInput2Value());
            }
            holder.tvResult.setText(resultBuilder.toString());
        }
        
        // 显示实验时间
        String date = dateFormat.format(new Date(experiment.getCreateTime()));
        holder.tvTime.setText(date);
        
        // 显示设备信息
        String deviceInfo = String.format("%s %s (%s)",
                experiment.getDeviceManufacturer(),
                experiment.getDeviceModel(),
                experiment.getDevicePurchaseYear());
        holder.tvDevice.setText(deviceInfo);
    }

    @Override
    public int getItemCount() {
        return experiments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName;
        TextView tvResult;
        TextView tvTime;
        TextView tvDevice;

        ViewHolder(View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvResult = itemView.findViewById(R.id.tvResult);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDevice = itemView.findViewById(R.id.tvDevice);
        }
    }
}
