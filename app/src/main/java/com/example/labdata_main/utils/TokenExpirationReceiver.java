package com.example.labdata_main.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.labdata_main.LoginActivity;
import com.example.labdata_main.utils.SharedPrefsManager;

/**
 * 令牌过期广播接收器
 * 用于处理JWT令牌过期的情况，提示用户重新登录
 */
public class TokenExpirationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "TokenExpirationReceiver";
    private final Activity activity;
    // 添加静态变量，防止多个实例同时处理
    private static boolean isHandlingExpiration = false;
    
    public TokenExpirationReceiver(Activity activity) {
        this.activity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.labdata_main.TOKEN_EXPIRED".equals(intent.getAction())) {
            Log.d(TAG, "收到令牌过期广播");
            
            // 检查是否已经有实例在处理过期流程
            if (isHandlingExpiration) {
                Log.d(TAG, "已有实例正在处理令牌过期，忽略此次广播");
                return;
            }
            
            // 设置处理标志
            isHandlingExpiration = true;
            
            // 在UI线程显示提示
            activity.runOnUiThread(() -> {
                try {
                    // 检查当前Activity是否是LoginActivity
                    if (activity instanceof com.example.labdata_main.LoginActivity) {
                        Log.d(TAG, "当前已在登录界面，不需要跳转");
                        isHandlingExpiration = false;
                        return;
                    }
                    
                    Toast.makeText(context, "您的登录信息已过期，请重新登录", Toast.LENGTH_LONG).show();
                    
                    // 清除用户登录状态
                    SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
                    sharedPrefsManager.clearUserLoginSession();
                    
                    // 跳转到登录页面，使用特殊标志防止回退
                    Intent loginIntent = new Intent(context, com.example.labdata_main.LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                         Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                                         Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 添加标志表明这是因令牌过期而跳转
                    loginIntent.putExtra("from_token_expiration", true);
                    context.startActivity(loginIntent);
                    
                    // 关闭当前活动
                    if (activity != null && !activity.isFinishing()) {
                        activity.finish();
                    }
                } finally {
                    // 5秒后重置处理标志，以防万一标志未被正确清除
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isHandlingExpiration = false;
                        Log.d(TAG, "重置令牌过期处理标志");
                    }, 5000);
                }
            });
        }
    }

    /**
     * 重置处理标志，允许再次处理令牌过期
     * 可在需要时手动调用
     */
    public static void resetHandlingFlag() {
        isHandlingExpiration = false;
        Log.d(TAG, "手动重置令牌过期处理标志");
    }
}
