package com.example.labdata_main;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        // 设置RecyclerView
        rvExperiments.setLayoutManager(new LinearLayoutManager(this));

        // 设置返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 设置保存按钮
        btnSave.setOnClickListener(v -> saveExperimentData());
    }

    private void loadTaskData(String taskId) {
        // 显示加载指示器
        showLoading(true);
        
        // 首先尝试从API获取最新的任务数据
        MixtureTaskService taskService = ServiceCreator.create(MixtureTaskService.class);
        
        taskService.getMixtureTaskByTaskId(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                // 隐藏加载指示器
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 从API成功获取数据后处理
                    processApiTaskData(response.body().getData(), taskId);
                    
                    // 同时获取试件制备数据
                    fetchSpecimenData(taskId);
                } else {
                    // API调用失败
                    String errorMsg = response.body() != null ? response.body().getMessage() : "API请求失败";
                    Log.e(TAG, "API Error: " + errorMsg);
                    String statusCode = response.code() + " " + response.message();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                            "无法从服务器获取任务数据: " + statusCode, Toast.LENGTH_LONG).show();
                        showErrorState("API响应错误: " + statusCode);
                    });
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // 隐藏加载指示器
                showLoading(false);
                
                Log.e(TAG, "Network error", t);
                runOnUiThread(() -> {
                    Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                        "网络请求失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    showErrorState("网络连接失败");
                });
            }
        });
    }
    
    /**
     * 处理从API获取的任务数据
     */
    private void processApiTaskData(Map<String, Object> data, String taskId) {
        if (data == null) {
            showErrorState("API返回数据为空");
            return;
        }
        
        executor.execute(() -> {
            try {
                // 将API数据同步到本地数据库
                // 然后从数据库获取完整的任务信息
                currentTask = database.experimentTaskDao().getFullTaskByTaskId(taskId);
                
                if (currentTask != null) {
                    // 在主线程更新UI
                    runOnUiThread(() -> {
                        // 获取配比信息和实验分配
                        List<MixRatio> mixRatios = currentTask.getSelectedMixRatios();
                        Map<Long, List<String>> experimentAssignments = currentTask.getExperimentAssignments();
                        
                        if (mixRatios != null && experimentAssignments != null) {
                            // 创建并设置适配器
                            adapter = new MixtureExperimentDataAdapter(mixRatios, experimentAssignments);
                            adapter.setOnDeviceScanRequestListener(this);
                            rvExperiments.setAdapter(adapter);
                        } else {
                            showErrorState("未找到配比或实验信息");
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        showErrorState("未找到任务信息");
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing task data", e);
                runOnUiThread(() -> {
                    showErrorState("处理任务数据时出错: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 获取试件制备数据
     */
    private void fetchSpecimenData(String taskId) {
        // 显示加载指示器
        showLoading(true, "获取试件制备数据...");
        
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
                    
                    // 打印methodsAndRatios的详细内容
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
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析方法数据时出错", e);
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
                        // 更新适配器中的试件制备数据
                        if (adapter != null) {
                            runOnUiThread(() -> {
                                adapter.updateSpecimenData(specimenData);
                                Toast.makeText(RecordMixtureExperimentDataActivity.this, 
                                    "试件制备数据已更新", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Log.w(TAG, "试件数据为空");
                        Toast.makeText(RecordMixtureExperimentDataActivity.this,
                            "试件数据为空，请检查任务配置", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "获取试件数据失败";
                    Log.e(TAG, "获取试件数据失败: " + errorMsg);
                    Toast.makeText(RecordMixtureExperimentDataActivity.this,
                        "获取试件数据失败: " + errorMsg, Toast.LENGTH_LONG).show();
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
            }
        });
    }
    
    /**
     * 显示加载指示器
     */
    private void showLoading(boolean show) {
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
    }
    
    /**
     * 显示加载指示器
     */
    private void showLoading(boolean show, String message) {
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
    }
    
    /**
     * 显示错误状态
     */
    private void showErrorState(String errorMessage) {
        // 显示错误UI
        if (rvExperiments != null) {
            rvExperiments.setVisibility(View.GONE);
        }
        
        // 显示错误信息Toast
        Toast.makeText(this, "错误: " + errorMessage, Toast.LENGTH_LONG).show();
        
        // 禁用保存按钮
        if (btnSave != null) {
            btnSave.setEnabled(false);
        }
        
        // 返回按钮仍然可用
        if (btnBack != null) {
            btnBack.setClickable(true);
        }
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
            
            executor.execute(() -> {
                try {
                    // 更新任务状态
                    currentTask.setExperimentCompletionTime(System.currentTimeMillis());
                    currentTask.setStatus("已完成");
                    currentTask.setExperimentType("MIXTURE"); // 设置实验类型为混合料实验
                    
                    // 保存实验数据
                    for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                        String mixRatioId = entry.getKey();
                        Map<String, String> experiments = entry.getValue();
                        
                        for (Map.Entry<String, String> experimentEntry : experiments.entrySet()) {
                            String experimentName = experimentEntry.getKey();
                            String value = experimentEntry.getValue();
                            
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
        executor.shutdown();
    }
}
