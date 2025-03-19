package com.example.labdata_main;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.adapter.QRCodePagerAdapter;
import com.example.labdata_main.adapter.SpecimenInfoAdapter;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.ProjectStatus;
import com.example.labdata_main.printer.BluetoothPrinterManager;
import com.example.labdata_main.printer.PrinterDialogManager;
import com.example.labdata_main.printer.PrinterInfo;
import com.example.labdata_main.printer.PrinterManager;
import com.example.labdata_main.printer.WiFiPrinterManager;
import com.example.labdata_main.util.GsonUtil;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.api.model.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateSpecimenCodeStep2Activity extends AppCompatActivity {
    private final String TAG = "GenerateSpecimenCodeStep2";
    private static final int QR_CODE_SIZE = 500;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private RecyclerView rvSpecimenInfos;
    private ViewPager2 viewPagerQRCodes;
    private TextView tvCurrentPage;
    private TextView tvTotalPages;
    private TextView tvQRCodeContent;
    private MaterialButton btnGenerateQRCode;
    private MaterialButton btnComplete;
    private SpecimenInfoAdapter specimenInfoAdapter;
    private QRCodePagerAdapter qrCodePagerAdapter;
    
    // 使用transient关键字而不是注解
    private transient BluetoothPrinterManager bluetoothPrinterManager;
    private transient WiFiPrinterManager wifiPrinterManager;
    private transient PrinterDialogManager printerDialogManager;
    private transient PrinterManager currentPrinterManager;
    private transient PrinterInfo currentPrinter;
    
    // 使用transient关键字而不是注解
    private transient ProjectStatus projectStatus;
    
    // 使用transient关键字而不是注解
    private transient MixtureTaskService mixtureTaskService;

    private List<MoldingMethod> moldingMethods;
    private List<MixRatio> mixRatios;
    private List<DeviceInfo> mixingDevices;
    private List<DeviceInfo> formingDevices;

    // 使用transient关键字而不是注解
    private transient final ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPermissionsAndPrint();
            } else {
                Toast.makeText(this, "需要蓝牙和位置权限才能连接打印机", Toast.LENGTH_LONG).show();
            }
        });

    // 添加第二个权限请求启动器，用于单独的蓝牙连接权限请求
    private transient final ActivityResultLauncher<String[]> requestMultiplePermissions =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // 继续执行需要权限的操作
                checkPermissionsAndPrint();
            } else {
                Toast.makeText(this, "需要蓝牙和位置权限才能连接打印机", Toast.LENGTH_LONG).show();
            }
        });

    // 使用transient关键字而不是注解
    private transient ExecutorService executor;
    
    // 使用transient关键字而不是注解
    private transient AppDatabase database;
    private String taskId;
    private List<String> selectedMixingDevices;
    private List<String> selectedFormingDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_specimen_code_step2);
        executor = Executors.newFixedThreadPool(1);

        // 获取Intent中的数据
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        moldingMethods = GsonUtil.fromJson(intent.getStringExtra("moldingMethods"), new TypeToken<List<MoldingMethod>>() {}.getType());
        mixRatios = GsonUtil.fromJson(intent.getStringExtra("mixRatios"), new TypeToken<List<MixRatio>>() {}.getType());
        mixingDevices = GsonUtil.fromJson(intent.getStringExtra("mixingDevices"), new TypeToken<List<DeviceInfo>>() {}.getType());
        formingDevices = GsonUtil.fromJson(intent.getStringExtra("formingDevices"), new TypeToken<List<DeviceInfo>>() {}.getType());

        selectedFormingDevices = new ArrayList<>();

        // 初始化打印管理器
        bluetoothPrinterManager = new BluetoothPrinterManager(this);
        wifiPrinterManager = new WiFiPrinterManager(this);
        printerDialogManager = new PrinterDialogManager(this);

        // 初始化项目状态
        projectStatus = new ProjectStatus();

        // 初始化API服务
        mixtureTaskService = ServiceCreator.create(MixtureTaskService.class);

        // 初始化数据库
        database = AppDatabase.getInstance(this);

        // 初始化变量
        selectedMixingDevices = new ArrayList<>();

        // 初始化数据列表
        moldingMethods = new ArrayList<>();
        mixRatios = new ArrayList<>();
        mixingDevices = new ArrayList<>();
        formingDevices = new ArrayList<>();

        // 初始化视图
        initializeViews();
        setupListeners();
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置ViewPager
        setupViewPager();
        
        // 加载任务数据
        loadTaskData();

        Log.d(TAG, "Activity created with taskId: " + taskId);
    }

    private void initializeViews() {
        rvSpecimenInfos = findViewById(R.id.rvSpecimenInfos);
        viewPagerQRCodes = findViewById(R.id.viewPagerQRCodes);
        tvCurrentPage = findViewById(R.id.tvCurrentPage);
        tvTotalPages = findViewById(R.id.tvTotalPages);
        tvQRCodeContent = findViewById(R.id.tvQRCodeContent);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnComplete = findViewById(R.id.btnComplete);
    }
    
    private void setupListeners() {
        btnGenerateQRCode.setText("打印试件码");
        btnGenerateQRCode.setOnClickListener(v -> checkPermissionsAndPrint());

        btnComplete.setOnClickListener(v -> completeSpecimenGeneration());
    }

    /**
     * 检查权限并打印
     */
    private void checkPermissionsAndPrint() {
        // 检查Android 12 (API 31)及以上版本所需的BLUETOOTH_CONNECT权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                
                Log.d(TAG, "权限未授予，正在请求权限...");
                
                // 请求蓝牙和位置权限
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT, 
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    },
                    100 // 请求码
                );
                return;
            }
        } else {
            // 较低版本Android需要的权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                
                Log.d(TAG, "权限未授予，正在请求权限...");
                
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    100 // 请求码
                );
                return;
            }
        }

        // 在这里使用自动化的打印机选择流程
        printerDialogManager.showPrinterSelectionDialog(
            bluetoothPrinterManager, 
            wifiPrinterManager,
            new PrinterDialogManager.PrinterSelectionCallback() {
                @Override
                public void onPrinterSelected(PrinterInfo printer) {
                    // 根据打印机类型选择管理器
                    if (printer.getType() == PrinterInfo.TYPE_BLUETOOTH) {
                        currentPrinterManager = bluetoothPrinterManager;
                    } else if (printer.getType() == PrinterInfo.TYPE_WIFI) {
                        currentPrinterManager = wifiPrinterManager;
                    }
                    
                    if (currentPrinterManager != null) {
                        // 连接打印机后打印二维码
                        currentPrinterManager.connectToPrinter(printer, new PrinterManager.PrintCallback() {
                            @Override
                            public void onPrintStart() {
                                runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                        "正在打印...", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onPrintSuccess() {
                                runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                        "打印成功", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onPrintError(String error) {
                                runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                        "打印失败: " + error, Toast.LENGTH_LONG).show());
                            }
                        });
                        
                        // 准备二维码并打印
                        printCurrentQRCode();
                    }
                }

                @Override
                public void onCancelled() {
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "取消打印", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    /**
     * 打印当前显示的二维码
     */
    private void printCurrentQRCode() {
        if (currentPrinterManager == null) {
            Toast.makeText(this, "请先选择打印机", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int currentPosition = viewPagerQRCodes.getCurrentItem();
        if (currentPosition < qrCodePagerAdapter.getItemCount()) {
            Bitmap qrCode = qrCodePagerAdapter.getQRCode(currentPosition);
            if (qrCode != null) {
                currentPrinterManager.printQRCode(qrCode, new PrinterManager.PrintCallback() {
                    @Override
                    public void onPrintStart() {
                        runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                "正在打印...", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onPrintSuccess() {
                        runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                "打印成功", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onPrintError(String error) {
                        runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                "打印失败: " + error, Toast.LENGTH_LONG).show());
                    }
                });
            } else {
                Toast.makeText(this, "获取二维码图像失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        specimenInfoAdapter = new SpecimenInfoAdapter();
        specimenInfoAdapter.setTaskId(taskId); // 设置当前任务ID
        specimenInfoAdapter.setOnFinishSpecimenClickListener(new SpecimenInfoAdapter.OnFinishSpecimenClickListener() {
            @Override
            public void onFinishSpecimenClicked(String taskId) {
                // 调用完成制件的方法
                completeSpecimenGeneration();
            }
        });
        rvSpecimenInfos.setAdapter(specimenInfoAdapter);
        rvSpecimenInfos.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewPager() {
        Log.d(TAG, "Setting up ViewPager2");
        qrCodePagerAdapter = new QRCodePagerAdapter();
        viewPagerQRCodes.setAdapter(qrCodePagerAdapter);
        
        viewPagerQRCodes.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "Page selected: " + position);
                updatePageIndicator(position);
            }
        });
    }

    private void updatePageIndicator(int position) {
        tvCurrentPage.setText(String.valueOf(position + 1));
        tvTotalPages.setText(String.valueOf(moldingMethods.size()));
        
        // 更新当前显示的制件方法信息
        MoldingMethod method = moldingMethods.get(position);
        String methodInfo = String.format("制件方法 %d: %s", 
            position + 1, 
            method.getCompactionMethod());
        tvQRCodeContent.setText(methodInfo);
    }

    private void updateUI() {
        // 添加所有制件信息到适配器
        if (moldingMethods != null && mixRatios != null && 
            mixingDevices != null && formingDevices != null) {
            for (int i = 0; i < moldingMethods.size(); i++) {
                MoldingMethod method = moldingMethods.get(i);
                MixRatio ratio = mixRatios.get(i % mixRatios.size());
                DeviceInfo mixingDevice = mixingDevices.get(i);
                DeviceInfo formingDevice = formingDevices.get(i);
                
                specimenInfoAdapter.addSpecimenInfo(method, ratio, mixingDevice, formingDevice);
            }
            
            // 初始化时就生成二维码
            generateQRCodes();
        }
    }

    private void generateQRCodes() {
        try {
            Log.d(TAG, "Starting QR code generation");
            qrCodePagerAdapter.clearQRCodes();
            
            for (int i = 0; i < moldingMethods.size(); i++) {
                Log.d(TAG, "Generating QR code for method " + (i + 1));
                
                // 获取当前方法对象
                MoldingMethod method = moldingMethods.get(i);
                String compactionMethod = method.getCompactionMethod();
                if (compactionMethod == null || compactionMethod.trim().isEmpty() || 
                    compactionMethod.equals("????")) {
                    compactionMethod = "标准压实";
                    Log.d(TAG, "为二维码设置默认压实方法: 标准压实");
                } else {
                    Log.d(TAG, "二维码使用压实方法: " + compactionMethod);
                }
                
                // 获取配比对象
                MixRatio ratio = mixRatios.get(i % mixRatios.size());
                String projectId = ratio.getProjectId();
                String mixName = ratio.getName();
                if (projectId == null || projectId.trim().isEmpty()) {
                    projectId = "默认项目";
                    Log.d(TAG, "为二维码设置默认项目ID: 默认项目");
                } else {
                    Log.d(TAG, "二维码使用项目ID: " + projectId);
                }
                
                if (mixName == null || mixName.trim().isEmpty() || "???".equals(mixName)) {
                    mixName = "标准配比";
                    Log.d(TAG, "为二维码设置默认配比名称: 标准配比");
                }
                
                // 获取设备对象
                DeviceInfo mixingDevice = mixingDevices.get(i);
                DeviceInfo formingDevice = formingDevices.get(i);
                
                String mixingManufacturer = mixingDevice.getManufacturer();
                String formingManufacturer = formingDevice.getManufacturer();
                
                // 确保设备制造商不为空
                if (mixingManufacturer == null || mixingManufacturer.trim().isEmpty() || 
                    "?????".equals(mixingManufacturer)) {
                    mixingManufacturer = "标准制造商";
                    Log.d(TAG, "为二维码设置默认拌合设备制造商: 标准制造商");
                }
                
                if (formingManufacturer == null || formingManufacturer.trim().isEmpty() || 
                    "?????".equals(formingManufacturer)) {
                    formingManufacturer = "标准制造商";
                    Log.d(TAG, "为二维码设置默认成型设备制造商: 标准制造商");
                }
                
                // 创建干净的JSON对象，完全避免序列化问题
                JsonObject manualJson = new JsonObject();
                
                // 添加taskId字段
                if (taskId != null && !taskId.isEmpty()) {
                    manualJson.addProperty("taskId", taskId);
                    Log.d(TAG, "二维码添加taskId: " + taskId);
                } else {
                    Log.d(TAG, "taskId为空，QR码中不包含taskId");
                }
                
                // 仅添加必要的moldingMethod字段
                JsonObject moldingMethodJson = new JsonObject();
                moldingMethodJson.addProperty("compactionMethod", compactionMethod);
                moldingMethodJson.addProperty("mixingTemperature", method.getMixingTemperature());
                moldingMethodJson.addProperty("mixingSpeed", method.getMixingSpeed());
                moldingMethodJson.addProperty("mixingTime", method.getMixingTime());
                manualJson.add("moldingMethod", moldingMethodJson);
                
                // 仅添加必要的mixRatio字段
                JsonObject mixRatioJson = new JsonObject();
                mixRatioJson.addProperty("name", mixName);
                mixRatioJson.addProperty("projectId", projectId);
                manualJson.add("mixRatio", mixRatioJson);
                
                // 仅添加必要的mixingDevice字段
                JsonObject mixingDeviceJson = new JsonObject();
                mixingDeviceJson.addProperty("manufacturer", mixingManufacturer);
                mixingDeviceJson.addProperty("model", mixingDevice.getModel());
                mixingDeviceJson.addProperty("deviceId", mixingDevice.getDeviceId());
                mixingDeviceJson.addProperty("type", mixingDevice.getType());
                manualJson.add("mixingDevice", mixingDeviceJson);
                
                // 仅添加必要的formingDevice字段
                JsonObject formingDeviceJson = new JsonObject();
                formingDeviceJson.addProperty("manufacturer", formingManufacturer);
                formingDeviceJson.addProperty("model", formingDevice.getModel());
                formingDeviceJson.addProperty("deviceId", formingDevice.getDeviceId());
                formingDeviceJson.addProperty("type", formingDevice.getType());
                manualJson.add("formingDevice", formingDeviceJson);
                
                // 使用手动构建的JSON
                String jsonContent = manualJson.toString();
                Log.d(TAG, "手动构建的最终JSON (用于二维码): " + jsonContent);

                // 生成二维码
                MultiFormatWriter writer = new MultiFormatWriter();
                BitMatrix bitMatrix = writer.encode(jsonContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    }
                }

                Log.d(TAG, "QR code bitmap generated with size: " + width + "x" + height);
                
                // 添加到适配器 - 确保适配器没有再次修改数据
                qrCodePagerAdapter.addQRCode(bitmap);
                Log.d(TAG, "Added QR code to adapter, total count: " + qrCodePagerAdapter.getItemCount());
            }

            // 更新页面指示器
            updatePageIndicator(0);
            Log.d(TAG, "QR code generation completed successfully");
            
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR codes", e);
            Toast.makeText(this, "生成二维码失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeSpecimenGeneration() {
        // 显示进度对话框
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("正在完成制件")
            .setMessage("正在更新任务状态...")
            .setCancelable(false)
            .create();
        progressDialog.show();
        
        // 获取任务ID前缀（去掉最后的后缀数字）
        String taskIdPrefix = extractTaskIdPrefix(taskId);
        Log.d(TAG, "提取的任务ID前缀: " + taskIdPrefix);
        
        // 调用API更新making_status为finished
        mixtureTaskService.updateMakingStatusToFinished(taskIdPrefix)
            .enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                    progressDialog.dismiss();
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // 更新成功
                        Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                "制件完成，任务状态已更新", Toast.LENGTH_LONG).show();
                        
                        // 保存到本地数据库
                        saveToLocalDatabase();
                        
                        // 返回主界面
                        returnToMainActivity();
                    } else {
                        // 更新失败
                        String errorMsg = response.body() != null ? response.body().getMessage() : "服务器响应错误";
                        Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                "更新任务状态失败: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "API响应失败: " + errorMsg);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    progressDialog.dismiss();
                    
                    // 网络错误时提示用户并尝试仅更新本地数据库
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                            "网络连接错误，将仅更新本地数据", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API调用失败", t);
                    
                    // 尝试仅保存本地数据
                    saveToLocalDatabase();
                    returnToMainActivity();
                }
            });
    }
    
    /**
     * 保存数据到本地数据库
     */
    private void saveToLocalDatabase() {
        // 保存设备信息到数据库
        executor.execute(() -> {
            ExperimentTask task = database.experimentTaskDao().getTaskByTaskId(taskId);
            if (task != null) {
                task.setSelectedMixingDevices(selectedMixingDevices);
                task.setSelectedFormingDevices(selectedFormingDevices);
                task.setSpecimenGenerationTime(System.currentTimeMillis());
                database.experimentTaskDao().update(task);
                Log.d(TAG, "已更新本地数据库");
            } else {
                Log.e(TAG, "本地数据库中未找到任务ID: " + taskId);
            }
        });
    }
    
    /**
     * 从完整的taskId中提取前缀（去掉最后的数字部分）
     */
    private String extractTaskIdPrefix(String fullTaskId) {
        // 任务ID通常格式为：58c3d798-89bc-4e91-81b0-7dee2ae4fae5-0
        // 我们需要提取58c3d798-89bc-4e91-81b0-7dee2ae4fae5部分
        
        // 查找最后一个破折号的位置
        int lastDashIndex = fullTaskId.lastIndexOf("-");
        if (lastDashIndex > 0) {
            String prefix = fullTaskId.substring(0, lastDashIndex);
            Log.d(TAG, "从 " + fullTaskId + " 提取前缀: " + prefix);
            return prefix;
        }
        // 如果没有破折号，则返回整个ID
        return fullTaskId;
    }
    
    /**
     * 返回主界面并刷新
     */
    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("refreshData", true); // 添加刷新标记
        startActivity(intent);
        finish();
    }

    private void loadTaskData() {
        // 从数据库获取本地任务数据（兼容离线模式）
        executor.execute(() -> {
            ExperimentTask task = database.experimentTaskDao().getTaskByTaskId(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    // 加载已保存的设备信息
                });
            }
        });
        
        // 从后端获取完整的试件数据
        loadSpecimenDataFromBackend();
    }
    
    /**
     * 从后端加载试件制备所需的数据
     */
    private void loadSpecimenDataFromBackend() {
        // 检查taskId是否有效
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "Invalid taskId: " + taskId);
            Toast.makeText(this, "无效的任务ID，无法获取试件数据", Toast.LENGTH_SHORT).show();
            // 显示错误状态而非使用本地数据
            showErrorState("无效的任务ID");
            return;
        }
        
        // 显示加载指示器
        showLoading(true, "正在获取数据...");
        
        // 检查API服务是否已初始化
        if (mixtureTaskService == null) {
            Log.e(TAG, "MixtureTaskService not initialized");
            Toast.makeText(this, "API服务未初始化，无法获取试件数据", Toast.LENGTH_SHORT).show();
            showLoading(false, null);
            // 显示错误状态而非回退到使用Intent传入的数据
            showErrorState("API服务初始化失败");
            return;
        }
        
        mixtureTaskService.getSpecimenData(taskId).enqueue(new Callback<ApiResponse<Map<String, Object>>>(){
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                // 隐藏加载指示器
                showLoading(false, null);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> specimenData = response.body().getData();
                    
                    // 处理制件方法和配比信息
                    if (specimenData.containsKey("methodsAndRatios")) {
                        try {
                            List<Map<String, Object>> methodsAndRatios = (List<Map<String, Object>>) specimenData.get("methodsAndRatios");
                            if (methodsAndRatios != null) {
                                processMethodsAndRatios(methodsAndRatios);
                            }
                        } catch (ClassCastException e) {
                            Log.e(TAG, "Error processing methodsAndRatios: " + e.getMessage());
                        }
                    }
                    
                    // 处理混合设备信息
                    if (specimenData.containsKey("mixingEquipment")) {
                        try {
                            List<Map<String, Object>> mixingEquipmentData = (List<Map<String, Object>>) specimenData.get("mixingEquipment");
                            if (mixingEquipmentData != null) {
                                processMixingEquipment(mixingEquipmentData);
                            }
                        } catch (ClassCastException e) {
                            Log.e(TAG, "Error processing mixingEquipment: " + e.getMessage());
                        }
                    }
                    
                    // 处理成型设备信息
                    if (specimenData.containsKey("formingEquipment")) {
                        try {
                            List<Map<String, Object>> formingEquipmentData = (List<Map<String, Object>>) specimenData.get("formingEquipment");
                            if (formingEquipmentData != null) {
                                processFormingEquipment(formingEquipmentData);
                            }
                        } catch (ClassCastException e) {
                            Log.e(TAG, "Error processing formingEquipment: " + e.getMessage());
                        }
                    }
                    
                    // 检查数据是否为空
                    if (moldingMethods.isEmpty() || mixRatios.isEmpty() || 
                        mixingDevices.isEmpty() || formingDevices.isEmpty()) {
                        showErrorState("无法获取完整试件数据");
                        return;
                    }
                    
                    // 更新UI显示
                    updateUI();
                } else {
                    // 显示错误信息
                    String errorMsg = response.body() != null ? response.body().getMessage() : "获取数据失败";
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    
                    // 显示错误状态而非回退到使用Intent传入的数据
                    String statusCode = response.code() + " " + response.message();
                    showErrorState("API响应错误: " + statusCode);
                    
                    Log.d(TAG, "API call failed, status code: " + statusCode);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // 隐藏加载指示器
                showLoading(false, null);
                
                // 显示错误信息
                String errorMsg = "网络请求失败: " + t.getMessage();
                Toast.makeText(GenerateSpecimenCodeStep2Activity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API call error", t);
                
                // 显示错误状态而非回退到使用Intent传入的数据
                showErrorState("网络连接失败");
            }
        });
    }
    
    /**
     * 处理从API获取的制件方法和配比信息
     */
    private void processMethodsAndRatios(List<Map<String, Object>> methodsAndRatios) {
        moldingMethods = new ArrayList<>();
        mixRatios = new ArrayList<>();
        
        for (Map<String, Object> item : methodsAndRatios) {
            // 打印调试信息
            Log.d(TAG, "处理方法和配比项: " + item.toString());
            
            // 创建制件方法对象
            MoldingMethod method = new MoldingMethod();
            if (item.containsKey("mixing_temperature")) {
                Double temp = item.get("mixing_temperature") instanceof Number ? 
                    ((Number) item.get("mixing_temperature")).doubleValue() : 0.0;
                method.setMixingTemperature(temp.floatValue());
                Log.d(TAG, "设置拌合温度: " + temp);
            } else {
                Log.d(TAG, "missing mixing_temperature");
            }
            
            if (item.containsKey("mixing_speed")) {
                Double speed = item.get("mixing_speed") instanceof Number ? 
                    ((Number) item.get("mixing_speed")).doubleValue() : 0.0;
                method.setMixingSpeed(speed.floatValue());
                Log.d(TAG, "设置拌合速度: " + speed);
            } else {
                Log.d(TAG, "missing mixing_speed");
            }
            
            if (item.containsKey("mixing_time")) {
                Double time = item.get("mixing_time") instanceof Number ? 
                    ((Number) item.get("mixing_time")).doubleValue() : 0.0;
                method.setMixingTime(time.floatValue());
                Log.d(TAG, "设置拌合时间: " + time);
            } else {
                Log.d(TAG, "missing mixing_time");
            }
            
            if (item.containsKey("compaction_method")) {
                String compMethod = (String) item.get("compaction_method");
                // 确保压实方法不为空
                method.setCompactionMethod(compMethod != null && !compMethod.trim().isEmpty() ? compMethod : "标准压实");
                Log.d(TAG, "设置压实方法: " + compMethod);
            } else {
                // 提供默认值，不再显示为"????"
                method.setCompactionMethod("标准压实");
                Log.d(TAG, "missing compaction_method, 使用默认值: 标准压实");
            }
            
            // 设置ID
            if (item.containsKey("id")) {
                Number id = (Number) item.get("id");
                method.setId(id != null ? id.longValue() : 0L);
                Log.d(TAG, "设置方法ID: " + id);
            } else {
                Log.d(TAG, "missing id");
            }
            
            moldingMethods.add(method);
            
            // 创建配比对象
            MixRatio mixRatio = new MixRatio();
            if (item.containsKey("mixName")) {
                String mixName = (String) item.get("mixName");
                // 确保配比名称不为空
                mixRatio.setName(mixName != null && !mixName.trim().isEmpty() ? mixName : "标准配比");
                Log.d(TAG, "设置配比名称: " + mixName);
            } else if (item.containsKey("mix_name")) {
                // 尝试使用下划线命名风格
                String mixName = (String) item.get("mix_name");
                mixRatio.setName(mixName != null && !mixName.trim().isEmpty() ? mixName : "标准配比");
                Log.d(TAG, "设置配比名称(下划线风格): " + mixName);
            } else {
                // 提供默认值，不再显示为"???"
                mixRatio.setName("标准配比");
                Log.d(TAG, "missing mixName/mix_name, 使用默认值: 标准配比");
            }
            
            // 设置ID和时间
            if (item.containsKey("id")) {
                Number id = (Number) item.get("id");
                mixRatio.setId(id != null ? id.longValue() : 1L);
                Log.d(TAG, "设置配比ID: " + id);
            } else {
                mixRatio.setId(1L); // 提供非零默认值
                Log.d(TAG, "missing id for mixRatio, 使用默认值: 1");
            }
            
            // 设置创建时间
            mixRatio.setCreationTime(System.currentTimeMillis());
            
            // 设置项目ID - 从API中获取
            if (item.containsKey("project_id")) {
                Object projectIdObj = item.get("project_id");
                // 处理projectId，可能是String或Number类型
                String projectId;
                if (projectIdObj instanceof Number) {
                    projectId = String.valueOf(((Number) projectIdObj).longValue());
                } else {
                    projectId = projectIdObj != null ? projectIdObj.toString() : "";
                }
                // 确保项目ID不为空
                mixRatio.setProjectId(projectId != null && !projectId.trim().isEmpty() ? projectId : "默认项目");
                Log.d(TAG, "设置项目ID: " + projectId);
            } else {
                // 如果找不到project_id，尝试找projectId
                if (item.containsKey("projectId")) {
                    Object projectIdObj = item.get("projectId");
                    String projectId = projectIdObj != null ? projectIdObj.toString() : "";
                    // 确保项目ID不为空
                    mixRatio.setProjectId(projectId != null && !projectId.trim().isEmpty() ? projectId : "默认项目");
                    Log.d(TAG, "设置项目ID(驼峰风格): " + projectId);
                } else {
                    // 提供默认值而不是空字符串
                    mixRatio.setProjectId("默认项目");
                    Log.d(TAG, "missing project_id/projectId, 使用默认值: 默认项目");
                }
            }
            
            mixRatios.add(mixRatio);
        }
        
        Log.d(TAG, "处理完成，共加载了 " + moldingMethods.size() + " 个制件方法和 " + 
              mixRatios.size() + " 个配比");
    }
    
    /**
     * 处理从API获取的混合设备信息
     */
    private void processMixingEquipment(List<Map<String, Object>> mixingEquipmentData) {
        mixingDevices = new ArrayList<>();
        
        for (Map<String, Object> item : mixingEquipmentData) {
            DeviceInfo device = new DeviceInfo();
            
            if (item.containsKey("deviceId")) {
                String deviceId = (String) item.get("deviceId");
                device.setDeviceId(deviceId);
                Log.d(TAG, "设置拌合设备ID: " + deviceId);
            } else {
                device.setDeviceId("MIX001");
                Log.d(TAG, "拌合设备ID缺失，使用默认值: MIX001");
            }
            
            if (item.containsKey("model")) {
                String model = (String) item.get("model");
                device.setModel(model != null ? model : "标准拌合设备");
                Log.d(TAG, "设置拌合设备型号: " + model);
            } else {
                device.setModel("标准拌合设备");
                Log.d(TAG, "拌合设备型号缺失，使用默认值: 标准拌合设备");
            }
            
            if (item.containsKey("manufacturer")) {
                String manufacturer = (String) item.get("manufacturer");
                device.setManufacturer(manufacturer != null ? manufacturer : "标准制造商");
                Log.d(TAG, "设置拌合设备厂家: " + manufacturer);
            } else {
                device.setManufacturer("标准制造商");
                Log.d(TAG, "拌合设备厂家缺失，使用默认值: 标准制造商");
            }
            
            if (item.containsKey("deviceType")) {
                String deviceType = (String) item.get("deviceType");
                device.setDeviceType(deviceType);
                Log.d(TAG, "设置拌合设备类型: " + deviceType);
            } else {
                device.setDeviceType("mixing");
                Log.d(TAG, "拌合设备类型缺失，使用默认值: mixing");
            }
            
            mixingDevices.add(device);
        }
        
        Log.d(TAG, "处理完成，共加载了 " + mixingDevices.size() + " 个拌合设备");
    }
    
    /**
     * 处理从API获取的成型设备信息
     */
    private void processFormingEquipment(List<Map<String, Object>> formingEquipmentData) {
        formingDevices = new ArrayList<>();
        
        for (Map<String, Object> item : formingEquipmentData) {
            DeviceInfo device = new DeviceInfo();
            
            if (item.containsKey("deviceId")) {
                String deviceId = (String) item.get("deviceId");
                device.setDeviceId(deviceId);
                Log.d(TAG, "设置压实设备ID: " + deviceId);
            } else {
                device.setDeviceId("FORM001");
                Log.d(TAG, "压实设备ID缺失，使用默认值: FORM001");
            }
            
            if (item.containsKey("model")) {
                String model = (String) item.get("model");
                device.setModel(model != null ? model : "标准压实设备");
                Log.d(TAG, "设置压实设备型号: " + model);
            } else {
                device.setModel("标准压实设备");
                Log.d(TAG, "压实设备型号缺失，使用默认值: 标准压实设备");
            }
            
            if (item.containsKey("manufacturer")) {
                String manufacturer = (String) item.get("manufacturer");
                device.setManufacturer(manufacturer != null ? manufacturer : "标准制造商");
                Log.d(TAG, "设置压实设备厂家: " + manufacturer);
            } else {
                device.setManufacturer("标准制造商");
                Log.d(TAG, "压实设备厂家缺失，使用默认值: 标准制造商");
            }
            
            if (item.containsKey("deviceType")) {
                String deviceType = (String) item.get("deviceType");
                device.setDeviceType(deviceType);
                Log.d(TAG, "设置压实设备类型: " + deviceType);
            } else {
                device.setDeviceType("forming");
                Log.d(TAG, "压实设备类型缺失，使用默认值: forming");
            }
            
            formingDevices.add(device);
        }
        
        Log.d(TAG, "处理完成，共加载了 " + formingDevices.size() + " 个压实设备");
    }
    
    /**
     * 显示或隐藏加载指示器
     */
    private void showLoading(boolean show, String message) {
        // 这里假设有一个加载指示器UI，如果没有，需要先在布局中添加
        // 例如使用ProgressBar或自定义的加载视图
        if (show) {
            // 显示加载指示器
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理Intent中传递的数据
     */
    private void handleIntentData() {
        // 获取Intent中传递的JSON数据
        String moldingMethodsJson = getIntent().getStringExtra("moldingMethod");
        String mixRatiosJson = getIntent().getStringExtra("mixRatio");
        String mixingDevicesJson = getIntent().getStringExtra("mixingDevice");
        String formingDevicesJson = getIntent().getStringExtra("formingDevice");
        
        Log.d(TAG, "Processing Intent data:");
        Log.d(TAG, "moldingMethodsJson: " + (moldingMethodsJson != null ? moldingMethodsJson : "null"));
        Log.d(TAG, "mixRatiosJson: " + (mixRatiosJson != null ? mixRatiosJson : "null"));
        
        // 解析制件方法数据
        if (moldingMethodsJson != null && !moldingMethodsJson.isEmpty()) {
            try {
                Type type = new TypeToken<List<MoldingMethod>>(){}.getType();
                List<MoldingMethod> methods = GsonUtil.getGson().fromJson(moldingMethodsJson, type);
                if (methods != null && !methods.isEmpty()) {
                    // 确保压实方法不为空
                    for (MoldingMethod method : methods) {
                        if (method.getCompactionMethod() == null || method.getCompactionMethod().equals("????")) {
                            method.setCompactionMethod("标准压实");
                        }
                    }
                    moldingMethods = methods;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing moldingMethods: " + e.getMessage());
                // 创建默认的制件方法
                MoldingMethod defaultMethod = new MoldingMethod();
                defaultMethod.setCompactionMethod("标准压实");
                defaultMethod.setMixingTemperature(25.0f);
                defaultMethod.setMixingSpeed(60.0f);
                defaultMethod.setMixingTime(180.0f);
                moldingMethods.add(defaultMethod);
            }
        } else {
            // 创建默认的制件方法
            MoldingMethod defaultMethod = new MoldingMethod();
            defaultMethod.setCompactionMethod("标准压实");
            defaultMethod.setMixingTemperature(25.0f);
            defaultMethod.setMixingSpeed(60.0f);
            defaultMethod.setMixingTime(180.0f);
            moldingMethods.add(defaultMethod);
        }
        
        // 解析配比数据
        if (mixRatiosJson != null && !mixRatiosJson.isEmpty()) {
            try {
                Type type = new TypeToken<List<MixRatio>>(){}.getType();
                List<MixRatio> ratios = GsonUtil.getGson().fromJson(mixRatiosJson, type);
                if (ratios != null && !ratios.isEmpty()) {
                    // 确保配比名称不为空
                    for (MixRatio ratio : ratios) {
                        if (ratio.getName() == null || ratio.getName().equals("???")) {
                            ratio.setName("标准配比");
                        }
                        if (ratio.getId() <= 0) {
                            ratio.setId(1L);
                        }
                        if (ratio.getCreationTime() <= 0) {
                            ratio.setCreationTime(System.currentTimeMillis());
                        }
                        if (ratio.getProjectId() == null || ratio.getProjectId().isEmpty() || "0".equals(ratio.getProjectId())) {
                            ratio.setProjectId("1");
                        }
                    }
                    mixRatios = ratios;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing mixRatios: " + e.getMessage());
                // 创建默认的配比
                MixRatio defaultRatio = new MixRatio();
                defaultRatio.setName("标准配比");
                defaultRatio.setId(1L);
                defaultRatio.setCreationTime(System.currentTimeMillis());
                defaultRatio.setProjectId("1");
                mixRatios.add(defaultRatio);
            }
        } else {
            // 创建默认的配比
            MixRatio defaultRatio = new MixRatio();
            defaultRatio.setName("标准配比");
            defaultRatio.setId(1L);
            defaultRatio.setCreationTime(System.currentTimeMillis());
            defaultRatio.setProjectId("1");
            mixRatios.add(defaultRatio);
        }
        
        // 解析混合设备数据
        if (mixingDevicesJson != null && !mixingDevicesJson.isEmpty()) {
            try {
                Type type = new TypeToken<List<DeviceInfo>>(){}.getType();
                List<DeviceInfo> devices = GsonUtil.getGson().fromJson(mixingDevicesJson, type);
                if (devices != null && !devices.isEmpty()) {
                    mixingDevices = devices;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing mixingDevices: " + e.getMessage());
                // 创建默认的混合设备
                DeviceInfo defaultDevice = new DeviceInfo();
                defaultDevice.setDeviceId("MIX001");
                defaultDevice.setModel("标准混合器");
                defaultDevice.setManufacturer("标准制造商");
                mixingDevices.add(defaultDevice);
            }
        } else {
            // 创建默认的混合设备
            DeviceInfo defaultDevice = new DeviceInfo();
            defaultDevice.setDeviceId("MIX001");
            defaultDevice.setModel("标准混合器");
            defaultDevice.setManufacturer("标准制造商");
            mixingDevices.add(defaultDevice);
        }
        
        // 解析成型设备数据
        if (formingDevicesJson != null && !formingDevicesJson.isEmpty()) {
            try {
                Type type = new TypeToken<List<DeviceInfo>>(){}.getType();
                List<DeviceInfo> devices = GsonUtil.getGson().fromJson(formingDevicesJson, type);
                if (devices != null && !devices.isEmpty()) {
                    formingDevices = devices;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing formingDevices: " + e.getMessage());
                // 创建默认的成型设备
                DeviceInfo defaultDevice = new DeviceInfo();
                defaultDevice.setDeviceId("FORM001");
                defaultDevice.setModel("标准成型器");
                defaultDevice.setManufacturer("标准制造商");
                formingDevices.add(defaultDevice);
            }
        } else {
            // 创建默认的成型设备
            DeviceInfo defaultDevice = new DeviceInfo();
            defaultDevice.setDeviceId("FORM001");
            defaultDevice.setModel("标准成型器");
            defaultDevice.setManufacturer("标准制造商");
            formingDevices.add(defaultDevice);
        }
        
        // 确保各列表长度一致
        while (moldingMethods.size() < mixRatios.size()) {
            MoldingMethod method = new MoldingMethod();
            method.setCompactionMethod("标准压实");
            method.setMixingTemperature(25.0f);
            method.setMixingSpeed(60.0f);
            method.setMixingTime(180.0f);
            moldingMethods.add(method);
        }
        
        while (mixRatios.size() < moldingMethods.size()) {
            MixRatio ratio = new MixRatio();
            ratio.setName("标准配比");
            ratio.setId(1L);
            ratio.setCreationTime(System.currentTimeMillis());
            ratio.setProjectId("1");
            mixRatios.add(ratio);
        }
        
        while (mixingDevices.size() < moldingMethods.size()) {
            DeviceInfo device = new DeviceInfo();
            device.setDeviceId("MIX001");
            device.setModel("标准混合器");
            device.setManufacturer("标准制造商");
            mixingDevices.add(device);
        }
        
        while (formingDevices.size() < moldingMethods.size()) {
            DeviceInfo device = new DeviceInfo();
            device.setDeviceId("FORM001");
            device.setModel("标准成型器");
            device.setManufacturer("标准制造商");
            formingDevices.add(device);
        }
        
        // 更新UI
        updateUI();
    }

    /**
     * 显示错误状态
     */
    private void showErrorState(String errorMessage) {
        // 清空数据
        moldingMethods.clear();
        mixRatios.clear();
        mixingDevices.clear();
        formingDevices.clear();
        
        // 显示错误信息
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        // 显示错误UI
        if (rvSpecimenInfos != null) {
            rvSpecimenInfos.setVisibility(View.GONE);
        }
        if (viewPagerQRCodes != null) {
            viewPagerQRCodes.setVisibility(View.GONE);
        }
        if (tvCurrentPage != null) {
            tvCurrentPage.setVisibility(View.GONE);
        }
        if (tvTotalPages != null) {
            tvTotalPages.setVisibility(View.GONE);
        }
        if (tvQRCodeContent != null) {
            tvQRCodeContent.setText("发生错误: " + errorMessage);
            tvQRCodeContent.setVisibility(View.VISIBLE);
        }
        if (btnGenerateQRCode != null) {
            btnGenerateQRCode.setEnabled(false);
        }
        
        // 返回按钮仍然可用，允许用户返回
        if (btnComplete != null) {
            btnComplete.setText("返回");
            btnComplete.setOnClickListener(v -> finish());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 100) { // 与请求时使用的请求码匹配
            boolean allGranted = true;
            
            // 检查是否所有权限都已授予
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                
                if (allGranted) {
                    // 所有权限都已授予，继续打印流程
                    Log.d(TAG, "所有权限已授予，继续打印流程");
                    
                    // 在这里使用自动化的打印机选择流程
                    printerDialogManager.showPrinterSelectionDialog(
                        bluetoothPrinterManager, 
                        wifiPrinterManager,
                        new PrinterDialogManager.PrinterSelectionCallback() {
                            @Override
                            public void onPrinterSelected(PrinterInfo printer) {
                                // 根据打印机类型选择管理器
                                if (printer.getType() == PrinterInfo.TYPE_BLUETOOTH) {
                                    currentPrinterManager = bluetoothPrinterManager;
                                } else if (printer.getType() == PrinterInfo.TYPE_WIFI) {
                                    currentPrinterManager = wifiPrinterManager;
                                }
                                currentPrinter = printer;
                                
                                // 连接到打印机并打印二维码
                                if (currentPrinterManager != null) {
                                    // 连接打印机后打印二维码
                                    currentPrinterManager.connectToPrinter(printer, new PrinterManager.PrintCallback() {
                                        @Override
                                        public void onPrintStart() {
                                            runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                                    "正在打印...", Toast.LENGTH_SHORT).show());
                                        }

                                        @Override
                                        public void onPrintSuccess() {
                                            runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                                    "打印成功", Toast.LENGTH_SHORT).show());
                                        }

                                        @Override
                                        public void onPrintError(String error) {
                                            runOnUiThread(() -> Toast.makeText(GenerateSpecimenCodeStep2Activity.this, 
                                                    "打印失败: " + error, Toast.LENGTH_LONG).show());
                                        }
                                    });
                                    
                                    // 准备二维码并打印
                                    printCurrentQRCode();
                                }
                            }

                            @Override
                            public void onCancelled() {
                                Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "取消打印", Toast.LENGTH_SHORT).show();
                            }
                        }
                    );
                } else {
                    // 用户拒绝了某些权限
                    Log.d(TAG, "用户拒绝了某些权限，无法继续");
                    Toast.makeText(this, "需要蓝牙和位置权限才能连接打印机", Toast.LENGTH_LONG).show();
                }
            } else {
                // 用户可能取消了权限对话框
                Log.d(TAG, "用户取消了权限请求");
                Toast.makeText(this, "需要蓝牙和位置权限才能连接打印机", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开所有打印机连接
        if (currentPrinterManager != null) {
            currentPrinterManager.disconnect();
        } else if (bluetoothPrinterManager != null) {
            bluetoothPrinterManager.disconnect();
        }
        executor.shutdown();
    }
}
