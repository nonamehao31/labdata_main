package com.example.labdata_main.adapter;

import android.graphics.Color;
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
        holder.bind(mixRatio, experimentAssignments.get(mixRatio.getId()));
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
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

            // 获取材料列表
            List<MaterialItem> materials = mixRatio.getMaterials();
            
            // 设置饼图数据
            pieChart.setMaterials(materials);

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
                percentage.setText(material.getAmount() + "%");

                legendContainer.addView(legendItem);
            }

            // 清除旧的实验芯片
            experimentChips.removeAllViews();

            // 添加实验芯片
            if (experiments != null) {
                for (String experiment : experiments) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(experiment);
                    chip.setClickable(false);
                    experimentChips.addView(chip);
                }
            }
        }
    }
}
