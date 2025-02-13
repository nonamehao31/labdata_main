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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.ExperimentTaskAdapter;
import com.example.labdata_main.adapter.ProjectCardAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.fragment.BottomSheetAsphaltTaskDetailFragment;
import com.example.labdata_main.fragment.BottomSheetMakeSpecimenFragment;
import com.example.labdata_main.fragment.BottomSheetMixRatioDetailFragment;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
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
        addExperimentButton.setOnClickListener(v -> showAddExperimentDialog());

        // 初始化添加沥青实验任务按钮
        MaterialButton addAsphaltExperimentButton = view.findViewById(R.id.add_asphalt_task_button);
        addAsphaltExperimentButton.setOnClickListener(v -> showAddAsphaltTaskDialog());

        // 设置下拉菜单选项
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
                BottomSheetMixRatioDetailFragment bottomSheet = BottomSheetMixRatioDetailFragment.newInstance(task);
                bottomSheet.setOnMaterialCompletedListener(completedTask -> {
                    // 更新数据库中的任务状态
                    executor.execute(() -> {
                        AppDatabase.getInstance(requireContext()).experimentTaskDao().update(completedTask);
                        requireActivity().runOnUiThread(() -> {
                            // 刷新任务列表
                            loadExperimentTasks();
                        });
                    });
                });
                bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
            }

            @Override
            public void onViewSpecimenCode(ExperimentTask task) {
                // TODO: 实现查看试件码功能
                Toast.makeText(requireContext(), "查看试件码功能开发中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGenerateSpecimenCode(ExperimentTask task) {
                BottomSheetMakeSpecimenFragment bottomSheet = BottomSheetMakeSpecimenFragment.newInstance(task);
                bottomSheet.show(getChildFragmentManager(), "make_specimen");
            }

            @Override
            public void onRecordExperimentData(ExperimentTask task) {
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
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("OverviewFragment", "Company ID is null");
                    return;
                }

                // 获取未完成的任务
                List<ExperimentTask> allTasks = database.experimentTaskDao().getTasksByCompany(companyId);
                
                // 分离已接受和未接受的任务
                List<ExperimentTask> acceptedTasks = new ArrayList<>();
                List<ExperimentTask> unacceptedTasks = new ArrayList<>();
                
                for (ExperimentTask task : allTasks) {
                    if (task.getStatus() != null && task.getStatus().equals("已接受")) {
                        acceptedTasks.add(task);
                    } else if (task.getStatus() == null || !task.getStatus().equals("已完成")) {
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
                executor.execute(() -> {
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedType = parent.getItemAtPosition(position).toString();
        String experimentType;
        
        switch (selectedType) {
            case "沥青混合料试验":
                experimentType = "MIXTURE";
                break;
            case "沥青试验":
                experimentType = "ASPHALT";
                break;
            default:
                return;
        }
        
        filterTasksByType(experimentType);
    }

    private void filterTasksByType(String type) {
        executor.execute(() -> {
            try {
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("OverviewFragment", "Company ID is null");
                    return;
                }

                // 获取该公司的指定类型的任务
                List<ExperimentTask> filteredTasks = database.experimentTaskDao().getTasksByCompanyAndType(companyId, type);
                
                // 分离已接受和未接受的任务
                List<ExperimentTask> acceptedTasks = new ArrayList<>();
                List<ExperimentTask> unacceptedTasks = new ArrayList<>();
                
                for (ExperimentTask task : filteredTasks) {
                    if (task.getStatus() != null && task.getStatus().equals("已接受")) {
                        acceptedTasks.add(task);
                    } else if (task.getStatus() == null || !task.getStatus().equals("已完成")) {
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
                Log.e("OverviewFragment", "Error filtering tasks", e);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public void onTaskClick(ExperimentTask task) {
        // 先获取完整的任务信息
        executor.execute(() -> {
            ExperimentTask fullTask = database.experimentTaskDao().getFullTaskById(task.getId());
            if (fullTask == null) {
                Log.e("OverviewFragment", "Task not found: " + task.getId());
                return;
            }

            requireActivity().runOnUiThread(() -> {
                if ("ASPHALT".equals(fullTask.getExperimentType())) {
                    // 显示沥青实验任务详情
                    BottomSheetAsphaltTaskDetailFragment bottomSheet = BottomSheetAsphaltTaskDetailFragment.newInstance(fullTask);
                    bottomSheet.setOnTaskActionListener(new BottomSheetAsphaltTaskDetailFragment.OnTaskActionListener() {
                        @Override
                        public void onTaskAccepted(ExperimentTask task) {
                            onTaskAccepted(task);
                        }

                        @Override
                        public void onTaskRejected(ExperimentTask task) {
                            executor.execute(() -> {
                                database.experimentTaskDao().update(task);
                                requireActivity().runOnUiThread(() -> {
                                    loadExperimentTasks();
                                });
                            });
                        }
                    });
                    bottomSheet.show(getChildFragmentManager(), "asphalt_task_detail");
                } else {
                    // 显示混合料实验任务详情
                    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(fullTask);
                    bottomSheet.setTaskAcceptListener(new TaskDetailBottomSheet.TaskAcceptListener() {
                        @Override
                        public void onTaskAccepted(ExperimentTask task) {
                            onTaskAccepted(task);
                        }
                    });
                    bottomSheet.show(getChildFragmentManager(), "task_detail");
                }
            });
        });
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
