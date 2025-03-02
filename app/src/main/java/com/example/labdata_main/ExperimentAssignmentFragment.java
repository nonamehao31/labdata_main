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
        chipGroup.setSelectionRequired(false);
        chipGroup.setSingleSelection(false);

        // 添加混合料实验类型选项
        String[] experimentTypes = {
            "马歇尔稳定度试验",
            "理论最大相对密度试验",
            "体积密度试验",
            "空隙率试验",
            "飞散试验",
            "动稳定度试验",
            "沥青混合料车辙实验（汉堡车辙）",
            "沥青混合料弯曲试验",
            "动态模量试验",
            "沥青混合料直接拉伸循环疲劳测黏弹损伤试验",
            "沥青混合料四点弯曲疲劳寿命试验",
            "沥青混合料单轴压缩试验(圆柱体法)"
        };

        for (String type : experimentTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(type);
            chip.setCheckable(true);
            chipGroup.addView(chip);
        }

        // 添加选择状态变化监听
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            checkInputValidity();
        });

        return chipGroup;
    }

    private void checkInputValidity() {
        // 检查是否每个配比都至少选择了一个实验类型
        boolean isValid = true;
        for (MixRatio mixRatio : selectedMixRatios) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatio.getId());
            if (chipGroup == null || chipGroup.getCheckedChipIds().isEmpty()) {
                isValid = false;
                break;
            }
        }

        // 通知 Activity 更新按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) getActivity()).updateNextButton();
        }
    }

    public Map<Long, List<String>> getExperimentAssignments() {
        Map<Long, List<String>> assignments = new HashMap<>();
        
        for (MixRatio mixRatio : selectedMixRatios) {
            ChipGroup chipGroup = experimentChipGroups.get(mixRatio.getId());
            if (chipGroup != null) {
                List<String> selectedTypes = new ArrayList<>();
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    View child = chipGroup.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (chip.isChecked()) {
                            selectedTypes.add(chip.getText().toString());
                        }
                    }
                }
                assignments.put(mixRatio.getId(), selectedTypes);
            }
        }
        
        return assignments;
    }

    public String getNotes() {
        return etNotes != null ? etNotes.getText().toString() : "";
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在 Fragment 恢复时更新按钮状态
        checkInputValidity();
    }
}
