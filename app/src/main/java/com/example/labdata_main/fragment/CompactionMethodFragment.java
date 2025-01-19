package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.R;
import com.example.labdata_main.SelectMoldingMethodFragment;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.MoldingMethod;

public class CompactionMethodFragment extends Fragment {
    private AutoCompleteTextView actvCompactionMethod;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compaction_method, container, false);

        actvCompactionMethod = view.findViewById(R.id.actvCompactionMethod);
        viewPager = requireActivity().findViewById(R.id.viewPagerMoldingMethod);

        // 设置压实方法的选项
        String[] compactionMethods = {"马歇尔击实", "旋转压实", "轮碾压实", "震动压实"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            compactionMethods
        );
        actvCompactionMethod.setAdapter(adapter);

        view.findViewById(R.id.btnCompleteMoldingMethod).setOnClickListener(v -> {
            if (validateInput()) {
                saveMoldingMethod();
            }
        });

        return view;
    }

    private boolean validateInput() {
        String compactionMethod = actvCompactionMethod.getText().toString().trim();

        if (compactionMethod.isEmpty()) {
            actvCompactionMethod.setError("请选择压实方法");
            return false;
        }

        return true;
    }

    private void saveMoldingMethod() {
        SelectMoldingMethodFragment parentFragment = (SelectMoldingMethodFragment) 
            requireParentFragment();

        // 获取 ViewPager 和 MixingMethodFragment
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPagerMoldingMethod);
        MixingMethodFragment mixingMethodFragment = (MixingMethodFragment) 
            getParentFragmentManager().findFragmentByTag("f0");

        if (mixingMethodFragment != null) {
            float mixingTemperature = mixingMethodFragment.getMixingTemperature();
            float mixingSpeed = mixingMethodFragment.getMixingSpeed();
            float mixingTime = mixingMethodFragment.getMixingTime();
            String compactionMethod = actvCompactionMethod.getText().toString().trim();

            MoldingMethod moldingMethod = new MoldingMethod(
                mixingTemperature, 
                mixingSpeed, 
                mixingTime, 
                compactionMethod
            );

            new Thread(() -> {
                long id = AppDatabase.getInstance(requireContext())
                    .moldingMethodDao()
                    .insert(moldingMethod);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "制件方法保存成功", Toast.LENGTH_SHORT).show();
                    parentFragment.addMoldingMethod(moldingMethod);
                    // 通过父Fragment关闭底部弹窗
                    if (parentFragment.currentBottomSheet != null) {
                        parentFragment.currentBottomSheet.dismiss();
                    }
                });
            }).start();
        } else {
            Toast.makeText(requireContext(), "无法获取拌合方法信息", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCompactionMethod() {
        return actvCompactionMethod.getText().toString().trim();
    }
}
