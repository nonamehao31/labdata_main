package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MaterialProperty;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 添加沥青材料的界面
 * 收集沥青的名称、标号、沥青性质(改性沥青或普通沥青)
 */
public class AddAsphaltActivity extends AppCompatActivity {
    private static final String TAG = "AddAsphaltActivity";

    private EditText etAsphaltName;
    private EditText etAsphaltGrade;
    private RadioGroup rgAsphaltType;
    private RadioButton rbNormalAsphalt;
    private RadioButton rbModifiedAsphalt;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private TextView tvTitle;

    private ExecutorService executorService;
    private AppDatabase database;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asphalt);

        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        databaseHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAsphaltName = findViewById(R.id.et_asphalt_name);
        etAsphaltGrade = findViewById(R.id.et_asphalt_grade);
        rgAsphaltType = findViewById(R.id.rg_asphalt_type);
        rbNormalAsphalt = findViewById(R.id.rb_normal_asphalt);
        rbModifiedAsphalt = findViewById(R.id.rb_modified_asphalt);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);

        tvTitle.setText("添加沥青");
        rbNormalAsphalt.setChecked(true); // 默认选择普通沥青
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveAsphalt());
    }

    private void saveAsphalt() {
        String name = etAsphaltName.getText().toString().trim();
        String grade = etAsphaltGrade.getText().toString().trim();
        boolean isModified = rbModifiedAsphalt.isChecked();
        String type = isModified ? "改性沥青" : "普通沥青";

        // 验证输入
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入沥青名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(grade)) {
            Toast.makeText(this, "请输入沥青标号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 组合沥青完整名称：名称 + 标号 + 类型
        String fullName = name + " " + grade + " " + type;
        String description = "标号: " + grade + ", 类型: " + type;

        // 创建新材料
        Material asphalt = new Material(
                UUID.randomUUID().toString(),
                fullName,
                description,
                "asphalt"
        );

        // 保存到数据库
        executorService.execute(() -> {
            try {
                // 使用 insert 而不是 insertMaterial
                database.materialDao().insert(asphalt);

                // 创建并保存材料属性记录
                MaterialProperty property = new MaterialProperty();
                // 使用 setter 方法设置 id
                // PropertyId 自动生成，不需要手动设置
                property.setType("asphalt");
                property.setName(fullName);
                property.setCode(grade);
                
                // 使用 databaseHelper 保存材料属性
                databaseHelper.saveOrUpdateMaterialProperty(property, isModified ? "modified" : "normal");

                runOnUiThread(() -> {
                    Toast.makeText(AddAsphaltActivity.this, "沥青添加成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "保存沥青材料失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(AddAsphaltActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
