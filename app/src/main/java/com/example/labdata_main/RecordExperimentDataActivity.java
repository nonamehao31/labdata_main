package com.example.labdata_main;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import com.example.labdata_main.adapter.AsphaltExperimentDataAdapter;
import com.example.labdata_main.api.RetrofitClient;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.AsphaltDetailResponse;
import com.example.labdata_main.api.service.AsphaltTaskService;
import com.example.labdata_main.api.request.DuctilityTestRequest;
import com.example.labdata_main.api.request.BbrTestRequest;
import com.example.labdata_main.api.request.DynamicShearRheometerTestRequest;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.api.request.PenetrationTestRequest;
import com.example.labdata_main.api.request.SofteningPointTestRequest;
import com.example.labdata_main.api.request.BrookfieldViscosityTestRequest;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordExperimentDataActivity extends AppCompatActivity implements AsphaltExperimentDataAdapter.OnDeviceScanListener {
    private RecyclerView rvExperiments;
    private AsphaltExperimentDataAdapter asphaltAdapter;
    private MaterialButton btnSave;
    private AppDatabase database;
    private ExecutorService executor;
    private long taskId;
    private String experimentType;
    private int currentScanPosition = -1;
    private String taskIdString;
    private SharedPrefsManager sharedPrefsManager;
    private AsphaltTaskService asphaltTaskService;

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
        taskIdString = getIntent().getStringExtra("taskId");
        experimentType = getIntent().getStringExtra("experiment_type");

        Log.d("RecordExperiment", "收到传入的任务ID参数: " + taskIdString + ", 实验类型: " + experimentType);

        // 判断任务ID是否为UUID格式
        if (taskIdString != null && !taskIdString.isEmpty()) {
            // 如果是UUID格式，则直接使用该UUID进行API请求
            if (taskIdString.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                Log.d("RecordExperiment", "检测到UUID格式任务ID: " + taskIdString);
                // 将UUID保存为字符串ID，后续使用该UUID查询API
                taskId = -1; // 将数字ID设为-1表示使用UUID
                // 这里应该保存UUID，但当前应用主要使用数字ID，需要更多修改
                // TODO: 保存和使用UUID进行API查询
            } else {
                // 尝试将其转换为数字ID
                try {
                    taskId = Long.parseLong(taskIdString);
                    Log.d("RecordExperiment", "解析数字任务ID: " + taskId);
                } catch (NumberFormatException e) {
                    Log.e("RecordExperiment", "无法解析任务ID: " + taskIdString, e);
                    Toast.makeText(this, "无效的任务ID格式", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        } else {
            // 获取数字任务ID（后向兼容）
            taskId = getIntent().getLongExtra("taskId", -1);
            Log.d("RecordExperiment", "使用数字任务ID: " + taskId);
        }

        if (taskId == -1 && (taskIdString == null || taskIdString.isEmpty())) {
            Toast.makeText(this, "无效的任务ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化数据库和线程池
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        sharedPrefsManager = new SharedPrefsManager(this);
        asphaltTaskService = RetrofitClient.getInstance(this).createService(AsphaltTaskService.class);

        // 调试日志：打印当前登录用户信息
        String userName = sharedPrefsManager.getUserName();
        String userEmail = sharedPrefsManager.getUserEmail();
        Log.d("RecordExperiment", "当前用户信息 - 姓名: " + userName + ", 邮箱: " + userEmail);

        // 注释掉本地数据库验证逻辑，因为现在已经改用API获取数据
        /*
        // 验证任务是否存在
        executor.execute(() -> {
            ExperimentTask task = null;
            if (taskId != -1) {
                // 使用数字ID查询
                task = database.experimentTaskDao().getFullTaskById(taskId);
            } else if (taskIdString != null && !taskIdString.isEmpty()) {
                // 使用字符串任务ID查询
                task = database.experimentTaskDao().getFullTaskByTaskId(taskIdString);
            }

            if (task == null) {
                Log.e("RecordExperiment", "在数据库中未找到任务: " + (taskId != -1 ? taskId : taskIdString));
                runOnUiThread(() -> {
                    Toast.makeText(this, "未找到任务", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            Log.d("RecordExperiment", "成功获取任务: " + task.getTaskName());
            Log.d("RecordExperiment", "任务实验指派: " + (task.getExperimentAssignments() != null ? task.getExperimentAssignments().toString() : "null"));
            Log.d("RecordExperiment", "任务备注: " + task.getNotes());
        });
        */

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

        // 添加数据验证监听器
        asphaltAdapter.setOnDataValidityChangeListener(isValid -> {
            // 根据数据有效性设置保存按钮的启用状态
            if (btnSave != null) {
                btnSave.setEnabled(isValid);
            }
        });

        // 初始状态下禁用保存按钮
        if (btnSave != null) {
            btnSave.setEnabled(false);
        }

        // 加载沥青实验数据，使用API而不是本地数据库
        Log.d("SetupAsphalt", "开始从API加载沥青实验类型...");
        Log.d("SetupAsphalt", "使用任务ID: " + taskIdString);

        // 显示加载指示器
        showLoading(true);

        asphaltTaskService.getAsphaltDetailByTaskId(taskIdString).enqueue(new Callback<ApiResponse<AsphaltDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AsphaltDetailResponse>> call,
                    Response<ApiResponse<AsphaltDetailResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AsphaltDetailResponse detailResponse = response.body().getData();
                    if (detailResponse != null) {
                        Log.d("SetupAsphalt", "成功获取沥青任务详情");
                        processAsphaltDetailResponse(detailResponse);
                    } else {
                        Log.e("SetupAsphalt", "API返回了空的详情数据");
                        showError("获取实验数据失败：返回的数据为空");
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                    Log.e("SetupAsphalt", "API请求失败: " + errorMsg);
                    showError("获取实验数据失败：" + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AsphaltDetailResponse>> call, Throwable t) {
                showLoading(false);
                Log.e("SetupAsphalt", "API请求异常", t);
                showError("网络错误：" + t.getMessage());
            }
        });
    }

    private void processAsphaltDetailResponse(AsphaltDetailResponse detailResponse) {
        if (detailResponse == null) {
            showError("未能获取沥青实验任务详情");
            return;
        }

        // 获取选定的实验类型
        Set<String> selectedExperiments = new HashSet<>();
        Map<Long, List<String>> assignments = detailResponse.getExperimentAssignments();
        if (assignments != null) {
            for (List<String> experimentList : assignments.values()) {
                if (experimentList != null) {
                    selectedExperiments.addAll(experimentList);
                }
            }
        }

        if (selectedExperiments.isEmpty()) {
            Log.w("SetupAsphalt", "任务中没有选择任何实验");
            showError("任务中没有选择任何实验");
            return;
        }

        // 转换实验类型为标准格式
        Set<String> standardizedExperiments = new HashSet<>();
        for (String experimentType : selectedExperiments) {
            String standardType = standardizeExperimentType(experimentType);
            standardizedExperiments.add(standardType);
            Log.d("SetupAsphalt", "转换实验类型: " + experimentType + " -> " + standardType);
        }

        // 使用标准化后的实验类型
        List<String> experimentTypesList = new ArrayList<>(standardizedExperiments);

        // 如果只有一个实验类型，明确设置当前活动的实验类型
        if (experimentTypesList.size() == 1) {
            experimentType = experimentTypesList.get(0);
            Log.d("SetupAsphalt", "设置当前实验类型为: " + experimentType);
        }

        // 获取沥青信息（如果有）
        List<AsphaltDetailResponse.AsphaltInfo> asphaltInfoList = detailResponse.getAsphaltInfoList();

        // 初始化每个实验类型的数据Map
        Map<String, Map<String, String>> initialDataMap = new HashMap<>();
        for (String type : experimentTypesList) {
            Map<String, String> dataMap = new HashMap<>();

            // 如果有沥青信息，添加到实验数据中
            if (asphaltInfoList != null && !asphaltInfoList.isEmpty()) {
                AsphaltDetailResponse.AsphaltInfo asphaltInfo = asphaltInfoList.get(0); // 取第一个沥青信息

                // 确保值不为null，防止空指针异常
                String supplier = asphaltInfo.getAsphaltSupplier() != null ? asphaltInfo.getAsphaltSupplier() : "";
                String grade = asphaltInfo.getAsphaltGrade() != null ? asphaltInfo.getAsphaltGrade() : "";
                String catalog = asphaltInfo.getAsphaltCatalog() != null ? asphaltInfo.getAsphaltCatalog() : "";

                // 添加沥青信息到数据Map
                dataMap.put("asphalt_supplier", supplier);
                dataMap.put("asphalt_grade", grade);
                dataMap.put("asphalt_catalog", catalog);

                if (asphaltInfo.getAsphaltId() != null) {
                    dataMap.put("asphalt_id", String.valueOf(asphaltInfo.getAsphaltId()));
                }

                Log.d("SetupAsphalt", "添加沥青信息: 供应商=" + supplier + ", 等级=" + grade + ", 类型=" + catalog);
            }

            initialDataMap.put(type, dataMap);
        }

        // 设置实验类型列表和初始数据到适配器
        asphaltAdapter.setExperimentTypes(experimentTypesList);
        asphaltAdapter.setInitialData(initialDataMap);

        // 在设置完初始实验类型后获取状态并过滤已完成的实验
        fetchExperimentStatus();
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
                                device.getType(),        // type
                                device.getManufacturer(), // manufacturer
                                device.getModel(),        // model
                                device.getPurchaseYear(), // purchaseYear
                                device.getCompanyId(),    // name (used as companyId)
                                device.getId()            // deviceId
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
        // 添加调试日志
        Log.d("SaveData", "保存实验数据 - 开始 - 当前实验类型: " + experimentType);

        // 获取实验数据
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();

        // 打印所有实验类型
        Log.d("SaveData", "实验数据包含的类型: " + experimentData.keySet());

        // 验证实验类型
        if (experimentType == null || experimentType.isEmpty()) {
            Log.w("SaveData", "实验类型未指定，尝试从数据中检测");

            // 在这段代码中添加延度实验类型处理（大约在第370行左右）
            // 从数据中检测实验类型
            if (experimentData.containsKey(AsphaltExperimentData.TYPE_SOFTENING_POINT)) {
                experimentType = AsphaltExperimentData.TYPE_SOFTENING_POINT;
                Log.d("SaveData", "检测到软化点实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_PENETRATION)) {
                experimentType = AsphaltExperimentData.TYPE_PENETRATION;
                Log.d("SaveData", "检测到针入度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_DUCTILITY)) {
                experimentType = AsphaltExperimentData.TYPE_DUCTILITY;
                Log.d("SaveData", "检测到延度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY)) {
                experimentType = AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY;
                Log.d("SaveData", "检测到布克菲尔粘度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_BBR)) {
                experimentType = AsphaltExperimentData.TYPE_BBR;
                Log.d("SaveData", "检测到弯曲蠕变劲度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_DSR)) {
                experimentType = AsphaltExperimentData.TYPE_DSR;
                Log.d("SaveData", "检测到动态剪切流变仪实验数据，设置实验类型为: " + experimentType);
            } else {
                // 其他处理逻辑...
            }
            
            // 从数据中检测实验类型
            if (experimentData.containsKey(AsphaltExperimentData.TYPE_SOFTENING_POINT)) {
                experimentType = AsphaltExperimentData.TYPE_SOFTENING_POINT;
                Log.d("SaveData", "检测到软化点实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_PENETRATION)) {
                experimentType = AsphaltExperimentData.TYPE_PENETRATION;
                Log.d("SaveData", "检测到针入度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_DUCTILITY)) {
                experimentType = AsphaltExperimentData.TYPE_DUCTILITY;
                Log.d("SaveData", "检测到延度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY)) {
                experimentType = AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY;
                Log.d("SaveData", "检测到布克菲尔粘度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_BBR)) {
                experimentType = AsphaltExperimentData.TYPE_BBR;
                Log.d("SaveData", "检测到弯曲蠕变劲度实验数据，设置实验类型为: " + experimentType);
            } else if (experimentData.containsKey(AsphaltExperimentData.TYPE_DSR)) {
                experimentType = AsphaltExperimentData.TYPE_DSR;
                Log.d("SaveData", "检测到动态剪切流变仪实验数据，设置实验类型为: " + experimentType);
            } else {
                // 如果只有一个实验类型，使用它
                if (experimentData.size() == 1) {
                    experimentType = experimentData.keySet().iterator().next();
                    Log.d("SaveData", "数据中只有一个实验类型，设置为: " + experimentType);
                } else {
                    Toast.makeText(this, "无法确定实验类型", Toast.LENGTH_SHORT).show();
                    Log.e("SaveData", "无法确定实验类型，数据包含: " + experimentData.keySet());
                    return;
                }
            }
        } else {
            // 确认数据中是否包含该实验类型
            if (!experimentData.containsKey(experimentType)) {
                Log.w("SaveData", "数据中不包含当前实验类型: " + experimentType + "，尝试查找替代类型");

                // 检查是否有软化点数据
                if (experimentData.containsKey(AsphaltExperimentData.TYPE_SOFTENING_POINT)) {
                    experimentType = AsphaltExperimentData.TYPE_SOFTENING_POINT;
                    Log.d("SaveData", "找到软化点实验数据，使用该类型: " + experimentType);
                } else if (experimentData.size() == 1) {
                    experimentType = experimentData.keySet().iterator().next();
                    Log.d("SaveData", "使用唯一可用的实验类型: " + experimentType);
                } else {
                    Toast.makeText(this, "找不到匹配的实验数据", Toast.LENGTH_SHORT).show();
                    Log.e("SaveData", "找不到匹配的实验数据，当前类型: " + experimentType + "，可用类型: " + experimentData.keySet());
                    return;
                }
            } else {
                Log.d("SaveData", "确认使用当前实验类型: " + experimentType);
            }
        }

        // 记录最终使用的实验类型
        Log.d("SaveData", "最终使用的实验类型: " + experimentType);

        // 根据实验类型调用相应的保存方法
        if (AsphaltExperimentData.TYPE_PENETRATION.equals(experimentType)) {
            savePenetrationExperimentData();
        } else if (AsphaltExperimentData.TYPE_SOFTENING_POINT.equals(experimentType)) {
            saveSofteningPointExperimentData();
        } else if (AsphaltExperimentData.TYPE_DUCTILITY.equals(experimentType)) {
            saveDuctilityExperimentData();
        } else if (AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY.equals(experimentType)) {
            saveBrookfieldViscosityExperimentData();
        } else if (AsphaltExperimentData.TYPE_BBR.equals(experimentType)) {
            saveBbrExperimentData();
        } else if (AsphaltExperimentData.TYPE_DSR.equals(experimentType)) {
            saveDynamicShearRheometerExperimentData();
        } else {
            saveAsphaltExperimentData();
        }
    }

    /**
     * 保存针入度实验数据
     * 专门处理针入度实验的数据验证和保存
     */
    private void savePenetrationExperimentData() {
        if (!validateExperimentData()) {
            return;
        }

        // 获取实验数据
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();

        // 检查是否包含针入度实验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_PENETRATION)) {
            Toast.makeText(this, "未找到针入度实验数据", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> penetrationData = experimentData.get(AsphaltExperimentData.TYPE_PENETRATION);

        // 验证必要的数据字段
        if (penetrationData == null ||
            !penetrationData.containsKey(AsphaltExperimentData.Fields.TEMPERATURE) ||
            TextUtils.isEmpty(penetrationData.get(AsphaltExperimentData.Fields.TEMPERATURE))) {
            Toast.makeText(this, "针入度实验温度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!penetrationData.containsKey(AsphaltExperimentData.Fields.Penetration.READING) ||
            TextUtils.isEmpty(penetrationData.get(AsphaltExperimentData.Fields.Penetration.READING))) {
            Toast.makeText(this, "针入度实验读数不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 记录日志
        Log.d("SaveData", "针入度实验数据 - 温度: " +
              penetrationData.get(AsphaltExperimentData.Fields.TEMPERATURE) +
              ", 读数: " + penetrationData.get(AsphaltExperimentData.Fields.Penetration.READING));

        executor.execute(() -> {
            try {
                // 构建要提交的数据对象
                PenetrationTestRequest request = new PenetrationTestRequest();

                // 使用原始字符串类型的taskIdString而不是转换后的taskId
                // 记录日志输出taskIdString的值
                Log.d("SaveData", "针入度实验数据提交 - 任务ID字符串: " + taskIdString);
                request.setTaskId(taskIdString); // 直接使用从intent获取的原始ID字符串
                request.setTemperature(penetrationData.get(AsphaltExperimentData.Fields.TEMPERATURE));
                request.setReading(penetrationData.get(AsphaltExperimentData.Fields.Penetration.READING));
                request.setExperimenter(sharedPrefsManager.getUserName());
                request.setTestDate(System.currentTimeMillis());

                // 设置设备信息
                if (penetrationData.containsKey("device_id")) {
                    request.setDeviceId(penetrationData.get("device_id"));
                    if (penetrationData.containsKey("device_name")) {
                        request.setDeviceName(penetrationData.get("device_name"));
                    }
                    if (penetrationData.containsKey("device_manufacturer")) {
                        request.setDeviceManufacturer(penetrationData.get("device_manufacturer"));
                    }
                    if (penetrationData.containsKey("device_model")) {
                        request.setDeviceModel(penetrationData.get("device_model"));
                    }
                }

                // 保存到本地数据库
                database.runInTransaction(() -> {
                    AsphaltExperimentData experimentRecord = new AsphaltExperimentData();
                    experimentRecord.setTaskId(taskId);
                    experimentRecord.setExperimentType(AsphaltExperimentData.TYPE_PENETRATION);
                    experimentRecord.setExperimentValues(penetrationData);
                    experimentRecord.setExperimenter(sharedPrefsManager.getUserName());
                    experimentRecord.setCreateTime(System.currentTimeMillis());

                    // 设置设备信息
                    if (penetrationData.containsKey("device_id")) {
                        experimentRecord.setDeviceCode(penetrationData.get("device_id"));
                        if (penetrationData.containsKey("device_manufacturer")) {
                            experimentRecord.setDeviceManufacturer(penetrationData.get("device_manufacturer"));
                        }
                        if (penetrationData.containsKey("device_model")) {
                            experimentRecord.setDeviceModel(penetrationData.get("device_model"));
                        }
                    }

                    database.asphaltExperimentDataDao().insert(experimentRecord);

                    // 更新任务状态为已完成
                    ExperimentTask task = database.experimentTaskDao().getTaskById((int)taskId);
                    if (task != null) {
                        task.setStatus("已完成");
                        task.setExperimentCompletionTime(System.currentTimeMillis());
                        database.experimentTaskDao().update(task);
                    }
                });

                // 同步发送到服务器
                if (asphaltTaskService != null) {
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitPenetrationTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "针入度实验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_PENETRATION);
                            } else {
                                Log.e("SaveData", "提交针入度实验数据到服务器失败: " +
                                      (response.body() != null ? response.body().getMessage() : "未知错误"));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交针入度实验数据到服务器失败", t);
                        }
                    });
                }

                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);

                // 在主线程中显示成功消息并关闭页面
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this,
                        "针入度实验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.e("SaveData", "保存针入度实验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this,
                        String.format("保存针入度实验数据时出错：%s", e.getMessage()),
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 保存软化点试验数据
     */
    private void saveSofteningPointExperimentData() {
        Log.d("SaveData", "开始保存软化点试验数据");

        // 验证数据
        if (!validateExperimentData()) {
            return;
        }

        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();

        // 检查是否包含软化点试验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_SOFTENING_POINT)) {
            Log.e("SaveData", "未找到软化点试验数据，实验类型键值 = " + AsphaltExperimentData.TYPE_SOFTENING_POINT);
            Log.e("SaveData", "可用的实验数据键值: " + experimentData.keySet());
            Toast.makeText(this, "未找到软化点试验数据", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> softeningPointData = experimentData.get(AsphaltExperimentData.TYPE_SOFTENING_POINT);

        // 添加调试日志
        Log.d("SaveData", "软化点试验数据内容: " + softeningPointData.toString());
        Log.d("SaveData", "温度字段键: " + AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE);
        Log.d("SaveData", "软化温度字段键: " + AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE);

        // 验证必要的数据字段
        if (softeningPointData == null ||
            !softeningPointData.containsKey(AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE) ||
            TextUtils.isEmpty(softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE))) {
            Log.e("SaveData", "软化点试验初始温度不能为空");
            Toast.makeText(this, "软化点试验初始温度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!softeningPointData.containsKey(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE) ||
            TextUtils.isEmpty(softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE))) {
            Log.e("SaveData", "软化点试验软化温度不能为空");
            Toast.makeText(this, "软化点试验软化温度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 记录日志
        Log.d("SaveData", "软化点试验数据 - 初始温度: " +
              softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE) +
              ", 软化温度: " + softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE));

        executor.execute(() -> {
            try {
                // 构建要提交的数据对象
                SofteningPointTestRequest request = new SofteningPointTestRequest();

                // 使用原始字符串类型的taskIdString，避免大数值问题
                Log.d("SaveData", "软化点试验数据提交 - 任务ID字符串: " + taskIdString);
                request.setTaskId(taskIdString);
                request.setTemperature(softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.TEMPERATURE));
                request.setSofteningTemperature(softeningPointData.get(AsphaltExperimentData.Fields.SofteningPoint.SOFTENING_TEMPERATURE));
                request.setExperimenter(sharedPrefsManager.getUserName());
                request.setTestDate(System.currentTimeMillis());

                // 设置设备信息
                if (softeningPointData.containsKey("device_id")) {
                    request.setDeviceId(softeningPointData.get("device_id"));
                    if (softeningPointData.containsKey("device_name")) {
                        request.setDeviceName(softeningPointData.get("device_name"));
                    }
                    if (softeningPointData.containsKey("device_manufacturer")) {
                        request.setDeviceManufacturer(softeningPointData.get("device_manufacturer"));
                    }
                    if (softeningPointData.containsKey("device_model")) {
                        request.setDeviceModel(softeningPointData.get("device_model"));
                    }
                }

                // 不再保存到本地数据库，只发送到服务器
                // 同步发送到服务器
                if (asphaltTaskService != null) {
                    Log.d("SaveData", "开始提交软化点试验数据到服务器 - 请求内容: " + request.toString());
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitSofteningPointTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "软化点试验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_SOFTENING_POINT);

                                // 在主线程中显示成功消息并关闭页面
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this,
                                        "软化点试验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                String errorMsg = (response.body() != null) ? response.body().getMessage() : "未知错误";
                                Log.e("SaveData", "提交软化点试验数据到服务器失败: " + errorMsg);
                                Log.e("SaveData", "HTTP状态码: " + response.code());

                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this,
                                        "提交软化点试验数据到服务器失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交软化点试验数据到服务器失败", t);
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this,
                                    String.format("提交软化点试验数据到服务器失败：%s", t.getMessage()),
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Log.e("SaveData", "asphaltTaskService is null");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this,
                            "无法连接到服务器，请检查网络连接", Toast.LENGTH_SHORT).show();
                    });
                }

                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);
            } catch (Exception e) {
                Log.e("SaveData", "保存软化点试验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this,
                        String.format("保存软化点试验数据时出错：%s", e.getMessage()),
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 保存延度试验数据
     */
    private void saveDuctilityExperimentData() {
        Log.d("SaveData", "开始保存延度试验数据");
        
        // 验证数据
        if (!validateExperimentData()) {
            return;
        }
    
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
        
        // 检查是否包含延度试验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_DUCTILITY)) {
            Log.e("SaveData", "未找到延度试验数据，实验类型键值 = " + AsphaltExperimentData.TYPE_DUCTILITY);
            Log.e("SaveData", "可用的实验数据键值: " + experimentData.keySet());
            Toast.makeText(this, "未找到延度试验数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> ductilityData = experimentData.get(AsphaltExperimentData.TYPE_DUCTILITY);
        
        // 添加调试日志
        Log.d("SaveData", "延度试验数据内容: " + ductilityData.toString());
        Log.d("SaveData", "温度字段键: " + AsphaltExperimentData.Fields.TEMPERATURE);
        Log.d("SaveData", "位移字段键: " + AsphaltExperimentData.Fields.Ductility.DISPLACEMENT);
        
        // 验证必要的数据字段
        if (ductilityData == null || 
            !ductilityData.containsKey(AsphaltExperimentData.Fields.TEMPERATURE) || 
            TextUtils.isEmpty(ductilityData.get(AsphaltExperimentData.Fields.TEMPERATURE))) {
            Log.e("SaveData", "延度试验温度不能为空");
            Toast.makeText(this, "延度试验温度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!ductilityData.containsKey(AsphaltExperimentData.Fields.Ductility.DISPLACEMENT) || 
            TextUtils.isEmpty(ductilityData.get(AsphaltExperimentData.Fields.Ductility.DISPLACEMENT))) {
            Log.e("SaveData", "延度试验拉长位移不能为空");
            Toast.makeText(this, "延度试验拉长位移不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 记录日志
        Log.d("SaveData", "延度试验数据 - 温度: " + 
              ductilityData.get(AsphaltExperimentData.Fields.TEMPERATURE) + 
              ", 拉长位移: " + ductilityData.get(AsphaltExperimentData.Fields.Ductility.DISPLACEMENT));
            
        executor.execute(() -> {
            try {
                // 构建要提交的数据对象
                DuctilityTestRequest request = new DuctilityTestRequest();
                
                // 使用原始字符串类型的taskIdString，避免大数值问题
                Log.d("SaveData", "延度试验数据提交 - 任务ID字符串: " + taskIdString);
                request.setTaskId(taskIdString);
                request.setTemperature(ductilityData.get(AsphaltExperimentData.Fields.TEMPERATURE));
                request.setDisplacement(ductilityData.get(AsphaltExperimentData.Fields.Ductility.DISPLACEMENT));
                request.setExperimenter(sharedPrefsManager.getUserName());
                request.setTestDate(System.currentTimeMillis());
                
                // 设置设备信息
                if (ductilityData.containsKey("device_id")) {
                    request.setDeviceId(ductilityData.get("device_id"));
                    if (ductilityData.containsKey("device_name")) {
                        request.setDeviceName(ductilityData.get("device_name"));
                    }
                    if (ductilityData.containsKey("device_manufacturer")) {
                        request.setDeviceManufacturer(ductilityData.get("device_manufacturer"));
                    }
                    if (ductilityData.containsKey("device_model")) {
                        request.setDeviceModel(ductilityData.get("device_model"));
                    }
                }
                
                // 不再保存到本地数据库，只发送到服务器
                // 同步发送到服务器
                if (asphaltTaskService != null) {
                    Log.d("SaveData", "开始提交延度试验数据到服务器 - 请求内容: " + request.toString());
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitDuctilityTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "延度试验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_DUCTILITY);

                                // 在主线程中显示成功消息并关闭页面
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "延度试验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                String errorMsg = (response.body() != null) ? response.body().getMessage() : "未知错误";
                                Log.e("SaveData", "提交延度试验数据到服务器失败: " + errorMsg);
                                Log.e("SaveData", "HTTP状态码: " + response.code());
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "提交延度试验数据到服务器失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交延度试验数据到服务器失败", t);
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    String.format("提交延度试验数据到服务器失败：%s", t.getMessage()), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Log.e("SaveData", "asphaltTaskService is null");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "无法连接到服务器，请检查网络连接", Toast.LENGTH_SHORT).show();
                    });
                }
                
                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);
            } catch (Exception e) {
                Log.e("SaveData", "保存延度试验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this, 
                        String.format("保存延度试验数据时出错：%s", e.getMessage()), 
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

    /**
     * 保存布鲁克菲尔德旋转黏度实验数据
     */
    private void saveBrookfieldViscosityExperimentData() {
        Log.d("SaveData", "开始保存布鲁克菲尔德旋转黏度实验数据");
        
        // 验证数据
        if (!validateExperimentData()) {
            return;
        }
    
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
        
        // 检查是否包含布鲁克菲尔德旋转黏度实验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY)) {
            Log.e("SaveData", "未找到布鲁克菲尔德旋转黏度实验数据，实验类型键值 = " + AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY);
            Log.e("SaveData", "可用的实验数据键值: " + experimentData.keySet());
            Toast.makeText(this, "未找到布鲁克菲尔德旋转黏度实验数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> viscosityData = experimentData.get(AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY);
        
        // 添加调试日志
        Log.d("SaveData", "布鲁克菲尔德旋转黏度实验数据内容: " + viscosityData.toString());
        
        // 验证必要的数据字段 - 布鲁克菲尔德旋转黏度实验需要至少一个温度点和对应的黏度测量值
        boolean hasTemperaturePoint = false;
        
        for (String key : viscosityData.keySet()) {
            if (key.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX)) {
                hasTemperaturePoint = true;
                
                // 检查该温度点是否有对应的黏度值
                String pointId = key.substring(AsphaltExperimentData.Fields.BrookfieldViscosity.TEMPERATURE_PREFIX.length());
                boolean hasViscosityValue = false;
                
                for (String viscosityKey : viscosityData.keySet()) {
                    if (viscosityKey.startsWith(AsphaltExperimentData.Fields.BrookfieldViscosity.VISCOSITY_PREFIX + pointId + "_")) {
                        hasViscosityValue = true;
                        break;
                    }
                }
                
                if (!hasViscosityValue) {
                    Log.e("SaveData", "温度点 " + pointId + " 没有对应的黏度测量值");
                    Toast.makeText(this, "温度点 " + pointId + " 没有对应的黏度测量值", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        
        if (!hasTemperaturePoint) {
            Log.e("SaveData", "布鲁克菲尔德旋转黏度实验至少需要一个温度点");
            Toast.makeText(this, "布鲁克菲尔德旋转黏度实验至少需要一个温度点", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(() -> {
            try {
                // 构建要提交的数据对象
                BrookfieldViscosityTestRequest request = new BrookfieldViscosityTestRequest();
                
                // 使用原始字符串类型的taskIdString，避免大数值问题
                Log.d("SaveData", "布鲁克菲尔德旋转黏度实验数据提交 - 任务ID字符串: " + taskIdString);
                request.setTaskId(taskIdString);
                request.setExperimenter(sharedPrefsManager.getUserName());
                request.setTestDate(System.currentTimeMillis());
                
                // 设置设备信息
                if (viscosityData.containsKey("device_id")) {
                    request.setDeviceId(viscosityData.get("device_id"));
                    if (viscosityData.containsKey("device_name")) {
                        request.setDeviceName(viscosityData.get("device_name"));
                    }
                    if (viscosityData.containsKey("device_manufacturer")) {
                        request.setDeviceManufacturer(viscosityData.get("device_manufacturer"));
                    }
                    if (viscosityData.containsKey("device_model")) {
                        request.setDeviceModel(viscosityData.get("device_model"));
                    }
                }
                
                // 设置所有实验值
                request.setExperimentValues(viscosityData);
                
                // 发送到服务器
                if (asphaltTaskService != null) {
                    Log.d("SaveData", "开始提交布鲁克菲尔德旋转黏度实验数据到服务器 - 请求内容: " + request.toString());
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitBrookfieldViscosityTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "布鲁克菲尔德旋转黏度实验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY);

                                // 在主线程中显示成功消息并关闭页面
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "布鲁克菲尔德旋转黏度实验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                String errorMsg = (response.body() != null) ? response.body().getMessage() : "未知错误";
                                Log.e("SaveData", "提交布鲁克菲尔德旋转黏度实验数据到服务器失败: " + errorMsg);
                                Log.e("SaveData", "HTTP状态码: " + response.code());
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "提交布鲁克菲尔德旋转黏度实验数据到服务器失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交布鲁克菲尔德旋转黏度实验数据到服务器失败", t);
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    String.format("提交布鲁克菲尔德旋转黏度实验数据到服务器失败：%s", t.getMessage()), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Log.e("SaveData", "asphaltTaskService is null");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "无法连接到服务器，请检查网络连接", Toast.LENGTH_SHORT).show();
                    });
                }
                
                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);
            } catch (Exception e) {
                Log.e("SaveData", "保存布鲁克菲尔德旋转黏度实验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this, 
                        String.format("保存布鲁克菲尔德旋转黏度实验数据时出错：%s", e.getMessage()), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }

    // 标准化实验类型名称
    private String standardizeExperimentType(String experimentType) {
        // 去除空格和转换为小写，便于匹配
        String normalizedType = experimentType.trim().toLowerCase();

        // 根据名称匹配实验类型常量
        if (normalizedType.contains("密度") || normalizedType.contains("相对密度")) {
            return AsphaltExperimentData.TYPE_DENSITY;
        } else if (normalizedType.contains("针入度")) {
            return AsphaltExperimentData.TYPE_PENETRATION;
        } else if (normalizedType.contains("延度") && !normalizedType.contains("力延度")) {
            return AsphaltExperimentData.TYPE_DUCTILITY;
        } else if (normalizedType.contains("软化点")) {
            return AsphaltExperimentData.TYPE_SOFTENING_POINT;
        } else if (normalizedType.contains("薄膜烘箱") && !normalizedType.contains("旋转")) {
            return AsphaltExperimentData.TYPE_TFOT;
        } else if (normalizedType.contains("旋转薄膜烘箱") || normalizedType.contains("rtfot")) {
            return AsphaltExperimentData.TYPE_RTFOT;
        } else if (normalizedType.contains("闪点") || normalizedType.contains("燃点")) {
            return AsphaltExperimentData.TYPE_FLASH_POINT;
        } else if (normalizedType.contains("标准粘度") ||
                  (normalizedType.contains("粘度") && !normalizedType.contains("布鲁克") && !normalizedType.contains("旋转粘度"))) {
            return AsphaltExperimentData.TYPE_VISCOSITY;
        } else if (normalizedType.contains("弯曲梁") || normalizedType.contains("bbr")) {
            return AsphaltExperimentData.TYPE_BBR;
        } else if (normalizedType.contains("动态剪切") || normalizedType.contains("dsr")) {
            return AsphaltExperimentData.TYPE_DSR;
        } else if (normalizedType.contains("直接拉伸") || normalizedType.contains("dtt")) {
            return AsphaltExperimentData.TYPE_DTT;
        } else if (normalizedType.contains("压力老化") || normalizedType.contains("pav")) {
            return AsphaltExperimentData.TYPE_PAV;
        } else if (normalizedType.contains("多重应力") || normalizedType.contains("mscr")) {
            return AsphaltExperimentData.TYPE_MSCR;
        } else if (normalizedType.contains("力延度")) {
            return AsphaltExperimentData.TYPE_FORCE_DUCTILITY;
        } else if (normalizedType.contains("布鲁克菲尔德") || normalizedType.contains("旋转粘度") ||
                  normalizedType.contains("brookfield")) {
            return AsphaltExperimentData.TYPE_BROOKFIELD_VISCOSITY;
        }

        // 如果无法匹配，返回原始类型，让适配器处理
        Log.w("SetupAsphalt", "无法标准化实验类型: " + experimentType);
        return experimentType;
    }

    /**
     * 保存沥青弯曲蠕变劲度试验（弯曲梁流变仪法）数据
     */
    private void saveBbrExperimentData() {
        Log.d("SaveData", "开始保存BBR实验数据");
        
        // 验证数据
        if (!validateExperimentData()) {
            return;
        }
    
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
        
        // 检查是否包含BBR实验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_BBR)) {
            Log.e("SaveData", "未找到BBR实验数据，实验类型键值 = " + AsphaltExperimentData.TYPE_BBR);
            Log.e("SaveData", "可用的实验数据键值: " + experimentData.keySet());
            Toast.makeText(this, "未找到BBR实验数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> bbrData = experimentData.get(AsphaltExperimentData.TYPE_BBR);
        
        // 添加调试日志
        Log.d("SaveData", "BBR实验数据内容: " + bbrData.toString());
        
        executor.execute(() -> {
            try {
                // 构建要提交的数据对象
                BbrTestRequest request = new BbrTestRequest(
                    taskIdString,
                    sharedPrefsManager.getUserName(),
                    bbrData.getOrDefault("specimen_id", ""),
                    bbrData.getOrDefault("specimen_type", ""),
                    bbrData.getOrDefault("material_type", ""),
                    bbrData.getOrDefault("remarks", ""),
                    bbrData
                );
                
                // 设置设备信息
                if (bbrData.containsKey("device_id")) {
                    // 设备信息已经包含在experimentValues中，无需额外处理
                }
                
                // 发送到服务器
                if (asphaltTaskService != null) {
                    Log.d("SaveData", "开始提交BBR实验数据到服务器 - 请求内容: " + request.toString());
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitBbrTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "BBR实验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_BBR);

                                // 在主线程中显示成功消息并关闭页面
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "BBR实验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                String errorMsg = (response.body() != null) ? response.body().getMessage() : "未知错误";
                                Log.e("SaveData", "提交BBR实验数据到服务器失败: " + errorMsg);
                                Log.e("SaveData", "HTTP状态码: " + response.code());
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "提交BBR实验数据到服务器失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交BBR实验数据到服务器失败", t);
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    String.format("提交BBR实验数据到服务器失败：%s", t.getMessage()), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Log.e("SaveData", "asphaltTaskService is null");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "无法连接到服务器，请检查网络连接", Toast.LENGTH_SHORT).show();
                    });
                }
                
                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);
            } catch (Exception e) {
                Log.e("SaveData", "保存BBR实验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this, 
                        String.format("保存BBR实验数据时出错：%s", e.getMessage()), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 保存动态剪切流变仪实验数据
     */
    private void saveDynamicShearRheometerExperimentData() {
        Log.d("SaveData", "开始保存动态剪切流变仪实验数据");
        
        // 验证数据
        if (!validateExperimentData()) {
            return;
        }
    
        Map<String, Map<String, String>> experimentData = asphaltAdapter.getExperimentData();
        
        // 检查是否包含动态剪切流变仪实验数据
        if (!experimentData.containsKey(AsphaltExperimentData.TYPE_DSR)) {
            Log.e("SaveData", "未找到动态剪切流变仪实验数据，实验类型键值 = " + AsphaltExperimentData.TYPE_DSR);
            Log.e("SaveData", "可用的实验数据键值: " + experimentData.keySet());
            Toast.makeText(this, "未找到动态剪切流变仪实验数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> dsrData = experimentData.get(AsphaltExperimentData.TYPE_DSR);
        
        // 添加调试日志
        Log.d("SaveData", "动态剪切流变仪实验数据内容: " + dsrData.toString());
        
        // 重新组织温度点数据，确保符合后端期望的格式
        Map<String, String> formattedData = new HashMap<>();
        
        // 先处理基本参数
        for (Map.Entry<String, String> entry : dsrData.entrySet()) {
            String key = entry.getKey();
            // 将基本参数直接复制到新的map
            if (!key.contains("temperature_point_")) {
                formattedData.put(key, entry.getValue());
            }
        }
        
        // 处理温度点数据
        int pointIndex = 1; // 温度点索引从1开始
        Pattern pattern = Pattern.compile("temperature_point_(\\d+)_(.+)");
        
        // 先找出所有温度点
        Set<Integer> pointNumbers = new HashSet<>();
        for (String key : dsrData.keySet()) {
            if (key.startsWith("temperature_point_")) {
                Matcher matcher = pattern.matcher(key);
                if (matcher.matches()) {
                    pointNumbers.add(Integer.parseInt(matcher.group(1)));
                }
            }
        }
        
        // 对每个温度点，提取参数并按照新格式重新命名
        for (Integer pointNum : pointNumbers) {
            // 提取该温度点的所有参数
            for (Map.Entry<String, String> entry : dsrData.entrySet()) {
                String key = entry.getKey();
                Matcher matcher = pattern.matcher(key);
                
                if (matcher.matches() && Integer.parseInt(matcher.group(1)) == pointNum) {
                    String paramName = matcher.group(2);
                    String value = entry.getValue();
                    
                    // 使用新格式为参数命名
                    if ("temperature".equals(paramName)) {
                        formattedData.put("temperature_" + pointIndex, value);
                    } else if ("frequency".equals(paramName)) {
                        formattedData.put("frequency_" + pointIndex, value);
                    } else if ("max_shear_stress".equals(paramName)) {
                        formattedData.put("max_shear_stress_" + pointIndex, value);
                    } else if ("max_shear_strain".equals(paramName)) {
                        formattedData.put("max_shear_strain_" + pointIndex, value);
                    } else if ("phase_angle".equals(paramName)) {
                        formattedData.put("phase_angle_" + pointIndex, value);
                    } else if ("complex_shear_modulus".equals(paramName)) {
                        formattedData.put("complex_shear_modulus_" + pointIndex, value);
                    }
                }
            }
            pointIndex++; // 移到下一个索引
        }
        
        // 添加调试日志，查看重新格式化后的数据
        Log.d("SaveData", "格式化后的动态剪切流变仪数据: " + formattedData.toString());
        
        executor.execute(() -> {
            try {
                // 解析基本参数
                Double testRadius = null;
                if (formattedData.containsKey("test_radius") && !formattedData.get("test_radius").isEmpty()) {
                    testRadius = Double.parseDouble(formattedData.get("test_radius"));
                }
                
                Double plateGap = null;
                if (formattedData.containsKey("plate_gap") && !formattedData.get("plate_gap").isEmpty()) {
                    plateGap = Double.parseDouble(formattedData.get("plate_gap"));
                }
                
                // 构建要提交的数据对象
                DynamicShearRheometerTestRequest request = new DynamicShearRheometerTestRequest(
                    taskIdString,
                    sharedPrefsManager.getUserName(),
                    formattedData.getOrDefault("specimen_id", ""),
                    formattedData.getOrDefault("specimen_type", ""),
                    formattedData.getOrDefault("material_type", ""),
                    formattedData.getOrDefault("control_mode", ""),
                    testRadius,
                    plateGap,
                    formattedData.getOrDefault("remarks", ""),
                    formattedData  // 使用格式化后的数据
                );
                
                // 发送到服务器
                if (asphaltTaskService != null) {
                    Log.d("SaveData", "开始提交动态剪切流变仪实验数据到服务器 - 请求内容: " + request.toString());
                    Call<ApiResponse<Boolean>> call = asphaltTaskService.submitDynamicShearRheometerTest(request);
                    call.enqueue(new Callback<ApiResponse<Boolean>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d("SaveData", "动态剪切流变仪实验数据已成功提交到服务器");
                                updateExperimentStatusToFinished(AsphaltExperimentData.TYPE_BBR);

                                // 在主线程中显示成功消息并关闭页面
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "动态剪切流变仪实验数据保存成功，任务已完成", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                });
                            } else {
                                String errorMsg = (response.body() != null) ? response.body().getMessage() : "未知错误";
                                Log.e("SaveData", "提交动态剪切流变仪实验数据到服务器失败: " + errorMsg);
                                Log.e("SaveData", "HTTP状态码: " + response.code());
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(RecordExperimentDataActivity.this, 
                                        "提交动态剪切流变仪实验数据到服务器失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                            Log.e("SaveData", "提交动态剪切流变仪实验数据到服务器失败", t);
                            runOnUiThread(() -> {
                                Toast.makeText(RecordExperimentDataActivity.this, 
                                    String.format("提交动态剪切流变仪实验数据到服务器失败：%s", t.getMessage()), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    Log.e("SaveData", "asphaltTaskService is null");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExperimentDataActivity.this, 
                            "无法连接到服务器，请检查网络连接", Toast.LENGTH_SHORT).show();
                    });
                }
                
                // 发送广播通知更新任务列表
                Intent refreshIntent = new Intent("com.example.labdata_main.REFRESH_TASKS");
                sendBroadcast(refreshIntent);
            } catch (Exception e) {
                Log.e("SaveData", "保存动态剪切流变仪实验数据时出错", e);
                runOnUiThread(() -> {
                    Toast.makeText(RecordExperimentDataActivity.this, 
                        String.format("保存动态剪切流变仪实验数据时出错：%s", e.getMessage()), 
                        Toast.LENGTH_SHORT).show();
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

    private void showLoading(boolean show) {
        // 这里可以添加加载指示器的显示逻辑
        // 简单起见，我们暂时不添加UI元素，只打印日志
        Log.d("SetupAsphalt", "Loading indicator: " + (show ? "showing" : "hidden"));
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 更新实验状态为已完成
     * @param experimentTypeParam 特定的实验类型，如果为null则更新整个任务状态
     */
    private void updateExperimentStatusToFinished(String experimentTypeParam) {
        if (taskIdString == null || taskIdString.isEmpty()) {
            Log.e("UpdateStatus", "任务ID为空，无法更新状态");
            return;
        }
    
        showLoading(true);
        Log.d("UpdateStatus", "正在更新实验状态为已完成，任务ID: " + taskIdString + ", 实验类型: " 
              + (experimentTypeParam != null ? experimentTypeParam : "全部"));
    
        // 使用API更新实验状态
        if (experimentTypeParam != null) {
            // 更新特定实验类型的状态
            asphaltTaskService.updateExperimentTypeStatusToFinished(taskIdString, experimentTypeParam)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.d("UpdateStatus", "成功更新" + experimentTypeParam + "实验状态为已完成");
                        } else {
                            Log.e("UpdateStatus", "更新" + experimentTypeParam + "实验状态失败: " 
                                  + (response.body() != null ? response.body().getMessage() : "未知错误"));
                        }
                    }
    
                    @Override
                    public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                        showLoading(false);
                        Log.e("UpdateStatus", "更新" + experimentTypeParam + "实验状态请求失败", t);
                    }
                });
        } else {
            // 更新整个任务的状态
            asphaltTaskService.updateExperimentStatus(taskIdString, null)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Log.d("UpdateStatus", "成功更新任务状态为已完成");
                        } else {
                            Log.e("UpdateStatus", "更新任务状态失败: " 
                                  + (response.body() != null ? response.body().getMessage() : "未知错误"));
                        }
                    }
    
                    @Override
                    public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                        showLoading(false);
                        Log.e("UpdateStatus", "更新任务状态请求失败", t);
                    }
                });
        }
    }
    
    // 为了兼容现有代码，保留原来的无参方法
    private void updateExperimentStatusToFinished() {
        updateExperimentStatusToFinished(null);
    }

    // 在RecordExperimentDataActivity.java中添加一个新方法来获取实验状态
    private void fetchExperimentStatus() {
        if (taskIdString == null || taskIdString.isEmpty()) {
            Log.e("FetchStatus", "任务ID为空，无法获取实验状态");
            return;
        }
        
        showLoading(true);
        Log.d("FetchStatus", "正在获取实验状态，任务ID: " + taskIdString);
        
        // 使用API获取实验状态信息
        asphaltTaskService.getExperimentStatus(taskIdString)
            .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, 
                                      Response<ApiResponse<Map<String, String>>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, String> statusMap = response.body().getData();
                        if (statusMap != null) {
                            Log.d("FetchStatus", "成功获取实验状态: " + statusMap);
                            filterExperimentTypes(statusMap);
                        } else {
                            Log.w("FetchStatus", "实验状态数据为空");
                        }
                    } else {
                        Log.e("FetchStatus", "获取实验状态失败: " + 
                              (response.body() != null ? response.body().getMessage() : "未知错误"));
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    showLoading(false);
                    Log.e("FetchStatus", "获取实验状态请求失败", t);
                }
            });
    }
    
    private void filterExperimentTypes(Map<String, String> statusMap) {
        if (asphaltAdapter == null) {
            Log.e("FilterExperiments", "适配器为空，无法过滤实验类型");
            return;
        }
        
        List<String> allExperimentTypes = asphaltAdapter.getExperimentTypes();
        List<String> filteredExperimentTypes = new ArrayList<>();
        
        // 打印完整的状态映射和实验类型以便调试
        Log.d("FilterExperiments", "状态映射: " + statusMap);
        Log.d("FilterExperiments", "所有实验类型: " + allExperimentTypes);
        
        // 创建实验类型映射（规范化后的名称 -> 原始名称）
        Map<String, String> normalizedTypeMap = new HashMap<>();
        for (String expType : allExperimentTypes) {
            String normalized = normalizeExperimentType(expType);
            normalizedTypeMap.put(normalized, expType);
            Log.d("FilterExperiments", "实验类型标准化: " + expType + " -> " + normalized);
        }
        
        // 创建状态映射的标准化版本
        Map<String, String> normalizedStatusMap = new HashMap<>();
        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            String normalized = normalizeExperimentType(entry.getKey());
            normalizedStatusMap.put(normalized, entry.getValue());
            Log.d("FilterExperiments", "状态键标准化: " + entry.getKey() + " -> " + normalized + " = " + entry.getValue());
        }
        
        // 遍历所有实验类型，只保留未完成的
        for (String expType : allExperimentTypes) {
            String normalized = normalizeExperimentType(expType);
            
            // 首先尝试直接匹配
            String status = statusMap.get(expType);
            
            // 如果直接匹配失败，尝试标准化后匹配
            if (status == null) {
                status = normalizedStatusMap.get(normalized);
                if (status != null) {
                    Log.d("FilterExperiments", "通过标准化匹配成功: " + expType + " -> " + normalized + " = " + status);
                }
            }
            
            // 如果仍然匹配失败，尝试遍历检查是否包含
            if (status == null) {
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    String normalizedKey = normalizeExperimentType(entry.getKey());
                    if (normalized.contains(normalizedKey) || normalizedKey.contains(normalized)) {
                        status = entry.getValue();
                        Log.d("FilterExperiments", "通过部分匹配成功: " + expType + " 与 " + entry.getKey() + " = " + status);
                        break;
                    }
                }
            }
            
            // 根据状态决定是否保留
            if (status == null || !status.equals("finished")) {
                filteredExperimentTypes.add(expType);
                Log.d("FilterExperiments", "保留未完成实验: " + expType + " (状态: " + (status == null ? "未知" : status) + ")");
            } else {
                Log.d("FilterExperiments", "过滤已完成实验: " + expType + " (状态: " + status + ")");
            }
        }
        
        Log.d("FilterExperiments", "过滤前: " + allExperimentTypes.size() + "项, 过滤后: " + filteredExperimentTypes.size() + "项");
        asphaltAdapter.updateExperimentTypes(filteredExperimentTypes);
    }
    
    // 添加实验类型标准化方法
    private String normalizeExperimentType(String type) {
        if (type == null) return "";
        
        // 转为小写并移除空格和标点
        return type.toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z0-9\u4e00-\u9fa5]", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面恢复时获取最新状态，确保实验完成后返回页面时能过滤掉
        if (taskIdString != null && !taskIdString.isEmpty()) {
            fetchExperimentStatus();
        }
    }
}
