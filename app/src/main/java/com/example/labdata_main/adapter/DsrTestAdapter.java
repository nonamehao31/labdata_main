package com.example.labdata_main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DsrTestResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态剪切流变仪(DSR)实验结果适配器
 */
public class DsrTestAdapter extends RecyclerView.Adapter<DsrTestAdapter.ViewHolder> {

    private Context context;
    private List<DsrTestResponse> testResults;
    
    public DsrTestAdapter(Context context) {
        this.context = context;
        this.testResults = new ArrayList<>();
    }
    
    public void updateData(List<DsrTestResponse> testResults) {
        this.testResults = testResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dsr_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DsrTestResponse result = testResults.get(position);
        
        // 设置试验参数
        holder.tvTestRadius.setText("试验板半径(R): " + 
                (result.getTestRadius() != null ? result.getTestRadius() + " mm" : "--"));
        holder.tvPlateGap.setText("试验平板间距(h): " + 
                (result.getPlateGap() != null ? result.getPlateGap() + " mm" : "--"));
        holder.tvControlMode.setText("控制方式: " + 
                (result.getControlMode() != null ? result.getControlMode() : "--"));

        // 设置数据点适配器
        DsrDataPointAdapter dataPointAdapter = new DsrDataPointAdapter();
        holder.rvDsrDataPoints.setAdapter(dataPointAdapter);
        
        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        holder.rvDsrDataPoints.setLayoutManager(layoutManager);
        
        // 更新数据点
        List<DsrTestResponse.DataPoint> dataPoints = result.getDataPoints();
        dataPointAdapter.updateData(dataPoints);
        
        // 设置备注
        holder.tvRemarks.setText(result.getRemarks() != null ? result.getRemarks() : "无");
    }

    @Override
    public int getItemCount() {
        return testResults != null ? testResults.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // 试验参数
        TextView tvTestRadius;
        TextView tvPlateGap;
        TextView tvControlMode;
        
        // 数据点列表
        RecyclerView rvDsrDataPoints;
        
        // 备注
        TextView tvRemarks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // 初始化视图引用
            tvTestRadius = itemView.findViewById(R.id.tvTestRadius);
            tvPlateGap = itemView.findViewById(R.id.tvPlateGap);
            tvControlMode = itemView.findViewById(R.id.tvControlMode);
            rvDsrDataPoints = itemView.findViewById(R.id.rvDsrDataPoints);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
        }
    }
}
