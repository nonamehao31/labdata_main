package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.labdata_main.R;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.dialog.ExperimentSelectionBottomSheetDialog;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class AsphaltExperimentAssignmentFragment extends Fragment {
    private static final String ARG_TASK_NAME = "task_name";
    private String taskName;
    private LinearLayout cardContainer;
    private AsphaltSelectionFragment asphaltSelectionFragment;
    private MaterialButton btnConfirm;
    private Map<AsphaltInfo, Set<ExperimentType>> asphaltExperiments = new HashMap<>();
    private Executor executor;
    private AppDatabase database;

    public static AsphaltExperimentAssignmentFragment newInstance(String taskName) {
        AsphaltExperimentAssignmentFragment fragment = new AsphaltExperimentAssignmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskName = getArguments().getString(ARG_TASK_NAME);
        }
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };
        database = AppDatabase.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asphalt_experiment_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardContainer = view.findViewById(R.id.cardContainer);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        // 获取AsphaltSelectionFragment的引用
        asphaltSelectionFragment = (AsphaltSelectionFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag("f0");
                
        if (asphaltSelectionFragment != null) {
            Set<AsphaltInfo> selectedAsphalt = asphaltSelectionFragment.getSelectedAsphalt();
            createAsphaltCards(selectedAsphalt);
        }

        btnConfirm.setOnClickListener(v -> saveExperimentTask());
    }

    private void createAsphaltCards(Set<AsphaltInfo> asphaltInfos) {
        cardContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (AsphaltInfo asphalt : asphaltInfos) {
            MaterialCardView card = (MaterialCardView) inflater.inflate(
                    R.layout.card_asphalt_experiment_assignment, cardContainer, false);

            TextView tvGrade = card.findViewById(R.id.tvGrade);
            TextView tvType = card.findViewById(R.id.tvType);
            TextView tvSupplier = card.findViewById(R.id.tvSupplier);
            MaterialButton btnSelectExperiments = card.findViewById(R.id.btnSelectExperiments);
            TextView tvSelectedExperiments = card.findViewById(R.id.tvSelectedExperiments);

            tvGrade.setText(asphalt.getGrade());
            tvType.setText(asphalt.getType());
            tvSupplier.setText(asphalt.getSupplier());

            btnSelectExperiments.setOnClickListener(v -> showExperimentSelectionDialog(asphalt, tvSelectedExperiments));

            cardContainer.addView(card);
        }
    }

    private void showExperimentSelectionDialog(AsphaltInfo asphalt, TextView tvSelectedExperiments) {
        ExperimentSelectionBottomSheetDialog dialog = new ExperimentSelectionBottomSheetDialog();
        dialog.setOnExperimentsSelectedListener(selectedExperiments -> {
            // 更新显示
            updateSelectedExperimentsView(tvSelectedExperiments, selectedExperiments);
            
            // 更新数据库中的任务
            executor.execute(() -> {
                String taskName = getArguments().getString(ARG_TASK_NAME);
                ExperimentTask task = database.experimentTaskDao().getTaskByName(taskName);
                if (task != null) {
                    // 设置截止日期
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date expiryDate = dateFormat.parse(asphalt.getExpiryDate());
                        if (expiryDate != null) {
                            task.setDeadline(expiryDate.getTime());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 将选中的实验和沥青信息保存到notes字段
                    StringBuilder notes = new StringBuilder();
                    notes.append("沥青信息:\n");
                    notes.append("标号: ").append(asphalt.getGrade()).append("\n");
                    notes.append("类型: ").append(asphalt.getType()).append("\n");
                    notes.append("供应商: ").append(asphalt.getSupplier()).append("\n");
                    notes.append("检测截止日期: ").append(asphalt.getExpiryDate()).append("\n\n");
                    notes.append("选中的实验:\n");
                    int index = 1;
                    for (ExperimentType experiment : selectedExperiments) {
                        notes.append(index++).append(". ").append(experiment.getName()).append("\n");
                    }
                    task.setNotes(notes.toString());
                    
                    // 保存到数据库
                    database.experimentTaskDao().update(task);
                }
            });
        });
        dialog.show(getChildFragmentManager(), "experiment_selection");
    }

    private void updateSelectedExperimentsView(TextView textView, Set<ExperimentType> experiments) {
        if (experiments.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (ExperimentType experiment : experiments) {
                sb.append(index++).append(". ").append(experiment.getName()).append("\n");
            }
            // 移除最后一个换行符
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            textView.setText(sb.toString());
        }
    }

    private void updateConfirmButtonState() {
        boolean allAsphaltHasExperiments = true;
        for (AsphaltInfo asphalt : asphaltSelectionFragment.getSelectedAsphalt()) {
            Set<ExperimentType> experiments = asphaltExperiments.get(asphalt);
            if (experiments == null || experiments.isEmpty()) {
                allAsphaltHasExperiments = false;
                break;
            }
        }
        btnConfirm.setEnabled(allAsphaltHasExperiments);
    }

    private void saveExperimentTask() {
        // 创建实验任务
        List<ExperimentTask> tasks = new ArrayList<>();
        for (Map.Entry<AsphaltInfo, Set<ExperimentType>> entry : asphaltExperiments.entrySet()) {
            AsphaltInfo asphalt = entry.getKey();
            for (ExperimentType experiment : entry.getValue()) {
                ExperimentTask task = new ExperimentTask();
                task.setTaskName(taskName);
                task.setExperimentType("ASPHALT");
                task.setStatus("未接受");
                task.setCreationTime(System.currentTimeMillis());
                
                // 解析检测截止日期字符串为时间戳
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date expiryDate = dateFormat.parse(asphalt.getExpiryDate());
                    if (expiryDate != null) {
                        task.setDeadline(expiryDate.getTime());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 将沥青信息存储在notes字段中
                String notes = String.format("沥青信息:\n标号: %s\n类型: %s\n供应商: %s\n\n选中的实验:\n%s",
                    asphalt.getGrade(),
                    asphalt.getType(),
                    asphalt.getSupplier(),
                    experiment.getName());
                task.setNotes(notes);
                tasks.add(task);
            }
        }

        // 在后台线程中保存任务
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                for (ExperimentTask task : tasks) {
                    db.experimentTaskDao().insert(task);
                }
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "任务已创建", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "创建任务失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
