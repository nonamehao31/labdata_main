package com.example.labdata_main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.request.MixtureTaskRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.model.ExperimentAssignment;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MixtureTaskModel;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.SupportMixtureTaskModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExperimentAssignmentFragment extends Fragment {
    private LinearLayout containerMixRatioExperiments;
    private TextInputEditText etNotes;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();
    private Map<Long, ChipGroup> experimentChipGroups = new HashMap<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private Long projectId;
    private List<MoldingMethod> selectedMoldingMethods = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment_assignment, container, false);
        
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        containerMixRatioExperiments = view.findViewById(R.id.containerMixRatioExperiments);
        etNotes = view.findViewById(R.id.etNotes);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar == null) {
            // 创建并添加进度条，如果布局中没有
            progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.GONE);
            ((ViewGroup) containerMixRatioExperiments.getParent()).addView(progressBar);
        }
    }
    
    private void setupListeners() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshExperimentTypes);
        }
    }
    
    private void refreshExperimentTypes() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // 清除当前的实验类型选项
        experimentChipGroups.clear();
        containerMixRatioExperiments.removeAllViews();
        
        // 重新添加配比卡片
        for (MixRatio mixRatio : selectedMixRatios) {
            addExperimentCard(mixRatio);
        }
        
        // 完成刷新
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        // 提示用户刷新成功
        Toast.makeText(requireContext(), "实验类型已刷新", Toast.LENGTH_SHORT).show();
    }

    public void setSelectedMixRatios(List<MixRatio> mixRatios) {
        if (mixRatios == null) return;
        
        // 清除已移除的配比卡片
        List<Long> toRemove = new ArrayList<>();
        for (Long mixRatioId : experimentChipGroups.keySet()) {
            boolean found = false;
            for (MixRatio mixRatio : mixRatios) {
                if (mixRatio.getId() == mixRatioId) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                toRemove.add(mixRatioId);
            }
        }
        
        for (Long mixRatioId : toRemove) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatioId);
            if (chipGroup != null) {
                View cardView = (View) chipGroup.getParent().getParent();
                containerMixRatioExperiments.removeView(cardView);
                experimentChipGroups.remove(mixRatioId);
            }
        }
        
        // 添加新的配比卡片
        for (MixRatio mixRatio : mixRatios) {
            if (!experimentChipGroups.containsKey(mixRatio.getId())) {
                addExperimentCard(mixRatio);
            }
        }
        
        this.selectedMixRatios = new ArrayList<>(mixRatios);
        checkInputValidity();
    }

    private void addExperimentCard(MixRatio mixRatio) {
        View cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_mix_ratio_experiment, containerMixRatioExperiments, false);

        // 设置卡片标题
        TextView tvTitle = cardView.findViewById(R.id.tvMixRatioTitle);
        tvTitle.setText(String.format("选择%s的实验类型", mixRatio.getName()));

        // 创建实验类型选择区域
        LinearLayout experimentContainer = cardView.findViewById(R.id.experimentContainer);
        ChipGroup chipGroup = createExperimentChipGroup();
        experimentContainer.addView(chipGroup);
        experimentChipGroups.put(mixRatio.getId(), chipGroup);

        // 添加到容器
        containerMixRatioExperiments.addView(cardView);
    }

    private ChipGroup createExperimentChipGroup() {
        ChipGroup chipGroup = new ChipGroup(requireContext());
        chipGroup.setSelectionRequired(false);
        chipGroup.setSingleSelection(false);

        // 创建一个进度条，表示正在加载数据
        ProgressBar progressBar = new ProgressBar(requireContext());
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        LinearLayout container = new LinearLayout(requireContext());
        container.addView(progressBar);
        
        // 从API获取实验类型
        ApiService apiService = ApiClient.getInstance();
        // 使用新的API调用从support_mixture_task表获取数据
        apiService.getSupportedMixtureTasks("MINTURE").enqueue(new Callback<List<SupportMixtureTaskModel>>() {
            @Override
            public void onResponse(Call<List<SupportMixtureTaskModel>> call, 
                                  Response<List<SupportMixtureTaskModel>> response) {
                if (getContext() == null) return; // Fragment已分离
                
                if (response.isSuccessful() && response.body() != null) {
                    List<SupportMixtureTaskModel> experimentTypes = response.body();
                    
                    for (SupportMixtureTaskModel type : experimentTypes) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(type.getName());
                        chip.setCheckable(true);
                        chip.setTag(type.getId()); // 存储任务ID以便后续使用
                        chipGroup.addView(chip);
                    }
                } else {
                    // API请求失败，使用默认的实验类型列表作为备用
                    String[] defaultExperimentTypes = {
                        "马歇尔稳定度试验",
                        "动稳定度试验",
                        "沥青混合料车辙实验（汉堡车辙）",
                        "沥青混合料弯曲试验",
                        "动态模量试验",
                        "沥青混合料直接拉伸循环疲劳测黏弹损伤试验",
                        "沥青混合料四点弯曲疲劳寿命试验",
                        "沥青混合料单轴压缩试验(圆柱体法)",
                        "沥青混合料劈裂试验"
                    };
                    
                    for (String type : defaultExperimentTypes) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(type);
                        chip.setCheckable(true);
                        chipGroup.addView(chip);
                    }
                    
                    Toast.makeText(requireContext(), "获取实验类型失败，使用默认列表", Toast.LENGTH_SHORT).show();
                }
                
                // 移除进度条容器
                if (container.getParent() != null) {
                    ((ViewGroup) container.getParent()).removeView(container);
                }
            }
            
            @Override
            public void onFailure(Call<List<SupportMixtureTaskModel>> call, Throwable t) {
                if (getContext() == null) return; // Fragment已分离
                
                // 请求失败，使用默认的实验类型列表作为备用
                String[] defaultExperimentTypes = {
                    "马歇尔稳定度试验",
                    "动稳定度试验",
                    "沥青混合料车辙实验（汉堡车辙）",
                    "沥青混合料弯曲试验",
                    "动态模量试验",
                    "沥青混合料直接拉伸循环疲劳测黏弹损伤试验",
                    "沥青混合料四点弯曲疲劳寿命试验",
                    "沥青混合料单轴压缩试验(圆柱体法)",
                    "沥青混合料劈裂试验"
                };
                
                for (String type : defaultExperimentTypes) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(type);
                    chip.setCheckable(true);
                    chipGroup.addView(chip);
                }
                
                // 显示错误消息
                Toast.makeText(requireContext(), "获取实验类型失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                // 移除进度条容器
                if (container.getParent() != null) {
                    ((ViewGroup) container.getParent()).removeView(container);
                }
            }
        });
        
        // 添加选择状态变化监听
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            checkInputValidity();
        });

        return chipGroup;
    }

    private void checkInputValidity() {
        // 检查是否每个配比都至少选择了一个实验类型
        boolean isValid = true;
        for (MixRatio mixRatio : selectedMixRatios) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatio.getId());
            if (chipGroup == null || chipGroup.getCheckedChipIds().isEmpty()) {
                isValid = false;
                break;
            }
        }

        // 通知 Activity 更新按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).updateNextButton();
        }
    }

    public Map<Long, List<String>> getExperimentAssignments() {
        Map<Long, List<String>> assignments = new HashMap<>();
        
        for (MixRatio mixRatio : selectedMixRatios) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatio.getId());
            if (chipGroup != null) {
                List<String> selectedTypes = new ArrayList<>();
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    View child = chipGroup.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (chip.isChecked()) {
                            selectedTypes.add(chip.getText().toString());
                            // 注意：我们可以通过 chip.getTag() 获取任务ID，如果需要
                            // 但保持与之前相同的返回格式，以避免影响现有功能
                        }
                    }
                }
                assignments.put(mixRatio.getId(), selectedTypes);
            }
        }
        
        return assignments;
    }

    public String getNotes() {
        return etNotes != null ? etNotes.getText().toString() : "";
    }

    // 设置项目ID
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    // 设置已选择的制件方式
    public void setSelectedMoldingMethods(List<MoldingMethod> methods) {
        this.selectedMoldingMethods = new ArrayList<>(methods);
    }

    // 保存任务到后端
    public void saveMixtureTask(Runnable onSuccess, Runnable onFailure) {
        if (projectId == null || projectId <= 0) {
            Toast.makeText(requireContext(), "项目ID无效", Toast.LENGTH_SHORT).show();
            if (onFailure != null) onFailure.run();
            return;
        }
        
        if (selectedMixRatios.isEmpty()) {
            Toast.makeText(requireContext(), "未选择配比", Toast.LENGTH_SHORT).show();
            if (onFailure != null) onFailure.run();
            return;
        }
        
        if (selectedMoldingMethods.isEmpty()) {
            Toast.makeText(requireContext(), "未选择制件方式", Toast.LENGTH_SHORT).show();
            if (onFailure != null) onFailure.run();
            return;
        }
        
        // 显示进度条
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // 构建请求数据
        MixtureTaskRequest request = new MixtureTaskRequest();
        request.setProjectId(String.valueOf(projectId));
        request.setRemarks(getNotes());
        
        // 从Activity获取任务名称并设置
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            String taskName = ((ExperimentTaskSetupActivity) getActivity()).getTaskName();
            request.setTaskName(taskName);
            Log.d("MixtureTask", "设置任务名称: " + taskName);
        }
        
        // 获取各配比的实验指派
        Map<Long, List<String>> experimentAssignments = getExperimentAssignments();
        Log.d("MixtureTask", "获取到各配比的实验指派: " + experimentAssignments.size() + "个配比");
        
        // 设置配比ID到实验类型的映射
        request.setMixratioAssignments(experimentAssignments);
        Log.d("MixtureTask", "设置配比ID到实验类型的映射: " + experimentAssignments);
        
        // 创建配比与制件方式的配对
        List<MixtureTaskRequest.MixratioSpecimenPair> pairs = new ArrayList<>();
        
        // 为了向后兼容，我们仍然收集所有实验指派
        List<String> allTaskAssignments = new ArrayList<>();
        
        // 检查每个制件方法是否已经关联了配比
        for (MoldingMethod method : selectedMoldingMethods) {
            Long mixRatioId = method.getMixRatioId();
            Log.d("MixtureTask", "制件方式ID: " + method.getId() + ", 已关联配比ID: " + mixRatioId);
            
            // 如果制件方法已关联了配比ID
            if (mixRatioId != null && mixRatioId > 0) {
                // 创建配对
                MixtureTaskRequest.MixratioSpecimenPair pair = 
                    new MixtureTaskRequest.MixratioSpecimenPair(mixRatioId, method.getId());
                pairs.add(pair);
                Log.d("MixtureTask", "创建配对 - 配比ID: " + pair.getMixratioId() + ", 制件方法ID: " + pair.getSpecimenId());
                
                // 添加该配比对应的实验指派到allTaskAssignments (仅为向后兼容)
                List<String> assignments = experimentAssignments.get(mixRatioId);
                if (assignments != null && !assignments.isEmpty()) {
                    Log.d("MixtureTask", "添加配比ID " + mixRatioId + " 的 " + assignments.size() + " 个实验指派");
                    for (String assignment : assignments) {
                        if (!allTaskAssignments.contains(assignment)) {
                            allTaskAssignments.add(assignment);
                        }
                    }
                }
            } else {
                // 如果制件方法没有关联配比ID，记录警告
                Log.w("MixtureTask", "制件方式ID: " + method.getId() + " 没有关联配比ID");
                
                // 为了保持向后兼容，将此制件方法与第一个配比关联
                if (!selectedMixRatios.isEmpty()) {
                    Long firstMixRatioId = selectedMixRatios.get(0).getId();
                    MixtureTaskRequest.MixratioSpecimenPair pair = 
                        new MixtureTaskRequest.MixratioSpecimenPair(firstMixRatioId, method.getId());
                    pairs.add(pair);
                    Log.d("MixtureTask", "默认创建配对 - 配比ID: " + pair.getMixratioId() + ", 制件方法ID: " + pair.getSpecimenId());
                }
            }
        }
        
        if (pairs.isEmpty()) {
            Log.w("MixtureTask", "没有创建有效的配比-制件方法配对，回退到旧方法");
            // 如果没有有效配对，回退到旧的方法，但只为每个配比选择一个制件方法
            for (MixRatio mixRatio : selectedMixRatios) {
                if (!selectedMoldingMethods.isEmpty()) {
                    MoldingMethod firstMethod = selectedMoldingMethods.get(0);
                    MixtureTaskRequest.MixratioSpecimenPair pair = 
                        new MixtureTaskRequest.MixratioSpecimenPair(mixRatio.getId(), firstMethod.getId());
                    pairs.add(pair);
                    Log.d("MixtureTask", "回退创建配对 - 配比ID: " + pair.getMixratioId() + ", 制件方法ID: " + pair.getSpecimenId());
                    
                    // 添加该配比对应的实验指派
                    List<String> assignments = experimentAssignments.get(mixRatio.getId());
                    if (assignments != null && !assignments.isEmpty()) {
                        Log.d("MixtureTask", "回退时添加配比ID " + mixRatio.getId() + " 的 " + assignments.size() + " 个实验指派");
                        for (String assignment : assignments) {
                            if (!allTaskAssignments.contains(assignment)) {
                                allTaskAssignments.add(assignment);
                            }
                        }
                    }
                }
            }
        }
        
        // 设置所有任务指派，确保不会重复添加 (仅为向后兼容)
        request.setTaskAssignments(allTaskAssignments);
        Log.d("MixtureTask", "最终添加的实验指派数量: " + allTaskAssignments.size());
        
        request.setMixratioSpecimenPairs(pairs);
        
        // 发送保存请求
        ApiService apiService = ApiClient.getInstance();
        apiService.saveMixtureTask(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, 
                                Response<ApiResponse<String>> response) {
                // 隐藏进度条
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (getContext() == null) return; // Fragment已分离
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String taskId = response.body().getData();
                    Toast.makeText(requireContext(), 
                                 "任务保存成功，任务ID: " + taskId, 
                                 Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                } else {
                    String errorMsg = "保存失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    if (onFailure != null) onFailure.run();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // 隐藏进度条
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (getContext() == null) return; // Fragment已分离
                
                Toast.makeText(requireContext(), 
                             "保存失败: " + t.getMessage(), 
                             Toast.LENGTH_SHORT).show();
                if (onFailure != null) onFailure.run();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在 Fragment 恢复时更新按钮状态
        checkInputValidity();
    }
}
