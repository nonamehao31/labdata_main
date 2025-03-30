package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;

import java.util.List;
import java.util.Map;

/**
 * 沥青实验指派适配器，用于选择实验界面
 */
public class AsphaltExperimentAssignmentAdapter extends RecyclerView.Adapter<AsphaltExperimentAssignmentAdapter.ExperimentViewHolder> {

    private List<String> experimentTypes;
    private Map<String, String> experimentStatusMap;
    private OnExperimentClickListener listener;

    public AsphaltExperimentAssignmentAdapter(List<String> experimentTypes, Map<String, String> experimentStatusMap) {
        this.experimentTypes = experimentTypes;
        this.experimentStatusMap = experimentStatusMap;
    }

    @NonNull
    @Override
    public ExperimentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asphalt_experiment_assignment, parent, false);
        return new ExperimentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperimentViewHolder holder, int position) {
        String experimentType = experimentTypes.get(position);
        
        // 格式化实验类型名称，使其更易读
        String displayName = formatExperimentTypeName(experimentType);
        holder.tvExperimentType.setText(displayName);
        
        // 设置实验状态
        String status = experimentStatusMap.get(experimentType);
        if (status != null) {
            holder.tvExperimentStatus.setText(status);
            
            // 根据状态设置不同颜色
            if ("已完成".equals(status)) {
                // 设置状态文本颜色
                holder.tvExperimentStatus.setTextColor(
                        holder.itemView.getContext().getResources().getColor(R.color.step_completed));
                // 设置实验图标为绿色
                holder.ivExperimentIcon.setColorFilter(
                        holder.itemView.getContext().getResources().getColor(R.color.step_completed));
            } else if ("进行中".equals(status)) {
                holder.tvExperimentStatus.setTextColor(
                        holder.itemView.getContext().getResources().getColor(R.color.blue_light_custom));
                // 设置实验图标为蓝色
                holder.ivExperimentIcon.setColorFilter(
                        holder.itemView.getContext().getResources().getColor(R.color.blue_theme));
            } else {
                holder.tvExperimentStatus.setTextColor(
                        holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
                // 设置实验图标为蓝色
                holder.ivExperimentIcon.setColorFilter(
                        holder.itemView.getContext().getResources().getColor(R.color.blue_theme));
            }
        } else {
            holder.tvExperimentStatus.setText("未开始");
            // 默认设置实验图标为蓝色
            holder.ivExperimentIcon.setColorFilter(
                    holder.itemView.getContext().getResources().getColor(R.color.blue_theme));
        }

        // 设置按钮点击事件
        holder.btnSelectExperiment.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExperimentClick(experimentType);
            }
        });
        
        // 设置整个项目的点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExperimentClick(experimentType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return experimentTypes != null ? experimentTypes.size() : 0;
    }

    public void updateData(List<String> experimentTypes, Map<String, String> experimentStatusMap) {
        this.experimentTypes = experimentTypes;
        this.experimentStatusMap = experimentStatusMap;
        notifyDataSetChanged();
    }

    /**
     * 格式化实验类型名称，使其更易读
     * 例如：softening_point 转换为 软化点
     */
    private String formatExperimentTypeName(String experimentType) {
        if (experimentType == null) return "未知实验";
        
        String lowerCase = experimentType.toLowerCase();
        
        // 针入度相关
        if (lowerCase.contains("penetration")) {
            return "针入度";
        }
        
        // 软化点相关
        if (lowerCase.contains("soften") || lowerCase.contains("soft_point")) {
            return "软化点";
        }
        
        // 延度相关
        if (lowerCase.contains("ductility") || lowerCase.contains("duct")) {
            return "延度";
        }
        
        // 布鲁克菲尔德旋转粘度相关
        if (lowerCase.contains("brookfield") || lowerCase.contains("viscosity")) {
            return "布鲁克菲尔德旋转粘度";
        }
        
        // 弯曲梁流变仪相关
        if (lowerCase.contains("bbr") || lowerCase.contains("bending_beam") || 
            lowerCase.contains("bendingbeam") || lowerCase.contains("bending")) {
            return "弯曲梁流变仪";
        }
        
        // 动态剪切流变仪相关
        if (lowerCase.contains("dsr") || lowerCase.contains("dynamic_shear") || 
            lowerCase.contains("dynamicshear") || lowerCase.contains("shear")) {
            return "动态剪切流变仪";
        }
        
        // 针对英文显示名称的特殊处理
        if (lowerCase.equals("dynamic shear rheometer")) {
            return "动态剪切流变仪";
        }
        
        if (lowerCase.equals("bending beam rheometer")) {
            return "弯曲梁流变仪";
        }
        
        // 返回原始名称，但首字母大写
        return experimentType;
    }

    /**
     * ViewHolder类
     */
    static class ExperimentViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentType;
        TextView tvExperimentStatus;
        ImageView ivExperimentIcon;
        ImageView btnSelectExperiment;

        ExperimentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentType = itemView.findViewById(R.id.tvExperimentType);
            tvExperimentStatus = itemView.findViewById(R.id.tvExperimentStatus);
            ivExperimentIcon = itemView.findViewById(R.id.ivExperimentIcon);
            btnSelectExperiment = itemView.findViewById(R.id.btnSelectExperiment);
        }
    }

    /**
     * 设置点击监听器
     */
    public void setOnExperimentClickListener(OnExperimentClickListener listener) {
        this.listener = listener;
    }

    /**
     * 实验点击监听器接口
     */
    public interface OnExperimentClickListener {
        void onExperimentClick(String experimentType);
    }
}
