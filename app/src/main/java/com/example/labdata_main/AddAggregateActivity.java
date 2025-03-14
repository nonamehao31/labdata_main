package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.api.response.SandMaterialResponse;
import com.example.labdata_main.api.response.StoneMaterialResponse;
import com.example.labdata_main.api.service.MaterialApiService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 添加集料材料的界面（沙子和石子）
 * 只需收集材料名称
 */
public class AddAggregateActivity extends AppCompatActivity {
    private static final String TAG = "AddAggregateActivity";

    private EditText etAggregateName;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private TextView tvTitle;

    private String materialType; // "sand" 或 "stone"
    private ExecutorService executorService;
    private AppDatabase database;
    private DatabaseHelper databaseHelper;
    private MaterialApiService materialApiService;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_aggregate);

        executorService = Executors.newSingleThreadExecutor();
        database = AppDatabase.getInstance(this);
        databaseHelper = DatabaseHelper.getInstance(this);
        materialApiService = new MaterialApiService();
        sharedPrefsManager = new SharedPrefsManager(this);

        // 获取材料类型
        materialType = getIntent().getStringExtra("materialType");
        if (materialType == null) {
            materialType = "sand"; // 默认为沙子
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAggregateName = findViewById(R.id.et_aggregate_name);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);

        // 设置标题
        String title = getIntent().getStringExtra("title");
        if (title != null) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText(materialType.equals("sand") ? "添加沙子" : "添加石子");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveAggregate());
    }

    private void saveAggregate() {
        String name = etAggregateName.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入" + (materialType.equals("sand") ? "沙子" : "石子") + "名称", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建新材料
        Material aggregate = new Material(
                UUID.randomUUID().toString(),
                name,
                "",
                materialType
        );

        // 保存到数据库
        executorService.execute(() -> {
            try {
                // 使用 insert 方法代替 insertMaterial
                database.materialDao().insert(aggregate);

                // 创建并保存材料属性记录
                MaterialProperty property = new MaterialProperty();
                // 使用 setter 方法设置属性
                property.setName(name);
                property.setType(materialType);
                property.setCode(UUID.randomUUID().toString().substring(0, 8)); // 生成简短的唯一代码

                // 使用 databaseHelper 保存材料属性
                databaseHelper.saveOrUpdateMaterialProperty(property, null);
                
                // 同步到后端服务器
                syncAggregateToBackend(name);
                
                runOnUiThread(() -> {
                    Toast.makeText(AddAggregateActivity.this, (materialType.equals("sand") ? "沙子" : "石子") + "添加成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "保存" + (materialType.equals("sand") ? "沙子" : "石子") + "材料失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(AddAggregateActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * 同步集料(沙子或石子)材料到后端
     * @param name 材料名称
     */
    private void syncAggregateToBackend(String name) {
        try {
            Log.d(TAG, "开始同步" + (materialType.equals("sand") ? "沙子" : "石子") + "材料到后端: name=" + name);
            
            // 获取用户的公司ID
            String companyId = sharedPrefsManager.getUserCompany();
            if (TextUtils.isEmpty(companyId)) {
                Log.w(TAG, "用户公司ID为空，将使用默认值");
                companyId = "unknown";
            }
            
            if (materialApiService == null) {
                Log.e(TAG, "MaterialApiService未初始化!");
                materialApiService = new MaterialApiService();
            }
            
            final String finalCompanyId = companyId;
            
            if (materialType.equals("sand")) {
                // 保存沙子材料到后端
                materialApiService.saveSandMaterial(name, finalCompanyId, new MaterialApiService.ApiCallback<SandMaterialResponse>() {
                    @Override
                    public void onSuccess(SandMaterialResponse data) {
                        Log.d(TAG, "沙子材料同步成功: id=" + (data != null ? data.getId() : "null") + ", name=" + (data != null ? data.getName() : "null"));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "沙子材料同步失败: " + errorMessage);
                        // 同步失败不影响本地保存，所以这里只记录日志
                    }
                });
            } else {
                // 保存石子材料到后端
                materialApiService.saveStoneMaterial(name, finalCompanyId, new MaterialApiService.ApiCallback<StoneMaterialResponse>() {
                    @Override
                    public void onSuccess(StoneMaterialResponse data) {
                        Log.d(TAG, "石子材料同步成功: id=" + (data != null ? data.getId() : "null") + ", name=" + (data != null ? data.getName() : "null"));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "石子材料同步失败: " + errorMessage);
                        // 同步失败不影响本地保存，所以这里只记录日志
                    }
                });
            }
            
            Log.d(TAG, (materialType.equals("sand") ? "沙子" : "石子") + "材料同步请求已发送");
        } catch (Exception e) {
            Log.e(TAG, "调用同步" + (materialType.equals("sand") ? "沙子" : "石子") + "材料API失败", e);
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
