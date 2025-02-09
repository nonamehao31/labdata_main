package com.example.labdata_main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.labdata_main.adapter.SpecimenMethodAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class GenerateSpecimenCodeActivity extends AppCompatActivity {
    private static final String TAG = "GenerateSpecimenCode";
    
    private View step1Layout;
    private View step2Layout;
    private TextView step1Indicator;
    private TextView step2Indicator;
    private TextView step1Text;
    private TextView step2Text;
    private View step1Line;
    private RecyclerView rvMoldingMethods;
    private SpecimenMethodAdapter specimenMethodAdapter;
    private String taskId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置布局
        setContentView(R.layout.activity_generate_specimen_code);
        
        Log.d(TAG, "Activity created, initializing views...");

        // 获取传递过来的任务ID
        taskId = getIntent().getStringExtra("taskId");
        Log.d(TAG, "Received taskId: " + taskId);

        // 初始化数据库
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "labdata_db").build();

        // 初始化所有视图
        initializeViews();
        
        // 加载任务数据
        loadTaskData();
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views...");
        
        // 初始化步骤相关视图
        step1Layout = findViewById(R.id.step1Layout);
        step2Layout = findViewById(R.id.step2Layout);
        step1Indicator = findViewById(R.id.step1Indicator);
        step2Indicator = findViewById(R.id.step2Indicator);
        step1Text = findViewById(R.id.step1Text);
        step2Text = findViewById(R.id.step2Text);
        step1Line = findViewById(R.id.step1Line);

        // 初始化RecyclerView
        rvMoldingMethods = findViewById(R.id.rvMoldingMethods);
        Log.d(TAG, "RecyclerView found: " + (rvMoldingMethods != null));
        
        if (rvMoldingMethods != null) {
            rvMoldingMethods.setLayoutManager(new LinearLayoutManager(this));
            
            // 先用空数据初始化适配器
            specimenMethodAdapter = new SpecimenMethodAdapter("[]", new ArrayList<>());
            rvMoldingMethods.setAdapter(specimenMethodAdapter);
            Log.d(TAG, "RecyclerView initialized with empty adapter");
        } else {
            Log.e(TAG, "Failed to find RecyclerView with ID: rvMoldingMethods");
        }

        // 设置初始状态
        updateStepStatus(1);
        Log.d(TAG, "Views initialization completed");
    }

    private void loadTaskData() {
        if (taskId != null) {
            Log.d(TAG, "Starting to load task data for taskId: " + taskId);
            // 在后台线程中访问数据库
            new Thread(() -> {
                try {
                    Log.d(TAG, "Loading task data for taskId: " + taskId);
                    // 从数据库获取任务信息
                    ExperimentTask task = db.experimentTaskDao().getTaskByTaskId(taskId);
                    if (task != null) {
                        Log.d(TAG, "Task found in database");
                        
                        // 获取混合比例
                        List<MixRatio> mixRatios = task.getSelectedMixRatios();
                        Log.d(TAG, "Mix ratios size: " + (mixRatios != null ? mixRatios.size() : 0));
                        
                        // 获取制件方法
                        String moldingMethodStr = task.getMoldingMethod();
                        Log.d(TAG, "Molding method string: " + moldingMethodStr);
                        
                        // 在主线程更新UI
                        runOnUiThread(() -> {
                            if (moldingMethodStr != null && !moldingMethodStr.isEmpty()) {
                                try {
                                    Gson gson = new Gson();
                                    List<MoldingMethod> methods = new ArrayList<>();

                                    if (moldingMethodStr.trim().startsWith("[")) {
                                        // 如果是JSON数组格式，直接解析
                                        methods = gson.fromJson(moldingMethodStr, new TypeToken<List<MoldingMethod>>(){}.getType());
                                        Log.d(TAG, "Parsed JSON array format, size: " + methods.size());
                                    } else if (moldingMethodStr.trim().startsWith("{")) {
                                        // 如果是单个JSON对象，转换为数组
                                        MoldingMethod singleMethod = gson.fromJson(moldingMethodStr, MoldingMethod.class);
                                        if (singleMethod != null) {
                                            methods.add(singleMethod);
                                            Log.d(TAG, "Added single JSON object to methods");
                                        }
                                    } else {
                                        // 如果是老格式（temp=160|speed=60|time=90|method=振动压实）
                                        MoldingMethod method = new MoldingMethod();
                                        String[] parts = moldingMethodStr.split("\\|");
                                        for (String part : parts) {
                                            String[] keyValue = part.split("=");
                                            if (keyValue.length == 2) {
                                                String key = keyValue[0].trim();
                                                String value = keyValue[1].trim();
                                                try {
                                                    switch (key) {
                                                        case "temp":
                                                            method.setMixingTemperature(Float.parseFloat(value));
                                                            break;
                                                        case "speed":
                                                            method.setMixingSpeed(Float.parseFloat(value));
                                                            break;
                                                        case "time":
                                                            method.setMixingTime(Float.parseFloat(value));
                                                            break;
                                                        case "method":
                                                            method.setCompactionMethod(value);
                                                            break;
                                                    }
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "Error parsing number: " + value, e);
                                                }
                                            }
                                        }
                                        methods.add(method);
                                        Log.d(TAG, "Parsed old format string and added to methods");
                                    }

                                    // 将解析后的方法列表转换为JSON数组字符串
                                    String jsonArray = gson.toJson(methods);
                                    Log.d(TAG, "Converted methods to JSON: " + jsonArray);
                                    Log.d(TAG, "Methods size: " + methods.size());

                                    if (rvMoldingMethods != null) {
                                        // 创建新的适配器并设置给RecyclerView
                                        List<MixRatio> finalMixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
                                        Log.d(TAG, "Creating new adapter with " + methods.size() + " methods and " + 
                                            finalMixRatios.size() + " mix ratios");
                                            
                                        specimenMethodAdapter = new SpecimenMethodAdapter(jsonArray, finalMixRatios);
                                        rvMoldingMethods.setAdapter(specimenMethodAdapter);
                                        // 通知适配器数据已更新
                                        specimenMethodAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "Updated RecyclerView with new adapter");
                                    } else {
                                        Log.e(TAG, "RecyclerView is null when trying to update adapter");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing molding method", e);
                                    e.printStackTrace();
                                }
                            } else {
                                Log.w(TAG, "Molding method string is null or empty");
                            }
                        });
                    } else {
                        Log.e(TAG, "Task not found in database for taskId: " + taskId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading task data", e);
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.e(TAG, "TaskId is null");
        }
    }

    private void updateStepStatus(int currentStep) {
        // 更新步骤1的状态
        step1Indicator.setBackgroundResource(currentStep >= 1 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step1Text.setTextColor(getResources().getColor(currentStep >= 1 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));

        // 更新步骤2的状态
        step2Indicator.setBackgroundResource(currentStep >= 2 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step2Text.setTextColor(getResources().getColor(currentStep >= 2 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));

        // 更新连接线的状态
        step1Line.setBackgroundColor(getResources().getColor(currentStep >= 2 ? 
            android.R.color.holo_blue_dark : android.R.color.darker_gray));
        
        Log.d(TAG, "Updated step status to: " + currentStep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        if (db != null) {
            db.close();
            Log.d(TAG, "Database connection closed");
        }
    }
}
