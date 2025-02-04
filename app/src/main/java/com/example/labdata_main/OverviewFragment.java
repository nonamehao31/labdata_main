package com.example.labdata_main;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.ExperimentTaskAdapter;
import com.example.labdata_main.adapter.ProjectCardAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, ExperimentTaskAdapter.OnTaskClickListener {
    private TextView welcomeText;
    private Spinner spinner;
    private RecyclerView taskRecyclerView;
    private RecyclerView myTasksRecyclerView;
    private TextView emptyTaskText;
    private TextView emptyMyTaskText;
    private ExperimentTaskAdapter taskAdapter;
    private ProjectCardAdapter myTasksAdapter;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BroadcastReceiver taskRefreshReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        // 注册广播接收器
        taskRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.labdata_main.REFRESH_TASKS".equals(intent.getAction())) {
                    loadExperimentTasks();
                }
            }
        };
        requireContext().registerReceiver(taskRefreshReceiver, new IntentFilter("com.example.labdata_main.REFRESH_TASKS"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview, container, false);

        // 初始化视图
        welcomeText = view.findViewById(R.id.welcome_text);
        spinner = view.findViewById(R.id.experiment_spinner);
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        myTasksRecyclerView = view.findViewById(R.id.rvMyTasks);
        emptyTaskText = view.findViewById(R.id.empty_task_text);
        emptyMyTaskText = view.findViewById(R.id.empty_my_task_text);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 SharedPrefsManager
        if (sharedPrefsManager == null) {
            sharedPrefsManager = new SharedPrefsManager(requireActivity());
        }

        // 设置欢迎信息
        welcomeText.setText("你好，实验员");  // 设置默认欢迎语
        if (sharedPrefsManager != null) {
            String userName = sharedPrefsManager.getUserName();
            if (userName != null && !userName.isEmpty()) {
                welcomeText.setText(String.format("你好，%s", userName));
            }
        }

        // 初始化RecyclerView
        setupRecyclerView();
        setupAcceptedTasksRecyclerView();

        // 初始化添加配合比按钮
        MaterialButton addMixButton = view.findViewById(R.id.add_mix_button);
        addMixButton.setOnClickListener(v -> {
            MixRatioBottomSheetFragment bottomSheet = MixRatioBottomSheetFragment.newInstance();
            bottomSheet.show(getChildFragmentManager(), "MixRatioBottomSheet");
        });

        // 初始化添加实验任务按钮
        MaterialButton addExperimentButton = view.findViewById(R.id.assign_task_button);
        addExperimentButton.setOnClickListener(v -> {
            AddExperimentBottomSheet bottomSheet = AddExperimentBottomSheet.newInstance();
            bottomSheet.setOnExperimentNameSubmitListener(experimentName -> {
                ExperimentTaskSetupActivity.start(requireContext(), experimentName);
            });
            bottomSheet.show(getChildFragmentManager(), "bottom_sheet_add_experiment");
        });

        // 创建下拉菜单选项
        String[] items = new String[]{"请选择实验类型", "混合料实验", "沥青试验"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                items
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // 加载实验任务
        loadExperimentTasks();
    }

    private void setupRecyclerView() {
        taskAdapter = new ExperimentTaskAdapter(this);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }

    private void setupAcceptedTasksRecyclerView() {
        myTasksAdapter = new ProjectCardAdapter();
        myTasksAdapter.setOnProjectCardActionListener(new ProjectCardAdapter.OnProjectCardActionListener() {
            @Override
            public void onViewMixRatios(ExperimentTask task) {
                // 这个方法不再需要实现
            }

            @Override
            public void onGenerateSpecimenCode(ExperimentTask task) {
                // 处理生成试件码
                Toast.makeText(requireContext(), "生成试件码功能开发中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecordExperimentData(ExperimentTask task) {
                // 处理记录实验数据
                Toast.makeText(requireContext(), "记录实验数据功能开发中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskUpdated(ExperimentTask task) {
                // 更新数据库中的任务状态
                executor.execute(() -> {
                    AppDatabase.getInstance(requireContext()).experimentTaskDao().update(task);
                    requireActivity().runOnUiThread(() -> {
                        // 刷新任务列表
                        loadExperimentTasks();
                    });
                });
            }
        });
        myTasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        myTasksRecyclerView.setAdapter(myTasksAdapter);
    }

    private void loadExperimentTasks() {
        executor.execute(() -> {
            try {
                // 获取当前用户的公司ID
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("OverviewFragment", "Company ID is null");
                    return;
                }

                // 获取该公司的所有任务
                List<ExperimentTask> allTasks = database.experimentTaskDao().getTasksByCompany(companyId);
                
                // 分离已接受和未接受的任务
                List<ExperimentTask> acceptedTasks = new ArrayList<>();
                List<ExperimentTask> unacceptedTasks = new ArrayList<>();
                
                for (ExperimentTask task : allTasks) {
                    if (task.getStatus() != null && task.getStatus().equals("已接受")) {
                        acceptedTasks.add(task);
                    } else {
                        unacceptedTasks.add(task);
                    }
                }

                // 在主线程更新UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 更新未接受任务列表
                    if (unacceptedTasks.isEmpty()) {
                        emptyTaskText.setVisibility(View.VISIBLE);
                        taskRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyTaskText.setVisibility(View.GONE);
                        taskRecyclerView.setVisibility(View.VISIBLE);
                        taskAdapter.setTasks(unacceptedTasks);
                    }

                    // 更新已接受任务列表
                    if (acceptedTasks.isEmpty()) {
                        emptyMyTaskText.setVisibility(View.VISIBLE);
                        myTasksRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMyTaskText.setVisibility(View.GONE);
                        myTasksRecyclerView.setVisibility(View.VISIBLE);
                        myTasksAdapter.setTasks(acceptedTasks);
                    }
                });
            } catch (Exception e) {
                Log.e("OverviewFragment", "Error loading tasks", e);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedType = parent.getItemAtPosition(position).toString();
        String experimentType;
        
        switch (selectedType) {
            case "混合料实验":
                experimentType = "MIXTURE";
                break;
            case "沥青试验":
                experimentType = "ASPHALT";
                break;
            default:
                loadExperimentTasks(); // 加载所有任务
                return;
        }
        
        filterTasksByType(experimentType);
    }

    private void filterTasksByType(String type) {
        executor.execute(() -> {
            try {
                // 获取当前用户的公司ID
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("OverviewFragment", "Company ID is null");
                    return;
                }

                // 获取该公司的所有任务
                List<ExperimentTask> allTasks = database.experimentTaskDao().getTasksByCompany(companyId);
                List<ExperimentTask> filteredTasks = new ArrayList<>();

                // 根据实验类型过滤任务
                for (ExperimentTask task : allTasks) {
                    Map<Long, List<String>> assignments = task.getExperimentAssignments();
                    if (assignments != null) {
                        boolean hasMatchingType = false;
                        for (List<String> types : assignments.values()) {
                            if (types != null) {
                                for (String experimentType : types) {
                                    if (type.equals("MIXTURE") && 
                                        (experimentType.contains("混合料") || experimentType.contains("配合比"))) {
                                        hasMatchingType = true;
                                        break;
                                    } else if (type.equals("ASPHALT") && 
                                             experimentType.contains("沥青")) {
                                        hasMatchingType = true;
                                        break;
                                    }
                                }
                            }
                            if (hasMatchingType) break;
                        }
                        if (hasMatchingType) {
                            filteredTasks.add(task);
                        }
                    }
                }
                
                // 分离已接受和未接受的任务
                List<ExperimentTask> acceptedTasks = new ArrayList<>();
                List<ExperimentTask> unacceptedTasks = new ArrayList<>();
                
                for (ExperimentTask task : filteredTasks) {
                    if (task.getStatus() != null && task.getStatus().equals("已接受")) {
                        acceptedTasks.add(task);
                    } else {
                        unacceptedTasks.add(task);
                    }
                }

                // 在主线程更新UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 更新未接受任务列表
                    if (unacceptedTasks.isEmpty()) {
                        emptyTaskText.setVisibility(View.VISIBLE);
                        taskRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyTaskText.setVisibility(View.GONE);
                        taskRecyclerView.setVisibility(View.VISIBLE);
                        taskAdapter.setTasks(unacceptedTasks);
                    }

                    // 更新已接受任务列表
                    if (acceptedTasks.isEmpty()) {
                        emptyMyTaskText.setVisibility(View.VISIBLE);
                        myTasksRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMyTaskText.setVisibility(View.GONE);
                        myTasksRecyclerView.setVisibility(View.VISIBLE);
                        myTasksAdapter.setTasks(acceptedTasks);
                    }
                });
            } catch (Exception e) {
                Log.e("OverviewFragment", "Error loading tasks", e);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public void onTaskClick(ExperimentTask task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task);
        bottomSheet.setTaskAcceptListener(new TaskDetailBottomSheet.TaskAcceptListener() {
            @Override
            public void onTaskAccepted(ExperimentTask task) {
                // 在后台线程中更新任务状态
                executor.execute(() -> {
                    // 更新任务状态为已接受
                    task.setStatus("已接受");
                    database.experimentTaskDao().update(task);

                    // 在主线程中更新UI
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "已接受任务：" + task.getTaskName(), Toast.LENGTH_SHORT).show();
                        // 刷新任务列表
                        loadExperimentTasks();
                    });
                });
            }
        });
        bottomSheet.show(getChildFragmentManager(), "TaskDetailBottomSheet");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskRefreshReceiver != null) {
            requireContext().unregisterReceiver(taskRefreshReceiver);
        }
        executor.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时刷新任务列表
        loadExperimentTasks();
    }
}
