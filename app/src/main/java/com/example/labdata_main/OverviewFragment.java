package com.example.labdata_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.ExperimentTaskAdapter;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewFragment extends Fragment implements ExperimentTaskAdapter.OnTaskClickListener {
    private TextView welcomeText;
    private Spinner spinner;
    private SharedPrefsManager sharedPrefsManager;
    private RecyclerView taskRecyclerView;
    private TextView emptyTaskText;
    private ExperimentTaskAdapter taskAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview, container, false);

        // 初始化SharedPrefsManager
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        // 获取控件
        welcomeText = view.findViewById(R.id.welcome_text);
        spinner = view.findViewById(R.id.experiment_spinner);
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        emptyTaskText = view.findViewById(R.id.empty_task_text);

        // 初始化RecyclerView
        setupRecyclerView();

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

        // 设置欢迎语
        updateWelcomeMessage();

        // 创建下拉菜单选项
        String[] items = new String[]{"请选择实验类型", "混合料实验", "沥青试验"};
        
        // 创建并设置适配器（使用自定义布局）
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                items
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 设置选项选择监听器
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // 显示默认文本
                    taskRecyclerView.setVisibility(View.VISIBLE);
                    getParentFragmentManager().popBackStack();
                    return;
                }
                
                if (position == 1) {
                    // 显示混合料实验任务列表
                    taskRecyclerView.setVisibility(View.VISIBLE);
                    getParentFragmentManager().popBackStack();
                } else if (position == 2) {
                    // 隐藏任务列表并显示沥青试验Fragment
                    taskRecyclerView.setVisibility(View.GONE);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.experiment_container, new AsphaltFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 加载实验任务列表
        loadExperimentTasks();

        return view;
    }

    private void setupRecyclerView() {
        taskAdapter = new ExperimentTaskAdapter(this);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }

    private void loadExperimentTasks() {
        AppDatabase.getInstance(requireContext())
                .experimentTaskDao()
                .getAllTasks()
                .observe(getViewLifecycleOwner(), tasks -> {
                    if (tasks != null && !tasks.isEmpty()) {
                        taskAdapter.setTasks(tasks);
                        emptyTaskText.setVisibility(View.GONE);
                        taskRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyTaskText.setVisibility(View.VISIBLE);
                        taskRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onTaskClick(ExperimentTask task) {
        // TODO: 处理任务点击事件，打开任务详情页面
        Toast.makeText(requireContext(), "点击了任务: " + task.getTaskId(), Toast.LENGTH_SHORT).show();
    }

    private void updateWelcomeMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (hour < 6) {
            timeGreeting = "凌晨好";
        } else if (hour < 11) {
            timeGreeting = "早上好";
        } else if (hour < 13) {
            timeGreeting = "中午好";
        } else if (hour < 18) {
            timeGreeting = "下午好";
        } else {
            timeGreeting = "晚上好";
        }

        String username = sharedPrefsManager.getUserName();
        if (username != null && !username.isEmpty()) {
            welcomeText.setText(String.format("%s，%s", timeGreeting, username));
        } else {
            welcomeText.setText(timeGreeting);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 更新实验任务列表
        loadExperimentTasks();
    }
}
