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
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
                createMoldingMethod();
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

    private void createMoldingMethod() {
        SelectMoldingMethodFragment parentFragment = (SelectMoldingMethodFragment) 
            requireParentFragment();

        // 获取 ViewPager 和 MixingMethodFragment
        MixingMethodFragment mixingMethodFragment = (MixingMethodFragment) 
            getParentFragmentManager().findFragmentByTag("f0");

        if (mixingMethodFragment != null) {
            try {
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

                // 添加到父Fragment，由父Fragment处理数据库操作
                parentFragment.addMoldingMethod(moldingMethod);

                // 关闭底部弹窗
                View bottomSheet = requireView().getRootView();
                if (bottomSheet.getParent() instanceof View) {
                    View parent = (View) bottomSheet.getParent();
                    if (parent.getParent() instanceof BottomSheetDialog) {
                        ((BottomSheetDialog) parent.getParent()).dismiss();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效的数值", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "创建制件方法失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "无法获取拌合方法信息", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCompactionMethod() {
        return actvCompactionMethod.getText().toString().trim();
    }
}
