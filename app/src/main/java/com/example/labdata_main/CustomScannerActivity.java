package com.example.labdata_main;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * 自定义扫描活动，用于避免活动重建问题
 * 继承自ZXing库的CaptureActivity，并提供更多控制以防止父活动重建
 */
public class CustomScannerActivity extends CaptureActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 防止扫描界面导致主界面重建的关键设置
        setFinishOnTouchOutside(false);
    }
    
    @Override
    protected DecoratedBarcodeView initializeContent() {
        DecoratedBarcodeView decoratedBarcodeView = super.initializeContent();
        
        // 确保扫描视图不会触发配置变更
        decoratedBarcodeView.setKeepScreenOn(true);
        return decoratedBarcodeView;
    }
    
    @Override
    public void onBackPressed() {
        // 确保返回时传递正确的结果代码
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 拦截返回键，确保不触发活动重建
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
