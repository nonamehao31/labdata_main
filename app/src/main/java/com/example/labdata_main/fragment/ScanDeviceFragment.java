package com.example.labdata_main.fragment;

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
import com.google.android.material.button.MaterialButton;

public class ScanDeviceFragment extends Fragment {
    private ExperimentTask task;
    private RecyclerView rvMoldingMethods;
    private MaterialButton btnScanDevice;

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

        // 设置RecyclerView
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        if (task != null) {
            ExperimentMoldingAdapter adapter = new ExperimentMoldingAdapter(task);
            rvMoldingMethods.setAdapter(adapter);
        }

        // 设置扫描按钮点击事件
        btnScanDevice.setOnClickListener(v -> {
            // TODO: 实现扫描功能
            Toast.makeText(requireContext(), "扫描功能开发中", Toast.LENGTH_SHORT).show();
        });
    }
}
