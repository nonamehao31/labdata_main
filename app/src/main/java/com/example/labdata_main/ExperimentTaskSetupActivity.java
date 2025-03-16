package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.service.SyncService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.Project;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.utils.TaskIdGenerator;
import com.example.labdata_main.utils.TokenExpirationReceiver;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class ExperimentTaskSetupActivity extends AppCompatActivity implements AddProjectBottomSheet.OnProjectAddedListener {
    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private String taskName;
    private Project selectedProject;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SharedPrefsManager sharedPrefsManager;
    private TokenExpirationReceiver tokenExpirationReceiver;

    // 步骤导航视图
    private TextView[] stepCircles;
    private TextView[] stepTexts;
    private View[] stepLines;
    private int currentStep = 0;
    private final int TOTAL_STEPS = 4;

    public static void start(Context context, String taskName) {
        Intent intent = new Intent(context, ExperimentTaskSetupActivity.class);
        intent.putExtra("task_name", taskName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_task_setup);

        taskName = getIntent().getStringExtra("task_name");
        if (taskName == null || taskName.isEmpty()) {
            taskName = "实验任务设置";
        }
        
        // 初始化数据库和SharedPrefsManager
        AppDatabase database = AppDatabase.getInstance(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        
        // 注册令牌过期广播接收器
        tokenExpirationReceiver = new TokenExpirationReceiver(this);
        IntentFilter intentFilter = new IntentFilter("com.example.labdata_main.TOKEN_EXPIRED");
        registerReceiver(tokenExpirationReceiver, intentFilter);
        
        initViews();
        setupViewPager();
        setupClickListeners();
        
        // 初始时禁用下一步按钮，直到选择了项目
        enableNextButton(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        // 取消注册令牌过期广播接收器
        unregisterReceiver(tokenExpirationReceiver);
    }

    private void initViews() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(taskName);
        }

        // 初始化视图
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);

        // 初始化步骤导航
        stepCircles = new TextView[TOTAL_STEPS];
        stepTexts = new TextView[TOTAL_STEPS];
        stepLines = new View[TOTAL_STEPS - 1];

        for (int i = 0; i < TOTAL_STEPS; i++) {
            stepCircles[i] = findViewById(getResources().getIdentifier("step" + (i + 1) + "Circle", "id", getPackageName()));
            stepTexts[i] = findViewById(getResources().getIdentifier("step" + (i + 1) + "Text", "id", getPackageName()));
            if (i < TOTAL_STEPS - 1) {
                stepLines[i] = findViewById(getResources().getIdentifier("step" + (i + 1) + "Line", "id", getPackageName()));
            }
        }

        updateStepIndicators();
    }

    private void setupViewPager() {
        ExperimentTaskPagerAdapter adapter = new ExperimentTaskPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(true); // 启用滑动切换

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentStep = position;
                updateStepIndicators();
                updateNextButton();

                // 当切换到实验指派页面时，传递选中的配比
                ExperimentTaskPagerAdapter pagerAdapter = (ExperimentTaskPagerAdapter) viewPager.getAdapter();
                if (pagerAdapter != null) {
                    if (position == 3) {
                        SelectMixRatioFragment mixRatioFragment = pagerAdapter.getMixRatioFragment();
                        ExperimentAssignmentFragment assignmentFragment = pagerAdapter.getExperimentAssignmentFragment();
                        if (mixRatioFragment != null && assignmentFragment != null) {
                            assignmentFragment.setSelectedMixRatios(mixRatioFragment.getSelectedMixRatios());
                        }
                    }
                    // 延迟更新按钮状态，确保 Fragment 已完全初始化
                    viewPager.post(() -> updateNextButton());
                }
            }
        });
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                viewPager.setCurrentItem(currentStep + 1);
            } else {
                // 在后台线程中保存任务
                saveExperimentTask();
            }
        });
    }

    private void saveExperimentTask() {
        ExperimentTaskPagerAdapter pagerAdapter = (ExperimentTaskPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter == null) return;

        SelectProjectFragment projectFragment = pagerAdapter.getProjectFragment();
        SelectMixRatioFragment mixRatioFragment = pagerAdapter.getMixRatioFragment();
        SelectMoldingMethodFragment moldingMethodFragment = pagerAdapter.getMoldingMethodFragment();
        ExperimentAssignmentFragment assignmentFragment = pagerAdapter.getExperimentAssignmentFragment();

        if (projectFragment == null || mixRatioFragment == null || 
            moldingMethodFragment == null || assignmentFragment == null) {
            Toast.makeText(this, "设置任务时出现错误，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 禁用按钮防止多次提交
        btnNext.setEnabled(false);
        btnNext.setText("保存中...");

        // 为混合料任务设置项目ID和制件方式
        assignmentFragment.setProjectId(Long.valueOf(selectedProject.getId()));
        assignmentFragment.setSelectedMoldingMethods(moldingMethodFragment.getSelectedMethods());

        // 调用新的混合料任务保存方法
        assignmentFragment.saveMixtureTask(
            // 成功回调
            () -> {
                // 在UI线程上执行
                runOnUiThread(() -> {
                    Toast.makeText(this, "任务保存成功！", Toast.LENGTH_LONG).show();
                    btnNext.setText("完成");
                    btnNext.setEnabled(true);
                    // 设置延迟以便用户看到成功消息
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        finish(); // 关闭当前活动，返回上一个界面
                    }, 1500);
                });
            },
            // 失败回调
            () -> {
                // 在UI线程上执行
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                    btnNext.setText("保存");
                    btnNext.setEnabled(true);
                });
            }
        );
    }

    private void updateStepIndicators() {
        for (int i = 0; i < TOTAL_STEPS; i++) {
            if (i < currentStep) {
                // 已完成的步骤
                stepCircles[i].setBackgroundResource(R.drawable.step_circle_completed);
                stepCircles[i].setTextColor(getResources().getColor(android.R.color.white));
                stepTexts[i].setTextColor(getResources().getColor(R.color.step_text_active));
                if (i < TOTAL_STEPS - 1) {
                    stepLines[i].setBackgroundColor(getResources().getColor(R.color.step_line_active));
                }
            } else if (i == currentStep) {
                // 当前步骤
                stepCircles[i].setBackgroundResource(R.drawable.step_circle_active);
                stepCircles[i].setTextColor(getResources().getColor(android.R.color.white));
                stepTexts[i].setTextColor(getResources().getColor(R.color.step_text_active));
                if (i < TOTAL_STEPS - 1) {
                    stepLines[i].setBackgroundColor(getResources().getColor(R.color.step_line_inactive));
                }
            } else {
                // 未完成的步骤
                stepCircles[i].setBackgroundResource(R.drawable.step_circle_inactive);
                stepCircles[i].setTextColor(getResources().getColor(R.color.step_text_inactive));
                stepTexts[i].setTextColor(getResources().getColor(R.color.step_text_inactive));
                if (i < TOTAL_STEPS - 1) {
                    stepLines[i].setBackgroundColor(getResources().getColor(R.color.step_line_inactive));
                }
            }
        }
    }

    public void updateNextButton() {
        updateNextButton(currentStep);
    }

    private void updateNextButton(int step) {
        ExperimentTaskPagerAdapter pagerAdapter = (ExperimentTaskPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter == null) return;

        if (step == TOTAL_STEPS - 1) {
            // 在最后一步，检查所有必要数据是否完整
            SelectMixRatioFragment mixRatioFragment = pagerAdapter.getMixRatioFragment();
            SelectMoldingMethodFragment moldingMethodFragment = pagerAdapter.getMoldingMethodFragment();
            ExperimentAssignmentFragment assignmentFragment = pagerAdapter.getExperimentAssignmentFragment();

            boolean isComplete = selectedProject != null &&
                    mixRatioFragment != null && !mixRatioFragment.getSelectedMixRatios().isEmpty() &&
                    moldingMethodFragment != null && !moldingMethodFragment.getSelectedMethods().isEmpty() &&
                    assignmentFragment != null && !assignmentFragment.getExperimentAssignments().isEmpty();

            // 检查每个配比是否都分配了实验
            if (isComplete && assignmentFragment != null) {
                Map<Long, List<String>> assignments = assignmentFragment.getExperimentAssignments();
                for (List<String> experiments : assignments.values()) {
                    if (experiments.isEmpty()) {
                        isComplete = false;
                        break;
                    }
                }
            }

            btnNext.setText("保存");
            btnNext.setEnabled(isComplete);
        } else {
            btnNext.setText("下一步");
            // 根据当前步骤检查是否可以进入下一步
            switch (step) {
                case 0: // 项目选择
                    btnNext.setEnabled(selectedProject != null);
                    break;
                case 1: // 配比选择
                    SelectMixRatioFragment mixRatioFragment = pagerAdapter.getMixRatioFragment();
                    btnNext.setEnabled(mixRatioFragment != null && !mixRatioFragment.getSelectedMixRatios().isEmpty());
                    break;
                case 2: // 制件方法
                    SelectMoldingMethodFragment moldingMethodFragment = pagerAdapter.getMoldingMethodFragment();
                    btnNext.setEnabled(moldingMethodFragment != null && !moldingMethodFragment.getSelectedMethods().isEmpty());
                    break;
                default:
                    btnNext.setEnabled(true);
            }
        }
    }

    public void enableNextButton(boolean enable) {
        if (btnNext != null) {
            btnNext.setEnabled(enable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTaskName() {
        return taskName;
    }

    public void setSelectedProject(Project project) {
        this.selectedProject = project;
        enableNextButton(true);
    }

    @Override
    public void onProjectAdded(Project project) {
        // 当新项目添加后，更新选中的项目
        setSelectedProject(project);
    }
}
