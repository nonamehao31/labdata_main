package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
    private EditText etSearch;
    private ImageView ivClearSearch;
    private CompletedExperimentAdapter adapter;
    private final List<CompletedExperimentTask> allTasks = new ArrayList<>();
    private final List<CompletedExperimentTask> filteredTasks = new ArrayList<>();
    
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
        etSearch = view.findViewById(R.id.etSearch);
        ivClearSearch = view.findViewById(R.id.ivClearSearch);
        
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
            intent.putExtra(MixtureTaskResultActivity.EXTRA_TASK, (Parcelable) task);
            
            // 启动对应的Activity
            startActivity(intent);
        });
        
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        
        // 设置搜索框文本变化监听器
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要实现
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 实时筛选
                filterTasks(s.toString());
                
                // 根据输入内容显示/隐藏清除按钮
                if (TextUtils.isEmpty(s)) {
                    ivClearSearch.setVisibility(View.GONE);
                } else {
                    ivClearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 不需要实现
            }
        });
        
        // 设置清除按钮点击事件
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            ivClearSearch.setVisibility(View.GONE);
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
            
            // 先执行一次筛选（如果搜索框有内容）
            if (etSearch != null && etSearch.getText() != null && !etSearch.getText().toString().isEmpty()) {
                filterTasks(etSearch.getText().toString());
            } else {
                // 如果搜索框为空，直接显示所有数据
                filteredTasks.clear();
                filteredTasks.addAll(allTasks);
                adapter.updateData(filteredTasks, "");
            }
            
            // 显示/隐藏空数据提示
            if (filteredTasks.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                rvExperiments.setVisibility(View.GONE);
            } else {
                tvNoData.setVisibility(View.GONE);
                rvExperiments.setVisibility(View.VISIBLE);
            }
            
            Log.d(TAG, "UI更新完成，显示 " + filteredTasks.size() + " 个任务（总共 " + allTasks.size() + " 个）");
        });
    }
    
    /**
     * 筛选任务
     * @param keyword 关键词
     */
    private void filterTasks(String keyword) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "执行筛选，关键词: " + keyword);
            
            filteredTasks.clear();
            
            if (keyword == null || keyword.trim().isEmpty()) {
                // 如果关键词为空，显示所有任务
                filteredTasks.addAll(allTasks);
                adapter.updateData(filteredTasks, "");
            } else {
                String normalizedKeyword = keyword.toLowerCase().trim();
                
                // 从所有任务中筛选
                for (CompletedExperimentTask task : allTasks) {
                    // 匹配各个字段
                    if (containsIgnoreCase(task.getExperimentName(), normalizedKeyword) ||
                        containsIgnoreCase(task.getExperimentType(), normalizedKeyword) ||
                        containsIgnoreCase(task.getTaskId(), normalizedKeyword) ||
                        containsIgnoreCase(task.getTaskAssignment(), normalizedKeyword) ||
                        containsIgnoreCase(task.getExperimenter(), normalizedKeyword) ||
                        (task.isMixtureTask() && containsIgnoreCase(task.getMixName(), normalizedKeyword)) ||
                        (task.isMixtureTask() && containsIgnoreCase(task.getCompactionMethod(), normalizedKeyword)) ||
                        (!task.isMixtureTask() && containsIgnoreCase(task.getTaskName(), normalizedKeyword))) {
                        
                        filteredTasks.add(task);
                    }
                }
                
                // 更新适配器，并传递关键词用于高亮
                adapter.updateData(filteredTasks, normalizedKeyword);
            }
            
            // 显示/隐藏空数据提示
            if (filteredTasks.isEmpty()) {
                tvNoData.setText("没有找到匹配\"" + keyword + "\"的实验");
                tvNoData.setVisibility(View.VISIBLE);
                rvExperiments.setVisibility(View.GONE);
            } else {
                tvNoData.setVisibility(View.GONE);
                rvExperiments.setVisibility(View.VISIBLE);
            }
            
            Log.d(TAG, "筛选完成，找到 " + filteredTasks.size() + " 个匹配任务");
        });
    }
    
    /**
     * 判断字符串是否包含关键词（忽略大小写）
     */
    private boolean containsIgnoreCase(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }
}