package com.example.labdata_main;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.labdata_main.utils.TokenExpirationReceiver;

/**
 * 基础活动类
 * 用于处理所有活动共享的功能，如令牌过期处理
 * 所有活动应继承此类以获取统一的功能
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private TokenExpirationReceiver tokenExpirationReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建令牌过期广播接收器
        tokenExpirationReceiver = new TokenExpirationReceiver(this);
        
        // 注册令牌过期广播接收器
        IntentFilter intentFilter = new IntentFilter("com.example.labdata_main.TOKEN_EXPIRED");
        registerReceiver(tokenExpirationReceiver, intentFilter);
        
        Log.d(TAG, "令牌过期广播接收器已注册: " + getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        // 解注册令牌过期广播接收器
        if (tokenExpirationReceiver != null) {
            try {
                unregisterReceiver(tokenExpirationReceiver);
                Log.d(TAG, "令牌过期广播接收器已解注册: " + getClass().getSimpleName());
            } catch (Exception e) {
                Log.e(TAG, "解注册令牌过期广播接收器失败", e);
            }
        }
        
        super.onDestroy();
    }
}
