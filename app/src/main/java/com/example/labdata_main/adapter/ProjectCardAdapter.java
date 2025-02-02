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
import com.example.labdata_main.model.ProjectStep;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

            // 设置步骤1状态
            if (task.getPreparationTime() > 0) {
                tvStep1Status.setText("完成时间：" + timeFormat.format(task.getPreparationTime()));
                btnStep1Action.setText("查看配比信息");
            } else {
                tvStep1Status.setText("等待开始");
                btnStep1Action.setText("开始备料");
            }

            // 设置步骤2状态
            if (task.getSpecimenGenerationTime() > 0) {
                tvStep2Status.setText("完成时间：" + timeFormat.format(task.getSpecimenGenerationTime()));
                btnStep2Action.setText("查看试件码");
            } else {
                tvStep2Status.setText(task.getPreparationTime() > 0 ? "等待开始" : "请先完成备料");
                btnStep2Action.setText("生成试件码");
                btnStep2Action.setEnabled(task.getPreparationTime() > 0);
            }

            // 设置步骤3状态
            if (task.getExperimentCompletionTime() > 0) {
                tvStep3Status.setText("完成时间：" + timeFormat.format(task.getExperimentCompletionTime()));
                btnStep3Action.setText("查看实验数据");
            } else {
                tvStep3Status.setText(task.getSpecimenGenerationTime() > 0 ? "等待开始" : "请先生成试件码");
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
