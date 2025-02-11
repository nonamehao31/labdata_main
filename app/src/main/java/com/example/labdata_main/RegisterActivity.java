package com.example.labdata_main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.User;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.utils.ValidationUtils;

/**
 * 注册界面Activity
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etCompany;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private RadioGroup rgUserType;

    private DatabaseHelper databaseHelper;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);

        // 初始化工具类
        databaseHelper = new DatabaseHelper(this);
        sharedPrefsManager = new SharedPrefsManager(this);

        // 初始化界面控件
        initViews();
        // 设置输入监听
        setupInputValidation();
        // 设置点击事件监听
        setClickListeners();
    }

    /**
     * 初始化界面控件
     */
    private void initViews() {
        etCompany = findViewById(R.id.etCompany);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        rgUserType = findViewById(R.id.rgUserType);
    }

    /**
     * 设置输入验证
     */
    private void setupInputValidation() {
        // 邮箱输入验证
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!ValidationUtils.isValidEmail(email)) {
                    etEmail.setError("请输入有效的邮箱地址");
                } else {
                    // 检查邮箱是否已被注册
                    if (databaseHelper.checkEmail(email)) {
                        etEmail.setError("该邮箱已被注册");
                    } else {
                        etEmail.setError(null);
                    }
                }
                updateRegisterButtonState();
            }
        });

        // 密码输入验证
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswords();
            }
        };
        etPassword.addTextChangedListener(passwordWatcher);
        etConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    /**
     * 验证密码
     */
    private void validatePasswords() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证密码强度
        String errorMessage = ValidationUtils.getPasswordStrengthMessage(password);
        if (errorMessage != null) {
            etPassword.setError(errorMessage);
        } else {
            etPassword.setError(null);
        }

        // 验证两次密码是否一致
        if (!TextUtils.isEmpty(confirmPassword) && !password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
        } else {
            etConfirmPassword.setError(null);
        }

        updateRegisterButtonState();
    }

    /**
     * 更新注册按钮状态
     */
    private void updateRegisterButtonState() {
        String company = etCompany.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = !TextUtils.isEmpty(company) &&
                !TextUtils.isEmpty(name) &&
                !TextUtils.isEmpty(phone) &&
                ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isValidPassword(password) &&
                password.equals(confirmPassword) &&
                !databaseHelper.checkEmail(email);

        btnRegister.setEnabled(isValid);
        btnRegister.setAlpha(isValid ? 1.0f : 0.5f);
    }

    /**
     * 设置点击事件监听
     */
    private void setClickListeners() {
        // 注册按钮点击事件
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        // 登录链接点击事件
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 尝试注册
     */
    private void attemptRegister() {
        // 获取输入的用户信息
        String company = etCompany.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 获取选择的用户类型
        int userType = rgUserType.getCheckedRadioButtonId() == R.id.rbAdmin ? 1 : 0;

        // 验证必填字段
        if (TextUtils.isEmpty(company)) {
            etCompany.setError("请输入单位名称");
            etCompany.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            etName.setError("请输入姓名");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("请输入电话号码");
            etPhone.requestFocus();
            return;
        }

        // 验证邮箱
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("请输入有效的邮箱地址");
            etEmail.requestFocus();
            return;
        }

        // 检查邮箱是否已被注册
        if (databaseHelper.checkEmail(email)) {
            etEmail.setError("该邮箱已被注册");
            etEmail.requestFocus();
            return;
        }

        // 验证密码
        String errorMessage = ValidationUtils.getPasswordStrengthMessage(password);
        if (errorMessage != null) {
            etPassword.setError(errorMessage);
            etPassword.requestFocus();
            return;
        }

        // 验证确认密码
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            etConfirmPassword.requestFocus();
            return;
        }

        // 创建用户对象
        User user = new User(company, name, phone, email, password, userType);

        // 保存用户信息到数据库
        long id = databaseHelper.addUser(user);
        if (id != -1) {
            // 保存登录状态
            sharedPrefsManager.saveUserLoginSession(
                (int) id,
                email,
                name,
                company,
                phone,
                userType
            );
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 检查是否是邮箱重复导致的失败
            if (databaseHelper.checkEmail(email)) {
                etEmail.setError("该邮箱已被注册");
                etEmail.requestFocus();
            } else {
                Toast.makeText(this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
