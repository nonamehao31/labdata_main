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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                    
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        String paramName = PARAMETER_MAP.getOrDefault(entry.getKey(), entry.getKey());
                        contentBuilder.append(paramName).append(": ").append(entry.getValue()).append("\n");
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
