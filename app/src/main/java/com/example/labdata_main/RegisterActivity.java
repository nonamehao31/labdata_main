package com.example.labdata_main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.labdata_main.api.ApiConfig;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.RetrofitClient;
import com.example.labdata_main.api.request.RegisterRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.User;
import com.example.labdata_main.utils.NetworkUtils;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.utils.ValidationUtils;

import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 注册界面Activity
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private EditText etCompany;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private RadioGroup rgUserType;
    private boolean isKeyboardShowing = false;
    private int screenHeight;
    private int screenWidth;
    private boolean isButtonInVisibleArea = false; // 添加变量定义

    private DatabaseHelper databaseHelper;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);

        // 获取屏幕尺寸
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        Log.d(TAG, "屏幕尺寸: 宽=" + screenWidth + ", 高=" + screenHeight);

        // 初始化工具类
        databaseHelper = new DatabaseHelper(this);
        sharedPrefsManager = new SharedPrefsManager(this);

        // 初始化界面控件
        initViews();
        // 设置输入监听
        setupInputValidation();
        // 设置点击事件监听
        setClickListeners();
        // 设置键盘监听
        setupKeyboardVisibilityListener();
        
        // 调试日志
        Log.d(TAG, "onCreate: 初始化完成，按钮可见性: " + 
            (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
        
        // 记录按钮位置
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                logButtonPosition("onCreate-post");
            }
        });
    }

    /**
     * 记录按钮位置和尺寸信息
     */
    private void logButtonPosition(String where) {
        if (btnRegister == null) {
            Log.e(TAG, where + ": 按钮为空");
            return;
        }
        
        int[] btnLocation = new int[2];
        btnRegister.getLocationOnScreen(btnLocation);
        
        // 获取当前窗口可见区域
        Rect visibleFrame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
        
        Log.d(TAG, where + ": 屏幕尺寸: 宽=" + screenWidth + ", 高=" + screenHeight);
        Log.d(TAG, where + ": 可见区域: 左=" + visibleFrame.left + ", 上=" + visibleFrame.top + 
              ", 右=" + visibleFrame.right + ", 下=" + visibleFrame.bottom + 
              ", 宽=" + visibleFrame.width() + ", 高=" + visibleFrame.height());
        Log.d(TAG, where + ": 按钮位置: x=" + btnLocation[0] + ", y=" + btnLocation[1] + 
              ", 宽度=" + btnRegister.getWidth() + ", 高度=" + btnRegister.getHeight() + 
              ", 底部y=" + (btnLocation[1] + btnRegister.getHeight()));
        
        // 检查按钮是否在屏幕可见区域内
        boolean isButtonVisibleOnScreen = btnLocation[1] >= 0 && 
                            btnLocation[1] + btnRegister.getHeight() <= screenHeight &&
                            btnLocation[0] >= 0 && 
                            btnLocation[0] + btnRegister.getWidth() <= screenWidth;
        
        // 检查按钮是否在当前窗口可见区域内（考虑键盘等因素）
        boolean isButtonVisibleInWindow = btnLocation[1] >= visibleFrame.top && 
                            btnLocation[1] + btnRegister.getHeight() <= visibleFrame.bottom &&
                            btnLocation[0] >= visibleFrame.left && 
                            btnLocation[0] + btnRegister.getWidth() <= visibleFrame.right;
        
        Log.d(TAG, where + ": 按钮在屏幕可见区域内: " + isButtonVisibleOnScreen);
        Log.d(TAG, where + ": 按钮在窗口可见区域内: " + isButtonVisibleInWindow);
        
        // 如果按钮不在窗口可见区域内，提供更详细的信息
        if (!isButtonVisibleInWindow) {
            if (btnLocation[1] < visibleFrame.top) {
                Log.d(TAG, where + ": 按钮在可见区域上方: 按钮顶部=" + btnLocation[1] + ", 可见区域顶部=" + visibleFrame.top);
            }
            if (btnLocation[1] + btnRegister.getHeight() > visibleFrame.bottom) {
                Log.d(TAG, where + ": 按钮在可见区域下方: 按钮底部=" + (btnLocation[1] + btnRegister.getHeight()) + 
                      ", 可见区域底部=" + visibleFrame.bottom + 
                      ", 超出=" + (btnLocation[1] + btnRegister.getHeight() - visibleFrame.bottom) + "像素");
            }
        }
        
        // 检查按钮的父视图链
        ViewGroup parent = (ViewGroup) btnRegister.getParent();
        StringBuilder parentChain = new StringBuilder();
        while (parent != null) {
            parentChain.append(parent.getClass().getSimpleName())
                    .append("(").append(parent.getVisibility() == View.VISIBLE ? "VISIBLE" : "INVISIBLE/GONE").append(")")
                    .append(" -> ");
            
            if (parent.getParent() instanceof ViewGroup) {
                parent = (ViewGroup) parent.getParent();
            } else {
                parent = null;
            }
        }
        
        Log.d(TAG, where + ": 按钮父视图链: " + parentChain.toString());
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

        
        // 强制设置按钮可见
        btnRegister.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "初始化视图: 按钮ID=" + getResources().getResourceEntryName(btnRegister.getId()));
        
        // 为邮箱输入框添加焦点变化监听器，确保在获取焦点时滚动到可见区域
        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(TAG, "邮箱输入框获取了焦点");
                    // 延迟一点时间等键盘弹出后再滚动
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 新布局中直接使用ScrollView作为根布局，无需查找ID
                            // 确保表单滚动到足够位置
                            ScrollView scrollView = findViewById(R.id.root_scroll_view);
                            if (scrollView != null) {
                                scrollView.smoothScrollBy(0, 200); // 向下滚动
                            }
                            
                            // 记录按钮位置
                            logButtonPosition("邮箱获取焦点-滚动");
                        }
                    }, 300);
                } else {
                    Log.d(TAG, "邮箱输入框失去了焦点");
                }
            }
        });
    }

    /**
     * 设置键盘可见性监听
     */
    private void setupKeyboardVisibilityListener() {
        final View rootView = findViewById(android.R.id.content);
        
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                
                // 计算键盘高度
                int keyboardHeight = screenHeight - r.bottom;
                
                // 一般认为超过屏幕高度的1/4为键盘显示状态
                boolean isKeyboardNowVisible = keyboardHeight > screenHeight * 0.15;
                
                if (isKeyboardNowVisible != isKeyboardShowing) {
                    isKeyboardShowing = isKeyboardNowVisible;
                    
                    if (isKeyboardShowing) {
                        // 键盘显示时，确保按钮可见
                        btnRegister.setVisibility(View.VISIBLE);
                        
                        logButtonPosition("键盘显示");
                        
                        // 计算按钮是否在可见区域内
                        int[] btnLocation = new int[2];
                        btnRegister.getLocationOnScreen(btnLocation);
                        isButtonInVisibleArea = btnLocation[1] + btnRegister.getHeight() <= r.bottom;
                        Log.d(TAG, "按钮是否在可见区域内: " + isButtonInVisibleArea);
                        
                        // 如果按钮不在可见区域内，强制调整滚动位置
                        if (!isButtonInVisibleArea) {
                            int scrollDistance = (btnLocation[1] + btnRegister.getHeight()) - r.bottom + 100; // 额外空间
                            // 直接使用ID查找ScrollView
                            ScrollView scrollView = findViewById(R.id.root_scroll_view);
                            if (scrollView != null) {
                                scrollView.smoothScrollBy(0, scrollDistance);
                                Log.d(TAG, "滚动调整后按钮仍不可见，额外滚动: " + scrollDistance + "像素");
                                
                                scrollView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 记录滚动后的位置
                                        logButtonPosition("键盘显示-滚动后");
                                        
                                        // 再次检查按钮是否在可见区域内
                                        Rect r = new Rect();
                                        rootView.getWindowVisibleDisplayFrame(r);
                                        int[] btnLocation = new int[2];
                                        btnRegister.getLocationOnScreen(btnLocation);
                                        boolean isVisibleNow = btnLocation[1] + btnRegister.getHeight() <= r.bottom;
                                        
                                        Log.d(TAG, "滚动调整后按钮在可见区域内: " + isVisibleNow);
                                        
                                        // 如果仍然不在可见区域，尝试再次滚动
                                        if (!isVisibleNow) {
                                            int additionalScroll = (btnLocation[1] + btnRegister.getHeight()) - r.bottom + 100;
                                            // 直接使用ID查找ScrollView
                                            ScrollView scrollView = findViewById(R.id.root_scroll_view);
                                            if (scrollView != null) {
                                                scrollView.smoothScrollBy(0, additionalScroll);
                                                Log.d(TAG, "滚动调整后按钮仍不可见，额外滚动: " + additionalScroll + "像素");
                                            }
                                        }
                                    }
                                }, 300);
                            }
                        }
                        
                        View focusedView = getCurrentFocus();
                        if (focusedView == etEmail) {
                            // 如果邮箱输入框获取焦点，向下滚动确保视图不被键盘遮挡
                            // 新布局中直接使用ScrollView作为根布局
                            ScrollView scrollView = findViewById(R.id.root_scroll_view);
                            if (scrollView != null) {
                                scrollView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.smoothScrollBy(0, 300);
                                        
                                        // 记录滚动后的位置
                                        logButtonPosition("键盘显示-邮箱焦点-滚动后");
                                    }
                                }, 200);
                            }
                        }
                    } else {
                        // 键盘隐藏时也记录位置
                        logButtonPosition("键盘隐藏");
                    }
                }
            }
        });
        
        // 额外添加键盘状态监听（更简单的方式）
        rootView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int bottom;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    bottom = insets.getInsets(WindowInsets.Type.ime()).bottom;
                } else {
                    bottom = insets.getStableInsetBottom();
                }
                
                boolean keyboardVisible = bottom > 100; // 阈值判断键盘是否显示
                
                Log.d(TAG, "键盘状态检测(OnApplyWindowInsets): 键盘可能" + 
                     (keyboardVisible ? "显示" : "隐藏") + ", 底部插入: " + bottom);
                
                // 强制确保按钮可见
                if (keyboardVisible) {
                    btnRegister.post(new Runnable() {
                        @Override
                        public void run() {
                            btnRegister.setVisibility(View.VISIBLE);
                            logButtonPosition("OnApplyWindowInsets-键盘显示");
                        }
                    });
                }
                
                return v.onApplyWindowInsets(insets);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "配置变化: orientation=" + newConfig.orientation);
        
        // 延迟记录按钮位置
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                logButtonPosition("onConfigurationChanged");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保按钮在恢复时可见
        if (btnRegister != null) {
            btnRegister.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "onResume: 注册按钮可见性: " + 
            (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
        
        // 记录按钮位置
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                logButtonPosition("onResume");
            }
        });
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "窗口焦点变化: " + hasFocus);
        
        if (hasFocus) {
            // 记录按钮位置
            logButtonPosition("onWindowFocusChanged");
        }
    }

    /**
     * 设置输入验证
     */
    private void setupInputValidation() {
        // 所有编辑框的文本变化监听
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "文本变化: 按钮可见性: " + 
                    (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
                
                // 强制设置按钮可见
                btnRegister.setVisibility(View.VISIBLE);
            }
        };
        
        // 为所有编辑框添加文本变化监听
        etCompany.addTextChangedListener(generalTextWatcher);
        etName.addTextChangedListener(generalTextWatcher);
        etPhone.addTextChangedListener(generalTextWatcher);
        
        // 邮箱输入验证
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "邮箱输入前: 按钮可见性: " + 
                    (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
                
                // 记录按钮位置和尺寸
                logButtonPosition("邮箱输入前");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "邮箱输入中: 按钮可见性: " + 
                    (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
                
                // 记录按钮位置和尺寸
                logButtonPosition("邮箱输入中");
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                Log.d(TAG, "邮箱输入后: 开始验证邮箱: " + email);
                
                // 验证邮箱
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
                
                // 验证并更新按钮状态
                updateRegisterButtonState();
                
                Log.d(TAG, "邮箱输入后: 按钮可见性: " + 
                    (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
                
                // 强制设置按钮可见
                btnRegister.setVisibility(View.VISIBLE);
                
                // 延迟记录按钮位置，确保布局更新
                btnRegister.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "邮箱输入后-post: 按钮强制可见后位置检查");
                        logButtonPosition("邮箱输入后-post");
                        
                        // 检查按钮是否在屏幕可见区域内
                        int[] btnLocation = new int[2];
                        btnRegister.getLocationOnScreen(btnLocation);
                        Rect visibleFrame = new Rect();
                        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
                        
                        boolean visible = btnLocation[1] + btnRegister.getHeight() <= visibleFrame.bottom &&
                                         btnLocation[1] >= visibleFrame.top;
                        
                        Log.d(TAG, "按钮在当前可见区域中: " + visible + 
                              ", 按钮底部位置: " + (btnLocation[1] + btnRegister.getHeight()) + 
                              ", 可见区域底部: " + visibleFrame.bottom);
                        
                        // 如果不在可见区域内，尝试滚动到按钮位置
                        if (!visible) {
                            Log.d(TAG, "按钮不在可见区域，尝试滚动");
                            int scrollDistance = (btnLocation[1] + btnRegister.getHeight()) - visibleFrame.bottom + 100; // 额外空间
                            ScrollView scrollView = findViewById(R.id.root_scroll_view);
                            if (scrollView != null) {
                                scrollView.smoothScrollBy(0, scrollDistance);
                            }
                            
                            // 再次记录滚动后的位置
                            btnRegister.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    logButtonPosition("邮箱输入后-滚动调整后");
                                }
                            }, 300);
                        }
                    }
                });
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
                
                // 强制设置按钮可见
                btnRegister.setVisibility(View.VISIBLE);
                
                Log.d(TAG, "密码输入后: 按钮可见性: " + 
                    (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
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

        Log.d(TAG, "更新按钮状态前: 按钮可见性: " + 
            (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
        logButtonPosition("updateRegisterButtonState-before");
            
        // 始终保持按钮可见，只改变其状态（启用/禁用）
        btnRegister.setEnabled(isValid);
        btnRegister.setAlpha(isValid ? 1.0f : 0.8f);
        
        // 强制设置按钮可见
        btnRegister.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "更新按钮状态后: 按钮可见性: " + 
            (btnRegister.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE") +
            ", 有效性: " + isValid);
            
        // 记录位置变化及可见性
        btnRegister.post(new Runnable() {
            @Override
            public void run() {
                logButtonPosition("updateRegisterButtonState-after");
                
                // 检查是否在可视区域内
                Rect visibleFrame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
                int[] btnLocation = new int[2];
                btnRegister.getLocationOnScreen(btnLocation);
                
                Log.d(TAG, "按钮位置: y=" + btnLocation[1] + ", 高度=" + btnRegister.getHeight() + 
                      ", 按钮底部=" + (btnLocation[1] + btnRegister.getHeight()) + 
                      ", 可视区域底部=" + visibleFrame.bottom);
                      
                boolean isVisible = btnLocation[1] >= visibleFrame.top && 
                                   btnLocation[1] + btnRegister.getHeight() <= visibleFrame.bottom;
                                   
                Log.d(TAG, "按钮在屏幕可视区域内: " + isVisible);
                
                // 如果不在可视区域内，尝试调整滚动位置
                if (!isVisible && btnLocation[1] + btnRegister.getHeight() > visibleFrame.bottom) {
                    Log.d(TAG, "尝试调整滚动位置使按钮可见");
                    int scrollY = btnLocation[1] + btnRegister.getHeight() - visibleFrame.bottom + 100; // 额外空间
                    ScrollView scrollView = findViewById(R.id.root_scroll_view);
                    if (scrollView != null) {
                        scrollView.smoothScrollBy(0, scrollY);
                    }
                }
            }
        });
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

        // 创建用户对象并保存到本地数据库
        User user = new User(company, name, phone, email, password, userType);
        
        // 保存用户信息到本地数据库
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
            
            // 同时将用户数据发送到后端API
            submitToServer(name, email, phone, password, company);
            
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
    
    /**
     * 将用户数据提交到服务器
     */
    private void submitToServer(String name, String email, String phone, String password, String organization) {
        try {
            // 检查网络连接
            if (!NetworkUtils.isNetworkConnected(this)) {
                Log.w(TAG, "无网络连接，无法提交用户数据到服务器");
                Toast.makeText(this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show();
                return;
            }
            
            String username = email.substring(0, email.indexOf('@')); // 使用邮箱前缀作为用户名
            
            // 创建API服务
            ApiService apiService = RetrofitClient.getInstance().createService(ApiService.class);
            
            // 创建注册请求
            RegisterRequest request = new RegisterRequest(name, username, email, password, phone, organization);
            
            // 显示进度对话框
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在注册...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            // 发送注册请求
            apiService.register(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Log.d(TAG, "服务器注册成功: " + apiResponse.getMessage());
                            Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "服务器注册失败: " + apiResponse.getMessage());
                            Toast.makeText(RegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "服务器注册失败，响应码: " + response.code() + ", 错误信息: " + errorBody);
                                Toast.makeText(RegisterActivity.this, "服务器错误 (" + response.code() + ")", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e(TAG, "服务器注册失败，响应码: " + response.code());
                                Toast.makeText(RegisterActivity.this, "服务器错误 (" + response.code() + ")", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析错误响应失败: " + e.getMessage());
                            Toast.makeText(RegisterActivity.this, "解析响应失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "服务器注册请求失败: " + t.getMessage(), t);
                    Toast.makeText(RegisterActivity.this, "连接服务器失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    // 尝试检查连接是否可达
                    new Thread(() -> {
                        try {
                            URL url = new URL(ApiConfig.BASE_URL);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(5000);
                            connection.setRequestMethod("HEAD");
                            int responseCode = connection.getResponseCode();
                            final String message = "服务器连接测试: " + (responseCode >= 200 && responseCode < 400 ? "成功" : "失败 (" + responseCode + ")");
                            runOnUiThread(() -> Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show());
                        } catch (Exception e) {
                            final String errorMsg = e.getMessage();
                            runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "服务器不可达: " + errorMsg, Toast.LENGTH_LONG).show());
                        }
                    }).start();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "提交到服务器出错: " + e.getMessage(), e);
            Toast.makeText(this, "提交失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
