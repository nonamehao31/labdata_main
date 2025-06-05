package com.example.labdata_main;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.Calendar;
import java.util.Date;

/**
 * 应用程序类，用于初始化全局状态
 */
public class App extends Application {
    private static Context context;

    private static final String TAG = "App";
    
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 初始化数据库
        AppDatabase.getInstance(this);
        
        // 初始化 API 客户端
        ApiClient.init(this);
        
        // 检查令牌是否已过期
        checkTokenExpiration();
    }
    
    /**
     * 检查令牌是否已过期
     * 应用启动时检查令牌状态，如果已过期则直接清除登录状态并发送广播
     */
    private void checkTokenExpiration() {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(this);
        
        // 如果用户未登录，无需检查
        if (!sharedPrefsManager.isLoggedIn()) {
            return;
        }
        
        // 获取登录时间和当前时间
        long loginTime = sharedPrefsManager.getLoginTime();
        long currentTime = System.currentTimeMillis();
        
        // 计算时间差（24小时 = 86400000毫秒）
        long timeDifference = currentTime - loginTime;
        long dayInMillis = 24 * 6 * 6 * 1; // 24小时对应的毫秒数
        
        Log.d(TAG, "登录时间检查: 登录时间="+ new Date(loginTime) + ", 当前时间="+ new Date(currentTime) + ", 差值=" + (timeDifference / (1000 * 60 * 60)) + "小时");
        
        // 如果登录时间超过24小时，则认为令牌已过期
        if (timeDifference > dayInMillis) {
            Log.d(TAG, "令牌已过期，登录时间超过24小时");
            
            // 清除登录状态
            sharedPrefsManager.clearUserLoginSession();
            
            // 发送令牌过期广播
            Intent tokenExpiredIntent = new Intent("com.example.labdata_main.TOKEN_EXPIRED");
            sendBroadcast(tokenExpiredIntent);
            
            // 启动登录界面
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    /**
     * 获取应用程序上下文
     */
    public static Context getAppContext() {
        return context;
    }
}
