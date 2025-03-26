package com.example.labdata_main;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.MixtureTaskApi;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.model.MixratioAndCompactionResponse;
import com.example.labdata_main.model.TaskAssignmentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixtureTaskResultActivity extends AppCompatActivity {
    private static final String TAG = "MixtureTaskResult";
    
    public static final String EXTRA_TASK = "extra_task";
    
    private TextView tvTaskId;
    private TextView tvMixName;
    private TextView tvCompactionMethod;
    private CompletedExperimentTask task;
    private TextView tvResultTitle;
    private TextView tvExperimentName;
    private TextView tvExperimenter;
    private TextView tvCompletionTime;
    
    private MixtureTaskApi mixtureTaskApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixture_task_result);
        
        tvResultTitle = findViewById(R.id.tvResultTitle);
        
        // 初始化视图
        tvTaskId = findViewById(R.id.tvTaskId);
        tvMixName = findViewById(R.id.tvMixName);
        tvCompactionMethod = findViewById(R.id.tvCompactionMethod);
        tvExperimentName = findViewById(R.id.tvExperimentName);
        tvExperimenter = findViewById(R.id.tvExperimenter);
        tvCompletionTime = findViewById(R.id.tvCompletionTime);
        
        // 初始化API
        mixtureTaskApi = ApiClient.getClient().create(MixtureTaskApi.class);
        
        // 获取传递的任务数据
        if (getIntent().hasExtra(EXTRA_TASK)) {
            task = (CompletedExperimentTask) getIntent().getSerializableExtra(EXTRA_TASK);
            displayTaskDetails();
            
            // 获取任务指派信息
            fetchTaskAssignment();
            
            // 获取配比和压实方法信息
            fetchMixratioAndCompaction();
        } else {
            Log.e(TAG, "没有传递任务数据");
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
        
        // 显示配比名称
        String mixName = task.getMixName();
        String mixratioId = task.getMixratioId();
        if (mixName != null && !mixName.isEmpty()) {
            if (mixratioId != null && !mixratioId.isEmpty()) {
                tvMixName.setText("配比名称: " + mixName + " (ID: " + mixratioId + ")");
            } else {
                tvMixName.setText("配比名称: " + mixName);
            }
        } else {
            tvMixName.setText("配比名称: 未知");
        }
        
        // 显示压实方法
        String compactionMethod = task.getCompactionMethod();
        if (compactionMethod != null && !compactionMethod.isEmpty()) {
            tvCompactionMethod.setText("压实方法: " + compactionMethod);
        } else {
            tvCompactionMethod.setText("压实方法: 未知");
        }

        // 在标题显示实验名称
        String projectName = task.getProjectName();
        if (projectName != null && !projectName.isEmpty()) {
            tvResultTitle.setText(projectName);
        } else {
            tvResultTitle.setText("混合料实验结果");
        }

        //基本信息卡片——任务指派信息
        String taskAssignment = task.getTaskAssignment();
        if (taskAssignment != null && !taskAssignment.isEmpty()) {
            tvExperimentName.setText("任务指派: " + taskAssignment);
        } else {
            // 如果taskAssignment为空，尝试使用taskName
            String taskName = task.getTaskName();
            if (taskName != null && !taskName.isEmpty()) {
                tvExperimentName.setText("任务指派: " + taskName);
            } else {
                tvExperimentName.setText("任务指派: 未知");
            }
        }
        
        //基本信息卡片——实验人
        String experimenter = task.getExperimenter();
        if (experimenter != null && !experimenter.isEmpty()) {
            tvExperimenter.setText("实验人: " + experimenter);
        } else {
            tvExperimenter.setText("实验人: 未知");
        }
        
        //基本信息卡片——完成时间
        long completionTimestamp = task.getCompletionTime();
        String formattedTime = CompletedExperimentTask.formatTime(completionTimestamp);
        tvCompletionTime.setText("完成时间: " + formattedTime);
        
        Log.d(TAG, "显示混合料实验任务详情: " + taskId + ", 配比: " + mixName);
    }
    
    private void fetchTaskAssignment() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取任务指派信息");
            return;
        }
        
        // 调用API获取任务指派信息
        Call<TaskAssignmentResponse> call = mixtureTaskApi.getTaskAssignment(taskId);
        call.enqueue(new Callback<TaskAssignmentResponse>() {
            @Override
            public void onResponse(Call<TaskAssignmentResponse> call, Response<TaskAssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TaskAssignmentResponse result = response.body();
                    String taskAssignment = result.getTaskAssignment();
                    
                    // 更新UI显示任务指派信息
                    if (taskAssignment != null && !taskAssignment.isEmpty()) {
                        tvExperimentName.setText("任务指派: " + taskAssignment);
                        Log.d(TAG, "成功获取任务指派信息: " + taskAssignment);
                    } else {
                        Log.w(TAG, "获取到的任务指派信息为空");
                    }
                } else {
                    Log.e(TAG, "获取任务指派信息失败: " + response.code());
                    // 保持使用之前的实验类型信息
                }
            }

            @Override
            public void onFailure(Call<TaskAssignmentResponse> call, Throwable t) {
                Log.e(TAG, "获取任务指派信息请求失败", t);
                Toast.makeText(MixtureTaskResultActivity.this, "获取任务指派信息失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                // 保持使用之前的实验类型信息
            }
        });
    }

    /**
     * 获取配比名称和压实方法信息
     */
    private void fetchMixratioAndCompaction() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取配比和压实方法信息");
            return;
        }
        
        // 调用API获取配比和压实方法信息
        Call<MixratioAndCompactionResponse> call = mixtureTaskApi.getMixratioAndCompaction(taskId);
        call.enqueue(new Callback<MixratioAndCompactionResponse>() {
            @Override
            public void onResponse(Call<MixratioAndCompactionResponse> call, Response<MixratioAndCompactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MixratioAndCompactionResponse result = response.body();
                    
                    // 更新配比名称
                    String mixName = result.getMixName();
                    String mixratioId = task.getMixratioId(); // 保留mixratioId显示
                    if (mixName != null && !mixName.isEmpty()) {
                        if (mixratioId != null && !mixratioId.isEmpty()) {
                            tvMixName.setText("配比名称: " + mixName + " (ID: " + mixratioId + ")");
                        } else {
                            tvMixName.setText("配比名称: " + mixName);
                        }
                        Log.d(TAG, "成功获取配比名称: " + mixName);
                    }
                    
                    // 更新压实方法
                    String compactionMethod = result.getCompactionMethod();
                    if (compactionMethod != null && !compactionMethod.isEmpty()) {
                        tvCompactionMethod.setText("压实方法: " + compactionMethod);
                        Log.d(TAG, "成功获取压实方法: " + compactionMethod);
                    }
                } else {
                    Log.e(TAG, "获取配比和压实方法信息失败: " + (response.code()));
                }
            }
    
            @Override
            public void onFailure(Call<MixratioAndCompactionResponse> call, Throwable t) {
                Log.e(TAG, "获取配比和压实方法信息请求失败", t);
            }
        });
    }
}
