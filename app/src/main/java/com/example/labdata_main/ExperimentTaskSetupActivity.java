package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.example.labdata_main.model.Project;

public class ExperimentTaskSetupActivity extends AppCompatActivity implements AddProjectBottomSheet.OnProjectAddedListener {
    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private String taskName;
    private Project selectedProject;

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
        
        initViews();
        setupViewPager();
        setupClickListeners();
        
        // 初始时禁用下一步按钮，直到选择了项目
        enableNextButton(false);
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
            }
        });
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                viewPager.setCurrentItem(currentStep + 1);
            } else {
                // 完成设置
                finish();
            }
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

    private void updateNextButton() {
        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("完成");
        } else {
            btnNext.setText("下一步");
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
        // TODO: 更新项目列表的显示
    }

    @Override
    public void onProjectAdded(Project project) {
        // 当新项目添加后，更新选中的项目
        setSelectedProject(project);
    }
}
