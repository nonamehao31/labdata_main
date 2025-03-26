package com.example.labdata_main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.BbrTestAdapter;
import com.example.labdata_main.adapter.BrookfieldViscosityAdapter;
import com.example.labdata_main.adapter.DsrTestAdapter;
import com.example.labdata_main.adapter.DuctilityAdapter;
import com.example.labdata_main.adapter.PenetrationTestAdapter;
import com.example.labdata_main.adapter.SofteningPointAdapter;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.AsphaltTaskApi;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.AsphaltDetailResponse;
import com.example.labdata_main.model.BbrTestResponse;
import com.example.labdata_main.model.BrookfieldViscosityResponse;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.model.DsrTestResponse;
import com.example.labdata_main.model.DuctilityTestResponse;
import com.example.labdata_main.model.PenetrationTestResponse;
import com.example.labdata_main.model.SofteningPointResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsphaltTaskResultActivity extends AppCompatActivity {
    private static final String TAG = "AsphaltTaskResult";
    public static final String EXTRA_TASK = "extra_task";
    
    // UI组件
    private TextView tvTaskId;
    private TextView tvExperimentName;
    private TextView tvExperimenter;
    private TextView tvCompletionTime;
    private TextView tvResultTitle;
    private TextView tvNoResults;
    private RecyclerView rvExperimentResults;
    
    // 适配器
    private PenetrationTestAdapter penetrationTestAdapter;
    private SofteningPointAdapter softeningPointAdapter;
    private DuctilityAdapter ductilityAdapter;
    private BrookfieldViscosityAdapter brookfieldViscosityAdapter;
    private BbrTestAdapter bbrTestAdapter;
    private DsrTestAdapter dsrTestAdapter;
    
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
        tvNoResults = findViewById(R.id.tvNoResults);
        rvExperimentResults = findViewById(R.id.rvExperimentResults);
        
        // 初始化适配器
        setupRecyclerView();
        
        // 获取传递的任务数据
        if (getIntent().hasExtra(EXTRA_TASK)) {
            task = (CompletedExperimentTask) getIntent().getSerializableExtra(EXTRA_TASK);
            if (task != null) {
                displayTaskDetails();
                fetchAsphaltTaskDetails();
                
                // 根据任务指派类型加载相应的实验数据
                String taskAssignment = task.getTaskAssignment();
                if (taskAssignment != null) {
                    if (taskAssignment.contains("针入度")) {
                        // 获取针入度实验数据
                        fetchPenetrationTestResult();
                    } else if (taskAssignment.contains("软化点")) {
                        // 获取软化点实验数据
                        fetchSofteningPointResult();
                    } else if (taskAssignment.contains("延度")) {
                        // 获取延度实验数据
                        fetchDuctilityTestResult();
                    } else if (taskAssignment.contains("旋转黏度") || taskAssignment.contains("布鲁克菲尔德")) {
                        // 获取旋转黏度实验数据
                        fetchBrookfieldViscosityResult();
                    } else if (taskAssignment.contains("弯曲梁") || taskAssignment.contains("BBR") || taskAssignment.contains("流变仪")) {
                        // 获取弯曲梁流变仪实验数据
                        fetchBbrTestResult();
                    } else if (taskAssignment.contains("动态剪切") || taskAssignment.contains("DSR")) {
                        // 获取动态剪切流变仪实验数据
                        fetchDsrTestResult();
                    } else {
                        // 未知实验类型，显示无数据提示
                        Log.d(TAG, "未知实验类型，无法获取对应的实验数据");
                        showNoResultsMessage();
                    }
                } else {
                    // 任务指派类型未知，显示无数据提示
                    Log.d(TAG, "任务指派类型未知，无法获取对应的实验数据");
                    showNoResultsMessage();
                }
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
     * 初始化RecyclerView及适配器
     */
    private void setupRecyclerView() {
        rvExperimentResults.setLayoutManager(new LinearLayoutManager(this));
        
        // 针入度适配器
        penetrationTestAdapter = new PenetrationTestAdapter(this);
        
        // 软化点适配器
        softeningPointAdapter = new SofteningPointAdapter(this);
        
        // 延度适配器
        ductilityAdapter = new DuctilityAdapter(this);
        
        // 旋转黏度适配器
        brookfieldViscosityAdapter = new BrookfieldViscosityAdapter(this);

        // 弯曲梁流变仪适配器
        bbrTestAdapter = new BbrTestAdapter(this);
        
        // 动态剪切流变仪适配器
        dsrTestAdapter = new DsrTestAdapter(this);
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
    
    /**
     * 获取针入度实验结果数据
     */
    private void fetchPenetrationTestResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取针入度实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取针入度实验数据: " + taskId);
        
        asphaltTaskApi.getPenetrationTestByTaskId(taskId).enqueue(new Callback<ApiResponse<PenetrationTestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PenetrationTestResponse>> call, Response<ApiResponse<PenetrationTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    PenetrationTestResponse testResult = response.body().getData();
                    Log.d(TAG, "成功获取针入度实验数据");
                    
                    // 创建一个包含单个结果的列表，传递给适配器
                    List<PenetrationTestResponse> testResultList = new ArrayList<>();
                    testResultList.add(testResult);
                    displayPenetrationTestResults(testResultList);
                } else {
                    Log.e(TAG, "获取针入度实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是针入度实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("针入度")) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<PenetrationTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取针入度实验数据网络请求失败", t);
                // 任务指派明确是针入度实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("针入度")) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 获取软化点实验结果数据
     */
    private void fetchSofteningPointResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取软化点实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取软化点实验数据: " + taskId);
        
        asphaltTaskApi.getSofteningPointByTaskId(taskId).enqueue(new Callback<ApiResponse<SofteningPointResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SofteningPointResponse>> call, Response<ApiResponse<SofteningPointResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    SofteningPointResponse testResult = response.body().getData();
                    Log.d(TAG, "成功获取软化点实验数据");
                    
                    // 创建一个包含单个结果的列表，传递给适配器
                    List<SofteningPointResponse> testResultList = new ArrayList<>();
                    testResultList.add(testResult);
                    displaySofteningPointResults(testResultList);
                } else {
                    Log.e(TAG, "获取软化点实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是软化点实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("软化点")) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<SofteningPointResponse>> call, Throwable t) {
                Log.e(TAG, "获取软化点实验数据网络请求失败", t);
                // 任务指派明确是软化点实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("软化点")) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 获取延度实验结果数据
     */
    private void fetchDuctilityTestResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取延度实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取延度实验数据: " + taskId);
        
        asphaltTaskApi.getDuctilityTestByTaskId(taskId).enqueue(new Callback<ApiResponse<DuctilityTestResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DuctilityTestResponse>> call, Response<ApiResponse<DuctilityTestResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    DuctilityTestResponse testResult = response.body().getData();
                    Log.d(TAG, "成功获取延度实验数据");
                    
                    // 创建一个包含单个结果的列表，传递给适配器
                    List<DuctilityTestResponse> testResultList = new ArrayList<>();
                    testResultList.add(testResult);
                    displayDuctilityTestResults(testResultList);
                } else {
                    Log.e(TAG, "获取延度实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是延度实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("延度")) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<DuctilityTestResponse>> call, Throwable t) {
                Log.e(TAG, "获取延度实验数据网络请求失败", t);
                // 任务指派明确是延度实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("延度")) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 获取布鲁克菲尔德旋转黏度实验结果数据
     */
    private void fetchBrookfieldViscosityResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取旋转黏度实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取旋转黏度实验数据: " + taskId);
        
        asphaltTaskApi.getBrookfieldViscosityByTaskId(taskId).enqueue(new Callback<List<BrookfieldViscosityResponse>>() {
            @Override
            public void onResponse(Call<List<BrookfieldViscosityResponse>> call, Response<List<BrookfieldViscosityResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BrookfieldViscosityResponse> testResults = response.body();
                    Log.d(TAG, "成功获取旋转黏度实验数据");
                    
                    displayBrookfieldViscosityResults(testResults);
                } else {
                    Log.e(TAG, "获取旋转黏度实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是旋转黏度实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && 
                        (task.getTaskAssignment().contains("旋转黏度") || task.getTaskAssignment().contains("布鲁克菲尔德"))) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<BrookfieldViscosityResponse>> call, Throwable t) {
                Log.e(TAG, "获取旋转黏度实验数据网络请求失败", t);
                // 任务指派明确是旋转黏度实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && 
                    (task.getTaskAssignment().contains("旋转黏度") || task.getTaskAssignment().contains("布鲁克菲尔德"))) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 获取弯曲梁流变仪(BBR)实验结果数据
     */
    private void fetchBbrTestResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取弯曲梁流变仪实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取弯曲梁流变仪实验数据: " + taskId);
        
        asphaltTaskApi.getBbrTestByTaskId(taskId).enqueue(new Callback<ApiResponse<List<BbrTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BbrTestResponse>>> call, Response<ApiResponse<List<BbrTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<BbrTestResponse> testResults = response.body().getData();
                    Log.d(TAG, "成功获取弯曲梁流变仪实验数据，共 " + testResults.size() + " 条记录");
                    
                    displayBbrTestResults(testResults);
                } else {
                    Log.e(TAG, "获取弯曲梁流变仪实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是弯曲梁流变仪实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && 
                        (task.getTaskAssignment().contains("弯曲梁") || task.getTaskAssignment().contains("BBR") || task.getTaskAssignment().contains("流变仪"))) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<BbrTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取弯曲梁流变仪实验数据网络请求失败", t);
                // 任务指派明确是弯曲梁流变仪实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && 
                    (task.getTaskAssignment().contains("弯曲梁") || task.getTaskAssignment().contains("BBR") || task.getTaskAssignment().contains("流变仪"))) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 获取动态剪切流变仪(DSR)实验结果数据
     */
    private void fetchDsrTestResult() {
        if (task == null || task.getTaskId() == null) {
            Log.e(TAG, "任务对象或任务ID为空，无法获取动态剪切流变仪实验数据");
            return;
        }
        
        String taskId = task.getTaskId();
        Log.d(TAG, "正在获取动态剪切流变仪实验数据: " + taskId);
        
        asphaltTaskApi.getDsrTestByTaskId(taskId).enqueue(new Callback<ApiResponse<List<DsrTestResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DsrTestResponse>>> call, Response<ApiResponse<List<DsrTestResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<DsrTestResponse> testResults = response.body().getData();
                    Log.d(TAG, "成功获取动态剪切流变仪实验数据，共 " + testResults.size() + " 条记录");
                    
                    displayDsrTestResults(testResults);
                } else {
                    Log.e(TAG, "获取动态剪切流变仪实验数据失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                    // 任务指派明确是动态剪切流变仪实验但数据获取失败时，显示无数据提示
                    if (task.getTaskAssignment() != null && 
                        (task.getTaskAssignment().contains("动态剪切") || task.getTaskAssignment().contains("DSR"))) {
                        showNoResultsMessage();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<DsrTestResponse>>> call, Throwable t) {
                Log.e(TAG, "获取动态剪切流变仪实验数据网络请求失败", t);
                // 任务指派明确是动态剪切流变仪实验但数据获取失败时，显示无数据提示
                if (task.getTaskAssignment() != null && 
                    (task.getTaskAssignment().contains("动态剪切") || task.getTaskAssignment().contains("DSR"))) {
                    showNoResultsMessage();
                }
            }
        });
    }
    
    /**
     * 显示针入度实验结果数据
     * @param testResults 针入度实验结果列表
     */
    private void displayPenetrationTestResults(List<PenetrationTestResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是针入度指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("针入度")) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(penetrationTestAdapter);
        penetrationTestAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示针入度实验数据");
    }
    
    /**
     * 显示软化点实验结果数据
     * @param testResults 软化点实验结果列表
     */
    private void displaySofteningPointResults(List<SofteningPointResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是软化点指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("软化点")) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(softeningPointAdapter);
        softeningPointAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示软化点实验数据");
    }
    
    /**
     * 显示延度实验结果数据
     * @param testResults 延度实验结果列表
     */
    private void displayDuctilityTestResults(List<DuctilityTestResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是延度指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && task.getTaskAssignment().contains("延度")) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(ductilityAdapter);
        ductilityAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示延度实验数据");
    }
    
    /**
     * 显示布鲁克菲尔德旋转黏度实验结果
     * 
     * @param testResults 旋转黏度实验结果列表
     */
    private void displayBrookfieldViscosityResults(List<BrookfieldViscosityResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是旋转黏度指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && 
                (task.getTaskAssignment().contains("旋转黏度") || task.getTaskAssignment().contains("布鲁克菲尔德"))) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(brookfieldViscosityAdapter);
        brookfieldViscosityAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示旋转黏度实验数据");
    }
    
    /**
     * 显示弯曲梁流变仪(BBR)实验结果数据
     * @param testResults 弯曲梁流变仪实验结果列表
     */
    private void displayBbrTestResults(List<BbrTestResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是弯曲梁流变仪指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && 
                (task.getTaskAssignment().contains("弯曲梁") || task.getTaskAssignment().contains("BBR") || task.getTaskAssignment().contains("流变仪"))) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(bbrTestAdapter);
        bbrTestAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示弯曲梁流变仪实验数据");
    }
    
    /**
     * 显示动态剪切流变仪(DSR)实验结果数据
     * @param testResults 动态剪切流变仪实验结果列表
     */
    private void displayDsrTestResults(List<DsrTestResponse> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            // 如果是动态剪切流变仪指派的任务但数据为空，则显示无数据提示
            if (task.getTaskAssignment() != null && 
                (task.getTaskAssignment().contains("动态剪切") || task.getTaskAssignment().contains("DSR"))) {
                showNoResultsMessage();
            }
            return;
        }
        
        // 更新适配器数据
        rvExperimentResults.setAdapter(dsrTestAdapter);
        dsrTestAdapter.updateData(testResults);
        
        // 显示RecyclerView，隐藏无数据提示
        rvExperimentResults.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        
        Log.d(TAG, "显示动态剪切流变仪实验数据");
    }
    
    /**
     * 显示无实验结果的提示信息
     */
    private void showNoResultsMessage() {
        rvExperimentResults.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.VISIBLE);
        Log.d(TAG, "显示无实验结果提示");
    }
}
