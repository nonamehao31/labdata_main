package com.example.labdata_main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.adapter.SpecimenMoldingPagerAdapter;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MakeSpecimenBottomSheet extends BottomSheetDialogFragment {
    private ViewPager2 viewPager;
    private Button btnNext;
    private SpecimenMoldingPagerAdapter adapter;
    private ExperimentTask experimentTask;
    private OnMethodSelectedListener listener;

    public interface OnMethodSelectedListener {
        void onMethodSelected(MoldingMethod method, int position);
    }

    public static MakeSpecimenBottomSheet newInstance(ExperimentTask task) {
        MakeSpecimenBottomSheet fragment = new MakeSpecimenBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            experimentTask = getArguments().getParcelable("task");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_make_specimen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.viewPager);
        btnNext = view.findViewById(R.id.btnNext);

        setupViewPager();
        setupListeners();
    }

    private void setupViewPager() {
        if (experimentTask != null) {
            adapter = new SpecimenMoldingPagerAdapter(
                experimentTask.getMoldingMethod(),
                experimentTask.getSelectedMixRatios()
            );

            adapter.setOnMethodSelectedListener((method, position) -> {
                // 启用下一步按钮
                btnNext.setEnabled(true);
                if (listener != null) {
                    listener.onMethodSelected(method, position);
                }
            });

            viewPager.setAdapter(adapter);
            
            // 设置页面切换回调
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    // 可以在这里处理页面切换事件
                }
            });
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            // 处理下一步按钮点击事件
            if (listener != null) {
                int currentPosition = viewPager.getCurrentItem();
                MoldingMethod currentMethod = adapter.getCurrentMethod();
                if (currentMethod != null) {
                    listener.onMethodSelected(currentMethod, currentPosition);
                }
            }
            dismiss();
        });
    }

    public void setOnMethodSelectedListener(OnMethodSelectedListener listener) {
        this.listener = listener;
    }

    public void updateDeviceInfo(String deviceType, DeviceInfo deviceInfo) {
        if (adapter != null) {
            adapter.updateDeviceInfo(deviceType, deviceInfo);
        }
    }
}
