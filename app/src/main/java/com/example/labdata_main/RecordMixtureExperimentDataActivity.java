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

import androidx.annotation.NonNull;
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
import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        loadTaskData(taskId, true);
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
                            return;
                        }
                        
                        adapter = new MixtureExperimentDataAdapter(convertMixRatiosToMaps(mixRatios), convertToStringKeyMap(experimentAssignments));
                        adapter.setOnDeviceScanRequestListener(RecordMixtureExperimentDataActivity.this);
                        rvExperiments.setAdapter(adapter);
                        rvExperiments.setVisibility(View.VISIBLE);
                        btnSave.setEnabled(true);
                        
                        // 适配器准备好后，加载已保存的实验数据
                        for (MixRatio mixRatio : mixRatios) {
                            String mixRatioId = String.valueOf(mixRatio.getId());
                            List<String> experiments = experimentAssignments.get(mixRatio.getId());
                            
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

                        // 检查是否有动态模量试验数据
                        boolean hasDynamicModulusData = false;
                        for (String key : experiments.keySet()) {
                            if (key.startsWith("动态模量试验_")) {
                                hasDynamicModulusData = true;
                                break;
                            }
                        }

                        // 在检查前，记录所有的实验数据键
                        Log.d(TAG, "检查直接拉伸循环疲劳测黏弹损伤试验数据前，实验数据键: " + experiments.keySet());
                        
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
                            
                            // 检查实验是否被指派给该配比
                            boolean isAssigned = false;
                            for (String assigned : assignedExperiments) {
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
        requestData.put("taskId", currentTask.getTaskId());
        
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
            Log.i(TAG, "正在保存配比 " + mixRatioId + " 的马歇尔试验数据，任务UUID: " + currentTask.getTaskId());
            
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
        requestData.put("taskId", currentTask.getTaskId());
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
        requestData.put("taskId", currentTask.getTaskId());
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
        
        // 计算总和用于计算平均值
        float totalFlexuralStrength = 0;
        float totalMaxStrain = 0;
        float totalStiffnessModulus = 0;
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
            specimen.put("specimenIndex", i);
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
        requestData.put("taskId", currentTask.getTaskId());
        requestData.put("mixRatioId", mixRatioId);
        
        // 实验名称和日志记录
        String experimentName = "沥青混合料四点弯曲疲劳寿命试验";
        Log.d(TAG, "开始保存" + experimentName + "数据，配比ID: " + mixRatioId);
        Log.d(TAG, "实验数据键集合: " + experiments.keySet());
        
        // 主实验表数据
        Float temperature = parseFloatSafely(experiments.get(experimentName + "_temperature"));
        Float frequency = parseFloatSafely(experiments.get(experimentName + "_frequency"));
        Float loadingMode = parseFloatSafely(experiments.get(experimentName + "_loading_mode"));
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("experimentName", experimentName);
        testData.put("temperature", temperature);  // 映射到mix_temperature
        testData.put("frequency", frequency);      // 映射到frequency_hz
        testData.put("loadingMode", loadingMode);  // 可能需要添加到表中
        
        requestData.put("testData", testData);
        
        // 计算试件数量
        int specimenCount = 0;
        Pattern specimenPattern = Pattern.compile(experimentName + "_width_(\\d+)");
        
        for (String key : experiments.keySet()) {
            Matcher matcher = specimenPattern.matcher(key);
            if (matcher.matches()) {
                specimenCount++;
            }
        }
        
        Log.d(TAG, "找到" + experimentName + "试件数量: " + specimenCount);
        
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
            specimen.put("specimenNumber", i);
            
            // 添加试件几何尺寸参数
            Float width = parseFloatSafely(experiments.get(experimentName + "_width_" + i));
            Float height = parseFloatSafely(experiments.get(experimentName + "_height_" + i));
            Float length = parseFloatSafely(experiments.get(experimentName + "_length_" + i));
            
            specimen.put("width", width);     // 映射到width_mm
            specimen.put("height", height);   // 映射到height_mm 
            specimen.put("length", length);   // 映射到length_mm
            
            // 最终疲劳寿命 - 直接对应fatigue_life字段
            String fatigueLifeKey = experimentName + "_result_" + i + "_fatigue_life";
            Float fatigueLife = parseFloatSafely(experiments.get(fatigueLifeKey));
            specimen.put("fatigueLife", fatigueLife);
            
            // 添加试验结果数据
            List<Map<String, Object>> resultsList = new ArrayList<>();
            
            for (int j = 1; j <= 6; j++) {  // 6个结果参数（不包括疲劳寿命）
                String initialKey = experimentName + "_result_" + i + "_initial_" + j;
                String currentKey = experimentName + "_result_" + i + "_current_" + j;
                
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
            Log.d(TAG, "已添加试件" + i + "数据: " + specimen.toString());
        }
        
        // 将试件数据添加到请求数据中
        requestData.put("specimens", specimensData);
        
        // 记录完整的请求数据，用于调试
        Log.d(TAG, "完整的四点弯曲疲劳寿命试验请求数据: " + new Gson().toJson(requestData));
        
        // 发送请求到API
        MixtureTaskService apiService = ServiceCreator.createMixtureTaskService();
        Call<ApiResponse<Map<String, String>>> call = apiService.saveFourPointFatigueTestData(requestData);
        
        try {
            Response<ApiResponse<Map<String, String>>> response = call.execute();
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Log.i(TAG, "成功保存配比 " + mixRatioId + " 的" + experimentName + "数据");
            } else {
                String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                Log.e(TAG, "保存" + experimentName + "数据失败: " + errorMsg);
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    Log.e(TAG, "错误详情: " + errorBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "保存" + experimentName + "数据时出错", e);
        }
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
    requestData.put("taskId", currentTask.getTaskId());
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
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("taskId", currentTask.getTaskId());
    requestData.put("mixRatioId", mixRatioId);
    
    // 1. 测试基本信息 - 映射到direct_stretching_fatigue_test表
    String testDate = experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_测试日期", "");
    String operator = experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_操作人员", "");
    String equipmentId = experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_测试设备", "");
    String notes = experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_备注", "");
    
    Map<String, Object> testInfo = new HashMap<>();
    testInfo.put("testDate", testDate);
    testInfo.put("operator", operator);
    testInfo.put("equipmentId", equipmentId);
    testInfo.put("notes", notes);
    requestData.put("testInfo", testInfo);
    
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
    specimenData.put("height", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_height_" + specimenId, ""));
    specimenData.put("diameter", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_diameter_" + specimenId, ""));
    
    // 3.2 动态模量数据 - 使用dynamic前缀
    Map<String, Map<String, String>> modulusData = new HashMap<>();
    
    // 初始动态模量数据
    Map<String, String> initialModulusData = new HashMap<>();
    initialModulusData.put("dynamicModulus", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_modulus", ""));
    initialModulusData.put("cycleCount", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_cycle", ""));
    initialModulusData.put("phaseAngle", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_phase", ""));
    initialModulusData.put("forceLevel", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_stress", ""));
    initialModulusData.put("uniformStrain", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_strain", ""));
    initialModulusData.put("strainChange", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_actuator", ""));
    initialModulusData.put("temperature", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_initial_temp", ""));
    initialModulusData.put("stage", "initial");
    modulusData.put("initial", initialModulusData);
    
    // 最终动态模量数据
    Map<String, String> finalModulusData = new HashMap<>();
    finalModulusData.put("dynamicModulus", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_modulus", ""));
    finalModulusData.put("cycleCount", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_cycle", ""));
    finalModulusData.put("phaseAngle", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_phase", ""));
    finalModulusData.put("forceLevel", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_stress", ""));
    finalModulusData.put("uniformStrain", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_strain", ""));
    finalModulusData.put("strainChange", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_actuator", ""));
    finalModulusData.put("temperature", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_dynamic_" + specimenId + "_final_temp", ""));
    finalModulusData.put("stage", "final");
    modulusData.put("final", finalModulusData);
    
    specimenData.put("modulusData", modulusData);
    
    // 3.3 疲劳数据 - 使用fatigue前缀
    Map<String, Map<String, String>> fatigueData = new HashMap<>();
    
    // 初始疲劳数据
    Map<String, String> initialFatigueData = new HashMap<>();
    initialFatigueData.put("dynamicModulus", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_modulus", ""));
    initialFatigueData.put("cycleCount", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_cycle", ""));
    initialFatigueData.put("phaseAngle", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_phase", ""));
    initialFatigueData.put("forceLevel", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_stress", ""));
    initialFatigueData.put("uniformStrain", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_strain", ""));
    initialFatigueData.put("strainChange", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_actuator", ""));
    initialFatigueData.put("temperature", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_initial_temp", ""));
    initialFatigueData.put("stage", "initial");
    fatigueData.put("initial", initialFatigueData);
    
    // 最终疲劳数据
    Map<String, String> finalFatigueData = new HashMap<>();
    finalFatigueData.put("dynamicModulus", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_modulus", ""));
    finalFatigueData.put("cycleCount", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_cycle", ""));
    finalFatigueData.put("phaseAngle", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_phase", ""));
    finalFatigueData.put("forceLevel", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_stress", ""));
    finalFatigueData.put("uniformStrain", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_strain", ""));
    finalFatigueData.put("strainChange", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_actuator", ""));
    finalFatigueData.put("temperature", experiments.getOrDefault("沥青混合料直接拉伸循环疲劳测黏弹损伤试验_fatigue_" + specimenId + "_final_temp", ""));
    finalFatigueData.put("stage", "final");
    fatigueData.put("final", finalFatigueData);
    
    specimenData.put("fatigueData", fatigueData);
    
    // 添加到试件列表
    specimensData.add(specimenData);
}

// 记录收集到的试件数量
Log.d(TAG, "收集到的试件数据数量: " + specimensData.size());
    
    // 请求API保存数据
    Log.d(TAG, "准备发送沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据到服务器");
    mixtureTaskService.saveDirectStretchingFatigueTestData(requestData)
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
     * 保存应用状态，在屏幕旋转或其他配置变更时调用
     * 
     * @param outState 用于保存状态的Bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: 保存当前实验数据状态");
        
        // 保存当前已输入的实验数据
        if (adapter != null) {
            try {
                // 获取并保存所有实验数据
                Map<String, Map<String, String>> experimentData = adapter.getExperimentData();
                outState.putSerializable("experimentData", (Serializable) experimentData);
                Log.d(TAG, "已保存实验数据: " + experimentData.size() + " 条记录");
                
                // 保存马歇尔稳定度试验数据（特殊处理）
                for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                    String mixRatioId = entry.getKey();
                    Map<String, String> experiments = entry.getValue();
                    
                    // 找出马歇尔稳定度试验相关的数据
                    for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                        String key = experimentEntry.getKey();
                        if (key.startsWith("马歇尔稳定度试验_")) {
                            String value = experimentEntry.getValue();
                            outState.putString("marshall_" + mixRatioId + "_" + key, value);
                        }
                    }
                    
                    // 找出汉堡车辙实验相关的数据
                    for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                        String key = experimentEntry.getKey();
                        if (key.startsWith("沥青混合料车辙实验（汉堡车辙）_")) {
                            String value = experimentEntry.getValue();
                            outState.putString("hamburg_" + mixRatioId + "_" + key, value);
                        }
                    }
                    
                    // 找出沥青混合料弯曲试验相关的数据
                    for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                        String key = experimentEntry.getKey();
                        if (key.startsWith("沥青混合料弯曲试验_")) {
                            String value = experimentEntry.getValue();
                            outState.putString("bending_" + mixRatioId + "_" + key, value);
                        }
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "保存实验数据时出错", e);
            }
        }
        
        // 保存其他状态...
    }
    
    /**
     * 恢复应用状态，在屏幕旋转或其他配置变更后重新创建Activity时调用
     * 
     * @param savedInstanceState 包含已保存状态的Bundle
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: 恢复实验数据状态");
        
        try {
            // 恢复实验数据
            if (savedInstanceState.containsKey("experimentData") && adapter != null) {
                Map<String, Map<String, String>> experimentData = (Map<String, Map<String, String>>) savedInstanceState.getSerializable("experimentData");
                if (experimentData != null) {
                    // 遍历并逐个更新实验数据，而不是直接传递Map
                    for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                        String mixRatioId = entry.getKey();
                        Map<String, String> experiments = entry.getValue();
                        
                        for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                            String key = experimentEntry.getKey();
                            String value = experimentEntry.getValue();
                            adapter.updateExperimentValue(mixRatioId, key, value);
                        }
                    }
                    Log.d(TAG, "已恢复实验数据: " + experimentData.size() + " 条记录");
                }
                
                // 恢复特殊处理的马歇尔稳定度试验数据
                for (String key : savedInstanceState.keySet()) {
                    if (key.startsWith("marshall_")) {
                        String value = savedInstanceState.getString(key);
                        String[] parts = key.split("_", 3);
                        if (parts.length >= 3) {
                            String mixRatioId = parts[1];
                            String experimentKey = parts[2];
                            for (int i = 3; i < parts.length; i++) {
                                experimentKey += "_" + parts[i];
                            }
                            adapter.updateExperimentValue(mixRatioId, experimentKey, value);
                            Log.d(TAG, "恢复马歇尔试验数据: " + mixRatioId + ", " + experimentKey + " = " + value);
                        }
                    }
                }
                
                // 恢复特殊处理的汉堡车辙实验数据
                for (String key : savedInstanceState.keySet()) {
                    if (key.startsWith("hamburg_")) {
                        String value = savedInstanceState.getString(key);
                        String[] parts = key.split("_", 3);
                        if (parts.length >= 3) {
                            String mixRatioId = parts[1];
                            String experimentKey = parts[2];
                            for (int i = 3; i < parts.length; i++) {
                                experimentKey += "_" + parts[i];
                            }
                            adapter.updateExperimentValue(mixRatioId, experimentKey, value);
                            Log.d(TAG, "恢复汉堡车辙实验数据: " + mixRatioId + ", " + experimentKey + " = " + value);
                        }
                    }
                }
                
                // 恢复特殊处理的沥青混合料弯曲试验数据
                for (String key : savedInstanceState.keySet()) {
                    if (key.startsWith("bending_")) {
                        String value = savedInstanceState.getString(key);
                        String[] parts = key.split("_", 3);
                        if (parts.length >= 3) {
                            String mixRatioId = parts[1];
                            String experimentKey = parts[2];
                            for (int i = 3; i < parts.length; i++) {
                                experimentKey += "_" + parts[i];
                            }
                            adapter.updateExperimentValue(mixRatioId, experimentKey, value);
                            Log.d(TAG, "恢复沥青混合料弯曲试验数据: " + mixRatioId + ", " + experimentKey + " = " + value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "恢复实验数据状态时出错", e);
        }
        
        // 恢复其他状态...
    }
}
