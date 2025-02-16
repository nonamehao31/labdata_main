package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.adapter.AsphaltExperimentAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BottomSheetAsphaltTaskDetailFragment extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final String ARG_SHOW_ACCEPT_BUTTON = "show_accept_button";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ExperimentTask task;
    private OnTaskActionListener listener;
    private boolean showAcceptButton = true; // 默认显示按钮

    public interface OnTaskActionListener {
        void onTaskAccepted(ExperimentTask task);
        void onTaskRejected(ExperimentTask task);
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
        AsphaltExperimentAdapter adapter = new AsphaltExperimentAdapter(parseExperimentData());
        asphaltExperimentRecyclerView.setAdapter(adapter);

        // 根据 showAcceptButton 参数控制按钮显示
        if (!showAcceptButton) {
            fabAcceptTask.setVisibility(View.GONE);
        } else {
            // 设置接受按钮点击事件
            fabAcceptTask.setOnClickListener(v -> {
                if (listener != null) {
                    task.setStatus("已接受");
                    listener.onTaskAccepted(task);
                }
                dismiss();
            });

            // 如果任务已经有状态，禁用按钮
            if (task.getStatus() != null && !task.getStatus().equals("未接受")) {
                fabAcceptTask.setEnabled(false);
            }
        }
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
