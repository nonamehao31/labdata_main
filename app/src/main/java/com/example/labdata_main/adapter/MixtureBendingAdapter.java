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
import com.example.labdata_main.model.MixtureBendingTestResponse;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

/**
 * 沥青混合料弯曲试验数据适配器
 */
public class MixtureBendingAdapter extends RecyclerView.Adapter<MixtureBendingAdapter.ViewHolder> {
    private static final String TAG = "MixtureBendingAdapter";
    private List<MixtureBendingTestResponse> mixtureBendingTests = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private Context context;
    
    /**
     * 构造函数
     * @param context 上下文
     * @param tests 测试数据列表
     */
    public MixtureBendingAdapter(Context context, List<MixtureBendingTestResponse> tests) {
        this.context = context;
        this.mixtureBendingTests = new ArrayList<>(tests);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mixture_bending_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MixtureBendingTestResponse test = mixtureBendingTests.get(position);
        
        // 添加详细的数据绑定日志
        Log.d(TAG, "绑定沥青混合料弯曲试验数据到视图 [position=" + position + "]:" +
              "\n  ID: " + test.getId() +
              "\n  taskId: " + test.getTaskId() +
              "\n  mixRatioId: " + test.getMixRatioId() +
              "\n  spanLength: " + test.getSpanLength() + 
              "\n  specimenCount: " + test.getSpecimenCount() + 
              "\n  averageFlexuralStrength: " + test.getAverageFlexuralStrength() + 
              "\n  averageMaxStrain: " + test.getAverageMaxStrain() + 
              "\n  averageStiffnessModulus: " + test.getAverageStiffnessModulus());
        
        Log.d(TAG, "绑定数据到ViewHolder: position=" + position + 
                  ", 配比ID=" + test.getMixRatioId() + 
                  ", 抗弯拉强度=" + test.getAverageFlexuralStrength() + 
                  ", 最大弯拉应变=" + test.getAverageMaxStrain() + 
                  ", 弯曲劲度模量=" + test.getAverageStiffnessModulus());

        // 设置跨径长度，如果为null则显示"--"
        holder.tvSpanLength.setText(test.getSpanLength() != null ? 
                decimalFormat.format(test.getSpanLength()) + " mm" : "--");
                
        // 设置试件数量，如果为null则显示"--"
        holder.tvSpecimenCount.setText(test.getSpecimenCount() != null ? 
                String.valueOf(test.getSpecimenCount()) : "--");
        
        // 设置计算结果，突出显示这些重要数据
        holder.tvAverageFlexuralStrength.setText(test.getAverageFlexuralStrength() != null ? 
                decimalFormat.format(test.getAverageFlexuralStrength()) : "--");
                
        holder.tvAverageMaxStrain.setText(test.getAverageMaxStrain() != null ? 
                decimalFormat.format(test.getAverageMaxStrain()) : "--");
                
        holder.tvAverageStiffnessModulus.setText(test.getAverageStiffnessModulus() != null ? 
                decimalFormat.format(test.getAverageStiffnessModulus()) : "--");
        
        // 设置组标签，如果有配比ID则显示
        if (test.getMixRatioId() != null) {
            holder.tvGroupLabel.setText("沥青混合料弯曲试验数据 (配比ID: " + test.getMixRatioId() + ")");
        } else {
            holder.tvGroupLabel.setText("沥青混合料弯曲试验数据");
        }
    }

    @Override
    public int getItemCount() {
        return mixtureBendingTests.size();
    }

    /**
     * 更新数据列表
     * @param newTests 新的测试数据列表
     */
    public void updateData(List<MixtureBendingTestResponse> newTests) {
        if (newTests != null) {
            this.mixtureBendingTests.clear();
            this.mixtureBendingTests.addAll(newTests);
            Log.d(TAG, "沥青混合料弯曲试验数据已更新: " + newTests.size() + " 条");
            
            // 添加详细日志
            for (int i = 0; i < newTests.size(); i++) {
                MixtureBendingTestResponse test = newTests.get(i);
                Log.d(TAG, "更新后的数据项 #" + i + ":" +
                      "\n  ID: " + test.getId() +
                      "\n  taskId: " + test.getTaskId() +
                      "\n  mixRatioId: " + test.getMixRatioId() +
                      "\n  spanLength: " + test.getSpanLength() + 
                      "\n  specimenCount: " + test.getSpecimenCount() + 
                      "\n  averageFlexuralStrength: " + test.getAverageFlexuralStrength() + 
                      "\n  averageMaxStrain: " + test.getAverageMaxStrain() + 
                      "\n  averageStiffnessModulus: " + test.getAverageStiffnessModulus() +
                      "\n  specimens: " + (test.getSpecimens() != null ? 
                                         test.getSpecimens().substring(0, Math.min(50, test.getSpecimens().length())) + "..." : "null"));
            }
            
            notifyDataSetChanged();
        }
    }

    /**
     * 获取当前数据列表
     * @return 当前数据列表
     */
    public List<MixtureBendingTestResponse> getData() {
        return mixtureBendingTests;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupLabel;
        TextView tvSpanLength;
        TextView tvSpecimenCount;
        TextView tvAverageFlexuralStrength;
        TextView tvAverageMaxStrain;
        TextView tvAverageStiffnessModulus;

        ViewHolder(View itemView) {
            super(itemView);
            tvGroupLabel = itemView.findViewById(R.id.tvGroupLabel);
            tvSpanLength = itemView.findViewById(R.id.tvSpanLength);
            tvSpecimenCount = itemView.findViewById(R.id.tvSpecimenCount);
            tvAverageFlexuralStrength = itemView.findViewById(R.id.tvAverageFlexuralStrength);
            tvAverageMaxStrain = itemView.findViewById(R.id.tvAverageMaxStrain);
            tvAverageStiffnessModulus = itemView.findViewById(R.id.tvAverageStiffnessModulus);
        }
    }
}
