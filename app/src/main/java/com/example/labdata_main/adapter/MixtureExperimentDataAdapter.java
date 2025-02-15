package com.example.labdata_main.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixtureExperimentDataAdapter extends RecyclerView.Adapter<MixtureExperimentDataAdapter.ViewHolder> {
    private final List<MixRatio> mixRatios;
    private final Map<Long, List<String>> experimentAssignments;
    private final Map<String, Map<String, String>> experimentData = new HashMap<>();
    private final Map<String, DeviceInfo> deviceData = new HashMap<>();
    private OnDeviceScanRequestListener deviceScanListener;

    public interface OnDeviceScanRequestListener {
        void onDeviceScanRequested(int position, String experimentName);
    }

    public void setOnDeviceScanRequestListener(OnDeviceScanRequestListener listener) {
        this.deviceScanListener = listener;
    }

    public MixtureExperimentDataAdapter(List<MixRatio> mixRatios, Map<Long, List<String>> experimentAssignments) {
        this.mixRatios = mixRatios;
        this.experimentAssignments = experimentAssignments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MixRatio mixRatio = mixRatios.get(position);
        List<String> experiments = experimentAssignments.get(mixRatio.getId());

        // 设置实验名称
        holder.tvExperimentName.setText(mixRatio.getName());

        // 清除之前的输入字段
        holder.layoutInputs.removeAllViews();

        // 为每个实验创建输入字段
        if (experiments != null) {
            for (String experiment : experiments) {
                holder.addInputField(experiment, mixRatio.getId());
            }
        }

        // 设置扫描按钮点击事件
        holder.btnScanDevice.setOnClickListener(v -> {
            if (deviceScanListener != null) {
                deviceScanListener.onDeviceScanRequested(position, mixRatio.getName());
            }
        });

        // 显示已保存的设备信息
        String deviceKey = String.valueOf(mixRatio.getId());
        DeviceInfo deviceInfo = deviceData.get(deviceKey);
        if (deviceInfo != null) {
            holder.tvDeviceInfo.setText(String.format("设备编号：%s", deviceInfo.getDeviceId()));
        } else {
            holder.tvDeviceInfo.setText("未选择设备");
        }
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
    }

    public Map<String, Map<String, String>> getExperimentData() {
        return experimentData;
    }

    public void setDeviceInfo(int position, DeviceInfo deviceInfo) {
        String deviceKey = String.valueOf(mixRatios.get(position).getId());
        deviceData.put(deviceKey, deviceInfo);
        notifyItemChanged(position);
    }

    public DeviceInfo getDeviceInfo(int mixRatioId) {
        return deviceData.get(String.valueOf(mixRatioId));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName;
        TextView tvDeviceInfo;
        MaterialButton btnScanDevice;
        LinearLayout layoutInputs;

        ViewHolder(View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvDeviceInfo = itemView.findViewById(R.id.tvDeviceInfo);
            btnScanDevice = itemView.findViewById(R.id.btnScanDevice);
            layoutInputs = itemView.findViewById(R.id.layoutInputs);
        }

        void addInputField(String experimentName, long mixRatioId) {
            switch (experimentName) {
                case "马歇尔稳定度试验":
                    addMarshallStabilityFields(mixRatioId);
                    break;
                case "沥青混合料车辙实验（汉堡车辙）":
                    addHamburgWheelTrackingFields(mixRatioId);
                    break;
                case "理论最大相对密度试验":
                    addSingleInputField("理论最大相对密度", "g/cm³", experimentName, mixRatioId);
                    break;
                case "体积密度试验":
                    addSingleInputField("体积密度", "g/cm³", experimentName, mixRatioId);
                    break;
                case "空隙率试验":
                    addSingleInputField("空隙率", "%", experimentName, mixRatioId);
                    break;
                case "飞散试验":
                    addSingleInputField("飞散损失率", "%", experimentName, mixRatioId);
                    break;
                case "动稳定度试验":
                    addSingleInputField("动稳定度", "次/mm", experimentName, mixRatioId);
                    break;
            }
        }

        private void addMarshallStabilityFields(long mixRatioId) {
            String experimentName = "马歇尔稳定度试验";
            // 添加稳定度输入字段
            for (int i = 1; i <= 3; i++) {
                addSingleInputField("稳定度" + i, "kN", experimentName + "_stability_" + i, mixRatioId);
            }
            // 添加流值输入字段
            for (int i = 1; i <= 3; i++) {
                addSingleInputField("流值" + i, "mm", experimentName + "_flow_" + i, mixRatioId);
            }
        }

        private void addHamburgWheelTrackingFields(long mixRatioId) {
            String experimentName = "沥青混合料车辙实验（汉堡车辙）";
            addSingleInputField("第一稳态曲线斜率", "mm/cycle", experimentName + "_first_slope", mixRatioId);
            addSingleInputField("第一稳态曲线截距", "mm", experimentName + "_first_intercept", mixRatioId);
            addSingleInputField("第二稳态曲线斜率", "mm/cycle", experimentName + "_second_slope", mixRatioId);
            addSingleInputField("第二稳态曲线截距", "mm", experimentName + "_second_intercept", mixRatioId);
        }

        private void addSingleInputField(String label, String unit, String dataKey, long mixRatioId) {
            // 创建输入布局
            TextInputLayout inputLayout = new TextInputLayout(itemView.getContext(), null, 
                    com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            inputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            inputLayout.setHint(label + " (" + unit + ")");
            inputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            ((LinearLayout.LayoutParams) inputLayout.getLayoutParams()).setMargins(0, 0, 0, 16);

            // 创建输入框
            TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

            // 添加输入框到布局
            inputLayout.addView(editText);
            layoutInputs.addView(inputLayout);

            // 为输入框添加文本变化监听器
            String key = mixRatioId + "_" + dataKey;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // 获取或创建该配比的数据Map
                    Map<String, String> mixRatioData = experimentData.computeIfAbsent(
                            String.valueOf(mixRatioId),
                            k -> new HashMap<>()
                    );
                    // 保存实验数据
                    mixRatioData.put(dataKey, s.toString());
                }
            });
        }
    }
}
