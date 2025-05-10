package com.example.labdata_main;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.model.MaterialItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {
    private List<MaterialItem> materials;
    private Runnable onDataChangedListener;
    private Context context;

    public MaterialAdapter(List<MaterialItem> materials, Runnable onDataChangedListener) {
        this.materials = materials;
        this.onDataChangedListener = onDataChangedListener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        MaterialItem item = materials.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    class MaterialViewHolder extends RecyclerView.ViewHolder {
        private ImageButton deleteButton;
        private TextView nameText;
        private TextView percentageText;
        private ImageButton editButton;

        MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.delete_button);
            nameText = itemView.findViewById(R.id.material_name);
            percentageText = itemView.findViewById(R.id.material_percentage);
            editButton = itemView.findViewById(R.id.edit_button);
        }

        void bind(MaterialItem item) {
            nameText.setText(item.getName());
            percentageText.setText(String.format("%.1f%%", item.getPercentage()));

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                    .setTitle("删除原料")
                    .setMessage("确定要删除该原料吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            materials.remove(position);
                            notifyItemRemoved(position);
                            onDataChangedListener.run();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            });

            editButton.setOnClickListener(v -> showEditDialog(item));
            
            // 设置整个项目可点击，触发编辑
            itemView.setOnClickListener(v -> showEditDialog(item));
        }

        private void showEditDialog(MaterialItem item) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_material, null);
            TextInputEditText percentageInput = dialogView.findViewById(R.id.percentage_input);
            percentageInput.setText(String.format("%.1f", item.getPercentage()));

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            dialogView.findViewById(R.id.cancel_button).setOnClickListener(v -> dialog.dismiss());
            dialogView.findViewById(R.id.confirm_button).setOnClickListener(v -> {
                String input = percentageInput.getText().toString();
                if (!input.isEmpty()) {
                    try {
                        float percentage = Float.parseFloat(input);
                        if (percentage >= 0 && percentage <= 100) {
                            item.setPercentage(percentage);
                            notifyItemChanged(getAdapterPosition());
                            onDataChangedListener.run();
                            dialog.dismiss();
                        } else {
                            percentageInput.setError("请输入0-100之间的数值");
                        }
                    } catch (NumberFormatException e) {
                        percentageInput.setError("请输入有效的数值");
                    }
                }
            });

            dialog.show();
        }
    }
}
