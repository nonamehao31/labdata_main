package com.example.labdata_main.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * SharedPreferences 工具类
 */
public class SPUtils {
    private static final String PREFERENCES_NAME = "labdata_prefs";
    
    private SharedPreferences mPreferences;
    private static SPUtils mInstance;
    
    /**
     * 获取SPUtils实例
     * 
     * @param context 上下文
     * @return SPUtils实例
     */
    public static synchronized SPUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SPUtils(context.getApplicationContext());
        }
        return mInstance;
    }
    
    private SPUtils(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 存储String类型数据
     * 
     * @param key 键
     * @param value 值
     */
    public void putString(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().putString(key, value).apply();
    }
    
    /**
     * 获取String类型数据
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值，如果不存在则返回默认值
     */
    public String getString(String key, String defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        return mPreferences.getString(key, defaultValue);
    }
    
    /**
     * 存储int类型数据
     * 
     * @param key 键
     * @param value 值
     */
    public void putInt(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().putInt(key, value).apply();
    }
    
    /**
     * 获取int类型数据
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值，如果不存在则返回默认值
     */
    public int getInt(String key, int defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        return mPreferences.getInt(key, defaultValue);
    }
    
    /**
     * 存储boolean类型数据
     * 
     * @param key 键
     * @param value 值
     */
    public void putBoolean(String key, boolean value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().putBoolean(key, value).apply();
    }
    
    /**
     * 获取boolean类型数据
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值，如果不存在则返回默认值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        return mPreferences.getBoolean(key, defaultValue);
    }
    
    /**
     * 存储long类型数据
     * 
     * @param key 键
     * @param value 值
     */
    public void putLong(String key, long value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().putLong(key, value).apply();
    }
    
    /**
     * 获取long类型数据
     * 
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值，如果不存在则返回默认值
     */
    public long getLong(String key, long defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        return mPreferences.getLong(key, defaultValue);
    }
    
    /**
     * 移除指定键的值
     * 
     * @param key 键
     */
    public void remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().remove(key).apply();
    }
    
    /**
     * 清除所有数据
     */
    public void clear() {
        mPreferences.edit().clear().apply();
    }
    
    /**
     * 检查是否包含指定键的值
     * 
     * @param key 键
     * @return 如果包含返回true，否则返回false
     */
    public boolean contains(String key) {
        return mPreferences.contains(key);
    }
}
