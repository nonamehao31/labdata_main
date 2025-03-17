package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.adapter.DynamicMoldingParametersAdapter;
import com.example.labdata_main.adapter.MixRatioDetailAdapter;
import com.example.labdata_main.adapter.TaskDetailMoldingAdapter;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.MixRatioDetailResponse;
import com.example.labdata_main.api.model.MixratioSpecimenPair;
import com.example.labdata_main.api.model.ProjectNameResponse;
import com.example.labdata_main.api.model.SpecimenParametersResponse;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.api.service.SpecimenService;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetMixRatioDetailFragment extends BottomSheetDialogFragment {
    private static final String TAG = "MixRatioDetailFragment";
    private static final String ARG_TASK = "task";
    
    private ExperimentTask task;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    // UI 组件
    private TextView taskNameTextView;
    private TextView projectNameTextView;
    private TextView deadlineTextView;
    private TextView tvNotes;
    private RecyclerView moldingMethodsRecyclerView;
    
    // 适配器
    private MixRatioDetailAdapter mixRatioDetailAdapter;
    private DynamicMoldingParametersAdapter dynamicAdapter;
    
    // 数据列表
    private final List<SpecimenParametersResponse> moldingParameters = new ArrayList<>();
    private final List<MixRatioDetailResponse> mixRatioDetails = new ArrayList<>();
    private final List<MixratioSpecimenPair> mixratioSpecimenPairs = new ArrayList<>();

    public static BottomSheetMixRatioDetailFragment newInstance(ExperimentTask task) {
        BottomSheetMixRatioDetailFragment fragment = new BottomSheetMixRatioDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 适配不同版本的Android API
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                task = getArguments().getParcelable(ARG_TASK, ExperimentTask.class);
            } else {
                task = getArguments().getParcelable(ARG_TASK);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_mixratio_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (task == null) return;

        // 初始化UI组件
        taskNameTextView = view.findViewById(R.id.task_name);
        projectNameTextView = view.findViewById(R.id.tvProjectName);
        deadlineTextView = view.findViewById(R.id.tvDeadline);
        tvNotes = view.findViewById(R.id.tvNotes);
        
        // 设置悬浮按钮状态
        ExtendedFloatingActionButton fabCompleteMaterial = view.findViewById(R.id.fabCompleteMaterial);
        if (task.getPreparationTime() > 0) {
            fabCompleteMaterial.setVisibility(View.GONE);
        } else {
            fabCompleteMaterial.setVisibility(View.VISIBLE);
            fabCompleteMaterial.setOnClickListener(v -> {
                if (materialCompletedListener != null) {
                    task.setPreparationTime(System.currentTimeMillis());
                    materialCompletedListener.onMaterialCompleted(task);
                    dismiss();
                }
            });
        }

        // 设置基本信息
        taskNameTextView.setText(task.getTaskName());
        
        // 设置临时项目名称，后续会通过API获取更新
        projectNameTextView.setText("项目：加载中...");
        
        // 从后端获取项目名称
        fetchProjectName(task.getId());
        
        if (task.getDeadline() > 0) {
            deadlineTextView.setText("截止日期：" + dateFormat.format(new java.util.Date(task.getDeadline())));
        } else {
            deadlineTextView.setText("无截止日期");
        }

        // 初始化制件参数区域
        moldingMethodsRecyclerView = view.findViewById(R.id.rvMoldingMethods);
        moldingMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 使用新的适配器以支持动态获取数据
        dynamicAdapter = new DynamicMoldingParametersAdapter();
        moldingMethodsRecyclerView.setAdapter(dynamicAdapter);
        
        // 从后端获取制件参数配比组合
        fetchMixratioSpecimenPairs(task.getId());
        
        // 设置配比信息
        RecyclerView rvMixRatios = view.findViewById(R.id.rvMixRatios);
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<MixRatio> mixRatios = task.getSelectedMixRatios() != null ? task.getSelectedMixRatios() : new ArrayList<>();
        mixRatioDetailAdapter = new MixRatioDetailAdapter();
        
        // 设置配比到动态适配器
        dynamicAdapter.setMixRatios(mixRatios);
        
        // 传递配比和对应的实验指派信息
        Map<Long, List<String>> experimentAssignments = task.getExperimentAssignments();
        if (experimentAssignments == null) {
            experimentAssignments = new HashMap<>();
        }
        mixRatioDetailAdapter.setData(mixRatios, experimentAssignments);
        rvMixRatios.setAdapter(mixRatioDetailAdapter);

        // 设置备注
        String notes = task.getNotes();
        if (notes != null && !notes.isEmpty()) {
            tvNotes.setText(notes);
        } else {
            tvNotes.setText("暂无备注");
        }
    }
    
    /**
     * 从后端获取项目名称
     * 
     * @param taskId 任务ID
     */
    private void fetchProjectName(long taskId) {
        // 创建项目名称查询服务
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        Call<ProjectNameResponse> call = mixtureTaskService.getProjectNameByTaskId(taskId);
        
        call.enqueue(new Callback<ProjectNameResponse>() {
            @Override
            public void onResponse(Call<ProjectNameResponse> call, Response<ProjectNameResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProjectNameResponse projectResponse = response.body();
                    String projectName = projectResponse.getProjectName();
                    
                    // 确保Fragment仍然附加到Activity
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (projectNameTextView != null) {
                                // 更新项目名称
                                projectNameTextView.setText("项目：" + projectName);
                            }
                        });
                    }
                    
                    Log.d(TAG, "成功获取项目名称: " + projectName);
                } else {
                    Log.e(TAG, "获取项目名称失败: " + response.message());
                    
                    // 请求失败时，显示未知项目
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (projectNameTextView != null) {
                                projectNameTextView.setText("项目：未知项目");
                            }
                        });
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ProjectNameResponse> call, Throwable t) {
                Log.e(TAG, "获取项目名称请求失败: " + t.getMessage());
                
                // 请求失败时，显示未知项目
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (projectNameTextView != null) {
                            projectNameTextView.setText("项目：未知项目");
                        }
                    });
                }
            }
        });
    }
    
    /**
     * 获取配比和制件组合
     * 
     * @param taskId 任务ID
     */
    private void fetchMixratioSpecimenPairs(long taskId) {
        Log.d(TAG, "获取任务配比和制件组合，任务ID: " + taskId);
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        // 同时获取配比详情信息
        fetchMixRatioDetails(taskId);
        
        mixtureTaskService.getMixratioSpecimenPairsByTaskId(taskId).enqueue(new Callback<ApiResponse<List<MixratioSpecimenPair>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixratioSpecimenPair>>> call, Response<ApiResponse<List<MixratioSpecimenPair>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<MixratioSpecimenPair> pairs = response.body().getData();
                    Log.d(TAG, "成功获取配比和制件组合: " + pairs.size() + " 个组合");
                    
                    // 保存组合列表
                    mixratioSpecimenPairs.clear();
                    mixratioSpecimenPairs.addAll(pairs);
                    
                    // 清空当前参数列表
                    moldingParameters.clear();
                    
                    // 如果没有组合数据，创建一个默认卡片
                    if (pairs.isEmpty()) {
                        SpecimenParametersResponse defaultParam = new SpecimenParametersResponse();
                        defaultParam.setCompactionMethod("暂无参数");
                        moldingParameters.add(defaultParam);
                        dynamicAdapter.setMoldingParameters(moldingParameters);
                        return;
                    }
                    
                    // 为每个组合获取制件参数
                    for (MixratioSpecimenPair pair : pairs) {
                        if (pair.getSpecimenId() != null && pair.getSpecimenId() > 0) {
                            fetchMoldingParameters(pair.getSpecimenId(), pair.getMixName());
                        } else {
                            // 创建默认参数
                            SpecimenParametersResponse tempParam = new SpecimenParametersResponse();
                            tempParam.setId(null);
                            tempParam.setMixRatioName(pair.getMixName());
                            tempParam.setCompactionMethod("制件ID无效");
                            moldingParameters.add(tempParam);
                            dynamicAdapter.setMoldingParameters(moldingParameters);
                        }
                    }
                } else {
                    Log.e(TAG, "获取配比和制件组合失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    // 创建默认卡片
                    SpecimenParametersResponse defaultParam = new SpecimenParametersResponse();
                    defaultParam.setCompactionMethod("获取参数失败");
                    moldingParameters.add(defaultParam);
                    dynamicAdapter.setMoldingParameters(moldingParameters);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MixratioSpecimenPair>>> call, Throwable t) {
                Log.e(TAG, "获取配比和制件组合请求失败: " + t.getMessage());
                t.printStackTrace();
                
                // 创建默认卡片
                SpecimenParametersResponse defaultParam = new SpecimenParametersResponse();
                defaultParam.setCompactionMethod("网络请求失败");
                moldingParameters.add(defaultParam);
                dynamicAdapter.setMoldingParameters(moldingParameters);
            }
        });
    }
    
    /**
     * 从后端获取制件参数
     * @param specimenId 制件ID
     * @param mixName 配比名称，如果有的话
     */
    private void fetchMoldingParameters(Long specimenId, String mixName) {
        Log.d(TAG, "获取制件参数，ID: " + specimenId + ", 配比名称: " + mixName);
        SpecimenService specimenService = ServiceCreator.createSpecimenService();
        
        specimenService.getSpecimenParameters(specimenId).enqueue(new Callback<ApiResponse<SpecimenParametersResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SpecimenParametersResponse>> call, Response<ApiResponse<SpecimenParametersResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    SpecimenParametersResponse parameters = response.body().getData();
                    Log.d(TAG, "成功获取制件参数: " + parameters);
                    
                    // 设置配比名称
                    parameters.setMixRatioName(mixName);
                    
                    // 添加到参数列表
                    moldingParameters.add(parameters);
                    
                    // 更新适配器
                    dynamicAdapter.setMoldingParameters(moldingParameters);
                } else {
                    Log.e(TAG, "获取制件参数失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    
                    // 创建临时参数对象
                    SpecimenParametersResponse tempParam = new SpecimenParametersResponse();
                    tempParam.setId(specimenId);
                    tempParam.setMixRatioName(mixName);
                    
                    // 添加到参数列表
                    moldingParameters.add(tempParam);
                    
                    // 更新适配器
                    dynamicAdapter.setMoldingParameters(moldingParameters);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SpecimenParametersResponse>> call, Throwable t) {
                Log.e(TAG, "获取制件参数请求失败: " + t.getMessage());
                t.printStackTrace();
                
                // 创建临时参数对象
                SpecimenParametersResponse tempParam = new SpecimenParametersResponse();
                tempParam.setId(specimenId);
                tempParam.setMixRatioName(mixName);
                
                // 添加到参数列表
                moldingParameters.add(tempParam);
                
                // 更新适配器
                dynamicAdapter.setMoldingParameters(moldingParameters);
                
                if (getContext() != null) {
                    Toast.makeText(getContext(), "获取制件参数失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 从后端获取配比详情信息
     * 
     * @param taskId 任务ID
     */
    private void fetchMixRatioDetails(long taskId) {
        Log.d(TAG, "获取配比详情，任务ID: " + taskId);
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        mixtureTaskService.getMixRatioDetailsByTaskId(taskId).enqueue(new Callback<ApiResponse<List<MixRatioDetailResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixRatioDetailResponse>>> call, Response<ApiResponse<List<MixRatioDetailResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<MixRatioDetailResponse> details = response.body().getData();
                    Log.d(TAG, "成功获取配比详情: " + details.size() + " 个配比");
                    
                    // 清空并保存数据
                    mixRatioDetails.clear();
                    mixRatioDetails.addAll(details);
                    
                    // 将API返回的配比详情转换为前端使用的MixRatio对象
                    List<MixRatio> mixRatios = convertToMixRatios(details);
                    
                    // 直接使用已转换的配比数据更新适配器
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // 更新动态适配器的配比数据
                            dynamicAdapter.setMixRatios(mixRatios);
                            
                            // 更新配比适配器的数据
                            Map<Long, List<String>> currentAssignments = mixRatioDetailAdapter.getExperimentAssignments();
                            mixRatioDetailAdapter.setData(mixRatios, currentAssignments);
                            
                            Log.d(TAG, "已更新UI适配器，配比数量: " + mixRatios.size());
                        });
                    }
                    
                    // 获取实验指派信息
                    fetchTaskAssignments(mixRatios);
                    
                } else {
                    Log.e(TAG, "获取配比详情失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    if (isAdded() && getActivity() != null && getContext() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "获取配比详情失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<MixRatioDetailResponse>>> call, Throwable t) {
                Log.e(TAG, "获取配比详情请求失败: " + t.getMessage());
                t.printStackTrace();
                
                if (isAdded() && getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "获取配比详情网络请求失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    /**
     * 获取任务的实验指派信息
     * 
     * @param mixRatios 配比列表
     */
    private void fetchTaskAssignments(List<MixRatio> mixRatios) {
        // 从Arguments中安全地获取任务对象
        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "Arguments为空，无法获取任务ID");
            return;
        }
        
        ExperimentTask task;
        // 适配不同版本的Android API
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            task = args.getParcelable(ARG_TASK, ExperimentTask.class);
        } else {
            task = args.getParcelable(ARG_TASK);
        }
        
        if (task == null) {
            Log.e(TAG, "无法从Arguments中获取任务对象");
            return;
        }
        
        long taskId = task.getId();
        Log.d(TAG, "获取任务ID: " + taskId + " 的实验指派信息 (数据库主键)");
        
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        mixtureTaskService.getTaskAssignmentsByTaskId(taskId).enqueue(new Callback<ApiResponse<Map<Long, List<String>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<Long, List<String>>>> call, Response<ApiResponse<Map<Long, List<String>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    Map<Long, List<String>> assignments = response.body().getData();
                    Log.d(TAG, "成功获取实验指派信息: " + assignments.size() + " 个配比");
                    
                    // 更新适配器数据
                    mixRatioDetailAdapter.setData(mixRatios, assignments);
                } else {
                    Log.e(TAG, "获取实验指派信息失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<Long, List<String>>>> call, Throwable t) {
                Log.e(TAG, "获取实验指派信息请求失败: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
    
    /**
     * 将API返回的配比详情转换为前端使用的MixRatio对象
     * 
     * @param details API返回的配比详情
     * @return 前端使用的MixRatio对象列表
     */
    private List<MixRatio> convertToMixRatios(List<MixRatioDetailResponse> details) {
        List<MixRatio> mixRatios = new ArrayList<>();
        
        for (MixRatioDetailResponse detail : details) {
            MixRatio mixRatio = new MixRatio();
            mixRatio.setId(detail.getMixratioId());
            mixRatio.setName(detail.getMixName());
            
            // 创建材料列表
            List<MaterialItem> materials = new ArrayList<>();
            
            // 添加沥青材料
            if (detail.getAsphaltName() != null && detail.getAsphaltPercentage() != null) {
                MaterialItem asphalt = new MaterialItem();
                asphalt.setMaterialId(1L); // 假设ID
                asphalt.setName(detail.getAsphaltName());
                asphalt.setAmount(detail.getAsphaltPercentage().toString() + "%");
                materials.add(asphalt);
            }
            
            // 添加砂料
            if (detail.getSandName() != null && detail.getSandPercentage() != null) {
                MaterialItem sand = new MaterialItem();
                sand.setMaterialId(2L); // 假设ID
                sand.setName(detail.getSandName());
                sand.setAmount(detail.getSandPercentage().toString() + "%");
                materials.add(sand);
            }
            
            // 添加石料
            if (detail.getStoneName() != null && detail.getStonePercentage() != null) {
                MaterialItem stone = new MaterialItem();
                stone.setMaterialId(3L); // 假设ID
                stone.setName(detail.getStoneName());
                stone.setAmount(detail.getStonePercentage().toString() + "%");
                materials.add(stone);
            }
            
            // 设置材料列表到配比对象
            mixRatio.setMaterials(materials);
            
            mixRatios.add(mixRatio);
            
            // 记录日志
            Log.d(TAG, "转换配比: " + detail.getMixName() + ", 材料数量: " + materials.size());
        }
        
        return mixRatios;
    }
    
    public interface OnMaterialCompletedListener {
        void onMaterialCompleted(ExperimentTask task);
    }
    
    private OnMaterialCompletedListener materialCompletedListener;

    public void setOnMaterialCompletedListener(OnMaterialCompletedListener listener) {
        this.materialCompletedListener = listener;
    }
}
