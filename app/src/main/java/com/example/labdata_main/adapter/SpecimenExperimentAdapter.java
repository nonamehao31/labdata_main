package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.SpecimenExperimentModel;
import java.util.List;

public class SpecimenExperimentAdapter extends RecyclerView.Adapter<SpecimenExperimentAdapter.SpecimenViewHolder> {

    private List<SpecimenExperimentModel> specimenList;
    private OnExperimentSelectedListener listener;

    public interface OnExperimentSelectedListener {
        void onExperimentSelected(int position, String experiment);
    }

    public SpecimenExperimentAdapter(List<SpecimenExperimentModel> specimenList, OnExperimentSelectedListener listener) {
        this.specimenList = specimenList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpecimenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_specimen_experiment, parent, false);
        return new SpecimenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecimenViewHolder holder, int position) {
        SpecimenExperimentModel specimen = specimenList.get(position);
        holder.tvSpecimenTitle.setText("试块" + specimen.getSpecimenNumber());
        
        holder.btnMarshal.setSelected(specimen.getSelectedExperiments().contains("马歇尔稳定度"));
        holder.btnBendingBeam.setSelected(specimen.getSelectedExperiments().contains("弯曲梁"));
        holder.btnElasticModulus.setSelected(specimen.getSelectedExperiments().contains("弹性模量"));

        holder.btnMarshal.setOnClickListener(v -> {
            if (holder.btnMarshal.isSelected()) {
                specimen.removeExperiment("马歇尔稳定度");
                holder.btnMarshal.setSelected(false);
            } else {
                specimen.addExperiment("马歇尔稳定度");
                holder.btnMarshal.setSelected(true);
            }
            listener.onExperimentSelected(position, "马歇尔稳定度");
        });

        holder.btnBendingBeam.setOnClickListener(v -> {
            if (holder.btnBendingBeam.isSelected()) {
                specimen.removeExperiment("弯曲梁");
                holder.btnBendingBeam.setSelected(false);
            } else {
                specimen.addExperiment("弯曲梁");
                holder.btnBendingBeam.setSelected(true);
            }
            listener.onExperimentSelected(position, "弯曲梁");
        });

        holder.btnElasticModulus.setOnClickListener(v -> {
            if (holder.btnElasticModulus.isSelected()) {
                specimen.removeExperiment("弹性模量");
                holder.btnElasticModulus.setSelected(false);
            } else {
                specimen.addExperiment("弹性模量");
                holder.btnElasticModulus.setSelected(true);
            }
            listener.onExperimentSelected(position, "弹性模量");
        });
    }

    @Override
    public int getItemCount() {
        return specimenList.size();
    }

    static class SpecimenViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpecimenTitle;
        Button btnMarshal, btnBendingBeam, btnElasticModulus;

        SpecimenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpecimenTitle = itemView.findViewById(R.id.tvSpecimenTitle);
            btnMarshal = itemView.findViewById(R.id.btnMarshal);
            btnBendingBeam = itemView.findViewById(R.id.btnBendingBeam);
            btnElasticModulus = itemView.findViewById(R.id.btnElasticModulus);
        }
    }
}
