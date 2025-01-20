package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.adapter.SpecimenExperimentAdapter;
import com.example.labdata_main.model.SpecimenExperimentModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ExperimentAssignmentFragment extends Fragment {

    private TextInputEditText editTextSpecimenCount;
    private RadioGroup radioGroupSpecimenShape;
    private RecyclerView recyclerView;
    private Button buttonFinish;
    private SpecimenExperimentAdapter adapter;
    private List<SpecimenExperimentModel> specimenList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_experiment_assignment, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        editTextSpecimenCount = view.findViewById(R.id.editTextSpecimenCount);
        radioGroupSpecimenShape = view.findViewById(R.id.radioGroupSpecimenShape);
        recyclerView = view.findViewById(R.id.recyclerView);
        buttonFinish = view.findViewById(R.id.buttonFinish);

        specimenList = new ArrayList<>();
        adapter = new SpecimenExperimentAdapter(specimenList, this::onExperimentSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        editTextSpecimenCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateSpecimenList();
            }
        });

        radioGroupSpecimenShape.setOnCheckedChangeListener((group, checkedId) -> {
            updateSpecimenList();
        });

        buttonFinish.setOnClickListener(v -> {
            if (validateExperimentAssignment()) {
                saveExperimentAssignment();
            }
        });
    }

    private void updateSpecimenList() {
        specimenList.clear();
        String countStr = editTextSpecimenCount.getText().toString();
        if (!countStr.isEmpty()) {
            int count = Integer.parseInt(countStr);
            String shape = radioGroupSpecimenShape.getCheckedRadioButtonId() == R.id.radioRectangular ? "长方体" : "圆柱体";
            
            for (int i = 0; i < count; i++) {
                specimenList.add(new SpecimenExperimentModel(i + 1, shape));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void onExperimentSelected(int position, String experiment) {
        SpecimenExperimentModel specimen = specimenList.get(position);
        if (specimen.getSelectedExperiments().contains(experiment)) {
            specimen.removeExperiment(experiment);
        } else {
            specimen.addExperiment(experiment);
        }
        adapter.notifyItemChanged(position);
    }

    private boolean validateExperimentAssignment() {
        // 检查是否输入了试块数量
        String countStr = editTextSpecimenCount.getText().toString().trim();
        if (countStr.isEmpty()) {
            Toast.makeText(getContext(), "请输入试块数量", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查是否选择了试块形状
        if (radioGroupSpecimenShape.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "请选择试块形状", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查每个试块是否完成全部实验指派
        for (SpecimenExperimentModel specimen : specimenList) {
            if (!specimen.hasAllExperimentsAssigned()) {
                Toast.makeText(getContext(), "请为所有试块指派全部实验", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void saveExperimentAssignment() {
        // 这里添加保存逻辑，可以是保存到数据库或传递给下一个Fragment
        // 示例：
        List<Integer> specimenNumbers = new ArrayList<>();
        List<String> specimenShapes = new ArrayList<>();
        List<List<String>> experimentAssignments = new ArrayList<>();

        for (SpecimenExperimentModel specimen : specimenList) {
            specimenNumbers.add(specimen.getSpecimenNumber());
            specimenShapes.add(specimen.getSpecimenShape());
            experimentAssignments.add(specimen.getSelectedExperiments());
        }

        // 可以通过Intent或ViewModel传递数据
        Toast.makeText(getContext(), "实验指派完成", Toast.LENGTH_SHORT).show();
        
        // 返回上一个Fragment或主页
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
