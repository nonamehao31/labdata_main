package com.example.labdata_main.adapter;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpecimenMethodAdapter extends RecyclerView.Adapter<SpecimenMethodAdapter.ViewHolder> {
    private static final String TAG = "SpecimenMethodAdapter";
    private List<MoldingMethod> moldingMethods;
    private List<MixRatio> mixRatios;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnMethodSelectedListener onMethodSelectedListener;
    private Map<Integer, DeviceInfo> mixingDevices = new HashMap<>();
    private Map<Integer, DeviceInfo> formingDevices = new HashMap<>();

    public interface OnMethodSelectedListener {
        void onMethodSelected(MoldingMethod method, MixRatio mixRatio, int position);
    }

    public SpecimenMethodAdapter(String moldingMethodJson, List<MixRatio> mixRatios) {
        Log.d(TAG, "Creating adapter with JSON: " + moldingMethodJson);
        Log.d(TAG, "Mix ratios size: " + (mixRatios != null ? mixRatios.size() : 0));
        
        this.mixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
        this.moldingMethods = new ArrayList<>();
        
        try {
            if (moldingMethodJson != null && !moldingMethodJson.isEmpty()) {
                Gson gson = new Gson();
                if (moldingMethodJson.trim().startsWith("[")) {
                    this.moldingMethods = gson.fromJson(moldingMethodJson, 
                        new TypeToken<List<MoldingMethod>>(){}.getType());
                    Log.d(TAG, "Parsed JSON array, size: " + this.moldingMethods.size());
                } else if (moldingMethodJson.trim().startsWith("{")) {
                    MoldingMethod method = gson.fromJson(moldingMethodJson, MoldingMethod.class);
                    if (method != null) {
                        this.moldingMethods.add(method);
                        Log.d(TAG, "Added single method from JSON object");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        Log.d(TAG, "Final molding methods size: " + this.moldingMethods.size());
        Log.d(TAG, "Final mix ratios size: " + this.mixRatios.size());
    }

    public void setOnMethodSelectedListener(OnMethodSelectedListener listener) {
        this.onMethodSelectedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding ViewHolder at position: " + position);
        
        MoldingMethod method = moldingMethods.get(position);
        // 找到对应的配比
        MixRatio mixRatio = null;
        if (!mixRatios.isEmpty()) {
            // 根据position计算对应的配比索引
            int mixRatioIndex = position % mixRatios.size();
            mixRatio = mixRatios.get(mixRatioIndex);
        }

        Log.d(TAG, String.format("Binding data - Position: %d, MixRatio: %s, Method: temp=%.1f, speed=%.1f, time=%.1f, compaction=%s",
            position,
            mixRatio != null ? mixRatio.getName() : "null",
            method.getMixingTemperature(),
            method.getMixingSpeed(),
            method.getMixingTime(),
            method.getCompactionMethod()));

        // 设置配比名称
        if (mixRatio != null) {
            holder.tvMixRatioName.setText(String.format("配比%d：%s", (position / mixRatios.size()) + 1, mixRatio.getName()));
            holder.tvMixRatioName.setVisibility(View.VISIBLE);
        } else {
            holder.tvMixRatioName.setVisibility(View.GONE);
        }

        // 设置拌合参数
        holder.tvMixingTemp.setText(String.format(Locale.getDefault(), 
            "%.1f℃", method.getMixingTemperature()));
        holder.tvMixingSpeed.setText(String.format(Locale.getDefault(), 
            "%.1frpm", method.getMixingSpeed()));
        holder.tvMixingTime.setText(String.format(Locale.getDefault(), 
            "%.1fmin", method.getMixingTime()));

        // 设置压实方法
        holder.tvCompactionMethod.setText(method.getCompactionMethod());

        // 设置设备信息
        DeviceInfo mixingDevice = mixingDevices.get(position);
        if (mixingDevice != null) {
            holder.tvMixingDevice.setText(String.format("拌合设备：%s %s",
                mixingDevice.getManufacturer(),
                mixingDevice.getModel()));
        } else {
            holder.tvMixingDevice.setText("拌合设备：未扫描");
        }

        DeviceInfo formingDevice = formingDevices.get(position);
        if (formingDevice != null) {
            holder.tvFormingDevice.setText(String.format("压实设备：%s %s",
                formingDevice.getManufacturer(),
                formingDevice.getModel()));
        } else {
            holder.tvFormingDevice.setText("压实设备：未扫描");
        }

        // 设置选中状态
        boolean isSelected = position == selectedPosition;
        int backgroundColor = isSelected ? 
            holder.itemView.getContext().getResources().getColor(R.color.selected_card_background) : 
            holder.itemView.getContext().getResources().getColor(android.R.color.white);
        
        // 应用卡片背景颜色
        if (isSelected) {
            holder.cardView.setCardElevation(6f); // 稍微降低选中时的阴影高度
            holder.cardView.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_specimen_card_selected));
            holder.cardView.setStrokeWidth(0); // 移除默认边框，使用背景drawable的边框
        } else {
            holder.cardView.setCardElevation(2f); // 与XML中定义的相匹配
            holder.cardView.setCardBackgroundColor(backgroundColor);
            holder.cardView.setBackground(null);
            holder.cardView.setStrokeWidth(0); // 移除默认边框
        }

        // 设置点击事件
        final MixRatio finalMixRatio = mixRatio;
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // 更新之前选中项和新选中项的视图
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            
            if (selectedPosition != RecyclerView.NO_POSITION) {
                // 添加选中动画效果
                if (previousPosition != selectedPosition) {
                    // 缩放动画
                    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.05f, 1.0f);
                    scaleAnimator.setDuration(300);
                    scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    scaleAnimator.addUpdateListener(animation -> {
                        float scale = (float) animation.getAnimatedValue();
                        holder.cardView.setScaleX(scale);
                        holder.cardView.setScaleY(scale);
                    });
                    scaleAnimator.start();
                    
                    // 立即应用背景变化，不等待notifyItemChanged
                    holder.cardView.setCardElevation(6f);
                    holder.cardView.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.bg_specimen_card_selected));
                }
                
                notifyItemChanged(selectedPosition);
            }
            
            // 通知监听器
            if (onMethodSelectedListener != null) {
                onMethodSelectedListener.onMethodSelected(method, finalMixRatio, selectedPosition);
            }
        });
        
        Log.d(TAG, "ViewHolder bound successfully");
    }

    @Override
    public int getItemCount() {
        int count = moldingMethods.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvMixRatioName;
        TextView tvMixingTemp;
        TextView tvMixingSpeed;
        TextView tvMixingTime;
        TextView tvCompactionMethod;
        TextView tvMixingDevice;
        TextView tvFormingDevice;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingTemp = itemView.findViewById(R.id.tvMixingTemp);
            tvMixingSpeed = itemView.findViewById(R.id.tvMixingSpeed);
            tvMixingTime = itemView.findViewById(R.id.tvMixingTime);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            tvMixingDevice = itemView.findViewById(R.id.tvMixingDevice);
            tvFormingDevice = itemView.findViewById(R.id.tvFormingDevice);
            
            // 验证所有视图都找到了
            if (tvMixRatioName == null || tvMixingTemp == null || 
                tvMixingSpeed == null || tvMixingTime == null || 
                tvCompactionMethod == null || tvMixingDevice == null ||
                tvFormingDevice == null) {
                Log.e(TAG, "Failed to find one or more views in ViewHolder");
            }
        }
    }

    public MoldingMethod getSelectedMethod() {
        return selectedPosition != RecyclerView.NO_POSITION ? 
            moldingMethods.get(selectedPosition) : null;
    }

    public MixRatio getSelectedMixRatio() {
        if (selectedPosition != RecyclerView.NO_POSITION && !mixRatios.isEmpty()) {
            int mixRatioIndex = selectedPosition % mixRatios.size();
            return mixRatios.get(mixRatioIndex);
        }
        return null;
    }

    public void updateDeviceInfo(DeviceInfo deviceInfo) {
        if (deviceInfo == null || selectedPosition == RecyclerView.NO_POSITION) {
            return;
        }

        if (DeviceInfo.TYPE_MIXING.equals(deviceInfo.getType())) {
            mixingDevices.put(selectedPosition, deviceInfo);
        } else if (DeviceInfo.TYPE_FORMING.equals(deviceInfo.getType())) {
            formingDevices.put(selectedPosition, deviceInfo);
        }

        notifyItemChanged(selectedPosition);
    }

    public DeviceInfo getMixingDevice(int position) {
        return mixingDevices.get(position);
    }

    public DeviceInfo getFormingDevice(int position) {
        return formingDevices.get(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public MoldingMethod getMoldingMethod(int position) {
        if (position < 0 || position >= moldingMethods.size()) {
            return null;
        }
        return moldingMethods.get(position);
    }

    public MixRatio getMixRatio(int position) {
        if (position < 0 || position >= moldingMethods.size() || mixRatios.isEmpty()) {
            return null;
        }
        int mixRatioIndex = position % mixRatios.size();
        return mixRatios.get(mixRatioIndex);
    }
}
