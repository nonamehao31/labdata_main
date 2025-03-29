package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DirectStretchingFatigueTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 沥青混合料直接拉伸循环疲劳测黏弹损伤实验结果适配器
 */
public class DirectStretchingFatigueAdapter extends RecyclerView.Adapter<DirectStretchingFatigueAdapter.ViewHolder> {

    private List<DirectStretchingFatigueTestResponse> dataList;
    private Context context;

    public DirectStretchingFatigueAdapter(Context context, List<DirectStretchingFatigueTestResponse> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    
    public DirectStretchingFatigueAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }
    
    public void updateData(List<DirectStretchingFatigueTestResponse> newData) {
        this.dataList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_direct_stretching_fatigue_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DirectStretchingFatigueTestResponse data = dataList.get(position);
        
        // 设置实验名称
        holder.tvExperimentName.setText("沥青混合料直接拉伸循环疲劳测黏弹损伤实验");
        
        // 检查是否有试件数据
        if (data.getSpecimens() != null && !data.getSpecimens().isEmpty()) {
            DirectStretchingFatigueTestResponse.Specimen specimen = data.getSpecimens().get(0);
            
            // 设置试件标题
            holder.tvSpecimenTitle.setText("试件 #1");
            
            // 设置试件ID
            holder.tvSpecimenId.setText(specimen.getSpecimenId() != null ? specimen.getSpecimenId() : "null");
            
            // 设置直径和高度
            holder.tvDiameter.setText(String.format("%.1f mm", specimen.getDiameter()));
            holder.tvHeight.setText(String.format("%.1f mm", specimen.getHeight()));
            
            // 处理动态模量数据
            if (specimen.getModulusData() != null && !specimen.getModulusData().isEmpty()) {
                holder.tvNoModulusData.setVisibility(View.GONE);
                holder.modulusDataContainer.removeAllViews();
                
                for (DirectStretchingFatigueTestResponse.ModulusData modulusData : specimen.getModulusData()) {
                    View modulusItemView = LayoutInflater.from(context).inflate(
                            R.layout.item_modulus_data_row, holder.modulusDataContainer, false);
                    
                    // 设置数据行内容
                    TextView tvStage = modulusItemView.findViewById(R.id.tvStage);
                    TextView tvDynamicModulus = modulusItemView.findViewById(R.id.tvDynamicModulus);
                    TextView tvCycleCount = modulusItemView.findViewById(R.id.tvCycleCount);
                    TextView tvPhaseAngle = modulusItemView.findViewById(R.id.tvPhaseAngle);
                    
                    tvStage.setText("阶段: " + modulusData.getStage());
                    tvDynamicModulus.setText(String.format("动态模量: %.2f MPa", modulusData.getDynamicModulus()));
                    tvCycleCount.setText(String.format("循环次数: %d", modulusData.getCycleCount()));
                    tvPhaseAngle.setText(String.format("相位角: %.2f°", modulusData.getPhaseAngle()));
                    
                    // 添加到容器
                    holder.modulusDataContainer.addView(modulusItemView);
                }
            } else {
                holder.tvNoModulusData.setVisibility(View.VISIBLE);
            }
            
            // 处理疲劳数据
            if (specimen.getFatigueData() != null && !specimen.getFatigueData().isEmpty()) {
                holder.tvNoFatigueData.setVisibility(View.GONE);
                holder.fatigueDataContainer.removeAllViews();
                
                for (DirectStretchingFatigueTestResponse.FatigueData fatigueData : specimen.getFatigueData()) {
                    View fatigueItemView = LayoutInflater.from(context).inflate(
                            R.layout.item_fatigue_data_row, holder.fatigueDataContainer, false);
                    
                    // 设置数据行内容
                    TextView tvStage = fatigueItemView.findViewById(R.id.tvStage);
                    TextView tvDynamicModulus = fatigueItemView.findViewById(R.id.tvDynamicModulus);
                    TextView tvCycleCount = fatigueItemView.findViewById(R.id.tvCycleCount);
                    TextView tvForceLevel = fatigueItemView.findViewById(R.id.tvForceLevel);
                    
                    tvStage.setText("阶段: " + fatigueData.getStage());
                    tvDynamicModulus.setText(String.format("动态模量: %.2f MPa", fatigueData.getDynamicModulus()));
                    tvCycleCount.setText(String.format("循环次数: %d", fatigueData.getCycleCount()));
                    tvForceLevel.setText(String.format("力水平: %.2f N", fatigueData.getForceLevel()));
                    
                    // 添加到容器
                    holder.fatigueDataContainer.addView(fatigueItemView);
                }
            } else {
                holder.tvNoFatigueData.setVisibility(View.VISIBLE);
            }
        } else {
            // 如果没有试件数据，显示默认提示
            holder.tvSpecimenId.setText("null");
            holder.tvDiameter.setText("0.0 mm");
            holder.tvHeight.setText("0.0 mm");
            holder.tvNoModulusData.setVisibility(View.VISIBLE);
            holder.tvNoFatigueData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName;
        TextView tvSpecimenTitle;
        TextView tvSpecimenId;
        TextView tvDiameter;
        TextView tvHeight;
        TextView tvNoModulusData;
        TextView tvNoFatigueData;
        LinearLayout modulusDataContainer;
        LinearLayout fatigueDataContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvSpecimenTitle = itemView.findViewById(R.id.tvSpecimenTitle);
            tvSpecimenId = itemView.findViewById(R.id.tvSpecimenId);
            tvDiameter = itemView.findViewById(R.id.tvDiameter);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            tvNoModulusData = itemView.findViewById(R.id.tvNoModulusData);
            tvNoFatigueData = itemView.findViewById(R.id.tvNoFatigueData);
            modulusDataContainer = itemView.findViewById(R.id.modulusDataContainer);
            fatigueDataContainer = itemView.findViewById(R.id.fatigueDataContainer);
        }
    }
}
