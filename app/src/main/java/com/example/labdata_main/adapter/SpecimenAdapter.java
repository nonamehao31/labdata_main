package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.Specimen;
import java.util.ArrayList;
import java.util.List;

public class SpecimenAdapter extends RecyclerView.Adapter<SpecimenAdapter.SpecimenViewHolder> {
    private List<Specimen> specimens;
    private OnSpecimenClickListener listener;

    public interface OnSpecimenClickListener {
        void onSpecimenClick(Specimen specimen);
    }

    public SpecimenAdapter(OnSpecimenClickListener listener) {
        this.specimens = new ArrayList<>();
        this.listener = listener;
    }

    public void setSpecimens(List<Specimen> specimens) {
        this.specimens = specimens;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpecimenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen, parent, false);
        return new SpecimenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecimenViewHolder holder, int position) {
        Specimen specimen = specimens.get(position);
        
        // 设置试件编号
        holder.tvSpecimenNumber.setText("试件 #" + (position + 1));
        
        // 设置拌合信息
        holder.tvMixingInfo.setText(String.format("拌合温度: %.1f°C | 拌合速度: %.1f rpm",
                specimen.getMixingTemperature(), specimen.getMixingSpeed()));
        
        // 设置压实方法
        holder.tvCompactionMethod.setText("压实方法: " + specimen.getCompactionMethod());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpecimenClick(specimen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return specimens.size();
    }

    static class SpecimenViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpecimenNumber;
        TextView tvMixingInfo;
        TextView tvCompactionMethod;

        SpecimenViewHolder(View itemView) {
            super(itemView);
            tvSpecimenNumber = itemView.findViewById(R.id.tvSpecimenNumber);
            tvMixingInfo = itemView.findViewById(R.id.tvMixingInfo);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
        }
    }
}
