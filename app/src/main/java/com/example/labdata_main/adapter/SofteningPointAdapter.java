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
import com.example.labdata_main.model.SofteningPointResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 软化点实验（环球法）结果适配器
 */
public class SofteningPointAdapter extends RecyclerView.Adapter<SofteningPointAdapter.ViewHolder> {
    
    private static final String TAG = "SofteningPointAdapter";
    private final List<SofteningPointResponse> testList;
    private final Context context;
    
    public SofteningPointAdapter(Context context) {
        this.context = context;
        this.testList = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_softening_point_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SofteningPointResponse test = testList.get(position);
        
        // 设置初始温度
        String temperature = test.getTemperature();
        if (!TextUtils.isEmpty(temperature)) {
            holder.tvTemperature.setText(temperature + "℃");
        } else {
            holder.tvTemperature.setText("未记录");
        }
        
        // 设置软化温度
        String softeningTemperature = test.getSofteningTemperature();
        if (!TextUtils.isEmpty(softeningTemperature)) {
            holder.tvSofteningTemperature.setText(softeningTemperature + "℃");
        } else {
            holder.tvSofteningTemperature.setText("未记录");
        }
        
        // 设置设备信息
        String deviceName = test.getDeviceName();
        holder.tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "未记录" : deviceName);
        
        String manufacturer = test.getDeviceManufacturer();
        holder.tvManufacturer.setText(TextUtils.isEmpty(manufacturer) ? "未记录" : manufacturer);
        
        String model = test.getDeviceModel();
        holder.tvModel.setText(TextUtils.isEmpty(model) ? "未记录" : model);
        
        Log.d(TAG, "绑定软化点试验数据: 位置=" + position + ", 初始温度=" + temperature + ", 软化温度=" + softeningTemperature);
    }
    
    @Override
    public int getItemCount() {
        return testList.size();
    }
    
    /**
     * 更新试验数据列表
     * @param tests 新的试验数据列表
     */
    public void updateData(List<SofteningPointResponse> tests) {
        if (tests == null) {
            return;
        }
        
        testList.clear();
        testList.addAll(tests);
        notifyDataSetChanged();
        
        Log.d(TAG, "更新软化点试验数据列表，共" + tests.size() + "条");
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature;
        TextView tvSofteningTemperature;
        TextView tvDeviceName;
        TextView tvManufacturer;
        TextView tvModel;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvSofteningTemperature = itemView.findViewById(R.id.tvSofteningTemperature);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvManufacturer = itemView.findViewById(R.id.tvManufacturer);
            tvModel = itemView.findViewById(R.id.tvModel);
        }
    }
}
