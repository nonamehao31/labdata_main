package com.example.labdata_main.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.labdata_main.LoginActivity;

/**
 * 令牌过期广播接收器
 * 用于处理JWT令牌过期的情况，提示用户重新登录
 */
public class TokenExpirationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "TokenExpirationReceiver";
    private final Activity activity;
    
    public TokenExpirationReceiver(Activity activity) {
        this.activity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.labdata_main.TOKEN_EXPIRED".equals(intent.getAction())) {
            Log.d(TAG, "收到令牌过期广播");
            
            // 在UI线程显示提示
            activity.runOnUiThread(() -> {
                Toast.makeText(context, "您的登录信息已过期，请重新登录", Toast.LENGTH_LONG).show();
                
                // 清除用户登录状态
                SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
                sharedPrefsManager.clearUserLoginSession();
                
                // 跳转到登录页面
                Intent loginIntent = new Intent(context, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
                
                // 关闭当前活动
                if (activity != null) {
                    activity.finish();
                }
            });
        }
    }
}
