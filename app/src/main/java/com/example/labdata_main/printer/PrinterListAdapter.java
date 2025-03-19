package com.example.labdata_main.printer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 打印机列表适配器
 */
public class PrinterListAdapter extends RecyclerView.Adapter<PrinterListAdapter.ViewHolder> {
    private final List<PrinterInfo> printers;
    private OnItemClickListener listener;
    
    public PrinterListAdapter(List<PrinterInfo> printers) {
        this.printers = printers;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrinterInfo printer = printers.get(position);
        holder.text1.setText(printer.getName());
        String subtitle = "";
        
        if (printer.getType() == PrinterInfo.TYPE_BLUETOOTH) {
            subtitle = printer.getAddress();
        } else if (printer.getType() == PrinterInfo.TYPE_WIFI) {
            subtitle = printer.getAddress() + ":" + printer.getPort();
        }
        
        holder.text2.setText(subtitle);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return printers.size();
    }
    
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
