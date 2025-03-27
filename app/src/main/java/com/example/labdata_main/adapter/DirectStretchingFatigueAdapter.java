package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
        }
    }
}
