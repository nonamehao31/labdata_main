package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DsrTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态剪切流变仪实验数据点适配器
 */
public class DsrDataPointAdapter extends RecyclerView.Adapter<DsrDataPointAdapter.ViewHolder> {

    private List<DsrTestResponse.DataPoint> dataPoints;

    public DsrDataPointAdapter() {
        this.dataPoints = new ArrayList<>();
    }

    public void updateData(List<DsrTestResponse.DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dsr_data_point, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DsrTestResponse.DataPoint dataPoint = dataPoints.get(position);

        // 格式化并显示数据点信息
        holder.tvTemperature.setText(formatDouble(dataPoint.getTemperature(), 1));
        holder.tvFrequency.setText(formatDouble(dataPoint.getFrequency(), 2));
        
        // 突出显示计算结果（使用粗体和红色）
        holder.tvComplexModulus.setText(formatScientific(dataPoint.getComplexModulus()));
        holder.tvStorageModulus.setText(formatScientific(dataPoint.getStorageModulus()));
        holder.tvLossModulus.setText(formatScientific(dataPoint.getLossModulus()));
        holder.tvPhaseAngle.setText(formatDouble(dataPoint.getPhaseAngle(), 1));
        
        // 显示黏弹性等级
        holder.tvViscoelasticGrade.setText(dataPoint.getViscoelasticGrade() != null ? 
                dataPoint.getViscoelasticGrade() : "--");
    }

    @Override
    public int getItemCount() {
        return dataPoints.size();
    }

    /**
     * 格式化小数
     */
    private String formatDouble(Double value, int decimalPlaces) {
        if (value == null) {
            return "--";
        }
        return String.format("%." + decimalPlaces + "f", value);
    }

    /**
     * 格式化科学计数法显示
     */
    private String formatScientific(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format("%.2e", value);
    }

    /**
     * ViewHolder类
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature;
        TextView tvFrequency;
        TextView tvComplexModulus;
        TextView tvStorageModulus;
        TextView tvLossModulus;
        TextView tvPhaseAngle;
        TextView tvViscoelasticGrade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvComplexModulus = itemView.findViewById(R.id.tvComplexModulus);
            tvStorageModulus = itemView.findViewById(R.id.tvStorageModulus);
            tvLossModulus = itemView.findViewById(R.id.tvLossModulus);
            tvPhaseAngle = itemView.findViewById(R.id.tvPhaseAngle);
            tvViscoelasticGrade = itemView.findViewById(R.id.tvViscoelasticGrade);
        }
    }
}
