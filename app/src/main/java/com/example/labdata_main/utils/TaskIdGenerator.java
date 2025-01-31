package com.example.labdata_main.utils;

import android.content.Context;
import com.example.labdata_main.database.AppDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskIdGenerator {
    private static final String TASK_PREFIX = "TASK";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static String generateTaskId(Context context) {
        // 获取当前时间戳
        String timestamp = DATE_FORMAT.format(new Date());
        String prefix = TASK_PREFIX + "_" + timestamp.substring(0, 8); // TASK_yyyyMMdd

        try {
            // 在后台线程中获取任务数量
            Future<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return AppDatabase.getInstance(context)
                            .experimentTaskDao()
                            .getTaskCountByPrefix(prefix);
                }
            });

            // 等待结果（这是安全的，因为我们已经在后台线程中）
            int count = future.get();
            
            // 生成任务ID：TASK_yyyyMMdd_HHmmss_序号
            return String.format("%s_%s_%03d", 
                    TASK_PREFIX,
                    timestamp,
                    count + 1);
        } catch (Exception e) {
            // 如果出现错误，使用时间戳作为唯一标识
            return String.format("%s_%s_%03d", 
                    TASK_PREFIX,
                    timestamp,
                    1);
        }
    }
}
