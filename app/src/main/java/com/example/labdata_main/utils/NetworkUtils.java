package com.example.labdata_main.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 网络工具类，提供网络连接检查功能
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    
    /**
     * 检查设备是否连接到网络
     * @param context 上下文
     * @return 如果已连接返回true，否则返回false
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
    
    /**
     * 异步测试服务器连接
     * @param urlString 要测试的URL
     * @param timeout 超时时间（秒）
     * @return Future对象，可用于获取结果
     */
    public static Future<Boolean> checkServerConnectivity(final String urlString, final int timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                HttpURLConnection connection = null;
                try {
                    Log.d(TAG, "测试服务器连接: " + urlString);
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
                    connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
                    connection.setRequestMethod("HEAD");
                    
                    int responseCode = connection.getResponseCode();
                    boolean success = responseCode >= 200 && responseCode < 400;
                    Log.d(TAG, "服务器响应码: " + responseCode + ", 连接状态: " + (success ? "成功" : "失败"));
                    return success;
                } catch (IOException e) {
                    Log.e(TAG, "服务器连接测试失败: " + e.getMessage());
                    return false;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    executor.shutdown();
                }
            }
        });
    }
    
    /**
     * 同步测试服务器连接（注意：不应在主线程中调用）
     * @param urlString 要测试的URL
     * @param timeout 超时时间（秒）
     * @return 如果可以连接返回true，否则返回false
     */
    public static boolean checkServerConnectivitySync(String urlString, int timeout) {
        HttpURLConnection connection = null;
        try {
            Log.d(TAG, "同步测试服务器连接: " + urlString);
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
            connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
            connection.setRequestMethod("HEAD");
            
            int responseCode = connection.getResponseCode();
            boolean success = responseCode >= 200 && responseCode < 400;
            Log.d(TAG, "服务器响应码: " + responseCode + ", 连接状态: " + (success ? "成功" : "失败"));
            return success;
        } catch (IOException e) {
            Log.e(TAG, "服务器连接测试失败: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
