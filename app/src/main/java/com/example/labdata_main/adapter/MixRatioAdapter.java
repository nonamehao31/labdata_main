package com.example.labdata_main.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.MaterialItem;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.view.PieChartView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixRatioAdapter extends ListAdapter<MixRatio, MixRatioAdapter.MixRatioViewHolder> {

    private final OnMixRatioSelectedListener listener;
    private final OnMixRatioDeleteListener deleteListener;
    private final Set<Long> selectedMixRatioIds = new HashSet<>();
    private static final int[] pieChartColors = {
        Color.parseColor("#FF6384"),
        Color.parseColor("#36A2EB"),
        Color.parseColor("#FFCE56"),
        Color.parseColor("#4BC0C0"),
        Color.parseColor("#9966FF")
    };

    public MixRatioAdapter(OnMixRatioSelectedListener listener, OnMixRatioDeleteListener deleteListener) {
        super(new DiffUtil.ItemCallback<MixRatio>() {
            @Override
            public boolean areItemsTheSame(@NonNull MixRatio oldItem, @NonNull MixRatio newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull MixRatio oldItem, @NonNull MixRatio newItem) {
                String oldName = oldItem.getName() != null ? oldItem.getName() : "";
                String newName = newItem.getName() != null ? newItem.getName() : "";
                String oldDescription = oldItem.getDescription() != null ? oldItem.getDescription() : "";
                String newDescription = newItem.getDescription() != null ? newItem.getDescription() : "";
                
                return oldName.equals(newName) && oldDescription.equals(newDescription);
            }
        });
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MixRatioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mix_ratio, parent, false);
        return new MixRatioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MixRatioViewHolder holder, int position) {
        MixRatio mixRatio = getItem(position);
        
        // 设置选中状态
        holder.cardView.setChecked(selectedMixRatioIds.contains(mixRatio.getId()));
        
        holder.bind(mixRatio, selectedMixRatioIds.contains(mixRatio.getId()));

        holder.cardView.setOnClickListener(v -> {
            boolean isSelected = selectedMixRatioIds.contains(mixRatio.getId());
            if (isSelected) {
                selectedMixRatioIds.remove(mixRatio.getId());
            } else {
                selectedMixRatioIds.add(mixRatio.getId());
            }
            
            // 通知当前项更新
            notifyItemChanged(holder.getAdapterPosition());
            
            // 调用选择监听器
            if (listener != null) {
                listener.onMixRatioSelected(mixRatio);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onMixRatioDelete(mixRatio);
            }
        });
    }

    public void removeMixRatio(MixRatio mixRatio) {
        List<MixRatio> currentList = new ArrayList<>(getCurrentList());
        currentList.remove(mixRatio);
        submitList(currentList);
        selectedMixRatioIds.remove(mixRatio.getId());
    }

    static class MixRatioViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvName;
        TextView tvDescription;
        LinearLayout legendContainer;
        PieChartView pieChartMaterials;
        ImageButton btnDelete;

        public MixRatioViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvMixRatioName);
            tvDescription = itemView.findViewById(R.id.tvMixRatioDescription);
            legendContainer = itemView.findViewById(R.id.legendContainer);
            pieChartMaterials = itemView.findViewById(R.id.pieChartMaterials);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            
            // 设置卡片为可选中状态
            cardView.setCheckable(true);
        }

        public void bind(MixRatio mixRatio, boolean isSelected) {
            cardView.setChecked(isSelected);
            
            tvName.setText(mixRatio.getName());
            if (mixRatio.getDescription() != null && !mixRatio.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(mixRatio.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // 清除之前的图例
            legendContainer.removeAllViews();

            // 添加材料列表和图例
            if (mixRatio.getMaterials() != null) {
                float totalWeight = 0;
                for (MaterialItem material : mixRatio.getMaterials()) {
                    totalWeight += material.getAmount() != null ? Float.parseFloat(material.getAmount()) : 0;
                }

                float[] values = new float[mixRatio.getMaterials().size()];
                int[] colors = new int[mixRatio.getMaterials().size()];
                String[] labels = new String[mixRatio.getMaterials().size()];

                for (int i = 0; i < mixRatio.getMaterials().size(); i++) {
                    MaterialItem material = mixRatio.getMaterials().get(i);
                    
                    // 添加图例项
                    View legendItem = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_pie_chart_legend, legendContainer, false);
                    
                    TextView tvLegendColor = legendItem.findViewById(R.id.tvLegendColor);
                    TextView tvLegendName = legendItem.findViewById(R.id.tvLegendName);
                    TextView tvLegendPercentage = legendItem.findViewById(R.id.tvLegendPercentage);
                    
                    tvLegendColor.setBackgroundColor(pieChartColors[i % pieChartColors.length]);
                    tvLegendName.setText(material.getName());

                    float percentage = 0;
                    if (material.getAmount() != null && totalWeight > 0) {
                        percentage = Float.parseFloat(material.getAmount()) / totalWeight * 100;
                    }
                    tvLegendPercentage.setText(String.format("%.1f%%", percentage));
                    
                    legendContainer.addView(legendItem);

                    // 准备饼图数据
                    values[i] = percentage;
                    colors[i] = pieChartColors[i % pieChartColors.length];
                    labels[i] = material.getName();
                }

                // 更新饼图
                pieChartMaterials.setMaterials(mixRatio.getMaterials());
            }
        }
    }

    public interface OnMixRatioSelectedListener {
        void onMixRatioSelected(MixRatio mixRatio);
    }

    public interface OnMixRatioDeleteListener {
        void onMixRatioDelete(MixRatio mixRatio);
    }
}