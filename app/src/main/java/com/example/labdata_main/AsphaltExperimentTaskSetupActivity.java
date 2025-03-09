package com.example.labdata_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.labdata_main.R;
import com.example.labdata_main.fragment.AsphaltExperimentAssignmentFragment;
import com.example.labdata_main.fragment.AsphaltSelectionFragment;
import com.example.labdata_main.fragment.TaskNameFragment;
import com.example.labdata_main.service.AsphaltExperimentService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AsphaltExperimentTaskSetupActivity extends AppCompatActivity {
    private static final String EXTRA_TASK_NAME = "extra_task_name";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String[] tabTitles = {"任务名称", "沥青选择", "实验分配"};
    private MaterialButton btnNext;
    private View step1Layout, step2Layout, step3Layout;
    private TextView step1Circle, step2Circle, step3Circle;
    private TextView step1Text, step2Text, step3Text;
    private View step1Line, step2Line;
    private String taskName;
    private AsphaltExperimentService asphaltService;

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
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnNext = findViewById(R.id.btnNext);

        // 初始化沥青实验服务
        asphaltService = new AsphaltExperimentService(this);

        // 初始化步骤视图
        step1Layout = findViewById(R.id.step1Layout);
        step2Layout = findViewById(R.id.step2Layout);
        step3Layout = findViewById(R.id.step3Layout);
        step1Circle = findViewById(R.id.step1Circle);
        step2Circle = findViewById(R.id.step2Circle);
        step3Circle = findViewById(R.id.step3Circle);
        step1Text = findViewById(R.id.step1Text);
        step2Text = findViewById(R.id.step2Text);
        step3Text = findViewById(R.id.step3Text);
        step1Line = findViewById(R.id.step1Line);
        step2Line = findViewById(R.id.step2Line);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupViewPager();
        setupTabLayout();
        setupStepViews();
        updateStepStatus(0);

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
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
                        return TaskNameFragment.newInstance(taskName);
                    case 1:
                        return AsphaltSelectionFragment.newInstance(taskName);
                    case 2:
                        return AsphaltExperimentAssignmentFragment.newInstance(taskName);
                    default:
                        throw new IllegalStateException("Unexpected position " + position);
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateStepStatus(position);
                btnNext.setText(position == 2 ? "完成" : "下一步");
            }
        });
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();
    }

    private void setupStepViews() {
        step1Layout.setOnClickListener(v -> viewPager.setCurrentItem(0));
        step2Layout.setOnClickListener(v -> viewPager.setCurrentItem(1));
        step3Layout.setOnClickListener(v -> viewPager.setCurrentItem(2));
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

        // 更新连接线状态
        step2Line.setBackgroundColor(getColor(currentStep >= 2 ? 
            R.color.step_line_active : R.color.step_line_inactive));

        // 更新第三步状态
        step3Circle.setBackgroundResource(currentStep >= 2 ? 
            R.drawable.step_circle_active : R.drawable.step_circle_inactive);
        step3Text.setTextColor(getColor(currentStep >= 2 ? 
            R.color.step_text_active : R.color.step_text_inactive));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
