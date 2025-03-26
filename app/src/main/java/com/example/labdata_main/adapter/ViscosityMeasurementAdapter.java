package com.example.labdata_main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.BrookfieldViscosityMeasurementResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 旋转黏度实验测量值适配器
 */
public class ViscosityMeasurementAdapter extends RecyclerView.Adapter<ViscosityMeasurementAdapter.ViewHolder> {
    
    private final List<BrookfieldViscosityMeasurementResponse> measurementList;
    private final Context context;
    
    public ViscosityMeasurementAdapter(Context context) {
        this.context = context;
        this.measurementList = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_viscosity_measurement, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrookfieldViscosityMeasurementResponse measurement = measurementList.get(position);
        
        // 设置转子类型
        String spindleType = measurement.getSpindleType();
        holder.tvSpindleType.setText(TextUtils.isEmpty(spindleType) ? "未记录" : spindleType);
        
        // 设置转速
        String rotationSpeed = measurement.getRotationSpeed();
        holder.tvRotationSpeed.setText(TextUtils.isEmpty(rotationSpeed) ? "未记录" : rotationSpeed);
        
        // 设置黏度值
        String viscosity = measurement.getViscosity();
        holder.tvViscosity.setText(TextUtils.isEmpty(viscosity) ? "未记录" : viscosity);
    }
    
    @Override
    public int getItemCount() {
        return measurementList.size();
    }
    
    /**
     * 更新测量值数据列表
     * @param measurements 新的测量值数据列表
     */
    public void updateData(List<BrookfieldViscosityMeasurementResponse> measurements) {
        if (measurements == null) {
            return;
        }
        
        measurementList.clear();
        measurementList.addAll(measurements);
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpindleType;
        TextView tvRotationSpeed;
        TextView tvViscosity;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpindleType = itemView.findViewById(R.id.tvSpindleType);
            tvRotationSpeed = itemView.findViewById(R.id.tvRotationSpeed);
            tvViscosity = itemView.findViewById(R.id.tvViscosity);
        }
    }
}
