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
import com.example.labdata_main.model.BrookfieldViscosityResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验适配器
 */
public class BrookfieldViscosityAdapter extends RecyclerView.Adapter<BrookfieldViscosityAdapter.ViewHolder> {
    
    private final List<BrookfieldViscosityResponse> viscosityTests;
    private final Context context;
    
    public BrookfieldViscosityAdapter(Context context) {
        this.context = context;
        this.viscosityTests = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brookfield_viscosity_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrookfieldViscosityResponse viscosityTest = viscosityTests.get(position);
        
        // 设置设备信息
        String deviceName = viscosityTest.getDeviceName();
        holder.tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "未记录" : deviceName);
        
        String manufacturer = viscosityTest.getDeviceManufacturer();
        holder.tvManufacturer.setText(TextUtils.isEmpty(manufacturer) ? "未记录" : manufacturer);
        
        String model = viscosityTest.getDeviceModel();
        holder.tvModel.setText(TextUtils.isEmpty(model) ? "未记录" : model);
        
        // 设置温度点适配器
        ViscosityTemperaturePointAdapter temperaturePointAdapter = new ViscosityTemperaturePointAdapter(context);
        holder.rvTemperaturePoints.setLayoutManager(new LinearLayoutManager(context));
        holder.rvTemperaturePoints.setAdapter(temperaturePointAdapter);
        
        // 更新温度点数据
        if (viscosityTest.getTemperaturePoints() != null && !viscosityTest.getTemperaturePoints().isEmpty()) {
            temperaturePointAdapter.updateData(viscosityTest.getTemperaturePoints());
        }
    }
    
    @Override
    public int getItemCount() {
        return viscosityTests.size();
    }
    
    /**
     * 更新旋转黏度实验数据列表
     * @param tests 新的旋转黏度实验数据列表
     */
    public void updateData(List<BrookfieldViscosityResponse> tests) {
        if (tests == null) {
            return;
        }
        
        viscosityTests.clear();
        viscosityTests.addAll(tests);
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        TextView tvManufacturer;
        TextView tvModel;
        RecyclerView rvTemperaturePoints;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvManufacturer = itemView.findViewById(R.id.tvManufacturer);
            tvModel = itemView.findViewById(R.id.tvModel);
            rvTemperaturePoints = itemView.findViewById(R.id.rvTemperaturePoints);
        }
    }
}
