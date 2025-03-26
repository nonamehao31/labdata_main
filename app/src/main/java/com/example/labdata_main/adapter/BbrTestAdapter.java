package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.BbrTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 弯曲梁流变仪(BBR)实验结果适配器
 */
public class BbrTestAdapter extends RecyclerView.Adapter<BbrTestAdapter.ViewHolder> {

    private Context context;
    private List<BbrTestResponse> testResults;
    
    public BbrTestAdapter(Context context) {
        this.context = context;
        this.testResults = new ArrayList<>();
    }
    
    public void updateData(List<BbrTestResponse> testResults) {
        this.testResults = testResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bbr_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BbrTestResponse result = testResults.get(position);
        
        // 试件尺寸
        holder.tvBeamSpan.setText("弯曲梁跨度: " + (result.getBeamSpan() != null ? result.getBeamSpan() + " mm" : "--"));
        holder.tvSpecimenWidth.setText("试件宽度: " + (result.getSpecimenWidth() != null ? result.getSpecimenWidth() + " mm" : "--"));
        holder.tvSpecimenHeight.setText("试件高度: " + (result.getSpecimenHeight() != null ? result.getSpecimenHeight() + " mm" : "--"));
        
        // 计算结果 - 突出显示
        holder.tvCreepRate.setText("蠕变速率(m值): " + (result.getCreepRate() != null ? String.format("%.3f", result.getCreepRate()) : "--"));
        
        // 测量数据 - 8秒
        holder.tvTemperature8s.setText(result.getTemperature8s() != null ? String.format("%.1f", result.getTemperature8s()) : "--");
        holder.tvLoad8s.setText(result.getLoad8s() != null ? String.format("%.1f", result.getLoad8s()) : "--");
        holder.tvDeflection8s.setText(result.getDeflection8s() != null ? String.format("%.3f", result.getDeflection8s()) : "--");
        holder.tvStiffness8s.setText(result.getStiffness8s() != null ? String.format("%.1f", result.getStiffness8s()) : "--");
        
        // 测量数据 - 15秒
        holder.tvTemperature15s.setText(result.getTemperature15s() != null ? String.format("%.1f", result.getTemperature15s()) : "--");
        holder.tvLoad15s.setText(result.getLoad15s() != null ? String.format("%.1f", result.getLoad15s()) : "--");
        holder.tvDeflection15s.setText(result.getDeflection15s() != null ? String.format("%.3f", result.getDeflection15s()) : "--");
        holder.tvStiffness15s.setText(result.getStiffness15s() != null ? String.format("%.1f", result.getStiffness15s()) : "--");
        
        // 测量数据 - 30秒
        holder.tvTemperature30s.setText(result.getTemperature30s() != null ? String.format("%.1f", result.getTemperature30s()) : "--");
        holder.tvLoad30s.setText(result.getLoad30s() != null ? String.format("%.1f", result.getLoad30s()) : "--");
        holder.tvDeflection30s.setText(result.getDeflection30s() != null ? String.format("%.3f", result.getDeflection30s()) : "--");
        holder.tvStiffness30s.setText(result.getStiffness30s() != null ? String.format("%.1f", result.getStiffness30s()) : "--");
        
        // 测量数据 - 60秒
        holder.tvTemperature60s.setText(result.getTemperature60s() != null ? String.format("%.1f", result.getTemperature60s()) : "--");
        holder.tvLoad60s.setText(result.getLoad60s() != null ? String.format("%.1f", result.getLoad60s()) : "--");
        holder.tvDeflection60s.setText(result.getDeflection60s() != null ? String.format("%.3f", result.getDeflection60s()) : "--");
        holder.tvStiffness60s.setText(result.getStiffness60s() != null ? String.format("%.1f", result.getStiffness60s()) : "--");
        
        // 测量数据 - 120秒
        holder.tvTemperature120s.setText(result.getTemperature120s() != null ? String.format("%.1f", result.getTemperature120s()) : "--");
        holder.tvLoad120s.setText(result.getLoad120s() != null ? String.format("%.1f", result.getLoad120s()) : "--");
        holder.tvDeflection120s.setText(result.getDeflection120s() != null ? String.format("%.3f", result.getDeflection120s()) : "--");
        holder.tvStiffness120s.setText(result.getStiffness120s() != null ? String.format("%.1f", result.getStiffness120s()) : "--");
        
        // 测量数据 - 240秒
        holder.tvTemperature240s.setText(result.getTemperature240s() != null ? String.format("%.1f", result.getTemperature240s()) : "--");
        holder.tvLoad240s.setText(result.getLoad240s() != null ? String.format("%.1f", result.getLoad240s()) : "--");
        holder.tvDeflection240s.setText(result.getDeflection240s() != null ? String.format("%.3f", result.getDeflection240s()) : "--");
        holder.tvStiffness240s.setText(result.getStiffness240s() != null ? String.format("%.1f", result.getStiffness240s()) : "--");
        
        // 备注
        holder.tvRemarks.setText(result.getRemarks() != null ? result.getRemarks() : "无");
    }

    @Override
    public int getItemCount() {
        return testResults != null ? testResults.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // 试件尺寸
        TextView tvBeamSpan, tvSpecimenWidth, tvSpecimenHeight;
        
        // 计算结果
        TextView tvCreepRate;
        
        // 8秒时间点数据
        TextView tvTemperature8s, tvLoad8s, tvDeflection8s, tvStiffness8s;
        
        // 15秒时间点数据
        TextView tvTemperature15s, tvLoad15s, tvDeflection15s, tvStiffness15s;
        
        // 30秒时间点数据
        TextView tvTemperature30s, tvLoad30s, tvDeflection30s, tvStiffness30s;
        
        // 60秒时间点数据
        TextView tvTemperature60s, tvLoad60s, tvDeflection60s, tvStiffness60s;
        
        // 120秒时间点数据
        TextView tvTemperature120s, tvLoad120s, tvDeflection120s, tvStiffness120s;
        
        // 240秒时间点数据
        TextView tvTemperature240s, tvLoad240s, tvDeflection240s, tvStiffness240s;
        
        // 备注
        TextView tvRemarks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // 试件尺寸
            tvBeamSpan = itemView.findViewById(R.id.tvBeamSpan);
            tvSpecimenWidth = itemView.findViewById(R.id.tvSpecimenWidth);
            tvSpecimenHeight = itemView.findViewById(R.id.tvSpecimenHeight);
            
            // 计算结果
            tvCreepRate = itemView.findViewById(R.id.tvCreepRate);
            
            // 8秒时间点数据
            tvTemperature8s = itemView.findViewById(R.id.tvTemperature8s);
            tvLoad8s = itemView.findViewById(R.id.tvLoad8s);
            tvDeflection8s = itemView.findViewById(R.id.tvDeflection8s);
            tvStiffness8s = itemView.findViewById(R.id.tvStiffness8s);
            
            // 15秒时间点数据
            tvTemperature15s = itemView.findViewById(R.id.tvTemperature15s);
            tvLoad15s = itemView.findViewById(R.id.tvLoad15s);
            tvDeflection15s = itemView.findViewById(R.id.tvDeflection15s);
            tvStiffness15s = itemView.findViewById(R.id.tvStiffness15s);
            
            // 30秒时间点数据
            tvTemperature30s = itemView.findViewById(R.id.tvTemperature30s);
            tvLoad30s = itemView.findViewById(R.id.tvLoad30s);
            tvDeflection30s = itemView.findViewById(R.id.tvDeflection30s);
            tvStiffness30s = itemView.findViewById(R.id.tvStiffness30s);
            
            // 60秒时间点数据
            tvTemperature60s = itemView.findViewById(R.id.tvTemperature60s);
            tvLoad60s = itemView.findViewById(R.id.tvLoad60s);
            tvDeflection60s = itemView.findViewById(R.id.tvDeflection60s);
            tvStiffness60s = itemView.findViewById(R.id.tvStiffness60s);
            
            // 120秒时间点数据
            tvTemperature120s = itemView.findViewById(R.id.tvTemperature120s);
            tvLoad120s = itemView.findViewById(R.id.tvLoad120s);
            tvDeflection120s = itemView.findViewById(R.id.tvDeflection120s);
            tvStiffness120s = itemView.findViewById(R.id.tvStiffness120s);
            
            // 240秒时间点数据
            tvTemperature240s = itemView.findViewById(R.id.tvTemperature240s);
            tvLoad240s = itemView.findViewById(R.id.tvLoad240s);
            tvDeflection240s = itemView.findViewById(R.id.tvDeflection240s);
            tvStiffness240s = itemView.findViewById(R.id.tvStiffness240s);
            
            // 备注
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
        }
    }
}
