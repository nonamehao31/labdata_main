package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.fragment.AsphaltExperimentAssignmentFragment;
import com.example.labdata_main.fragment.AsphaltSelectionFragment;
import com.google.android.material.button.MaterialButton;

public class AsphaltExperimentTaskSetupActivity extends AppCompatActivity {
    private static final String EXTRA_TASK_NAME = "extra_task_name";
    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private View step1Layout, step2Layout;
    private TextView step1Circle, step2Circle;
    private TextView step1Text, step2Text;
    private View step1Line;
    private String taskName;

    public static void start(Context context, String taskName) {
        Intent intent = new Intent(context, AsphaltExperimentTaskSetupActivity.class);
        intent.putExtra(EXTRA_TASK_NAME, taskName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asphalt_experiment_task_setup);

        taskName = getIntent().getStringExtra(EXTRA_TASK_NAME);
        initializeViews();
        setupViewPager();
        setupStepViews();
        updateStepStatus(0);
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);

        // 初始化步骤视图
        step1Layout = findViewById(R.id.step1Layout);
        step2Layout = findViewById(R.id.step2Layout);
        step1Circle = findViewById(R.id.step1Circle);
        step2Circle = findViewById(R.id.step2Circle);
        step1Text = findViewById(R.id.step1Text);
        step2Text = findViewById(R.id.step2Text);
        step1Line = findViewById(R.id.step1Line);

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                // 发送广播通知任务更新
                Intent intent = new Intent("com.example.labdata_main.TASK_UPDATED");
                sendBroadcast(intent);
                finish();
            }
        });
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return AsphaltSelectionFragment.newInstance(taskName);
                    case 1:
                        return AsphaltExperimentAssignmentFragment.newInstance(taskName);
                    default:
                        throw new IllegalStateException("Unexpected position " + position);
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepStatus(position);
                btnNext.setText(position == 1 ? "完成" : "下一步");
            }
        });
    }

    private void setupStepViews() {
        step1Layout.setOnClickListener(v -> viewPager.setCurrentItem(0));
        step2Layout.setOnClickListener(v -> viewPager.setCurrentItem(1));
    }

    private void updateStepStatus(int currentStep) {
        // 更新第一步状态
        step1Circle.setBackgroundResource(currentStep >= 0 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step1Text.setTextColor(getColor(currentStep >= 0 ? 
            R.color.step_text_active : R.color.step_text_inactive));

        // 更新连接线状态
        step1Line.setBackgroundColor(getColor(currentStep >= 1 ? 
            R.color.step_line_active : R.color.step_line_inactive));

        // 更新第二步状态
        step2Circle.setBackgroundResource(currentStep >= 1 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step2Text.setTextColor(getColor(currentStep >= 1 ? 
            R.color.step_text_active : R.color.step_text_inactive));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
