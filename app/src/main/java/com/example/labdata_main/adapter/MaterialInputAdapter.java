package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.MaterialItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class MaterialInputAdapter extends RecyclerView.Adapter<MaterialInputAdapter.MaterialInputViewHolder> {
    private final List<MaterialItem> materials;

    public MaterialInputAdapter() {
        this.materials = new ArrayList<>();
    }

    public void addMaterial() {
        materials.add(new MaterialItem());
        notifyItemInserted(materials.size() - 1);
    }

    public List<MaterialItem> getMaterials() {
        return new ArrayList<>(materials);
    }

    @NonNull
    @Override
    public MaterialInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_input, parent, false);
        return new MaterialInputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialInputViewHolder holder, int position) {
        MaterialItem material = materials.get(position);
        holder.bind(material);

        holder.btnDelete.setOnClickListener(v -> {
            materials.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        });
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    static class MaterialInputViewHolder extends RecyclerView.ViewHolder {
        private final TextInputLayout tilMaterial;
        private final AutoCompleteTextView actvMaterial;
        private final TextInputEditText etAmount;
        private final ImageButton btnDelete;

        MaterialInputViewHolder(@NonNull View itemView) {
            super(itemView);
            tilMaterial = itemView.findViewById(R.id.tilMaterial);
            actvMaterial = itemView.findViewById(R.id.actvMaterial);
            etAmount = itemView.findViewById(R.id.etAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(MaterialItem material) {
            // 设置材料选择器
            if (actvMaterial.getAdapter() == null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        itemView.getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{"水泥", "砂", "石", "水", "外加剂"}
                );
                actvMaterial.setAdapter(adapter);
            }

            // 恢复已保存的值
            if (material.getMaterialName() != null) {
                actvMaterial.setText(material.getMaterialName(), false);
            }
            if (material.getAmount() != null) {
                etAmount.setText(material.getAmount());
            }

            // 添加文本变化监听器
            actvMaterial.setOnItemClickListener((parent, view, position, id) -> {
                material.setMaterialName(parent.getItemAtPosition(position).toString());
            });

            etAmount.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    material.setAmount(etAmount.getText().toString());
                }
            });
        }
    }
}
