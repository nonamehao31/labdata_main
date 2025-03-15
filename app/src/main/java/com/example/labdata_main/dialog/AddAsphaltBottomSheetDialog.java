package com.example.labdata_main.dialog;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.labdata_main.R;
import com.example.labdata_main.api.request.TestAsphaltMaterialRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.TestAsphaltMaterialResponse;
import com.example.labdata_main.api.service.TestAsphaltMaterialService;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.utils.ApiClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAsphaltBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String TAG = "AddAsphaltDialog";
    
    private TextInputLayout supplierLayout;
    private TextInputLayout expiryDateLayout;
    private TextInputLayout gradeLayout;
    private TextInputLayout typeLayout;
    private TextInputEditText supplierInput;
    private TextInputEditText expiryDateInput;
    private TextInputEditText gradeInput;
    private AutoCompleteTextView typeDropdown;
    private MaterialButton btnConfirm;
    
    // 类型映射，将UI显示文本映射到后端值
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    static {
        TYPE_MAPPING.put("普通沥青", "NORMAL");
        TYPE_MAPPING.put("改性沥青", "MODIFIED");
    }

    private OnAsphaltAddedListener listener;
    private TestAsphaltMaterialService asphaltService;

    public interface OnAsphaltAddedListener {
        void onAsphaltAdded(AsphaltInfo asphaltInfo);
    }

    public void setOnAsphaltAddedListener(OnAsphaltAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asphaltService = ApiClient.getClient().create(TestAsphaltMaterialService.class);
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
                btnConfirm.setEnabled(false);
                
                String supplier = supplierInput.getText().toString().trim();
                String expiryDate = expiryDateInput.getText().toString().trim();
                String grade = gradeInput.getText().toString().trim();
                String typeName = typeDropdown.getText().toString().trim();
                
                // 将UI显示的类型名称转换为后端需要的类型值
                String typeValue = TYPE_MAPPING.getOrDefault(typeName, "NORMAL");
                
                // 创建请求对象
                TestAsphaltMaterialRequest request = new TestAsphaltMaterialRequest(
                        supplier, expiryDate, grade, typeValue
                );
                
                // 调用API保存沥青材料
                asphaltService.saveAsphaltMaterial(request).enqueue(new Callback<ApiResponse<TestAsphaltMaterialResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<TestAsphaltMaterialResponse>> call, Response<ApiResponse<TestAsphaltMaterialResponse>> response) {
                        btnConfirm.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            TestAsphaltMaterialResponse savedMaterial = response.body().getData();
                            Log.d(TAG, "沥青材料保存成功，ID: " + savedMaterial.getId());
                            
                            // 创建AsphaltInfo对象并回调给监听器
                            AsphaltInfo asphaltInfo = new AsphaltInfo(
                                    savedMaterial.getId(),
                                    savedMaterial.getAsphaltGrade(),
                                    savedMaterial.getAsphaltCatalog(),
                                    savedMaterial.getAsphaltSupplier(),
                                    savedMaterial.getAsphaltTestDue()
                            );
                            
                            if (listener != null) {
                                listener.onAsphaltAdded(asphaltInfo);
                            }
                            
                            Toast.makeText(requireContext(), "沥青材料添加成功", Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            String errorMessage = response.body() != null ? response.body().getMessage() : "未知错误";
                            Log.e(TAG, "保存沥青材料失败: " + errorMessage);
                            Toast.makeText(requireContext(), "保存失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<TestAsphaltMaterialResponse>> call, Throwable t) {
                        btnConfirm.setEnabled(true);
                        Log.e(TAG, "保存沥青材料请求失败", t);
                        Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
