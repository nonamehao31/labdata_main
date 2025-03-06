package com.example.labdata_main;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.labdata_main.adapter.QRCodeAdapter;
import com.example.labdata_main.model.Equipment;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class QRCodeDisplayActivity extends AppCompatActivity {
    private static final String TAG = "QRCodeDisplay";
    private ViewPager2 viewPager;
    private ArrayList<Equipment> equipmentList;
    private QRCodeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Starting QRCodeDisplayActivity");
            setContentView(R.layout.activity_qr_code_display);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            // 获取传递过来的设备列表
            Intent intent = getIntent();
            if (intent.hasExtra("equipment_json")) {
                try {
                    // 从JSON字符串反序列化设备列表
                    String equipmentJson = intent.getStringExtra("equipment_json");
                    equipmentList = Equipment.fromJsonString(equipmentJson);
                    Log.d(TAG, "Deserialized equipment list from JSON: " + (equipmentList != null ? equipmentList.size() : "null"));
                } catch (Exception e) {
                    Log.e(TAG, "Error deserializing equipment list: " + e.getMessage(), e);
                    equipmentList = new ArrayList<>();
                }
            } else {
                equipmentList = new ArrayList<>();
                Log.w(TAG, "Equipment JSON not found, creating empty list");
            }

            // 初始化 ViewPager2
            viewPager = findViewById(R.id.viewPager);
            if (viewPager == null) {
                throw new IllegalStateException("ViewPager2 not found in layout");
            }
            adapter = new QRCodeAdapter(equipmentList);
            viewPager.setAdapter(adapter);
            Log.d(TAG, "ViewPager2 initialized");

            // 设置 TabLayout
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            if (tabLayout == null) {
                throw new IllegalStateException("TabLayout not found in layout");
            }
            new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("设备 " + (position + 1))
            ).attach();
            Log.d(TAG, "TabLayout initialized");

            // 保存按钮点击事件
            MaterialButton btnSave = findViewById(R.id.btnSave);
            if (btnSave == null) {
                throw new IllegalStateException("Save button not found in layout");
            }
            btnSave.setOnClickListener(v -> saveCurrentQRCode());

            // 完成按钮点击事件
            MaterialButton btnFinish = findViewById(R.id.btnFinish);
            if (btnFinish == null) {
                throw new IllegalStateException("Finish button not found in layout");
            }
            btnFinish.setOnClickListener(v -> {
                // 创建返回主页的意图
                Intent mainIntent = new Intent(this, MainActivity.class);
                // 清除任务栈中所有活动
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(mainIntent);
                // 确保当前活动被销毁
                finish();
            });
            Log.d(TAG, "Buttons initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveCurrentQRCode() {
        try {
            // 获取 ViewPager2 内部的 RecyclerView
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            if (recyclerView == null) {
                Toast.makeText(this, "无法获取视图列表", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取当前显示的视图
            View currentView = recyclerView.findViewHolderForAdapterPosition(viewPager.getCurrentItem()).itemView;
            if (currentView == null) {
                Toast.makeText(this, "无法获取当前视图", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取二维码图片视图
            ImageView qrCodeImageView = currentView.findViewById(R.id.ivQRCode);
            if (qrCodeImageView == null) {
                Toast.makeText(this, "无法获取二维码图片", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取二维码图片
            qrCodeImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(qrCodeImageView.getDrawingCache());
            qrCodeImageView.setDrawingCacheEnabled(false);

            // 获取当前设备信息用于文件名
            Equipment currentDevice = equipmentList.get(viewPager.getCurrentItem());
            String deviceType = getDeviceTypeName(currentDevice.getType());

            // 保存图片
            String fileName = deviceType + "_二维码_" + System.currentTimeMillis() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri == null) {
                Toast.makeText(this, "保存失败：无法创建文件", Toast.LENGTH_SHORT).show();
                return;
            }

            try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                if (out == null) {
                    Toast.makeText(this, "保存失败：无法写入文件", Toast.LENGTH_SHORT).show();
                    return;
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, deviceType + "二维码已保存到相册", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR code: " + e.getMessage(), e);
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getDeviceTypeName(String type) {
        switch (type) {
            case "MIXING":
                return "拌合设备";
            case "FORMING":
                return "制件设备";
            case "TESTING":
                return "实验设备";
            default:
                return "未知设备";
        }
    }
}
