package com.example.labdata_main.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.calculator.SplittingTestCalculator;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MixtureExperimentDataAdapter extends RecyclerView.Adapter<MixtureExperimentDataAdapter.ViewHolder> {
    private final List<Map<String, Object>> mixRatios;
    private final Map<String, List<String>> experimentAssignments;
    private final Map<String, Map<String, String>> experimentData = new HashMap<>();
    private final Map<String, DeviceInfo> deviceData = new HashMap<>();
    private OnDeviceScanRequestListener deviceScanListener;
    private List<Map<String, Object>> taskAssignments;
    private Map<String, List<Map<String, Object>>> mixRatioTaskAssignments;
    private List<Map<String, Object>> methodsAndRatios;
    private List<Map<String, Object>> mixingEquipment;
    private List<Map<String, Object>> formingEquipment;
    private List<Map<String, Object>> mixRatioExperimentPairs = new ArrayList<>();
    private String selectedExperimentType; // 存储用户在上一个界面选择的实验类型

    public interface OnDeviceScanRequestListener {
        void onDeviceScanRequested(int position, String experimentName);
    }

    public void setOnDeviceScanRequestListener(OnDeviceScanRequestListener listener) {
        this.deviceScanListener = listener;
    }

    public MixtureExperimentDataAdapter(List<Map<String, Object>> mixRatios, Map<String, List<String>> experimentAssignments) {
        this(mixRatios, experimentAssignments, null);
    }
    
    /**
     * 创建适配器，支持指定实验类型过滤
     * @param mixRatios 配比数据列表
     * @param experimentAssignments 实验分配数据
     * @param selectedExperimentType 用户选择的实验类型（可为null表示不过滤）
     */
    public MixtureExperimentDataAdapter(List<Map<String, Object>> mixRatios, Map<String, List<String>> experimentAssignments, String selectedExperimentType) {
        this.mixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
        this.experimentAssignments = experimentAssignments != null ? experimentAssignments : new HashMap<>();
        this.selectedExperimentType = selectedExperimentType;
        this.methodsAndRatios = new ArrayList<>();
        this.mixingEquipment = new ArrayList<>();
        this.formingEquipment = new ArrayList<>();
        this.taskAssignments = new ArrayList<>();
        this.mixRatioTaskAssignments = new HashMap<>();
        
        if (selectedExperimentType != null && !selectedExperimentType.isEmpty()) {
            Log.d("MixtureAdapter", "创建适配器时设置实验类型过滤条件: " + selectedExperimentType);
        }
        
        initMixRatioExperimentPairs(); // 调用初始化方法，根据选定的实验类型进行过滤
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
        // 获取配比-实验组合项
        Map<String, Object> pair = mixRatioExperimentPairs.get(position);
        Map<String, Object> mixRatio = (Map<String, Object>) pair.get("mixRatio");
        String experimentType = (String) pair.get("experimentType");
        
        Object idObj = mixRatio.get("id");
        
        // 安全地将ID解析为Long
        Long mixRatioId = parseLongSafely(idObj);
        if (mixRatioId == null) {
            Log.e("MixtureAdapter", "无法解析配比ID: " + (idObj != null ? idObj.toString() : "null"));
            return;
        }
        
        Log.d("MixtureAdapter", "开始绑定配比 " + mixRatioId + " 的视图 | 实验类型: " + experimentType);
        
        // 获取当前配比的基本名称和描述，这可能不是最终显示值
        String mixRatioName = (String) mixRatio.get("name");
        String mixRatioDescription = (String) mixRatio.get("description");
        
        // 从配比对象中直接获取mix_name字段
        Object mixNameObj = mixRatio.get("mix_name");
        if (mixNameObj != null && !mixNameObj.toString().isEmpty()) {
            // 如果mixRatio对象中有mix_name字段，优先使用这个名称
            mixRatioName = mixNameObj.toString();
            Log.d("MixtureAdapter", "从mixRatio直接获取mix_name: " + mixRatioName);
        }
        
        Log.d("MixtureAdapter", "配比 " + mixRatioId + " 基础名称: " + mixRatioName + ", 描述: " + mixRatioDescription);
        
        // 查找所有关联到此配比ID的试件，用于获取更多详细信息
        List<Map<String, Object>> relatedSpecimens = new ArrayList<>();
        for (Map<String, Object> methodRatio : methodsAndRatios) {
            Object methodMixRatioIdObj = methodRatio.get("mixratio_id");
            Long methodMixRatioId = parseLongSafely(methodMixRatioIdObj);
            
            if (methodMixRatioId != null && methodMixRatioId.equals(mixRatioId)) {
                relatedSpecimens.add(methodRatio);
                Log.d("MixtureAdapter", "配比 " + mixRatioId + " 关联的试件: " + methodRatio);
            }
        }
        
        // 查找更有意义的名称信息
        if (!relatedSpecimens.isEmpty()) {
            boolean foundMixName = false;
            for (Map<String, Object> specimen : relatedSpecimens) {
                Object specimenMixNameObj = specimen.get("mix_name");
                String compactionMethod = (String) specimen.get("compaction_method");
                Object specimenId = specimen.get("id");
                
                Log.d("MixtureAdapter", "试件 " + specimenId + " 配方名称: " + specimenMixNameObj + ", 压实方法: " + compactionMethod);
                
                // 优先使用试件中的mix_name
                if (specimenMixNameObj != null && !specimenMixNameObj.toString().isEmpty()) {
                    String specimenMixName = specimenMixNameObj.toString();
                    
                    // 使用试件的名称，并添加区分信息
                    StringBuilder nameBuilder = new StringBuilder(specimenMixName);
                    nameBuilder.append(" (ID:").append(mixRatioId);
                    
                    if (compactionMethod != null && !compactionMethod.isEmpty()) {
                        nameBuilder.append(", ").append(compactionMethod);
                    }
                    nameBuilder.append(")");
                    
                    mixRatioName = nameBuilder.toString();
                    Log.d("MixtureAdapter", "从试件获取并更新配比 " + mixRatioId + " 显示名称为: " + mixRatioName);
                    foundMixName = true;
                    break;
                }
            }
            
            // 如果在试件中没有找到mix_name，尝试从后端API响应中查找
            if (!foundMixName) {
                // 可能需要在这里添加调用后端API获取mix_name的逻辑
                Log.d("MixtureAdapter", "未在试件中找到配比 " + mixRatioId + " 的mix_name，使用基础名称: " + mixRatioName);
            }
        }
        
        // 确保tvName和tvDescription可见
        holder.tvName.setVisibility(View.VISIBLE);
        holder.tvDescription.setVisibility(View.VISIBLE);
        
        // 确保tvName有足够的可见性和大小
        holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        holder.tvName.setTextColor(Color.BLACK);
        holder.tvName.setPadding(0, 10, 0, 10);
        holder.tvName.setText(mixRatioName);
        Log.d("MixtureAdapter", "设置tvName文本为: " + mixRatioName);
        
        holder.tvDescription.setText(mixRatioDescription);
        Log.d("MixtureAdapter", "设置tvDescription文本为: " + mixRatioDescription);
        
        // 设置实验名称和输入字段
        holder.tvExperimentName.setText("实验: " + experimentType);
        Log.d("MixtureAdapter", "设置tvExperimentName文本为: 实验: " + experimentType);
        
        // 添加实验输入字段
        holder.layoutInputs.removeAllViews();
        holder.addInputField(experimentType, mixRatioId);
        
        // 设置设备信息
        String deviceKey = String.valueOf(mixRatioId);
        DeviceInfo deviceInfo = deviceData.get(deviceKey);
        if (deviceInfo != null) {
            String deviceText = String.format("设备编号：%s，厂家：%s，型号：%s", 
                deviceInfo.getDeviceId(), 
                deviceInfo.getManufacturer(), 
                deviceInfo.getModel());
            holder.tvDeviceInfo.setText(deviceText);
            Log.d("MixtureAdapter", "设置tvDeviceInfo文本为: " + deviceText);
        } else {
            holder.tvDeviceInfo.setText("未选择设备");
            Log.d("MixtureAdapter", "设置tvDeviceInfo文本为: 未选择设备");
        }
        
        // 设置扫描设备按钮点击监听器
        final int adapterPosition = holder.getAdapterPosition();
        holder.btnScanDevice.setOnClickListener(v -> {
            if (deviceScanListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                // 获取当前配比的实验名称
                deviceScanListener.onDeviceScanRequested(adapterPosition, experimentType);
            }
        });
    }

    private void initMixRatioExperimentPairs() {
        mixRatioExperimentPairs.clear();
        
        Log.d("MixtureAdapter", "开始初始化配比-实验类型组合，mixRatios大小: " + mixRatios.size());
        Log.d("MixtureAdapter", "当前选定的实验类型: " + (selectedExperimentType != null ? selectedExperimentType : "无"));
        
        // 使用Set跟踪已添加的配比-实验组合，防止重复
        Set<String> addedCombinations = new HashSet<>();
        
        // 调试信息：当前实验分配
        for (String key : experimentAssignments.keySet()) {
            Log.d("MixtureAdapter", "实验分配: 键=" + key + ", 值=" + experimentAssignments.get(key));
        }
        
        // 如果mixRatios为空，尝试从任务分配中创建
        if (mixRatios.isEmpty() && !mixRatioTaskAssignments.isEmpty()) {
            Log.d("MixtureAdapter", "mixRatios为空，尝试从任务分配创建");
            
            Set<String> processedMixRatioIds = new HashSet<>();
            
            for (String groupKey : mixRatioTaskAssignments.keySet()) {
                // 尝试提取配比ID（可能是单独的ID或复合ID的一部分）
                String mixRatioId = groupKey;
                if (groupKey.contains("_")) {
                    mixRatioId = groupKey.split("_")[0];
                }
                
                if (!processedMixRatioIds.contains(mixRatioId)) {
                    processedMixRatioIds.add(mixRatioId);
                    
                    // 创建新的配比记录
                    Map<String, Object> newRatio = new HashMap<>();
                    newRatio.put("id", mixRatioId);
                    
                    mixRatios.add(newRatio);
                    Log.d("MixtureAdapter", "从任务分配创建新配比: ID=" + mixRatioId);
                }
            }
        }
        
        // 遍历所有配比
        for (Map<String, Object> mixRatio : mixRatios) {
            Object idObj = mixRatio.get("id");
            if (idObj == null) {
                Log.w("MixtureAdapter", "配比缺少ID，跳过");
                continue;
            }
            
            String mixRatioId = String.valueOf(idObj);
            Log.d("MixtureAdapter", "处理配比ID: " + mixRatioId);
            
            // 获取当前配比分配的实验类型
            Set<String> experimentTypes = new HashSet<>();
            
            // 尝试使用配比ID直接匹配
            if (experimentAssignments.containsKey(mixRatioId)) {
                List<String> types = experimentAssignments.get(mixRatioId);
                if (types != null && !types.isEmpty()) {
                    experimentTypes.addAll(types);
                    Log.d("MixtureAdapter", "为配比 " + mixRatioId + " 找到实验: " + types);
                }
            }
            
            // 如果experimentTypes仍然为空，尝试使用复合ID匹配
            if (experimentTypes.isEmpty()) {
                // 尝试所有以这个配比ID开头的键
                for (String key : experimentAssignments.keySet()) {
                    if (key.startsWith(mixRatioId + "_") || key.contains("_" + mixRatioId)) {
                        List<String> types = experimentAssignments.get(key);
                        if (types != null && !types.isEmpty()) {
                            experimentTypes.addAll(types);
                            Log.d("MixtureAdapter", "通过匹配键 " + key + " 找到实验: " + types);
                        }
                    }
                }
                
                // 获取试件ID
                String specimenId = null;
                if (mixRatio.containsKey("specimenId")) {
                    specimenId = String.valueOf(mixRatio.get("specimenId"));
                }
                
                if (specimenId != null) {
                    // 构造复合ID: 配比ID_试件ID
                    String combinedKey = mixRatioId + "_" + specimenId;
                    
                    // 尝试使用复合ID查找实验类型
                    if (experimentAssignments.containsKey(combinedKey)) {
                        List<String> types = experimentAssignments.get(combinedKey);
                        if (types != null && !types.isEmpty()) {
                            experimentTypes.addAll(types);
                            Log.d("MixtureAdapter", "通过复合键 " + combinedKey + " 找到实验: " + types);
                        }
                    }
                }
            }
            
            // 如果仍然没有找到实验类型，尝试从任务分配中获取
            if (experimentTypes.isEmpty() && mixRatioTaskAssignments != null) {
                // 首先尝试使用配比ID
                if (mixRatioTaskAssignments.containsKey(mixRatioId)) {
                    List<Map<String, Object>> assignments = mixRatioTaskAssignments.get(mixRatioId);
                    extractExperimentTypesFromAssignments(assignments, experimentTypes, mixRatioId);
                } else {
                    // 尝试所有可能的组合键
                    for (String key : mixRatioTaskAssignments.keySet()) {
                        if (key.startsWith(mixRatioId + "_") || key.equals(mixRatioId)) {
                            List<Map<String, Object>> assignments = mixRatioTaskAssignments.get(key);
                            extractExperimentTypesFromAssignments(assignments, experimentTypes, key);
                        }
                    }
                }
            }
            
            // 如果没有找到任何实验类型，但存在任务分配，从任务分配中提取所有实验
            if (experimentTypes.isEmpty() && !mixRatioTaskAssignments.isEmpty()) {
                Log.d("MixtureAdapter", "没有找到实验类型，尝试从所有任务分配中提取");
                
                for (String key : mixRatioTaskAssignments.keySet()) {
                    List<Map<String, Object>> assignments = mixRatioTaskAssignments.get(key);
                    extractExperimentTypesFromAssignments(assignments, experimentTypes, key);
                }
            }
            
            // 筛选实验类型，如果有过滤条件
            if (selectedExperimentType != null && !selectedExperimentType.isEmpty() && !experimentTypes.isEmpty()) {
                Set<String> filteredTypes = new HashSet<>();
                
                // 调试信息
                Log.d("MixtureAdapter", "开始过滤实验类型，过滤条件: " + selectedExperimentType);
                
                // 更宽松的匹配规则
                for (String type : experimentTypes) {
                    // 将两个字符串都转成小写进行比较，忽略大小写差异
                    String lowerType = type.toLowerCase();
                    String lowerSelected = selectedExperimentType.toLowerCase();
                    
                    // 1. 完全匹配
                    if (lowerType.equals(lowerSelected)) {
                        filteredTypes.add(type);
                        Log.d("MixtureAdapter", "完全匹配: " + type);
                    } 
                    // 2. 一个包含另一个
                    else if (lowerType.contains(lowerSelected) || lowerSelected.contains(lowerType)) {
                        filteredTypes.add(type);
                        Log.d("MixtureAdapter", "部分匹配: " + type);
                    }
                    // 3. 关键词匹配（例如"弯曲试验"匹配"沥青混合料弯曲试验"）
                    else {
                        String[] keywords = lowerSelected.split("\\s+");
                        boolean allKeywordsMatch = true;
                        
                        for (String keyword : keywords) {
                            if (!lowerType.contains(keyword)) {
                                allKeywordsMatch = false;
                                break;
                            }
                        }
                        
                        if (allKeywordsMatch) {
                            filteredTypes.add(type);
                            Log.d("MixtureAdapter", "关键词匹配: " + type + ", 关键词: " + String.join(", ", keywords));
                        }
                    }
                }
                
                if (!filteredTypes.isEmpty()) {
                    experimentTypes = filteredTypes;
                    Log.d("MixtureAdapter", "过滤后保留: " + filteredTypes);
                } else {
                    // 如果过滤后为空，降低匹配标准，检查是否有任何包含关系
                    for (String type : experimentTypes) {
                        String lowerType = type.toLowerCase();
                        String lowerSelected = selectedExperimentType.toLowerCase();
                        
                        // 检查是否有任何关键词匹配
                        String[] typeWords = lowerType.split("\\s+");
                        String[] selectedWords = lowerSelected.split("\\s+");
                        
                        for (String typeWord : typeWords) {
                            for (String selectedWord : selectedWords) {
                                if (typeWord.contains(selectedWord) || selectedWord.contains(typeWord)) {
                                    filteredTypes.add(type);
                                    Log.d("MixtureAdapter", "降级匹配: " + type + " 包含关键词 " + selectedWord);
                                    break;
                                }
                            }
                            if (filteredTypes.contains(type)) break;
                        }
                    }
                    
                    if (!filteredTypes.isEmpty()) {
                        experimentTypes = filteredTypes;
                        Log.d("MixtureAdapter", "降级过滤后保留: " + filteredTypes);
                    } else {
                        Log.w("MixtureAdapter", "过滤后无匹配实验，使用所有实验");
                        // 如果仍然没有匹配，可以选择保留所有实验或使用最符合的一个
                        if (selectedExperimentType.toLowerCase().contains("弯曲")) {
                            for (String type : experimentTypes) {
                                if (type.toLowerCase().contains("弯曲")) {
                                    filteredTypes.add(type);
                                }
                            }
                        }
                        
                        if (!filteredTypes.isEmpty()) {
                            experimentTypes = filteredTypes;
                        }
                    }
                }
            }
            
            // 为每个实验类型创建一个项目，使用addedCombinations集合防止重复
            for (String experimentType : experimentTypes) {
                // 对于选定的实验类型，只保留第一个匹配项
                if (selectedExperimentType != null && !selectedExperimentType.isEmpty()) {
                    // 如果已经添加了这个实验类型，跳过
                    boolean alreadyHasExperiment = false;
                    for (Map<String, Object> existingPair : mixRatioExperimentPairs) {
                        String existingType = (String) existingPair.get("experimentType");
                        if (existingType != null && existingType.equals(experimentType)) {
                            alreadyHasExperiment = true;
                            Log.d("MixtureAdapter", "跳过重复实验类型: " + experimentType);
                            break;
                        }
                    }
                    
                    if (alreadyHasExperiment) {
                        continue;
                    }
                }
                
                // 继续添加
                Map<String, Object> item = new HashMap<>();
                item.put("mixRatio", mixRatio);
                item.put("experimentType", experimentType);
                mixRatioExperimentPairs.add(item);
                Log.d("MixtureAdapter", "添加配比-实验组合: 配比ID=" + mixRatioId + ", 实验=" + experimentType);
            }
        }
        
        // 如果在第一阶段没有创建任何配比-实验组合，则尝试备用策略
        if (mixRatioExperimentPairs.isEmpty() && !experimentAssignments.isEmpty()) {
            Log.w("MixtureAdapter", "无法创建任何配比-实验组合，但有实验分配数据，尝试备用策略");
            
            // 最后一次尝试：为每个实验分配创建一个通用的配比
            for (String key : experimentAssignments.keySet()) {
                String mixRatioId = key;
                if (key.contains("_")) {
                    mixRatioId = key.split("_")[0];
                }
                
                Map<String, Object> genericMixRatio = new HashMap<>();
                genericMixRatio.put("id", mixRatioId);
                
                List<String> types = experimentAssignments.get(key);
                if (types != null) {
                    for (String type : types) {
                        // 创建唯一组合键
                        String combinationKey = mixRatioId + "::" + type;
                        
                        // 如果有选定实验类型，只添加匹配的，并且确保不重复
                        if (!addedCombinations.contains(combinationKey) && 
                            (selectedExperimentType == null || 
                             selectedExperimentType.isEmpty() || 
                             type.toLowerCase().contains(selectedExperimentType.toLowerCase()) ||
                             selectedExperimentType.toLowerCase().contains(type.toLowerCase()))) {
                                
                            Map<String, Object> item = new HashMap<>();
                            item.put("mixRatio", genericMixRatio);
                            item.put("experimentType", type);
                            mixRatioExperimentPairs.add(item);
                            
                            // 将组合添加到已处理集合
                            addedCombinations.add(combinationKey);
                            
                            Log.d("MixtureAdapter", "添加通用配比-实验组合: 配比ID=" + mixRatioId + ", 实验=" + type);
                        }
                    }
                }
            }
        }
        
        Log.d("MixtureAdapter", "初始化了 " + mixRatioExperimentPairs.size() + " 个配比-实验类型组合");
    }
    
    // 辅助方法：从任务分配中提取实验类型
    private void extractExperimentTypesFromAssignments(List<Map<String, Object>> assignments, Set<String> experimentTypes, String key) {
        if (assignments != null) {
            for (Map<String, Object> assignment : assignments) {
                if (assignment.containsKey("task_assignment")) {
                    String type = (String) assignment.get("task_assignment");
                    if (type != null && !type.isEmpty()) {
                        experimentTypes.add(type);
                        Log.d("MixtureAdapter", "从任务指派 " + key + " 提取实验类型: " + type);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mixRatioExperimentPairs.size();
    }

    public Map<String, Map<String, String>> getExperimentData() {
        return experimentData;
    }
    
    /**
     * 获取用户选择的实验类型
     * @return 选定的实验类型，如果未选择则返回null
     */
    public String getSelectedExperimentType() {
        return selectedExperimentType;
    }
    
    /**
     * 获取配比数据列表
     * @return 配比数据列表
     */
    public List<Map<String, Object>> getMixRatios() {
        return mixRatios;
    }

    public void setDeviceInfo(int position, DeviceInfo deviceInfo) {
        String deviceKey = String.valueOf(mixRatios.get(position).get("id"));
        deviceData.put(deviceKey, deviceInfo);
        notifyItemChanged(position);
    }

    /**
     * 获取设备信息Map
     * @return 设备信息Map
     */
    public Map<String, DeviceInfo> getDeviceData() {
        return deviceData;
    }
    
    /**
     * 获取特定ID的设备信息
     * @param mixRatioId 配比ID
     * @return 设备信息
     */
    public DeviceInfo getDeviceInfo(int mixRatioId) {
        return deviceData.get(String.valueOf(mixRatioId));
    }
    
    /**
     * 获取配比ID与任务分配的映射
     * @return 配比任务分配映射
     */
    public Map<String, List<Map<String, Object>>> getMixRatioTaskAssignments() {
        return mixRatioTaskAssignments;
    }

    /**
     * 更新试件制备信息
     * 
     * @param specimenData 从API获取的试件制备数据
     */
    public void updateSpecimenData(Map<String, Object> specimenData) {
        Log.d("MixtureAdapter", "收到试件数据: " + specimenData);
        
        // 清空现有数据
        methodsAndRatios.clear();
        mixingEquipment.clear();
        formingEquipment.clear();
        taskAssignments.clear();
        mixRatioTaskAssignments.clear();
        
        // 重要：清空mixRatios列表
        mixRatios.clear();
        
        if (specimenData == null) {
            Log.w("MixtureAdapter", "试件数据为空");
            return;
        }
        
        // 处理任务指派数据
        if (specimenData.containsKey("taskAssignments")) {
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) specimenData.get("taskAssignments");
            if (tasks != null) {
                Log.d("MixtureAdapter", "收到" + tasks.size() + "个任务指派");
                taskAssignments.addAll(tasks);
                
                // 按配比ID和试件ID分组
                for (Map<String, Object> task : tasks) {
                    String specimenId = null;
                    String mixRatioId = null;
                    
                    if (task.containsKey("specimen_id")) {
                        specimenId = String.valueOf(task.get("specimen_id"));
                        Log.d("MixtureAdapter", "映射关系: 试件ID " + specimenId);
                    }
                    
                    if (task.containsKey("mixratio_id")) {
                        mixRatioId = String.valueOf(task.get("mixratio_id"));
                        Log.d("MixtureAdapter", "映射关系: 配比ID " + mixRatioId);
                    }
                    
                    if (specimenId != null && mixRatioId != null) {
                        // 使用复合键 (配比ID_试件ID)
                        String groupKey = mixRatioId + "_" + specimenId;
                        
                        if (!mixRatioTaskAssignments.containsKey(groupKey)) {
                            mixRatioTaskAssignments.put(groupKey, new ArrayList<>());
                        }
                        mixRatioTaskAssignments.get(groupKey).add(task);
                        
                        // 同时添加单独的配比ID键，以提高匹配率
                        if (!mixRatioTaskAssignments.containsKey(mixRatioId)) {
                            mixRatioTaskAssignments.put(mixRatioId, new ArrayList<>());
                        }
                        mixRatioTaskAssignments.get(mixRatioId).add(task);
                    }
                }
                
                // 打印分组结果
                StringBuilder keySetStr = new StringBuilder();
                for (String key : mixRatioTaskAssignments.keySet()) {
                    keySetStr.append(key).append(",");
                }
                Log.d("MixtureAdapter", "分组键集合: [" + keySetStr + "]");
                Log.d("MixtureAdapter", "最终mixRatioTaskAssignments包含的键: [" + keySetStr + "]");
                
                // 打印任务ID列表
                StringBuilder taskIds = new StringBuilder();
                for (Map<String, Object> task : tasks) {
                    if (task.containsKey("task_id")) {
                        taskIds.append(task.get("task_id")).append(", ");
                    }
                }
                Log.d("MixtureAdapter", "任务ID: " + taskIds);
            }
        }
        
        // 创建试件ID到配比ID的映射表
        Map<String, String> specimenToMixRatio = new HashMap<>();
        for (Map<String, Object> task : taskAssignments) {
            if (task.containsKey("specimen_id") && task.containsKey("mixratio_id")) {
                String specimenId = String.valueOf(task.get("specimen_id"));
                String mixRatioId = String.valueOf(task.get("mixratio_id"));
                specimenToMixRatio.put(specimenId, mixRatioId);
                Log.d("MixtureAdapter", "映射: 试件ID " + specimenId + " -> 配比ID " + mixRatioId);
            }
        }
        
        // 保存已处理的配比ID
        Set<String> processedMixRatioIds = new HashSet<>();
        
        // 处理方法和配比数据
        if (specimenData.containsKey("methodsAndRatios")) {
            List<Map<String, Object>> methods = (List<Map<String, Object>>) specimenData.get("methodsAndRatios");
            if (methods != null) {
                methodsAndRatios.addAll(methods);
                Log.d("MixtureAdapter", "方法和配比数据: " + methods);
                
                // 处理配比数据
                for (Map<String, Object> method : methods) {
                    if (method.containsKey("id")) {
                        String specimenId = String.valueOf(method.get("id"));
                        String mixRatioId = specimenToMixRatio.getOrDefault(specimenId, null);
                        
                        if (mixRatioId == null) {
                            Log.w("MixtureAdapter", "找不到试件ID " + specimenId + " 对应的配比ID，使用试件ID作为配比ID");
                            mixRatioId = specimenId;
                        }
                        
                        // 检查是否已处理过这个配比ID
                        if (!processedMixRatioIds.contains(mixRatioId)) {
                            // 创建新的mixRatio对象
                            Map<String, Object> mixRatio = new HashMap<>();
                            
                            // 设置ID
                            mixRatio.put("id", mixRatioId);
                            
                            // 保存原始试件ID
                            mixRatio.put("specimenId", specimenId);
                            
                            // 复制方法信息
                            for (Map.Entry<String, Object> entry : method.entrySet()) {
                                if (!entry.getKey().equals("id")) {
                                    mixRatio.put(entry.getKey(), entry.getValue());
                                }
                            }
                            
                            // 添加到mixRatios列表
                            mixRatios.add(mixRatio);
                            processedMixRatioIds.add(mixRatioId);
                            
                            Log.d("MixtureAdapter", "添加配比: ID=" + mixRatioId + ", 试件ID=" + specimenId);
                        } else {
                            Log.d("MixtureAdapter", "配比ID " + mixRatioId + " 已处理，跳过");
                        }
                    }
                }
            }
        }
        
        // 输出当前mixRatios中包含的配比ID和内容
        Log.d("MixtureAdapter", "处理后的mixRatios大小: " + mixRatios.size());
        for (Map<String, Object> ratio : mixRatios) {
            StringBuilder ratioInfo = new StringBuilder("配比 {");
            for (Map.Entry<String, Object> entry : ratio.entrySet()) {
                ratioInfo.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
            }
            ratioInfo.append("}");
            Log.d("MixtureAdapter", ratioInfo.toString());
        }
        
        // 更新实验分配
        if (specimenData.containsKey("experimentAssignments")) {
            experimentAssignments.clear();
            Map<String, List<String>> assignments = (Map<String, List<String>>) specimenData.get("experimentAssignments");
            experimentAssignments.putAll(assignments);
            
            // 输出实验分配信息
            StringBuilder assignmentKeys = new StringBuilder();
            for (String key : experimentAssignments.keySet()) {
                assignmentKeys.append(key).append(", ");
            }
            Log.d("MixtureAdapter", "更新实验分配: [" + assignmentKeys + "]");
            
            // 打印每个配比ID对应的实验
            for (String key : experimentAssignments.keySet()) {
                Log.d("MixtureAdapter", "配比ID " + key + " 的实验: " + experimentAssignments.get(key));
            }
        }
        
        // 处理拌合设备数据
        if (specimenData.containsKey("mixingEquipment")) {
            List<Map<String, Object>> equipment = (List<Map<String, Object>>) specimenData.get("mixingEquipment");
            if (equipment != null) {
                mixingEquipment.addAll(equipment);
                Log.d("MixtureAdapter", "拌合设备数据: " + equipment);
            }
        }
        
        // 处理成型设备数据
        if (specimenData.containsKey("formingEquipment")) {
            List<Map<String, Object>> equipment = (List<Map<String, Object>>) specimenData.get("formingEquipment");
            if (equipment != null) {
                formingEquipment.addAll(equipment);
                Log.d("MixtureAdapter", "成型设备数据: " + equipment);
            }
        }
        
        // 重新初始化配比-实验类型组合
        initMixRatioExperimentPairs();
        // 通知适配器数据已更新
        notifyDataSetChanged();
    }

    /**
     * 安全地解析对象为Long，处理各种可能的数值格式
     * @param obj 要解析的对象
     * @return 解析后的Long值，如果无法解析则返回null
     */
    private Long parseLongSafely(Object obj) {
        if (obj == null) return null;
        
        try {
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else {
                String str = String.valueOf(obj);
                if (str.contains(".")) {
                    return Double.valueOf(str).longValue();
                } else {
                    return Long.valueOf(str);
                }
            }
        } catch (NumberFormatException e) {
            Log.e("MixtureAdapter", "无法将对象转换为Long: " + obj, e);
            return null;
        }
    }

    private String getMixRatioIds() {
        StringBuilder ids = new StringBuilder();
        for (Map<String, Object> mixRatio : mixRatios) {
            ids.append(mixRatio.get("id")).append(", ");
        }
        return ids.toString();
    }

    /**
     * 获取配比ID到有效实验类型的映射
     * 只返回实际指派给每个配比的实验类型
     * 
     * @return Map<String, List<String>> 配比ID到指派实验类型列表的映射
     */
    public Map<String, List<String>> getValidExperimentAssignments() {
        Map<String, List<String>> validAssignments = new HashMap<>();
        
        // 1. 处理experimentAssignments
        if (experimentAssignments != null) {
            // 直接复制现有的实验指派
            for (Map.Entry<String, List<String>> entry : experimentAssignments.entrySet()) {
                String key = entry.getKey();
                List<String> experiments = entry.getValue();
                
                if (key != null && experiments != null && !experiments.isEmpty()) {
                    // 提取配比ID（如果键包含下划线，取第一部分）
                    String mixRatioId = key;
                    if (key.contains("_")) {
                        mixRatioId = key.split("_")[0];
                    }
                    
                    if (!validAssignments.containsKey(mixRatioId)) {
                        validAssignments.put(mixRatioId, new ArrayList<>());
                    }
                    
                    // 添加实验类型，避免重复
                    for (String experiment : experiments) {
                        if (!validAssignments.get(mixRatioId).contains(experiment)) {
                            validAssignments.get(mixRatioId).add(experiment);
                        }
                    }
                }
            }
        }
        
        // 2. 处理taskAssignments
        for (Map<String, Object> assignment : taskAssignments) {
            // 获取配比ID
            Object mixRatioIdObj = assignment.get("mixratio_id");
            if (mixRatioIdObj != null) {
                String mixRatioId = String.valueOf(mixRatioIdObj);
                String taskAssignment = (String) assignment.get("task_assignment");
                
                if (taskAssignment != null && !taskAssignment.isEmpty()) {
                    if (!validAssignments.containsKey(mixRatioId)) {
                        validAssignments.put(mixRatioId, new ArrayList<>());
                    }
                    
                    // 添加实验类型，避免重复
                    if (!validAssignments.get(mixRatioId).contains(taskAssignment)) {
                        validAssignments.get(mixRatioId).add(taskAssignment);
                    }
                }
            }
        }
        
        // 记录日志，调试时使用
        for (Map.Entry<String, List<String>> entry : validAssignments.entrySet()) {
            String mixRatioId = entry.getKey();
            List<String> experiments = entry.getValue();
            Log.d("MixtureAdapter", "配比ID " + mixRatioId + " 的有效实验指派: " + experiments);
        }
        
        return validAssignments;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDescription;
        TextView tvExperimentName;
        TextView tvDeviceInfo;
        MaterialButton btnScanDevice;
        LinearLayout layoutInputs;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
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
            specimenCountTitle.setText("样本数量：");
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
            specimenCountTitle.setText("样本数量：");
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

                    // 用于调试p值平均值计算
                    Log.d("SplittingTest", "P值： p1=" + p1Str + ", p2=" + p2Str + ", p3=" + p3Str);

                    try {
                    if (p1Str != null && !p1Str.isEmpty()) {
                        pSum += Double.parseDouble(p1Str);
                        pCount++;
                    }
                    }
                    catch (NumberFormatException e) {
                        Log.e("SplittingTest", "P1解析错误: " + e.getMessage());
                    }
                    try {
                    if (p2Str != null && !p2Str.isEmpty()) {
                        pSum += Double.parseDouble(p2Str);
                        pCount++;
                    }   
                    }
                    catch (NumberFormatException e) {
                        Log.e("SplittingTest", "P2解析错误: " + e.getMessage());
                    }
                    try {
                    if (p3Str != null && !p3Str.isEmpty()) {
                        pSum += Double.parseDouble(p3Str);
                        pCount++;
                    }
                    }
                    catch (NumberFormatException e) {
                        Log.e("SplittingTest", "P3解析错误: " + e.getMessage());
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
                            LinearLayout container = (LinearLayout) child;
                            for (int j = 0; j < container.getChildCount(); j++) {
                                View innerChild = container.getChildAt(j);
                                if (innerChild instanceof LinearLayout) {
                                    LinearLayout avgContainer = (LinearLayout) innerChild;
                                    if (avgContainer.getChildCount() >= 2) {
                                        View labelView = avgContainer.getChildAt(0);
                                        View valueView = avgContainer.getChildAt(1);
                                        if (labelView instanceof TextView && valueView instanceof TextView) {
                                            TextView label = (TextView) labelView;
                                            TextView value = (TextView) valueView;
                                            if (label.getText().toString().contains("P平均值")) {
                                                value.setText(String.format("%.2f", pAvg));
                                            } else if (label.getText().toString().contains("X平均值")) {
                                                value.setText(String.format("%.2f", xAvg));
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

            TextView avgValueDisplay = new TextView(itemView.getContext());
            avgValueDisplay.setText("0.00");
            avgValueDisplay.setTextSize(14);
            avgValueDisplay.setTypeface(null, android.graphics.Typeface.BOLD);
            avgValueDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            avgContainer.addView(avgValueDisplay);

            TextInputEditText avgInput = new TextInputEditText(itemView.getContext());
            avgInput.setVisibility(View.GONE);
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

            TextView avgValueDisplay = new TextView(itemView.getContext());
            avgValueDisplay.setText("0.00");
            avgValueDisplay.setTextSize(14);
            avgValueDisplay.setTypeface(null, android.graphics.Typeface.BOLD);
            avgValueDisplay.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            avgContainer.addView(avgValueDisplay);

            TextInputEditText avgInput = new TextInputEditText(itemView.getContext());
            avgInput.setVisibility(View.GONE);
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
            resultsTitle.setTextSize(14);
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
            specimenCountTitle.setText("样本数量：");
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
            String[] headers = {"频率(Hz)", "循环次数", "动态模量(MPa)", "相位角(°)", 
                              "峰-峰应力水平(kPa)", "峰-峰平均轴向微应变", "峰-峰作动器微应变", "温度(°C)"};
            int[] weights = {1, 1, 1, 1, 2, 2, 2, 1};
            
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
            // 为EditText设置Tag，以便后续能找到它
            editText.setTag(dataKey);
            
            // 从保存的数据中恢复值（如果有）
            String mixRatioIdStr = String.valueOf(mixRatioId);
            Map<String, String> savedData = experimentData.get(mixRatioIdStr);
            if (savedData != null && savedData.containsKey(dataKey)) {
                String savedValue = savedData.get(dataKey);
                if (savedValue != null && !savedValue.isEmpty()) {
                    editText.setText(savedValue);
                    Log.d("MixtureAdapter", "恢复输入框值: key=" + dataKey + ", value=" + savedValue);
                }
            }
            
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
                @Override
                public void afterTextChanged(Editable s) {
                    // 获取或创建该配比的数据Map
                    Map<String, String> mixRatioData = experimentData.computeIfAbsent(
                            mixRatioIdStr,
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
            specimenCountTitle.setText("样本数量：");
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
            
            tableContainer.addView(headerRow);
            
            // 添加初始和最终数据行
            addDirectStretchingDynamicModulusRow(tableContainer, mixRatioId, experimentName, specimenId, "Initial");
            addDirectStretchingDynamicModulusRow(tableContainer, mixRatioId, experimentName, specimenId, "Final");
            
            // 创建水平滚动视图包装表格
            HorizontalScrollView scrollView = new HorizontalScrollView(itemView.getContext());
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            scrollView.setHorizontalScrollBarEnabled(true);
            scrollView.addView(tableContainer);
            
            // 添加滚动视图到主布局
            layoutInputs.addView(scrollView);
        }
        
        private void addDirectStretchingDynamicModulusRow(LinearLayout container, long mixRatioId, String experimentName, 
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
            
            container.addView(dataRow);
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
        
        private void addDirectStretchingFatigueTestTable(long mixRatioId, String experimentName, int specimenId) {
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
            
            tableContainer.addView(headerRow);
            
            // 添加初始和最终数据行
            addDirectStretchingFatigueTestRow(tableContainer, mixRatioId, experimentName, specimenId, "Initial");
            addDirectStretchingFatigueTestRow(tableContainer, mixRatioId, experimentName, specimenId, "Final");
            
            // 创建水平滚动视图包装表格
            HorizontalScrollView scrollView = new HorizontalScrollView(itemView.getContext());
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            scrollView.setHorizontalScrollBarEnabled(true);
            scrollView.addView(tableContainer);
            
            // 添加滚动视图到主布局
            layoutInputs.addView(scrollView);
        }
        
        private void addDirectStretchingFatigueTestRow(LinearLayout container, long mixRatioId, String experimentName, 
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
            
            container.addView(dataRow);
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
            specimenCountTitle.setText("样本数量：");
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
            specimenCountTitle.setText("样本数量：");
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
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
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

    /**
     * 获取所有实验数据用于保存配置变更状态
     * @return 包含所有实验数据的ArrayList，用于保存到Bundle
     */
    public ArrayList<Map<String, String>> getAllExperimentData() {
        Log.d("MixtureAdapter", "获取所有实验数据用于保存状态");
        ArrayList<Map<String, String>> result = new ArrayList<>();
        
        // 转换内部存储的实验数据为可序列化的ArrayList
        for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
            HashMap<String, String> item = new HashMap<>();
            // 添加mixRatioId作为标识符
            item.put("mixRatioId", entry.getKey());
            
            // 复制所有该配比的实验数据
            Map<String, String> data = entry.getValue();
            if (data != null) {
                for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                    item.put(dataEntry.getKey(), dataEntry.getValue());
                }
            }
            
            result.add(item);
        }
        
        Log.d("MixtureAdapter", "已保存" + result.size() + "条实验数据记录");
        return result;
    }
    
    /**
     * 从保存的状态恢复实验数据
     * @param savedData 从Bundle恢复的实验数据列表
     */
    public void restoreExperimentData(ArrayList<Map<String, String>> savedData) {
        if (savedData == null || savedData.isEmpty()) {
            Log.w("MixtureAdapter", "没有可恢复的实验数据");
            return;
        }
        
        Log.d("MixtureAdapter", "恢复" + savedData.size() + "条实验数据记录");
        
        // 清空现有数据
        experimentData.clear();
        
        // 恢复保存的实验数据
        for (Map<String, String> item : savedData) {
            String mixRatioId = item.remove("mixRatioId");
            if (mixRatioId != null) {
                Map<String, String> data = new HashMap<>(item);
                experimentData.put(mixRatioId, data);
                
                Log.d("MixtureAdapter", "已恢复配比ID=" + mixRatioId + "的实验数据，包含" + 
                      data.size() + "个字段");
            }
        }
        
        // 通知数据集变化
        notifyDataSetChanged();
    }
    
    /**
     * 将Map格式的配比数据转换为MixRatio对象列表
     * @return 配比对象列表
     */
    public List<MixRatio> getMixRatioObjects() {
        List<MixRatio> result = new ArrayList<>();
        for (Map<String, Object> map : mixRatios) {
            MixRatio ratio = new MixRatio();
            
            // 提取ID
            Object id = map.get("id");
            if (id != null) {
                if (id instanceof Number) {
                    ratio.setId(((Number) id).longValue());
                } else if (id instanceof String) {
                    try {
                        ratio.setId(Long.parseLong((String) id));
                    } catch (NumberFormatException e) {
                        Log.e("MixtureAdapter", "无法解析配比ID: " + id);
                    }
                }
            }
            
            // 提取名称和描述
            Object name = map.get("name");
            if (name != null) {
                ratio.setName(name.toString());
            }
            
            Object description = map.get("description");
            if (description != null) {
                ratio.setDescription(description.toString());
            }
            
            result.add(ratio);
        }
        return result;
    }
    
    /**
     * 添加新的配比
     * @param ratio 要添加的配比
     */
    public void addMixRatio(MixRatio ratio) {
        // 检查是否已存在该ID的配比
        for (Map<String, Object> map : mixRatios) {
            Object id = map.get("id");
            if (id != null) {
                long ratioId = -1;
                if (id instanceof Number) {
                    ratioId = ((Number) id).longValue();
                } else if (id instanceof String) {
                    try {
                        ratioId = Long.parseLong((String) id);
                    } catch (NumberFormatException ignored) {
                    }
                }
                
                if (ratioId == ratio.getId()) {
                    Log.d("MixtureAdapter", "配比ID=" + ratio.getId() + "已存在，不再添加");
                    return;
                }
            }
        }
        
        // 添加新配比
        Map<String, Object> newRatio = new HashMap<>();
        newRatio.put("id", ratio.getId());
        newRatio.put("name", ratio.getName());
        newRatio.put("description", ratio.getDescription());
        mixRatios.add(newRatio);
        
        // 通知视图刷新
        notifyDataSetChanged();
        Log.d("MixtureAdapter", "已添加新配比: ID=" + ratio.getId() + ", 名称=" + ratio.getName());
    }

    /**
     * 更新指定配比和实验字段的值
     *
     * @param mixRatioId 配比ID
     * @param experimentKey 实验数据键
     * @param value 要设置的值
     */
    public void updateExperimentValue(String mixRatioId, String experimentKey, String value) {
        // 先检查mixRatioId是否存在于experimentData中
        Map<String, String> mixRatioExperiments = experimentData.get(mixRatioId);
        if (mixRatioExperiments == null) {
            mixRatioExperiments = new HashMap<>();
            experimentData.put(mixRatioId, mixRatioExperiments);
        }
        
        // 更新实验数据值
        mixRatioExperiments.put(experimentKey, value);
        
        Log.d("MixtureAdapter", "已更新数据: mixRatioId=" + mixRatioId + ", key=" + experimentKey + ", value=" + value);
        
        // 延迟一点时间让RecyclerView完全刷新
        new Handler(Looper.getMainLooper()).post(() -> {
            notifyDataSetChanged();
        });
    }
    
    /**
     * 更新实验数据 - 用于从API加载的数据
     * 
     * @param mixRatioId 配比ID
     * @param key 实验数据键
     * @param value 要设置的值
     */
    public void updateExperimentData(String mixRatioId, String key, String value) {
        Map<String, String> mixRatioData = experimentData.computeIfAbsent(mixRatioId, k -> new HashMap<>());
        mixRatioData.put(key, value);

        //跟踪P值的信息
        if (key.contains("strength") && (key.endsWith("_p1") || key.endsWith("_p2") || key.endsWith("_p3"))) {
        Log.d("SplittingTest", "保存P值： mixRatioId=" + mixRatioId + ", key=" + key + ", value=" + value);
    }

        // 通知数据集变化
        Log.d("MixtureAdapter", "更新实验数据: mixRatioId=" + mixRatioId + ", key=" + key + ", value=" + value);
    }

    /**
     * 设置实验数据
     * @param data 实验数据Map
     */
    public void setExperimentData(Map<String, Map<String, String>> data) {
        if (data == null) return;
        
        Log.d("MixtureAdapter", "设置实验数据: " + data.size() + "个配比");
        
        // 遍历每个配比
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String mixRatioId = entry.getKey();
            Map<String, String> experimentValues = entry.getValue();
            
            // 遍历每个实验值
            for (Map.Entry<String, String> valueEntry : experimentValues.entrySet()) {
                String experimentKey = valueEntry.getKey();
                String value = valueEntry.getValue();
                
                // 更新实验值
                updateExperimentValue(mixRatioId, experimentKey, value);
            }
        }
        
        // 通知适配器数据已变更
        notifyDataSetChanged();
    }
    
    /**
     * 刷新所有输入控件
     */
    public void refreshAllInputs() {
        Log.d("MixtureAdapter", "刷新所有输入控件");
        
        // 在主线程上执行UI刷新
        new Handler(Looper.getMainLooper()).post(() -> {
            notifyDataSetChanged();
        });
    }
}
