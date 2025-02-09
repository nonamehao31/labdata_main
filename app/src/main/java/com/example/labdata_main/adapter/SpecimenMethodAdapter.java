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
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpecimenMethodAdapter extends RecyclerView.Adapter<SpecimenMethodAdapter.ViewHolder> {
    private static final String TAG = "SpecimenMethodAdapter";
    private List<MoldingMethod> moldingMethods;
    private List<MixRatio> mixRatios;

    public SpecimenMethodAdapter(String moldingMethodJson, List<MixRatio> mixRatios) {
        Log.d(TAG, "Creating adapter with JSON: " + moldingMethodJson);
        Log.d(TAG, "Mix ratios size: " + (mixRatios != null ? mixRatios.size() : 0));
        
        this.mixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
        this.moldingMethods = new ArrayList<>();
        
        try {
            if (moldingMethodJson != null && !moldingMethodJson.isEmpty()) {
                Gson gson = new Gson();
                if (moldingMethodJson.trim().startsWith("[")) {
                    this.moldingMethods = gson.fromJson(moldingMethodJson, 
                        new TypeToken<List<MoldingMethod>>(){}.getType());
                    Log.d(TAG, "Parsed JSON array, size: " + this.moldingMethods.size());
                } else if (moldingMethodJson.trim().startsWith("{")) {
                    MoldingMethod method = gson.fromJson(moldingMethodJson, MoldingMethod.class);
                    if (method != null) {
                        this.moldingMethods.add(method);
                        Log.d(TAG, "Added single method from JSON object");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        Log.d(TAG, "Final molding methods size: " + this.moldingMethods.size());
        Log.d(TAG, "Final mix ratios size: " + this.mixRatios.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding ViewHolder at position: " + position);
        
        if (position < moldingMethods.size() && position < mixRatios.size()) {
            MoldingMethod method = moldingMethods.get(position);
            MixRatio mixRatio = mixRatios.get(position);

            Log.d(TAG, String.format("Binding data - MixRatio: %s, Method: temp=%.1f, speed=%.1f, time=%.1f, compaction=%s",
                mixRatio.getName(),
                method.getMixingTemperature(),
                method.getMixingSpeed(),
                method.getMixingTime(),
                method.getCompactionMethod()));

            // 设置配比名称
            holder.tvMixRatioName.setText(mixRatio.getName());

            // 设置拌合参数
            String mixingParams = String.format(Locale.getDefault(),
                "拌合参数：温度 %.1f℃，速度 %.1f rpm，时间 %.1f min",
                method.getMixingTemperature(),
                method.getMixingSpeed(),
                method.getMixingTime());
            holder.tvMixingParams.setText(mixingParams);

            // 设置压实方法
            String compactionMethod = "压实方法：" + method.getCompactionMethod();
            holder.tvCompactionMethod.setText(compactionMethod);
            
            Log.d(TAG, "ViewHolder bound successfully");
        } else {
            Log.w(TAG, String.format("Position %d out of bounds - methods size: %d, ratios size: %d",
                position, moldingMethods.size(), mixRatios.size()));
        }
    }

    @Override
    public int getItemCount() {
        int count = Math.min(moldingMethods.size(), mixRatios.size());
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixRatioName;
        TextView tvMixingParams;
        TextView tvCompactionMethod;

        ViewHolder(View itemView) {
            super(itemView);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            
            // 验证所有视图都找到了
            if (tvMixRatioName == null || tvMixingParams == null || tvCompactionMethod == null) {
                Log.e(TAG, "Failed to find one or more views in ViewHolder");
            }
        }
    }
}
