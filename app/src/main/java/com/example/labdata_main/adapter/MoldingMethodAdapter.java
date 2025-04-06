package com.example.labdata_main.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;

import com.example.labdata_main.R;
import com.example.labdata_main.database.AppDatabase;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoldingMethodAdapter extends RecyclerView.Adapter<MoldingMethodAdapter.MoldingMethodViewHolder> {
    private List<MoldingMethod> moldingMethods;
    private List<Integer> selectedPositions;
    private OnDeleteClickListener deleteClickListener;
    private OnItemSelectionChangedListener selectionChangedListener;
    private Context context;
    private List<MixRatio> availableMixRatios = new ArrayList<>();
    private Map<Integer, MixRatio> selectedMixRatios = new HashMap<>();
    private AppDatabase database;

    public interface OnDeleteClickListener {
        void onDeleteClick(MoldingMethod method);
    }

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(List<MoldingMethod> selectedMethods);
    }

    public MoldingMethodAdapter(List<MoldingMethod> moldingMethods, 
                              OnDeleteClickListener deleteClickListener, 
                              OnItemSelectionChangedListener selectionChangedListener) {
        this.moldingMethods = moldingMethods;
        this.deleteClickListener = deleteClickListener;
        this.selectionChangedListener = selectionChangedListener;
        this.selectedPositions = new ArrayList<>();
    }

    public void setAvailableMixRatios(List<MixRatio> mixRatios) {
        Log.d("MoldingMethodAdapter", "Setting available mix ratios: " + mixRatios.size());
        this.availableMixRatios.clear();
        this.availableMixRatios.addAll(mixRatios);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoldingMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_molding_method, parent, false);
        return new MoldingMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoldingMethodViewHolder holder, int position) {
        MoldingMethod method = moldingMethods.get(position);
        
        // 设置标题编号
        holder.tvMoldingMethodTitle.setText(String.format("制件方法%d", position + 1));
        
        // 检查拌合参数是否为0，如果是则显示"暂无数据"
        if (method.getMixingTemperature() == 0 && method.getMixingSpeed() == 0 && method.getMixingTime() == 0) {
            holder.tvMixingMethodDetails.setText("拌合参数：暂无数据");
        } else {
            String mixingDetails = String.format("拌合温度: %.1f℃\n拌合速度: %.1f r/min\n拌合时间: %.1f s", 
                method.getMixingTemperature(), 
                method.getMixingSpeed(), 
                method.getMixingTime());
            holder.tvMixingMethodDetails.setText(mixingDetails);
        }
        
        holder.tvCompactionMethodDetails.setText("" + method.getCompactionMethod());
        
        holder.btnDeleteMoldingMethod.setOnClickListener(v -> showDeleteConfirmationDialog(position));

        // 设置选中状态
        boolean isSelected = selectedPositions.contains(position);
        holder.cardView.setSelected(isSelected);
        holder.itemView.setSelected(isSelected);

        // 设置配比下拉菜单
        setupMixRatioDropdown(holder, position, method);

        // 设置点击事件
        holder.cardView.setOnClickListener(v -> toggleSelection(position));
    }

    private void setupMixRatioDropdown(MoldingMethodViewHolder holder, int position, MoldingMethod method) {
        Log.d("MoldingMethodAdapter", "Setting up dropdown for position " + position + ", available mix ratios: " + availableMixRatios.size());
        Log.d("MoldingMethodAdapter", "当前制件方法ID: " + method.getId() + ", 压实方法: " + method.getCompactionMethod());
        Log.d("MoldingMethodAdapter", "当前制件方法信息: " + method.toString());
        
        // 创建下拉菜单选项，使用配比的实际名称
        String[] items = new String[availableMixRatios.size()];
        for (int i = 0; i < availableMixRatios.size(); i++) {
            MixRatio mixRatio = availableMixRatios.get(i);
            items[i] = mixRatio.getName();
        }

        // 创建下拉菜单适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_dropdown_item_1line,
            items
        );

        holder.actvMixRatioSelector.setAdapter(adapter);

        // 如果已经选择了配比，显示选中的配比名称
        MixRatio selectedMixRatio = selectedMixRatios.get(position);
        if (selectedMixRatio != null) {
            int index = availableMixRatios.indexOf(selectedMixRatio);
            Log.d("MoldingMethodAdapter", "Position " + position + " has selected mix ratio at index: " + index);
            if (index != -1) {
                holder.actvMixRatioSelector.setText(
                    availableMixRatios.get(index).getName(),
                    false
                );
            }
        }

        // 设置选择监听器
        holder.actvMixRatioSelector.setOnItemClickListener((parent, view, pos, id) -> {
            MixRatio selectedRatio = availableMixRatios.get(pos);
            Log.d("MoldingMethodAdapter", "Selected mix ratio at position " + pos + " for molding method " + position);
            Log.d("MoldingMethodAdapter", "将配比ID " + selectedRatio.getId() + " 关联到制件方法ID " + method.getId());
            selectedMixRatios.put(position, selectedRatio);
            method.setMixRatioId(selectedRatio.getId());
            notifyItemChanged(position);
        });
    }

    /**
     * 刷新制件方法ID，确保使用最新的数据库ID
     * 在保存试件后调用此方法，以确保使用后端分配的ID而不是本地临时ID
     * @param context 上下文，用于获取数据库实例
     */
    public void refreshMoldingMethodIds(Context context) {
        if (moldingMethods == null || moldingMethods.isEmpty()) {
            Log.d("MoldingMethodAdapter", "没有制件方法需要刷新");
            return;
        }

        Log.d("MoldingMethodAdapter", "开始刷新制件方法ID");
        
        // 初始化数据库（如果还没有初始化）
        if (database == null) {
            database = AppDatabase.getInstance(context);
        }
        
        // 在后台线程中执行数据库操作
        new Thread(() -> {
            try {
                // 从数据库获取所有制件方法
                List<MoldingMethod> allMethods = database.moldingMethodDao().getAllMoldingMethods();
                Log.d("MoldingMethodAdapter", "从数据库获取到 " + allMethods.size() + " 个制件方法");
                
                // 遍历当前适配器中的所有制件方法
                for (int i = 0; i < moldingMethods.size(); i++) {
                    MoldingMethod currentMethod = moldingMethods.get(i);
                    Long currentId = currentMethod.getId();
                    Log.d("MoldingMethodAdapter", "检查制件方法 ID: " + currentId);
                    
                    // 在数据库返回的列表中查找匹配的对象
                    for (MoldingMethod dbMethod : allMethods) {
                        // 如果找到相同ID的制件方法，检查其他属性是否有变化
                        if (dbMethod.getId() == currentId) {
                            Log.d("MoldingMethodAdapter", "找到匹配ID: " + currentId);
                            
                            // 检查是否有变更（例如，如果ID在服务器端被更新）
                            if (!currentMethod.equals(dbMethod)) {
                                Log.d("MoldingMethodAdapter", "制件方法已更新: " + 
                                      "原ID=" + currentId + 
                                      ", 新方法=" + dbMethod.toString());
                                
                                // 更新当前列表中的对象
                                moldingMethods.set(i, dbMethod);
                                
                                // 如果需要，更新选中的配比关联
                                if (selectedMixRatios.containsKey(i)) {
                                    MixRatio ratio = selectedMixRatios.get(i);
                                    dbMethod.setMixRatioId(ratio.getId());
                                }
                            }
                            break;
                        }
                    }
                }
                
                // 在UI线程更新界面
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        notifyDataSetChanged();
                        Log.d("MoldingMethodAdapter", "制件方法ID刷新完成");
                    });
                }
            } catch (Exception e) {
                Log.e("MoldingMethodAdapter", "刷新制件方法ID时出错", e);
            }
        }).start();
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);

        // 通知选中状态变化
        List<MoldingMethod> selectedMethods = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            selectedMethods.add(moldingMethods.get(pos));
        }
        selectionChangedListener.onSelectionChanged(selectedMethods);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
            .setTitle("删除制件方式")
            .setMessage("确定要删除这个制件方式吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                MoldingMethod methodToDelete = moldingMethods.get(position);
                moldingMethods.remove(position);
                // 更新选中位置
                selectedPositions.remove(Integer.valueOf(position));
                selectedMixRatios.remove(position);
                notifyDataSetChanged();
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(methodToDelete);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return moldingMethods.size();
    }

    static class MoldingMethodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoldingMethodTitle;
        TextView tvMixingMethodDetails;
        TextView tvCompactionMethodDetails;
        ImageButton btnDeleteMoldingMethod;
        MaterialCardView cardView;
        TextInputLayout tilMixRatioSelector;
        AutoCompleteTextView actvMixRatioSelector;

        MoldingMethodViewHolder(View itemView) {
            super(itemView);
            tvMoldingMethodTitle = itemView.findViewById(R.id.tvMoldingMethodTitle);
            tvMixingMethodDetails = itemView.findViewById(R.id.tvMixingMethodDetails);
            tvCompactionMethodDetails = itemView.findViewById(R.id.tvCompactionMethodDetails);
            btnDeleteMoldingMethod = itemView.findViewById(R.id.btnDeleteMoldingMethod);
            cardView = itemView.findViewById(R.id.cardMoldingMethod);
            tilMixRatioSelector = itemView.findViewById(R.id.tilMixRatioSelector);
            actvMixRatioSelector = itemView.findViewById(R.id.actvMixRatioSelector);
        }
    }
}
