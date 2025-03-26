package com.example.labdata_main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.BrookfieldViscosityTemperaturePointResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 旋转黏度实验温度点适配器
 */
public class ViscosityTemperaturePointAdapter extends RecyclerView.Adapter<ViscosityTemperaturePointAdapter.ViewHolder> {
    
    private final List<BrookfieldViscosityTemperaturePointResponse> temperaturePoints;
    private final Context context;
    
    public ViscosityTemperaturePointAdapter(Context context) {
        this.context = context;
        this.temperaturePoints = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_viscosity_temperature_point, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrookfieldViscosityTemperaturePointResponse temperaturePoint = temperaturePoints.get(position);
        
        // 设置温度值
        String temperature = temperaturePoint.getTemperature();
        holder.tvTemperature.setText("温度点: " + (TextUtils.isEmpty(temperature) ? "--" : temperature) + "℃");
        
        // 设置测量值适配器
        ViscosityMeasurementAdapter measurementAdapter = new ViscosityMeasurementAdapter(context);
        holder.rvMeasurements.setLayoutManager(new LinearLayoutManager(context));
        holder.rvMeasurements.setAdapter(measurementAdapter);
        
        // 更新测量值数据
        if (temperaturePoint.getMeasurements() != null && !temperaturePoint.getMeasurements().isEmpty()) {
            measurementAdapter.updateData(temperaturePoint.getMeasurements());
        }
    }
    
    @Override
    public int getItemCount() {
        return temperaturePoints.size();
    }
    
    /**
     * 更新温度点数据列表
     * @param points 新的温度点数据列表
     */
    public void updateData(List<BrookfieldViscosityTemperaturePointResponse> points) {
        if (points == null) {
            return;
        }
        
        temperaturePoints.clear();
        temperaturePoints.addAll(points);
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature;
        RecyclerView rvMeasurements;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            rvMeasurements = itemView.findViewById(R.id.rvMeasurements);
        }
    }
}
