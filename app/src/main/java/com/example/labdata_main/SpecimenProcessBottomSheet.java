package com.example.labdata_main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SpecimenProcessBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText etMixing;
    private TextInputEditText etCompaction;
    private TextInputEditText etCutting;
    private MaterialButton btnConfirm;
    private OnProcessConfirmedListener listener;

    public interface OnProcessConfirmedListener {
        void onProcessConfirmed(String mixing, String compaction, String cutting);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnProcessConfirmedListener) {
            listener = (OnProcessConfirmedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnProcessConfirmedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_specimen_process, container, false);

        // 初始化视图
        etMixing = view.findViewById(R.id.etMixing);
        etCompaction = view.findViewById(R.id.etCompaction);
        etCutting = view.findViewById(R.id.etCutting);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        // 设置点击监听器
        etMixing.setOnClickListener(v -> showMixingDialog());
        etCompaction.setOnClickListener(v -> showCompactionDialog());
        etCutting.setOnClickListener(v -> showCuttingDialog());

        btnConfirm.setOnClickListener(v -> {
            String mixing = etMixing.getText().toString();
            String compaction = etCompaction.getText().toString();
            String cutting = etCutting.getText().toString();

            if (mixing.isEmpty() || compaction.isEmpty() || cutting.isEmpty()) {
                Toast.makeText(requireContext(), "请完成所有选项", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onProcessConfirmed(mixing, compaction, cutting);
            }
            dismiss();
        });

        return view;
    }

    private void showMixingDialog() {
        // 显示拌合方式选择对话框
        String[] mixingMethods = {"机械拌合", "人工拌合"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择拌合方式")
            .setItems(mixingMethods, (dialog, which) -> {
                etMixing.setText(mixingMethods[which]);
            })
            .show();
    }

    private void showCompactionDialog() {
        // 显示击实方式选择对话框
        String[] compactionMethods = {"振动台振实", "重型击实", "马歇尔击实"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择击实方式")
            .setItems(compactionMethods, (dialog, which) -> {
                etCompaction.setText(compactionMethods[which]);
            })
            .show();
    }

    private void showCuttingDialog() {
        // 显示切割形状选择对话框
        String[] shapes = {"长方体", "圆柱体", "半圆形"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择切割形状")
            .setItems(shapes, (dialog, which) -> {
                etCutting.setText(shapes[which]);
            })
            .show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
