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
import com.example.labdata_main.adapter.MixRatioDetailAdapter;
import com.example.labdata_main.adapter.TaskDetailMoldingAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BottomSheetMixRatioDetailFragment extends BottomSheetDialogFragment {
    private ExperimentTask task;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static BottomSheetMixRatioDetailFragment newInstance(ExperimentTask task) {
        BottomSheetMixRatioDetailFragment fragment = new BottomSheetMixRatioDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable("task");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_mixratio_detail, container, false);
    }

    public interface OnMaterialCompletedListener {
        void onMaterialCompleted(ExperimentTask task);
    }
    
    // 添加任务操作接口
    public interface OnTaskActionListener {
        void onTaskAccepted(long taskId);
        void onTaskRejected(long taskId);
    }
    
    private OnMaterialCompletedListener materialCompletedListener;
    private OnTaskActionListener taskActionListener;

    public void setOnMaterialCompletedListener(OnMaterialCompletedListener listener) {
        this.materialCompletedListener = listener;
    }
    
    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.taskActionListener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (task == null) return;

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
        TextView taskName = view.findViewById(R.id.task_name);
        TextView projectName = view.findViewById(R.id.tvProjectName);
        TextView deadline = view.findViewById(R.id.tvDeadline);

        taskName.setText(task.getTaskName());
        projectName.setText(String.format("项目名称：%s", task.getProjectName()));
        deadline.setText(String.format("截止时间：%s", dateFormat.format(task.getDeadline())));

        // 设置制件参数
        RecyclerView rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        String moldingMethod = task.getMoldingMethod();
        if (moldingMethod != null && !moldingMethod.isEmpty()) {
            TaskDetailMoldingAdapter adapter = new TaskDetailMoldingAdapter(
                moldingMethod,
                task.getSelectedMixRatios() != null ? task.getSelectedMixRatios() : new ArrayList<>()
            );
            rvMoldingMethods.setAdapter(adapter);
        }

        // 设置配比列表
        RecyclerView rvMixRatios = view.findViewById(R.id.rvMixRatios);
        rvMixRatios.setLayoutManager(new LinearLayoutManager(requireContext()));
        MixRatioDetailAdapter adapter = new MixRatioDetailAdapter();
        adapter.setData(task.getSelectedMixRatios(), task.getExperimentAssignments());
        rvMixRatios.setAdapter(adapter);

        // 设置备注
        TextView tvNotes = view.findViewById(R.id.tvNotes);
        String notes = task.getNotes();
        if (notes != null && !notes.isEmpty()) {
            tvNotes.setText(notes);
        } else {
            tvNotes.setText("暂无备注");
        }
    }
}
