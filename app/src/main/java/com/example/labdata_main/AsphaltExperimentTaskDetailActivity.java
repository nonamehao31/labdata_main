package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.api.dto.AsphaltExperimentResponse;
import com.example.labdata_main.service.AsphaltExperimentService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 沥青实验任务详情界面
 */
public class AsphaltExperimentTaskDetailActivity extends AppCompatActivity {

    private static final String EXTRA_TASK_ID = "extra_task_id";
    
    private Long taskId;
    private TextView tvTaskName;
    private TextView tvTaskStatus;
    private TextView tvCreationTime;
    private RecyclerView rvAsphalt;
    private LinearLayout asphaltContainer;
    private MaterialButton btnStartTask;
    private MaterialButton btnCompleteTask;
    
    private AsphaltExperimentService asphaltService;

    public static void start(Context context, Long taskId) {
        Intent intent = new Intent(context, AsphaltExperimentTaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asphalt_experiment_task_detail);
        
        // 获取任务ID
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            Toast.makeText(this, "任务ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 初始化服务
        asphaltService = new AsphaltExperimentService(this);
        
        // 初始化视图
        initViews();
        
        // 加载任务数据
        loadTaskData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("沥青实验任务详情");
        }
        
        tvTaskName = findViewById(R.id.tvTaskName);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvCreationTime = findViewById(R.id.tvCreationTime);
        asphaltContainer = findViewById(R.id.asphaltContainer);
        btnStartTask = findViewById(R.id.btnStartTask);
        btnCompleteTask = findViewById(R.id.btnCompleteTask);
        
        btnStartTask.setOnClickListener(v -> startTask());
        btnCompleteTask.setOnClickListener(v -> completeTask());
    }

    private void loadTaskData() {
        // 显示加载提示
        Toast.makeText(this, "正在加载任务数据...", Toast.LENGTH_SHORT).show();
        
        // 调用API获取任务详情
        asphaltService.getAsphaltExperimentTask(taskId, new AsphaltExperimentService.ServiceCallback<AsphaltExperimentResponse>() {
            @Override
            public void onSuccess(AsphaltExperimentResponse task) {
                runOnUiThread(() -> {
                    displayTaskDetails(task);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AsphaltExperimentTaskDetailActivity.this, 
                            "加载任务失败: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayTaskDetails(AsphaltExperimentResponse task) {
        tvTaskName.setText(task.getTaskName());
        tvTaskStatus.setText(task.getStatus());
        
        // 根据任务状态更新按钮显示
        updateButtonsVisibility(task.getStatus());
        
        // 显示沥青数据
        displayAsphaltData(task);
    }

    private void displayAsphaltData(AsphaltExperimentResponse task) {
        asphaltContainer.removeAllViews();
        
        if (task.getAsphaltDataList() == null || task.getAsphaltDataList().isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("没有沥青数据");
            emptyView.setPadding(16, 16, 16, 16);
            asphaltContainer.addView(emptyView);
            return;
        }
        
        for (AsphaltExperimentResponse.AsphaltData asphalt : task.getAsphaltDataList()) {
            View cardView = getLayoutInflater().inflate(R.layout.item_asphalt_detail, asphaltContainer, false);
            
            TextView tvGrade = cardView.findViewById(R.id.tvGrade);
            TextView tvType = cardView.findViewById(R.id.tvType);
            TextView tvSupplier = cardView.findViewById(R.id.tvSupplier);
            TextView tvExpiryDate = cardView.findViewById(R.id.tvExpiryDate);
            TextView tvExperiments = cardView.findViewById(R.id.tvExperiments);
            
            tvGrade.setText(asphalt.getGrade());
            tvType.setText(asphalt.getType());
            tvSupplier.setText(asphalt.getSupplier());
            tvExpiryDate.setText(asphalt.getExpiryDate());
            
            // TODO: 显示关联的实验类型，这需要后端API提供实验类型名称
            
            asphaltContainer.addView(cardView);
        }
    }

    private void updateButtonsVisibility(String status) {
        if ("未接受".equals(status)) {
            btnStartTask.setVisibility(View.VISIBLE);
            btnCompleteTask.setVisibility(View.GONE);
        } else if ("进行中".equals(status)) {
            btnStartTask.setVisibility(View.GONE);
            btnCompleteTask.setVisibility(View.VISIBLE);
        } else {
            btnStartTask.setVisibility(View.GONE);
            btnCompleteTask.setVisibility(View.GONE);
        }
    }

    private void startTask() {
        // TODO: 实现开始任务的逻辑
        Toast.makeText(this, "任务开始功能将在后续版本中实现", Toast.LENGTH_SHORT).show();
    }

    private void completeTask() {
        // TODO: 实现完成任务的逻辑
        Toast.makeText(this, "任务完成功能将在后续版本中实现", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
