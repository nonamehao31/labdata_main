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
import java.util.List;

public class QRCodePagerAdapter extends RecyclerView.Adapter<QRCodePagerAdapter.ViewHolder> {
    private static final String TAG = "QRCodePagerAdapter";
    private final List<Bitmap> qrCodes = new ArrayList<>();

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
        if (qrCodes != null) {
            this.qrCodes.addAll(qrCodes);
        }
        notifyDataSetChanged();
    }

    public void addQRCode(Bitmap qrCode) {
        if (qrCode != null) {
            Log.d(TAG, "Adding new QR code, current size: " + qrCodes.size());
            qrCodes.add(qrCode);
            notifyItemInserted(qrCodes.size() - 1);
        } else {
            Log.e(TAG, "Attempted to add null QR code");
        }
    }

    public void clearQRCodes() {
        Log.d(TAG, "Clearing all QR codes");
        qrCodes.clear();
        notifyDataSetChanged();
    }

    public Bitmap getQRCode(int position) {
        if (position >= 0 && position < qrCodes.size()) {
            return qrCodes.get(position);
        }
        return null;
    }
}
