package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.labdata_main.api.service.MaterialSyncService;
import com.example.labdata_main.model.MaterialItem;

public class AddMaterialActivity extends AppCompatActivity {
    private static final String TAG = "AddMaterialActivity";
    private EditText etMaterialName;
    private EditText etPercentage;
    private Spinner spMaterialType;
    private Button btnConfirm;
    private MaterialSyncService materialSyncService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material);

        initViews();
        setupSpinner();
        setupListeners();
        materialSyncService = new MaterialSyncService(this);
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        etMaterialName = findViewById(R.id.etMaterialName);
        etPercentage = findViewById(R.id.etPercentage);
        spMaterialType = findViewById(R.id.spMaterialType);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvTitle.setText("添加材料");
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] materialTypes = {"胶凝材料", "集料", "外加剂", "其他"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, materialTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMaterialType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> {
            String name = etMaterialName.getText().toString().trim();
            String percentageStr = etPercentage.getText().toString().trim();
            String type = spMaterialType.getSelectedItem().toString();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入材料名称");
                return;
            }

            if (TextUtils.isEmpty(percentageStr)) {
                showToast("请输入百分比");
                return;
            }

            float percentage;
            try {
                percentage = Float.parseFloat(percentageStr);
                if (percentage <= 0 || percentage > 100) {
                    showToast("百分比必须在0-100之间");
                    return;
                }
            } catch (NumberFormatException e) {
                showToast("请输入有效的百分比");
                return;
            }

            // 创建MaterialItem对象
            MaterialItem material = new MaterialItem(name, percentage, type);
            
            // 将材料数据传回调用活动
            Intent resultIntent = new Intent();
            resultIntent.putExtra("material", material);
            setResult(RESULT_OK, resultIntent);
            
            // 同步材料数据到后端
            syncMaterialToBackend(material);
            
            finish();
        });
    }
    
    /**
     * 同步材料数据到后端
     * @param material 材料项
     */
    private void syncMaterialToBackend(MaterialItem material) {
        if (material == null) {
            return;
        }
        
        // 使用MaterialSyncService自动识别材料类型并同步到后端
        materialSyncService.autoSyncMaterial(material, new MaterialSyncService.SyncResultListener() {
            @Override
            public void onSyncSuccess(MaterialItem material, Long remoteId) {
                Log.d(TAG, "材料同步成功: " + material.getName() + ", 远程ID: " + remoteId);
                // 同步成功后的操作，由于已经结束活动，这里不做UI更新
            }
            
            @Override
            public void onSyncFailed(String errorMessage) {
                Log.e(TAG, "材料同步失败: " + errorMessage);
                // 同步失败的处理，由于已经结束活动，这里仅记录日志不做UI提示
                // 在实际应用中，可能需要更完善的错误处理机制
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
