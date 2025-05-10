package com.example.labdata_main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddExperimentBottomSheet extends BottomSheetDialogFragment {
    private TextInputLayout experimentNameLayout;
    private TextInputEditText experimentNameInput;
    private MaterialButton nextStepButton;
    private OnExperimentNameSubmitListener listener;

    public interface OnExperimentNameSubmitListener {
        void onExperimentNameSubmit(String experimentName);
    }

    public void setOnExperimentNameSubmitListener(OnExperimentNameSubmitListener listener) {
        this.listener = listener;
    }

    public static AddExperimentBottomSheet newInstance() {
        return new AddExperimentBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_experiment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        experimentNameLayout = view.findViewById(R.id.experimentNameLayout);
        experimentNameInput = view.findViewById(R.id.experimentNameInput);
        nextStepButton = view.findViewById(R.id.nextStepButton);

        // 设置点击监听器
        nextStepButton.setOnClickListener(v -> {
            String experimentName = experimentNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(experimentName)) {
                experimentNameLayout.setError("请输入实验任务名称");
                return;
            }

            if (listener != null) {
                listener.onExperimentNameSubmit(experimentName);
            }
            dismiss();
        });
    }
}
