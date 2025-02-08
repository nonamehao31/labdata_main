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
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SpecimenMoldingPagerAdapter extends RecyclerView.Adapter<SpecimenMoldingPagerAdapter.ViewHolder> {
    private final List<MoldingMethod> moldingMethods;
    private final List<MixRatio> mixRatios;
    private OnMethodSelectedListener listener;
    private int currentPosition = 0;
    private RecyclerView recyclerView;

    public interface OnMethodSelectedListener {
        void onMethodSelected(MoldingMethod method, int position);
    }

    public SpecimenMoldingPagerAdapter(String moldingMethodJson, List<MixRatio> mixRatios) {
        this.mixRatios = mixRatios;
        this.moldingMethods = new ArrayList<>();
        
        // 解析制件方法JSON
        if (moldingMethodJson != null && !moldingMethodJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<MoldingMethod>>(){}.getType();
            List<MoldingMethod> methods = gson.fromJson(moldingMethodJson, type);
            if (methods != null) {
                this.moldingMethods.addAll(methods);
            }
        }
    }

    public void setOnMethodSelectedListener(OnMethodSelectedListener listener) {
        this.listener = listener;
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
        MoldingMethod method = moldingMethods.get(position);
        MixRatio mixRatio = position < mixRatios.size() ? mixRatios.get(position) : null;
        
        holder.bind(method, mixRatio, position);
        
        // 更新选中状态
        holder.radioButton.setChecked(position == currentPosition);
        
        // 点击事件处理
        View.OnClickListener clickListener = v -> {
            if (position != currentPosition) {
                int oldPosition = currentPosition;
                currentPosition = position;
                notifyItemChanged(oldPosition);
                notifyItemChanged(currentPosition);
                
                if (listener != null) {
                    listener.onMethodSelected(method, position);
                }
            }
        };
        
        holder.itemView.setOnClickListener(clickListener);
        holder.radioButton.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    public MoldingMethod getCurrentMethod() {
        return currentPosition >= 0 && currentPosition < moldingMethods.size() ? 
            moldingMethods.get(currentPosition) : null;
    }

    public void updateDeviceInfo(String deviceType, DeviceInfo deviceInfo) {
        if (deviceType == null || deviceInfo == null) return;

        // 获取当前显示的 ViewHolder
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(currentPosition);
        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;
            if ("MIXING".equals(deviceType)) {
                vh.updateDeviceInfo(deviceInfo, null);
            } else if ("FORMING".equals(deviceType)) {
                vh.updateDeviceInfo(null, deviceInfo);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
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

        public void bind(MoldingMethod method, MixRatio mixRatio, int position) {
            // 设置配比名称
            if (mixRatio != null) {
                tvMixRatioName.setText(mixRatio.getName());
                tvMixRatioName.setVisibility(View.VISIBLE);
            } else {
                tvMixRatioName.setVisibility(View.GONE);
            }

            // 设置拌合参数
            if (method != null) {
                tvMixingTemp.setText(String.format("温度：%.0f℃", method.getMixingTemperature()));
                tvMixingSpeed.setText(String.format("速度：%.0f rpm", method.getMixingSpeed()));
                tvMixingTime.setText(String.format("时间：%.0f min", method.getMixingTime()));
                tvCompactionMethod.setText(method.getCompactionMethod());
                
                // 显示设备信息区域
                layoutMixingDevice.setVisibility(View.VISIBLE);
                layoutCompactionDevice.setVisibility(View.VISIBLE);
            } else {
                // 如果没有制件方法，使用占位符
                tvMixingTemp.setText("温度：--");
                tvMixingSpeed.setText("速度：--");
                tvMixingTime.setText("时间：--");
                tvCompactionMethod.setText("--");
                
                // 隐藏设备信息区域
                layoutMixingDevice.setVisibility(View.GONE);
                layoutCompactionDevice.setVisibility(View.GONE);
            }
        }

        public void updateDeviceInfo(DeviceInfo mixingDevice, DeviceInfo formingDevice) {
            // 显示拌合设备信息
            if (mixingDevice != null) {
                layoutMixingDevice.setVisibility(View.VISIBLE);
                tvMixingDeviceInfo.setText(String.format(
                    "型号：%s\n制造商：%s\n购买年份：%s",
                    mixingDevice.getModel(),
                    mixingDevice.getManufacturer(),
                    mixingDevice.getPurchaseYear()
                ));
            }

            // 显示压实设备信息
            if (formingDevice != null) {
                layoutCompactionDevice.setVisibility(View.VISIBLE);
                tvCompactionDeviceInfo.setText(String.format(
                    "型号：%s\n制造商：%s\n购买年份：%s",
                    formingDevice.getModel(),
                    formingDevice.getManufacturer(),
                    formingDevice.getPurchaseYear()
                ));
            }
        }
    }
}
