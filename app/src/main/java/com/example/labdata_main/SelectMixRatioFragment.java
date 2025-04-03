package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.adapter.MixRatioAdapter;
import com.example.labdata_main.adapter.MixRatioAdapter.OnMixRatioDeleteListener;
import com.example.labdata_main.api.MixRatioApiService;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MixRatioResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectMixRatioFragment extends Fragment implements MixRatioAdapter.OnMixRatioSelectedListener, OnMixRatioDeleteListener {
    private static final String TAG = "SelectMixRatioFragment";
    
    private RecyclerView rvMixRatios;
    private MaterialCardView cardAddMixRatio;
    private TextView emptyView;
    private ProgressBar progressBar;
    private MixRatioAdapter mixRatioAdapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private MixRatioApiService apiService;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        apiService = new MixRatioApiService(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_mix_ratio, container, false);

        initViews(view);
        setupListeners();
        loadMixRatios();

        return view;
    }

    private void initViews(View view) {
        cardAddMixRatio = view.findViewById(R.id.cardAddMixRatio);
        rvMixRatios = view.findViewById(R.id.rvMixRatios);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        loadingLayout = view.findViewById(R.id.loadingLayout);

        // 初始化配比列表
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        mixRatioAdapter = new MixRatioAdapter(this, this);
        rvMixRatios.setAdapter(mixRatioAdapter);
    }

    private void setupListeners() {
        cardAddMixRatio.setOnClickListener(v -> {
            // 跳转到添加配比界面
            Intent intent = new Intent(requireContext(), MixRatioEditActivity.class);
            startActivity(intent);
        });
        
        // 设置下拉刷新监听器
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadMixRatios);
        }
    }

    private void loadMixRatios() {
        // 显示加载中状态
        showLoading(true);
        
        // 获取当前用户的公司ID
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String companyIdStr = sharedPreferences.getString("userCompany", "");
        long companyId = -1;
        
        // 尝试将公司ID字符串转换为长整型
        if (companyIdStr != null && !companyIdStr.isEmpty()) {
            try {
                companyId = Long.parseLong(companyIdStr);
                Log.d(TAG, "从SharedPreferences获取到公司ID: " + companyId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "公司ID格式不正确，无法转换为数字: " + companyIdStr);
                // 如果公司ID不是数字格式，可能是直接使用公司名称
            }
        }
        
        if (companyId == -1) {
            Log.e(TAG, "无法获取当前用户的有效公司ID");
            showError("无法获取公司信息，请重新登录");
            showLoading(false);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        
        Log.d(TAG, "获取公司ID: " + companyId + " 的配比数据");
        
        // 从API获取当前公司的配比数据
        apiService.getMixRatiosByCompany(String.valueOf(companyId), new Callback<ApiResponse<List<MixRatioResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixRatioResponse>>> call, Response<ApiResponse<List<MixRatioResponse>>> response) {
                showLoading(false);
                
                // 停止刷新动画
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MixRatioResponse> mixRatioResponses = response.body().getData();
                    Log.d(TAG, "成功从API获取 " + (mixRatioResponses != null ? mixRatioResponses.size() : 0) + " 条公司配比数据");
                    
                    // 打印API返回的原始数据
                    if (mixRatioResponses != null && !mixRatioResponses.isEmpty()) {
                        MixRatioResponse firstResponse = mixRatioResponses.get(0);
                        Log.d(TAG, "API返回的第一条配比数据: ID=" + firstResponse.getId() + ", 名称=" + firstResponse.getMixName());
                        
                        if (firstResponse.getAsphaltComponents() != null) {
                            for (MixRatioResponse.AsphaltComponentResponse component : firstResponse.getAsphaltComponents()) {
                                Log.d(TAG, "沥青组件: 名称=" + component.getAsphaltName() + ", 百分比=" + component.getPercentage());
                            }
                        }
                        
                        if (firstResponse.getSandComponents() != null) {
                            for (MixRatioResponse.SandComponentResponse component : firstResponse.getSandComponents()) {
                                Log.d(TAG, "砂料组件: 名称=" + component.getSandName() + ", 百分比=" + component.getPercentage());
                            }
                        }
                        
                        if (firstResponse.getStoneComponents() != null) {
                            for (MixRatioResponse.StoneComponentResponse component : firstResponse.getStoneComponents()) {
                                Log.d(TAG, "石料组件: 名称=" + component.getStoneName() + ", 百分比=" + component.getPercentage());
                            }
                        }
                    }
                    
                    // 转换API响应为本地模型
                    List<MixRatio> mixRatios = convertToLocalMixRatios(mixRatioResponses);
                    
                    // 日志输出转换后的模型数据
                    if (!mixRatios.isEmpty()) {
                        MixRatio firstMixRatio = mixRatios.get(0);
                        Log.d(TAG, "转换后的第一条配比数据: ID=" + firstMixRatio.getId() + ", 名称=" + firstMixRatio.getName());
                        
                        List<MaterialItem> materials = firstMixRatio.getMaterials();
                        if (materials != null) {
                            for (MaterialItem item : materials) {
                                Log.d(TAG, "材料项: 名称=" + item.getName() + ", 类型=" + item.getType() + ", 百分比=" + item.getPercentage());
                            }
                        }
                    }
                    
                    // 更新UI
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {  // 再次检查，确保Fragment仍然附加
                                mixRatioAdapter.submitList(mixRatios);
                                updateEmptyView(mixRatios.isEmpty());
                                checkInputValidity();
                            }
                        });
                    }
                } else {
                    // 处理错误
                    String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                    Log.e(TAG, "获取配比数据失败: " + errorMessage);
                    showError("获取配比数据失败: " + errorMessage);
                    
                    // 尝试从本地数据库加载作为备选方案
                    loadFromLocalDatabase();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MixRatioResponse>>> call, Throwable t) {
                showLoading(false);
                
                // 停止刷新动画
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                Log.e(TAG, "API调用失败", t);
                showError("网络请求失败: " + t.getMessage());
                
                // 网络请求失败时使用本地数据
                loadFromLocalDatabase();
            }
        });
    }
    
    // 从本地数据库加载数据的备用方法
    private void loadFromLocalDatabase() {
        Log.d(TAG, "从本地数据库加载配比数据");
        
        // 获取当前用户的公司ID
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String companyIdStr = sharedPreferences.getString("userCompany", "");
        long companyId = -1;
        
        // 尝试将公司ID字符串转换为长整型
        if (companyIdStr != null && !companyIdStr.isEmpty()) {
            try {
                companyId = Long.parseLong(companyIdStr);
                Log.d(TAG, "从SharedPreferences获取到公司ID: " + companyId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "公司ID格式不正确，无法转换为数字: " + companyIdStr);
                // 如果公司ID不是数字格式，可能是直接使用公司名称
            }
        }
        
        if (companyId == -1) {
            Log.e(TAG, "无法获取当前用户的有效公司ID，无法从本地数据库加载配比");
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    // 停止刷新动画
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    updateEmptyView(true);
                    showError("无法获取公司信息，请重新登录");
                });
            }
            return;
        }
        
        // 创建一个final变量存储公司ID，以便在lambda表达式中使用
        final long finalCompanyId = companyId;
        
        executorService.execute(() -> {
            // 尝试获取当前公司ID的配比
            List<MixRatio> mixRatios = new ArrayList<>();
            
            try {
                // 获取全部配比，然后根据公司ID过滤
                List<MixRatio> allMixRatios = databaseHelper.mixRatioDao().getAllMixRatios();
                
                // 过滤属于当前公司的配比
                // 注意：根据数据模型，这里假设MixRatio中有getCompanyId方法或类似字段
                // 如果MixRatio模型没有公司ID字段，可能需要修改数据模型
                for (MixRatio mixRatio : allMixRatios) {
                    // TODO: 这里根据实际的MixRatio模型进行筛选
                    // 由于图片显示MixRatio表包含mix_company字段，应该可以用它来过滤
                    if (mixRatio.getCompanyId() != null && mixRatio.getCompanyId() == finalCompanyId) {
                        mixRatios.add(mixRatio);
                    }
                }
                
                Log.d(TAG, "从本地数据库成功加载 " + mixRatios.size() + " 条公司配比数据");
            } catch (Exception e) {
                Log.e(TAG, "从本地数据库加载配比发生错误", e);
            }
            
            // 更新UI
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    
                    // 停止刷新动画
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    mixRatioAdapter.submitList(mixRatios);
                    updateEmptyView(mixRatios.isEmpty());
                    checkInputValidity();
                    
                    if (mixRatios.isEmpty()) {
                        showError("未找到公司相关配比数据");
                    }
                });
            }
        });
    }
    
    // 将API响应转换为本地模型
    private List<MixRatio> convertToLocalMixRatios(List<MixRatioResponse> responses) {
        if (responses == null) {
            return new ArrayList<>();
        }
        
        List<MixRatio> result = new ArrayList<>();
        
        for (MixRatioResponse response : responses) {
            MixRatio mixRatio = new MixRatio();
            mixRatio.setId(response.getId());
            mixRatio.setName(response.getMixName());
            mixRatio.setDescription(""); // 删除说明文字
            
            // 提取并设置公司ID
            if (response.getMixCompany() != null && !response.getMixCompany().isEmpty()) {
                try {
                    // 尝试将公司ID字符串转换为Long类型
                    Long companyId = null;
                    
                    try {
                        // 尝试直接解析为数字
                        companyId = Long.parseLong(response.getMixCompany());
                    } catch (NumberFormatException e) {
                        // 如果不是纯数字，可能是其他格式，记录日志
                        Log.d(TAG, "公司ID格式不是纯数字: " + response.getMixCompany());
                    }
                    
                    mixRatio.setCompanyId(companyId);
                    Log.d(TAG, "设置配比公司ID: " + companyId + ", 原始值: " + response.getMixCompany());
                } catch (Exception e) {
                    Log.e(TAG, "设置公司ID时出错: " + e.getMessage());
                }
            } else {
                // 从当前用户的SharedPreferences获取公司ID作为后备
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String companyIdStr = sharedPreferences.getString("userCompany", "");
                long companyId = -1;
                
                // 尝试将公司ID字符串转换为长整型
                if (companyIdStr != null && !companyIdStr.isEmpty()) {
                    try {
                        companyId = Long.parseLong(companyIdStr);
                        Log.d(TAG, "从SharedPreferences获取到公司ID: " + companyId);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "公司ID格式不正确，无法转换为数字: " + companyIdStr);
                        // 如果公司ID不是数字格式，可能是直接使用公司名称
                    }
                }
                
                if (companyId != -1) {
                    mixRatio.setCompanyId(companyId);
                    Log.d(TAG, "使用当前用户公司ID作为配比公司ID: " + companyId);
                }
            }
            
            // 将材料组件转换为本地MaterialItem
            List<MaterialItem> materials = new ArrayList<>();
            
            // 转换沥青组件
            if (response.getAsphaltComponents() != null) {
                for (MixRatioResponse.AsphaltComponentResponse component : response.getAsphaltComponents()) {
                    float percentage = component.getPercentage() != null ? component.getPercentage().floatValue() : 0f;
                    String percentageStr = String.valueOf(percentage);
                    Log.d(TAG, "转换沥青组件: " + component.getAsphaltName() + ", 百分比=" + percentage);
                    
                    MaterialItem item = new MaterialItem(
                        component.getAsphaltName(), // 使用实际的沥青名称作为MaterialItem的name
                        percentage,
                        "ASPHALT" // 类型标识
                    );
                    item.setAmount(percentageStr); // 设置amount字段，用于在适配器中显示百分比
                    materials.add(item);
                }
            }
            
            // 转换砂料组件
            if (response.getSandComponents() != null) {
                for (MixRatioResponse.SandComponentResponse component : response.getSandComponents()) {
                    float percentage = component.getPercentage() != null ? component.getPercentage().floatValue() : 0f;
                    String percentageStr = String.valueOf(percentage);
                    Log.d(TAG, "转换砂料组件: " + component.getSandName() + ", 百分比=" + percentage);
                    
                    MaterialItem item = new MaterialItem(
                        component.getSandName(), // 使用实际的砂料名称作为MaterialItem的name
                        percentage,
                        "SAND" // 类型标识
                    );
                    item.setAmount(percentageStr); // 设置amount字段，用于在适配器中显示百分比
                    materials.add(item);
                }
            }
            
            // 转换石料组件
            if (response.getStoneComponents() != null) {
                for (MixRatioResponse.StoneComponentResponse component : response.getStoneComponents()) {
                    float percentage = component.getPercentage() != null ? component.getPercentage().floatValue() : 0f;
                    String percentageStr = String.valueOf(percentage);
                    Log.d(TAG, "转换石料组件: " + component.getStoneName() + ", 百分比=" + percentage);
                    
                    MaterialItem item = new MaterialItem(
                        component.getStoneName(), // 使用实际的石料名称作为MaterialItem的name
                        percentage,
                        "STONE" // 类型标识
                    );
                    item.setAmount(percentageStr); // 设置amount字段，用于在适配器中显示百分比
                    materials.add(item);
                }
            }
            
            mixRatio.setMaterials(materials);
            
            // 检查设置后的材料列表
            List<MaterialItem> finalMaterials = mixRatio.getMaterials();
            if (finalMaterials != null) {
                Log.d(TAG, "最终设置到MixRatio的材料数量: " + finalMaterials.size());
                for (MaterialItem item : finalMaterials) {
                    Log.d(TAG, "最终材料项: 名称=" + item.getName() + ", 类型=" + item.getType() + ", 百分比=" + item.getPercentage());
                }
            }
            
            result.add(mixRatio);
        }
        
        return result;
    }

    // 显示/隐藏加载进度条
    private void showLoading(boolean isLoading) {
        if (loadingLayout != null) {
            loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    // 显示错误信息
    private void showError(String errorMessage) {
        if (getView() != null) {
            Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMixRatios.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void checkInputValidity() {
        boolean isValid = !selectedMixRatios.isEmpty();
        // 通知Activity更新下一步按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).enableNextButton(isValid);
        }
    }

    public List<MixRatio> getSelectedMixRatios() {
        Log.d(TAG, "Getting selected mix ratios: " + selectedMixRatios.size());
        return selectedMixRatios;
    }

    @Override
    public void onMixRatioSelected(MixRatio mixRatio) {
        if (selectedMixRatios.contains(mixRatio)) {
            selectedMixRatios.remove(mixRatio);
        } else {
            selectedMixRatios.add(mixRatio);
        }
        
        // 更新 ViewPager 中的配比列表
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ExperimentTaskSetupActivity activity = (ExperimentTaskSetupActivity) getActivity();
            ViewPager2 viewPager = activity.findViewById(R.id.viewPager);
            if (viewPager != null && viewPager.getAdapter() instanceof ExperimentTaskPagerAdapter) {
                ExperimentTaskPagerAdapter adapter = (ExperimentTaskPagerAdapter) viewPager.getAdapter();
                adapter.setSelectedMixRatios(new ArrayList<>(selectedMixRatios));
            }
        }
        
        Log.d(TAG, "Selected mix ratios: " + selectedMixRatios.size());
        checkInputValidity();
    }

    @Override
    public void onMixRatioDelete(MixRatio mixRatio) {
        // 创建确认删除的对话框
        new AlertDialog.Builder(requireContext())
            .setTitle("删除配合比")
            .setMessage("确定要删除配合比 \"" + mixRatio.getName() + "\" 吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                // 显示加载状态
                showLoading(true);
                
                // 调用API删除配比
                apiService.deleteMixRatio(mixRatio.getId(), new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // 从适配器中移除
                            mixRatioAdapter.removeMixRatio(mixRatio);
                            
                            // 显示删除成功的提示
                            showError("配合比删除成功");
                            
                            // 如果被删除的配比在已选择列表中，也要移除
                            if (selectedMixRatios.contains(mixRatio)) {
                                selectedMixRatios.remove(mixRatio);
                                checkInputValidity();
                            }
                        } else {
                            // 处理错误
                            String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                            showError("删除配合比失败: " + errorMessage);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        showLoading(false);
                        showError("网络请求失败: " + t.getMessage());
                        
                        // 网络请求失败时，尝试从本地删除
                        deleteFromLocalDatabase(mixRatio);
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    // 从本地数据库中删除配比的备用方法
    private void deleteFromLocalDatabase(MixRatio mixRatio) {
        executorService.execute(() -> {
            try {
                // 删除配合比
                databaseHelper.mixRatioDao().delete(mixRatio);
                
                // 在主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    // 从适配器中移除
                    mixRatioAdapter.removeMixRatio(mixRatio);
                    
                    // 显示删除成功的提示
                    showError("配合比从本地删除成功");
                });
            } catch (Exception e) {
                // 在主线程显示错误提示
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "删除配合比失败", e);
                    showError("删除配合比失败：" + e.getLocalizedMessage());
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMixRatios(); // 页面恢复时刷新数据
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}