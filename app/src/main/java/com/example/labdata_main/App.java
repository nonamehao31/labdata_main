package com.example.labdata_main;

import android.app.Application;
import android.content.Context;

/**
 * 应用程序类，用于初始化全局状态
 */
public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /**
     * 获取应用程序上下文
     */
    public static Context getAppContext() {
        return context;
    }
}
