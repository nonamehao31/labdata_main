package com.example.labdata_main;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

    private void loadExperimentTypes(String selectedMixRatio) {
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            Set<String> experimentTypes = new HashSet<>();
            for (ExperimentData experiment : experiments) {
                String mixRatiosStr = experiment.getMixRatio();
                if (mixRatiosStr != null) {
                    String[] ratios = mixRatiosStr.split(",");
                    for (String ratio : ratios) {
                        if (selectedMixRatio.equals(ratio.trim())) {
                            String mainType = extractMainExperimentType(experiment.getExperimentName());
                            if (!mainType.isEmpty()) {
                                experimentTypes.add(mainType);
                            }
                            break;
                        }
                    }
                }
            }

            List<String> sortedTypes = new ArrayList<>(experimentTypes);
            java.util.Collections.sort(sortedTypes);

            if (sortedTypes.isEmpty()) {
                spinnerExperimentType.setVisibility(View.GONE);
                return;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                sortedTypes
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExperimentType.setAdapter(adapter);
            
            if (sortedTypes.size() == 1) {
                currentExperimentType = sortedTypes.get(0);
                loadExperimentData(selectedMixRatio, currentExperimentType);
            }
        });
    }

    private void loadExperimentData(String mixRatio, String mainExperimentType) {
        Log.d("RealExperimentAnalysis", "开始加载数据 - 配比: " + mixRatio + ", 实验类型: " + mainExperimentType);
        
        LiveData<List<ExperimentData>> experimentsLiveData = experimentDataDao.getAllExperiments();
        experimentsLiveData.observe(this, experiments -> {
            Log.d("RealExperimentAnalysis", "获取到实验数据总数: " + experiments.size());
            
            Map<String, List<ExperimentData>> subTypeData = new TreeMap<>();
            List<ExperimentData> allData = new ArrayList<>();

            // 按子类型分组数据
            for (ExperimentData experiment : experiments) {
                if (experiment.getMixRatio() != null && 
                    experiment.getMixRatio().contains(mixRatio) && 
                    experiment.getExperimentName() != null && 
                    extractMainExperimentType(experiment.getExperimentName()).equals(mainExperimentType)) {
                    
                    String subType = experiment.getExperimentName();
                    Log.d("RealExperimentAnalysis", "找到匹配数据 - 子类型: " + subType + 
                        ", 配比: " + experiment.getMixRatio() + 
                        ", 结果: " + experiment.getResult());
                    
                    if (!subTypeData.containsKey(subType)) {
                        subTypeData.put(subType, new ArrayList<>());
                    }
                    subTypeData.get(subType).add(experiment);
                    allData.add(experiment);
                }
            }

            Log.d("RealExperimentAnalysis", "筛选后的数据总数: " + allData.size());
            Log.d("RealExperimentAnalysis", "子类型数量: " + subTypeData.size());

            // 更新RecyclerView显示所有数据
            adapter.setExperiments(allData);

            // 准备图表数据
            List<ILineDataSet> dataSets = new ArrayList<>();
            List<String> xAxisLabels = new ArrayList<>();
            int colorIndex = 0;
            int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN};

            for (Map.Entry<String, List<ExperimentData>> entry : subTypeData.entrySet()) {
                List<Entry> entries = new ArrayList<>();
                List<ExperimentData> subTypeExperiments = entry.getValue();
                
                Log.d("RealExperimentAnalysis", "处理子类型: " + entry.getKey() + 
                    ", 数据点数量: " + subTypeExperiments.size());
                
                // 按时间排序
                subTypeExperiments.sort((a, b) -> Long.compare(a.getCreateTime(), b.getCreateTime()));
                
                // 创建数据点
                for (int i = 0; i < subTypeExperiments.size(); i++) {
                    ExperimentData data = subTypeExperiments.get(i);
                    String result = data.getResult();
                    
                    // 检查结果是否为空
                    if (result == null || result.trim().isEmpty()) {
                        Log.w("RealExperimentAnalysis", "跳过空结果 - 子类型: " + entry.getKey());
                        continue;
                    }
                    
                    try {
                        // 尝试解析数值
                        float resultValue;
                        if (result.contains("kN")) {
                            // 如果结果包含单位，去掉单位再解析
                            resultValue = Float.parseFloat(result.replace("kN", "").trim());
                        } else if (result.contains("mm")) {
                            // 如果结果包含单位，去掉单位再解析
                            resultValue = Float.parseFloat(result.replace("mm", "").trim());
                        } else {
                            // 直接解析数值
                            resultValue = Float.parseFloat(result.trim());
                        }
                        
                        entries.add(new Entry(i, resultValue));
                        Log.d("RealExperimentAnalysis", "添加数据点 - 子类型: " + entry.getKey() + 
                            ", 索引: " + i + ", 值: " + resultValue);
                        
                        // 添加时间标签
                        String timeLabel = dateFormat.format(new Date(data.getCreateTime()));
                        if (!xAxisLabels.contains(timeLabel)) {
                            xAxisLabels.add(timeLabel);
                        }
                    } catch (NumberFormatException e) {
                        Log.w("RealExperimentAnalysis", "无法解析结果值: " + result + 
                            " - 子类型: " + entry.getKey(), e);
                        continue;
                    }
                }

                // 只有当有数据点时才创建数据集
                if (!entries.isEmpty()) {
                    // 创建数据集
                    LineDataSet dataSet = new LineDataSet(entries, entry.getKey());
                    dataSet.setColor(colors[colorIndex % colors.length]);
                    dataSet.setCircleColor(colors[colorIndex % colors.length]);
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(4f);
                    dataSet.setDrawValues(true);
                    dataSets.add(dataSet);
                    
                    Log.d("RealExperimentAnalysis", "创建数据集 - 子类型: " + entry.getKey() + 
                        ", 数据点数量: " + entries.size());
                    
                    colorIndex++;
                } else {
                    Log.w("RealExperimentAnalysis", "子类型没有有效数据点: " + entry.getKey());
                }
            }

            // 更新图表
            if (!dataSets.isEmpty()) {
                LineData lineData = new LineData(dataSets);
                lineChart.setData(lineData);
                
                // 设置X轴标签
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
                xAxis.setLabelCount(xAxisLabels.size());
                
                lineChart.invalidate();
                Log.d("RealExperimentAnalysis", "更新图表 - 数据集数量: " + dataSets.size() + 
                    ", 时间标签数量: " + xAxisLabels.size());
            } else {
                lineChart.clear();
                lineChart.setNoDataText("暂无数据");
                Log.w("RealExperimentAnalysis", "没有可显示的数据集");
            }
        });
    }
}
