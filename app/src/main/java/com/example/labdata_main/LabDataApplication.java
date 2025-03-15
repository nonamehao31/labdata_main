package com.example.labdata_main;

import android.app.Application;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.utils.MaterialPropertyInitializer;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.AuthService;
import com.example.labdata_main.api.CompactionMethodApiService;
import android.util.Log;

public class LabDataApplication extends Application {
    private ExecutorService executorService;
    private static Context appContext;
    private static CompactionMethodApiService compactionMethodApiService;

    @Override
    public void onCreate() {
        super.onCreate();
        // 保存应用上下文的静态引用
        appContext = getApplicationContext();
        
        // 初始化数据库实例
        AppDatabase.getInstance(this);
        
        // 初始化 API 客户端
        ApiClient.init(this);
        
        // 初始化认证服务
        AuthService.init(this);
        
        // 初始化 CompactionMethodApiService
        try {
            compactionMethodApiService = new CompactionMethodApiService(this);
            Log.d("LabDataApplication", "成功初始化CompactionMethodApiService");
        } catch (Exception e) {
            Log.e("LabDataApplication", "初始化CompactionMethodApiService失败", e);
        }
        
        // 创建单线程执行器
        executorService = Executors.newSingleThreadExecutor();
        
        // 在后台线程初始化材料属性
        executorService.execute(() -> 
            MaterialPropertyInitializer.initializeMaterialProperties(getApplicationContext())
        );
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 关闭执行器服务
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * 获取应用上下文
     * @return 应用上下文
     */
    public static Context getAppContext() {
        return appContext;
    }
    
    /**
     * 获取制件方法API服务实例
     * @return CompactionMethodApiService实例
     */
    public static CompactionMethodApiService getCompactionMethodApiService() {
        return compactionMethodApiService;
    }
}
