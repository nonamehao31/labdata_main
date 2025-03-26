package com.example.labdata_main.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DuctilityTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 延度实验结果适配器
 */
public class DuctilityAdapter extends RecyclerView.Adapter<DuctilityAdapter.ViewHolder> {
    
    private static final String TAG = "DuctilityAdapter";
    private final List<DuctilityTestResponse> testList;
    private final Context context;
    
    public DuctilityAdapter(Context context) {
        this.context = context;
        this.testList = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ductility_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DuctilityTestResponse test = testList.get(position);
        
        // 设置温度
        String temperature = test.getTemperature();
        if (!TextUtils.isEmpty(temperature)) {
            holder.tvTemperature.setText(temperature + "℃");
        } else {
            holder.tvTemperature.setText("未记录");
        }
        
        // 设置拉长位移
        String displacement = test.getDisplacement();
        if (!TextUtils.isEmpty(displacement)) {
            holder.tvDisplacement.setText(displacement + "mm");
        } else {
            holder.tvDisplacement.setText("未记录");
        }
        
        // 设置设备信息
        String deviceName = test.getDeviceName();
        holder.tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "未记录" : deviceName);
        
        String manufacturer = test.getDeviceManufacturer();
        holder.tvManufacturer.setText(TextUtils.isEmpty(manufacturer) ? "未记录" : manufacturer);
        
        String model = test.getDeviceModel();
        holder.tvModel.setText(TextUtils.isEmpty(model) ? "未记录" : model);
        
        Log.d(TAG, "绑定延度实验数据: 位置=" + position + ", 温度=" + temperature + ", 拉长位移=" + displacement);
    }
    
    @Override
    public int getItemCount() {
        return testList.size();
    }
    
    /**
     * 更新实验数据列表
     * @param tests 新的实验数据列表
     */
    public void updateData(List<DuctilityTestResponse> tests) {
        if (tests == null) {
            return;
        }
        
        testList.clear();
        testList.addAll(tests);
        notifyDataSetChanged();
        
        Log.d(TAG, "更新延度实验数据列表，共" + tests.size() + "条");
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature;
        TextView tvDisplacement;
        TextView tvDeviceName;
        TextView tvManufacturer;
        TextView tvModel;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvDisplacement = itemView.findViewById(R.id.tvDisplacement);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvManufacturer = itemView.findViewById(R.id.tvManufacturer);
            tvModel = itemView.findViewById(R.id.tvModel);
        }
    }
}
