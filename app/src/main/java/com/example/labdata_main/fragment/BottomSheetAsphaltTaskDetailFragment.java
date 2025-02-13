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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BottomSheetAsphaltTaskDetailFragment extends BottomSheetDialogFragment {
    private static final String ARG_TASK = "task";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ExperimentTask task;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskAccepted(ExperimentTask task);
        void onTaskRejected(ExperimentTask task);
    }

    public static BottomSheetAsphaltTaskDetailFragment newInstance(ExperimentTask task) {
        BottomSheetAsphaltTaskDetailFragment fragment = new BottomSheetAsphaltTaskDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable(ARG_TASK);
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

    private List<AsphaltExperimentItem> parseExperimentData() {
        List<AsphaltExperimentItem> items = new ArrayList<>();
        String notes = task.getNotes();
        if (notes != null) {
            String[] sections = notes.split("\n\n");
            for (String section : sections) {
                if (section.startsWith("沥青信息:")) {
                    items.add(new AsphaltExperimentItem(
                        "沥青信息",
                        section.substring("沥青信息:".length()).trim()
                    ));
                } else if (section.startsWith("选中的实验:")) {
                    items.add(new AsphaltExperimentItem(
                        "实验指派",
                        section.substring("选中的实验:".length()).trim()
                    ));
                }
            }
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
