package com.example.labdata_main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.CompletedExperimentAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentAnalysisActivity extends AppCompatActivity {
    private static final String TAG = "ExperimentAnalysis";
    
    private MaterialButton btnStartDate, btnEndDate, btnSearch;
    private AutoCompleteTextView spinnerMachine, spinnerExperimentType, spinnerExperimenter;
    private RecyclerView recyclerViewResults;
    private CompletedExperimentAdapter adapter;
    
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private ExecutorService executor;
    
    private long startDate = 0;
    private long endDate = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_analysis);

        // 初始化工具类
        database = AppDatabase.getInstance(this);
        sharedPrefsManager = new SharedPrefsManager(this);
        executor = Executors.newSingleThreadExecutor();

        // 初始化视图
        initViews();
        // 设置工具栏
        setupToolbar();
        // 设置日期选择器
        setupDatePickers();
        // 加载筛选选项
        loadFilterOptions();
        // 设置搜索按钮
        setupSearchButton();
    }

    private void initViews() {
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSearch = findViewById(R.id.btnSearch);
        spinnerMachine = findViewById(R.id.spinnerMachine);
        spinnerExperimentType = findViewById(R.id.realspinnerExperimentType);
        spinnerExperimenter = findViewById(R.id.spinnerExperimenter);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompletedExperimentAdapter(this);
        recyclerViewResults.setAdapter(adapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            startDate = calendar.getTimeInMillis();
            btnStartDate.setText(dateFormat.format(new Date(startDate)));
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            endDate = calendar.getTimeInMillis();
            btnEndDate.setText(dateFormat.format(new Date(endDate)));
        };

        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this, startDateListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        btnEndDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this, endDateListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void loadFilterOptions() {
        executor.execute(() -> {
            try {
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e(TAG, "Company ID is null");
                    return;
                }

                // 获取所有已完成的实验任务
                List<ExperimentTask> tasks = database.experimentTaskDao()
                        .getCompletedTasksByCompany(companyId);

                Set<String> experimentTypes = new HashSet<>();
                Set<String> machines = new HashSet<>();
                Set<String> experimenters = new HashSet<>();

                for (ExperimentTask task : tasks) {
                    // 获取实验人
                    if (task.getExperimenter() != null && !task.getExperimenter().isEmpty()) {
                        experimenters.add(task.getExperimenter());
                    }

                    // 获取实验类型和机器信息
                    List<ExperimentData> dataList = database.experimentDataDao()
                            .getExperimentDataByTaskId(task.getId());
                    
                    for (ExperimentData data : dataList) {
                        // 添加主实验类型
                        if (data.getExperimentName() != null) {
                            String mainExperimentType = extractMainExperimentType(data.getExperimentName());
                            if (!mainExperimentType.isEmpty()) {
                                experimentTypes.add(mainExperimentType);
                            }
                        }

                        // 添加机器信息
                        String deviceInfo = data.getDeviceManufacturer() + " " + data.getDeviceModel();
                        if (!deviceInfo.trim().equals("null null")) {
                            machines.add(deviceInfo);
                        }
                    }
                }

                // 转换为排序列表
                List<String> sortedExperimentTypes = new ArrayList<>(experimentTypes);
                List<String> sortedMachines = new ArrayList<>(machines);
                List<String> sortedExperimenters = new ArrayList<>(experimenters);

                Collections.sort(sortedExperimentTypes);
                Collections.sort(sortedMachines);
                Collections.sort(sortedExperimenters);

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    // 设置实验类型下拉列表
                    ArrayAdapter<String> experimentTypeAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            sortedExperimentTypes
                    );
                    spinnerExperimentType.setAdapter(experimentTypeAdapter);

                    // 设置机器下拉列表
                    ArrayAdapter<String> machineAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            sortedMachines
                    );
                    spinnerMachine.setAdapter(machineAdapter);

                    // 设置实验人下拉列表
                    ArrayAdapter<String> experimenterAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            sortedExperimenters
                    );
                    spinnerExperimenter.setAdapter(experimenterAdapter);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading filter options", e);
                runOnUiThread(() -> 
                    Toast.makeText(this, "加载筛选选项出错: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String extractMainExperimentType(String experimentName) {
        if (experimentName == null) return "";
        
        // 马歇尔稳定度实验
        if (experimentName.contains("马歇尔稳定度")) {
            return "马歇尔实验";
        }
        // 车辙实验
        if (experimentName.contains("车辙")) {
            return "车辙实验";
        }
        // 其他实验类型可以继续添加...
        
        return experimentName;
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            String selectedMachine = spinnerMachine.getText().toString();
            String selectedType = spinnerExperimentType.getText().toString();
            String selectedExperimenter = spinnerExperimenter.getText().toString();

            // 验证时间范围
            if (startDate == 0 || endDate == 0) {
                Toast.makeText(this, "请选择完整的时间范围", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDate < startDate) {
                Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                return;
            }

            // 执行搜索
            searchExperiments(startDate, endDate, selectedMachine, selectedType, selectedExperimenter);
        });
    }

    private void searchExperiments(long startDate, long endDate, String machine, String type, String experimenter) {
        executor.execute(() -> {
            try {
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e(TAG, "Company ID is null");
                    return;
                }

                Log.d(TAG, "Searching experiments with criteria - Start: " + startDate + ", End: " + endDate +
                        ", Machine: " + machine + ", Type: " + type + ", Experimenter: " + experimenter);

                // 获取时间范围内的已完成任务
                List<ExperimentTask> tasks = database.experimentTaskDao()
                        .getCompletedTasksByTimeRange(companyId, startDate, endDate);
                Log.d(TAG, "Found " + tasks.size() + " tasks in time range");
                
                List<CompletedExperimentAdapter.TaskWithData> filteredTasks = new ArrayList<>();

                for (ExperimentTask task : tasks) {
                    // 检查实验人筛选条件
                    if (!experimenter.isEmpty() && !experimenter.equals(task.getExperimenter())) {
                        continue;
                    }

                    List<ExperimentData> dataList = database.experimentDataDao()
                            .getExperimentDataByTaskId(task.getId());
                    
                    // 检查实验类型和机器筛选条件
                    boolean matchesType = type.isEmpty();
                    boolean matchesMachine = machine.isEmpty();
                    
                    for (ExperimentData data : dataList) {
                        // 检查实验类型
                        if (!type.isEmpty() && data.getExperimentName() != null) {
                            String mainExperimentType = extractMainExperimentType(data.getExperimentName());
                            if (type.equals(mainExperimentType)) {
                                matchesType = true;
                            }
                        }
                        
                        // 检查机器
                        String deviceInfo = data.getDeviceManufacturer() + " " + data.getDeviceModel();
                        if (!machine.isEmpty() && machine.equals(deviceInfo)) {
                            matchesMachine = true;
                        }

                        // 如果已经找到匹配的类型和机器，可以提前退出循环
                        if (matchesType && matchesMachine) {
                            break;
                        }
                    }

                    // 如果满足所有筛选条件，添加到结果列表
                    if (matchesType && matchesMachine) {
                        Log.d(TAG, "Adding matched task: " + task.getTaskId());
                        filteredTasks.add(new CompletedExperimentAdapter.TaskWithData(task, dataList));
                    }
                }

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    adapter.setTasks(filteredTasks);
                    if (filteredTasks.isEmpty()) {
                        Toast.makeText(this, "未找到符合条件的实验数据", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Found " + filteredTasks.size() + " matching tasks");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error searching experiments", e);
                runOnUiThread(() -> 
                    Toast.makeText(this, "搜索出错: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
