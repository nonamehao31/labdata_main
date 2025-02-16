package com.example.labdata_main;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.adapter.ExperimentResultAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.dao.ExperimentDataDao;
import com.example.labdata_main.model.ExperimentData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

public class RealExperimentAnalysisActivity extends AppCompatActivity {
    private LineChart lineChart;
    private Spinner spinnerMixRatio;
    private Spinner spinnerMainExperimentType;  // 新增：实验主类选择器
    private Spinner spinnerSubExperimentType;   // 新增：实验数据子类选择器
    private RecyclerView rvExperimentData;
    private AppDatabase database;
    private ExperimentDataDao experimentDataDao;
    private ExperimentResultAdapter adapter;
    private String currentMixRatio;
    private String currentMainExperimentType;   // 新增：当前选中的实验主类
    private String currentSubExperimentType;    // 新增：当前选中的数据子类
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private LiveData<List<ExperimentData>> currentRuttingDataObserver;

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
        spinnerMainExperimentType = findViewById(R.id.spinnerMainExperimentType);  // 新增：实验主类选择器
        spinnerSubExperimentType = findViewById(R.id.spinnerSubExperimentType);   // 新增：实验数据子类选择器
        rvExperimentData = findViewById(R.id.rvExperimentData);

        // 初始化数据库
        database = AppDatabase.getInstance(this);
        experimentDataDao = database.experimentDataDao();

        // 设置RecyclerView
        rvExperimentData.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExperimentResultAdapter();
        rvExperimentData.setAdapter(adapter);

        // 配置图表
        setupChart();

        // 加载配比数据
        loadMixRatios();

        // 设置Spinner选择监听器
        spinnerMixRatio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMixRatio = parent.getItemAtPosition(position).toString();
                loadMainExperimentTypes(currentMixRatio);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerMainExperimentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                currentMainExperimentType = adapter.getItem(position);  // 直接从适配器获取原始值
                loadSubExperimentTypes(currentMixRatio, currentMainExperimentType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSubExperimentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                currentSubExperimentType = adapter.getItem(position);  // 直接从适配器获取原始值
                loadExperimentData(currentMixRatio, currentMainExperimentType, currentSubExperimentType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupChart() {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // 设置X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // 将float值转换为时间戳，然后格式化
                long timestamp = (long) value;
                return dateFormat.format(new Date(timestamp));
            }
        });

        // 设置左Y轴
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        // 禁用右Y轴
        lineChart.getAxisRight().setEnabled(false);

        // 设置图例
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
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
                spinnerMainExperimentType.setVisibility(View.GONE);
                spinnerSubExperimentType.setVisibility(View.GONE);
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
                loadMainExperimentTypes(currentMixRatio);
            }
        });
    }

    private void loadMainExperimentTypes(String mixRatio) {
        new Thread(() -> {
            try {
                Log.d("ExperimentAnalysis", "开始加载主实验类型，配比: " + mixRatio);

                // 获取该配比下的所有实验数据
                List<ExperimentData> experimentDataList = experimentDataDao.getExperimentDataByMixRatio(mixRatio);
                Log.d("ExperimentAnalysis", "从数据库获取到 " + experimentDataList.size() + " 条实验数据");

                // 使用TreeMap来保存主实验类型和其对应的参数类型
                Map<String, Set<String>> experimentParamMap = new TreeMap<>();

                for (ExperimentData data : experimentDataList) {
                    String experimentName = data.getExperimentName();
                    Log.d("ExperimentAnalysis", "处理实验数据：" + experimentName);

                    if (experimentName != null) {
                        // 获取主实验类型
                        String mainType = extractMainExperimentType(experimentName);
                        Log.d("ExperimentAnalysis", "提取的主实验类型: " + mainType);

                        if (mainType != null && !mainType.isEmpty()) {
                            // 将参数类型添加到对应的主类型集合中
                            experimentParamMap.computeIfAbsent(mainType, k -> new HashSet<>());
                        }
                    }
                }

                // 创建实验参数类型列表
                List<String> mainTypes = new ArrayList<>(experimentParamMap.keySet());
                Log.d("ExperimentAnalysis", "最终获取到的主实验类型列表: " + mainTypes);

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        mainTypes
                    ) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            String item = getItem(position);
                            // 显示参数类型名称
                            textView.setText(getDisplayName(item));
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            String item = getItem(position);
                            // 显示参数类型名称
                            textView.setText(getDisplayName(item));
                            return view;
                        }
                    };

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMainExperimentType.setAdapter(adapter);

                    // 如果有数据，选择第一项
                    if (!mainTypes.isEmpty()) {
                        spinnerMainExperimentType.setSelection(0);
                        currentMainExperimentType = mainTypes.get(0);
                        loadSubExperimentTypes(currentMixRatio, currentMainExperimentType);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSubExperimentTypes(String mixRatio, String mainType) {
        Log.d("ExperimentAnalysis", "开始加载实验子类型，配比: " + mixRatio + ", 主类型: " + mainType);

        new Thread(() -> {
            try {
                // 获取该配比下的所有实验数据
                List<ExperimentData> experimentDataList = experimentDataDao.getExperimentDataByMixRatio(mixRatio);
                Log.d("ExperimentAnalysis", "从数据库获取到 " + experimentDataList.size() + " 条实验数据");

                // 使用TreeMap来保存主实验类型和其对应的参数类型
                Map<String, Set<String>> experimentParamMap = new TreeMap<>();

                for (ExperimentData data : experimentDataList) {
                    String experimentName = data.getExperimentName();
                    Log.d("ExperimentAnalysis", "处理实验数据：" + experimentName);

                    if (experimentName != null) {
                        // 获取主实验类型
                        String currentMainType = extractMainExperimentType(experimentName);
                        Log.d("ExperimentAnalysis", "提取的主实验类型: " + currentMainType);

                        if (mainType.equals(currentMainType)) {
                            // 提取实验参数类型
                            String paramType = extractParamType(experimentName);
                            Log.d("ExperimentAnalysis", "提取的参数类型: " + paramType);

                            if (paramType != null) {
                                // 将参数类型添加到对应的主类型集合中
                                experimentParamMap.computeIfAbsent(mainType, k -> new HashSet<>())
                                              .add(paramType);
                                Log.d("ExperimentAnalysis", "添加参数类型: " + paramType + " 到主类型: " + mainType);
                            }
                        }
                    }
                }

                // 创建实验参数类型列表
                Set<String> paramTypes = experimentParamMap.getOrDefault(mainType, new HashSet<>());
                List<String> displayParamTypes = new ArrayList<>();

                for (String paramType : paramTypes) {
                    String displayName = getDisplayName(paramType);
                    displayParamTypes.add(displayName);
                    Log.d("ExperimentAnalysis", "添加显示名称: " + displayName + " (原参数类型: " + paramType + ")");
                }

                Log.d("ExperimentAnalysis", "最终获取到的参数类型列表: " + displayParamTypes);

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        RealExperimentAnalysisActivity.this,
                        android.R.layout.simple_spinner_item,
                        displayParamTypes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSubExperimentType.setAdapter(adapter);

                    Log.d("ExperimentAnalysis", "更新子类型下拉框，选项数量: " + adapter.getCount());
                });

            } catch (Exception e) {
                Log.e("ExperimentAnalysis", "加载实验子类型时出错", e);
            }
        }).start();
    }

    private String extractParamType(String experimentName) {
        // 从实验名称中提取参数类型
        if (experimentName == null) return null;

        Log.d("ExperimentAnalysis", "提取参数类型，实验名称: " + experimentName);

        // 马歇尔实验参数
        if (experimentName.contains("_stability_")) {
            Log.d("ExperimentAnalysis", "识别为稳定度参数");
            return "stability";
        }
        if (experimentName.contains("_flow_")) {
            Log.d("ExperimentAnalysis", "识别为流值参数");
            return "flow";
        }

        // 车辙实验参数 - 所有车辙实验数据都返回 sip 类型
        if (experimentName.contains("车辙") && !experimentName.contains("_sip")) {
            Log.d("ExperimentAnalysis", "识别为车辙实验原始数据，返回 sip 参数");
            return "sip";
        }

        // 沥青实验参数
        if (experimentName.contains("_penetration_")) {
            Log.d("ExperimentAnalysis", "识别为针入度参数");
            return "penetration";
        }
        if (experimentName.contains("_softening_point_")) {
            Log.d("ExperimentAnalysis", "识别为软化点参数");
            return "softening_point";
        }
        if (experimentName.contains("_ductility_")) {
            Log.d("ExperimentAnalysis", "识别为延度参数");
            return "ductility";
        }

        Log.d("ExperimentAnalysis", "未能识别参数类型");
        return null;
    }

    private String getDisplayName(String paramType) {
        // 将参数类型转换为显示名称
        switch (paramType) {
            // 马歇尔实验参数
            case "stability":
                return "稳定度";
            case "flow":
                return "流值";

            // 车辙实验参数
            case "sip":
                return "剥离拐点(SIP)";

            // 沥青实验参数
            case "penetration":
                return "针入度";
            case "softening_point":
                return "软化点";
            case "ductility":
                return "延度";
            default:
                return paramType;
        }
    }

    private double calculateSIP(ExperimentData data) {
        try {
            // 提取第一、第二稳态截距和斜率
            double firstIntercept = 0.0;
            double secondIntercept = 0.0;
            double firstSlope = 0.0;
            double secondSlope = 0.0;

            String experimentName = data.getExperimentName();
            String mixRatio = data.getMixRatio();

            // 获取所有相关数据
            List<ExperimentData> allData = experimentDataDao.getExperimentDataByMixRatio(mixRatio);

            for (ExperimentData d : allData) {
                String name = d.getExperimentName();
                if (name == null) continue;

                if (name.contains("_first_intercept")) {
                    firstIntercept = d.getInput1Value();
                } else if (name.contains("_second_intercept")) {
                    secondIntercept = d.getInput1Value();
                } else if (name.contains("_first_slope")) {
                    firstSlope = d.getInput1Value();
                } else if (name.contains("_second_slope")) {
                    secondSlope = d.getInput1Value();
                }
            }

            // 计算 SIP
            // SIP = (第二稳态截距 - 第一稳态截距) / (第一稳态斜率 - 第二稳态斜率)
            if (Math.abs(firstSlope - secondSlope) > 0.0001) {  // 避免除以零
                return (secondIntercept - firstIntercept) / (firstSlope - secondSlope);
            }

            return 0.0;
        } catch (Exception e) {
            Log.e("ExperimentAnalysis", "计算 SIP 时出错: " + e.getMessage());
            return 0.0;
        }
    }

    private String extractMainExperimentType(String experimentName) {
        if (experimentName == null) return "";

        Log.d("ExperimentAnalysis", "开始提取主实验类型，实验名称: " + experimentName);

        if (experimentName.contains("马歇尔")) {
            Log.d("ExperimentAnalysis", "识别为马歇尔实验");
            return "马歇尔实验";
        }
        if (experimentName.contains("车辙")) {
            Log.d("ExperimentAnalysis", "识别为车辙实验");
            return "车辙实验";
        }
        if (experimentName.contains("针入度") || experimentName.contains("软化点") || experimentName.contains("延度")) {
            Log.d("ExperimentAnalysis", "识别为沥青实验");
            return "沥青实验";
        }

        Log.d("ExperimentAnalysis", "未能识别实验类型");
        return "";
    }

    private String extractSubType(String experimentName) {
        // 提取实验数据的序号（如从"稳定度1"提取"1"）
        if (experimentName == null) return "";

        // 使用正则表达式匹配数字
        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(experimentName);

        if (matcher.find()) {
            return matcher.group();
        }

        return "1"; // 如果没有找到序号，默认返回"1"
    }

    private void loadExperimentData(String mixRatio, String mainExperimentType, String subExperimentType) {
        Log.d("ExperimentAnalysis", "加载实验数据：");
        Log.d("ExperimentAnalysis", "配合比: " + mixRatio);
        Log.d("ExperimentAnalysis", "主实验类型: " + mainExperimentType);
        Log.d("ExperimentAnalysis", "子实验类型: " + subExperimentType);

        if (mixRatio == null || mainExperimentType == null || subExperimentType == null) {
            Log.e("ExperimentAnalysis", "参数不完整，无法加载数据");
            return;
        }

        // 根据选择的实验类型和子类型获取数据
        if ("马歇尔实验".equals(mainExperimentType)) {
            Log.d("ExperimentAnalysis", "开始查询马歇尔实验数据");

            // 添加SQL调试日志
            Log.d("ExperimentAnalysis", "SQL查询参数：");
            Log.d("ExperimentAnalysis", "实验名称模式: 马歇尔稳定度试验_" + subExperimentType);
            Log.d("ExperimentAnalysis", "配合比: " + mixRatio);

            database.experimentDataDao()
                .getMarshallDataByType(mixRatio, subExperimentType)
                .observe(this, experimentDataList -> {
                    Log.d("ExperimentAnalysis", "查询结果返回，数据条数: " +
                          (experimentDataList != null ? experimentDataList.size() : 0));

                    if (experimentDataList != null && !experimentDataList.isEmpty()) {
                        List<Entry> entries = new ArrayList<>();
                        for (int i = 0; i < experimentDataList.size(); i++) {
                            ExperimentData data = experimentDataList.get(i);
                            Log.d("ExperimentAnalysis", "处理第 " + (i+1) + " 条数据:");
                            Log.d("ExperimentAnalysis", "实验名称: " + data.getExperimentName());
                            Log.d("ExperimentAnalysis", "配合比: " + data.getMixRatio());
                            Log.d("ExperimentAnalysis", "输入标签1: " + data.getInput1Label());
                            Log.d("ExperimentAnalysis", "输入值1: " + data.getInput1Value());

                            float value = (float) data.getInput1Value();
                            entries.add(new Entry(i + 1, value));
                        }
                        Log.d("ExperimentAnalysis", "生成图表数据点数: " + entries.size());
                        // 更新图表
                        updateChartWithData(entries);
                    } else {
                        Log.w("ExperimentAnalysis", "没有找到符合条件的数据");
                        // 清空图表
                        lineChart.clear();
                        lineChart.invalidate();
                    }
                });
        } else if ("车辙实验".equals(mainExperimentType)) {
            Log.d("ExperimentAnalysis", "开始查询车辙实验数据");

            // 清除旧的观察者
            if (currentRuttingDataObserver != null) {
                currentRuttingDataObserver.removeObservers(this);
            }

            // 添加SQL调试日志
            Log.d("ExperimentAnalysis", "SQL查询参数：");
            Log.d("ExperimentAnalysis", "配合比: " + mixRatio);

            // 保存新的 LiveData 引用
            currentRuttingDataObserver = database.experimentDataDao().getRuttingDataByType(mixRatio);

            // 观察数据变化
            currentRuttingDataObserver.observe(this, new Observer<List<ExperimentData>>() {
                @Override
                public void onChanged(List<ExperimentData> experimentDataList) {
                    Log.d("ExperimentAnalysis", "收到车辙实验数据更新，数据条数: " +
                          (experimentDataList != null ? experimentDataList.size() : "null"));

                    if (experimentDataList != null && !experimentDataList.isEmpty()) {
                        Log.d("ExperimentAnalysis", "查询到 " + experimentDataList.size() + " 条车辙实验数据");

                        // 打印所有实验数据的名称和值
                        for (ExperimentData data : experimentDataList) {
                            Log.d("ExperimentAnalysis", String.format("实验数据: 名称=%s, 值=%.2f, 标签=%s",
                                data.getExperimentName(),
                                data.getInput1Value(),
                                data.getInput1Label()));
                        }

                        // 获取所有相关数据
                        Map<String, Double> dataMap = new HashMap<>();
                        for (ExperimentData data : experimentDataList) {
                            String name = data.getExperimentName();
                            if (name == null) continue;

                            double value = data.getInput1Value();
                            if (name.contains("_first_intercept")) {
                                dataMap.put("first_intercept", value);
                                Log.d("ExperimentAnalysis", "找到第一截距: " + value);
                            } else if (name.contains("_second_intercept")) {
                                dataMap.put("second_intercept", value);
                                Log.d("ExperimentAnalysis", "找到第二截距: " + value);
                            } else if (name.contains("_first_slope")) {
                                dataMap.put("first_slope", value);
                                Log.d("ExperimentAnalysis", "找到第一斜率: " + value);
                            } else if (name.contains("_second_slope")) {
                                dataMap.put("second_slope", value);
                                Log.d("ExperimentAnalysis", "找到第二斜率: " + value);
                            }
                        }

                        Log.d("ExperimentAnalysis", "收集到的数据点数量: " + dataMap.size());

                        // 如果所有必需的数据都存在，计算 SIP
                        if (dataMap.size() == 4) {
                            double firstIntercept = dataMap.get("first_intercept");
                            double secondIntercept = dataMap.get("second_intercept");
                            double firstSlope = dataMap.get("first_slope");
                            double secondSlope = dataMap.get("second_slope");

                            Log.d("ExperimentAnalysis", String.format(
                                "计算 SIP 值使用的数据: \n" +
                                "第一截距: %.2f\n" +
                                "第二截距: %.2f\n" +
                                "第一斜率: %.2f\n" +
                                "第二斜率: %.2f",
                                firstIntercept, secondIntercept, firstSlope, secondSlope));

                            // 计算 SIP
                            if (Math.abs(firstSlope - secondSlope) > 0.0001) {  // 避免除以零
                                double sip = (secondIntercept - firstIntercept) / (firstSlope - secondSlope);
                                List<Entry> entries = new ArrayList<>();
                                entries.add(new Entry(1, (float) sip));
                                Log.d("ExperimentAnalysis", "计算得到的 SIP 值: " + sip);

                                updateChartWithData(entries);
                                Log.d("ExperimentAnalysis", "更新图表，SIP 值: " + sip);
                            } else {
                                Log.w("ExperimentAnalysis", "斜率差值过小，无法计算 SIP");
                                clearChart();
                            }
                        } else {
                            Log.w("ExperimentAnalysis", "缺少计算 SIP 所需的数据，当前只有 " + dataMap.size() + " 个数据点");
                            // 打印缺少哪些数据
                            if (!dataMap.containsKey("first_intercept")) Log.w("ExperimentAnalysis", "缺少第一截距");
                            if (!dataMap.containsKey("second_intercept")) Log.w("ExperimentAnalysis", "缺少第二截距");
                            if (!dataMap.containsKey("first_slope")) Log.w("ExperimentAnalysis", "缺少第一斜率");
                            if (!dataMap.containsKey("second_slope")) Log.w("ExperimentAnalysis", "缺少第二斜率");
                            clearChart();
                        }
                    } else {
                        Log.w("ExperimentAnalysis", "没有找到符合条件的数据");
                        clearChart();
                    }
                }
            });
        } else {
            Log.w("ExperimentAnalysis", "不支持的实验类型: " + mainExperimentType);
            clearChart();
        }
    }

    private void clearChart() {
        lineChart.clear();
        lineChart.invalidate();
    }

    private void updateChartWithData(List<Entry> entries) {
        if (entries.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        // 创建数据集
        LineDataSet dataSet;
        if (lineChart.getData() != null &&
            lineChart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            dataSet = new LineDataSet(entries, "SIP值变化");
            dataSet.setDrawIcons(false);
            dataSet.setColor(Color.BLUE);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(true);
            dataSet.setFormLineWidth(1f);
            dataSet.setFormSize(15.f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            // 使用渐变填充
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                dataSet.setFillDrawable(drawable);
            } else {
                dataSet.setFillColor(Color.BLUE);
            }

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
        }

        // 启用拖动和缩放手势
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // 刷新图表
        lineChart.invalidate();
    }

    private List<String> getXLabels(List<Entry> entries) {
        List<String> xLabels = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            xLabels.add(String.valueOf((int) entries.get(i).getX()));
        }
        return xLabels;
    }
}
