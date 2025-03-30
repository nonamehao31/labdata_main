package com.example.labdata_main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.DynamicModulusAdapter;
import com.example.labdata_main.adapter.DirectStretchingFatigueSpecimenAdapter;
import com.example.labdata_main.adapter.FourPointBendingFatigueAdapter;
import com.example.labdata_main.adapter.HamburgRuttingAdapter;
import com.example.labdata_main.adapter.MarshallAdapter;
import com.example.labdata_main.adapter.MixtureBendingAdapter;
import com.example.labdata_main.adapter.SplittingTestAdapter;
import com.example.labdata_main.adapter.UniaxialCompressionAdapter;
import com.example.labdata_main.model.DirectStretchingFatigueTestResponse;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.MixtureTaskApi;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.model.DynamicModulusTestResponse;
import com.example.labdata_main.model.FourPointBendingFatigueTestResponse;
import com.example.labdata_main.model.HamburgRuttingTestResponse;
import com.example.labdata_main.model.MarshallTestResponse;
import com.example.labdata_main.model.MixtureBendingTestResponse;
import com.example.labdata_main.model.MixratioAndCompactionResponse;
import com.example.labdata_main.model.SplittingTestResponse;
import com.example.labdata_main.model.TaskAssignmentResponse;
import com.example.labdata_main.model.UniaxialCompressionTestResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MixtureTaskResultActivity extends AppCompatActivity {
    private static final String TAG = "MixtureTaskResult";
    
    public static final String EXTRA_TASK = "extra_task";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    
    private TextView tvTaskId;
    private TextView tvMixName;
    private TextView tvCompactionMethod;
    private CompletedExperimentTask task;
    private TextView tvResultTitle;
    private TextView tvExperimentName;
    private TextView tvExperimenter;
    private TextView tvCompletionTime;
    private RecyclerView rvExperimentResults;
    private TextView tvNoResults;
    
    private MixtureTaskApi mixtureTaskApi;
    private MarshallAdapter marshallAdapter;
    private HamburgRuttingAdapter hamburgRuttingAdapter;
    private MixtureBendingAdapter mixtureBendingAdapter;
    private DynamicModulusAdapter dynamicModulusAdapter;
    private DirectStretchingFatigueSpecimenAdapter directStretchingFatigueSpecimenAdapter;
    private FourPointBendingFatigueAdapter fourPointBendingFatigueAdapter;
    private UniaxialCompressionAdapter uniaxialCompressionAdapter;
    private SplittingTestAdapter splittingTestAdapter;

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
        rvExperimentResults = findViewById(R.id.rvExperimentResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        
        // 初始化RecyclerView
        rvExperimentResults.setLayoutManager(new LinearLayoutManager(this));
        
        // 初始化API
        mixtureTaskApi = ApiClient.getClient().create(MixtureTaskApi.class);
        
        // 优先检查是否有单独的参数传递
        if (getIntent().hasExtra(EXTRA_TASK_ID)) {
            try {
                // 从Intent中获取各个独立参数
                String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
                String taskName = getIntent().getStringExtra("task_name");
                String experimenter = getIntent().getStringExtra("experimenter");
                long completionTime = getIntent().getLongExtra("completion_time", 0);
                String experimentType = getIntent().getStringExtra("experiment_type");
                String experimentName = getIntent().getStringExtra("experiment_name");
                
                // 创建临时CompletedExperimentTask对象以保持现有逻辑兼容
                task = new CompletedExperimentTask();
                task.setTaskId(taskId);
                task.setTaskName(taskName);
                task.setExperimenter(experimenter);
                task.setCompletionTime(completionTime);
                task.setExperimentType(experimentType);
                task.setExperimentName(experimentName);
                task.setMixtureTask(true); // 由于这是MixtureTaskResultActivity，所以设为true
                
                Log.d(TAG, "通过独立参数创建任务对象: " + taskId);
                displayTaskDetails();
                
                // 获取任务指派信息
                fetchTaskAssignment();
                
                // 获取配比和压实方法信息
                fetchMixratioAndCompaction();
            } catch (Exception e) {
                Log.e(TAG, "处理独立参数时发生异常", e);
                Toast.makeText(this, "无法获取任务信息: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        // 兼容旧版本的Parcelable传递方式
        else if (getIntent().hasExtra(EXTRA_TASK) || getIntent().hasExtra("task_id")) {
            try {
                // 尝试两种方式获取数据，增加容错能力
                if (getIntent().hasExtra(EXTRA_TASK)) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        // Android 13及以上使用新API
                        task = getIntent().getParcelableExtra(EXTRA_TASK, CompletedExperimentTask.class);
                    } else {
                        // 兼容旧版本
                        task = getIntent().getParcelableExtra(EXTRA_TASK);
                    }
                }
                
                // 获取并处理任务数据
                if (task != null) {
                    Log.d(TAG, "成功获取任务数据: " + task.getTaskId());
                    displayTaskDetails();
                    
                    // 获取任务指派信息
                    fetchTaskAssignment();
                    
                    // 获取配比和压实方法信息
                    fetchMixratioAndCompaction();
                } else {
                    // 直接从intent中获取任务ID
                    String taskId = getIntent().getStringExtra("task_id");
                    if (taskId != null && !taskId.isEmpty()) {
                        Log.d(TAG, "使用任务ID直接获取数据: " + taskId);
                        tvTaskId.setText(taskId);
                        
                        // 使用taskId继续获取数据
                        fetchTaskAssignment();
                        fetchMixratioAndCompaction();
                    } else {
                        Toast.makeText(this, "无法获取任务信息", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取任务数据时发生异常", e);
                // 提取任务ID直接从intent中获取
                String taskId = getIntent().getStringExtra("task_id");
                if (taskId != null && !taskId.isEmpty()) {
                    Log.d(TAG, "异常恢复：使用任务ID: " + taskId);
                    tvTaskId.setText(taskId);
                    
                    // 使用taskId继续获取数据
                    fetchTaskAssignment();
                    fetchMixratioAndCompaction();
                } else {
                    Toast.makeText(this, "无法获取任务信息", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            Toast.makeText(this, "未提供任务信息", Toast.LENGTH_SHORT).show();
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
        String mixratioId = task.getMixratioId(); // 保留mixratioId显示
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
            // 使用更灵活的方式检查实验类型
            if (containsKeyword(taskAssignment, "马歇尔", "Marshall", "稳定度")) {
                fetchMarshallTestData();
            } else if (containsKeyword(taskAssignment, "沥青混合料四点弯曲疲劳寿命试验", "四点弯曲")) {
                fetchFourPointBendingFatigueTestData();
            } else if (containsKeyword(taskAssignment, "汉堡车辙", "Hamburg", "车辙")) {
                fetchHamburgRuttingTestData();
            } else if (containsKeyword(taskAssignment, "沥青混合料弯曲实验")) {
                fetchMixtureBendingTestData();
            } else if (containsKeyword(taskAssignment, "动态模量")) {
                fetchDynamicModulusTestData();
            } else if (containsKeyword(taskAssignment, "直接拉伸循环疲劳", "疲劳", "黏弹损伤")) {
                fetchDirectStretchingFatigueTestData();
            } else if (containsKeyword(taskAssignment, "沥青混合料单轴压缩试验", "单轴压缩")) {
                fetchUniaxialCompressionTestData();
            } else if (containsKeyword(taskAssignment, "劈裂", "劈裂试验")) {
                fetchSplittingTestData();
            } else {
                // 根据关键字组合尝试确定实验类型
                determineExperimentTypeAndFetchData(taskAssignment);
            }
        } else {
            // 如果taskAssignment为空，尝试使用taskName
            String taskName = task.getTaskName();
            if (taskName != null && !taskName.isEmpty()) {
                tvExperimentName.setText("任务指派: " + taskName);
                // 使用更灵活的方式检查实验类型
                if (containsKeyword(taskName, "马歇尔", "Marshall", "稳定度")) {
                    fetchMarshallTestData();
                } else if (containsKeyword(taskName, "汉堡车辙", "Hamburg", "车辙")) {
                    fetchHamburgRuttingTestData();
                } else if (containsKeyword(taskName, "沥青混合料弯曲实验", "弯曲")) {
                    fetchMixtureBendingTestData();
                } else if (containsKeyword(taskName, "动态模量")) {
                    fetchDynamicModulusTestData();
                } else if (containsKeyword(taskName, "直接拉伸循环疲劳", "疲劳", "黏弹损伤")) {
                    fetchDirectStretchingFatigueTestData();
                } else if (containsKeyword(taskName, "沥青混合料四点弯曲疲劳寿命试验", "四点弯曲")) {
                    fetchFourPointBendingFatigueTestData();
                } else if (containsKeyword(taskName, "沥青混合料单轴压缩试验", "单轴压缩")) {
                    fetchUniaxialCompressionTestData();
                } else if (containsKeyword(taskName, "劈裂", "劈裂试验")) {
                    fetchSplittingTestData();
                } else {
                    // 根据关键字组合尝试确定实验类型
                    determineExperimentTypeAndFetchData(taskName);
                }
            } else {
                tvExperimentName.setText("任务指派: 未知");
                // 尝试获取所有类型的实验数据
                tryFetchAllExperimentTypes();
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
                        
                        // 检查任务指派是否包含"马歇尔"、"汉堡车辙"或"弯曲"关键词
                        if (containsKeyword(taskAssignment, "马歇尔", "Marshall", "稳定度")) {
                            fetchMarshallTestData();
                        } else if (containsKeyword(taskAssignment, "汉堡车辙", "Hamburg", "车辙")) {
                            fetchHamburgRuttingTestData();
                        } else if (containsKeyword(taskAssignment, "沥青混合料弯曲实验", "弯曲")) {
                            fetchMixtureBendingTestData();
                        } else if (containsKeyword(taskAssignment, "动态模量")) {
                            fetchDynamicModulusTestData();
                        } else if (containsKeyword(taskAssignment, "直接拉伸循环疲劳", "疲劳", "黏弹损伤")) {
                            fetchDirectStretchingFatigueTestData();
                        } else if (containsKeyword(taskAssignment, "沥青混合料四点弯曲疲劳寿命试验", "四点弯曲")) {
                            fetchFourPointBendingFatigueTestData();
                        } else if (containsKeyword(taskAssignment, "沥青混合料单轴压缩试验", "单轴压缩")) {
                            fetchUniaxialCompressionTestData();
                        } else if (containsKeyword(taskAssignment, "劈裂", "劈裂试验")) {
                            fetchSplittingTestData();
                        } else {
                            showNoResultsMessage("当前任务不包含支持的实验数据");
                        }
                    } else {
                        Log.w(TAG, "获取到的任务指派信息为空");
                        showNoResultsMessage("未知任务类型");
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
    
    /**
     * 获取马歇尔稳定度实验数据
     */
    private void fetchMarshallTestData() {
        // 确保taskId不为空
        String taskId = task != null ? task.getTaskId() : getIntent().getStringExtra("task_id");
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取马歇尔稳定度实验数据");
            showNoResultsMessage("无法获取马歇尔实验数据：任务ID为空");
            return;
        }
        
        // 增加更详细的日志，记录准确的任务ID
        Log.d(TAG, "准备查询马歇尔实验数据，完整任务ID: " + taskId);
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 调用API获取马歇尔稳定度实验数据
        Call<List<MarshallTestResponse>> call = mixtureTaskApi.getMarshallTestByTaskId(taskId);
        call.enqueue(new Callback<List<MarshallTestResponse>>() {
            @Override
            public void onResponse(Call<List<MarshallTestResponse>> call, Response<List<MarshallTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MarshallTestResponse> marshallTests = response.body();
                    
                    if (marshallTests.isEmpty()) {
                        Log.w(TAG, "未找到马歇尔实验数据，尝试替代查询方式");
                        
                        // 尝试使用不同格式的任务ID
                        if (taskId.contains("-")) {
                            // 1. 尝试去除末尾的"-0"后缀
                            String altTaskId = taskId;
                            if (taskId.endsWith("-0")) {
                                altTaskId = taskId.substring(0, taskId.length() - 2);
                                Log.d(TAG, "尝试使用替代任务ID (去除-0后缀): " + altTaskId);
                                
                                // 尝试用替代ID重新查询
                                tryMarshallTestWithAltId(altTaskId);
                                return;
                            }
                        }
                        
                        // 如果没有尝试替代ID或尝试失败，则继续尝试从新API获取数据
                        Log.w(TAG, "尝试从新API获取马歇尔实验数据");
                        fetchMarshallTestDataFromNewAPI(taskId);
                        return;
                    }
                    
                    Log.d(TAG, "成功获取马歇尔稳定度实验数据: " + marshallTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (marshallAdapter == null) {
                        marshallAdapter = new MarshallAdapter(MixtureTaskResultActivity.this, marshallTests);
                        rvExperimentResults.setAdapter(marshallAdapter);
                    } else {
                        marshallAdapter.updateData(marshallTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取马歇尔稳定度实验数据失败: " + response.code());
                    // 尝试从新API获取数据
                    fetchMarshallTestDataFromNewAPI(taskId);
                }
            }
            
            @Override
            public void onFailure(Call<List<MarshallTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取马歇尔稳定度实验数据请求失败", t);
                // 尝试从新API获取数据
                fetchMarshallTestDataFromNewAPI(taskId);
            }
        });
    }
    
    /**
     * 使用替代任务ID尝试获取马歇尔实验数据
     */
    private void tryMarshallTestWithAltId(String altTaskId) {
        Call<List<MarshallTestResponse>> call = mixtureTaskApi.getMarshallTestByTaskId(altTaskId);
        call.enqueue(new Callback<List<MarshallTestResponse>>() {
            @Override
            public void onResponse(Call<List<MarshallTestResponse>> call, Response<List<MarshallTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MarshallTestResponse> marshallTests = response.body();
                    
                    if (marshallTests.isEmpty()) {
                        Log.w(TAG, "使用替代任务ID仍未找到马歇尔实验数据，尝试从新API获取");
                        // 如果替代ID查询仍然失败，尝试从新API获取数据
                        fetchMarshallTestDataFromNewAPI(altTaskId);
                        return;
                    }
                    
                    Log.d(TAG, "使用替代任务ID成功获取马歇尔稳定度实验数据: " + marshallTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (marshallAdapter == null) {
                        marshallAdapter = new MarshallAdapter(MixtureTaskResultActivity.this, marshallTests);
                        rvExperimentResults.setAdapter(marshallAdapter);
                    } else {
                        marshallAdapter.updateData(marshallTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "使用替代任务ID获取马歇尔稳定度实验数据失败: " + response.code());
                    // 尝试从新API获取数据
                    fetchMarshallTestDataFromNewAPI(altTaskId);
                }
            }
            
            @Override
            public void onFailure(Call<List<MarshallTestResponse>> call, Throwable t) {
                Log.e(TAG, "使用替代任务ID获取马歇尔稳定度实验数据请求失败", t);
                // 尝试从新API获取数据
                fetchMarshallTestDataFromNewAPI(altTaskId);
            }
        });
    }
    
    /**
     * 从新API获取马歇尔稳定度实验数据
     */
    private void fetchMarshallTestDataFromNewAPI(String taskId) {
        // 尝试使用新的动态模量实验数据API结构获取数据
        Call<ApiResponse<List<DynamicModulusTestResponse>>> call = mixtureTaskApi.getDynamicModulusTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DynamicModulusTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Response<ApiResponse<List<DynamicModulusTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DynamicModulusTestResponse> dynamicModulusTests = response.body().getData();
                    
                    if (dynamicModulusTests.isEmpty()) {
                        Log.w(TAG, "未找到马歇尔实验数据");
                        showNoResultsMessage("未找到马歇尔实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取马歇尔实验数据(通过动态模量API): " + dynamicModulusTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (dynamicModulusAdapter == null) {
                        dynamicModulusAdapter = new DynamicModulusAdapter(MixtureTaskResultActivity.this, dynamicModulusTests);
                        rvExperimentResults.setAdapter(dynamicModulusAdapter);
                    } else {
                        dynamicModulusAdapter.updateData(dynamicModulusTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取马歇尔实验数据失败(通过新API): " + response.code());
                    showNoResultsMessage("未找到马歇尔实验数据");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取马歇尔实验数据请求失败(通过新API)", t);
                showNoResultsMessage("未找到马歇尔实验数据");
            }
        });
    }
    
    /**
     * 获取汉堡车辙实验数据
     */
    private void fetchHamburgRuttingTestData() {
        // 确保taskId不为空
        String taskId = task != null ? task.getTaskId() : getIntent().getStringExtra("task_id");
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取汉堡车辙实验数据");
            showNoResultsMessage("无法获取汉堡车辙实验数据：任务ID为空");
            return;
        }
        
        // 增加更详细的日志，记录准确的任务ID
        Log.d(TAG, "准备查询汉堡车辙实验数据，完整任务ID: " + taskId);
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 调用API获取汉堡车辙实验数据
        Call<List<HamburgRuttingTestResponse>> call = mixtureTaskApi.getHamburgRuttingTestByTaskId(taskId);
        call.enqueue(new Callback<List<HamburgRuttingTestResponse>>() {
            @Override
            public void onResponse(Call<List<HamburgRuttingTestResponse>> call, Response<List<HamburgRuttingTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HamburgRuttingTestResponse> hamburgTests = response.body();
                    
                    if (hamburgTests.isEmpty()) {
                        Log.w(TAG, "未找到汉堡车辙实验数据，尝试替代查询方式");
                        
                        // 尝试使用不同格式的任务ID
                        if (taskId.contains("-")) {
                            // 1. 尝试去除末尾的"-0"后缀
                            String altTaskId = taskId;
                            if (taskId.endsWith("-0")) {
                                altTaskId = taskId.substring(0, taskId.length() - 2);
                                Log.d(TAG, "尝试使用替代任务ID (去除-0后缀): " + altTaskId);
                                
                                // 尝试用替代ID重新查询
                                tryHamburgRuttingTestWithAltId(altTaskId);
                                return;
                            }
                        }
                        
                        // 如果没有尝试替代ID或尝试失败，则继续尝试从新API获取数据
                        Log.w(TAG, "尝试从新API获取汉堡车辙实验数据");
                        fetchHamburgRuttingTestDataFromNewAPI(taskId);
                        return;
                    }
                    
                    Log.d(TAG, "成功获取汉堡车辙实验数据: " + hamburgTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (hamburgRuttingAdapter == null) {
                        hamburgRuttingAdapter = new HamburgRuttingAdapter(MixtureTaskResultActivity.this, hamburgTests);
                        rvExperimentResults.setAdapter(hamburgRuttingAdapter);
                    } else {
                        hamburgRuttingAdapter.updateData(hamburgTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取汉堡车辙实验数据失败: " + response.code());
                    // 尝试从新API获取数据
                    fetchHamburgRuttingTestDataFromNewAPI(taskId);
                }
            }
            
            @Override
            public void onFailure(Call<List<HamburgRuttingTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取汉堡车辙实验数据请求失败", t);
                // 尝试从新API获取数据
                fetchHamburgRuttingTestDataFromNewAPI(taskId);
            }
        });
    }
    
    /**
     * 使用替代任务ID尝试获取汉堡车辙实验数据
     */
    private void tryHamburgRuttingTestWithAltId(String altTaskId) {
        Call<List<HamburgRuttingTestResponse>> call = mixtureTaskApi.getHamburgRuttingTestByTaskId(altTaskId);
        call.enqueue(new Callback<List<HamburgRuttingTestResponse>>() {
            @Override
            public void onResponse(Call<List<HamburgRuttingTestResponse>> call, Response<List<HamburgRuttingTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HamburgRuttingTestResponse> hamburgTests = response.body();
                    
                    if (hamburgTests.isEmpty()) {
                        Log.w(TAG, "使用替代任务ID仍未找到汉堡车辙实验数据，尝试从新API获取");
                        // 如果替代ID查询仍然失败，尝试从新API获取数据
                        fetchHamburgRuttingTestDataFromNewAPI(altTaskId);
                        return;
                    }
                    
                    Log.d(TAG, "使用替代任务ID成功获取汉堡车辙实验数据: " + hamburgTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (hamburgRuttingAdapter == null) {
                        hamburgRuttingAdapter = new HamburgRuttingAdapter(MixtureTaskResultActivity.this, hamburgTests);
                        rvExperimentResults.setAdapter(hamburgRuttingAdapter);
                    } else {
                        hamburgRuttingAdapter.updateData(hamburgTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "使用替代任务ID获取汉堡车辙实验数据失败: " + response.code());
                    // 尝试从新API获取数据
                    fetchHamburgRuttingTestDataFromNewAPI(altTaskId);
                }
            }
            
            @Override
            public void onFailure(Call<List<HamburgRuttingTestResponse>> call, Throwable t) {
                Log.e(TAG, "使用替代任务ID获取汉堡车辙实验数据请求失败", t);
                // 尝试从新API获取数据
                fetchHamburgRuttingTestDataFromNewAPI(altTaskId);
            }
        });
    }
    
    /**
     * 从新API获取汉堡车辙实验数据
     */
    private void fetchHamburgRuttingTestDataFromNewAPI(String taskId) {
        // 尝试使用新的动态模量实验数据API结构获取数据
        Call<ApiResponse<List<DynamicModulusTestResponse>>> call = mixtureTaskApi.getDynamicModulusTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DynamicModulusTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Response<ApiResponse<List<DynamicModulusTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DynamicModulusTestResponse> dynamicModulusTests = response.body().getData();
                    
                    if (dynamicModulusTests.isEmpty()) {
                        Log.w(TAG, "未找到汉堡车辙实验数据");
                        showNoResultsMessage("未找到汉堡车辙实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取汉堡车辙实验数据(通过动态模量API): " + dynamicModulusTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (dynamicModulusAdapter == null) {
                        dynamicModulusAdapter = new DynamicModulusAdapter(MixtureTaskResultActivity.this, dynamicModulusTests);
                        rvExperimentResults.setAdapter(dynamicModulusAdapter);
                    } else {
                        dynamicModulusAdapter.updateData(dynamicModulusTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取汉堡车辙实验数据失败(通过新API): " + response.code());
                    showNoResultsMessage("未找到汉堡车辙实验数据");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取汉堡车辙实验数据请求失败(通过新API)", t);
                showNoResultsMessage("未找到汉堡车辙实验数据");
            }
        });
    }
    
    /**
     * 获取混合料弯曲试验数据
     */
    private void fetchMixtureBendingTestData() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取混合料弯曲试验数据");
            showNoResultsMessage("无法获取混合料弯曲试验数据：任务ID为空");
            return;
        }
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 调用API获取混合料弯曲试验数据
        Call<List<MixtureBendingTestResponse>> call = mixtureTaskApi.getMixtureBendingTestByTaskId(taskId);
        call.enqueue(new Callback<List<MixtureBendingTestResponse>>() {
            @Override
            public void onResponse(Call<List<MixtureBendingTestResponse>> call, Response<List<MixtureBendingTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MixtureBendingTestResponse> mixtureBendingTests = response.body();
                    
                    if (mixtureBendingTests.isEmpty()) {
                        Log.w(TAG, "未找到混合料弯曲试验数据");
                        showNoResultsMessage("未找到混合料弯曲试验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取混合料弯曲试验数据: " + mixtureBendingTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (mixtureBendingAdapter == null) {
                        mixtureBendingAdapter = new MixtureBendingAdapter(MixtureTaskResultActivity.this, mixtureBendingTests);
                        rvExperimentResults.setAdapter(mixtureBendingAdapter);
                    } else {
                        mixtureBendingAdapter.updateData(mixtureBendingTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取混合料弯曲试验数据失败: " + response.code());
                    showNoResultsMessage("获取混合料弯曲试验数据失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<MixtureBendingTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取混合料弯曲试验数据请求失败", t);
                showNoResultsMessage("获取混合料弯曲试验数据失败: 网络错误");
            }
        });
    }
    
    /**
     * 获取动态模量实验数据
     */
    private void fetchDynamicModulusTestData() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取动态模量实验数据");
            showNoResultsMessage("无法获取动态模量实验数据：任务ID为空");
            return;
        }
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 调用API获取动态模量实验数据
        Call<ApiResponse<List<DynamicModulusTestResponse>>> call = mixtureTaskApi.getDynamicModulusTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DynamicModulusTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Response<ApiResponse<List<DynamicModulusTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DynamicModulusTestResponse> dynamicModulusTests = response.body().getData();
                    
                    if (dynamicModulusTests.isEmpty()) {
                        Log.w(TAG, "未找到动态模量实验数据");
                        showNoResultsMessage("未找到动态模量实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取动态模量实验数据: " + dynamicModulusTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (dynamicModulusAdapter == null) {
                        dynamicModulusAdapter = new DynamicModulusAdapter(MixtureTaskResultActivity.this, dynamicModulusTests);
                        rvExperimentResults.setAdapter(dynamicModulusAdapter);
                    } else {
                        dynamicModulusAdapter.updateData(dynamicModulusTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取动态模量实验数据失败: " + response.code());
                    showNoResultsMessage("获取动态模量实验数据失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取动态模量实验数据请求失败", t);
                showNoResultsMessage("获取动态模量实验数据失败: 网络错误");
            }
        });
    }
    
    /**
     * 获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
     */
    private void fetchDirectStretchingFatigueTestData() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据");
            showNoResultsMessage("无法获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据：任务ID为空");
            return;
        }
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 调用API获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
        Call<ApiResponse<List<DirectStretchingFatigueTestResponse>>> call = mixtureTaskApi.getDirectStretchingFatigueTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DirectStretchingFatigueTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DirectStretchingFatigueTestResponse>>> call, Response<ApiResponse<List<DirectStretchingFatigueTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DirectStretchingFatigueTestResponse> directStretchingFatigueTests = response.body().getData();
                    
                    if (directStretchingFatigueTests.isEmpty()) {
                        Log.w(TAG, "未找到沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据");
                        showNoResultsMessage("未找到沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据: " + directStretchingFatigueTests.size() + " 条");
                    
                    // 收集所有试件数据
                    List<DirectStretchingFatigueTestResponse.Specimen> allSpecimens = new ArrayList<>();
                    for (DirectStretchingFatigueTestResponse test : directStretchingFatigueTests) {
                        if (test.getSpecimens() != null && !test.getSpecimens().isEmpty()) {
                            allSpecimens.addAll(test.getSpecimens());
                        }
                    }
                    
                    if (allSpecimens.isEmpty()) {
                        Log.w(TAG, "未找到沥青混合料直接拉伸循环疲劳测黏弹损伤实验的试件数据");
                        showNoResultsMessage("未找到沥青混合料直接拉伸循环疲劳测黏弹损伤实验的试件数据");
                        return;
                    }
                    
                    // 初始化或更新适配器，直接使用试件适配器
                    DirectStretchingFatigueSpecimenAdapter specimenAdapter = new DirectStretchingFatigueSpecimenAdapter(
                            MixtureTaskResultActivity.this, allSpecimens);
                    rvExperimentResults.setAdapter(specimenAdapter);
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: " + response.code());
                    showNoResultsMessage("获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DirectStretchingFatigueTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据请求失败", t);
                showNoResultsMessage("获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: 网络错误");
            }
        });
    }
    
    /**
     * 获取沥青混合料四点弯曲疲劳寿命实验数据
     */
    private void fetchFourPointBendingFatigueTestData() {
        // 确保taskId不为空
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取四点弯曲疲劳寿命实验数据");
            showNoResultsMessage("无法获取四点弯曲疲劳寿命实验数据：任务ID为空");
            return;
        }
        
        // 隐藏无结果提示，准备显示数据
        tvNoResults.setVisibility(View.GONE);
        
        // 记录详细日志，便于调试
        Log.d(TAG, "开始获取四点弯曲疲劳寿命实验数据，使用任务ID: " + taskId);
        
        // 使用已有的mixtureTaskApi实例，而不是创建新的AsphaltTaskApi实例
        Call<ApiResponse<FourPointBendingFatigueTestResponse>> call = mixtureTaskApi.getFourPointBendingTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<FourPointBendingFatigueTestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FourPointBendingFatigueTestResponse>> call, Response<ApiResponse<FourPointBendingFatigueTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    FourPointBendingFatigueTestResponse testData = response.body().getData();
                    
                    if (testData.getSpecimens() == null || testData.getSpecimens().isEmpty()) {
                        Log.w(TAG, "未找到四点弯曲疲劳寿命实验数据，尝试使用替代任务ID");
                        
                        // 提取任务ID前缀，去掉可能的后缀
                        if (taskId.contains("-")) {
                            String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                            Log.d(TAG, "尝试使用任务ID前缀: " + taskIdPrefix);
                            
                            // 尝试使用前缀 + "-0" 后缀
                            String altTaskId = taskIdPrefix + "-0";
                            Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                            
                            // 使用替代ID重新查询
                            tryFourPointBendingWithAltId(altTaskId);
                            return;
                        } else {
                            // 没有后缀，显示没有找到数据的消息
                            Log.w(TAG, "未找到四点弯曲疲劳寿命实验数据，且任务ID没有后缀可供替换");
                            showNoResultsMessage("未找到四点弯曲疲劳寿命实验数据");
                            return;
                        }
                    }
                    
                    Log.d(TAG, "成功获取四点弯曲疲劳寿命实验数据: " + testData.getSpecimens().size() + " 个试件");
                    
                    // 创建包含单个测试数据的列表
                    List<FourPointBendingFatigueTestResponse> testDataList = new ArrayList<>();
                    testDataList.add(testData);
                    
                    // 初始化或更新适配器
                    if (fourPointBendingFatigueAdapter == null) {
                        fourPointBendingFatigueAdapter = new FourPointBendingFatigueAdapter(MixtureTaskResultActivity.this);
                        rvExperimentResults.setAdapter(fourPointBendingFatigueAdapter);
                    }
                    
                    fourPointBendingFatigueAdapter.updateData(testDataList);
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取四点弯曲疲劳寿命实验数据失败，尝试使用替代任务ID");
                    
                    // 提取任务ID前缀，去掉可能的后缀
                    if (taskId.contains("-")) {
                        String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                        Log.d(TAG, "尝试使用任务ID前缀: " + taskIdPrefix);
                        
                        // 尝试使用前缀 + "-0" 后缀
                        String altTaskId = taskIdPrefix + "-0";
                        Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                        
                        // 使用替代ID重新查询
                        tryFourPointBendingWithAltId(altTaskId);
                        return;
                    } else {
                        String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                        Log.e(TAG, "获取四点弯曲疲劳寿命实验数据失败: " + errorMessage);
                        showNoResultsMessage("获取四点弯曲疲劳寿命实验数据失败: " + errorMessage);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<FourPointBendingFatigueTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取四点弯曲疲劳寿命实验数据请求失败", t);
                
                // 提取任务ID前缀，去掉可能的后缀
                if (taskId.contains("-")) {
                    String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                    Log.d(TAG, "网络请求失败，尝试使用任务ID前缀: " + taskIdPrefix);
                    
                    // 尝试使用前缀 + "-0" 后缀
                    String altTaskId = taskIdPrefix + "-0";
                    Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                    
                    // 使用替代ID重新查询
                    tryFourPointBendingWithAltId(altTaskId);
                } else {
                    showNoResultsMessage("获取四点弯曲疲劳寿命实验数据失败: 网络错误");
                }
            }
        });
    }
    
    /**
     * 使用替代任务ID尝试获取四点弯曲疲劳寿命实验数据
     */
    private void tryFourPointBendingWithAltId(String altTaskId) {
        Call<ApiResponse<FourPointBendingFatigueTestResponse>> call = mixtureTaskApi.getFourPointBendingTestByTaskId(altTaskId);
        call.enqueue(new Callback<ApiResponse<FourPointBendingFatigueTestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FourPointBendingFatigueTestResponse>> call, Response<ApiResponse<FourPointBendingFatigueTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    FourPointBendingFatigueTestResponse testData = response.body().getData();
                    
                    if (testData.getSpecimens() == null || testData.getSpecimens().isEmpty()) {
                        Log.w(TAG, "使用替代任务ID仍未找到四点弯曲疲劳寿命实验数据");
                        showNoResultsMessage("未找到四点弯曲疲劳寿命实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "使用替代任务ID成功获取四点弯曲疲劳寿命实验数据: " + testData.getSpecimens().size() + " 个试件");
                    
                    // 创建包含单个测试数据的列表
                    List<FourPointBendingFatigueTestResponse> testDataList = new ArrayList<>();
                    testDataList.add(testData);
                    
                    // 初始化或更新适配器
                    if (fourPointBendingFatigueAdapter == null) {
                        fourPointBendingFatigueAdapter = new FourPointBendingFatigueAdapter(MixtureTaskResultActivity.this);
                        rvExperimentResults.setAdapter(fourPointBendingFatigueAdapter);
                    }
                    
                    fourPointBendingFatigueAdapter.updateData(testDataList);
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                    Log.e(TAG, "使用替代任务ID获取四点弯曲疲劳寿命实验数据失败: " + errorMessage);
                    showNoResultsMessage("获取四点弯曲疲劳寿命实验数据失败: " + errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<FourPointBendingFatigueTestResponse>> call, Throwable t) {
                Log.e(TAG, "使用替代任务ID获取四点弯曲疲劳寿命实验数据请求失败", t);
                showNoResultsMessage("获取四点弯曲疲劳寿命实验数据失败: 网络错误");
            }
        });
    }
    
    /**
     * 获取沥青混合料单轴压缩试验（圆柱体法）数据
     */
    private void fetchUniaxialCompressionTestData() {
        String taskId = task != null ? task.getTaskId() : getIntent().getStringExtra("task_id");
        if (taskId == null || taskId.isEmpty()) {
            showNoResultsMessage("无法获取任务ID");
            return;
        }

        Log.d(TAG, "开始获取沥青混合料单轴压缩试验数据，任务ID: " + taskId);
        mixtureTaskApi.getUniaxialCompressionTestByTaskId(taskId).enqueue(new Callback<ApiResponse<UniaxialCompressionTestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UniaxialCompressionTestResponse>> call, Response<ApiResponse<UniaxialCompressionTestResponse>> response) {
                Log.d(TAG, "收到API响应: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                
                // 详细记录API响应内容
                if (response.body() != null) {
                    Log.d(TAG, "响应成功状态: " + response.body().isSuccess() + ", 消息: " + response.body().getMessage());
                    
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        UniaxialCompressionTestResponse testData = response.body().getData();
                        Log.d(TAG, "测试日期: " + testData.getTestDate() + 
                                ", 操作员: " + testData.getOperator() + 
                                ", 测试温度: " + testData.getTestTemperature());
                        
                        List<UniaxialCompressionTestResponse.SpecimenData> specimens = testData.getSpecimens();
                        Log.d(TAG, "试件数量: " + (specimens != null ? specimens.size() : "null"));
                        
                        if (specimens != null && !specimens.isEmpty()) {
                            // 记录第一个试件的详细信息
                            UniaxialCompressionTestResponse.SpecimenData firstSpecimen = specimens.get(0);
                            Log.d(TAG, "第一个试件 - 编号: " + firstSpecimen.getSpecimenNumber() + 
                                    ", 直径: " + firstSpecimen.getDiameterMm() + 
                                    ", 高度: " + firstSpecimen.getHeightMm());
                            
                            // 强制处理每个试件的UTM数据
                            for (UniaxialCompressionTestResponse.SpecimenData specimen : specimens) {
                                // 不再显式调用processUtmData，依靠getUtmDataList的安全机制
                                // specimen.processUtmData();
                                
                                // 记录UTM数据列表
                                if (specimen.getUtmDataList() != null) {
                                    Log.d(TAG, "试件 " + specimen.getSpecimenNumber() + 
                                           " 的UTM数据数量: " + specimen.getUtmDataList().size());
                                    
                                    for (UniaxialCompressionTestResponse.SpecimenData.UtmData utmData : specimen.getUtmDataList()) {
                                        Log.d(TAG, "压力级别: " + utmData.getPressureLevel() + 
                                               ", 最大力: " + utmData.getMaxForceKn() + 
                                               ", 弹性模量: " + utmData.getResilientModulusMpa());
                                    }
                                } else {
                                    Log.d(TAG, "试件 " + specimen.getSpecimenNumber() + " 的UTM数据为空");
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "响应体中的数据为空");
                    }
                } else {
                    Log.d(TAG, "响应体为空");
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    UniaxialCompressionTestResponse testData = response.body().getData();
                    
                    // 确保所有试件的UTM数据都被处理
                    if (testData.getSpecimens() != null) {
                        for (UniaxialCompressionTestResponse.SpecimenData specimen : testData.getSpecimens()) {
                            // 恢复这行代码，确保数据被处理一次
                            specimen.processUtmData();
                            
                            // 记录处理后的UTM数据
                            Log.d(TAG, "处理后的试件 " + specimen.getSpecimenNumber() + 
                                  " 的UTM数据数量: " + specimen.getUtmDataList().size());
                        }
                    }
                    
                    // 更新UI
                    Log.d(TAG, "处理后的数据: 试件数量=" + 
                           (testData.getSpecimens() != null ? testData.getSpecimens().size() : 0));
                    
                    if (testData.getSpecimens() != null && !testData.getSpecimens().isEmpty()) {
                        UniaxialCompressionTestResponse.SpecimenData firstSpecimen = testData.getSpecimens().get(0);
                        Log.d(TAG, "第一个试件直径=" + firstSpecimen.getDiameterMm() + 
                               ", 高度=" + firstSpecimen.getHeightMm() + 
                               ", UTM数据数量=" + firstSpecimen.getUtmDataList().size());
                    }
                    
                    rvExperimentResults.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.GONE);
                    
                    List<UniaxialCompressionTestResponse> dataList = new ArrayList<>();
                    dataList.add(testData);
                    
                    UniaxialCompressionAdapter adapter = new UniaxialCompressionAdapter(MixtureTaskResultActivity.this);
                    adapter.updateData(dataList);
                    rvExperimentResults.setAdapter(adapter);
                } else {
                    String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                    Log.w(TAG, "获取沥青混合料单轴压缩试验（圆柱体法）数据失败: " + errorMessage);
                    showNoResultsMessage("获取沥青混合料单轴压缩试验（圆柱体法）数据失败: " + errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<UniaxialCompressionTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取沥青混合料单轴压缩试验（圆柱体法）数据请求失败", t);
                showNoResultsMessage("获取沥青混合料单轴压缩试验（圆柱体法）数据失败: 网络错误");
            }
        });
    }
    
    /**
     * 获取沥青混合料劈裂试验数据
     */
    private void fetchSplittingTestData() {
        String taskId = task != null ? task.getTaskId() : getIntent().getStringExtra("task_id");
        if (taskId == null || taskId.isEmpty()) {
            showNoResultsMessage("无法获取任务ID");
            return;
        }

        Log.d(TAG, "开始获取沥青混合料劈裂试验数据，任务ID: " + taskId);
        mixtureTaskApi.getSplittingTestByTaskId(taskId).enqueue(new Callback<ApiResponse<List<SplittingTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SplittingTestResponse>>> call, Response<ApiResponse<List<SplittingTestResponse>>> response) {
                Log.d(TAG, "劈裂试验API响应状态码: " + response.code());
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "劈裂试验API响应成功");
                    if (response.body() != null) {
                        Log.d(TAG, "劈裂试验API响应体不为空，成功状态: " + response.body().isSuccess() + ", 消息: " + response.body().getMessage());
                        
                        if (response.body().isSuccess() && response.body().getData() != null) {
                            List<SplittingTestResponse> testResults = response.body().getData();
                            Log.d(TAG, "劈裂试验数据不为空，包含" + testResults.size() + "条记录");
                            
                            if (!testResults.isEmpty()) {
                                // 记录第一条数据的关键字段，帮助调试
                                SplittingTestResponse firstItem = testResults.get(0);
                                Log.d(TAG, "第一条劈裂试验数据: taskId=" + firstItem.getTaskId() 
                                    + ", testId=" + firstItem.getTestId()
                                    + ", 温度=" + firstItem.getTestTemperature()
                                    + ", 操作员=" + firstItem.getOperator());
                                
                                if (firstItem.getSpecimens() != null) {
                                    Log.d(TAG, "第一条数据包含" + firstItem.getSpecimens().size() + "个试件");
                                } else {
                                    Log.d(TAG, "第一条数据的试件列表为null");
                                }
                                
                                Log.d(TAG, "成功获取沥青混合料劈裂试验数据: " + testResults.size() + "条");
                                displaySplittingTestResults(testResults);
                            } else {
                                Log.w(TAG, "劈裂试验数据列表为空，尝试使用替代任务ID");
                                
                                // 尝试使用任务ID前缀
                                if (taskId.contains("-")) {
                                    String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                                    Log.d(TAG, "尝试使用任务ID前缀: " + taskIdPrefix);
                                    
                                    // 尝试使用前缀 + "-0" 后缀
                                    String altTaskId = taskIdPrefix + "-0";
                                    Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                                    
                                    // 使用替代ID重新查询
                                    trySplittingTestWithAltId(altTaskId);
                                } else {
                                    Log.w(TAG, "劈裂试验数据列表为空，且任务ID没有后缀可供替换");
                                    showNoResultsMessage("未找到劈裂试验数据");
                                }
                            }
                        } else {
                            Log.w(TAG, "劈裂试验API返回失败或数据为null");
                            
                            // 尝试使用任务ID前缀
                            if (taskId.contains("-")) {
                                String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                                Log.d(TAG, "API返回失败，尝试使用任务ID前缀: " + taskIdPrefix);
                                
                                // 尝试使用前缀 + "-0" 后缀
                                String altTaskId = taskIdPrefix + "-0";
                                Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                                
                                // 使用替代ID重新查询
                                trySplittingTestWithAltId(altTaskId);
                            } else {
                                showNoResultsMessage("获取劈裂试验数据失败: " + 
                                    (response.body() != null ? response.body().getMessage() : "返回数据为空"));
                            }
                        }
                    } else {
                        Log.w(TAG, "劈裂试验API响应体为空，尝试使用替代任务ID");
                        
                        // 尝试使用任务ID前缀
                        if (taskId.contains("-")) {
                            String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                            Log.d(TAG, "响应体为空，尝试使用任务ID前缀: " + taskIdPrefix);
                            
                            // 尝试使用前缀 + "-0" 后缀
                            String altTaskId = taskIdPrefix + "-0";
                            Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                            
                            // 使用替代ID重新查询
                            trySplittingTestWithAltId(altTaskId);
                        } else {
                            showNoResultsMessage("获取劈裂试验数据失败，服务器返回为空");
                        }
                    }
                } else {
                    Log.e(TAG, "劈裂试验API响应失败，状态码: " + response.code());
                    
                    // 尝试使用任务ID前缀
                    if (taskId.contains("-")) {
                        String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                        Log.d(TAG, "API响应失败，尝试使用任务ID前缀: " + taskIdPrefix);
                        
                        // 尝试使用前缀 + "-0" 后缀
                        String altTaskId = taskIdPrefix + "-0";
                        Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                        
                        // 使用替代ID重新查询
                        trySplittingTestWithAltId(altTaskId);
                    } else {
                        if (response.body() != null) {
                            showNoResultsMessage("获取劈裂试验数据失败: " + response.body().getMessage());
                        } else {
                            showNoResultsMessage("获取劈裂试验数据失败，服务器返回为空");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SplittingTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取劈裂试验数据网络请求失败", t);
                
                // 尝试使用任务ID前缀
                if (taskId.contains("-")) {
                    String taskIdPrefix = taskId.substring(0, taskId.lastIndexOf("-"));
                    Log.d(TAG, "网络请求失败，尝试使用任务ID前缀: " + taskIdPrefix);
                    
                    // 尝试使用前缀 + "-0" 后缀
                    String altTaskId = taskIdPrefix + "-0";
                    Log.d(TAG, "尝试使用替代任务ID: " + altTaskId);
                    
                    // 使用替代ID重新查询
                    trySplittingTestWithAltId(altTaskId);
                } else {
                    showNoResultsMessage("网络请求失败: " + t.getMessage());
                }
            }
        });
    }
    
    /**
     * 使用替代任务ID尝试获取劈裂试验数据
     */
    private void trySplittingTestWithAltId(String altTaskId) {
        Log.d(TAG, "使用替代任务ID获取劈裂试验数据: " + altTaskId);
        mixtureTaskApi.getSplittingTestByTaskId(altTaskId).enqueue(new Callback<ApiResponse<List<SplittingTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SplittingTestResponse>>> call, Response<ApiResponse<List<SplittingTestResponse>>> response) {
                Log.d(TAG, "使用替代ID的劈裂试验API响应状态码: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<SplittingTestResponse> testResults = response.body().getData();
                    
                    if (!testResults.isEmpty()) {
                        Log.d(TAG, "使用替代任务ID成功获取劈裂试验数据: " + testResults.size() + "条");
                        displaySplittingTestResults(testResults);
                    } else {
                        Log.w(TAG, "使用替代任务ID获取的劈裂试验数据列表为空");
                        showNoResultsMessage("未找到劈裂试验数据");
                    }
                } else {
                    Log.e(TAG, "使用替代任务ID获取劈裂试验数据失败");
                    String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                    showNoResultsMessage("获取劈裂试验数据失败: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SplittingTestResponse>>> call, Throwable t) {
                Log.e(TAG, "使用替代任务ID获取劈裂试验数据网络请求失败", t);
                showNoResultsMessage("网络请求失败: " + t.getMessage());
            }
        });
    }
    /**
     * 显示沥青混合料劈裂试验结果数据
     * 
     * @param testResults 劈裂试验结果列表
     */
    private void displaySplittingTestResults(List<SplittingTestResponse> testResults) {
        tvNoResults.setVisibility(View.GONE);
        rvExperimentResults.setVisibility(View.VISIBLE);
        
        splittingTestAdapter = new SplittingTestAdapter(this, testResults);
        rvExperimentResults.setAdapter(splittingTestAdapter);
        
        Log.d(TAG, "显示劈裂试验数据：" + testResults.size() + "条");
    }
    /**
     * 显示无结果提示信息
     */
    private void showNoResultsMessage(String message) {
        // 如果RecyclerView已经显示了数据，不要再显示错误信息
        if (rvExperimentResults.getVisibility() == View.VISIBLE && rvExperimentResults.getAdapter() != null) {
            Log.d(TAG, "已有实验数据显示，忽略错误信息: " + message);
            return;
        }
        
        rvExperimentResults.setVisibility(View.GONE);
        tvNoResults.setText(message);
        tvNoResults.setVisibility(View.VISIBLE);
        Log.d(TAG, "显示无结果提示: " + message);
    }
    private boolean containsKeyword(String text, String... keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    private void determineExperimentTypeAndFetchData(String taskAssignment) {
        // 根据关键字组合尝试确定实验类型
        if (containsKeyword(taskAssignment, "沥青混合料", "混合料")) {
            // 尝试获取沥青混合料相关实验数据
            fetchMixtureRelatedTestData();
        } else {
            // 如果无法确定实验类型，显示错误信息
            showNoResultsMessage("无法确定实验类型");
        }
    }
    private void fetchMixtureRelatedTestData() {
        // 尝试获取沥青混合料相关实验数据
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取沥青混合料相关实验数据");
            showNoResultsMessage("无法获取沥青混合料相关实验数据：任务ID为空");
            return;
        }
        
        // 调用API获取沥青混合料相关实验数据
        Call<ApiResponse<List<DynamicModulusTestResponse>>> call = mixtureTaskApi.getDynamicModulusTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DynamicModulusTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Response<ApiResponse<List<DynamicModulusTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DynamicModulusTestResponse> dynamicModulusTests = response.body().getData();
                    
                    if (dynamicModulusTests.isEmpty()) {
                        Log.w(TAG, "未找到沥青混合料相关实验数据");
                        showNoResultsMessage("未找到沥青混合料相关实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取沥青混合料相关实验数据: " + dynamicModulusTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (dynamicModulusAdapter == null) {
                        dynamicModulusAdapter = new DynamicModulusAdapter(MixtureTaskResultActivity.this, dynamicModulusTests);
                        rvExperimentResults.setAdapter(dynamicModulusAdapter);
                    } else {
                        dynamicModulusAdapter.updateData(dynamicModulusTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取沥青混合料相关实验数据失败: " + response.code());
                    showNoResultsMessage("获取沥青混合料相关实验数据失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取沥青混合料相关实验数据请求失败", t);
                showNoResultsMessage("获取沥青混合料相关实验数据失败: 网络错误");
            }
        });
    }
    private void tryFetchAllExperimentTypes() {
        // 尝试获取所有类型的实验数据
        String taskId = task.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "任务ID为空，无法获取实验数据");
            showNoResultsMessage("无法获取实验数据：任务ID为空");
            return;
        }
        
        // 调用API获取实验数据
        Call<ApiResponse<List<DynamicModulusTestResponse>>> call = mixtureTaskApi.getDynamicModulusTestByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<List<DynamicModulusTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Response<ApiResponse<List<DynamicModulusTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DynamicModulusTestResponse> dynamicModulusTests = response.body().getData();
                    
                    if (dynamicModulusTests.isEmpty()) {
                        Log.w(TAG, "未找到实验数据");
                        showNoResultsMessage("未找到实验数据");
                        return;
                    }
                    
                    Log.d(TAG, "成功获取实验数据: " + dynamicModulusTests.size() + " 条");
                    
                    // 初始化或更新适配器
                    if (dynamicModulusAdapter == null) {
                        dynamicModulusAdapter = new DynamicModulusAdapter(MixtureTaskResultActivity.this, dynamicModulusTests);
                        rvExperimentResults.setAdapter(dynamicModulusAdapter);
                    } else {
                        dynamicModulusAdapter.updateData(dynamicModulusTests);
                    }
                    
                    // 显示RecyclerView
                    rvExperimentResults.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "获取实验数据失败: " + response.code());
                    showNoResultsMessage("获取实验数据失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DynamicModulusTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取实验数据请求失败", t);
                showNoResultsMessage("获取实验数据失败: 网络错误");
            }
        });
    }
}
