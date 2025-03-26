package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.adapter.CompletedExperimentAdapter;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.CompletedAsphaltTaskResponse;
import com.example.labdata_main.api.response.CompletedMixtureTaskResponse;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.util.SPUtils;
import com.example.labdata_main.util.PreferenceManager;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 实验记录Fragment
 */
public class ExperimentFragment extends Fragment {
    private static final String TAG = "ExperimentFragment";
    
    // SharedPreferences管理器
    private SharedPrefsManager sharedPrefsManager;

    // UI组件
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvExperiments;
    private TextView tvNoData;
    private CompletedExperimentAdapter adapter;
    private final List<CompletedExperimentTask> allTasks = new ArrayList<>();
    
    // API服务
    private ApiService apiService;
    private String companyId;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        apiService = ApiClient.getClient().create(ApiService.class);
        
        // 使用SharedPrefsManager替代PreferenceManager
        sharedPrefsManager = new SharedPrefsManager(context);
        companyId = sharedPrefsManager.getUserCompany();
        
        // 记录日志以便调试
        Log.d(TAG, "使用SharedPrefsManager获取到公司ID: " + companyId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    /**
     * 初始化视图
     */
    private void initViews(View view) {
        rvExperiments = view.findViewById(R.id.rvExperiments);
        tvNoData = view.findViewById(R.id.tvNoData);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // 初始化适配器
        adapter = new CompletedExperimentAdapter(getContext());
        rvExperiments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExperiments.setAdapter(adapter);
        
        // 设置点击监听器
        adapter.setOnItemClickListener((task, position) -> {
            Log.d(TAG, "任务点击: ID=" + task.getTaskId() + ", 类型=" + (task.isMixtureTask() ? "混合料" : "沥青"));
            
            // 根据任务类型打开不同的结果界面
            Intent intent;
            if (task.isMixtureTask()) {
                // 混合料任务，打开混合料结果界面
                intent = new Intent(getContext(), MixtureTaskResultActivity.class);
            } else {
                // 沥青任务，打开沥青结果界面
                intent = new Intent(getContext(), AsphaltTaskResultActivity.class);
            }
            
            // 传递任务数据（确保CompletedExperimentTask已实现Serializable接口）
            intent.putExtra(MixtureTaskResultActivity.EXTRA_TASK, task);
            
            // 启动对应的Activity
            startActivity(intent);
        });
        
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        
        // 处理筛选按钮
        Button btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            // 暂不实现筛选功能，可以在这里添加筛选逻辑
            Toast.makeText(getContext(), "筛选功能暂未实现", Toast.LENGTH_SHORT).show();
        });
        
        // 处理分析按钮
        Button btnAnalyze = view.findViewById(R.id.btnAnalyze);
        btnAnalyze.setOnClickListener(v -> {
            // 暂不实现分析功能，可以在这里添加分析逻辑
            Toast.makeText(getContext(), "分析功能暂未实现", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 加载数据
     */
    private void loadData() {
        Log.d(TAG, "开始加载已完成实验数据");
        swipeRefreshLayout.setRefreshing(true);
        allTasks.clear();
        
        // 并行加载混合料和沥青任务
        fetchCompletedMixtureTasks();
        fetchCompletedAsphaltTasks();
    }
    
    /**
     * 获取已完成的混合料任务
     */
    private void fetchCompletedMixtureTasks() {
        if (apiService == null) {
            Log.e(TAG, "API服务未初始化");
            return;
        }

        // 获取公司ID
        companyId = sharedPrefsManager.getUserCompany();
        Log.d(TAG, "获取混合料任务，使用公司ID: " + companyId);
        
        apiService.getCompletedMixtureTasks(companyId).enqueue(new Callback<ApiResponse<List<CompletedMixtureTaskResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CompletedMixtureTaskResponse>>> call, Response<ApiResponse<List<CompletedMixtureTaskResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CompletedMixtureTaskResponse> mixtureTasks = response.body().getData();
                    Log.d(TAG, "成功获取混合料任务: " + (mixtureTasks != null ? mixtureTasks.size() : 0) + "个");
                    
                    // 转换为通用任务模型
                    if (mixtureTasks != null) {
                        for (CompletedMixtureTaskResponse task : mixtureTasks) {
                            allTasks.add(CompletedExperimentTask.fromMixtureTask(task));
                        }
                    }
                    
                    // 更新UI
                    updateUI();
                } else {
                    Log.e(TAG, "获取混合料任务失败: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    Toast.makeText(getContext(), "获取混合料任务失败", Toast.LENGTH_SHORT).show();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CompletedMixtureTaskResponse>>> call, Throwable t) {
                Log.e(TAG, "获取混合料任务请求失败", t);
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }
    
    /**
     * 获取已完成的沥青任务
     */
    private void fetchCompletedAsphaltTasks() {
        if (apiService == null) {
            Log.e(TAG, "API服务未初始化");
            return;
        }

        // 获取公司ID
        companyId = sharedPrefsManager.getUserCompany();
        Log.d(TAG, "获取沥青任务，使用公司ID: " + companyId);
        
        apiService.getCompletedAsphaltTasks(companyId).enqueue(new Callback<ApiResponse<List<CompletedAsphaltTaskResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CompletedAsphaltTaskResponse>>> call, Response<ApiResponse<List<CompletedAsphaltTaskResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CompletedAsphaltTaskResponse> asphaltTasks = response.body().getData();
                    Log.d(TAG, "成功获取沥青任务: " + (asphaltTasks != null ? asphaltTasks.size() : 0) + "个");
                    
                    // 转换为通用任务模型
                    if (asphaltTasks != null) {
                        for (CompletedAsphaltTaskResponse task : asphaltTasks) {
                            allTasks.add(CompletedExperimentTask.fromAsphaltTask(task));
                        }
                    }
                    
                    // 更新UI
                    updateUI();
                } else {
                    Log.e(TAG, "获取沥青任务失败: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    Toast.makeText(getContext(), "获取沥青任务失败", Toast.LENGTH_SHORT).show();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CompletedAsphaltTaskResponse>>> call, Throwable t) {
                Log.e(TAG, "获取沥青任务请求失败", t);
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }
    
    /**
     * 更新UI显示
     */
    private void updateUI() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // 停止刷新动画
            swipeRefreshLayout.setRefreshing(false);
            
            // 更新数据列表
            adapter.updateData(allTasks);
            
            // 显示/隐藏空数据提示
            if (allTasks.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                rvExperiments.setVisibility(View.GONE);
            } else {
                tvNoData.setVisibility(View.GONE);
                rvExperiments.setVisibility(View.VISIBLE);
            }
            
            Log.d(TAG, "UI更新完成，显示 " + allTasks.size() + " 个任务");
        });
    }
}