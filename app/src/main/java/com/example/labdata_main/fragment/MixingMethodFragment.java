package com.example.labdata_main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.labdata_main.R;
import com.google.android.material.textfield.TextInputEditText;

public class MixingMethodFragment extends Fragment {
    private TextInputEditText etMixingTemperature;
    private TextInputEditText etMixingSpeed;
    private TextInputEditText etMixingTime;
    private OnNextStepListener nextStepListener;

    public interface OnNextStepListener {
        void onNextStep();
    }

    public MixingMethodFragment(OnNextStepListener listener) {
        this.nextStepListener = listener;
    }

    public MixingMethodFragment() {
        // 必须有一个无参数的构造函数，用于 FragmentStateAdapter
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mixing_method, container, false);

        etMixingTemperature = view.findViewById(R.id.etMixingTemperature);
        etMixingSpeed = view.findViewById(R.id.etMixingSpeed);
        etMixingTime = view.findViewById(R.id.etMixingTime);

        view.findViewById(R.id.btnNextMixingMethod).setOnClickListener(v -> {
            if (validateInput() && nextStepListener != null) {
                nextStepListener.onNextStep();
            }
        });

        return view;
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (etMixingTemperature.getText().toString().trim().isEmpty()) {
            etMixingTemperature.setError("请输入拌合温度");
            isValid = false;
        }

        if (etMixingSpeed.getText().toString().trim().isEmpty()) {
            etMixingSpeed.setError("请输入拌合速度");
            isValid = false;
        }

        if (etMixingTime.getText().toString().trim().isEmpty()) {
            etMixingTime.setError("请输入拌合时间");
            isValid = false;
        }

        return isValid;
    }

    public float getMixingTemperature() {
        return Float.parseFloat(etMixingTemperature.getText().toString().trim());
    }

    public float getMixingSpeed() {
        return Float.parseFloat(etMixingSpeed.getText().toString().trim());
    }

    public float getMixingTime() {
        return Float.parseFloat(etMixingTime.getText().toString().trim());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
