package com.example.labdata_main.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
import com.example.labdata_main.model.UniaxialCompressionTestResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 沥青混合料单轴压缩试验（圆柱体法）数据适配器
 */
public class UniaxialCompressionAdapter extends RecyclerView.Adapter<UniaxialCompressionAdapter.ViewHolder> {
    private static final String TAG = "UniaxialCompressionAdapter";
    private Context context;
    private List<UniaxialCompressionTestResponse> testDataList = new ArrayList<>();
    private DecimalFormat decimalFormat = new DecimalFormat("0.##");

    public UniaxialCompressionAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_uniaxial_compression_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UniaxialCompressionTestResponse testData = testDataList.get(position);
        
        // 设置实验标题
        holder.tvExperimentTitle.setText("沥青混合料单轴压缩试验（圆柱体法）");
        
        // 设置测试温度
        holder.tvTestTemperature.setText("测试温度: " + formatDoubleValue(testData.getTestTemperature()) + "°C");

        // 清除旧数据
        holder.tableSpecimenInfo.removeAllViews();
        holder.tableUtmData.removeAllViews();
        holder.tableStrengthData.removeAllViews();
        
        // 添加表头
        addSpecimenInfoTableHeader(holder.tableSpecimenInfo);
        addUtmDataTableHeader(holder.tableUtmData);
        addStrengthDataTableHeader(holder.tableStrengthData);
        
        // 添加数据行
        if (testData.getSpecimens() != null && !testData.getSpecimens().isEmpty()) {
            // 先添加所有试件的基本信息
            for (UniaxialCompressionTestResponse.SpecimenData specimen : testData.getSpecimens()) {
                addSpecimenInfoRow(holder.tableSpecimenInfo, specimen);
                
                if (specimen.getStrengthData() != null && !specimen.getStrengthData().isEmpty()) {
                    addStrengthDataRows(holder.tableStrengthData, specimen);
                }
            }
            
            // 单独处理UTM数据 - 只使用第一个试件的UTM数据填充表格
            // 在实际应用中，通常多个试件的压力级别和数据结构是相同的
            if (!testData.getSpecimens().isEmpty()) {
                UniaxialCompressionTestResponse.SpecimenData firstSpecimen = testData.getSpecimens().get(0);
                fillUtmDataTable(holder.tableUtmData, firstSpecimen);
                Log.d(TAG, "使用第一个试件的UTM数据填充表格，试件编号: " + firstSpecimen.getSpecimenNumber());
            }
            
            // 添加平均值行（如果有多个试件）
            if (testData.getSpecimens().size() > 1) {
                addStrengthAverageRow(holder.tableStrengthData, testData.getSpecimens());
            }
        } else {
            // 如果没有试件数据，显示空数据行
            addEmptyDataRow(holder.tableSpecimenInfo, 3);
            // UTM数据表不需要添加空行，因为已经在addUtmDataTableHeader中创建了带有占位符的表格
            addEmptyDataRow(holder.tableStrengthData, 2);
        }
    }

    @Override
    public int getItemCount() {
        return testDataList.size();
    }

    public void updateData(List<UniaxialCompressionTestResponse> newData) {
        this.testDataList.clear();
        if (newData != null) {
            this.testDataList.addAll(newData);
        }
        notifyDataSetChanged();
        Log.d(TAG, "更新单轴压缩试验数据: " + testDataList.size() + " 项");
    }

    /**
     * 添加试件信息表头
     */
    private void addSpecimenInfoTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"试件编号", "直径(mm)", "高度(mm)"};
        for (String header : headers) {
            TextView textView = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);
            textView.setText(header);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(context.getResources().getColor(android.R.color.white));
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    /**
     * 添加UTM数据表头
     */
    private void addUtmDataTableHeader(TableLayout tableLayout) {
        // 清空表格防止重复添加
        tableLayout.removeAllViews();
        
        // 添加参数名称行
        TableRow parameterRow = new TableRow(context);
        parameterRow.setBackgroundResource(android.R.color.darker_gray);
        parameterRow.setPadding(4, 4, 4, 4);

        String[] parameters = {"最大力\nForce-Max (KN)", "最小力\nForce-min (N)", "σd (应力水平)\nStress-Dev (kpa)", "Δh (弹性形变)\nDispl-resil (mm)", "ε弹性变形\nStrain-resil", "抗压回弹模量\n(Mpa)", "温度\n(Temperature)"};
        
        // 添加压力级别单元格作为第一列
        TextView emptyCell = new TextView(context);
        TableRow.LayoutParams emptyCellParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
        emptyCell.setLayoutParams(emptyCellParams);
        emptyCell.setText("压力级别");
        emptyCell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyCell.setTextColor(context.getResources().getColor(android.R.color.white));
        parameterRow.addView(emptyCell);

        // 添加参数名称
        for (String parameter : parameters) {
            TextView textView = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);
            textView.setText(parameter);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(context.getResources().getColor(android.R.color.white));
            textView.setTextSize(12);
            parameterRow.addView(textView);
        }
        tableLayout.addView(parameterRow);
        
        // 不再预先创建空的压力级别行，而是在fillUtmDataTable中根据实际数据创建
    }

    /**
     * 添加抗压强度数据表头
     */
    private void addStrengthDataTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"试件编号", "强度值(kN)"};
        for (String header : headers) {
            TextView textView = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);
            textView.setText(header);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(context.getResources().getColor(android.R.color.white));
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    /**
     * 添加试件信息行
     */
    private void addSpecimenInfoRow(TableLayout tableLayout, UniaxialCompressionTestResponse.SpecimenData specimen) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);

        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSpecimenNumber.setLayoutParams(params);
        tvSpecimenNumber.setText(String.valueOf(specimen.getSpecimenNumber()));
        tvSpecimenNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvSpecimenNumber);

        // 直径
        TextView tvDiameter = new TextView(context);
        tvDiameter.setLayoutParams(params);
        tvDiameter.setText(formatDoubleValue(specimen.getDiameterMm()));
        tvDiameter.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvDiameter);

        // 高度
        TextView tvHeight = new TextView(context);
        tvHeight.setLayoutParams(params);
        tvHeight.setText(formatDoubleValue(specimen.getHeightMm()));
        tvHeight.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvHeight);

        tableLayout.addView(dataRow);
    }

    /**
     * 添加抗压强度数据行
     */
    private void addStrengthDataRows(TableLayout tableLayout, UniaxialCompressionTestResponse.SpecimenData specimen) {
        for (UniaxialCompressionTestResponse.SpecimenData.StrengthData strengthData : specimen.getStrengthData()) {
            TableRow dataRow = new TableRow(context);
            dataRow.setPadding(4, 4, 4, 4);

            // 标签（P1, P2等）
            TextView tvLabel = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tvLabel.setLayoutParams(params);
            tvLabel.setText(specimen.getSpecimenNumber() + "-" + strengthData.getLabel());
            tvLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(tvLabel);

            // 强度值
            TextView tvValue = new TextView(context);
            tvValue.setLayoutParams(params);
            tvValue.setText(formatDoubleValue(strengthData.getValueKn()));
            tvValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(tvValue);

            tableLayout.addView(dataRow);
        }
    }

    /**
     * 添加平均值行
     */
    private void addStrengthAverageRow(TableLayout tableLayout, List<UniaxialCompressionTestResponse.SpecimenData> specimens) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);
        dataRow.setBackgroundResource(R.color.gray_light);

        // 平均值标签
        TextView tvAverageLabel = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvAverageLabel.setLayoutParams(params);
        tvAverageLabel.setText("平均值");
        tvAverageLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvAverageLabel.setTextColor(context.getResources().getColor(android.R.color.white));
        dataRow.addView(tvAverageLabel);

        // 计算并显示平均值
        double sum = 0;
        int count = 0;
        for (UniaxialCompressionTestResponse.SpecimenData specimen : specimens) {
            sum += specimen.getStrengthAverage();
            count++;
        }
        double average = count > 0 ? sum / count : 0;

        TextView tvAverageValue = new TextView(context);
        tvAverageValue.setLayoutParams(params);
        tvAverageValue.setText(formatDoubleValue(average));
        tvAverageValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvAverageValue.setTextColor(context.getResources().getColor(android.R.color.white));
        dataRow.addView(tvAverageValue);

        tableLayout.addView(dataRow);
    }

    /**
     * 添加空数据行
     */
    private void addEmptyDataRow(TableLayout tableLayout, int columnCount) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);

        for (int i = 0; i < columnCount; i++) {
            TextView textView = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);
            textView.setText("--");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(textView);
        }

        tableLayout.addView(dataRow);
    }

    /**
     * 填充UTM数据表格
     */
    private void fillUtmDataTable(TableLayout tableLayout, UniaxialCompressionTestResponse.SpecimenData specimen) {
        // 删除所有现有行，然后重新构建表格
        tableLayout.removeAllViews();
        
        // 记录有多少UTM数据
        int utmDataCount = specimen.getUtmDataList() != null ? specimen.getUtmDataList().size() : 0;
        Log.d(TAG, "填充UTM数据 - 试件编号: " + specimen.getSpecimenNumber() + ", UTM数据数量: " + utmDataCount);
        
        // 添加表头
        addUtmDataTableHeader(tableLayout);
        
        // 如果没有UTM数据，添加一个提示行
        if (utmDataCount == 0) {
            Log.d(TAG, "没有UTM数据可显示");
            TableRow noDataRow = new TableRow(context);
            noDataRow.setPadding(4, 8, 4, 8);
            
            TextView tvNoData = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.span = 8; // 跨越所有列
            tvNoData.setLayoutParams(params);
            tvNoData.setText("无UTM数据");
            tvNoData.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noDataRow.addView(tvNoData);
            
            tableLayout.addView(noDataRow);
            return;
        }
        
        // 检查是否有任何有效的UTM数据
        boolean hasAnyValidData = false;
        
        // 获取所有的压力级别，同时排序
        List<String> pressureLevels = new ArrayList<>();
        for (UniaxialCompressionTestResponse.SpecimenData.UtmData utmData : specimen.getUtmDataList()) {
            if (utmData.getPressureLevel() != null && !pressureLevels.contains(utmData.getPressureLevel())) {
                pressureLevels.add(utmData.getPressureLevel());
                
                // 检查是否有有效数据
                boolean dataValid = utmData.getMaxForceKn() != 0 || 
                                  utmData.getMinForceN() != 0 || 
                                  utmData.getStressDevKpa() != 0 ||
                                  utmData.getDisplResilMm() != 0 || 
                                  utmData.getStrainResil() != 0 || 
                                  utmData.getResilientModulusMpa() != 0;
                
                if (dataValid) {
                    hasAnyValidData = true;
                }
            }
        }
        
        // 如果没有有效的UTM数据，显示提示并返回
        if (!hasAnyValidData) {
            Log.d(TAG, "所有UTM数据均无效，不显示数据行");
            TableRow noValidDataRow = new TableRow(context);
            noValidDataRow.setPadding(4, 8, 4, 8);
            
            TextView tvNoValidData = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.span = 8; // 跨越所有列
            tvNoValidData.setLayoutParams(params);
            tvNoValidData.setText("无有效UTM数据");
            tvNoValidData.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noValidDataRow.addView(tvNoValidData);
            
            tableLayout.addView(noValidDataRow);
            return;
        }
        
        // 对压力级别进行排序
        Collections.sort(pressureLevels, (o1, o2) -> {
            try {
                // 假设格式为"0.1P", "0.2P"等，提取数字部分进行比较
                double v1 = Double.parseDouble(o1.replace("P", ""));
                double v2 = Double.parseDouble(o2.replace("P", ""));
                return Double.compare(v1, v2);
            } catch (Exception e) {
                // 如果解析出错，按字符串比较
                return o1.compareTo(o2);
            }
        });
        
        Log.d(TAG, "排序后的压力级别: " + pressureLevels);
        
        // 为每个压力级别创建一行，但只处理有实际数据的压力级别
        for (String level : pressureLevels) {
            // 获取对应压力级别的数据
            UniaxialCompressionTestResponse.SpecimenData.UtmData utmData = specimen.getUtmDataByPressureLevel(level);
            
            if (utmData == null) {
                Log.d(TAG, "压力级别 " + level + " 没有数据");
                continue;
            }
            
            // 检查是否有有效数据 - 所有数值都为0的行可能是无效数据
            boolean hasValidData = utmData.getMaxForceKn() != 0 || 
                                  utmData.getMinForceN() != 0 || 
                                  utmData.getStressDevKpa() != 0 ||
                                  utmData.getDisplResilMm() != 0 || 
                                  utmData.getStrainResil() != 0 || 
                                  utmData.getResilientModulusMpa() != 0;
            
            Log.d(TAG, "压力级别 " + level + " 有效数据: " + hasValidData);
            
            // 跳过无效数据行
            if (!hasValidData) {
                Log.d(TAG, "跳过压力级别 " + level + " 的行，因为没有有效数据");
                continue;
            }
            
            // 创建新行
            TableRow dataRow = new TableRow(context);
            dataRow.setPadding(4, 8, 4, 8);
            
            // 设置压力级别列
            TextView tvPressureLevel = new TextView(context);
            TableRow.LayoutParams levelParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
            tvPressureLevel.setLayoutParams(levelParams);
            tvPressureLevel.setText(level);
            tvPressureLevel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvPressureLevel.setTypeface(Typeface.DEFAULT_BOLD);
            dataRow.addView(tvPressureLevel);
            
            // 填充数据
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getMaxForceKn()));     // 最大力
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getMinForceN()));      // 最小力
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getStressDevKpa()));   // 应力偏差
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getDisplResilMm()));   // 位移恢复
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getStrainResil()));    // 应变恢复
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getResilientModulusMpa())); // 回弹模量
            addTextViewToRow(dataRow, formatDoubleValue(utmData.getTemperature()));    // 温度
            
            // 将行添加到表格
            tableLayout.addView(dataRow);
            
            // 添加分隔线
            View separator = new View(context);
            TableLayout.LayoutParams separatorParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1);
            separator.setLayoutParams(separatorParams);
            separator.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            tableLayout.addView(separator);
        }
    }
    
    /**
     * 向表格行添加文本视图
     */
    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        row.addView(textView);
    }

    /**
     * 格式化double值为字符串
     */
    private String formatDoubleValue(double value) {
        if (value == 0) {
            return "--";
        }
        return decimalFormat.format(value);
    }

    /**
     * ViewHolder类
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentTitle;
        TextView tvTestTemperature;
        TableLayout tableSpecimenInfo;
        TableLayout tableUtmData;
        TableLayout tableStrengthData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentTitle = itemView.findViewById(R.id.tvExperimentTitle);
            tvTestTemperature = itemView.findViewById(R.id.tvTestTemperature);
            tableSpecimenInfo = itemView.findViewById(R.id.tableSpecimenInfo);
            tableUtmData = itemView.findViewById(R.id.tableUtmData);
            tableStrengthData = itemView.findViewById(R.id.tableStrengthData);
        }
    }
}
