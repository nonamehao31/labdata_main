package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

public class BottomSheetMakeSpecimenFragment extends BottomSheetDialogFragment {
    private ViewPager2 viewPager;
    private View step1Indicator, step2Indicator;
    private TextView step1Text, step2Text;
    private ExperimentTask task;
    private DeviceInfo scannedDevice;
    private MixRatio selectedMixRatio;
    private final Gson gson = new Gson();
    private View rootView;

    public static BottomSheetMakeSpecimenFragment newInstance(ExperimentTask task) {
        BottomSheetMakeSpecimenFragment fragment = new BottomSheetMakeSpecimenFragment();
        Bundle args = new Bundle();
        args.putParcelable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable("task");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.bottom_sheet_make_specimen, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupViewPager();
        setupStepClickListeners();
        updateStepIndicators(0);
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        step1Indicator = view.findViewById(R.id.step1Indicator);
        step2Indicator = view.findViewById(R.id.step2Indicator);
        step1Text = view.findViewById(R.id.step1Text);
        step2Text = view.findViewById(R.id.step2Text);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? ScanDeviceFragment.newInstance(task) 
                                   : GenerateSpecimenFragment.newInstance(task);
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepIndicators(position);
            }
        });

        // 禁用用户手动滑动
        viewPager.setUserInputEnabled(false);
    }

    private void setupStepClickListeners() {
        View.OnClickListener stepClickListener = v -> {
            int targetStep = v.getId() == R.id.step1Layout ? 0 : 1;
            // 只允许在扫描设备成功后才能进入第二步
            if (targetStep == 1 && scannedDevice == null) {
                Toast.makeText(requireContext(), "请先完成设备扫描", Toast.LENGTH_SHORT).show();
                return;
            }
            viewPager.setCurrentItem(targetStep);
        };

        rootView.findViewById(R.id.step1Layout).setOnClickListener(stepClickListener);
        rootView.findViewById(R.id.step2Layout).setOnClickListener(stepClickListener);
    }

    private void updateStepIndicators(int position) {
        step1Indicator.setSelected(position == 0);
        step2Indicator.setSelected(position == 1);
        
        step1Text.setTextColor(getResources().getColor(position == 0 ? 
            R.color.step_text_active : R.color.step_text_inactive));
        step2Text.setTextColor(getResources().getColor(position == 1 ? 
            R.color.step_text_active : R.color.step_text_inactive));
    }

    /**
     * 处理设备扫描结果
     * @param mixRatio 选中的配比
     * @param deviceCode 扫描到的设备码
     */
    public void onDeviceScanned(MixRatio mixRatio, String deviceCode) {
        try {
            DeviceInfo deviceInfo = gson.fromJson(deviceCode, DeviceInfo.class);
            if (deviceInfo != null) {
                deviceInfo.validateDeviceAsync(requireContext()).observe(this, isValid -> {
                    if (isValid) {
                        this.scannedDevice = deviceInfo;
                        this.selectedMixRatio = mixRatio;
                        // 显示成功提示
                        Toast.makeText(requireContext(), 
                            "设备扫描成功:\n" + deviceInfo.getDescription(), 
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), 
                            "无效的设备信息，请确保扫描已注册的设备", 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), 
                "二维码格式错误，请重试", 
                Toast.LENGTH_SHORT).show();
        }
    }

    public DeviceInfo getScannedDevice() {
        return scannedDevice;
    }

    public MixRatio getSelectedMixRatio() {
        return selectedMixRatio;
    }

    public ExperimentTask getTask() {
        return task;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scannedDevice != null) {
            scannedDevice.dispose();
        }
    }
}
