package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.labdata_main.api.MixRatioApiService;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MixRatioResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectRatioActivity extends AppCompatActivity {
    private static final String TAG = "SelectRatioActivity";
    
    private RadioGroup radioGroup;
    private Button btnNext;
    private ImageButton btnBack;
    private TextView tvProjectName;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private TextView tvNoRatios;
    
    private String projectName;
    private String selectedRatio;
    private Long selectedRatioId;
    
    private MixRatioApiService mixRatioApiService;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ratio);

        projectName = getIntent().getStringExtra("project_name");
        
        mixRatioApiService = new MixRatioApiService(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        
        initViews();
        setupClickListeners();
        updateUI();
        loadCompanyMixRatios();
    }

    private void initViews() {
        radioGroup = findViewById(R.id.radioGroup);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        tvNoRatios = findViewById(R.id.tvNoRatios);
    }

    private void updateUI() {
        tvProjectName.setText(projectName);
        // 设置进度文字中"原料配比"为紫色
        String progressText = tvProgress.getText().toString();
        int purpleColor = ContextCompat.getColor(this, android.R.color.holo_purple);
        tvProgress.setTextColor(purpleColor);
    }
    
    private void loadCompanyMixRatios() {
        progressBar.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);
        tvNoRatios.setVisibility(View.GONE);
        
        try {
            String companyIdStr = sharedPrefsManager.getUserCompany();
            if (companyIdStr == null || companyIdStr.isEmpty()) {
                showError("无法获取公司信息");
                return;
            }
            
            Long companyId;
            try {
                companyId = Long.parseLong(companyIdStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "公司ID格式不正确: " + companyIdStr, e);
                showError("公司ID格式不正确");
                return;
            }
            
            Log.d(TAG, "开始获取公司配比数据，公司ID: " + companyId);
            
            mixRatioApiService.getMixRatiosByCompany(companyId, new Callback<ApiResponse<List<MixRatioResponse>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<MixRatioResponse>>> call, Response<ApiResponse<List<MixRatioResponse>>> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<MixRatioResponse> mixRatios = response.body().getData();
                        if (mixRatios != null && !mixRatios.isEmpty()) {
                            displayMixRatios(mixRatios);
                        } else {
                            showNoRatiosMessage();
                        }
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "获取配比失败";
                        showError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<MixRatioResponse>>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "API调用失败", t);
                    showError("无法连接到服务器");
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "加载配比异常", e);
            showError("加载配比数据出错");
        }
    }
    
    private void displayMixRatios(List<MixRatioResponse> mixRatios) {
        radioGroup.removeAllViews();
        
        for (MixRatioResponse mixRatio : mixRatios) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(mixRatio.getMixName());
            radioButton.setTag(mixRatio.getId()); // 将配比ID存储在Tag中
            
            // 设置样式，与预设的按钮样式一致
            radioButton.setBackground(ContextCompat.getDrawable(this, R.drawable.radio_button_background));
            radioButton.setButtonDrawable(null);
            radioButton.setPadding(36, 36, 36, 36);
            radioButton.setGravity(android.view.Gravity.CENTER);
            
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 24); // 底部间距
            radioButton.setLayoutParams(params);
            
            radioGroup.addView(radioButton);
        }
        
        // 添加"新建配比"按钮
        RadioButton addNewButton = new RadioButton(this);
        addNewButton.setId(R.id.btnAddRatio);
        addNewButton.setText("添加配比");
        addNewButton.setBackground(ContextCompat.getDrawable(this, R.drawable.radio_button_background));
        addNewButton.setButtonDrawable(null);
        addNewButton.setPadding(36, 36, 36, 36);
        addNewButton.setGravity(android.view.Gravity.CENTER);
        
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        addNewButton.setLayoutParams(params);
        
        radioGroup.addView(addNewButton);
        radioGroup.setVisibility(View.VISIBLE);
    }
    
    private void showNoRatiosMessage() {
        tvNoRatios.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvNoRatios.setText("加载配比失败: " + message);
        tvNoRatios.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            btnNext.setEnabled(true);
            btnNext.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_purple));
            
            if (checkedId == R.id.btnAddRatio) {
                selectedRatio = "新配比";
                selectedRatioId = null;
            } else {
                RadioButton selectedButton = findViewById(checkedId);
                if (selectedButton != null) {
                    selectedRatio = selectedButton.getText().toString();
                    selectedRatioId = (Long) selectedButton.getTag();
                }
            }
            
            // 保存选择的配比名称和ID
            SharedPrefsManager.saveString(this, "selected_ratio", selectedRatio);
            if (selectedRatioId != null) {
                SharedPrefsManager.saveLong(this, "selected_ratio_id", selectedRatioId);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (selectedRatio != null) {
                // 跳转到制件方式界面
                Intent intent = new Intent(this, ManufacturingMethodActivity.class);
                intent.putExtra("project_name", projectName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "请先选择配比", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
