package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.adapter.ExperimentAssignmentAdapter;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.model.ExperimentAssignment;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExperimentAssignmentSelectionActivity extends AppCompatActivity implements ExperimentAssignmentAdapter.OnAssignmentClickListener {

    private static final String TAG = "ExperimentAssignmentSelectionActivity";
    
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvExperimentAssignments;
    private TextView tvTaskName;
    private TextView tvEmptyAssignments;
    private ProgressBar progressBar;
    
    private ExperimentAssignmentAdapter adapter;
    private CompletedExperimentTask task;
    private SharedPrefsManager sharedPrefsManager;
    private MixtureTaskService mixtureTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_assignment_selection);
        
        sharedPrefsManager = new SharedPrefsManager(this);
        // 初始化API服务
        mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        // 初始化视图
        initViews();
        
        // 获取任务数据
        getTaskData();
        
        // 加载实验指派列表
        loadExperimentAssignments();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rvExperimentAssignments = findViewById(R.id.rvExperimentAssignments);
        tvTaskName = findViewById(R.id.tvTaskName);
        tvEmptyAssignments = findViewById(R.id.tvEmptyAssignments);
        progressBar = findViewById(R.id.progressBar);
        
        // 设置RecyclerView
        rvExperimentAssignments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExperimentAssignmentAdapter(this);
        rvExperimentAssignments.setAdapter(adapter);
        
        // 设置下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_theme);
        swipeRefreshLayout.setOnRefreshListener(this::loadExperimentAssignments);
    }
    
    private void getTaskData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("task")) {
            task = (CompletedExperimentTask) intent.getSerializableExtra("task");
            if (task != null) {
                tvTaskName.setText(task.getTaskName());
            }
        } else {
            Toast.makeText(this, "未找到任务数据", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadExperimentAssignments() {
        swipeRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        
        // 确保task不为null
        if (task == null) {
            tvEmptyAssignments.setText("未找到任务数据");
            tvEmptyAssignments.setVisibility(View.VISIBLE);
            rvExperimentAssignments.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // 从taskId中提取前缀
        String taskId = task.getTaskId();
        String taskIdPrefix = taskId;
        
        // 如果任务ID包含连字符，取连字符前的部分作为前缀
        int dashIndex = taskId.indexOf('-');
        if (dashIndex > 0) {
            taskIdPrefix = taskId.substring(0, dashIndex);
        }
        
        Log.d(TAG, "任务ID前缀: " + taskIdPrefix);
        
        // 优先尝试从后端获取任务指派信息
        mixtureTaskService.getTaskAssignmentsByTaskIdPrefix(taskIdPrefix)
            .enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call, 
                                      Response<ApiResponse<List<Map<String, Object>>>> response) {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, Object>> assignmentData = response.body().getData();
                        Log.d(TAG, "获取到 " + assignmentData.size() + " 条任务指派信息");
                        
                        // 日志输出所有API返回数据的字段，用于调试
                        if (!assignmentData.isEmpty()) {
                            for (Map<String, Object> data : assignmentData) {
                                Log.d(TAG, "任务数据字段: " + data.keySet());
                                // 特别检查testing_status字段
                                if (data.containsKey("testing_status")) {
                                    Log.d(TAG, "testing_status值: " + data.get("testing_status"));
                                } else {
                                    Log.d(TAG, "数据中不包含testing_status字段");
                                }
                            }
                            
                            // 将后端返回的数据转换为ExperimentAssignment对象列表
                            List<ExperimentAssignment> assignments = convertToExperimentAssignments(assignmentData);
                            
                            if (assignments.isEmpty()) {
                                // 如果转换后列表为空，显示空视图
                                tvEmptyAssignments.setVisibility(View.VISIBLE);
                                rvExperimentAssignments.setVisibility(View.GONE);
                            } else {
                                // 更新适配器数据
                                tvEmptyAssignments.setVisibility(View.GONE);
                                rvExperimentAssignments.setVisibility(View.VISIBLE);
                                adapter.setAssignments(assignments);
                            }
                            return;
                        }
                    } else {
                        String errorMsg = response.isSuccessful() && response.body() != null ? 
                                        response.body().getMessage() : "获取任务指派信息失败";
                        Log.e(TAG, errorMsg);
                    }
                    
                    // 如果API请求失败或返回空数据，使用本地解析方式作为备选
                    fallbackToLocalParsing();
                }
                
                @Override
                public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "获取任务指派信息失败: " + t.getMessage(), t);
                    
                    // API请求失败，使用本地解析方式作为备选
                    fallbackToLocalParsing();
                }
            });
    }
    
    /**
     * 转换后端返回的任务指派数据为ExperimentAssignment对象列表
     */
    private List<ExperimentAssignment> convertToExperimentAssignments(List<Map<String, Object>> assignmentData) {
        List<ExperimentAssignment> assignments = new ArrayList<>();
        
        for (Map<String, Object> data : assignmentData) {
            String taskId = getString(data, "taskId");
            
            // 按照记忆中的优先级，先使用assignmentInfo（对应后端的taskAssignment），后使用taskName
            String assignmentInfo = getString(data, "assignmentInfo");
            String taskName = getString(data, "taskName");
            
            // 创建实验指派对象
            ExperimentAssignment assignment = new ExperimentAssignment();
            
            // 设置实验类型列表，只添加一个元素即可
            List<String> experimentTypes = new ArrayList<>();
            
            // 按优先级确定要显示的指派信息
            String displayName;
            if (assignmentInfo != null && !assignmentInfo.isEmpty()) {
                displayName = assignmentInfo;  // 优先使用taskAssignment
            } else if (taskName != null && !taskName.isEmpty()) {
                displayName = taskName;  // 次优先使用taskName
            } else {
                displayName = "未知实验";  // 默认值
            }
            
            experimentTypes.add(displayName);
            assignment.setExperimentTypes(experimentTypes);
            
            // 设置UI显示用的字段
            assignment.setAssignmentName(displayName);
            assignment.setAssignmentDetails("任务ID: " + taskId);
            
            // 初始设置任务未完成
            assignment.setFinished(false);
            
            assignments.add(assignment);
            
            // 发起请求获取任务测试状态
            fetchTestingStatus(taskId, assignment);
        }
        
        return assignments;
    }
    
    /**
     * 获取任务的测试状态并更新UI
     * 
     * @param taskId 任务ID
     * @param assignment 相关联的实验指派对象
     */
    private void fetchTestingStatus(String taskId, ExperimentAssignment assignment) {
        // 先检查任务ID是否有效
        if (taskId == null || taskId.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "正在获取任务 " + taskId + " 的测试状态");
        
        // 调用API获取任务测试状态
        mixtureTaskService.getTestingStatus(taskId)
            .enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String status = response.body().getData();
                        Log.d(TAG, "任务 " + taskId + " 的测试状态: " + status);
                        
                        boolean isFinished = "finished".equalsIgnoreCase(status);
                        assignment.setFinished(isFinished);
                        
                        if (isFinished) {
                            Log.d(TAG, "任务 " + taskId + " 已完成，将显示为绿色");
                        }
                        
                        // 通知适配器更新UI
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "获取任务 " + taskId + " 测试状态失败: " + 
                              (response.body() != null ? response.body().getMessage() : "未知错误"));
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e(TAG, "获取任务 " + taskId + " 测试状态请求失败: " + t.getMessage(), t);
                }
            });
    }
    
    /**
     * 当API请求失败时，使用本地解析方式作为备选
     */
    private void fallbackToLocalParsing() {
        Log.d(TAG, "使用本地解析方式获取任务指派信息");
        List<ExperimentAssignment> assignments = parseAssignmentsFromTask(task);
        
        if (assignments.isEmpty()) {
            tvEmptyAssignments.setVisibility(View.VISIBLE);
            rvExperimentAssignments.setVisibility(View.GONE);
        } else {
            tvEmptyAssignments.setVisibility(View.GONE);
            rvExperimentAssignments.setVisibility(View.VISIBLE);
            adapter.setAssignments(assignments);
        }
    }
    
    /**
     * 安全获取Map中的字符串值，避免类型转换异常
     */
    private String getString(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object value = map.get(key);
            return value.toString();
        }
        return "";
    }
    
    /**
     * 从任务中解析实验指派信息
     */
    private List<ExperimentAssignment> parseAssignmentsFromTask(CompletedExperimentTask task) {
        List<ExperimentAssignment> result = new ArrayList<>();
        
        // 首先尝试使用taskAssignment字段
        String taskAssignment = task.getTaskAssignment();
        if (taskAssignment != null && !taskAssignment.isEmpty()) {
            // 创建一个实验指派对象来表示这个任务指派
            ExperimentAssignment assignment = new ExperimentAssignment();
            List<String> experimentTypes = new ArrayList<>();
            experimentTypes.add(taskAssignment);
            assignment.setExperimentTypes(experimentTypes);
            
            // 设置一个自定义的assignmentName和details，用于在UI中显示
            result.add(createUIAssignment(taskAssignment, "任务ID: " + task.getId()));
        } 
        
        // 如果taskAssignment为空，则尝试使用实验任务名称
        if (result.isEmpty() && task.getTaskName() != null && !task.getTaskName().isEmpty()) {
            result.add(createUIAssignment(task.getTaskName(), "任务ID: " + task.getId()));
        }
        
        // 如果还是没有指派信息，添加一个默认的
        if (result.isEmpty()) {
            result.add(createUIAssignment("未知实验", "任务ID: " + task.getId()));
        }
        
        return result;
    }
    
    /**
     * 创建用于UI显示的实验指派对象
     */
    private ExperimentAssignment createUIAssignment(String name, String details) {
        ExperimentAssignment assignment = new ExperimentAssignment();
        
        // 添加名称到experimentTypes，这会在toString()时显示
        List<String> experimentTypes = new ArrayList<>();
        experimentTypes.add(name);
        assignment.setExperimentTypes(experimentTypes);
        
        // 增加额外的展示信息
        assignment.setNotes(details);
        
        return assignment;
    }

    @Override
    public void onAssignmentClick(ExperimentAssignment assignment) {
        // 启动记录实验数据界面，并传递所选实验指派信息
        Intent intent;
        if (task.getType().equals("MIXTURE")) {
            intent = new Intent(this, RecordMixtureExperimentDataActivity.class);
        } else {
            // 对于其他类型的任务，可以添加相应的处理
            Toast.makeText(this, "暂不支持此类型任务", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 明确指定使用Serializable接口，避免歧义
        intent.putExtra("task", (Serializable) task);
        intent.putExtra("selectedAssignment", assignment.getExperimentTypes().get(0));
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
