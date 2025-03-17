package com.example.labdata_main.fragment;

import android.os.Bundle;
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
import com.example.labdata_main.adapter.AsphaltExperimentAdapter;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.AsphaltDetailResponse;
import com.example.labdata_main.api.model.AsphaltDetailResponse.AsphaltInfo;
import com.example.labdata_main.api.service.AsphaltTaskService;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetAsphaltTaskDetailFragment extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final String ARG_SHOW_ACCEPT_BUTTON = "show_accept_button";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ExperimentTask task;
    private OnTaskActionListener listener;
    private boolean showAcceptButton = true; // 默认显示按钮
    private AsphaltTaskService asphaltTaskService;
    private SharedPrefsManager sharedPrefsManager;

    public interface OnTaskActionListener {
        void onTaskAccepted(ExperimentTask task);
        void onTaskRejected(ExperimentTask task);
        void onTaskCompleted(ExperimentTask task);
    }

    public static BottomSheetAsphaltTaskDetailFragment newInstance(ExperimentTask task, boolean showAcceptButton) {
        BottomSheetAsphaltTaskDetailFragment fragment = new BottomSheetAsphaltTaskDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        args.putBoolean(ARG_SHOW_ACCEPT_BUTTON, showAcceptButton);
        fragment.setArguments(args);
        return fragment;
    }

    // 保持原有的 newInstance 方法以保持向后兼容
    public static BottomSheetAsphaltTaskDetailFragment newInstance(ExperimentTask task) {
        return newInstance(task, true);
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable(ARG_TASK);
            showAcceptButton = getArguments().getBoolean(ARG_SHOW_ACCEPT_BUTTON, true);
        }
        
        // 初始化API服务
        asphaltTaskService = ApiClient.getClient().create(AsphaltTaskService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_asphalt_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 在视图创建后初始化 SharedPrefsManager
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        TextView taskNameText = view.findViewById(R.id.taskNameText);
        TextView taskDeadlineText = view.findViewById(R.id.taskDeadlineText);
        RecyclerView asphaltExperimentRecyclerView = view.findViewById(R.id.asphaltExperimentRecyclerView);
        ExtendedFloatingActionButton fabAcceptTask = view.findViewById(R.id.fabAcceptTask);

        // 设置任务名称
        taskNameText.setText(task.getTaskName());
        
        // 设置截止日期
        long deadline = task.getDeadline();
        if (deadline > 0) {
            taskDeadlineText.setText(DATE_FORMAT.format(new Date(deadline)));
        } else {
            taskDeadlineText.setText("无截止日期");
        }

        // 设置RecyclerView
        asphaltExperimentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 优先使用UUID格式的任务分配ID，如果不存在再尝试使用任务ID
        if (task.getTaskAssignmentId() != null && !task.getTaskAssignmentId().isEmpty()) {
            fetchAsphaltDetail(task.getTaskAssignmentId(), asphaltExperimentRecyclerView);
        } else if (task.getTaskId() != null && !task.getTaskId().isEmpty()) {
            // 任务分配ID为空时，记录警告并回退到使用本地数据
            Toast.makeText(requireContext(), "无法获取任务UUID，将使用本地数据", Toast.LENGTH_SHORT).show();
            AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(parseExperimentData());
            asphaltExperimentRecyclerView.setAdapter(adapter);
        } else {
            // 使用本地数据
            AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(parseExperimentData());
            asphaltExperimentRecyclerView.setAdapter(adapter);
        }

        // 根据 showAcceptButton 参数控制按钮显示
        if (!showAcceptButton) {
            fabAcceptTask.setVisibility(View.GONE);
        } else {
            // 设置接受按钮点击事件
            fabAcceptTask.setOnClickListener(v -> {
                // 禁用按钮，防止重复点击
                fabAcceptTask.setEnabled(false);
                
                // 直接通知监听器任务已接受，由OverviewFragment处理API调用
                if (listener != null) {
                    listener.onTaskAccepted(task);
                    Toast.makeText(requireContext(), "正在处理任务接受请求...", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "错误：监听器未设置", Toast.LENGTH_SHORT).show();
                    fabAcceptTask.setEnabled(true);
                }
            });

            // 如果任务不是CREATED状态，禁用按钮
            if (task.getStatus() != null && !task.getStatus().equals("CREATED")) {
                fabAcceptTask.setEnabled(false);
            }
        }
    }

    private void fetchAsphaltDetail(String taskId, RecyclerView recyclerView) {
        Call<ApiResponse<AsphaltDetailResponse>> call = asphaltTaskService.getAsphaltDetailByTaskId(taskId);
        call.enqueue(new Callback<ApiResponse<AsphaltDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AsphaltDetailResponse>> call, Response<ApiResponse<AsphaltDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AsphaltDetailResponse detailResponse = response.body().getData();
                    // 处理详细信息并更新RecyclerView
                    List<AsphaltExperimentItem> items = parseAsphaltDetail(detailResponse);
                    AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(items);
                    recyclerView.setAdapter(adapter);
                } else {
                    // 处理错误
                    Toast.makeText(requireContext(), "获取详细信息失败", Toast.LENGTH_SHORT).show();
                    // 回退到使用本地数据
                    AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(parseExperimentData());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AsphaltDetailResponse>> call, Throwable t) {
                // 处理错误
                Toast.makeText(requireContext(), "获取详细信息失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // 回退到使用本地数据
                AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(parseExperimentData());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private List<AsphaltExperimentItem> parseAsphaltDetail(AsphaltDetailResponse detailResponse) {
        List<AsphaltExperimentItem> items = new ArrayList<>();
        
        // 1. 处理沥青信息
        if (detailResponse.getAsphaltInfoList() != null && !detailResponse.getAsphaltInfoList().isEmpty()) {
            StringBuilder asphaltInfoBuilder = new StringBuilder();
            for (AsphaltInfo asphaltInfo : detailResponse.getAsphaltInfoList()) {
                asphaltInfoBuilder.append("• 供应商: ").append(asphaltInfo.getAsphaltSupplier()).append("\n");
                asphaltInfoBuilder.append("• 标号: ").append(asphaltInfo.getAsphaltGrade()).append("\n");
                asphaltInfoBuilder.append("• 类型: ").append(asphaltInfo.getAsphaltCatalog()).append("\n\n");
            }
            
            if (asphaltInfoBuilder.length() > 0) {
                items.add(new AsphaltExperimentItem(
                    "沥青信息",
                    asphaltInfoBuilder.toString().trim()
                ));
            }
        }
        
        // 2. 处理实验指派信息
        if (detailResponse.getExperimentAssignments() != null && !detailResponse.getExperimentAssignments().isEmpty()) {
            StringBuilder experimentInfoBuilder = new StringBuilder();
            
            for (Map.Entry<Long, List<String>> entry : detailResponse.getExperimentAssignments().entrySet()) {
                Long asphaltId = entry.getKey();
                List<String> assignments = entry.getValue();
                
                if (assignments != null && !assignments.isEmpty()) {
                    // 找到对应的沥青信息
                    String asphaltName = "沥青 #" + asphaltId;
                    if (detailResponse.getAsphaltInfoList() != null) {
                        for (AsphaltInfo info : detailResponse.getAsphaltInfoList()) {
                            if (info.getAsphaltId().equals(asphaltId)) {
                                asphaltName = info.getAsphaltGrade() + " (" + info.getAsphaltSupplier() + ")";
                                break;
                            }
                        }
                    }
                    
                    experimentInfoBuilder.append(asphaltName).append(":\n");
                    for (String assignment : assignments) {
                        experimentInfoBuilder.append("• ").append(assignment).append("\n");
                    }
                    experimentInfoBuilder.append("\n");
                }
            }
            
            if (experimentInfoBuilder.length() > 0) {
                items.add(new AsphaltExperimentItem(
                    "实验指派",
                    experimentInfoBuilder.toString().trim()
                ));
            }
        }
        
        // 如果没有找到任何数据，添加一个提示信息
        if (items.isEmpty()) {
            items.add(new AsphaltExperimentItem(
                "提示",
                "暂无实验信息"
            ));
        }
        
        return items;
    }

    private List<AsphaltExperimentItem> parseExperimentData() {
        List<AsphaltExperimentItem> items = new ArrayList<>();
        
        // 1. 处理实验指派信息
        Map<Long, List<String>> experimentAssignments = task.getExperimentAssignments();
        if (experimentAssignments != null && !experimentAssignments.isEmpty()) {
            StringBuilder experimentInfo = new StringBuilder();
            
            // 遍历每个配比的实验指派
            for (Map.Entry<Long, List<String>> entry : experimentAssignments.entrySet()) {
                List<String> experiments = entry.getValue();
                if (experiments != null && !experiments.isEmpty()) {
                    for (String experiment : experiments) {
                        experimentInfo.append("• ").append(experiment).append("\n");
                    }
                }
            }
            
            if (experimentInfo.length() > 0) {
                items.add(new AsphaltExperimentItem(
                    "实验指派",
                    experimentInfo.toString().trim()
                ));
            }
        }

        // 2. 处理 notes 中的信息
        String notes = task.getNotes();
        if (notes != null && !notes.isEmpty()) {
            // 分割主要部分
            String[] mainSections = notes.split("---+\\s*\\n");
            
            for (String section : mainSections) {
                section = section.trim();
                
                // 处理沥青信息部分
                if (section.startsWith("沥青信息")) {
                    String[] lines = section.split("\\n");
                    StringBuilder asphaltInfo = new StringBuilder();
                    
                    // 跳过标题行，从第二行开始处理
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (!line.isEmpty()) {
                            asphaltInfo.append(line).append("\n");
                        }
                    }
                    
                    if (asphaltInfo.length() > 0) {
                        items.add(new AsphaltExperimentItem(
                            "沥青信息",
                            asphaltInfo.toString().trim()
                        ));
                    }
                }
            }
        }
        
        // 3. 添加混合料配比信息
        List<MixRatio> mixRatios = task.getSelectedMixRatios();
        if (mixRatios != null && !mixRatios.isEmpty()) {
            StringBuilder mixRatioInfo = new StringBuilder();
            for (MixRatio ratio : mixRatios) {
                mixRatioInfo.append("• 配比 ").append(ratio.getName()).append("\n");
            }
            
            if (mixRatioInfo.length() > 0) {
                items.add(new AsphaltExperimentItem(
                    "配比信息",
                    mixRatioInfo.toString().trim()
                ));
            }
        }
        
        // 如果没有找到任何数据，添加一个提示信息
        if (items.isEmpty()) {
            items.add(new AsphaltExperimentItem(
                "提示",
                "暂无实验信息"
            ));
        }
        
        return items;
    }

    // 用于RecyclerView的数据类
    public static class AsphaltExperimentItem {
        private final String title;
        private final String content;

        public AsphaltExperimentItem(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
