package com.example.labdata_main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.UserAdapter;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.model.User;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 单位人员管理界面
 * 仅管理员可以访问，显示同一单位的所有用户
 */
public class UserManagementActivity extends AppCompatActivity {
    private static final String TAG = "UserManagementActivity";

    private RecyclerView rvUsers;
    private TextView tvCompanyName;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private UserAdapter userAdapter;
    private SharedPrefsManager sharedPrefsManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // 初始化工具类
        sharedPrefsManager = new SharedPrefsManager(this);
        apiService = ApiClient.getApiService();

        // 初始化视图
        initViews();
        
        // 加载用户数据
        loadUsers();
    }

    private void initViews() {
        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 初始化其他视图
        rvUsers = findViewById(R.id.rvUsers);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);

        // 设置RecyclerView
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new ArrayList<>());
        
        // 设置用户点击事件
        userAdapter.setOnUserClickListener((user, position) -> {
            // 只允许对非管理员用户设置权限
            if (!user.isAdmin()) {
                showPermissionDialog(user);
            } else {
                Toast.makeText(this, "管理员默认拥有所有权限", Toast.LENGTH_SHORT).show();
            }
        });
        
        rvUsers.setAdapter(userAdapter);

        // 设置单位名称
        String company = sharedPrefsManager.getUserCompany();
        tvCompanyName.setText(company);
    }
    
    /**
     * 显示权限设置对话框
     * @param user 需要设置权限的用户
     */
    private void showPermissionDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_permissions, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        TextView tvUserNameDisplay = dialogView.findViewById(R.id.tvUserNameDisplay);
        CheckBox cbAllowAddMixture = dialogView.findViewById(R.id.cbAllowAddMixture);
        CheckBox cbAllowAddAsphalt = dialogView.findViewById(R.id.cbAllowAddAsphalt);
        CheckBox cbAllowAddMixratio = dialogView.findViewById(R.id.cbAllowAddMixratio);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        // 设置用户信息
        tvUserNameDisplay.setText("用户: " + user.getName());
        
        // 设置当前用户权限状态
        cbAllowAddMixture.setChecked(user.isAllowAddMixture());
        cbAllowAddAsphalt.setChecked(user.isAllowAddAsphalt());
        cbAllowAddMixratio.setChecked(user.isAllowAddMixratio());
        
        AlertDialog dialog = builder.create();
        
        // 取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            // 更新用户权限状态
            boolean allowAddMixture = cbAllowAddMixture.isChecked();
            boolean allowAddAsphalt = cbAllowAddAsphalt.isChecked();
            boolean allowAddMixratio = cbAllowAddMixratio.isChecked();
            
            // 创建权限参数
            Map<String, Boolean> permissions = new HashMap<>();
            permissions.put("allowAddMixture", allowAddMixture);
            permissions.put("allowAddAsphalt", allowAddAsphalt);
            permissions.put("allowAddMixratio", allowAddMixratio);
            
            // 调用API更新权限
            updateUserPermissions(user.getId(), permissions, dialog);
        });
        
        dialog.show();
    }
    
    /**
     * 更新用户权限
     * @param userId 用户ID
     * @param permissions 权限参数
     * @param dialog 对话框实例，用于成功后关闭
     */
    private void updateUserPermissions(int userId, Map<String, Boolean> permissions, AlertDialog dialog) {
        // 显示加载状态
        Toast.makeText(this, "正在更新权限...", Toast.LENGTH_SHORT).show();
        
        apiService.updateUserPermissions(userId, permissions).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserManagementActivity.this, "权限更新成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    
                    // 刷新用户列表
                    loadUsers();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "未知错误";
                    Toast.makeText(UserManagementActivity.this, "权限更新失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(UserManagementActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "更新用户权限失败", t);
            }
        });
    }

    private void loadUsers() {
        // 显示加载状态
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        
        // 获取当前用户单位ID
        String companyId = sharedPrefsManager.getUserCompany();
        
        // 调用API获取同一单位的所有用户
        apiService.getCompanyUsers(companyId).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<User> users = response.body().getData();
                    
                    if (users != null && !users.isEmpty()) {
                        // 更新适配器数据
                        userAdapter.updateUsers(users);
                        rvUsers.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                    } else {
                        // 显示空状态
                        rvUsers.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 显示错误提示
                    showError("获取用户列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("网络错误: " + t.getMessage());
                Log.e(TAG, "获取用户列表失败", t);
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvEmptyState.setText("加载失败: " + message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
