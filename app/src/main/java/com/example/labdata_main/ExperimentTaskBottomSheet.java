package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ExperimentTaskBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText etTaskName;
    private MaterialButton btnNext;

    public static ExperimentTaskBottomSheet newInstance() {
        return new ExperimentTaskBottomSheet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_experiment_task, container, false);

        etTaskName = view.findViewById(R.id.etTaskName);
        btnNext = view.findViewById(R.id.btnNext);

        btnNext.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            if (taskName.isEmpty()) {
                Toast.makeText(requireContext(), "请输入任务名称", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ExperimentTaskSetupActivity.start(requireContext(), taskName);
            dismiss();
        });

        return view;
    }
}
