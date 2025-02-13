package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.fragment.BottomSheetAsphaltTaskDetailFragment.AsphaltExperimentItem;

import java.util.List;

public class AsphaltExperimentAdapter extends RecyclerView.Adapter<AsphaltExperimentAdapter.ViewHolder> {
    private final List<AsphaltExperimentItem> items;

    public AsphaltExperimentAdapter(List<AsphaltExperimentItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asphalt_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AsphaltExperimentItem item = items.get(position);
        holder.titleText.setText(item.getTitle());
        holder.contentText.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView contentText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            contentText = itemView.findViewById(R.id.contentText);
        }
    }
}
