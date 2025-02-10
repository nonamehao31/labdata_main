package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.MixRatio;
import com.example.labdata_main.model.MoldingMethod;
import java.util.ArrayList;
import java.util.List;

public class SpecimenInfoAdapter extends RecyclerView.Adapter<SpecimenInfoAdapter.ViewHolder> {
    private final List<SpecimenInfo> specimenInfos = new ArrayList<>();
    private List<String> selectedMixingDevices;
    private List<String> selectedFormingDevices;

    public static class SpecimenInfo {
        public MoldingMethod moldingMethod;
        public MixRatio mixRatio;
        public DeviceInfo mixingDevice;
        public DeviceInfo formingDevice;

        public SpecimenInfo(MoldingMethod moldingMethod, MixRatio mixRatio,
                          DeviceInfo mixingDevice, DeviceInfo formingDevice) {
            this.moldingMethod = moldingMethod;
            this.mixRatio = mixRatio;
            this.mixingDevice = mixingDevice;
            this.formingDevice = formingDevice;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMixRatioInfo;
        TextView tvMixingParams;
        TextView tvCompactionMethod;
        TextView tvMixingDevice;
        TextView tvFormingDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMixRatioInfo = itemView.findViewById(R.id.tvMixRatioInfo);
            tvMixingParams = itemView.findViewById(R.id.tvMixingParams);
            tvCompactionMethod = itemView.findViewById(R.id.tvCompactionMethod);
            tvMixingDevice = itemView.findViewById(R.id.tvMixingDevice);
            tvFormingDevice = itemView.findViewById(R.id.tvFormingDevice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specimen_info_step2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpecimenInfo info = specimenInfos.get(position);

        if (info.mixRatio != null) {
            holder.tvMixRatioInfo.setText(String.format("配比：%s", info.mixRatio.getName()));
        }

        if (info.moldingMethod != null) {
            holder.tvMixingParams.setText(String.format("拌合参数：温度=%.1f℃, 速度=%.1f rpm, 时间=%.1f min",
                    info.moldingMethod.getMixingTemperature(),
                    info.moldingMethod.getMixingSpeed(),
                    info.moldingMethod.getMixingTime()));
            holder.tvCompactionMethod.setText(String.format("压实方法：%s", 
                    info.moldingMethod.getCompactionMethod()));
        }

        if (info.mixingDevice != null) {
            holder.tvMixingDevice.setText(String.format("拌合设备：%s %s",
                    info.mixingDevice.getManufacturer(),
                    info.mixingDevice.getModel()));
        }

        if (info.formingDevice != null) {
            holder.tvFormingDevice.setText(String.format("压实设备：%s %s",
                    info.formingDevice.getManufacturer(),
                    info.formingDevice.getModel()));
        }
    }

    @Override
    public int getItemCount() {
        return specimenInfos.size();
    }

    public void setSpecimenInfos(List<SpecimenInfo> specimenInfos) {
        this.specimenInfos.clear();
        if (specimenInfos != null) {
            this.specimenInfos.addAll(specimenInfos);
        }
        notifyDataSetChanged();
    }

    public void addSpecimenInfo(MoldingMethod moldingMethod, MixRatio mixRatio,
                              DeviceInfo mixingDevice, DeviceInfo formingDevice) {
        specimenInfos.add(new SpecimenInfo(moldingMethod, mixRatio, mixingDevice, formingDevice));
        notifyItemInserted(specimenInfos.size() - 1);
    }

    public void setSelectedMixingDevices(List<String> selectedMixingDevices) {
        this.selectedMixingDevices = selectedMixingDevices;
    }

    public void setSelectedFormingDevices(List<String> selectedFormingDevices) {
        this.selectedFormingDevices = selectedFormingDevices;
    }
}
