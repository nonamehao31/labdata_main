package com.example.labdata_main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.labdata_main.model.ExperimentAssignment;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentAssignmentFragment extends Fragment {
    private LinearLayout containerMixRatioExperiments;
    private TextInputEditText etNotes;
    private List<MixRatio> selectedMixRatios = new ArrayList<>();
    private Map<Long, ChipGroup> experimentChipGroups = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment_assignment, container, false);
        
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        containerMixRatioExperiments = view.findViewById(R.id.containerMixRatioExperiments);
        etNotes = view.findViewById(R.id.etNotes);
    }

    public void setSelectedMixRatios(List<MixRatio> mixRatios) {
        if (mixRatios == null) return;
        
        // 清除已移除的配比卡片
        List<Long> toRemove = new ArrayList<>();
        for (Long mixRatioId : experimentChipGroups.keySet()) {
            boolean found = false;
            for (MixRatio mixRatio : mixRatios) {
                if (mixRatio.getId() == mixRatioId) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                toRemove.add(mixRatioId);
            }
        }
        
        for (Long mixRatioId : toRemove) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatioId);
            if (chipGroup != null) {
                View cardView = (View) chipGroup.getParent().getParent();
                containerMixRatioExperiments.removeView(cardView);
                experimentChipGroups.remove(mixRatioId);
            }
        }
        
        // 添加新的配比卡片
        for (MixRatio mixRatio : mixRatios) {
            if (!experimentChipGroups.containsKey(mixRatio.getId())) {
                addExperimentCard(mixRatio);
            }
        }
        
        this.selectedMixRatios = new ArrayList<>(mixRatios);
        checkInputValidity();
    }

    private void addExperimentCard(MixRatio mixRatio) {
        View cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_mix_ratio_experiment, containerMixRatioExperiments, false);

        // 设置卡片标题
        TextView tvTitle = cardView.findViewById(R.id.tvMixRatioTitle);
        tvTitle.setText(String.format("选择%s的实验类型", mixRatio.getName()));

        // 创建实验类型选择区域
        LinearLayout experimentContainer = cardView.findViewById(R.id.experimentContainer);
        ChipGroup chipGroup = createExperimentChipGroup();
        experimentContainer.addView(chipGroup);
        experimentChipGroups.put(mixRatio.getId(), chipGroup);

        // 添加到容器
        containerMixRatioExperiments.addView(cardView);
    }

    private ChipGroup createExperimentChipGroup() {
        ChipGroup chipGroup = new ChipGroup(requireContext());
        chipGroup.setSelectionRequired(true);
        chipGroup.setSingleSelection(false);

        // 添加实验类型选项
        String[] experimentTypes = {"抗压强度", "抗折强度", "劈裂抗拉", "弹性模量"};
        String[] experimentIds = {
            ExperimentAssignment.EXPERIMENT_COMPRESSION,
            ExperimentAssignment.EXPERIMENT_FLEXURAL,
            ExperimentAssignment.EXPERIMENT_SPLITTING,
            ExperimentAssignment.EXPERIMENT_ELASTIC
        };

        for (int i = 0; i < experimentTypes.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(experimentTypes[i]);
            chip.setCheckable(true);
            chip.setTag(experimentIds[i]);
            chipGroup.addView(chip);
        }

        // 添加选择监听器
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> checkInputValidity());

        return chipGroup;
    }

    private void checkInputValidity() {
        boolean isValid = true;
        
        // 检查每个配比是否都选择了至少一个实验类型
        for (ChipGroup chipGroup : experimentChipGroups.values()) {
            if (chipGroup.getCheckedChipIds().isEmpty()) {
                isValid = false;
                break;
            }
        }
        
        // 通知Activity更新下一步按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).enableNextButton(isValid);
        }
    }

    public ExperimentAssignment getExperimentAssignment() {
        ExperimentAssignment assignment = new ExperimentAssignment();
        
        // 获取每个配比的实验类型
        for (MixRatio mixRatio : selectedMixRatios) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatio.getId());
            if (chipGroup != null) {
                List<String> experimentTypes = new ArrayList<>();
                for (int chipId : chipGroup.getCheckedChipIds()) {
                    Chip chip = chipGroup.findViewById(chipId);
                    if (chip != null) {
                        experimentTypes.add((String) chip.getTag());
                    }
                }
                
                assignment.addMixRatioExperiments(mixRatio.getId(), experimentTypes);
            }
        }
        
        // 获取备注说明
        String notes = etNotes.getText().toString();
        if (!TextUtils.isEmpty(notes)) {
            assignment.setNotes(notes);
        }
        
        return assignment;
    }
}
