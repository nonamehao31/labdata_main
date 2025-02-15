package com.example.labdata_main.adapter;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.model.DeviceInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsphaltExperimentDataAdapter extends RecyclerView.Adapter<AsphaltExperimentDataAdapter.ViewHolder> {
    private List<String> experimentTypes;
    private Map<String, Map<String, String>> experimentDataMap;
    private Map<String, DeviceInfo> deviceInfoMap;
    private OnDeviceScanListener onDeviceScanListener;

    public AsphaltExperimentDataAdapter() {
        this.experimentTypes = new ArrayList<>();
        this.experimentDataMap = new HashMap<>();
        this.deviceInfoMap = new HashMap<>();
    }

    public interface OnDeviceScanListener {
        void onScanDevice(int position);
    }

    public void setOnDeviceScanListener(OnDeviceScanListener listener) {
        this.onDeviceScanListener = listener;
    }

    public void setExperimentTypes(List<String> types) {
        this.experimentTypes = types;
        // 初始化每个实验类型的数据Map
        for (String type : types) {
            if (!experimentDataMap.containsKey(type)) {
                experimentDataMap.put(type, new HashMap<>());
            }
        }
        notifyDataSetChanged();
    }

    public Map<String, Map<String, String>> getExperimentData() {
        // 收集所有输入字段的数据
        for (int i = 0; i < experimentTypes.size(); i++) {
            String experimentType = experimentTypes.get(i);
            DeviceInfo deviceInfo = deviceInfoMap.get(experimentType);
            if (deviceInfo != null) {
                Map<String, String> values = experimentDataMap.get(experimentType);
                if (values != null) {
                    values.put("device_id", deviceInfo.getDeviceId());
                    values.put("device_type", deviceInfo.getType());
                    values.put("device_name", deviceInfo.getName());
                    values.put("device_manufacturer", deviceInfo.getManufacturer());
                    values.put("device_model", deviceInfo.getModel());
                }
            }
        }
        return experimentDataMap;
    }

    public void updateDeviceInfo(int position, DeviceInfo deviceInfo) {
        if (position >= 0 && position < experimentTypes.size()) {
            String experimentType = experimentTypes.get(position);
            deviceInfoMap.put(experimentType, deviceInfo);
            
            // 更新实验数据中的设备信息
            Map<String, String> values = experimentDataMap.get(experimentType);
            if (values != null && deviceInfo != null) {
                values.put("device_id", deviceInfo.getDeviceId());
                values.put("device_type", deviceInfo.getType());
                values.put("device_name", deviceInfo.getName());
                values.put("device_manufacturer", deviceInfo.getManufacturer());
                values.put("device_model", deviceInfo.getModel());
            }
            
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asphalt_experiment_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String experimentType = experimentTypes.get(position);
        DeviceInfo deviceInfo = deviceInfoMap.get(experimentType);
        holder.bind(experimentType, deviceInfo, position);
    }

    @Override
    public int getItemCount() {
        return experimentTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExperimentTitle;
        private MaterialButton btnScanDevice;
        private TextView tvDeviceInfo;
        private LinearLayout layoutDataInputs;
        private Map<String, EditText> inputFields;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentTitle = itemView.findViewById(R.id.tvExperimentTitle);
            btnScanDevice = itemView.findViewById(R.id.btnScanDevice);
            tvDeviceInfo = itemView.findViewById(R.id.tvDeviceInfo);
            layoutDataInputs = itemView.findViewById(R.id.layoutDataInputs);
            inputFields = new HashMap<>();
        }

        void bind(String experimentType, DeviceInfo deviceInfo, int position) {
            // 设置实验标题
            tvExperimentTitle.setText(getExperimentTitle(experimentType));
            
            // 设置扫描设备按钮点击事件
            btnScanDevice.setOnClickListener(v -> {
                if (onDeviceScanListener != null) {
                    onDeviceScanListener.onScanDevice(position);
                }
            });

            // 更新设备信息显示
            updateDeviceInfoDisplay(deviceInfo);

            // 清除之前的输入字段
            layoutDataInputs.removeAllViews();
            inputFields.clear();

            // 根据实验类型添加相应的输入字段
            addInputFieldsForExperiment(experimentType);

            // 恢复已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            if (savedData != null) {
                for (Map.Entry<String, EditText> entry : inputFields.entrySet()) {
                    String value = savedData.get(entry.getKey());
                    if (value != null) {
                        entry.getValue().setText(value);
                    }
                }
            }
        }

        private void updateDeviceInfoDisplay(DeviceInfo deviceInfo) {
            if (deviceInfo != null) {
                String deviceText = String.format("设备名称: %s\n制造商: %s\n型号: %s\n购买年份: %s",
                    deviceInfo.getName(),
                    deviceInfo.getManufacturer(),
                    deviceInfo.getModel(),
                    deviceInfo.getPurchaseYear());
                    
                android.util.Log.d("DeviceDisplay", "Updating device display: " + deviceText);
                tvDeviceInfo.setText(deviceText);
                tvDeviceInfo.setVisibility(View.VISIBLE);
                btnScanDevice.setText("重新扫描实验设备码");
            } else {
                tvDeviceInfo.setText("未选择设备");
                tvDeviceInfo.setVisibility(View.VISIBLE);
                btnScanDevice.setText("扫描实验设备码");
            }
        }

        private void addInputField(String key, String label, String precision) {
            TextInputLayout textInputLayout = new TextInputLayout(itemView.getContext(), null, 
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textInputLayout.setHint(label);
            textInputLayout.setHelperText("精确到 " + precision);
            textInputLayout.setHelperTextEnabled(true);

            EditText editText = new EditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            // 添加文本变化监听器
            editText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    String experimentType = experimentTypes.get(getAdapterPosition());
                    Map<String, String> values = experimentDataMap.get(experimentType);
                    if (values != null) {
                        values.put(key, s.toString());
                    }
                }
            });

            textInputLayout.addView(editText);
            layoutDataInputs.addView(textInputLayout);
            
            // 添加间距
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textInputLayout.getLayoutParams();
            layoutParams.bottomMargin = (int) (16 * itemView.getContext().getResources().getDisplayMetrics().density);
            textInputLayout.setLayoutParams(layoutParams);
            
            inputFields.put(key, editText);
        }

        private void addInputFieldsForExperiment(String experimentType) {
            switch (experimentType) {
                case AsphaltExperimentData.TYPE_DENSITY:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.Density.M1, "干净比重瓶质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M2, "比重瓶和水的质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M3, "比重瓶和沥青的质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M4, "比重瓶和黏稠沥青试样的质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M5, "比重瓶和黏稠沥青试样和水的质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M6, "比重瓶和固体沥青试样的质量 (mg)", "1");
                    addInputField(AsphaltExperimentData.Fields.Density.M7, "比重瓶和固体沥青试样和水的质量 (mg)", "1");
                    break;

                case AsphaltExperimentData.TYPE_PENETRATION:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.Penetration.READING, "读数 (mm)", "0.1");
                    break;

                case AsphaltExperimentData.TYPE_DUCTILITY:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.Ductility.DISPLACEMENT, "拉长位移 (cm)", "0.1");
                    break;

                case AsphaltExperimentData.TYPE_SOFTENING_POINT:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMP, "软化温度 (℃)", "0.5");
                    break;

                case AsphaltExperimentData.TYPE_TFOT:
                    addInputField(AsphaltExperimentData.Fields.TFOT.M0, "盛样皿质量 (g)", "0.001");
                    addInputField(AsphaltExperimentData.Fields.TFOT.M1, "薄膜烘箱加热前盛样皿与试样合计质量 (g)", "0.001");
                    addInputField(AsphaltExperimentData.Fields.TFOT.M2, "薄膜烘箱加热后盛样皿与试样合计质量 (g)", "0.001");
                    break;

                case AsphaltExperimentData.TYPE_RTFOT:
                    addInputField(AsphaltExperimentData.Fields.RTFOT.M0, "盛样瓶质量 (g)", "0.001");
                    addInputField(AsphaltExperimentData.Fields.RTFOT.M1, "旋转薄膜加热前盛样瓶与试样合计质量 (g)", "0.001");
                    addInputField(AsphaltExperimentData.Fields.RTFOT.M2, "旋转薄膜加热后盛样瓶与试样合计质量 (g)", "0.001");
                    break;

                case AsphaltExperimentData.TYPE_FLASH_POINT:
                    addInputField(AsphaltExperimentData.Fields.FlashPoint.HEATING_RATE1, "升温速度1 (℃/min)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.FlashPoint.HEATING_RATE2, "升温速度2 (℃/min)", "0.1");
                    break;

                case AsphaltExperimentData.TYPE_VISCOSITY:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.Viscosity.ROTOR_TYPE, "转子型号", "");
                    addInputField(AsphaltExperimentData.Fields.Viscosity.ROTOR_SPEED, "转子速率 (r/min)", "1");
                    break;

                case AsphaltExperimentData.TYPE_BBR:
                    addInputField("temperature", "实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD, "试验荷载 (mN)", "1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFORMATION, "试件的形变量 (μm)", "1");
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS, "劲度模量 (MPa)", "1");
                    addInputField(AsphaltExperimentData.Fields.BBR.M_VALUE, "m值", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_8S, "8.0s下的试验结果", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_15S, "15.0s下的试验结果", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_30S, "30.0s下的试验结果", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_60S, "60.0s下的试验结果", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_120S, "120.0s下的试验结果", "0.001");
                    addInputField(AsphaltExperimentData.Fields.BBR.RESULT_240S, "240.0s下的试验结果", "0.001");
                    break;

                case AsphaltExperimentData.TYPE_DSR:
                    addInputField("temperature", "实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.DSR.CONTROL_MODE, "控制方式", "");
                    addInputField(AsphaltExperimentData.Fields.DSR.STRESS, "应力值 (kPa)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DSR.FREQUENCY, "试验频率 (rad/s)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.DSR.COMPLEX_MODULUS, "复合模量G* (kPa)", "1");
                    addInputField(AsphaltExperimentData.Fields.DSR.PHASE_ANGLE, "相位角 (°)", "0.1");
                    break;

                case AsphaltExperimentData.TYPE_DTT:
                    addInputField("temperature", "实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.DTT.FAILURE_LOAD, "破坏荷载 (N)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DTT.CROSS_SECTION, "试件的初始横断面积 (mm²)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DTT.ELONGATION, "破坏时伸长值 (mm)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DTT.EFFECTIVE_LENGTH, "试件有效拉伸长度 (mm)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DTT.FAILURE_STRESS, "破坏应力 (MPa)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.DTT.FAILURE_STRAIN, "破坏应变 (mm/mm)", "0.001");
                    break;

                case AsphaltExperimentData.TYPE_PAV:
                    addInputField(AsphaltExperimentData.Fields.PAV.PRESSURE, "压力 (MPa)", "0.1");
                    addInputField("temperature", "温度 (℃)", "0.5");
                    addInputField(AsphaltExperimentData.Fields.PAV.DURATION, "时间 (h)", "0.1");
                    break;

                case AsphaltExperimentData.TYPE_MSCR:
                    addInputField("temperature", "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.MSCR.FREQUENCY, "加载频率", "0.1");
                    addInputField(AsphaltExperimentData.Fields.MSCR.INITIAL_STRAIN, "初始应变 ∈0", "0.001");
                    addInputField(AsphaltExperimentData.Fields.MSCR.CREEP_STRAIN, "蠕变循环完成时的应变值 ∈c", "0.001");
                    addInputField(AsphaltExperimentData.Fields.MSCR.RECOVERY_STRAIN, "恢复循环完成时的应变值 ∈r", "0.001");
                    break;

                case AsphaltExperimentData.TYPE_FORCE_DUCTILITY:
                    addInputField("temperature", "试验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.ForceDuctility.STRETCH_SPEED, "拉伸速度", "0.1");
                    addInputField(AsphaltExperimentData.Fields.ForceDuctility.MAX_FORCE, "最大拉力值 (N)", "0.01");
                    addInputField(AsphaltExperimentData.Fields.ForceDuctility.MAX_DEFORMATION, "最大拉力时的伸长变形值 (cm)", "0.1");
                    break;
            }
        }

        private String getExperimentTitle(String experimentType) {
            switch (experimentType) {
                case AsphaltExperimentData.TYPE_DENSITY:
                    return "沥青密度与相对密度试验";
                case AsphaltExperimentData.TYPE_PENETRATION:
                    return "沥青针入度试验";
                case AsphaltExperimentData.TYPE_DUCTILITY:
                    return "沥青延度试验";
                case AsphaltExperimentData.TYPE_SOFTENING_POINT:
                    return "沥青软化点试验（环球法）";
                case AsphaltExperimentData.TYPE_TFOT:
                    return "沥青薄膜加热试验";
                case AsphaltExperimentData.TYPE_RTFOT:
                    return "沥青旋转薄膜加热试验";
                case AsphaltExperimentData.TYPE_FLASH_POINT:
                    return "沥青闪点与燃点试验（克利夫兰开口杯法）";
                case AsphaltExperimentData.TYPE_VISCOSITY:
                    return "沥青旋转黏度试验（布洛克菲尔德黏度计法）";
                case AsphaltExperimentData.TYPE_BBR:
                    return "沥青弯曲蠕变劲度试验（弯曲梁流变仪法）";
                case AsphaltExperimentData.TYPE_DSR:
                    return "沥青流变性质试验（动态剪切流变仪法）";
                case AsphaltExperimentData.TYPE_DTT:
                    return "沥青断裂性能试验（直接拉伸法）";
                case AsphaltExperimentData.TYPE_PAV:
                    return "压力老化容器加速沥青老化试验";
                case AsphaltExperimentData.TYPE_MSCR:
                    return "沥青多重应力蠕变恢复试验（MSCR）";
                case AsphaltExperimentData.TYPE_FORCE_DUCTILITY:
                    return "沥青拉伸性能试验（测力延度仪法）";
                default:
                    return "未知实验类型";
            }
        }
    }
}
