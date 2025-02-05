package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;

import java.util.ArrayList;
import java.util.List;

public class ExperimentMoldingAdapter extends RecyclerView.Adapter<ExperimentMoldingAdapter.ViewHolder> {
    private final List<MixRatio> mixRatios = new ArrayList<>();
    private final String moldingMethod;

    public ExperimentMoldingAdapter(ExperimentTask task) {
        this.mixRatios.addAll(task.getSelectedMixRatios());
        this.moldingMethod = task.getMoldingMethod();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experment_molding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MixRatio mixRatio = mixRatios.get(position);
        holder.bind(mixRatio, moldingMethod, position + 1);
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMixRatioName;
        private final TextView tvMixingTemp;
        private final TextView tvMixingSpeed;
        private final TextView tvMixingTime;
        private final TextView tvCompactionMethod;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingTemp = itemView.findViewById(R.id.tvMixingTemp);
            tvMixingSpeed = itemView.findViewById(R.id.tvMixingSpeed);
            tvMixingTime = itemView.findViewById(R.id.tvMixingTime);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
        }

        public void bind(MixRatio mixRatio, String moldingMethod, int position) {
            tvMixRatioName.setText("配比" + position);

            // 解析制件方法字符串
            if (moldingMethod != null && !moldingMethod.isEmpty()) {
                String[] parts = moldingMethod.split("\\|");
                float temp = 0, speed = 0, time = 0;
                String method = "";
                
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String value = keyValue[1].trim();
                        switch (key) {
                            case "temp":
                                temp = Float.parseFloat(value);
                                break;
                            case "speed":
                                speed = Float.parseFloat(value);
                                break;
                            case "time":
                                time = Float.parseFloat(value);
                                break;
                            case "method":
                                method = value;
                                break;
                        }
                    }
                }

                // 设置拌合参数
                tvMixingTemp.setText(String.format("拌合温度：%.1f℃", temp));
                tvMixingSpeed.setText(String.format("拌合速度：%.1f rpm", speed));
                tvMixingTime.setText(String.format("拌合时间：%.1f min", time));
                tvCompactionMethod.setText(method);
            }
        }
    }
}
