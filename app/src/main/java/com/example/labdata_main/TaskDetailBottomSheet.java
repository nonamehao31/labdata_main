package com.example.labdata_main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.MixRatioDetailAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TaskAcceptListener taskAcceptListener;

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
        TextView projectName = view.findViewById(R.id.tvProjectName);
        TextView deadline = view.findViewById(R.id.tvDeadline);
        
        taskName.setText(task.getTaskName());
        projectName.setText("项目：" + task.getProjectName());
        if (task.getDeadline() > 0) {
            deadline.setText("截止日期：" + DATE_FORMAT.format(new java.util.Date(task.getDeadline())));
        } else {
            deadline.setText("无截止日期");
        }

        // 设置制件参数信息
        TextView mixingTemperature = view.findViewById(R.id.tvMixingTemperature);
        TextView mixingSpeed = view.findViewById(R.id.tvMixingSpeed);
        TextView mixingTime = view.findViewById(R.id.tvMixingTime);
        TextView compactionMethod = view.findViewById(R.id.tvCompactionMethodValue);

        String methodStr = task.getMoldingMethod();
        if (methodStr != null && !methodStr.isEmpty()) {
            String[] parts = methodStr.split("\\|");
            float temp = 0, speed = 0, time = 0;
            String method = "";
            
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "temp":
                            temp = Float.parseFloat(value);
                            break;
                        case "speed":
                            speed = Float.parseFloat(value);
                            break;
                        case "time":
                            time = Float.parseFloat(value);
                            break;
                        case "method":
                            method = value;
                            break;
                    }
                }
            }
            
            mixingTemperature.setText(String.format("拌合温度：%.1f℃", temp));
            mixingSpeed.setText(String.format("拌合速度：%.1f rpm", speed));
            mixingTime.setText(String.format("拌合时间：%.1f min", time));
            compactionMethod.setText("压实方式：" + method);
        }

        // 设置配比信息
        RecyclerView rvMixRatios = view.findViewById(R.id.rvMixRatios);
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<MixRatio> mixRatios = task.getSelectedMixRatios() != null ? task.getSelectedMixRatios() : new ArrayList<>();
        MixRatioDetailAdapter adapter = new MixRatioDetailAdapter();
        
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
}
