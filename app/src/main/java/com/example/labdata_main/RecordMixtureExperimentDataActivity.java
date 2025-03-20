package com.example.labdata_main;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.adapter.MixtureExperimentDataAdapter;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordMixtureExperimentDataActivity extends AppCompatActivity 
    implements MixtureExperimentDataAdapter.OnDeviceScanRequestListener {
    
    private static final String TAG = "RecordMixtureExperiment";
    private RecyclerView rvExperiments;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private AppDatabase database;
    private MixtureExperimentDataAdapter adapter;
    private ExecutorService executor;
    private ExperimentTask currentTask;
    private int currentScanPosition = -1;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mixture_experiment_data);

        // 初始化数据库和线程池
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // 初始化视图
        initViews();

        // 获取传入的任务ID
        String taskId = getIntent().getStringExtra("taskId");
        if (taskId != null && !taskId.isEmpty()) {
            loadTaskData(taskId);
        } else {
            Toast.makeText(this, "未找到任务信息", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        rvExperiments = findViewById(R.id.rvExperiments);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        
        // 初始化SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_light_custom);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 获取传入的任务ID
            String taskId = getIntent().getStringExtra("taskId");
            if (taskId != null && !taskId.isEmpty()) {
                // 刷新数据
                fetchSpecimenData(taskId);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "任务ID无效，无法刷新数据", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置RecyclerView
        rvExperiments.setLayoutManager(new LinearLayoutManager(this));

        // 设置按钮监听
        btnSave.setOnClickListener(v -> saveExperimentData());
        btnSave.setEnabled(false); // 数据加载前禁用按钮

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * 加载任务数据
     * @param taskId 任务ID
     */
    private void loadTaskData(String taskId) {
        showLoading(true, "加载任务信息...");
        
        MixtureTaskService taskService = ServiceCreator.create(MixtureTaskService.class);
        taskService.getMixtureTaskByTaskId(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    
                    // 处理从API获取的任务数据
                    processApiTaskData(data, taskId);
                    
                    // 获取试件制备数据（第二步）
                    fetchSpecimenData(taskId);
                } else {
                    // 隐藏加载指示器
                    showLoading(false);
                    
                    String errorMsg = response.body() != null ? response.body().getMessage() : "加载任务数据失败";
                    showErrorState(errorMsg);
                    Log.e(TAG, "加载任务数据失败: " + errorMsg);
                    
                    // 确保刷新控件不在刷新状态
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    // 显示空界面但允许下拉刷新
                    setupEmptyState(taskId);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // 隐藏加载指示器
                showLoading(false);
                
                showErrorState("网络错误: " + t.getMessage());
                Log.e(TAG, "加载任务数据时网络错误", t);
                
                // 确保刷新控件不在刷新状态
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                // 显示空界面但允许下拉刷新
                setupEmptyState(taskId);
            }
        });
    }
    
    // 处理从API获取的任务数据
    private void processApiTaskData(Map<String, Object> data, String taskId) {
        if (data != null) {
            try {
                // 解析配比数据
                List<Map<String, Object>> mixRatioData = (List<Map<String, Object>>) data.get("mixRatios");
                List<MixRatio> mixRatios = new ArrayList<>();
                
                if (mixRatioData != null) {
                    for (Map<String, Object> ratioMap : mixRatioData) {
                        MixRatio mixRatio = new MixRatio();
                        
                        mixRatio.setId(((Number) ratioMap.get("id")).longValue());
                        mixRatio.setName((String) ratioMap.get("name"));
                        mixRatio.setDescription((String) ratioMap.get("description"));
                        
                        // 如果有aggregate_percentage，解析它
                        if (ratioMap.containsKey("aggregate_percentage")) {
                            Object aggregateObj = ratioMap.get("aggregate_percentage");
                            if (aggregateObj instanceof Number) {
                                mixRatio.setAggregatePercentage(((Number) aggregateObj).doubleValue());
                            } else if (aggregateObj instanceof String) {
                                try {
                                    mixRatio.setAggregatePercentage(Double.parseDouble((String) aggregateObj));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "解析aggregate_percentage出错", e);
                                    mixRatio.setAggregatePercentage(0.0);
                                }
                            }
                        }
                        
                        // 如果有asphalt_percentage，解析它
                        if (ratioMap.containsKey("asphalt_percentage")) {
                            Object asphaltObj = ratioMap.get("asphalt_percentage");
                            if (asphaltObj instanceof Number) {
                                mixRatio.setAsphaltPercentage(((Number) asphaltObj).doubleValue());
                            } else if (asphaltObj instanceof String) {
                                try {
                                    mixRatio.setAsphaltPercentage(Double.parseDouble((String) asphaltObj));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "解析asphalt_percentage出错", e);
                                    mixRatio.setAsphaltPercentage(0.0);
                                }
                            }
                        }
                        
                        mixRatios.add(mixRatio);
                    }
                }
                
                // 解析实验分配数据，直接从API获取experimentAssignments
                Map<Long, List<String>> experimentAssignments;
                
                // 尝试从API响应中获取experimentAssignments
                if (data.containsKey("experimentAssignments")) {
                    experimentAssignments = convertToLongKeyMap((Map<String, List<String>>) data.get("experimentAssignments"));
                } else {
                    // 如果API没有直接提供experimentAssignments，则创建默认分配
                    experimentAssignments = new HashMap<>();
                    // 为每个配比ID分配默认实验类型列表
                    for (MixRatio ratio : mixRatios) {
                        experimentAssignments.put(ratio.getId(), new ArrayList<>(List.of("MixStability", "MixSplitting")));
                    }
                }
                
                // 打印实验分配的日志
                Log.d(TAG, "配比到实验类型映射: " + experimentAssignments);
                
                // 初始化任务并保存到数据库（如果需要）
                executeTaskSafely(() -> {
                    // 检查是否已存在该任务
                    ExperimentTask existingTask = database.experimentTaskDao().findByTaskId(taskId);
                    
                    if (existingTask == null) {
                        // 创建新任务
                        ExperimentTask task = new ExperimentTask();
                        task.setTaskId(taskId);
                        task.setTaskName((String) data.get("taskName"));
                        task.setStatus((String) data.get("status"));
                        
                        // 处理projectId，可能是Number或String
                        Object projectIdObj = data.get("projectId");
                        if (projectIdObj instanceof Number) {
                            task.setProjectId(((Number) projectIdObj).longValue());
                        } else if (projectIdObj instanceof String) {
                            try {
                                task.setProjectId(Long.parseLong((String) projectIdObj));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "解析projectId出错，使用默认值0", e);
                                task.setProjectId(0L);
                            }
                        } else {
                            task.setProjectId(0L);
                        }
                        
                        task.setSelectedMixRatios(mixRatios);
                        task.setExperimentAssignments(experimentAssignments);
                        task.setExperimentType("MIXTURE"); // 设置实验类型为混合料实验
                        
                        // 保存到数据库
                        long id = database.experimentTaskDao().insert(task);
                        task.setId(id);
                        currentTask = task;
                    } else {
                        // 使用已存在的任务，但更新其配比和实验分配
                        existingTask.setSelectedMixRatios(mixRatios);
                        existingTask.setExperimentAssignments(experimentAssignments);
                        database.experimentTaskDao().update(existingTask);
                        currentTask = existingTask;
                    }
                    
                    // 在UI线程中更新界面
                    runOnUiThread(() -> {
                        if (mixRatios.isEmpty()) {
                            Log.w(TAG, "没有找到配比数据，但继续显示空列表");
                            // 清除加载状态
                            showLoading(false);
                            // 停止刷新动画
                            swipeRefreshLayout.setRefreshing(false);
                            // 使用空列表初始化适配器（不显示错误状态）
                            adapter = new MixtureExperimentDataAdapter(new ArrayList<>(), convertToStringKeyMap(experimentAssignments));
                            adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                            rvExperiments.setAdapter(adapter);
                            rvExperiments.setVisibility(View.VISIBLE);
                            return;
                        }
                        
                        adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(mixRatios), convertToStringKeyMap(experimentAssignments));
                        adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                        rvExperiments.setAdapter(adapter);
                        rvExperiments.setVisibility(View.VISIBLE);
                        btnSave.setEnabled(true);
                    });
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> showErrorState("解析数据错误: " + e.getMessage()));
                Log.e(TAG, "处理API数据时出错", e);
            }
        } else {
            runOnUiThread(() -> showErrorState("任务数据为空"));
        }
    }
    
    /**
     * 辅助方法，将String键的Map转换为Long键的Map
     */
    private Map<Long, List<String>> convertToLongKeyMap(Map<String, List<String>> stringKeyMap) {
        if (stringKeyMap == null) {
            Log.d(TAG, "convertToLongKeyMap: stringKeyMap为空");
            return new HashMap<>();
        }
        
        Map<Long, List<String>> longKeyMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : stringKeyMap.entrySet()) {
            try {
                // 处理包含小数点的ID，例如"18.0"
                if (entry.getKey().contains(".")) {
                    // 尝试将其解析为Double，然后转换为Long
                    double doubleKey = Double.parseDouble(entry.getKey());
                    longKeyMap.put((long) doubleKey, entry.getValue());
                    Log.d(TAG, "将浮点数键 " + entry.getKey() + " 转换为Long: " + doubleKey);
                } else {
                    // 直接解析为Long
                    longKeyMap.put(Long.parseLong(entry.getKey()), entry.getValue());
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "无法将键转换为Long: " + entry.getKey(), e);
                // 如果转换失败，则跳过该键值对
            }
        }
        return longKeyMap;
    }
    
    /**
     * 辅助方法，将Long键的Map转换为String键的Map
     */
    private Map<String, List<String>> convertToStringKeyMap(Map<Long, List<String>> longKeyMap) {
        if (longKeyMap == null) return new HashMap<>();
        
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : longKeyMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }
    
    /**
     * 辅助方法，将MixRatio列表转换为Map<String,Object>列表
     */
    private List<Map<String, Object>> convertMixRatiosToMaps(List<MixRatio> mixRatios) {
        if (mixRatios == null) return new ArrayList<>();
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (MixRatio mixRatio : mixRatios) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mixRatio.getId());
            map.put("name", mixRatio.getName());
            map.put("description", mixRatio.getDescription());
            // 添加其他需要的字段
            map.put("aggregate_percentage", mixRatio.getAggregatePercentage());
            map.put("asphalt_percentage", mixRatio.getAsphaltPercentage());
            result.add(map);
        }
        return result;
    }
    
    /**
     * 获取试件制备数据
     */
    private void fetchSpecimenData(String taskId) {
        Log.d(TAG, "获取试件制备数据: " + taskId);
        
        // 如果是通过下拉刷新调用的，不显示额外的加载指示器
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true, "获取试件制备数据...");
        }
        
        // 清除错误提示（如果有）
        TextView tvError = findViewById(R.id.tvError);
        if (tvError != null) {
            tvError.setVisibility(View.GONE);
        }
        
        MixtureTaskService taskService = ServiceCreator.create(MixtureTaskService.class);
        taskService.getSpecimenData(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                // 隐藏加载指示器
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> specimenData = response.body().getData();
                    
                    // 使用日志记录收到的数据
                    Log.d(TAG, "接收到试件数据: " + (specimenData != null ? specimenData.toString() : "null"));
                    
                    // 检查和处理主任务ID
                    if (specimenData != null && specimenData.containsKey("mainTaskId")) {
                        String mainTaskId = (String) specimenData.get("mainTaskId");
                        Log.d(TAG, "主任务ID: " + mainTaskId);
                        
                        // 如果需要，更新当前任务的ID
                        if (currentTask != null && !currentTask.getTaskId().equals(mainTaskId)) {
                            currentTask.setTaskId(mainTaskId);
                            // 保存更新后的任务ID
                            executeTaskSafely(() -> {
                                database.experimentTaskDao().update(currentTask);
                            });
                        }
                    }
                    
                    // 处理配比ID到实验类型的映射
                    if (specimenData != null && specimenData.containsKey("experimentAssignments")) {
                        try {
                            Map<String, List<String>> rawAssignments = (Map<String, List<String>>) specimenData.get("experimentAssignments");
                            Map<Long, List<String>> typedAssignments = new HashMap<>();
                            
                            // 将String类型的key转换为Long类型
                            for (Map.Entry<String, List<String>> entry : rawAssignments.entrySet()) {
                                try {
                                    Long mixRatioId = Long.parseLong(entry.getKey());
                                    typedAssignments.put(mixRatioId, entry.getValue());
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "无法将实验分配键转换为Long: " + entry.getKey(), e);
                                }
                            }
                            
                            Log.d(TAG, "配比ID到实验类型的映射: " + typedAssignments);
                            
                            // 更新当前任务的实验分配
                            if (currentTask != null) {
                                currentTask.setExperimentAssignments(typedAssignments);
                                // 保存更新后的实验分配
                                executeTaskSafely(() -> {
                                    database.experimentTaskDao().update(currentTask);
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理实验分配数据时出错", e);
                        }
                    }
                    
                    // 处理任务指派信息
                    if (specimenData != null && specimenData.containsKey("taskAssignments")) {
                        try {
                            List<Map<String, Object>> assignments = (List<Map<String, Object>>) specimenData.get("taskAssignments");
                            if (assignments != null && !assignments.isEmpty()) {
                                Log.d(TAG, "任务指派数据详情:");
                                for (Map<String, Object> assignment : assignments) {
                                    String taskId = (String) assignment.get("task_id");
                                    String taskAssignment = (String) assignment.get("task_assignment");
                                    String assignedTo = (String) assignment.get("assigned_to");
                                    String status = (String) assignment.get("status");
                                    Log.d(TAG, "  - 任务ID: " + taskId + 
                                            ", 实验类型: " + taskAssignment + 
                                            ", 指派给: " + assignedTo + 
                                            ", 状态: " + status);
                                }
                                
                                // 这里不需要额外处理，因为MixtureExperimentDataAdapter会从specimenData中提取任务指派信息
                            } else {
                                Log.w(TAG, "任务指派信息为空");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理任务指派数据时出错", e);
                        }
                    } else {
                        Log.d(TAG, "没有找到任务指派信息");
                    }
                    
                    // 从methodsAndRatios创建MixRatio对象列表
                    List<MixRatio> mixRatios = new ArrayList<>();
                    
                    // 打印methodsAndRatios的详细内容并处理数据
                    if (specimenData != null && specimenData.containsKey("methodsAndRatios")) {
                        try {
                            List<Map<String, Object>> methodsData = (List<Map<String, Object>>) specimenData.get("methodsAndRatios");
                            if (methodsData != null && !methodsData.isEmpty()) {
                                Log.d(TAG, "方法和配比详细数据:");
                                for (Map<String, Object> method : methodsData) {
                                    Log.d(TAG, "  - ID: " + method.get("id"));
                                    Log.d(TAG, "    拌合温度: " + method.get("mixing_temperature"));
                                    Log.d(TAG, "    拌合速度: " + method.get("mixing_speed"));
                                    Log.d(TAG, "    拌合时间: " + method.get("mixing_time"));
                                    Log.d(TAG, "    压实方法: " + method.get("compaction_method"));
                                    
                                    // 创建MixRatio对象
                                    MixRatio mixRatio = new MixRatio();
                                    
                                    // 设置ID
                                    Object idObj = method.get("id");
                                    if (idObj instanceof Number) {
                                        mixRatio.setId(((Number) idObj).longValue());
                                    } else if (idObj instanceof String) {
                                        try {
                                            mixRatio.setId(Long.parseLong((String) idObj));
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "无法将ID转换为Long: " + idObj, e);
                                            // 使用默认ID
                                            mixRatio.setId(0L);
                                        }
                                    }
                                    
                                    // 设置名称，使用mix_name或project_id作为名称
                                    if (method.containsKey("mix_name")) {
                                        mixRatio.setName((String) method.get("mix_name"));
                                    } else if (method.containsKey("project_id")) {
                                        mixRatio.setName(String.valueOf(method.get("project_id")));
                                    } else {
                                        mixRatio.setName("配比 " + mixRatio.getId());
                                    }
                                    
                                    // 设置描述信息，使用拌合参数信息
                                    StringBuilder description = new StringBuilder();
                                    if (method.containsKey("mixing_temperature")) {
                                        description.append("拌合温度: ").append(method.get("mixing_temperature")).append("°C");
                                    }
                                    if (method.containsKey("compaction_method")) {
                                        if (description.length() > 0) description.append(", ");
                                        description.append("压实方法: ").append(method.get("compaction_method"));
                                    }
                                    mixRatio.setDescription(description.toString());
                                    
                                    // 添加到列表
                                    mixRatios.add(mixRatio);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析方法数据时出错", e);
                        }
                    }
                    
                    // 检查任务指派中是否存在ID为18的配比，如果mixRatios中不存在则添加
                    boolean hasRatio18 = false;
                    for (MixRatio ratio : mixRatios) {
                        if (ratio.getId() == 18L) {
                            hasRatio18 = true;
                            break;
                        }
                    }
                    
                    // 如果没有ID为18的配比，但任务指派中有这个ID，添加一个默认配比
                    if (!hasRatio18 && specimenData != null && specimenData.containsKey("taskAssignments")) {
                        List<Map<String, Object>> taskAssignments = (List<Map<String, Object>>) specimenData.get("taskAssignments");
                        boolean id18InTaskAssignments = false;
                        
                        if (taskAssignments != null && !taskAssignments.isEmpty()) {
                            for (Map<String, Object> assignment : taskAssignments) {
                                String taskId = (String) assignment.get("task_id");
                                if (taskId != null && taskId.equals("18")) {
                                    id18InTaskAssignments = true;
                                    break;
                                }
                            }
                        }
                        
                        if (id18InTaskAssignments) {
                            Log.d(TAG, "在任务指派中发现ID为18的配比，但mixRatios中不存在，添加一个默认配比");
                            MixRatio ratio18 = new MixRatio();
                            ratio18.setId(18L);
                            ratio18.setName("配比 18");
                            ratio18.setDescription("从任务指派自动创建的配比");
                            mixRatios.add(ratio18);
                            
                            // 确保experimentAssignments中有这个配比ID
                            if (currentTask != null) {
                                Map<Long, List<String>> assignments = currentTask.getExperimentAssignments();
                                if (!assignments.containsKey(18L)) {
                                    List<String> defaultAssignments = new ArrayList<>();
                                    for (Map<String, Object> assignment : taskAssignments) {
                                        String experimentType = (String) assignment.get("task_assignment");
                                        if (experimentType != null && !defaultAssignments.contains(experimentType)) {
                                            defaultAssignments.add(experimentType);
                                        }
                                    }
                                    
                                    if (!defaultAssignments.isEmpty()) {
                                        assignments.put(18L, defaultAssignments);
                                        currentTask.setExperimentAssignments(assignments);
                                        executeTaskSafely(() -> database.experimentTaskDao().update(currentTask));
                                    }
                                }
                            }
                        }
                    }
                    
                    // 打印设备信息
                    if (specimenData != null && specimenData.containsKey("mixingEquipment")) {
                        try {
                            List<Map<String, Object>> mixingEquip = (List<Map<String, Object>>) specimenData.get("mixingEquipment");
                            if (mixingEquip != null && !mixingEquip.isEmpty()) {
                                Log.d(TAG, "拌合设备详细数据:");
                                for (Map<String, Object> device : mixingEquip) {
                                    Log.d(TAG, "  - 设备ID: " + device.get("deviceId"));
                                    Log.d(TAG, "    制造商: " + device.get("manufacturer"));
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析拌合设备数据时出错", e);
                        }
                    }
                    
                    if (specimenData != null && specimenData.containsKey("formingEquipment")) {
                        try {
                            List<Map<String, Object>> formingEquip = (List<Map<String, Object>>) specimenData.get("formingEquipment");
                            if (formingEquip != null && !formingEquip.isEmpty()) {
                                Log.d(TAG, "成型设备详细数据:");
                                for (Map<String, Object> device : formingEquip) {
                                    Log.d(TAG, "  - 设备ID: " + device.get("deviceId"));
                                    Log.d(TAG, "    制造商: " + device.get("manufacturer"));
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析成型设备数据时出错", e);
                        }
                    }
                    
                    if (specimenData != null && !specimenData.isEmpty()) {
                        // 在UI线程中更新界面
                        runOnUiThread(() -> {
                            if (currentTask != null) {
                                // 获取当前任务的实验分配
                                Map<Long, List<String>> assignments = currentTask.getExperimentAssignments();
                                
                                // 更新适配器
                                if (adapter == null) {
                                    // 如果没有配比数据但有实验分配，创建一个默认配比
                                    if ((mixRatios == null || mixRatios.isEmpty()) && 
                                        specimenData.containsKey("taskAssignments")) {
                                        // 提取任务指派信息以创建配比
                                        try {
                                            List<Map<String, Object>> taskAssignments = 
                                                (List<Map<String, Object>>) specimenData.get("taskAssignments");
                                            
                                            if (taskAssignments != null && !taskAssignments.isEmpty()) {
                                                // 从任务指派创建配比和实验分配
                                                Map<Long, List<String>> newAssignments = new HashMap<>();
                                                List<MixRatio> newMixRatios = new ArrayList<>();
                                                
                                                for (Map<String, Object> assignment : taskAssignments) {
                                                    // 提取mixRatioId和experimentType
                                                    String mixRatioIdStr = (String) assignment.get("task_id");
                                                    String experimentType = (String) assignment.get("task_assignment");
                                                    
                                                    if (mixRatioIdStr != null && experimentType != null) {
                                                        try {
                                                            Long mixRatioId = Long.parseLong(mixRatioIdStr);
                                                            
                                                            // 添加到实验分配
                                                            List<String> experiments = newAssignments.getOrDefault(mixRatioId, new ArrayList<>());
                                                            if (!experiments.contains(experimentType)) {
                                                                experiments.add(experimentType);
                                                                newAssignments.put(mixRatioId, experiments);
                                                            }
                                                            
                                                            // 检查是否已存在此配比
                                                            boolean ratioExists = false;
                                                            for (MixRatio ratio : newMixRatios) {
                                                                if (ratio.getId() == mixRatioId) {
                                                                    ratioExists = true;
                                                                    break;
                                                                }
                                                            }
                                                            
                                                            // 如果不存在，创建新配比
                                                            if (!ratioExists) {
                                                                MixRatio newRatio = new MixRatio();
                                                                newRatio.setId(mixRatioId);
                                                                newRatio.setName("配比 " + mixRatioId);
                                                                newRatio.setDescription("从任务指派生成的配比");
                                                                newMixRatios.add(newRatio);
                                                            }
                                                        } catch (NumberFormatException e) {
                                                            Log.e(TAG, "无法将混合比ID转换为Long: " + mixRatioIdStr, e);
                                                        }
                                                    }
                                                }
                                                
                                                // 如果成功创建了配比和分配，更新当前任务
                                                if (!newMixRatios.isEmpty() && !newAssignments.isEmpty()) {
                                                    Log.d(TAG, "从任务指派创建了 " + newMixRatios.size() + " 个配比和 " + 
                                                            newAssignments.size() + " 个实验分配");
                                                    
                                                    // 更新当前任务
                                                    currentTask.setExperimentAssignments(newAssignments);
                                                    executeTaskSafely(() -> {
                                                        database.experimentTaskDao().update(currentTask);
                                                    });
                                                    
                                                    // 创建适配器
                                                    adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(newMixRatios), convertToStringKeyMap(newAssignments));
                                                    adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                                                    rvExperiments.setAdapter(adapter);
                                                    adapter.updateSpecimenData(specimenData);
                                                    
                                                    // 更新UI状态
                                                    rvExperiments.setVisibility(View.VISIBLE);
                                                    btnSave.setEnabled(true);
                                                    swipeRefreshLayout.setRefreshing(false);
                                                    return; // 跳过下面的默认适配器创建
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "从任务指派创建配比时出错", e);
                                        }
                                    }
                                }
                                
                                // 更新适配器
                                if (mixRatios.isEmpty()) {
                                    Log.w(TAG, "没有找到配比数据，但继续显示空列表");
                                    // 清除加载状态
                                    showLoading(false);
                                    // 停止刷新动画
                                    swipeRefreshLayout.setRefreshing(false);
                                    // 使用空列表初始化适配器（不显示错误状态）
                                    adapter = new MixtureExperimentDataAdapter(new ArrayList<>(), convertToStringKeyMap(assignments));
                                    adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                                    rvExperiments.setAdapter(adapter);
                                    rvExperiments.setVisibility(View.VISIBLE);
                                    return;
                                }
                                
                                // 更新当前任务的配比数据
                                currentTask.setSelectedMixRatios(mixRatios);
                                executeTaskSafely(() -> {
                                    database.experimentTaskDao().update(currentTask);
                                });
                                
                                // 更新适配器
                                adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(mixRatios), convertToStringKeyMap(assignments));
                                adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                                rvExperiments.setAdapter(adapter);
                                adapter.updateSpecimenData(specimenData);
                                rvExperiments.setVisibility(View.VISIBLE);
                                btnSave.setEnabled(true);
                                
                                // 显示成功状态
                                Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                                    "数据加载成功", Toast.LENGTH_SHORT).show();
                                
                                // 停止刷新动画
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "获取试件数据失败";
                    Log.e(TAG, errorMsg);
                    showErrorState(errorMsg);
                    // 停止刷新动画
                    swipeRefreshLayout.setRefreshing(false);
                    
                    // 显示空列表而不是错误提示
                    setupEmptyState(taskId);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // 隐藏加载指示器
                showLoading(false);
                
                Log.e(TAG, "获取试件数据时网络错误", t);
                runOnUiThread(() -> {
                    Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                        "获取试件制备数据失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
                // 停止刷新动画
                swipeRefreshLayout.setRefreshing(false);
                
                // 显示空列表而不是错误提示
                setupEmptyState(taskId);
            }
        });
    }
    
    /**
     * 显示加载指示器
     */
    private void showLoading(boolean show) {
        // 在UI线程中执行
        runOnUiThread(() -> {
            // 获取进度条
            ProgressBar progressBar = findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            
            // 如果正在加载，显示Toast提示
            if (show) {
                Toast.makeText(this, "正在加载数据...", Toast.LENGTH_SHORT).show();
            }
            
            // 禁用/启用界面交互
            if (btnSave != null) {
                btnSave.setEnabled(!show);
            }
            if (btnBack != null) {
                btnBack.setClickable(!show);
            }
            if (rvExperiments != null) {
                rvExperiments.setEnabled(!show);
            }
        });
    }
    
    /**
     * 显示加载指示器并附带消息
     */
    private void showLoading(boolean show, String message) {
        // 在UI线程中执行
        runOnUiThread(() -> {
            // 获取进度条
            ProgressBar progressBar = findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            
            // 如果正在加载，显示Toast提示
            if (show) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
            
            // 禁用/启用界面交互
            if (btnSave != null) {
                btnSave.setEnabled(!show);
            }
            if (btnBack != null) {
                btnBack.setClickable(!show);
            }
            if (rvExperiments != null) {
                rvExperiments.setEnabled(!show);
            }
        });
    }
    
    /**
     * 显示错误状态，但保持界面可用
     */
    private void showErrorState(String errorMessage) {
        // 在UI线程中执行
        runOnUiThread(() -> {
            // 获取进度条和错误文本
            ProgressBar progressBar = findViewById(R.id.progressBar);
            TextView tvError = findViewById(R.id.tvError);
            
            // 隐藏进度条
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            // 显示错误文本（如果存在）
            if (tvError != null) {
                tvError.setText(errorMessage);
                tvError.setVisibility(View.VISIBLE);
            }
            
            // 确保刷新控件不在刷新状态
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            
            // 日志记录错误
            Log.e(TAG, "错误状态: " + errorMessage);
            
            // 允许用户尝试刷新
            if (rvExperiments != null) {
                rvExperiments.setVisibility(View.VISIBLE);
                rvExperiments.setEnabled(true);
            }
            
            // 禁用保存按钮
            if (btnSave != null) {
                btnSave.setEnabled(false);
            }
            
            // 返回按钮仍然可用
            if (btnBack != null) {
                btnBack.setClickable(true);
            }
        });
    }

    /**
     * 设置空状态界面，但允许用户下拉刷新
     */
    private void setupEmptyState(String taskId) {
        runOnUiThread(() -> {
            // 设置空适配器
            adapter = new MixtureExperimentDataAdapter(new ArrayList<>(), new HashMap<>());
            adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
            rvExperiments.setAdapter(adapter);
            rvExperiments.setVisibility(View.VISIBLE);
            
            // 禁用保存按钮
            btnSave.setEnabled(false);
            
            // 确保刷新控件可用
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // 清除错误状态
                TextView tvError = findViewById(R.id.tvError);
                if (tvError != null) {
                    tvError.setVisibility(View.GONE);
                }
                
                // 重新加载数据
                if (taskId != null && !taskId.isEmpty()) {
                    fetchSpecimenData(taskId);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        });
    }

    @Override
    public void onDeviceScanRequested(int position, String experimentName) {
        currentScanPosition = position;
        // 启动扫描
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请扫描实验设备二维码");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            handleScanResult(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleScanResult(String deviceCode) {
        if (currentScanPosition != -1 && adapter != null) {
            try {
                // 解析设备信息
                DeviceInfo deviceInfo = new Gson().fromJson(deviceCode, DeviceInfo.class);
                if (deviceInfo != null) {
                    adapter.setDeviceInfo(currentScanPosition, deviceInfo);
                } else {
                    Toast.makeText(this, "无效的设备码", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing device code", e);
                Toast.makeText(this, "设备码格式错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveExperimentData() {
        if (adapter != null && currentTask != null) {
            // 获取适配器中的实验数据
            Map<String, Map<String, String>> experimentData = adapter.getExperimentData();
            
            // 获取适配器中的有效实验分配信息
            final Map<String, List<String>> validExperimentAssignments = adapter.getValidExperimentAssignments();
            
            executeTaskSafely(() -> {
                try {
                    // 更新任务状态
                    currentTask.setExperimentCompletionTime(System.currentTimeMillis());
                    currentTask.setStatus("已完成");
                    currentTask.setExperimentType("MIXTURE"); // 设置实验类型为混合料实验
                    
                    // 保存实验数据
                    for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                        String mixRatioId = entry.getKey();
                        Map<String, String> experiments = entry.getValue();
                        
                        // 检查这个配比是否有实验分配
                        List<String> assignedExperiments = validExperimentAssignments.get(mixRatioId);
                        if (assignedExperiments == null || assignedExperiments.isEmpty()) {
                            Log.w(TAG, "配比ID " + mixRatioId + " 没有指派的实验，跳过保存");
                            continue;
                        }

                        // 只保存被指派给该配比的实验
                        for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                            String experimentName = experimentEntry.getKey();
                            String value = experimentEntry.getValue();
                            
                            // 检查实验是否被指派给该配比
                            if (!assignedExperiments.contains(experimentName)) {
                                Log.w(TAG, "跳过保存：实验 '" + experimentName + "' 未被指派给配比ID " + mixRatioId);
                                continue;
                            }
                            
                            if (value != null && !value.trim().isEmpty()) {
                                ExperimentData data = new ExperimentData();
                                data.setTaskId(currentTask.getId());
                                data.setExperimentName(experimentName);
                                data.setMixRatio(mixRatioId);
                                data.setInput1Label("测量值");
                                data.setInput1Value(Double.parseDouble(value));
                                
                                // 设置设备信息
                                DeviceInfo deviceInfo = adapter.getDeviceInfo(Integer.parseInt(mixRatioId));
                                if (deviceInfo != null) {
                                    data.setDeviceId(deviceInfo.getDeviceId());
                                    data.setDeviceManufacturer(deviceInfo.getManufacturer());
                                    data.setDeviceModel(deviceInfo.getModel());
                                }
                                
                                // 保存到数据库
                                database.experimentDataDao().insert(data);
                                Log.i(TAG, "已保存配比 " + mixRatioId + " 的实验 '" + experimentName + "' 数据");
                            }
                        }
                    }
                    
                    // 更新任务
                    database.experimentTaskDao().update(currentTask);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "实验数据保存成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error saving experiment data", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "保存数据时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 不立即关闭executor，而是有序关闭
        if (executor != null && !executor.isShutdown()) {
            // 拒绝接受新任务，但是会处理队列中已有的任务
            executor.shutdown();
            try {
                // 等待所有任务完成，最多等待2秒
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    // 如果等待超时，则强制关闭
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // 如果等待过程中线程被中断，则强制关闭
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // 辅助方法，安全执行任务
    private void executeTaskSafely(Runnable task) {
        if (executor != null && !executor.isShutdown() && !executor.isTerminated()) {
            try {
                executor.execute(task);
            } catch (RejectedExecutionException e) {
                Log.e(TAG, "任务执行被拒绝，线程池可能已关闭", e);
                executeTaskInNewThread(task);
            }
        } else {
            // 如果executor已关闭，则创建新线程执行任务
            Log.w(TAG, "线程池已关闭，创建新线程执行任务");
            executeTaskInNewThread(task);
        }
    }
    
    // 在新线程中执行任务
    private void executeTaskInNewThread(Runnable task) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Log.e(TAG, "在新线程执行任务时出错", e);
                // 在UI线程显示错误消息
                runOnUiThread(() -> {
                    Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                            "执行任务时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
