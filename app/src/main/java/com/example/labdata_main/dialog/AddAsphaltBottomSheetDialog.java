package com.example.labdata_main.dialog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.labdata_main.R;
import com.example.labdata_main.model.AsphaltInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import java.util.Locale;

public class AddAsphaltBottomSheetDialog extends BottomSheetDialogFragment {
    private TextInputLayout supplierLayout;
    private TextInputLayout expiryDateLayout;
    private TextInputLayout gradeLayout;
    private TextInputLayout typeLayout;
    private TextInputEditText supplierInput;
    private TextInputEditText expiryDateInput;
    private TextInputEditText gradeInput;
    private AutoCompleteTextView typeDropdown;
    private MaterialButton btnConfirm;

    private OnAsphaltAddedListener listener;

    public interface OnAsphaltAddedListener {
        void onAsphaltAdded(AsphaltInfo asphaltInfo);
    }

    public void setOnAsphaltAddedListener(OnAsphaltAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_asphalt, container, false);
        initializeViews(view);
        setupTypeDropdown();
        setupDatePicker();
        setupConfirmButton();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    private void initializeViews(View view) {
        supplierLayout = view.findViewById(R.id.supplierLayout);
        expiryDateLayout = view.findViewById(R.id.expiryDateLayout);
        gradeLayout = view.findViewById(R.id.gradeLayout);
        typeLayout = view.findViewById(R.id.typeLayout);
        supplierInput = view.findViewById(R.id.supplierInput);
        expiryDateInput = view.findViewById(R.id.expiryDateInput);
        gradeInput = view.findViewById(R.id.gradeInput);
        typeDropdown = view.findViewById(R.id.typeDropdown);
        btnConfirm = view.findViewById(R.id.btnConfirm);
    }

    private void setupTypeDropdown() {
        String[] types = new String[]{"普通沥青", "改性沥青"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, types);
        typeDropdown.setAdapter(adapter);
    }

    private void setupDatePicker() {
        expiryDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                                year, month + 1, dayOfMonth);
                        expiryDateInput.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                AsphaltInfo asphaltInfo = new AsphaltInfo(
                        supplierInput.getText().toString().trim(),
                        expiryDateInput.getText().toString().trim(),
                        gradeInput.getText().toString().trim(),
                        typeDropdown.getText().toString().trim()
                );
                if (listener != null) {
                    listener.onAsphaltAdded(asphaltInfo);
                }
                dismiss();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (supplierInput.getText().toString().trim().isEmpty()) {
            supplierLayout.setError("请输入供应商");
            isValid = false;
        } else {
            supplierLayout.setError(null);
        }

        if (expiryDateInput.getText().toString().trim().isEmpty()) {
            expiryDateLayout.setError("请选择检测截止日期");
            isValid = false;
        } else {
            expiryDateLayout.setError(null);
        }

        if (gradeInput.getText().toString().trim().isEmpty()) {
            gradeLayout.setError("请输入沥青标号");
            isValid = false;
        } else {
            gradeLayout.setError(null);
        }

        if (typeDropdown.getText().toString().trim().isEmpty()) {
            typeLayout.setError("请选择沥青类型");
            isValid = false;
        } else {
            typeLayout.setError(null);
        }

        return isValid;
    }
}
