package com.example.labdata_main;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.request.ProjectRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.ProjectResponse;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProjectBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "AddProjectBottomSheet";
    
    private TextInputEditText etProjectName;
    private TextInputEditText etDeadline;
    private MaterialButton btnNext;
    private DatabaseHelper databaseHelper;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private OnProjectAddedListener projectAddedListener;
    private SharedPrefsManager sharedPrefsManager;
    private ApiService apiService;

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
        sharedPrefsManager = new SharedPrefsManager(requireContext());
        
        // 获取认证令牌并初始化ApiService
        String token = sharedPrefsManager.getAuthToken();
        String authHeader = sharedPrefsManager.getAuthHeader();
        Log.d(TAG, "认证令牌: " + (token != null ? "已获取" : "未获取"));
        Log.d(TAG, "认证头信息: " + (authHeader != null ? authHeader : "无效"));
        
        if (authHeader != null && !authHeader.isEmpty()) {
            apiService = ApiClient.createService(ApiService.class, authHeader);
        } else {
            apiService = ApiClient.getApiService();
            Log.w(TAG, "未能获取有效的认证信息，可能无法保存到服务器");
        }
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

        // 创建项目对象（本地数据库用）
        Project project = new Project(projectName, deadline);

        try {
            // 保存到本地数据库
            long id = databaseHelper.insertProject(project);
            if (id != -1) {
                project.setId((int) id);
                
                // 同步到远程服务器
                saveProjectToServer(projectName, deadline);
                
                Toast.makeText(requireContext(), "项目保存成功", Toast.LENGTH_SHORT).show();

                // 通知监听器项目已添加
                if (projectAddedListener != null) {
                    projectAddedListener.onProjectAdded(project);
                }

                dismiss();
            } else {
                Toast.makeText(requireContext(), "本地保存失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "保存项目失败", e);
        }
    }
    
    /**
     * 将项目信息保存到远程服务器
     * @param projectName 项目名称
     * @param deadline 截止日期
     */
    private void saveProjectToServer(String projectName, String deadline) {
        try {
            // 获取用户公司ID和名称
            String companyId = sharedPrefsManager.getUserCompany();
            String companyName = sharedPrefsManager.getUserCompany(); // 获取公司名称
            String authHeader = sharedPrefsManager.getAuthHeader();
            
            if (companyId == null || authHeader == null) {
                Log.w(TAG, "无法获取公司ID或认证令牌，仅保存到本地");
                return;
            }
            
            Log.d(TAG, "准备发送项目数据到服务器，认证头: " + authHeader);
            
            // 创建包含正确字段名称的JSON对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", projectName);  // 修改字段名
            jsonObject.put("companyId", companyId);  // 修改字段名
            jsonObject.put("description", "通过移动端创建的项目");  // 添加描述字段
            jsonObject.put("deadline", deadline);  // 修改字段名
            
            String jsonBody = jsonObject.toString();
            Log.d(TAG, "发送到服务器的JSON数据: " + jsonBody);

            // 创建请求体
            RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody
            );
            
            // 发送标准的ProjectRequest对象请求（而不是自定义JSON）
            ProjectRequest projectRequest = new ProjectRequest();
            projectRequest.setName(projectName);
            projectRequest.setCompanyId(companyId.trim());
            projectRequest.setDescription("通过移动端创建的项目");
            projectRequest.setDeadline(deadline);
            
            Log.d(TAG, "使用标准ProjectRequest对象发送请求");
            
            // 使用ProjectRequest对象，避免字段名称不匹配
            Call<ApiResponse<ProjectResponse>> call = apiService.createProject(projectRequest);
            
            // 打印完整API请求路径
            Log.d(TAG, "完整API请求URL: " + call.request().url().toString());
            Log.d(TAG, "请求方法: " + call.request().method());
            Log.d(TAG, "请求头信息: " + call.request().headers().toString());
            
            call.enqueue(new Callback<ApiResponse<ProjectResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ProjectResponse>> call, Response<ApiResponse<ProjectResponse>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "项目成功保存到服务器");
                    } else {
                        Log.e(TAG, "保存项目到服务器失败，状态码: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                Log.e(TAG, "错误详情: " + response.errorBody().string());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "读取错误响应失败", e);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<ProjectResponse>> call, Throwable t) {
                    Log.e(TAG, "连接服务器失败: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "创建项目请求失败", e);
        }
    }
}