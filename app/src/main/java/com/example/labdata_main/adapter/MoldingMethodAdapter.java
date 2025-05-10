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

import java.util.List;

public class MoldingMethodAdapter extends RecyclerView.Adapter<MoldingMethodAdapter.MoldingMethodViewHolder> {
    private List<MoldingMethod> moldingMethods;
    private OnDeleteClickListener deleteClickListener;
    private Context context;

    public interface OnDeleteClickListener {
        void onDeleteClick(MoldingMethod method);
    }

    public MoldingMethodAdapter(List<MoldingMethod> moldingMethods, OnDeleteClickListener deleteClickListener) {
        this.moldingMethods = moldingMethods;
        this.deleteClickListener = deleteClickListener;
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
        
        String mixingDetails = String.format("拌合温度: %.1f℃\n拌合速度: %.1f r/min\n拌合时间: %.1f s", 
            method.getMixingTemperature(), 
            method.getMixingSpeed(), 
            method.getMixingTime());
        
        holder.tvMixingMethodDetails.setText(mixingDetails);
        holder.tvCompactionMethodDetails.setText("压实方法: " + method.getCompactionMethod());
        
        holder.btnDeleteMoldingMethod.setOnClickListener(v -> showDeleteConfirmationDialog(position));
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
            .setTitle("删除制件方式")
            .setMessage("确定要删除这个制件方式吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                MoldingMethod methodToDelete = moldingMethods.get(position);
                moldingMethods.remove(position);
                notifyItemRemoved(position);
                deleteClickListener.onDeleteClick(methodToDelete);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    static class MoldingMethodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixingMethodDetails;
        TextView tvCompactionMethodDetails;
        ImageButton btnDeleteMoldingMethod;

        MoldingMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixingMethodDetails = itemView.findViewById(R.id.tvMixingMethodDetails);
            tvCompactionMethodDetails = itemView.findViewById(R.id.tvCompactionMethodDetails);
            btnDeleteMoldingMethod = itemView.findViewById(R.id.btnDeleteMoldingMethod);
        }
    }
}
