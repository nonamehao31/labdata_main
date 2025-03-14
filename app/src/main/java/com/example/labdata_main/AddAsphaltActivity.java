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

import com.example.labdata_main.api.service.MaterialApiService;
import com.example.labdata_main.api.response.AsphaltMaterialResponse;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.utils.SharedPrefsManager;
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
    private MaterialApiService materialApiService;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asphalt);

        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        databaseHelper = DatabaseHelper.getInstance(this);
        materialApiService = new MaterialApiService();
        sharedPrefsManager = new SharedPrefsManager(this);

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

        // 先同步到后端，不管成功与否都保存到本地数据库
        syncAsphaltToBackend(name, grade, isModified ? "modified" : "normal");

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
                databaseHelper.saveOrUpdateMaterialProperty(property, isModified ? "MODIFIED" : "NORMAL");

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

    /**
     * 同步沥青材料到后端
     * @param name 沥青名称
     * @param grade 沥青标号
     * @param character 沥青性质
     */
    private void syncAsphaltToBackend(String name, String grade, String character) {
        try {
            Log.d(TAG, "开始同步沥青材料到后端: name=" + name + ", grade=" + grade + ", character=" + character);
            
            // 将沥青性质映射为API需要的格式（大写）
            String apiCharacter = "NORMAL";
            if (character.equalsIgnoreCase("modified")) {
                apiCharacter = "MODIFIED";
            }
            
            // 获取用户的公司ID
            String companyId = sharedPrefsManager.getUserCompany();
            if (TextUtils.isEmpty(companyId)) {
                Log.w(TAG, "用户公司ID为空，将使用默认值");
                companyId = "unknown";
            }
            
            Log.d(TAG, "转换后的沥青性质: " + apiCharacter + ", 公司ID: " + companyId);
            
            if (materialApiService == null) {
                Log.e(TAG, "MaterialApiService未初始化!");
                materialApiService = new MaterialApiService();
            }
            
            materialApiService.saveAsphaltMaterial(name, grade, apiCharacter, companyId, new MaterialApiService.ApiCallback<AsphaltMaterialResponse>() {
                @Override
                public void onSuccess(AsphaltMaterialResponse data) {
                    Log.d(TAG, "沥青材料同步成功: id=" + (data != null ? data.getId() : "null") + ", name=" + (data != null ? data.getName() : "null"));
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "沥青材料同步失败: " + errorMessage);
                    // 同步失败不影响本地保存，所以这里只记录日志
                }
            });
            
            Log.d(TAG, "沥青材料同步请求已发送");
        } catch (Exception e) {
            Log.e(TAG, "调用同步沥青材料API失败", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
