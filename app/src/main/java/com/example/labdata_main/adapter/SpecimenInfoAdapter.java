package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import java.util.ArrayList;
import java.util.List;

public class SpecimenInfoAdapter extends RecyclerView.Adapter<SpecimenInfoAdapter.ViewHolder> {
    private final List<SpecimenInfo> specimenInfos = new ArrayList<>();
    private List<String> selectedMixingDevices;
    private List<String> selectedFormingDevices;
    private String taskId;
    private OnFinishSpecimenClickListener onFinishSpecimenClickListener;
    
    // 添加日志标签
    private static final String TAG = "SpecimenInfoAdapter";

    // 定义完成试件制备的点击监听器接口
    public interface OnFinishSpecimenClickListener {
        void onFinishSpecimenClicked(String taskId);
    }
    
    // 设置点击监听器
    public void setOnFinishSpecimenClickListener(OnFinishSpecimenClickListener listener) {
        this.onFinishSpecimenClickListener = listener;
    }
    
    // 设置任务ID
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public static class SpecimenInfo {
        public MoldingMethod moldingMethod;
        public MixRatio mixRatio;
        public DeviceInfo mixingDevice;
        public DeviceInfo formingDevice;

        public SpecimenInfo(MoldingMethod moldingMethod, MixRatio mixRatio,
                          DeviceInfo mixingDevice, DeviceInfo formingDevice) {
            this.moldingMethod = moldingMethod;
            this.mixRatio = mixRatio;
            this.mixingDevice = mixingDevice;
            this.formingDevice = formingDevice;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixRatioInfo;
        TextView tvMixingParams;
        TextView tvCompactionMethod;
        TextView tvMixingDevice;
        TextView tvFormingDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioInfo = itemView.findViewById(R.id.tvMixRatioInfo);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            tvMixingDevice = itemView.findViewById(R.id.tvMixingDevice);
            tvFormingDevice = itemView.findViewById(R.id.tvFormingDevice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen_info_step2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpecimenInfo info = specimenInfos.get(position);

        if (info.mixRatio != null) {
            holder.tvMixRatioInfo.setText(String.format("配比：%s", info.mixRatio.getName()));
        }

        if (info.moldingMethod != null) {
            // 检查拌合参数是否有效
            float temp = info.moldingMethod.getMixingTemperature();
            float speed = info.moldingMethod.getMixingSpeed();
            float time = info.moldingMethod.getMixingTime();
            
            // 日志记录当前参数值
            android.util.Log.d(TAG, "拌合参数: 温度=" + temp + "℃, 速度=" + speed + " rpm, 时间=" + time + " min");
            
            if (temp > 0 || speed > 0 || time > 0) {
                holder.tvMixingParams.setText(String.format("拌合参数：温度=%.1f℃, 速度=%.1f rpm, 时间=%.1f min",
                        temp, speed, time));
            } else {
                holder.tvMixingParams.setText("拌合参数：无数据");
            }
            
            // 检查压实方法是否有效
            String compactionMethod = info.moldingMethod.getCompactionMethod();
            android.util.Log.d(TAG, "压实方法: " + compactionMethod);
            
            if (compactionMethod != null && !compactionMethod.isEmpty() && !compactionMethod.equals("????")) {
                holder.tvCompactionMethod.setText(String.format("压实方法：%s", compactionMethod));
            } else {
                holder.tvCompactionMethod.setText("压实方法：标准压实");
            }
        }

        if (info.mixingDevice != null) {
            String manufacturer = info.mixingDevice.getManufacturer();
            String model = info.mixingDevice.getModel();
            
            // 日志记录设备信息
            android.util.Log.d(TAG, "拌合设备: 制造商=" + manufacturer + ", 型号=" + model);
            
            if (manufacturer != null && !manufacturer.isEmpty() && model != null && !model.isEmpty()) {
                holder.tvMixingDevice.setText(String.format("拌合设备：%s %s", manufacturer, model));
            } else {
                holder.tvMixingDevice.setText("拌合设备：标准拌合器");
            }
        }

        if (info.formingDevice != null) {
            String manufacturer = info.formingDevice.getManufacturer();
            String model = info.formingDevice.getModel();
            
            // 日志记录设备信息
            android.util.Log.d(TAG, "压实设备: 制造商=" + manufacturer + ", 型号=" + model);
            
            if (manufacturer != null && !manufacturer.isEmpty() && model != null && !model.isEmpty()) {
                holder.tvFormingDevice.setText(String.format("压实设备：%s %s", manufacturer, model));
            } else {
                holder.tvFormingDevice.setText("压实设备：标准压实器");
            }
        }
    }

    @Override
    public int getItemCount() {
        return specimenInfos.size();
    }

    public void setSpecimenInfos(List<SpecimenInfo> specimenInfos) {
        this.specimenInfos.clear();
        if (specimenInfos != null) {
            this.specimenInfos.addAll(specimenInfos);
        }
        notifyDataSetChanged();
    }

    public void addSpecimenInfo(MoldingMethod moldingMethod, MixRatio mixRatio,
                              DeviceInfo mixingDevice, DeviceInfo formingDevice) {
        specimenInfos.add(new SpecimenInfo(moldingMethod, mixRatio, mixingDevice, formingDevice));
        notifyItemInserted(specimenInfos.size() - 1);
    }

    public void setSelectedMixingDevices(List<String> selectedMixingDevices) {
        this.selectedMixingDevices = selectedMixingDevices;
    }

    public void setSelectedFormingDevices(List<String> selectedFormingDevices) {
        this.selectedFormingDevices = selectedFormingDevices;
    }
}
