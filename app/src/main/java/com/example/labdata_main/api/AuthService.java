package com.example.labdata_main.api;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.utils.SharedPrefsManager;

/**
 * 认证服务类，用于管理API认证
 */
public class AuthService {
    private static final String TAG = "AuthService";
    
    // 应用上下文，用于访问 SharedPreferences
    private static Context applicationContext;
    
    /**
     * 初始化认证服务
     * @param context 应用上下文
     */
    public static void init(Context context) {
        if (context != null) {
            applicationContext = context.getApplicationContext();
            Log.d(TAG, "AuthService初始化成功");
        } else {
            Log.e(TAG, "AuthService初始化失败，传入的上下文为null");
        }
    }
    
    /**
     * 获取应用上下文
     * @return 应用上下文
     */
    public static Context getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 获取JWT令牌
     * @return JWT令牌字符串，如果没有登录则返回null
     */
    public static String getJwtToken() {
        if (applicationContext == null) {
            Log.e(TAG, "AuthService未初始化，请先调用init方法");
            return null;
        }
        
        Log.d(TAG, "开始从SharedPrefsManager获取JWT令牌");
        
        // 使用SharedPrefsManager获取已保存的令牌
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(applicationContext);
        
        // 检查用户是否已登录
        boolean isLoggedIn = sharedPrefsManager.isLoggedIn();
        Log.d(TAG, "用户登录状态: " + (isLoggedIn ? "已登录" : "未登录"));
        
        if (!isLoggedIn) {
            Log.d(TAG, "用户未登录，无法获取JWT令牌");
            return null;
        }
        
        // 获取已保存的JWT令牌
        String token = sharedPrefsManager.getAuthToken();
        
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "成功获取已登录用户的JWT令牌，令牌长度: " + token.length());
            // 打印令牌的前10个字符和后10个字符，避免完整泄露
            if (token.length() > 20) {
                Log.d(TAG, "令牌前缀: " + token.substring(0, 10) + "..." + 
                      "令牌后缀: ..." + token.substring(token.length() - 10));
            }
            return token;
        } else {
            Log.e(TAG, "用户已登录但无法获取有效的JWT令牌");
            
            // 尝试使用getAuthHeader方法获取完整的认证头
            String authHeader = sharedPrefsManager.getAuthHeader();
            Log.d(TAG, "通过getAuthHeader方法获取的认证头: " + 
                  (authHeader != null ? "非空，长度: " + authHeader.length() : "为空"));
                  
            // 获取用户信息，帮助调试
            long userId = sharedPrefsManager.getUserId();
            String userName = sharedPrefsManager.getUserName();
            String userType = sharedPrefsManager.getUserTypeString();
            Log.d(TAG, "已登录用户信息 - ID: " + userId + ", 名称: " + userName + ", 类型: " + userType);
            
            return null;
        }
    }
}
