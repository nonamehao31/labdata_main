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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.adapter.ExperimentTaskAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OverviewFragment extends Fragment {
    private TextView mixText;
    private TextView welcomeText;
    private Spinner spinner;
    private SharedPrefsManager sharedPrefsManager;
    private RecyclerView rvExperimentTasks;
    private ExperimentTaskAdapter experimentTaskAdapter;
    private List<ExperimentTask> experimentTasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.overview, container, false);

        // 初始化SharedPrefsManager
        sharedPrefsManager = new SharedPrefsManager(requireContext());

        // 获取控件
        mixText = view.findViewById(R.id.mix_text);
        welcomeText = view.findViewById(R.id.welcome_text);
        spinner = view.findViewById(R.id.experiment_spinner);

        // 初始化实验任务 RecyclerView
        rvExperimentTasks = view.findViewById(R.id.rvExperimentTasks);
        rvExperimentTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        experimentTasks = generateSampleTasks();
        experimentTaskAdapter = new ExperimentTaskAdapter(requireContext(), experimentTasks, this::onTaskClick);
        rvExperimentTasks.setAdapter(experimentTaskAdapter);

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
                // TODO: 处理实验任务名称的提交
                Toast.makeText(requireContext(), "实验任务名称: " + experimentName, Toast.LENGTH_SHORT).show();
            });
            bottomSheet.show(getChildFragmentManager(), "AddExperimentBottomSheet");
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
                    mixText.setVisibility(View.VISIBLE);
                    getParentFragmentManager().popBackStack();
                    return;
                }
                
                if (position == 1) {
                    // 显示混合料实验文本，并清除沥青试验Fragment
                    mixText.setVisibility(View.VISIBLE);
                    getParentFragmentManager().popBackStack();
                } else if (position == 2) {
                    // 隐藏文本并显示沥青试验Fragment
                    mixText.setVisibility(View.GONE);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new AsphaltFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // 设置制订实验任务按钮点击事件
        view.findViewById(R.id.assign_task_button).setOnClickListener(v -> {
            ExperimentTaskBottomSheet.newInstance()
                .show(getParentFragmentManager(), "ExperimentTaskBottomSheet");
        });

        return view;
    }

    private List<ExperimentTask> generateSampleTasks() {
        List<ExperimentTask> tasks = new ArrayList<>();
        tasks.add(new ExperimentTask("沥青混合料配比实验", "项目：城市道路建设 | 配比：A-1", false));
        tasks.add(new ExperimentTask("水泥混凝土强度测试", "项目：高速公路建设 | 配比：C-2", true));
        tasks.add(new ExperimentTask("骨料筛分实验", "项目：桥梁工程 | 配比：B-3", false));
        return tasks;
    }

    private void onTaskClick(ExperimentTask task) {
        Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
        intent.putExtra("TASK_NAME", task.getTaskName());
        intent.putExtra("TASK_INFO", task.getTaskInfo());
        intent.putExtra("TASK_COMPLETED", task.isCompleted());
        startActivity(intent);
    }

    private void updateWelcomeMessage() {
        if (welcomeText == null || sharedPrefsManager == null) return;

        // 获取用户信息
        String userName = sharedPrefsManager.getUserName();
        int userType = sharedPrefsManager.getUserType();
        
        // 添加日志
        Log.d("OverviewFragment", "User type from SharedPrefs: " + userType);
        
        // 根据用户类型确定显示文本
        String userTypeStr;
        if (userType == 1) {
            userTypeStr = "管理员";
            Log.d("OverviewFragment", "Setting user type as: 管理员");
        } else if (userType == 0) {
            userTypeStr = "实验员";
            Log.d("OverviewFragment", "Setting user type as: 实验员");
        } else {
            userTypeStr = "用户"; // 默认显示
            Log.d("OverviewFragment", "Setting user type as: 用户 (default)");
        }

        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // 根据时间确定问候语
        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "上午好";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "下午好";
        } else if (hourOfDay >= 18 && hourOfDay < 22) {
            greeting = "晚上好";
        } else {
            greeting = "夜深了";
        }

        // 设置欢迎语
        String welcomeMessage = String.format("%s，%s%s", greeting, userTypeStr, userName);
        welcomeText.setText(welcomeMessage);
        Log.d("OverviewFragment", "Final welcome message: " + welcomeMessage);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 当Fragment恢复时，更新欢迎语
        updateWelcomeMessage();
        // 确保TextView可见并重置Spinner
        if (mixText != null) {
            mixText.setVisibility(View.VISIBLE);
        }
        if (spinner != null) {
            spinner.setSelection(1); // 设置为"混合料实验"
        }
    }
}
