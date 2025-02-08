package com.example.labdata_main.fragment;

import android.os.Bundle;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            // 解析所有制件方法
            List<MoldingMethodInfo> moldingMethods = parseMoldingMethods(task);
            adapter.setMoldingMethods(moldingMethods);
        }
    }

    private List<MoldingMethodInfo> parseMoldingMethods(ExperimentTask task) {
        List<MoldingMethodInfo> result = new ArrayList<>();
        String moldingMethodStr = task.getMoldingMethod();
        List<MixRatio> mixRatios = task.getSelectedMixRatios();
        
        if (moldingMethodStr == null || moldingMethodStr.isEmpty() || mixRatios == null || mixRatios.isEmpty()) {
            return result;
        }

        try {
            // 解析JSON格式的制件方法字符串
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Long, List<Map<String, Object>>>>(){}.getType();
            Map<Long, List<Map<String, Object>>> methodsMap = gson.fromJson(moldingMethodStr, type);

            // 遍历每个配比
            for (MixRatio mixRatio : mixRatios) {
                List<Map<String, Object>> methods = methodsMap.get(mixRatio.getId());
                if (methods != null) {
                    // 遍历该配比的所有制件方法
                    for (Map<String, Object> method : methods) {
                        try {
                            float temp = ((Number) method.get("temp")).floatValue();
                            float speed = ((Number) method.get("speed")).floatValue();
                            float time = ((Number) method.get("time")).floatValue();
                            String compactionMethod = (String) method.get("method");
                            
                            result.add(new MoldingMethodInfo(
                                mixRatio,
                                temp,
                                speed,
                                time,
                                compactionMethod
                            ));
                        } catch (Exception e) {
                            Logger.getLogger(GenerateSpecimenFragment.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(GenerateSpecimenFragment.class.getName()).log(Level.SEVERE, null, e);
            // 如果JSON解析失败，尝试使用旧的分隔符格式
            String[] mixRatioMethods = moldingMethodStr.split(";");
            for (int i = 0; i < mixRatioMethods.length && i < mixRatios.size(); i++) {
                String mixRatioMethod = mixRatioMethods[i];
                MixRatio mixRatio = mixRatios.get(i);
                
                // 一个配比可能有多个制件方法，用 "||" 分隔
                String[] methods = mixRatioMethod.split("\\|\\|");
                
                for (String method : methods) {
                    if (method.trim().isEmpty()) continue;
                    
                    float temp = 0, speed = 0, time = 0;
                    String compactionMethod = "";
                    
                    String[] parts = method.split("\\|");
                    for (String part : parts) {
                        String[] keyValue = part.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            switch (key) {
                                case "temp":
                                    temp = Float.parseFloat(value);
                                    break;
                                case "speed":
                                    speed = Float.parseFloat(value);
                                    break;
                                case "time":
                                    time = Float.parseFloat(value);
                                    break;
                                case "method":
                                    compactionMethod = value;
                                    break;
                            }
                        }
                    }
                    
                    if (!compactionMethod.isEmpty()) {
                        result.add(new MoldingMethodInfo(
                            mixRatio,
                            temp,
                            speed,
                            time,
                            compactionMethod
                        ));
                    }
                }
            }
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
