package com.example.labdata_main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.api.model.SpecimenParametersResponse;

import java.util.List;

/**
 * 制件参数列表适配器
 */
public class MoldingParametersAdapter extends RecyclerView.Adapter<MoldingParametersAdapter.MoldingParametersViewHolder> {
    private static final String TAG = "MoldingParametersAdapter";
    private List<SpecimenParametersResponse> moldingParameters;

    public MoldingParametersAdapter(List<SpecimenParametersResponse> moldingParameters) {
        this.moldingParameters = moldingParameters;
        Log.d(TAG, "初始化适配器，参数数量: " + (moldingParameters != null ? moldingParameters.size() : 0));
    }

    @NonNull
    @Override
    public MoldingParametersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_detail_molding, parent, false);
        Log.d(TAG, "创建ViewHolder");
        return new MoldingParametersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoldingParametersViewHolder holder, int position) {
        SpecimenParametersResponse parameter = moldingParameters.get(position);
        Log.d(TAG, "绑定位置 " + position + " 的数据: " + parameter);

        // 构建拌合参数文本
        StringBuilder mixingParamsText = new StringBuilder("拌合参数：");
        
        // 添加拌合温度
        if (parameter.getMixingTemperature() != null) {
            mixingParamsText.append("温度 ").append(String.format("%.1f", parameter.getMixingTemperature())).append("℃, ");
        } else {
            mixingParamsText.append("温度 暂无数据, ");
        }
        
        // 添加拌合速度
        if (parameter.getMixingSpeed() != null) {
            mixingParamsText.append("速度 ").append(String.format("%.1f", parameter.getMixingSpeed())).append(" rpm, ");
        } else {
            mixingParamsText.append("速度 暂无数据, ");
        }
        
        // 添加拌合时间
        if (parameter.getMixingTime() != null) {
            mixingParamsText.append("时间 ").append(parameter.getMixingTime()).append(" s");
        } else {
            mixingParamsText.append("时间 暂无数据");
        }
        
        // 设置拌合参数
        holder.tvMixingParams.setText(mixingParamsText.toString());

        // 设置压实方法
        if (parameter.getCompactionMethod() != null && !parameter.getCompactionMethod().isEmpty()) {
            holder.tvCompactionMethod.setText("压实方法：" + parameter.getCompactionMethod());
        } else {
            holder.tvCompactionMethod.setText("压实方法：暂无数据");
        }
    }

    @Override
    public int getItemCount() {
        return moldingParameters != null ? moldingParameters.size() : 0;
    }

    /**
     * 更新数据源
     * @param parameters 新的制件参数列表
     */
    public void updateData(List<SpecimenParametersResponse> parameters) {
        Log.d(TAG, "更新数据，新参数数量: " + (parameters != null ? parameters.size() : 0));
        this.moldingParameters = parameters;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder类
     */
    static class MoldingParametersViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixingParams;
        TextView tvCompactionMethod;
        TextView tvMixRatioName;

        MoldingParametersViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
        }
    }
}
