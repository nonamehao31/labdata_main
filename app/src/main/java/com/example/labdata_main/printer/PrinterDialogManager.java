package com.example.labdata_main.printer;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    String ssid = wifiInfo.getSSID();
                    // 去除SSID两端的引号
                    ssid = ssid.replace("\"", "");
                    
                    if (isPrinterHotspotSSID(ssid)) {
                        Log.d(TAG, "已连接到打印机热点: " + ssid);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检测打印机网络时出错", e);
        }
        return false;
    }
    
    /**
     * 检查SSID是否可能是打印机热点
     */
    private boolean isPrinterHotspotSSID(String ssid) {
        if (ssid == null || ssid.isEmpty()) {
            return false;
        }
        
        // 转为小写进行比较
        String lowerSsid = ssid.toLowerCase();
        
        // 检查SSID是否包含打印机相关关键词
        return lowerSsid.contains("printer") || 
               lowerSsid.contains("print") ||
               lowerSsid.contains("hp") ||
               lowerSsid.contains("epson") ||
               lowerSsid.contains("canon") ||
               lowerSsid.contains("brother") ||
               lowerSsid.contains("zebra") ||
               lowerSsid.contains("thermal") ||
               lowerSsid.contains("receipt") ||
               lowerSsid.contains("pos") ||
               lowerSsid.contains("wifi-direct");
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
        // 首先检查WiFi权限
        if (!checkWifiPermission()) {
            // 如果没有权限，显示提示并返回
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("权限缺失")
                   .setMessage("搜索WiFi打印机需要WiFi访问权限，请在应用设置中授予权限。")
                   .setPositiveButton("确定", (dialog, which) -> {
                       if (callback != null) {
                           callback.onCancelled();
                       }
                   })
                   .show();
            return;
        }
        
        // 获取保存的WiFi打印机
        final List<PrinterInfo> printers = new ArrayList<>(getSavedWiFiPrinters());
        
        // 创建适配器
        PrinterListAdapter adapter = new PrinterListAdapter(printers);
        
        // 创建对话框
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择WiFi打印机");
        
        View printerDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_printer_selection, null);
        RecyclerView printerListView = printerDialogView.findViewById(R.id.printer_list);
        printerListView.setLayoutManager(new LinearLayoutManager(context));
        printerListView.setAdapter(adapter);
        
        // 添加进度指示器
        ProgressBar progressBar = printerDialogView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        
        // 添加状态文本
        TextView statusTextView = printerDialogView.findViewById(R.id.status_text);
        statusTextView.setText("正在搜索打印机...");
        statusTextView.setVisibility(View.VISIBLE);
        
        // 检查是否连接到打印机热点，显示提示信息
        boolean isPrinterHotspot = isConnectedToPrinterNetwork();
        if (isPrinterHotspot) {
            // 添加热点提示信息
            TextView hotspotTipView = new TextView(context);
            hotspotTipView.setText("已检测到打印机WiFi热点连接，系统将自动尝试连接打印机");
            hotspotTipView.setTextSize(12);
            hotspotTipView.setTextColor(Color.BLUE);
            hotspotTipView.setTypeface(null, Typeface.ITALIC);
            hotspotTipView.setPadding(16, 8, 16, 8);
            
            // 将提示添加到对话框
            LinearLayout container = (LinearLayout) printerDialogView;
            container.addView(hotspotTipView, 1);
            
            // 更新状态文本
            statusTextView.setText("检测到打印机WiFi热点，正在尝试连接...");
        }
        
        builder.setView(printerDialogView);
        
        // 创建并显示对话框
        final AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置适配器的点击监听器
        adapter.setOnItemClickListener(position -> {
            PrinterInfo selectedPrinter = printers.get(position);
            
            // 保存选择的打印机以便下次使用
            saveWiFiPrinter(selectedPrinter);
            
            // 通过回调返回选择的打印机
            dialog.dismiss();
            if (callback != null) {
                callback.onPrinterSelected(selectedPrinter);
            }
        });
        
        // 开始搜索打印机
        wifiPrinterManager.discoverPrinters(new PrinterDiscoveryManager.PrinterDiscoveryListener() {
            @Override
            public void onPrinterDiscoveryStarted() {
                Log.d("PrinterDialog", "打印机搜索开始");
            }
            
            @Override
            public void onPrinterFound(PrinterInfo printer) {
                Log.d("PrinterDialog", "找到打印机: " + printer.getName());
                
                // 更新UI线程中的适配器
                if (!dialog.isShowing()) {
                    Log.d("PrinterDialog", "对话框已关闭，忽略新找到的打印机");
                    return;
                }
                
                // 在主线程更新UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 检查打印机是否已存在
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
                        
                        // 找到打印机后，隐藏进度条
                        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.GONE);
                        }
                        
                        // 更新状态文本
                        if (statusTextView != null) {
                            statusTextView.setText("找到 " + printers.size() + " 台打印机");
                        }
                    }
                });
            }
            
            @Override
            public void onPrinterDiscoveryFinished(List<PrinterInfo> discoveredPrinters) {
                Log.d("PrinterDialog", "打印机搜索完成，找到 " + discoveredPrinters.size() + " 台打印机");
                
                if (!dialog.isShowing()) {
                    Log.d("PrinterDialog", "对话框已关闭，忽略搜索完成回调");
                    return;
                }
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 无论找到多少打印机，都确保隐藏进度条
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    // 根据找到的打印机数量更新状态文本
                    if (statusTextView != null) {
                        if (printers.isEmpty()) {
                            statusTextView.setText("未找到打印机，请确认打印机已连接到同一网络");
                        } else {
                            statusTextView.setText("找到 " + printers.size() + " 台打印机");
                        }
                    }
                    
                    // 如果找到了新的打印机，更新列表
                    for (PrinterInfo printer : discoveredPrinters) {
                        boolean exists = false;
                        for (PrinterInfo p : printers) {
                            if (p.getAddress().equals(printer.getAddress()) && p.getPort() == printer.getPort()) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists) {
                            printers.add(printer);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                });
            }
            
            @Override
            public void onDiscoveryError(String errorMessage) {
                Log.e("PrinterDialog", "打印机搜索出错: " + errorMessage);
                
                if (!dialog.isShowing()) {
                    return;
                }
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 隐藏进度条
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    // 更新状态文本
                    if (statusTextView != null) {
                        statusTextView.setText("搜索出错: " + errorMessage);
                    }
                });
            }
        });
        
        // 添加手动输入按钮
        builder.setPositiveButton("手动输入", (d, which) -> {
            showAddWiFiPrinterDialog(callback);
        });
        
        // 添加取消按钮
        builder.setNegativeButton("取消", (d, which) -> {
            d.dismiss();
            wifiPrinterManager.stopDiscovery();
        });
        
        // 对话框关闭时停止搜索
        dialog.setOnDismissListener(d -> wifiPrinterManager.stopDiscovery());
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
     * 检查是否有WiFi访问权限
     * @return 是否有权限
     */
    private boolean checkWifiPermission() {
        Log.d(TAG, "检查WiFi访问权限");
        // 检查基本WiFi权限
        boolean hasWifiPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) 
                == PackageManager.PERMISSION_GRANTED;
                
        if (!hasWifiPermission) {
            Log.e(TAG, "缺少ACCESS_WIFI_STATE权限");
            Toast.makeText(context, "缺少WiFi访问权限，无法搜索WiFi打印机", Toast.LENGTH_LONG).show();
            return false;
        }
        
        // 为获取SSID信息，在Android 10+上检查位置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            boolean hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED;
            
            if (!hasFineLocation) {
                Log.e(TAG, "Android 10及以上系统获取WiFi SSID需要位置权限，但未获得");
                Toast.makeText(context, "获取WiFi信息需要位置权限，请在应用设置中授予权限", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        
        Log.d(TAG, "已获取所需WiFi权限");
        return true;
    }
}
