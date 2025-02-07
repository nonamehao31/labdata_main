package com.example.labdata_main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.adapter.ExperimentMoldingAdapter;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class ScanDeviceFragment extends Fragment {
    private ExperimentTask task;
    private ExperimentMoldingAdapter adapter;
    private MaterialButton btnScanDevice;
    private MaterialButton btnNext;
    private final Map<Integer, DeviceInfo> mixingDevices = new HashMap<>();
    private final Map<Integer, DeviceInfo> formingDevices = new HashMap<>();
    private final Gson gson = new Gson();

    public static ScanDeviceFragment newInstance(ExperimentTask task) {
        ScanDeviceFragment fragment = new ScanDeviceFragment();
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
        return inflater.inflate(R.layout.fragment_scan_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView(view);
    }

    private void initViews(View view) {
        btnScanDevice = view.findViewById(R.id.btnScanDevice);
        btnNext = view.findViewById(R.id.btnNext);

        btnScanDevice.setOnClickListener(v -> {
            if (adapter.getSelectedMixRatio() == null) {
                Toast.makeText(requireContext(), "请先选择一个配比", Toast.LENGTH_SHORT).show();
                return;
            }
            startQRCodeScan();
        });

        btnNext.setOnClickListener(v -> {
            if (getParentFragment() instanceof BottomSheetMakeSpecimenFragment) {
                ((BottomSheetMakeSpecimenFragment) getParentFragment())
                    .onDeviceScanned(adapter.getSelectedMixRatio(), "");
            }
        });
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvMoldingMethods);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new ExperimentMoldingAdapter(task);
        adapter.setOnMixRatioSelectedListener(mixRatio -> {
            // 当选择新的配比时，更新设备信息显示
            int position = task.getSelectedMixRatios().indexOf(mixRatio);
            DeviceInfo mixingDevice = mixingDevices.get(position);
            DeviceInfo formingDevice = formingDevices.get(position);
            adapter.updateDeviceInfo(mixingDevice);
            adapter.updateDeviceInfo(formingDevice);
            updateNextButtonState();
        });
        
        recyclerView.setAdapter(adapter);
    }

    private void startQRCodeScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请将二维码对准扫描框");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            handleScanResult(result.getContents());
        }
    }

    private void handleScanResult(String deviceCode) {
        try {
            MixRatio selectedMixRatio = adapter.getSelectedMixRatio();
            if (selectedMixRatio == null) {
                Toast.makeText(requireContext(), "请先选择一个配比", Toast.LENGTH_SHORT).show();
                return;
            }

            int position = task.getSelectedMixRatios().indexOf(selectedMixRatio);
            DeviceInfo deviceInfo = gson.fromJson(deviceCode, DeviceInfo.class);
            if (deviceInfo == null) {
                Toast.makeText(requireContext(), "无效的设备信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 根据设备类型保存到对应的Map中
            switch (deviceInfo.getType()) {
                case "MIXING":
                    mixingDevices.put(position, deviceInfo);
                    break;
                case "FORMING":
                    formingDevices.put(position, deviceInfo);
                    break;
                default:
                    Toast.makeText(requireContext(), "制件环节未完成", Toast.LENGTH_SHORT).show();
                    return;
            }

            // 更新UI显示
            adapter.updateDeviceInfo(deviceInfo);
            updateNextButtonState();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "二维码格式错误，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNextButtonState() {
        // 检查是否所有配比都有对应的拌合设备和制件设备
        boolean allDevicesScanned = true;
        for (int i = 0; i < task.getSelectedMixRatios().size(); i++) {
            if (!mixingDevices.containsKey(i) || !formingDevices.containsKey(i)) {
                allDevicesScanned = false;
                break;
            }
        }
        btnNext.setEnabled(allDevicesScanned);
    }
}
