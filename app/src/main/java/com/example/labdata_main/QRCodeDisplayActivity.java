package com.example.labdata_main;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.labdata_main.adapter.QRCodeAdapter;
import com.example.labdata_main.model.Equipment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
            btnSave.setText("保存并分享设备码");
            btnSave.setOnClickListener(v -> saveAndShareCurrentQRCode());

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

    /**
     * 保存并分享当前二维码
     */
    private void saveAndShareCurrentQRCode() {
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
            
            // 保存图片并分享
            saveAndShareQRCodeImage(bitmap, deviceType + "_二维码");
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR code: " + e.getMessage(), e);
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 保存二维码图像到文件并分享
     * @param bitmap 二维码位图
     * @param fileName 文件名前缀
     */
    private void saveAndShareQRCodeImage(Bitmap bitmap, String fileName) {
        try {
            // 生成文件名，添加日期时间戳
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = fileName + "_" + timeStamp + ".png";
            
            Uri imageUri;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+使用MediaStore API
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LabData");
                
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri != null) {
                    try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    }
                }
            } else {
                // Android 9及以下使用传统文件存储
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "LabData");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                
                File imageFile = new File(storageDir, imageFileName);
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
                
                // 通知媒体扫描器更新
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                imageUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(imageUri);
                sendBroadcast(mediaScanIntent);
                
                // 对于Android 7+，需要使用FileProvider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".provider",
                            imageFile);
                }
            }
            
            // 显示成功消息
            Toast.makeText(this, "设备码已保存", Toast.LENGTH_SHORT).show();
            
            // 创建分享Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // 启动分享选择器
            startActivity(Intent.createChooser(shareIntent, "分享设备码"));
            
        } catch (IOException e) {
            Log.e(TAG, "保存设备码图像失败", e);
            Toast.makeText(this, "保存设备码失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "处理设备码图像时出错", e);
            Toast.makeText(this, "处理设备码时出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
