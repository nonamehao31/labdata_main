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

public class MixRatioAdapter extends ListAdapter<MixRatio, MixRatioAdapter.MixRatioViewHolder> {

    private final OnMixRatioSelectedListener listener;
    private final OnMixRatioDeleteListener deleteListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
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
        holder.cardView.setSelected(position == selectedPosition);
        
        holder.bind(mixRatio, position == selectedPosition);

        holder.cardView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // 通知之前选中的项目更新
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            
            // 通知当前选中的项目更新
            notifyItemChanged(selectedPosition);
            
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

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        
        // 通知之前选中的项目更新
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        
        // 通知当前选中的项目更新
        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public MixRatio getSelectedMixRatio() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < getCurrentList().size()) {
            return getCurrentList().get(selectedPosition);
        }
        return null;
    }

    @Override
    public void submitList(java.util.List<MixRatio> list) {
        if (list != null) {
            Log.d("MixRatioAdapter", "Submitting list with size: " + list.size());
            for (MixRatio ratio : list) {
                Log.d("MixRatioAdapter", "MixRatio: " + ratio.getName() + ", Description: " + ratio.getDescription());
            }
        } else {
            Log.d("MixRatioAdapter", "Submitting null list");
        }
        super.submitList(list);
    }

    public void removeMixRatio(MixRatio mixRatio) {
        int position = getCurrentList().indexOf(mixRatio);
        if (position != -1) {
            getCurrentList().remove(position);
            notifyItemRemoved(position);
        }
    }

    static class MixRatioViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvDescription;
        private final PieChartView pieChartMaterials;
        private final LinearLayout legendContainer;
        private final ImageButton btnDelete;

        MixRatioViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMixRatio);
            tvName = itemView.findViewById(R.id.tvMixRatioName);
            tvDescription = itemView.findViewById(R.id.tvMixRatioDescription);
            pieChartMaterials = itemView.findViewById(R.id.pieChartMaterials);
            legendContainer = itemView.findViewById(R.id.legendContainer);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(MixRatio mixRatio, boolean isSelected) {
            // 手动设置选中状态
            cardView.setChecked(isSelected);
            
            // 如果选中，添加选中效果
            cardView.setCardBackgroundColor(isSelected 
                ? ContextCompat.getColor(itemView.getContext(), R.color.selected_mix_ratio_background)
                : ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            
            tvName.setText(mixRatio.getName());
            tvDescription.setText(mixRatio.getDescription());

            // 设置饼图
            if (mixRatio.getMaterials() != null && !mixRatio.getMaterials().isEmpty()) {
                pieChartMaterials.setMaterials(mixRatio.getMaterials());
                
                // 清除之前的图例
                legendContainer.removeAllViews();
                
                // 添加新的图例
                for (int i = 0; i < mixRatio.getMaterials().size(); i++) {
                    MaterialItem material = mixRatio.getMaterials().get(i);
                    
                    // 创建图例项
                    View legendItem = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_pie_chart_legend, legendContainer, false);
                    
                    TextView tvLegendColor = legendItem.findViewById(R.id.tvLegendColor);
                    TextView tvLegendName = legendItem.findViewById(R.id.tvLegendName);
                    TextView tvLegendPercentage = legendItem.findViewById(R.id.tvLegendPercentage);
                    
                    tvLegendColor.setBackgroundColor(pieChartColors[i % pieChartColors.length]);
                    tvLegendName.setText(material.getName());
                    
                    // 计算百分比
                    float total = 0;
                    Log.d("MixRatioAdapter", "Total materials: " + mixRatio.getMaterials().size());
                    
                    // 首先尝试从 MixRatio 中获取总量
                    String totalAmountStr = mixRatio.getTotalAmount();
                    float totalAmount = 0;
                    if (totalAmountStr != null && !totalAmountStr.trim().isEmpty()) {
                        try {
                            totalAmount = Float.parseFloat(totalAmountStr.trim());
                        } catch (NumberFormatException e) {
                            Log.e("MixRatioAdapter", "Invalid total amount: " + totalAmountStr);
                        }
                    }
                    
                    // 如果没有总量，则计算总量
                    if (totalAmount <= 0) {
                        for (MaterialItem item : mixRatio.getMaterials()) {
                            Log.d("MixRatioAdapter", "Material: " + item.getName() + ", Amount: " + item.getAmount());
                            if (item.getAmount() != null && !item.getAmount().trim().isEmpty()) {
                                try {
                                    total += Float.parseFloat(item.getAmount().trim());
                                } catch (NumberFormatException e) {
                                    Log.e("MixRatioAdapter", "Invalid amount: " + item.getAmount());
                                }
                            }
                        }
                        totalAmount = total;
                    }
                    
                    // 如果总量仍然为0，尝试使用预设的百分比
                    if (totalAmount <= 0) {
                        for (MaterialItem item : mixRatio.getMaterials()) {
                            if (item.getPercentage() > 0) {
                                totalAmount += item.getPercentage();
                            }
                        }
                    }
                    
                    Log.d("MixRatioAdapter", "Total amount: " + totalAmount);
                    
                    float percentage = 0;
                    if (material.getAmount() != null && !material.getAmount().trim().isEmpty() && totalAmount > 0) {
                        try {
                            percentage = (Float.parseFloat(material.getAmount().trim()) / totalAmount) * 100;
                        } catch (NumberFormatException e) {
                            Log.e("MixRatioAdapter", "Invalid material amount: " + material.getAmount());
                        }
                    } else if (material.getPercentage() > 0 && totalAmount > 0) {
                        // 如果没有数量，使用预设百分比
                        percentage = (material.getPercentage() / totalAmount) * 100;
                    }
                    
                    Log.d("MixRatioAdapter", "Material: " + material.getName() + ", Percentage: " + percentage);
                    tvLegendPercentage.setText(String.format("%.1f%%", percentage));
                    
                    legendContainer.addView(legendItem);
                }
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