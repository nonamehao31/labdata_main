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
import com.example.labdata_main.model.FourPointBendingFatigueTestResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 沥青混合料四点弯曲疲劳寿命试验数据适配器
 */
public class FourPointBendingFatigueAdapter extends RecyclerView.Adapter<FourPointBendingFatigueAdapter.ViewHolder> {
    private static final String TAG = "FourPointAdapter";
    private Context context;
    private List<FourPointBendingFatigueTestResponse> testDataList = new ArrayList<>();
    private DecimalFormat decimalFormat = new DecimalFormat("0.##");

    public FourPointBendingFatigueAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_four_point_bending_fatigue_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FourPointBendingFatigueTestResponse testData = testDataList.get(position);
        
        // 设置实验标题
        holder.tvExperimentTitle.setText("沥青混合料四点弯曲疲劳寿命实验");

        // 清除旧数据
        holder.tableSpecimenInfo.removeAllViews();
        holder.tableTestParams.removeAllViews();
        holder.tableTestResults.removeAllViews();
        holder.tableDetailedResults.removeAllViews();
        
        // 添加表头
        addSpecimenInfoTableHeader(holder.tableSpecimenInfo);
        addTestParamsTableHeader(holder.tableTestParams);
        addTestResultsTableHeader(holder.tableTestResults);
        
        // 添加试件数据行
        if (testData.getSpecimens() != null && !testData.getSpecimens().isEmpty()) {
            for (FourPointBendingFatigueTestResponse.SpecimenData specimen : testData.getSpecimens()) {
                addSpecimenInfoRow(holder.tableSpecimenInfo, specimen);
                addTestParamsRow(holder.tableTestParams, specimen);
                addTestResultsRow(holder.tableTestResults, specimen);
                
                // 添加详细测试结果数据
                if (specimen.getResults() != null && !specimen.getResults().isEmpty()) {
                    addDetailedResultsTable(holder.tableDetailedResults, specimen);
                }
            }
        } else {
            // 如果没有试件数据，显示空数据行
            addEmptyDataRow(holder.tableSpecimenInfo, 4);
            addEmptyDataRow(holder.tableTestParams, 4);
            addEmptyDataRow(holder.tableTestResults, 3);
        }
    }

    @Override
    public int getItemCount() {
        return testDataList.size();
    }

    public void updateData(List<FourPointBendingFatigueTestResponse> newData) {
        this.testDataList.clear();
        if (newData != null) {
            this.testDataList.addAll(newData);
        }
        notifyDataSetChanged();
        Log.d(TAG, "更新四点弯曲疲劳寿命实验数据: " + testDataList.size() + " 项");
    }

    /**
     * 添加试件信息表头
     */
    private void addSpecimenInfoTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"试件编号", "长度(mm)", "宽度(mm)", "高度(mm)"};
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
     * 添加测试参数表头
     */
    private void addTestParamsTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"试件编号", "跨度(mm)", "应变范围", "频率(Hz)"};
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
     * 添加测试结果表头
     */
    private void addTestResultsTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"试件编号", "测试温度(°C)", "疲劳寿命(次)"};
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
    private void addSpecimenInfoRow(TableLayout tableLayout, FourPointBendingFatigueTestResponse.SpecimenData specimen) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);

        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSpecimenNumber.setLayoutParams(params);
        tvSpecimenNumber.setText(String.valueOf(specimen.getSpecimenNumber()));
        tvSpecimenNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvSpecimenNumber);

        // 长度
        TextView tvLength = new TextView(context);
        tvLength.setLayoutParams(params);
        tvLength.setText(formatDoubleValue(specimen.getLength()));
        tvLength.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvLength);

        // 宽度
        TextView tvWidth = new TextView(context);
        tvWidth.setLayoutParams(params);
        tvWidth.setText(formatDoubleValue(specimen.getWidth()));
        tvWidth.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvWidth);

        // 高度
        TextView tvHeight = new TextView(context);
        tvHeight.setLayoutParams(params);
        tvHeight.setText(formatDoubleValue(specimen.getHeight()));
        tvHeight.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvHeight);

        tableLayout.addView(dataRow);
    }

    /**
     * 添加测试参数行
     */
    private void addTestParamsRow(TableLayout tableLayout, FourPointBendingFatigueTestResponse.SpecimenData specimen) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);

        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSpecimenNumber.setLayoutParams(params);
        tvSpecimenNumber.setText(String.valueOf(specimen.getSpecimenNumber()));
        tvSpecimenNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvSpecimenNumber);

        // 跨度
        TextView tvSpan = new TextView(context);
        tvSpan.setLayoutParams(params);
        tvSpan.setText(formatDoubleValue(specimen.getSpanMm()));
        tvSpan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvSpan);

        // 应变范围
        TextView tvStrainRange = new TextView(context);
        tvStrainRange.setLayoutParams(params);
        tvStrainRange.setText(formatDoubleValue(specimen.getStrainRange()));
        tvStrainRange.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvStrainRange);

        // 频率
        TextView tvFrequency = new TextView(context);
        tvFrequency.setLayoutParams(params);
        tvFrequency.setText(formatDoubleValue(specimen.getFrequencyHz()));
        tvFrequency.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvFrequency);

        tableLayout.addView(dataRow);
    }

    /**
     * 添加测试结果行
     */
    private void addTestResultsRow(TableLayout tableLayout, FourPointBendingFatigueTestResponse.SpecimenData specimen) {
        TableRow dataRow = new TableRow(context);
        dataRow.setPadding(4, 4, 4, 4);

        // 试件编号
        TextView tvSpecimenNumber = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSpecimenNumber.setLayoutParams(params);
        tvSpecimenNumber.setText(String.valueOf(specimen.getSpecimenNumber()));
        tvSpecimenNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvSpecimenNumber);

        // 测试温度
        TextView tvTemperature = new TextView(context);
        tvTemperature.setLayoutParams(params);
        tvTemperature.setText(formatDoubleValue(specimen.getTestTemperature()));
        tvTemperature.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvTemperature);

        // 疲劳寿命
        TextView tvFatigueLife = new TextView(context);
        tvFatigueLife.setLayoutParams(params);
        tvFatigueLife.setText(formatDoubleValue(specimen.getFatigueLife()));
        tvFatigueLife.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dataRow.addView(tvFatigueLife);

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
     * 格式化double值为字符串
     */
    private String formatDoubleValue(double value) {
        if (value == 0) {
            return "--";
        }
        return decimalFormat.format(value);
    }

    /**
     * 添加详细测试结果表
     */
    private void addDetailedResultsTable(TableLayout tableLayout, FourPointBendingFatigueTestResponse.SpecimenData specimen) {
        // 确保有结果数据
        if (specimen.getResults() == null || specimen.getResults().isEmpty()) {
            Log.d(TAG, "试件 " + specimen.getSpecimenNumber() + " 没有详细测试结果数据");
            return;
        }
        
        // 添加详细结果表头
        TableRow headerRow = new TableRow(context);
        headerRow.setBackgroundResource(android.R.color.darker_gray);
        headerRow.setPadding(4, 4, 4, 4);
        
        String[] headers = {"参数名称", "单位", "初始值", "当前值"};
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
        
        // 排序结果，确保顺序一致
        List<FourPointBendingFatigueTestResponse.SpecimenData.ResultData> sortedResults = new ArrayList<>(specimen.getResults());
        sortedResults.sort((r1, r2) -> {
            // 如果结果索引存在，按索引排序
            if (r1.getResultIndex() != null && r2.getResultIndex() != null) {
                return r1.getResultIndex().compareTo(r2.getResultIndex());
            }
            // 否则按名称排序
            return r1.getResultTypeDisplayName().compareTo(r2.getResultTypeDisplayName());
        });
        
        // 添加每个结果数据行
        for (FourPointBendingFatigueTestResponse.SpecimenData.ResultData result : sortedResults) {
            TableRow dataRow = new TableRow(context);
            dataRow.setPadding(4, 4, 4, 4);
            
            // 参数名称
            TextView tvName = new TextView(context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(params);
            tvName.setText(result.getResultTypeDisplayName());
            tvName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            dataRow.addView(tvName);
            
            // 单位
            TextView tvUnit = new TextView(context);
            tvUnit.setLayoutParams(params);
            tvUnit.setText(result.getResultTypeUnit());
            tvUnit.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(tvUnit);
            
            // 初始值
            TextView tvInitialValue = new TextView(context);
            tvInitialValue.setLayoutParams(params);
            tvInitialValue.setText(formatDoubleValue(result.getInitialValue()));
            tvInitialValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(tvInitialValue);
            
            // 当前值
            TextView tvCurrentValue = new TextView(context);
            tvCurrentValue.setLayoutParams(params);
            tvCurrentValue.setText(formatDoubleValue(result.getCurrentValue()));
            tvCurrentValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dataRow.addView(tvCurrentValue);
            
            tableLayout.addView(dataRow);
        }
        
        // 添加空白行作为分隔
        TableRow spacerRow = new TableRow(context);
        spacerRow.setPadding(0, 8, 0, 8);
        TextView spacer = new TextView(context);
        spacerRow.addView(spacer);
        tableLayout.addView(spacerRow);
    }

    /**
     * ViewHolder类
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentTitle;
        TableLayout tableSpecimenInfo;
        TableLayout tableTestParams;
        TableLayout tableTestResults;
        TableLayout tableDetailedResults;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentTitle = itemView.findViewById(R.id.tvExperimentTitle);
            tableSpecimenInfo = itemView.findViewById(R.id.tableSpecimenInfo);
            tableTestParams = itemView.findViewById(R.id.tableTestParams);
            tableTestResults = itemView.findViewById(R.id.tableTestResults);
            tableDetailedResults = itemView.findViewById(R.id.tableDetailedResults);
        }
    }
}
