package com.example.labdata_main.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.DeviceInfo;
import com.example.labdata_main.model.ExperimentDataItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ExperimentDataAdapter extends RecyclerView.Adapter<ExperimentDataAdapter.ViewHolder> {
    private List<ExperimentDataItem> items;
    private OnScanDeviceClickListener scanDeviceClickListener;

    public interface OnScanDeviceClickListener {
        void onScanDeviceClick(int position);
    }

    public ExperimentDataAdapter(List<ExperimentDataItem> items, OnScanDeviceClickListener listener) {
        this.items = items;
        this.scanDeviceClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExperimentDataItem item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<ExperimentDataItem> getItems() {
        return items;
    }

    public void updateDeviceInfo(int position, DeviceInfo deviceInfo) {
        if (position >= 0 && position < items.size()) {
            items.get(position).setDeviceInfo(deviceInfo);
            notifyItemChanged(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExperimentName;
        private final TextInputLayout input1Layout;
        private final TextInputLayout input2Layout;
        private final TextInputEditText input1;
        private final TextInputEditText input2;
        private final TextView tvDeviceInfo;
        private final MaterialButton btnScanDevice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            input1Layout = itemView.findViewById(R.id.input1Layout);
            input2Layout = itemView.findViewById(R.id.input2Layout);
            input1 = itemView.findViewById(R.id.input1);
            input2 = itemView.findViewById(R.id.input2);
            tvDeviceInfo = itemView.findViewById(R.id.tvDeviceInfo);
            btnScanDevice = itemView.findViewById(R.id.btnScanDevice);
        }

        public void bind(ExperimentDataItem item, int position) {
            tvExperimentName.setText(item.getExperimentName());
            
            // 设置第一个输入框
            input1Layout.setHint(item.getInput1Label());
            input1Layout.setVisibility(View.VISIBLE);
            input1.setText(item.getInput1Value() != 0 ? String.valueOf(item.getInput1Value()) : "");
            
            // 设置第二个输入框
            if (item.hasSecondInput()) {
                input2Layout.setHint(item.getInput2Label());
                input2Layout.setVisibility(View.VISIBLE);
                input2.setText(item.getInput2Value() != 0 ? String.valueOf(item.getInput2Value()) : "");
            } else {
                input2Layout.setVisibility(View.GONE);
            }

            // 设置设备信息
            if (item.hasDevice()) {
                DeviceInfo deviceInfo = item.getDeviceInfo();
                tvDeviceInfo.setText(String.format("%s\n%s", deviceInfo.getName(), deviceInfo.getDeviceId()));
            } else {
                tvDeviceInfo.setText("未选择设备");
            }

            // 设置扫描按钮点击事件
            btnScanDevice.setOnClickListener(v -> {
                if (scanDeviceClickListener != null) {
                    scanDeviceClickListener.onScanDeviceClick(position);
                }
            });

            // 添加输入监听器
            input1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && !s.toString().isEmpty()) {
                        try {
                            double value = Double.parseDouble(s.toString());
                            item.setInput1Value(value);
                        } catch (NumberFormatException e) {
                            input1.setError("请输入有效的数值");
                        }
                    }
                }
            });

            input2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && !s.toString().isEmpty()) {
                        try {
                            double value = Double.parseDouble(s.toString());
                            item.setInput2Value(value);
                        } catch (NumberFormatException e) {
                            input2.setError("请输入有效的数值");
                        }
                    }
                }
            });
        }
    }
}
