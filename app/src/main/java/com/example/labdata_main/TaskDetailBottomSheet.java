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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
            deadline.setText("截止日期：" + DATE_FORMAT.format(new Date(task.getDeadline())));
        } else {
            deadline.setText("无截止日期");
        }

        // 设置制件方式信息
        TextView moldingMethod = view.findViewById(R.id.tvMoldingMethod);
        TextView mixingTemperature = view.findViewById(R.id.tvMixingTemperature);
        TextView mixingSpeed = view.findViewById(R.id.tvMixingSpeed);
        TextView mixingTime = view.findViewById(R.id.tvMixingTime);
        TextView compactionMethod = view.findViewById(R.id.tvCompactionMethod);

        String methodStr = task.getMoldingMethod();
        if (methodStr == null || methodStr.isEmpty()) {
            moldingMethod.setText("制件方式：未设置");
            mixingTemperature.setVisibility(View.GONE);
            mixingSpeed.setVisibility(View.GONE);
            mixingTime.setVisibility(View.GONE);
            compactionMethod.setVisibility(View.GONE);
        } else {
            String[] parts = methodStr.split("\\|");
            if (parts.length >= 5) {
                // 第一部分是试块尺寸信息
                moldingMethod.setText("制件方式：" + parts[0].trim());
                
                // 解析其他参数
                float temp = 0, speed = 0, time = 0;
                String method = "";
                
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.startsWith("temp=")) {
                        temp = Float.parseFloat(part.substring(5));
                    } else if (part.startsWith("speed=")) {
                        speed = Float.parseFloat(part.substring(6));
                    } else if (part.startsWith("time=")) {
                        time = Float.parseFloat(part.substring(5));
                    } else if (part.startsWith("method=")) {
                        method = part.substring(7);
                    }
                }
                
                mixingTemperature.setText(String.format("拌合温度：%.1f℃", temp));
                mixingSpeed.setText(String.format("拌合速度：%.1f r/min", speed));
                mixingTime.setText(String.format("拌合时间：%.1f s", time));
                compactionMethod.setText("压实方式：" + method);
                
                mixingTemperature.setVisibility(View.VISIBLE);
                mixingSpeed.setVisibility(View.VISIBLE);
                mixingTime.setVisibility(View.VISIBLE);
                compactionMethod.setVisibility(View.VISIBLE);
            } else {
                moldingMethod.setText("制件方式：" + methodStr);
                mixingTemperature.setVisibility(View.GONE);
                mixingSpeed.setVisibility(View.GONE);
                mixingTime.setVisibility(View.GONE);
                compactionMethod.setVisibility(View.GONE);
            }
        }

        // 设置配比和实验列表
        RecyclerView mixRatioRecyclerView = view.findViewById(R.id.rvMixRatios);
        mixRatioRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        MixRatioDetailAdapter adapter = new MixRatioDetailAdapter();
        mixRatioRecyclerView.setAdapter(adapter);
        adapter.setData(task.getSelectedMixRatios(), task.getExperimentAssignments());

        // 设置备注
        TextView notes = view.findViewById(R.id.tvNotes);
        if (task.getNotes() != null && !task.getNotes().isEmpty()) {
            notes.setText(task.getNotes());
        } else {
            notes.setText("无备注");
        }
    }
}
