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

import com.example.labdata_main.adapter.ExperimentDataAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentDataItem;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordExperimentDataActivity extends AppCompatActivity implements ExperimentDataAdapter.OnScanDeviceClickListener {
    private RecyclerView rvExperiments;
    private ExperimentDataAdapter adapter;
    private MaterialButton btnSave;
    private AppDatabase database;
    private ExecutorService executor;
    private long taskId;
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

        // 获取任务ID
        taskId = getIntent().getLongExtra("taskId", -1);
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
        
        // 加载实验数据
        loadExperimentData();

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveExperimentData());
    }

    private void loadExperimentData() {
        executor.execute(() -> {
            ExperimentTask task = database.experimentTaskDao().getExperimentTaskById(taskId);
            if (task != null) {
                List<ExperimentDataItem> items = createExperimentList(task.getExperimentAssignments());
                runOnUiThread(() -> {
                    adapter = new ExperimentDataAdapter(items, this);
                    rvExperiments.setAdapter(adapter);
                });
            }
        });
    }

    private List<ExperimentDataItem> createExperimentList(Map<Long, List<String>> experimentAssignments) {
        List<ExperimentDataItem> items = new ArrayList<>();
        
        if (experimentAssignments != null) {
            for (Map.Entry<Long, List<String>> entry : experimentAssignments.entrySet()) {
                for (String experiment : entry.getValue()) {
                    ExperimentDataItem item = null;
                    switch (experiment) {
                        case "抗压强度":
                            item = new ExperimentDataItem("抗压强度实验", "抗压值", null);
                            break;
                        case "抗折强度":
                            item = new ExperimentDataItem("抗折强度实验", "抗折强度", null);
                            break;
                        case "抗渗性能":
                            item = new ExperimentDataItem("抗渗性能实验", "渗漏值", "性能值");
                            break;
                        case "抗冻性能":
                            item = new ExperimentDataItem("抗冻性能实验", "最低温度", "劈裂值");
                            break;
                        case "收缩性能":
                            item = new ExperimentDataItem("收缩性能实验", "收缩值", "收缩温度");
                            break;
                    }
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    @Override
    public void onScanDeviceClick(int position) {
        currentScanPosition = position;
        Intent intent = new Intent(this, ScanActivity.class);
        scanDeviceLauncher.launch(intent);
    }

    private void processScannedDevice(String scannedContent) {
        try {
            DeviceInfo deviceInfo = new Gson().fromJson(scannedContent, DeviceInfo.class);
            if (deviceInfo != null && deviceInfo.getType().equals("TESTING")) {
                deviceInfo.setName(String.format("%s %s", deviceInfo.getManufacturer(), deviceInfo.getModel()));
                deviceInfo.setDeviceId(deviceInfo.getModel());
                adapter.updateDeviceInfo(currentScanPosition, deviceInfo);
            } else {
                Toast.makeText(this, "无效的实验设备", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "二维码格式错误", Toast.LENGTH_SHORT).show();
        }
        currentScanPosition = -1;
    }

    private void saveExperimentData() {
        List<ExperimentDataItem> items = adapter.getItems();
        Log.d("RecordExperiment", "Starting to save experiment data for task: " + taskId);
        
        // 验证数据
        boolean isValid = true;
        for (ExperimentDataItem item : items) {
            if (item.getInput1Value() == 0) {
                isValid = false;
                Toast.makeText(this, "请填写所有必要的数据", Toast.LENGTH_SHORT).show();
                break;
            }
            if (item.hasSecondInput() && item.getInput2Value() == 0) {
                isValid = false;
                Toast.makeText(this, "请填写所有必要的数据", Toast.LENGTH_SHORT).show();
                break;
            }
            if (!item.hasDevice()) {
                isValid = false;
                Toast.makeText(this, "请为所有实验选择设备", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (isValid) {
            executor.execute(() -> {
                try {
                    database.runInTransaction(() -> {
                        // 更新任务状态为已完成
                        ExperimentTask task = database.experimentTaskDao().getExperimentTaskById(taskId);
                        Log.d("RecordExperiment", "Retrieved task: " + task);
                        if (task != null) {
                            Log.d("RecordExperiment", "Current task status: " + task.getStatus());
                            task.setStatus("已完成");
                            // 设置实验人员为当前登录用户
                            String currentUser = sharedPrefsManager.getUserName();
                            Log.d("RecordExperiment", "Current user name from SharedPrefs: " + currentUser);
                            if (currentUser != null && !currentUser.isEmpty()) {
                                task.setExperimenter(currentUser);
                                Log.d("RecordExperiment", "Set experimenter to: " + currentUser);
                            } else {
                                Log.w("RecordExperiment", "Current user name is null or empty");
                                // 尝试从其他方式获取用户信息
                                String email = sharedPrefsManager.getUserEmail();
                                if (email != null && !email.isEmpty()) {
                                    task.setExperimenter(email);
                                    Log.d("RecordExperiment", "Set experimenter to email: " + email);
                                }
                            }
                            task.setExperimentCompletionTime(System.currentTimeMillis());
                            database.experimentTaskDao().update(task);
                            Log.d("RecordExperiment", "Updated task with experimenter: " + task.getExperimenter());
                            
                            // 验证更新是否成功
                            ExperimentTask updatedTask = database.experimentTaskDao().getExperimentTaskById(taskId);
                            Log.d("RecordExperiment", "Verified task status after update: " + 
                                (updatedTask != null ? updatedTask.getStatus() : "task not found"));
                        } else {
                            Log.e("RecordExperiment", "Task not found with id: " + taskId);
                            return;
                        }

                        // 保存所有实验数据
                        for (ExperimentDataItem item : items) {
                            ExperimentData data = new ExperimentData();
                            data.setTaskId(taskId);
                            data.setExperimentName(item.getExperimentName());
                            data.setInput1Label(item.getInput1Label());
                            data.setInput1Value(item.getInput1Value());
                            
                            if (item.hasSecondInput()) {
                                data.setInput2Label(item.getInput2Label());
                                data.setInput2Value(item.getInput2Value());
                            }

                            // 设置实验结果
                            StringBuilder result = new StringBuilder();
                            result.append(item.getInput1Value());
                            if (item.hasSecondInput()) {
                                result.append(", ").append(item.getInput2Value());
                            }
                            data.setResult(result.toString());

                            // 从任务中获取并设置配比信息
                            if (task != null) {
                                List<MixRatio> selectedMixRatios = task.getSelectedMixRatios();
                                if (selectedMixRatios != null && !selectedMixRatios.isEmpty()) {
                                    // 将所有配比名称拼接在一起
                                    StringBuilder mixRatioNames = new StringBuilder();
                                    for (int i = 0; i < selectedMixRatios.size(); i++) {
                                        if (i > 0) {
                                            mixRatioNames.append(", ");
                                        }
                                        mixRatioNames.append(selectedMixRatios.get(i).getName());
                                    }
                                    data.setMixRatio(mixRatioNames.toString());
                                }
                            }

                            DeviceInfo deviceInfo = item.getDeviceInfo();
                            data.setDeviceManufacturer(deviceInfo.getManufacturer());
                            data.setDeviceModel(deviceInfo.getModel());
                            data.setDevicePurchaseYear(deviceInfo.getPurchaseYear());
                            data.setCreateTime(System.currentTimeMillis());

                            long dataId = database.experimentDataDao().insert(data);
                            Log.d("RecordExperiment", "Saved experiment data with id: " + dataId);
                        }
                    });

                    runOnUiThread(() -> {
                        // 发送广播通知任务状态更新
                        Intent intent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                        sendBroadcast(intent);
                        
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "数据保存成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    Log.e("RecordExperiment", "Error saving data", e);
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
