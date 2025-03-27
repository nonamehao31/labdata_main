package com.example.labdata_main.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.MarshallTestResponse;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 马歇尔稳定度实验数据适配器
 */
public class MarshallAdapter extends RecyclerView.Adapter<MarshallAdapter.ViewHolder> {
    private static final String TAG = "MarshallAdapter";
    private final Context context;
    private List<MarshallTestResponse> marshallTests;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public MarshallAdapter(Context context, List<MarshallTestResponse> marshallTests) {
        this.context = context;
        this.marshallTests = marshallTests;
    }

    public void updateData(List<MarshallTestResponse> marshallTests) {
        this.marshallTests = marshallTests;
        notifyDataSetChanged();
        Log.d(TAG, "马歇尔实验数据已更新: " + marshallTests.size() + " 条");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_marshall_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarshallTestResponse test = marshallTests.get(position);
        
        // 设置稳定度和流值数据
        setTextIfNotNull(holder.tvStability1, test.getStability1());
        setTextIfNotNull(holder.tvStreamValue1, test.getStreamValue1());
        
        setTextIfNotNull(holder.tvStability2, test.getStability2());
        setTextIfNotNull(holder.tvStreamValue2, test.getStreamValue2());
        
        setTextIfNotNull(holder.tvStability3, test.getStability3());
        setTextIfNotNull(holder.tvStreamValue3, test.getStreamValue3());
        
        // 计算平均值
        float avgStability = calculateAverage(test.getStability1(), test.getStability2(), test.getStability3());
        float avgStreamValue = calculateAverage(test.getStreamValue1(), test.getStreamValue2(), test.getStreamValue3());
        
        if (avgStability > 0) {
            holder.tvAverageStability.setText(decimalFormat.format(avgStability));
        } else {
            holder.tvAverageStability.setText("--");
        }
        
        if (avgStreamValue > 0) {
            holder.tvAverageStreamValue.setText(decimalFormat.format(avgStreamValue));
        } else {
            holder.tvAverageStreamValue.setText("--");
        }
        
        Log.d(TAG, "绑定马歇尔实验数据: 位置=" + position + 
                ", 稳定度1=" + test.getStability1() + 
                ", 稳定度2=" + test.getStability2() + 
                ", 稳定度3=" + test.getStability3() + 
                ", 平均稳定度=" + avgStability);
    }

    private void setTextIfNotNull(TextView textView, Float value) {
        if (value != null && value > 0) {
            textView.setText(decimalFormat.format(value));
        } else {
            textView.setText("--");
        }
    }

    private float calculateAverage(Float... values) {
        int count = 0;
        float sum = 0;
        
        for (Float value : values) {
            if (value != null && value > 0) {
                sum += value;
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }

    @Override
    public int getItemCount() {
        return marshallTests != null ? marshallTests.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStability1, tvStreamValue1;
        TextView tvStability2, tvStreamValue2;
        TextView tvStability3, tvStreamValue3;
        TextView tvAverageStability, tvAverageStreamValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStability1 = itemView.findViewById(R.id.tvStability1);
            tvStreamValue1 = itemView.findViewById(R.id.tvStreamValue1);
            
            tvStability2 = itemView.findViewById(R.id.tvStability2);
            tvStreamValue2 = itemView.findViewById(R.id.tvStreamValue2);
            
            tvStability3 = itemView.findViewById(R.id.tvStability3);
            tvStreamValue3 = itemView.findViewById(R.id.tvStreamValue3);
            
            tvAverageStability = itemView.findViewById(R.id.tvAverageStability);
            tvAverageStreamValue = itemView.findViewById(R.id.tvAverageStreamValue);
        }
    }
}
