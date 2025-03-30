package com.example.labdata_main.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences管理类
 * 用于管理用户登录状态和基本信息的持久化存储
 */
public class SharedPrefsManager {
    // SharedPreferences文件名
    private static final String PREF_NAME = "UserPrefs";
    
    // 存储的键名
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_COMPANY = "userCompany";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_AVATAR = "userAvatar";
    private static final String KEY_USER_TYPE = "userType";
    
    // 新增权限相关的键
    private static final String KEY_ALLOW_ADD_MIXTURE = "allowAddMixture";
    private static final String KEY_ALLOW_ADD_ASPHALT = "allowAddAsphalt";
    private static final String KEY_ALLOW_ADD_MIXRATIO = "allowAddMixratio";
    
    // 新增JWT相关的键
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_TOKEN_TYPE = "tokenType";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    /**
     * 构造函数
     * @param context 应用上下文
     */
    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 保存用户登录状态和基本信息
     * @param id 用户ID
     * @param email 用户邮箱
     * @param username 用户名
     * @param company 用户单位
     * @param phone 用户电话
     * @param userType 用户类型
     * @param name 用户真实姓名
     */
    public void saveUserLoginSession(long id, String email, String username, String company, String phone, int userType, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, id);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null && !name.isEmpty() ? name : username); // 优先使用真实姓名，如果没有则使用用户名
        editor.putString(KEY_USER_COMPANY, company);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.apply();
    }
    
    /**
     * 保存用户登录状态和基本信息（字符串类型的用户类型）
     * @param id 用户ID
     * @param email 用户邮箱
     * @param username 用户名
     * @param company 用户单位
     * @param phone 用户电话
     * @param userType 用户类型（字符串）
     * @param name 用户真实姓名
     */
    public void saveUserLoginSession(long id, String email, String username, String company, String phone, String userType, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, id);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null && !name.isEmpty() ? name : username); // 优先使用真实姓名，如果没有则使用用户名
        editor.putString(KEY_USER_COMPANY, company);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putInt(KEY_USER_TYPE, "admin".equals(userType) ? 1 : 0); // admin类型为1，其他类型为0
        editor.apply();
    }
    
    /**
     * 保存JWT令牌信息
     * @param token JWT令牌
     * @param tokenType 令牌类型（例如"Bearer"）
     */
    public void saveAuthToken(String token, String tokenType) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putString(KEY_TOKEN_TYPE, tokenType);
        editor.apply();
    }
    
    /**
     * 获取Authorization头信息
     * @return Authorization头信息，格式为"Bearer [token]"
     */
    public String getAuthHeader() {
        String tokenType = sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer");
        String token = sharedPreferences.getString(KEY_AUTH_TOKEN, "");
        if (token.isEmpty()) {
            return null;
        }
        return tokenType + " " + token;
    }
    
    /**
     * 获取JWT令牌
     * @return JWT令牌字符串
     */
    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, "");
    }

    /**
     * 获取令牌类型
     * @return 令牌类型（例如 "Bearer"），如果未设置则返回默认值 "Bearer"
     */
    public String getTokenType() {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    /**
     * 清除用户登录状态和信息
     */
    public void clearUserLoginSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * 检查用户是否已登录
     * @return true表示已登录，false表示未登录
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 获取用户ID
     * @return 用户ID，如果未登录返回-1
     */
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * 获取用户邮箱
     * @return 用户邮箱，如果未登录返回null
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * 获取用户姓名
     * @return 用户姓名，如果未登录返回null
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * 获取用户单位
     * @return 用户单位，如果未登录返回null
     */
    public String getUserCompany() {
        return sharedPreferences.getString(KEY_USER_COMPANY, null);
    }

    /**
     * 获取用户电话
     * @return 用户电话，如果未登录返回null
     */
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, null);
    }

    /**
     * 保存用户姓名
     * @param name 用户姓名
     */
    public void saveUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * 保存用户单位
     * @param company 用户单位
     */
    public void saveUserCompany(String company) {
        editor.putString(KEY_USER_COMPANY, company);
        editor.apply();
    }

    /**
     * 保存用户电话
     * @param phone 用户电话
     */
    public void saveUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    /**
     * 保存用户头像URI
     * @param uri 头像图片的URI
     */
    public void saveAvatarUri(String uri) {
        editor.putString(KEY_USER_AVATAR, uri);
        editor.apply();
    }

    /**
     * 获取用户头像URI
     * @return 头像图片的URI，如果未设置则返回null
     */
    public String getAvatarUri() {
        return sharedPreferences.getString(KEY_USER_AVATAR, null);
    }

    /**
     * 获取用户类型
     * @return 用户类型，如果未登录返回-1
     */
    public int getUserType() {
        int userType = sharedPreferences.getInt(KEY_USER_TYPE, -1);
        android.util.Log.d("SharedPrefsManager", "Getting user type: " + userType);
        return userType;
    }
    
    /**
     * 获取用户类型（字符串）
     * @return 用户类型字符串，"admin"或"user"
     */
    public String getUserTypeString() {
        int userType = getUserType();
        return userType == 1 ? "admin" : "user";
    }

    /**
     * 清除认证令牌
     */
    public void clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_TOKEN_TYPE);
        editor.apply();
    }

    /**
     * 保存用户权限信息
     * @param allowAddMixture 是否允许添加混合料实验
     * @param allowAddAsphalt 是否允许添加沥青实验
     * @param allowAddMixratio 是否允许添加配合比
     */
    public void saveUserPermissions(boolean allowAddMixture, boolean allowAddAsphalt, boolean allowAddMixratio) {
        editor.putBoolean(KEY_ALLOW_ADD_MIXTURE, allowAddMixture);
        editor.putBoolean(KEY_ALLOW_ADD_ASPHALT, allowAddAsphalt);
        editor.putBoolean(KEY_ALLOW_ADD_MIXRATIO, allowAddMixratio);
        editor.apply();
    }

    /**
     * 检查用户是否有添加混合料实验的权限
     * @return 如果有权限返回true，否则返回false
     */
    public boolean canAddMixture() {
        // 如果是管理员，自动拥有所有权限
        if (getUserType() == 1) {
            return true;
        }
        return sharedPreferences.getBoolean(KEY_ALLOW_ADD_MIXTURE, false);
    }

    /**
     * 检查用户是否有添加沥青实验的权限
     * @return 如果有权限返回true，否则返回false
     */
    public boolean canAddAsphalt() {
        // 如果是管理员，自动拥有所有权限
        if (getUserType() == 1) {
            return true;
        }
        return sharedPreferences.getBoolean(KEY_ALLOW_ADD_ASPHALT, false);
    }

    /**
     * 检查用户是否有添加配合比的权限
     * @return 如果有权限返回true，否则返回false
     */
    public boolean canAddMixratio() {
        // 如果是管理员，自动拥有所有权限
        if (getUserType() == 1) {
            return true;
        }
        return sharedPreferences.getBoolean(KEY_ALLOW_ADD_MIXRATIO, false);
    }
}
