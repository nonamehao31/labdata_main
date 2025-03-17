package com.example.labdata_main;

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
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final String TAG = "TaskDetailBottomSheet";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TaskAcceptListener taskAcceptListener;
    private DynamicMoldingParametersAdapter dynamicAdapter;
    private MixRatioDetailAdapter mixRatioDetailAdapter;
    private List<SpecimenParametersResponse> moldingParameters = new ArrayList<>();
    private List<MixRatioDetailResponse> mixRatioDetails = new ArrayList<>();
    private TextView projectNameTextView;
    private TextView deadlineTextView;
    private TextView taskNameTextView;
    private TextView tvNotes; // 备注文本视图
    private RecyclerView moldingMethodsRecyclerView;
    // 用于存储配比制件组合的列表
    private List<MixratioSpecimenPair> mixratioSpecimenPairs = new ArrayList<>();

    public interface TaskAcceptListener {
        void onTaskAccepted(ExperimentTask task);
    }

    public void setTaskAcceptListener(TaskAcceptListener listener) {
        this.taskAcceptListener = listener;
    }

    public static TaskDetailBottomSheet newInstance(ExperimentTask task) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExperimentTask task;
        // 适配不同版本的Android API
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            task = getArguments().getParcelable(ARG_TASK, ExperimentTask.class);
        } else {
            task = getArguments().getParcelable(ARG_TASK);
        }
        
        if (task == null) return;

        // 设置基本信息
        taskNameTextView = view.findViewById(R.id.task_name);
        projectNameTextView = view.findViewById(R.id.tvProjectName);
        deadlineTextView = view.findViewById(R.id.tvDeadline);
        tvNotes = view.findViewById(R.id.tvNotes);
        
        taskNameTextView.setText(task.getTaskName());
        
        // 设置临时项目名称，后续会通过API获取更新
        projectNameTextView.setText("项目：加载中...");
        
        // 从后端获取项目名称
        fetchProjectName(task.getId());
        
        if (task.getDeadline() > 0) {
            deadlineTextView.setText("截止日期：" + DATE_FORMAT.format(new java.util.Date(task.getDeadline())));
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

        // 获取任务备注信息
        fetchTaskRemarks(task.getId());

        // 设置接受任务按钮点击事件
        ExtendedFloatingActionButton fabAcceptTask = view.findViewById(R.id.fabAcceptTask);
        fabAcceptTask.setOnClickListener(v -> {
            // 显示加载提示
            Toast.makeText(requireContext(), "正在接受任务...", Toast.LENGTH_SHORT).show();
            
            // 获取当前用户名
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(requireContext());
            String acceptor = sharedPrefsManager.getUserName();
            
            // 获取当前时间戳
            long acceptTime = System.currentTimeMillis();
            
            Log.d(TAG, "接受任务: " + task.getId() + ", 接受人: " + acceptor + ", 接受时间: " + acceptTime);
            
            // 调用API接受任务
            MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
            mixtureTaskService.acceptTask(task.getId(), acceptor, acceptTime).enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess() 
                            && response.body().getData() != null && response.body().getData()) {
                        Log.d(TAG, "成功接受任务: " + task.getId());
                        
                        // 更新本地任务状态
                        task.setStatus("已接受");
                        
                        // 回调通知OverviewFragment更新
                        if (taskAcceptListener != null) {
                            taskAcceptListener.onTaskAccepted(task);
                        }
                        
                        // 显示成功消息
                        Toast.makeText(requireContext(), "成功接受任务", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "接受任务失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                        Toast.makeText(requireContext(), "接受任务失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    Log.e(TAG, "接受任务请求失败: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "网络错误，请重试", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
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
     * 从后端获取mixratio_id和specimen_id的组合
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
                
                if (getContext() != null) {
                    Toast.makeText(getContext(), "获取配比和制件组合失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 从后端获取配比详细信息
     * 
     * @param taskId 任务ID
     */
    private void fetchMixRatioDetails(long taskId) {
        Log.d(TAG, "获取任务配比详情，任务ID: " + taskId);
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        mixtureTaskService.getMixRatioDetailsByTaskId(taskId).enqueue(new Callback<ApiResponse<List<MixRatioDetailResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixRatioDetailResponse>>> call, Response<ApiResponse<List<MixRatioDetailResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    List<MixRatioDetailResponse> details = response.body().getData();
                    Log.d(TAG, "成功获取配比详情: " + details.size() + " 个配比");
                    
                    // 保存配比详情
                    mixRatioDetails.clear();
                    mixRatioDetails.addAll(details);
                    
                    // 将API返回的配比详情转换为前端使用的MixRatio对象
                    List<MixRatio> mixRatios = convertToMixRatios(details);
                    
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
        Log.d(TAG, "任务详情 - ID: " + taskId + ", 任务ID字符串: " + task.getTaskId() + ", 名称: " + task.getTaskName());
        
        MixtureTaskService service = ServiceCreator.createMixtureTaskService();
        service.getTaskAssignmentsByTaskId(taskId).enqueue(new Callback<ApiResponse<Map<Long, List<String>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<Long, List<String>>>> call, Response<ApiResponse<Map<Long, List<String>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<Long, List<String>>> apiResponse = response.body();
                    Log.d(TAG, "实验指派信息获取成功，状态: " + apiResponse.isSuccess() + ", 消息: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        Map<Long, List<String>> assignments = apiResponse.getData();
                        Log.d(TAG, "获取到 " + assignments.size() + " 个配比的实验指派信息");
                        
                        // 详细记录每个配比的实验指派信息
                        for (Map.Entry<Long, List<String>> entry : assignments.entrySet()) {
                            Log.d(TAG, "配比ID: " + entry.getKey() + ", 实验指派: " + entry.getValue().toString());
                        }
                        
                        // 初始化适配器并显示数据
                        setupMixRatioDetailAdapter(mixRatios, assignments);
                    } else {
                        // 即使没有实验指派信息，也要显示配比详情
                        setupMixRatioDetailAdapter(mixRatios, new HashMap<>());
                        Log.w(TAG, "未获取到实验指派信息或信息为空");
                    }
                } else {
                    // 即使实验指派信息获取失败，也要显示配比详情
                    setupMixRatioDetailAdapter(mixRatios, new HashMap<>());
                    Log.e(TAG, "获取实验指派信息请求失败，状态码: " + (response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<Long, List<String>>>> call, Throwable t) {
                Log.e(TAG, "获取实验指派信息请求失败: " + t.getMessage());
                t.printStackTrace();
                
                // 即使实验指派信息获取失败，也要显示配比详情
                setupMixRatioDetailAdapter(mixRatios, new HashMap<>());
                
                if (isAdded() && getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "获取实验指派信息网络请求失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    /**
     * 设置配比详情适配器
     * 
     * @param mixRatios 配比列表
     * @param experimentAssignments 实验指派信息
     */
    private void setupMixRatioDetailAdapter(List<MixRatio> mixRatios, Map<Long, List<String>> experimentAssignments) {
        if (getActivity() == null || !isAdded()) {
            Log.e(TAG, "Fragment未附加到Activity，无法设置适配器");
            return;
        }
        
        getActivity().runOnUiThread(() -> {
            if (mixRatios != null && !mixRatios.isEmpty()) {
                mixRatioDetailAdapter.setData(mixRatios, experimentAssignments);
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
            mixRatio.setDescription("配比ID: " + detail.getMixratioId());
            
            // 创建材料列表
            List<MaterialItem> materials = new ArrayList<>();
            
            // 添加沥青
            if (detail.getAsphaltName() != null && detail.getAsphaltPercentage() != null) {
                MaterialItem asphalt = new MaterialItem();
                asphalt.setName(detail.getAsphaltName());
                asphalt.setAmount(detail.getAsphaltPercentage().toString() + "%");
                materials.add(asphalt);
            }
            
            // 添加沙子
            if (detail.getSandName() != null && detail.getSandPercentage() != null) {
                MaterialItem sand = new MaterialItem();
                sand.setName(detail.getSandName() + (detail.getSandGradation() != null ? " (" + detail.getSandGradation() + ")" : ""));
                sand.setAmount(detail.getSandPercentage().toString() + "%");
                materials.add(sand);
            }
            
            // 添加石子
            if (detail.getStoneName() != null && detail.getStonePercentage() != null) {
                MaterialItem stone = new MaterialItem();
                stone.setName(detail.getStoneName() + (detail.getStoneGradation() != null ? " (" + detail.getStoneGradation() + ")" : ""));
                stone.setAmount(detail.getStonePercentage().toString() + "%");
                materials.add(stone);
            }
            
            // 设置材料列表
            mixRatio.setMaterials(materials);
            
            // 计算总量
            float total = 0;
            for (MaterialItem item : materials) {
                // 从百分比字符串中提取数值
                String amountStr = item.getAmount();
                if (amountStr != null && amountStr.endsWith("%")) {
                    try {
                        float value = Float.parseFloat(amountStr.substring(0, amountStr.length() - 1));
                        total += value;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "解析百分比值失败: " + amountStr, e);
                    }
                }
            }
            mixRatio.setTotalAmount(String.format(Locale.getDefault(), "%.2f", total) + "%");
            
            mixRatios.add(mixRatio);
        }
        
        return mixRatios;
    }
    
    /**
     * 从后端获取任务备注信息
     * 
     * @param taskId 任务ID
     */
    private void fetchTaskRemarks(long taskId) {
        Log.d(TAG, "获取任务备注信息，任务ID: " + taskId);
        MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();
        
        mixtureTaskService.getTaskRemarksByTaskId(taskId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    String remarks = response.body().getData();
                    Log.d(TAG, "成功获取任务备注信息: " + remarks);
                    
                    // 更新备注文本视图
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (tvNotes != null) {
                                tvNotes.setText("备注: " + remarks);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "获取任务备注信息失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    
                    // 请求失败时，显示无备注
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (tvNotes != null) {
                                tvNotes.setText("备注: 无");
                            }
                        });
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "获取任务备注信息请求失败: " + t.getMessage());
                
                // 请求失败时，显示无备注
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvNotes != null) {
                            tvNotes.setText("备注: 无");
                        }
                    });
                }
            }
        });
    }
}
