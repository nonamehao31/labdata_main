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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.CompletedExperimentAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentFragment extends Fragment {
    private RecyclerView experimentRecyclerView;
    private TextView emptyExperimentText;
    private CompletedExperimentAdapter experimentAdapter;
    private AppDatabase database;
    private SharedPrefsManager sharedPrefsManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BroadcastReceiver taskRefreshReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 注册广播接收器
        taskRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.labdata_main.REFRESH_TASKS".equals(intent.getAction())) {
                    loadCompletedExperiments();
                }
            }
        };
        requireContext().registerReceiver(taskRefreshReceiver, new IntentFilter("com.example.labdata_main.REFRESH_TASKS"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment, container, false);

        experimentRecyclerView = view.findViewById(R.id.rvCompletedExperiments);
        emptyExperimentText = view.findViewById(R.id.empty_experiment_text);
        
        // 数据筛选按钮
        MaterialButton btnDataFilter = view.findViewById(R.id.btnDataFilter);
        btnDataFilter.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ExperimentAnalysisActivity.class);
            startActivity(intent);
        });

        // 数据分析按钮
        MaterialButton btnDataAnalysis = view.findViewById(R.id.btnDataAnalysis);
        btnDataAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RealExperimentAnalysisActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = AppDatabase.getInstance(requireContext());
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        setupExperimentRecyclerView();
        loadCompletedExperiments();
    }

    private void setupExperimentRecyclerView() {
        experimentAdapter = new CompletedExperimentAdapter();
        experimentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        experimentRecyclerView.setAdapter(experimentAdapter);
    }

    private void loadCompletedExperiments() {
        executor.execute(() -> {
            try {
                String companyId = sharedPrefsManager.getUserCompany();
                if (companyId == null) {
                    Log.e("ExperimentFragment", "Company ID is null");
                    return;
                }

                // 获取已完成的任务
                List<ExperimentTask> completedTasks = database.experimentTaskDao().getCompletedTasksByCompany(companyId);
                List<CompletedExperimentAdapter.TaskWithData> tasksWithData = new ArrayList<>();

                for (ExperimentTask task : completedTasks) {
                    Log.d("ExperimentFragment", String.format(
                        "Processing task - ID: %d, TaskId: %s, Name: %s, Type: %s, Status: %s",
                        task.getId(),
                        task.getTaskId(),
                        task.getTaskName(),
                        task.getExperimentType(),
                        task.getStatus()
                    ));

                    // 根据实验类型获取对应的实验数据
                    if ("ASPHALT".equals(task.getExperimentType())) {
                        // 获取沥青实验数据
                        List<AsphaltExperimentData> asphaltData = 
                            database.asphaltExperimentDataDao().getByTaskId(task.getId());
                        if (!asphaltData.isEmpty()) {
                            tasksWithData.add(new CompletedExperimentAdapter.TaskWithData(task, asphaltData));
                        }
                    } else if ("MIXTURE".equals(task.getExperimentType())) {
                        // 获取混合料实验数据
                        List<ExperimentData> mixtureData = 
                            database.experimentDataDao().getExperimentDataByTaskId(task.getId());
                        if (!mixtureData.isEmpty()) {
                            tasksWithData.add(new CompletedExperimentAdapter.TaskWithData(task, mixtureData));
                        }
                    } else {
                        // 获取其他类型的实验数据
                        List<ExperimentData> experimentDataList = 
                            database.experimentDataDao().getExperimentDataByTaskId(task.getId());
                        if (!experimentDataList.isEmpty()) {
                            tasksWithData.add(new CompletedExperimentAdapter.TaskWithData(task, experimentDataList));
                        }
                    }
                }

                Log.d("ExperimentFragment", "Loaded " + tasksWithData.size() + " completed tasks with data");

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (tasksWithData.isEmpty()) {
                        emptyExperimentText.setVisibility(View.VISIBLE);
                        experimentRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyExperimentText.setVisibility(View.GONE);
                        experimentRecyclerView.setVisibility(View.VISIBLE);
                        experimentAdapter.setTasks(tasksWithData);
                    }
                });
            } catch (Exception e) {
                Log.e("ExperimentFragment", "Error loading completed tasks", e);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCompletedExperiments();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskRefreshReceiver != null) {
            requireContext().unregisterReceiver(taskRefreshReceiver);
        }
        executor.shutdown();
    }
}