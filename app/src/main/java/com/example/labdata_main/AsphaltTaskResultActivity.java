package com.example.labdata_main;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.api.AsphaltTaskApi;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.AsphaltDetailResponse;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsphaltTaskResultActivity extends AppCompatActivity {
    private static final String TAG = "AsphaltTaskResult";
    
    public static final String EXTRA_TASK = "extra_task";
    
    private TextView tvTaskId;
    private TextView tvExperimentName;
    private TextView tvExperimenter;
    private TextView tvCompletionTime;
    private TextView tvResultTitle;
    private CompletedExperimentTask task;
    private AsphaltTaskApi asphaltTaskApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asphalt_task_result);
        
        // 初始化API客户端
        asphaltTaskApi = ApiClient.getClient().create(AsphaltTaskApi.class);
        
        // 初始化视图
        tvTaskId = findViewById(R.id.tvTaskId);
        tvExperimentName = findViewById(R.id.tvExperimentName);
        tvExperimenter = findViewById(R.id.tvExperimenter);
        tvCompletionTime = findViewById(R.id.tvCompletionTime);
        tvResultTitle = findViewById(R.id.tvResultTitle);        
        // 获取传递的任务数据
        if (getIntent().hasExtra(EXTRA_TASK)) {
            task = (CompletedExperimentTask) getIntent().getSerializableExtra(EXTRA_TASK);
            if (task != null) {
                displayTaskDetails();
                fetchAsphaltTaskDetails();
            } else {
                Log.e(TAG, "无法获取任务信息");
                finish();
            }
        } else {
            Log.e(TAG, "未传递任务信息");
            finish();
        }
    }
    
    /**
     * 显示任务详情
     */
    private void displayTaskDetails() {
        // 确保使用String类型处理taskId，避免大数值问题
        String taskId = task.getTaskId();
        tvTaskId.setText("任务ID: " + taskId);

        //显示结果标题
        tvResultTitle.setText((task.getTaskName() != null ? task.getTaskName() : "未知"));
        
        // 显示实验名称和实验人员信息
        // 获取任务指派信息
        String taskAssignment = task.getTaskAssignment();
        if (taskAssignment == null || taskAssignment.isEmpty()) {
            taskAssignment = "未知";
        }
        
        // 使用任务名称作为实验名称，而不是使用experimentName
        String experimentAssignment = task.getTaskAssignment();
        if (experimentAssignment == null || experimentAssignment.isEmpty()) {
            experimentAssignment = "未知实验";
        }
        
        tvExperimentName.setText("实验名称: " + experimentAssignment);
        tvExperimenter.setText("实验人员: " + (task.getExperimenter() != null ? task.getExperimenter() : "未知"));
        
        // 显示完成时间，使用CompletedExperimentTask的格式化方法
        long completionTimeValue = task.getCompletionTime();
        String completionTime = completionTimeValue > 0 ? 
                CompletedExperimentTask.formatTime(completionTimeValue) : "未知";
        tvCompletionTime.setText("完成时间: " + completionTime);
        
        Log.d(TAG, "显示沥青实验任务详情: " + taskId);
    }
    
    /**
     * 从后端获取沥青任务详细信息
     */
    private void fetchAsphaltTaskDetails() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取详情");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取沥青任务详情: " + taskId);
        
        asphaltTaskApi.getAsphaltDetailByTaskId(taskId).enqueue(new Callback<ApiResponse<AsphaltDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AsphaltDetailResponse>> call, Response<ApiResponse<AsphaltDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    AsphaltDetailResponse detailResponse = response.body().getData();
                    Log.d(TAG, "成功获取沥青任务详情");
                    
                    // 补充实验名称信息
                    if (detailResponse.getAsphaltInfoList() != null && !detailResponse.getAsphaltInfoList().isEmpty()) {
                        // 使用任务名称作为实验名称，而不是构建带有材料信息的名称
                        String experimentType = task.getTaskAssignment();
                        if (experimentType == null || experimentType.isEmpty()) {
                            experimentType = "未知实验";
                        }
                        
                        // 更新显示，只显示实验类型名称
                        tvExperimentName.setText("实验名称: " + experimentType);
                    }
                } else {
                    Log.e(TAG, "获取沥青任务详情失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<AsphaltDetailResponse>> call, Throwable t) {
                Log.e(TAG, "获取沥青任务详情网络请求失败", t);
            }
        });
    }
}
