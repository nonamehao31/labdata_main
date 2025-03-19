package com.example.labdata_main.adapter;

import android.content.Context;
import android.graphics.Color;
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
import com.example.labdata_main.calculator.SplittingTestCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.Gravity;

import java.util.ArrayList;
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

    /**
     * 更新试件制备信息（拌合参数、压实方法、设备信息）
     * @param specimenData 包含试件制备信息的Map
     */
    public void updateSpecimenData(Map<String, Object> specimenData) {
        if (specimenData == null) return;
        
        android.util.Log.d("MixtureAdapter", "收到试件数据: " + specimenData.toString());
        
        // 处理拌合参数和压实方法信息
        if (specimenData.containsKey("methodsAndRatios")) {
            try {
                List<Map<String, Object>> methodsData = (List<Map<String, Object>>) specimenData.get("methodsAndRatios");
                if (methodsData != null && !methodsData.isEmpty()) {
                    // 打印接收到的数据以便调试
                    android.util.Log.d("MixtureAdapter", "方法和配比数据: " + methodsData.toString());
                    
                    // 存储每个试件ID对应的拌合参数和压实方法
                    Map<Long, Map<String, Object>> specimenParams = new HashMap<>();
                    
                    for (Map<String, Object> method : methodsData) {
                        if (method.containsKey("id")) {
                            Long specimenId = ((Number) method.get("id")).longValue();
                            specimenParams.put(specimenId, method);
                        }
                    }
                    
                    // 存储到experimentData中
                    for (Long mixRatioId : experimentAssignments.keySet()) {
                        List<String> experiments = experimentAssignments.get(mixRatioId);
                        if (experiments != null) {
                            for (String experiment : experiments) {
                                // 查找对应的试件参数
                                for (Long specimenId : specimenParams.keySet()) {
                                    Map<String, Object> params = specimenParams.get(specimenId);
                                    Map<String, String> existingData = experimentData.getOrDefault(
                                            experiment, new HashMap<>());
                                    
                                    // 添加拌合参数 - 使用下划线命名与后端匹配
                                    if (params.containsKey("mixing_temperature")) {
                                        Object tempObj = params.get("mixing_temperature");
                                        double mixingTemp = tempObj instanceof Number ? ((Number) tempObj).doubleValue() : 0;
                                        existingData.put("mixingTemperature", String.valueOf(mixingTemp));
                                    }
                                    
                                    if (params.containsKey("mixing_speed")) {
                                        Object speedObj = params.get("mixing_speed");
                                        double mixingSpeed = speedObj instanceof Number ? ((Number) speedObj).doubleValue() : 0;
                                        existingData.put("mixingSpeed", String.valueOf(mixingSpeed));
                                    }
                                    
                                    if (params.containsKey("mixing_time")) {
                                        Object timeObj = params.get("mixing_time");
                                        double mixingTime = timeObj instanceof Number ? ((Number) timeObj).doubleValue() : 0;
                                        existingData.put("mixingTime", String.valueOf(mixingTime));
                                    }
                                    
                                    // 添加压实方法
                                    if (params.containsKey("compaction_method")) {
                                        String compactionMethod = (String) params.get("compaction_method");
                                        existingData.put("compactionMethod", compactionMethod);
                                    }
                                    
                                    experimentData.put(experiment, existingData);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MixtureAdapter", "处理方法数据时出错", e);
            }
        }
        
        // 处理拌合设备信息
        if (specimenData.containsKey("mixingEquipment")) {
            try {
                List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) specimenData.get("mixingEquipment");
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    android.util.Log.d("MixtureAdapter", "拌合设备数据: " + equipmentList.toString());
                    
                    for (Map<String, Object> equipment : equipmentList) {
                        if (equipment.containsKey("deviceId") && equipment.containsKey("manufacturer")) {
                            String deviceId = (String) equipment.get("deviceId");
                            String manufacturer = (String) equipment.get("manufacturer");
                            
                            if (deviceId != null) {
                                DeviceInfo deviceInfo = new DeviceInfo();
                                deviceInfo.setDeviceId(deviceId);
                                deviceInfo.setManufacturer(manufacturer != null ? manufacturer : "未知");
                                // 如果没有型号信息，使用设备ID作为型号
                                deviceInfo.setModel(deviceId);
                                deviceInfo.setDeviceType("mixing");
                                
                                deviceData.put(deviceId, deviceInfo);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MixtureAdapter", "处理拌合设备数据时出错", e);
            }
        }
        
        // 处理成型设备信息
        if (specimenData.containsKey("formingEquipment")) {
            try {
                List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) specimenData.get("formingEquipment");
                if (equipmentList != null && !equipmentList.isEmpty()) {
                    android.util.Log.d("MixtureAdapter", "成型设备数据: " + equipmentList.toString());
                    
                    for (Map<String, Object> equipment : equipmentList) {
                        if (equipment.containsKey("deviceId") && equipment.containsKey("manufacturer")) {
                            String deviceId = (String) equipment.get("deviceId");
                            String manufacturer = (String) equipment.get("manufacturer");
                            
                            if (deviceId != null) {
                                DeviceInfo deviceInfo = new DeviceInfo();
                                deviceInfo.setDeviceId(deviceId);
                                deviceInfo.setManufacturer(manufacturer != null ? manufacturer : "未知");
                                // 如果没有型号信息，使用设备ID作为型号
                                deviceInfo.setModel(deviceId);
                                deviceInfo.setDeviceType("forming");
                                
                                deviceData.put(deviceId, deviceInfo);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MixtureAdapter", "处理成型设备数据时出错", e);
            }
        }
        
        // 处理任务指派信息
        if (specimenData.containsKey("taskAssignments")) {
            try {
                List<Map<String, Object>> assignments = (List<Map<String, Object>>) specimenData.get("taskAssignments");
                if (assignments != null && !assignments.isEmpty()) {
                    android.util.Log.d("MixtureAdapter", "任务指派数据: " + assignments.toString());
                    
                    // 处理任务指派信息
                    for (Map<String, Object> assignment : assignments) {
                        String taskId = (String) assignment.get("task_id");
                        String assignedTo = (String) assignment.get("assigned_to");
                        String status = (String) assignment.get("status");
                        String taskAssignment = (String) assignment.get("task_assignment");
                        
                        android.util.Log.d("MixtureAdapter", "任务指派: taskId=" + taskId + 
                                ", assignedTo=" + assignedTo + ", status=" + status + 
                                ", taskAssignment=" + taskAssignment);
                        
                        // 将任务指派信息添加到所有实验数据中
                        for (String experiment : experimentData.keySet()) {
                            Map<String, String> existingData = experimentData.get(experiment);
                            if (existingData != null) {
                                existingData.put("assignedTo", assignedTo != null ? assignedTo : "未指派");
                                existingData.put("status", status != null ? status : "未开始");
                                if (taskAssignment != null && !taskAssignment.isEmpty()) {
                                    existingData.put("taskAssignment", taskAssignment);
                                }
                            }
                        }
                    }
                } else {
                    android.util.Log.w("MixtureAdapter", "未找到任务指派信息");
                }
            } catch (Exception e) {
                android.util.Log.e("MixtureAdapter", "处理任务指派数据时出错", e);
            }
        } else {
            android.util.Log.w("MixtureAdapter", "返回数据中不包含任务指派信息");
        }
        
        // 通知适配器数据已更新
        notifyDataSetChanged();
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
                case "沥青混合料弯曲试验":
                    addMixtureBendingFields(mixRatioId);
                    break;
                case "理论最大相对密度试验":
                    addSingleInputField("理论最大相对密度", "g/cm³", experimentName + "_max_density", mixRatioId);
                    break;
                case "体积密度试验":
                    addSingleInputField("体积密度", "g/cm³", experimentName + "_volume_density", mixRatioId);
                    break;
                case "空隙率试验":
                    addSingleInputField("空隙率", "%", experimentName + "_void_ratio", mixRatioId);
                    break;
                case "飞散试验":
                    addSingleInputField("飞散损失率", "%", experimentName + "_flakiness_ratio", mixRatioId);
                    break;
                case "动稳定度试验":
                    addSingleInputField("动稳定度", "次/mm", experimentName + "_dynamic_stability", mixRatioId);
                    break;
                case "动态模量试验":
                    addDynamicModulusFields(mixRatioId);
                    break;
                case "沥青混合料直接拉伸循环疲劳测黏弹损伤试验":
                    addDirectStretchingFatigueFields(mixRatioId);
                    break;
                case "沥青混合料四点弯曲疲劳寿命试验":
                    addFourPointBendingFields(mixRatioId);
                    break;
                case "沥青混合料单轴压缩试验(圆柱体法)":
                    addSingleAxisCompressionFields(mixRatioId);
                    break;
                case "沥青混合料劈裂试验":
                    addMixSplittingFields(mixRatioId);
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

        private void addMixtureBendingFields(long mixRatioId) {
            String experimentName = "沥青混合料弯曲试验";
            // 添加固定参数
            addSingleInputField("跨径长度L", "mm", experimentName + "_span_length", mixRatioId);

            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("3");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的3个试件
            for (int i = 1; i <= 3; i++) {
                addBendingSpecimen(mixRatioId, experimentName, i);
            }

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addBendingSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });
        }

        private void addBendingSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件参数输入字段
            addSingleInputField("试件宽度b", "mm", experimentName + "_width_" + specimenId, mixRatioId);
            addSingleInputField("试件高度h", "mm", experimentName + "_height_" + specimenId, mixRatioId);
            addSingleInputField("最大荷载Pb", "N", experimentName + "_max_load_" + specimenId, mixRatioId);
            addSingleInputField("跨中挠度d", "mm", experimentName + "_deflection_" + specimenId, mixRatioId);

            // 添加计算结果标题
            TextView resultsTitle = new TextView(itemView.getContext());
            resultsTitle.setText("计算结果");
            resultsTitle.setTextSize(14);
            resultsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            resultsTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(resultsTitle);

            // 添加计算结果显示字段
            TextView flexuralStrengthText = new TextView(itemView.getContext());
            flexuralStrengthText.setText("抗弯拉强度Rb (MPa): ");
            flexuralStrengthText.setTag(experimentName + "_flexural_strength_" + specimenId);
            layoutInputs.addView(flexuralStrengthText);

            TextView maxStrainText = new TextView(itemView.getContext());
            maxStrainText.setText("最大弯拉应变εb (με): ");
            maxStrainText.setTag(experimentName + "_max_strain_" + specimenId);
            layoutInputs.addView(maxStrainText);

            TextView stiffnessModulusText = new TextView(itemView.getContext());
            stiffnessModulusText.setText("弯曲劲度模量Sb (MPa): ");
            stiffnessModulusText.setTag(experimentName + "_stiffness_modulus_" + specimenId);
            layoutInputs.addView(stiffnessModulusText);

            // 添加分隔线
            View divider = new View(itemView.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2));
            divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2);
            dividerParams.setMargins(0, 16, 0, 16);
            divider.setLayoutParams(dividerParams);
            layoutInputs.addView(divider);

            // 设置文本变化监听器，实时计算结果
            setupBendingCalculation(mixRatioId, specimenId, experimentName, flexuralStrengthText, maxStrainText, stiffnessModulusText);
        }

        private void setupBendingCalculation(long mixRatioId, int specimenId, String experimentName, 
                                           TextView flexuralStrengthText, TextView maxStrainText, TextView stiffnessModulusText) {
            // 获取数据键
            String spanLengthKey = experimentName + "_span_length";
            String widthKey = experimentName + "_width_" + specimenId;
            String heightKey = experimentName + "_height_" + specimenId;
            String maxLoadKey = experimentName + "_max_load_" + specimenId;
            String deflectionKey = experimentName + "_deflection_" + specimenId;
            String flexuralStrengthKey = experimentName + "_flexural_strength_" + specimenId;
            String maxStrainKey = experimentName + "_max_strain_" + specimenId;
            String stiffnessModulusKey = experimentName + "_stiffness_modulus_" + specimenId;

            // 创建计算结果更新器
            Runnable updateCalculation = () -> {
                // 获取该配比的数据Map
                Map<String, String> mixRatioData = experimentData.computeIfAbsent(
                        String.valueOf(mixRatioId),
                        k -> new HashMap<>()
                );
                if (mixRatioData == null) {
                    return;
                }

                // 获取输入值
                String spanLengthStr = mixRatioData.get(spanLengthKey);
                String widthStr = mixRatioData.get(widthKey);
                String heightStr = mixRatioData.get(heightKey);
                String maxLoadStr = mixRatioData.get(maxLoadKey);
                String deflectionStr = mixRatioData.get(deflectionKey);

                // 计算结果
                try {
                    // 检查所有输入是否都有效
                    if (spanLengthStr != null && !spanLengthStr.isEmpty() &&
                        widthStr != null && !widthStr.isEmpty() &&
                        heightStr != null && !heightStr.isEmpty() &&
                        maxLoadStr != null && !maxLoadStr.isEmpty() &&
                        deflectionStr != null && !deflectionStr.isEmpty()) {

                        double spanLength = Double.parseDouble(spanLengthStr);
                        double width = Double.parseDouble(widthStr);
                        double height = Double.parseDouble(heightStr);
                        double maxLoad = Double.parseDouble(maxLoadStr);
                        double deflection = Double.parseDouble(deflectionStr);

                        // 计算抗弯拉强度 Rb = 3LPb/(2bh^2)
                        double flexuralStrength = (3 * spanLength * maxLoad) / (2 * width * height * height);
                        // 转换为MPa
                        flexuralStrength = flexuralStrength / 1000;

                        // 计算最大弯拉应变 εb = 6hd/L^2
                        double maxStrain = (6 * height * deflection) / (spanLength * spanLength);
                        // 转换为με (微应变)
                        maxStrain = maxStrain * 1000000;

                        // 计算弯曲劲度模量 Sb = Rb/εb
                        double stiffnessModulus = flexuralStrength / (maxStrain / 1000000);

                        // 更新结果显示
                        flexuralStrengthText.setText("抗弯拉强度Rb (MPa): " + String.format("%.2f", flexuralStrength));
                        maxStrainText.setText("最大弯拉应变εb (με): " + String.format("%.2f", maxStrain));
                        stiffnessModulusText.setText("弯曲劲度模量Sb (MPa): " + String.format("%.2f", stiffnessModulus));

                        // 保存计算结果到数据中
                        mixRatioData.put(flexuralStrengthKey, String.valueOf(flexuralStrength));
                        mixRatioData.put(maxStrainKey, String.valueOf(maxStrain));
                        mixRatioData.put(stiffnessModulusKey, String.valueOf(stiffnessModulus));
                    }
                } catch (NumberFormatException e) {
                    // 输入无效，清空结果
                    flexuralStrengthText.setText("抗弯拉强度Rb (MPa): ");
                    maxStrainText.setText("最大弯拉应变εb (με): ");
                    stiffnessModulusText.setText("弯曲劲度模量Sb (MPa): ");

                    // 从数据中移除结果
                    mixRatioData.remove(flexuralStrengthKey);
                    mixRatioData.remove(maxStrainKey);
                    mixRatioData.remove(stiffnessModulusKey);
                }
            };

            // 为所有相关字段添加数据变化监听
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // 触发计算更新
                    updateCalculation.run();
                }
            };

            // 为每个输入字段添加监听器
            for (int i = 0; i < layoutInputs.getChildCount(); i++) {
                View child = layoutInputs.getChildAt(i);
                if (child instanceof TextInputLayout) {
                    View editText = ((TextInputLayout) child).getEditText();
                    if (editText != null) {
                        String hint = ((TextInputLayout) child).getHint().toString();
                        if (hint.contains("跨径长度L") || 
                            (hint.contains("试件宽度b") && i > layoutInputs.getChildCount() - 20) || 
                            (hint.contains("试件高度h") && i > layoutInputs.getChildCount() - 20) || 
                            (hint.contains("最大荷载Pb") && i > layoutInputs.getChildCount() - 20) || 
                            (hint.contains("跨中挠度d") && i > layoutInputs.getChildCount() - 20)) {
                            ((TextInputEditText) editText).addTextChangedListener(textWatcher);
                        }
                    }
                }
            }
        }

        private void addMixSplittingFields(long mixRatioId) {
            String experimentName = "沥青混合料劈裂试验";

            // 创建试件基本信息区域
            TextView basicInfoTitle = new TextView(itemView.getContext());
            basicInfoTitle.setText("试件基本信息");
            basicInfoTitle.setTextSize(18);
            basicInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            basicInfoTitle.setPadding(0, 16, 0, 16);
            layoutInputs.addView(basicInfoTitle);

            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("1");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的试件
            addMixSplittingSpecimen(mixRatioId, experimentName, 1);

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addMixSplittingSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });

            // 添加泊松比参考表
            addPoissonRatioReferenceTable();
        }

        private void addMixSplittingSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件尺寸输入字段
            TextView dimensionsTitle = new TextView(itemView.getContext());
            dimensionsTitle.setText("试件尺寸");
            dimensionsTitle.setTextSize(14);
            dimensionsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            dimensionsTitle.setPadding(0, 8, 0, 8);
            layoutInputs.addView(dimensionsTitle);

            // 添加直径输入字段
            TextInputLayout diameterLayout = new TextInputLayout(itemView.getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            diameterLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            diameterLayout.setHint("直径 (mm)");
            diameterLayout.setHelperText("精确至0.1mm");

            TextInputEditText diameterInput = new TextInputEditText(itemView.getContext());
            diameterInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            String diameterKey = experimentName + "_diameter_" + specimenId;
            setupDataInput(diameterInput, diameterKey, mixRatioId);
            diameterLayout.addView(diameterInput);
            layoutInputs.addView(diameterLayout);

            // 添加高度输入字段
            TextInputLayout heightLayout = new TextInputLayout(itemView.getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            heightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            heightLayout.setHint("高度 (mm)");
            heightLayout.setHelperText("精确至0.1mm");

            TextInputEditText heightInput = new TextInputEditText(itemView.getContext());
            heightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            String heightKey = experimentName + "_height_" + specimenId;
            setupDataInput(heightInput, heightKey, mixRatioId);
            heightLayout.addView(heightInput);
            layoutInputs.addView(heightLayout);

            // 添加抗拉强度数据表格
            TextView strengthTitle = new TextView(itemView.getContext());
            strengthTitle.setText("抗拉强度数据");
            strengthTitle.setTextSize(14);
            strengthTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            strengthTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(strengthTitle);

            // 添加抗拉强度数据输入
            addStrengthDataInputs(mixRatioId, experimentName, specimenId);

            // 添加水平变形数据
            TextView deformationTitle = new TextView(itemView.getContext());
            deformationTitle.setText("水平应变变形数据");
            deformationTitle.setTextSize(14);
            deformationTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            deformationTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(deformationTitle);

            // 添加水平变形数据输入
            addDeformationDataInputs(mixRatioId, experimentName, specimenId);

            // 添加计算结果区域
            TextView resultsTitle = new TextView(itemView.getContext());
            resultsTitle.setText("计算结果");
            resultsTitle.setTextSize(14);
            resultsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            resultsTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(resultsTitle);

            // 添加计算结果输入字段
            addCalculationResults(mixRatioId, experimentName, specimenId);

            // 设置自动计算
            setupSplittingCalculation(mixRatioId, experimentName, specimenId);

            // 添加分隔线
            View divider = new View(itemView.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2));
            divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2);
            dividerParams.setMargins(0, 24, 0, 24);
            divider.setLayoutParams(dividerParams);
            layoutInputs.addView(divider);
        }

        private void setupSplittingCalculation(long mixRatioId, String experimentName, int specimenId) {
            // Define keys for all input fields
            String diameterKey = experimentName + "_diameter_" + specimenId;
            String heightKey = experimentName + "_height_" + specimenId;
            String temperatureKey = experimentName + "_temperature";
            String poissonRatioKey = experimentName + "_poisson_ratio_" + specimenId;

            // Strength data keys
            String p1Key = experimentName + "_strength_" + specimenId + "_p1";
            String p2Key = experimentName + "_strength_" + specimenId + "_p2";
            String p3Key = experimentName + "_strength_" + specimenId + "_p3";
            String pAvgKey = experimentName + "_strength_" + specimenId + "_avg";

            // Deformation data keys
            String x1Key = experimentName + "_deformation_" + specimenId + "_x1";
            String x2Key = experimentName + "_deformation_" + specimenId + "_x2";
            String x3Key = experimentName + "_deformation_" + specimenId + "_x3";
            String xAvgKey = experimentName + "_deformation_" + specimenId + "_avg";

            // Result keys
            String rtKey = experimentName + "_rt_" + specimenId;
            String strainKey = experimentName + "_strain_" + specimenId;
            String stKey = experimentName + "_st_" + specimenId;

            // Create calculation update function
            Runnable updateCalculation = () -> {
                // Get the data map for this mix ratio
                Map<String, String> mixRatioData = experimentData.computeIfAbsent(
                        String.valueOf(mixRatioId),
                        k -> new HashMap<>()
                );
                if (mixRatioData == null) {
                    return;
                }

                try {
                    // Get temperature and determine Poisson's ratio if not manually entered
                    String temperatureStr = mixRatioData.get(temperatureKey);
                    String poissonRatioStr = mixRatioData.get(poissonRatioKey);

                    // If Poisson's ratio is not entered but temperature is available, determine from table
                    if ((poissonRatioStr == null || poissonRatioStr.isEmpty()) && temperatureStr != null && !temperatureStr.isEmpty()) {
                        double temperature = Double.parseDouble(temperatureStr);
                        double poissonRatio = SplittingTestCalculator.getPoissonRatioFromTemperature(temperature);

                        // Update Poisson's ratio in the data map and UI
                        mixRatioData.put(poissonRatioKey, String.valueOf(poissonRatio));

                        // Find and update the Poisson ratio input field
                        for (int i = 0; i < layoutInputs.getChildCount(); i++) {
                            View child = layoutInputs.getChildAt(i);
                            if (child instanceof TextInputLayout) {
                                TextInputLayout layout = (TextInputLayout) child;
                                if (layout.getHint() != null && layout.getHint().toString().contains("泊松比")) {
                                    TextInputEditText editText = (TextInputEditText) layout.getEditText();
                                    if (editText != null && editText.getText().toString().isEmpty()) {
                                        editText.setText(String.valueOf(poissonRatio));
                                        editText.setEnabled(false); // Add this line to disable the input field
                                    }
                                }
                            }
                        }
                    }

                    // Calculate P average
                    String p1Str = mixRatioData.get(p1Key);
                    String p2Str = mixRatioData.get(p2Key);
                    String p3Str = mixRatioData.get(p3Key);

                    double pSum = 0;
                    int pCount = 0;

                    if (p1Str != null && !p1Str.isEmpty()) {
                        pSum += Double.parseDouble(p1Str);
                        pCount++;
                    }
                    if (p2Str != null && !p2Str.isEmpty()) {
                        pSum += Double.parseDouble(p2Str);
                        pCount++;
                    }
                    if (p3Str != null && !p3Str.isEmpty()) {
                        pSum += Double.parseDouble(p3Str);
                        pCount++;
                    }

                    double pAvg = (pCount > 0) ? (pSum / pCount) : 0;
                    mixRatioData.put(pAvgKey, String.format("%.2f", pAvg));

                    // Calculate X average
                    String x1Str = mixRatioData.get(x1Key);
                    String x2Str = mixRatioData.get(x2Key);
                    String x3Str = mixRatioData.get(x3Key);

                    double xSum = 0;
                    int xCount = 0;

                    if (x1Str != null && !x1Str.isEmpty()) {
                        xSum += Double.parseDouble(x1Str);
                        xCount++;
                    }
                    if (x2Str != null && !x2Str.isEmpty()) {
                        xSum += Double.parseDouble(x2Str);
                        xCount++;
                    }
                    if (x3Str != null && !x3Str.isEmpty()) {
                        xSum += Double.parseDouble(x3Str);
                        xCount++;
                    }

                    double xAvg = (xCount > 0) ? (xSum / xCount) : 0;
                    mixRatioData.put(xAvgKey, String.format("%.2f", xAvg));

                    // Update average fields in UI
                    for (int i = 0; i < layoutInputs.getChildCount(); i++) {
                        View child = layoutInputs.getChildAt(i);
                        if (child instanceof LinearLayout) {
                            LinearLayout layout = (LinearLayout) child;
                            for (int j = 0; j < layout.getChildCount(); j++) {
                                View innerChild = layout.getChildAt(j);
                                if (innerChild instanceof TextInputEditText) {
                                    TextInputEditText editText = (TextInputEditText) innerChild;
                                    if (editText.getHint() != null && editText.getHint().toString().equals("计算获得")) {
                                        if (layout.getChildAt(0) instanceof TextView) {
                                            TextView label = (TextView) layout.getChildAt(0);
                                            if (label.getText().toString().contains("P平均值")) {
                                                editText.setText(String.format("%.2f", pAvg));
                                                editText.setEnabled(false);
                                            } else if (label.getText().toString().contains("X平均值")) {
                                                editText.setText(String.format("%.2f", xAvg));
                                                editText.setEnabled(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Calculate RT, εT, and ST if all necessary data is available
                    String diameterStr = mixRatioData.get(diameterKey);
                    String heightStr = mixRatioData.get(heightKey);
                    poissonRatioStr = mixRatioData.get(poissonRatioKey); // Get updated value

                    if (diameterStr != null && !diameterStr.isEmpty() &&
                            heightStr != null && !heightStr.isEmpty() &&
                            poissonRatioStr != null && !poissonRatioStr.isEmpty() &&
                            pAvg > 0) {

                        double diameter = Double.parseDouble(diameterStr);
                        double height = Double.parseDouble(heightStr);
                        double poissonRatio = Double.parseDouble(poissonRatioStr);

                        // Convert pAvg from KN to N
                        double pN = pAvg * 1000;

                        // Use the calculator to calculate splitting tensile strength
                        double rt = SplittingTestCalculator.calculateSplittingTensileStrength(pN, height, diameter);

                        // Use the calculator to calculate horizontal deformation if needed
                        double xt = xAvg;

                        // Use the calculator to calculate failure tensile strain
                        double strainT = SplittingTestCalculator.calculateFailureTensileStrain(xt, poissonRatio);

                        // Use the calculator to calculate stiffness modulus
                        double st = SplittingTestCalculator.calculateStiffnessModulus(pN, height, xt, poissonRatio);

                        // Update results in data map
                        mixRatioData.put(rtKey, String.format("%.4f", rt));
                        mixRatioData.put(strainKey, String.format("%.6f", strainT));
                        mixRatioData.put(stKey, String.format("%.2f", st));

                        // Update UI fields
                        for (int i = 0; i < layoutInputs.getChildCount(); i++) {
                            View child = layoutInputs.getChildAt(i);
                            if (child instanceof TextInputLayout) {
                                TextInputLayout layout = (TextInputLayout) child;
                                TextInputEditText editText = (TextInputEditText) layout.getEditText();
                                if (editText != null) {
                                    if (layout.getHint() != null) {
                                        String hint = layout.getHint().toString();
                                        if (hint.contains("劈裂抗拉强度")) {
                                            editText.setText(String.format("%.4f", rt));
                                            editText.setEnabled(false);
                                        } else if (hint.contains("破坏拉伸应变")) {
                                            editText.setText(String.format("%.6f", strainT));
                                            editText.setEnabled(false);
                                        } else if (hint.contains("破坏韧度模量")) {
                                            editText.setText(String.format("%.2f", st));
                                            editText.setEnabled(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid input
                }
            };

            // Create TextWatcher for all input fields
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    updateCalculation.run();
                }
            };

            // Add TextWatcher to all relevant input fields
            for (int i = 0; i < layoutInputs.getChildCount(); i++) {
                View child = layoutInputs.getChildAt(i);
                if (child instanceof TextInputLayout) {
                    TextInputLayout layout = (TextInputLayout) child;
                    TextInputEditText editText = (TextInputEditText) layout.getEditText();
                    if (editText != null) {
                        if (layout.getHint() != null) {
                            String hint = layout.getHint().toString();
                            if (hint.contains("直径") || hint.contains("高度") ||
                                    hint.contains("泊松比") || hint.contains("测试温度")) {
                                editText.addTextChangedListener(textWatcher);
                            }
                        }
                    }
                } else if (child instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) child;
                    for (int j = 0; j < layout.getChildCount(); j++) {
                        View innerChild = layout.getChildAt(j);
                        if (innerChild instanceof TextInputEditText) {
                            TextInputEditText editText = (TextInputEditText) innerChild;
                            String tag = editText.getTag() != null ? editText.getTag().toString() : "";
                            if (tag.contains("_strength_") || tag.contains("_deformation_")) {
                                editText.addTextChangedListener(textWatcher);
                            }
                        }
                    }
                }
            }

            // Run initial calculation
            updateCalculation.run();
        }

        private void addStrengthDataInputs(long mixRatioId, String experimentName, int specimenId) {
            // 创建P值输入区域
            LinearLayout pValuesContainer = new LinearLayout(itemView.getContext());
            pValuesContainer.setOrientation(LinearLayout.VERTICAL);
            pValuesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // 添加P1-P3输入字段
            for (int i = 1; i <= 3; i++) {
                TextInputLayout pLayout = new TextInputLayout(itemView.getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                pLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                pLayout.setHint("P" + i + " (KN)");
                pLayout.setHelperText("设备直接读取");

                TextInputEditText pInput = new TextInputEditText(itemView.getContext());
                pInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                String pKey = experimentName + "_strength_" + specimenId + "_p" + i;
                setupDataInput(pInput, pKey, mixRatioId);
                pLayout.addView(pInput);
                pValuesContainer.addView(pLayout);
            }

            // 添加平均值计算区域
            LinearLayout avgContainer = new LinearLayout(itemView.getContext());
            avgContainer.setOrientation(LinearLayout.HORIZONTAL);
            avgContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgContainer.setPadding(0, 16, 0, 8);

            TextView avgLabel = new TextView(itemView.getContext());
            avgLabel.setText("P平均值 (KN):");
            avgLabel.setTextSize(14);
            avgLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            avgLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgLabel.setPadding(0, 0, 16, 0);
            avgContainer.addView(avgLabel);

            TextInputEditText avgInput = new TextInputEditText(itemView.getContext());
            avgInput.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            avgInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            avgInput.setHint("计算获得");
            String avgKey = experimentName + "_strength_" + specimenId + "_avg";
            setupDataInput(avgInput, avgKey, mixRatioId);
            avgContainer.addView(avgInput);

            pValuesContainer.addView(avgContainer);
            layoutInputs.addView(pValuesContainer);
        }

        private void addDeformationDataInputs(long mixRatioId, String experimentName, int specimenId) {
            // 创建水平变形输入区域
            LinearLayout deformationContainer = new LinearLayout(itemView.getContext());
            deformationContainer.setOrientation(LinearLayout.VERTICAL);
            deformationContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // 添加X1-X3输入字段
            for (int i = 1; i <= 3; i++) {
                TextInputLayout xLayout = new TextInputLayout(itemView.getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                xLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                xLayout.setHint("X" + i + " (水平应变变形) (mm)");
                xLayout.setHelperText("设备直接读取");

                TextInputEditText xInput = new TextInputEditText(itemView.getContext());
                xInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                String xKey = experimentName + "_deformation_" + specimenId + "_x" + i;
                setupDataInput(xInput, xKey, mixRatioId);
                xLayout.addView(xInput);
                deformationContainer.addView(xLayout);
            }

            // 添加平均值计算区域
            LinearLayout avgContainer = new LinearLayout(itemView.getContext());
            avgContainer.setOrientation(LinearLayout.HORIZONTAL);
            avgContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgContainer.setPadding(0, 16, 0, 8);

            TextView avgLabel = new TextView(itemView.getContext());
            avgLabel.setText("X平均值 (mm):");
            avgLabel.setTextSize(14);
            avgLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            avgLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgLabel.setPadding(0, 0, 16, 0);
            avgContainer.addView(avgLabel);

            TextInputEditText avgInput = new TextInputEditText(itemView.getContext());
            avgInput.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            avgInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            avgInput.setHint("计算获得");
            String avgKey = experimentName + "_deformation_" + specimenId + "_avg";
            setupDataInput(avgInput, avgKey, mixRatioId);
            avgContainer.addView(avgInput);

            deformationContainer.addView(avgContainer);
            layoutInputs.addView(deformationContainer);
        }

        private void addCalculationResults(long mixRatioId, String experimentName, int specimenId) {
            // 创建计算结果输入区域
            LinearLayout resultsContainer = new LinearLayout(itemView.getContext());
            resultsContainer.setOrientation(LinearLayout.VERTICAL);
            resultsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // 添加标题
            TextView resultsTitle = new TextView(itemView.getContext());
            resultsTitle.setText("计算结果");
            resultsTitle.setTextSize(16);
            resultsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            resultsTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            resultsTitle.setPadding(0, 16, 0, 8);
            resultsContainer.addView(resultsTitle);

            // 添加泊松比显示
            LinearLayout poissonContainer = new LinearLayout(itemView.getContext());
            poissonContainer.setOrientation(LinearLayout.HORIZONTAL);
            poissonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            poissonContainer.setPadding(0, 8, 0, 8);

            TextView poissonLabel = new TextView(itemView.getContext());
            poissonLabel.setText("泊松比 μ:");
            poissonLabel.setTextSize(14);
            poissonLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            poissonLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            poissonContainer.addView(poissonLabel);

            TextView poissonValue = new TextView(itemView.getContext());
            poissonValue.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            poissonValue.setTextSize(14);
            poissonValue.setId(View.generateViewId());
            String poissonKey = experimentName + "_poisson_ratio_" + specimenId;
            // 设置初始值
            Map<String, String> mixRatioData = experimentData.computeIfAbsent(
                    String.valueOf(mixRatioId),
                    k -> new HashMap<>()
            );
            String poissonRatioStr = mixRatioData.get(poissonKey);
            if (poissonRatioStr != null && !poissonRatioStr.isEmpty()) {
                poissonValue.setText(poissonRatioStr);
            }
            poissonContainer.addView(poissonValue);
            resultsContainer.addView(poissonContainer);

            // 添加参考表提示
            TextView referenceText = new TextView(itemView.getContext());
            referenceText.setText("参考表T 0716");
            referenceText.setTextSize(12);
            referenceText.setTextColor(Color.GRAY);
            referenceText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            referenceText.setPadding(0, 0, 0, 16);
            resultsContainer.addView(referenceText);

            // 添加劈裂抗拉强度显示
            LinearLayout rtContainer = new LinearLayout(itemView.getContext());
            rtContainer.setOrientation(LinearLayout.HORIZONTAL);
            rtContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rtContainer.setPadding(0, 8, 0, 8);

            TextView rtLabel = new TextView(itemView.getContext());
            rtLabel.setText("劈裂抗拉强度 RT (MPa):");
            rtLabel.setTextSize(14);
            rtLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            rtLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            rtContainer.addView(rtLabel);

            TextView rtValue = new TextView(itemView.getContext());
            rtValue.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            rtValue.setTextSize(14);
            rtValue.setId(View.generateViewId());
            String rtKey = experimentName + "_rt_" + specimenId;
            // 设置初始值
            String rtStr = mixRatioData.get(rtKey);
            if (rtStr != null && !rtStr.isEmpty()) {
                rtValue.setText(rtStr);
            }
            rtContainer.addView(rtValue);
            resultsContainer.addView(rtContainer);

            // 添加公式提示
            TextView rtFormulaText = new TextView(itemView.getContext());
            rtFormulaText.setText("根据公式T 0716-1或T 0716-2计算");
            rtFormulaText.setTextSize(12);
            rtFormulaText.setTextColor(Color.GRAY);
            rtFormulaText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rtFormulaText.setPadding(0, 0, 0, 16);
            resultsContainer.addView(rtFormulaText);

            // 添加破坏拉伸应变显示
            LinearLayout strainContainer = new LinearLayout(itemView.getContext());
            strainContainer.setOrientation(LinearLayout.HORIZONTAL);
            strainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            strainContainer.setPadding(0, 8, 0, 8);

            TextView strainLabel = new TextView(itemView.getContext());
            strainLabel.setText("破坏拉伸应变 εT:");
            strainLabel.setTextSize(14);
            strainLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            strainLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            strainContainer.addView(strainLabel);

            TextView strainValue = new TextView(itemView.getContext());
            strainValue.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            strainValue.setTextSize(14);
            strainValue.setId(View.generateViewId());
            String strainKey = experimentName + "_strain_" + specimenId;
            // 设置初始值
            String strainStr = mixRatioData.get(strainKey);
            if (strainStr != null && !strainStr.isEmpty()) {
                strainValue.setText(strainStr);
            }
            strainContainer.addView(strainValue);
            resultsContainer.addView(strainContainer);

            // 添加公式提示
            TextView strainFormulaText = new TextView(itemView.getContext());
            strainFormulaText.setText("根据公式T 0716-4计算");
            strainFormulaText.setTextSize(12);
            strainFormulaText.setTextColor(Color.GRAY);
            strainFormulaText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            strainFormulaText.setPadding(0, 0, 0, 16);
            resultsContainer.addView(strainFormulaText);

            // 添加破坏韧度模量显示
            LinearLayout stContainer = new LinearLayout(itemView.getContext());
            stContainer.setOrientation(LinearLayout.HORIZONTAL);
            stContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            stContainer.setPadding(0, 8, 0, 8);

            TextView stLabel = new TextView(itemView.getContext());
            stLabel.setText("破坏韧度模量 ST (MPa):");
            stLabel.setTextSize(14);
            stLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            stLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            stContainer.addView(stLabel);

            TextView stValue = new TextView(itemView.getContext());
            stValue.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            stValue.setTextSize(14);
            stValue.setId(View.generateViewId());
            String stKey = experimentName + "_st_" + specimenId;
            // 设置初始值
            String stStr = mixRatioData.get(stKey);
            if (stStr != null && !stStr.isEmpty()) {
                stValue.setText(stStr);
            }
            stContainer.addView(stValue);
            resultsContainer.addView(stContainer);

            // 添加公式提示
            TextView stFormulaText = new TextView(itemView.getContext());
            stFormulaText.setText("根据公式T 0716-5计算");
            stFormulaText.setTextSize(12);
            stFormulaText.setTextColor(Color.GRAY);
            stFormulaText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            stFormulaText.setPadding(0, 0, 0, 16);
            resultsContainer.addView(stFormulaText);

            // 保存TextView的引用，以便在setupSplittingCalculation中更新
            mixRatioData.put(poissonKey + "_view_id", String.valueOf(poissonValue.getId()));
            mixRatioData.put(rtKey + "_view_id", String.valueOf(rtValue.getId()));
            mixRatioData.put(strainKey + "_view_id", String.valueOf(strainValue.getId()));
            mixRatioData.put(stKey + "_view_id", String.valueOf(stValue.getId()));

            layoutInputs.addView(resultsContainer);
        }

        private void addPoissonRatioReferenceTable() {
            // 创建泊松比参考表标题
            TextView tableTitle = new TextView(itemView.getContext());
            tableTitle.setText("表 T 0716 泊松比参考值");
            tableTitle.setTextSize(16);
            tableTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tableTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(tableTitle);

            // 创建表格容器
            TableLayout tableLayout = new TableLayout(itemView.getContext());
            tableLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tableLayout.setStretchAllColumns(true);

            // 添加表头行
            TableRow headerRow = new TableRow(itemView.getContext());
            String[] headers = {"试验温度(°C)", "≤10", "15", "20", "25", "30"};

            for (String header : headers) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(header);
                headerText.setTextSize(14);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                headerText.setPadding(8, 8, 8, 8);
                headerText.setGravity(Gravity.CENTER);
                headerText.setBackgroundResource(R.drawable.border_background);
                headerRow.addView(headerText);
            }
            tableLayout.addView(headerRow);

            // 添加数据行
            TableRow dataRow = new TableRow(itemView.getContext());
            String[] values = {"泊松比 μ 值", "0.25", "0.30", "0.35", "0.40", "0.45"};

            for (String value : values) {
                TextView valueText = new TextView(itemView.getContext());
                valueText.setText(value);
                valueText.setTextSize(14);
                valueText.setPadding(8, 8, 8, 8);
                valueText.setGravity(Gravity.CENTER);
                valueText.setBackgroundResource(R.drawable.border_background);
                dataRow.addView(valueText);
            }
            tableLayout.addView(dataRow);

            layoutInputs.addView(tableLayout);
        }

        private void addDynamicModulusFields(long mixRatioId) {
            String experimentName = "动态模量试验";
            
            // 创建试件基本信息区域
            TextView basicInfoTitle = new TextView(itemView.getContext());
            basicInfoTitle.setText("试件基本信息");
            basicInfoTitle.setTextSize(18);
            basicInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            basicInfoTitle.setPadding(0, 16, 0, 16);
            layoutInputs.addView(basicInfoTitle);
            
            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("1");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的试件
            addDynamicModulusSpecimen(mixRatioId, experimentName, 1);

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addDynamicModulusSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });
        }

        private void addDynamicModulusSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件尺寸输入字段
            addSingleInputField("试件直径", "mm", experimentName + "_diameter_" + specimenId, mixRatioId);
            addSingleInputField("试件高度", "mm", experimentName + "_height_" + specimenId, mixRatioId);

            // 为每个温度创建数据输入区域
            String[] temperatures = {"-10", "4.4", "21.1", "37.8", "54"};
            
            for (String temp : temperatures) {
                // 添加温度标题
                TextView tempTitle = new TextView(itemView.getContext());
                tempTitle.setText("温度: " + temp + "°C");
                tempTitle.setTextSize(16);
                tempTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tempTitle.setPadding(0, 16, 0, 8);
                layoutInputs.addView(tempTitle);
                
                // 添加表格标题
                addDynamicModulusTableHeader();
                
                // 添加表格数据行
                String[] frequencies = {"25", "10", "5", "1", "0.5", "0.1"};
                String[] cycles = {"200", "200", "100", "20", "15", "15"};
                
                for (int i = 0; i < frequencies.length; i++) {
                    addDynamicModulusDataRow(
                        mixRatioId, 
                        experimentName, 
                        specimenId, 
                        temp, 
                        frequencies[i], 
                        cycles[i]
                    );
                }
                
                // 添加分隔线
                View divider = new View(itemView.getContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2));
                divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2);
                dividerParams.setMargins(0, 16, 0, 16);
                divider.setLayoutParams(dividerParams);
                layoutInputs.addView(divider);
            }
        }
        
        private void addDynamicModulusTableHeader() {
            // 创建表格标题行
            LinearLayout headerRow = new LinearLayout(itemView.getContext());
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setPadding(0, 8, 0, 8);
            
            // 添加表头列
            String[] headers = {"频率(Hz)", "循环次数", "动态模量(MPa)", "相位角(°)", "温度(°C)", 
                               "轴向应力(kPa)", "轴向应变(με)", "永久轴向应变(με)"};
            int[] weights = {1, 1, 1, 1, 1, 1, 1, 1};
            
            for (int i = 0; i < headers.length; i++) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(headers[i]);
                headerText.setTextSize(12);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weights[i]
                );
                params.setMargins(4, 0, 4, 0);
                headerText.setLayoutParams(params);
                headerRow.addView(headerText);
            }
            
            layoutInputs.addView(headerRow);
        }
        
        private void addDynamicModulusDataRow(long mixRatioId, String experimentName, 
                                             int specimenId, String temperature, 
                                             String frequency, String cycles) {
            // 创建数据行
            LinearLayout dataRow = new LinearLayout(itemView.getContext());
            dataRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dataRow.setOrientation(LinearLayout.HORIZONTAL);
            dataRow.setPadding(0, 4, 0, 4);
            
            // 频率列 - 只显示不可编辑
            TextView freqText = new TextView(itemView.getContext());
            freqText.setText(frequency);
            freqText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            dataRow.addView(freqText);
            
            // 循环次数列 - 只显示不可编辑
            TextView cyclesText = new TextView(itemView.getContext());
            cyclesText.setText(cycles);
            cyclesText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            dataRow.addView(cyclesText);
            
            // 动态模量列 - 可编辑
            TextInputEditText modulusInput = createDataInput(dataRow);
            String modulusKey = experimentName + "_" + specimenId + "_" + temperature + "_" + frequency + "_modulus";
            setupDataInput(modulusInput, modulusKey, mixRatioId);
            
            // 相位角列 - 可编辑
            TextInputEditText phaseInput = createDataInput(dataRow);
            String phaseKey = experimentName + "_" + specimenId + "_" + temperature + "_" + frequency + "_phase";
            setupDataInput(phaseInput, phaseKey, mixRatioId);
            
            // 温度列 - 只显示不可编辑
            TextView tempText = new TextView(itemView.getContext());
            tempText.setText(temperature);
            tempText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            dataRow.addView(tempText);
            
            // 轴向应力列 - 可编辑
            TextInputEditText stressInput = createDataInput(dataRow);
            String stressKey = experimentName + "_" + specimenId + "_" + temperature + "_" + frequency + "_stress";
            setupDataInput(stressInput, stressKey, mixRatioId);
            
            // 轴向应变列 - 可编辑
            TextInputEditText strainInput = createDataInput(dataRow);
            String strainKey = experimentName + "_" + specimenId + "_" + temperature + "_" + frequency + "_strain";
            setupDataInput(strainInput, strainKey, mixRatioId);
            
            // 永久轴向应变列 - 可编辑
            TextInputEditText permStrainInput = createDataInput(dataRow);
            String permStrainKey = experimentName + "_" + specimenId + "_" + temperature + "_" + frequency + "_perm_strain";
            setupDataInput(permStrainInput, permStrainKey, mixRatioId);
            
            layoutInputs.addView(dataRow);
        }
        
        private TextInputEditText createDataInput(LinearLayout parent) {
            TextInputEditText editText = new TextInputEditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setHint("设备给出");
            editText.setTextSize(12);
            parent.addView(editText);
            return editText;
        }
        
        private void setupDataInput(TextInputEditText editText, String dataKey, long mixRatioId) {
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

        private void addDirectStretchingFatigueFields(long mixRatioId) {
            String experimentName = "沥青混合料直接拉伸循环疲劳测黏弹损伤试验";
            
            // 创建试件基本信息区域
            TextView basicInfoTitle = new TextView(itemView.getContext());
            basicInfoTitle.setText("试件基本信息");
            basicInfoTitle.setTextSize(18);
            basicInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            basicInfoTitle.setPadding(0, 16, 0, 16);
            layoutInputs.addView(basicInfoTitle);
            
            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("1");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的试件
            addDirectStretchingFatigueSpecimen(mixRatioId, experimentName, 1);

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addDirectStretchingFatigueSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });
        }

        private void addDirectStretchingFatigueSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件尺寸输入字段
            addSingleInputField("试件直径", "mm", experimentName + "_diameter_" + specimenId, mixRatioId);
            addSingleInputField("试件高度", "mm", experimentName + "_height_" + specimenId, mixRatioId);
            
            // 添加测试温度输入字段
            addSingleInputField("测试温度", "°C", experimentName + "_temperature_" + specimenId, mixRatioId);

            // 添加动态模量阶段标题
            TextView dynamicModulusTitle = new TextView(itemView.getContext());
            dynamicModulusTitle.setText("动态模量阶段");
            dynamicModulusTitle.setTextSize(16);
            dynamicModulusTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            dynamicModulusTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(dynamicModulusTitle);

            // 添加动态模量阶段表格
            addDirectStretchingDynamicModulusTable(mixRatioId, experimentName, specimenId);

            // 添加疲劳测试阶段标题
            TextView fatigueTestTitle = new TextView(itemView.getContext());
            fatigueTestTitle.setText("疲劳测试阶段");
            fatigueTestTitle.setTextSize(16);
            fatigueTestTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            fatigueTestTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(fatigueTestTitle);

            // 添加疲劳测试阶段表格
            addDirectStretchingFatigueTestTable(mixRatioId, experimentName, specimenId);

            // 添加分隔线
            View divider = new View(itemView.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2));
            divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2);
            dividerParams.setMargins(0, 24, 0, 24);
            divider.setLayoutParams(dividerParams);
            layoutInputs.addView(divider);
        }

        private void addDirectStretchingDynamicModulusTable(long mixRatioId, String experimentName, int specimenId) {
            // 创建表格标题行
            LinearLayout headerRow = new LinearLayout(itemView.getContext());
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setPadding(0, 8, 0, 8);
            
            // 添加表头列
            String[] headers = {"动态模量(阶段)", "循环次数", "动态模量(MPa)", "相位角(°)", 
                              "峰-峰应力水平(kPa)", "峰-峰平均轴向微应变", "峰-峰作动器微应变", "温度(°C)"};
            int[] weights = {2, 1, 1, 1, 2, 2, 2, 1};
            
            for (int i = 0; i < headers.length; i++) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(headers[i]);
                headerText.setTextSize(12);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weights[i]
                );
                params.setMargins(4, 0, 4, 0);
                headerText.setLayoutParams(params);
                headerRow.addView(headerText);
            }
            
            layoutInputs.addView(headerRow);
            
            // 添加初始和最终数据行
            addDirectStretchingDynamicModulusRow(mixRatioId, experimentName, specimenId, "Initial");
            addDirectStretchingDynamicModulusRow(mixRatioId, experimentName, specimenId, "Final");
        }
        
        private void addDirectStretchingDynamicModulusRow(long mixRatioId, String experimentName, 
                                                        int specimenId, String stage) {
            // 创建数据行
            LinearLayout dataRow = new LinearLayout(itemView.getContext());
            dataRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dataRow.setOrientation(LinearLayout.HORIZONTAL);
            dataRow.setPadding(0, 4, 0, 4);
            
            // 阶段列 - 只显示不可编辑
            TextView stageText = new TextView(itemView.getContext());
            stageText.setText(stage);
            stageText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2
            ));
            dataRow.addView(stageText);
            
            // 循环次数列 - 可编辑
            TextInputEditText cycleInput = createDataInput(dataRow);
            String cycleKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_cycle";
            setupDataInput(cycleInput, cycleKey, mixRatioId);
            
            // 动态模量列 - 可编辑
            TextInputEditText modulusInput = createDataInput(dataRow);
            String modulusKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_modulus";
            setupDataInput(modulusInput, modulusKey, mixRatioId);
            
            // 相位角列 - 可编辑
            TextInputEditText phaseInput = createDataInput(dataRow);
            String phaseKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_phase";
            setupDataInput(phaseInput, phaseKey, mixRatioId);
            
            // 峰-峰应力水平列 - 可编辑
            TextInputEditText stressInput = createDataInput(dataRow, 2);
            String stressKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_stress";
            setupDataInput(stressInput, stressKey, mixRatioId);
            
            // 峰-峰平均轴向微应变列 - 可编辑
            TextInputEditText strainInput = createDataInput(dataRow, 2);
            String strainKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_strain";
            setupDataInput(strainInput, strainKey, mixRatioId);
            
            // 峰-峰作动器微应变列 - 可编辑
            TextInputEditText actuatorInput = createDataInput(dataRow, 2);
            String actuatorKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_actuator";
            setupDataInput(actuatorInput, actuatorKey, mixRatioId);
            
            // 温度列 - 可编辑
            TextInputEditText tempInput = createDataInput(dataRow, 1);
            String tempKey = experimentName + "_dynamic_" + specimenId + "_" + stage.toLowerCase() + "_temp";
            setupDataInput(tempInput, tempKey, mixRatioId);
            
            layoutInputs.addView(dataRow);
        }
        
        private void addDirectStretchingFatigueTestTable(long mixRatioId, String experimentName, int specimenId) {
            // 创建表格标题行
            LinearLayout headerRow = new LinearLayout(itemView.getContext());
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setPadding(0, 8, 0, 8);
            
            // 添加表头列
            String[] headers = {"疲劳测试(阶段)", "循环次数", "动态模量(MPa)", "相位角(°)", 
                              "峰-峰应力水平(kPa)", "峰-峰平均轴向微应变", "峰-峰作动器微应变", "温度(°C)"};
            int[] weights = {2, 1, 1, 1, 2, 2, 2, 1};
            
            for (int i = 0; i < headers.length; i++) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(headers[i]);
                headerText.setTextSize(12);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weights[i]
                );
                params.setMargins(4, 0, 4, 0);
                headerText.setLayoutParams(params);
                headerRow.addView(headerText);
            }
            
            layoutInputs.addView(headerRow);
            
            // 添加初始和最终数据行
            addDirectStretchingFatigueTestRow(mixRatioId, experimentName, specimenId, "Initial");
            addDirectStretchingFatigueTestRow(mixRatioId, experimentName, specimenId, "Final");
        }
        
        private void addDirectStretchingFatigueTestRow(long mixRatioId, String experimentName, 
                                                     int specimenId, String stage) {
            // 创建数据行
            LinearLayout dataRow = new LinearLayout(itemView.getContext());
            dataRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dataRow.setOrientation(LinearLayout.HORIZONTAL);
            dataRow.setPadding(0, 4, 0, 4);
            
            // 阶段列 - 只显示不可编辑
            TextView stageText = new TextView(itemView.getContext());
            stageText.setText(stage);
            stageText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2
            ));
            dataRow.addView(stageText);
            
            // 循环次数列 - 可编辑
            TextInputEditText cycleInput = createDataInput(dataRow, 1);
            String cycleKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_cycle";
            setupDataInput(cycleInput, cycleKey, mixRatioId);
            
            // 动态模量列 - 可编辑
            TextInputEditText modulusInput = createDataInput(dataRow, 1);
            String modulusKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_modulus";
            setupDataInput(modulusInput, modulusKey, mixRatioId);
            
            // 相位角列 - 可编辑
            TextInputEditText phaseInput = createDataInput(dataRow, 1);
            String phaseKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_phase";
            setupDataInput(phaseInput, phaseKey, mixRatioId);
            
            // 峰-峰应力水平列 - 可编辑
            TextInputEditText stressInput = createDataInput(dataRow, 2);
            String stressKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_stress";
            setupDataInput(stressInput, stressKey, mixRatioId);
            
            // 峰-峰平均轴向微应变列 - 可编辑
            TextInputEditText strainInput = createDataInput(dataRow, 2);
            String strainKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_strain";
            setupDataInput(strainInput, strainKey, mixRatioId);
            
            // 峰-峰作动器微应变列 - 可编辑
            TextInputEditText actuatorInput = createDataInput(dataRow, 2);
            String actuatorKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_actuator";
            setupDataInput(actuatorInput, actuatorKey, mixRatioId);
            
            // 温度列 - 可编辑
            TextInputEditText tempInput = createDataInput(dataRow, 1);
            String tempKey = experimentName + "_fatigue_" + specimenId + "_" + stage.toLowerCase() + "_temp";
            setupDataInput(tempInput, tempKey, mixRatioId);
            
            layoutInputs.addView(dataRow);
        }
        
        private TextInputEditText createDataInput(LinearLayout parent, int weight) {
            TextInputEditText editText = new TextInputEditText(itemView.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight
            ));
            editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setHint("设备给出");
            editText.setTextSize(12);
            parent.addView(editText);
            return editText;
        }

        private void addFourPointBendingFields(long mixRatioId) {
            String experimentName = "沥青混合料四点弯曲疲劳寿命试验";
            
            // 创建试件基本信息区域
            TextView basicInfoTitle = new TextView(itemView.getContext());
            basicInfoTitle.setText("试件基本信息");
            basicInfoTitle.setTextSize(18);
            basicInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            basicInfoTitle.setPadding(0, 16, 0, 16);
            layoutInputs.addView(basicInfoTitle);
            
            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("1");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的试件
            addFourPointBendingSpecimen(mixRatioId, experimentName, 1);

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addFourPointBendingSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });
        }

        private void addFourPointBendingSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件尺寸输入字段
            addSingleInputField("试件长度", "mm", experimentName + "_length_" + specimenId, mixRatioId);
            addSingleInputField("试件梁宽", "mm", experimentName + "_width_" + specimenId, mixRatioId);
            addSingleInputField("试件梁高", "mm", experimentName + "_height_" + specimenId, mixRatioId);
            addSingleInputField("梁跨距", "mm", experimentName + "_span_" + specimenId, mixRatioId);
            
            // 添加应变范围输入字段
            addSingleInputField("应变范围", "με", experimentName + "_strain_range_" + specimenId, mixRatioId);
            
            // 添加加载频率输入字段
            addSingleInputField("加载频率", "Hz", experimentName + "_frequency_" + specimenId, mixRatioId);
            
            // 添加测试温度输入字段
            addSingleInputField("测试温度", "°C", experimentName + "_temperature_" + specimenId, mixRatioId);

            // 添加实验结果标题
            TextView resultsTitle = new TextView(itemView.getContext());
            resultsTitle.setText("实验结果");
            resultsTitle.setTextSize(16);
            resultsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            resultsTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(resultsTitle);

            // 添加实验结果表格
            addFourPointBendingResultsTable(mixRatioId, experimentName, specimenId);

            // 添加分隔线
            View divider = new View(itemView.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2));
            divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2);
            dividerParams.setMargins(0, 24, 0, 24);
            divider.setLayoutParams(dividerParams);
            layoutInputs.addView(divider);
        }

        private void addFourPointBendingResultsTable(long mixRatioId, String experimentName, int specimenId) {
            // 创建表格标题行
            LinearLayout headerRow = new LinearLayout(itemView.getContext());
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setPadding(0, 8, 0, 8);
            
            // 添加表头列
            String[] headers = {"序号", "数据名称", "初始值(Initial)", "实时值(Current)"};
            int[] weights = {1, 3, 2, 2};
            
            for (int i = 0; i < headers.length; i++) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(headers[i]);
                headerText.setTextSize(14);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weights[i]
                );
                params.setMargins(4, 0, 4, 0);
                headerText.setLayoutParams(params);
                headerRow.addView(headerText);
            }
            
            layoutInputs.addView(headerRow);
            
            // 添加数据行
            String[][] dataRows = {
                {"1", "最大拉应力\nTensile stress(kPa)"},
                {"2", "最大拉应变\nTensile strain(με)"},
                {"3", "弯曲劲度模量\nFlexural stiffness (MPa)"},
                {"4", "相位角\nPhase Angle (deg)"},
                {"5", "单个循环耗散能\nDissipated energy (J/m³)"},
                {"6", "累积耗散能\nCumulative dissipated energy (kJ/m³)"},
                {"7", "最终疲劳寿命\nFatigue life (次)"}
            };
            
            for (int i = 0; i < dataRows.length; i++) {
                addFourPointBendingResultRow(mixRatioId, experimentName, specimenId, dataRows[i][0], dataRows[i][1], i == 6);
            }
        }
        
        private void addFourPointBendingResultRow(long mixRatioId, String experimentName, 
                                                 int specimenId, String index, String name, boolean isFinalRow) {
            // 创建数据行
            LinearLayout dataRow = new LinearLayout(itemView.getContext());
            dataRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dataRow.setOrientation(LinearLayout.HORIZONTAL);
            dataRow.setPadding(0, 8, 0, 8);
            
            // 序号列 - 只显示不可编辑
            TextView indexText = new TextView(itemView.getContext());
            indexText.setText(index);
            indexText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            dataRow.addView(indexText);
            
            // 数据名称列 - 只显示不可编辑
            TextView nameText = new TextView(itemView.getContext());
            nameText.setText(name);
            nameText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3
            ));
            dataRow.addView(nameText);
            
            // 初始值列 - 可编辑或特殊处理
            if (isFinalRow) {
                // 最终疲劳寿命行的初始值为提示文本
                TextView hintText = new TextView(itemView.getContext());
                hintText.setText("根据最终测试结果填入");
                hintText.setTextColor(Color.RED);
                hintText.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        2
                ));
                dataRow.addView(hintText);
                
                // 最终疲劳寿命行的实时值为可编辑字段
                TextInputEditText finalValueInput = new TextInputEditText(itemView.getContext());
                finalValueInput.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        2
                ));
                finalValueInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                finalValueInput.setHint("输入次数");
                String finalValueKey = experimentName + "_result_" + specimenId + "_fatigue_life";
                setupDataInput(finalValueInput, finalValueKey, mixRatioId);
                dataRow.addView(finalValueInput);
            } else {
                // 普通行的初始值为可编辑字段
                TextInputEditText initialInput = new TextInputEditText(itemView.getContext());
                initialInput.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        2
                ));
                initialInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                initialInput.setHint("初始值");
                String initialKey = experimentName + "_result_" + specimenId + "_initial_" + index;
                setupDataInput(initialInput, initialKey, mixRatioId);
                dataRow.addView(initialInput);
                
                // 普通行的实时值为可编辑字段
                TextInputEditText currentInput = new TextInputEditText(itemView.getContext());
                currentInput.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        2
                ));
                currentInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                currentInput.setHint("实时值");
                String currentKey = experimentName + "_result_" + specimenId + "_current_" + index;
                setupDataInput(currentInput, currentKey, mixRatioId);
                dataRow.addView(currentInput);
            }
            
            layoutInputs.addView(dataRow);
        }

        private void addSingleAxisCompressionFields(long mixRatioId) {
            String experimentName = "沥青混合料单轴压缩试验";
            
            // 创建试件基本信息区域
            TextView basicInfoTitle = new TextView(itemView.getContext());
            basicInfoTitle.setText("试件基本信息");
            basicInfoTitle.setTextSize(18);
            basicInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            basicInfoTitle.setPadding(0, 16, 0, 16);
            layoutInputs.addView(basicInfoTitle);
            
            // 添加测试温度输入字段
            addSingleInputField("测试温度", "°C", experimentName + "_temperature", mixRatioId);
            
            // 创建试件数量控制区域
            LinearLayout specimenControlLayout = new LinearLayout(itemView.getContext());
            specimenControlLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.setOrientation(LinearLayout.HORIZONTAL);
            specimenControlLayout.setPadding(0, 16, 0, 16);

            // 创建试件数量标题
            TextView specimenCountTitle = new TextView(itemView.getContext());
            specimenCountTitle.setText("试件数量: ");
            specimenCountTitle.setTextSize(16);
            specimenCountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenCountTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(specimenCountTitle);

            // 创建试件数量显示
            TextView specimenCountText = new TextView(itemView.getContext());
            specimenCountText.setText("1");
            specimenCountText.setTextSize(16);
            specimenCountText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenCountText.setPadding(8, 0, 16, 0);
            specimenControlLayout.addView(specimenCountText);

            // 创建添加试件按钮
            MaterialButton addSpecimenButton = new MaterialButton(itemView.getContext());
            addSpecimenButton.setText("添加试件");
            addSpecimenButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            specimenControlLayout.addView(addSpecimenButton);

            layoutInputs.addView(specimenControlLayout);

            // 添加初始的试件
            addSingleAxisCompressionSpecimen(mixRatioId, experimentName, 1);

            // 设置添加试件按钮点击事件
            addSpecimenButton.setOnClickListener(v -> {
                // 获取当前试件数量
                int currentCount = Integer.parseInt(specimenCountText.getText().toString());
                // 添加新试件
                int newSpecimenId = currentCount + 1;
                addSingleAxisCompressionSpecimen(mixRatioId, experimentName, newSpecimenId);
                // 更新试件数量显示
                specimenCountText.setText(String.valueOf(newSpecimenId));
            });
        }

        private void addSingleAxisCompressionSpecimen(long mixRatioId, String experimentName, int specimenId) {
            // 创建试件分组标题
            TextView specimenTitle = new TextView(itemView.getContext());
            specimenTitle.setText("试件 " + specimenId);
            specimenTitle.setTextSize(16);
            specimenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            specimenTitle.setPadding(0, 24, 0, 8);
            layoutInputs.addView(specimenTitle);

            // 添加试件尺寸输入字段
            TextView dimensionsTitle = new TextView(itemView.getContext());
            dimensionsTitle.setText("试件尺寸");
            dimensionsTitle.setTextSize(14);
            dimensionsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            dimensionsTitle.setPadding(0, 8, 0, 8);
            layoutInputs.addView(dimensionsTitle);
            
            addSingleInputField("直径", "mm", experimentName + "_diameter_" + specimenId, mixRatioId);
            addSingleInputField("高度", "mm", experimentName + "_height_" + specimenId, mixRatioId);
            
            // 添加抗压强度表格
            TextView strengthTitle = new TextView(itemView.getContext());
            strengthTitle.setText("抗压强度数据");
            strengthTitle.setTextSize(14);
            strengthTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            strengthTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(strengthTitle);
            
            addCompressionStrengthTable(mixRatioId, experimentName, specimenId);

            // 添加UTM数据表格
            TextView utmTitle = new TextView(itemView.getContext());
            utmTitle.setText("根据UTM的UTS028号程序记录数据");
            utmTitle.setTextSize(14);
            utmTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            utmTitle.setPadding(0, 16, 0, 8);
            layoutInputs.addView(utmTitle);
            
            addUTMDataTable(mixRatioId, experimentName, specimenId);

            // 添加分隔线
            View divider = new View(itemView.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2));
            divider.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2);
            dividerParams.setMargins(0, 24, 0, 24);
            divider.setLayoutParams(dividerParams);
            layoutInputs.addView(divider);
        }
        
        private void addCompressionStrengthTable(long mixRatioId, String experimentName, int specimenId) {
            // 创建表格容器
            LinearLayout tableContainer = new LinearLayout(itemView.getContext());
            tableContainer.setOrientation(LinearLayout.VERTICAL);
            tableContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            // 创建表格标题
            TextView tableTitle = new TextView(itemView.getContext());
            tableTitle.setText("抗压强度数据");
            tableTitle.setTextSize(14);
            tableTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tableTitle.setPadding(0, 8, 0, 8);
            tableContainer.addView(tableTitle);
            
            // 创建P值输入区域
            LinearLayout pValuesContainer = new LinearLayout(itemView.getContext());
            pValuesContainer.setOrientation(LinearLayout.VERTICAL);
            pValuesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            // 添加初始的P值输入框（P1, P2, P3）
            for (int i = 1; i <= 3; i++) {
                addPValueInputField(pValuesContainer, mixRatioId, experimentName, specimenId, i);
            }
            
            // 创建添加P值按钮
            MaterialButton addPButton = new MaterialButton(itemView.getContext());
            addPButton.setText("+ 添加P值");
            addPButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            addPButton.setTextSize(12);
            
            // 平均值显示区域
            LinearLayout avgContainer = new LinearLayout(itemView.getContext());
            avgContainer.setOrientation(LinearLayout.HORIZONTAL);
            avgContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgContainer.setPadding(0, 16, 0, 8);
            
            TextView avgLabel = new TextView(itemView.getContext());
            avgLabel.setText("平均值 (KN):");
            avgLabel.setTextSize(14);
            avgLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            avgLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgLabel.setPadding(0, 0, 16, 0);
            avgContainer.addView(avgLabel);
            
            TextView avgValueText = new TextView(itemView.getContext());
            avgValueText.setText("0.00");
            avgValueText.setTextSize(14);
            avgValueText.setTypeface(null, android.graphics.Typeface.BOLD);
            avgValueText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            avgContainer.addView(avgValueText);
            
            // 保存平均值到数据库的隐藏输入框
            TextInputEditText avgValueInput = new TextInputEditText(itemView.getContext());
            avgValueInput.setVisibility(View.GONE);
            String avgKey = experimentName + "_strength_" + specimenId + "_avg";
            setupDataInput(avgValueInput, avgKey, mixRatioId);
            avgContainer.addView(avgValueInput);
            
            // 设置添加P值按钮点击事件
            final int[] pCount = {3}; // 使用数组来存储当前P值数量
            final List<TextInputEditText> pInputs = new ArrayList<>(); // 存储所有P值输入框
            
            // 查找并添加初始的P值输入框到列表
            for (int i = 0; i < pValuesContainer.getChildCount(); i++) {
                View child = pValuesContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout pRow = (LinearLayout) child;
                    for (int j = 0; j < pRow.getChildCount(); j++) {
                        View rowChild = pRow.getChildAt(j);
                        if (rowChild instanceof TextInputEditText) {
                            pInputs.add((TextInputEditText) rowChild);
                        }
                    }
                }
            }
            
            // 设置P值输入框文本变化监听器，用于自动计算平均值
            TextWatcher pValueWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    calculateAverage(pInputs, avgValueText, avgValueInput);
                }
            };
            
            // 为初始的P值输入框添加文本变化监听器
            for (TextInputEditText input : pInputs) {
                input.addTextChangedListener(pValueWatcher);
            }
            
            addPButton.setOnClickListener(v -> {
                pCount[0]++;
                TextInputEditText newInput = addPValueInputField(pValuesContainer, mixRatioId, experimentName, specimenId, pCount[0]);
                pInputs.add(newInput);
                newInput.addTextChangedListener(pValueWatcher);
            });
            
            // 添加所有组件到容器
            tableContainer.addView(pValuesContainer);
            tableContainer.addView(addPButton);
            tableContainer.addView(avgContainer);
            
            layoutInputs.addView(tableContainer);
        }
        
        private TextInputEditText addPValueInputField(LinearLayout container, long mixRatioId, String experimentName, int specimenId, int pIndex) {
            LinearLayout pRow = new LinearLayout(itemView.getContext());
            pRow.setOrientation(LinearLayout.HORIZONTAL);
            pRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            pRow.setPadding(0, 8, 0, 8);
            
            // 添加P值标签
            TextView pLabel = new TextView(itemView.getContext());
            pLabel.setText("P" + pIndex + " (KN):");
            pLabel.setTextSize(14);
            pLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            pLabel.setPadding(0, 0, 16, 0);
            pRow.addView(pLabel);
            
            // 添加P值输入框
            TextInputEditText pInput = new TextInputEditText(itemView.getContext());
            pInput.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            pInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                              android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            pInput.setHint("输入P" + pIndex + "值");
            String pKey = experimentName + "_strength_" + specimenId + "_p" + pIndex;
            setupDataInput(pInput, pKey, mixRatioId);
            pRow.addView(pInput);
            container.addView(pRow);
            return pInput;
        }
        
        private void calculateAverage(List<TextInputEditText> inputs, TextView avgDisplay, TextInputEditText avgInput) {
            double sum = 0;
            int count = 0;
            
            for (TextInputEditText input : inputs) {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    try {
                        double value = Double.parseDouble(text);
                        sum += value;
                        count++;
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            }
            
            double average = (count > 0) ? (sum / count) : 0;
            String formattedAvg = String.format("%.2f", average);
            avgDisplay.setText(formattedAvg);
            avgInput.setText(formattedAvg);
        }

        private void addUTMDataTable(long mixRatioId, String experimentName, int specimenId) {
            // 创建表格容器
            LinearLayout tableContainer = new LinearLayout(itemView.getContext());
            tableContainer.setOrientation(LinearLayout.VERTICAL);
            tableContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            // 创建表格标题行
            LinearLayout headerRow = new LinearLayout(itemView.getContext());
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setPadding(0, 8, 0, 8);
            
            // 添加表头列
            String[] headers = {"", "0.1P", "0.2P", "0.3P", "0.4P", "0.5P", "0.6P", "0.7P"};
            int[] weights = {3, 1, 1, 1, 1, 1, 1, 1};
            
            for (int i = 0; i < headers.length; i++) {
                TextView headerText = new TextView(itemView.getContext());
                headerText.setText(headers[i]);
                headerText.setTextSize(12);
                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weights[i]
                );
                params.setMargins(4, 0, 4, 0);
                headerText.setLayoutParams(params);
                headerRow.addView(headerText);
            }
            
            tableContainer.addView(headerRow);
            
            // 添加数据行
            String[][] dataRows = {
                {"最大力 Force-max (KN)"},
                {"最小力 Force-min (N)"},
                {"qi (应力水平) Stress-Dev (kpa)"},
                {"ΔLi (回弹变形) Displ-resil (mm)"},
                {"回弹应变 Strain-resil"},
                {"抗压回弹模量 (Mpa)"},
                {"温度 (Temperature) (°C)"}
            };
            
            for (int i = 0; i < dataRows.length; i++) {
                addUTMDataRow(tableContainer, mixRatioId, experimentName, specimenId, dataRows[i][0], i);
            }
            
            layoutInputs.addView(tableContainer);
        }
        
        private void addUTMDataRow(LinearLayout container, long mixRatioId, String experimentName, 
                                 int specimenId, String rowTitle, int rowIndex) {
            LinearLayout dataRow = new LinearLayout(itemView.getContext());
            dataRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            dataRow.setOrientation(LinearLayout.HORIZONTAL);
            dataRow.setPadding(0, 8, 0, 8);
            
            // 添加行标题
            TextView rowTitleView = new TextView(itemView.getContext());
            rowTitleView.setText(rowTitle);
            rowTitleView.setTextSize(12);
            rowTitleView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3
            ));
            dataRow.addView(rowTitleView);
            
            // 添加数据输入框
            for (int i = 1; i <= 7; i++) {
                TextInputEditText dataInput = new TextInputEditText(itemView.getContext());
                dataInput.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                ));
                dataInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                     android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                dataInput.setHint("0." + i + "P");
                dataInput.setTextSize(12);
                String dataKey = experimentName + "_utm_" + specimenId + "_row" + rowIndex + "_col" + i;
                setupDataInput(dataInput, dataKey, mixRatioId);
                dataRow.addView(dataInput);
            }
            
            container.addView(dataRow);
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
