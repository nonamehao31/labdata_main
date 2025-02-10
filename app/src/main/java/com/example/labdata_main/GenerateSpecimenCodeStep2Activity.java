package com.example.labdata_main;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.labdata_main.database.AppDatabase;

public class GenerateSpecimenCodeStep2Activity extends AppCompatActivity {
    private static final String TAG = "GenerateSpecimenStep2";
    private static final int QR_CODE_SIZE = 500;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
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
    private BluetoothPrinterManager printerManager;
    private ProjectStatus projectStatus;

    private List<MoldingMethod> moldingMethods;
    private List<MixRatio> mixRatios;
    private List<DeviceInfo> mixingDevices;
    private List<DeviceInfo> formingDevices;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showPrinterSelectionDialog();
            } else {
                Toast.makeText(this, "需要蓝牙和位置权限才能连接打印机", Toast.LENGTH_LONG).show();
            }
        });

    private ExecutorService executor;
    private AppDatabase database;
    private long taskId;
    private List<String> selectedMixingDevices;
    private List<String> selectedFormingDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_specimen_code_step2);

        // 初始化变量
        executor = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        taskId = getIntent().getLongExtra("taskId", -1);
        selectedMixingDevices = new ArrayList<>();
        selectedFormingDevices = new ArrayList<>();

        // 初始化蓝牙打印管理器
        printerManager = new BluetoothPrinterManager(this);

        // 初始化项目状态
        projectStatus = new ProjectStatus();

        // 获取传递的数据
        String moldingMethodsJson = getIntent().getStringExtra("moldingMethod");
        String mixRatiosJson = getIntent().getStringExtra("mixRatio");
        String mixingDevicesJson = getIntent().getStringExtra("mixingDevice");
        String formingDevicesJson = getIntent().getStringExtra("formingDevice");

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<MoldingMethod>>(){}.getType();
        moldingMethods = gson.fromJson(moldingMethodsJson, listType);
        
        listType = new TypeToken<ArrayList<MixRatio>>(){}.getType();
        mixRatios = gson.fromJson(mixRatiosJson, listType);
        
        listType = new TypeToken<ArrayList<DeviceInfo>>(){}.getType();
        mixingDevices = gson.fromJson(mixingDevicesJson, listType);
        formingDevices = gson.fromJson(formingDevicesJson, listType);

        initializeViews();
        setupRecyclerView();
        setupViewPager();
        loadTaskData();
        updateUI();
    }

    private void initializeViews() {
        rvSpecimenInfos = findViewById(R.id.rvSpecimenInfos);
        viewPagerQRCodes = findViewById(R.id.viewPagerQRCodes);
        tvCurrentPage = findViewById(R.id.tvCurrentPage);
        tvTotalPages = findViewById(R.id.tvTotalPages);
        tvQRCodeContent = findViewById(R.id.tvQRCodeContent);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnComplete = findViewById(R.id.btnComplete);

        btnGenerateQRCode.setText("打印试件码");
        btnGenerateQRCode.setOnClickListener(v -> checkPermissionsAndPrint());

        btnComplete.setOnClickListener(v -> completeSpecimenGeneration());
    }

    private void checkPermissionsAndPrint() {
        if (!printerManager.isBluetoothEnabled()) {
            Toast.makeText(this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allPermissionsGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            showPrinterSelectionDialog();
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    private void showPrinterSelectionDialog() {
        List<BluetoothDevice> printers = printerManager.getPairedPrinters();
        if (printers.isEmpty()) {
            Toast.makeText(this, "未找到已配对的打印机，请先在系统设置中配对打印机", Toast.LENGTH_LONG).show();
            return;
        }

        String[] printerNames = new String[printers.size()];
        for (int i = 0; i < printers.size(); i++) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                printerNames[i] = printers.get(i).getName();
            }
        }

        new AlertDialog.Builder(this)
            .setTitle("选择打印机")
            .setItems(printerNames, (dialog, which) -> {
                BluetoothDevice selectedPrinter = printers.get(which);
                connectAndPrint(selectedPrinter);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void connectAndPrint(BluetoothDevice printer) {
        printerManager.connectToPrinter(printer, new BluetoothPrinterManager.PrintCallback() {
            @Override
            public void onPrintStart() {
                Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "开始打印...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrintSuccess() {
                Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "打印完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrintError(String error) {
                Toast.makeText(GenerateSpecimenCodeStep2Activity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // 获取当前显示的二维码
        int currentPosition = viewPagerQRCodes.getCurrentItem();
        Bitmap currentQRCode = qrCodePagerAdapter.getQRCode(currentPosition);
        
        if (currentQRCode != null) {
            printerManager.printQRCode(currentQRCode, new BluetoothPrinterManager.PrintCallback() {
                @Override
                public void onPrintStart() {
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "正在打印第 " + (currentPosition + 1) + " 个二维码", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrintSuccess() {
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "打印成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrintError(String error) {
                    Toast.makeText(GenerateSpecimenCodeStep2Activity.this, "打印失败: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerManager.disconnect();
        executor.shutdown();
    }

    private void setupRecyclerView() {
        specimenInfoAdapter = new SpecimenInfoAdapter();
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
                
                // 为每个制件方法创建单独的QR码数据
                QRCodeData qrCodeData = new QRCodeData(
                    moldingMethods.get(i),
                    mixRatios.get(i % mixRatios.size()),
                    mixingDevices.get(i),
                    formingDevices.get(i)
                );

                // 转换为JSON
                String jsonContent = new Gson().toJson(qrCodeData);
                Log.d(TAG, "JSON content: " + jsonContent);

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
                
                // 添加到适配器
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
        // 保存设备信息到数据库
        executor.execute(() -> {
            ExperimentTask task = database.experimentTaskDao().getExperimentTaskById(taskId);
            if (task != null) {
                task.setSelectedMixingDevices(selectedMixingDevices);
                task.setSelectedFormingDevices(selectedFormingDevices);
                task.setSpecimenGenerationTime(System.currentTimeMillis());
                database.experimentTaskDao().update(task);
            }
        });

        Intent resultIntent = new Intent();
        resultIntent.putExtra("specimenStatus", ProjectStatus.STATUS_COMPLETED);
        resultIntent.putExtra("experimentStatus", ProjectStatus.STATUS_IN_PROGRESS);
        resultIntent.putExtra("preparationStatus", ProjectStatus.STATUS_COMPLETED);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void loadTaskData() {
        executor.execute(() -> {
            ExperimentTask task = database.experimentTaskDao().getExperimentTaskById(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    // 加载已保存的设备信息
                    selectedMixingDevices = task.getSelectedMixingDevices();
                    selectedFormingDevices = task.getSelectedFormingDevices();
                    
                    // 更新RecyclerView
                    specimenInfoAdapter.setSelectedMixingDevices(selectedMixingDevices);
                    specimenInfoAdapter.setSelectedFormingDevices(selectedFormingDevices);
                    specimenInfoAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private static class QRCodeData {
        MoldingMethod moldingMethod;
        MixRatio mixRatio;
        DeviceInfo mixingDevice;
        DeviceInfo formingDevice;

        public QRCodeData(MoldingMethod moldingMethod, MixRatio mixRatio,
                         DeviceInfo mixingDevice, DeviceInfo formingDevice) {
            this.moldingMethod = moldingMethod;
            this.mixRatio = mixRatio;
            this.mixingDevice = mixingDevice;
            this.formingDevice = formingDevice;
        }
    }
}
