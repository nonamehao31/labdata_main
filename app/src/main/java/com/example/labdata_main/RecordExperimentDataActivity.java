package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.AsphaltExperimentDataAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentDataItem;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordExperimentDataActivity extends AppCompatActivity implements AsphaltExperimentDataAdapter.OnDeviceScanListener {
    private RecyclerView rvExperiments;
    private AsphaltExperimentDataAdapter asphaltAdapter;
    private MaterialButton btnSave;
    private AppDatabase database;
    private ExecutorService executor;
    private long taskId;
    private String experimentType;
    private int currentScanPosition = -1;
    private SharedPrefsManager sharedPrefsManager;

    private final ActivityResultLauncher<Intent> scanDeviceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String scannedContent = result.getData().getStringExtra("SCAN_RESULT");
                    if (scannedContent != null && currentScanPosition != -1) {
                        processScannedDevice(scannedContent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_record_experiment_data);

        // 获取任务ID和实验类型
        taskId = getIntent().getLongExtra("taskId", -1);
        experimentType = getIntent().getStringExtra("experiment_type");
        
        if (taskId == -1) {
            Toast.makeText(this, "无效的任务ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化数据库和线程池
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        sharedPrefsManager = new SharedPrefsManager(this);

        // 调试日志：打印当前登录用户信息
        String userName = sharedPrefsManager.getUserName();
        String userEmail = sharedPrefsManager.getUserEmail();
        Log.d("RecordExperiment", "Current user information - Name: " + userName + ", Email: " + userEmail);

        // 设置返回按钮
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 初始化视图
        rvExperiments = findViewById(R.id.rvExperiments);
        btnSave = findViewById(R.id.btnSave);

        // 设置RecyclerView
        rvExperiments.setLayoutManager(new LinearLayoutManager(this));
        
        // 根据实验类型加载不同的适配器和数据
        if ("ASPHALT".equals(experimentType)) {
            setupAsphaltExperiment();
        } else {
            // TODO: 实现其他实验类型的逻辑
        }

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveExperimentData());
    }

    private void setupAsphaltExperiment() {
        // 创建沥青实验适配器
        asphaltAdapter = new AsphaltExperimentDataAdapter();
        asphaltAdapter.setOnDeviceScanListener(this);
        rvExperiments.setAdapter(asphaltAdapter);

        // 加载沥青实验数据
        executor.execute(() -> {
            try {
                Log.d("SetupAsphalt", "开始加载沥青实验类型...");
                
                // 获取所有沥青实验类型
                List<ExperimentType> experimentTypes = database.experimentTypeDao()
                    .getExperimentTypesByCategory(ExperimentType.CATEGORY_ASPHALT);
                
                Log.d("SetupAsphalt", "查询结果: " + 
                    (experimentTypes != null ? experimentTypes.size() : 0) + " 个实验类型");
                if (experimentTypes != null) {
                    for (ExperimentType type : experimentTypes) {
                        Log.d("SetupAsphalt", "实验类型: " + type.getName() + 
                            ", 类型: " + type.getType() + 
                            ", 类别: " + type.getCategory());
                    }
                }

                // 如果没有找到实验类型，显示错误消息
                if (experimentTypes == null || experimentTypes.isEmpty()) {
                    Log.w("SetupAsphalt", "未找到任何实验类型");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "未找到任何实验类型", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    List<String> typeNames = new ArrayList<>();
                    for (ExperimentType type : experimentTypes) {
                        typeNames.add(type.getType());
                    }
                    Log.d("SetupAsphalt", "更新UI，显示 " + typeNames.size() + " 个实验类型");
                    asphaltAdapter.setExperimentTypes(typeNames);
                });
            } catch (Exception e) {
                Log.e("SetupAsphalt", "Error loading experiment types", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载实验类型时出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    public void onScanDevice(int position) {
        currentScanPosition = position;
        // 启动扫描设备的Activity
        Intent intent = new Intent(this, ScanDeviceActivity.class);
        scanDeviceLauncher.launch(intent);
    }

    private void processScannedDevice(String deviceCode) {
        // 处理扫描到的设备编号
        if (asphaltAdapter != null && currentScanPosition >= 0) {
            try {
                // 验证设备编号格式
                if (deviceCode == null || deviceCode.trim().isEmpty()) {
                    throw new IllegalArgumentException("无效的设备编号");
                }

                // 在数据库中查找设备
                executor.execute(() -> {
                    try {
                        Device device = database.deviceDao().getDeviceByCode(deviceCode);
                        if (device != null) {
                            // 将 Device 转换为 DeviceInfo，确保设置所有必要字段
                            DeviceInfo deviceInfo = new DeviceInfo(
                                device.getId(),          // deviceId
                                device.getType(),        // type
                                device.getManufacturer(), // manufacturer
                                device.getModel(),        // model
                                device.getPurchaseYear(), // purchaseYear
                                device.getCompanyId()     // companyId
                            );
                            
                            // 在主线程中更新UI
                            runOnUiThread(() -> {
                                Log.d("DeviceInfo", "Updating device info: " + 
                                    "Name=" + deviceInfo.getName() + 
                                    ", Manufacturer=" + deviceInfo.getManufacturer() + 
                                    ", Model=" + deviceInfo.getModel());
                                    
                                asphaltAdapter.updateDeviceInfo(currentScanPosition, deviceInfo);
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    "设备扫描成功：" + deviceInfo.getName(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    "未找到该设备：" + deviceCode, 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("ProcessDevice", "Error processing device: " + deviceCode, e);
                        runOnUiThread(() -> {
                            Toast.makeText(RecordExperimentDataActivity.this, 
                                "处理设备信息时出错：" + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (Exception e) {
                Log.e("ProcessDevice", "Error validating device code", e);
                Toast.makeText(this, "设备码格式错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveExperimentData() {
        if ("ASPHALT".equals(experimentType)) {
            saveAsphaltExperimentData();
        } else {
            Toast.makeText(this, "暂不支持该实验类型", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAsphaltExperimentData() {
        if (!validateExperimentData()) {
            return;
        }

        executor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    // 获取实验数据
                    Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
                    String experimenter = sharedPrefsManager.getUserName();
                    long createTime = System.currentTimeMillis();

                    // 保存每个实验的数据
                    for (Map.Entry<String, Map<String, String>> entry : experimentData.entrySet()) {
                        String type = entry.getKey();
                        Map<String, String> data = entry.getValue();
                        
                        AsphaltExperimentData experimentRecord = new AsphaltExperimentData();
                        experimentRecord.setTaskId(taskId);
                        experimentRecord.setExperimentType(type);
                        experimentRecord.setExperimentValues(data);
                        experimentRecord.setExperimenter(experimenter);
                        experimentRecord.setCreateTime(createTime);
                        
                        // 设置设备信息
                        if (data.containsKey("device_id")) {
                            experimentRecord.setDeviceCode(data.get("device_id"));
                            // 设置设备制造商和型号
                            if (data.containsKey("device_manufacturer")) {
                                experimentRecord.setDeviceManufacturer(data.get("device_manufacturer"));
                            }
                            if (data.containsKey("device_model")) {
                                experimentRecord.setDeviceModel(data.get("device_model"));
                            }
                        }
                        
                        database.asphaltExperimentDataDao().insert(experimentRecord);
                    }

                    // 更新任务状态为已完成
                    ExperimentTask task = database.experimentTaskDao().getTaskById((int)taskId);
                    if (task != null) {
                        task.setStatus("已完成"); // 设置任务状态为"已完成"
                        task.setExperimentCompletionTime(System.currentTimeMillis()); // 设置完成时间
                        database.experimentTaskDao().update(task);
                    }
                });

                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);

                // 在主线程中显示成功消息并关闭页面
                runOnUiThread(() -> {
                    Toast.makeText(this, "实验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // 设置结果码，通知上一个页面刷新数据
                    finish();
                });
            } catch (Exception e) {
                Log.e("SaveData", "Error saving experiment data", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, 
                        String.format("保存数据时出错：%s", e.getMessage()), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateExperimentData() {
        if (asphaltAdapter == null) {
            Toast.makeText(this, "实验数据适配器未初始化", Toast.LENGTH_SHORT).show();
            return false;
        }

        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
        if (experimentData == null || experimentData.isEmpty()) {
            Toast.makeText(this, "没有要保存的实验数据", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
