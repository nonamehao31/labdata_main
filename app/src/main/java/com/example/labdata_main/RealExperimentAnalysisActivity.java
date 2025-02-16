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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RealExperimentAnalysisActivity extends AppCompatActivity {
    private LineChart lineChart;
    private Spinner spinnerMixRatio;
    private Spinner spinnerExperimentType;
    private RecyclerView rvExperimentData;
    private AppDatabase database;
    private ExperimentDataDao experimentDataDao;
    private ExperimentResultAdapter adapter;
    private String currentMixRatio;
    private String currentExperimentType;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

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
        spinnerExperimentType = findViewById(R.id.spinnerExperimentType);
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
                currentMixRatio = parent.getItemAtPosition(position).toString();
                loadExperimentTypes(currentMixRatio);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerExperimentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentExperimentType = parent.getItemAtPosition(position).toString();
                loadExperimentData(currentMixRatio, currentExperimentType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
    }

    private void loadMixRatios() {
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            Set<String> mixRatios = new HashSet<>();
            for (ExperimentData experiment : experiments) {
                String mixRatiosStr = experiment.getMixRatio();
                if (mixRatiosStr != null && !mixRatiosStr.trim().isEmpty()) {
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
                TextView emptyView = new TextView(this);
                emptyView.setText("暂无配比数据");
                emptyView.setGravity(android.view.Gravity.CENTER);
                emptyView.setTextSize(16);
                emptyView.setTextColor(Color.GRAY);
                ((ViewGroup) spinnerMixRatio.getParent()).addView(emptyView);
                spinnerMixRatio.setVisibility(View.GONE);
                spinnerExperimentType.setVisibility(View.GONE);
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortedMixRatios
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMixRatio.setAdapter(adapter);
            
            if (sortedMixRatios.size() == 1) {
                currentMixRatio = sortedMixRatios.get(0);
                loadExperimentTypes(currentMixRatio);
            }
        });
    }

    private void loadExperimentTypes(String mixRatio) {
        new Thread(() -> {
            try {
                // 获取该配比下的所有实验数据
                List<ExperimentData> experimentDataList = experimentDataDao.getExperimentDataByMixRatio(mixRatio);
                
                // 使用TreeMap来保存主实验类型和其对应的子类型
                Map<String, Set<String>> experimentTypeMap = new TreeMap<>();
                
                for (ExperimentData data : experimentDataList) {
                    String experimentName = data.getExperimentName();
                    if (experimentName != null) {
                        // 获取主实验类型
                        String mainType = extractMainExperimentType(experimentName);
                        
                        // 将子类型添加到对应的主类型集合中
                        experimentTypeMap.computeIfAbsent(mainType, k -> new HashSet<>())
                                      .add(experimentName);
                    }
                }
                
                // 创建实验类型列表，包含所有子类型
                List<String> experimentTypes = new ArrayList<>();
                for (Map.Entry<String, Set<String>> entry : experimentTypeMap.entrySet()) {
                    experimentTypes.addAll(entry.getValue());
                }

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            experimentTypes
                    ) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            String item = getItem(position);
                            // 显示完整的实验名称
                            textView.setText(item);
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            String item = getItem(position);
                            // 显示完整的实验名称，可以考虑添加缩进等视觉效果
                            textView.setText(item);
                            return view;
                        }
                    };
                    
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerExperimentType.setAdapter(adapter);
                    
                    // 如果有数据，选择第一项
                    if (!experimentTypes.isEmpty()) {
                        spinnerExperimentType.setSelection(0);
                        currentExperimentType = experimentTypes.get(0);
                        loadExperimentData(currentMixRatio, currentExperimentType);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String extractMainExperimentType(String experimentName) {
        if (experimentName == null) return "";
        
        if (experimentName.contains("马歇尔")) {
            return "马歇尔实验";
        }
        if (experimentName.contains("车辙")) {
            return "车辙实验";
        }
        // 其他实验类型可以继续添加...
        
        return experimentName;
    }

    private void loadExperimentData(String selectedMixRatio, String selectedExperimentType) {
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            List<ExperimentData> filteredExperiments = new ArrayList<>();
            Map<String, List<ExperimentData>> subTypeData = new TreeMap<>();

            for (ExperimentData experiment : experiments) {
                String mixRatiosStr = experiment.getMixRatio();
                if (mixRatiosStr != null && experiment.getExperimentName().equals(selectedExperimentType)) {
                    String[] ratios = mixRatiosStr.split(",");
                    for (String ratio : ratios) {
                        if (selectedMixRatio.equals(ratio.trim())) {
                            filteredExperiments.add(experiment);
                            
                            // 按输入标签分组
                            String subType = experiment.getInput1Label();
                            subTypeData.computeIfAbsent(subType, k -> new ArrayList<>()).add(experiment);
                            
                            if (experiment.getInput2Label() != null && !experiment.getInput2Label().isEmpty()) {
                                subType = experiment.getInput2Label();
                                subTypeData.computeIfAbsent(subType, k -> new ArrayList<>()).add(experiment);
                            }
                            break;
                        }
                    }
                }
            }

            // 更新适配器数据
            adapter.setExperiments(filteredExperiments);
            adapter.notifyDataSetChanged();

            // 更新图表数据
            updateLineChart(subTypeData);
        });
    }

    private void updateLineChart(Map<String, List<ExperimentData>> subTypeData) {
        lineChart.clear();
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        
        int colorIndex = 0;
        int[] colors = new int[]{
            Color.rgb(255, 87, 34),  // 深橙色
            Color.rgb(33, 150, 243), // 蓝色
            Color.rgb(76, 175, 80),  // 绿色
            Color.rgb(156, 39, 176), // 紫色
            Color.rgb(255, 152, 0),  // 橙色
            Color.rgb(3, 169, 244),  // 浅蓝色
        };

        for (Map.Entry<String, List<ExperimentData>> entry : subTypeData.entrySet()) {
            String subType = entry.getKey();
            List<ExperimentData> dataList = entry.getValue();
            List<Entry> entries = new ArrayList<>();
            
            // 按时间排序
            dataList.sort((a, b) -> Long.compare(a.getCreateTime(), b.getCreateTime()));
            
            // 生成数据点
            for (int i = 0; i < dataList.size(); i++) {
                ExperimentData data = dataList.get(i);
                float value;
                if (subType.equals(data.getInput1Label())) {
                    value = (float) data.getInput1Value();
                } else {
                    value = (float) data.getInput2Value();
                }
                entries.add(new Entry(i, value));
                
                // 添加时间标签
                String timeLabel = dateFormat.format(new Date(data.getCreateTime()));
                if (i >= xAxisLabels.size()) {
                    xAxisLabels.add(timeLabel);
                }
            }
            
            // 创建数据集
            LineDataSet dataSet = new LineDataSet(entries, subType);
            dataSet.setColor(colors[colorIndex % colors.length]);
            dataSet.setCircleColor(colors[colorIndex % colors.length]);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawValues(true);
            dataSet.setMode(LineDataSet.Mode.LINEAR);
            dataSet.setCubicIntensity(0.2f);
            
            dataSets.add(dataSet);
            colorIndex++;
        }

        // 设置X轴标签
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        
        // 更新图表
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}
