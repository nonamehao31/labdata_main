package com.example.labdata_main.printer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 打印机对话框管理器，用于显示打印机选择和配置对话框
 */
public class PrinterDialogManager {
    private static final String TAG = "PrinterDialogManager";
    private static final String PREF_NAME = "printer_preferences";
    private static final String PREF_SAVED_WIFI_PRINTERS = "saved_wifi_printers";
    private static final String PREF_LAST_PRINTER_TYPE = "last_printer_type";
    private static final String PREF_LAST_PRINTER_ADDRESS = "last_printer_address";
    private static final String PREF_LAST_PRINTER_NAME = "last_printer_name";
    private static final String PREF_LAST_PRINTER_PORT = "last_printer_port";
    
    private final Context context;
    private final BluetoothPrinterManager bluetoothPrinterManager;
    private final WiFiPrinterManager wifiPrinterManager;
    private PrinterSelectionListener listener;
    
    public interface PrinterSelectionListener {
        void onPrinterSelected(PrinterManager manager, PrinterInfo printer);
        void onCancelled();
    }
    
    /**
     * 打印机选择回调接口
     */
    public interface PrinterSelectionCallback {
        void onPrinterSelected(PrinterInfo printer);
        void onCancelled();
    }
    
    /**
     * 构造函数
     * @param context 上下文
     */
    public PrinterDialogManager(Context context) {
        this.context = context;
        this.bluetoothPrinterManager = new BluetoothPrinterManager(context);
        this.wifiPrinterManager = new WiFiPrinterManager(context);
    }
    
    /**
     * 显示打印机类型选择对话框
     * @param bluetoothPrinterManager 蓝牙打印机管理器
     * @param wifiPrinterManager WiFi打印机管理器
     * @param callback 选择回调
     */
    public void showPrinterSelectionDialog(BluetoothPrinterManager bluetoothPrinterManager, 
                                          WiFiPrinterManager wifiPrinterManager,
                                          PrinterSelectionCallback callback) {
        // 首先检查是否有上次使用的打印机
        PrinterInfo lastPrinter = getLastSelectedPrinter();
        if (lastPrinter != null) {
            // 如果有上次使用的打印机，尝试直接连接
            Log.d(TAG, "使用上次选择的打印机: " + lastPrinter.getName());
            
            // 根据打印机类型选择管理器
            if (lastPrinter.getType() == PrinterInfo.TYPE_BLUETOOTH) {
                // 检查设备是否仍然配对
                BluetoothDevice device = bluetoothPrinterManager.getDeviceByAddress(lastPrinter.getAddress());
                if (device != null) {
                    callback.onPrinterSelected(lastPrinter);
                    return;
                }
            } else if (lastPrinter.getType() == PrinterInfo.TYPE_WIFI) {
                // 直接使用上次的WiFi打印机
                callback.onPrinterSelected(lastPrinter);
                return;
            }
        }
        
        // 检查是否连接到打印机的WiFi网络
        if (isConnectedToPrinterNetwork()) {
            // 如果已连接到打印机网络，直接显示WiFi打印机选择/发现对话框
            showWiFiPrinterSelectionDialog(wifiPrinterManager, callback);
            return;
        }
        
        // 否则，显示打印机类型选择对话框
        String[] items = {"蓝牙打印机", "WiFi打印机"};
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("选择打印机类型")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: // 蓝牙打印机
                            showBluetoothPrinterSelectionDialog(bluetoothPrinterManager, callback);
                            break;
                        case 1: // WiFi打印机
                            showWiFiPrinterSelectionDialog(wifiPrinterManager, callback);
                            break;
                    }
                })
                .show();
    }
    
    /**
     * 检查是否已连接到打印机网络
     * 这是一个启发式方法，尝试根据网络名称猜测是否连接到打印机
     * @return 如果连接到打印机网络返回true
     */
    private boolean isConnectedToPrinterNetwork() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) 
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected() && 
                        networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    
                    WifiManager wifiManager = (WifiManager) 
                            context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    
                    if (wifiManager != null) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID().replace("\"", "");
                        
                        // 检查SSID是否包含打印机相关名称（启发式方法）
                        String[] printerKeywords = {"print", "打印", "printer", "HP", "Canon", "Epson", 
                                "Brother", "Zebra", "TSC", "Honeywell"};
                        
                        for (String keyword : printerKeywords) {
                            if (ssid.toLowerCase().contains(keyword.toLowerCase())) {
                                Log.d(TAG, "检测到可能的打印机网络: " + ssid);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查打印机网络时出错", e);
        }
        
        return false;
    }
    
    /**
     * 获取上次选择的打印机信息
     * @return 上次选择的打印机信息
     */
    private PrinterInfo getLastSelectedPrinter() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int lastType = prefs.getInt(PREF_LAST_PRINTER_TYPE, PrinterInfo.TYPE_BLUETOOTH);
        String lastAddress = prefs.getString(PREF_LAST_PRINTER_ADDRESS, "");
        String lastName = prefs.getString(PREF_LAST_PRINTER_NAME, "");
        
        if (lastType == PrinterInfo.TYPE_BLUETOOTH) {
            return PrinterInfo.fromBluetooth(lastName, lastAddress);
        } else if (lastType == PrinterInfo.TYPE_WIFI) {
            // WiFi打印机需要端口号
            int port = prefs.getInt(PREF_LAST_PRINTER_PORT, 9100);
            return PrinterInfo.fromWiFi(lastName, lastAddress, port);
        }
        
        return null;
    }
    
    /**
     * 显示蓝牙打印机选择对话框
     * @param bluetoothPrinterManager 蓝牙打印机管理器
     * @param callback 选择回调
     */
    private void showBluetoothPrinterSelectionDialog(BluetoothPrinterManager bluetoothPrinterManager, 
                                                     PrinterSelectionCallback callback) {
        List<PrinterInfo> pairedPrinters = bluetoothPrinterManager.getPairedPrinters();
        
        if (pairedPrinters.isEmpty()) {
            new MaterialAlertDialogBuilder(context)
                .setTitle("没有配对的蓝牙打印机")
                .setMessage("请先在系统设置中配对蓝牙打印机，或使用WiFi打印机")
                .setPositiveButton("使用WiFi打印机", (dialog, which) -> showWiFiPrinterSelectionDialog(wifiPrinterManager, callback))
                .setNegativeButton("取消", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancelled();
                    }
                })
                .show();
            return;
        }
        
        // 转换为打印机名称数组用于显示
        final String[] printerNames = new String[pairedPrinters.size()];
        for (int i = 0; i < pairedPrinters.size(); i++) {
            PrinterInfo info = pairedPrinters.get(i);
            printerNames[i] = info.getName() != null ? info.getName() : "未知设备 (" + info.getAddress() + ")";
        }
        
        // 获取上次使用的打印机地址
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastAddress = prefs.getString(PREF_LAST_PRINTER_ADDRESS, "");
        
        // 找到上次使用的打印机的索引
        int defaultSelection = 0;
        for (int i = 0; i < pairedPrinters.size(); i++) {
            if (pairedPrinters.get(i).getAddress().equals(lastAddress)) {
                defaultSelection = i;
                break;
            }
        }
        
        new MaterialAlertDialogBuilder(context)
            .setTitle("选择蓝牙打印机")
            .setSingleChoiceItems(printerNames, defaultSelection, (dialog, which) -> {
                // 保存所选打印机信息
                PrinterInfo selectedPrinter = pairedPrinters.get(which);
                saveSelectedPrinter(selectedPrinter);
                
                if (listener != null) {
                    listener.onPrinterSelected(bluetoothPrinterManager, selectedPrinter);
                }
                dialog.dismiss();
            })
            .setNeutralButton("使用WiFi打印机", (dialog, which) -> showWiFiPrinterSelectionDialog(wifiPrinterManager, callback))
            .setNegativeButton("取消", (dialog, which) -> {
                if (listener != null) {
                    listener.onCancelled();
                }
            })
            .show();
    }
    
    /**
     * 显示WiFi打印机选择对话框
     * @param wifiPrinterManager WiFi打印机管理器
     * @param callback 选择回调
     */
    private void showWiFiPrinterSelectionDialog(WiFiPrinterManager wifiPrinterManager, 
                                                PrinterSelectionCallback callback) {
        // 创建对话框
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("选择WiFi打印机");
        
        // 获取保存的打印机列表
        final List<PrinterInfo> printers = new ArrayList<>(getSavedWiFiPrinters());
        
        // 创建视图
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_printer_selection, null);
        RecyclerView recyclerView = view.findViewById(R.id.rvPrinters);
        ProgressBar progressBar = view.findViewById(R.id.progressDiscovery);
        TextView tvStatus = view.findViewById(R.id.tvDiscoveryStatus);
        Button btnManualAdd = view.findViewById(R.id.btnAddManually);
        
        // 设置适配器
        PrinterListAdapter adapter = new PrinterListAdapter(printers);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        
        // 创建对话框
        AlertDialog dialog = builder.setView(view)
                .setNegativeButton("取消", (d, which) -> d.dismiss())
                .create();
        
        // 添加主线程处理器
        Handler mainHandler = new Handler(Looper.getMainLooper());
        
        // 自动发现打印机
        tvStatus.setText("正在搜索网络打印机...");
        progressBar.setVisibility(View.VISIBLE);
        
        wifiPrinterManager.discoverPrinters(new PrinterDiscoveryManager.PrinterDiscoveryListener() {
            @Override
            public void onPrinterDiscoveryStarted() {
                mainHandler.post(() -> {
                    tvStatus.setText("正在搜索网络打印机...");
                    progressBar.setVisibility(View.VISIBLE);
                });
            }
            
            @Override
            public void onPrinterFound(PrinterInfo printer) {
                mainHandler.post(() -> {
                    // 避免重复添加
                    boolean exists = false;
                    for (PrinterInfo p : printers) {
                        if (p.getAddress().equals(printer.getAddress()) && p.getPort() == printer.getPort()) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists) {
                        printers.add(printer);
                        adapter.notifyDataSetChanged();
                        
                        // 保存到SharedPreferences
                        saveWiFiPrinter(printer);
                    }
                    
                    tvStatus.setText("找到 " + printers.size() + " 台打印机");
                });
            }
            
            @Override
            public void onPrinterDiscoveryFinished(List<PrinterInfo> discoveredPrinters) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (printers.isEmpty()) {
                        tvStatus.setText("未找到打印机，请尝试手动添加或确认打印机已开启");
                    } else {
                        tvStatus.setText("找到 " + printers.size() + " 台打印机");
                    }
                });
            }
            
            @Override
            public void onDiscoveryError(String errorMessage) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("搜索出错: " + errorMessage);
                });
            }
        });
        
        // 设置手动添加按钮
        btnManualAdd.setOnClickListener(v -> {
            dialog.dismiss();
            showAddWiFiPrinterDialog(callback);
        });
        
        // 设置列表项点击事件
        adapter.setOnItemClickListener(position -> {
            PrinterInfo selectedPrinter = printers.get(position);
            dialog.dismiss();
            wifiPrinterManager.stopDiscovery();
            
            // 保存选择的打印机
            saveSelectedPrinter(selectedPrinter);
            
            // 回调选择结果
            callback.onPrinterSelected(selectedPrinter);
        });
        
        dialog.setOnDismissListener(d -> wifiPrinterManager.stopDiscovery());
        dialog.show();
    }
    
    /**
     * 显示添加WiFi打印机对话框
     * @param callback 选择回调
     */
    private void showAddWiFiPrinterDialog(PrinterSelectionCallback callback) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wifi_printer, null);
        
        final EditText etPrinterName = dialogView.findViewById(R.id.et_printer_name);
        final EditText etPrinterIp = dialogView.findViewById(R.id.et_printer_ip);
        final EditText etPrinterPort = dialogView.findViewById(R.id.et_printer_port);
        
        // 默认端口为9100
        etPrinterPort.setText("9100");
        
        new MaterialAlertDialogBuilder(context)
            .setTitle("添加WiFi打印机")
            .setView(dialogView)
            .setPositiveButton("添加", (dialog, which) -> {
                String name = etPrinterName.getText().toString().trim();
                String ip = etPrinterIp.getText().toString().trim();
                String portStr = etPrinterPort.getText().toString().trim();
                
                if (name.isEmpty() || ip.isEmpty() || portStr.isEmpty()) {
                    Toast.makeText(context, "请输入完整的打印机信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                int port;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "端口号无效", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 创建新的打印机信息
                PrinterInfo newPrinter = PrinterInfo.fromWiFi(name, ip, port);
                
                // 保存打印机信息
                saveWiFiPrinter(newPrinter);
                saveSelectedPrinter(newPrinter);
                
                if (listener != null) {
                    listener.onPrinterSelected(wifiPrinterManager, newPrinter);
                }
            })
            .setNegativeButton("取消", (dialog, which) -> showWiFiPrinterSelectionDialog(wifiPrinterManager, callback))
            .show();
    }
    
    /**
     * 获取保存的WiFi打印机列表
     * @return 打印机信息列表
     */
    private List<PrinterInfo> getSavedWiFiPrinters() {
        List<PrinterInfo> printers = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> savedPrinters = prefs.getStringSet(PREF_SAVED_WIFI_PRINTERS, new HashSet<>());
        
        for (String printerStr : savedPrinters) {
            String[] parts = printerStr.split("\\|");
            if (parts.length >= 3) {
                String name = parts[0];
                String ip = parts[1];
                int port = Integer.parseInt(parts[2]);
                printers.add(PrinterInfo.fromWiFi(name, ip, port));
            }
        }
        
        return printers;
    }
    
    /**
     * 保存WiFi打印机信息
     * @param printer 打印机信息
     */
    private void saveWiFiPrinter(PrinterInfo printer) {
        if (printer.getType() != PrinterInfo.TYPE_WIFI) {
            return;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> savedPrinters = new HashSet<>(prefs.getStringSet(PREF_SAVED_WIFI_PRINTERS, new HashSet<>()));
        
        // 格式: 名称|IP|端口
        String printerStr = printer.getName() + "|" + printer.getAddress() + "|" + printer.getPort();
        savedPrinters.add(printerStr);
        
        prefs.edit().putStringSet(PREF_SAVED_WIFI_PRINTERS, savedPrinters).apply();
    }
    
    /**
     * 保存所选打印机信息
     * @param printer 打印机信息
     */
    private void saveSelectedPrinter(PrinterInfo printer) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt(PREF_LAST_PRINTER_TYPE, printer.getType());
        editor.putString(PREF_LAST_PRINTER_ADDRESS, printer.getAddress());
        editor.putString(PREF_LAST_PRINTER_NAME, printer.getName());
        
        if (printer.getType() == PrinterInfo.TYPE_WIFI) {
            editor.putInt(PREF_LAST_PRINTER_PORT, printer.getPort());
        }
        
        editor.apply();
    }
}
