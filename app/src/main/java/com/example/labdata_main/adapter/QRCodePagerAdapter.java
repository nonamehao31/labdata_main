package com.example.labdata_main.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.labdata_main.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRCodePagerAdapter extends RecyclerView.Adapter<QRCodePagerAdapter.ViewHolder> {
    private static final String TAG = "QRCodePagerAdapter";
    private final List<Bitmap> qrCodes = new ArrayList<>();
    private final Map<Integer, String> qrContents = new HashMap<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivQRCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQRCode = itemView.findViewById(R.id.ivQRCode);
        }

        public void bind(Bitmap qrCode) {
            if (qrCode != null) {
                Log.d(TAG, "Setting QR code bitmap to ImageView");
                ivQRCode.setImageBitmap(qrCode);
            } else {
                Log.e(TAG, "QR code bitmap is null");
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating new ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qr_code_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding ViewHolder at position " + position);
        Bitmap qrCode = qrCodes.get(position);
        holder.bind(qrCode);
    }

    @Override
    public int getItemCount() {
        return qrCodes.size();
    }

    public void setQRCodes(List<Bitmap> qrCodes) {
        Log.d(TAG, "Setting " + (qrCodes != null ? qrCodes.size() : 0) + " QR codes");
        this.qrCodes.clear();
        this.qrContents.clear();
        if (qrCodes != null) {
            this.qrCodes.addAll(qrCodes);
        }
        notifyDataSetChanged();
    }

    public void addQRCode(Bitmap qrCode, String content) {
        if (qrCode != null) {
            int position = qrCodes.size();
            Log.d(TAG, "Adding new QR code at position " + position + ", content: " + content);
            qrCodes.add(qrCode);
            qrContents.put(position, content);
            notifyItemInserted(position);
        } else {
            Log.e(TAG, "Attempted to add null QR code");
        }
    }
    
    // 保留旧方法以兼容现有代码
    public void addQRCode(Bitmap qrCode) {
        addQRCode(qrCode, null);
    }

    public void clearQRCodes() {
        Log.d(TAG, "Clearing all QR codes");
        qrCodes.clear();
        qrContents.clear();
        notifyDataSetChanged();
    }

    public Bitmap getQRCode(int position) {
        if (position >= 0 && position < qrCodes.size()) {
            return qrCodes.get(position);
        }
        return null;
    }
    
    public String getQRContent(int position) {
        if (position >= 0 && position < qrCodes.size()) {
            return qrContents.get(position);
        }
        return null;
    }
}
