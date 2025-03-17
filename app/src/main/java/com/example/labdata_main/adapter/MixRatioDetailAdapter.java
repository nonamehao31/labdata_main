package com.example.labdata_main.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.view.PieChartView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixRatioDetailAdapter extends RecyclerView.Adapter<MixRatioDetailAdapter.MixRatioDetailViewHolder> {
    private List<MixRatio> mixRatios = new ArrayList<>();
    private Map<Long, List<String>> experimentAssignments;

    public void setData(List<MixRatio> mixRatios, Map<Long, List<String>> experimentAssignments) {
        this.mixRatios = mixRatios;
        this.experimentAssignments = experimentAssignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MixRatioDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mix_ratio_detail, parent, false);
        return new MixRatioDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MixRatioDetailViewHolder holder, int position) {
        MixRatio mixRatio = mixRatios.get(position);
        
        // 记录配比信息，辅助调试
        List<MaterialItem> materials = mixRatio.getMaterials();
        Log.d("MixRatioDetailAdapter", "绑定配比: " + mixRatio.getName() 
            + ", ID: " + mixRatio.getId()
            + ", 材料数量: " + (materials != null ? materials.size() : "null"));
        
        // 安全地获取实验指派，如果为null则传递一个空列表
        List<String> assignments = null;
        if (experimentAssignments != null) {
            assignments = experimentAssignments.get(mixRatio.getId());
            Log.d("MixRatioDetailAdapter", "实验指派: " + (assignments != null ? assignments.size() : "null") 
                + " 个指派, 配比ID: " + mixRatio.getId());
        } else {
            Log.d("MixRatioDetailAdapter", "无实验指派数据");
        }
        
        holder.bind(mixRatio, assignments);
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
    }
    
    /**
     * 获取实验指派信息
     * 
     * @return 实验指派信息映射表，如果为空则返回新的HashMap
     */
    public Map<Long, List<String>> getExperimentAssignments() {
        return experimentAssignments != null ? experimentAssignments : new HashMap<>();
    }

    static class MixRatioDetailViewHolder extends RecyclerView.ViewHolder {
        private final TextView mixRatioName;
        private final PieChartView pieChart;
        private final LinearLayout legendContainer;
        private final ChipGroup experimentChips;
        private final int[] colors = {
            Color.parseColor("#FF6384"),
            Color.parseColor("#36A2EB"),
            Color.parseColor("#FFCE56"),
            Color.parseColor("#4BC0C0"),
            Color.parseColor("#9966FF")
        };

        public MixRatioDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            mixRatioName = itemView.findViewById(R.id.mix_ratio_name);
            pieChart = itemView.findViewById(R.id.pie_chart);
            legendContainer = itemView.findViewById(R.id.legend_container);
            experimentChips = itemView.findViewById(R.id.experiment_chips);
        }

        public void bind(MixRatio mixRatio, List<String> experiments) {
            // 设置配比名称
            mixRatioName.setText(mixRatio.getName());

            // 获取材料列表并确保不为null
            List<MaterialItem> materials = mixRatio.getMaterials();
            if (materials == null) {
                materials = new ArrayList<>();
                Log.w("MixRatioDetailAdapter", "材料列表为空：" + mixRatio.getName());
            }
            
            if (materials.isEmpty()) {
                Log.w("MixRatioDetailAdapter", "配比没有材料，可能导致饼图和图例不显示: " + mixRatio.getName());
                // 创建一个占位材料，避免饼图显示为空
                MaterialItem placeholder = new MaterialItem();
                placeholder.setName("无材料信息");
                placeholder.setAmount("100%");
                materials.add(placeholder);
            }
            
            // 设置饼图数据
            try {
                pieChart.setMaterials(materials);
            } catch (Exception e) {
                Log.e("MixRatioDetailAdapter", "设置饼图数据异常: " + e.getMessage());
                e.printStackTrace();
            }

            // 清除旧的图例
            legendContainer.removeAllViews();

            // 添加图例
            for (int i = 0; i < materials.size(); i++) {
                MaterialItem material = materials.get(i);
                View legendItem = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_pie_chart_legend, legendContainer, false);

                TextView colorBox = legendItem.findViewById(R.id.tvLegendColor);
                TextView name = legendItem.findViewById(R.id.tvLegendName);
                TextView percentage = legendItem.findViewById(R.id.tvLegendPercentage);

                colorBox.setBackgroundColor(colors[i % colors.length]);
                name.setText(material.getName());
                percentage.setText(material.getAmount());

                legendContainer.addView(legendItem);
            }

            // 清除旧的实验芯片
            experimentChips.removeAllViews();

            // 添加实验芯片
            if (experiments != null && !experiments.isEmpty()) {
                Log.d("MixRatioDetailAdapter", "Adding experiments for mix ratio: " + mixRatio.getName());
                Log.d("MixRatioDetailAdapter", "Experiments: " + experiments.toString());
                
                for (String experiment : experiments) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(experiment);
                    chip.setClickable(false);
                    chip.setChipBackgroundColorResource(R.color.gray_light);
                    chip.setTextColor(Color.WHITE);
                    chip.setTextSize(12);
                    chip.setChipStrokeWidth(0);
                    chip.setEnsureMinTouchTargetSize(false);
                    experimentChips.addView(chip);
                }
                experimentChips.setVisibility(View.VISIBLE);
            } else {
                Log.d("MixRatioDetailAdapter", "No experiments for mix ratio: " + mixRatio.getName());
                experimentChips.setVisibility(View.GONE);
            }
        }
    }
}
