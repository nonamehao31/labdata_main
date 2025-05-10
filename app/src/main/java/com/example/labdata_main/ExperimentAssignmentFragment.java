package com.example.labdata_main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.MixExperimentAdapter;
import com.example.labdata_main.model.ExperimentAssignment;
import com.example.labdata_main.model.MixExperiment;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAssignmentFragment extends Fragment implements MixExperimentAdapter.OnExperimentSelectedListener {
    private RecyclerView recyclerView;
    private MixExperimentAdapter adapter;
    private List<MixExperiment> mixExperiments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment_assignment, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadMixExperiments();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        mixExperiments = new ArrayList<>();
        adapter = new MixExperimentAdapter(mixExperiments, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMixExperiments() {
        // TODO: 从数据库或其他数据源加载配比信息
        // 这里先添加测试数据
        mixExperiments.add(new MixExperiment("配比方案 1"));
        mixExperiments.add(new MixExperiment("配比方案 2"));
        mixExperiments.add(new MixExperiment("配比方案 3"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onExperimentSelected(int position, String experiment, boolean isSelected) {
        MixExperiment mixExperiment = mixExperiments.get(position);
        if (isSelected) {
            mixExperiment.addExperiment(experiment);
        } else {
            mixExperiment.removeExperiment(experiment);
        }
        checkInputValidity();
    }

    private void checkInputValidity() {
        boolean isValid = false;
        for (MixExperiment mix : mixExperiments) {
            if (!mix.getSelectedExperiments().isEmpty()) {
                isValid = true;
                break;
            }
        }
        
        // 通知Activity更新下一步按钮状态
        if (getActivity() instanceof ExperimentTaskSetupActivity) {
            ((ExperimentTaskSetupActivity) requireActivity()).enableNextButton(isValid);
        }
    }

    public ExperimentAssignment getExperimentAssignment() {
        ExperimentAssignment assignment = new ExperimentAssignment();
        
        // 遍历所有配比方案，收集实验指派信息
        for (int i = 0; i < mixExperiments.size(); i++) {
            MixExperiment mix = mixExperiments.get(i);
            for (String experiment : mix.getSelectedExperiments()) {
                // TODO: 根据新的实验类型进行转换和保存
                switch (experiment) {
                    case "马歇尔稳定度":
                        assignment.addExperimentType(ExperimentAssignment.EXPERIMENT_MARSHALL);
                        break;
                    case "弯曲梁":
                        assignment.addExperimentType(ExperimentAssignment.EXPERIMENT_BEAM);
                        break;
                    case "弹性模量":
                        assignment.addExperimentType(ExperimentAssignment.EXPERIMENT_ELASTIC);
                        break;
                }
            }
        }
        
        return assignment;
    }
}
