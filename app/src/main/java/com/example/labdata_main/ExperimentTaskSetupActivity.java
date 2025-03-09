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

        SelectMixRatioFragment mixRatioFragment = pagerAdapter.getMixRatioFragment();
        SelectMoldingMethodFragment moldingMethodFragment = pagerAdapter.getMoldingMethodFragment();
        ExperimentAssignmentFragment assignmentFragment = pagerAdapter.getExperimentAssignmentFragment();

        ExperimentTask task = new ExperimentTask();
        task.setTaskName(taskName);
        task.setStatus("未接受");
        task.setExperimentType("MIXTURE"); // 设置为混合料实验类型
        task.setCompanyId(sharedPrefsManager.getUserCompany()); // 设置公司ID
        task.setProjectId(selectedProject.getId());
        task.setProjectName(selectedProject.getName());

        // 设置截止日期
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String deadlineStr = selectedProject.getDeadline();
            if (deadlineStr != null && !deadlineStr.isEmpty()) {
                Date deadlineDate = dateFormat.parse(deadlineStr);
                if (deadlineDate != null) {
                    task.setDeadline(deadlineDate.getTime());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            task.setDeadline(0); // 如果转换失败，设置为0
        }

        // 设置选中的配比
        List<MixRatio> selectedMixRatios = mixRatioFragment.getSelectedMixRatios();
        task.setSelectedMixRatios(selectedMixRatios);
        
        // 如果选择了一种配比，则设置配比ID
        if (selectedMixRatios != null && !selectedMixRatios.isEmpty()) {
            // 选择第一个配比的ID
            task.setMixRatioId(selectedMixRatios.get(0).getId());
        }
        
        // 设置制件方法
        String moldingMethod = moldingMethodFragment.getSelectedMoldingMethod();
        task.setMoldingMethod(moldingMethod);
        
        // 如果有制件方法ID，则设置制件方法ID
        List<Long> selectedMethodIds = moldingMethodFragment.getSelectedMethodIds();
        if (selectedMethodIds != null && !selectedMethodIds.isEmpty()) {
            task.setMixingMethodId(selectedMethodIds.get(0));
        }
        
        // 设置材料ID列表
        // 注意：需要从配比中获取材料ID
        List<Long> materialIds = new ArrayList<>();
        if (selectedMixRatios != null) {
            for (MixRatio ratio : selectedMixRatios) {
                // 获取配比中的材料ID
                // 如果配比中没有材料ID，则跳过
                if (ratio.getMaterialIds() != null) {
                    materialIds.addAll(ratio.getMaterialIds());
                }
            }
        }
        // 去除重复的材料ID
        Set<Long> uniqueMaterialIds = new HashSet<>(materialIds);
        task.setMaterialIds(new ArrayList<>(uniqueMaterialIds));
        
        task.setExperimentAssignments(assignmentFragment.getExperimentAssignments());
        task.setNotes(assignmentFragment.getNotes());
        task.setCreationTime(System.currentTimeMillis());

        executor.execute(() -> {
            // 先将实验任务保存到本地数据库
            AppDatabase.getInstance(this).experimentTaskDao().insert(task);

            // 使用新的SyncService将实验任务同步到服务器
            try {
                Log.d("ExperimentTask", "正在发送实验任务同步请求: " + task.getTaskName() + ", projectId: " + task.getProjectId());
                Log.d("ExperimentTask", "mixRatioId: " + task.getMixRatioId() + ", mixingMethodId: " + task.getMixingMethodId());
                if (task.getMaterialIds() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Long id : task.getMaterialIds()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(id);
                    }
                    Log.d("ExperimentTask", "materialIds: " + sb.toString());
                }
                
                // 使用SyncService异步处理同步
                SyncService.syncExperimentTask(task, new SyncService.SyncResultListener<ExperimentTask>() {
                    @Override
                    public void onSyncSuccess(ExperimentTask result) {
                        Log.d("ExperimentTask", "实验任务同步到服务器成功");
                        if (result != null) {
                            Log.d("ExperimentTask", "服务器返回任务ID: " + result.getId());
                        }
                        // 成功后更新UI或进行其他操作
                        runOnUiThread(() -> {
                            Toast.makeText(ExperimentTaskSetupActivity.this, "任务已成功同步到服务器", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onSyncFailure(int code, String message) {
                        Log.e("ExperimentTask", "实验任务同步到服务器失败: " + code);
                        Log.e("ExperimentTask", "错误详情: " + message);
                        // 失败后更新UI或进行其他操作
                        runOnUiThread(() -> {
                            Toast.makeText(ExperimentTaskSetupActivity.this, "任务同步失败: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
                
            } catch (Exception e) {
                Log.e("ExperimentTask", "实验任务同步到服务器异常", e);
                Toast.makeText(this, "任务同步异常: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // 发送广播通知主页刷新
            Intent refreshIntent = new Intent("com.example.labdata_main.TASK_UPDATED");
            sendBroadcast(refreshIntent);

            // 返回主页
            finish();
        });
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
