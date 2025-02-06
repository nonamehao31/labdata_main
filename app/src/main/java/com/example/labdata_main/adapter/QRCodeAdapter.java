package com.example.labdata_main.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import com.example.labdata_main.model.Equipment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONObject;
import java.util.ArrayList;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {
    private final ArrayList<Equipment> equipmentList;

    public QRCodeAdapter(ArrayList<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qr_code, parent, false);
        return new QRCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        
        // 设置设备名称
        String deviceType = getDeviceTypeName(equipment.getType());
        holder.tvDeviceName.setText(deviceType);

        // 设置设备详细信息
        String deviceInfo = String.format("厂家：%s\n型号：%s\n购买年份：%s",
            equipment.getManufacturer(),
            equipment.getModel(),
            equipment.getPurchaseYear()
        );
        holder.tvDeviceInfo.setText(deviceInfo);

        try {
            // 创建包含设备信息的JSON对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", equipment.getType());
            jsonObject.put("manufacturer", equipment.getManufacturer());
            jsonObject.put("model", equipment.getModel());
            jsonObject.put("purchaseYear", equipment.getPurchaseYear());

            // 生成二维码
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(
                jsonObject.toString(),
                BarcodeFormat.QR_CODE,
                280, // 使用布局中定义的尺寸
                280
            );
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            holder.ivQRCode.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDeviceTypeName(String type) {
        switch (type) {
            case "MIXING":
                return "拌合设备";
            case "FORMING":
                return "制件设备";
            case "TESTING":
                return "实验设备";
            default:
                return "未知设备";
        }
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class QRCodeViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        ImageView ivQRCode;
        TextView tvDeviceInfo;

        QRCodeViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            ivQRCode = itemView.findViewById(R.id.ivQRCode);
            tvDeviceInfo = itemView.findViewById(R.id.tvDeviceInfo);
        }
    }
}
