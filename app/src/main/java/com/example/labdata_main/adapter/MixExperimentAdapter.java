package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.MixExperiment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class MixExperimentAdapter extends RecyclerView.Adapter<MixExperimentAdapter.ViewHolder> {
    private List<MixExperiment> mixExperiments;
    private OnExperimentSelectedListener listener;

    public interface OnExperimentSelectedListener {
        void onExperimentSelected(int position, String experiment, boolean isSelected);
    }

    public MixExperimentAdapter(List<MixExperiment> mixExperiments, OnExperimentSelectedListener listener) {
        this.mixExperiments = mixExperiments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mix_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MixExperiment mixExperiment = mixExperiments.get(position);
        holder.bind(mixExperiment, position);
    }

    @Override
    public int getItemCount() {
        return mixExperiments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMixTitle;
        private ChipGroup chipGroupExperiments;
        private Chip chipMarshall;
        private Chip chipBeam;
        private Chip chipElastic;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixTitle = itemView.findViewById(R.id.tvMixTitle);
            chipGroupExperiments = itemView.findViewById(R.id.chipGroupExperiments);
            chipMarshall = itemView.findViewById(R.id.chipMarshall);
            chipBeam = itemView.findViewById(R.id.chipBeam);
            chipElastic = itemView.findViewById(R.id.chipElastic);
        }

        void bind(MixExperiment mixExperiment, int position) {
            tvMixTitle.setText(mixExperiment.getMixTitle());

            // 设置选中状态
            chipMarshall.setChecked(mixExperiment.hasExperiment("马歇尔稳定度"));
            chipBeam.setChecked(mixExperiment.hasExperiment("弯曲梁"));
            chipElastic.setChecked(mixExperiment.hasExperiment("弹性模量"));

            // 设置点击监听器
            chipMarshall.setOnCheckedChangeListener((chip, isChecked) ->
                    listener.onExperimentSelected(position, "马歇尔稳定度", isChecked));

            chipBeam.setOnCheckedChangeListener((chip, isChecked) ->
                    listener.onExperimentSelected(position, "弯曲梁", isChecked));

            chipElastic.setOnCheckedChangeListener((chip, isChecked) ->
                    listener.onExperimentSelected(position, "弹性模量", isChecked));
        }
    }
}
