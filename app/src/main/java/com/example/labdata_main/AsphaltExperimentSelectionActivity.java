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

import com.example.labdata_main.adapter.AsphaltExperimentAssignmentAdapter;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.service.AsphaltTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 沥青实验指派选择界面
 * 用户在此界面选择要进行数据收集的具体沥青实验指派
 */
public class AsphaltExperimentSelectionActivity extends AppCompatActivity implements AsphaltExperimentAssignmentAdapter.OnExperimentClickListener {

    private static final String TAG = "AsphaltExperimentSelectionActivity";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_NAME = "task_name";
    public static final String EXTRA_SELECTED_EXPERIMENT = "selected_experiment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvAsphaltExperiments;
    private TextView tvTaskName;
    private TextView tvEmptyExperiments;
    private ProgressBar progressBar;

    private AsphaltExperimentAssignmentAdapter adapter;
    private String taskId;
    private String taskName;
    private SharedPrefsManager sharedPrefsManager;
    private AsphaltTaskService asphaltTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asphalt_experiment_selection);

        // 初始化SharedPrefs和API服务
        sharedPrefsManager = new SharedPrefsManager(this);
        asphaltTaskService = ServiceCreator.create(AsphaltTaskService.class);

        // 获取传递的任务信息
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getStringExtra(EXTRA_TASK_ID);
            taskName = intent.getStringExtra(EXTRA_TASK_NAME);
        }

        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "任务ID不能为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        setupViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        // 加载实验指派数据
        loadExperimentAssignments();
    }

    private void setupViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rvAsphaltExperiments = findViewById(R.id.rvAsphaltExperiments);
        tvTaskName = findViewById(R.id.tvTaskName);
        tvEmptyExperiments = findViewById(R.id.tvEmptyExperiments);
        progressBar = findViewById(R.id.progressBar);

        if (taskName != null && !taskName.isEmpty()) {
            tvTaskName.setText("任务名称: " + taskName);
        } else {
            tvTaskName.setText("任务ID: " + taskId);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        rvAsphaltExperiments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AsphaltExperimentAssignmentAdapter(new ArrayList<>(), new HashMap<>());
        adapter.setOnExperimentClickListener(this);
        rvAsphaltExperiments.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_light_custom, R.color.white);
        swipeRefreshLayout.setOnRefreshListener(this::loadExperimentAssignments);
    }

    private void loadExperimentAssignments() {
        showLoading(true);

        // 显示用户可操作的沥青实验类型
        asphaltTaskService.getExperimentTypeStatus(taskId)
                .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, String>>> call, 
                                           Response<ApiResponse<Map<String, String>>> response) {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, String> statusMap = response.body().getData();
                            if (statusMap != null && !statusMap.isEmpty()) {
                                updateExperimentList(statusMap);
                            } else {
                                showEmptyState(true);
                                Log.w(TAG, "没有找到沥青实验指派");
                            }
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "获取实验指派失败";
                            Toast.makeText(AsphaltExperimentSelectionActivity.this, message, Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                            Log.e(TAG, "API响应错误: " + message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(AsphaltExperimentSelectionActivity.this, 
                                      "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                        Log.e(TAG, "API请求失败", t);
                    }
                });
    }

    /**
     * 更新实验列表数据
     */
    private void updateExperimentList(Map<String, String> statusMap) {
        List<String> experimentTypes = new ArrayList<>();
        Map<String, String> filteredStatusMap = new HashMap<>();

        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            String experimentType = entry.getKey();
            String status = entry.getValue();
            
            // 添加所有实验类型，无论状态如何
            experimentTypes.add(experimentType);
            
            // 设置显示状态文本
            if ("finished".equals(status)) {
                filteredStatusMap.put(experimentType, "已完成");
            } else {
                filteredStatusMap.put(experimentType, "未开始");
            }
        }

        // 更新适配器数据
        if (!experimentTypes.isEmpty()) {
            adapter.updateData(experimentTypes, filteredStatusMap);
            showEmptyState(false);
        } else {
            showEmptyState(true);
        }
    }

    /**
     * 处理实验类型点击事件
     */
    @Override
    public void onExperimentClick(String experimentType) {
        // 启动实验数据收集界面，并传递选中的实验类型
        Intent intent = new Intent(this, RecordExperimentDataActivity.class);
        intent.putExtra("taskId", taskId);  // 使用与RecordExperimentDataActivity一致的键名
        intent.putExtra("experiment_type", "ASPHALT");  // 设置实验类型为沥青
        intent.putExtra(EXTRA_SELECTED_EXPERIMENT, experimentType);
        startActivity(intent);
        
        // 选择后关闭当前界面
        finish();
    }

    /**
     * 显示或隐藏加载状态
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示或隐藏空状态
     */
    private void showEmptyState(boolean show) {
        if (show) {
            rvAsphaltExperiments.setVisibility(View.GONE);
            tvEmptyExperiments.setVisibility(View.VISIBLE);
        } else {
            rvAsphaltExperiments.setVisibility(View.VISIBLE);
            tvEmptyExperiments.setVisibility(View.GONE);
        }
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
