package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.fragment.BottomSheetAsphaltTaskDetailFragment;
import com.example.labdata_main.model.AsphaltExperimentData;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CompletedExperimentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MIXTURE = 1;
    private static final int VIEW_TYPE_ASPHALT = 2;

    private List<TaskWithData> tasks;
    private final Context context;

    // 实验类型中英文映射
    private static final Map<String, String> EXPERIMENT_TYPE_MAP = new HashMap<String, String>() {{
        // 沥青实验类型
        put("ductility", "延度试验");
        put("penetration", "针入度试验");
        put("softening_point", "软化点试验");
        put("density", "密度试验");
        put("viscosity", "粘度试验");
        put("flash_point", "闪点试验");
        put("bbr", "弯曲梁流变仪试验");
        put("dsr", "动态剪切仪试验");
        // 混合料实验类型
        put("marshall", "马歇尔试验");
        put("rutting", "车辙试验");
        put("water_stability", "水稳定性试验");
    }};

    // 实验参数中英文映射
    private static final Map<String, String> PARAMETER_MAP = new HashMap<String, String>() {{
        // 沥青实验参数
        put("temperature", "温度");
        put("displacement", "位移");
        put("reading", "读数");
        put("softening_temp", "软化点温度");
        put("density_value", "密度值");
        put("viscosity_value", "粘度值");
        put("flash_temp", "闪点温度");
        // BBR实验参数
        put("beam_span", "试梁跨度(mm)");
        put("specimen_width", "试件宽度(mm)");
        put("specimen_height", "试件高度(mm)");
        put("deflection_8s", "8秒时的挠度(mm)");
        put("deflection_15s", "15秒时的挠度(mm)");
        put("deflection_30s", "30秒时的挠度(mm)");
        put("deflection_60s", "60秒时的挠度(mm)");
        put("deflection_120s", "120秒时的挠度(mm)");
        put("deflection_240s", "240秒时的挠度(mm)");
        put("load_8s", "8秒时的加载力(mN)");
        put("load_15s", "15秒时的加载力(mN)");
        put("load_30s", "30秒时的加载力(mN)");
        put("load_60s", "60秒时的加载力(mN)");
        put("load_120s", "120秒时的加载力(mN)");
        put("load_240s", "240秒时的加载力(mN)");
        put("stiffness_8s", "8秒时的弯曲蠕变劲度模量(MPa)");
        put("stiffness_15s", "15秒时的弯曲蠕变劲度模量(MPa)");
        put("stiffness_30s", "30秒时的弯曲蠕变劲度模量(MPa)");
        put("stiffness_60s", "60秒时的弯曲蠕变劲度模量(MPa)");
        put("stiffness_120s", "120秒时的弯曲蠕变劲度模量(MPa)");
        put("stiffness_240s", "240秒时的弯曲蠕变劲度模量(MPa)");
        put("creep_rate", "蠕变速率(m值)");
        put("temperature_8s", "8秒时的温度(℃)");
        put("temperature_15s", "15秒时的温度(℃)");
        put("temperature_30s", "30秒时的温度(℃)");
        put("temperature_60s", "60秒时的温度(℃)");
        put("temperature_120s", "120秒时的温度(℃)");
        put("temperature_240s", "240秒时的温度(℃)");
        // DSR实验参数
        put("plate_radius", "试验板半径(R) (mm)");
        put("plate_gap", "试验平板间距(h) (mm)");
        put("temperature", "试验温度(℃)");
        put("frequency", "试验加载频率(Hz)");
        put("max_shear_stress", "最大剪切应力(τmax) (Pa)");
        put("max_shear_strain", "最大剪切应变(γmax) (%)");
        put("phase_angle", "相位角(δ)");
        put("complex_modulus", "复合剪切模量(G*)");
        // 混合料实验参数
        put("stability", "稳定度");
        put("flow", "流值");
        put("voids", "空隙率");
        put("dynamic_stability", "动态稳定度");
        put("residual_stability", "残留稳定度");
    }};

    public CompletedExperimentAdapter(Context context) {
        this.context = context;
        this.tasks = new ArrayList<>();
    }

    public static class TaskWithData {
        public final ExperimentTask task;
        public final List<? extends Object> data;

        public TaskWithData(ExperimentTask task, List<? extends Object> data) {
            this.task = task;
            this.data = data;
        }
    }

    @Override
    public int getItemViewType(int position) {
        TaskWithData taskWithData = tasks.get(position);
        return "ASPHALT".equals(taskWithData.task.getExperimentType()) 
            ? VIEW_TYPE_ASPHALT 
            : VIEW_TYPE_MIXTURE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ASPHALT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_completed_asphalt_experiment, parent, false);
            return new AsphaltViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_completed_mixture_experiment, parent, false);
            return new MixtureViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskWithData taskWithData = tasks.get(position);
        ExperimentTask task = taskWithData.task;
        
        // 设置基本信息
        String taskName = task.getTaskName() != null ? task.getTaskName() : "未命名任务";
        String taskId = String.format("任务ID：%s", task.getTaskId() != null ? task.getTaskId() : "无任务ID");
        String experimenter = String.format("实验人员：%s", task.getExperimenter() != null ? task.getExperimenter() : "未指定");
        
        // 格式化完成时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String completionTime = dateFormat.format(new Date(task.getExperimentCompletionTime()));

        if (holder instanceof AsphaltViewHolder) {
            bindAsphaltData((AsphaltViewHolder) holder, taskWithData, taskName, taskId, experimenter, completionTime);
        } else if (holder instanceof MixtureViewHolder) {
            bindMixtureData((MixtureViewHolder) holder, taskWithData, taskName, taskId, experimenter, completionTime);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            BottomSheetAsphaltTaskDetailFragment bottomSheet = 
                BottomSheetAsphaltTaskDetailFragment.newInstance(task, false);
            bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "TaskDetail");
        });
    }

    private void bindAsphaltData(AsphaltViewHolder holder, TaskWithData taskWithData, 
                               String taskName, String taskId, String experimenter, String completionTime) {
        holder.tvTaskName.setText(taskName);
        holder.tvTaskId.setText(taskId);
        holder.tvExperimenter.setText(experimenter);
        holder.tvCompletionTime.setText(completionTime);

        // 清除之前的沥青信息
        holder.llAsphaltInfo.removeAllViews();

        // 处理沥青基本信息
        String notes = taskWithData.task.getNotes();
        if (notes != null && !notes.isEmpty()) {
            String[] sections = notes.split("---+\\s*\\n");
            for (String section : sections) {
                section = section.trim();
                if (section.startsWith("沥青信息")) {
                    String[] lines = section.split("\\n");
                    StringBuilder asphaltInfo = new StringBuilder();
                    // 跳过标题行，从第二行开始处理
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (!line.isEmpty()) {
                            TextView textView = new TextView(context);
                            textView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                            textView.setText(line);
                            holder.llAsphaltInfo.addView(textView);
                        }
                    }
                    break;
                }
            }
        }

        // 构建实验结果字符串
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder devicesBuilder = new StringBuilder();

        if (taskWithData.data != null) {
            for (Object data : taskWithData.data) {
                if (data instanceof AsphaltExperimentData) {
                    AsphaltExperimentData asphaltData = (AsphaltExperimentData) data;
                    Map<String, String> values = asphaltData.getExperimentValues();
                    
                    String experimentType = asphaltData.getExperimentType();
                    contentBuilder.append(EXPERIMENT_TYPE_MAP.getOrDefault(experimentType, experimentType)).append("\n");
                    
                    // 特殊处理BBR实验数据，按类别分组显示
                    if ("bbr".equals(experimentType)) {
                        // 试件基本信息
                        contentBuilder.append("【试件基本信息】\n");
                        appendParameterIfExists(values, contentBuilder, "beam_span");
                        appendParameterIfExists(values, contentBuilder, "specimen_width");
                        appendParameterIfExists(values, contentBuilder, "specimen_height");
                        contentBuilder.append("\n");
                        
                        // 测试温度信息
                        contentBuilder.append("【测试温度】\n");
                        appendParameterIfExists(values, contentBuilder, "temperature_8s");
                        appendParameterIfExists(values, contentBuilder, "temperature_15s");
                        appendParameterIfExists(values, contentBuilder, "temperature_30s");
                        appendParameterIfExists(values, contentBuilder, "temperature_60s");
                        appendParameterIfExists(values, contentBuilder, "temperature_120s");
                        appendParameterIfExists(values, contentBuilder, "temperature_240s");
                        contentBuilder.append("\n");
                        
                        // 加载力信息
                        contentBuilder.append("【加载力】\n");
                        appendParameterIfExists(values, contentBuilder, "load_8s");
                        appendParameterIfExists(values, contentBuilder, "load_15s");
                        appendParameterIfExists(values, contentBuilder, "load_30s");
                        appendParameterIfExists(values, contentBuilder, "load_60s");
                        appendParameterIfExists(values, contentBuilder, "load_120s");
                        appendParameterIfExists(values, contentBuilder, "load_240s");
                        contentBuilder.append("\n");
                        
                        // 挠度信息
                        contentBuilder.append("【挠度】\n");
                        appendParameterIfExists(values, contentBuilder, "deflection_8s");
                        appendParameterIfExists(values, contentBuilder, "deflection_15s");
                        appendParameterIfExists(values, contentBuilder, "deflection_30s");
                        appendParameterIfExists(values, contentBuilder, "deflection_60s");
                        appendParameterIfExists(values, contentBuilder, "deflection_120s");
                        appendParameterIfExists(values, contentBuilder, "deflection_240s");
                        contentBuilder.append("\n");
                        
                        // 劲度模量信息
                        contentBuilder.append("【弯曲蠕变劲度模量】\n");
                        appendParameterIfExists(values, contentBuilder, "stiffness_8s");
                        appendParameterIfExists(values, contentBuilder, "stiffness_15s");
                        appendParameterIfExists(values, contentBuilder, "stiffness_30s");
                        appendParameterIfExists(values, contentBuilder, "stiffness_60s");
                        appendParameterIfExists(values, contentBuilder, "stiffness_120s");
                        appendParameterIfExists(values, contentBuilder, "stiffness_240s");
                        contentBuilder.append("\n");
                        
                        // 蠕变速率
                        contentBuilder.append("【蠕变速率】\n");
                        appendParameterIfExists(values, contentBuilder, "creep_rate");
                        contentBuilder.append("\n");
                    } else if ("dsr".equals(experimentType)) {
                        // 试验板参数信息
                        contentBuilder.append("【试验板参数】\n");
                        appendParameterIfExists(values, contentBuilder, "plate_radius");
                        appendParameterIfExists(values, contentBuilder, "plate_gap");
                        contentBuilder.append("\n");
                        
                        // 查找所有温度点
                        Set<String> temperaturePoints = new HashSet<>();
                        for (String key : values.keySet()) {
                            if (key.startsWith("temperature_")) {
                                String pointId = key.substring("temperature_".length());
                                temperaturePoints.add(pointId);
                            }
                        }
                        
                        // 按温度点分组显示数据
                        for (String pointId : temperaturePoints) {
                            contentBuilder.append("【温度点 #").append(pointId.substring(Math.max(0, pointId.length() - 4))).append("】\n");
                            appendParameterIfExists(values, contentBuilder, "temperature_" + pointId);
                            appendParameterIfExists(values, contentBuilder, "frequency_" + pointId);
                            appendParameterIfExists(values, contentBuilder, "max_shear_stress_" + pointId);
                            appendParameterIfExists(values, contentBuilder, "max_shear_strain_" + pointId);
                            appendParameterIfExists(values, contentBuilder, "phase_angle_" + pointId);
                            appendParameterIfExists(values, contentBuilder, "complex_modulus_" + pointId);
                            contentBuilder.append("\n");
                        }
                    } else {
                        // 其他实验类型的常规处理
                        for (Map.Entry<String, String> entry : values.entrySet()) {
                            String paramName = PARAMETER_MAP.getOrDefault(entry.getKey(), entry.getKey());
                            contentBuilder.append(paramName).append(": ").append(entry.getValue()).append("\n");
                        }
                    }
                    contentBuilder.append("\n");

                    if (asphaltData.getDeviceManufacturer() != null && asphaltData.getDeviceModel() != null) {
                        devicesBuilder.append(asphaltData.getDeviceManufacturer())
                                .append(" ")
                                .append(asphaltData.getDeviceModel())
                                .append("\n");
                    }
                }
            }
        }

        holder.tvExperimentContent.setText(contentBuilder.toString().trim());
        holder.tvDevices.setText(devicesBuilder.toString().trim());
    }

    private void bindMixtureData(MixtureViewHolder holder, TaskWithData taskWithData,
                                String taskName, String taskId, String experimenter, String completionTime) {
        ExperimentTask task = taskWithData.task;
        holder.tvTaskName.setText(taskName);
        holder.tvTaskId.setText(taskId);
        holder.tvExperimenter.setText(experimenter);
        holder.tvCompletionTime.setText(completionTime);

        // 清除之前的配比信息
        holder.llMixRatios.removeAllViews();

        // 添加配比信息
        if (task.getSelectedMixRatios() != null && !task.getSelectedMixRatios().isEmpty()) {
            StringBuilder mixRatioBuilder = new StringBuilder();
            for (int i = 0; i < task.getSelectedMixRatios().size(); i++) {
                MixRatio ratio = task.getSelectedMixRatios().get(i);
                mixRatioBuilder.append("配比").append(i + 1).append("：");
                mixRatioBuilder.append(ratio.getName()).append("\n");
            }
            
            TextView tvMixRatio = new TextView(context);
            tvMixRatio.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            tvMixRatio.setText(mixRatioBuilder.toString());
            tvMixRatio.setTextSize(14);
            holder.llMixRatios.addView(tvMixRatio);
        }

        // 添加制件方法
        if (task.getMoldingMethod() != null && !task.getMoldingMethod().isEmpty()) {
            try {
                String moldingMethod = task.getMoldingMethod()
                    .replace("[{", "{")
                    .replace("}]", "}")
                    .replace("\\", "");
                
                Gson gson = new Gson();
                JsonObject methodJson = gson.fromJson(moldingMethod, JsonObject.class);
                
                StringBuilder methodBuilder = new StringBuilder();
                
                // 提取拌合参数
                if (methodJson.has("mixingSpeed")) {
                    methodBuilder.append("拌合速度：").append(methodJson.get("mixingSpeed").getAsString()).append("rpm/min\n");
                }
                if (methodJson.has("mixingTemperature")) {
                    methodBuilder.append("拌合温度：").append(methodJson.get("mixingTemperature").getAsString()).append("°C\n");
                }
                if (methodJson.has("mixingTime")) {
                    methodBuilder.append("拌合时间：").append(methodJson.get("mixingTime").getAsString()).append("s\n");
                }
                
                // 提取制件方法
                if (methodJson.has("compactionMethod")) {
                    String method = methodJson.get("compactionMethod").getAsString();
                    methodBuilder.append("制件方法：").append(method).append("\n");
                }

                TextView tvMoldingMethod = new TextView(context);
                tvMoldingMethod.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                tvMoldingMethod.setText(methodBuilder.toString().trim());
                tvMoldingMethod.setTextSize(14);
                holder.llMixRatios.addView(tvMoldingMethod);
            } catch (Exception e) {
                // 如果解析失败，尝试直接从字符串提取关键信息
                String moldingMethod = task.getMoldingMethod();
                StringBuilder methodBuilder = new StringBuilder();
                
                // 提取拌合温度
                if (moldingMethod.contains("mixingTemperature")) {
                    String temp = extractValue(moldingMethod, "mixingTemperature");
                    if (!temp.isEmpty()) {
                        methodBuilder.append("拌合温度：").append(temp).append("°C\n");
                    }
                }
                
                // 提取拌合速度
                if (moldingMethod.contains("mixingSpeed")) {
                    String speed = extractValue(moldingMethod, "mixingSpeed");
                    if (!speed.isEmpty()) {
                        methodBuilder.append("拌合速度：").append(speed).append("rpm/min\n");
                    }
                }
                
                // 提取制件方法
                if (moldingMethod.contains("compactionMethod")) {
                    String method = extractValue(moldingMethod, "compactionMethod");
                    if (!method.isEmpty()) {
                        methodBuilder.append("制件方法：").append(method).append("\n");
                    }
                }

                TextView tvMoldingMethod = new TextView(context);
                tvMoldingMethod.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                tvMoldingMethod.setText(methodBuilder.toString().trim());
                tvMoldingMethod.setTextSize(14);
                holder.llMixRatios.addView(tvMoldingMethod);
            }
        }

        // 构建实验结果字符串
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder devicesBuilder = new StringBuilder();

        if (taskWithData.data != null) {
            Map<String, List<Double>> marshallResults = new HashMap<>();
            
            for (Object data : taskWithData.data) {
                if (data instanceof ExperimentData) {
                    ExperimentData mixtureData = (ExperimentData) data;
                    String experimentName = mixtureData.getExperimentName();
                    
                    // 处理马歇尔稳定度实验数据
                    if (experimentName != null && experimentName.contains("马歇尔稳定度")) {
                        String type = experimentName.contains("stability") ? "稳定值" : "流值";
                        double value = mixtureData.getInput1Value();
                        if (value > 0) {  // 只添加有效的数值
                            marshallResults.computeIfAbsent(type, k -> new ArrayList<>())
                                .add(value);
                        }
                        continue;
                    }
                    
                    // 处理其他实验数据
                    contentBuilder.append(EXPERIMENT_TYPE_MAP.getOrDefault(experimentName, experimentName)).append("\n");
                    if (mixtureData.getInput1Label() != null) {
                        String label1 = PARAMETER_MAP.getOrDefault(mixtureData.getInput1Label(), mixtureData.getInput1Label());
                        contentBuilder.append(label1).append(": ").append(mixtureData.getInput1Value()).append("\n");
                    }
                    if (mixtureData.getInput2Label() != null) {
                        String label2 = PARAMETER_MAP.getOrDefault(mixtureData.getInput2Label(), mixtureData.getInput2Label());
                        contentBuilder.append(label2).append(": ").append(mixtureData.getInput2Value()).append("\n");
                    }
                    if (mixtureData.getResult() != null) {
                        contentBuilder.append("结果：").append(mixtureData.getResult()).append("\n");
                    }
                    contentBuilder.append("\n");

                    if (mixtureData.getDeviceManufacturer() != null && mixtureData.getDeviceModel() != null) {
                        devicesBuilder.append(mixtureData.getDeviceManufacturer())
                                .append(" ")
                                .append(mixtureData.getDeviceModel())
                                .append("\n");
                    }
                }
            }
            
            // 显示马歇尔稳定度实验结果
            if (!marshallResults.isEmpty()) {
                contentBuilder.append("马歇尔稳定度实验\n");
                
                // 显示稳定值
                List<Double> stabilityValues = marshallResults.getOrDefault("稳定值", new ArrayList<>());
                if (!stabilityValues.isEmpty()) {
                    contentBuilder.append("稳定值(kN)：");
                    for (int i = 0; i < stabilityValues.size(); i++) {
                        contentBuilder.append(String.format("%.1f", stabilityValues.get(i)));
                        if (i < stabilityValues.size() - 1) {
                            contentBuilder.append("、");
                        }
                    }
                    contentBuilder.append("\n");
                }
                
                // 显示流值
                List<Double> flowValues = marshallResults.getOrDefault("流值", new ArrayList<>());
                if (!flowValues.isEmpty()) {
                    contentBuilder.append("流值(mm)：");
                    for (int i = 0; i < flowValues.size(); i++) {
                        contentBuilder.append(String.format("%.1f", flowValues.get(i)));
                        if (i < flowValues.size() - 1) {
                            contentBuilder.append("、");
                        }
                    }
                    contentBuilder.append("\n");
                }
                
                contentBuilder.append("\n");
            }
        }

        holder.tvExperimentContent.setText(contentBuilder.toString().trim());
        holder.tvDevices.setText(devicesBuilder.toString().trim());
    }

    private String extractValue(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return "";
        
        int valueStart = json.indexOf(":", keyIndex) + 1;
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }
        
        if (valueStart > 0 && valueEnd > valueStart) {
            String value = json.substring(valueStart, valueEnd).trim();
            return value.replace("\"", "").replace("'", "");
        }
        
        return "";
    }

    /**
     * 如果参数存在，则添加到内容构建器中
     * @param values 参数值映射
     * @param builder 内容构建器
     * @param key 参数键
     */
    private void appendParameterIfExists(Map<String, String> values, StringBuilder builder, String key) {
        if (values.containsKey(key) && values.get(key) != null) {
            String paramName = PARAMETER_MAP.getOrDefault(key, key);
            builder.append(paramName).append(": ").append(values.get(key)).append("\n");
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<TaskWithData> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public static class AsphaltViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvTaskId;
        TextView tvExperimenter;
        TextView tvCompletionTime;
        TextView tvExperimentContent;
        TextView tvDevices;
        LinearLayout llAsphaltInfo;

        public AsphaltViewHolder(View view) {
            super(view);
            tvTaskName = view.findViewById(R.id.tvTaskName);
            tvTaskId = view.findViewById(R.id.tvTaskId);
            tvExperimenter = view.findViewById(R.id.tvExperimenter);
            tvCompletionTime = view.findViewById(R.id.tvCompletionTime);
            tvExperimentContent = view.findViewById(R.id.tvExperimentContent);
            tvDevices = view.findViewById(R.id.tvDevices);
            llAsphaltInfo = view.findViewById(R.id.llAsphaltInfo);
        }
    }

    public static class MixtureViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvTaskId;
        TextView tvExperimenter;
        TextView tvCompletionTime;
        TextView tvExperimentContent;
        TextView tvDevices;
        LinearLayout llMixRatios;

        public MixtureViewHolder(View view) {
            super(view);
            tvTaskName = view.findViewById(R.id.tvTaskName);
            tvTaskId = view.findViewById(R.id.tvTaskId);
            tvExperimenter = view.findViewById(R.id.tvExperimenter);
            tvCompletionTime = view.findViewById(R.id.tvCompletionTime);
            tvExperimentContent = view.findViewById(R.id.tvExperimentContent);
            tvDevices = view.findViewById(R.id.tvDevices);
            llMixRatios = view.findViewById(R.id.llMixRatios);
        }
    }
}
