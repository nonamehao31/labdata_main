package com.example.labdata_main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private PieChart pieChartMaterials;
    private TabLayout tabLayoutMoldingMethods;
    private Button btnMaterialDetails;
    private Button btnPrintSpecimenCode;
    private EditText etPrintQuantity;
    private TextView tvExperimentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initViews();
        setupPieChart();
        setupMoldingMethodTabs();
        setupButtonListeners();
        setupExperimentDetails();
    }

    private void initViews() {
        pieChartMaterials = findViewById(R.id.pieChartMaterials);
        tabLayoutMoldingMethods = findViewById(R.id.tabLayoutMoldingMethods);
        btnMaterialDetails = findViewById(R.id.btnMaterialDetails);
        btnPrintSpecimenCode = findViewById(R.id.btnPrintSpecimenCode);
        etPrintQuantity = findViewById(R.id.etPrintQuantity);
        tvExperimentDetails = findViewById(R.id.tvExperimentDetails);
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30f, "水泥"));
        entries.add(new PieEntry(20f, "骨料"));
        entries.add(new PieEntry(15f, "砂子"));
        entries.add(new PieEntry(35f, "水"));

        PieDataSet dataSet = new PieDataSet(entries, "配料比例");
        dataSet.setColors(new int[]{
            Color.rgb(33, 150, 243),   // 蓝色
            Color.rgb(76, 175, 80),    // 绿色
            Color.rgb(255, 152, 0),    // 橙色
            Color.rgb(96, 125, 139)    // 灰蓝色
        });

        PieData pieData = new PieData(dataSet);
        pieChartMaterials.setData(pieData);
        pieChartMaterials.getDescription().setEnabled(false);
        pieChartMaterials.setEntryLabelColor(Color.BLACK);
        pieChartMaterials.invalidate();
    }

    private void setupMoldingMethodTabs() {
        tabLayoutMoldingMethods.addTab(tabLayoutMoldingMethods.newTab().setText("压实法"));
        tabLayoutMoldingMethods.addTab(tabLayoutMoldingMethods.newTab().setText("浇筑法"));
        tabLayoutMoldingMethods.addTab(tabLayoutMoldingMethods.newTab().setText("振动法"));

        tabLayoutMoldingMethods.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 根据选择的制件方式显示对应的二维码或详情
                Toast.makeText(TaskDetailActivity.this, "选择了：" + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupButtonListeners() {
        btnMaterialDetails.setOnClickListener(v -> {
            // 跳转到配料详情页面
            Toast.makeText(this, "查看配料详情", Toast.LENGTH_SHORT).show();
        });

        btnPrintSpecimenCode.setOnClickListener(v -> {
            String quantity = etPrintQuantity.getText().toString();
            if (!quantity.isEmpty()) {
                // 生成并打印试件码
                Toast.makeText(this, "打印 " + quantity + " 个试件码", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "请输入打印数量", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExperimentDetails() {
        String taskName = getIntent().getStringExtra("TASK_NAME");
        String taskInfo = getIntent().getStringExtra("TASK_INFO");
        boolean isTaskCompleted = getIntent().getBooleanExtra("TASK_COMPLETED", false);

        String detailsText = "任务名称：" + taskName + "\n" +
                             "任务详情：" + taskInfo + "\n" +
                             "任务状态：" + (isTaskCompleted ? "已完成" : "进行中");

        tvExperimentDetails.setText(detailsText);
    }
}
