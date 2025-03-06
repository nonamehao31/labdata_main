package com.example.labdata_main.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * 输入验证工具类
 */
public class ValidationUtils {
    
    // 密码正则表达式：必须包含数字、小写字母和大写字母，长度6-20位
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{6,20}$";
    
    // 用户名正则表达式：只允许字母、数字和下划线，长度5-20位
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{5,20}$";
    
    // 手机号正则表达式：中国大陆手机号
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    /**
     * 验证邮箱格式
     * @param email 待验证的邮箱
     * @return 验证结果
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 验证密码格式
     * 要求：
     * 1. 必须包含数字
     * 2. 必须包含小写字母
     * 3. 必须包含大写字母
     * 4. 长度在6-20位之间
     * @param password 待验证的密码
     * @return 验证结果
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }

    /**
     * 获取密码强度提示信息
     * @param password 密码
     * @return 提示信息，如果密码符合要求则返回null
     */
    public static String getPasswordStrengthMessage(String password) {
        if (TextUtils.isEmpty(password)) {
            return "密码不能为空";
        }
        
        if (password.length() < 6) {
            return "密码长度不能小于6位";
        }
        
        if (password.length() > 20) {
            return "密码长度不能大于20位";
        }
        
        if (!Pattern.compile(".*[0-9].*").matcher(password).matches()) {
            return "密码必须包含数字";
        }
        
        if (!Pattern.compile(".*[a-z].*").matcher(password).matches()) {
            return "密码必须包含小写字母";
        }
        
        if (!Pattern.compile(".*[A-Z].*").matcher(password).matches()) {
            return "密码必须包含大写字母";
        }
        
        return null;
    }
    
    /**
     * 验证用户名格式
     * 要求：
     * 1. 只允许字母、数字和下划线
     * 2. 长度在5-20位之间
     * @param username 待验证的用户名
     * @return 验证结果
     */
    public static boolean isValidUsername(String username) {
        return !TextUtils.isEmpty(username) && Pattern.compile(USERNAME_PATTERN).matcher(username).matches();
    }
    
    /**
     * 获取用户名验证提示信息
     * @param username 用户名
     * @return 提示信息，如果用户名符合要求则返回null
     */
    public static String getUsernameErrorMessage(String username) {
        if (TextUtils.isEmpty(username)) {
            return "用户名不能为空";
        }
        
        if (username.length() < 5) {
            return "用户名长度不能小于5位";
        }
        
        if (username.length() > 20) {
            return "用户名长度不能大于20位";
        }
        
        if (!Pattern.compile("^[a-zA-Z0-9_]*$").matcher(username).matches()) {
            return "用户名只能包含字母、数字和下划线";
        }
        
        return null;
    }
    
    /**
     * 验证手机号格式
     * @param phone 待验证的手机号
     * @return 验证结果
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Pattern.compile(PHONE_PATTERN).matcher(phone).matches();
    }
    
    /**
     * 获取手机号验证提示信息
     * @param phone 手机号
     * @return 提示信息，如果手机号符合要求则返回null
     */
    public static String getPhoneErrorMessage(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "手机号不能为空";
        }
        
        if (!Pattern.compile(PHONE_PATTERN).matcher(phone).matches()) {
            return "请输入有效的手机号码";
        }
        
        return null;
    }
}
