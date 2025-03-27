package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DirectStretchingFatigueTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 沥青混合料直接拉伸循环疲劳测黏弹损伤实验试件数据适配器
 */
public class DirectStretchingFatigueSpecimenAdapter extends RecyclerView.Adapter<DirectStretchingFatigueSpecimenAdapter.ViewHolder> {

    private List<DirectStretchingFatigueTestResponse.Specimen> specimens;
    private Context context;

    public DirectStretchingFatigueSpecimenAdapter(Context context, List<DirectStretchingFatigueTestResponse.Specimen> specimens) {
        this.context = context;
        this.specimens = specimens != null ? specimens : new ArrayList<>();
    }
    
    public void updateData(List<DirectStretchingFatigueTestResponse.Specimen> newSpecimens) {
        this.specimens = newSpecimens != null ? newSpecimens : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_direct_stretching_fatigue_specimen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DirectStretchingFatigueTestResponse.Specimen specimen = specimens.get(position);
        
        // 设置实验标题（仅在第一个试件显示）
        if (position == 0) {
            holder.tvExperimentTitle.setVisibility(View.VISIBLE);
            holder.tvExperimentTitle.setText("沥青混合料直接拉伸循环疲劳测黏弹损伤实验");
        } else {
            holder.tvExperimentTitle.setVisibility(View.GONE);
        }
        
        // 设置试件基本信息
        holder.tvSpecimenTitle.setText("试件 #" + (position + 1));
        holder.tvSpecimenId.setText("试件ID: " + specimen.getSpecimenId());
        holder.tvDiameter.setText("直径: " + specimen.getDiameter() + " mm");
        holder.tvHeight.setText("高度: " + specimen.getHeight() + " mm");
        
        // 清除旧数据
        holder.modulusDataContainer.removeAllViews();
        holder.fatigueDataContainer.removeAllViews();
        
        // 设置动态模量数据
        if (specimen.getModulusData() != null && !specimen.getModulusData().isEmpty()) {
            // 创建表格
            TableLayout modulusTable = createDataTable(
                    new String[]{"阶段", "动态模量(MPa)", "循环次数", "相位角(°)", "力水平", "均衡应变", "温度(°C)"},
                    specimen.getModulusData());
            holder.modulusDataContainer.addView(modulusTable);
        } else {
            TextView noDataText = new TextView(context);
            noDataText.setText("无动态模量数据");
            holder.modulusDataContainer.addView(noDataText);
        }
        
        // 设置疲劳数据
        if (specimen.getFatigueData() != null && !specimen.getFatigueData().isEmpty()) {
            // 创建表格
            TableLayout fatigueTable = createDataTable(
                    new String[]{"阶段", "动态模量(MPa)", "循环次数", "相位角(°)", "力水平", "均衡应变", "温度(°C)"},
                    specimen.getFatigueData());
            holder.fatigueDataContainer.addView(fatigueTable);
        } else {
            TextView noDataText = new TextView(context);
            noDataText.setText("无疲劳数据");
            holder.fatigueDataContainer.addView(noDataText);
        }
        
        // 设置卡片样式
        CardView cardView = (CardView) holder.itemView;
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setUseCompatPadding(true);
    }
    
    /**
     * 创建数据表格
     * 
     * @param headers 表头
     * @param dataList 数据列表
     * @return 表格布局
     */
    private TableLayout createDataTable(String[] headers, List<?> dataList) {
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setStretchAllColumns(true);
        
        // 添加表头
        TableRow headerRow = new TableRow(context);
        for (String header : headers) {
            headerRow.addView(createTableHeaderTextView(header));
        }
        tableLayout.addView(headerRow);
        
        // 添加数据行
        if (dataList.get(0) instanceof DirectStretchingFatigueTestResponse.ModulusData) {
            List<DirectStretchingFatigueTestResponse.ModulusData> modulus = 
                    (List<DirectStretchingFatigueTestResponse.ModulusData>) dataList;
            
            for (DirectStretchingFatigueTestResponse.ModulusData data : modulus) {
                TableRow dataRow = new TableRow(context);
                
                dataRow.addView(createTableCellTextView(data.getStage()));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getDynamicModulus())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getCycleCount())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getPhaseAngle())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getForceLevel())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getEquilibriumStrain())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getTemperature())));
                
                tableLayout.addView(dataRow);
            }
        } else if (dataList.get(0) instanceof DirectStretchingFatigueTestResponse.FatigueData) {
            List<DirectStretchingFatigueTestResponse.FatigueData> fatigue = 
                    (List<DirectStretchingFatigueTestResponse.FatigueData>) dataList;
            
            for (DirectStretchingFatigueTestResponse.FatigueData data : fatigue) {
                TableRow dataRow = new TableRow(context);
                
                dataRow.addView(createTableCellTextView(data.getStage()));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getDynamicModulus())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getCycleCount())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getPhaseAngle())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getForceLevel())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getEquilibriumStrain())));
                dataRow.addView(createTableCellTextView(String.valueOf(data.getTemperature())));
                
                tableLayout.addView(dataRow);
            }
        }
        
        return tableLayout;
    }
    
    private TextView createTableHeaderTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(4, 8, 4, 8);
        textView.setBackgroundResource(R.drawable.table_header_background);
        return textView;
    }
    
    private TextView createTableCellTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(4, 8, 4, 8);
        textView.setBackgroundResource(R.drawable.table_cell_background);
        return textView;
    }

    @Override
    public int getItemCount() {
        return specimens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentTitle;
        TextView tvSpecimenTitle;
        TextView tvSpecimenId;
        TextView tvDiameter;
        TextView tvHeight;
        ViewGroup modulusDataContainer;
        ViewGroup fatigueDataContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentTitle = itemView.findViewById(R.id.tvExperimentTitle);
            tvSpecimenTitle = itemView.findViewById(R.id.tvSpecimenTitle);
            tvSpecimenId = itemView.findViewById(R.id.tvSpecimenId);
            tvDiameter = itemView.findViewById(R.id.tvDiameter);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            modulusDataContainer = itemView.findViewById(R.id.modulusDataContainer);
            fatigueDataContainer = itemView.findViewById(R.id.fatigueDataContainer);
        }
    }
}
