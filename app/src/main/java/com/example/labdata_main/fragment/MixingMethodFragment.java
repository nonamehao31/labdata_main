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

    public int getMixingTime() {
        try {
            // 解析用户输入的拌合时间，并直接以整数形式返回
            String timeStr = etMixingTime.getText().toString().trim();
            if (timeStr.isEmpty()) {
                return 0; // 如果用户未输入，返回默认值0
            }
            return (int)Float.parseFloat(timeStr);
        } catch (NumberFormatException e) {
            return 0; // 解析失败时返回默认值0
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
