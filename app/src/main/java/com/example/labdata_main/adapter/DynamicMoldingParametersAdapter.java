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
import com.example.labdata_main.model.MixRatio;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示从后端动态获取的制件参数的适配器
 */
public class DynamicMoldingParametersAdapter extends RecyclerView.Adapter<DynamicMoldingParametersAdapter.ViewHolder> {
    private static final String TAG = "DynamicMoldingAdapter";
    private List<SpecimenParametersResponse> moldingParameters;
    private List<MixRatio> mixRatios;

    public DynamicMoldingParametersAdapter() {
        this.moldingParameters = new ArrayList<>();
        this.mixRatios = new ArrayList<>();
    }

    public void setMoldingParameters(List<SpecimenParametersResponse> params) {
        Log.d(TAG, "设置制件参数列表，数量: " + (params != null ? params.size() : 0));
        this.moldingParameters = params != null ? params : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setMixRatios(List<MixRatio> mixRatios) {
        this.mixRatios = mixRatios != null ? mixRatios : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_detail_molding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpecimenParametersResponse parameter = moldingParameters.get(position);
        MixRatio mixRatio = position < mixRatios.size() ? mixRatios.get(position) : null;
        holder.bind(parameter, mixRatio, position + 1);
    }

    @Override
    public int getItemCount() {
        return moldingParameters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMixRatioName;
        private final TextView tvMixingParams;
        private final TextView tvCompactionMethod;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioName = itemView.findViewById(R.id.tvMixRatioName);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
        }

        public void bind(SpecimenParametersResponse parameter, MixRatio mixRatio, int index) {
            // 设置配比名称
            if (mixRatio != null) {
                tvMixRatioName.setText(String.format("配比%d：%s", index, mixRatio.getName()));
                tvMixRatioName.setVisibility(View.VISIBLE);
            } else {
                tvMixRatioName.setVisibility(View.GONE);
            }

            // 设置拌合参数
            if (parameter != null) {
                StringBuilder mixingParamsText = new StringBuilder("拌合参数：");
                
                // 温度
                if (parameter.getMixingTemperature() != null) {
                    mixingParamsText.append("温度 ").append(parameter.getMixingTemperature()).append("℃");
                } else {
                    mixingParamsText.append("温度 --");
                }
                
                // 速度
                mixingParamsText.append("，速度 ");
                if (parameter.getMixingSpeed() != null) {
                    mixingParamsText.append(parameter.getMixingSpeed()).append(" rpm");
                } else {
                    mixingParamsText.append("--");
                }
                
                // 时间
                mixingParamsText.append("，时间 ");
                if (parameter.getMixingTime() != null) {
                    mixingParamsText.append(parameter.getMixingTime()).append(" s");
                } else {
                    mixingParamsText.append("--");
                }
                
                tvMixingParams.setText(mixingParamsText.toString());
                
                // 设置压实方法
                if (parameter.getCompactionMethod() != null && !parameter.getCompactionMethod().isEmpty()) {
                    tvCompactionMethod.setText(String.format("压实方法：%s", parameter.getCompactionMethod()));
                } else {
                    tvCompactionMethod.setText("压实方法：--");
                }
            } else {
                tvMixingParams.setText("拌合参数：--");
                tvCompactionMethod.setText("压实方法：--");
            }
        }
    }
}
