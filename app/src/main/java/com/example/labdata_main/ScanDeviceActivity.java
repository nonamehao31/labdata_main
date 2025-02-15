package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanDeviceActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE_CODE = "device_code";
    public static final String EXTRA_POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化二维码扫描器
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请将二维码放入框内扫描");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            // 扫描成功，返回设备码
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_CODE, result.getContents());
            intent.putExtra(EXTRA_POSITION, getIntent().getIntExtra(EXTRA_POSITION, -1));
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
