package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.MixRatio;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixRatioExperimentsAdapter extends RecyclerView.Adapter<MixRatioExperimentsAdapter.ViewHolder> {
    private List<MixRatio> mixRatios = new ArrayList<>();
    private Map<Long, List<String>> experimentAssignments;

    public void setData(List<MixRatio> mixRatios, Map<Long, List<String>> experimentAssignments) {
        this.mixRatios = mixRatios;
        this.experimentAssignments = experimentAssignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mix_ratio_experiments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        MixRatio mixRatio = mixRatios.get(position);
        
        // 设置配比名称
        holder.tvMixRatioName.setText(mixRatio.getName());
        
        // 清除现有的实验芯片
        holder.chipGroupExperiments.removeAllViews();
        
        // 添加该配比的实验芯片
        List<String> experiments = experimentAssignments.get(mixRatio.getId());
        if (experiments != null) {
            for (String experiment : experiments) {
                Chip chip = new Chip(context);
                chip.setText(getExperimentDisplayName(experiment));
                chip.setClickable(false);
                holder.chipGroupExperiments.addView(chip);
            }
        }
    }

    private String getExperimentDisplayName(String experimentType) {
        switch (experimentType) {
            case "compression":
                return "抗压强度";
            case "flexural":
                return "抗折强度";
            case "splitting":
                return "劈裂强度";
            case "elastic":
                return "弹性模量";
            default:
                return experimentType;
        }
    }

    @Override
    public int getItemCount() {
        return mixRatios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixRatioName;
        ChipGroup chipGroupExperiments;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            chipGroupExperiments = itemView.findViewById(R.id.chipGroupExperiments);
        }
    }
}
