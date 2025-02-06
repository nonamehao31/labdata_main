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
import androidx.lifecycle.ViewModelProvider;

import com.example.labdata_main.constants.EquipmentConstants;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.viewmodel.EquipmentInitViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EquipmentInitActivity extends AppCompatActivity {
    private LinearLayout mixingContainer;
    private LinearLayout formingContainer;
    private LinearLayout testingContainer;
    private Button btnFinish;
    private String companyId;
    private EquipmentInitViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_equipment_init);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(EquipmentInitViewModel.class);

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

        Log.d("EquipmentInit", "Received companyId: " + companyId);
    }

    private void initViews() {
        mixingContainer = findViewById(R.id.mixingEquipmentContainer);
        formingContainer = findViewById(R.id.formingEquipmentContainer);
        testingContainer = findViewById(R.id.testingEquipmentContainer);
        btnFinish = findViewById(R.id.btnFinish);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnAddMixing).setOnClickListener(v -> 
            addEquipmentView(mixingContainer, EquipmentConstants.TYPE_MIXING));
        findViewById(R.id.btnAddForming).setOnClickListener(v -> 
            addEquipmentView(formingContainer, EquipmentConstants.TYPE_FORMING));
        findViewById(R.id.btnAddTesting).setOnClickListener(v -> 
            addEquipmentView(testingContainer, EquipmentConstants.TYPE_TESTING));
        
        btnFinish.setOnClickListener(v -> validateAndSaveEquipment());
    }

    private void addEquipmentView(LinearLayout container, String type) {
        View equipmentView = LayoutInflater.from(this).inflate(R.layout.item_equipment, container, false);
        
        Spinner spinnerManufacturer = equipmentView.findViewById(R.id.spinnerManufacturer);
        Spinner spinnerModel = equipmentView.findViewById(R.id.spinnerModel);
        EditText etPurchaseYear = equipmentView.findViewById(R.id.etPurchaseYear);
        Button btnDelete = equipmentView.findViewById(R.id.btnDelete);

        // 设置厂家下拉菜单
        List<String> manufacturers = EquipmentConstants.getManufacturers(type);
        if (manufacturers.isEmpty()) {
            Toast.makeText(this, "暂无可用的厂家信息", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> manufacturerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, manufacturers);
        manufacturerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerManufacturer.setAdapter(manufacturerAdapter);

        // 设置型号下拉菜单
        spinnerManufacturer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedManufacturer = manufacturers.get(position);
                List<String> models = EquipmentConstants.getModels(type, selectedManufacturer);
                if (models.isEmpty()) {
                    Toast.makeText(EquipmentInitActivity.this, 
                        "该厂家暂无可用的型号信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(EquipmentInitActivity.this,
                        android.R.layout.simple_spinner_item, models);
                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerModel.setAdapter(modelAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 设置删除按钮
        btnDelete.setOnClickListener(v -> container.removeView(equipmentView));

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
                // 清除任务栈并跳转到主界面
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                              Intent.FLAG_ACTIVITY_NEW_TASK | 
                              Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
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
                purchaseYear
            );
            devices.add(device);
        }
        return true;
    }
}
