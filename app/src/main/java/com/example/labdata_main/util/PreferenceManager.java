package com.example.labdata_main.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 偏好设置管理类，用于存储和获取应用的偏好设置
 */
public class PreferenceManager {
    private static final String PREF_NAME = "labdata_preferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COMPANY_ID = "company_id";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPreferences;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    /**
     * 保存用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    /**
     * 保存公司ID
     *
     * @param companyId 公司ID
     */
    public void setCompanyId(String companyId) {
        sharedPreferences.edit().putString(KEY_COMPANY_ID, companyId).apply();
    }

    /**
     * 获取公司ID
     *
     * @return 公司ID
     */
    public String getCompanyId() {
        return sharedPreferences.getString(KEY_COMPANY_ID, "");
    }

    /**
     * 保存认证令牌
     *
     * @param token 认证令牌
     */
    public void setToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * 获取认证令牌
     *
     * @return 认证令牌
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, "");
    }

    /**
     * 设置登录状态
     *
     * @param isLoggedIn 是否已登录
     */
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    /**
     * 获取登录状态
     *
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 清除所有偏好设置（用于登出）
     */
    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}
