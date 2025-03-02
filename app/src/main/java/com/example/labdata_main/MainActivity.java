package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.adapter.ProjectCardAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ProjectStatus;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.model.Device;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.labdata_main.utils.ExperimentTypeInitializer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.database.Cursor;

public class MainActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private ExecutorService executorService;

    private static final int SPECIMEN_CODE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化工具类
        database = AppDatabase.getInstance(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        executorService = Executors.newSingleThreadExecutor();

        // 初始化实验类型
        ExperimentTypeInitializer.initializeExperimentTypes(this);

        // 添加数据库调试代码
        executorService.execute(() -> {
            try {
                // 获取数据库实例
                AppDatabase db = AppDatabase.getInstance(this);
                // 获取可写数据库
                SupportSQLiteDatabase sqliteDb = ((androidx.room.RoomDatabase) db).getOpenHelper().getWritableDatabase();
                
                // 查询experiment_tasks表的结构
                Cursor cursor = sqliteDb.query("SELECT * FROM sqlite_master WHERE type='table' AND name='experiment_tasks'");
                if (cursor.moveToFirst()) {
                    String sql = cursor.getString(cursor.getColumnIndex("sql"));
                    Log.d("DatabaseDebug", "experiment_tasks table structure: " + sql);
                }
                cursor.close();
                
                // 查询所有任务的状态
                cursor = sqliteDb.query("SELECT id, task_status FROM experiment_tasks");
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex("id"));
                    String status = cursor.getString(cursor.getColumnIndex("task_status"));
                    Log.d("DatabaseDebug", "Task " + id + " status: " + status);
                }
                cursor.close();
            } catch (Exception e) {
                Log.e("DatabaseDebug", "Error querying database", e);
            }
        });

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

        // 制件按钮点击事件
        Button btnStep2Action = findViewById(R.id.btnStep2Action);
        if (btnStep2Action != null) {
            btnStep2Action.setOnClickListener(v -> startGenerateSpecimenCode());
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPECIMEN_CODE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 更新制件和实验环节的状态
            String specimenStatus = data.getStringExtra("specimenStatus");
            String experimentStatus = data.getStringExtra("experimentStatus");
            String preparationStatus = data.getStringExtra("preparationStatus");
            
            // 更新状态文本
            TextView tvStep1Status = findViewById(R.id.tvStep1Status);
            TextView tvStep2Status = findViewById(R.id.tvStep2Status);
            TextView tvStep3Status = findViewById(R.id.tvStep3Status);
            TextView step1Circle = findViewById(R.id.step1Circle);
            TextView step2Circle = findViewById(R.id.step2Circle);
            TextView step3Circle = findViewById(R.id.step3Circle);
            
            // 更新第一步状态
            if (tvStep1Status != null && step1Circle != null && preparationStatus != null) {
                tvStep1Status.setText(preparationStatus);
                if (ProjectStatus.STATUS_COMPLETED.equals(preparationStatus)) {
                    tvStep1Status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    step1Circle.setBackgroundResource(R.drawable.circle_background_completed);
                }
            }
            
            // 更新第二步状态
            if (tvStep2Status != null && step2Circle != null) {
                tvStep2Status.setText(specimenStatus);
                if (ProjectStatus.STATUS_COMPLETED.equals(specimenStatus)) {
                    tvStep2Status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    step2Circle.setBackgroundResource(R.drawable.circle_background_completed);
                }
            }
            
            // 更新第三步状态
            if (tvStep3Status != null && step3Circle != null) {
                tvStep3Status.setText(experimentStatus);
                if (ProjectStatus.STATUS_IN_PROGRESS.equals(experimentStatus)) {
                    tvStep3Status.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    step3Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                }
            }

            // 更新按钮状态
            Button btnStep2Action = findViewById(R.id.btnStep2Action);
            Button btnStep3Action = findViewById(R.id.btnStep3Action);
            
            if (btnStep2Action != null) {
                btnStep2Action.setEnabled(false);
                btnStep2Action.setText("已完成");
            }
            
            if (btnStep3Action != null) {
                btnStep3Action.setEnabled(true);
            }

            // 显示成功提示
            Toast.makeText(this, "制件环节已完成，实验环节开始", Toast.LENGTH_SHORT).show();

            // 刷新任务列表
            executorService.execute(() -> {
                List<ExperimentTask> tasks = database.experimentTaskDao().getTasksByType();
                runOnUiThread(() -> {
                    RecyclerView recyclerView = findViewById(R.id.rvProjects);
                    if (recyclerView != null && recyclerView.getAdapter() instanceof ProjectCardAdapter) {
                        ProjectCardAdapter adapter = (ProjectCardAdapter) recyclerView.getAdapter();
                        adapter.setTasks(tasks);
                        adapter.notifyDataSetChanged();
                    }
                });
            });
        } else if (requestCode == ProjectCardAdapter.RECORD_EXPERIMENT_DATA_REQUEST && resultCode == RESULT_OK) {
            // 刷新任务列表
            loadTasks();
        }
    }

    private void loadTasks() {
        executorService.execute(() -> {
            List<ExperimentTask> tasks = database.experimentTaskDao().getAllExperimentTasks();
            runOnUiThread(() -> {
                RecyclerView recyclerView = findViewById(R.id.task_recycler_view);
                if (recyclerView != null && recyclerView.getAdapter() instanceof ProjectCardAdapter) {
                    ProjectCardAdapter adapter = (ProjectCardAdapter) recyclerView.getAdapter();
                    adapter.setTasks(tasks);
                }
            });
        });
    }

    // 启动制件码生成活动时使用
    private void startGenerateSpecimenCode() {
        Intent intent = new Intent(this, GenerateSpecimenCodeActivity.class);
        startActivityForResult(intent, SPECIMEN_CODE_REQUEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}