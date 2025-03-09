package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.labdata_main.R;

/**
 * 任务名称输入Fragment
 * 用于在沥青实验任务设置流程中输入任务名称
 */
public class TaskNameFragment extends Fragment {
    private static final String ARG_TASK_NAME = "task_name";
    
    private EditText etTaskName;
    private Button btnNext;
    private ViewPager2 viewPager;
    private String taskName;

    public static TaskNameFragment newInstance(String taskName) {
        TaskNameFragment fragment = new TaskNameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_NAME, taskName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskName = getArguments().getString(ARG_TASK_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etTaskName = view.findViewById(R.id.etTaskName);
        btnNext = view.findViewById(R.id.btnNext);
        
        // 如果已有任务名称，则显示
        if (!TextUtils.isEmpty(taskName)) {
            etTaskName.setText(taskName);
        }
        
        // 获取ViewPager2的引用
        if (getActivity() != null) {
            viewPager = getActivity().findViewById(R.id.viewPager);
        }
        
        btnNext.setOnClickListener(v -> {
            String name = etTaskName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "请输入任务名称", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存任务名称
            taskName = name;
            
            // 跳转到下一页
            if (viewPager != null) {
                viewPager.setCurrentItem(1, true);
            }
        });
    }
    
    /**
     * 获取任务名称
     * @return 任务名称
     */
    public String getTaskName() {
        return taskName != null ? taskName : etTaskName.getText().toString().trim();
    }
}
