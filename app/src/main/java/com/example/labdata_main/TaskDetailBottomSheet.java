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
import com.example.labdata_main.api.model.ProjectNameResponse;
import com.example.labdata_main.api.model.SpecimenParametersResponse;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.api.service.SpecimenService;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<SpecimenParametersResponse> moldingParameters = new ArrayList<>();
    private TextView projectNameTextView;

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

        ExperimentTask task = getArguments().getParcelable(ARG_TASK);
        if (task == null) return;

        // 设置基本信息
        TextView taskName = view.findViewById(R.id.task_name);
        projectNameTextView = view.findViewById(R.id.tvProjectName);
        TextView deadline = view.findViewById(R.id.tvDeadline);
        
        taskName.setText(task.getTaskName());
        
        // 设置临时项目名称，后续会通过API获取更新
        projectNameTextView.setText("项目：加载中...");
        
        // 从后端获取项目名称
        fetchProjectName(task.getId());
        
        if (task.getDeadline() > 0) {
            deadline.setText("截止日期：" + DATE_FORMAT.format(new java.util.Date(task.getDeadline())));
        } else {
            deadline.setText("无截止日期");
        }

        // 初始化制件参数区域
        RecyclerView rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 使用新的适配器以支持动态获取数据
        dynamicAdapter = new DynamicMoldingParametersAdapter();
        rvMoldingMethods.setAdapter(dynamicAdapter);
        
        String moldingMethodStr = task.getMoldingMethod();
        if (moldingMethodStr != null && !moldingMethodStr.isEmpty()) {
            try {
                Gson gson = new Gson();
                List<MoldingMethod> methods = new ArrayList<>();
                
                if (moldingMethodStr.trim().startsWith("[")) {
                    // 如果是JSON数组格式，直接解析
                    methods = gson.fromJson(moldingMethodStr, new TypeToken<List<MoldingMethod>>(){}.getType());
                } else if (moldingMethodStr.trim().startsWith("{")) {
                    // 如果是单个JSON对象，转换为数组
                    MoldingMethod singleMethod = gson.fromJson(moldingMethodStr, MoldingMethod.class);
                    if (singleMethod != null) {
                        methods.add(singleMethod);
                    }
                } else {
                    // 如果是老格式（temp=160|speed=60|time=90|method=振动压实）
                    MoldingMethod method = new MoldingMethod();
                    String[] parts = moldingMethodStr.split("\\|");
                    for (String part : parts) {
                        String[] keyValue = part.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            try {
                                switch (key) {
                                    case "temp":
                                        method.setMixingTemperature(Float.parseFloat(value));
                                        break;
                                    case "speed":
                                        method.setMixingSpeed(Float.parseFloat(value));
                                        break;
                                    case "time":
                                        method.setMixingTime(Float.parseFloat(value));
                                        break;
                                    case "method":
                                        method.setCompactionMethod(value);
                                        break;
                                }
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing number: " + value);
                            }
                        }
                    }
                    methods.add(method);
                }
                
                // 从后端获取制件参数
                for (MoldingMethod method : methods) {
                    if (method.getId() > 0) {
                        Log.d(TAG, "开始获取制件方法ID: " + method.getId() + " 的参数");
                        fetchMoldingParameters(method.getId());
                    } else {
                        Log.d(TAG, "制件方法ID为空或无效，使用本地数据");
                        // 如果没有ID，使用本地数据创建临时参数对象
                        SpecimenParametersResponse tempParam = new SpecimenParametersResponse();
                        tempParam.setId(null);
                        tempParam.setMixingTemperature(method.getMixingTemperature());
                        tempParam.setMixingSpeed(method.getMixingSpeed());
                        tempParam.setMixingTime((int) method.getMixingTime());
                        tempParam.setCompactionMethod(method.getCompactionMethod());
                        
                        // 添加到参数列表
                        moldingParameters.add(tempParam);
                        
                        // 更新适配器
                        dynamicAdapter.setMoldingParameters(moldingParameters);
                    }
                }
                
                // 如果没有制件方法或为空，显示空数据
                if (methods.isEmpty()) {
                    moldingParameters.clear();
                    dynamicAdapter.setMoldingParameters(moldingParameters);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error parsing molding method: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 设置配比信息
        RecyclerView rvMixRatios = view.findViewById(R.id.rvMixRatios);
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<MixRatio> mixRatios = task.getSelectedMixRatios() != null ? task.getSelectedMixRatios() : new ArrayList<>();
        MixRatioDetailAdapter adapter = new MixRatioDetailAdapter();
        
        // 设置配比到动态适配器
        dynamicAdapter.setMixRatios(mixRatios);
        
        // 传递配比和对应的实验指派信息
        Map<Long, List<String>> experimentAssignments = task.getExperimentAssignments();
        if (experimentAssignments == null) {
            experimentAssignments = new HashMap<>();
        }
        adapter.setData(mixRatios, experimentAssignments);
        rvMixRatios.setAdapter(adapter);

        // 设置备注信息
        TextView notes = view.findViewById(R.id.tvNotes);
        if (task.getNotes() != null && !task.getNotes().isEmpty()) {
            notes.setText(task.getNotes());
        } else {
            notes.setText("无备注");
        }

        // 设置接受任务按钮点击事件
        ExtendedFloatingActionButton fabAcceptTask = view.findViewById(R.id.fabAcceptTask);
        fabAcceptTask.setOnClickListener(v -> {
            if (taskAcceptListener != null) {
                taskAcceptListener.onTaskAccepted(task);
            }
            dismiss();
        });
    }
    
    /**
     * 从后端获取制件参数
     * @param specimenId 制件ID
     */
    private void fetchMoldingParameters(Long specimenId) {
        Log.d(TAG, "获取制件参数，ID: " + specimenId);
        SpecimenService specimenService = ServiceCreator.createSpecimenService();
        
        specimenService.getSpecimenParameters(specimenId).enqueue(new Callback<ApiResponse<SpecimenParametersResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SpecimenParametersResponse>> call, Response<ApiResponse<SpecimenParametersResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    SpecimenParametersResponse parameters = response.body().getData();
                    Log.d(TAG, "成功获取制件参数: " + parameters);
                    
                    // 添加到参数列表
                    moldingParameters.add(parameters);
                    
                    // 更新适配器
                    dynamicAdapter.setMoldingParameters(moldingParameters);
                } else {
                    Log.e(TAG, "获取制件参数失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                    
                    // 创建临时参数对象
                    SpecimenParametersResponse tempParam = new SpecimenParametersResponse();
                    tempParam.setId(specimenId);
                    
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
}
