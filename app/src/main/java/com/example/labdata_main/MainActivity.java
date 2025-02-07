package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.Device;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化工具类
        database = AppDatabase.getInstance(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        executorService = Executors.newSingleThreadExecutor();

        // 检查是否需要设备初始化
        checkEquipmentInitialization();
    }

    private void checkEquipmentInitialization() {
        String companyId = sharedPrefsManager.getUserCompany();
        if (companyId != null) {
            executorService.execute(() -> {
                List<Device> devices = database.deviceDao().getDevicesByCompanyId(companyId);
                runOnUiThread(() -> {
                    if (devices.isEmpty()) {
                        // 如果没有设备记录，先跳转到设备初始化引导界面
                        Log.d("MainActivity", "No equipment found for company: " + companyId + ", starting guide");
                        Intent intent = new Intent(this, EquipmentGuideActivity.class);
                        intent.putExtra("company_id", companyId);
                        startActivity(intent);
                        finish();
                    } else {
                        // 如果有设备记录，继续正常的 UI 初始化
                        initializeUI();
                    }
                });
            });
        } else {
            initializeUI();
        }
    }

    private void initializeUI() {
        setContentView(R.layout.activity_main);

        // 初始化 TabLayout 和 ViewPager2
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // 创建适配器并设置给 ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 使用 TabLayoutMediator 来连接 TabLayout 和 ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // 为每个 Tab 设置名称
            switch (position) {
                case 0:
                    tab.setText("概览");
                    break;
                case 1:
                    tab.setText("实验");
                    break;
                case 2:
                    tab.setText("我的");
                    break;
            }
        }).attach();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            moveTaskToBack(true);
            return;
        } else {
            backToast = Toast.makeText(this, "再按一次返回键最小化应用", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}