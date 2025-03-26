package com.example.labdata_main.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.CompletedExperimentTask;
import com.example.labdata_main.model.ExperimentData;
import com.example.labdata_main.model.ExperimentTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 已完成实验适配器
 */
public class CompletedExperimentAdapter extends RecyclerView.Adapter<CompletedExperimentAdapter.ViewHolder> {
    private static final String TAG = "CompletedExpAdapter";
    private final Context context;
    private final List<CompletedExperimentTask> tasks = new ArrayList<>();
    private final List<TaskWithData> tasksWithData = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private boolean isAnalysisMode = false;  // 是否为分析模式（包含详细数据）

    public CompletedExperimentAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_completed_experiment_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompletedExperimentTask task;
        
        if (isAnalysisMode && position < tasksWithData.size()) {
            // 从TaskWithData获取任务
            task = convertTaskToCompletedExperimentTask(tasksWithData.get(position));
        } else {
            // 直接使用普通任务
            task = tasks.get(position);
        }
        
        // 设置基本信息
        holder.tvExperimentType.setText(task.getExperimentType());
        holder.tvExperimentName.setText(task.getExperimentName());
        holder.tvTaskId.setText("任务ID: " + task.getTaskId());
        holder.tvTaskAssignment.setText(task.getTaskAssignment());
        holder.tvExperimenter.setText(task.getExperimenter());
        holder.tvAcceptTime.setText(CompletedExperimentTask.formatTime(task.getAcceptTime()));
        holder.tvCompletionTime.setText(CompletedExperimentTask.formatTime(task.getCompletionTime()));
        
        // 根据任务类型显示不同的附加信息
        if (task.isMixtureTask()) {
            // 显示混合料特有信息
            holder.llMixtureInfo.setVisibility(View.VISIBLE);
            holder.llAsphaltInfo.setVisibility(View.GONE);
            
            // 配比名称可能包含ID和压实方法，所以显示原始配比名
            holder.tvMixName.setText(task.getMixName());
            holder.tvCompactionMethod.setText(task.getCompactionMethod());
            
            // 记录日志以便调试
            Log.d(TAG, "绑定混合料任务: " + task.getTaskId() + 
                  ", 配比: " + task.getMixName() + 
                  ", 压实方法: " + task.getCompactionMethod());
        } else {
            // 沥青任务，隐藏混合料信息，显示沥青信息
            holder.llMixtureInfo.setVisibility(View.GONE);
            holder.llAsphaltInfo.setVisibility(View.VISIBLE);
            
            // 显示沥青实验类型
            String asphaltType = task.getTaskName() != null ? task.getTaskName() : "未知沥青实验";
            holder.tvAsphaltExperimentType.setText(asphaltType);
            
            // 记录日志以便调试
            Log.d(TAG, "绑定沥青任务: " + task.getTaskId() +
                 ", 实验类型: " + asphaltType);
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                if (isAnalysisMode && position < tasksWithData.size()) {
                    // 如果是分析模式，则传递带数据的任务
                    onItemClickListener.onItemClick(task, position);
                } else {
                    onItemClickListener.onItemClick(task, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return isAnalysisMode ? tasksWithData.size() : tasks.size();
    }

    /**
     * 更新数据
     */
    public void updateData(List<CompletedExperimentTask> newTasks) {
        Log.d(TAG, "更新数据: " + (newTasks != null ? newTasks.size() : 0) + " 个任务");
        isAnalysisMode = false;
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }
    
    /**
     * 设置带详细数据的任务（用于分析模式）
     */
    public void setTasks(List<TaskWithData> tasks) {
        Log.d(TAG, "设置分析模式任务: " + (tasks != null ? tasks.size() : 0) + " 个任务");
        isAnalysisMode = true;
        tasksWithData.clear();
        if (tasks != null) {
            tasksWithData.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    /**
     * 将ExperimentTask转换为CompletedExperimentTask
     */
    private CompletedExperimentTask convertTaskToCompletedExperimentTask(TaskWithData taskWithData) {
        ExperimentTask task = taskWithData.task;
        
        CompletedExperimentTask completedTask = new CompletedExperimentTask();
        completedTask.setTaskId(task.getTaskId());
        completedTask.setExperimentType("混合料实验"); // 默认类型
        completedTask.setExperimentName(task.getTaskName());
        
        // 使用任务分配ID作为任务分配信息
        completedTask.setTaskAssignment(task.getTaskAssignmentId() != null ? 
                                       task.getTaskAssignmentId() : "");
        
        completedTask.setExperimenter(task.getExperimenter());
        
        // 使用准备时间作为接受时间，完成时间作为完成时间
        completedTask.setAcceptTime(task.getPreparationTime());
        completedTask.setCompletionTime(task.getExperimentCompletionTime());
        
        // 从ExperimentData中获取额外信息
        if (taskWithData.dataList != null && !taskWithData.dataList.isEmpty()) {
            ExperimentData data = taskWithData.dataList.get(0);
            if (data.getExperimentName() != null) {
                completedTask.setExperimentName(data.getExperimentName());
            }
            
            // 设置混合料信息（如果有）
            // 使用getMixRatio方法获取配比
            if (data.getMixRatio() != null) {
                // 配比名称处理，确保有ID和压实方法等信息以区分同名配比
                String mixRatio = data.getMixRatio();
                
                // 检查混合料名称是否包含ID和压实方法，如果没有则尝试添加
                if (!mixRatio.contains("(ID:") && !mixRatio.contains("压实")) {
                    // 在实际应用中，应该从其他字段或数据中提取这些信息
                    // 这里仅作为示例
                    Log.d(TAG, "配比名称不包含ID和压实方法，原始名称: " + mixRatio);
                }
                
                completedTask.setMixName(mixRatio);
            } else {
                completedTask.setMixName("未知配比");
            }
            
            // 设置压实方法，这里假设可能需要从结果或其他字段中提取
            // 实际字段应根据数据库和API响应结构调整
            String compactionMethod = "未知";
            if (data.getResult() != null && data.getResult().contains("压实方法")) {
                // 从结果中提取压实方法信息（示例逻辑）
                compactionMethod = "从结果提取的压实方法";
            }
            completedTask.setCompactionMethod(compactionMethod);
            
            Log.d(TAG, "从实验数据提取信息 - 实验名称: " + data.getExperimentName() + 
                  ", 配比: " + data.getMixRatio() + 
                  ", 结果: " + data.getResult());
        }
        
        return completedTask;
    }

    /**
     * 设置点击监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * 视图持有者
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // 基本信息视图
        TextView tvExperimentType;
        TextView tvExperimentName;
        TextView tvTaskId;
        TextView tvTaskAssignment;
        TextView tvExperimenter;
        TextView tvAcceptTime;
        TextView tvCompletionTime;
        
        // 混合料特有信息
        LinearLayout llMixtureInfo;
        TextView tvMixName;
        TextView tvCompactionMethod;
        
        // 沥青特有信息
        LinearLayout llAsphaltInfo;
        TextView tvAsphaltExperimentType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 查找基本信息视图
            tvExperimentType = itemView.findViewById(R.id.tvExperimentType);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvTaskId = itemView.findViewById(R.id.tvTaskId);
            tvTaskAssignment = itemView.findViewById(R.id.tvTaskAssignment);
            tvExperimenter = itemView.findViewById(R.id.tvExperimenter);
            tvAcceptTime = itemView.findViewById(R.id.tvAcceptTime);
            tvCompletionTime = itemView.findViewById(R.id.tvCompletionTime);
            
            // 查找混合料特有信息视图
            llMixtureInfo = itemView.findViewById(R.id.llMixtureInfo);
            tvMixName = itemView.findViewById(R.id.tvMixName);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            
            // 查找沥青特有信息视图
            llAsphaltInfo = itemView.findViewById(R.id.llAsphaltInfo);
            tvAsphaltExperimentType = itemView.findViewById(R.id.tvAsphaltExperimentType);
        }
    }

    /**
     * 点击事件监听器
     */
    public interface OnItemClickListener {
        void onItemClick(CompletedExperimentTask task, int position);
    }
    
    /**
     * 带实验数据的任务
     */
    public static class TaskWithData {
        private final ExperimentTask task;
        private final List<ExperimentData> dataList;
        
        public TaskWithData(ExperimentTask task, List<ExperimentData> dataList) {
            this.task = task;
            this.dataList = dataList;
        }
        
        public ExperimentTask getTask() {
            return task;
        }
        
        public List<ExperimentData> getDataList() {
            return dataList;
        }
    }
}
