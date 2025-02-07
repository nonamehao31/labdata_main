package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;

import java.util.ArrayList;
import java.util.List;

public class ExperimentMoldingAdapter extends RecyclerView.Adapter<ExperimentMoldingAdapter.ViewHolder> {
    private final List<MixRatio> mixRatios = new ArrayList<>();
    private final List<String> moldingMethods = new ArrayList<>();
    private int selectedPosition = -1;
    private OnMixRatioSelectedListener listener;
    private DeviceInfo lastMixingDevice;
    private DeviceInfo lastFormingDevice;

    public interface OnMixRatioSelectedListener {
        void onMixRatioSelected(MixRatio mixRatio);
    }

    public ExperimentMoldingAdapter(ExperimentTask task) {
        this.mixRatios.addAll(task.getSelectedMixRatios());
        // 解析每个配比的制件方法
        String[] methods = task.getMoldingMethod().split(";");
        for (int i = 0; i < task.getSelectedMixRatios().size(); i++) {
            if (i < methods.length) {
                moldingMethods.add(methods[i]);
            } else {
                moldingMethods.add("");  // 如果制件方法数量不足，添加空字符串
            }
        }
    }

    public void setOnMixRatioSelectedListener(OnMixRatioSelectedListener listener) {
        this.listener = listener;
    }

    public void updateDeviceInfo(DeviceInfo deviceInfo) {
        if (deviceInfo == null) return;

        // 根据设备类型保存设备信息
        switch (deviceInfo.getType()) {
            case "MIXING":
                this.lastMixingDevice = deviceInfo;
                break;
            case "FORMING":
                this.lastFormingDevice = deviceInfo;
                break;
        }
        notifyItemChanged(selectedPosition);
    }

    public MixRatio getSelectedMixRatio() {
        return selectedPosition >= 0 && selectedPosition < mixRatios.size() ? 
            mixRatios.get(selectedPosition) : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experment_molding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MixRatio mixRatio = mixRatios.get(position);
        String moldingMethod = position < moldingMethods.size() ? moldingMethods.get(position) : "";
        holder.bind(mixRatio, moldingMethod, position + 1, position == selectedPosition);
        
        // 如果是选中的项目，显示设备信息
        if (position == selectedPosition) {
            holder.updateDeviceInfo(lastMixingDevice, lastFormingDevice);
        }
        
        holder.radioButton.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onMixRatioSelected(mixRatio);
            }
        });

        holder.itemView.setOnClickListener(v -> holder.radioButton.performClick());
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton radioButton;
        private final TextView tvMixRatioName;
        private final TextView tvMixingTemp;
        private final TextView tvMixingSpeed;
        private final TextView tvMixingTime;
        private final TextView tvCompactionMethod;
        private final LinearLayout layoutMixingDevice;
        private final LinearLayout layoutCompactionDevice;
        private final TextView tvMixingDeviceInfo;
        private final TextView tvCompactionDeviceInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingTemp = itemView.findViewById(R.id.tvMixingTemp);
            tvMixingSpeed = itemView.findViewById(R.id.tvMixingSpeed);
            tvMixingTime = itemView.findViewById(R.id.tvMixingTime);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            layoutMixingDevice = itemView.findViewById(R.id.layoutMixingDevice);
            layoutCompactionDevice = itemView.findViewById(R.id.layoutCompactionDevice);
            tvMixingDeviceInfo = itemView.findViewById(R.id.tvMixingDeviceInfo);
            tvCompactionDeviceInfo = itemView.findViewById(R.id.tvCompactionDeviceInfo);
        }

        public void bind(MixRatio mixRatio, String moldingMethod, int position, boolean isSelected) {
            radioButton.setChecked(isSelected);
            
            // 设置配比名称，处理 null 情况
            String mixRatioName = mixRatio != null ? mixRatio.getName() : "未选择配比";
            tvMixRatioName.setText(mixRatioName);

            // 解析制件方法字符串
            if (moldingMethod != null && !moldingMethod.isEmpty()) {
                String[] parts = moldingMethod.split("\\|");
                float temp = 0, speed = 0, time = 0;
                String method = "";
                
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim();
                        String value = keyValue[1].trim();
                        switch (key) {
                            case "temp":
                                temp = Float.parseFloat(value);
                                break;
                            case "speed":
                                speed = Float.parseFloat(value);
                                break;
                            case "time":
                                time = Float.parseFloat(value);
                                break;
                            case "method":
                                method = value;
                                break;
                        }
                    }
                }

                // 设置拌合参数
                tvMixingTemp.setText(String.format("拌合温度：%.1f℃", temp));
                tvMixingSpeed.setText(String.format("拌合速度：%.1f rpm", speed));
                tvMixingTime.setText(String.format("拌合时间：%.1f min", time));
                tvCompactionMethod.setText(method);
            } else {
                // 如果没有制件方法，清空显示
                tvMixingTemp.setText("拌合温度：--");
                tvMixingSpeed.setText("拌合速度：--");
                tvMixingTime.setText("拌合时间：--");
                tvCompactionMethod.setText("--");
            }

            // 清除设备信息显示
            layoutMixingDevice.setVisibility(View.GONE);
            layoutCompactionDevice.setVisibility(View.GONE);
        }

        public void updateDeviceInfo(DeviceInfo mixingDevice, DeviceInfo formingDevice) {
            // 显示拌合设备信息
            if (mixingDevice != null) {
                layoutMixingDevice.setVisibility(View.VISIBLE);
                tvMixingDeviceInfo.setText(String.format(
                    "设备型号：%s\n制造商：%s\n购买年份：%s",
                    mixingDevice.getModel(),
                    mixingDevice.getManufacturer(),
                    mixingDevice.getPurchaseYear()
                ));
            }

            // 显示制件设备信息
            if (formingDevice != null) {
                layoutCompactionDevice.setVisibility(View.VISIBLE);
                tvCompactionDeviceInfo.setText(String.format(
                    "设备型号：%s\n制造商：%s\n购买年份：%s",
                    formingDevice.getModel(),
                    formingDevice.getManufacturer(),
                    formingDevice.getPurchaseYear()
                ));
            }
        }
    }
}
