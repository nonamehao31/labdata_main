package com.example.labdata_main;

import android.app.Application;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.utils.MaterialPropertyInitializer;

public class LabDataApplication extends Application {
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.getInstance(this);
        
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
}
