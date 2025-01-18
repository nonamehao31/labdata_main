package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDeviceActivity extends AppCompatActivity {
    private EditText etDeviceName; 
    private Button btnNext;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etDeviceName = findViewById(R.id.etDeviceName);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnNext.setOnClickListener(v -> {
            String deviceName = etDeviceName.getText().toString().trim();
            if (deviceName.isEmpty()) {
                Toast.makeText(this, "请输入设备名称", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存设备名称
            Intent intent = new Intent(this, SelectRatioActivity.class);
            intent.putExtra("device_name", deviceName);
            startActivity(intent);
        });
    }
}
