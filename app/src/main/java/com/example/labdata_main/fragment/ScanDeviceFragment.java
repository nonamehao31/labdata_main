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
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanDeviceFragment extends Fragment {
    private ExperimentTask task;
    private RecyclerView rvMoldingMethods;
    private MaterialButton btnScanDevice;
    private ExperimentMoldingAdapter adapter;
    private MixRatio selectedMixRatio;

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

        // 初始化视图
        rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        btnScanDevice = view.findViewById(R.id.btnScanDevice);
        btnScanDevice.setEnabled(false); // 初始状态下禁用扫描按钮

        // 设置RecyclerView
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        if (task != null) {
            adapter = new ExperimentMoldingAdapter(task);
            adapter.setOnMixRatioSelectedListener(mixRatio -> {
                selectedMixRatio = mixRatio;
                btnScanDevice.setEnabled(true);
            });
            rvMoldingMethods.setAdapter(adapter);
        }

        // 设置扫描按钮点击事件
        btnScanDevice.setOnClickListener(v -> {
            if (selectedMixRatio != null) {
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("请将二维码对准扫描框");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setOrientationLocked(true); // 锁定竖屏
                integrator.initiateScan();
            } else {
                Toast.makeText(requireContext(), "请先选择一个配比", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(requireContext(), "扫描已取消", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: 处理扫描结果
                String scannedData = result.getContents();
                if (getParentFragment() instanceof BottomSheetMakeSpecimenFragment) {
                    ((BottomSheetMakeSpecimenFragment) getParentFragment())
                        .onDeviceScanned(selectedMixRatio, scannedData);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
