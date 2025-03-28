package com.example.labdata_main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.labdata_main.adapter.ExperimentTaskAdapter;
import com.example.labdata_main.adapter.ProjectCardAdapter;
import com.example.labdata_main.adapter.AsphaltProjectCardAdapter;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.AsphaltTaskResponse;
import com.example.labdata_main.api.model.MixtureTaskResponse;
import com.example.labdata_main.api.service.AsphaltTaskService;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.fragment.BottomSheetAsphaltTaskDetailFragment;
import com.example.labdata_main.fragment.BottomSheetMakeSpecimenFragment;
import com.example.labdata_main.fragment.BottomSheetMixRatioDetailFragment;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.utils.ApiClient;
import com.example.labdata_main.utils.NetworkUtil;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import okhttp3.Request;

import com.google.gson.Gson;

public class OverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, 
        ExperimentTaskAdapter.OnTaskClickListener, 
        AsphaltProjectCardAdapter.OnAsphaltTaskActionListener,
        TaskDetailBottomSheet.TaskAcceptListener, BottomSheetMixRatioDetailFragment.OnMaterialCompletedListener {
    private static final String TAG = "OverviewFragment";
    
    private TextView welcomeText;
    private TextView greetingText;
    private TextView mixtureTaskText;
    private TextView asphaltTaskText;
    private Spinner spinner;
    private RecyclerView taskRecyclerView;
    private RecyclerView myTasksRecyclerView;
    private TextView emptyTaskText;
    private TextView emptyMyTaskText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExperimentTaskAdapter taskAdapter;
    private ProjectCardAdapter mixtureTaskAdapter;
    private AsphaltProjectCardAdapter asphaltTaskAdapter;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private ExecutorService executorService;
    private BroadcastReceiver taskRefreshReceiver;
    private MixtureTaskService mixtureTaskService;
    private AsphaltTaskService asphaltTaskService;
    
    // 添加成员变量存储最近从API获取的任务
    private List<ExperimentTask> apiMixtureUnacceptedTasks = new ArrayList<>();
    private List<ExperimentTask> apiMixtureAcceptedTasks = new ArrayList<>();
    private List<ExperimentTask> apiAsphaltUnacceptedTasks = new ArrayList<>();
    private List<ExperimentTask> apiAsphaltAcceptedTasks = new ArrayList<>();
    private boolean hasLoadedApiData = false;

    // 添加ActivityResultLauncher，用于处理从实验数据录入界面返回的结果
    private ActivityResultLauncher<Intent> recordExperimentLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            // 当返回结果为RESULT_OK时，表示实验已完成或状态已更新，需要刷新数据
            if (result.getResultCode() == Activity.RESULT_OK) {
                Log.d(TAG, "从实验数据录入界面返回，刷新任务列表");
                refreshData();
            }
        }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(requireContext());
        sharedPrefsManager = new SharedPrefsManager(requireContext());
        executorService = Executors.newFixedThreadPool(4);

        // 注册广播接收器
        taskRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.labdata_main.REFRESH_TASKS".equals(intent.getAction())) {
                    loadExperimentTasks();
                }
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(taskRefreshReceiver, 
                new IntentFilter("com.example.labdata_main.REFRESH_TASKS"),
                android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(taskRefreshReceiver, 
                new IntentFilter("com.example.labdata_main.REFRESH_TASKS"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview, container, false);

        // 初始化视图
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        // 初始化所有视图引用
        welcomeText = view.findViewById(R.id.welcomeText);
        greetingText = view.findViewById(R.id.greetingText);
        mixtureTaskText = view.findViewById(R.id.mixtureTaskText);
        asphaltTaskText = view.findViewById(R.id.asphaltTaskText);
        spinner = view.findViewById(R.id.spinner);
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        myTasksRecyclerView = view.findViewById(R.id.rvTasks);
        emptyTaskText = view.findViewById(R.id.empty_task_text);
        emptyMyTaskText = view.findViewById(R.id.empty_my_task_text);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // 初始化数据库和SharedPrefs
        database = AppDatabase.getInstance(requireContext());
        sharedPrefsManager = new SharedPrefsManager(requireContext());
        
        // 初始化API服务 - 确保使用包含认证拦截器的ApiClient
        // 首先确保ApiClient已初始化
        ApiClient.init(requireContext());
        mixtureTaskService = ApiClient.getClient().create(MixtureTaskService.class);
        asphaltTaskService = ApiClient.getClient().create(AsphaltTaskService.class);

        // 设置下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // 初始化按钮点击事件
        LinearLayout btnAssignExperiment = view.findViewById(R.id.btnAssignExperiment);
        LinearLayout btnAssignAsphaltExperiment = view.findViewById(R.id.btnAssignAsphaltExperiment);
        LinearLayout btnAddMixRatio = view.findViewById(R.id.btnAddMixRatio);

        if (btnAssignExperiment != null) {
            btnAssignExperiment.setOnClickListener(v -> showAddExperimentDialog());
        }

        if (btnAssignAsphaltExperiment != null) {
            btnAssignAsphaltExperiment.setOnClickListener(v -> showAddAsphaltTaskDialog());
        }

        if (btnAddMixRatio != null) {
            btnAddMixRatio.setOnClickListener(v -> {
                MixRatioBottomSheetFragment bottomSheet = MixRatioBottomSheetFragment.newInstance();
                bottomSheet.show(getChildFragmentManager(), "MixRatioBottomSheet");
            });
        }

        // 设置RecyclerView
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置适配器
        taskAdapter = new ExperimentTaskAdapter(this);
        taskRecyclerView.setAdapter(taskAdapter);

        // 初始化混合料任务适配器
        mixtureTaskAdapter = new ProjectCardAdapter();
        mixtureTaskAdapter.setOnProjectCardActionListener(new ProjectCardAdapter.OnProjectCardActionListener() {
            @Override
            public void onViewMixRatios(ExperimentTask task) {
                showMixtureExperimentInfo(task);
            }

            @Override
            public void onViewSpecimenCode(ExperimentTask task) {
                // 不需要实现
            }

            @Override
            public void onGenerateSpecimenCode(ExperimentTask task) {
                // 不需要实现
            }

            @Override
            public void onRecordExperimentData(ExperimentTask task) {
                startMixtureExperiment(task);
            }

            @Override
            public void onTaskUpdated(ExperimentTask task) {
                // 不再从数据库重新加载，而是直接从API获取最新数据
                refreshData();
            }
        });

        // 初始化沥青任务适配器
        asphaltTaskAdapter = new AsphaltProjectCardAdapter();
        asphaltTaskAdapter.setOnAsphaltTaskActionListener(this);

        // 设置下拉框
        setupSpinner();

        // 注册广播接收器
        registerTaskRefreshReceiver();

        // 加载实验任务
        loadExperimentTasks();
        
        // 更新欢迎区域
        updateWelcomeSection();
    }

    private void setupSpinner() {
        spinner.setOnItemSelectedListener(this);
        
        // 设置默认选中项
        String[] experimentTypes = getResources().getStringArray(R.array.experiment_types);
        if (experimentTypes.length > 0) {
            spinner.setSelection(0);
        }
    }

    private void registerTaskRefreshReceiver() {
        IntentFilter filter = new IntentFilter("com.example.labdata_main.TASK_UPDATED");
        taskRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 直接从API刷新数据，而不是从本地数据库加载
                refreshData();
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(taskRefreshReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(taskRefreshReceiver, filter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (taskRefreshReceiver != null) {
            requireActivity().unregisterReceiver(taskRefreshReceiver);
            taskRefreshReceiver = null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getInstance(requireContext());
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.example.labdata_main.TASK_UPDATED");
        taskRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshData();
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(taskRefreshReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(taskRefreshReceiver, filter);
        }

        // 设置Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.experiment_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        loadExperimentTasks();
        
        // 测试API连接状态
        testApiConnection();
    }

    private void refreshData() {
        // 显示刷新指示器
        swipeRefreshLayout.setRefreshing(true);
        
        // 检查网络连接状态
        if (!NetworkUtil.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "网络连接不可用", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            showEmptyState("ALL");
            return;
        }
        
        // 检查用户是否已登录
        if (!sharedPrefsManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            showEmptyState("ALL");
            return;
        }
        
        // 重置API客户端，确保使用最新的认证信息
        ApiClient.resetClient();
        ApiClient.init(requireContext());
        
        // 获取当前选中的实验类型
        String selectedType = spinner.getSelectedItem().toString();
        fetchDataFromApi(selectedType);
    }
    
    private void fetchDataFromApi(String selectedType) {
        // 根据选中的实验类型获取对应的数据
        if ("沥青混合料试验".equals(selectedType)) {
            fetchMixtureTasks();
        } else if ("沥青试验".equals(selectedType)) {
            fetchAsphaltTasks();
        } else {
            // 默认获取混合料任务
            fetchMixtureTasks();
        }
    }
    
    private void fetchMixtureTasks() {
        Log.d(TAG, "开始获取混合料任务");
        String companyId = sharedPrefsManager.getUserCompany();
        Call<ApiResponse<List<MixtureTaskResponse>>> call = mixtureTaskService.getUserMixtureTasks(companyId);
        
        // 打印请求详情
        Request request = call.request();
        Log.d(TAG, "混合料任务请求URL: " + request.url());
        Log.d(TAG, "混合料任务请求方法: " + request.method());
        Log.d(TAG, "混合料任务请求头: " + request.headers());
        
        call.enqueue(new Callback<ApiResponse<List<MixtureTaskResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixtureTaskResponse>>> call, Response<ApiResponse<List<MixtureTaskResponse>>> response) {
                Log.d(TAG, "混合料任务响应码: " + response.code());
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "混合料任务获取成功");
                    
                    // 获取任务列表
                    List<MixtureTaskResponse> taskResponses = response.body().getData();
                    Log.d(TAG, "获取到 " + (taskResponses != null ? taskResponses.size() : 0) + " 个混合料任务");
                    
                    if (taskResponses != null && !taskResponses.isEmpty()) {
                        // 清空旧数据，确保使用最新数据
                        apiMixtureUnacceptedTasks.clear();
                        apiMixtureAcceptedTasks.clear();
                        
                        // 处理任务响应
                        handleMixtureTaskResponse(taskResponses);
                    } else {
                        Log.d(TAG, "没有混合料任务");
                        // 清空旧数据并显示空状态
                        apiMixtureUnacceptedTasks.clear();
                        apiMixtureAcceptedTasks.clear();
                        showEmptyState("MIXTURE");
                    }
                } else {
                    // 请求失败
                    int code = response.code();
                    String message = "";
                    try {
                        if (response.errorBody() != null) {
                            message = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "获取错误消息失败", e);
                    }
                    
                    Log.e(TAG, "获取混合料任务失败: " + code + ", " + message);
                    showEmptyState("MIXTURE");
                    Toast.makeText(requireContext(), "获取混合料任务失败: " + code, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<MixtureTaskResponse>>> call, Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                swipeRefreshLayout.setRefreshing(false);
                showEmptyState("MIXTURE");
                Toast.makeText(requireContext(), "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void fetchAsphaltTasks() {
        Log.d(TAG, "开始获取沥青任务");
        Call<ApiResponse<List<AsphaltTaskResponse>>> call = asphaltTaskService.getUserAsphaltTasks(sharedPrefsManager.getUserCompany());
        
        // 打印请求详情
        Request request = call.request();
        Log.d(TAG, "请求URL: " + request.url());
        Log.d(TAG, "请求方法: " + request.method());
        Log.d(TAG, "请求头: " + request.headers());
        
        call.enqueue(new Callback<ApiResponse<List<AsphaltTaskResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AsphaltTaskResponse>>> call, Response<ApiResponse<List<AsphaltTaskResponse>>> response) {
                // 停止刷新动画
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "沥青任务获取成功");
                    
                    // 获取任务列表
                    List<AsphaltTaskResponse> taskResponses = response.body().getData();
                    Log.d(TAG, "获取到 " + (taskResponses != null ? taskResponses.size() : 0) + " 个沥青任务");
                    
                    // 打印原始API响应数据，用于诊断
                    if (taskResponses != null) {
                        Log.d(TAG, "API响应原始JSON: " + new Gson().toJson(taskResponses));
                        
                        for (int i = 0; i < taskResponses.size(); i++) {
                            AsphaltTaskResponse task = taskResponses.get(i);
                            Log.d(TAG, "任务 " + i + " 详情：");
                            Log.d(TAG, "  实验ID: " + task.getAsphaltExperimentId());
                            Log.d(TAG, "  实验名称: " + task.getAsphaltExperimentName());
                            Log.d(TAG, "  任务名称: " + task.getAsphaltTaskName());
                            Log.d(TAG, "  任务状态: " + task.getTaskStatus());
                            Log.d(TAG, "  状态: " + task.getStatus());
                            Log.d(TAG, "  公司ID: " + task.getCompanyId());
                        }
                    }
                    
                    if (taskResponses != null && !taskResponses.isEmpty()) {
                        // 清空旧数据，确保使用最新数据
                        apiAsphaltUnacceptedTasks.clear();
                        apiAsphaltAcceptedTasks.clear();
                        
                        // 处理任务响应
                        processAsphaltTasks(taskResponses);
                    } else {
                        Log.d(TAG, "没有沥青任务");
                        // 清空旧数据并显示空状态
                        apiAsphaltUnacceptedTasks.clear();
                        apiAsphaltAcceptedTasks.clear();
                        showEmptyState("ASPHALT");
                    }
                } else {
                    // 记录错误
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "读取错误响应失败", e);
                    }
                    
                    Log.e(TAG, "获取沥青任务失败, 状态码: " + statusCode + ", 错误信息: " + errorBody);
                    showEmptyState("ASPHALT");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<AsphaltTaskResponse>>> call, Throwable t) {
                // 停止刷新动画
                swipeRefreshLayout.setRefreshing(false);
                
                Log.e(TAG, "获取沥青任务请求失败", t);
                showEmptyState("ASPHALT");
            }
        });
    }
    
    private void handleMixtureTaskResponse(List<MixtureTaskResponse> tasks) {
        List<ExperimentTask> unacceptedTasks = new ArrayList<>();
        List<ExperimentTask> acceptedTasks = new ArrayList<>();
        
        // 用于跟踪已处理的task_id基础部分，避免重复
        Set<String> processedBaseTaskIds = new HashSet<>();
        
        // 用于跟踪任务组前缀及其状态 - key是基础taskId前缀，value是该组所有任务是否都已完成
        Map<String, Boolean> taskPrefixStatusMap = new HashMap<>();
        
        // 用于统计每个任务组的任务总数和各状态已完成任务数
        Map<String, Integer> taskPrefixTotalCount = new HashMap<>();
        Map<String, Integer> taskPrefixTestingFinishedCount = new HashMap<>();
        Map<String, Boolean> taskPrefixPrepareFinishedMap = new HashMap<>();
        Map<String, Boolean> taskPrefixMakingFinishedMap = new HashMap<>();
        
        // 获取当前用户的公司ID
        String userCompanyId = sharedPrefsManager.getUserCompany();
        
        // 第一遍：统计每个任务组的任务数量和已完成任务数量
        for (MixtureTaskResponse mixtureTask : tasks) {
            // 跳过非本单位的任务
            if (!userCompanyId.equals(mixtureTask.getTaskCompany())){
                continue;
            }
            
            String taskId = mixtureTask.getTaskId();
            
            // 提取基础UUID部分（去掉最后的"-数字"后缀）
            String baseTaskId = taskId;
            int lastDashIndex = taskId.lastIndexOf("-");
            if (lastDashIndex > 0) {
                baseTaskId = taskId.substring(0, lastDashIndex);
            }
            
            // 更新该前缀的任务总数
            taskPrefixTotalCount.put(baseTaskId, taskPrefixTotalCount.getOrDefault(baseTaskId, 0) + 1);
            
            // 检查任务的三种状态（大小写不敏感）
            String testingStatus = mixtureTask.getTestingStatus();
            String prepareStatus = mixtureTask.getPrepareStatus();
            String makingStatus = mixtureTask.getMakingStatus();
            
            Log.d(TAG, "任务ID: " + taskId + ", 前缀: " + baseTaskId + 
                  ", 准备状态: " + prepareStatus + 
                  ", 制件状态: " + makingStatus + 
                  ", 测试状态: " + testingStatus);
            
            // 检查测试状态是否为"finished"
            boolean isTestingFinished = testingStatus != null && testingStatus.toLowerCase().equals("finished");
            
            // 如果测试状态已完成，更新已完成测试任务计数
            if (isTestingFinished) {
                taskPrefixTestingFinishedCount.put(baseTaskId, taskPrefixTestingFinishedCount.getOrDefault(baseTaskId, 0) + 1);
                Log.d(TAG, "任务测试已完成: " + taskId + ", 前缀: " + baseTaskId);
            }
            
            // 检查准备状态是否为"finished"并更新映射
            boolean isPrepareFinished = prepareStatus != null && prepareStatus.toLowerCase().equals("finished");
            if (isPrepareFinished) {
                taskPrefixPrepareFinishedMap.put(baseTaskId, true);
                Log.d(TAG, "任务准备已完成: " + taskId + ", 前缀: " + baseTaskId);
            } else if (!taskPrefixPrepareFinishedMap.containsKey(baseTaskId)) {
                // 如果没有记录过，或者之前记录为false，则设置为false
                taskPrefixPrepareFinishedMap.put(baseTaskId, false);
            }
            
            // 检查制件状态是否为"finished"并更新映射
            boolean isMakingFinished = makingStatus != null && makingStatus.toLowerCase().equals("finished");
            if (isMakingFinished) {
                taskPrefixMakingFinishedMap.put(baseTaskId, true);
                Log.d(TAG, "任务制件已完成: " + taskId + ", 前缀: " + baseTaskId);
            } else if (!taskPrefixMakingFinishedMap.containsKey(baseTaskId)) {
                // 如果没有记录过，或者之前记录为false，则设置为false
                taskPrefixMakingFinishedMap.put(baseTaskId, false);
            }
        }
        
        // 确定每个任务组是否全部完成（三个状态都为finished）
        for (String baseTaskId : taskPrefixTotalCount.keySet()) {
            int totalCount = taskPrefixTotalCount.get(baseTaskId);
            int testingFinishedCount = taskPrefixTestingFinishedCount.getOrDefault(baseTaskId, 0);
            boolean prepareFinished = taskPrefixPrepareFinishedMap.getOrDefault(baseTaskId, false);
            boolean makingFinished = taskPrefixMakingFinishedMap.getOrDefault(baseTaskId, false);
            
            // 只有当所有任务的测试状态都已完成，并且准备状态和制件状态也都完成时，才认为任务组已完成
            boolean allFinished = (totalCount > 0) && 
                                 (testingFinishedCount == totalCount) && 
                                 prepareFinished && 
                                 makingFinished;
            
            taskPrefixStatusMap.put(baseTaskId, allFinished);
            
            Log.d(TAG, "任务组前缀: " + baseTaskId + 
                  ", 总任务数: " + totalCount + 
                  ", 已完成测试任务数: " + testingFinishedCount + 
                  ", 准备已完成: " + prepareFinished +
                  ", 制件已完成: " + makingFinished +
                  ", 是否全部完成: " + allFinished);
        }
        
        // 第二遍：处理任务，按照原有逻辑，但跳过全部完成的任务组
        for (MixtureTaskResponse mixtureTask : tasks) {
            // 跳过非本单位的任务
            if (!userCompanyId.equals(mixtureTask.getTaskCompany())){
                continue;
            }
            
            String taskId = mixtureTask.getTaskId();
            
            // 提取基础UUID部分（去掉最后的"-数字"后缀）
            String baseTaskId = taskId;
            int lastDashIndex = taskId.lastIndexOf("-");
            if (lastDashIndex > 0) {
                baseTaskId = taskId.substring(0, lastDashIndex);
            }
            
            // 检查当前任务前缀是否所有任务都已完成（包括三个状态）
            boolean allTasksFinished = taskPrefixStatusMap.getOrDefault(baseTaskId, false);
            
            // 如果所有任务都已完成（三个状态都为finished），则跳过此任务
            if (allTasksFinished) {
                Log.d(TAG, "跳过已完成任务组（所有状态均已完成），前缀: " + baseTaskId + ", 完整ID: " + taskId);
                continue;
            }
            
            // 如果这个基础task_id已经处理过，则跳过（保留原有的去重逻辑）
            if (processedBaseTaskIds.contains(baseTaskId)) {
                Log.d(TAG, "跳过重复任务，基础ID: " + baseTaskId + ", 完整ID: " + taskId + ", 任务名称: " + mixtureTask.getTaskName());
                continue;
            }
            
            // 记录这个基础task_id已经处理
            processedBaseTaskIds.add(baseTaskId);
            
            Log.d(TAG, "处理混合料任务: " + mixtureTask.getTaskName() + ", 状态: " + mixtureTask.getStatus() + ", ID: " + taskId + ", 基础ID: " + baseTaskId);
            
            // 创建任务对象
            ExperimentTask task = convertMixtureTaskToExperimentTask(mixtureTask);
            
            // 处理状态
            String status = mixtureTask.getStatus();
            Log.d(TAG, "混合料任务原始状态: " + status + " 对应任务: " + task.getTaskName());
            
            if (status != null && (status.equals("PROCESSING") || status.equals("ONGOING"))) {
                Log.d(TAG, "设置状态为'已接受'");
                task.setStatus("已接受");
                acceptedTasks.add(task);
                Log.d(TAG, "添加到已接受任务: " + task.getTaskName());
            } else if (status != null && status.equals("CREATED")) {
                Log.d(TAG, "设置状态为'未接受'");
                task.setStatus("未接受");
                unacceptedTasks.add(task);
                Log.d(TAG, "添加到未接受任务: " + task.getTaskName());
            } else if (status != null && status.equals("COMPLETE")) {
                // 已完成的任务不添加到任何列表
                Log.d(TAG, "任务已完成，不添加到任何列表: " + task.getTaskName());
            } else {
                // 默认处理为未接受
                Log.d(TAG, "未知状态，默认设置为'未接受': " + status);
                task.setStatus("未接受");
                unacceptedTasks.add(task);
                Log.d(TAG, "添加到未接受任务: " + task.getTaskName());
            }
        }
        
        Log.d(TAG, "共转换 " + unacceptedTasks.size() + " 个未接受任务和 " + acceptedTasks.size() + " 个已接受任务");
        
        // 保存到成员变量
        apiMixtureUnacceptedTasks.clear();
        apiMixtureUnacceptedTasks.addAll(unacceptedTasks);
        apiMixtureAcceptedTasks.clear();
        apiMixtureAcceptedTasks.addAll(acceptedTasks);
        hasLoadedApiData = true;
        
        // 更新UI显示
        updateTaskUI(unacceptedTasks, acceptedTasks, "MIXTURE");
    }
    
    private void handleAsphaltTaskResponse(List<AsphaltTaskResponse> tasks) {
        Log.d(TAG, "开始处理沥青任务响应");
        
        // 清空现有集合
        List<ExperimentTask> unacceptedTasks = new ArrayList<>();
        List<ExperimentTask> acceptedTasks = new ArrayList<>();
        
        // 用于跟踪已处理的assignment_id，避免重复
        Set<String> processedAssignmentIds = new HashSet<>();
        
        // 用于跟踪每个任务组（按asphalt_task_name分组）的完成状态
        Map<String, Integer> taskNameTotalCount = new HashMap<>();
        Map<String, Integer> taskNameFinishedCount = new HashMap<>();
        Map<String, Boolean> taskNameCompletionStatus = new HashMap<>();
        
        // 第一遍：扫描所有任务，统计每个任务组（按asphalt_task_name分组）的完成情况
        for (AsphaltTaskResponse asphaltTask : tasks) {
            String taskName = asphaltTask.getAsphaltTaskName();
            if (taskName == null || taskName.trim().isEmpty()) {
                continue; // 跳过没有任务名称的任务
            }
            
            // 更新该任务名称组的任务总数
            taskNameTotalCount.put(taskName, taskNameTotalCount.getOrDefault(taskName, 0) + 1);
            
            // 检查实验状态（大小写不敏感）
            String experimentStatus = asphaltTask.getExperimentStatus();
            Log.d(TAG, "任务名称: " + taskName + ", 实验状态: " + experimentStatus);
            
            // 大小写不敏感地检查状态是否为"finished"
            boolean isFinished = experimentStatus != null && experimentStatus.toLowerCase().equals("finished");
            
            // 如果实验已完成，更新已完成实验计数
            if (isFinished) {
                taskNameFinishedCount.put(taskName, taskNameFinishedCount.getOrDefault(taskName, 0) + 1);
                Log.d(TAG, "实验已完成: " + taskName);
            }
        }
        
        // 确定每个任务组是否全部完成
        for (String taskName : taskNameTotalCount.keySet()) {
            int totalCount = taskNameTotalCount.get(taskName);
            int finishedCount = taskNameFinishedCount.getOrDefault(taskName, 0);
            
            // 如果所有实验都已完成，标记该组为完成状态
            boolean allFinished = (totalCount > 0) && (finishedCount == totalCount);
            taskNameCompletionStatus.put(taskName, allFinished);
            
            Log.d(TAG, "任务组: " + taskName + 
                  ", 总任务数: " + totalCount + 
                  ", 已完成任务数: " + finishedCount + 
                  ", 是否全部完成: " + allFinished);
        }
        
        // 处理每个任务，按照原有逻辑但跳过已完成的任务组
        for (AsphaltTaskResponse asphaltTask : tasks) {
            String taskName = asphaltTask.getAsphaltTaskName();
            if (taskName == null || taskName.trim().isEmpty()) {
                continue; // 跳过没有任务名称的任务
            }
            
            // 检查该任务所属组是否全部完成，如果是，则跳过
            boolean isTaskGroupCompleted = taskNameCompletionStatus.getOrDefault(taskName, false);
            if (isTaskGroupCompleted) {
                Log.d(TAG, "跳过已完成任务组: " + taskName);
                continue;
            }
            
            String assignmentId = asphaltTask.getAsphaltTaskAssignmentId();
            
            // 如果没有分配ID，则使用实验ID作为唯一标识
            if (assignmentId == null || assignmentId.trim().isEmpty()) {
                assignmentId = String.valueOf(asphaltTask.getAsphaltExperimentId());
                Log.d(TAG, "任务没有分配ID，使用实验ID作为唯一标识: " + assignmentId);
            }
            
            // 如果这个assignment_id已经处理过，则跳过
            if (processedAssignmentIds.contains(assignmentId)) {
                Log.d(TAG, "跳过重复任务，分配ID: " + assignmentId + ", 任务名称: " + asphaltTask.getAsphaltTaskName());
                continue;
            }
            
            // 记录这个assignment_id已经处理
            processedAssignmentIds.add(assignmentId);
            
            Log.d(TAG, "处理沥青任务: " + asphaltTask.getAsphaltTaskName() + ", 状态: " + asphaltTask.getStatus() + ", 分配ID: " + assignmentId + ", taskStatus=" + asphaltTask.getTaskStatus());
            
            // 直接跳过已完成的任务
            if ("COMPLETED".equalsIgnoreCase(asphaltTask.getTaskStatus())) {
                Log.d(TAG, "直接跳过已完成任务: " + asphaltTask.getAsphaltTaskName() + ", taskStatus=" + asphaltTask.getTaskStatus());
                continue;
            }
            
            // 转换为通用实验任务模型
            ExperimentTask task = convertAsphaltTaskToExperimentTask(asphaltTask);
            
            // 根据状态分类 - 优先使用 taskStatus，其次使用 status
            String taskStatus = asphaltTask.getTaskStatus();
            String status = asphaltTask.getStatus();
            
            // 如果 taskStatus 为 ONGOING 或者 status 为 ONGOING，则添加到已接受任务
            if ("ONGOING".equalsIgnoreCase(taskStatus) || 
                "ACCEPTED".equalsIgnoreCase(taskStatus) || 
                "PROCESSING".equalsIgnoreCase(taskStatus) ||
                "ONGOING".equalsIgnoreCase(status)) {
                acceptedTasks.add(task);
                Log.d(TAG, "添加到已接受任务: " + task.getTaskName());
            } 
            // 如果是 CREATED 状态，则添加到未接受任务
            else if ("CREATED".equalsIgnoreCase(taskStatus)) {
                unacceptedTasks.add(task);
                Log.d(TAG, "添加到未接受任务: " + task.getTaskName());
            } 
            // 默认情况，添加到未接受任务
            else {
                unacceptedTasks.add(task);
                Log.d(TAG, "状态未知，默认添加到未接受任务: " + task.getTaskName());
            }
        }
        
        // 更新集合
        apiAsphaltUnacceptedTasks.clear();  // 先清空，避免添加重复数据
        apiAsphaltUnacceptedTasks.addAll(unacceptedTasks);
        apiAsphaltAcceptedTasks.clear();  // 先清空，避免添加重复数据
        apiAsphaltAcceptedTasks.addAll(acceptedTasks);
        
        Log.d(TAG, "共转换 " + unacceptedTasks.size() + " 个未接受任务和 " + acceptedTasks.size() + " 个已接受任务");
        
        // 更新UI
        updateTaskUI(unacceptedTasks, acceptedTasks, "ASPHALT");
    }
    
    private ExperimentTask convertMixtureTaskToExperimentTask(MixtureTaskResponse mixtureTask) {
        ExperimentTask task = new ExperimentTask();
        
        task.setId(mixtureTask.getId() != null ? mixtureTask.getId() : 0);
        task.setTaskId(mixtureTask.getTaskId() != null ? mixtureTask.getTaskId() : "未知ID");
        
        // 设置任务名称，如果为空，则使用默认名称
        String taskName = mixtureTask.getTaskName();
        if (taskName == null || taskName.isEmpty()) {
            taskName = "混合料任务-" + mixtureTask.getId();
            Log.d(TAG, "使用默认名称: " + taskName);
        }
        task.setTaskName(taskName);
        
        task.setExperimentType("MIXTURE");
        task.setStatus(mixtureTask.getStatus() != null ? 
            mixtureTask.getStatus().equals("PROCESSING") ? "已接受" : "未接受" : "未接受");
        task.setCompanyId(String.valueOf(mixtureTask.getTaskCompany()));
        
        // 设置截止日期 - 如果dueDate不为空，尝试将其转换为时间戳
        if (mixtureTask.getDueDate() != null && !mixtureTask.getDueDate().isEmpty()) {
            try {
                // 尝试解析日期字符串为日期对象，然后获取时间戳
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(mixtureTask.getDueDate());
                if (date != null) {
                    task.setDeadline(date.getTime());
                    Log.d(TAG, "设置混合料任务截止日期: " + mixtureTask.getDueDate() + " -> " + date.getTime());
                }
            } catch (Exception e) {
                Log.e(TAG, "解析混合料任务截止日期时出错: " + mixtureTask.getDueDate(), e);
                // 如果解析失败，设置为0（表示无截止日期）
                task.setDeadline(0);
            }
        } else {
            // 如果没有截止日期，设置为0
            task.setDeadline(0);
        }
        
        return task;
    }
    
    private ExperimentTask convertAsphaltTaskToExperimentTask(AsphaltTaskResponse asphaltTask) {
        // 创建新的实验任务对象
        ExperimentTask task = new ExperimentTask();
        
        // 设置基本字段
        task.setId(asphaltTask.getAsphaltExperimentId() != null ? asphaltTask.getAsphaltExperimentId() : 0);
        
        // 设置任务ID和任务分配ID
        // 注意：首先设置标准格式的任务ID作为taskId
        String generatedTaskId = generateTaskId();
        task.setTaskId(generatedTaskId);
        
        // 设置UUID格式的任务分配ID，用于API查询
        // 前端需要保存这个UUID以便后续API调用
        task.setTaskAssignmentId(asphaltTask.getAsphaltTaskAssignmentId());
        
        // 任务名称处理 - 首先尝试使用任务名称，如果为空则尝试使用实验名称，都为空则使用默认名称
        // 注意：这里做了多重判断，防止空指针
        String taskName = null;
        if (asphaltTask.getAsphaltTaskName() != null && !asphaltTask.getAsphaltTaskName().isEmpty()) {
            taskName = asphaltTask.getAsphaltTaskName();
            Log.d(TAG, "使用任务名称: " + taskName);
        } else if (asphaltTask.getAsphaltExperimentName() != null && !asphaltTask.getAsphaltExperimentName().isEmpty()) {
            taskName = asphaltTask.getAsphaltExperimentName();
            Log.d(TAG, "使用实验名称作为任务名称: " + taskName);
        } else {
            taskName = "沥青任务-" + asphaltTask.getAsphaltExperimentId();
            Log.d(TAG, "使用默认名称: " + taskName);
        }
        task.setTaskName(taskName);
        
        task.setExperimentType("ASPHALT");
        
        // 设置公司ID (假设在当前上下文中不直接可用，使用当前用户的公司ID)
        task.setCompanyId(sharedPrefsManager.getUserCompany()); // 设置公司ID

        // 设置任务状态 - 优先使用 taskStatus
        String taskStatus = asphaltTask.getTaskStatus();
        String statusField = asphaltTask.getStatus();
        
        Log.d(TAG, "沥青任务状态解析：taskStatus=" + taskStatus + ", status=" + statusField + ", 任务=" + task.getTaskName());
        
        // 优先使用 taskStatus 字段
        if (taskStatus != null && !taskStatus.isEmpty()) {
            Log.d(TAG, "使用 taskStatus 字段值：" + taskStatus);
            
            if ("ONGOING".equalsIgnoreCase(taskStatus)) {
                task.setStatus("ONGOING");
                Log.d(TAG, "👉设置沥青任务状态为 ONGOING (来自taskStatus)");
            } else if ("CREATED".equalsIgnoreCase(taskStatus)) {
                task.setStatus("CREATED");
                Log.d(TAG, "设置沥青任务状态为 CREATED (来自taskStatus)");
            } else if ("COMPLETED".equalsIgnoreCase(taskStatus)) {
                task.setStatus("COMPLETED");
                Log.d(TAG, "设置沥青任务状态为 COMPLETED (来自taskStatus)");
            } else {
                // 如果 taskStatus 不是已知状态，则使用 status 字段
                if (statusField != null && !statusField.isEmpty()) {
                    if ("ONGOING".equalsIgnoreCase(statusField)) {
                        task.setStatus("ONGOING");
                        Log.d(TAG, "设置沥青任务状态为 ONGOING (来自status)");
                    } else {
                        task.setStatus("CREATED");
                        Log.d(TAG, "设置沥青任务状态为默认 CREATED");
                    }
                } else {
                    task.setStatus("CREATED");
                    Log.d(TAG, "设置沥青任务状态为默认 CREATED (字段为空)");
                }
            }
        } else if (statusField != null && !statusField.isEmpty()) {
            // 如果 taskStatus 为空，则使用 status 字段
            Log.d(TAG, "使用 status 字段值：" + statusField);
            
            if ("ONGOING".equalsIgnoreCase(statusField)) {
                task.setStatus("ONGOING");
                Log.d(TAG, "设置沥青任务状态为 ONGOING (来自status)");
            } else if ("CREATED".equalsIgnoreCase(statusField) || "PENDING".equalsIgnoreCase(statusField)) {
                task.setStatus("CREATED");
                Log.d(TAG, "设置沥青任务状态为 CREATED (来自status)");
            } else if ("COMPLETED".equalsIgnoreCase(statusField)) {
                task.setStatus("COMPLETED");
                Log.d(TAG, "设置沥青任务状态为 COMPLETED (来自status)");
            } else {
                task.setStatus("CREATED");
                Log.d(TAG, "设置沥青任务状态为默认 CREATED (未知status值)");
            }
        } else {
            // 如果两个字段都为空，默认为未接受
            task.setStatus("CREATED");
            Log.d(TAG, "设置沥青任务状态为默认 CREATED (字段为空)");
        }
        
        // 设置截止日期 - 如果dueDate不为空，尝试将其转换为时间戳
        if (asphaltTask.getDueDate() != null && !asphaltTask.getDueDate().isEmpty()) {
            try {
                // 尝试解析日期字符串为日期对象，然后获取时间戳
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(asphaltTask.getDueDate());
                if (date != null) {
                    task.setDeadline(date.getTime());
                    Log.d(TAG, "设置沥青任务截止日期: " + asphaltTask.getDueDate() + " -> " + date.getTime());
                }
            } catch (Exception e) {
                Log.e(TAG, "解析沥青任务截止日期时出错: " + asphaltTask.getDueDate(), e);
                // 如果解析失败，设置为0（表示无截止日期）
                task.setDeadline(0);
            }
        } else {
            // 如果没有截止日期，设置为0
            task.setDeadline(0);
        }
        
        return task;
    }
    
    private String generateTaskId() {
        return UUID.randomUUID().toString();
    }
    
    private void updateTaskUI(List<ExperimentTask> unacceptedTasks, List<ExperimentTask> acceptedTasks, String experimentType) {
        Log.d(TAG, "开始更新UI: " + experimentType + " 类型, " + 
              unacceptedTasks.size() + " 个未接受任务, " + 
              acceptedTasks.size() + " 个已接受任务");
        
        requireActivity().runOnUiThread(() -> {
            try {
                // 更新欢迎区域信息
                updateWelcomeSection();
                
                // 更新未接受任务列表
                if (unacceptedTasks.isEmpty()) {
                    emptyTaskText.setVisibility(View.VISIBLE);
                    emptyTaskText.setText("暂无实验任务");
                    taskRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyTaskText.setVisibility(View.GONE);
                    taskRecyclerView.setVisibility(View.VISIBLE);
                    
                    // 确保Adapter已初始化
                    if (taskAdapter == null) {
                        Log.w(TAG, "任务适配器为空，重新初始化");
                        taskAdapter = new ExperimentTaskAdapter(this);
                        taskRecyclerView.setAdapter(taskAdapter);
                    }
                    
                    // 设置未接受任务
                    taskAdapter.setTasks(unacceptedTasks);
                    taskAdapter.notifyDataSetChanged();
                    Log.d(TAG, "已更新任务适配器，通知数据集变化");
                }
                
                // 选择适当的适配器
                RecyclerView.Adapter<?> currentAdapter = null;
                if ("MIXTURE".equals(experimentType)) {
                    Log.d(TAG, "使用混合料任务适配器");
                    myTasksRecyclerView.setAdapter(mixtureTaskAdapter);
                    currentAdapter = mixtureTaskAdapter;
                } else if ("ASPHALT".equals(experimentType)) {
                    Log.d(TAG, "使用沥青任务适配器");
                    myTasksRecyclerView.setAdapter(asphaltTaskAdapter);
                    currentAdapter = asphaltTaskAdapter;
                }
                
                // 更新已接受任务列表
                if (acceptedTasks.isEmpty()) {
                    emptyMyTaskText.setVisibility(View.VISIBLE);
                    emptyMyTaskText.setText("暂无进行中的任务");
                    myTasksRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyMyTaskText.setVisibility(View.GONE);
                    myTasksRecyclerView.setVisibility(View.VISIBLE);
                    
                    if (currentAdapter != null) {
                        if (currentAdapter instanceof ProjectCardAdapter) {
                            ((ProjectCardAdapter) currentAdapter).setTasks(acceptedTasks);
                            Log.d(TAG, "已更新ProjectCardAdapter");
                        } else if (currentAdapter instanceof AsphaltProjectCardAdapter) {
                            ((AsphaltProjectCardAdapter) currentAdapter).setTasks(acceptedTasks);
                            Log.d(TAG, "已更新AsphaltProjectCardAdapter");
                        }
                    } else {
                        Log.e(TAG, "当前适配器为空，无法设置已接受任务");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "更新UI时发生错误", e);
            }
        });
    }

    private void loadExperimentTasks() {
        // 如果已经从API加载了数据，则直接使用
        if (hasLoadedApiData) {
            // 获取当前选中的实验类型
            String selectedType = spinner.getSelectedItem().toString();
            
            if ("沥青混合料试验".equals(selectedType)) {
                updateTaskUI(apiMixtureUnacceptedTasks, apiMixtureAcceptedTasks, "MIXTURE");
            } else if ("沥青试验".equals(selectedType)) {
                updateTaskUI(apiAsphaltUnacceptedTasks, apiAsphaltAcceptedTasks, "ASPHALT");
            } else {
                // 默认使用混合料任务数据
                updateTaskUI(apiMixtureUnacceptedTasks, apiMixtureAcceptedTasks, "MIXTURE");
            }
            return;
        }
        
        // 如果尚未从API加载数据，尝试从服务器获取
        refreshData();
    }

    private void showAddExperimentDialog() {
        AddExperimentBottomSheet bottomSheet = AddExperimentBottomSheet.newInstance();
        bottomSheet.setOnExperimentNameSubmitListener(experimentName -> {
            // 直接跳转到实验设置页面，在那里创建任务
            ExperimentTaskSetupActivity.start(requireContext(), experimentName);
        });
        bottomSheet.show(getChildFragmentManager(), "bottom_sheet_add_experiment");
    }

    private void showAddAsphaltTaskDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_name_asphalt_task_name, null);
        dialog.setContentView(dialogView);

        TextInputEditText editTextAsphaltTaskName = dialogView.findViewById(R.id.editTextAsphaltTaskName);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmAsphaltTaskName);

        btnConfirm.setOnClickListener(v -> {
            String taskName = editTextAsphaltTaskName.getText().toString().trim();
            if (!taskName.isEmpty()) {
                // 创建新的实验任务
                ExperimentTask task = new ExperimentTask();
                task.setTaskName(taskName);
                task.setStatus("未接受");
                task.setExperimentType("ASPHALT"); // 设置为沥青实验类型
                task.setCompanyId(sharedPrefsManager.getUserCompany()); // 设置公司ID

                // 保存到数据库并跳转
                executorService.execute(() -> {
                    // 保存到数据库
                    AppDatabase.getInstance(requireContext()).experimentTaskDao().insert(task);

                    // 在主线程更新UI和跳转
                    requireActivity().runOnUiThread(() -> {
                        loadExperimentTasks(); // 刷新任务列表
                        dialog.dismiss();
                        AsphaltExperimentTaskSetupActivity.start(requireContext(), taskName);
                    });
                });
            }
        });

        dialog.show();
    }

    private void filterTasksByType(String type) {
        Log.d(TAG, "按类型过滤任务: " + type);
        
        // 如果已经从API加载了数据，直接使用内存中的数据
        if (hasLoadedApiData) {
            if ("MIXTURE".equals(type)) {
                updateTaskUI(apiMixtureUnacceptedTasks, apiMixtureAcceptedTasks, type);
            } else if ("ASPHALT".equals(type)) {
                updateTaskUI(apiAsphaltUnacceptedTasks, apiAsphaltAcceptedTasks, type);
            }
            return;
        }
        
        // 如果尚未从API加载数据，尝试从服务器获取
        refreshData();
    }

    @Override
    public void onTaskClick(ExperimentTask task) {
        if ("ASPHALT".equals(task.getExperimentType())) {
            // 沥青实验特有处理
            showDetailBottomSheet(task);
        } else {
            // 混合料实验处理
            showMixtureExperimentInfo(task);
        }
    }

    // 处理混合料任务的接受
    private void handleMixtureTaskAccepted(ExperimentTask task) {
        Log.d(TAG, "处理混合料任务接受: " + task.getTaskName() + ", ID: " + task.getTaskId());
        
        // 更新任务状态为"已接受"
        task.setStatus("已接受");
        
        // 设置实验人员为当前登录用户
        String currentUser = sharedPrefsManager.getUserName();
        if (currentUser != null && !currentUser.isEmpty()) {
            task.setExperimenter(currentUser);
            Log.d(TAG, "设置实验人员: " + currentUser);
        }
        
        // 从未接受列表中移除任务
        for (Iterator<ExperimentTask> iterator = apiMixtureUnacceptedTasks.iterator(); iterator.hasNext();) {
            ExperimentTask t = iterator.next();
            if (t.getTaskId().equals(task.getTaskId())) {
                iterator.remove();
                Log.d(TAG, "从未接受列表中移除任务: " + t.getTaskName());
                break;
            }
        }
        
        // 添加到已接受列表
        boolean alreadyInList = false;
        for (ExperimentTask t : apiMixtureAcceptedTasks) {
            if (t.getTaskId().equals(task.getTaskId())) {
                alreadyInList = true;
                t.setStatus("已接受");
                t.setExperimenter(task.getExperimenter());
                Log.d(TAG, "更新已接受列表中的任务: " + t.getTaskName());
                break;
            }
        }
        
        if (!alreadyInList) {
            apiMixtureAcceptedTasks.add(task);
            Log.d(TAG, "添加到已接受列表: " + task.getTaskName());
        }
        
        // 更新UI显示
        updateTaskUI(apiMixtureUnacceptedTasks, apiMixtureAcceptedTasks, "MIXTURE");
        
        // 显示成功提示
        Toast.makeText(requireContext(), "成功接受任务：" + task.getTaskName(), Toast.LENGTH_SHORT).show();
    }

    // 处理沥青任务的接受
    public void onAcceptTask(ExperimentTask task) {
        // 获取当前登录用户
        String currentUser = sharedPrefsManager.getUserName();
        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(requireContext(), "未登录用户无法接受任务", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取任务分配ID，如果为空则使用任务ID
        String taskId = task.getTaskAssignmentId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = task.getTaskId();
            Log.w(TAG, "任务缺少分配ID，使用任务ID代替: " + taskId);
        }
        
        // 调用API接受任务
        asphaltTaskService.acceptAsphaltTask(
                taskId, 
                currentUser, 
                System.currentTimeMillis()
        ).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 显示成功提示
                    Toast.makeText(requireContext(), "成功接受任务：" + task.getTaskName(), Toast.LENGTH_SHORT).show();
                    
                    // 重新从后端获取最新的任务列表
                    fetchAsphaltTasks();
                } else {
                    // 获取错误信息
                    String errorMsg = "接受任务失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // 处理沥青任务的详情查看
    public void onViewTaskDetails(ExperimentTask task) {
        showAsphaltTaskDetail(task);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedType = parent.getItemAtPosition(position).toString();
        Log.d(TAG, "选择的实验类型: " + selectedType);
        
        // 如果已经从API加载了数据，直接使用内存中的数据
        if (hasLoadedApiData) {
            if ("沥青混合料试验".equals(selectedType)) {
                Log.d(TAG, "使用内存中的混合料任务数据");
                updateTaskUI(apiMixtureUnacceptedTasks, apiMixtureAcceptedTasks, "MIXTURE");
            } else if ("沥青试验".equals(selectedType)) {
                Log.d(TAG, "使用内存中的沥青任务数据");
                updateTaskUI(apiAsphaltUnacceptedTasks, apiAsphaltAcceptedTasks, "ASPHALT");
            }
        } else {
            // 如果尚未从API加载数据，尝试从服务器获取
            refreshData();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskRefreshReceiver != null) {
            requireContext().unregisterReceiver(taskRefreshReceiver);
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExperimentTasks();
    }

    // 实现 AsphaltProjectCardAdapter.OnAsphaltTaskActionListener 接口方法
    @Override
    public void onViewAsphaltInfo(ExperimentTask task) {
        showDetailBottomSheet(task);
    }
    
    @Override
    public void onRecordData(ExperimentTask task) {
        startAsphaltExperiment(task);
    }
    
    private void showAsphaltTaskDetail(ExperimentTask task) {
        // 确保传递的任务对象包含UUID格式的任务分配ID
        if (task.getTaskAssignmentId() == null || task.getTaskAssignmentId().isEmpty()) {
            Log.w(TAG, "任务缺少UUID格式的任务分配ID，使用任务ID: " + task.getTaskId());
        } else {
            Log.d(TAG, "使用UUID格式的任务分配ID: " + task.getTaskAssignmentId());
        }
        
        // 直接使用传入的任务对象显示底部弹窗
        BottomSheetAsphaltTaskDetailFragment bottomSheet = 
                BottomSheetAsphaltTaskDetailFragment.newInstance(task, task.getStatus().equals("CREATED"));
        
        bottomSheet.setOnTaskActionListener(new BottomSheetAsphaltTaskDetailFragment.OnTaskActionListener() {
            @Override
            public void onTaskAccepted(ExperimentTask acceptedTask) {
                // 使用 onAcceptTask 方法更新任务状态和UI
                onAcceptTask(acceptedTask);
            }
            
            @Override
            public void onTaskRejected(ExperimentTask rejectedTask) {
                // 拒绝任务后直接刷新数据 
                refreshData();
            }
            
            @Override
            public void onTaskCompleted(ExperimentTask completedTask) {
                // 完成任务后直接刷新数据
                refreshData();
            }
        });
        
        bottomSheet.show(getChildFragmentManager(), "asphalt_task_detail");
        
        // 添加任务分配ID的完整日志记录
        Log.d(TAG, "任务分配ID: " + task.getTaskAssignmentId());
        Log.d(TAG, "任务ID: " + task.getTaskId());
        Log.d(TAG, "实验类型: " + task.getExperimentType());
        Log.d(TAG, "公司ID: " + task.getCompanyId());
        Log.d(TAG, "任务状态: " + task.getStatus());
        Log.d(TAG, "截止日期: " + task.getDeadline());
    }

    private void showDetailBottomSheet(ExperimentTask task) {
        // 确保传递的任务对象包含UUID格式的任务分配ID
        if (task.getTaskAssignmentId() == null || task.getTaskAssignmentId().isEmpty()) {
            Log.w(TAG, "任务缺少UUID格式的任务分配ID，使用任务ID: " + task.getTaskId());
        } else {
            Log.d(TAG, "使用UUID格式的任务分配ID: " + task.getTaskAssignmentId());
        }
        
        // 使用传入的任务对象创建底部弹窗
        BottomSheetAsphaltTaskDetailFragment bottomSheet = 
                BottomSheetAsphaltTaskDetailFragment.newInstance(task, task.getStatus().equals("CREATED"));
        
        bottomSheet.setOnTaskActionListener(new BottomSheetAsphaltTaskDetailFragment.OnTaskActionListener() {
            @Override
            public void onTaskAccepted(ExperimentTask acceptedTask) {
                // 使用 onAcceptTask 方法更新任务状态和UI
                onAcceptTask(acceptedTask);
            }
            
            @Override
            public void onTaskRejected(ExperimentTask rejectedTask) {
                // 拒绝任务后直接刷新数据
                refreshData();
            }
            
            @Override
            public void onTaskCompleted(ExperimentTask completedTask) {
                // 完成任务后直接刷新数据
                refreshData();
            }
        });
        
        bottomSheet.show(getChildFragmentManager(), "asphalt_task_detail");
    }

    private void updateLocalTaskStatus(ExperimentTask task) {
        executorService.execute(() -> {
            database.experimentTaskDao().update(task);
            requireActivity().runOnUiThread(() -> {
                loadExperimentTasks();
            });
        });
    }

    private void startAsphaltExperiment(ExperimentTask task) {
        // 直接使用传入的任务信息启动实验数据记录活动
        Intent intent = new Intent(requireContext(), RecordExperimentDataActivity.class);
        
        // 首先检查任务是否有UUID格式的任务分配ID
        if (task.getTaskAssignmentId() != null && !task.getTaskAssignmentId().isEmpty()) {
            // 使用任务分配ID
            Log.d(TAG, "使用UUID任务分配ID启动实验: " + task.getTaskAssignmentId());
            intent.putExtra("taskId", task.getTaskAssignmentId());
        } else {
            // 回退到使用任务ID
            Log.w(TAG, "UUID任务分配ID缺失，回退使用任务ID: " + task.getTaskId());
            intent.putExtra("taskId", task.getTaskId());
        }
        
        intent.putExtra("experiment_type", "ASPHALT");
        recordExperimentLauncher.launch(intent);
    }
    
    // 显示混合料实验信息
    private void showMixtureExperimentInfo(ExperimentTask task) {
        // 检查任务状态，决定显示哪个底部表单
        if (task.getStatus() != null && 
            (task.getStatus().equals("已接受") || task.getStatus().equals("ONGOING"))) {
            // 对于已接受的任务，显示配比详情底部表单
            BottomSheetMixRatioDetailFragment bottomSheet = BottomSheetMixRatioDetailFragment.newInstance(task);
            
            // 设置材料准备完成监听器，当前类已经实现接口
            bottomSheet.setOnMaterialCompletedListener(this);
            
            bottomSheet.show(getChildFragmentManager(), "mixratio_detail_bottom_sheet");
        } else {
            // 对于未接受的任务，显示任务详情底部表单
            TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task);
            
            // 设置任务接受监听器
            bottomSheet.setTaskAcceptListener(this);
            
            bottomSheet.show(getChildFragmentManager(), "task_detail_bottom_sheet");
        }
    }

    private void startMixtureExperiment(ExperimentTask task) {
        Intent intent = new Intent(requireContext(), RecordExperimentDataActivity.class);
        intent.putExtra("taskId", task.getTaskId());  // 这里也需要修改为 "taskId"
        intent.putExtra("experiment_type", "MIXTURE");
        recordExperimentLauncher.launch(intent);
    }

    /**
     * 测试服务器连接和API访问
     */
    private void testApiConnection() {
        // 确保ApiClient已正确初始化
        ApiClient.init(requireContext());
        
        // 获取Token信息并显示详细调试信息
        Log.d(TAG, "当前认证令牌详情：");
        Log.d(TAG, "令牌值: " + sharedPrefsManager.getAuthToken());
        Log.d(TAG, "令牌类型: " + sharedPrefsManager.getTokenType());
        Log.d(TAG, "认证头: " + sharedPrefsManager.getAuthHeader());
        
        // 测试实际API端点而不是根路径
        Toast.makeText(requireContext(), "正在测试API连接...", Toast.LENGTH_SHORT).show();
        
        // 使用包含认证拦截器的ApiClient
        ApiClient.resetClient();
        MixtureTaskService mixtureService = ApiClient.getClient().create(MixtureTaskService.class);
        
        // 使用混合料实验API端点测试认证，避免使用可能有问题的沥青API
        String companyId = sharedPrefsManager.getUserCompany();
        Call<ApiResponse<List<MixtureTaskResponse>>> testCall = mixtureService.getUserMixtureTasks(companyId);
        
        testCall.enqueue(new Callback<ApiResponse<List<MixtureTaskResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MixtureTaskResponse>>> call, Response<ApiResponse<List<MixtureTaskResponse>>> response) {
                Log.d(TAG, "测试API连接响应码: " + response.code());
                Log.d(TAG, "测试API连接响应头: " + response.headers());
                
                if (response.isSuccessful()) {
                    ApiResponse<List<MixtureTaskResponse>> apiResponse = response.body();
                    Log.d(TAG, "API连接测试成功，响应体: " + (apiResponse != null ? "数据获取成功" : "响应体为空"));
                    Toast.makeText(requireContext(), "API连接测试成功: " + response.code(), Toast.LENGTH_SHORT).show();
                    
                    // 成功后尝试获取混合料和沥青任务
                    if (sharedPrefsManager.isLoggedIn()) {
                        fetchMixtureTasks();
                        fetchAsphaltTasks();
                    }
                } else {
                    // 获取错误信息
                    String errorMsg = "接受任务失败";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MixtureTaskResponse>>> call, Throwable t) {
                Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 用于测试API连接的简单接口
     */
    private interface TestApiService {
        @GET("/")
        Call<Void> testConnection();
    }
    
    // 添加显示空状态的辅助方法
    private void showEmptyState(String experimentType) {
        requireActivity().runOnUiThread(() -> {
            if ("MIXTURE".equals(experimentType)) {
                // 更新未接受任务列表
                emptyTaskText.setVisibility(View.VISIBLE);
                emptyTaskText.setText("暂无实验任务");
                taskRecyclerView.setVisibility(View.GONE);
                
                // 更新已接受任务列表
                emptyMyTaskText.setVisibility(View.VISIBLE);
                emptyMyTaskText.setText("暂无进行中的任务");
                myTasksRecyclerView.setVisibility(View.GONE);
            } else if ("ASPHALT".equals(experimentType)) {
                // 更新未接受任务列表
                emptyTaskText.setVisibility(View.VISIBLE);
                emptyTaskText.setText("暂无待接受的任务");
                taskRecyclerView.setVisibility(View.GONE);
                
                // 更新已接受任务列表
                emptyMyTaskText.setVisibility(View.VISIBLE);
                emptyMyTaskText.setText("暂无进行中的任务");
                myTasksRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskAccepted(ExperimentTask task) {
        handleMixtureTaskAccepted(task);
    }

    @Override
    public void onMaterialCompleted(ExperimentTask task) {
        // 调用API将prepare_status更新为"finished"
        Log.d(TAG, "更新任务备料状态: " + task.getTaskId());
        
        // 显示加载提示
        Toast.makeText(requireContext(), "正在保存备料状态...", Toast.LENGTH_SHORT).show();
        
        // 调用API更新备料状态
        mixtureTaskService.updatePrepareStatus(task.getTaskId())
            .enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "成功更新任务备料状态: " + task.getTaskId());
                        
                        // 显示成功消息
                        Toast.makeText(requireContext(), "备料完成！", Toast.LENGTH_SHORT).show();
                        
                        // 刷新任务列表数据
                        refreshData();
                    } else {
                        Log.e(TAG, "更新任务备料状态失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                        Toast.makeText(requireContext(), "更新备料状态失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    Log.e(TAG, "更新任务备料状态请求失败: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "网络错误，请重试", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void processAsphaltTasks(List<AsphaltTaskResponse> tasks) {
        // 清空已有数据
        List<ExperimentTask> unacceptedTasks = new ArrayList<>();
        List<ExperimentTask> acceptedTasks = new ArrayList<>();
        
        // 用于跟踪已处理的任务分配ID，避免重复处理
        Set<String> processedAssignmentIds = new HashSet<>();
        
        // 用于跟踪每个任务组（按asphalt_task_name分组）的完成状态
        Map<String, Integer> taskNameTotalCount = new HashMap<>();
        Map<String, Integer> taskNameFinishedCount = new HashMap<>();
        Map<String, Boolean> taskNameCompletionStatus = new HashMap<>();
        
        // 第一遍：扫描所有任务，统计每个任务组（按asphalt_task_name分组）的完成情况
        for (AsphaltTaskResponse asphaltTask : tasks) {
            String taskName = asphaltTask.getAsphaltTaskName();
            if (taskName == null || taskName.trim().isEmpty()) {
                continue; // 跳过没有任务名称的任务
            }
            
            // 更新该任务名称组的任务总数
            taskNameTotalCount.put(taskName, taskNameTotalCount.getOrDefault(taskName, 0) + 1);
            
            // 检查实验状态（大小写不敏感）
            String experimentStatus = asphaltTask.getExperimentStatus();
            Log.d(TAG, "任务名称: " + taskName + ", 实验状态: " + experimentStatus);
            
            // 大小写不敏感地检查状态是否为"finished"
            boolean isFinished = experimentStatus != null && experimentStatus.toLowerCase().equals("finished");
            
            // 如果实验已完成，更新已完成实验计数
            if (isFinished) {
                taskNameFinishedCount.put(taskName, taskNameFinishedCount.getOrDefault(taskName, 0) + 1);
                Log.d(TAG, "实验已完成: " + taskName);
            }
        }
        
        // 确定每个任务组是否全部完成
        for (String taskName : taskNameTotalCount.keySet()) {
            int totalCount = taskNameTotalCount.get(taskName);
            int finishedCount = taskNameFinishedCount.getOrDefault(taskName, 0);
            
            // 如果所有实验都已完成，标记该组为完成状态
            boolean allFinished = (totalCount > 0) && (finishedCount == totalCount);
            taskNameCompletionStatus.put(taskName, allFinished);
            
            Log.d(TAG, "任务组: " + taskName + 
                  ", 总任务数: " + totalCount + 
                  ", 已完成任务数: " + finishedCount + 
                  ", 是否全部完成: " + allFinished);
        }
        
        // 处理每个任务，按照原有逻辑但跳过已完成的任务组
        for (AsphaltTaskResponse asphaltTask : tasks) {
            String taskName = asphaltTask.getAsphaltTaskName();
            if (taskName == null || taskName.trim().isEmpty()) {
                continue; // 跳过没有任务名称的任务
            }
            
            // 检查该任务所属组是否全部完成，如果是，则跳过
            boolean isTaskGroupCompleted = taskNameCompletionStatus.getOrDefault(taskName, false);
            if (isTaskGroupCompleted) {
                Log.d(TAG, "跳过已完成任务组: " + taskName);
                continue;
            }
            
            String assignmentId = asphaltTask.getAsphaltTaskAssignmentId();
            
            // 如果没有分配ID，则使用实验ID作为唯一标识
            if (assignmentId == null || assignmentId.trim().isEmpty()) {
                assignmentId = String.valueOf(asphaltTask.getAsphaltExperimentId());
                Log.d(TAG, "任务没有分配ID，使用实验ID作为唯一标识: " + assignmentId);
            }
            
            // 如果这个assignment_id已经处理过，则跳过
            if (processedAssignmentIds.contains(assignmentId)) {
                Log.d(TAG, "跳过重复任务，分配ID: " + assignmentId + ", 任务名称: " + asphaltTask.getAsphaltTaskName());
                continue;
            }
            
            // 记录这个assignment_id已经处理
            processedAssignmentIds.add(assignmentId);
            
            Log.d(TAG, "处理沥青任务: " + asphaltTask.getAsphaltTaskName() + ", 状态: " + asphaltTask.getStatus() + ", 分配ID: " + assignmentId + ", taskStatus=" + asphaltTask.getTaskStatus());
            
            // 直接跳过已完成的任务
            if ("COMPLETED".equalsIgnoreCase(asphaltTask.getTaskStatus())) {
                Log.d(TAG, "直接跳过已完成任务: " + asphaltTask.getAsphaltTaskName() + ", taskStatus=" + asphaltTask.getTaskStatus());
                continue;
            }
            
            // 转换为通用实验任务模型
            ExperimentTask task = convertAsphaltTaskToExperimentTask(asphaltTask);
            
            // 根据状态分类 - 优先使用 taskStatus，其次使用 status
            String taskStatus = asphaltTask.getTaskStatus();
            String status = asphaltTask.getStatus();
            
            // 如果 taskStatus 为 ONGOING 或者 status 为 ONGOING，则添加到已接受任务
            if ("ONGOING".equalsIgnoreCase(taskStatus) || 
                "ACCEPTED".equalsIgnoreCase(taskStatus) || 
                "PROCESSING".equalsIgnoreCase(taskStatus) ||
                "ONGOING".equalsIgnoreCase(status)) {
                acceptedTasks.add(task);
                Log.d(TAG, "添加到已接受任务: " + task.getTaskName());
            } 
            // 如果是 CREATED 状态，则添加到未接受任务
            else if ("CREATED".equalsIgnoreCase(taskStatus)) {
                unacceptedTasks.add(task);
                Log.d(TAG, "添加到未接受任务: " + task.getTaskName());
            } 
            // 默认情况，添加到未接受任务
            else {
                unacceptedTasks.add(task);
                Log.d(TAG, "状态未知，默认添加到未接受任务: " + task.getTaskName());
            }
        }
        
        // 更新集合
        apiAsphaltUnacceptedTasks.clear();  // 先清空，避免添加重复数据
        apiAsphaltUnacceptedTasks.addAll(unacceptedTasks);
        apiAsphaltAcceptedTasks.clear();  // 先清空，避免添加重复数据
        apiAsphaltAcceptedTasks.addAll(acceptedTasks);
        
        Log.d(TAG, "共转换 " + unacceptedTasks.size() + " 个未接受任务和 " + acceptedTasks.size() + " 个已接受任务");
        
        // 更新UI
        updateTaskUI(unacceptedTasks, acceptedTasks, "ASPHALT");
    }

    private void updateWelcomeSection() {
        // 获取用户名
        String username = sharedPrefsManager.getUserName();
        if (username == null || username.isEmpty()) {
            username = "用户";
        }
        
        // 获取可接受任务数量
        int mixtureTaskCount = apiMixtureUnacceptedTasks.size();
        int asphaltTaskCount = apiAsphaltUnacceptedTasks.size();
        
        // 设置欢迎语和任务数量信息
        greetingText.setText("你好，");
        welcomeText.setText(username);
        mixtureTaskText.setText("可接受混合料任务: " + mixtureTaskCount + " 个");
        asphaltTaskText.setText("可接受沥青任务: " + asphaltTaskCount + " 个");
        
        // 记录日志
        Log.d(TAG, "更新欢迎信息: 未接受混合料任务: " + mixtureTaskCount + ", 未接受沥青任务: " + asphaltTaskCount);
    }
}