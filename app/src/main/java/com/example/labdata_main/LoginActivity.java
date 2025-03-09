package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.request.LoginRequest;
import com.example.labdata_main.api.response.LoginResponse;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.User;
import com.example.labdata_main.utils.JwtUtils;
import com.example.labdata_main.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 登录界面Activity
 * 处理用户登录和注册跳转功能
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelper databaseHelper;
    private SharedPrefsManager sharedPrefsManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        // 初始化工具类
        databaseHelper = new DatabaseHelper(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        apiService = ApiClient.getApiService();

        // 检查是否已登录
        if (sharedPrefsManager.isLoggedIn()) {
            startMainActivity();
            finish();
            return;
        }

        // 初始化视图
        initViews();
        // 设置点击事件
        setClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }

        // 显示登录中提示
        Toast.makeText(this, "登录中...", Toast.LENGTH_SHORT).show();
        btnLogin.setEnabled(false);

        // 创建登录请求对象
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        // 调用后端API进行认证
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "API登录成功. 用户ID: " + loginResponse.getUserId());
                    
                    // 查询本地数据库获取用户类型和其他信息
                    User user = databaseHelper.getUserByEmail(email);
                    if (user != null) {
                        // 保存登录状态和用户信息
                        // 保存用户登录会话信息，包括用户ID、邮箱、用户名、公司ID、电话号码和用户类型
                        // 如果API返回的公司ID不为空且非空字符串，则优先使用API返回的公司ID，否则使用本地数据库中的公司ID
                        sharedPrefsManager.saveUserLoginSession(
                            loginResponse.getUserId(),
                            email,
                            loginResponse.getUsername(),
                            loginResponse.getCompanyId() != null && !loginResponse.getCompanyId().trim().isEmpty() ?
                                loginResponse.getCompanyId() : user.getCompany(), // 优先使用API返回的公司ID
                            user.getPhone(),
                            user.getUserType()
                        );
                        
                        // 保存token信息
                        sharedPrefsManager.saveAuthToken(
                            loginResponse.getAccessToken(),
                            loginResponse.getTokenType()
                        );
                        
                        // 解析并保存JWT令牌信息用于测试
                        String authToken = loginResponse.getTokenType() + " " + loginResponse.getAccessToken();
                        Log.d(TAG, "登录成功后的令牌信息:");
                        JwtUtils.decodeAndLogJwt(authToken);
                        
                        // 重置API客户端，确保新的认证令牌生效
                        ApiClient.resetClient();
                        
                        // 登录成功，跳转到主界面
                        startMainActivity();
                        finish();
                    } else {
                        // 用户在后端存在但本地数据库没有记录，创建本地记录
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(loginResponse.getUsername());
                        newUser.setPassword(password); // 密码应该加密保存
                        newUser.setUserType(0); // 默认普通用户(0:实验员, 1:管理员)
                        
                        long userId = databaseHelper.addUser(newUser);
                        if (userId > 0) {
                            // 保存登录状态和用户信息
                            // 确保公司ID有效
                            // 如果API返回的公司ID不为空且不为空白，则使用API返回的公司ID，否则使用默认值
                            sharedPrefsManager.saveUserLoginSession(
                                loginResponse.getUserId(),
                                email,
                                loginResponse.getUsername(),
                                loginResponse.getCompanyId() != null && !loginResponse.getCompanyId().trim().isEmpty() ? 
                                    loginResponse.getCompanyId() : "default", // 使用API返回的公司ID或默认值
                                "", // 电话信息为空
                                0  // 默认用户类型
                            );
                            
                            // 保存token信息
                            sharedPrefsManager.saveAuthToken(
                                loginResponse.getAccessToken(),
                                loginResponse.getTokenType()
                            );
                            
                            // 解析并保存JWT令牌信息用于测试
                            String authToken = loginResponse.getTokenType() + " " + loginResponse.getAccessToken();
                            Log.d(TAG, "登录成功后的令牌信息:");
                            JwtUtils.decodeAndLogJwt(authToken);
                            
                            // 重置API客户端，确保新的认证令牌生效
                            ApiClient.resetClient();
                            
                            // 登录成功，跳转到主界面
                            startMainActivity();
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "本地数据保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // 登录失败
                    String errorMsg = "登录失败: ";
                    if (response.errorBody() != null) {
                        errorMsg += "服务器验证错误";
                    } else {
                        errorMsg += "账号或密码错误";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                // 网络错误
                Toast.makeText(LoginActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "登录网络请求失败", t);
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
