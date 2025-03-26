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
import com.example.labdata_main.model.PenetrationTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 针入度实验结果适配器
 */
public class PenetrationTestAdapter extends RecyclerView.Adapter<PenetrationTestAdapter.ViewHolder> {
    
    private static final String TAG = "PenetrationTestAdapter";
    private final List<PenetrationTestResponse> testList;
    private final Context context;
    
    public PenetrationTestAdapter(Context context) {
        this.context = context;
        this.testList = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_penetration_test_result, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PenetrationTestResponse test = testList.get(position);
        
        // 设置试验温度
        String temperature = test.getTemperature();
        if (!TextUtils.isEmpty(temperature)) {
            holder.tvTemperature.setText(temperature + "℃");
        } else {
            holder.tvTemperature.setText("未记录");
        }
        
        // 设置针入深度读数
        String reading = test.getReading();
        if (!TextUtils.isEmpty(reading)) {
            holder.tvReading.setText(reading + "mm");
        } else {
            holder.tvReading.setText("未记录");
        }
        
        // 设置设备信息
        String deviceName = test.getDeviceName();
        holder.tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "未记录" : deviceName);
        
        String manufacturer = test.getDeviceManufacturer();
        holder.tvManufacturer.setText(TextUtils.isEmpty(manufacturer) ? "未记录" : manufacturer);
        
        String model = test.getDeviceModel();
        holder.tvModel.setText(TextUtils.isEmpty(model) ? "未记录" : model);
        
        Log.d(TAG, "绑定针入度试验数据: 位置=" + position + ", 温度=" + temperature + ", 读数=" + reading);
    }
    
    @Override
    public int getItemCount() {
        return testList.size();
    }
    
    /**
     * 更新试验数据列表
     * @param tests 新的试验数据列表
     */
    public void updateData(List<PenetrationTestResponse> tests) {
        if (tests == null) {
            return;
        }
        
        testList.clear();
        testList.addAll(tests);
        notifyDataSetChanged();
        
        Log.d(TAG, "更新针入度试验数据列表，共" + tests.size() + "条");
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature;
        TextView tvReading;
        TextView tvDeviceName;
        TextView tvManufacturer;
        TextView tvModel;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvReading = itemView.findViewById(R.id.tvReading);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvManufacturer = itemView.findViewById(R.id.tvManufacturer);
            tvModel = itemView.findViewById(R.id.tvModel);
        }
    }
}
