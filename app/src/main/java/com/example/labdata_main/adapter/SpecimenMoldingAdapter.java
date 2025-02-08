package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.fragment.GenerateSpecimenFragment.MoldingMethodInfo;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import android.widget.ImageView;

public class SpecimenMoldingAdapter extends RecyclerView.Adapter<SpecimenMoldingAdapter.ViewHolder> {
    private List<MoldingMethodInfo> moldingMethods = new ArrayList<>();
    private Set<Integer> expandedPositions = new HashSet<>();

    public void setMoldingMethods(List<MoldingMethodInfo> moldingMethods) {
        this.moldingMethods = moldingMethods;
        expandedPositions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen_molding_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoldingMethodInfo method = moldingMethods.get(position);
        boolean isExpanded = expandedPositions.contains(position);
        
        // 设置配比名称和序号
        holder.tvMixRatioName.setText(String.format("配比%d：%s", position + 1, method.getMixRatio().getName()));
        
        // 设置拌合参数
        holder.tvMixingTemp.setText(String.format("拌合温度：%.1f℃", method.getMixingTemp()));
        holder.tvMixingSpeed.setText(String.format("拌合速度：%.1f rpm", method.getMixingSpeed()));
        holder.tvMixingTime.setText(String.format("拌合时间：%.1f min", method.getMixingTime()));
        
        // 设置压实方法
        holder.tvCompactionMethod.setText(String.format("压实方法：%s", method.getCompactionMethod()));

        // 设置详细信息的可见性
        holder.detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // 设置展开/折叠指示器
        holder.expandIndicator.setRotation(isExpanded ? 180 : 0);
        
        // 设置点击事件
        holder.cardView.setOnClickListener(v -> {
            if (isExpanded) {
                expandedPositions.remove(position);
            } else {
                expandedPositions.add(position);
            }
            notifyItemChanged(position);
        });

        // 设置卡片样式
        holder.cardView.setStrokeColor(holder.cardView.getContext().getColor(
            isExpanded ? R.color.blue_light_custom : R.color.cardStrokeColor
        ));
        holder.cardView.setStrokeWidth(
            isExpanded ? 2 : 1
        );
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMixRatioName;
        private final TextView tvMixingTemp;
        private final TextView tvMixingSpeed;
        private final TextView tvMixingTime;
        private final TextView tvCompactionMethod;
        private final MaterialCardView cardView;
        private final View detailsContainer;
        private final ImageView expandIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingTemp = itemView.findViewById(R.id.tvMixingTemp);
            tvMixingSpeed = itemView.findViewById(R.id.tvMixingSpeed);
            tvMixingTime = itemView.findViewById(R.id.tvMixingTime);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            detailsContainer = itemView.findViewById(R.id.detailsContainer);
            expandIndicator = itemView.findViewById(R.id.expandIndicator);
        }
    }
}
