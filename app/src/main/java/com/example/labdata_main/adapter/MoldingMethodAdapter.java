package com.example.labdata_main.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MoldingMethodAdapter extends RecyclerView.Adapter<MoldingMethodAdapter.MoldingMethodViewHolder> {
    private List<MoldingMethod> moldingMethods;
    private List<Integer> selectedPositions;
    private OnDeleteClickListener deleteClickListener;
    private OnItemSelectionChangedListener selectionChangedListener;
    private Context context;

    public interface OnDeleteClickListener {
        void onDeleteClick(MoldingMethod method);
    }

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(List<MoldingMethod> selectedMethods);
    }

    public MoldingMethodAdapter(List<MoldingMethod> moldingMethods, OnDeleteClickListener deleteClickListener, OnItemSelectionChangedListener selectionChangedListener) {
        this.moldingMethods = moldingMethods;
        this.deleteClickListener = deleteClickListener;
        this.selectionChangedListener = selectionChangedListener;
        this.selectedPositions = new ArrayList<>();
    }

    @NonNull
    @Override
    public MoldingMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_molding_method, parent, false);
        return new MoldingMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoldingMethodViewHolder holder, int position) {
        MoldingMethod method = moldingMethods.get(position);
        
        // 设置标题编号
        holder.tvMoldingMethodTitle.setText(String.format("制件方法%d", position + 1));
        
        String mixingDetails = String.format("拌合温度: %.1f℃\n拌合速度: %.1f r/min\n拌合时间: %.1f s", 
            method.getMixingTemperature(), 
            method.getMixingSpeed(), 
            method.getMixingTime());
        
        holder.tvMixingMethodDetails.setText(mixingDetails);
        holder.tvCompactionMethodDetails.setText("" + method.getCompactionMethod());
        
        holder.btnDeleteMoldingMethod.setOnClickListener(v -> showDeleteConfirmationDialog(position));

        // 设置选中状态
        boolean isSelected = selectedPositions.contains(position);
        holder.cardView.setSelected(isSelected);
        holder.itemView.setSelected(isSelected);

        // 设置点击事件
        holder.cardView.setOnClickListener(v -> toggleSelection(position));
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);

        // 通知选中状态变化
        List<MoldingMethod> selectedMethods = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            selectedMethods.add(moldingMethods.get(pos));
        }
        selectionChangedListener.onSelectionChanged(selectedMethods);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
            .setTitle("删除制件方式")
            .setMessage("确定要删除这个制件方式吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                MoldingMethod methodToDelete = moldingMethods.get(position);
                moldingMethods.remove(position);
                // 更新选中位置
                selectedPositions.remove(Integer.valueOf(position));
                for (int i = 0; i < selectedPositions.size(); i++) {
                    if (selectedPositions.get(i) > position) {
                        selectedPositions.set(i, selectedPositions.get(i) - 1);
                    }
                }
                notifyDataSetChanged(); // 更新所有item以刷新编号
                deleteClickListener.onDeleteClick(methodToDelete);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    public List<MoldingMethod> getSelectedMethods() {
        List<MoldingMethod> selectedMethods = new ArrayList<>();
        for (Integer position : selectedPositions) {
            selectedMethods.add(moldingMethods.get(position));
        }
        return selectedMethods;
    }

    static class MoldingMethodViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvMoldingMethodTitle;
        TextView tvMixingMethodDetails;
        TextView tvCompactionMethodDetails;
        ImageButton btnDeleteMoldingMethod;

        MoldingMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMoldingMethod);
            tvMoldingMethodTitle = itemView.findViewById(R.id.tvMoldingMethodTitle);
            tvMixingMethodDetails = itemView.findViewById(R.id.tvMixingMethodDetails);
            tvCompactionMethodDetails = itemView.findViewById(R.id.tvCompactionMethodDetails);
            btnDeleteMoldingMethod = itemView.findViewById(R.id.btnDeleteMoldingMethod);
        }
    }
}
