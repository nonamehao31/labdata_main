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
import com.example.labdata_main.model.HamburgRuttingTestResponse;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

/**
 * 汉堡车辙实验数据适配器
 */
public class HamburgRuttingAdapter extends RecyclerView.Adapter<HamburgRuttingAdapter.ViewHolder> {
    private static final String TAG = "HamburgRuttingAdapter";
    private List<HamburgRuttingTestResponse> hamburgRuttingTests = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private Context context;
    
    /**
     * 构造函数
     * @param context 上下文
     * @param tests 测试数据列表
     */
    public HamburgRuttingAdapter(Context context, List<HamburgRuttingTestResponse> tests) {
        this.context = context;
        this.hamburgRuttingTests = new ArrayList<>(tests);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hamburg_rutting_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HamburgRuttingTestResponse test = hamburgRuttingTests.get(position);
        Log.d(TAG, "绑定汉堡车辙实验数据: 位置=" + position + 
                  ", 斜率1=" + test.getSteadySlope1() + 
                  ", 截距1=" + test.getSteadyCurvilinear1() + 
                  ", 斜率2=" + test.getSteadySlope2() + 
                  ", 截距2=" + test.getSteadyCurvilinear2());

        // 设置显示值，如果为null则显示"--"
        holder.tvSteadySlope1.setText(test.getSteadySlope1() != null ? 
                decimalFormat.format(test.getSteadySlope1()) : "--");
        holder.tvSteadyCurvilinear1.setText(test.getSteadyCurvilinear1() != null ? 
                decimalFormat.format(test.getSteadyCurvilinear1()) : "--");
        holder.tvSteadySlope2.setText(test.getSteadySlope2() != null ? 
                decimalFormat.format(test.getSteadySlope2()) : "--");
        holder.tvSteadyCurvilinear2.setText(test.getSteadyCurvilinear2() != null ? 
                decimalFormat.format(test.getSteadyCurvilinear2()) : "--");
        
        // 设置组标签，如果有配比ID则显示
        if (test.getMixRatioId() != null) {
            holder.tvGroupLabel.setText("汉堡车辙实验数据 (配比ID: " + test.getMixRatioId() + ")");
        } else {
            holder.tvGroupLabel.setText("汉堡车辙实验数据");
        }
    }

    @Override
    public int getItemCount() {
        return hamburgRuttingTests.size();
    }

    /**
     * 更新数据列表
     * @param newTests 新的测试数据列表
     */
    public void updateData(List<HamburgRuttingTestResponse> newTests) {
        if (newTests != null) {
            this.hamburgRuttingTests.clear();
            this.hamburgRuttingTests.addAll(newTests);
            Log.d(TAG, "汉堡车辙实验数据已更新: " + newTests.size() + " 条");
            notifyDataSetChanged();
        }
    }

    /**
     * 获取当前数据列表
     * @return 当前数据列表
     */
    public List<HamburgRuttingTestResponse> getData() {
        return hamburgRuttingTests;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupLabel;
        TextView tvSteadySlope1;
        TextView tvSteadyCurvilinear1;
        TextView tvSteadySlope2;
        TextView tvSteadyCurvilinear2;

        ViewHolder(View itemView) {
            super(itemView);
            tvGroupLabel = itemView.findViewById(R.id.tvGroupLabel);
            tvSteadySlope1 = itemView.findViewById(R.id.tvSteadySlope1);
            tvSteadyCurvilinear1 = itemView.findViewById(R.id.tvSteadyCurvilinear1);
            tvSteadySlope2 = itemView.findViewById(R.id.tvSteadySlope2);
            tvSteadyCurvilinear2 = itemView.findViewById(R.id.tvSteadyCurvilinear2);
        }
    }
}
