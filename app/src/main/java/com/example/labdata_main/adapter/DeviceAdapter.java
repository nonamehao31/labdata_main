package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.Device;

import java.util.List;

/**
 * 设备列表适配器
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private Context context;
    private List<Device> deviceList;
    private OnItemClickListener listener;

    public DeviceAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        
        // 设置类型指示条颜色
        holder.deviceTypeIndicator.setBackgroundColor(device.getIndicatorColor());
        
        // 设置设备类型
        String deviceTypeText = "";
        int deviceIconRes = R.drawable.ic_device_other; // 默认图标
        
        String type = device.getType().toLowerCase();
        if (type.contains("拌合") || type.contains("mixing") || 
            type.contains("搅拌") || type.contains("mixer")) {
            deviceTypeText = "拌合设备";
            deviceIconRes = R.drawable.ic_device_mixing;
        } else if (type.contains("压实") || type.contains("compaction") || 
                   type.contains("压路") || type.contains("压实机") || 
                   type.contains("forming") || type.equals("FORMING")) {
            deviceTypeText = "压实设备";
            deviceIconRes = R.drawable.ic_device_forming;
        } else if (type.contains("实验") || type.contains("test") || 
                   type.contains("检测") || type.contains("apparatus")) {
            deviceTypeText = "实验设备";
            deviceIconRes = R.drawable.ic_device_test;
        } else {
            deviceTypeText = "其他设备";
            deviceIconRes = R.drawable.ic_device_other;
        }
        
        // 设置设备图标
        try {
            holder.ivDeviceIcon.setImageResource(deviceIconRes);
        } catch (Exception e) {
            // 如果图标不存在，使用默认图标
            holder.ivDeviceIcon.setImageResource(android.R.drawable.ic_menu_manage);
        }
        
        holder.tvDeviceType.setText(deviceTypeText);
        
        // 设置设备详情
        String manufacturer = device.getManufacturer();
        String model = device.getModel();
        String deviceInfo = manufacturer + " " + model;
        holder.tvDeviceInfo.setText(deviceInfo);
        
        // 设置购买年份
        holder.tvPurchaseYear.setText(device.getPurchaseYear());
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList != null ? deviceList.size() : 0;
    }

    /**
     * 设置列表项点击监听器
     * @param listener 监听器实例
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 列表项点击事件监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(Device device);
    }

    /**
     * 设备列表项ViewHolder
     */
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        View deviceTypeIndicator;
        ImageView ivDeviceIcon;
        TextView tvDeviceType;
        TextView tvDeviceInfo;
        TextView tvPurchaseYear;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceTypeIndicator = itemView.findViewById(R.id.deviceTypeIndicator);
            ivDeviceIcon = itemView.findViewById(R.id.ivDeviceIcon);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvDeviceInfo = itemView.findViewById(R.id.tvDeviceInfo);
            tvPurchaseYear = itemView.findViewById(R.id.tvPurchaseYear);
        }
    }
}
