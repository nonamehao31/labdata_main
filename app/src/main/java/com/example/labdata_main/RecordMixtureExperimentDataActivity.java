package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.MixtureExperimentDataAdapter;
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
        long taskId = getIntent().getLongExtra("taskId", -1);
        if (taskId != -1) {
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

    private void loadTaskData(long taskId) {
        executor.execute(() -> {
            try {
                // 获取完整的任务信息
                currentTask = database.experimentTaskDao().getFullTaskById(taskId);
                
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
                            Toast.makeText(this, "未找到配比或实验信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "未找到任务信息", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading task data", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载任务数据时出错", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
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
