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
import android.widget.LinearLayout;
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
import com.example.labdata_main.adapter.AsphaltProjectCardAdapter;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class OverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, 
        ExperimentTaskAdapter.OnTaskClickListener, 
        AsphaltProjectCardAdapter.OnAsphaltTaskActionListener {
    private TextView welcomeText;
    private Spinner spinner;
    private RecyclerView taskRecyclerView;
    private RecyclerView myTasksRecyclerView;
    private TextView emptyTaskText;
    private TextView emptyMyTaskText;
    private ExperimentTaskAdapter taskAdapter;
    private ProjectCardAdapter mixtureTaskAdapter;
    private AsphaltProjectCardAdapter asphaltTaskAdapter;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private ExecutorService executorService;
    private BroadcastReceiver taskRefreshReceiver;

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
        requireContext().registerReceiver(taskRefreshReceiver, new IntentFilter("com.example.labdata_main.REFRESH_TASKS"));
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
        spinner = view.findViewById(R.id.spinner);
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        myTasksRecyclerView = view.findViewById(R.id.rvTasks);
        emptyTaskText = view.findViewById(R.id.empty_task_text);
        emptyMyTaskText = view.findViewById(R.id.empty_my_task_text);

        // 初始化数据库和SharedPrefs
        database = AppDatabase.getInstance(requireContext());
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        // 设置按钮点击事件
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
                loadExperimentTasks();
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
                loadExperimentTasks();
            }
        };
        requireActivity().registerReceiver(taskRefreshReceiver, filter);
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
                loadExperimentTasks();
            }
        };
        requireContext().registerReceiver(taskRefreshReceiver, filter);

        // 设置Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.experiment_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        loadExperimentTasks();
    }

    private void loadExperimentTasks() {
        if (executorService.isShutdown()) {
            // 如果线程池已关闭，重新创建
            executorService = Executors.newFixedThreadPool(4);
        }

        try {
            executorService.execute(() -> {
                try {
                    String companyId = sharedPrefsManager.getUserCompany();
                    if (companyId == null) {
                        Log.e("OverviewFragment", "Company ID is null");
                        return;
                    }

                    // 获取未完成的任务
                    List<ExperimentTask> allTasks = database.experimentTaskDao().getTasksByCompany(companyId);

                    // 分离已接受和未接受的任务，并按实验类型分类
                    List<ExperimentTask> unacceptedTasks = new ArrayList<>();
                    List<ExperimentTask> acceptedMixtureTasks = new ArrayList<>();
                    List<ExperimentTask> acceptedAsphaltTasks = new ArrayList<>();

                    for (ExperimentTask task : allTasks) {
                        String status = task.getStatus();
                        if ("已接受".equals(status)) {
                            // 根据实验类型分类已接受的任务
                            if ("MIXTURE".equals(task.getExperimentType())) {
                                acceptedMixtureTasks.add(task);
                            } else if ("ASPHALT".equals(task.getExperimentType())) {
                                acceptedAsphaltTasks.add(task);
                            }
                        } else if (!"已完成".equals(status)) {
                            // 未完成且未接受的任务
                            unacceptedTasks.add(task);
                        }
                    }

                    // 在主线程更新UI
                    requireActivity().runOnUiThread(() -> {
                        // 更新未接受任务列表
                        if (unacceptedTasks.isEmpty()) {
                            emptyTaskText.setVisibility(View.VISIBLE);
                            taskRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyTaskText.setVisibility(View.GONE);
                            taskRecyclerView.setVisibility(View.VISIBLE);
                            taskAdapter.setTasks(unacceptedTasks);
                        }

                        // 获取当前选中的实验类型
                        String selectedType = spinner.getSelectedItem().toString();
                        List<ExperimentTask> acceptedTasks;
                        
                        // 根据选中的实验类型显示对应的已接受任务
                        if ("沥青混合料试验".equals(selectedType)) {
                            myTasksRecyclerView.setAdapter(mixtureTaskAdapter);
                            acceptedTasks = acceptedMixtureTasks;
                        } else if ("沥青试验".equals(selectedType)) {
                            myTasksRecyclerView.setAdapter(asphaltTaskAdapter);
                            acceptedTasks = acceptedAsphaltTasks;
                        } else {
                            // 默认显示混合料任务
                            myTasksRecyclerView.setAdapter(mixtureTaskAdapter);
                            acceptedTasks = acceptedMixtureTasks;
                        }

                        // 更新已接受任务列表
                        if (acceptedTasks.isEmpty()) {
                            emptyMyTaskText.setVisibility(View.VISIBLE);
                            myTasksRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyMyTaskText.setVisibility(View.GONE);
                            myTasksRecyclerView.setVisibility(View.VISIBLE);
                            if (myTasksRecyclerView.getAdapter() instanceof ProjectCardAdapter) {
                                ((ProjectCardAdapter) myTasksRecyclerView.getAdapter()).setTasks(acceptedTasks);
                            } else if (myTasksRecyclerView.getAdapter() instanceof AsphaltProjectCardAdapter) {
                                ((AsphaltProjectCardAdapter) myTasksRecyclerView.getAdapter()).setTasks(acceptedTasks);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("OverviewFragment", "Error loading tasks", e);
                }
            });
        } catch (RejectedExecutionException e) {
            Log.e("OverviewFragment", "Task execution rejected", e);
        }
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
        executorService.execute(() -> {
            try {
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("OverviewFragment", "Company ID is null");
                    return;
                }

                // 获取该公司的所有任务
                List<ExperimentTask> allTasks = database.experimentTaskDao().getTasksByCompany(companyId);
                List<ExperimentTask> filteredTasks = new ArrayList<>();
                List<ExperimentTask> acceptedTasks = new ArrayList<>();

                for (ExperimentTask task : allTasks) {
                    if (type.equals(task.getExperimentType())) {
                        if ("已接受".equals(task.getStatus())) {
                            acceptedTasks.add(task);
                        } else {
                            filteredTasks.add(task);
                        }
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    if (filteredTasks.isEmpty()) {
                        taskRecyclerView.setVisibility(View.GONE);
                        emptyTaskText.setVisibility(View.VISIBLE);
                    } else {
                        emptyTaskText.setVisibility(View.GONE);
                        taskRecyclerView.setVisibility(View.VISIBLE);
                        taskAdapter.setTasks(filteredTasks);
                    }

                    if (acceptedTasks.isEmpty()) {
                        myTasksRecyclerView.setVisibility(View.GONE);
                        emptyMyTaskText.setVisibility(View.VISIBLE);
                    } else {
                        emptyMyTaskText.setVisibility(View.GONE);
                        myTasksRecyclerView.setVisibility(View.VISIBLE);
                        
                        // 根据任务类型选择不同的适配器
                        if ("MIXTURE".equals(type)) {
                            myTasksRecyclerView.setAdapter(mixtureTaskAdapter);
                            mixtureTaskAdapter.setTasks(acceptedTasks);
                        } else if ("ASPHALT".equals(type)) {
                            myTasksRecyclerView.setAdapter(asphaltTaskAdapter);
                            asphaltTaskAdapter.setTasks(acceptedTasks);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("OverviewFragment", "Error filtering tasks", e);
            }
        });
    }

    @Override
    public void onTaskClick(ExperimentTask task) {
        // 先获取完整的任务信息
        executorService.execute(() -> {
            ExperimentTask fullTask = database.experimentTaskDao().getFullTaskById(task.getId());
            if (fullTask == null) {
                Log.e("OverviewFragment", "Task not found: " + task.getId());
                return;
            }

            requireActivity().runOnUiThread(() -> {
                if ("MIXTURE".equals(fullTask.getExperimentType())) {
                    // 显示混合料实验任务详情
                    TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(fullTask);
                    bottomSheet.setTaskAcceptListener(task1 -> handleMixtureTaskAccepted(task1));
                    bottomSheet.show(getChildFragmentManager(), "task_detail");
                } else if ("ASPHALT".equals(fullTask.getExperimentType())) {
                    // 显示沥青实验任务详情
                    BottomSheetAsphaltTaskDetailFragment bottomSheet = BottomSheetAsphaltTaskDetailFragment.newInstance(fullTask);
                    bottomSheet.setOnTaskActionListener(new BottomSheetAsphaltTaskDetailFragment.OnTaskActionListener() {
                        @Override
                        public void onTaskAccepted(ExperimentTask task) {
                            handleAsphaltTaskAccepted(task);
                        }

                        @Override
                        public void onTaskRejected(ExperimentTask task) {
                            executorService.execute(() -> {
                                database.experimentTaskDao().update(task);
                                requireActivity().runOnUiThread(() -> {
                                    loadExperimentTasks();
                                });
                            });
                        }
                    });
                    bottomSheet.show(getChildFragmentManager(), "asphalt_task_detail");
                }
            });
        });
    }

    // 处理混合料任务的接受
    private void handleMixtureTaskAccepted(ExperimentTask task) {
        task.setStatus("已接受");
        // 设置实验人员为当前登录用户
        String currentUser = sharedPrefsManager.getUserName();
        if (currentUser != null && !currentUser.isEmpty()) {
            task.setExperimenter(currentUser);
        }
        executorService.execute(() -> {
            database.experimentTaskDao().update(task);
            requireActivity().runOnUiThread(() -> {
                loadExperimentTasks();
            });
        });
    }

    // 处理沥青任务的接受
    private void handleAsphaltTaskAccepted(ExperimentTask task) {
        task.setStatus("已接受");
        // 设置实验人员为当前登录用户
        String currentUser = sharedPrefsManager.getUserName();
        if (currentUser != null && !currentUser.isEmpty()) {
            task.setExperimenter(currentUser);
        }
        executorService.execute(() -> {
            database.experimentTaskDao().update(task);
            requireActivity().runOnUiThread(() -> {
                loadExperimentTasks();
            });
        });
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
        // 每次页面恢复时刷新任务列表
        loadExperimentTasks();
    }

    // 实现 AsphaltProjectCardAdapter.OnAsphaltTaskActionListener 接口方法
    @Override
    public void onViewAsphaltInfo(ExperimentTask task) {
        // 显示沥青信息的底部弹窗
        executorService.execute(() -> {
            ExperimentTask fullTask = database.experimentTaskDao().getFullTaskById(task.getId());
            if (fullTask == null) {
                Log.e("OverviewFragment", "Task not found: " + task.getId());
                return;
            }

            requireActivity().runOnUiThread(() -> {
                BottomSheetAsphaltTaskDetailFragment bottomSheet = 
                        BottomSheetAsphaltTaskDetailFragment.newInstance(fullTask, false);  // 传入 false 隐藏接受按钮
                bottomSheet.setOnTaskActionListener(new BottomSheetAsphaltTaskDetailFragment.OnTaskActionListener() {
                    @Override
                    public void onTaskAccepted(ExperimentTask task) {
                        // 任务已经被接受，这里不需要处理
                    }

                    @Override
                    public void onTaskRejected(ExperimentTask task) {
                        // 更新任务状态
                        executorService.execute(() -> {
                            database.experimentTaskDao().update(task);
                            requireActivity().runOnUiThread(() -> {
                                loadExperimentTasks();
                            });
                        });
                    }
                });
                bottomSheet.show(getChildFragmentManager(), "asphalt_task_detail");
            });
        });
    }

    @Override
    public void onRecordData(ExperimentTask task) {
        // 先获取完整的任务信息
        executorService.execute(() -> {
            ExperimentTask fullTask = database.experimentTaskDao().getFullTaskById(task.getId());
            if (fullTask == null) {
                Log.e("OverviewFragment", "Task not found: " + task.getId());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "未找到任务", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // 在主线程中启动实验数据记录活动
            requireActivity().runOnUiThread(() -> {
                Intent intent = new Intent(requireContext(), RecordExperimentDataActivity.class);
                intent.putExtra("taskId", fullTask.getId());
                intent.putExtra("experiment_type", "ASPHALT");
                startActivity(intent);
            });
        });
    }

    // 显示混合料实验信息
    private void showMixtureExperimentInfo(ExperimentTask task) {
        executorService.execute(() -> {
            ExperimentTask fullTask = database.experimentTaskDao().getFullTaskById(task.getId());
            if (fullTask == null) {
                Log.e("OverviewFragment", "Task not found: " + task.getId());
                return;
            }

            requireActivity().runOnUiThread(() -> {
                BottomSheetMixRatioDetailFragment bottomSheet = 
                    BottomSheetMixRatioDetailFragment.newInstance(fullTask);
                
                // 设置完成备料的监听器
                bottomSheet.setOnMaterialCompletedListener(updatedTask -> {
                    // 在后台线程更新数据库
                    executorService.execute(() -> {
                        database.experimentTaskDao().update(updatedTask);
                        // 在主线程更新UI
                        requireActivity().runOnUiThread(() -> {
                            loadExperimentTasks();
                        });
                    });
                });
                
                bottomSheet.show(getChildFragmentManager(), "mix_ratio_detail");
            });
        });
    }

    // 开始混合料实验
    private void startMixtureExperiment(ExperimentTask task) {
        Intent intent = new Intent(requireContext(), RecordExperimentDataActivity.class);
        intent.putExtra("taskId", task.getId());  // 这里也需要修改为 "taskId"
        intent.putExtra("experiment_type", "MIXTURE");
        startActivity(intent);
    }
}
