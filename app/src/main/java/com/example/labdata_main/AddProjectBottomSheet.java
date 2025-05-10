package com.example.labdata_main;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddProjectBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText etProjectName;
    private TextInputEditText etDeadline;
    private MaterialButton btnNext;
    private DatabaseHelper databaseHelper;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private OnProjectAddedListener projectAddedListener;

    public interface OnProjectAddedListener {
        void onProjectAdded(Project project);
    }

    public void setOnProjectAddedListener(OnProjectAddedListener listener) {
        this.projectAddedListener = listener;
    }

    public static AddProjectBottomSheet newInstance() {
        return new AddProjectBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();
        databaseHelper = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_project, container, false);
        
        etProjectName = view.findViewById(R.id.etProjectName);
        etDeadline = view.findViewById(R.id.etDeadline);
        btnNext = view.findViewById(R.id.btnNext);

        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        etDeadline.setOnClickListener(v -> showDatePicker());
        btnNext.setOnClickListener(v -> saveProject());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDeadline.setText(dateFormat.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveProject() {
        String projectName = etProjectName.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        if (TextUtils.isEmpty(projectName)) {
            Toast.makeText(requireContext(), "请输入项目名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(deadline)) {
            Toast.makeText(requireContext(), "请选择截止日期", Toast.LENGTH_SHORT).show();
            return;
        }

        Project project = new Project(projectName, deadline);

        try {
            long id = databaseHelper.insertProject(project);
            if (id != -1) {
                project.setId((int) id);
                Toast.makeText(requireContext(), "项目保存成功", Toast.LENGTH_SHORT).show();
                
                // 通知监听器项目已添加
                if (projectAddedListener != null) {
                    projectAddedListener.onProjectAdded(project);
                }
                
                dismiss();
            } else {
                Toast.makeText(requireContext(), "保存失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
