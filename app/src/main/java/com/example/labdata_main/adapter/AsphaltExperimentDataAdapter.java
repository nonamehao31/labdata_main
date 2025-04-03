package com.example.labdata_main.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.utils.BBRCalculator;
import com.example.labdata_main.utils.DSRCalculator;
import com.example.labdata_main.utils.ExperimentTypeInitializer;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AsphaltExperimentDataAdapter extends RecyclerView.Adapter<AsphaltExperimentDataAdapter.ViewHolder> {
    private List<String> experimentTypes;
    private Map<String, Map<String, String>> experimentDataMap;
    private Map<String, DeviceInfo> deviceInfoMap;
    private OnDeviceScanListener onDeviceScanListener;
    private OnDataValidityChangeListener dataValidityChangeListener;
    private boolean isAllDataValid = false;

    // 数据有效性监听接口
    public interface OnDataValidityChangeListener {
        void onDataValidityChanged(boolean isValid);
    }

    // 设置数据有效性监听器
    public void setOnDataValidityChangeListener(OnDataValidityChangeListener listener) {
        this.dataValidityChangeListener = listener;
    }

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
    
    /**
     * 设置初始化数据
     * @param initialData 实验类型到数据Map的映射
     */
    public void setInitialData(Map<String, Map<String, String>> initialData) {
        if (initialData == null) {
            return;
        }
        
        // 合并初始数据到实验数据Map
        for (Map.Entry<String, Map<String, String>> entry : initialData.entrySet()) {
            String experimentType = entry.getKey();
            Map<String, String> data = entry.getValue();
            
            // 确保实验类型存在于实验类型列表中
            if (experimentTypes.contains(experimentType)) {
                // 获取或创建该实验的数据Map
                Map<String, String> existingData = experimentDataMap.get(experimentType);
                if (existingData == null) {
                    existingData = new HashMap<>();
                    experimentDataMap.put(experimentType, existingData);
                }
                
                // 合并数据，已有数据不覆盖
                for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                    if (!existingData.containsKey(dataEntry.getKey())) {
                        existingData.put(dataEntry.getKey(), dataEntry.getValue());
                    }
                }
            }
        }
        
        // 通知数据变化
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
        private Button btnScanDevice;
        private TextView tvDeviceInfo;
        private LinearLayout layoutDataInputs;
        private Map<String, EditText> inputFields;
        private boolean isCalculating = false; // 添加标志位，防止递归计算

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
            String title = getExperimentTitle(experimentType);
            if (title == null || title.isEmpty()) {
                title = "未知实验类型 (" + experimentType + ")";
            }
            tvExperimentTitle.setText(title);
            tvExperimentTitle.setTag(experimentType);
            
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
            
            // 计算 BBR 实验结果
            calculateBBRResults(experimentType);
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
            addInputField(key, label, precision, false);
        }

        /**
         * 添加输入字段
         * @param key 字段键
         * @param label 字段标签
         * @param precision 精度
         * @param readOnly 是否只读
         */
        private void addInputField(String key, String label, String precision, boolean readOnly) {
            TextInputLayout textInputLayout = new TextInputLayout(itemView.getContext(), null, 
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textInputLayout.setHint(label);
            
            if (!readOnly) {
                textInputLayout.setHelperText("精确到 " + precision);
                textInputLayout.setHelperTextEnabled(true);
            }

            EditText editText = new EditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setTag(precision);
            
            // 如果是只读字段，设置为不可编辑
            if (readOnly) {
                editText.setEnabled(false);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setBackgroundResource(android.R.color.transparent);
            }
            
            // 获取已有的值
            String experimentType = experimentTypes.get(getAdapterPosition());
            Map<String, String> values = experimentDataMap.get(experimentType);
            if (values != null && values.containsKey(key)) {
                editText.setText(values.get(key));
            }
            
            // 添加文本变化监听器
            android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
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
                    
                    // 计算 BBR 实验结果，但避免递归调用
                    if (!isCalculating && AsphaltExperimentData.TYPE_BBR.equals(experimentType)) {
                        calculateBBRResults(experimentType);
                    }

                    // 验证实验数据完整性
                    validateExperimentData();
                }
            };
            editText.addTextChangedListener(textWatcher);
            editText.setTag(R.id.tag_text_watcher, textWatcher); // 保存监听器到标签中，以便后续可以移除

            textInputLayout.addView(editText);
            layoutDataInputs.addView(textInputLayout);
            
            // 添加间距
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textInputLayout.getLayoutParams();
            layoutParams.bottomMargin = (int) (16 * itemView.getContext().getResources().getDisplayMetrics().density);
            textInputLayout.setLayoutParams(layoutParams);
            
            inputFields.put(key, editText);
        }

        private void addInputFieldToContainer(LinearLayout container, String key, String label, String precision, Map<String, String> savedData) {
            addInputFieldToContainer(container, key, label, precision, savedData, false);
        }

        private void addInputFieldToContainer(LinearLayout container, String key, String label, String precision, Map<String, String> savedData, boolean readOnly) {
            TextInputLayout textInputLayout = new TextInputLayout(itemView.getContext(), null, 
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textInputLayout.setHint(label);
            
            if (!readOnly) {
                textInputLayout.setHelperText("精确到 " + precision);
                textInputLayout.setHelperTextEnabled(true);
            }

            EditText editText = new EditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setTag(precision);
            
            // 如果是只读字段，设置为不可编辑
            if (readOnly) {
                editText.setEnabled(false);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                editText.setBackgroundResource(android.R.color.transparent);
            }
            
            // 获取已有的值
            if (savedData != null && savedData.containsKey(key)) {
                editText.setText(savedData.get(key));
            }
            
            // 添加文本变化监听器
            android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (savedData != null) {
                        savedData.put(key, s.toString());
                    }
                }
            };
            editText.addTextChangedListener(textWatcher);
            editText.setTag(R.id.tag_text_watcher, textWatcher); // 保存监听器到标签中，以便后续可以移除

            textInputLayout.addView(editText);
            container.addView(textInputLayout);
            
            // 添加间距
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textInputLayout.getLayoutParams();
            layoutParams.bottomMargin = (int) (16 * itemView.getContext().getResources().getDisplayMetrics().density);
            textInputLayout.setLayoutParams(layoutParams);
            
            inputFields.put(key, editText);
        }

        private void addTextInputField(String key, String label) {
            TextInputLayout textInputLayout = new TextInputLayout(itemView.getContext(), null, 
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textInputLayout.setHint(label);
            
            EditText editText = new EditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setInputType(InputType.TYPE_CLASS_TEXT); // 设置为文本输入
            
            // 获取已有的值
            String experimentType = experimentTypes.get(getAdapterPosition());
            Map<String, String> values = experimentDataMap.get(experimentType);
            if (values != null && values.containsKey(key)) {
                editText.setText(values.get(key));
            }
            
            // 添加文本变化监听器
            android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
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
                    
                    // 验证实验数据完整性
                    validateExperimentData();
                }
            };
            editText.addTextChangedListener(textWatcher);
            editText.setTag(R.id.tag_text_watcher, textWatcher); // 保存监听器到标签中，以便后续可以移除

            textInputLayout.addView(editText);
            layoutDataInputs.addView(textInputLayout);
            
            // 添加间距
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textInputLayout.getLayoutParams();
            layoutParams.bottomMargin = (int) (16 * itemView.getContext().getResources().getDisplayMetrics().density);
            textInputLayout.setLayoutParams(layoutParams);
            
            inputFields.put(key, editText);
        }

        private void addInputFieldsForExperiment(String experimentType) {
            Log.d("AsphaltAdapter", "开始添加实验输入字段，实验类型: " + experimentType);
            // 检查实验类型是否有效
            if (experimentType == null || experimentType.isEmpty()) {
                TextView errorText = new TextView(itemView.getContext());
                errorText.setText("无效的实验类型");
                errorText.setTextColor(Color.RED);
                layoutDataInputs.addView(errorText);
                return;
            }
            
            // 获取实验数据的HashMap
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            if (savedData == null) {
                savedData = new HashMap<>();
                experimentDataMap.put(experimentType, savedData);
            }

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
                    addInputField(AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE, "温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE, "软化温度 (℃)", "0.5");
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
                    // 添加标题
                    TextView titleView = new TextView(itemView.getContext());
                    titleView.setText("试件尺寸");
                    titleView.setTextSize(16);
                    titleView.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(titleView);
                    
                    // 试件尺寸
                    addInputField(AsphaltExperimentData.Fields.BBR.BEAM_SPAN, "弯曲梁跨度 (mm)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.SPECIMEN_WIDTH, "试件宽度 (mm)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.SPECIMEN_HEIGHT, "试件高度 (mm)", "0.1");
                    
                    // 添加时间点数据标题
                    TextView timePointsTitle = new TextView(itemView.getContext());
                    timePointsTitle.setText("时间点数据");
                    timePointsTitle.setTextSize(16);
                    timePointsTitle.setPadding(0, 24, 0, 8);
                    layoutDataInputs.addView(timePointsTitle);
                    
                    // 时间点数据 - 8秒
                    TextView time8sTitle = new TextView(itemView.getContext());
                    time8sTitle.setText("8秒时间点");
                    time8sTitle.setTextSize(14);
                    time8sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time8sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_8S, "8秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_8S, "8秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_8S, "8秒时的变形挠度 (mm)", "0.001");
                    
                    // 时间点数据 - 15秒
                    TextView time15sTitle = new TextView(itemView.getContext());
                    time15sTitle.setText("15秒时间点");
                    time15sTitle.setTextSize(14);
                    time15sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time15sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_15S, "15秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_15S, "15秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_15S, "15秒时的变形挠度 (mm)", "0.001");
                    
                    // 时间点数据 - 30秒
                    TextView time30sTitle = new TextView(itemView.getContext());
                    time30sTitle.setText("30秒时间点");
                    time30sTitle.setTextSize(14);
                    time30sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time30sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_30S, "30秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_30S, "30秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_30S, "30秒时的变形挠度 (mm)", "0.001");
                    
                    // 时间点数据 - 60秒
                    TextView time60sTitle = new TextView(itemView.getContext());
                    time60sTitle.setText("60秒时间点");
                    time60sTitle.setTextSize(14);
                    time60sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time60sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_60S, "60秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_60S, "60秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_60S, "60秒时的变形挠度 (mm)", "0.001");
                    
                    // 时间点数据 - 120秒
                    TextView time120sTitle = new TextView(itemView.getContext());
                    time120sTitle.setText("120秒时间点");
                    time120sTitle.setTextSize(14);
                    time120sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time120sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_120S, "120秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_120S, "120秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_120S, "120秒时的变形挠度 (mm)", "0.001");
                    
                    // 时间点数据 - 240秒
                    TextView time240sTitle = new TextView(itemView.getContext());
                    time240sTitle.setText("240秒时间点");
                    time240sTitle.setTextSize(14);
                    time240sTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(time240sTitle);
                    
                    addInputField(AsphaltExperimentData.Fields.BBR.TEMPERATURE_240S, "240秒时的实验温度 (℃)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.LOAD_240S, "240秒时的施加荷载 (N)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.BBR.DEFLECTION_240S, "240秒时的变形挠度 (mm)", "0.001");
                    
                    // 添加计算结果标题
                    TextView resultsTitle = new TextView(itemView.getContext());
                    resultsTitle.setText("计算结果（自动计算）");
                    resultsTitle.setTextSize(16);
                    resultsTitle.setPadding(0, 24, 0, 8);
                    layoutDataInputs.addView(resultsTitle);
                    
                    // 弯曲蠕变劲度模量结果
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_8S, "8秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_15S, "15秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_30S, "30秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_60S, "60秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_120S, "120秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    addInputField(AsphaltExperimentData.Fields.BBR.STIFFNESS_240S, "240秒时的弯曲蠕变劲度模量 (MPa)", "0.6", true);
                    
                    // 蠕变速率结果
                    addInputField(AsphaltExperimentData.Fields.BBR.CREEP_RATE, "蠕变速率 (m值)", "0.001", true);
                    break;

                case AsphaltExperimentData.TYPE_DSR:
                    // 添加试验基本参数标题
                    TextView dsrBasicTitle = new TextView(itemView.getContext());
                    dsrBasicTitle.setText("试验基本参数");
                    dsrBasicTitle.setTextSize(16);
                    dsrBasicTitle.setPadding(0, 16, 0, 8);
                    layoutDataInputs.addView(dsrBasicTitle);
                    
                    // 添加试验板参数
                    addInputField(AsphaltExperimentData.Fields.DSR.PLATE_RADIUS, "试验板半径(R) (mm)", "0.1");
                    addInputField(AsphaltExperimentData.Fields.DSR.PLATE_GAP, "试验平板间距(h) (mm)", "0.01");
                    addTextInputField(AsphaltExperimentData.Fields.DSR.CONTROL_MODE, "控制方式");
                    
                    // 动态温度点数据部分
                    TextView dsrDataTitle = new TextView(itemView.getContext());
                    dsrDataTitle.setText("试验温度点数据");
                    dsrDataTitle.setTextSize(16);
                    dsrDataTitle.setPadding(0, 24, 0, 8);
                    layoutDataInputs.addView(dsrDataTitle);
                    
                    // 添加已有的温度点数据
                    addExistingTemperaturePoints();
                    
                    // 添加新温度点按钮
                    Button btnAddTemperature = new Button(itemView.getContext());
                    btnAddTemperature.setText("+ 添加新温度点");
                    btnAddTemperature.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_light));
                    btnAddTemperature.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                    btnAddTemperature.setPadding(16, 8, 16, 8);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    buttonParams.setMargins(0, 16, 0, 16);
                    btnAddTemperature.setLayoutParams(buttonParams);
                    
                    btnAddTemperature.setOnClickListener(v -> {
                        addNewTemperaturePoint();
                    });
                    
                    layoutDataInputs.addView(btnAddTemperature);
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

                case AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY:
                    // 只添加转子型号和转子速率
                    addInputField(AsphaltExperimentData.Fields.BrookfieldViscosity.ROTOR_TYPE, "转子型号", "");
                    addInputField(AsphaltExperimentData.Fields.BrookfieldViscosity.ROTATION_SPEED, "转子速率 (r/min)", "1");
                    
                    // 添加温度点数据部分
                    TextView viscosityDataTitle = new TextView(itemView.getContext());
                    viscosityDataTitle.setText("温度点数据");
                    viscosityDataTitle.setTextSize(16);
                    viscosityDataTitle.setPadding(0, 24, 0, 8);
                    layoutDataInputs.addView(viscosityDataTitle);
                    
                    // 添加已有的温度点数据
                    addExistingViscosityTemperaturePoints();
                    
                    // 添加新温度点按钮
                    Button btnAddViscosityTemperature = new Button(itemView.getContext());
                    btnAddViscosityTemperature.setText("+ 添加新温度点");
                    btnAddViscosityTemperature.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_light));
                    btnAddViscosityTemperature.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                    btnAddViscosityTemperature.setPadding(16, 8, 16, 8);
                    LinearLayout.LayoutParams viscosityButtonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    viscosityButtonParams.setMargins(0, 16, 0, 16);
                    btnAddViscosityTemperature.setLayoutParams(viscosityButtonParams);
                    
                    btnAddViscosityTemperature.setOnClickListener(v -> {
                        addNewViscosityTemperaturePoint();
                    });
                    
                    layoutDataInputs.addView(btnAddViscosityTemperature);
                    break;
                    
                default:
                    break;
            }
        }

        private String getExperimentTitle(String experimentType) {
            switch (experimentType) {
                case AsphaltExperimentData.TYPE_DENSITY:
                    return "密度与相对密度试验";
                case AsphaltExperimentData.TYPE_PENETRATION:
                    return "针入度试验";
                case AsphaltExperimentData.TYPE_DUCTILITY:
                    return "延度试验";
                case AsphaltExperimentData.TYPE_SOFTENING_POINT:
                    return "软化点试验";
                case AsphaltExperimentData.TYPE_TFOT:
                    return "薄膜烘箱老化试验";
                case AsphaltExperimentData.TYPE_RTFOT:
                    return "旋转薄膜烘箱老化试验";
                case AsphaltExperimentData.TYPE_FLASH_POINT:
                    return "闪点与燃点试验";
                case AsphaltExperimentData.TYPE_VISCOSITY:
                    return "标准粘度试验";
                case AsphaltExperimentData.TYPE_BBR:
                    return "弯曲梁流变仪试验";
                case AsphaltExperimentData.TYPE_DSR:
                    return "动态剪切流变仪试验";
                case AsphaltExperimentData.TYPE_DTT:
                    return "直接拉伸试验";
                case AsphaltExperimentData.TYPE_PAV:
                    return "压力老化试验";
                case AsphaltExperimentData.TYPE_MSCR:
                    return "多重应力蠕变恢复试验";
                case AsphaltExperimentData.TYPE_FORCE_DUCTILITY:
                    return "力延度试验";
                case AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY:
                    return "布鲁克菲尔德旋转黏度试验";
                default:
                    return "未知实验类型 (" + experimentType + ")";
            }
        }

        /**
         * 计算 BBR 实验的弯曲蠕变劲度模量和蠕变速率
         * 当用户输入了必要的数据后自动计算
         * @param experimentType 实验类型
         */
        private void calculateBBRResults(String experimentType) {
            if (!AsphaltExperimentData.TYPE_BBR.equals(experimentType)) {
                return;
            }

            // 设置标志位，防止递归调用
            if (isCalculating) {
                return;
            }
            isCalculating = true;

            try {
                Map<String, String> values = experimentDataMap.get(experimentType);
                if (values == null) {
                    return;
                }

                // 检查是否输入了必要的数据
                String beamSpanStr = values.get(AsphaltExperimentData.Fields.BBR.BEAM_SPAN);
                String specimenWidthStr = values.get(AsphaltExperimentData.Fields.BBR.SPECIMEN_WIDTH);
                String specimenHeightStr = values.get(AsphaltExperimentData.Fields.BBR.SPECIMEN_HEIGHT);

                // 检查挠度数据
                String deflection8sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_8S);
                String deflection15sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_15S);
                String deflection30sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_30S);
                String deflection60sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_60S);
                String deflection120sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_120S);
                String deflection240sStr = values.get(AsphaltExperimentData.Fields.BBR.DEFLECTION_240S);

                // 如果缺少基本数据，则不计算
                if (beamSpanStr == null || beamSpanStr.isEmpty() || 
                    specimenWidthStr == null || specimenWidthStr.isEmpty() || 
                    specimenHeightStr == null || specimenHeightStr.isEmpty()) {
                    return;
                }

                try {
                    // 转换基本数据为数值
                    double beamSpan = Double.parseDouble(beamSpanStr);
                    double specimenWidth = Double.parseDouble(specimenWidthStr);
                    double specimenHeight = Double.parseDouble(specimenHeightStr);
                    
                    // 创建时间点和挠度数组
                    double[] times = new double[6];
                    double[] deflections = new double[6];
                    double[] stiffnesses = new double[6];
                    boolean[] hasDeflection = new boolean[6];
                    
                    // 尝试解析每个时间点的挠度，如果有效则计算相应的劲度模量
                    if (deflection8sStr != null && !deflection8sStr.isEmpty()) {
                        try {
                            double deflection8s = Double.parseDouble(deflection8sStr);
                            String load8sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_8S);
                            if (load8sStr == null || load8sStr.isEmpty()) {
                                hasDeflection[0] = false;
                            } else {
                                double load8s = Double.parseDouble(load8sStr);
                                double stiffness8s = BBRCalculator.calculateStiffness(load8s, beamSpan, specimenWidth, specimenHeight, deflection8s);
                                
                                // 添加日志输出
                                android.util.Log.d("BBRCalculator", "8s: load=" + load8s + ", beamSpan=" + beamSpan + 
                                    ", width=" + specimenWidth + ", height=" + specimenHeight + 
                                    ", deflection=" + deflection8s + ", stiffness=" + stiffness8s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_8S, String.format("%.6f", stiffness8s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness8sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_8S);
                                if (stiffness8sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness8sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness8sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness8sField.setText(String.format("%.6f", stiffness8s));
                                    if (watcher != null) {
                                        stiffness8sField.addTextChangedListener(watcher);
                                    }
                                    stiffness8sField.invalidate(); // 强制刷新UI
                                }
                                
                                times[0] = 8.0;
                                deflections[0] = deflection8s;
                                stiffnesses[0] = stiffness8s;
                                hasDeflection[0] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[0] = false;
                        }
                    }
                    
                    if (deflection15sStr != null && !deflection15sStr.isEmpty()) {
                        try {
                            double deflection15s = Double.parseDouble(deflection15sStr);
                            String load15sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_15S);
                            if (load15sStr == null || load15sStr.isEmpty()) {
                                hasDeflection[1] = false;
                            } else {
                                double load15s = Double.parseDouble(load15sStr);
                                double stiffness15s = BBRCalculator.calculateStiffness(load15s, beamSpan, specimenWidth, specimenHeight, deflection15s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_15S, String.format("%.1f", stiffness15s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness15sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_15S);
                                if (stiffness15sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness15sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness15sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness15sField.setText(String.format("%.1f", stiffness15s));
                                    if (watcher != null) {
                                        stiffness15sField.addTextChangedListener(watcher);
                                    }
                                }
                                
                                times[1] = 15.0;
                                deflections[1] = deflection15s;
                                stiffnesses[1] = stiffness15s;
                                hasDeflection[1] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[1] = false;
                        }
                    }
                    
                    if (deflection30sStr != null && !deflection30sStr.isEmpty()) {
                        try {
                            double deflection30s = Double.parseDouble(deflection30sStr);
                            String load30sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_30S);
                            if (load30sStr == null || load30sStr.isEmpty()) {
                                hasDeflection[2] = false;
                            } else {
                                double load30s = Double.parseDouble(load30sStr);
                                double stiffness30s = BBRCalculator.calculateStiffness(load30s, beamSpan, specimenWidth, specimenHeight, deflection30s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_30S, String.format("%.6f", stiffness30s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness30sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_30S);
                                if (stiffness30sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness30sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness30sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness30sField.setText(String.format("%.6f", stiffness30s));
                                    if (watcher != null) {
                                        stiffness30sField.addTextChangedListener(watcher);
                                    }
                                }
                                
                                times[2] = 30.0;
                                deflections[2] = deflection30s;
                                stiffnesses[2] = stiffness30s;
                                hasDeflection[2] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[2] = false;
                        }
                    }
                    
                    if (deflection60sStr != null && !deflection60sStr.isEmpty()) {
                        try {
                            double deflection60s = Double.parseDouble(deflection60sStr);
                            String load60sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_60S);
                            if (load60sStr == null || load60sStr.isEmpty()) {
                                hasDeflection[3] = false;
                            } else {
                                double load60s = Double.parseDouble(load60sStr);
                                double stiffness60s = BBRCalculator.calculateStiffness(load60s, beamSpan, specimenWidth, specimenHeight, deflection60s);
                                
                                // 添加日志输出
                                android.util.Log.d("BBRCalculator", "60s: load=" + load60s + ", beamSpan=" + beamSpan + 
                                    ", width=" + specimenWidth + ", height=" + specimenHeight + 
                                    ", deflection=" + deflection60s + ", stiffness=" + stiffness60s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_60S, String.format("%.1f", stiffness60s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness60sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_60S);
                                if (stiffness60sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness60sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness60sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness60sField.setText(String.format("%.1f", stiffness60s));
                                    if (watcher != null) {
                                        stiffness60sField.addTextChangedListener(watcher);
                                    }
                                    stiffness60sField.invalidate(); // 强制刷新UI
                                }
                                
                                times[3] = 60.0;
                                deflections[3] = deflection60s;
                                stiffnesses[3] = stiffness60s;
                                hasDeflection[3] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[3] = false;
                        }
                    }
                    
                    if (deflection120sStr != null && !deflection120sStr.isEmpty()) {
                        try {
                            double deflection120s = Double.parseDouble(deflection120sStr);
                            String load120sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_120S);
                            if (load120sStr == null || load120sStr.isEmpty()) {
                                hasDeflection[4] = false;
                            } else {
                                double load120s = Double.parseDouble(load120sStr);
                                double stiffness120s = BBRCalculator.calculateStiffness(load120s, beamSpan, specimenWidth, specimenHeight, deflection120s);
                                
                                // 添加日志输出
                                android.util.Log.d("BBRCalculator", "120s: load=" + load120s + ", beamSpan=" + beamSpan + 
                                    ", width=" + specimenWidth + ", height=" + specimenHeight + 
                                    ", deflection=" + deflection120s + ", stiffness=" + stiffness120s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_120S, String.format("%.6f", stiffness120s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness120sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_120S);
                                if (stiffness120sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness120sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness120sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness120sField.setText(String.format("%.6f", stiffness120s));
                                    if (watcher != null) {
                                        stiffness120sField.addTextChangedListener(watcher);
                                    }
                                }
                                
                                times[4] = 120.0;
                                deflections[4] = deflection120s;
                                stiffnesses[4] = stiffness120s;
                                hasDeflection[4] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[4] = false;
                        }
                    }
                    
                    if (deflection240sStr != null && !deflection240sStr.isEmpty()) {
                        try {
                            double deflection240s = Double.parseDouble(deflection240sStr);
                            String load240sStr = values.get(AsphaltExperimentData.Fields.BBR.LOAD_240S);
                            if (load240sStr == null || load240sStr.isEmpty()) {
                                hasDeflection[5] = false;
                            } else {
                                double load240s = Double.parseDouble(load240sStr);
                                double stiffness240s = BBRCalculator.calculateStiffness(load240s, beamSpan, specimenWidth, specimenHeight, deflection240s);
                                
                                // 存储计算结果
                                values.put(AsphaltExperimentData.Fields.BBR.STIFFNESS_240S, String.format("%.1f", stiffness240s));
                                
                                // 更新UI，但不触发文本变化监听器
                                EditText stiffness240sField = inputFields.get(AsphaltExperimentData.Fields.BBR.STIFFNESS_240S);
                                if (stiffness240sField != null) {
                                    android.text.TextWatcher watcher = (android.text.TextWatcher) stiffness240sField.getTag(R.id.tag_text_watcher);
                                    if (watcher != null) {
                                        stiffness240sField.removeTextChangedListener(watcher);
                                    }
                                    stiffness240sField.setText(String.format("%.1f", stiffness240s));
                                    if (watcher != null) {
                                        stiffness240sField.addTextChangedListener(watcher);
                                    }
                                    stiffness240sField.invalidate(); // 强制刷新UI
                                }
                                
                                times[5] = 240.0;
                                deflections[5] = deflection240s;
                                stiffnesses[5] = stiffness240s;
                                hasDeflection[5] = true;
                            }
                        } catch (NumberFormatException e) {
                            hasDeflection[5] = false;
                        }
                    }
                    
                    // 计算蠕变速率(m值)，至少需要3个有效的时间点数据
                    int validCount = 0;
                    for (boolean valid : hasDeflection) {
                        if (valid) validCount++;
                    }
                    
                    if (validCount >= 3) {
                        // 准备有效的时间点和劲度模量数据
                        double[] validTimes = new double[validCount];
                        double[] validStiffnesses = new double[validCount];
                        int index = 0;
                        
                        for (int i = 0; i < hasDeflection.length; i++) {
                            if (hasDeflection[i]) {
                                validTimes[index] = times[i];
                                validStiffnesses[index] = stiffnesses[i];
                                index++;
                            }
                        }
                        
                        // 计算蠕变速率
                        double creepRate = BBRCalculator.calculateCreepRate(validTimes, validStiffnesses);
                        
                        // 添加日志输出
                        StringBuilder timesLog = new StringBuilder("Valid times: ");
                        StringBuilder stiffnessesLog = new StringBuilder("Valid stiffnesses: ");
                        for (int i = 0; i < validCount; i++) {
                            timesLog.append(validTimes[i]).append(", ");
                            stiffnessesLog.append(validStiffnesses[i]).append(", ");
                        }
                        android.util.Log.d("BBRCalculator", timesLog.toString());
                        android.util.Log.d("BBRCalculator", stiffnessesLog.toString());
                        android.util.Log.d("BBRCalculator", "Calculated creep rate: " + creepRate);
                        
                        // 存储蠕变速率
                        values.put(AsphaltExperimentData.Fields.BBR.CREEP_RATE, String.format("%.3f", creepRate));
                        
                        // 更新蠕变速率输入字段，但不触发文本变化监听器
                        EditText creepRateField = inputFields.get(AsphaltExperimentData.Fields.BBR.CREEP_RATE);
                        if (creepRateField != null) {
                            android.text.TextWatcher watcher = (android.text.TextWatcher) creepRateField.getTag(R.id.tag_text_watcher);
                            if (watcher != null) {
                                creepRateField.removeTextChangedListener(watcher);
                            }
                            creepRateField.setText(String.format("%.3f", creepRate));
                            if (watcher != null) {
                                creepRateField.addTextChangedListener(watcher);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 其他错误
                    android.util.Log.e("BBRCalculation", "计算过程中出现错误", e);
                }
            } finally {
                // 计算完成，重置标志位
                isCalculating = false;
            }
        }
        
        /**
         * ViewHolder类中的方法，用于添加温度点的输入字段组
         * @param pointId 温度点ID
         */
        private void addTemperaturePointInputs(String pointId) {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_DSR.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            
            // 使用简短的温度点ID作为标题
            String shortId = pointId.substring(Math.max(0, pointId.length() - 4));
            
            // 创建温度点容器
            LinearLayout pointContainer = new LinearLayout(itemView.getContext());
            pointContainer.setOrientation(LinearLayout.VERTICAL);
            pointContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            pointContainer.setPadding(8, 8, 8, 16);
            pointContainer.setTag(pointId);
            pointContainer.setBackgroundResource(R.drawable.border_background);
            
            // 添加标题和删除按钮
            LinearLayout headerLayout = new LinearLayout(itemView.getContext());
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            
            TextView pointTitle = new TextView(itemView.getContext());
            pointTitle.setText("温度点 " + shortId);
            pointTitle.setTextSize(16);
            pointTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            pointTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1));
            pointTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            
            Button btnDelete = new Button(itemView.getContext());
            btnDelete.setText("删除");
            btnDelete.setTextSize(12);
            btnDelete.setBackgroundColor(Color.RED);
            btnDelete.setTextColor(Color.WHITE);
            
            // 设置删除按钮点击事件
            btnDelete.setOnClickListener(v -> {
                // 从布局中移除温度点容器
                layoutDataInputs.removeView(pointContainer);
                
                // 从数据中移除温度点相关数据
                String tempKey = AsphaltExperimentData.Fields.DSR.TEMPERATURE_PREFIX + pointId;
                String freqKey = AsphaltExperimentData.Fields.DSR.FREQUENCY_PREFIX + pointId;
                String stressKey = AsphaltExperimentData.Fields.DSR.MAX_SHEAR_STRESS_PREFIX + pointId;
                String strainKey = AsphaltExperimentData.Fields.DSR.MAX_SHEAR_STRAIN_PREFIX + pointId;
                String phaseKey = AsphaltExperimentData.Fields.DSR.PHASE_ANGLE_PREFIX + pointId;
                String modulusKey = AsphaltExperimentData.Fields.DSR.COMPLEX_MODULUS_PREFIX + pointId;
                
                // 从输入字段映射中移除
                inputFields.remove(tempKey);
                inputFields.remove(freqKey);
                inputFields.remove(stressKey);
                inputFields.remove(strainKey);
                inputFields.remove(phaseKey);
                inputFields.remove(modulusKey);
                
                // 从保存的数据中移除
                savedData.remove(tempKey);
                savedData.remove(freqKey);
                savedData.remove(stressKey);
                savedData.remove(strainKey);
                savedData.remove(phaseKey);
                savedData.remove(modulusKey);
                
                // 重新编号剩余的温度点
                renumberTemperaturePoints();
            });
            
            headerLayout.addView(pointTitle);
            headerLayout.addView(btnDelete);
            pointContainer.addView(headerLayout);
            
            // 定义温度点的数据键
            String tempKey = AsphaltExperimentData.Fields.DSR.TEMPERATURE_PREFIX + pointId;
            String freqKey = AsphaltExperimentData.Fields.DSR.FREQUENCY_PREFIX + pointId;
            String stressKey = AsphaltExperimentData.Fields.DSR.MAX_SHEAR_STRESS_PREFIX + pointId;
            String strainKey = AsphaltExperimentData.Fields.DSR.MAX_SHEAR_STRAIN_PREFIX + pointId;
            String phaseKey = AsphaltExperimentData.Fields.DSR.PHASE_ANGLE_PREFIX + pointId;
            String modulusKey = AsphaltExperimentData.Fields.DSR.COMPLEX_MODULUS_PREFIX + pointId;
            
            // 添加输入数据部分
            TextView inputDataTitle = new TextView(itemView.getContext());
            inputDataTitle.setText("输入数据");
            inputDataTitle.setTextSize(14);
            inputDataTitle.setPadding(0, 8, 0, 8);
            inputDataTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            pointContainer.addView(inputDataTitle);
            
            // 添加输入字段到容器
            addInputFieldToContainer(pointContainer, tempKey, "试验温度 (℃)", "0.1", savedData);
            addInputFieldToContainer(pointContainer, freqKey, "试验加载频率 (Hz)", "0.01", savedData);
            addInputFieldToContainer(pointContainer, stressKey, "最大剪切应力 (τmax) (Pa)", "0.1", savedData);
            addInputFieldToContainer(pointContainer, strainKey, "最大剪切应变 (γmax) (%)", "0.01", savedData);
            addInputFieldToContainer(pointContainer, phaseKey, "相位角 (δ) (°)", "0.1", savedData);
            
            // 添加计算结果部分
            TextView calculatedDataTitle = new TextView(itemView.getContext());
            calculatedDataTitle.setText("计算结果（自动计算）");
            calculatedDataTitle.setTextSize(14);
            calculatedDataTitle.setPadding(0, 16, 0, 8);
            calculatedDataTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            pointContainer.addView(calculatedDataTitle);
            
            // 添加复合模量字段（只读）
            addInputFieldToContainer(pointContainer, modulusKey, "复合剪切模量 (G*) (Pa)", "0.1", savedData, true);
            
            // 添加文本变化监听器，用于计算复合模量
            EditText etStress = inputFields.get(stressKey);
            EditText etStrain = inputFields.get(strainKey);
            
            if (etStress != null && etStrain != null) {
                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {
                        // 计算复合模量
                        calculateComplexModulus(pointId, stressKey, strainKey, modulusKey);
                    }
                };
                
                etStress.addTextChangedListener(textWatcher);
                etStrain.addTextChangedListener(textWatcher);
            }
            
            // 将温度点容器添加到主布局
            layoutDataInputs.addView(pointContainer);
            
            // 初始计算复合模量
            calculateComplexModulus(pointId, stressKey, strainKey, modulusKey);
        }
        
        /**
         * 重新编号温度点
         */
        private void renumberTemperaturePoints() {
            // 收集所有温度点容器
            List<View> pointViews = new ArrayList<>();
            for (int i = 0; i < layoutDataInputs.getChildCount(); i++) {
                View child = layoutDataInputs.getChildAt(i);
                if (child instanceof LinearLayout && child.getTag() != null && 
                    child.getTag().toString().startsWith("temperature_point_")) {
                    pointViews.add(child);
                }
            }
            
            // 重新编号
            for (int i = 0; i < pointViews.size(); i++) {
                View pointView = pointViews.get(i);
                // 找到标题文本视图
                for (int j = 0; j < ((LinearLayout) pointView).getChildCount(); j++) {
                    View headerChild = ((LinearLayout) pointView).getChildAt(j);
                    if (headerChild instanceof LinearLayout) {
                        for (int k = 0; k < ((LinearLayout) headerChild).getChildCount(); k++) {
                            View titleView = ((LinearLayout) headerChild).getChildAt(k);
                            if (titleView instanceof TextView && 
                                ((TextView) titleView).getText().toString().startsWith("温度点")) {
                                // 更新标题
                                ((TextView) titleView).setText("温度点 " + (i + 1));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        /**
         * 计算复合模量
         * @param pointId 温度点ID
         * @param stressKey 应力键
         * @param strainKey 应变键
         * @param modulusKey 模量键
         */
        private void calculateComplexModulus(String pointId, String stressKey, String strainKey, String modulusKey) {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_DSR.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            if (savedData == null) {
                return;
            }
            
            // 获取输入字段
            EditText etStress = inputFields.get(stressKey);
            EditText etStrain = inputFields.get(strainKey);
            EditText etModulus = inputFields.get(modulusKey);
            
            if (etStress == null || etStrain == null || etModulus == null) {
                return;
            }
            
            try {
                // 获取应力和应变值
                String stressStr = etStress.getText().toString().trim();
                String strainStr = etStrain.getText().toString().trim();
                
                if (stressStr.isEmpty() || strainStr.isEmpty()) {
                    etModulus.setText("");
                    savedData.put(modulusKey, "");
                    return;
                }
                
                double stress = Double.parseDouble(stressStr);
                double strain = Double.parseDouble(strainStr);
                
                // 应变值是百分比，需要转换为小数
                strain = strain / 100.0;
                
                if (strain <= 0) {
                    etModulus.setText("");
                    savedData.put(modulusKey, "");
                    return;
                }
                
                // 使用DSRCalculator计算复合模量
                double complexModulus = DSRCalculator.calculateComplexModulus(stress, strain);
                
                // 格式化结果
                String formattedModulus = String.format("%.1f", complexModulus);
                
                // 更新UI和保存数据
                etModulus.setText(formattedModulus);
                savedData.put(modulusKey, formattedModulus);
                
            } catch (NumberFormatException e) {
                // 输入无效，清空模量
                etModulus.setText("");
                savedData.put(modulusKey, "");
            }
        }
        
        /**
         * 添加已有的温度点数据
         */
        private void addExistingTemperaturePoints() {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_DSR.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            if (savedData == null || savedData.isEmpty()) {
                return;
            }
            
            // 查找所有温度点ID
            Set<String> pointIds = new HashSet<>();
            String tempPrefix = AsphaltExperimentData.Fields.DSR.TEMPERATURE_PREFIX;
            
            for (String key : savedData.keySet()) {
                if (key.startsWith(tempPrefix)) {
                    String pointId = key.substring(tempPrefix.length());
                    pointIds.add(pointId);
                }
            }
            
            // 添加每个温度点
            for (String pointId : pointIds) {
                addTemperaturePointInputs(pointId);
            }
        }
        
        /**
         * 添加新的温度点
         */
        private void addNewTemperaturePoint() {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_DSR.equals(experimentType)) {
                return;
            }
            
            // 计算当前容器中的测量值数量
            int currentCount = 0;
            for (int i = 0; i < layoutDataInputs.getChildCount(); i++) {
                View child = layoutDataInputs.getChildAt(i);
                if (child instanceof LinearLayout && child.getTag() != null && 
                    child.getTag().toString().startsWith("temperature_point_")) {
                    currentCount++;
                }
            }
            
            // 新的测量值ID为当前数量+1
            int newTempId = currentCount + 1;
            
            // 添加新的温度点
            addTemperaturePointInputs(String.valueOf(newTempId));
        }
        
        /**
         * 添加已有的布鲁克菲尔德旋转黏度试验温度点
         */
        private void addExistingViscosityTemperaturePoints() {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            
            // 查找已有的温度点
            Set<String> pointIds = new HashSet<>();
            for (String key : savedData.keySet()) {
                if (key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX)) {
                    String pointId = key.substring(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX.length());
                    pointIds.add(pointId);
                }
            }
            
            for (String pointId : pointIds) {
                addViscosityTemperaturePointInputs(pointId);
            }
            
            // 如果没有温度点，添加一个默认的
            if (pointIds.isEmpty()) {
                addViscosityTemperaturePointInputs("1");
            }
        }
        
        /**
         * 添加新的布鲁克菲尔德旋转黏度试验温度点
         */
        private void addNewViscosityTemperaturePoint() {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            
            // 查找已有的温度点
            Set<Integer> pointIds = new HashSet<>();
            for (String key : savedData.keySet()) {
                if (key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX)) {
                    String pointIdStr = key.substring(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX.length());
                    try {
                        int pointId = Integer.parseInt(pointIdStr);
                        pointIds.add(pointId);
                    } catch (NumberFormatException e) {
                        // 忽略非数字ID
                    }
                }
            }
            
            // 查找已有的温度点容器
            for (int i = 0; i < layoutDataInputs.getChildCount(); i++) {
                View child = layoutDataInputs.getChildAt(i);
                if (child.getTag() instanceof String) {
                    String tag = (String) child.getTag();
                    if (tag.startsWith("viscosity_temp_point_")) {
                        String pointIdStr = tag.substring("viscosity_temp_point_".length());
                        try {
                            int pointId = Integer.parseInt(pointIdStr);
                            pointIds.add(pointId);
                        } catch (NumberFormatException e) {
                            // 忽略非数字ID
                        }
                    }
                }
            }
            
            // 找到最大的ID
            int maxId = 0;
            for (int id : pointIds) {
                if (id > maxId) {
                    maxId = id;
                }
            }
            
            // 添加新的温度点
            addViscosityTemperaturePointInputs(String.valueOf(maxId + 1));
        }
        
        /**
         * 添加布鲁克菲尔德旋转黏度试验温度点的输入字段组
         * @param pointId 温度点ID
         */
        private void addViscosityTemperaturePointInputs(String pointId) {
            // 获取当前实验类型
            String experimentType = (String) tvExperimentTitle.getTag();
            if (!AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY.equals(experimentType)) {
                return;
            }
            
            // 获取已保存的数据
            Map<String, String> savedData = experimentDataMap.get(experimentType);
            
            // 使用简短的温度点ID作为标题
            String shortId = pointId.substring(Math.max(0, pointId.length() - 4));
            
            // 创建温度点容器
            LinearLayout pointContainer = new LinearLayout(itemView.getContext());
            pointContainer.setOrientation(LinearLayout.VERTICAL);
            pointContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            pointContainer.setPadding(8, 8, 8, 16);
            pointContainer.setTag("viscosity_temp_point_" + pointId);
            pointContainer.setBackgroundResource(R.drawable.border_background);
            
            // 添加标题和删除按钮
            LinearLayout headerLayout = new LinearLayout(itemView.getContext());
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            
            TextView pointTitle = new TextView(itemView.getContext());
            pointTitle.setText("温度点 " + shortId);
            pointTitle.setTextSize(16);
            pointTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            pointTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1));
            pointTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
            
            Button btnDelete = new Button(itemView.getContext());
            btnDelete.setText("删除");
            btnDelete.setTextSize(12);
            btnDelete.setBackgroundColor(Color.RED);
            btnDelete.setTextColor(Color.WHITE);
            
            // 设置删除按钮点击事件
            btnDelete.setOnClickListener(v -> {
                // 从布局中移除温度点容器
                layoutDataInputs.removeView(pointContainer);
                
                // 从数据中移除温度点相关数据
                String tempKey = AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX + pointId;
                
                // 查找并移除该温度点的所有黏度测量值
                List<String> keysToRemove = new ArrayList<>();
                for (String key : savedData.keySet()) {
                    if (key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.SPINDLE_TYPE_PREFIX + pointId + "_") ||
                        key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.ROTATION_SPEED_PREFIX + pointId + "_") ||
                        key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_")) {
                        keysToRemove.add(key);
                        
                        // 从输入字段映射中移除
                        inputFields.remove(key);
                    }
                }
                
                // 从输入字段映射中移除温度
                inputFields.remove(tempKey);
                
                // 从保存的数据中移除
                savedData.remove(tempKey);
                for (String key : keysToRemove) {
                    savedData.remove(key);
                }
                
                // 重新编号剩余的温度点
                renumberViscosityTemperaturePoints();
            });
            
            headerLayout.addView(pointTitle);
            headerLayout.addView(btnDelete);
            pointContainer.addView(headerLayout);
            
            // 添加温度输入字段
            String tempKey = AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX + pointId;
            addInputFieldToContainer(pointContainer, tempKey, "试验温度 (℃)", "0.1", savedData);
            
            // 添加黏度测量值容器
            LinearLayout viscosityContainer = new LinearLayout(itemView.getContext());
            viscosityContainer.setOrientation(LinearLayout.VERTICAL);
            viscosityContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            viscosityContainer.setPadding(0, 8, 0, 8);
            viscosityContainer.setTag("viscosity_container_" + pointId);
            pointContainer.addView(viscosityContainer);
            
            // 添加黏度测量值标题
            TextView viscosityTitle = new TextView(itemView.getContext());
            viscosityTitle.setText("黏度测量值");
            viscosityTitle.setTextSize(14);
            viscosityTitle.setPadding(0, 8, 0, 8);
            viscosityContainer.addView(viscosityTitle);
            
            // 添加已有的黏度测量值
            Set<String> viscosityIds = new HashSet<>();
            for (String key : savedData.keySet()) {
                if (key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_")) {
                    String viscosityId = key.substring((AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_").length());
                    viscosityIds.add(viscosityId);
                }
            }
            
            for (String viscosityId : viscosityIds) {
                addViscosityMeasurementInputs(viscosityContainer, pointId, viscosityId, savedData);
            }
            
            // 如果没有黏度测量值，添加一个默认的
            if (viscosityIds.isEmpty()) {
                addViscosityMeasurementInputs(viscosityContainer, pointId, "1", savedData);
            }
            
            // 添加新增黏度测量值按钮
            Button btnAddViscosity = new Button(itemView.getContext());
            btnAddViscosity.setText("添加黏度测量值");
            btnAddViscosity.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            btnAddViscosity.setOnClickListener(v -> {
                // 计算当前容器中的测量值数量
                int currentCount = 0;
                for (int i = 0; i < viscosityContainer.getChildCount(); i++) {
                    View child = viscosityContainer.getChildAt(i);
                    if (child instanceof LinearLayout && child.getTag() != null && 
                        child.getTag().toString().startsWith("viscosity_measurement_")) {
                        currentCount++;
                    }
                }
                
                // 新的测量值ID为当前数量+1
                int newViscosityId = currentCount + 1;
                
                // 添加新的黏度测量值
                addViscosityMeasurementInputs(viscosityContainer, pointId, String.valueOf(newViscosityId), savedData);
            });
            viscosityContainer.addView(btnAddViscosity);
            
            // 将温度点容器添加到主布局
            layoutDataInputs.addView(pointContainer);
            
            // 重新编号剩余的温度点
            renumberViscosityTemperaturePoints();
        }
        
        /**
         * 重新编号黏度温度点
         */
        private void renumberViscosityTemperaturePoints() {
            // 收集所有温度点容器
            List<View> pointViews = new ArrayList<>();
            for (int i = 0; i < layoutDataInputs.getChildCount(); i++) {
                View child = layoutDataInputs.getChildAt(i);
                if (child instanceof LinearLayout && child.getTag() != null && 
                    child.getTag().toString().startsWith("viscosity_temp_point_")) {
                    pointViews.add(child);
                }
            }
            
            // 重新编号
            for (int i = 0; i < pointViews.size(); i++) {
                View pointView = pointViews.get(i);
                // 找到标题文本视图
                for (int j = 0; j < ((LinearLayout) pointView).getChildCount(); j++) {
                    View headerChild = ((LinearLayout) pointView).getChildAt(j);
                    if (headerChild instanceof LinearLayout) {
                        for (int k = 0; k < ((LinearLayout) headerChild).getChildCount(); k++) {
                            View titleView = ((LinearLayout) headerChild).getChildAt(k);
                            if (titleView instanceof TextView && 
                                ((TextView) titleView).getText().toString().startsWith("温度点")) {
                                // 更新标题
                                ((TextView) titleView).setText("温度点 " + (i + 1));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        /**
         * 添加黏度测量值的输入字段组
         * @param container 容器
         * @param pointId 温度点ID
         * @param viscosityId 黏度测量值ID
         * @param savedData 已保存的数据
         */
        private void addViscosityMeasurementInputs(LinearLayout container, String pointId, String viscosityId, Map<String, String> savedData) {
            // 创建黏度测量值容器
            LinearLayout measurementContainer = new LinearLayout(itemView.getContext());
            measurementContainer.setOrientation(LinearLayout.VERTICAL);
            measurementContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            measurementContainer.setPadding(8, 8, 8, 8);
            measurementContainer.setTag("viscosity_measurement_" + pointId + "_" + viscosityId);
            measurementContainer.setBackgroundResource(R.drawable.border_background);
            
            // 添加标题和删除按钮
            LinearLayout headerLayout = new LinearLayout(itemView.getContext());
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            
            TextView measurementTitle = new TextView(itemView.getContext());
            measurementTitle.setText("测量值 " + viscosityId);
            measurementTitle.setTextSize(14);
            measurementTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            measurementTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1));
            measurementTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
            
            Button btnDelete = new Button(itemView.getContext());
            btnDelete.setText("删除");
            btnDelete.setTextSize(12);
            btnDelete.setBackgroundColor(Color.RED);
            btnDelete.setTextColor(Color.WHITE);
            
            // 设置删除按钮点击事件
            btnDelete.setOnClickListener(v -> {
                // 从布局中移除黏度测量值容器
                container.removeView(measurementContainer);
                
                // 从数据中移除黏度测量值相关数据
                String viscosityKey = AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_" + viscosityId;
                
                // 从输入字段映射中移除
                inputFields.remove(viscosityKey);
                
                // 从保存的数据中移除
                savedData.remove(viscosityKey);
                
                // 重新编号剩余的测量值
                renumberViscosityMeasurements(container);
            });
            
            headerLayout.addView(measurementTitle);
            headerLayout.addView(btnDelete);
            measurementContainer.addView(headerLayout);
            
            // 只添加黏度值输入字段
            String viscosityKey = AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_" + viscosityId;
            addInputFieldToContainer(measurementContainer, viscosityKey, "黏度值 (Pa·s)", "0.1", savedData);
            
            // 将黏度测量值容器添加到容器中
            container.addView(measurementContainer);
        }
        
        /**
         * 重新编号容器中的黏度测量值
         * @param container 容器
         */
        private void renumberViscosityMeasurements(LinearLayout container) {
            // 收集所有测量值容器
            List<View> measurementViews = new ArrayList<>();
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof LinearLayout && child.getTag() != null && 
                    child.getTag().toString().startsWith("viscosity_measurement_")) {
                    measurementViews.add(child);
                }
            }
            
            // 重新编号
            for (int i = 0; i < measurementViews.size(); i++) {
                View measurementView = measurementViews.get(i);
                // 找到标题文本视图
                for (int j = 0; j < ((LinearLayout) measurementView).getChildCount(); j++) {
                    View headerChild = ((LinearLayout) measurementView).getChildAt(j);
                    if (headerChild instanceof LinearLayout) {
                        for (int k = 0; k < ((LinearLayout) headerChild).getChildCount(); k++) {
                            View titleView = ((LinearLayout) headerChild).getChildAt(k);
                            if (titleView instanceof TextView && 
                                ((TextView) titleView).getText().toString().startsWith("测量值")) {
                                // 更新标题
                                ((TextView) titleView).setText("测量值 " + (i + 1));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }

        /**
         * 验证当前实验数据是否完整有效
         * @return 如果所有必填字段都已填写，返回true；否则返回false
         */
        public boolean validateExperimentData() {
            boolean isValid = true;
            // 获取所有实验数据
            Map<String, Map<String, String>> data = getExperimentData();
            
            // 如果没有实验数据，则无效
            if (data == null || data.isEmpty()) {
                return false;
            }
            
            // 检查每个实验类型
            for (String experimentType : experimentTypes) {
                Map<String, String> experimentValues = data.get(experimentType);
                // 如果某个实验类型没有数据，则无效
                if (experimentValues == null || experimentValues.isEmpty()) {
                    isValid = false;
                    break;
                }
                
                // 根据实验类型检查必填字段
                if (AsphaltExperimentData.TYPE_PENETRATION.equals(experimentType)) {
                    // 针入度实验：检查温度和读数
                    if (!isValidField(experimentValues, "temperature") || 
                        !isValidField(experimentValues, AsphaltExperimentData.Fields.Penetration.READING)) {
                        isValid = false;
                        break;
                    }
                } else if (AsphaltExperimentData.TYPE_SOFTENING_POINT.equals(experimentType)) {
                    // 软化点实验：检查温度和软化温度
                    if (!isValidField(experimentValues, AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE) || 
                        !isValidField(experimentValues, AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE)) {
                        isValid = false;
                        break;
                    }
                } else if (AsphaltExperimentData.TYPE_DUCTILITY.equals(experimentType)) {
                    // 延度实验：检查温度和位移
                    if (!isValidField(experimentValues, "temperature") || 
                        !isValidField(experimentValues, AsphaltExperimentData.Fields.Ductility.DISPLACEMENT)) {
                        isValid = false;
                        break;
                    }
                }
                // 可以根据需要添加其他类型的实验验证
            }
            
            // 更新数据有效性状态
            if (isAllDataValid != isValid) {
                isAllDataValid = isValid;
                // 通知监听器数据有效性已更改
                if (dataValidityChangeListener != null) {
                    dataValidityChangeListener.onDataValidityChanged(isValid);
                }
            }
            
            return isValid;
        }
        
        /**
         * 检查字段是否有效（不为空且不全是空格）
         */
        private boolean isValidField(Map<String, String> data, String fieldKey) {
            String value = data.get(fieldKey);
            return value != null && !value.trim().isEmpty();
        }
    }

    /**
     * 更新实验类型列表，并刷新UI
     * @param newExperimentTypes 新的实验类型列表
     */
    public void updateExperimentTypes(List<String> newExperimentTypes) {
        if (newExperimentTypes == null) {
            return;
        }
        
        // 清除已经完成但不在新列表中的实验数据
        List<String> typesToRemove = new ArrayList<>();
        for (String type : experimentTypes) {
            if (!newExperimentTypes.contains(type)) {
                typesToRemove.add(type);
            }
        }
        
        for (String typeToRemove : typesToRemove) {
            experimentDataMap.remove(typeToRemove);
            deviceInfoMap.remove(typeToRemove);
        }
        Log.d("AsphaltAdapter", "更新实验类型列表: 原列表=" + experimentTypes.size() + "项, 新列表=" + newExperimentTypes.size() + "项");
        // 更新实验类型列表
        this.experimentTypes.clear();
        this.experimentTypes.addAll(newExperimentTypes);
        
        // 刷新UI
        notifyDataSetChanged();
        Log.d("AsphaltAdapter", "实验类型列表更新完成，通知UI更新");
    }
    
    /**
     * 获取当前的实验类型列表
     * @return 实验类型列表
     */
    public List<String> getExperimentTypes() {
        return new ArrayList<>(experimentTypes);
    }
}
