package com.example.labdata_main.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.adapter.SpecimenMoldingAdapter;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;

import java.util.ArrayList;
import java.util.List;

public class GenerateSpecimenFragment extends Fragment {
    private RecyclerView rvMoldingMethods;
    private Button btnGenerateCode;
    private SpecimenMoldingAdapter adapter;
    private ExperimentTask task;
    
    private static final String ARG_TASK = "task";
    
    public static GenerateSpecimenFragment newInstance(ExperimentTask task) {
        GenerateSpecimenFragment fragment = new GenerateSpecimenFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = getArguments().getParcelable(ARG_TASK);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate_specimen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadMoldingMethods();
    }

    private void initViews(View view) {
        rvMoldingMethods = view.findViewById(R.id.rvMoldingMethods);
        btnGenerateCode = view.findViewById(R.id.btnGenerateCode);
        
        btnGenerateCode.setOnClickListener(v -> generateSpecimenCode());
    }

    private void setupRecyclerView() {
        rvMoldingMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SpecimenMoldingAdapter();
        rvMoldingMethods.setAdapter(adapter);
    }

    private void loadMoldingMethods() {
        if (task != null) {
            // 打印更多调试信息
            Log.d("GenerateSpecimenFragment", "Task Details:");
            Log.d("GenerateSpecimenFragment", "Molding Method: " + task.getMoldingMethod());
            Log.d("GenerateSpecimenFragment", "Selected Mix Ratios Count: " + task.getSelectedMixRatios().size());
            
            for (MixRatio ratio : task.getSelectedMixRatios()) {
                Log.d("GenerateSpecimenFragment", "Mix Ratio: " + ratio.getName());
            }
            
            // 解析所有制件方法
            List<MoldingMethodInfo> moldingMethods = parseMoldingMethods(task);
            
            Log.d("GenerateSpecimenFragment", "Parsed Molding Methods Count: " + moldingMethods.size());
            
            adapter.setMoldingMethods(moldingMethods);
        } else {
            Log.e("GenerateSpecimenFragment", "Task is null");
        }
    }

    private List<MoldingMethodInfo> parseMoldingMethods(ExperimentTask task) {
        List<MoldingMethodInfo> result = new ArrayList<>();
        
        // 增加日志输出
        Log.d("GenerateSpecimenFragment", "原始制件方法字符串: " + task.getMoldingMethod());
        
        // 检查输入的有效性
        if (task.getMoldingMethod() == null || task.getMoldingMethod().isEmpty()) {
            Log.e("GenerateSpecimenFragment", "制件方法为空");
            return result;
        }
        
        String[] methods = task.getMoldingMethod().split(";");
        List<MixRatio> mixRatios = task.getSelectedMixRatios();
        
        Log.d("GenerateSpecimenFragment", "制件方法数量: " + methods.length);
        Log.d("GenerateSpecimenFragment", "配比数量: " + mixRatios.size());

        for (int i = 0; i < methods.length && i < mixRatios.size(); i++) {
            String method = methods[i];
            MixRatio mixRatio = mixRatios.get(i);
            
            Log.d("GenerateSpecimenFragment", "解析第 " + (i+1) + " 个制件方法: " + method);
            
            // 更灵活的解析逻辑
            float temp = 0, speed = 0, time = 0;
            String compactionMethod = "";
            
            // 支持多种分隔符
            String[] parts = method.contains("|") ? method.split("\\|") : method.split(",");
            
            for (String part : parts) {
                Log.d("GenerateSpecimenFragment", "解析部分: " + part);
                
                // 支持更多解析格式
                String[] keyValue = part.contains("=") ? 
                    part.split("=") : 
                    (part.contains(":") ? part.split(":") : new String[]{part});
                
                if (keyValue.length >= 2) {
                    String key = keyValue[0].trim().toLowerCase();
                    String value = keyValue[1].trim();
                    
                    try {
                        switch (key) {
                            case "temp":
                            case "temperature":
                                temp = Float.parseFloat(value);
                                break;
                            case "speed":
                                speed = Float.parseFloat(value);
                                break;
                            case "time":
                                time = Float.parseFloat(value);
                                break;
                            case "method":
                            case "compaction":
                                compactionMethod = value;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        Log.e("GenerateSpecimenFragment", "数值解析错误: " + e.getMessage());
                    }
                }
            }
            
            Log.d("GenerateSpecimenFragment", String.format(
                "解析结果 - 配比: %s, 温度: %.1f, 速度: %.1f, 时间: %.1f, 方法: %s", 
                mixRatio.getName(), temp, speed, time, compactionMethod
            ));
            
            result.add(new MoldingMethodInfo(
                mixRatio,
                temp,
                speed,
                time,
                compactionMethod
            ));
        }
        
        return result;
    }

    private void generateSpecimenCode() {
        // TODO: 实现生成试件码的逻辑
    }

    // 内部类用于存储制件方法信息
    public static class MoldingMethodInfo {
        private final MixRatio mixRatio;
        private final float mixingTemp;
        private final float mixingSpeed;
        private final float mixingTime;
        private final String compactionMethod;

        public MoldingMethodInfo(MixRatio mixRatio, float mixingTemp, float mixingSpeed, 
                               float mixingTime, String compactionMethod) {
            this.mixRatio = mixRatio;
            this.mixingTemp = mixingTemp;
            this.mixingSpeed = mixingSpeed;
            this.mixingTime = mixingTime;
            this.compactionMethod = compactionMethod;
        }

        public MixRatio getMixRatio() { return mixRatio; }
        public float getMixingTemp() { return mixingTemp; }
        public float getMixingSpeed() { return mixingSpeed; }
        public float getMixingTime() { return mixingTime; }
        public String getCompactionMethod() { return compactionMethod; }
    }
}
