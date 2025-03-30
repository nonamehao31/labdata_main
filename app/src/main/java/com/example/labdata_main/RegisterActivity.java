package com.example.labdata_main;

import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ContextThemeWrapper;
import android.view.Window;
import java.io.IOException;

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
import java.util.concurrent.atomic.AtomicBoolean;

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

    // 成员变量：用于跟踪当前活动的对话框
    private ProgressDialog mProgressDialog = null;

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
                    // databaseHelper.checkEmail(email) 
                    // showProgressDialog("正在检查...");
                    
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
                password.equals(confirmPassword);

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
                handleRegister();
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
     * 处理注册
     */
    private void handleRegister() {
        final String company = etCompany.getText().toString().trim();
        final String name = etName.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString();
        final String confirmPassword = etConfirmPassword.getText().toString();
        final int userType = rgUserType.getCheckedRadioButtonId() == R.id.rbAdmin ? 1 : 0;

        // 验证必填字段
        if (company.isEmpty()) {
            etCompany.setError("请输入单位名称");
            etCompany.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            etName.setError("请输入姓名");
            etName.requestFocus();
            return;
        }

        // 验证手机号格式
        String phoneError = ValidationUtils.getPhoneErrorMessage(phone);
        if (phoneError != null) {
            etPhone.setError(phoneError);
            etPhone.requestFocus();
            return;
        }

        // 验证邮箱
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("请输入有效的邮箱地址");
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

        // showProgressDialog("正在注册...");

        // 使用新的检查邮箱是否存在
        checkEmailExistsOnServer(email, new EmailCheckCallback() {
            @Override
            public void onCheckComplete(boolean emailExists) {
                if (emailExists) {
                    // dismissProgressDialog();
                    runOnUiThread(() -> {
                        etEmail.setError("该邮箱已被注册");
                        etEmail.requestFocus();
                    });
                    return;
                }
                
                // 邮箱不存在，继续注册流程
                runOnUiThread(() -> showProgressDialog("正在注册..."));
                
                User user = new User(company, name, phone, email, password, userType);
                
                // 保存用户信息到本地数据库
                long id = databaseHelper.addUser(user);
                if (id != -1) {
                    // 保存登录状态
                    sharedPrefsManager.saveUserLoginSession(
                        (int) id,
                        email,
                        email, // 用户名 - 注册时默认使用邮箱作为用户名
                        company,
                        phone,
                        userType,
                        name // 用户真实姓名
                    );
                    
                    // 同时将用户数据发送到后端API
                    submitToServer(name, email, phone, password, company);
                    
                    dismissProgressDialog();
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish();  // 关闭注册页面
                    });
                } else {
                    dismissProgressDialog();
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    /**
     * 将用户数据提交到服务器
     */
    private void submitToServer(String name, String email, String phone, String password, String organization) {
        // 检查网络连接
        if (!NetworkUtils.isNetworkConnected(this)) {
            Log.w(TAG, "无网络连接，无法提交用户数据到服务器");
            Toast.makeText(this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "开始检查邮箱是否已被注册: " + email);
        
        // 创建API服务
        ApiService apiService = RetrofitClient.getInstance(this).createService(ApiService.class);
        
        // 创建注册请求
        String username = generateValidUsername(email);
        
        // 获取用户类型（管理员状态）
        int userType = rgUserType.getCheckedRadioButtonId() == R.id.rbAdmin ? 1 : 0;
        Boolean isAdmin = userType == 1;
        Log.d(TAG, "用户类型: " + (isAdmin ? "管理员" : "普通用户") + ", userType值: " + userType);
        
        // 使用包含admin字段的构造函数
        RegisterRequest request = new RegisterRequest(name, username, email, password, phone, organization, isAdmin);
        
        // 关闭任何可能存在的进度对话框
        dismissProgressDialog();
        
        // 显示新的进度对话框
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在注册...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        
        // 标记是否已处理响应，防止多次处理
        final AtomicBoolean responseHandled = new AtomicBoolean(false);
        
        // 发送注册请求
        Call<ApiResponse<Void>> call = apiService.register(request);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // 如果Activity已销毁或响应已处理，直接返回
                if (isFinishing() || isDestroyed() || responseHandled.getAndSet(true)) {
                    dismissProgressDialog();
                    return;
                }
                
                // 关闭进度对话框
                dismissProgressDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "服务器注册成功: " + apiResponse.getMessage());
                        Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                        
                        // 进入登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        // API成功但业务逻辑失败
                        handleRegistrationError(apiResponse.getMessage());
                    }
                } else {
                    // API调用失败
                    String errorBodyString = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "读取错误响应体失败", e);
                    }
                    
                    handleErrorBody(errorBodyString, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // 如果Activity已销毁或响应已处理，直接返回
                if (isFinishing() || isDestroyed() || responseHandled.getAndSet(true)) {
                    dismissProgressDialog();
                    return;
                }
                
                // 关闭进度对话框
                dismissProgressDialog();
                
                if (t instanceof IOException) {
                    Log.e(TAG, "网络错误: " + t.getMessage(), t);
                    Toast.makeText(RegisterActivity.this, "网络连接失败，请检查网络设置", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "注册失败: " + t.getMessage(), t);
                    Toast.makeText(RegisterActivity.this, "注册失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        // 超时处理
        new Handler().postDelayed(() -> {
            if (!call.isCanceled() && !responseHandled.get()) {
                Log.w(TAG, "注册超时");
                call.cancel();
                if (!isFinishing() && !isDestroyed() && !responseHandled.getAndSet(true)) {
                    dismissProgressDialog();
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册超时，请稍后重试", Toast.LENGTH_LONG).show();
                    });
                }
            }
        }, 30000); // 30秒超时
    }

    /**
     * 生成有效的用户名（符合后端验证规则）
     * 基于邮箱前缀，但确保符合用户名规范：只允许字母、数字和下划线，长度5-20位
     *
     * @param email 用户邮箱
     * @return 符合规范的用户名
     */
    private String generateValidUsername(String email) {
        // 获取邮箱前缀作为用户名基础
        String baseUsername = email.substring(0, email.indexOf('@'));
        
        // 移除所有非法字符（只保留字母、数字和下划线）
        String cleanUsername = baseUsername.replaceAll("[^a-zA-Z0-9_]", "");
        
        // 如果清理后长度不足5位，添加随机数字作为后缀
        if (cleanUsername.length() < 5) {
            StringBuilder sb = new StringBuilder(cleanUsername);
            // 添加随机数字直到长度达到5位
            while (sb.length() < 5) {
                sb.append((int) (Math.random() * 10));
            }
            cleanUsername = sb.toString();
        } 
        // 如果长度超过20位，截取前20位
        else if (cleanUsername.length() > 20) {
            cleanUsername = cleanUsername.substring(0, 20);
        }
        
        return cleanUsername;
    }

    /**
     * 处理注册错误信息
     * @param message 错误信息
     */
    private void handleRegistrationError(String message) {
        // 显示错误信息提示
        Toast.makeText(this, "注册失败: " + message, Toast.LENGTH_LONG).show();

        // 根据错误信息提示对应的输入框
        if (message.toLowerCase().contains("email") || message.toLowerCase().contains("邮箱")) {
            etEmail.setError(message);
            etEmail.requestFocus();
        } else if (message.toLowerCase().contains("username") || message.toLowerCase().contains("用户名")) {
            // 用户名错误提示在邮箱输入框显示，因为用户名是基于邮箱前缀生成的
            etEmail.setError("用户名无效或已被占用: " + message);
            etEmail.requestFocus();
        } else if (message.toLowerCase().contains("password") || message.toLowerCase().contains("密码")) {
            etPassword.setError(message);
            etPassword.requestFocus();
        } else if (message.toLowerCase().contains("phone") || message.toLowerCase().contains("手机号") || 
                  message.toLowerCase().contains("电话")) {
            etPhone.setError(message);
            etPhone.requestFocus();
        } else if (message.toLowerCase().contains("name") || message.toLowerCase().contains("姓名") || 
                  message.toLowerCase().contains("名字")) {
            etName.setError(message);
            etName.requestFocus();
        } else if (message.toLowerCase().contains("organization") || message.toLowerCase().contains("单位") || 
                  message.toLowerCase().contains("公司")) {
            etCompany.setError(message);
            etCompany.requestFocus();
        }
    }

    /**
     * 处理错误响应体
     * @param errorBody 错误响应体
     * @param responseCode HTTP响应码
     */
    private void handleErrorBody(String errorBody, int responseCode) {
        try {
            // 尝试从错误响应体中提取错误信息
            if (errorBody.contains("message")) {
                // 简单的JSON解析，直接提取message字段的值
                int startIndex = errorBody.indexOf("message") + 10; // "message":" 的长度
                int endIndex = errorBody.indexOf("\"", startIndex);
                if (startIndex > 10 && endIndex > startIndex) {
                    String errorMessage = errorBody.substring(startIndex, endIndex);
                    handleRegistrationError(errorMessage);
                    return;
                }
            }
            
            // 如果无法提取错误信息，显示通用的错误提示
            String msg = "服务器错误 (" + responseCode + ")";
            if (responseCode == 400) {
                msg = "请求参数错误，请检查输入";
            } else if (responseCode == 409) {
                msg = "用户名或邮箱已被占用";
                etEmail.setError("用户名或邮箱已被占用");
                etEmail.requestFocus();
            } else if (responseCode == 401 || responseCode == 403) {
                msg = "无权访问，请检查登录状态";
            } else if (responseCode >= 500) {
                msg = "服务器内部错误，请稍后重试";
            }
            
            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "解析错误响应体失败: " + e.getMessage(), e);
            Toast.makeText(RegisterActivity.this, "解析响应失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 安全地关闭进度对话框
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                // 窗口已分离，记录但不处理
                Log.w(TAG, "关闭对话框时出现异常: " + e.getMessage());
            } finally {
                mProgressDialog = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源，防止内存泄漏
        dismissProgressDialog();
    }

    /**
     * 显示进度对话框
     * @param message 对话框显示的消息
     */
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    /**
     * 检查邮箱是否已被注册
     * @param email 邮箱地址
     * @param callback 回调函数，返回检查结果
     */
    private void checkEmailExistsOnServer(String email, final EmailCheckCallback callback) {
        // 检查网络连接
        if (!NetworkUtils.isNetworkConnected(this)) {
            Log.w(TAG, "检查邮箱: 无网络连接");
            Toast.makeText(this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show();
            callback.onCheckComplete(false); // 网络错误，返回false
            return;
        }
        
        Log.d(TAG, "开始检查邮箱是否已被注册: " + email);
        
        // 创建API服务
        ApiService apiService = RetrofitClient.getInstance(this).createService(ApiService.class);
        
        // 发送检查邮箱请求
        Call<ApiResponse<Boolean>> call = apiService.checkEmailExists(email);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Boolean exists = response.body().getData();
                    Log.d(TAG, "邮箱检查结果: " + (exists != null && exists));
                    callback.onCheckComplete(exists != null && exists);
                } else {
                    // 检查邮箱失败，显示错误信息
                    String errorMsg = "";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg = "无法解析错误响应体";
                        }
                    }
                    Log.e(TAG, "检查邮箱失败: " + errorMsg + ", code: " + response.code());
                    
                    Toast.makeText(RegisterActivity.this, "检查邮箱失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    callback.onCheckComplete(false);  // 检查失败，返回false
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                // 检查邮箱失败，显示错误信息
                Log.e(TAG, "检查邮箱失败: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "检查邮箱失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                callback.onCheckComplete(false);  // 检查失败，返回false
            }
        });
        
        // 超时处理
        new Handler().postDelayed(() -> {
            if (!call.isCanceled() && !call.isExecuted()) {
                Log.w(TAG, "检查邮箱超时");
                call.cancel();
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "检查邮箱超时", Toast.LENGTH_SHORT).show();
                    callback.onCheckComplete(false);  // 超时，返回false
                });
            }
        }, 10000);  // 10秒超时
    }

    /**
     * 邮箱检查回调函数
     */
    public interface EmailCheckCallback {
        void onCheckComplete(boolean emailExists);
    }
}
