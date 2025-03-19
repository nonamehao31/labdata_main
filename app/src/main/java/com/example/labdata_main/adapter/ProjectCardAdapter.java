package com.example.labdata_main.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.GenerateSpecimenCodeActivity;
import com.example.labdata_main.RecordExperimentDataActivity;
import com.example.labdata_main.RecordMixtureExperimentDataActivity;
import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.service.ServiceCreator;
import com.example.labdata_main.api.service.MixtureTaskService;
import com.example.labdata_main.fragment.BottomSheetMixRatioDetailFragment;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.ProjectStep;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.ProjectCardViewHolder> {
    private List<ExperimentTask> tasks = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
    private OnProjectCardActionListener listener;
    public static final int RECORD_EXPERIMENT_DATA_REQUEST = 1001;

    public interface OnProjectCardActionListener {
        void onViewMixRatios(ExperimentTask task);
        void onViewSpecimenCode(ExperimentTask task);
        void onGenerateSpecimenCode(ExperimentTask task);
        void onRecordExperimentData(ExperimentTask task);
        void onTaskUpdated(ExperimentTask task);
    }

    public void setOnProjectCardActionListener(OnProjectCardActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<ExperimentTask> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            for (ExperimentTask task : tasks) {
                // 只添加未完成的任务
                if (!"已完成".equals(task.getStatus())) {
                    this.tasks.add(task);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectCardViewHolder holder, int position) {
        ExperimentTask task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ProjectCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProjectTitle;
        private final TextView tvStep1Status;
        private final TextView tvStep2Status;
        private final TextView tvStep3Status;
        private final Button btnStep1Action;
        private final Button btnStep2Action;
        private final Button btnStep3Action;
        private final View step1Circle;
        private final View step2Circle;
        private final View step3Circle;
        private final MixtureTaskService mixtureTaskService = ServiceCreator.createMixtureTaskService();

        public ProjectCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvStep1Status = itemView.findViewById(R.id.tvStep1Status);
            tvStep2Status = itemView.findViewById(R.id.tvStep2Status);
            tvStep3Status = itemView.findViewById(R.id.tvStep3Status);
            btnStep1Action = itemView.findViewById(R.id.btnStep1Action);
            btnStep2Action = itemView.findViewById(R.id.btnStep2Action);
            btnStep3Action = itemView.findViewById(R.id.btnStep3Action);
            step1Circle = itemView.findViewById(R.id.step1Circle);
            step2Circle = itemView.findViewById(R.id.step2Circle);
            step3Circle = itemView.findViewById(R.id.step3Circle);
        }

        public void bind(ExperimentTask task) {
            tvProjectTitle.setText(task.getTaskName());

            // 查询任务的状态信息
            String taskIdPrefix = getTaskIdPrefix(task.getTaskId());
            updateTaskStatusUI(taskIdPrefix, task);

            // 设置步骤1状态：显示配比信息
            StringBuilder mixRatioInfo = new StringBuilder();
            List<MixRatio> mixRatios = task.getSelectedMixRatios();
            if (mixRatios != null && !mixRatios.isEmpty()) {
                for (int i = 0; i < mixRatios.size(); i++) {
                    if (i > 0) mixRatioInfo.append("\n");
                    mixRatioInfo.append("配比").append(i + 1).append(": ")
                            .append(mixRatios.get(i).getName());
                }
                tvStep1Status.setText(mixRatioInfo.toString());
            } else {
                tvStep1Status.setText("未设置配比");
            }

            // 设置步骤2状态：显示拌合和压实参数
            String methodStr = task.getMoldingMethod();
            if (methodStr != null && !methodStr.isEmpty()) {
                String[] parts = methodStr.split("\\|");
                float temp = 0, speed = 0, time = 0;
                String method = "";
                
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
                                method = value;
                                break;
                        }
                    }
                }
                
                String moldingInfo = String.format("拌合温度：%.1f℃\n拌合速度：%.1f rpm\n拌合时间：%.1f min\n压实方式：%s",
                        temp, speed, time, method);
                tvStep2Status.setText(moldingInfo);
            } else {
                tvStep2Status.setText("未设置制件参数");
            }

            // 设置步骤3状态：显示实验指派信息
            StringBuilder experimentInfo = new StringBuilder();
            Map<Long, List<String>> experimentAssignments = task.getExperimentAssignments();
            if (experimentAssignments != null && !experimentAssignments.isEmpty()) {
                List<MixRatio> selectedMixRatios = task.getSelectedMixRatios();
                if (selectedMixRatios != null) {
                    for (MixRatio mixRatio : selectedMixRatios) {
                        List<String> experiments = experimentAssignments.get(mixRatio.getId());
                        if (experiments != null && !experiments.isEmpty()) {
                            if (experimentInfo.length() > 0) experimentInfo.append("\n");
                            experimentInfo.append(mixRatio.getName()).append(":\n");
                            for (String experiment : experiments) {
                                experimentInfo.append("- ").append(experiment).append("\n");
                            }
                        }
                    }
                }
                tvStep3Status.setText(experimentInfo.toString().trim());
            } else {
                tvStep3Status.setText("未指派实验");
            }

            // 按钮事件将在任务状态更新完成后设置
        }

        /**
         * 获取任务ID的前缀部分（用于查询具有相同前缀的任务）
         * 示例: TASK_20230315_001_1 -> TASK_20230315_001
         */
        private String getTaskIdPrefix(String taskId) {
            if (taskId == null || taskId.isEmpty()) return "";
            
            int lastUnderscoreIndex = taskId.lastIndexOf("_");
            if (lastUnderscoreIndex > 0) {
                return taskId.substring(0, lastUnderscoreIndex);
            }
            return taskId;
        }

        /**
         * 查询任务状态并更新UI
         */
        private void updateTaskStatusUI(String taskIdPrefix, ExperimentTask task) {
            // 设置默认状态
            step1Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
            step2Circle.setBackgroundResource(R.drawable.step_circle_background);
            step3Circle.setBackgroundResource(R.drawable.step_circle_background);
            
            mixtureTaskService.getTaskStatusByPrefix(taskIdPrefix).enqueue(new Callback<ApiResponse<List<Map<String, String>>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Map<String, String>>>> call, Response<ApiResponse<List<Map<String, String>>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, String>> taskStatusList = response.body().getData();
                        if (taskStatusList != null && !taskStatusList.isEmpty()) {
                            // 检查所有任务的准备状态
                            boolean allPrepareFinished = true;
                            boolean allMakingFinished = true;
                            boolean allTestingFinished = true;
                            boolean anyTestingFinished = false;
                            
                            for (Map<String, String> taskStatus : taskStatusList) {
                                String prepareStatus = taskStatus.get("prepareStatus");
                                String makingStatus = taskStatus.get("makingStatus");
                                String testingStatus = taskStatus.get("testingStatus");
                                
                                if (!"finished".equals(prepareStatus)) {
                                    allPrepareFinished = false;
                                }
                                
                                if (!"finished".equals(makingStatus)) {
                                    allMakingFinished = false;
                                }
                                
                                if (!"finished".equals(testingStatus)) {
                                    allTestingFinished = false;
                                } else {
                                    anyTestingFinished = true;
                                }
                            }
                            
                            // 更新UI状态
                            if (allPrepareFinished) {
                                step1Circle.setBackgroundResource(R.drawable.circle_background_completed);
                                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                            } else {
                                step1Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                            }
                            
                            if (allMakingFinished) {
                                step2Circle.setBackgroundResource(R.drawable.circle_background_completed);
                                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                            } else if (allPrepareFinished) {
                                step2Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                            } else {
                                step2Circle.setBackgroundResource(R.drawable.step_circle_background);
                                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.black));
                            }
                            
                            if (allTestingFinished) {
                                step3Circle.setBackgroundResource(R.drawable.circle_background_completed);
                                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                            } else if (anyTestingFinished) {
                                step3Circle.setBackgroundResource(R.drawable.red_light);
                                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                            } else if (allMakingFinished) {
                                step3Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                            } else {
                                step3Circle.setBackgroundResource(R.drawable.step_circle_background);
                                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.black));
                            }
                            
                            // 设置按钮可用性
                            btnStep2Action.setEnabled(allPrepareFinished);
                            
                            // 设置按钮文本和事件
                            configureButtonEvents(task, allPrepareFinished, allMakingFinished, allTestingFinished);
                        }
                    } else {
                        // 如果API调用失败，使用默认UI状态设置
                        configureDefaultUI(task);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<List<Map<String, String>>>> call, Throwable t) {
                    // 如果API调用失败，使用默认UI状态设置
                    configureDefaultUI(task);
                }
            });
        }
        
        /**
         * 配置默认的UI状态（当API调用失败时使用）
         */
        private void configureDefaultUI(ExperimentTask task) {
            // 使用现有逻辑设置UI状态
            if (task.getExperimentCompletionTime() > 0) {
                // 所有步骤完成
                step1Circle.setBackgroundResource(R.drawable.circle_background_completed);
                step2Circle.setBackgroundResource(R.drawable.circle_background_completed);
                step3Circle.setBackgroundResource(R.drawable.circle_background_completed);
                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if (task.getSpecimenGenerationTime() > 0) {
                // 制件完成，实验进行中
                step1Circle.setBackgroundResource(R.drawable.circle_background_completed);
                step2Circle.setBackgroundResource(R.drawable.circle_background_completed);
                step3Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
            } else if (task.getPreparationTime() > 0) {
                // 备料完成，制件进行中
                step1Circle.setBackgroundResource(R.drawable.circle_background_completed);
                step2Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                step3Circle.setBackgroundResource(R.drawable.step_circle_background);
                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.black));
            } else {
                // 备料进行中
                step1Circle.setBackgroundResource(R.drawable.circle_background_in_progress);
                step2Circle.setBackgroundResource(R.drawable.step_circle_background);
                step3Circle.setBackgroundResource(R.drawable.step_circle_background);
                tvStep1Status.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                tvStep2Status.setTextColor(itemView.getContext().getColor(android.R.color.black));
                tvStep3Status.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }
            
            // 配置按钮
            configureButtonEvents(task, task.getPreparationTime() > 0, task.getSpecimenGenerationTime() > 0, task.getExperimentCompletionTime() > 0);
        }
        
        /**
         * 配置按钮文本和事件
         */
        private void configureButtonEvents(ExperimentTask task, boolean prepareFinished, boolean makingFinished, boolean testingFinished) {
            if (testingFinished) {
                btnStep2Action.setText("查看试件码");
                btnStep2Action.setEnabled(true);
                btnStep3Action.setText("查看实验数据");
            } else if (makingFinished) {
                btnStep2Action.setText("查看试件码");
                btnStep2Action.setEnabled(true);
                btnStep3Action.setText("记录实验数据");
            } else if (prepareFinished) {
                btnStep2Action.setText("生成试件码");
                btnStep2Action.setEnabled(true);
                btnStep3Action.setText("记录实验数据");
            } else {
                btnStep2Action.setText("生成试件码");
                btnStep2Action.setEnabled(false);
                btnStep3Action.setText("记录实验数据");
            }
            
            // 第三步按钮始终启用
            btnStep3Action.setEnabled(true);
            
            // 设置按钮点击事件
            btnStep1Action.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewMixRatios(task);
                }
            });
            
            btnStep2Action.setOnClickListener(v -> {
                if (listener != null) {
                    if (makingFinished) {
                        // 如果已经生成过试件码，显示查看界面
                        listener.onViewSpecimenCode(task);
                    } else {
                        // 启动新的Activity来生成试件码
                        Intent intent = new Intent(v.getContext(), GenerateSpecimenCodeActivity.class);
                        intent.putExtra("taskId", task.getTaskId());  
                        v.getContext().startActivity(intent);
                    }
                }
            });
            
            btnStep3Action.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), RecordMixtureExperimentDataActivity.class);
                intent.putExtra("taskId", task.getTaskId());
                ((FragmentActivity)itemView.getContext()).startActivityForResult(intent, RECORD_EXPERIMENT_DATA_REQUEST);
            });
        }
    }
}
