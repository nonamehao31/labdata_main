package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.AsphaltInfo;
import com.google.android.material.card.MaterialCardView;
import java.util.HashSet;
import java.util.Set;

public class AsphaltAdapter extends ListAdapter<AsphaltInfo, AsphaltAdapter.AsphaltViewHolder> {
    private final OnAsphaltClickListener listener;
    private final Set<AsphaltInfo> selectedAsphalt = new HashSet<>();

    public AsphaltAdapter(OnAsphaltClickListener listener) {
        super(new AsphaltDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public AsphaltViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asphalt, parent, false);
        return new AsphaltViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsphaltViewHolder holder, int position) {
        AsphaltInfo asphalt = getItem(position);
        holder.bind(asphalt, listener, selectedAsphalt.contains(asphalt));
    }

    public Set<AsphaltInfo> getSelectedAsphalt() {
        return new HashSet<>(selectedAsphalt);
    }

    public void toggleSelection(AsphaltInfo asphalt) {
        if (selectedAsphalt.contains(asphalt)) {
            selectedAsphalt.remove(asphalt);
        } else {
            selectedAsphalt.add(asphalt);
        }
        notifyItemChanged(getCurrentList().indexOf(asphalt));
    }

    public void clearSelections() {
        selectedAsphalt.clear();
        notifyDataSetChanged();
    }

    static class AsphaltViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvGrade;
        private final TextView tvType;
        private final TextView tvSupplier;
        private final TextView tvExpiryDate;
        private final MaterialCardView cardView;

        AsphaltViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvType = itemView.findViewById(R.id.tvType);
            tvSupplier = itemView.findViewById(R.id.tvSupplier);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
        }

        void bind(AsphaltInfo asphalt, OnAsphaltClickListener listener, boolean isSelected) {
            tvGrade.setText(asphalt.getGrade());
            tvType.setText(asphalt.getType());
            tvSupplier.setText("供应商：" + asphalt.getSupplier());
            tvExpiryDate.setText("检测截止日期：" + asphalt.getExpiryDate());
            
            // 设置选中状态
            cardView.setChecked(isSelected);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAsphaltClick(asphalt);
                }
            });
        }
    }

    public interface OnAsphaltClickListener {
        void onAsphaltClick(AsphaltInfo asphalt);
    }

    private static class AsphaltDiffCallback extends DiffUtil.ItemCallback<AsphaltInfo> {
        @Override
        public boolean areItemsTheSame(@NonNull AsphaltInfo oldItem, @NonNull AsphaltInfo newItem) {
            return oldItem.getSupplier().equals(newItem.getSupplier()) &&
                   oldItem.getExpiryDate().equals(newItem.getExpiryDate()) &&
                   oldItem.getGrade().equals(newItem.getGrade()) &&
                   oldItem.getType().equals(newItem.getType());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AsphaltInfo oldItem, @NonNull AsphaltInfo newItem) {
            return oldItem.getSupplier().equals(newItem.getSupplier()) &&
                   oldItem.getExpiryDate().equals(newItem.getExpiryDate()) &&
                   oldItem.getGrade().equals(newItem.getGrade()) &&
                   oldItem.getType().equals(newItem.getType());
        }
    }
}
