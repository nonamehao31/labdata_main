package com.example.labdata_main;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;

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
import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Date;
import java.sql.Timestamp;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.labdata_main.model.CompletedExperimentTask;

public class RecordMixtureExperimentDataActivity extends AppCompatActivity 
    implements MixtureExperimentDataAdapter.OnDeviceScanRequestListener {
    
    private static final String TAG = "RecordMixtureExperimentDataActivity";
    private RecyclerView rvExperiments;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private AppDatabase database;
    private MixtureExperimentDataAdapter adapter;
    private ExecutorService executor;
    private ExperimentTask currentTask;
    private int currentScanPosition = -1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MixtureTaskService mixtureTaskService;
    private ArrayList<Map<String, String>> savedExperimentData; // 保存实验数据的状态
    private String selectedAssignment; // 用户在上一个界面选择的实验指派
    private String currentTaskId; // 当前任务ID

    private  Gson gson = new Gson();

    /**
     * API回调接口
     */
    public interface APICallback {
        void onSuccess(JSONObject response);
        void onError(Exception e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mixture_experiment_data);

        
        // 初始化网络服务
        mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        // 初始化数据库和线程池
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
    
        // 初始化视图
        initViews();
    
        // 获取传入的任务数据
        CompletedExperimentTask task = (CompletedExperimentTask) getIntent().getSerializableExtra("task");
        // 获取选定的实验指派
        selectedAssignment = getIntent().getStringExtra("selectedAssignment");
        
        // 如果使用旧的启动方式（直接传递taskId）
        String taskId = getIntent().getStringExtra("taskId");
        
        if (task != null) {
            // 使用新的启动方式：从任务对象获取ID
            taskId = task.getTaskId();
            Log.d(TAG, "从任务对象获取ID: " + taskId);
            
            // 如果没有指定selectedAssignment，尝试从任务中获取
            if (selectedAssignment == null || selectedAssignment.isEmpty()) {
                // 根据记忆，优先使用taskAssignment，然后是taskName
                selectedAssignment = task.getTaskAssignment();
                if (selectedAssignment == null || selectedAssignment.isEmpty()) {
                    selectedAssignment = task.getTaskName();
                }
                Log.d(TAG, "从任务对象获取实验指派: " + selectedAssignment);
            }
        }
        
        if (taskId != null && !taskId.isEmpty()) {
            // 保存当前任务ID
            currentTaskId = taskId;
            Log.d(TAG, "保存当前任务ID: " + currentTaskId);
            
            // 先检查任务状态，如果已完成则显示提示并退出
            // 临时替换调用以避免编译错误
            // checkTaskStatus(currentTaskId);
            loadTaskData(currentTaskId, true);
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
            // 使用保存的currentTaskId，而不是每次从Intent获取
            if (currentTaskId != null && !currentTaskId.isEmpty()) {
                Log.d(TAG, "下拉刷新，使用当前任务ID: " + currentTaskId);
                // 刷新数据
                fetchSpecimenData(currentTaskId);
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
     * @param refreshUI 是否刷新UI界面（在屏幕旋转后恢复时应为false）
     */
    private void loadTaskData(String taskId, boolean refreshUI) {
        if (refreshUI) {
            showLoading(true, "加载任务信息...");
        }
        
        MixtureTaskService taskService = ServiceCreator.createMixtureTaskService();
        taskService.getMixtureTaskByTaskId(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    
                    // 处理从API获取的任务数据
                    processApiTaskData(data, taskId);
                    
                    // 获取试件制备数据（第二步）
                    if (refreshUI) {
                        fetchSpecimenData(taskId);
                    }
                } else {
                    // 隐藏加载指示器
                    if (refreshUI) {
                        showLoading(false);
                        
                        String errorMsg = response.body() != null ? response.body().getMessage() : "加载任务数据失败";
                        showErrorState(errorMsg);
                    }
                    Log.e(TAG, "加载任务数据失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    
                    // 确保刷新控件不在刷新状态
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    // 显示空界面但允许下拉刷新
                    if (refreshUI) {
                        setupEmptyState(taskId);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // 隐藏加载指示器
                if (refreshUI) {
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
                            // 恢复临时数据
                            restoreTemporaryData();
                            return;
                        }
                        
                        Map<Long, List<String>> filteredAssignments = experimentAssignments;
                        
                        if (selectedAssignment != null && !selectedAssignment.isEmpty()) {
                            Log.d(TAG, "根据选定的实验指派过滤: " + selectedAssignment);
                            // 为每个配比过滤实验列表，只保留选定的实验
                            filteredAssignments = new HashMap<>();
                            for (Map.Entry<Long, List<String>> entry : experimentAssignments.entrySet()) {
                                Long mixRatioId = entry.getKey();
                                List<String> experimentList = new ArrayList<>();
                                
                                for (String experiment : entry.getValue()) {
                                    // 检查实验名称是否包含或匹配选定的实验
                                    if (experiment.contains(selectedAssignment) || 
                                        selectedAssignment.contains(experiment) ||
                                        experiment.equalsIgnoreCase(selectedAssignment)) {
                                        experimentList.add(experiment);
                                        Log.d(TAG, "为配比 " + mixRatioId + " 添加匹配的实验: " + experiment);
                                    }
                                }
                                
                                if (!experimentList.isEmpty()) {
                                    filteredAssignments.put(mixRatioId, experimentList);
                                }
                            }
                            
                            // 如果过滤后没有实验，提示用户并显示全部实验（兜底方案）
                            if (filteredAssignments.isEmpty()) {
                                Log.w(TAG, "没有找到匹配的实验指派: " + selectedAssignment + "，显示所有实验");
                                Toast.makeText(this, "未找到指定的实验: " + selectedAssignment + "，将显示所有可用实验", Toast.LENGTH_SHORT).show();
                                filteredAssignments = experimentAssignments;
                            } else {
                                Log.d(TAG, "使用过滤后的实验指派列表: " + filteredAssignments.size() + " 个配比");
                                
                                // 修改标题，显示当前选择的实验
                                setTitle("记录 " + selectedAssignment + " 数据");
                            }
                        }
                        
                        adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(mixRatios), convertToStringKeyMap(filteredAssignments), selectedAssignment);
                        adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                        rvExperiments.setAdapter(adapter);
                        rvExperiments.setVisibility(View.VISIBLE);
                        // 恢复临时数据
                        restoreTemporaryData();
                        btnSave.setEnabled(true);
                        
                        // 适配器准备好后，加载已保存的实验数据
                        for (MixRatio mixRatio : mixRatios) {
                            String mixRatioId = String.valueOf(mixRatio.getId());
                            List<String> experiments = filteredAssignments.get(mixRatio.getId());
                            
                            // 检查这个配比是否分配了沥青混合料弯曲试验
                            if (experiments != null && experiments.contains("沥青混合料弯曲试验")) {
                                Log.d(TAG, "为配比 " + mixRatioId + " 加载沥青混合料弯曲试验数据");
                                loadMixtureBendingTestData(taskId, mixRatioId);
                            }
                        }
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
     * 辅助方法，将String键的Map转换为Long键的Map
     */
    private Map<Long, List<String>> convertStringKeysToLong(Map<String, List<String>> map) {
        Map<Long, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            // 处理复合键，如"7_18"，只取第一部分作为配比ID
            if (key.contains("_")) {
                key = key.split("_")[0];
            }
            
            try {
                Long longKey = Long.parseLong(key);
                // 如果结果Map中已存在该键，合并列表
                if (result.containsKey(longKey)) {
                    List<String> existingList = result.get(longKey);
                    for (String item : entry.getValue()) {
                        if (!existingList.contains(item)) {
                            existingList.add(item);
                        }
                    }
                } else {
                    result.put(longKey, new ArrayList<>(entry.getValue()));
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "无法将键转换为Long: " + key, e);
            }
        }
        return result;
    }
    
    /**
     * 计算所有实验的总数
     */
    private int countExperiments(Map<Long, List<String>> assignments) {
        int count = 0;
        for (List<String> experiments : assignments.values()) {
            count += experiments.size();
        }
        return count;
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
        
        MixtureTaskService taskService = ServiceCreator.createMixtureTaskService();
        taskService.getSpecimenData(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                // 隐藏加载指示器
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> specimenData = response.body().getData();
                    
                    // 使用日志记录收到的数据
                    Log.d(TAG, "接收到试件数据: " + (specimenData != null ? specimenData.toString() : "null"));
                    
                    // 检查任务指派中是否存在ID为18的配比，如果mixRatios中不存在则添加
                    boolean hasRatio18 = false;
                    for (MixRatio ratio : adapter.getMixRatioObjects()) {
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
                            adapter.addMixRatio(ratio18);
                            
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
                                        executeTaskSafely(() -> {
                                            database.experimentTaskDao().update(currentTask);
                                        });
                                    }
                                }
                            }
                        }
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
                    
                    if (specimenData != null && !specimenData.isEmpty()) {
                        // 在UI线程中更新界面
                        runOnUiThread(() -> {
                            if (currentTask != null) {
                                // 获取当前任务的实验分配
                                Map<Long, List<String>> assignments = currentTask.getExperimentAssignments();
                                
                                // 如果服务器返回了experiment_assignments数据，替换本地数据并应用过滤
                                if (specimenData.containsKey("experimentAssignments")) {
                                    Map<String, List<String>> serverAssignments = (Map<String, List<String>>) specimenData.get("experimentAssignments");
                                    if (serverAssignments != null && !serverAssignments.isEmpty()) {
                                        // 如果用户选择了特定实验类型，对服务器返回的实验类型列表进行过滤
                                        if (selectedAssignment != null && !selectedAssignment.isEmpty()) {
                                            Log.d(TAG, "对服务器返回的实验类型列表应用过滤: " + selectedAssignment);
                                            Map<String, List<String>> filteredServerAssignments = new HashMap<>();
                                            
                                            for (Map.Entry<String, List<String>> entry : serverAssignments.entrySet()) {
                                                String mixRatioId = entry.getKey();
                                                List<String> filteredExperiments = new ArrayList<>();
                                                
                                                for (String experiment : entry.getValue()) {
                                                    // 只保留匹配用户选择的实验
                                                    if (experiment.contains(selectedAssignment) || 
                                                        selectedAssignment.contains(experiment) ||
                                                        experiment.equalsIgnoreCase(selectedAssignment)) {
                                                        filteredExperiments.add(experiment);
                                                        Log.d(TAG, "为配比 " + mixRatioId + " 过滤后保留实验: " + experiment);
                                                    }
                                                }
                                                
                                                // 只有当有匹配的实验时才添加到过滤结果中
                                                if (!filteredExperiments.isEmpty()) {
                                                    filteredServerAssignments.put(mixRatioId, filteredExperiments);
                                                }
                                            }
                                            
                                            // 使用过滤后的服务器数据转换为Long键的Map并更新任务
                                            assignments = convertStringKeysToLong(filteredServerAssignments);
                                            currentTask.setExperimentAssignments(assignments);
                                        }
                                        
                                        // 将过滤后的服务器数据转换为Long键的Map并更新任务
                                        assignments = convertStringKeysToLong(serverAssignments);
                                        currentTask.setExperimentAssignments(assignments);
                                    }
                                }
                                
                                // 如果有选定的实验类型，过滤assignments只保留匹配的实验
                                if (selectedAssignment != null && !selectedAssignment.isEmpty()) {
                                    Map<Long, List<String>> filteredAssignments = new HashMap<>();
                                    
                                    for (Map.Entry<Long, List<String>> entry : assignments.entrySet()) {
                                        Long mixRatioId = entry.getKey();
                                        List<String> experimentList = new ArrayList<>();
                                        
                                        for (String experiment : entry.getValue()) {
                                            // 检查实验名称是否包含或匹配选定的实验
                                            if (experiment.contains(selectedAssignment) || 
                                                selectedAssignment.contains(experiment) ||
                                                experiment.equalsIgnoreCase(selectedAssignment)) {
                                                experimentList.add(experiment);
                                                Log.d(TAG, "为配比 " + mixRatioId + " 添加匹配的实验: " + experiment);
                                            }
                                        }
                                        
                                        if (!experimentList.isEmpty()) {
                                            filteredAssignments.put(mixRatioId, experimentList);
                                        }
                                    }
                                    
                                    if (!filteredAssignments.isEmpty()) {
                                        Log.d(TAG, "已过滤实验分配数据: 从 " + countExperiments(assignments) + 
                                              " 个实验过滤为 " + countExperiments(filteredAssignments) + " 个实验");
                                        assignments = filteredAssignments;
                                    } else {
                                        Log.w(TAG, "过滤后没有找到匹配的实验，使用原始数据");
                                    }
                                }
                                
                                // 更新适配器
                                adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(mixRatios), convertToStringKeyMap(assignments), selectedAssignment);
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
     * 加载沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId
     */
    private void loadMixtureBendingTestData(String taskId, String mixRatioId) {
        if (mixtureTaskService == null) {
            Log.e(TAG, "mixtureTaskService 为 null，无法加载沥青混合料弯曲试验数据");
            return;
        }
        
        try {
            Long mixRatioIdLong = Long.parseLong(mixRatioId);
            Log.d(TAG, "开始加载沥青混合料弯曲试验数据: taskId=" + taskId + ", mixRatioId=" + mixRatioId);
            
            mixtureTaskService.getMixtureBendingTestByTaskIdAndMixRatioId(taskId, mixRatioIdLong)
                    .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                               Response<ApiResponse<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Map<String, Object> data = response.body().getData();
                                if (data != null) {
                                    Log.d(TAG, "成功获取沥青混合料弯曲试验数据: " + data);
                                    // 解析数据
                                    try {
                                        // 获取基本参数
                                        Float spanLength = data.get("spanLength") != null ? 
                                                Float.parseFloat(data.get("spanLength").toString()) : null;
                                        
                                        // 获取平均值
                                        Float avgFlexuralStrength = data.get("averageFlexuralStrength") != null ? 
                                                Float.parseFloat(data.get("averageFlexuralStrength").toString()) : null;
                                        Float avgMaxStrain = data.get("averageMaxStrain") != null ? 
                                                Float.parseFloat(data.get("averageMaxStrain").toString()) : null;
                                        Float avgStiffnessModulus = data.get("averageStiffnessModulus") != null ? 
                                                Float.parseFloat(data.get("averageStiffnessModulus").toString()) : null;
                                        
                                        // 提取试件JSON数据
                                        String specimensJson = data.get("specimens") != null ? 
                                                data.get("specimens").toString() : null;
                                        
                                        if (specimensJson != null) {
                                            // 解析JSON数据
                                            try {
                                                Gson gson = new Gson();
                                                List<?> specimensList = gson.fromJson(specimensJson, List.class);
                                                
                                                // 更新UI
                                                if (adapter != null) {
                                                    // 更新跨径长度
                                                    if (spanLength != null) {
                                                        adapter.updateExperimentData(mixRatioId, "沥青混合料弯曲试验_跨径长度L", 
                                                                String.valueOf(spanLength));
                                                    }
                                                    
                                                    // 更新每个试件的数据
                                                    for (int i = 0; i < specimensList.size(); i++) {
                                                        Map<String, Object> specimen = (Map<String, Object>) specimensList.get(i);
                                                        String prefix = "沥青混合料弯曲试验_试件" + (i + 1) + "_";
                                                        
                                                        // 更新试件尺寸
                                                        if (specimen.get("width") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "宽度b", 
                                                                    specimen.get("width").toString());
                                                        }
                                                        if (specimen.get("height") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "高度h", 
                                                                    specimen.get("height").toString());
                                                        }
                                                        
                                                        // 更新试验结果
                                                        if (specimen.get("maxLoad") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "最大荷载P", 
                                                                    specimen.get("maxLoad").toString());
                                                        }
                                                        if (specimen.get("deflection") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "跨中挠度d", 
                                                                    specimen.get("deflection").toString());
                                                        }
                                                        
                                                        // 更新计算结果
                                                        if (specimen.get("flexuralStrength") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "抗弯拉强度R", 
                                                                    specimen.get("flexuralStrength").toString());
                                                        }
                                                        if (specimen.get("maxStrain") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "最大弯拉应变ε", 
                                                                    specimen.get("maxStrain").toString());
                                                        }
                                                        if (specimen.get("stiffnessModulus") != null) {
                                                            adapter.updateExperimentData(mixRatioId, prefix + "弯曲劲度模量S", 
                                                                    specimen.get("stiffnessModulus").toString());
                                                        }
                                                    }
                                                    
                                                    // 更新平均值
                                                    if (avgFlexuralStrength != null) {
                                                        adapter.updateExperimentData(mixRatioId, "沥青混合料弯曲试验_平均抗弯拉强度", 
                                                                String.valueOf(avgFlexuralStrength));
                                                    }
                                                    if (avgMaxStrain != null) {
                                                        adapter.updateExperimentData(mixRatioId, "沥青混合料弯曲试验_平均最大弯拉应变", 
                                                                String.valueOf(avgMaxStrain));
                                                    }
                                                    if (avgStiffnessModulus != null) {
                                                        adapter.updateExperimentData(mixRatioId, "沥青混合料弯曲试验_平均弯曲劲度模量", 
                                                                String.valueOf(avgStiffnessModulus));
                                                    }
                                                    
                                                    // 通知适配器数据已更新
                                                    adapter.notifyDataSetChanged();
                                                    Log.d(TAG, "沥青混合料弯曲试验数据已加载到UI");
                                                } else {
                                                    Log.e(TAG, "adapter为null，无法更新UI");
                                                }
                                            } catch (JsonSyntaxException e) {
                                                Log.e(TAG, "解析试件JSON数据失败: " + e.getMessage(), e);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "处理沥青混合料弯曲试验数据时出错: " + e.getMessage(), e);
                                    }
                                } else {
                                    Log.w(TAG, "沥青混合料弯曲试验数据为空");
                                }
                            } else {
                                Log.w(TAG, "获取沥青混合料弯曲试验数据失败: " + 
                                        (response.body() != null ? response.body().getMessage() : "未知错误"));
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "获取沥青混合料弯曲试验数据请求失败: " + t.getMessage(), t);
                        }
                    });
        } catch (NumberFormatException e) {
            Log.e(TAG, "解析配比ID失败: " + e.getMessage(), e);
        }
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
                    // 设置设备ID为当前位置
                    if (deviceInfo.getDeviceId() == null || deviceInfo.getDeviceId().trim().isEmpty()) {
                        deviceInfo.setDeviceId(String.valueOf(currentScanPosition + 1));
                        Log.d(TAG, "设备ID为空，设置为: " + deviceInfo.getDeviceId());
                    }
                    
                    // 更新适配器中的设备信息
                    adapter.setDeviceInfo(currentScanPosition, deviceInfo);
                    Log.d(TAG, "已更新适配器中的设备信息: " + deviceInfo.getManufacturer() + " " + deviceInfo.getModel());
                    
                    // 获取当前配比ID
                    Map<String, Object> mixRatio = adapter.getMixRatios().get(currentScanPosition);
                    Object idObj = mixRatio.get("id");
                    Long mixRatioId = null;
                    
                    try {
                        if (idObj instanceof Number) {
                            mixRatioId = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            mixRatioId = Long.parseLong((String) idObj);
                        }
                        Log.d(TAG, "获取到配比ID: " + mixRatioId);
                    } catch (Exception e) {
                        Log.e(TAG, "转换配比ID时出错", e);
                    }
                    
                    if (mixRatioId != null) {
                        // 保存设备信息到后端
                        saveDeviceInfo(mixRatioId, deviceInfo);
                        Log.d(TAG, "正在保存配比ID " + mixRatioId + " 的设备信息到后端");
                    } else {
                        Log.w(TAG, "无法保存设备信息到后端: 配比ID为null");
                    }
                    
                    // 显示成功提示
                    Toast.makeText(this, "设备扫描成功：" + deviceInfo.getManufacturer() + " " + deviceInfo.getModel(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "解析设备信息失败: 返回null");
                    Toast.makeText(this, "无效的设备码", Toast.LENGTH_SHORT).show();
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "解析设备码JSON格式错误", e);
                Toast.makeText(this, "设备码格式错误，请确认是否为正确的设备码", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "处理设备码时出现未知错误", e);
                Toast.makeText(this, "处理设备码出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "无法处理扫描结果: currentScanPosition=" + currentScanPosition + ", adapter=" + (adapter != null ? "不为null" : "为null"));
        }
    }
    
    /**
     * 保存设备信息到后端
     * 
     * @param mixRatioId 配比ID
     * @param deviceInfo 设备信息
     */
    private void saveDeviceInfo(long mixRatioId, DeviceInfo deviceInfo) {
        if (deviceInfo == null || currentTask == null) {
            Log.e(TAG, "保存设备信息失败: deviceInfo或currentTask为空");
            return;
        }

        try {
            Log.d(TAG, "开始保存设备信息: mixRatioId=" + mixRatioId + ", type=" + deviceInfo.getType() 
                  + ", model=" + deviceInfo.getModel() + ", manufacturer=" + deviceInfo.getManufacturer());
            
            // 使用更新的saveDeviceInfo方法，直接调用后端正确的endpoint
            mixtureTaskService.saveDeviceInfo(
                currentTask.getTaskId(),
                deviceInfo.getType(),
                deviceInfo.getModel(),
                deviceInfo.getManufacturer()
            ).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "设备信息保存成功: " + response.body().getData());
                        Toast.makeText(RecordMixtureExperimentDataActivity.this, "设备信息保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "设备信息保存失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                        Toast.makeText(RecordMixtureExperimentDataActivity.this, "设备信息保存失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "设备信息保存请求失败", t);
                    Toast.makeText(RecordMixtureExperimentDataActivity.this, "设备信息保存请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
            // 同时保存设备信息到本地适配器，确保UI也会更新
            if (adapter != null) {
                adapter.setDeviceInfo((int) mixRatioId, deviceInfo);
                Log.d(TAG, "设备信息已保存到本地适配器: 配比ID=" + mixRatioId);
            }
        } catch (Exception e) {
            Log.e(TAG, "保存设备信息时出错", e);
            Toast.makeText(this, "保存设备信息时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取当前配比对应的任务信息
     * @param mixRatioId 配比ID
     * @return 任务信息Map
     */
    private Map<String, Object> getCurrentTaskInfo(Long mixRatioId) {
        if (mixRatioId == null) {
            Log.w(TAG, "获取任务信息失败: mixRatioId为null");
            return null;
        }
        
        // 确保adapter和mixRatioTaskAssignments不为null
        if (adapter != null && adapter.getMixRatioTaskAssignments() != null) {
            List<Map<String, Object>> tasks = adapter.getMixRatioTaskAssignments().get(String.valueOf(mixRatioId));
            if (tasks != null && !tasks.isEmpty()) {
                Log.d(TAG, "找到配比" + mixRatioId + "对应的任务信息: " + tasks.get(0));
                return tasks.get(0);
            } else {
                Log.w(TAG, "未找到配比" + mixRatioId + "对应的任务");
            }
        } else {
            Log.w(TAG, "适配器为null或未初始化mixRatioTaskAssignments");
        }
        
        // 如果没有找到任务信息，返回基本任务信息
        Map<String, Object> basicTaskInfo = new HashMap<>();
        basicTaskInfo.put("id", currentTask != null ? currentTask.getTaskId() : "");
        Log.d(TAG, "使用基本任务信息: " + basicTaskInfo);
        return basicTaskInfo;
    }
    
    /**
     * 保存实验数据
     */
    private void saveExperimentData() {
        if (adapter != null && currentTask != null) {
            // 获取适配器中的实验数据
            Map<String, Map<String, String>> experimentData = adapter.getExperimentData();
            
            // 获取适配器中的有效实验分配信息
            final Map<String, List<String>> validExperimentAssignments = adapter.getValidExperimentAssignments();
            
            // 显示保存进度
            runOnUiThread(() -> {
                showLoading(true, "正在保存实验数据...");
                btnSave.setEnabled(false);
            });
            
            executeTaskSafely(() -> {
                try {
                    // 更新任务状态
                    currentTask.setExperimentCompletionTime(System.currentTimeMillis());
                    currentTask.setStatus("已完成");
                    currentTask.setExperimentType("MIXTURE"); // 设置实验类型为混合料实验
                    
                    // 用于跟踪已处理的马歇尔试验数据
                    Map<String, Boolean> processedMarshallTests = new HashMap<>();
                    // 用于跟踪已处理的汉堡车辙实验数据
                    Map<String, Boolean> processedHamburgTests = new HashMap<>();
                    // 用于跟踪已处理的沥青混合料弯曲试验数据
                    Map<String, Boolean> processedBendingTests = new HashMap<>();
                    // 用于跟踪已处理的动态模量试验数据
                    Map<String, Boolean> processedDynamicModulusTests = new HashMap<>();
                    // 用于跟踪已处理的沥青混合料四点弯曲疲劳寿命试验数据
                    Map<String, Boolean> processedFourPointBendingFatigueTests = new HashMap<>();
                    // 用于跟踪已处理的单轴压缩实验数据
                    Map<String, Boolean> processedSingleAxisCompressionTests = new HashMap<>();
                    // 用于跟踪已处理的劈裂试验数据
                    Map<String, Boolean> processedSplittingTests = new HashMap<>();
                    
                    
                    
                    // 保存实验数据
                    for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                        String mixRatioId = entry.getKey();
                        Map<String, String> experiments = entry.getValue();
                        
                        // 检查这个配比是否有实验分配
                        List<String> assignedExperiments = validExperimentAssignments.get(mixRatioId);
                        if (assignedExperiments == null || assignedExperiments.isEmpty()) {
                            // 如果没有指派实验但我们有selectedAssignment，使用它作为默认实验指派
                            if (selectedAssignment != null && !selectedAssignment.isEmpty()) {
                                assignedExperiments = new ArrayList<>();
                                assignedExperiments.add(selectedAssignment);
                                Log.d(TAG, "配比ID " + mixRatioId + " 没有指派实验，使用选定的实验指派: " + selectedAssignment);
                            } else {
                                Log.w(TAG, "配比ID " + mixRatioId + " 没有指派的实验，跳过保存");
                                continue;
                            }
                        }

                        // 检查是否有直接拉伸循环疲劳测黏弹损伤试验数据
                        boolean hasDirectStretchingFatigueTest = false;
                        for (Map.Entry<String, String> expEntry : experiments.entrySet()) {
                            String experimentName = expEntry.getKey();
                            if (experimentName.contains("沥青混合料直接拉伸循环疲劳测黏弹损伤试验")) {
                                hasDirectStretchingFatigueTest = true;
                                Log.d(TAG, "找到了直接拉伸循环疲劳测黏弹损伤试验数据: " + experimentName);
                                break;
                            }
                        }

                        // 检查后，记录检测结果
                        Log.d(TAG, "直接拉伸循环疲劳测黏弹损伤试验数据检测结果: " + hasDirectStretchingFatigueTest);

                        // 如果有直接拉伸循环疲劳测黏弹损伤试验数据
                        if (hasDirectStretchingFatigueTest) {
                            Log.d(TAG, "开始保存直接拉伸循环疲劳测黏弹损伤试验数据");
                            saveDirectStretchingFatigueTestData(mixRatioId, experiments);
                        }

                        // 检查是否有动态模量试验数据
                        boolean hasDynamicModulusData = false;
                        for (String key : experiments.keySet()) {
                            if (key.startsWith("动态模量试验_")) {
                                hasDynamicModulusData = true;
                                break;
                            }
                        }

                        // 如果有动态模量试验数据
                        if (hasDynamicModulusData) {
                            saveDynamicModulusTestData(mixRatioId, experiments);
                        }

                        // 检查是否有马歇尔稳定度试验数据需要保存
                        if (assignedExperiments.contains("马歇尔稳定度试验") && !processedMarshallTests.containsKey(mixRatioId)) {
                            // 提取并保存马歇尔试验数据
                            saveMarshallTestData(mixRatioId, experiments);
                            processedMarshallTests.put(mixRatioId, true);
                        }
                        
                        // 检查是否有汉堡车辙实验数据需要保存
                        if (assignedExperiments.contains("沥青混合料车辙实验（汉堡车辙）") && !processedHamburgTests.containsKey(mixRatioId)) {
                            // 提取并保存汉堡车辙实验数据
                            saveHamburgRuttingTestData(mixRatioId, experiments);
                            processedHamburgTests.put(mixRatioId, true);
                        }
                        
                        // 检查是否有沥青混合料弯曲试验数据需要保存
                        if (assignedExperiments.contains("沥青混合料弯曲试验") && !processedBendingTests.containsKey(mixRatioId)) {
                            // 提取并保存沥青混合料弯曲试验数据
                            saveMixtureBendingTestData(mixRatioId, experiments);
                            processedBendingTests.put(mixRatioId, true);
                        }
                        
                        // 检查是否有动态模量试验数据需要保存
                        if (assignedExperiments.contains("动态模量试验") && !processedDynamicModulusTests.containsKey(mixRatioId)) {
                            // 提取并保存动态模量试验数据
                            saveDynamicModulusTestData(mixRatioId, experiments);
                            processedDynamicModulusTests.put(mixRatioId, true);
                        }
                        
                        // 检查是否有沥青混合料四点弯曲疲劳寿命试验数据需要保存
                        if (assignedExperiments.contains("沥青混合料四点弯曲疲劳寿命试验") && !processedFourPointBendingFatigueTests.containsKey(mixRatioId)) {
                            // 提取并保存沥青混合料四点弯曲疲劳寿命试验数据
                            Log.d(TAG, "开始保存沥青混合料四点弯曲疲劳寿命试验数据，配比ID: " + mixRatioId);
                            saveFourPointBendingFatigueTestData(mixRatioId, experiments);
                            processedFourPointBendingFatigueTests.put(mixRatioId, true);
                        }

                        // 检查是否有单轴压缩试验数据需要保存
                        if (assignedExperiments.contains("沥青混合料单轴压缩试验(圆柱体法)") && !processedSingleAxisCompressionTests.containsKey(mixRatioId)) {
                            // 提取并保存单轴压缩试验数据
                            saveUniaxialCompressionTestData(mixRatioId, experiments);
                            processedSingleAxisCompressionTests.put(mixRatioId, true);
                        }

                        // 检查是否有劈裂试验数据需要保存
                        if (assignedExperiments.contains("沥青混合料劈裂试验") && !processedSplittingTests.containsKey(mixRatioId)) {
                            // 提取并保存劈裂试验数据
                            saveSplittingTestData(mixRatioId, experiments);
                            processedSplittingTests.put(mixRatioId, true);
                        }
                        
                        // 只保存被指派给该配比的实验
                        for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                            String experimentName = experimentEntry.getKey();
                            String value = experimentEntry.getValue();
                            
                            // 马歇尔稳定度试验数据已单独处理，跳过
                            if (experimentName.startsWith("马歇尔稳定度试验_")) {
                                continue;
                            }
                            
                            // 汉堡车辙实验数据已单独处理，跳过
                            if (experimentName.startsWith("沥青混合料车辙实验（汉堡车辙）_")) {
                                continue;
                            }
                            
                            // 沥青混合料弯曲试验数据已单独处理，跳过
                            if (experimentName.startsWith("沥青混合料弯曲试验_")) {
                                continue;
                            }

                            // 动态模量试验数据已单独处理，跳过
                            if (experimentName.startsWith("动态模量试验_")) {
                                continue;
                            }

                            // 沥青混合料四点弯曲疲劳寿命试验数据已单独处理，跳过
                            if (experimentName.startsWith("沥青混合料四点弯曲疲劳寿命试验_")) {
                                continue;
                            }

                            // 单轴压缩试验数据已单独处理，跳过
                            if (experimentName.startsWith("沥青混合料单轴压缩试验_")) {
                                continue;
                            }

                            // 棱柱劈裂试验数据已单独处理，跳过
                            if (experimentName.startsWith("棱柱劈裂试验_")) {
                                continue;
                            }
                            
                            // 检查实验是否被指派给该配比
                            boolean isAssigned = false;
                            for (String assigned : assignedExperiments) {
                                // 特殊处理单轴压缩试验的情况，由于命名不完全匹配
                                if (assigned.equals("沥青混合料单轴压缩试验(圆柱体法)") && experimentName.startsWith("沥青混合料单轴压缩试验_")) {
                                    isAssigned = true;
                                    break;
                                }
                                // 常规匹配处理
                                if (experimentName.startsWith(assigned)) {
                                    isAssigned = true;
                                    break;
                                }
                            }
                            
                            if (!isAssigned) {
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

    /**
     * 显示Toast消息
     * @param message 要显示的消息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 保存马歇尔试验数据到后端
     * 
     * @param mixRatioId 配比ID
     * @param experiments 实验数据Map
     */
    private void saveMarshallTestData(String mixRatioId, Map<String, String> experiments) {
        // 创建请求数据
        Map<String, Object> requestData = new HashMap<>();
        
        // 使用马歇尔稳定度试验对应的任务ID
        String taskId = getTaskIdForExperimentType("马歇尔稳定度试验");
        requestData.put("taskId", taskId);
        
        // 提取马歇尔稳定度试验数据
        Float stability1 = parseFloatSafely(experiments.get("马歇尔稳定度试验_stability_1"));
        Float streamValue1 = parseFloatSafely(experiments.get("马歇尔稳定度试验_flow_1"));
        Float stability2 = parseFloatSafely(experiments.get("马歇尔稳定度试验_stability_2"));
        Float streamValue2 = parseFloatSafely(experiments.get("马歇尔稳定度试验_flow_2"));
        Float stability3 = parseFloatSafely(experiments.get("马歇尔稳定度试验_stability_3"));
        Float streamValue3 = parseFloatSafely(experiments.get("马歇尔稳定度试验_flow_3"));
        
        // 添加到请求数据
        requestData.put("stability1", stability1);
        requestData.put("streamValue1", streamValue1);
        requestData.put("stability2", stability2);
        requestData.put("streamValue2", streamValue2);
        requestData.put("stability3", stability3);
        requestData.put("streamValue3", streamValue3);
        
        // 发送请求到API
        try {
            Log.i(TAG, "正在保存配比 " + mixRatioId + " 的马歇尔试验数据，任务UUID: " + taskId);
            
            MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
            Call<ApiResponse<Map<String, Object>>> call = apiService.saveMarshallTest(requestData);
            
            Response<ApiResponse<Map<String, Object>>> response = call.execute();
            if (response.isSuccessful()) {
                Log.i(TAG, "成功保存配比 " + mixRatioId + " 的马歇尔试验数据");
            } else {
                Log.e(TAG, "保存马歇尔试验数据失败, 状态码: " + response.code());
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "错误详情: " + errorBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "保存马歇尔试验数据时出错", e);
        }
    }
    
    /**
     * 保存汉堡车辙实验数据到后端
     * 
     * @param mixRatioId 配比ID
     * @param experiments 实验数据Map
     */
    private void saveHamburgRuttingTestData(String mixRatioId, Map<String, String> experiments) {
        // 创建请求数据
        Map<String, Object> requestData = new HashMap<>();
        
        // 使用汉堡车辙实验对应的任务ID
        String taskId = getTaskIdForExperimentType("沥青混合料车辙实验（汉堡车辙）");
        requestData.put("taskId", taskId);
        
        requestData.put("mixRatioId", mixRatioId);
        
        // 提取汉堡车辙实验数据
        // 注意：这里的字段名与MixtureExperimentDataAdapter中addHamburgWheelTrackingFields方法中的字段名保持一致
        String experimentName = "沥青混合料车辙实验（汉堡车辙）";
        Float steadySlope1 = parseFloatSafely(experiments.get(experimentName + "_first_slope"));
        Float steadyCurvilinear1 = parseFloatSafely(experiments.get(experimentName + "_first_intercept"));
        Float steadySlope2 = parseFloatSafely(experiments.get(experimentName + "_second_slope"));
        Float steadyCurvilinear2 = parseFloatSafely(experiments.get(experimentName + "_second_intercept"));
        
        // 添加到请求数据
        requestData.put("steadySlope1", steadySlope1);
        requestData.put("steadyCurvilinear1", steadyCurvilinear1);
        requestData.put("steadySlope2", steadySlope2);
        requestData.put("steadyCurvilinear2", steadyCurvilinear2);
        
        // 发送请求到API
        try {
            Log.i(TAG, "正在保存配比 " + mixRatioId + " 的汉堡车辙实验数据");
            
            MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
            Call<ApiResponse<Map<String, Object>>> call = apiService.saveHamburgRuttingTest(requestData);
            
            Response<ApiResponse<Map<String, Object>>> response = call.execute();
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Log.i(TAG, "成功保存配比 " + mixRatioId + " 的汉堡车辙实验数据");
            } else {
                String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                Log.e(TAG, "保存汉堡车辙实验数据失败: " + errorMsg);
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "错误详情: " + errorBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "保存汉堡车辙实验数据时出错", e);
        }
    }
    
    /**
     * 保存沥青混合料弯曲试验数据到后端
     * 
     * @param mixRatioId 配比ID
     * @param experiments 实验数据Map
     */
    private void saveMixtureBendingTestData(String mixRatioId, Map<String, String> experiments) {
        // 创建请求数据
        Map<String, Object> requestData = new HashMap<>();
        
        // 使用沥青混合料弯曲试验对应的任务ID
        String taskId = getTaskIdForExperimentType("沥青混合料弯曲试验");
        requestData.put("taskId", taskId);
        
        requestData.put("mixRatioId", mixRatioId);
        
        // 提取公共参数
        String experimentName = "沥青混合料弯曲试验";
        Float spanLength = parseFloatSafely(experiments.get(experimentName + "_span_length"));
        requestData.put("spanLength", spanLength);
        
        // 计算试件数量
        int specimenCount = 0;
        for (String key : experiments.keySet()) {
            if (key.matches(experimentName + "_width_\\d+")) {
                specimenCount++;
            }
        }
        requestData.put("specimenCount", specimenCount);
        
        // 准备试件数据列表
        List<Map<String, Object>> specimens = new ArrayList<>();
        
        // 准备结果类型映射表，确保后端能正确保存
        String[][] resultTypeMapping = {
            {"最大拉应力", "Tensile stress", "kPa"},
            {"最大拉应变", "Tensile strain", "με"},
            {"弯曲劲度模量", "Flexural stiffness", "MPa"},
            {"相位角", "Phase Angle", "deg"},
            {"单个循环耗散能", "Dissipated energy", "J/m³"},
            {"累积耗散能", "Cumulative dissipated energy", "kJ/m³"}
        };
        
        float totalFlexuralStrength = 0f;
        float totalMaxStrain = 0f;
        float totalStiffnessModulus = 0f;
        int validSpecimenCount = 0;
        
        for (int i = 1; i <= specimenCount; i++) {
            // 获取试件参数
            Float width = parseFloatSafely(experiments.get(experimentName + "_width_" + i));
            Float height = parseFloatSafely(experiments.get(experimentName + "_height_" + i));
            Float maxLoad = parseFloatSafely(experiments.get(experimentName + "_max_load_" + i));
            Float deflection = parseFloatSafely(experiments.get(experimentName + "_deflection_" + i));
            
            // 从存储的计算结果中获取值
            String flexuralStrengthStr = experiments.get(experimentName + "_flexural_strength_" + i);
            String maxStrainStr = experiments.get(experimentName + "_max_strain_" + i);
            String stiffnessModulusStr = experiments.get(experimentName + "_stiffness_modulus_" + i);
            
            // 解析计算结果
            Float flexuralStrength = parseFloatSafely(flexuralStrengthStr);
            Float maxStrain = parseFloatSafely(maxStrainStr);
            Float stiffnessModulus = parseFloatSafely(stiffnessModulusStr);
            
            // 检查是否有有效的计算结果
            boolean hasValidResults = (flexuralStrength != null && maxStrain != null && stiffnessModulus != null);
            
            // 累加计算结果用于平均值计算
            if (hasValidResults) {
                totalFlexuralStrength += flexuralStrength;
                totalMaxStrain += maxStrain;
                totalStiffnessModulus += stiffnessModulus;
                validSpecimenCount++;
            }
            
            // 添加试件数据
            Map<String, Object> specimen = new HashMap<>();
            specimen.put("specimenNumber", i);
            specimen.put("width", width);
            specimen.put("height", height);
            specimen.put("maxLoad", maxLoad);
            specimen.put("deflection", deflection);
            specimen.put("flexuralStrength", flexuralStrength);
            specimen.put("maxStrain", maxStrain);
            specimen.put("stiffnessModulus", stiffnessModulus);
            
            specimens.add(specimen);
        }
        
        // 计算平均值
        if (validSpecimenCount > 0) {
            requestData.put("averageFlexuralStrength", totalFlexuralStrength / validSpecimenCount);
            requestData.put("averageMaxStrain", totalMaxStrain / validSpecimenCount);
            requestData.put("averageStiffnessModulus", totalStiffnessModulus / validSpecimenCount);
        }
        
        // 将试件数据添加到请求数据中
        requestData.put("specimens", specimens);
        
        // 发送请求到API
        try {
            Log.i(TAG, "正在保存配比 " + mixRatioId + " 的沥青混合料弯曲试验数据");
            
            MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
            Call<ApiResponse<Map<String, Object>>> call = apiService.saveMixtureBendingTest(requestData);
            
            Response<ApiResponse<Map<String, Object>>> response = call.execute();
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Log.i(TAG, "成功保存配比 " + mixRatioId + " 的沥青混合料弯曲试验数据");
            } else {
                String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                Log.e(TAG, "保存沥青混合料弯曲试验数据失败: " + errorMsg);
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "错误详情: " + errorBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "保存沥青混合料弯曲试验数据时出错", e);
        }
    }

    /**
     * 保存沥青混合料四点弯曲疲劳寿命试验数据到后端
     * 
     * @param mixRatioId 配比ID
     * @param experiments 实验数据Map
     */
    private void saveFourPointBendingFatigueTestData(String mixRatioId, Map<String, String> experiments) {
        // 创建请求数据
        Map<String, Object> requestData = new HashMap<>();
        
        // 使用沥青混合料四点弯曲疲劳寿命试验对应的任务ID
        String taskId = getTaskIdForExperimentType("沥青混合料四点弯曲疲劳寿命试验");
        requestData.put("taskId", taskId);
        
        requestData.put("mixRatioId", mixRatioId);
        
        // 主实验表数据
        Float temperature = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_temperature"));
        Float frequency = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_frequency"));
        Float loadingMode = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_loading_mode"));
        
        // 获取和设置数据库表所需的所有字段
        String mixRatioName = currentTask != null ? currentTask.getTaskName() : "";
        String testDate = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_testDate", "");
        String operator = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_operator", "");
        String mixTemperature = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_mixTemperature", "");
        String mixSpeed = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_mixSpeed", "");  
        String mixTime = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_mixTime", "");
        String compactionMethod = experiments.getOrDefault("沥青混合料四点弯曲疲劳寿命试验_compactionMethod", "");
        
        // 直接设置数据库字段，而不仅仅是放在testData中
        requestData.put("mixRatioName", mixRatioName);
        requestData.put("experimentName", "沥青混合料四点弯曲疲劳寿命试验");
        requestData.put("mixTemperature", mixTemperature);
        requestData.put("mixSpeed", mixSpeed);
        requestData.put("mixTime", mixTime);
        requestData.put("compactionMethod", compactionMethod);
        requestData.put("testDate", testDate);
        requestData.put("operator", operator);
        
        // 测试数据保持不变，保持向后兼容
        Map<String, Object> testData = new HashMap<>();
        testData.put("temperature", temperature);  // 映射到mix_temperature
        testData.put("frequency", frequency);      // 映射到frequency_hz
        testData.put("loadingMode", loadingMode);  // 可能需要添加到表中
        
        requestData.put("testData", testData);
        
        // 计算试件数量
        int specimenCount = 0;
        Pattern specimenPattern = Pattern.compile("沥青混合料四点弯曲疲劳寿命试验_(fatigue|dynamic|height|diameter)_(\\d+)");
        
        for (String key : experiments.keySet()) {
            Matcher matcher = specimenPattern.matcher(key);
            if (matcher.matches()) {
                specimenCount++;
            }
        }
        
        Log.d(TAG, "找到" + "沥青混合料四点弯曲疲劳寿命试验" + "试件数量: " + specimenCount);
        
        // 准备试件数据列表
        List<Map<String, Object>> specimensData = new ArrayList<>();
        
        // 准备结果类型映射表，确保后端能正确保存
        String[][] resultTypeMapping = {
            {"最大拉应力", "Tensile stress", "kPa"},
            {"最大拉应变", "Tensile strain", "με"},
            {"弯曲劲度模量", "Flexural stiffness", "MPa"},
            {"相位角", "Phase Angle", "deg"},
            {"单个循环耗散能", "Dissipated energy", "J/m³"},
            {"累积耗散能", "Cumulative dissipated energy", "kJ/m³"}
        };
        
        for (int i = 1; i <= specimenCount; i++) {
            // 获取试件参数
            Map<String, Object> specimen = new HashMap<>();
            
            // 试件ID
            specimen.put("specimenId", UUID.randomUUID().toString());
            specimen.put("specimenNumber", i);
            
            // 试件尺寸
            Float width = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_width_" + i));
            Float height = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_height_" + i));
            Float length = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_length_" + i));
            Float spanMm = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_span_" + i));
            Float strainRange = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_strain_range_" + i));
            Float frequencyHz = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_frequency_" + i));
            Float testTemperature = parseFloatSafely(experiments.get("沥青混合料四点弯曲疲劳寿命试验_temperature_" + i));
            
            specimen.put("width", width);     // 映射到width_mm
            specimen.put("height", height);   // 映射到height_mm 
            specimen.put("length", length);   // 映射到length_mm
            specimen.put("spanMm", spanMm);   // 映射到span_mm
            specimen.put("strainRange", strainRange);   // 映射到strain_range
            specimen.put("frequencyHz", frequencyHz);   // 映射到frequency_hz
            specimen.put("testTemperature", testTemperature);   // 映射到test_temperature
            
            // 最终疲劳寿命 - 直接对应fatigue_life字段
            String fatigueLifeKey = "沥青混合料四点弯曲疲劳寿命试验_result_" + i + "_fatigue_life";
            Float fatigueLife = parseFloatSafely(experiments.get(fatigueLifeKey));
            specimen.put("fatigueLife", fatigueLife);
            
            // 添加试验结果数据
            List<Map<String, Object>> resultsList = new ArrayList<>();
            
            for (int j = 1; j <= 6; j++) {  // 6个结果参数（不包括疲劳寿命）
                String initialKey = "沥青混合料四点弯曲疲劳寿命试验_result_" + i + "_initial_" + j;
                String currentKey = "沥青混合料四点弯曲疲劳寿命试验_result_" + i + "_current_" + j;
                
                Float initialValue = parseFloatSafely(experiments.get(initialKey));
                Float currentValue = parseFloatSafely(experiments.get(currentKey));
                
                Map<String, Object> result = new HashMap<>();
                result.put("resultType", j);
                result.put("resultTypeDisplayName", resultTypeMapping[j-1][0]);
                result.put("resultTypeEnglishName", resultTypeMapping[j-1][1]);
                result.put("resultTypeUnit", resultTypeMapping[j-1][2]);
                result.put("initialValue", initialValue);
                result.put("currentValue", currentValue);
                
                resultsList.add(result);
                
                Log.d(TAG, "试件" + i + "参数" + j + " 初始值: " + initialValue + ", 实时值: " + currentValue);
            }
            
            specimen.put("results", resultsList);
            specimensData.add(specimen);
            Log.d(TAG, "已添加试件" + i + "数据");
        }
        
        // 记录收集到的试件数量
        Log.d(TAG, "收集到的试件数据数量: " + specimensData.size());
        
        // 添加试件数据到请求
        requestData.put("specimens", specimensData);
        
        // 添加创建时间参数 - 使用SQL Timestamp格式
        requestData.put("created_at", new java.sql.Timestamp(System.currentTimeMillis()));
        
        // 请求API保存数据
        Log.d(TAG, "准备发送沥青混合料四点弯曲疲劳寿命试验数据到服务器");
        mixtureTaskService.saveFourPointFatigueTestData(requestData)
                .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.d(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存成功: " + response.body().getMessage());
                            showToast("沥青混合料四点弯曲疲劳寿命试验数据保存成功");
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                            Log.e(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存失败: " + errorMsg);
                            showToast("沥青混合料四点弯曲疲劳寿命试验数据保存失败: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                        Log.e(TAG, "沥青混合料四点弯曲疲劳寿命试验数据保存请求失败", t);
                        showToast("沥青混合料四点弯曲疲劳寿命试验数据保存请求失败: " + t.getMessage());
                    }
                });
    }

/**
 * 保存动态模量试验数据到后端
 * 
 * @param mixRatioId 配比ID
 * @param experiments 实验数据Map
 */
private void saveDynamicModulusTestData(String mixRatioId, Map<String, String> experiments) {
    // 创建请求数据
    Map<String, Object> requestData = new HashMap<>();
    
    // 使用动态模量试验对应的任务ID
    String taskId = getTaskIdForExperimentType("动态模量试验");
    requestData.put("taskId", taskId);
    
    requestData.put("mixRatioId", mixRatioId);
    
    // 确定试件数量和ID列表
    Set<Integer> specimenIds = new HashSet<>();
    String experimentName = "动态模量试验";
    
    // 通过分析key值提取所有试件ID
    for (String key : experiments.keySet()) {
        Log.d(TAG, "分析key: " + key);
        if (key.startsWith(experimentName + "_") && key.contains("_diameter_")) {
            Log.d(TAG, "匹配到key: " + key);
            String[] parts = key.split("_");
            //Log.d(TAG, "key的拆分结果: " + Arrays.toString(parts));
            if (parts.length >= 4) {
                try {
                    int specimenId = Integer.parseInt(parts[3]);
                    specimenIds.add(specimenId);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "解析试件ID出错: " + key, e);
                }
            }
        }
    }
    
    int specimenCount = specimenIds.size();
    
    // 如果没有试件数据，添加一个默认试件
    if (specimenIds.isEmpty()) {
        // 添加一个默认试件ID
        Integer defaultSpecimenId = 1;
        specimenIds.add(defaultSpecimenId);
        
        // 设置默认试件的直径和高度数据
        //experiments.put(experimentName + "_diameter_" + defaultSpecimenId, "100.0");  // 默认直径100mm
        //experiments.put(experimentName + "_height_" + defaultSpecimenId, "150.0");    // 默认高度150mm
        
        // 更新试件数量
        specimenCount = 1;
        
        Log.d(TAG, "添加了默认试件数据，ID: " + defaultSpecimenId);
    }
    
    requestData.put("specimenCount", specimenCount);
    // 在准备试件数据前添加日志
    Log.d(TAG, "开始处理试件数据，specimenIds: " + specimenIds);
    Log.d(TAG, "experiments map包含的键: " + experiments.keySet());
    // 准备试件数据列表
    List<Map<String, Object>> specimens = new ArrayList<>();
    
    // 处理每个试件的数据
    for (Integer specimenId : specimenIds) {
        Map<String, Object> specimen = new HashMap<>();
        specimen.put("specimenNumber", specimenId);
        
        // 获取试件基本信息
        Float diameter = parseFloatSafely(experiments.get(experimentName + "_diameter_" + specimenId));
        Float height = parseFloatSafely(experiments.get(experimentName + "_height_" + specimenId));

        specimen.put("diameter", diameter);   // 映射到 diameter
        specimen.put("height", height);       // 映射到 height

        // 记录每个试件的键
        String diameterKey = experimentName + "_diameter_" + specimenId;
        String heightKey = experimentName + "_height_" + specimenId;
        Log.d(TAG, "试件" + specimenId + "的直径键: " + diameterKey + ", 值: " + experiments.get(diameterKey));
        Log.d(TAG, "试件" + specimenId + "的高度键: " + heightKey + ", 值: " + experiments.get(heightKey));
        
        specimen.put("diameter", diameter);
        specimen.put("height", height);

        // 记录解析后的直径和高度
        Log.d(TAG, "试件" + specimenId + "解析后的直径: " + diameter + ", 高度: " + height);
        
        // 计算体积密度和空隙率 (如果有这些数据)
        Float bulkDensity = parseFloatSafely(experiments.get(experimentName + "_bulk_density_" + specimenId));
        Float airVoidContent = parseFloatSafely(experiments.get(experimentName + "_air_void_" + specimenId));
        
        if (bulkDensity != null) {
            specimen.put("bulkDensity", bulkDensity);
        }
        if (airVoidContent != null) {
            specimen.put("airVoidContent", airVoidContent);
        }
        
        specimens.add(specimen);

        // 记录已添加的试件
        Log.d(TAG, "已添加试件: " + specimen);
    }
    
    // 记录 specimens 列表
    Log.d(TAG, " specimens 列表: " + specimens);
    
    requestData.put("specimens", specimens);
    
    // 准备温度数据列表
    List<Map<String, Object>> temperatures = new ArrayList<>();
    String[] tempValues = {"-10", "4.4", "21.1", "37.8", "54"};
    
    // 添加温度数据
    int tempOrder = 1;
    for (String temp : tempValues) {
        // 检查该温度是否有数据
        boolean hasTempData = false;
        for (String key : experiments.keySet()) {
            if (key.contains("_" + temp + "_") && experiments.get(key) != null && !experiments.get(key).isEmpty()) {
                hasTempData = true;
                break;
            }
        }
        
        if (hasTempData) {
            Map<String, Object> tempData = new HashMap<>();
            tempData.put("temperature", parseFloatSafely(temp));
            tempData.put("temperatureOrder", tempOrder++);
            temperatures.add(tempData);
        }
    }
    
    requestData.put("temperatures", temperatures);
    
    // 准备测量数据列表
    List<Map<String, Object>> measurements = new ArrayList<>();
    String[] frequencies = {"25", "10", "5", "1", "0.5", "0.1"};
    String[] cycles = {"200", "200", "100", "20", "15", "15"};
    
    // 收集测量数据
    for (Integer specimenId : specimenIds) {
        for (String temp : tempValues) {
            for (int i = 0; i < frequencies.length; i++) {
                String freq = frequencies[i];
                String cycle = cycles[i];
                
                // 基础键
                String baseKey = experimentName + "_" + specimenId + "_" + temp + "_" + freq + "_";
                
                // 获取实验数据
                Float modulus = parseFloatSafely(experiments.get(baseKey + "modulus"));
                Float phase = parseFloatSafely(experiments.get(baseKey + "phase"));
                Float stress = parseFloatSafely(experiments.get(baseKey + "stress"));
                Float strain = parseFloatSafely(experiments.get(baseKey + "strain"));
                Float permStrain = parseFloatSafely(experiments.get(baseKey + "perm_strain"));
                
                // 检查是否有至少一个有效值
                if (modulus != null || phase != null || stress != null || strain != null || permStrain != null) {
                    Map<String, Object> measurement = new HashMap<>();
                    measurement.put("specimenId", specimenId);
                    measurement.put("temperature", parseFloatSafely(temp));
                    measurement.put("frequency", parseFloatSafely(freq));
                    measurement.put("cycleCount", Integer.parseInt(cycle));
                    
                    if (modulus != null) measurement.put("dynamicModulus", modulus);
                    if (phase != null) measurement.put("phaseAngle", phase);
                    if (stress != null) measurement.put("axialStress", stress);
                    if (strain != null) measurement.put("axialStrain", strain);
                    if (permStrain != null) measurement.put("permanentDeformation", permStrain);
                    
                    measurements.add(measurement);
                }
            }
        }
    }
    
    requestData.put("measurements", measurements);
    
    // 发送请求到API
    try {
        Log.i(TAG, "正在保存配比 " + mixRatioId + " 的动态模量试验数据");
        
        MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
        Call<ApiResponse<Map<String, Object>>> call = apiService.saveDynamicModulusTest(requestData);
        
        Response<ApiResponse<Map<String, Object>>> response = call.execute();
        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            Log.i(TAG, "成功保存配比 " + mixRatioId + " 的动态模量试验数据");
        } else {
            String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
            Log.e(TAG, "保存动态模量试验数据失败: " + errorMsg);
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "错误详情: " + errorBody);
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "保存动态模量试验数据时出错", e);
    }
}

/**
 * 保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据到后端
 * 
 * @param mixRatioId 配比ID
 * @param experiments 实验数据Map
 */
private void saveDirectStretchingFatigueTestData(String mixRatioId, Map<String, String> experiments) {
    // 基本信息收集
    Log.d(TAG, "开始保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据，配比ID: " + mixRatioId);
    
    // 使用沥青混合料直接拉伸循环疲劳测黏弹损伤试验对应的任务ID
    String taskId = getTaskIdForExperimentType("沥青混合料直接拉伸循环疲劳测黏弹损伤试验");
    
    // 1. 测试基本信息 - 映射到direct_stretching_fatigue_test表
    String experimentName = "沥青混合料直接拉伸循环疲劳测黏弹损伤试验";
    String testDate = experiments.getOrDefault(experimentName + "_测试日期", "");
    String operator = experiments.getOrDefault(experimentName + "_操作人员", "");
    String equipmentId = experiments.getOrDefault(experimentName + "_测试设备", "");
    String notes = experiments.getOrDefault(experimentName + "_备注", "");
    
    // 创建新的请求数据，只包含SQL表所需的字段
    Map<String, Object> dbRequestData = new HashMap<>();
    dbRequestData.put("taskId", taskId);
    dbRequestData.put("mixRatioId", mixRatioId);
    dbRequestData.put("created_at", new Timestamp(new Date().getTime()));
    
    // 重用已存在的testInfo对象，而不是重新创建
    Map<String, Object> testInfo = new HashMap<>();
    testInfo.put("experimentName", experimentName);
    testInfo.put("testDate", testDate);
    testInfo.put("operator", operator);
    testInfo.put("equipmentId", equipmentId);
    testInfo.put("notes", notes);
    dbRequestData.put("testInfo", testInfo);
    
// 2. 识别所有试件ID - 修改为从已有键名中提取
Set<String> specimenIds = new HashSet<>();
Pattern pattern = Pattern.compile("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_(fatigue|dynamic|height|diameter)_(\\d+)");

for (String key : experiments.keySet()) {
    Matcher matcher = pattern.matcher(key);
    if (matcher.find()) {
        String specimenId = matcher.group(2); // 提取匹配的第二个组，即试件编号
        if (specimenId != null && !specimenId.trim().isEmpty()) {
            specimenIds.add(specimenId);
        }
    }
}

Log.d(TAG, "识别到的试件ID: " + specimenIds);

// 3. 收集每个试件的数据
List<Map<String, Object>> specimensData = new ArrayList<>();

for (String specimenId : specimenIds) {
    Map<String, Object> specimenData = new HashMap<>();
    
    // 3.1 试件基本信息 - 使用正确的键名格式
    specimenData.put("specimenId", specimenId);
    specimenData.put("height", experiments.getOrDefault(experimentName + "_height_" + specimenId, ""));
    specimenData.put("diameter", experiments.getOrDefault(experimentName + "_diameter_" + specimenId, ""));
    
    // 3.2 动态模量数据 - 使用dynamic前缀
    Map<String, Map<String, String>> modulusData = new HashMap<>();
    
    // 初始动态模量数据
    Map<String, String> initialModulusData = new HashMap<>();
    initialModulusData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_modulus", ""));
    initialModulusData.put("cycleCount", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_cycle", ""));
    initialModulusData.put("phaseAngle", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_phase", ""));
    initialModulusData.put("forceLevel", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_stress", ""));
    initialModulusData.put("uniformStrain", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_strain", ""));
    initialModulusData.put("strainChange", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_actuator", ""));
    initialModulusData.put("temperature", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_initial_temp", ""));
    initialModulusData.put("stage", "initial");
    modulusData.put("initial", initialModulusData);
    
    // 最终动态模量数据
    Map<String, String> finalModulusData = new HashMap<>();
    finalModulusData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_modulus", ""));
    finalModulusData.put("cycleCount", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_cycle", ""));
    finalModulusData.put("phaseAngle", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_phase", ""));
    finalModulusData.put("forceLevel", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_stress", ""));
    finalModulusData.put("uniformStrain", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_strain", ""));
    finalModulusData.put("strainChange", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_actuator", ""));
    finalModulusData.put("temperature", experiments.getOrDefault(experimentName + "_dynamic_" + specimenId + "_final_temp", ""));
    finalModulusData.put("stage", "final");
    modulusData.put("final", finalModulusData);
    
    specimenData.put("modulusData", modulusData);
    
    // 3.3 疲劳数据 - 使用fatigue前缀
    Map<String, Map<String, String>> fatigueData = new HashMap<>();
    
    // 初始疲劳数据
    Map<String, String> initialFatigueData = new HashMap<>();
    initialFatigueData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_modulus", ""));
    initialFatigueData.put("cycleCount", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_cycle", ""));
    initialFatigueData.put("phaseAngle", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_phase", ""));
    initialFatigueData.put("forceLevel", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_stress", ""));
    initialFatigueData.put("uniformStrain", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_strain", ""));
    initialFatigueData.put("strainChange", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_actuator", ""));
    initialFatigueData.put("temperature", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_initial_temp", ""));
    initialFatigueData.put("stage", "initial");
    fatigueData.put("initial", initialFatigueData);
    
    // 最终疲劳数据
    Map<String, String> finalFatigueData = new HashMap<>();
    finalFatigueData.put("dynamicModulus", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_modulus", ""));
    finalFatigueData.put("cycleCount", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_cycle", ""));
    finalFatigueData.put("phaseAngle", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_phase", ""));
    finalFatigueData.put("forceLevel", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_stress", ""));
    finalFatigueData.put("uniformStrain", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_strain", ""));
    finalFatigueData.put("strainChange", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_actuator", ""));
    finalFatigueData.put("temperature", experiments.getOrDefault(experimentName + "_fatigue_" + specimenId + "_final_temp", ""));
    finalFatigueData.put("stage", "final");
    fatigueData.put("final", finalFatigueData);
    
    specimenData.put("fatigueData", fatigueData);
    
    // 添加到试件列表
    specimensData.add(specimenData);
}

// 记录收集到的试件数量
Log.d(TAG, "收集到的试件数据数量: " + specimensData.size());
    
    // 将试件数据添加到请求中的单独字段
    dbRequestData.put("specimens", specimensData);

    mixtureTaskService.saveDirectStretchingFatigueTestData(dbRequestData)
            .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存成功: " + response.body().getMessage());
                        showToast("沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存成功");
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                        Log.e(TAG, "沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存失败: " + errorMsg);
                        showToast("沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存失败: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    Log.e(TAG, "沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存请求失败", t);
                    showToast("沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存请求失败: " + t.getMessage());
                }
            });
}
    
    /**
     * 安全地将字符串解析为Float
     * 
     * @param value 要解析的字符串
     * @return 解析后的Float值，如果解析失败则返回null
     */
    private Float parseFloatSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "无法解析Float值: " + value);
            return null;
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

/**
 * 保存沥青混合料单轴压缩试验数据到后端
 * 
 * @param mixRatioId 配比ID
 * @param experiments 实验数据Map
 */
private void saveUniaxialCompressionTestData(String mixRatioId, Map<String, String> experiments) {
    Log.d(TAG, "开始保存单轴压缩试验数据，配比ID: " + mixRatioId);
    
    if (currentTask == null) {
        Log.e(TAG, "保存单轴压缩试验数据失败: 当前任务为null");
        showToast("无法保存数据：任务信息不完整");
        return;
    }
    
    try {
        String experimentName = "沥青混合料单轴压缩试验";
        String taskId = getTaskIdForExperimentType("沥青混合料单轴压缩试验");
        
        // 确定试件数量
        int specimenCount = 0;
        for (int i = 1; i <= 10; i++) { // 假设最多10个试件
            if (experiments.containsKey("沥青混合料单轴压缩试验_diameter_" + i)) {
                int id = i; // 直接使用循环变量i作为试件ID
                if (id > specimenCount) {
                    specimenCount = id;
                }
            }
        }
        
        if (specimenCount == 0) {
            Log.e(TAG, "找不到单轴压缩试验的试件数据");
            showToast("无法保存数据：未找到试件信息");
            return;
        }
        
        // 准备要发送的数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("taskId", taskId);
        requestData.put("mixRatioId", mixRatioId);
        
        // 获取测试温度
        Float testTemperature = parseFloatSafely(experiments.get(experimentName + "_temperature"));
        requestData.put("testTemperature", testTemperature);

        // 其他测试信息字段 - 尝试多种可能的键名格式
        // 尝试直接从实验名获取
        String mixRatioName = experiments.getOrDefault("沥青混合料单轴压缩试验_混凝土强度等级", "");
        // 如果为空，尝试从strength获取
        if (mixRatioName == null) mixRatioName = experiments.getOrDefault("沥青混合料单轴压缩试验_混凝土强度等级_1", "");
        // 尝试不带实验名前缀
        if (mixRatioName == null) mixRatioName = experiments.getOrDefault("混凝土强度等级", "");
        
        // 同样处理其他字段
        String compactionMethod = experiments.getOrDefault("沥青混合料单轴压缩试验_压实方法", "");
        if (compactionMethod == null) compactionMethod = experiments.getOrDefault("沥青混合料单轴压缩试验_压实方法_1", "");
        if (compactionMethod == null) compactionMethod = experiments.getOrDefault("压实方法", "");
        
        Float mixingTemperature = parseFloatSafely(experiments.get(experimentName + "_混凝土温度"));
        if (mixingTemperature == null) mixingTemperature = parseFloatSafely(experiments.get(experimentName + "_混凝土温度_1"));
        if (mixingTemperature == null) mixingTemperature = parseFloatSafely(experiments.get("混凝土温度"));
        
        Float mixingSpeed = parseFloatSafely(experiments.get(experimentName + "_搅拌速度"));
        if (mixingSpeed == null) mixingSpeed = parseFloatSafely(experiments.get(experimentName + "_搅拌速度_1"));
        if (mixingSpeed == null) mixingSpeed = parseFloatSafely(experiments.get("搅拌速度"));
        
        Float mixingTime = parseFloatSafely(experiments.get(experimentName + "_搅拌时间"));
        if (mixingTime == null) mixingTime = parseFloatSafely(experiments.get(experimentName + "_搅拌时间_1"));
        if (mixingTime == null) mixingTime = parseFloatSafely(experiments.get("搅拌时间"));
        
        String testDate = experiments.get(experimentName + "_测试日期");
        if (testDate == null) testDate = experiments.get(experimentName + "_测试日期_1");
        if (testDate == null) testDate = experiments.get("测试日期");
        
        // 平均强度可能用average_force或strength_1_avg
        Float averageForce = parseFloatSafely(experiments.get(experimentName + "_平均抗压强度"));
        if (averageForce == null) averageForce = parseFloatSafely(experiments.get(experimentName + "_平均抗压强度_1"));
        
        Log.d(TAG, "收集的额外数据: mixRatioName=" + mixRatioName + ", compactionMethod=" + compactionMethod + 
              ", mixingTemperature=" + mixingTemperature + ", mixingSpeed=" + mixingSpeed + 
              ", mixingTime=" + mixingTime + ", testDate=" + testDate + ", averageForce=" + averageForce);
        
        // 准备试件列表
        List<Map<String, Object>> specimens = new ArrayList<>();
        
        for (int i = 1; i <= specimenCount; i++) {
            Map<String, Object> specimen = new HashMap<>();
            specimen.put("specimenNumber", i);
            
            // 添加试件尺寸参数
            Float diameter = parseFloatSafely(experiments.get(experimentName + "_diameter_" + i));
            Float height = parseFloatSafely(experiments.get(experimentName + "_height_" + i));

            specimen.put("diameter", diameter);   // 映射到 diameter
            specimen.put("height", height);       // 映射到 height

            // 获取P值列表
            List<Object> pValues = new ArrayList<>();
            
            // 记录原始数据键，用于调试
            Map<String, Float> pValueKeysMap = new HashMap<>();
            
            // 更新P值键名格式和收集逻辑
            String[] possiblePrefixes = {
                experimentName + "_p" + i + "_",            // 格式1
                experimentName + "_strength_" + i + "_p",   // 格式2: 动态添加时的主要格式
                experimentName + "_strength_1_p" + i + "_", // 格式3
                experimentName + "_strength_p" + i + "_"    // 格式4
            };
            
            // 首先尝试从标准格式中收集P值
            for (String prefix : possiblePrefixes) {
                for (String key : experiments.keySet()) {
                    if (key.startsWith(prefix)) {
                        try {
                            // 提取P值ID
                            int pId;
                            if (prefix.equals(experimentName + "_strength_" + i + "_p")) {
                                // 动态添加的P值格式: "实验名_strength_试件ID_p数字"
                                pId = Integer.parseInt(key.substring(prefix.length()));
                            } else {
                                // 其他格式
                                String pIdStr = key.substring(prefix.length());
                                if (pIdStr.contains("_")) {
                                    pIdStr = pIdStr.substring(0, pIdStr.indexOf("_"));
                                }
                                pId = Integer.parseInt(pIdStr);
                            }
                            
                            Float pValue = parseFloatSafely(experiments.get(key));
                            if (pValue != null) {
                                // 确保列表大小足够
                                while (pValues.size() < pId) {
                                    pValues.add(null);
                                }
                                pValues.set(pId - 1, pValue);
                                pValueKeysMap.put(key, pValue);
                                Log.d(TAG, "找到P值: 键=" + key + ", 值=" + pValue + ", pId=" + pId);
                            }
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "解析P值ID失败，键: " + key + ", 错误: " + e.getMessage());
                        }
                    }
                }
            }
            
            // 添加调试日志
            Log.d(TAG, "标准格式收集后，试件 " + i + " 的P值数量: " + pValues.size());
            
            // 尝试找出所有可能的P值，即使格式不标准
            String[] patterns = {
                ".*_strength_" + i + "_p(\\d+).*",  // 最常见的动态添加格式
                ".*_p" + i + "_(\\d+).*",           // 备用格式1
                ".*" + experimentName + ".*_p(\\d+).*", // 备用格式2
                ".*p_value_(\\d+).*"                // 备用格式3
            };
            
            for (String key : experiments.keySet()) {
                if (pValueKeysMap.containsKey(key)) continue; // 跳过已处理的键
                
                // 尝试所有可能的模式
                for (String patternStr : patterns) {
                    try {
                        Pattern pattern = Pattern.compile(patternStr);
                        Matcher matcher = pattern.matcher(key);
                        if (matcher.find()) {
                            int pId = Integer.parseInt(matcher.group(1));
                            Float pValue = parseFloatSafely(experiments.get(key));
                            if (pValue != null) {
                                while (pValues.size() < pId) {
                                    pValues.add(null);
                                }
                                pValues.set(pId - 1, pValue);
                                pValueKeysMap.put(key, pValue);
                                Log.d(TAG, "通过正则表达式找到P值: 键=" + key + ", 值=" + pValue + ", pId=" + pId);
                            }
                            break; // 匹配成功后不再尝试其他模式
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "正则表达式处理P值出错, 键: " + key + ", 错误: " + e.getMessage());
                    }
                }
            }
            
            // 输出最终收集的P值结果
            Log.d(TAG, "最终收集的P值: " + pValues + "，共 " + pValues.size() + " 个");
            Log.d(TAG, "所有P值的原始键: " + pValueKeysMap.keySet());
            
            // 将原始P值列表添加到试件数据中，确保后端能正确处理
            specimen.put("pValues", pValues);
            
            // 新增：收集UTS028表格数据 - 使用正确的字段命名格式
            List<Map<String, Object>> utmDataList = new ArrayList<>();
            String[] pressureLevels = {"0.1P", "0.2P", "0.3P", "0.4P", "0.5P", "0.6P", "0.7P"};
            
            // 映射行索引到对应的字段名称
            String[][] fieldMapping = {
                    {"0", "max_force"},       // row0 = 最大力
                    {"1", "min_force"},       // row1 = 最小力
                    {"2", "work_ratio"},      // row2 = 应力水平 (在数据库中是work_ratio)
                    {"3", "displacement"},    // row3 = 回弹变形
                    {"4", "strain"},          // row4 = 回弹应变
                    {"5", "rebound_modulus"}, // row5 = 抗压回弹模量
                    {"6", "temperature"}      // row6 = 温度
            };
            
            // 遍历7个压力级别 (0.1P-0.7P)，对应col1-col7
            for (int colIndex = 1; colIndex <= 7; colIndex++) {
                Map<String, Object> utmItem = new HashMap<>();
                String pressureLevel = pressureLevels[colIndex-1];
                utmItem.put("pressureLevel", pressureLevel);
                
                boolean hasData = false;
                
                // 遍历7个数据行 (最大力到温度)，对应row0-row6
                for (String[] mapping : fieldMapping) {
                    String rowIndex = mapping[0];
                    String fieldName = mapping[1];
                    
                    // 构建实际的字段键名
                    String key = experimentName + "_utm_" + i + "_row" + rowIndex + "_col" + colIndex;
                    Float value = parseFloatSafely(experiments.get(key));
                    
                    if (value != null) {
                        utmItem.put(fieldName, value);
                        hasData = true;
                        Log.d(TAG, "找到UTM数据: 压力级别=" + pressureLevel + 
                              ", 字段=" + fieldName + ", 值=" + value + 
                              ", 键=" + key);
                    }
                }
                
                // 只添加有数据的压力级别
                if (hasData) {
                    utmDataList.add(utmItem);
                    Log.d(TAG, "添加压力级别 " + pressureLevel + " 的UTM数据");
                }
            }
            
            // 如果有UTM数据，将列表添加到试件
            if (!utmDataList.isEmpty()) {
                specimen.put("utmDataList", utmDataList);
                Log.d(TAG, "试件 " + i + " 添加了 " + utmDataList.size() + " 条UTM数据");
            } else {
                Log.w(TAG, "试件 " + i + " 没有找到任何UTM数据");
            }
            
            specimens.add(specimen);
        }
        
        requestData.put("specimens", specimens);
        
        // 记录要发送的数据
        Log.d(TAG, "单轴压缩试验数据准备完成: " + requestData);
        
        // 调用API保存数据
        Call<ApiResponse<Map<String, String>>> call = mixtureTaskService.saveUniaxialCompressionTestData(requestData);
        call.enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, String>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "单轴压缩试验数据保存成功");
                        showToast("单轴压缩试验数据保存成功");
                    } else {
                        Log.e(TAG, "保存单轴压缩试验数据错误: " + apiResponse.getMessage());
                        showToast("保存数据失败: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "保存单轴压缩试验数据请求失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    showToast("保存数据请求失败: 服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Log.e(TAG, "保存单轴压缩试验数据网络错误", t);
                showToast("保存数据失败: 网络连接错误");
            }
        });
    } catch (Exception e) {
        Log.e(TAG, "保存单轴压缩试验数据时出现异常", e);
        showToast("保存数据失败: " + e.getMessage());
    }
}

private void saveSplittingTestData(String mixRatioId, Map<String, String> experiments) {
    Log.d(TAG, "开始保存劈裂试验数据，配比ID: " + mixRatioId);
    
    // 创建请求数据哈希表
    Map<String, Object> requestData = new HashMap<>();
    
    // 基本测试信息
    requestData.put("taskId", getTaskIdForExperimentType("沥青混合料劈裂试验"));
    requestData.put("mixRatioId", mixRatioId);
    requestData.put("testId", UUID.randomUUID().toString());
    requestData.put("operator", currentTask.getExperimenter());
    
    // 测试基本参数
    String testTemperature = null;
    for (Map.Entry<String, String> entry : experiments.entrySet()) {
        if (entry.getKey().equals("沥青混合料劈裂试验_temperature")) {
            testTemperature = entry.getValue();
            requestData.put("testTemperature", testTemperature);
            break;
        }
    }
    
    // 添加测试设备信息
    requestData.put("testEquipment", experiments.getOrDefault("沥青混合料劈裂试验_equipment", ""));
    requestData.put("testMethod", experiments.getOrDefault("沥青混合料劈裂试验_method", ""));
    requestData.put("testStandard", experiments.getOrDefault("沥青混合料劈裂试验_standard", ""));
    
    // 查找试件数量
    int specimenCount = 1;
    for (int i = 1; i <= 10; i++) { // 假设最多10个试件
        if (experiments.containsKey("沥青混合料劈裂试验_diameter_" + i)) {
            specimenCount = Math.max(specimenCount, i);
        }
    }
    requestData.put("specimenCount", specimenCount);
    
    // 创建试件数据数组
    List<Map<String, Object>> specimens = new ArrayList<>();
    
    // 收集每个试件的数据
    for (int specimenId = 1; specimenId <= specimenCount; specimenId++) {
        Map<String, Object> specimen = new HashMap<>();
        
        // 试件ID
        specimen.put("specimenId", UUID.randomUUID().toString());
        specimen.put("specimenNumber", specimenId);
        
        // 试件尺寸
        String diameterKey = "沥青混合料劈裂试验_diameter_" + specimenId;
        String heightKey = "沥青混合料劈裂试验_height_" + specimenId;
        
        specimen.put("diameter", experiments.getOrDefault(diameterKey, ""));
        specimen.put("height", experiments.getOrDefault(heightKey, ""));
        
        // 抗拉强度数据
        String p1Key = "沥青混合料劈裂试验_strength_" + specimenId + "_p1";
        String p2Key = "沥青混合料劈裂试验_strength_" + specimenId + "_p2";
        String p3Key = "沥青混合料劈裂试验_strength_" + specimenId + "_p3";
        String pAvgKey = "沥青混合料劈裂试验_strength_" + specimenId + "_avg";
        
        specimen.put("p1Value", experiments.getOrDefault(p1Key, ""));
        specimen.put("p2Value", experiments.getOrDefault(p2Key, ""));
        specimen.put("p3Value", experiments.getOrDefault(p3Key, ""));
        specimen.put("pAverage", experiments.getOrDefault(pAvgKey, ""));
        
        // 水平应变变形数据
        String x1Key = "沥青混合料劈裂试验_deformation_" + specimenId + "_x1";
        String x2Key = "沥青混合料劈裂试验_deformation_" + specimenId + "_x2";
        String x3Key = "沥青混合料劈裂试验_deformation_" + specimenId + "_x3";
        String xAvgKey = "沥青混合料劈裂试验_deformation_" + specimenId + "_avg";
        
        specimen.put("x1Value", experiments.getOrDefault(x1Key, ""));
        specimen.put("x2Value", experiments.getOrDefault(x2Key, ""));
        specimen.put("x3Value", experiments.getOrDefault(x3Key, ""));
        specimen.put("xAverage", experiments.getOrDefault(xAvgKey, ""));
        
        // 计算结果
        String poissonRatioKey = "沥青混合料劈裂试验_poisson_ratio_" + specimenId;
        String rtKey = "沥青混合料劈裂试验_rt_" + specimenId;
        String strainKey = "沥青混合料劈裂试验_strain_" + specimenId;
        String stKey = "沥青混合料劈裂试验_st_" + specimenId;
        
        specimen.put("poissonRatio", experiments.getOrDefault(poissonRatioKey, ""));
        specimen.put("tensileStrength", experiments.getOrDefault(rtKey, ""));
        specimen.put("failureStrain", experiments.getOrDefault(strainKey, ""));
        specimen.put("stiffnessModulus", experiments.getOrDefault(stKey, ""));
        
        specimens.add(specimen);
    }
    
    // 将试件数据添加到请求中
    requestData.put("specimens", specimens);
    
    // 调用API保存数据
    try {
        Log.d(TAG, "发送劈裂试验数据到后端: " + gson.toJson(requestData));
        
        Call<ApiResponse<Map<String, String>>> call = mixtureTaskService.saveSplittingTestData(requestData);
        call.enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "劈裂试验数据保存成功");
                    runOnUiThread(() -> Toast.makeText(RecordMixtureExperimentDataActivity.this, "劈裂试验数据保存成功", Toast.LENGTH_SHORT).show());
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                    Log.e(TAG, "劈裂试验数据保存失败: " + errorMsg);
                    runOnUiThread(() -> Toast.makeText(RecordMixtureExperimentDataActivity.this, "劈裂试验数据保存失败: " + errorMsg, Toast.LENGTH_LONG).show());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Log.e(TAG, "劈裂试验数据保存请求失败", t);
                runOnUiThread(() -> Toast.makeText(RecordMixtureExperimentDataActivity.this, "网络错误，请稍后重试", Toast.LENGTH_LONG).show());
            }
        });
    } catch (Exception e) {
        Log.e(TAG, "保存劈裂试验数据异常", e);
        runOnUiThread(() -> Toast.makeText(this, "保存劈裂试验数据出错: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

    /**
     * 根据实验类型获取对应的任务ID
     * 这是一个安全的实现，它会尝试根据实验类型匹配正确的任务ID
     * 如果找不到对应的ID，会返回默认的任务ID
     * 
     * @param experimentType 实验类型
     * @return 对应的任务ID
     */
    private String getTaskIdForExperimentType(String experimentType) {
        // 如果没有提供实验类型，使用默认任务ID
        if (experimentType == null || experimentType.isEmpty() || currentTask == null) {
            Log.w(TAG, "没有提供实验类型或currentTask为空，使用默认任务ID");
            return currentTask != null ? currentTask.getTaskId() : currentTaskId;
        }
        
        // 记录调试信息
        Log.d(TAG, "获取实验类型 '" + experimentType + "' 的任务ID");
        
        try {
            // 获取实验类型对应的任务ID
            // 假设我们的系统在ExperimentAssignmentSelectionActivity中将任务ID传递了过来
            String experimentSpecificTaskId = null;
            
            // 根据实验类型匹配任务ID
            if (experimentType.contains("马歇尔稳定度试验")) {
                // 尝试从Intent中获取指定实验的任务ID
                experimentSpecificTaskId = getIntent().getStringExtra("marshallTestTaskId");
                
                // 如果Intent中没有，尝试按照命名规则构造
                if (experimentSpecificTaskId == null && currentTaskId != null) {
                    // 获取基础ID（移除可能的后缀）
                    String baseId = currentTaskId;
                    if (baseId.contains("-")) {
                        baseId = baseId.substring(0, baseId.lastIndexOf("-"));
                    }
                    experimentSpecificTaskId = baseId + "-1"; // 假设马歇尔实验任务ID以-1为后缀
                }
            } else if (experimentType.contains("汉堡车辙") || experimentType.contains("沥青混合料车辙")) {
                experimentSpecificTaskId = getIntent().getStringExtra("hamburgRuttingTestTaskId");
                
                // 如果Intent中没有，尝试按照命名规则构造
                if (experimentSpecificTaskId == null && currentTaskId != null) {
                    // 获取基础ID（移除可能的后缀）
                    String baseId = currentTaskId;
                    if (baseId.contains("-")) {
                        baseId = baseId.substring(0, baseId.lastIndexOf("-"));
                    }
                    experimentSpecificTaskId = baseId + "-1"; // 根据日志分析，它也使用-1后缀
                }
            } else if (experimentType.contains("弯曲试验")) {
                experimentSpecificTaskId = getIntent().getStringExtra("mixtureBendingTestTaskId");
                
                // 如果Intent中没有，尝试按照命名规则构造
                if (experimentSpecificTaskId == null && currentTaskId != null) {
                    // 获取基础ID（移除可能的后缀）
                    String baseId = currentTaskId;
                    if (baseId.contains("-")) {
                        baseId = baseId.substring(0, baseId.lastIndexOf("-"));
                    }
                    experimentSpecificTaskId = baseId + "-2"; // 根据日志分析，弯曲试验使用-2后缀
                }
            }
            
            // 如果成功获取到特定实验的任务ID，返回它
            if (experimentSpecificTaskId != null && !experimentSpecificTaskId.isEmpty()) {
                Log.d(TAG, "找到实验类型 '" + experimentType + "' 的特定任务ID: " + experimentSpecificTaskId);
                return experimentSpecificTaskId;
            }
        } catch (Exception e) {
            // 捕获任何异常，确保即使出错也能返回有效的任务ID
            Log.e(TAG, "获取实验类型任务ID时出错", e);
        }
        
        // 如果无法获取特定实验的任务ID，返回默认任务ID
        Log.w(TAG, "无法获取实验类型 '" + experimentType + "' 的特定任务ID，使用默认任务ID: " + currentTask.getTaskId());
        return currentTask != null ? currentTask.getTaskId() : currentTaskId;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面恢复时重新检查任务状态
        if (currentTaskId != null && !currentTaskId.isEmpty()) {
            Log.d(TAG, "onResume: 使用保存的currentTaskId检查任务状态: " + currentTaskId);
            //checkTaskStatus(currentTaskId);
            loadTaskData(currentTaskId, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTemporaryData();
    }

    /**
     * 保存临时输入的数据到SharedPreferences，在用户离开Activity时调用
     */
    private void saveTemporaryData() {
        if (adapter != null) {
            try {
                // 获取当前输入的数据
                Map<String, Map<String, String>> experimentData = adapter.getExperimentData();
                
                // 使用Gson将数据转换为JSON字符串
                String jsonData = gson.toJson(experimentData);
                
                // 使用保存的currentTaskId，而不是从Intent获取
                String taskId = currentTaskId;
                
                // 使用SharedPreferences保存数据
                SharedPreferences preferences = getSharedPreferences("mixture_experiment_temp_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                
                // 使用任务ID作为唯一键
                String dataKey = "temp_data_" + taskId;
                editor.putString(dataKey, jsonData);
                editor.apply();
                
                Log.d(TAG, "已保存临时数据: " + dataKey + ", 数据大小: " + jsonData.length());
            } catch (Exception e) {
                Log.e(TAG, "保存临时数据时出错", e);
            }
        }
    }
    
    /**
     * 从SharedPreferences恢复临时保存的数据，在Activity创建时调用
     */
    private void restoreTemporaryData() {
        try {
            // 使用保存的currentTaskId，而不是从Intent获取
            String taskId = currentTaskId;
            
            // 从SharedPreferences读取之前保存的数据
            SharedPreferences preferences = getSharedPreferences("mixture_experiment_temp_data", MODE_PRIVATE);
            
            // 使用任务ID作为唯一键
            String dataKey = "temp_data_" + taskId;
            String jsonData = preferences.getString(dataKey, null);
            
            Log.d(TAG, "尝试恢复临时数据, key=" + dataKey + ", 数据是否存在: " + (jsonData != null));
            
            if (jsonData != null && !jsonData.isEmpty() && adapter != null) {
                // 使用Gson将JSON字符串转换回数据结构
                Map<String, Map<String, String>> savedData = gson.fromJson(jsonData, 
                        new com.google.gson.reflect.TypeToken<Map<String, Map<String, String>>>(){}.getType());
                
                if (savedData != null && !savedData.isEmpty()) {
                    // 先延迟一点时间让RecyclerView完全初始化
                    new Handler().postDelayed(() -> {
                        // 遍历并逐个更新实验数据，而不是直接传递Map
                        for (Map.Entry<String, Map<String, String>> entry : savedData.entrySet()) {
                            String mixRatioId = entry.getKey();
                            Map<String, String> experiments = entry.getValue();
                            
                            for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                                String key = experimentEntry.getKey();
                                String value = experimentEntry.getValue();
                                adapter.updateExperimentValue(mixRatioId, key, value);
                                Log.d(TAG, "恢复数据项: mixRatioId=" + mixRatioId + ", key=" + key + ", value=" + value);
                            }
                        }
                        
                        // 强制UI刷新
                        adapter.notifyDataSetChanged();
                        
                        Log.d(TAG, "已恢复临时数据: " + savedData.size() + " 条记录");
                        Toast.makeText(this, "已恢复之前输入的数据", Toast.LENGTH_SHORT).show();
                    }, 300); // 延迟300毫秒，确保视图已绑定
                } else {
                    Log.d(TAG, "解析的savedData为空或无效");
                }
            } else {
                if (jsonData == null) {
                    Log.d(TAG, "没有找到临时保存的数据");
                } else if (adapter == null) {
                    Log.d(TAG, "适配器尚未初始化");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "恢复临时数据时出错", e);
        }
    }

    /**
     * 处理配置变化，如屏幕旋转
     */
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "配置已更改，但Activity未重新创建");
        // 因为在AndroidManifest.xml中添加了配置变更处理，所以无需额外操作
    }
}
