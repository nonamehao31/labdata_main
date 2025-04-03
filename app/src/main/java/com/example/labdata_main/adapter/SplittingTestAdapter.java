package com.example.labdata_main.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.SplittingTestResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 沥青混合料劈裂试验结果适配器
 */
public class SplittingTestAdapter extends RecyclerView.Adapter<SplittingTestAdapter.ViewHolder> {

    private static final String TAG = "SplittingTestAdapter";
    private final List<SplittingTestResponse> splittingTests;
    private final Context context;

    public SplittingTestAdapter(Context context, List<SplittingTestResponse> splittingTests) {
        this.context = context;
        this.splittingTests = splittingTests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_splitting_test_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SplittingTestResponse testData = splittingTests.get(position);
        
        try {
            // 设置试验基本信息
            holder.tvTestMethod.setText("试验方法：" + (testData.getTestMethod() != null ? testData.getTestMethod() : "无"));
            
            if (testData.getTestTemperature() != null) {
                holder.tvTestTemperature.setText(String.format("试验温度：%.1f℃", testData.getTestTemperature()));
            } else {
                holder.tvTestTemperature.setText("试验温度：无");
            }
            
            holder.tvTestEquipment.setText("试验设备：" + (testData.getTestEquipment() != null ? testData.getTestEquipment() : "无"));
            
            // 清除之前的试件数据行
            if (holder.tableSpecimens.getChildCount() > 1) {
                holder.tableSpecimens.removeViews(1, holder.tableSpecimens.getChildCount() - 1);
            }
            
            // 隐藏计算结果表格
            holder.tableCalculation.setVisibility(View.GONE);
            
            // 清除之前的荷载数据行
            if (holder.tableLoadData.getChildCount() > 1) {
                holder.tableLoadData.removeViews(1, holder.tableLoadData.getChildCount() - 1);
            }
            
            // 清除之前的变形数据行
            if (holder.tableDeformationData.getChildCount() > 1) {
                holder.tableDeformationData.removeViews(1, holder.tableDeformationData.getChildCount() - 1);
            }
            
            // 填充试件数据表格
            if (testData.getSpecimens() != null && !testData.getSpecimens().isEmpty()) {
                for (SplittingTestResponse.SpecimenData specimen : testData.getSpecimens()) {
                    addSpecimenRow(holder, specimen);
                    addLoadDataRow(holder, specimen);
                    addDeformationDataRow(holder, specimen);
                    // 不再添加计算结果行
                    // addCalculationRow(holder, specimen);
                }
                Log.d(TAG, "已添加 " + testData.getSpecimens().size() + " 行试件数据");
            } else {
                Log.w(TAG, "无试件数据");
                // 添加一个无数据提示行
                TableRow row = new TableRow(context);
                TextView tv = new TextView(context);
                tv.setText("无试件数据");
                tv.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4));
                row.addView(tv);
                holder.tableSpecimens.addView(row);
                
                // 添加一个无荷载数据提示行
                TableRow loadRow = new TableRow(context);
                TextView loadTv = new TextView(context);
                loadTv.setText("无荷载数据");
                loadTv.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5));
                loadRow.addView(loadTv);
                holder.tableLoadData.addView(loadRow);
                
                // 添加一个无变形数据提示行
                TableRow deformRow = new TableRow(context);
                TextView deformTv = new TextView(context);
                deformTv.setText("无变形数据");
                deformTv.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5));
                deformRow.addView(deformTv);
                holder.tableDeformationData.addView(deformRow);
                
                // 不再添加计算结果部分
                /*
                // 添加一个无计算结果提示行
                TableRow calcRow = new TableRow(context);
                TextView calcTv = new TextView(context);
                calcTv.setText("无计算结果");
                calcTv.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4));
                calcRow.addView(calcTv);
                holder.tableCalculation.addView(calcRow);
                */
            }
            
        } catch (Exception e) {
            Log.e(TAG, "绑定数据时出错：" + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return splittingTests != null ? splittingTests.size() : 0;
    }

    /**
     * 添加试件数据行
     */
    private void addSpecimenRow(@NonNull ViewHolder holder, SplittingTestResponse.SpecimenData specimen) {
        TableRow row = new TableRow(context);
        row.setPadding(4, 4, 4, 4);
        
        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        tvSpecimenNumber.setText(specimen.getSpecimenNumber() != null ? specimen.getSpecimenNumber().toString() : "-");
        tvSpecimenNumber.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvSpecimenNumber);
        
        // 直径
        TextView tvDiameter = new TextView(context);
        tvDiameter.setText(specimen.getDiameter() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getDiameter()) : "-");
        tvDiameter.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvDiameter);
        
        // 高度
        TextView tvHeight = new TextView(context);
        tvHeight.setText(specimen.getHeight() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getHeight()) : "-");
        tvHeight.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvHeight);
        
        // 最大荷载 (使用P平均值)
        TextView tvMaxLoad = new TextView(context);
        tvMaxLoad.setText(specimen.getPAverage() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getPAverage()) : "-");
        tvMaxLoad.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvMaxLoad);
        
        holder.tableSpecimens.addView(row);
    }
    
    /**
     * 添加荷载数据行
     */
    private void addLoadDataRow(@NonNull ViewHolder holder, SplittingTestResponse.SpecimenData specimen) {
        TableRow row = new TableRow(context);
        row.setPadding(4, 4, 4, 4);
        
        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        tvSpecimenNumber.setText(specimen.getSpecimenNumber() != null ? specimen.getSpecimenNumber().toString() : "-");
        tvSpecimenNumber.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvSpecimenNumber);
        
        // P1值
        TextView tvP1 = new TextView(context);
        tvP1.setText(specimen.getP1Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getP1Value()) : "-");
        tvP1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvP1);
        
        // P2值
        TextView tvP2 = new TextView(context);
        tvP2.setText(specimen.getP2Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getP2Value()) : "-");
        tvP2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvP2);
        
        // P3值
        TextView tvP3 = new TextView(context);
        tvP3.setText(specimen.getP3Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getP3Value()) : "-");
        tvP3.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvP3);
        
        // P平均值
        TextView tvPAvg = new TextView(context);
        tvPAvg.setText(specimen.getPAverage() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getPAverage()) : "-");
        tvPAvg.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvPAvg);
        
        holder.tableLoadData.addView(row);
    }
    
    /**
     * 添加变形数据行
     */
    private void addDeformationDataRow(@NonNull ViewHolder holder, SplittingTestResponse.SpecimenData specimen) {
        TableRow row = new TableRow(context);
        row.setPadding(4, 4, 4, 4);
        
        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        tvSpecimenNumber.setText(specimen.getSpecimenNumber() != null ? specimen.getSpecimenNumber().toString() : "-");
        tvSpecimenNumber.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvSpecimenNumber);
        
        // X1值
        TextView tvX1 = new TextView(context);
        tvX1.setText(specimen.getX1Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getX1Value()) : "-");
        tvX1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvX1);
        
        // X2值
        TextView tvX2 = new TextView(context);
        tvX2.setText(specimen.getX2Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getX2Value()) : "-");
        tvX2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvX2);
        
        // X3值
        TextView tvX3 = new TextView(context);
        tvX3.setText(specimen.getX3Value() != null ? String.format(Locale.getDefault(), "%.2f", specimen.getX3Value()) : "-");
        tvX3.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvX3);
        
        // X平均值 - 手动计算平均值，而不使用getXAverage()方法
        TextView tvXAvg = new TextView(context);
        if (specimen.getX1Value() != null && specimen.getX2Value() != null && specimen.getX3Value() != null) {
            double xAvg = (specimen.getX1Value() + specimen.getX2Value() + specimen.getX3Value()) / 3.0;
            tvXAvg.setText(String.format(Locale.getDefault(), "%.2f", xAvg));
        } else {
            tvXAvg.setText("-");
        }
        tvXAvg.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvXAvg);
        
        holder.tableDeformationData.addView(row);
    }

    /**
     * 添加计算结果行
     */
    private void addCalculationRow(@NonNull ViewHolder holder, SplittingTestResponse.SpecimenData specimen) {
        TableRow row = new TableRow(context);
        row.setPadding(4, 4, 4, 4);
        
        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        tvSpecimenNumber.setText(specimen.getSpecimenNumber() != null ? specimen.getSpecimenNumber().toString() : "-");
        tvSpecimenNumber.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvSpecimenNumber);
        
        // 抗拉强度
        TextView tvTensileStrength = new TextView(context);
        tvTensileStrength.setText(specimen.getTensileStrength() != null ? 
                String.format(Locale.getDefault(), "%.3f", specimen.getTensileStrength()) : "-");
        tvTensileStrength.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvTensileStrength);
        
        // 刚度模量
        TextView tvStiffnessModulus = new TextView(context);
        tvStiffnessModulus.setText(specimen.getStiffnessModulus() != null ? 
                String.format(Locale.getDefault(), "%.1f", specimen.getStiffnessModulus()) : "-");
        tvStiffnessModulus.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvStiffnessModulus);
        
        // 泊松比
        TextView tvPoissonRatio = new TextView(context);
        tvPoissonRatio.setText(specimen.getPoissonRatio() != null ? 
                String.format(Locale.getDefault(), "%.3f", specimen.getPoissonRatio()) : "-");
        tvPoissonRatio.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvPoissonRatio);
        
        holder.tableCalculation.addView(row);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTestMethod;
        TextView tvTestTemperature;
        TextView tvTestEquipment;
        TableLayout tableSpecimens;
        TableLayout tableLoadData;
        TableLayout tableDeformationData;
        TableLayout tableCalculation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTestMethod = itemView.findViewById(R.id.tvTestMethod);
            tvTestTemperature = itemView.findViewById(R.id.tvTestTemperature);
            tvTestEquipment = itemView.findViewById(R.id.tvTestEquipment);
            
            // 直接使用findViewById获取表格布局
            tableSpecimens = itemView.findViewById(R.id.tableSpecimens);
            tableLoadData = itemView.findViewById(R.id.tableLoadData);
            tableDeformationData = itemView.findViewById(R.id.tableDeformationData);
            tableCalculation = itemView.findViewById(R.id.tableCalculation);
        }
    }
}
