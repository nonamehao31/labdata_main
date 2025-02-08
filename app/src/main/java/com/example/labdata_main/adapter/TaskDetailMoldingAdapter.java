package com.example.labdata_main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskDetailMoldingAdapter extends RecyclerView.Adapter<TaskDetailMoldingAdapter.ViewHolder> {
    private static final String TAG = "TaskDetailMoldingAdapter";
    private final List<MoldingMethod> moldingMethods;
    private final List<MixRatio> mixRatios;

    public TaskDetailMoldingAdapter(String moldingMethodStr, List<MixRatio> mixRatios) {
        this.mixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
        this.moldingMethods = new ArrayList<>();
        
        if (moldingMethodStr != null && !moldingMethodStr.isEmpty()) {
            Log.d(TAG, "Parsing molding method: " + moldingMethodStr);
            
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<MoldingMethod>>(){}.getType();
                List<MoldingMethod> methods = gson.fromJson(moldingMethodStr, type);
                if (methods != null) {
                    Log.d(TAG, "Successfully parsed method array, size: " + methods.size());
                    for (MoldingMethod method : methods) {
                        if (isValidMethod(method)) {
                            this.moldingMethods.add(method);
                        }
                    }
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Failed to parse molding method JSON: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while parsing molding method: " + e.getMessage());
            }
        }
        
        Log.d(TAG, "Final molding methods size: " + this.moldingMethods.size());
    }

    private boolean isValidMethod(MoldingMethod method) {
        if (method == null) return false;
        
        // 检查必要的字段是否有值
        boolean hasTemperature = method.getMixingTemperature() > 0;
        boolean hasSpeed = method.getMixingSpeed() > 0;
        boolean hasTime = method.getMixingTime() > 0;
        boolean hasCompactionMethod = method.getCompactionMethod() != null && 
                                    !method.getCompactionMethod().isEmpty();
        
        return hasTemperature || hasSpeed || hasTime || hasCompactionMethod;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_detail_molding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoldingMethod method = moldingMethods.get(position);
        MixRatio mixRatio = position < mixRatios.size() ? mixRatios.get(position) : null;
        holder.bind(method, mixRatio, position + 1);
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMixRatioName;
        private final TextView tvMixingParams;
        private final TextView tvCompactionMethod;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
        }

        public void bind(MoldingMethod method, MixRatio mixRatio, int index) {
            // 设置配比名称
            if (mixRatio != null) {
                tvMixRatioName.setText(String.format("配比%d：%s", index, mixRatio.getName()));
                tvMixRatioName.setVisibility(View.VISIBLE);
            } else {
                tvMixRatioName.setVisibility(View.GONE);
            }

            // 设置拌合参数
            if (method != null) {
                String mixingParams = String.format("拌合参数：温度 %.0f℃，速度 %.0f rpm，时间 %.0f min",
                    method.getMixingTemperature(),
                    method.getMixingSpeed(),
                    method.getMixingTime()
                );
                tvMixingParams.setText(mixingParams);
                
                // 设置压实方法
                String compactionMethod = String.format("压实方法：%s", method.getCompactionMethod());
                tvCompactionMethod.setText(compactionMethod);
            } else {
                tvMixingParams.setText("拌合参数：--");
                tvCompactionMethod.setText("压实方法：--");
            }
        }
    }
}
