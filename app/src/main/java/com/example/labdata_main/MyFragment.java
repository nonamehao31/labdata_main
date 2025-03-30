package com.example.labdata_main;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.labdata_main.db.DatabaseHelper;  
import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;

import java.io.File;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * "我的"界面Fragment
 * 显示用户个人信息和提供退出登录功能
 */
public class MyFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int CROP_IMAGE = 2;

    private ImageView ivAvatar;
    private TextView tvCompany;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPosition;
    private Button btnModifyInfo;
    private Button btnLogout;
    private Button btnUserManagement;

    private SharedPrefsManager sharedPrefsManager;
    private Uri tempImageUri;
    private DatabaseHelper databaseHelper; 
    private ApiService apiService; 

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 SharedPrefsManager
        sharedPrefsManager = new SharedPrefsManager(requireContext());
        databaseHelper = new DatabaseHelper(requireContext()); 
        apiService = ApiClient.getApiService(); 

        // 初始化图片选择器
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("MyFragment", "收到图片选择结果: " + result.getResultCode());
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Log.d("MyFragment", "选择的图片URI: " + imageUri);
                    if (imageUri != null) {
                        cropImage(imageUri);
                    } else {
                        Log.e("MyFragment", "选择的图片URI为空");
                    }
                } else {
                    Log.d("MyFragment", "用户取消选择图片或选择失败");
                }
            }
        );

        // 初始化裁剪器
        cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("MyFragment", "收到裁剪结果: " + result.getResultCode());
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (tempImageUri != null) {
                        Log.d("MyFragment", "裁剪完成，临时文件URI: " + tempImageUri);
                        saveAvatarUri(tempImageUri.toString());
                    } else {
                        Log.e("MyFragment", "裁剪完成但临时文件URI为空");
                    }
                } else {
                    Log.d("MyFragment", "用户取消裁剪或裁剪失败");
                }
            }
        );

        // 初始化权限请求处理
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                Log.d("MyFragment", "存储权限请求结果: " + (isGranted ? "已授权" : "已拒绝"));
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(requireContext(), "需要存储权限才能选择头像", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        // 初始化界面控件
        initViews(view);
        // 设置点击事件监听
        setClickListeners();
        // 显示用户信息
        displayUserInfo();

        return view;
    }

    /**
     * 初始化界面控件
     */
    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvCompany = view.findViewById(R.id.tvCompany);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPosition = view.findViewById(R.id.tvPosition);
        btnModifyInfo = view.findViewById(R.id.btnModifyInfo);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnUserManagement = view.findViewById(R.id.btnUserManagement);
    }

    /**
     * 设置点击事件监听
     */
    private void setClickListeners() {
        // 头像点击事件
        ivAvatar.setOnClickListener(v -> checkPermissionAndPickImage());

        // 更改信息按钮点击事件
        btnModifyInfo.setOnClickListener(v -> {
            // 处理编辑信息的点击事件
            Intent intent = new Intent(getActivity(), EditInfoActivity.class);
            startActivity(intent);
        });

        // 权限管理按钮点击事件
        btnUserManagement.setOnClickListener(v -> {
            // 处理权限管理的点击事件
            Intent intent = new Intent(getActivity(), UserManagementActivity.class);
            startActivity(intent);
        });

        // 退出登录按钮点击事件
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * 显示用户信息
     */
    private void displayUserInfo() {
        String company = sharedPrefsManager.getUserCompany();
        String name = sharedPrefsManager.getUserName();
        String email = sharedPrefsManager.getUserEmail();
        String username = sharedPrefsManager.getUserEmail(); // 默认使用邮箱作为用户名

        // 从数据库获取用户类型
        int userType = databaseHelper.getUserTypeByEmail(email);
        
        // 先显示本地存储的信息
        tvName.setText(name);
        tvEmail.setText(email);
        tvCompany.setText(company);
        tvPosition.setText(userType == 1 ? "管理员" : "实验员");
        
        // 根据用户类型显示或隐藏权限管理按钮
        boolean isAdmin = userType == 1;
        btnUserManagement.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        
        // 通过API获取用户组织信息
        fetchUserOrganizationByUsername(username);
        
        Log.d("MyFragment", "显示用户信息:");
        Log.d("MyFragment", "公司ID: " + company);
        Log.d("MyFragment", "姓名: " + name);
        Log.d("MyFragment", "邮箱: " + email);
        Log.d("MyFragment", "用户类型: " + (isAdmin ? "管理员" : "普通用户"));
    }
    
    /**
     * 从数据库获取用户的公司名称
     * @param email 用户邮箱
     * @param fallbackCompanyId 如果查询失败则使用的备用公司ID
     * @return 公司名称
     */
    private String getCompanyNameFromDatabase(String email, String fallbackCompanyId) {
        try {
            // 尝试从数据库获取用户完整信息
            com.example.labdata_main.model.User user = databaseHelper.getUserByEmail(email);
            if (user != null && user.getCompany() != null && !user.getCompany().trim().isEmpty()) {
                return user.getCompany(); // 返回用户表中存储的公司名称
            }
            
            // 如果没有找到用户或公司名称为空，则使用传入的公司ID作为显示值
            return fallbackCompanyId;
        } catch (Exception e) {
            Log.e("MyFragment", "获取公司名称时出错: " + e.getMessage());
            return fallbackCompanyId; // 出错时返回备用ID
        }
    }

    /**
     * 检查权限并打开图片选择器
     */
    private void checkPermissionAndPickImage() {
        Log.d("MyFragment", "检查存储权限");
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyFragment", "请求存储权限");
            ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
        } else {
            Log.d("MyFragment", "已有权限，打开图片选择器");
            openImagePicker();
        }
    }

    /**
     * 打开图片选择器
     */
    private void openImagePicker() {
        Log.d("MyFragment", "启动图片选择器");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            imagePickerLauncher.launch(intent);
            Log.d("MyFragment", "图片选择器已启动");
        } catch (Exception e) {
            Log.e("MyFragment", "启动图片选择器失败: " + e.getMessage());
            Toast.makeText(requireContext(), "无法打开图片选择器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        // 清除登录状态
        sharedPrefsManager.clearUserLoginSession();
        // 跳转到登录界面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayUserInfo();
        loadAvatar();
    }

    private void loadAvatar() {
        String avatarUri = sharedPrefsManager.getAvatarUri();
        Log.d("MyFragment", "Loading avatar URI: " + avatarUri);
        
        if (avatarUri != null && !avatarUri.isEmpty()) {
            try {
                Uri uri = Uri.parse(avatarUri);
                Log.d("MyFragment", "Loading avatar from URI: " + uri);
                
                Glide.with(requireContext())
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivAvatar);
                     
            } catch (Exception e) {
                Log.e("MyFragment", "Error loading avatar: " + e.getMessage());
                e.printStackTrace();
                ivAvatar.setImageResource(R.drawable.circle_avatar_background);
            }
        } else {
            Log.d("MyFragment", "No avatar URI found, using default background");
            ivAvatar.setImageResource(R.drawable.circle_avatar_background);
        }
    }

    private void saveAvatarUri(String uri) {
        Log.d("MyFragment", "Saving avatar URI: " + uri);
        sharedPrefsManager.saveAvatarUri(uri);
        loadAvatar(); // 立即重新加载头像
    }

    private void cropImage(Uri sourceUri) {
        Log.d("MyFragment", "开始裁剪图片，源URI: " + sourceUri);
        
        try {
            // 创建临时文件用于保存裁剪后的图片
            File outputDir = new File(requireContext().getCacheDir(), "images");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, "cropped_" + System.currentTimeMillis() + ".jpg");
            tempImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                outputFile
            );

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(sourceUri, "image/*");
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);
            cropIntent.putExtra("return-data", false);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);

            cropImageLauncher.launch(cropIntent);
            Log.d("MyFragment", "裁剪器已启动");
        } catch (Exception e) {
            Log.e("MyFragment", "裁剪图片时出错: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "图片裁剪失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过用户名从API获取用户组织信息
     * @param username 用户名
     */
    private void fetchUserOrganizationByUsername(String username) {
        if (username == null || username.isEmpty()) {
            Log.e("MyFragment", "用户名为空，无法获取用户组织信息");
            return;
        }
        
        // 显示加载提示
        // 这里可以添加进度指示器，但为了保持最小修改，我们不添加UI变化

        Log.d("MyFragment", "开始获取用户 " + username + " 的组织信息");
        
        // 调用API获取用户信息
        apiService.getUserInfoByUsername(username).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    Map<String, Object> userData = response.body().getData();
                    
                    // 提取组织信息
                    String organization = null;
                    if (userData.containsKey("organization")) {
                        organization = (String) userData.get("organization");
                    }
                    
                    Log.d("MyFragment", "成功获取用户组织信息: " + organization);
                    
                    // 更新UI显示
                    if (organization != null && !organization.isEmpty()) {
                        // 创建一个 final 的副本，以便在 lambda 表达式中使用
                        final String finalOrganization = organization;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvCompany.setText(finalOrganization);
                            });
                        }
                    }
                } else {
                    Log.e("MyFragment", "获取用户组织信息失败: " + (response.errorBody() != null ? response.errorBody().toString() : "未知错误"));
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Log.e("MyFragment", "获取用户组织信息网络请求失败: " + t.getMessage());
            }
        });
    }
}
