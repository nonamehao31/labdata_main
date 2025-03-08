package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.labdata_main.service.SupportedDeviceService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.model.Equipment;
import com.example.labdata_main.viewmodel.DeviceSelectionViewModel;
import com.example.labdata_main.viewmodel.EquipmentInitViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EquipmentInitActivity extends AppCompatActivity {
    private static final String TAG = "EquipmentInitActivity";
    
    private LinearLayout mixingContainer;
    private LinearLayout formingContainer;
    private LinearLayout testingContainer;
    private Button btnFinish;
    private String companyId;
    private EquipmentInitViewModel viewModel;
    private DeviceSelectionViewModel deviceSelectionViewModel;
    
    // 用于存储视图与设备选择ViewModel的映射关系
    private Map<View, DeviceSelectionViewModel> viewModelMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_equipment_init);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(EquipmentInitViewModel.class);
        deviceSelectionViewModel = new ViewModelProvider(this).get(DeviceSelectionViewModel.class);

        // 初始化视图
        initViews();
        // 设置点击事件
        setupClickListeners();
        
        // 获取公司ID
        companyId = getIntent().getStringExtra("company_id");
        if (companyId == null) {
            Toast.makeText(this, "公司信息获取失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Received companyId: " + companyId);
    }

    private void initViews() {
        mixingContainer = findViewById(R.id.mixingEquipmentContainer);
        formingContainer = findViewById(R.id.formingEquipmentContainer);
        testingContainer = findViewById(R.id.testingEquipmentContainer);
        btnFinish = findViewById(R.id.btnFinish);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnAddMixing).setOnClickListener(v -> 
            addEquipmentView(mixingContainer, SupportedDeviceService.TYPE_MIXING));
        findViewById(R.id.btnAddForming).setOnClickListener(v -> 
            addEquipmentView(formingContainer, SupportedDeviceService.TYPE_FORMING));
        findViewById(R.id.btnAddTesting).setOnClickListener(v -> 
            addEquipmentView(testingContainer, SupportedDeviceService.TYPE_TESTING));
        
        btnFinish.setOnClickListener(v -> validateAndSaveEquipment());
    }

    private void addEquipmentView(LinearLayout container, String type) {
        View equipmentView = LayoutInflater.from(this).inflate(R.layout.item_equipment, container, false);
        
        Spinner spinnerManufacturer = equipmentView.findViewById(R.id.spinnerManufacturer);
        Spinner spinnerModel = equipmentView.findViewById(R.id.spinnerModel);
        EditText etPurchaseYear = equipmentView.findViewById(R.id.etPurchaseYear);
        Button btnDelete = equipmentView.findViewById(R.id.btnDelete);

        // 创建每个设备视图专属的ViewModel实例
        DeviceSelectionViewModel itemViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(DeviceSelectionViewModel.class);
        viewModelMap.put(equipmentView, itemViewModel);
        
        // 设置设备类型
        itemViewModel.setSelectedType(type);
        
        // 设置厂家下拉菜单
        itemViewModel.getManufacturers().observe(this, manufacturers -> {
            if (manufacturers == null || manufacturers.isEmpty()) {
                Toast.makeText(this, "暂无可用的厂家信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ArrayAdapter<String> manufacturerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, manufacturers);
            manufacturerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerManufacturer.setAdapter(manufacturerAdapter);
            
            // 如果之前选择过厂家，尝试恢复选择
            String selectedManufacturer = itemViewModel.getSelectedManufacturer().getValue();
            if (selectedManufacturer != null) {
                int position = manufacturers.indexOf(selectedManufacturer);
                if (position >= 0) {
                    spinnerManufacturer.setSelection(position);
                }
            }
        });

        // 设置型号下拉菜单更新逻辑
        itemViewModel.getModels().observe(this, models -> {
            if (models == null || models.isEmpty()) {
                return;  // 不显示提示，因为用户可能还未选择厂家
            }
            
            ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, models);
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerModel.setAdapter(modelAdapter);
            
            // 如果之前选择过型号，尝试恢复选择
            String selectedModel = itemViewModel.getSelectedModel().getValue();
            if (selectedModel != null) {
                int position = models.indexOf(selectedModel);
                if (position >= 0) {
                    spinnerModel.setSelection(position);
                }
            }
        });
        
        // 设置厂家选择监听器
        spinnerManufacturer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedManufacturer = (String) parent.getItemAtPosition(position);
                itemViewModel.setSelectedManufacturer(selectedManufacturer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 清除选择
                itemViewModel.setSelectedManufacturer(null);
            }
        });
        
        // 设置型号选择监听器
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedModel = (String) parent.getItemAtPosition(position);
                itemViewModel.setSelectedModel(selectedModel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 清除选择
                itemViewModel.setSelectedModel(null);
            }
        });

        // 设置删除按钮
        btnDelete.setOnClickListener(v -> {
            viewModelMap.remove(equipmentView);  // 从映射中移除
            container.removeView(equipmentView);
        });

        // 设置年份输入限制
        etPurchaseYear.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        // 为视图设置标记，用于识别设备类型
        equipmentView.setTag(type);
        
        container.addView(equipmentView);
    }

    private void validateAndSaveEquipment() {
        List<Device> devices = new ArrayList<>();
        boolean isValid = true;

        // 验证拌合设备
        isValid &= validateContainer(mixingContainer, devices);
        // 验证制件设备
        isValid &= validateContainer(formingContainer, devices);
        // 验证试验设备
        isValid &= validateContainer(testingContainer, devices);

        if (!isValid) {
            Toast.makeText(this, "请完善设备信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (devices.isEmpty()) {
            Toast.makeText(this, "请至少添加一台设备", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存设备信息
        viewModel.saveDevices(devices).observe(this, success -> {
            if (success) {
                Toast.makeText(this, "设备信息保存成功", Toast.LENGTH_SHORT).show();
                // 跳转到二维码显示界面
                Intent intent = new Intent(this, QRCodeDisplayActivity.class);
                intent.putExtra("company_id", companyId);
                // 将设备列表转换为Equipment对象列表并传递
                ArrayList<Equipment> equipmentList = new ArrayList<>();
                try {
                    for (Device device : devices) {
                        Equipment equipment = new Equipment(
                            companyId,
                            device.getType(),
                            device.getModel(),
                            device.getManufacturer(),
                            device.getPurchaseYear()
                        );
                        equipmentList.add(equipment);
                    }
                    Log.d(TAG, "Created equipment list with " + equipmentList.size() + " items");
                    
                    // 使用JSON序列化传递Equipment对象列表
                    String equipmentJson = Equipment.toJsonString(equipmentList);
                    intent.putExtra("equipment_json", equipmentJson);
                    
                    Log.d(TAG, "Serialized equipment list to JSON");
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error preparing equipment data: " + e.getMessage(), e);
                    Toast.makeText(this, "设备信息处理失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "设备信息保存失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateContainer(LinearLayout container, List<Device> devices) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View equipmentView = container.getChildAt(i);
            String type = (String) equipmentView.getTag();
            
            Spinner spinnerManufacturer = equipmentView.findViewById(R.id.spinnerManufacturer);
            Spinner spinnerModel = equipmentView.findViewById(R.id.spinnerModel);
            EditText etPurchaseYear = equipmentView.findViewById(R.id.etPurchaseYear);

            // 检查是否有选中的项
            if (spinnerManufacturer.getSelectedItem() == null || 
                spinnerModel.getSelectedItem() == null) {
                return false;
            }

            String manufacturer = spinnerManufacturer.getSelectedItem().toString();
            String model = spinnerModel.getSelectedItem().toString();
            String purchaseYear = etPurchaseYear.getText().toString();

            if (manufacturer.isEmpty() || model.isEmpty() || purchaseYear.isEmpty()) {
                return false;
            }

            Device device = new Device(
                UUID.randomUUID().toString(),  // 生成唯一ID
                type,
                manufacturer,
                model,
                purchaseYear,
                companyId  // 添加公司ID
            );
            devices.add(device);
        }
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        viewModelMap.clear();
    }
}
