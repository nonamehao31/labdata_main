package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.db.DatabaseHelper;
import com.example.labdata_main.model.Equipment;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;
    private DatabaseHelper databaseHelper;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化工具类
        databaseHelper = new DatabaseHelper(this);
        sharedPrefsManager = new SharedPrefsManager(this);

        // 检查是否需要设备初始化
        if (checkEquipmentInitialization()) {
            // 如果需要初始化，方法内部会处理跳转，这里直接返回
            return;
        }

        // 如果不需要初始化，继续正常的 UI 初始化
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

    private boolean checkEquipmentInitialization() {
        String companyId = sharedPrefsManager.getUserCompany();
        if (companyId != null) {
            // 检查是否正在进行设备初始化
            if (getIntent().getBooleanExtra("equipment_initialized", false)) {
                Log.d("MainActivity", "Equipment initialization completed");
                return false;
            }

            List<Equipment> equipmentList = databaseHelper.getEquipmentsByCompanyId(companyId);
            if (equipmentList.isEmpty()) {
                // 如果没有设备记录，先跳转到设备初始化引导界面
                Log.d("MainActivity", "No equipment found for company: " + companyId + ", starting guide");
                Intent intent = new Intent(this, EquipmentGuideActivity.class);
                intent.putExtra("company_id", companyId);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return false;
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
}