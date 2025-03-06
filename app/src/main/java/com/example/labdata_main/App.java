package com.example.labdata_main;

import android.app.Application;
import android.content.Context;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.database.AppDatabase;

/**
 * 应用程序类，用于初始化全局状态
 */
public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        // 初始化数据库
        AppDatabase.getInstance(this);
        
        // 初始化 API 客户端
        ApiClient.init(this);
    }

    /**
     * 获取应用程序上下文
     */
    public static Context getAppContext() {
        return context;
    }
}
