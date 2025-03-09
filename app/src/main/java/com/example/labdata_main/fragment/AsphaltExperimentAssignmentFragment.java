package com.example.labdata_main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.labdata_main.dialog.ExperimentSelectionBottomSheetDialog;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;
import com.example.labdata_main.service.AsphaltExperimentService;
import com.example.labdata_main.service.AsphaltExperimentService.ServiceCallback;
import com.example.labdata_main.utils.SharedPrefsManager;
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
    private AsphaltExperimentService asphaltService;

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
        asphaltService = new AsphaltExperimentService(requireContext());
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
            
            // 更新实验分配
            asphaltExperiments.put(asphalt, selectedExperiments);
            updateConfirmButtonState();
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
        // 获取当前选中的沥青和实验分配
        Set<AsphaltInfo> selectedAsphalt = asphaltSelectionFragment.getSelectedAsphalt();
        if (selectedAsphalt.isEmpty()) {
            Toast.makeText(requireContext(), "请至少选择一种沥青", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 确保每种沥青都分配了实验
        boolean allAssigned = true;
        for (AsphaltInfo asphalt : selectedAsphalt) {
            Set<ExperimentType> experiments = asphaltExperiments.get(asphalt);
            if (experiments == null || experiments.isEmpty()) {
                allAssigned = false;
                break;
            }
        }
        
        if (!allAssigned) {
            Toast.makeText(requireContext(), "请为每种沥青分配至少一种实验", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 准备实验分配列表
        List<AsphaltExperimentService.ExperimentAssignment> experimentAssignments = new ArrayList<>();
        for (AsphaltInfo asphalt : selectedAsphalt) {
            Set<ExperimentType> experiments = asphaltExperiments.get(asphalt);
            if (experiments != null) {
                for (ExperimentType experiment : experiments) {
                    experimentAssignments.add(new AsphaltExperimentService.ExperimentAssignment(asphalt, experiment));
                }
            }
        }
        
        // 显示加载提示
        Toast.makeText(requireContext(), "正在创建实验任务...", Toast.LENGTH_SHORT).show();
        btnConfirm.setEnabled(false);
        
        // 调用API服务创建沥青实验任务
        asphaltService.createAsphaltExperimentTask(
                taskName,
                "沥青实验任务",
                selectedAsphalt,
                experimentAssignments,
                new AsphaltExperimentService.ServiceCallback<Long>() {
                    @Override
                    public void onSuccess(Long taskId) {
                        // 在UI线程更新界面
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "实验任务创建成功", Toast.LENGTH_SHORT).show();
                            
                            // 发送广播通知任务更新
                            Intent intent = new Intent("com.example.labdata_main.TASK_UPDATED");
                            requireContext().sendBroadcast(intent);
                            
                            // 关闭当前Activity
                            getActivity().finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        // 在UI线程显示错误
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "任务创建失败: " + message, Toast.LENGTH_LONG).show();
                            btnConfirm.setEnabled(true);
                        });
                    }
                });
    }
}
