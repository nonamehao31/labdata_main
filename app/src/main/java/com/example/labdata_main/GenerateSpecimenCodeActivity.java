package com.example.labdata_main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.example.labdata_main.adapter.SpecimenMethodAdapter;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.SpecimenMethodResponse;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateSpecimenCodeActivity extends AppCompatActivity {
    private static final String TAG = "GenerateSpecimenCode";
    private static final int SPECIMEN_CODE_STEP2_REQUEST = 1002;
    
    private View step1Layout;
    private View step2Layout;
    private TextView step1Indicator;
    private TextView step2Indicator;
    private TextView step1Text;
    private TextView step2Text;
    private View step1Line;
    private RecyclerView rvMoldingMethods;
    private ExtendedFloatingActionButton btnScanDevice;
    private MaterialButton btnNext;
    private SpecimenMethodAdapter specimenMethodAdapter;
    private String taskId;
    private AppDatabase db;
    private MoldingMethod selectedMethod;
    private MixRatio selectedMixRatio;
    
    // 加载指示器相关视图
    private View loadingContainer;
    private TextView tvLoadingMessage;

    // 错误提示相关视图
    private View errorContainer;
    private TextView tvErrorMessage;
    private Button btnRetry;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置布局
        setContentView(R.layout.activity_generate_specimen_code);
        
        Log.d(TAG, "Activity created, initializing views...");

        // 获取传递过来的任务ID
        taskId = getIntent().getStringExtra("taskId");
        Log.d(TAG, "Received taskId: " + taskId);

        // 初始化数据库
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "labdata_db").build();

        // 初始化所有视图
        initializeViews();
        
        // 加载任务数据
        loadTaskData();
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views...");
        
        // 初始化步骤相关视图
        step1Layout = findViewById(R.id.step1Layout);
        step2Layout = findViewById(R.id.step2Layout);
        step1Indicator = findViewById(R.id.step1Indicator);
        step2Indicator = findViewById(R.id.step2Indicator);
        step1Text = findViewById(R.id.step1Text);
        step2Text = findViewById(R.id.step2Text);
        step1Line = findViewById(R.id.step1Line);
        
        // 初始化加载指示器
        loadingContainer = findViewById(R.id.loadingContainer);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);

        // 初始化错误提示
        errorContainer = findViewById(R.id.errorContainer);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            // 隐藏错误提示
            showError(false, null);
            // 重新加载数据
            loadTaskData();
        });

        // 初始化下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, 
                                                 android.R.color.holo_green_light,
                                                 android.R.color.holo_orange_light, 
                                                 android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "用户触发下拉刷新");
            loadTaskData();
        });

        // 初始化扫描设备码按钮
        btnScanDevice = findViewById(R.id.btnScanDevice);
        btnScanDevice.setEnabled(false);
        btnScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRCodeScan();
            }
        });

        // 初始化下一步按钮
        btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false);
        btnNext.setOnClickListener(v -> {
            // 获取当前选中的位置
            int selectedPosition = specimenMethodAdapter.getSelectedPosition();
            if (selectedPosition != RecyclerView.NO_POSITION) {
                MoldingMethod moldingMethod = specimenMethodAdapter.getMoldingMethod(selectedPosition);
                DeviceInfo mixingDevice = specimenMethodAdapter.getMixingDevice(selectedPosition);
                DeviceInfo formingDevice = specimenMethodAdapter.getFormingDevice(selectedPosition);
                MixRatio mixRatio = specimenMethodAdapter.getSelectedMixRatio();

                // 检查所有必要数据是否完整
                if (moldingMethod == null || mixRatio == null || 
                    mixingDevice == null || formingDevice == null) {
                    Toast.makeText(this, "数据不完整，请确保已选择制件方法并扫描所有设备", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 更新步骤状态为第2步
                updateStepStatus(2);

                // 创建Intent并传递数据
                Intent intent = new Intent(this, GenerateSpecimenCodeStep2Activity.class);
                Gson gson = new Gson();
                
                // 创建包含单个对象的列表
                List<MoldingMethod> moldingMethods = Collections.singletonList(moldingMethod);
                List<MixRatio> mixRatios = Collections.singletonList(mixRatio);
                List<DeviceInfo> mixingDevices = Collections.singletonList(mixingDevice);
                List<DeviceInfo> formingDevices = Collections.singletonList(formingDevice);

                // 将列表转换为JSON并传递
                intent.putExtra("moldingMethod", gson.toJson(moldingMethods));
                intent.putExtra("mixRatio", gson.toJson(mixRatios));
                intent.putExtra("mixingDevice", gson.toJson(mixingDevices));
                intent.putExtra("formingDevice", gson.toJson(formingDevices));
                
                // 传递任务ID
                intent.putExtra("taskId", taskId);

                // 启动第二步Activity
                startActivityForResult(intent, SPECIMEN_CODE_STEP2_REQUEST);
            }
        });

        // 初始化RecyclerView
        rvMoldingMethods = findViewById(R.id.rvMoldingMethods);
        Log.d(TAG, "RecyclerView found: " + (rvMoldingMethods != null));
        
        if (rvMoldingMethods != null) {
            rvMoldingMethods.setLayoutManager(new LinearLayoutManager(this));
            
            // 先用空数据初始化适配器
            specimenMethodAdapter = new SpecimenMethodAdapter("[]", new ArrayList<>());
            specimenMethodAdapter.setOnMethodSelectedListener((method, mixRatio, position) -> {
                // 保存选中的制件方法和配比
                selectedMethod = method;
                selectedMixRatio = mixRatio;
                // 启用扫描设备码按钮
                btnScanDevice.setEnabled(true);
                Log.d(TAG, String.format("Selected method at position %d: %s, mixRatio: %s",
                    position,
                    method.getCompactionMethod(),
                    mixRatio != null ? mixRatio.getName() : "null"));
            });
            rvMoldingMethods.setAdapter(specimenMethodAdapter);
            Log.d(TAG, "RecyclerView initialized with empty adapter");
        } else {
            Log.e(TAG, "Failed to find RecyclerView with ID: rvMoldingMethods");
        }

        // 设置初始状态
        updateStepStatus(1);
        Log.d(TAG, "Views initialization completed");
    }

    private void loadTaskData() {
        if (taskId != null) {
            Log.d(TAG, "Starting to load task data for taskId: " + taskId);
            
            // 隐藏错误提示
            showError(false, null);
            
            // 显示加载中状态
            showLoading(true, "正在加载制件方法数据...");
            
            // 从ID中提取前缀（例如：58c3d798-89bc-4e91-81b0-7dee2ae4fae5-0 => 58c3d798-89bc-4e91-81b0-7dee2ae4fae5）
            String taskIdPrefix = taskId;
            if (taskId.contains("-")) {
                // 找到最后一个'-'的位置
                int lastDashIndex = taskId.lastIndexOf("-");
                if (lastDashIndex > 0) {
                    taskIdPrefix = taskId.substring(0, lastDashIndex);
                }
            }
            
            Log.d(TAG, "Extracted taskIdPrefix: " + taskIdPrefix);
            
            // 调用API获取试件制作方法和配比信息
            final String finalTaskIdPrefix = taskIdPrefix;
            MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
            mixtureTaskService.getSpecimenMethodsByTaskPrefix(finalTaskIdPrefix).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call, Response<ApiResponse<List<Map<String, Object>>>> response) {
                    // 隐藏加载状态
                    showLoading(false, null);
                    
                    // 停止下拉刷新动画
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                        Log.d(TAG, "API call successful, received data: " + response.body().getData().size() + " methods");
                        
                        List<Map<String, Object>> apiData = response.body().getData();
                        
                        if (apiData.isEmpty()) {
                            Log.w(TAG, "API returned empty data list");
                            Toast.makeText(GenerateSpecimenCodeActivity.this, 
                                "未找到制件方法数据，正在尝试使用本地数据", Toast.LENGTH_SHORT).show();
                            loadTaskDataFromLocalDatabase();
                            return;
                        }
                        
                        List<MoldingMethod> methods = new ArrayList<>();
                        List<MixRatio> mixRatios = new ArrayList<>();
                        
                        // 将API返回的数据转换为MoldingMethod和MixRatio对象
                        for (Map<String, Object> item : apiData) {
                            // 创建制件方法
                            MoldingMethod method = new MoldingMethod();
                            
                            // 提取数值，处理不同类型的数据
                            if (item.containsKey("mixingTemperature")) {
                                Number temp = (Number) item.get("mixingTemperature");
                                method.setMixingTemperature(temp != null ? temp.floatValue() : 0f);
                            }
                            
                            if (item.containsKey("mixingSpeed")) {
                                Number speed = (Number) item.get("mixingSpeed");
                                method.setMixingSpeed(speed != null ? speed.floatValue() : 0f);
                            }
                            
                            if (item.containsKey("mixingTime")) {
                                Number time = (Number) item.get("mixingTime");
                                method.setMixingTime(time != null ? time.floatValue() : 0f);
                            }
                            
                            if (item.containsKey("compactionMethod")) {
                                String compactionMethod = (String) item.get("compactionMethod");
                                method.setCompactionMethod(compactionMethod);
                            }
                            
                            methods.add(method);
                            
                            // 创建配比对象
                            if (item.containsKey("mixName")) {
                                MixRatio mixRatio = new MixRatio();
                                mixRatio.setName((String) item.get("mixName"));
                                mixRatios.add(mixRatio);
                            }
                        }
                        
                        // 将方法列表转换为JSON数组
                        Gson gson = new Gson();
                        String jsonArray = gson.toJson(methods);
                        Log.d(TAG, "Converted methods to JSON: " + jsonArray);
                        
                        // 更新RecyclerView
                        if (rvMoldingMethods != null) {
                            updateRecyclerView(jsonArray, mixRatios);
                        } else {
                            Log.e(TAG, "RecyclerView is null");
                        }
                    } else {
                        Log.e(TAG, "API call failed or returned error: " + 
                            (response.body() != null ? response.body().getMessage() : "unknown error"));
                        
                        String errorMessage = response.body() != null ? 
                            response.body().getMessage() : "未能从服务器获取数据";
                        
                        // 显示错误提示
                        showError(true, errorMessage);
                        
                        // 如果API调用失败，则回退到本地数据库
                        Toast.makeText(GenerateSpecimenCodeActivity.this, 
                            "从服务器获取数据失败，正在尝试使用本地数据", Toast.LENGTH_SHORT).show();
                        loadTaskDataFromLocalDatabase();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                    // 隐藏加载状态
                    showLoading(false, null);
                    
                    // 停止下拉刷新动画
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    Log.e(TAG, "API call failed", t);
                    
                    // 显示错误提示
                    showError(true, "网络请求失败: " + t.getMessage());
                    
                    Toast.makeText(GenerateSpecimenCodeActivity.this, 
                        "从服务器获取数据失败，正在尝试使用本地数据", Toast.LENGTH_SHORT).show();
                    // 如果API调用失败，则回退到本地数据库
                    loadTaskDataFromLocalDatabase();
                }
            });
        } else {
            Log.e(TAG, "Task ID is null");
            Toast.makeText(this, "任务ID为空", Toast.LENGTH_SHORT).show();
            showLoading(false, null);
            showError(true, "任务ID为空，无法加载数据");
        }
    }
    
    /**
     * 显示或隐藏加载状态
     * @param show 是否显示
     * @param message 加载消息
     */
    private void showLoading(boolean show, String message) {
        if (loadingContainer != null) {
            if (show) {
                // 如果要显示加载状态，隐藏错误提示
                showError(false, null);
                // 设置消息
                if (message != null && tvLoadingMessage != null) {
                    tvLoadingMessage.setText(message);
                }
                // 设置为可见并添加淡入动画
                loadingContainer.setVisibility(View.VISIBLE);
                loadingContainer.setAlpha(0f);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(loadingContainer, "alpha", 0f, 1f);
                fadeIn.setDuration(300);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                fadeIn.start();
            } else {
                // 如果加载状态当前可见，添加淡出动画
                if (loadingContainer.getVisibility() == View.VISIBLE) {
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(loadingContainer, "alpha", 1f, 0f);
                    fadeOut.setDuration(300);
                    fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
                    fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            loadingContainer.setVisibility(View.GONE);
                        }
                    });
                    fadeOut.start();
                } else {
                    // 如果已经不可见，则不需要动画
                    loadingContainer.setVisibility(View.GONE);
                }
            }
        }
    }
    
    /**
     * 显示或隐藏错误提示
     * @param show 是否显示
     * @param message 错误消息
     */
    private void showError(boolean show, String message) {
        if (errorContainer != null) {
            if (show) {
                // 如果要显示错误提示，隐藏加载状态
                if (loadingContainer != null) {
                    loadingContainer.setVisibility(View.GONE);
                }
                // 设置错误消息
                if (message != null && tvErrorMessage != null) {
                    tvErrorMessage.setText(message);
                }
                // 设置为可见并添加淡入动画
                errorContainer.setVisibility(View.VISIBLE);
                errorContainer.setAlpha(0f);
                
                // 创建淡入和向上移动动画
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(errorContainer, "alpha", 0f, 1f);
                ObjectAnimator moveUp = ObjectAnimator.ofFloat(errorContainer, "translationY", 50f, 0f);
                
                // 组合动画
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(fadeIn, moveUp);
                animSet.setDuration(400);
                animSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animSet.start();
            } else {
                // 如果错误提示当前可见，添加淡出动画
                if (errorContainer.getVisibility() == View.VISIBLE) {
                    // 创建淡出和向下移动动画
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(errorContainer, "alpha", 1f, 0f);
                    ObjectAnimator moveDown = ObjectAnimator.ofFloat(errorContainer, "translationY", 0f, 50f);
                    
                    // 组合动画
                    AnimatorSet animSet = new AnimatorSet();
                    animSet.playTogether(fadeOut, moveDown);
                    animSet.setDuration(300);
                    animSet.setInterpolator(new AccelerateDecelerateInterpolator());
                    animSet.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            errorContainer.setVisibility(View.GONE);
                            // 重置位置
                            errorContainer.setTranslationY(0f);
                        }
                    });
                    animSet.start();
                } else {
                    // 如果已经不可见，则不需要动画
                    errorContainer.setVisibility(View.GONE);
                    // 确保重置位置
                    errorContainer.setTranslationY(0f);
                }
            }
        }
    }
    
    /**
     * 更新RecyclerView的适配器
     * @param jsonArray JSON格式的制件方法数组
     * @param mixRatios 配比列表
     */
    private void updateRecyclerView(String jsonArray, List<MixRatio> mixRatios) {
        specimenMethodAdapter = new SpecimenMethodAdapter(jsonArray, mixRatios);
        specimenMethodAdapter.setOnMethodSelectedListener((method, mixRatio, position) -> {
            // 保存选中的制件方法和配比
            selectedMethod = method;
            selectedMixRatio = mixRatio;
            // 启用扫描设备码按钮
            btnScanDevice.setEnabled(true);
            Log.d(TAG, String.format("Selected method at position %d: %s, mixRatio: %s",
                position,
                method.getCompactionMethod(),
                mixRatio != null ? mixRatio.getName() : "null"));
        });
        rvMoldingMethods.setAdapter(specimenMethodAdapter);
        Log.d(TAG, "Updated RecyclerView with new adapter");
    }
    
    /**
     * 从本地数据库加载任务数据（作为后备方案）
     */
    private void loadTaskDataFromLocalDatabase() {
        if (taskId != null) {
            Log.d(TAG, "Loading task data from local database for taskId: " + taskId);
            
            // 显示加载中状态
            showLoading(true, "正在从本地数据库加载数据...");
            
            // 在后台线程中访问数据库
            new Thread(() -> {
                try {
                    // 从数据库获取任务信息
                    ExperimentTask task = db.experimentTaskDao().getFullTaskById(Long.parseLong(taskId));
                    if (task != null) {
                        Log.d(TAG, "Task found in database");
                        
                        // 获取混合比例
                        List<MixRatio> mixRatios = task.getSelectedMixRatios();
                        Log.d(TAG, "Mix ratios size: " + (mixRatios != null ? mixRatios.size() : 0));
                        
                        // 获取制件方法
                        String moldingMethodStr = task.getMoldingMethod();
                        Log.d(TAG, "Molding method string: " + moldingMethodStr);
                        
                        // 在主线程更新UI
                        runOnUiThread(() -> {
                            // 隐藏加载状态
                            showLoading(false, null);
                            
                            // 停止下拉刷新动画
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            
                            if (moldingMethodStr != null && !moldingMethodStr.isEmpty()) {
                                try {
                                    Gson gson = new Gson();
                                    List<MoldingMethod> methods = new ArrayList<>();

                                    // 尝试解析为JSON数组
                                    if (moldingMethodStr.trim().startsWith("[")) {
                                        // 如果是JSON数组格式
                                        TypeToken<List<MoldingMethod>> token = new TypeToken<List<MoldingMethod>>() {};
                                        methods = gson.fromJson(moldingMethodStr, token.getType());
                                        Log.d(TAG, "Parsed JSON array format, size: " + methods.size());
                                    } else if (moldingMethodStr.trim().startsWith("{")) {
                                        // 如果是单个JSON对象，转换为数组
                                        MoldingMethod singleMethod = gson.fromJson(moldingMethodStr, MoldingMethod.class);
                                        if (singleMethod != null) {
                                            methods.add(singleMethod);
                                            Log.d(TAG, "Added single JSON object to methods");
                                        }
                                    } else {
                                        // 如果是老格式（temp=160|speed=60|time=90|method=振动压实）
                                        MoldingMethod method = new MoldingMethod();
                                        
                                        // 提取数值，处理不同类型的数据
                                        String[] parts = moldingMethodStr.split("\\|");
                                        for (String part : parts) {
                                            String[] keyValue = part.split("=");
                                            if (keyValue.length == 2) {
                                                String key = keyValue[0].trim();
                                                String value = keyValue[1].trim();
                                                try {
                                                    switch (key) {
                                                        case "temp":
                                                            method.setMixingTemperature(Float.parseFloat(value));
                                                            break;
                                                        case "speed":
                                                            method.setMixingSpeed(Float.parseFloat(value));
                                                            break;
                                                        case "time":
                                                            method.setMixingTime(Float.parseFloat(value));
                                                            break;
                                                        case "method":
                                                            method.setCompactionMethod(value);
                                                            break;
                                                    }
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "Error parsing number: " + value, e);
                                                }
                                            }
                                        }
                                        methods.add(method);
                                        Log.d(TAG, "Parsed old format string and added to methods");
                                    }

                                    // 将解析后的方法列表转换为JSON数组字符串
                                    String jsonArray = gson.toJson(methods);
                                    Log.d(TAG, "Converted methods to JSON: " + jsonArray);
                                    Log.d(TAG, "Methods size: " + methods.size());

                                    if (rvMoldingMethods != null) {
                                        // 创建新的适配器并设置给RecyclerView
                                        List<MixRatio> finalMixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
                                        updateRecyclerView(jsonArray, finalMixRatios);
                                    } else {
                                        Log.e(TAG, "RecyclerView is null");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing molding method", e);
                                    Toast.makeText(GenerateSpecimenCodeActivity.this, 
                                        "解析制件方法时出错", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w(TAG, "No molding method string found");
                                showError(true, "未找到制件方法信息");
                                Toast.makeText(GenerateSpecimenCodeActivity.this, 
                                    "未找到制件方法信息", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "Task not found in database");
                        runOnUiThread(() -> {
                            // 隐藏加载状态
                            showLoading(false, null);
                            // 停止下拉刷新动画
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            showError(true, "本地数据库中未找到任务信息");
                            Toast.makeText(GenerateSpecimenCodeActivity.this, 
                                "未找到任务信息", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading task data from database", e);
                    runOnUiThread(() -> {
                        // 隐藏加载状态
                        showLoading(false, null);
                        // 停止下拉刷新动画
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        showError(true, "加载本地数据时出错: " + e.getMessage());
                        Toast.makeText(GenerateSpecimenCodeActivity.this, 
                            "加载任务数据时出错", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
    }

    private void updateStepStatus(int currentStep) {
        // 更新步骤1的状态
        step1Indicator.setBackgroundResource(currentStep >= 1 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step1Text.setTextColor(getResources().getColor(currentStep >= 1 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));

        // 更新步骤2的状态
        step2Indicator.setBackgroundResource(currentStep >= 2 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step2Text.setTextColor(getResources().getColor(currentStep >= 2 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));

        // 更新连接线的状态
        step1Line.setBackgroundColor(getResources().getColor(currentStep >= 2 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));
        
        Log.d(TAG, "Updated step status to: " + currentStep);
    }

    private void startQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请将二维码对准扫描框");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPECIMEN_CODE_STEP2_REQUEST && resultCode == RESULT_OK && data != null) {
            // 将结果传递回 MainActivity
            setResult(RESULT_OK, data);
            finish();
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                handleScanResult(result.getContents());
            }
        }
    }

    private void handleScanResult(String deviceCode) {
        try {
            Gson gson = new Gson();
            DeviceInfo deviceInfo = gson.fromJson(deviceCode, DeviceInfo.class);
            if (deviceInfo == null) {
                Toast.makeText(this, "无效的设备信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 如果是测试设备，显示提示信息
            if (DeviceInfo.TYPE_TESTING.equals(deviceInfo.getType())) {
                // 显示dialog，通知用户无法扫描测试设备
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("不支持的设备类型")
                    .setMessage("此阶段不支持扫描测试设备，请在实验数据记录阶段扫描测试设备。")
                    .setPositiveButton("确定", null)
                    .show();
                return;
            }

            // 提交设备信息到后端
            submitDeviceInfo(deviceInfo);

            // 更新适配器中的设备信息
            specimenMethodAdapter.updateDeviceInfo(deviceInfo);

            // 根据设备类型显示不同的提示信息
            String deviceType = deviceInfo.getType().equals(DeviceInfo.TYPE_MIXING) ? "拌合" : "压实";
            Toast.makeText(this, String.format("已扫描%s设备：%s %s", 
                deviceType, deviceInfo.getManufacturer(), deviceInfo.getModel()), 
                Toast.LENGTH_SHORT).show();

            // 检查是否所有设备都已扫描
            checkAllDevicesScanned();

        } catch (Exception e) {
            Toast.makeText(this, "二维码格式错误，请重试", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing QR code", e);
        }
    }

    private void submitDeviceInfo(DeviceInfo deviceInfo) {
        // 确保当前有选中的任务
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "无法提交设备信息：任务ID为空");
            return;
        }

        // 显示加载中状态
        showLoading(true, "正在提交设备信息...");

        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        mixtureTaskService.saveDeviceInfo(
            taskId, 
            deviceInfo.getType(), 
            deviceInfo.getModel(),
            deviceInfo.getManufacturer()  // 添加设备厂家参数
        ).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, 
                                 Response<ApiResponse<Map<String, String>>> response) {
                // 隐藏加载状态
                showLoading(false, null);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "设备信息提交成功: " + response.body().getData());
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "服务器响应错误";
                    Log.e(TAG, "设备信息提交失败: " + errorMsg);
                    Toast.makeText(GenerateSpecimenCodeActivity.this, 
                        "设备信息提交失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                // 隐藏加载状态
                showLoading(false, null);
                
                Log.e(TAG, "设备信息提交失败", t);
                Toast.makeText(GenerateSpecimenCodeActivity.this, 
                    "设备信息提交失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAllDevicesScanned() {
        // 获取当前选中的位置
        int selectedPosition = specimenMethodAdapter.getSelectedPosition();
        if (selectedPosition == RecyclerView.NO_POSITION) {
            return;
        }

        // 检查是否已扫描了拌合设备和压实设备
        DeviceInfo mixingDevice = specimenMethodAdapter.getMixingDevice(selectedPosition);
        DeviceInfo formingDevice = specimenMethodAdapter.getFormingDevice(selectedPosition);

        // 只有当两种设备都扫描了，才启用下一步按钮
        btnNext.setEnabled(mixingDevice != null && formingDevice != null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        if (db != null) {
            db.close();
            Log.d(TAG, "Database connection closed");
        }
    }
}
