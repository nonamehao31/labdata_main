package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.ProjectStep;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.ProjectCardViewHolder> {
    private List<ExperimentTask> tasks = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
    private OnProjectCardActionListener listener;

    public interface OnProjectCardActionListener {
        void onViewMixRatios(ExperimentTask task);
        void onGenerateSpecimenCode(ExperimentTask task);
        void onRecordExperimentData(ExperimentTask task);
    }

    public void setOnProjectCardActionListener(OnProjectCardActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<ExperimentTask> tasks) {
        this.tasks = tasks;
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

        public ProjectCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvStep1Status = itemView.findViewById(R.id.tvStep1Status);
            tvStep2Status = itemView.findViewById(R.id.tvStep2Status);
            tvStep3Status = itemView.findViewById(R.id.tvStep3Status);
            btnStep1Action = itemView.findViewById(R.id.btnStep1Action);
            btnStep2Action = itemView.findViewById(R.id.btnStep2Action);
            btnStep3Action = itemView.findViewById(R.id.btnStep3Action);
        }

        public void bind(ExperimentTask task) {
            tvProjectTitle.setText(task.getTaskName());

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

            // 设置按钮状态
            btnStep1Action.setText("查看配比信息");

            if (task.getSpecimenGenerationTime() > 0) {
                btnStep2Action.setText("查看试件码");
            } else {
                btnStep2Action.setText("生成试件码");
                btnStep2Action.setEnabled(task.getPreparationTime() > 0);
            }

            if (task.getExperimentCompletionTime() > 0) {
                btnStep3Action.setText("查看实验数据");
            } else {
                btnStep3Action.setText("记录实验数据");
                btnStep3Action.setEnabled(task.getSpecimenGenerationTime() > 0);
            }

            // 设置按钮点击事件
            btnStep1Action.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewMixRatios(task);
                }
            });

            btnStep2Action.setOnClickListener(v -> {
                if (listener != null && btnStep2Action.isEnabled()) {
                    listener.onGenerateSpecimenCode(task);
                }
            });

            btnStep3Action.setOnClickListener(v -> {
                if (listener != null && btnStep3Action.isEnabled()) {
                    listener.onRecordExperimentData(task);
                }
            });
        }
    }
}
