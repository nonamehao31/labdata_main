package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DynamicModulusTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态模量实验结果适配器
 */
public class DynamicModulusAdapter extends RecyclerView.Adapter<DynamicModulusAdapter.ViewHolder> {

    private List<DynamicModulusTestResponse> dataList;
    private Context context;

    public DynamicModulusAdapter(Context context, List<DynamicModulusTestResponse> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    
    public DynamicModulusAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }
    
    public void updateData(List<DynamicModulusTestResponse> newData) {
        this.dataList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dynamic_modulus_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DynamicModulusTestResponse data = dataList.get(position);
        
        // 设置基本信息
        holder.tvExperimentName.setText("动态模量试验");
        holder.tvMixRatioName.setText("配比名称: " + data.getMixRatioName());
        
        // 设置试件信息
        DynamicModulusTestResponse.Specimen specimen = data.getSpecimen();
        if (specimen != null) {
            holder.tvSpecimenNumber.setText("试件编号: " + specimen.getSpecimenNumber());
            holder.tvDiameter.setText("直径: " + specimen.getDiameter() + " mm");
            holder.tvHeight.setText("高度: " + specimen.getHeight() + " mm");
            holder.tvBulkDensity.setText("表观密度: " + specimen.getBulkDensity() + " g/cm³");
            holder.tvAirVoidContent.setText("空隙率: " + specimen.getAirVoidContent() + "%");
        }
        
        // 清除旧数据
        holder.dataTableContainer.removeAllViews();
        
        // 根据温度分组生成数据表格
        List<DynamicModulusTestResponse.TemperatureGroup> temperatureGroups = data.getTemperatureGroups();
        android.util.Log.d("DynamicModulusAdapter", "温度组数量: " + (temperatureGroups != null ? temperatureGroups.size() : "null"));
        
        if (temperatureGroups != null && !temperatureGroups.isEmpty()) {
            for (DynamicModulusTestResponse.TemperatureGroup group : temperatureGroups) {
                android.util.Log.d("DynamicModulusAdapter", "处理温度组: " + group.getTemperature() + "°C, 测量数据: " + 
                    (group.getMeasurements() != null ? group.getMeasurements().size() : "null"));
                
                // 创建温度标题
                TextView tvTemperature = new TextView(context);
                tvTemperature.setText("温度: " + group.getTemperature() + "°C");
                tvTemperature.setTextSize(16);
                tvTemperature.setPadding(0, 16, 0, 8);
                holder.dataTableContainer.addView(tvTemperature);
                
                // 创建数据表格
                TableLayout tableLayout = new TableLayout(context);
                tableLayout.setStretchAllColumns(true);
                
                // 添加表头
                TableRow headerRow = new TableRow(context);
                
                TextView headerFrequency = createTableHeaderTextView("频率(Hz)");
                TextView headerCycleCount = createTableHeaderTextView("循环次数");
                TextView headerPhaseAngle = createTableHeaderTextView("相位角(°)");
                TextView headerAxialStress = createTableHeaderTextView("轴向应力(kPa)");
                TextView headerAxialStrain = createTableHeaderTextView("轴向应变(μɛ)");
                TextView headerPermanentStrain = createTableHeaderTextView("永久轴向应变变化(μɛ)");
                
                headerRow.addView(headerFrequency);
                headerRow.addView(headerCycleCount);
                headerRow.addView(headerPhaseAngle);
                headerRow.addView(headerAxialStress);
                headerRow.addView(headerAxialStrain);
                headerRow.addView(headerPermanentStrain);
                
                tableLayout.addView(headerRow);
                
                // 添加数据行
                if (group.getMeasurements() != null) {
                    for (DynamicModulusTestResponse.Measurement measurement : group.getMeasurements()) {
                        TableRow dataRow = new TableRow(context);
                        
                        TextView tvFrequency = createTableCellTextView(String.valueOf(measurement.getFrequency()));
                        TextView tvCycleCount = createTableCellTextView(String.valueOf(measurement.getCycleCount()));
                        TextView tvPhaseAngle = createTableCellTextView(String.valueOf(measurement.getPhaseAngle()));
                        TextView tvAxialStress = createTableCellTextView(String.valueOf(measurement.getAxialStress()));
                        TextView tvAxialStrain = createTableCellTextView(String.valueOf(measurement.getAxialStrain()));
                        TextView tvPermanentStrain = createTableCellTextView(String.valueOf(measurement.getPermanentStrain()));
                        
                        dataRow.addView(tvFrequency);
                        dataRow.addView(tvCycleCount);
                        dataRow.addView(tvPhaseAngle);
                        dataRow.addView(tvAxialStress);
                        dataRow.addView(tvAxialStrain);
                        dataRow.addView(tvPermanentStrain);
                        
                        tableLayout.addView(dataRow);
                    }
                }
                
                holder.dataTableContainer.addView(tableLayout);
            }
        }
    }

    private TextView createTableHeaderTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(8, 8, 8, 8);
        textView.setBackgroundResource(R.drawable.table_header_background);
        return textView;
    }
    
    private TextView createTableCellTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(8, 8, 8, 8);
        textView.setBackgroundResource(R.drawable.table_cell_background);
        return textView;
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName;
        TextView tvMixRatioName;
        TextView tvSpecimenNumber;
        TextView tvDiameter;
        TextView tvHeight;
        TextView tvBulkDensity;
        TextView tvAirVoidContent;
        ViewGroup dataTableContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvSpecimenNumber = itemView.findViewById(R.id.tvSpecimenNumber);
            tvDiameter = itemView.findViewById(R.id.tvDiameter);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            tvBulkDensity = itemView.findViewById(R.id.tvBulkDensity);
            tvAirVoidContent = itemView.findViewById(R.id.tvAirVoidContent);
            dataTableContainer = itemView.findViewById(R.id.dataTableContainer);
        }
    }
}
