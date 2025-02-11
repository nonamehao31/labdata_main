package com.example.labdata_main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.adapter.ExperimentResultAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.dao.ExperimentDataDao;
import com.example.labdata_main.model.ExperimentData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RealExperimentAnalysisActivity extends AppCompatActivity {
    private LineChart lineChart;
    private Spinner spinnerMixRatio;
    private RecyclerView rvExperimentData;
    private AppDatabase database;
    private ExperimentDataDao experimentDataDao;
    private ExperimentResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_experiment_analysis);

        // 设置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化视图
        lineChart = findViewById(R.id.lineChart);
        spinnerMixRatio = findViewById(R.id.spinnerMixRatio);
        rvExperimentData = findViewById(R.id.rvExperimentData);

        // 初始化数据库
        database = AppDatabase.getInstance(this);
        experimentDataDao = database.experimentDataDao();

        // 设置RecyclerView
        rvExperimentData.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExperimentResultAdapter();
        rvExperimentData.setAdapter(adapter);

        // 配置图表
        setupLineChart();

        // 加载配比数据
        loadMixRatios();

        // 设置Spinner选择监听器
        spinnerMixRatio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMixRatio = parent.getItemAtPosition(position).toString();
                loadExperimentData(selectedMixRatio);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupLineChart() {
        // 配置X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // 配置Y轴
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);

        // 其他配置
        lineChart.setDescription(null);
        lineChart.setDrawBorders(true);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.WHITE);
    }

    private void loadMixRatios() {
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            Set<String> mixRatios = new HashSet<>();
            for (ExperimentData experiment : experiments) {
                String mixRatiosStr = experiment.getMixRatio();
                if (mixRatiosStr != null && !mixRatiosStr.trim().isEmpty()) {
                    // 拆分配比字符串
                    String[] ratios = mixRatiosStr.split(",");
                    for (String ratio : ratios) {
                        String trimmedRatio = ratio.trim();
                        if (!trimmedRatio.isEmpty()) {
                            mixRatios.add(trimmedRatio);
                        }
                    }
                }
            }
            
            List<String> sortedMixRatios = new ArrayList<>(mixRatios);
            java.util.Collections.sort(sortedMixRatios);
            
            if (sortedMixRatios.isEmpty()) {
                // 如果没有配比数据，显示提示信息
                TextView emptyView = new TextView(this);
                emptyView.setText("暂无配比数据");
                emptyView.setGravity(android.view.Gravity.CENTER);
                emptyView.setTextSize(16);
                emptyView.setTextColor(Color.GRAY);
                ((ViewGroup) spinnerMixRatio.getParent()).addView(emptyView);
                spinnerMixRatio.setVisibility(View.GONE);
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortedMixRatios
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMixRatio.setAdapter(adapter);
            
            // 如果只有一个配比，自动选择它
            if (sortedMixRatios.size() == 1) {
                loadExperimentData(sortedMixRatios.get(0));
            }
        });
    }

    private void loadExperimentData(String selectedMixRatio) {
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            List<ExperimentData> filteredExperiments = new ArrayList<>();
            for (ExperimentData experiment : experiments) {
                String mixRatiosStr = experiment.getMixRatio();
                if (mixRatiosStr != null) {
                    // 拆分配比字符串并检查是否包含选中的配比
                    String[] ratios = mixRatiosStr.split(",");
                    for (String ratio : ratios) {
                        if (selectedMixRatio.equals(ratio.trim())) {
                            filteredExperiments.add(experiment);
                            break;
                        }
                    }
                }
            }

            // 更新适配器数据
            adapter.setExperiments(filteredExperiments);
            adapter.notifyDataSetChanged();

            // 更新图表数据
            updateLineChart(filteredExperiments);
        });
    }

    private void updateLineChart(List<ExperimentData> experiments) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < experiments.size(); i++) {
            ExperimentData experiment = experiments.get(i);
            try {
                float result = Float.parseFloat(experiment.getResult());
                entries.add(new Entry(i, result));
            } catch (NumberFormatException e) {
                // 跳过无法解析为数字的结果
                continue;
            }
        }

        if (entries.isEmpty()) {
            lineChart.clear();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "实验结果");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
